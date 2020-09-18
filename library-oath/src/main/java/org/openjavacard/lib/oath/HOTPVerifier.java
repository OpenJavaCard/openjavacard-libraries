package org.openjavacard.lib.oath;

import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.PIN;

public class HOTPVerifier implements PIN {

    private static final short SW_PIN_TRIES_REMAINING = (short)0x63C0;

    private final byte mMaxTries;
    private final byte mMaxOffset;

    private byte mTries;

    private final OATHConfig mConfig;
    private final byte[] mCounter;

    private final boolean[] mFlags;
    private static final short NUM_FLAGS = 1;
    private static final short FLAG_VALIDATED = 0;

    public HOTPVerifier(OATHConfig config, byte maxTries, byte maxOffset, byte clearOn) {
        mMaxTries = maxTries;
        mMaxOffset = maxOffset;
        mConfig = config;
        mCounter = new byte[8];
        mFlags = JCSystem.makeTransientBooleanArray(NUM_FLAGS, clearOn);
    }

    public byte getTriesRemaining() {
        return (byte)(mMaxTries - mTries);
    }

    public boolean isValidated() {
        return mFlags[FLAG_VALIDATED];
    }

    public short getCounter(byte[] buf, short off, short len) {
        return (short)0;
    }

    public short setCounter(byte[] buf, short off, short len) {
        return (short)0;
    }

    public void reset() {
        if(mFlags[FLAG_VALIDATED]) {
            resetAndUnblock();
        }
    }

    public void resetAndUnblock() {
        mFlags[FLAG_VALIDATED] = false;
        mTries = 0;
    }

    public boolean check(byte[] bytes, short off, byte len) throws ArrayIndexOutOfBoundsException, NullPointerException {
        // assume password is incorrect
        boolean correct = false;

        // clear validated flag
        mFlags[FLAG_VALIDATED] = false;

        // compute new counter value
        byte newTries = (byte)(mTries + 1);
        // check try counter
        if(newTries >= mMaxTries || newTries <= 0) {
            errorTriesRemaining();
        }
        // save try counter
        mTries = newTries;

        // pre-wipe temp buffer for paranoia
        wipeTemp();
        try {
            // iterate acceptable offsets
            for(byte counterOffset = 0; counterOffset < mMaxOffset; counterOffset++) {
            }
            // handle correct password
            if(correct) {
                // set validated flag
                mFlags[FLAG_VALIDATED] = true;
                // reset try counter
                mTries = 0;
            }
        } finally {
            wipeTemp();
        }

        // return result
        return correct;
    }

    private void errorTriesRemaining() {
        short code = SW_PIN_TRIES_REMAINING;
        byte remaining = getTriesRemaining();
        if(remaining > 15) {
            remaining = 15;
        }
        code |= remaining;
        ISOException.throwIt(code);
    }

    private void wipeTemp() {
    }

}
