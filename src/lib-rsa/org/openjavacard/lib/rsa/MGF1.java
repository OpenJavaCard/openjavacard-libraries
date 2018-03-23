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

import javacard.framework.JCSystem;
import javacard.framework.Util;
import javacard.security.CryptoException;
import javacard.security.MessageDigest;

/**
 * Implementation of PKCS #1 v2.1 MGF-1
 *
 * This is a mask generation function used by OAEP and PSS.
 *
 * It can be used with any message digest, usually SHA-1 or SHA-256.
 */
public class MGF1 {

    private static final short short0 = (short)0;

    /** Internal hash */
    private final MessageDigest mHash;
    /** Temporary buffer */
    private final byte[] mTemp;

    /**
     * Creates a new MGF1 mask generator
     *
     * The generator will use the provided hash.
     *
     * @param hash to use for mask generation
     */
    public MGF1(MessageDigest hash) {
        mHash = hash;
        mTemp = JCSystem.makeTransientByteArray(mHash.getLength(), JCSystem.CLEAR_ON_DESELECT);
    }

    /**
     * Combined mask generation function and XOR (MGF1-XOR)
     *
     * This is equivalent to generating a mask using MGF1
     * and applying it to the output buffer using XOR.
     *
     * Doing this in one step saves an additional buffer.
     *
     * @param seed buffer for mask generation
     * @param seedOff offset of seed in buffer
     * @param seedLen length of seed in buffer
     * @param out buffer for generated mask
     * @param outOff offset of output in buffer
     * @param outLen length of output in buffer
     * @throws CryptoException on internal errors
     */
    public void applyMask(byte[] seed, short seedOff, short seedLen, byte[] out, short outOff, short outLen)
            throws CryptoException {
        // length of hash (octets)
        byte hLen = mHash.getLength();
        // determine number of rounds required to fill OUT
        short rounds = shortCeil(outLen, hLen);
        // iteration: remaining bytes for current round
        short outRemaining = outLen;
        // iteration: output position for current round
        short outPosition = outOff;
        // reset the hash
        mHash.reset();
        // run rounds
        for(short counter = 0; counter < rounds; counter++) {
            // compute number of bytes for current round
            short outBytes = shortMin(hLen, outRemaining);
            // update in the counter
            mTemp[0] = 0;
            mTemp[1] = 0;
            mTemp[2] = (byte)((counter >> 8) & 0xFF);
            mTemp[3] = (byte)((counter >> 0) & 0xFF);
            // compute the round hash from seed and counter
            mHash.update(seed, seedOff, seedLen);
            mHash.doFinal(mTemp, short0, (short) 4, mTemp, short0);
            // xor round hash to output array
            xorInPlace(out, outPosition, mTemp, short0, outBytes);
            // advance iteration
            outPosition += outBytes;
            outRemaining = shortMax((short)0, (short)(outRemaining - hLen));
        }
        // burn the evidence
        Util.arrayFillNonAtomic(mTemp, short0, (short) mTemp.length, (byte) 0);
    }

    private static short shortMin(short a, short b) {
        if(a < b) {
            return a;
        } else {
            return b;
        }
    }

    private static short shortMax(short a, short b) {
        if(a > b) {
            return a;
        } else {
            return b;
        }
    }

    /**
     * Implementation of ceiling division for short integers
     *
     * @param dividend
     * @param divisor
     * @return ceiled quotient
     */
    private static short shortCeil(short dividend, short divisor) {
        return (short)((dividend+(divisor-1)) / divisor);
    }

    /**
     * XOR a buffer onto another
     *
     * @param out
     * @param outOffset
     * @param in
     * @param inOffset
     * @param length
     */
    private static void xorInPlace(byte[] out, short outOffset, byte[] in, short inOffset, short length) {
        for(short i = 0; i < length; i++) {
            out[outOffset + i] ^= in[inOffset + i];
        }
    }

}
