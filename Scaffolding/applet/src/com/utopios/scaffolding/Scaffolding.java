
package com.utopios.scaffolding;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.OwnerPIN;

public class Scaffolding extends Applet {
    
    //private byte[] buffer;

    //Object pour gérer pin
    private OwnerPIN pin;
    private static final byte PIN_MAX_TRIES = 3;
    private static final byte PIN_SIZE = 4; 

    //Exercice 1

    private static final byte CLA_PROPRIETAIRE = (byte)0x80;
    private static final byte INS_ECHO = (byte)0x20;
    private static final byte INS_HELLO = (byte)0x10;
    private static final byte INS_INCREMENT = (byte)0x30;
    private static final byte[] HELLO = {(byte)'h', (byte)'e', (byte)'l', (byte)'l', (byte)'o'};

    private static final byte INS_VERIFY_PIN = (byte)0x21;
    private short counter;

    /**
     * Only this class's install method should create the applet object.
     */
    protected Scaffolding() {
        
        //Initilisation du PIN
        pin = new OwnerPIN(PIN_MAX_TRIES, PIN_SIZE);
        byte[] defaultPin = {'1', '2', '3', '4'};
        pin.update(defaultPin, (short) 0, (byte) 4);
        register();
    }

    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Scaffolding();
    }

   
   @Override 
   public void process(APDU apdu) {
       
        exercice1(apdu);
        /** 
        byte[] buffer = apdu.getBuffer();
        if(selectingApplet()) {
            ISOException.throwIt((short) 0x9000);
            return;
        }   
        switch(buffer[ISO7816.OFFSET_INS]) {
            case 0x10:
                handle10(apdu);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }*/

       //Vérification du pin
       byte[] buffer = apdu.getBuffer();
       byte lc = buffer[ISO7816.OFFSET_LC];
       short dataOffset = ISO7816.OFFSET_CDATA;
       if(lc < PIN_SIZE || lc > PIN_SIZE) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
       }
       if(pin.getTriesRemaining() == 0) {
        //
       }
       if(!pin.check(buffer, dataOffset, lc)) {
        ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
       }

        
    }

    private void exercice1(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        if(selectingApplet()) {
            return;
        } 
        byte cla = buffer[ISO7816.OFFSET_CLA];
        byte ins = buffer[ISO7816.OFFSET_INS];

        if(cla != CLA_PROPRIETAIRE) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        switch(ins) {
            case INS_HELLO:
                handleInsHello(apdu);
                break;
            case INS_ECHO:
                handleInsEcho(apdu);
                break;
            case INS_INCREMENT:
                handleInsIncrement(apdu);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }

    }

    private void handle10(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte[] hello = {(byte)'h', (byte)'e', (byte)'l', (byte)'l', (byte)'o'};
        Util.arrayCopyNonAtomic(hello, (short)0, buffer, (short) 0, (short) hello.length);
        apdu.setOutgoingAndSend((short) 0, (short) hello.length);
    }

    private void handleInsHello(APDU apdu) {
         byte[] buffer = apdu.getBuffer();
        Util.arrayCopyNonAtomic(HELLO, (short)0, buffer, (short) 0, (short) HELLO.length);
        apdu.setOutgoingAndSend((short) 0, (short) HELLO.length);
    } 

    private void handleInsIncrement(APDU apdu) {
         byte[] buffer = apdu.getBuffer();
        counter++;
        buffer[0]  = (byte) (counter >> 8);
        buffer[1] = (byte) counter;
        apdu.setOutgoingAndSend((short)0, (short) 2);
    }
    
    private void handleInsEcho(APDU apdu) {
         byte[] buffer = apdu.getBuffer();
        short dataLength = apdu.setIncomingAndReceive();
        Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_CDATA, buffer, (short)0, dataLength);
        apdu.setOutgoingAndSend((short)0, dataLength);
    }
    
}
