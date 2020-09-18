package org.openjavacard.lib.oath;

public class TOTPGenerator {

    private TOTPClock mClock;

    public TOTPClock getClock() {
        return mClock;
    }

    public void setClock(TOTPClock clock) {
        mClock = clock;
    }

    public short generate(byte[] buf, short off, short len) {
        return (short)0;
    }

}
