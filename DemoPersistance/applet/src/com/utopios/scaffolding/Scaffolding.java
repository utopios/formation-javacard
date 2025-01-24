
package com.utopios.scaffolding;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.OwnerPIN;
import javacard.framework.JCSystem;

public class Scaffolding extends Applet {
    
    private static final byte INS_GET_COUNTER = (byte)0x10;
    private static final byte INS_INCREMENT = (byte) 0x20;

    // Persistance en EPPROM
    private short counter;

    private byte[] tempBuffer;

    /**
     * Only this class's install method should create the applet object.
     */
    protected Scaffolding() {
        
        counter = 0;
        tempBuffer = JCSystem.makeTransientByteArray((short)64, JCSystem.CLEAR_ON_RESET);
        register();
        
    }

    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Scaffolding();
    }

   
   @Override 
   public void process(APDU apdu) {
        byte[] buffer  = apdu.getBuffer();
        byte ins = buffer[ISO7816.OFFSET_INS];
        switch(ins) {
            case INS_GET_COUNTER:
                Util.setShort(buffer, (short)0, counter);
                apdu.setOutgoingAndSend((short)0, (short)2);
                break;
            case INS_INCREMENT:
                
                JCSystem.beginTransaction();

                try {
                    counter++;
                    JCSystem.commitTransaction();
                }catch(Exception ex) {
                    JCSystem.abortTransaction();
                }

                break;
        }   
    }

    // public void deselect() {
    //     JCSystem.beginTransaction();

    //     try {
    //         //COPY

    //         JCSystem.commitTransaction();
    //     }catch(Exception ex) {
    //         JCSystem.abortTransaction();
    //     }
    // }
    
    
}
