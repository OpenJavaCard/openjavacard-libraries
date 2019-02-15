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

package org.openjavacard.lib.fortuna;

import javacard.framework.JCSystem;
import javacard.framework.Util;
import javacard.security.AESKey;
import javacard.security.CryptoException;
import javacard.security.KeyBuilder;
import javacard.security.MessageDigest;
import javacard.security.RandomData;
import javacardx.crypto.Cipher;

/**
 * Fortuna PRNG
 *
 * This is a PRNG based on Fortuna by Bruce Schneier.
 *
 * The output limit per request is significantly more strict
 * than Fortuna prescribes, being limited by the short type to 32768.
 */
public class FortunaRandom extends RandomData {

    /** Cipher algorithm to be used */
    private static final byte CIPHER_ALGO = Cipher.ALG_AES_BLOCK_128_CBC_NOPAD;
    /** Hash algorithm to be used */
    private static final byte HASH_ALGO = MessageDigest.ALG_SHA_256;
    /** Key type to be used */
    private static final byte KEY_TYPE = KeyBuilder.TYPE_AES;
    /** Key length to be used */
    private static final short KEY_LENGTH = KeyBuilder.LENGTH_AES_256;

    /** Size of an internal block of random data */
    private static final short BLOCK_SIZE = 16;
    /** Size of the internal counter */
    private static final short COUNTER_SIZE = 16;
    /** Size of the internal temporary buffer */
    private static final short TMP_SIZE = 48;

    /** Bootstrap flag */
    private boolean mInitialized;

    /** Cipher instance */
    private final Cipher mCipher;
    /** Hash instance */
    private final MessageDigest mHash;

    /** Fortuna counter */
    private final LongNum mCounter;
    /** Fortuna key */
    private final AESKey mKey;

    /** Temp buffer */
    private final byte[] mTmp;

    /**
     * Construct a persistent Fortuna PRNG
     */
    public FortunaRandom() {
        mInitialized = false;
        // get crypto instances
        mCipher = Cipher.getInstance(CIPHER_ALGO, false);
        mHash = MessageDigest.getInstance(HASH_ALGO, false);
        // construct state
        mCounter = new LongNum(COUNTER_SIZE);
        mKey = (AESKey)KeyBuilder.buildKey(KEY_TYPE, KEY_LENGTH, false);
        // allocate temp buffer (caution: used for zeroes)
        mTmp = JCSystem.makeTransientByteArray(TMP_SIZE, JCSystem.CLEAR_ON_DESELECT);
        // initialize the keys (to zero as per Fortuna)
        mKey.setKey(mTmp, (short)0);
    }

    /**
     * Reset the PRNG
     *
     * After this the PRNG will be unusable until reseeded.
     */
    public void reset() {
        mInitialized = false;
        mCounter.clear();
        wipeTmp();
        mKey.setKey(mTmp, (short)0);
    }

    /**
     * Generate random data
     *
     * PRNG must have been seeded.
     *
     * @param buf to generate into
     * @param off to generate at
     * @param len to generate
     * @throws CryptoException on error
     */
    public void generateData(byte[] buf, short off, short len)
            throws CryptoException {
        // check that we are seeded
        if(!mInitialized) {
            CryptoException.throwIt(CryptoException.ILLEGAL_USE);
        }
        // check requested length
        if(len <= 0) {
            CryptoException.throwIt(CryptoException.ILLEGAL_USE);
        }
        // handle exceptions so we can force key advance
        try {
            // determine number of blocks
            short bLen = (short) (len / BLOCK_SIZE);
            // consider remainder separately
            short bRem = (short) (len % BLOCK_SIZE);
            // loop over all blocks but remainder
            short bOff = off;
            for (short bIdx = 0; bIdx < bLen; bIdx++) {
                generateBlock();
                Util.arrayCopyNonAtomic(mTmp, (short) 0, buf, bOff, BLOCK_SIZE);
                bOff += 16;
            }
            // handle the remainder
            if (bRem != 0) {
                generateBlock();
                Util.arrayCopyNonAtomic(mTmp, (short) 0, buf, bOff, bRem);
            }
        } finally {
            // advance key, even if some fault occurs
            generateKey();
            // be safe and wipe the temp buffer
            wipeTmp();
        }
    }

    /**
     * Seed the PRNG
     *
     * Seed must be at least 32 bytes.
     *
     * After using this once the PRNG is initialized.
     *
     * @param buf to read from
     * @param off to read at
     * @param len to read
     */
    public void setSeed(byte[] buf, short off, short len) {
        // reject any seed less than 256 bits
        if(len < 32) {
            CryptoException.throwIt(CryptoException.ILLEGAL_USE);
        }
        // handle exceptions so we can force wiping
        try {
            // retrieve key into tmp[0:31]
            mKey.getKey(mTmp, (short) 0);
            // hash the key
            mHash.reset();
            mHash.update(mTmp, (short) 0, (short) 32);
            // finish hash with new seed into tmp[0:31]
            mHash.doFinal(buf, off, len, mTmp, (short) 0);
            // increment counter
            incrementCounter();
            // set the new key
            mKey.setKey(mTmp, (short) 0);
            // once seeded we are initialized
            mInitialized = true;
        } finally {
            // be safe and wipe the temp buffer
            wipeTmp();
        }
    }

    /**
     * Internal: wipe the temporary buffer
     */
    private void wipeTmp() {
        Util.arrayFillNonAtomic(mTmp, (short)0, (short)mTmp.length, (byte)0);
    }

    /**
     * Internal: increment the Fortuna counter
     */
    private void incrementCounter() {
        mCounter.add((byte)1);
    }

    /**
     * Internal: generate one Fortuna block
     *
     * The result will be in mTmp[0:15].
     *
     * Temporary buffer should be cleared before returning to client.
     */
    private void generateBlock() {
        // get the counter before we increment it
        //   mTmp[0:15] = counter
        mCounter.get(mTmp, (short)16, (short)16);
        // increment the counter early to better guarantee non-repetition
        incrementCounter();
        // first part of encrypt chain
        mCipher.init(mKey, Cipher.MODE_ENCRYPT);
        mCipher.doFinal(mTmp, (short)16, (short)16, mTmp, (short)0);
        // output is in mTmp[0:15]
    }

    /**
     * Internal: generate a new Fortuna key
     *
     * Temporary buffer should be cleared before returning to client.
     */
    private void generateKey() {
        generateBlock();
        Util.arrayCopyNonAtomic(mTmp, (short)0,  mTmp, (short)32, (short)16);
        generateBlock();
        Util.arrayCopyNonAtomic(mTmp, (short)0,  mTmp, (short)16, (short)16);
        Util.arrayCopyNonAtomic(mTmp, (short)32, mTmp, (short)0,  (short)16);
        mKey.setKey(mTmp, (short)0);
    }

}
