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

package org.openjavacard.lib.auth;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;

public class AuthContext {

    private final static byte MAX_METHODS = 16;

    private final AuthMethod[] mMethods;

    public AuthContext() {
        mMethods = new AuthMethod[MAX_METHODS];
    }

    public byte addMethod(AuthMethod method) {
        return (byte)0;
    }

    public void require(short mask) {
        boolean success = true;
        for(byte i = 0; i < MAX_METHODS; i++) {
            short flag = (short)(1 << i);
            boolean required = ((short)(flag & mask)) != 0;
            AuthMethod method = mMethods[i];
            boolean okay = method.verify();
            success &= okay || !required;
        }
        if(!success) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
    }

    public void requireAndDerive(short mask,
                                 byte[] saltBuf, short saltOff, short saltLen,
                                 byte[] dkeyBuf, short dkeyOff, short dkeyLen) {
        require(mask);
    }

}
