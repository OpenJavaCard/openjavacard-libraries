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

package org.openjavacard.lib.ber;

import javacard.framework.Util;

public final class BERLength {

    private static final byte LENGTH_LONG_FLAG = (byte)0x80;
    private static final byte LENGTH_SIZE_MASK = (byte)0x7F;

    /** @return true if the given first byte indicates short form */
    public static final boolean isShortForm(byte firstByte) {
        return (firstByte & LENGTH_LONG_FLAG) == 0;
    }

    /** @return true if the given first byte indicates long form */
    public static final boolean isLongForm(byte firstByte) {
        return (firstByte & LENGTH_LONG_FLAG) != 0;
    }

    /** @return length represented by the given short-form first byte */
    public static final byte shortFormLength(byte firstByte) {
        return (byte)(firstByte & LENGTH_SIZE_MASK);
    }

    /** @return length of long-form length indicated by given first byte */
    public static final byte longFormBytes(byte firstByte) {
        return (byte)(firstByte & LENGTH_SIZE_MASK);
    }

    /** @return number of bytes required to represent given length */
    public static final byte lengthSize(short length) {
        if(length <= 127) {
            return 1;
        } else {
            return 3;
        }
    }

    /** Put a length into the given buffer */
    public static final short putLength(byte[] buf, short off, short length) {
        if(length <= 127) {
            buf[off++] = (byte)length;
        } else {
            buf[off++] = (byte)0x82;
            off = Util.setShort(buf, (short)(off + 1), length);
        }
        return off;
    }

}
