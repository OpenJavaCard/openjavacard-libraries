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

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;

public class LongNum {

    private final byte[] mBuf;
    private final short mOff;
    private final short mLen;

    public LongNum(short len) {
        mBuf = new byte[len];
        mOff = 0;
        mLen = len;
    }

    public LongNum(short len, byte clearOn) {
        mBuf = JCSystem.makeTransientByteArray(len, clearOn);
        mOff = 0;
        mLen = len;
    }

    public LongNum(byte[] buf, short off, short len) {
        mBuf = buf;
        mOff = off;
        mLen = len;
    }

    public byte[] getBuffer() {
        return mBuf;
    }

    public short getOffset() {
        return mOff;
    }

    public short getLength() {
        return mLen;
    }

    private void error() {
        ISOException.throwIt(ISO7816.SW_DATA_INVALID);
    }

    public void clear() {
        Util.arrayFillNonAtomic(mBuf, (short)0, mLen, (byte)0);
    }

    public void get(byte[] buf, short off, short len) {
        if(len != mLen) {
            error();
        }
        Util.arrayCopyNonAtomic(mBuf, (short)0, buf, off, len);
    }

    public short getSignificantLength() {
        short result = 0;
        for(short i = 0; i < mLen; i++) {
            byte n = mBuf[i];
            if(n != 0) {
                result = (short)(mLen - i);
            }
        }
        return result;
    }

    public void getSignificant(byte[] buf, short off, short len) {
        short sigLen = getSignificantLength();
        short cutOff = (short)(mLen - len);
        if(len < sigLen) {
            error();
        }
        Util.arrayCopyNonAtomic(mBuf, cutOff, buf, off, len);
    }

    public void set(byte v) {
        clear();
        mBuf[(short)(mLen - 1)] = v;
    }

    public void set(byte[] buf, short off, short len) {
        if(len > mLen) {
            error();
        }
        clear();
        short shift = (short)(mLen - len);
        Util.arrayCopyNonAtomic(buf, off, mBuf, shift, len);
    }

    public void add(byte b) {
        short carry = (short)(b & 0xFF);
        for(short i = (short)(mLen - 1); i >= 0; i--) {
            short io = (short)(mOff + i);
            short o = (short)(mBuf[io] & 0xFF);
            short n = (short)(o + carry);
            mBuf[io] = (byte)(n & 0xFF);
            if(n > 255) {
                carry = 1;
            } else {
                carry = 0;
            }
        }
    }

    public void sub(byte b) {
        short carry = (short)(b & 0xFF);
        for(short i = (short)(mLen - 1); i >= 0; i--) {
            short io = (short)(mOff + i);
            short o = (short)(mBuf[io] & 0xFF);
            short n = (short)(o - carry);
            mBuf[io] = (byte)n;
            if(n < 0) {
                carry = 1;
            } else {
                carry = 0;
            }
        }
    }

}
