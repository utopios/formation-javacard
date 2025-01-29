/**
 * Copyright (c) 1998, 2024, Oracle and/or its affiliates. All rights reserved.
 */
package com.oracle.javacard.sample;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import com.oracle.javacard.ams.AMService;
import com.oracle.javacard.ams.AMServiceFactory;
import com.oracle.javacard.ams.AMSession;
import com.oracle.javacard.ams.config.AID;
import com.oracle.javacard.ams.config.CAPFile;
import com.oracle.javacard.ams.script.APDUScript;
import com.oracle.javacard.ams.script.ScriptFailedException;
import com.oracle.javacard.ams.script.Scriptable;

public class ClientArrayViews {

    
    final static String sAID_ISD = "aid:A000000151000000";

    // Loading/ (no installing) order: 1
    final static String sAIDPackageShareable = "aid:A00000006203010C0B";

    // Loading/Installing order: 2
    final static String sAIDPackageMeter    = "aid:A00000006203010C0C";
    final static String sAIDAppletMeter     = sAIDPackageMeter + "02";
    final static String sAIDAppletMeterInst = sAIDPackageMeter + "02";

    // Loading/Installing order: 3
    final static String sAIDPackageAggregator = "aid:A00000006203010C0C";
    final static String sAIDAppletAggregator  = sAIDPackageAggregator + "03";
    final static String sAIDAppletAggregatorInst = sAIDPackageAggregator + "03";

    // Loading/Installing order: 4
    final static String sAIDPackageGrid    = "aid:A00000006203010C0D";
    final static String sAIDAppletGrid     = sAIDPackageGrid + "01";
    final static String sAIDAppletGridInst = sAIDPackageGrid + "01";

    private final static ResponseAPDU responseOK = new ResponseAPDU(new byte[]{(byte) 0x90, (byte) 0x00});

    private static CAPFile appFile_Meter = null;
    private static CAPFile appFile_Aggregator = null;
    private static CAPFile appFile_Grid = null;

    private static CAPFile appFile_Shared = null;

    /**
     * Launch the sample
     *
     * @param args command line arguments.<br>
     * Use {@code
     * -capClient=<.../ClientApplet.cap>
     * -capServer=<.../ServerApplet.cap>
     * -capShared=<.../MyShareable.cap>
     * -props=<property file>}
     */
    public static void main(String[] args) {

        int iResult = 0;

        try {
            
            appFile_Shared = CAPFile.from(getArg(args, "capShared"));

            appFile_Meter = CAPFile.from(getArg(args, "capServer"));
            appFile_Aggregator = CAPFile.from(getArg(args, "capAggregator"));
            appFile_Grid = CAPFile.from(getArg(args, "capClient"));


            // Configuration stage for connection to simulator
            Properties props   = new Properties();
            props.load(new FileInputStream(getArg(args, "props")));

            AMService ams = AMServiceFactory.getInstance("GP2.2");
            ams.setProperties(props);
            for (String key : ams.getPropertiesKeys()) {
                System.out.println(key + " = " + ams.getProperty(key));
            }

            // Session for deployments of necessary packages/applets (load / install stage)
            AMSession deploy =
                // Select SD & open secure channel
                ams.openSession(sAID_ISD)       // select SD & open secure channel
                    .load(sAIDPackageShareable, appFile_Shared.getBytes())
                    .load(sAIDAppletMeter,    appFile_Meter.getBytes())
                    .load(sAIDAppletAggregator,    appFile_Aggregator.getBytes())
                    .load(sAIDAppletGrid,    appFile_Grid.getBytes())
                    .install(sAIDPackageMeter, sAIDAppletMeter, sAIDAppletMeterInst) // Installer MeterApplet
                    .install(sAIDPackageAggregator, sAIDAppletAggregator, sAIDAppletAggregatorInst) // Installer AggregatorApplet
                    .install(sAIDPackageGrid, sAIDAppletGrid, sAIDAppletGridInst) // Installer GridApplet
                    .close();

            // Session for clean-up of packages/applets used in example (uninstall/unload stage)
            AMSession undeploy =
                // Select SD & open secure channel
                ams.openSession(sAID_ISD)
                    .uninstall(sAIDAppletGridInst)
                    .uninstall(sAIDAppletAggregatorInst)
                    .uninstall(sAIDAppletMeterInst)
                    .unload(sAIDPackageGrid)
                    .unload(sAIDPackageAggregator)
                    .unload(sAIDPackageShareable)
                    .close();

            TestScript testScript = new TestScript()

                // AM Session preparation
                .append(deploy)
                // Sélectionner GridApplet
                .append(new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID.from(sAIDAppletGrid).toBytes(), 0x7F), responseOK)
                // Lancer la collecte des données via GridApplet
                .append(new CommandAPDU(0x00, 0x10, 0x00, 0x00, new byte[]{}, 0x7F), responseOK)
                // Nettoyer les applets
                .append(undeploy);

            // Terminal to simulator
            CardTerminal terminal = getTerminal("socket", "127.0.0.1", "9025"); // or getTerminal("pcsc");

            // Wait some seconds to allow connections
            if (terminal.waitForCardPresent(10000)) {
                System.out.println("Connection to simulator established: "+ terminal.getName());
                Card card = terminal.connect("*");
                System.out.println(getFormattedATR(card.getATR().getBytes()));

                List<ResponseAPDU> responses = testScript.run(card.getBasicChannel());
                card.disconnect(true);

                System.out.println("Responses count: " + responses.size());
            }
            else {
                System.out.println("Connection to simulator failed");
                iResult = -1;
            }

        }
        catch (NoSuchAlgorithmException | NoSuchProviderException | CardException | ScriptFailedException | IOException e) {
            e.printStackTrace();
            iResult = -1;
        }
        System.exit (iResult);
    }

    /**
     * Get single argument passed via command line
     *
     * @param args All arguments given
     * @params argName Argument to fetch
     * @return Argument found
     */
    private static String getArg(String[] args, String argName) throws IllegalArgumentException {

        String value = null;

        for (String param : args) {
            if (param.startsWith("-" + argName + "=")) {
                value = param.substring(param.indexOf('=') + 1);
            }
        }

        if(value == null || (value.length() == 0)) {
            throw new IllegalArgumentException("Mandatory argument [" + argName + "] is missing");
        }
        return value;
    }

    /**
     * Puts all ATR bytes into a single string using hexadecimal format
     * @param ATR ATR bytes
     * @return Formatted ATR
     */
    private static String getFormattedATR(byte[] ATR) {
        StringBuilder sb = new StringBuilder();
        for (byte b : ATR) {
            sb.append(String.format("%02X ", b));
        }
        return String.format("ATR: [%s]", sb.toString().trim());
    }

    /**
     * Obtain card terminal
     *
     * @param connectionParams
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws CardException
     */
    private static CardTerminal getTerminal(String... connectionParams) throws NoSuchAlgorithmException, NoSuchProviderException, CardException {

        TerminalFactory tf;
        String connectivityType = connectionParams[0];
        if (connectivityType.equals("socket")) {
            String ipaddr = connectionParams[1];
            String port = connectionParams[2];
            tf = TerminalFactory.getInstance("SocketCardTerminalFactoryType",
                    List.of(new InetSocketAddress(ipaddr, Integer.parseInt(port))),
                    "SocketCardTerminalProvider");
        } else {
            tf = TerminalFactory.getDefault();
        }
        return tf.terminals().list().get(0);
    }

    /**
     * Extension of APDUScript allowing to accept a ResonseAPDU with expected SW (e. g. when errors should be tested)
     */
    private static class TestScript extends APDUScript {
        private List<CommandAPDU>  commands = new LinkedList<>();
        private List<ResponseAPDU> responses = new LinkedList<>();
        private int index = 0;

        @Override
        public List<ResponseAPDU> run(CardChannel channel) throws ScriptFailedException {
            return super.run(channel, c -> lookupIndex(c), r -> !isExpected(r));
        }

        @Override
        public TestScript append(Scriptable<CardChannel, CommandAPDU, ResponseAPDU> other) {
            super.append(other);
            return this;
        }

        public TestScript append(CommandAPDU apdu, ResponseAPDU expected) {
            super.append(apdu);
            this.commands.add(apdu);
            this.responses.add(expected);
            return this;
        }

        @Override
        public TestScript append(CommandAPDU apdu) {
            super.append(apdu);
            return this;
        }

        private CommandAPDU lookupIndex(CommandAPDU apdu) {
            print(apdu);
            this.index = IntStream.range(0, this.commands.size())
                    .filter(i -> apdu == this.commands.get(i))
                    .findFirst()
                    .orElse(-1);
            return apdu;
        }

        private boolean isExpected(ResponseAPDU response) {

            ResponseAPDU expected = (index < 0)? response : this.responses.get(index);
            if (!response.equals(expected)) {
                System.out.println("Received: ");
                print(response);
                System.out.println("Expected: ");
                print(expected);
                return false;
            }
            print(response);
            return true;
        }

        private static void print(CommandAPDU apdu) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%02X%02X%02X%02X %02X[", apdu.getCLA(), apdu.getINS(), apdu.getP1(), apdu.getP2(), apdu.getNc()));
            for (byte b : apdu.getData()) {
                sb.append(String.format("%02X", b));
            }
            sb.append("]");
            System.out.format("[%1$tF %1$tT %1$tL %1$tZ] [APDU-C] %2$s %n", System.currentTimeMillis(), sb.toString());
        }

        private static void print(ResponseAPDU apdu) {
            byte[] bytes = apdu.getData();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02X", b));
            }
            System.out.format("[%1$tF %1$tT %1$tL %1$tZ] [APDU-R] [%2$s] SW:%3$04X %n", System.currentTimeMillis(), sb.toString(), apdu.getSW());
        }
    }
}
