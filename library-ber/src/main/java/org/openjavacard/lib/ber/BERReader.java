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

package org.openjavacard.lib.ber;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;

/**
 * Reader for data coded in SC-BER
 * <p/>
 * This is an intricately designed BER parser for JavaCard that
 * supports full non-strict parsing of some variant of BER.
 * <p/>
 * The interface is based on a callback interface, allowing a
 * client of this utility to parse a complete recursive TLV
 * structure without any memory allocation.
 * <p/>
 * It is strongly recommended to keep instances of this class
 * permanently available by using a global reference, as with all
 * such stateful objects on JavaCard. Doing so prevents memory
 * allocation, which should improve card life and be safer from
 * a security standpoint.
 * <p/>
 * All buffer handling is performed via a stack-based reference to
 * the buffer. This allows parsing directly from the APDU buffer,
 * which can not be referenced even from transient arrays.
 * <p/>
 * All internal state is kept in transient variables to prevent state
 * leaks and to harden this code against memory-based DoS.
 * <p/>
 */
public final class BERReader {

    /* Fixed: maximum depth allowed */
    private final byte mMaxDepth;

    /* Transient: parser variables */
    private final short[] mVars;
    /* Number of transient variables */
    private static final byte NUM_VAR = 4;
    /* Variable: initial offset into buffer */
    private static final byte VAR_BUF_OFF = 0;
    /* Variable: initial length of buffer */
    private static final byte VAR_BUF_LEN = 1;
    /* Variable: current parsing position */
    private static final byte VAR_POSN    = 2;
    /* Variable: current recursion depth */
    private static final byte VAR_DEPTH   = 3;

    /* Transient: parser tag stack */
    private final short[] mTagStk;
    /* Transient: parser return stack */
    private final short[] mOffStk;

    /**
     * Construct transient-state BER reader
     * @param maxDepth maximum depth of TLV structures
     */
    public BERReader(byte maxDepth, byte clearOn) {
        mMaxDepth = maxDepth;
        mVars = JCSystem.makeTransientShortArray(NUM_VAR, clearOn);
        mTagStk = JCSystem.makeTransientShortArray(maxDepth, clearOn);
        mOffStk = JCSystem.makeTransientShortArray(maxDepth, clearOn);
    }

    /**
     * Parse a block of BER data
     * @param buf to parse from
     * @param off to start at
     * @param len of data
     * @param handler to call with results
     */
    public final void parse(byte[] buf, short off, short len, BERHandler handler) {
        mVars[VAR_BUF_OFF] = off;
        mVars[VAR_BUF_LEN] = len;
        mVars[VAR_POSN] = 0;
        mVars[VAR_DEPTH] = 0;
        parseOne(buf, handler);
        if(mVars[VAR_POSN] != len) {
            parseError();
        }
    }

    /**
     * Internal: parse one contiguous BER block recursively
     * @param buf we are reading from
     * @param handler to call for every element
     */
    private void parseOne(byte[] buf, BERHandler handler) {
        /* read the tag */
        short t = readTag(buf);
        /* read the length */
        short l = readLength(buf);
        /* compute the end offset for this tag */
        short e = (short)(mVars[VAR_POSN] + l);

        /* check that we have enough data */
        checkLength(l);

        /* push state */
        mTagStk[mVars[VAR_DEPTH]] = t;
        mOffStk[mVars[VAR_DEPTH]] = (short)(mVars[VAR_POSN] + l);

        /* perform processing */
        if(BERTag.isPrimitive(t)) {
            /* call handler */
            if(!handler.handlePrimitive(this, (byte)mVars[VAR_DEPTH], t,
                                            buf, mVars[VAR_POSN], l)) {
                parseError();
            }
        } else {
            /* call begin handler */
            if(!handler.handleBeginConstructed(this, (byte)mVars[VAR_DEPTH], t)) {
                parseError();
            }

            /* parse children */
            mVars[VAR_DEPTH]++;

            /* check for maximum depth */
            if(mVars[VAR_DEPTH] == mMaxDepth) {
                parseError();
            }

            /* parse children */
            while(mVars[VAR_POSN] < e) {
                parseOne(buf, handler);
                if(mVars[VAR_POSN] > e) {
                    parseError();
                }
            }

            /* done with children*/
            mVars[VAR_DEPTH]--;

            /* call finish handler */
            if(!handler.handleFinishConstructed(this, (byte)mVars[VAR_DEPTH], t)) {
                parseError();
            }
        }

        /* pop state */
        mOffStk[mVars[VAR_DEPTH]] = 0;
        mTagStk[mVars[VAR_DEPTH]] = 0;

        /* advance position to after this tag */
        mVars[VAR_POSN] = e;
    }

    /**
     * Internal: throw a parse error
     *
     * We always throw SW=[data invalid].
     */
    private void parseError() {
        ISOException.throwIt(ISO7816.SW_DATA_INVALID);
    }

    /**
     * Internal: check number of available bytes
     * @param length required in bytes
     */
    private void checkLength(short length) {
        short newCur = (short)(mVars[VAR_POSN] + length);
        if(newCur < 0 || newCur > mVars[VAR_BUF_LEN]) {
            parseError();
        }
    }

    /**
     * Internal: read one byte at the current position
     * @param buf that we are reading from
     * @return the byte
     */
    private byte readByte(byte[] buf) {
        short newCur = (short)(mVars[VAR_POSN] + 1);
        if(newCur < 0 || newCur > mVars[VAR_BUF_LEN]) {
            parseError();
        }
        byte result = buf[(short)(mVars[VAR_BUF_OFF] + mVars[VAR_POSN])];
        mVars[VAR_POSN] = newCur;
        return result;
    }

    /**
     * Internal: read TLV tag at current position
     * @param buf that we are reading from
     * @return the tag
     */
    private short readTag(byte[] buf) {
        byte b0 = readByte(buf);
        byte b1 = 0;
        if(BERTag.byteIsLongForm(b0)) {
            b1 = readByte(buf);
            if(!BERTag.byteIsLast(b1)) {
                parseError();
            }
        }
        return (short)((b0 << 8) | b1);
    }

    /**
     * Internal: read TLV length at current position
     * @param buf that we are reading from
     * @return the length
     */
    private short readLength(byte[] buf) {
        short result = -1;

        // read the first byte
        byte b0 = readByte(buf);

        // check for short form
        if(BERLength.isShortForm(b0)) {
            // process short form
            result = BERLength.shortFormLength(b0);
        } else {
            // process long form
            byte bc = BERLength.longFormBytes(b0);
            if (bc == 1) {
                // read the length and cast it
                result = (short) readByte(buf);
            } else if (bc == 2) {
                // read the length
                byte b1 = readByte(buf);
                byte b2 = readByte(buf);
                // combine and cast
                result = (short) ((b1 << 8) | (b2));
            } else {
                parseError();
            }
        }

        // check for overflow
        if(result < 0) {
            parseError();
        }

        return result;
    }

}
