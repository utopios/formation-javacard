
package com.utopios.scaffolding;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.OwnerPIN;
import javacard.framework.JCSystem;

public class Scaffolding extends Applet {
    
    private static final byte INS_GET_BALANCE   = (byte) 0x30;
    private static final byte INS_CREDIT        = (byte) 0x32;
    private static final byte INS_DEBIT         = (byte) 0x34;
    private static final byte INS_RESET_BALANCE = (byte) 0x36; 

    // Champ persistant : stocké en EEPROM
    private short balance;

    private short[] tempBuffer;


    /**
     * Only this class's install method should create the applet object.
     */
    protected Scaffolding() {
        
        balance = 0;

        tempBuffer = JCSystem.makeTransientShortArray((short) 64, JCSystem.CLEAR_ON_DESELECT);
        register();
        
    }

    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Scaffolding();
    }

    public boolean select() {
        tempBuffer[0] = balance;
        return true;
    }
   
   @Override 
   public void process(APDU apdu) {
         if (selectingApplet()) {
            return;
        }

        byte[] buffer = apdu.getBuffer();
        byte ins = buffer[ISO7816.OFFSET_INS];

        switch (ins) {
            case INS_GET_BALANCE:
                getBalance(apdu);
                break;

            case INS_CREDIT:
                credit(apdu);
                break;

            case INS_DEBIT:
                debit(apdu);
                break;

            case INS_RESET_BALANCE:
                resetBalance(apdu);
                break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }  
    }

     private void getBalance(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        Util.setShort(buffer, (short) 0, balance);

        apdu.setOutgoingAndSend((short) 0, (short) 2);
    }

    private void credit(APDU apdu) {
        byte[] buffer = apdu.getBuffer();

        short bytesRead = apdu.setIncomingAndReceive();
        if (bytesRead != 2) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }

        short amount = Util.getShort(buffer, ISO7816.OFFSET_CDATA);

        if (amount < 0) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }

        JCSystem.beginTransaction();
        try {
            balance += amount;
            tempBuffer[0] += amount;
            JCSystem.commitTransaction();
        } catch (Exception e) {
            JCSystem.abortTransaction();
            ISOException.throwIt((short) 0x6FFF);
        }
    }

    private void debit(APDU apdu) {
        byte[] buffer = apdu.getBuffer();

        short bytesRead = apdu.setIncomingAndReceive();
        if (bytesRead != 2) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }

        short amount = Util.getShort(buffer, ISO7816.OFFSET_CDATA);

        if (amount < 0) {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }

        if ((short)(balance - amount) < 0) {
            ISOException.throwIt((short) 0x6900); 
        }

        JCSystem.beginTransaction();
        try {
            balance -= amount;
            tempBuffer[0] -= amount;
            JCSystem.commitTransaction();
        } catch (Exception e) {
            JCSystem.abortTransaction();
            ISOException.throwIt((short) 0x6FFF);
        }
    }

    private void resetBalance(APDU apdu) {
        JCSystem.beginTransaction();
        try {
            balance = 0;
            tempBuffer[0] = 0;
            JCSystem.commitTransaction();
        } catch (Exception e) {
            JCSystem.abortTransaction();
            ISOException.throwIt((short) 0x6FFF);
        }
    }


    public void deselect() {
        JCSystem.beginTransaction();
        try {
            balance = tempBuffer[0];
            JCSystem.commitTransaction();
        }catch(Exception ex) {
            JCSystem.abortTransaction();
            ISOException.throwIt((short) 0x6FFF);
        }
    }
    
    
}
