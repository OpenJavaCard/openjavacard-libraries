/*
 * openjavacard-libraries: Class libraries for JavaCard
 * Copyright (C) 2018 Ingo Albrecht <copyright@promovicz.org>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.openjavacard.lib.password;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.PIN;

import javacard.framework.Util;
import javacard.security.MessageDigest;
import javacard.security.RandomData;

public class PasswordHash implements PIN {

    private static final short SW_PIN_TRIES_REMAINING = (short)0x63C0;

    private final RandomData mRandom;
    private final MessageDigest mDigest;

    private final byte mMinLength;
    private final byte mMaxLength;
    private final byte mMaxTries;

    private byte mTries;

    private PasswordPolicy mPolicy;

    private final byte[] mSalt;
    private final byte[] mHash;
    private final byte[] mTemp;

    private final boolean[] mFlags;
    private static final byte FLAG_VALIDATED = 0;
    private static final byte NUM_FLAGS = 1;

    public PasswordHash(byte minLength, byte maxLength, byte maxTries,
                        RandomData random, MessageDigest digest) {
        byte hashLen = digest.getLength();
        mRandom = random;
        mDigest = digest;
        mMinLength = minLength;
        mMaxLength = maxLength;
        mMaxTries = maxTries;
        mTries = 0;
        mSalt = new byte[hashLen];
        mHash = new byte[hashLen];
        mTemp = JCSystem.makeTransientByteArray((short)(2 * hashLen), JCSystem.CLEAR_ON_DESELECT);
        mFlags = JCSystem.makeTransientBooleanArray(NUM_FLAGS, JCSystem.CLEAR_ON_RESET);
    }

    public PasswordHash(byte minLength, byte maxLength, byte maxTries) {
        this(minLength, maxLength, maxTries,
                RandomData.getInstance(RandomData.ALG_SECURE_RANDOM),
                getDefaultDigestInstance());
    }

    public PasswordPolicy getPasswordPolicy() {
        return mPolicy;
    }

    public void setPasswordPolicy(PasswordPolicy policy) {
        mPolicy = policy;
    }

    public byte getTriesRemaining() {
        return (byte)(mMaxTries - mTries);
    }

    public boolean isValidated() {
        return mFlags[FLAG_VALIDATED];
    }

    protected boolean getValidatedFlag() {
        return mFlags[FLAG_VALIDATED];
    }

    protected void setValidatedFlag(boolean validated) {
        mFlags[FLAG_VALIDATED] = validated;
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

    public boolean check(byte[] buf, short off, byte len) {
        // assume password is incorrect
        boolean correct;
        // get length of hash
        byte hashLen = mDigest.getLength();
        // compute new counter value
        byte newTries = (byte)(mTries + 1);
        // clear validated flag
        mFlags[FLAG_VALIDATED] = false;
        // check try counter
        if(newTries >= mMaxTries || newTries <= 0) {
            errorTriesRemaining();
        }
        // save try counter
        mTries = newTries;
        // pre-wipe temp buffer for paranoia
        wipeTemp();
        try {
            // perform the hash operation
            mDigest.reset();
            mDigest.update(mSalt, (short) 0, hashLen);
            mDigest.update(buf, off, len);
            mDigest.doFinal(null, (short) 0, (short) 0, mTemp, (short) 0);
            // compare hash
            correct = Util.arrayCompare(mHash, (short) 0, mTemp, (short) 0, hashLen) == 0;
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

    public void update(byte[] buf, short off, byte len) {
        // get length of hash
        short hashLen = mDigest.getLength();
        short offHash = (short)0;
        short offSalt = hashLen;
        // check validated status
        if(!mFlags[FLAG_VALIDATED]) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        // check length restrictions
        if(len < mMinLength || len > mMaxLength) {
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        }
        // check password policy
        if(mPolicy != null && !mPolicy.validate(buf, off, len)) {
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        }
        // pre-wipe temp buffer
        wipeTemp();
        // set up the new password
        try {
            // generate new salt
            mRandom.generateData(mTemp, offSalt, hashLen);
            // compute new hash
            mDigest.reset();
            mDigest.update(mTemp, offSalt, hashLen);
            mDigest.update(buf, off, len);
            mDigest.doFinal(null, (short)0, (short)0, mTemp, offHash);
            // atomically copy hash and salt, also reset try counter
            JCSystem.beginTransaction();
            Util.arrayCopy(mHash, (short) 0, mTemp, offHash, hashLen);
            Util.arrayCopy(mSalt, (short) 0, mTemp, offSalt, hashLen);
            mTries = 0;
            JCSystem.commitTransaction();
        } finally {
            // wipe temp buffer
            wipeTemp();
        }
    }

    private void wipeTemp() {
        Util.arrayFillNonAtomic(mTemp, (short)0, (short)mTemp.length, (byte)0);
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

    private static MessageDigest getDefaultDigestInstance() {
        MessageDigest res = null;
        if(res == null) {
            res = MessageDigest.getInstance(MessageDigest.ALG_SHA_512, false);
        }
        if(res == null) {
            res = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
        }
        if(res == null) {
            res = MessageDigest.getInstance(MessageDigest.ALG_SHA, false);
        }
        if(res == null) {
            res = MessageDigest.getInstance(MessageDigest.ALG_MD5, false);
        }
        return res;
    }

}
