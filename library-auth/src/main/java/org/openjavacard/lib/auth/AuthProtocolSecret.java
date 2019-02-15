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

package org.openjavacard.lib.auth;

import javacard.framework.JCSystem;
import javacard.framework.Util;
import javacard.security.AESKey;
import javacard.security.KeyBuilder;
import javacard.security.MessageDigest;
import javacard.security.RandomData;
import javacardx.crypto.Cipher;

public class AuthProtocolSecret extends AuthProtocol {

    private static final short short0 = (short)0;

    private final RandomData mRandom;
    private final MessageDigest mHash;
    private final Cipher mCipher;
    private final AESKey mKey;
    private final short mLength;

    private final byte[] mCheckSalt;
    private final byte[] mCheckHash;
    private final byte[] mCheckTemp;

    private final byte[] mCryptSalt;
    private final byte[] mCryptHash;

    private final byte[] mKeyEncrypted;
    private final byte[] mKeyPlain;

    public AuthProtocolSecret(RandomData random) {
        mRandom = random;
        mHash = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
        mCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        mKey = (AESKey)KeyBuilder.buildKey(KeyBuilder.TYPE_AES_TRANSIENT_DESELECT, (short)256, false);
        byte hashLen = mHash.getLength();
        mLength = hashLen;
        mCheckSalt = new byte[hashLen];
        mCheckHash = new byte[hashLen];
        mCheckTemp = JCSystem.makeTransientByteArray(hashLen, JCSystem.CLEAR_ON_DESELECT);
        mCryptSalt = new byte[hashLen];
        mCryptHash = JCSystem.makeTransientByteArray(hashLen, JCSystem.CLEAR_ON_DESELECT);
        mKeyEncrypted = new byte[hashLen];
        mKeyPlain = JCSystem.makeTransientByteArray(hashLen, JCSystem.CLEAR_ON_DESELECT);
    }

    public void check(byte[] buf, short off, short len) {
        beginAttempt();
        // compute the possible check hash into the temp buffer
        mHash.reset();
        mHash.update(mCheckSalt, short0, mLength);
        mHash.doFinal(buf, off, len, mCheckTemp, short0);
        // check the hash
        if(Util.arrayCompare(mCheckTemp, short0, mCheckHash, short0, mLength) == 0) {
            attemptSuccess();
        } else {
            attemptFailure();
        }
        // now compute the encryption key
        mHash.reset();
        mHash.update(mCryptSalt, short0, mLength);
        mHash.doFinal(buf, off, len, mCryptHash, short0);
        mKey.setKey(mCryptHash, short0);
        // and decrypt the secret
        mCipher.init(mKey, Cipher.MODE_DECRYPT);
        mCipher.doFinal(mKeyEncrypted, short0, mLength, mKeyPlain, short0);
    }

    public void update(byte[] buf, short off, short len) {
        // generate new salts
        mRandom.generateData(mCheckSalt, short0, mLength);
        mRandom.generateData(mCryptSalt, short0, mLength);
        // compute the new check hash
        mHash.reset();
        mHash.update(mCheckSalt, short0, mLength);
        mHash.doFinal(buf, off, len, mCheckHash, short0);
        // compute the new crypt hash
        mHash.reset();
        mHash.update(mCryptSalt, short0, mLength);
        mHash.doFinal(buf, off, len, mCryptHash, short0);
        // encrypt the existing secret with the new key
        mKey.setKey(mCryptHash, short0);
        mCipher.init(mKey, Cipher.MODE_ENCRYPT);
        mCipher.doFinal(mKeyPlain, short0, mLength, mKeyEncrypted, short0);
    }

}
