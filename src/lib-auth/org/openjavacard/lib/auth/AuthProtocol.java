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

public abstract class AuthProtocol extends AuthMethod {

    public static final byte DEFAULT_MAX_TRIES = 3;

    private final byte mMaxTries;

    private byte mBadTries;

    protected AuthProtocol(byte maxTries) {
        mMaxTries = maxTries;
        mBadTries = 0;
    }

    protected AuthProtocol() {
        this(DEFAULT_MAX_TRIES);
    }

    public boolean verify() {
        return false;
    }

    protected void beginAttempt() {
        if(mBadTries >= mMaxTries) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }
        mBadTries++;
    }

    protected void continueAttempt() {
    }

    protected void attemptFailure() {
        ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
    }

    protected void attemptSuccess() {
        mBadTries = 0;
    }

}
