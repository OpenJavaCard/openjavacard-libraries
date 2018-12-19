/*
 * openjavacard-libraries: OpenJavaCard Libraries
 * Copyright (C) 2017-2018 Ingo Albrecht, prom@berlin.ccc.de
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
 *
 */

package org.openjavacard.lib.rsa;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;
import javacard.security.CryptoException;
import javacard.security.Key;
import javacard.security.MessageDigest;
import javacard.security.RandomData;
import javacardx.crypto.Cipher;

/**
 * Implementation of PKCS #1 v2.1 RSAES-OAEP encryption scheme
 *
 * Uses SHA-1 as the cryptographic hash and MGF-1 as its masking function.
 *
 * This implementation is intended and optimized for JavaCard, minimizing
 * RAM usage as much as possible. Memory usage is dependent on application
 * usage in that significant memory can be saved by operating on the
 * whole message using doFinal(). Once update() is used once an additional
 * message buffer will be allocated and kept.
 *
 */
public class RSAOAEPCipher extends Cipher {

    private static final short MAX_HASH_LENGTH = 20;
    private static final short MAX_MESSAGE_LENGTH = 256;

    private static final short short0 = (short)0;

    private final byte[] empty = new byte[0];

    /** Mask generator */
    private MGF1 mMGF = null;

    /** RNG for the seed */
    private RandomData mRandom = null;

    /** Hash for both MGF and label */
    private MessageDigest mHash = null;

    /** Temporary buffer (CLEAR_ON_DESELECT) */
    private byte[] mTemp = null;

    /** Underlying plain RSA cipher */
    Cipher mRSA = null;

    /** Indicates that we have seen init() */
    boolean mInitialized = false;

    /** Currently initialized mode (encrypt/decrypt) */
    byte  mMode = 0;

    /** Size of our current RSA key */
    short mBits = 0;

    /**
     * Cached hash of label
     *
     * Set up during cipher initialization.
     */
    byte[] mLabelHash = null;

    /**
     * Input buffer (CLEAR_ON_DESELECT)
     *
     * Used as an input buffer whenever a message is being
     * fed incrementally using update(). Will not be allocated
     * or used when only doFinal() is being used.
     */
    byte[] mBuffer = null;

    /** Fill pointer into mBuffer */
    short mBufPtr = 0;

    /** Fill limit in mBuffer */
    short mBufEnd = 0;

    /** Main constructor */
    public RSAOAEPCipher() {
        mHash = MessageDigest.getInstance(MessageDigest.ALG_SHA, false);
        mMGF = new MGF1(mHash);
        mRandom = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        mTemp = JCSystem.makeTransientByteArray(MAX_MESSAGE_LENGTH, JCSystem.CLEAR_ON_DESELECT);
        mLabelHash = new byte[MAX_HASH_LENGTH];
        mRSA = Cipher.getInstance(Cipher.ALG_RSA_NOPAD, false);
    }

    /**
     * @return the algorithm of this cipher: Cipher.ALG_RSA_PKCS1_OAEP
     */
    public byte getAlgorithm() {
        return Cipher.ALG_RSA_PKCS1_OAEP;
    }

    /**
     * Initialize the cipher object for use
     *
     * Will use the empty string as the label for OAEP purposes.
     *
     * @param theKey the key object to use for encrypting or decrypting
     * @param theMode one of <code>MODE_DECRYPT</code> or <code>MODE_ENCRYPT</code>
     * @throws CryptoException
     */
    public void init(Key theKey, byte theMode) throws CryptoException {
        init(theKey, theMode, empty, short0, short0);
    }

    /**
     * Initialize the cipher object for use
     *
     * The additional cipher data will be used as the OAEP label.
     *
     * @param theKey the key object to use for encrypting or decrypting.
     * @param theMode one of <code>MODE_DECRYPT</code> or <code>MODE_ENCRYPT</code>
     * @param labelBuf
     * @param labelOff
     * @param labelLen
     * @throws CryptoException
     */
    public void init(Key theKey, byte theMode, byte[] labelBuf, short labelOff, short labelLen)
            throws CryptoException {
        // check that we actually got a key
        if(theKey == null) {
            CryptoException.throwIt(CryptoException.UNINITIALIZED_KEY);
        }

        // check that the key is initialized
        if(!theKey.isInitialized()) {
            CryptoException.throwIt(CryptoException.UNINITIALIZED_KEY);
        }

        // compute various lengths
        short kbits = theKey.getSize();
        short klen = (short)(kbits / 8);
        short hlen = mHash.getLength();

        // check mode and set up input buffering accordingly
        short end = -1;
        switch(theMode) {
            case MODE_DECRYPT:
                end = klen;
                break;
            case MODE_ENCRYPT:
                end = (short) (klen - 2 * hlen - 2);
                break;
            default:
                CryptoException.throwIt(CryptoException.ILLEGAL_VALUE);
                break;
        }

        // initialize the RSA instance
        mRSA.init(theKey, theMode);

        // set the mode
        mMode = theMode;
        mBits = kbits;

        // set up input buffer
        mBufPtr = 0;
        mBufEnd = end;

        // hash the label
        mHash.reset();
        mHash.doFinal(labelBuf, labelOff, labelLen, mLabelHash, short0);

        // mark as initialized
        mInitialized = true;
    }

    /**
     * Push message data into the cipher for processing.
     *
     * This never generates any output as we always operate on while messages internally.
     *
     * Using this function will have a significant memory cost, so it is better to
     * avoid it and operate on the whole message using doFinal().
     *
     * @param inBuff the input buffer of data to be encrypted/decrypted
     * @param inOffset the offset into the input buffer at which to begin encryption/decryption
     * @param inLength the byte length to be encrypted/decryptedv
     * @param outBuff the output buffer, may be the same as the input buffer
     * @param outOffset the offset into the output buffer where the resulting ciphertext/plaintext begins
     * @return number of bytes written to the output buffer (always 0)
     * @throws CryptoException
     */
    public short update(byte[] inBuff, short inOffset, short inLength, byte[] outBuff, short outOffset)
            throws CryptoException {
        // check initialized
        if(!mInitialized) {
            CryptoException.throwIt(CryptoException.INVALID_INIT);
        }
        // zero length is ok - do nothing
        if(inLength == 0) {
            return 0;
        }
        // allocate buffer since we need it
        if(mBuffer == null) {
            mBuffer = JCSystem.makeTransientByteArray((short)256, JCSystem.CLEAR_ON_DESELECT);
        }
        // check sufficient buffer remaining
        if(((short)(mBufPtr + inLength)) > mBufEnd) {
            CryptoException.throwIt(CryptoException.ILLEGAL_VALUE);
        }
        // copy data to the buffer
        mBufPtr = Util.arrayCopyNonAtomic(inBuff, inOffset, mBuffer, mBufPtr, inLength);
        // never produce any output
        return 0;
    }

    /**
     * Finish the ongoing operation on the cipher
     *
     * This will produce an encrypted OAEP message in the output buffer.
     *
     * @param inBuff the input buffer of data to be encrypted/decrypted
     * @param inOffset the offset into the input buffer at which to begin encryption/decryption
     * @param inLength the byte length to be encrypted/decrypted
     * @param outBuff the output buffer, may be the same as the input buffer
     * @param outOffset the offset into the output buffer where the resulting output data begins
     * @return number of bytes written to the output buffer
     * @throws CryptoException
     */
    public short doFinal(byte[] inBuff, short inOffset, short inLength, byte[] outBuff, short outOffset)
            throws CryptoException {
        byte[] msgBuf;
        short msgOff;
        short msgLen;
        short outLen;
        // check initialized
        if(!mInitialized) {
            CryptoException.throwIt(CryptoException.INVALID_INIT);
        }
        // check if we already have data in the buffer
        if(mBufPtr > 0) {
            // yes - must use the input buffer
            update(inBuff, inOffset, inLength, outBuff, outOffset);
            msgBuf = mBuffer;
            msgOff = short0;
            msgLen = mBufPtr;
        } else {
            // no - operate directly on the input
            msgBuf = inBuff;
            msgOff = inOffset;
            msgLen = inLength;
        }
        // perform the operation
        switch(mMode) {
            case MODE_ENCRYPT:
                outLen = performEncrypt(msgBuf, msgOff, msgLen, outBuff, outOffset);
                break;
            case MODE_DECRYPT:
                outLen = performDecrypt(msgBuf, msgOff, msgLen, outBuff, outOffset);
                break;
            default:
                CryptoException.throwIt(CryptoException.INVALID_INIT);
                return 0;
        }
        // burn the evidence
        Util.arrayFillNonAtomic(mTemp, short0, (short) mTemp.length, (byte)0);
        // reset the buffer pointer
        mBufPtr = 0;
        // return output length
        return outLen;
    }

    private short performEncrypt(byte[] inBuf, short inOff, short inLen,
                                byte[] outBuf, short outOff)
            throws CryptoException {
        // length of hash
        byte hLen = mHash.getLength();
        // length of RSA modulus in octets
        short kLen = (short)(mBits / 8);
        // length of output
        short outLen = (short)(kLen - 1);
        // maximum length of message
        short maxLen = (short)(kLen - 2 * hLen - 2);
        // length of DB
        short dbLen = (short)(kLen - hLen - 1);
        // length of pad (to compensate for smaller message size)
        short padLen = (short)(maxLen - inLen);
        // offset of seed in mTemp
        short seedOffset = 0;
        // offset of db in mTemp
        short dbOffset = hLen;

        // check message length
        if(inLen > maxLen) {
            CryptoException.throwIt(CryptoException.ILLEGAL_VALUE);
        }

        // generate seed at seedOffset
        mRandom.generateData(mTemp, seedOffset, hLen);

        // build DB [ hashLabel | padZeroes | 0x01 | input ] at dbOffset
        short dbPosn = dbOffset;
        // compute and insert hashLabel
        dbPosn = Util.arrayCopyNonAtomic(mLabelHash, short0, mTemp, dbPosn, hLen);
        // insert padding
        dbPosn = Util.arrayFillNonAtomic(mTemp, dbPosn, padLen, (byte)0);
        // insert marker
        mTemp[dbPosn++] = 1;
        // copy input message
        dbPosn = Util.arrayCopyNonAtomic(inBuf, inOff, mTemp, dbPosn, inLen);
        // sanity check
        if(dbPosn != ((short)(dbOffset + dbLen))) {
            ISOException.throwIt(ISO7816.SW_UNKNOWN);
        }

        // generate mask for DB (using seed)
        mMGF.applyMask(mTemp, seedOffset, hLen,
                mTemp, dbOffset, dbLen);

        // generate mask for seed (using masked DB)
        mMGF.applyMask(mTemp, dbOffset, dbLen,
                mTemp, seedOffset, hLen);

        // perform the final RSA operation
        outLen = mRSA.doFinal(mTemp, short0, outLen, outBuf, outOff);

        // return output length
        return outLen;
    }

    private short performDecrypt(byte[] inBuf, short inOff, short inLen,
                                byte[] outBuf, short outOff)
            throws CryptoException {
        // length of hash
        byte hLen = mHash.getLength();
        // length of RSA modulus in octets
        short kLen = (short)(mBits / 8);
        // length of mask
        short dbLen = (short)(kLen - hLen - 1);
        // handy constants
        short seedOffset = 0;
        short dbOffset = (short)(seedOffset + hLen);

        // perform the RSA decryption
        mRSA.doFinal(inBuf, inOff, inLen, mTemp, short0);

        // recover seed mask and unmask seed (using masked DB)
        mMGF.applyMask(mTemp, dbOffset, dbLen,
                mTemp, seedOffset, hLen);

        // recover DB mask and unmask DB (using unmasked seed)
        mMGF.applyMask(mTemp, seedOffset, hLen,
                mTemp, dbOffset, dbLen);

        // perform checks
        short outLen = checkDecrypt(mTemp, dbOffset, dbLen);
        if(outLen < 0) {
            CryptoException.throwIt(CryptoException.ILLEGAL_VALUE);
        }

        // copy data to output buffer
        short msgOff = (short)(inLen - outLen - 1);
        Util.arrayCopyNonAtomic(mTemp, msgOff, outBuf, outOff, outLen);

        // return length of message
        return outLen;
    }

    private short checkDecrypt(byte[] msg, short dbOff, short dbLen) {
        short hLen = mHash.getLength();
        short length = -1;
        boolean failed = false;

        // find start of message, determining length
        for(short i = hLen; i < dbLen; i++) {
            short o = (short)(dbOff + i);
            if(msg[o] == 1) {
                short newLength = (short) (dbLen - i - 1);
                if(length == -1) {
                    length = newLength;
                }
                continue;
            }
            if(msg[o] == 0) {
                continue;
            }
            if(length == -1) {
                failed |= true;
            }
        }

        // check the label hash
        failed |= (Util.arrayCompare(mLabelHash, short0, msg, dbOff, hLen) != 0);

        // return result, length or -1 for failure
        return failed ? -1 : length;
    }

}
