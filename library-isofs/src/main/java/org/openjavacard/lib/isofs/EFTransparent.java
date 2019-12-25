/*
 * openjavacard-libraries: Class libraries for JavaCard
 * Copyright (C) 2019 Ingo Albrecht <copyright@promovicz.org>
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

package org.openjavacard.lib.isofs;

import javacard.framework.ISOException;
import javacard.framework.Util;

public class EFTransparent extends EF {

    private final byte[] mData;

    EFTransparent(DF parent, byte fdb, short fid, byte sfi, short maxLength) {
        super(parent, fdb, fid, sfi);
        mData = new byte[maxLength];
    }

    public short getLength() {
        return (short)mData.length;
    }

    public byte[] getData() {
        return mData;
    }

    public void readData(short fileOff, byte[] dstBuf, short dstOff, short dstLen) {
        Util.arrayCopy(mData, fileOff, dstBuf, dstOff, dstLen);
    }

    public void updateData(short fileOff, byte[] srcBuf, short srcOff, short srcLen) {
        Util.arrayCopy(srcBuf, srcOff, mData, fileOff, srcLen);
    }

    public void writeData(short fileOff, byte[] srcBuf, short srcOff, short srcLen) {
        byte write = (byte)(mDCB & DCB_WRITE_MASK);
        short i;
        switch(write) {
            case DCB_WRITE_ONCE:
                // XXX
                break;
            case DCB_WRITE_PROPRIETARY:
                ISOException.throwIt(SW_COMMAND_NOT_ALLOWED);
                break;
            case DCB_WRITE_OR:
                for(i = 0; i < srcLen; i++) {
                    mData[(short)(fileOff+i)] |= srcBuf[(short)(srcOff+i)];
                }
                break;
            case DCB_WRITE_AND:
                for(i = 0; i < srcLen; i++) {
                    mData[(short)(fileOff+i)] &= srcBuf[(short)(srcOff+i)];
                }
                break;
        }
    }

    public void eraseData(short startOff, short endOff) {
        for(short off = startOff; off < endOff; off++) {
            mData[off] = 0;
        }
    }

}
