package org.example;

import com.ibm.jc.JCTerminal;
import com.ibm.jc.terminal.TraceJCTerminal;
import com.ibm.jc.terminal.PCSCJCTerminal;
import com.ibm.jc.CardManager;
import com.ibm.jc.OPKey;
import com.ibm.jc.JCard;
import com.ibm.jc.OPApplet;
import com.ibm.jc.JCException;

import java.io.PrintWriter;
/**
 *
 * @author petrs
 */
public class JCToolApp {
    protected static byte[] JCOP_DEFAULT_INIT_KEY1 = "@ABCDEFGHIJKLMNO".getBytes();
    protected static byte[] JCOP_DEFAULT_INIT_KEY2 = "@ABCDEFGHIJKLMNO".getBytes();
    protected static byte[] JCOP_DEFAULT_INIT_KEY3 = "@ABCDEFGHIJKLMNO".getBytes();
    protected static byte[] CM_AID_OBERTHUR = {(byte)0xa0, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x51, (byte)0x00, (byte)0x00};

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        OPAuthJCOPTools();
    }

    static void OPAuthJCOPTools() {
        JCTerminal terminal = new PCSCJCTerminal();
        terminal.init("any");
        //terminal.init("Gemplus GemPC Card Reader 0");
        terminal.open();

        // Create special terminal that will output all exchanged APDUs
        TraceJCTerminal _term = new TraceJCTerminal();
        _term.setLog(new PrintWriter(System.out));
        _term.init(terminal);
        terminal = _term;

        // new Card
        JCard card = new JCard(terminal, null, 2000);

        // Computer side CardManager
        CardManager cardManager = new CardManager(card, CM_AID_OBERTHUR);

        // KeySet must be set to value also set on smart card
        int keySet = 2;
        // 1 ... ENC_KEY, 2 ... MAC_KEY, 3 ... KEK_KEY
        OPKey key1 = new OPKey(keySet, 1, OPKey.DES_ECB, JCOP_DEFAULT_INIT_KEY1);
        OPKey key2 = new OPKey(keySet, 2, OPKey.DES_ECB, JCOP_DEFAULT_INIT_KEY2);
        OPKey key3 = new OPKey(keySet, 3, OPKey.DES_ECB, JCOP_DEFAULT_INIT_KEY3);

        // Select CardManager
        cardManager.select();

        // Set keys to Cardmanager on PC side
        cardManager.setKey(key1);
        cardManager.setKey(key2);
        cardManager.setKey(key3);

        // Authenticate and establish secure channel via OpenPlatform SCP'01 protocol
        try {
            cardManager.initializeUpdate(0, 0, CardManager.SCP_01_15);
            cardManager.externalAuthenticate(OPApplet.APDU_CLR);
        }
        catch (JCException ex) {
            System.out.println(ex.getMessage());
        }
    }
}