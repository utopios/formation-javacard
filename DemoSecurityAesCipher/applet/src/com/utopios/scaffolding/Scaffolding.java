
package com.utopios.scaffolding;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.OwnerPIN;
import javacard.framework.JCSystem;
import javacard.security.*;
import javacardx.crypto.Cipher;

public class Scaffolding extends Applet {
    
    private static final byte INS_WRITE_DATA = (byte) 0x30;
    private static final byte INS_READ_DATA = (byte) 0x40; 

    private byte[] secureData; 
    private short secureDataLength;

    private AESKey aesKey;
    private Cipher aesCipher;
    /**
     * Only this class's install method should create the applet object.
     */
    protected Scaffolding() {
        
        secureData = new byte[256];
        secureDataLength = 0; 

        aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        byte[] keyData = JCSystem.makeTransientByteArray((short) 16, JCSystem.CLEAR_ON_DESELECT);
        for (byte i = 0; i < 16; i++) {
            keyData[i] = (byte) (i + 1);
        }
        aesKey.setKey(keyData, (short) 0);
        aesCipher = Cipher.getInstance(Cipher.ALG_AES_CBC_PKCS5, false);
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

   private void writeData(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short bytesRead = apdu.setIncomingAndReceive();

        
        if (bytesRead > 256) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        
        aesCipher.init(aesKey, Cipher.MODE_ENCRYPT);
        aesCipher.doFinal(buffer, ISO7816.OFFSET_CDATA, bytesRead, secureData, (short) 0);

        
        secureDataLength = bytesRead;

        
        ISOException.throwIt(ISO7816.SW_NO_ERROR);
    }

    private void readData(APDU apdu) {
        byte[] buffer = apdu.getBuffer();

        
        if (secureDataLength == 0) {
            ISOException.throwIt((short) 0x6A84);
        }

        Util.arrayCopyNonAtomic(secureData, (short) 0, buffer, (short) 0, secureDataLength);

        
        apdu.setOutgoing();
        apdu.setOutgoingLength(secureDataLength);
        apdu.sendBytes((short) 0, secureDataLength);
    }
}
