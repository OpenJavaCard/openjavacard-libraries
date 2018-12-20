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

package org.openjavacard.lib.string;

public class ASCII {

    public static boolean isnull(byte c) {
        return c == 0;
    }

    public static boolean isascii(byte c) {
        return (c >= 0);
    }

    public static boolean isalnum(byte c) {
        return ((c >= 0x30) && (c <= 0x39))
                || ((c >= 0x41) && (c <= 0x5A))
                || ((c >= 0x61) && (c <= 0x7A));
    }

    public static boolean isalpha(byte c) {
        return ((c >= 0x41) && (c <= 0x5A))
                || ((c >= 0x61) && (c <= 0x7A));
    }

    public static boolean isblank(byte c) {
        return (c == 0x09) || (c == 0x20);
    }

    public static boolean iscntrl(byte c) {
        return (c < 0x20) || (c == 0x7F);
    }

    public static boolean isdigit(byte c) {
        return (c >= 0x30) && (c <= 0x39);
    }

    public static boolean isgraph(byte c) {
        return (c >= 0x21) && (c <= 0x7E);
    }

    public static boolean islower(byte c) {
        return (c >= 0x61) && (c <= 0x7A);
    }

    public static boolean isprint(byte c) {
        return (c >= 0x20) && (c <= 0x7F);
    }

    public static boolean ispunct(byte c) {
        return isgraph(c) && !isalnum(c);
    }

    public static boolean isspace(byte c) {
        return (c == 0x20) || ((c >= 0x09) && (c <= 0x0D));
    }

    public static boolean isupper(byte c) {
        return (c >= 0x41) && (c <= 0x5A);
    }

    public static boolean isxdigit(byte c) {
        return ((c >= 0x30) && (c <= 0x39))
                || ((c >= 0x41) && (c <= 0x46))
                || ((c >= 0x61) && (c <= 0x66));
    }

    public static byte tolower(byte c) {
        if(isupper(c)) {
            return (byte)(c | 32);
        }
        return c;
    }

    public static byte toupper(byte c) {
        if(islower(c)) {
            return (byte)(c & 0x5F);
        }
        return c;
    }

}
