
package com.utopios.scaffolding;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.OwnerPIN;
import javacard.security.*;

public class Scaffolding extends Applet {
    
    private KeyPair myKeyPair;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    /**
     * Only this class's install method should create the applet object.
     */
    protected Scaffolding() {
        
        try {

        myKeyPair = new KeyPair(KeyPair.ALG_RSA, KeyBuilder.LENGTH_RSA_2048);
        myKeyPair.genKeyPair();
        privateKey = (RSAPrivateKey) myKeyPair.getPrivate();
        publicKey = (RSAPublicKey) myKeyPair.getPublic();
        }catch(Exception ex) {
            ISOException.throwIt((short) 0x9002);
        }
        
        
        register();
    }

    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        
        new Scaffolding();
    }

   
   @Override 
   public void process(APDU apdu) {
       
    byte[] buffer = apdu.getBuffer();
        if (buffer[ISO7816.OFFSET_INS] == (byte) 0x20) { // Exemple d'instruction
            
            //Modulus
            short modLen = publicKey.getModulus(buffer, (short) 4);
            
            //Taille modulus
            Util.setShort(buffer, (short)0, modLen);

            short expLen = publicKey.getExponent(buffer, (short) (4 + modLen));
            Util.setShort(buffer, (short) 2, expLen);
            short totalLen = (short) (4 + modLen + expLen);
            apdu.setOutgoing();
            apdu.setOutgoingLength((short) totalLen);
            ISOException.throwIt((short) 0x9001);
            apdu.sendBytes((short) 0, totalLen);
        } else if(buffer[ISO7816.OFFSET_INS] == (byte) 0x10) { 
           
           
            //Modulus
            short modLen = privateKey.getModulus(buffer, (short) 4);
            
            //Taille modulus
            Util.setShort(buffer, (short)0, modLen);

            short expLen = privateKey.getExponent(buffer, (short) (4 + modLen));
            Util.setShort(buffer, (short) 2, expLen);
            short totalLen = (short) (4 + modLen + expLen);
            apdu.setOutgoing();
            apdu.setOutgoingLength((short) totalLen);
            
            apdu.sendBytes((short) 0, totalLen);
            ISOException.throwIt((short) 0x9000);
        }       
    }

    
    
}
