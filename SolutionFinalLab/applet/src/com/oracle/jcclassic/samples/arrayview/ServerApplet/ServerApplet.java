/**
 * Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.
 *
 */

package com.oracle.jcclassic.samples.arrayview.ServerApplet;

import com.oracle.jcclassic.samples.arrayview.MyShareable.MeterShareable;

import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.MultiSelectable;
import javacard.framework.Shareable;
import javacard.framework.Util;

import javacard.security.*;
import javacardx.crypto.Cipher;
import javacard.framework.*;

public class ServerApplet extends Applet implements MeterShareable {
    private byte[] consumptionData;
    private byte[] sharedSecret;
    private Cipher aesCipher;
    private AESKey aesKey;

    private ServerApplet() {
        consumptionData = new byte[16];
        sharedSecret = JCSystem.makeTransientByteArray((short) 16, JCSystem.CLEAR_ON_RESET);
        aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        aesKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        RandomData random = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        random.generateData(consumptionData, (short) 0, (short) consumptionData.length);
        random.generateData(sharedSecret, (short) 0, (short) sharedSecret.length);
        aesKey.setKey(sharedSecret, (short) 0);
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new ServerApplet().register();
    }

    @Override
    public Shareable getShareableInterfaceObject(AID clientAID, byte parameter) {
        return this;
    }

    @Override
    public short getConsumptionData(byte[] buffer, short offset) {
        // Chiffrer les données avant de les envoyer
        aesCipher.init(aesKey, Cipher.MODE_ENCRYPT);
        aesCipher.doFinal(consumptionData, (short) 0, (short) consumptionData.length, buffer, offset);
        return (short) consumptionData.length;
    }

    @Override
    public void process(APDU apdu) {
        // Aucune commande APDU directe gérée dans cet applet
        if (selectingApplet()) {
            return; // Retourner lors de la sélection
        }
        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
    }
}
