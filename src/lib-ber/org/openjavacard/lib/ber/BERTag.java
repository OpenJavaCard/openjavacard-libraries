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

public final class BERTag {

    public static final short TYPE_MASK = (short)0x1F00;
    public static final short TYPE_EOC = (short)0x0000;
    public static final short TYPE_BOOLEAN = (short)0x0100;
    public static final short TYPE_INTEGER = (short)0x0200;
    public static final short TYPE_BITSTRING = (short)0x0300;
    public static final short TYPE_OCTETSTRING = (short)0x0400;
    public static final short TYPE_NULL = (short)0x0500;
    public static final short TYPE_OID = (short)0x0600;
    public static final short TYPE_OBJECTDESCRIPTOR = (short)0x0700;
    public static final short TYPE_EXTERNAL = (short)0x0800;
    public static final short TYPE_REAL = (short)0x0900;
    public static final short TYPE_ENUMERATED = (short)0x0A00;
    public static final short TYPE_EMBEDDED_PDV = (short)0x0B00;
    public static final short TYPE_UTF8STRING = (short)0x0C00;
    public static final short TYPE_RELATIVE_OID = (short)0x0D00;
    public static final short TYPE_SEQUENCE = (short)0x1000;
    public static final short TYPE_SET = (short)0x1100;
    public static final short TYPE_NUMERICSTRING = (short)0x1200;
    public static final short TYPE_PRINTABLESTRING = (short)0x1300;
    public static final short TYPE_T61STRING = (short)0x1400;
    public static final short TYPE_VIDEOTEXSTRING = (short)0x1500;
    public static final short TYPE_IA5STRING = (short)0x1600;
    public static final short TYPE_UTCTIME = (short)0x1700;
    public static final short TYPE_GENERALIZEDTIME = (short)0x1800;
    public static final short TYPE_GRAPHICSTRING = (short)0x1900;
    public static final short TYPE_VISIBLESTRING = (short)0x1A00;
    public static final short TYPE_GENERALSTRING = (short)0x1B00;
    public static final short TYPE_UNIVERSALSTRING = (short)0x1C00;
    public static final short TYPE_CHARACTERSTRING = (short)0x1D00;
    public static final short TYPE_BMPSTRING = (short)0x1E00;
    public static final short TYPE_LONG = (short)0x1F00;

    public static final short CLASS_MASK        = (short)0xC000;
    public static final short CLASS_UNIVERSAL   = (short)0x0000;
    public static final short CLASS_APPLICATION = (short)0x4000;
    public static final short CLASS_CONTEXT     = (short)0x8000;
    public static final short CLASS_PRIVATE     = (short)0xC000;

    public static final short CONSTRUCTED_FLAG = (short)0x2000;

    private static final byte TAGBYTE_FIRST_TYPE_MASK = (byte)0x1F;
    private static final byte TAGBYTE_FIRST_TYPE_LONG = (byte)0x1F;
    private static final byte TAGBYTE_FLAG_CONTINUES = (byte)0x80;

    public static final boolean byteIsLongForm(byte firstByte) {
        return (firstByte & TAGBYTE_FIRST_TYPE_MASK)
                == TAGBYTE_FIRST_TYPE_LONG;
    }

    public static final boolean byteIsLast(byte tagByte) {
        return (tagByte & TAGBYTE_FLAG_CONTINUES) == 0;
    }

    public static final boolean isUniversal(short tag) {
        return tagClass(tag) == CLASS_UNIVERSAL;
    }

    public static final boolean isApplication(short tag) {
        return tagClass(tag) == CLASS_APPLICATION;
    }

    public static final boolean isContext(short tag) {
        return tagClass(tag) == CLASS_CONTEXT;
    }

    public static final boolean isPrivate(short tag) {
        return tagClass(tag) == CLASS_PRIVATE;
    }

    public static final boolean isPrimitive(short tag) {
        return (tag & CONSTRUCTED_FLAG) == 0;
    }

    public static final boolean isConstructed(short tag) {
        return (tag & CONSTRUCTED_FLAG) != 0;
    }

    public static final short tagType(short tag) {
        return (short)(tag & TYPE_MASK);
    }

    public static final short tagClass(short tag) {
        return (short)(tag & CLASS_MASK);
    }

    public static final short tagAsConstructed(short tag) {
        return (short)(tag & CONSTRUCTED_FLAG);
    }

    public static final short tagAsClass(short tag, short cls) {
        return (short)((tag & ~CLASS_MASK) | (cls & CLASS_MASK));
    }

    public static final short tagSize(short tag) {
        return 1;
    }

    public static final short putTag(byte[] buf, short bufOff, short tag) {
        buf[bufOff++] = (byte)(tag & 0xFF);
        return bufOff;
    }

}
