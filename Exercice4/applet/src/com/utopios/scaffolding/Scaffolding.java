
package com.utopios.scaffolding;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.OwnerPIN;
import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.Cipher;

public class Scaffolding extends Applet {
    
    
    private static final byte INS_GET_EC_PUBLIC = (byte) 0x10;
    private static final byte INS_ECDH = (byte) 0x20;
    private static final byte INS_SIGN_CHALLENGE = (byte) 0x30;
    private static final byte INS_VERIFY_SIGNATURE = (byte) 0x40;

    private ECPrivateKey ecPrivateKey;
    private ECPublicKey ecPublicKey;
    private KeyAgreement keyAgreement;

    private AESKey sessionKey;
    private Signature ecdsaSigner;
    private byte[] tempBuffer;
    /**
     * Only this class's install method should create the applet object.
     */
    protected Scaffolding() {
        KeyPair ecKeyPair = new KeyPair(KeyPair.ALG_EC_FP, KeyBuilder.LENGTH_EC_FP_256);
        ecKeyPair.genKeyPair();
        ecPrivateKey = (ECPrivateKey) ecKeyPair.getPrivate();
        ecPublicKey = (ECPublicKey) ecKeyPair.getPublic();
        keyAgreement = KeyAgreement.getInstance(KeyAgreement.ALG_EC_SVDP_DH, false);
        sessionKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        ecdsaSigner = Signature.getInstance(Signature.ALG_ECDSA_SHA_256, false);
        tempBuffer = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_DESELECT);
        
        register();
    }

    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        
        new Scaffolding();
    }

   
   @Override 
   public void process(APDU apdu) {
       byte[] buffer = apdu.getBuffer();

       
        if (selectingApplet()) {
            return;
        }

        
        switch (buffer[ISO7816.OFFSET_INS]) {
            case INS_WRITE_DATA:
                writeData(apdu);
                break;

            case INS_READ_DATA:
                readData(apdu);
                break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    
   }

   private void sendPublicKey(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short pubKeyLength = ecPublicKey.getW(buffer, (short) 0);
        apdu.setOutgoing();
        apdu.setOutgoingLength(pubKeyLength);
        apdu.sendBytes((short) 0, pubKeyLength);
    }

    //Clacule du secret partagé avec la clé public reçue par le terminal

    private void computeSharedSecret(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short receivedLength = apdu.setIncomingAndReceive();
        keyAgreement.init(ecPrivateKey);
        short sharedSecretLength = keyAgreement.generateSecret(buffer, ISO7816.OFFSET_CDATA, receivedLength, tempBuffer, (short) 0);
        sessionKey.setKey(tempBuffer, (short) 0);
        ISOException.throwIt(ISO7816.SW_NO_ERROR);
    }

    private void signChallenge(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short challengeLength = apdu.setIncomingAndReceive();
        ecdsaSigner.init(ecPrivateKey, Signature.MODE_SIGN);
        short signatureLength = ecdsaSigner.sign(buffer, ISO7816.OFFSET_CDATA, challengeLength, buffer, (short) 0);
        apdu.setOutgoing();
        apdu.setOutgoingLength(signatureLength);
        apdu.sendBytes((short) 0, signatureLength);
    }

    private void verifySignature(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short dataLength = apdu.setIncomingAndReceive();
        short dataLengthField = buffer[ISO7816.OFFSET_CDATA];
        short signatureOffset = (short) (ISO7816.OFFSET_CDATA + 1 + dataLengthField);
        ECPublicKey terminalPublicKey = (ECPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PUBLIC, KeyBuilder.LENGTH_EC_FP_256, false);
        terminalPublicKey.setW(buffer, signatureOffset);
        ecdsaSigner.init(terminalPublicKey, Signature.MODE_VERIFY);
        boolean isVerified = ecdsaSigner.verify(buffer, (short) (ISO7816.OFFSET_CDATA + 1), dataLengthField,
                                                buffer, signatureOffset, (short) (dataLength - signatureOffset));
        if (!isVerified) {
            ISOException.throwIt((short) 0x6A80);
        }
        ISOException.throwIt(ISO7816.SW_NO_ERROR);
    }
}
