
package com.utopios.scaffolding;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

public class Scaffolding extends Applet {
    
    
    /**
     * Only this class's install method should create the applet object.
     */
    protected Scaffolding() {
        
        register();
    }

    
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Scaffolding();
    }

   
   @Override 
   public void process(APDU apdu) {
        
    }
    
    
}
