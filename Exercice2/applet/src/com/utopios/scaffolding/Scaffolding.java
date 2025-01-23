
package com.utopios.scaffolding;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.OwnerPIN;

public class Scaffolding extends Applet {
    
    // -------------------------
    // 1. Constantes & Attributs
    // -------------------------
    // INS (Instructions APDU)
    private static final byte INS_VERIFY_PIN  = (byte) 0x20; // Vérifier le PIN
    private static final byte INS_CHANGE_PIN  = (byte) 0x21; // Changer le PIN
    private static final byte INS_UNBLOCK_PIN = (byte) 0x55; // Débloquer le PIN via PUK

    // Configuration du PIN
    private static final byte PIN_MAX_TRIES = 3;     // Nb max de tentatives PIN
    private static final byte PIN_MAX_SIZE  = 4;     // Taille max du PIN

    // Configuration du PUK
    private static final byte PUK_MAX_TRIES = 5;     // Nb max de tentatives PUK
    private static final byte PUK_SIZE      = 8;     // Taille exacte du PUK

    // Status words personnalisés
    private static final short SW_CARD_BLOCKED         = (short) 0x6283; // Carte bloquée
    private static final short SW_PIN_BLOCKED          = (short) 0x6983; // PIN bloqué
    private static final short SW_SECURITY_NOT_SATISFIED= ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED;

    // -------------------------
    // 2. Variables d'instance
    // -------------------------
    
    private OwnerPIN pin;

    
    private byte[] pukValue;

    private byte pukTriesRemaining;

   
    private boolean cardBlocked;

    // -------------------------
    // 3. Méthode d'installation
    // -------------------------
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Scaffolding();
    }

    protected Scaffolding() {
        // 1. Créer l'objet OwnerPIN pour le PIN
        pin = new OwnerPIN(PIN_MAX_TRIES, PIN_MAX_SIZE);

        // Définir un PIN par défaut (ex: "1234")
        byte[] defaultPin = { '1','2','3','4' };
        pin.update(defaultPin, (short) 0, (byte) defaultPin.length);

        // 2. Initialiser le PUK
        pukValue = new byte[PUK_SIZE];
        byte[] defaultPuk = { '1','2','3','4','5','6','7','8' };
        Util.arrayCopyNonAtomic(defaultPuk, (short)0, pukValue, (short)0, (short)defaultPuk.length);

        // 3. Initialiser le compteur de tentatives PUK
        pukTriesRemaining = PUK_MAX_TRIES;

        // 4. La carte n'est pas bloquée au départ
        cardBlocked = false;

        // 5. Enregistrer l'applet
        register();
    }

    // -------------------------
    // 4. Méthode process(APDU)
    // -------------------------
    @Override
    public void process(APDU apdu) {
        if (selectingApplet()) {
            return;
        }

        byte[] buffer = apdu.getBuffer();
        byte ins = buffer[ISO7816.OFFSET_INS];

        // Avant tout, on vérifie si la carte est bloquée
        // Si oui, on refuse toute opération sensible
        if (cardBlocked) {
            // On envoie un statut qui indique que la carte est bloquée
            ISOException.throwIt(SW_CARD_BLOCKED);
        }

        // Dispatcher selon l'INS
        switch (ins) {
            case INS_VERIFY_PIN:
                verifyPin(apdu);
                break;

            case INS_CHANGE_PIN:
                changePin(apdu);
                break;

            case INS_UNBLOCK_PIN:
                unblockPin(apdu);
                break;

            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    // -------------------------
    // 5. Méthodes de gestion du PIN/PUK
    // -------------------------

    
    private void verifyPin(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte lc = buffer[ISO7816.OFFSET_LC];       
        short dataOffset = ISO7816.OFFSET_CDATA;

        if (lc == 0 || lc > PIN_MAX_SIZE) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Tenter la vérification
        boolean pinOk = pin.check(buffer, dataOffset, lc);
        if (!pinOk) {
            // PIN incorrect => pin.check() décrémente déjà le compteur
            // Si le compteur d'essais PIN atteint 0, le PIN est bloqué
            if (pin.getTriesRemaining() == 0) {
                // PIN bloqué => on peut renvoyer un SW explicite
                ISOException.throwIt(SW_PIN_BLOCKED);
            } else {
                ISOException.throwIt(SW_SECURITY_NOT_SATISFIED);
            }
        }
        // Si on arrive ici, le PIN est vérifié et "authentifié"
    }

    
    private void changePin(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte lc = buffer[ISO7816.OFFSET_LC];
        short dataOffset = ISO7816.OFFSET_CDATA;

        // Vérifier que la session est authentifiée (PIN validé)
        if (!pin.isValidated()) {
            ISOException.throwIt(SW_SECURITY_NOT_SATISFIED);
        }

        // Vérifier la longueur du nouveau PIN
        if (lc == 0 || lc > PIN_MAX_SIZE) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Mettre à jour la valeur du PIN
        pin.update(buffer, dataOffset, lc);

        
        // nouvelle vérification la prochaine fois
        //pin.reset();
    }

    
    private void unblockPin(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte lc = buffer[ISO7816.OFFSET_LC];
        short dataOffset = ISO7816.OFFSET_CDATA;

        // Vérifier la longueur de PUK reçue
        if (lc != PUK_SIZE) {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        // Vérifier si le compteur de tentatives PUK n'est pas déjà à 0
        if (pukTriesRemaining == 0) {
            // La carte est déjà bloquée à cause du PUK épuisé
            // On met "cardBlocked" à true pour bloquer toutes les opérations ultérieures
            cardBlocked = true;
            ISOException.throwIt(SW_CARD_BLOCKED);
        }

        // Comparer le PUK reçu au PUK stocké
        if (Util.arrayCompare(buffer, dataOffset, pukValue, (short)0, (short)PUK_SIZE) == 0) {
            // PUK correct => débloquer le PIN
            pin.resetAndUnblock();

            // Réinitialiser le PIN à une valeur par défaut (ex: "1234")
            byte[] newPin = { '1','2','3','4' };
            pin.update(newPin, (short) 0, (byte) newPin.length);

            // Restaurer le compteur de tentatives PUK
            pukTriesRemaining = PUK_MAX_TRIES;

        } else {
            // PUK incorrect => décrémenter et vérifier si on arrive à 0
            pukTriesRemaining--;
            if (pukTriesRemaining == 0) {
                // Bloquer la carte
                cardBlocked = true;
                ISOException.throwIt(SW_CARD_BLOCKED);
            } else {
                // Sinon, on lève un SW "sécurité non satisfaite"
                ISOException.throwIt(SW_SECURITY_NOT_SATISFIED);
            }
        }
    }
    
}
