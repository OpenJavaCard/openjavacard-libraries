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

import javacard.framework.Util;

public class DebugLog {

    private short mFill;
    private Object[] mMessages;

    DebugLog() {
        mFill = 0;
        mMessages = new Object[64];
    }

    byte[] getRecord(short index) {
        return (byte[])mMessages[index];
    }

    void record(byte[] buf, short off, short len) {
        byte[] rec = new byte[len];
        Util.arrayCopyNonAtomic(buf, off, rec, off, len);
        mMessages[mFill++] = rec;
        if(mFill == mMessages.length) {
            mFill = 0;
        }
    }

}
