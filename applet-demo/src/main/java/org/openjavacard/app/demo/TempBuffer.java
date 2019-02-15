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

package org.openjavacard.app.demo;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;

public class TempBuffer {

    private final byte[] mBuffer;

    private short mOffset;

    public TempBuffer(short size) {
        mBuffer = new byte[size];
        mOffset = 0;
    }

    public TempBuffer(short size, byte clearOn) {
        mBuffer = JCSystem.makeTransientByteArray(size, clearOn);
        mOffset = 0;
    }

    private void fault() {
        ISOException.throwIt(ISO7816.SW_UNKNOWN);
    }

    private void checkSize(short request) {
        short newOffset = (short)(mOffset + request);
        if(newOffset < 0) {
            fault();
        }
        if(newOffset > mBuffer.length) {
            fault();
        }
    }

    public byte[] getBuffer() {
        return mBuffer;
    }

    public short getLength() {
        return mOffset;
    }

    public void clear() {
        Util.arrayFillNonAtomic(mBuffer, (short)0, (short)mBuffer.length, (byte)0);
        mOffset = 0;
    }

    public void put(byte b) {
        checkSize((short)1);
        mBuffer[mOffset++] = b;
    }

    public void put(short b) {
        checkSize((short)2);
        Util.setShort(mBuffer, mOffset, b);
        mOffset += 2;
    }

    public void put(byte[] buf, short off, short len) {
        checkSize(len);
        Util.arrayCopyNonAtomic(buf, off, mBuffer, mOffset, len);
        mOffset += len;
    }

    public void put(byte[] buf) {
        put(buf, (short)0, (short)buf.length);
    }

}
