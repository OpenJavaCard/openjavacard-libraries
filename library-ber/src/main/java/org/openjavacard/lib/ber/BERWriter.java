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

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;

public class BERWriter {

    /** Fixed: maximum number of tags */
    private final byte mMaxTags;
    /** Fixed: maximum depth of hierarchy */
    private final byte mMaxDepth;

    /** Transient: state variables */
    private final short[]  mVars;
    /** Number of transient variables */
    private static final byte NUM_VAR = 5;
    /** Variable: maximum allowed length */
    private static final byte VAR_MAX_LENGTH = 0;
    /** Variable: current running length */
    private static final byte VAR_LENGTH     = 1;
    /** Variable: current depth */
    private static final byte VAR_DEPTH      = 2;
    /** Variable: current index */
    private static final byte VAR_INDEX      = 3;
    /** Variable: current temp offset */
    private static final byte VAR_TMP        = 4;

    /** Transient: stack for tags */
    private final short[]  mTagStk;
    /** Transient: stack for tag depths */
    private final byte[]   mDepStk;
    /** Transient: stack for tag buffers */
    private final Object[] mBufStk;
    /** Transient: stack for tag buffer offsets */
    private final short[]  mOffStk;
    /** Transient: stack for tag buffer lengths */
    private final short[]  mLenStk;
    /** Transient: temporary storage */
    private final byte[]   mTmp;

    /**
     * Construct a persistent-state BER writer
     * @param maxTags
     * @param maxDepth
     */
    public BERWriter(byte maxTags, byte maxDepth) {
        mMaxTags = maxTags;
        mMaxDepth = maxDepth;
        mVars = new short[NUM_VAR];
        mTagStk = new short[maxTags];
        mDepStk = new byte[maxTags];
        mBufStk = new Object[maxTags];
        mOffStk = new short[maxTags];
        mLenStk = new short[maxTags];
        mTmp = new byte[128];
    }

    /**
     * Construct a transient-state BER writer
     * @param maxTags
     * @param maxDepth
     * @param clearOn
     */
    public BERWriter(byte maxTags, byte maxDepth, byte clearOn) {
        mMaxTags = maxTags;
        mMaxDepth = maxDepth;
        mVars = JCSystem.makeTransientShortArray(NUM_VAR, clearOn);
        mTagStk = JCSystem.makeTransientShortArray(maxTags, clearOn);
        mDepStk = JCSystem.makeTransientByteArray(maxTags, clearOn);
        mBufStk = JCSystem.makeTransientObjectArray(maxTags, clearOn);
        mOffStk = JCSystem.makeTransientShortArray(maxTags, clearOn);
        mLenStk = JCSystem.makeTransientShortArray(maxTags, clearOn);
        mTmp = JCSystem.makeTransientByteArray((short)128, clearOn);
    }

    /**
     * Return accumulated length
     *
     * Only valid when at top level.
     *
     * @return number of bytes currently used
     */
    public short getCurrentLength() {
        return mVars[VAR_LENGTH];
    }

    /**
     * Return remaining space
     *
     * Only valid when at top level.
     *
     * @return number of bytes remaining
     */
    public short getCurrentSpace() {
        return (short)(mVars[VAR_MAX_LENGTH] - mVars[VAR_LENGTH]);
    }

    /**
     * Return current nesting depth
     * @return tag nesting depth
     */
    public byte getCurrentDepth() {
        return (byte)mVars[VAR_DEPTH];
    }

    /**
     * Return current tag count
     * @return number of tags
     */
    public byte getCurrentCount() {
        return (byte)mVars[VAR_INDEX];
    }

    /**
     * Begin constructing BER data
     * @param maxLength maximum output length
     */
    public final void begin(short maxLength) {
        mVars[VAR_MAX_LENGTH] = maxLength;
        mVars[VAR_LENGTH]  = 0;
        mVars[VAR_DEPTH]   = 0;
        mVars[VAR_INDEX]   = 0;
        mVars[VAR_TMP]     = 0;
    }

    /**
     * Build a primitive BER tag
     * @param tag
     * @param buf
     * @param off
     * @param len
     */
    public final void buildPrimitive(short tag, byte[] buf, short off, short len) {
        byte current = (byte)mVars[VAR_INDEX];
        byte depth = (byte)mVars[VAR_DEPTH];
        short totalLength = (short)(BERTag.tagSize(tag) + BERLength.lengthSize(len) + len);
        // check limits
        if(current == mMaxTags) {
            error();
        }
        // check that we have only one top node
        checkSingleToplevel();
        // check for available space
        checkSpace(totalLength);
        // push everything
        mTagStk[current] = tag;
        mDepStk[current] = depth;
        mBufStk[current] = buf;
        mOffStk[current] = off;
        mLenStk[current] = len;
        // increment counters
        mVars[VAR_LENGTH] += totalLength;
        mVars[VAR_INDEX]++;
    }

    private short putTemp(short count) {
        short offset = mVars[VAR_TMP];
        short after = (short)(offset + count);
        if(after > mTmp.length) {
            error();
        }
        mVars[VAR_TMP] = after;
        return offset;
    }

    /**
     * Build a primitive BER tag with a byte value
     * @param tag
     * @param value
     */
    public final void primitiveByte(short tag, byte value) {
        short len = (short)1;
        short tmpOff = putTemp(len);
        mTmp[tmpOff] = value;
        buildPrimitive(tag, mTmp, tmpOff, len);
    }

    /**
     * Build a primitive BER tag with a short value
     * @param tag
     * @param value
     */
    public final void primitiveShort(short tag, short value) {
        short len = (short)2;
        short tmpOff = putTemp(len);
        Util.setShort(mTmp, tmpOff, value);
        buildPrimitive(tag, mTmp, tmpOff, len);
    }

    /**
     * Build a primitive BER tag with given value
     * @param tag
     * @param buf
     * @param off
     * @param len
     */
    public final void primitiveBytes(short tag, byte[] buf, short off, short len) {
        short tmpOff = putTemp(len);
        Util.arrayCopyNonAtomic(buf, off, mTmp, tmpOff, len);
        buildPrimitive(tag, mTmp, tmpOff, len);
    }

    /**
     * Begin building a constructed BER tag
     * @param tag
     */
    public final void beginConstructed(short tag) {
        byte current = (byte)mVars[VAR_INDEX];
        byte depth = (byte)mVars[VAR_DEPTH];
        // check limits
        if(current == mMaxTags || depth == mMaxDepth) {
            error();
        }
        // check that we have only one top node
        checkSingleToplevel();
        // set constructed flag in tag
        tag = BERTag.tagAsConstructed(tag);
        // push everything
        mTagStk[current] = tag;
        mDepStk[current] = depth;
        mBufStk[current] = null;
        mOffStk[current] = 0;
        mLenStk[current] = 0;
        // increment counters
        mVars[VAR_INDEX]++;
        mVars[VAR_DEPTH]++;
    }

    /**
     * Finish building a constructed BER tag
     */
    public final void endConstructed() {
        short last = (short)(mVars[VAR_INDEX] - 1);
        short depth = mVars[VAR_DEPTH];
        short start = -1;
        short valueLength = 0;
        // check that we are in a constructed node
        if(mVars[VAR_DEPTH] <= 0) {
            error();
        }
        // iterate through tags backwards until we hit
        // the parent node. count child sizes while doing so.
        for(short i = last; i >= 0; i--) {
            short tagDep = mDepStk[i];
            short tagTag = mTagStk[i];
            short tagLen = mLenStk[i];
            // ignore children of children since
            // they have already been accounted for.
            if(tagDep > depth) {
                continue;
            }
            // immediate children get counted
            if(tagDep == depth) {
                valueLength += BERTag.tagSize(tagTag);
                valueLength += BERLength.lengthSize(tagLen);
                valueLength += tagLen;
            }
            // found the parent - we are done
            if(tagDep < depth) {
                start = i;
                break;
            }
        }
        // get tag for parent
        short tag = mTagStk[start];
        // compute header length and check
        short headerLength = (short)(BERTag.tagSize(tag) + BERLength.lengthSize(valueLength));
        checkSpace(headerLength);
        // remember length of children
        mLenStk[start] = valueLength;
        // add what is left of the length
        mVars[VAR_LENGTH] += headerLength;
        // back up in depth
        mVars[VAR_DEPTH]--;
    }

    /**
     * Emit all the prepared data
     *
     * @param buf to write to
     * @param off to write at
     * @param len of available space
     * @return length of data
     */
    public final short finish(byte[] buf, short off, short len) {
        short cur = off;
        // check that we are at top level
        if(mVars[VAR_DEPTH] != 0) {
            error();
        }
        // check total length
        if(mVars[VAR_LENGTH] > len) {
            error();
        }
        // iterate all tags in forward direction
        for(short i = 0; i < mVars[VAR_INDEX]; i++) {
            short tag = mTagStk[i];
            Object tagBufObj = mBufStk[i];
            short tagOff = mOffStk[i];
            short tagLen = mLenStk[i];
            // put the tag
            cur = BERTag.putTag(buf, cur, tag);
            // put the length
            cur = BERLength.putLength(buf, cur, tagLen);
            // put the data, if present
            if(tagBufObj != null) {
                byte[] tagBuf = (byte[])tagBufObj;
                Util.arrayCopyNonAtomic(tagBuf, tagOff, buf, cur, tagLen);
                cur += tagLen;
            }
        }
        // compute the total length
        short actualLength = (short)(cur - off);
        // check that it agrees with state
        if(mVars[VAR_LENGTH] != actualLength) {
            error();
        }
        // return the length
        return actualLength;
    }

    /**
     * Emit prepared data into APDU buffer and send it
     *
     * @param apdu to use for sending
     * @return length of sent data
     */
    public final short finishAndSend(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short len = finish(buf, (short)0, (short)buf.length);
        apdu.setOutgoingAndSend((short)0, len);
        return len;
    }

    private void checkSingleToplevel() {
        if(mVars[VAR_DEPTH] == 0 && mVars[VAR_INDEX] > 0) {
            error();
        }
    }

    private void checkSpace(short len) {
        short newLength = (short)(mVars[VAR_LENGTH] + len);
        if(newLength > mVars[VAR_MAX_LENGTH]) {
            error();
        }
    }

    private void error() {
        ISOException.throwIt(ISO7816.SW_UNKNOWN);
    }

}
