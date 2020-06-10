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

/**
 * Salted hash password authenticator
 * <p/>
 * This class implements a salted hash password mechanism.
 * <p/>
 * The rationale behind this class is that hashing allows keeping the actual
 * password secret even if card memory can be extracted after password setup.
 * This can lead to design advantages from a trust and legal standpoint.
 * <p/>
 */
public class PasswordHash implements PIN {

    /** ISO7816: SW value for warning about remaining tries */
    private static final short SW_PIN_TRIES_REMAINING = (short)0x63C0;

    /** Configuration: allow use of SHA512 */
    private static final boolean USE_SHA512 = true;
    /** Configuration: allow use of SHA256 */
    private static final boolean USE_SHA256 = true;
    /** Configuration: allow use of SHA1 */
    private static final boolean USE_SHA160 = true;
    /** Configuration: allow use of MD5 */
    private static final boolean USE_MD5    = true;

    /** RNG for salt generation */
    private final RandomData mRandom;
    /** Digest for hash operations */
    private final MessageDigest mDigest;

    /** Minimum password length */
    private final byte mMinLength;
    /** Maximum password length */
    private final byte mMaxLength;
    /** Maximum number of tries before blocking */
    private final byte mMaxTries;

    /** Number of tries since last unblock */
    private byte mTries;

    /** Optional password policy */
    private PasswordPolicy mPolicy;

    /** Current password salt */
    private final byte[] mSalt;
    /** Current password hash */
    private final byte[] mHash;

    /** Transient flags */
    private final boolean[] mFlags;
    private static final byte FLAG_VALIDATED = 0;
    private static final byte NUM_FLAGS = 1;

    /** Temporary buffer */
    private final byte[] mTemp;

    /**
     * Full constructor
     *
     * @param minLength minimum password length
     * @param maxLength maximum password length
     * @param maxTries maximum number of tries before blocking
     * @param clearOn memory type for validation state
     * @param random RNG to use for generating salts
     * @param digest digest to be used for hash operations
     */
    public PasswordHash(byte minLength, byte maxLength, byte maxTries, byte clearOn,
                        RandomData random, MessageDigest digest) {
        byte hashLen = digest.getLength();
        // crypto instances
        mRandom = random;
        mDigest = digest;
        // configuration constants
        mMinLength = minLength;
        mMaxLength = maxLength;
        mMaxTries = maxTries;
        // state
        mTries = 0;
        mPolicy = null;
        // password data
        mSalt = new byte[hashLen];
        mHash = new byte[hashLen];
        // variables
        mFlags = JCSystem.makeTransientBooleanArray(NUM_FLAGS, clearOn);
        // temporary buffer
        mTemp = JCSystem.makeTransientByteArray((short)(2 * hashLen), clearOn);
    }

    /**
     * Convenience constructor
     *
     * @param minLength minimum password length
     * @param maxLength maximum password length
     * @param maxTries maximum number of tries before blocking
     * @param clearOn memory type for validation state
     */
    public PasswordHash(byte minLength, byte maxLength, byte maxTries, byte clearOn) {
        this(minLength, maxLength, maxTries, clearOn,
                RandomData.getInstance(RandomData.ALG_SECURE_RANDOM),
                getDefaultDigestInstance());
    }

    /**
     * Get digest algorithm used for password hashing
     * @return a MessageDigest algorithm identifier
     */
    public byte getDigestAlgorithm() {
        return mDigest.getAlgorithm();
    }

    /**
     * Get the current password policy
     * @return the policy or null
     */
    public PasswordPolicy getPasswordPolicy() {
        return mPolicy;
    }

    /**
     * Set the password policy
     * @param policy new policy or null
     */
    public void setPasswordPolicy(PasswordPolicy policy) {
        mPolicy = policy;
    }

    /**
     * Get number of tried remaining before blocking
     * @return number of tries
     */
    public byte getTriesRemaining() {
        return (byte)(mMaxTries - mTries);
    }

    /**
     * Return true if the password has been entered since last deselect/reset
     * @return true if validated
     */
    public boolean isValidated() {
        return mFlags[FLAG_VALIDATED];
    }

    /**
     * Reset the try counter
     * <p/>
     * Only works when validated. Should be used at a safe point after login.
     */
    public void reset() {
        if(mFlags[FLAG_VALIDATED]) {
            resetAndUnblock();
        }
    }

    /**
     * Reset the try counter and unblock
     * <p/>
     * Always works. Should be used for PUK-based recovery.
     */
    public void resetAndUnblock() {
        mFlags[FLAG_VALIDATED] = false;
        mTries = 0;
    }

    /**
     * Check the given password
     * @param buf containing password
     * @param off of password
     * @param len of password
     * @return true if validated
     */
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

    /**
     * Change the password
     * @param buf containing new password
     * @param off of password
     * @param len of password
     */
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

    /**
     * Internal: wipe temporary buffer
     */
    private void wipeTemp() {
        Util.arrayFillNonAtomic(mTemp, (short)0, (short)mTemp.length, (byte)0);
    }

    /**
     * Internal: throw a warning exception with tries remaining
     */
    private void errorTriesRemaining() {
        short code = SW_PIN_TRIES_REMAINING;
        byte remaining = getTriesRemaining();
        if(remaining > 15) {
            remaining = 15;
        }
        code |= remaining;
        ISOException.throwIt(code);
    }

    /**
     * Return an instance with the best supported digest algorithm
     * @return a MessageDigest instance
     */
    public static MessageDigest getDefaultDigestInstance() {
        MessageDigest res = null;
        if(USE_SHA512 && res == null) {
            res = MessageDigest.getInstance(MessageDigest.ALG_SHA_512, false);
        }
        if(USE_SHA256 && res == null) {
            res = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
        }
        if(USE_SHA160 && res == null) {
            res = MessageDigest.getInstance(MessageDigest.ALG_SHA, false);
        }
        if(USE_MD5 && res == null) {
            res = MessageDigest.getInstance(MessageDigest.ALG_MD5, false);
        }
        return res;
    }

}
