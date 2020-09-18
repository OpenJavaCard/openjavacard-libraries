package org.openjavacard.lib.oath;

import javacard.security.HMACKey;
import javacard.security.SecretKey;

public class OATHConfig implements SecretKey {

    private HMACKey mKey;

    private byte mDigits;

    private byte[] mCounter;

    public boolean isInitialized() {
        return false;
    }

    public short getSize() {
        return 0;
    }

    public byte getType() {
        return 0;
    }

    public void clearKey() {
        mKey.clearKey();
    }

}
