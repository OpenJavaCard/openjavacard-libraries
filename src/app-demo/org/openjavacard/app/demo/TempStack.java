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

package org.openjavacard.app.demo;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;

public class TempStack {

    private final short mMaxObjects;
    private final short mMaxBytes;

    private short mUsage;
    private short mCurrent;

    private final byte[]  mBuffer;
    private final short[] mOffsets;
    private final short[] mLengths;

    public TempStack(short maxObjects, short maxBytes, byte clearOn) {
        mMaxObjects = maxObjects;
        mMaxBytes = maxBytes;
        mUsage = 0;
        mCurrent = 0;
        mBuffer = JCSystem.makeTransientByteArray(maxBytes, clearOn);
        mOffsets = JCSystem.makeTransientShortArray(maxObjects, clearOn);
        mLengths = JCSystem.makeTransientShortArray(maxObjects, clearOn);
    }

    private void fault() {
        ISOException.throwIt(ISO7816.SW_UNKNOWN);
    }

    public byte[] getBuffer() {
        return mBuffer;
    }

    public short allocate(short length) {
        short newUsage = (short)(mUsage + length);
        if(mUsage >= mMaxBytes) {
            fault();
        }
        if(mCurrent >= mMaxObjects) {
            fault();
        }
        if(newUsage > mMaxBytes) {
            fault();
        }
        short result = mUsage;
        mOffsets[mCurrent] = mUsage;
        mLengths[mCurrent] = length;
        mUsage = newUsage;
        mCurrent++;
        return result;
    }

    public void free(short offset, short length) {
        if(mCurrent <= 0) {
            fault();
        }
        if(mOffsets[mCurrent] != offset) {
            fault();
        }
        if(mLengths[mCurrent] != length) {
            fault();
        }
        Util.arrayFillNonAtomic(mBuffer, offset, length, (byte)0);
        mOffsets[mCurrent] = 0;
        mLengths[mCurrent] = 0;
        mUsage -= length;
        mCurrent--;
    }

}
