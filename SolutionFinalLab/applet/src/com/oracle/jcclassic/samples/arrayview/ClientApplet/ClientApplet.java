/**
 * Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.
 *
 */

package com.oracle.jcclassic.samples.arrayview.ClientApplet;

import java.io.IOException;

import com.oracle.jcclassic.samples.arrayview.MyShareable.MyShareable;
import com.oracle.jcclassic.samples.arrayview.MyShareable.AggregatorShareable;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.MultiSelectable;
import javacard.framework.Resources;
import javacard.framework.Util;
import javacard.framework.*;

public class ClientApplet extends Applet {

    private static final byte[] AGGREGATOR_AID = new byte[] {
        (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
        (byte) 0x62, (byte) 0x03, (byte) 0x01, (byte) 0x0C, 
        (byte) 0x0C, (byte) 0x03
    };

    private short totalConsumption;

    private ClientApplet() {
        totalConsumption = 0;
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new ClientApplet().register();
    }

    @Override
    public void process(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        if (selectingApplet()) {
            return;
        }

        switch (buffer[ISO7816.OFFSET_INS]) {
            case 0x10: // Collecter et analyser
                collectAndAnalyze();
                Util.setShort(buffer, (short) 0, totalConsumption);
                apdu.setOutgoingAndSend((short) 0, (short) 2);
                break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private void collectAndAnalyze() {
        AID aggregatorAID = JCSystem.lookupAID(AGGREGATOR_AID,  (short) 0, (byte) AGGREGATOR_AID.length);
        AggregatorShareable aggregator = (AggregatorShareable) JCSystem.getAppletShareableInterfaceObject(aggregatorAID, (byte) 0x00);

        if (aggregator != null) {
            byte[] buffer = JCSystem.makeTransientByteArray((short) 2, JCSystem.CLEAR_ON_RESET);
            aggregator.getAggregatedData(buffer, (short) 0);
            totalConsumption = Util.getShort(buffer, (short) 0);
        }
    }
}
