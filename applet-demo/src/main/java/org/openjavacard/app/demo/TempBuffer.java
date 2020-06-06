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

    private final short[] mVars;
    private final static short VAR_FILL = 0;
    private final static short NUM_VARS = 1;

    public TempBuffer(short size) {
        mBuffer = new byte[size];
        mVars = new short[NUM_VARS];
    }

    public TempBuffer(short size, byte clearOn) {
        mBuffer = JCSystem.makeTransientByteArray(size, clearOn);
        mVars = JCSystem.makeTransientShortArray(NUM_VARS, clearOn);
    }

    private void fault() {
        ISOException.throwIt(ISO7816.SW_UNKNOWN);
    }

    private short checkSpace(short request) {
        short fill = mVars[VAR_FILL];
        short newOffset = (short)(fill + request);
        if(newOffset < 0) {
            fault();
        }
        if(newOffset > mBuffer.length) {
            fault();
        }
        return fill;
    }

    public byte[] getBuffer() {
        return mBuffer;
    }

    public short getFill() {
        return mVars[VAR_FILL];
    }

    public short getSpace() {
        return (short)(mBuffer.length - mVars[VAR_FILL]);
    }

    public void clear() {
        Util.arrayFillNonAtomic(mBuffer, (short)0, (short)mBuffer.length, (byte)0);
        mVars[VAR_FILL] = 0;
    }

    public short put(byte b) {
        short fill = checkSpace((short)1);
        mBuffer[fill++] = b;
        mVars[VAR_FILL] = fill;
        return fill;
    }

    public short put(short b) {
        short fill = checkSpace((short)2);
        fill = Util.setShort(mBuffer, fill, b);
        mVars[VAR_FILL] = fill;
        return fill;
    }

    public short put(byte[] buf, short off, short len) {
        short fill = checkSpace(len);
        Util.arrayCopyNonAtomic(buf, off, mBuffer, fill, len);
        fill += len;
        mVars[VAR_FILL] = fill;
        return fill;
    }

    public short put(byte[] buf) {
        return put(buf, (short)0, (short)buf.length);
    }

}
