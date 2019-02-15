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

import javacard.framework.APDU;

public class AuthMethodMedium extends AuthMethod {

    public static AuthMethodMedium getContactInstance() {
        return new AuthMethodMedium(APDU.PROTOCOL_MEDIA_DEFAULT);
    }

    public static AuthMethodMedium getContactlessInstance() {
        return new AuthMethodMedium(APDU.PROTOCOL_MEDIA_CONTACTLESS_TYPE_A, APDU.PROTOCOL_MEDIA_CONTACTLESS_TYPE_B);
    }

    private final byte[] mAllowedMedia;

    public AuthMethodMedium(byte allowedMedium) {
        mAllowedMedia = new byte[] {allowedMedium};
    }

    public AuthMethodMedium(byte allowedMediumA, byte allowedMediumB) {
        mAllowedMedia = new byte[] {allowedMediumA, allowedMediumB};
    }

    public boolean verify() {
        boolean result = false;
        byte medium = (byte)(APDU.getProtocol() & APDU.PROTOCOL_MEDIA_MASK);
        for(short idx = 0; idx < mAllowedMedia.length; idx++) {
            if(medium == mAllowedMedia[idx]) {
                result |= true;
            }
        }
        return result;
    }

}
