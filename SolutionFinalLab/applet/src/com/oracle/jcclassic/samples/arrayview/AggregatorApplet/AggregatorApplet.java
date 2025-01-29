/**
 * Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.
 *
 */

package com.oracle.jcclassic.samples.arrayview.AggregatorApplet;

import com.oracle.jcclassic.samples.arrayview.MyShareable.AggregatorShareable;
import com.oracle.jcclassic.samples.arrayview.MyShareable.MeterShareable;

import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.MultiSelectable;
import javacard.framework.Shareable;
import javacard.framework.Util;

import javacard.framework.*;

import javacard.security.*;
import javacardx.crypto.Cipher;

public class AggregatorApplet extends Applet implements AggregatorShareable {
    private short aggregatedConsumption;
    private Cipher aesCipher;
    private byte[] sharedSecret;
    private AESKey aesKey;

   private static final byte[] METER_AID = new byte[] {
        (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x62, (byte) 0x03, (byte) 0x01, (byte) 0x0C,
        (byte) 0x0C, (byte) 0x02
    };


    private AggregatorApplet() {
        aggregatedConsumption = 0;
        
        aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        sharedSecret = JCSystem.makeTransientByteArray((short) 16, JCSystem.CLEAR_ON_RESET);

        RandomData random = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        random.generateData(sharedSecret, (short) 0, (short) sharedSecret.length);
    }

    @Override
    public void process(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        if (selectingApplet()) {
            return;
        }

        switch (buffer[ISO7816.OFFSET_INS]) {
            

            case 0x30: // Collecter les données des MeterApplets
                collectDataFromMeters();
                break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new AggregatorApplet().register();
    }

    public void collectDataFromMeters() {

        
        byte[] encryptedData = JCSystem.makeTransientByteArray((short) 16, JCSystem.CLEAR_ON_RESET);
        byte[] decryptedData = JCSystem.makeTransientByteArray((short) 16, JCSystem.CLEAR_ON_RESET);

        AID meterAID = JCSystem.lookupAID(METER_AID, (short) 0, (byte) METER_AID.length);
        if (meterAID == null) {
            ISOException.throwIt((short) 0x6A88); // MeterApplet non trouvé
        }

        MeterShareable meter = (MeterShareable) JCSystem.getAppletShareableInterfaceObject(meterAID, (byte) 0x00);
        if (meter != null) {
            byte[] buffer = JCSystem.makeTransientByteArray((short) 16, JCSystem.CLEAR_ON_RESET);
            short length = meter.getConsumptionData(buffer, (short) 0);

            // Agréger les données reçues
            aggregatedConsumption = 0;
            for (short i = 0; i < length; i++) {
                aggregatedConsumption += buffer[i];
            }
        } else {
            ISOException.throwIt((short) 0x6982); // Accès non autorisé
        }
    }

    @Override
    public short getAggregatedData(byte[] buffer, short offset) {
        Util.setShort(buffer, offset, aggregatedConsumption);
        return 2;
    }
}
