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

package org.openjavacard.svc.debug;

import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.Util;
import org.openjavacard.lib.debug.DebugProtocol;
import org.openjavacard.lib.debug.DebugService;

public class DebugServiceImpl implements DebugService {

    private final DebugApplet mApplet;
    private final byte        mIndex;

    private final byte[] mAIDBuf;
    private final byte   mAIDLen;

    private final byte[] mBuffer;

    DebugServiceImpl(DebugApplet applet, byte index, AID clientAID) {
        mApplet = applet;
        mIndex = index;
        // copy the AID so we do not hold a reference to the client
        mAIDBuf = new byte[16];
        mAIDLen = clientAID.getBytes(mAIDBuf, (short)0);
        // buffer for message assembly - could be global
        mBuffer = new byte[32];
    }

    public byte getIndex() {
        return mIndex;
    }

    public byte[] getAIDBuffer() {
        return mAIDBuf;
    }

    public byte getAIDLength() {
        return mAIDLen;
    }

    public boolean isClient(AID clientAID) {
        return clientAID.equals(mAIDBuf, (short)0, mAIDLen);
    }

    public void logAPDUCommand(byte protocol, APDU apdu) {
        byte[] abuf = apdu.getBuffer();
        byte[] buf = mBuffer;
        short off = 0;
        off = Util.setShort(buf, off, mApplet.generateSeq());
        off = Util.setShort(buf, off, DebugProtocol.MSG_APDU_COMMAND);
        buf[off++] = protocol;
        off = Util.arrayCopyNonAtomic(abuf, (short)0, buf, off, (short)5);
        mApplet.getLog().record(buf, (short)0, off);
    }

    public void logAPDUResponse(APDU apdu) {
        byte[] abuf = apdu.getBuffer();
        byte[] buf = mBuffer;
        short off = 0;
        off = Util.setShort(buf, off, mApplet.generateSeq());
        off = Util.setShort(buf, off, DebugProtocol.MSG_APDU_RESPONSE);
        mApplet.getLog().record(buf, (short)0, off);
    }

    public void logException(short type, short code) {
        byte[] buf = mBuffer;
        short off = 0;
        off = Util.setShort(buf, off, mApplet.generateSeq());
        off = Util.setShort(buf, off, DebugProtocol.MSG_EXCEPTION);
        off = Util.setShort(buf, off, type);
        off = Util.setShort(buf, off, code);
        mApplet.getLog().record(buf, (short)0, off);
    }

    public void logMemory(short persistent, short clearReset, short clearDeselect) {
        byte[] buf = mBuffer;
        short off = 0;
        off = Util.setShort(buf, off, mApplet.generateSeq());
        off = Util.setShort(buf, off, DebugProtocol.MSG_MEMORY);
        off = Util.setShort(buf, off, persistent);
        off = Util.setShort(buf, off, clearReset);
        off = Util.setShort(buf, off, clearDeselect);
        mApplet.getLog().record(buf, (short)0, off);
    }

    public void logMessage(short code, byte[] msgBuf, short msgOff, short msgLen) {
        byte[] buf = mBuffer;
        short off = 0;
        off = Util.setShort(buf, off, mApplet.generateSeq());
        off = Util.setShort(buf, off, DebugProtocol.MSG_MESSAGE);
        off = Util.setShort(buf, off, code);
        if(msgBuf != null && msgLen > 0) {
            short max = (short)(buf.length - off);
            short len = msgLen;
            if(len > max) {
                len = max;
            }
            off = Util.arrayCopyNonAtomic(msgBuf, msgOff, buf, off, len);
        }
        mApplet.getLog().record(buf, (short)0, off);
    }

}
