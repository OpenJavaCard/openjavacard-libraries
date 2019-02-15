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

package org.openjavacard.lib.auth;

import javacard.framework.AID;
import javacard.framework.JCSystem;
import javacard.framework.Util;

public class AuthMethodCaller extends AuthMethod {

    private final byte[] mAID;

    public AuthMethodCaller(byte[] buf, short off, byte len) {
        mAID = new byte[len];
        Util.arrayCopyNonAtomic(buf, off, mAID, (short)0, len);
    }

    public boolean verify() {
        AID client = JCSystem.getPreviousContextAID();
        return client.partialEquals(mAID, (short)0, (byte)mAID.length);
    }

}
