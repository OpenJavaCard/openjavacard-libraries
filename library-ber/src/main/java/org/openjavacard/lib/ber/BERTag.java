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

package org.openjavacard.lib.ber;

import javacard.framework.Util;

/**
 * Helper functions related to BER tag values
 */
public final class BERTag {

    public static final short TYPE_MASK       = (short)0x1F7F;
    public static final short TYPE_MASK_FIRST = (short)0x1F00;

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

    /** Internal: return true of the given first byte of a tag indicates long form */
    static boolean byteIsLongForm(byte firstByte) {
        return (firstByte & TAGBYTE_FIRST_TYPE_MASK)
                == TAGBYTE_FIRST_TYPE_LONG;
    }

    /** Internal: return true of the given tag byte is the last byte of the tag */
    static boolean byteIsLast(byte tagByte) {
        return (tagByte & TAGBYTE_FLAG_CONTINUES) == 0;
    }

    /** True of the tag is of the universal class */
    public static boolean isUniversal(short tag) {
        return tagClass(tag) == CLASS_UNIVERSAL;
    }

    /** True of the tag is of the application-specific class */
    public static boolean isApplication(short tag) {
        return tagClass(tag) == CLASS_APPLICATION;
    }

    /** True of the tag is of the context-specific class */
    public static boolean isContext(short tag) {
        return tagClass(tag) == CLASS_CONTEXT;
    }

    /** True of the tag is of the private class */
    public static boolean isPrivate(short tag) {
        return tagClass(tag) == CLASS_PRIVATE;
    }

    /** True of the tag is primitive */
    public static boolean isPrimitive(short tag) {
        return (tag & CONSTRUCTED_FLAG) == 0;
    }

    /** True of the tag is constructed */
    public static boolean isConstructed(short tag) {
        return (tag & CONSTRUCTED_FLAG) != 0;
    }

    /** Returns the class of the tag */
    public static short tagClass(short tag) {
        return (short)(tag & CLASS_MASK);
    }

    /** Returns the type of the tag */
    public static short tagType(short tag) {
        return (short)(tag & TYPE_MASK);
    }

    /** Returns true if the tag requires two bytes */
    public static boolean tagIsLong(short tag) {
        return (tag & TYPE_MASK_FIRST) == TYPE_LONG;
    }

    /** Coerce a tag to be constructed */
    public static short tagAsConstructed(short tag) {
        return (short)(tag & CONSTRUCTED_FLAG);
    }

    /** Coerce a tag to be of the given class */
    public static short tagAsClass(short tag, short cls) {
        return (short)((tag & ~CLASS_MASK) | (cls & CLASS_MASK));
    }

    /** Get the encoded size of a tag */
    public static short tagSize(short tag) {
        if(tagIsLong(tag)) {
            return 2;
        } else {
            return 1;
        }
    }

    /** Put a tag into the given buffer */
    public static short putTag(byte[] buf, short off, short tag) {
        if(tagIsLong(tag)) {
            off = Util.setShort(buf, off, tag);
        } else {
            buf[off++] = (byte) ((tag >> 8) & 0xFF);
        }
        return off;
    }

}
