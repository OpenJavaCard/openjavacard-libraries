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

import javacard.framework.JCSystem;

public class StringStatistics {

    public static final byte NUM_STATS   = 14;

    public static final byte STAT_NULL   = 0;
    public static final byte STAT_ASCII  = 1;
    public static final byte STAT_ALNUM  = 2;
    public static final byte STAT_ALPHA  = 3;
    public static final byte STAT_BLANK  = 4;
    public static final byte STAT_CNTRL  = 5;
    public static final byte STAT_DIGIT  = 6;
    public static final byte STAT_GRAPH  = 7;
    public static final byte STAT_LOWER  = 8;
    public static final byte STAT_PRINT  = 9;
    public static final byte STAT_PUNCT  = 10;
    public static final byte STAT_SPACE  = 11;
    public static final byte STAT_UPPER  = 12;
    public static final byte STAT_XDIGIT = 13;

    private final short[] mStatistics;

    public StringStatistics() {
        mStatistics = new short[NUM_STATS];
    }

    public StringStatistics(byte clearOn) {
        mStatistics = JCSystem.makeTransientShortArray(NUM_STATS, clearOn);
    }

    public short get(byte statistic) {
        return mStatistics[statistic];
    }

    public void reset() {
        fillShortArray(mStatistics, (short)0, (short)mStatistics.length, (short)0);
    }

    public void update(byte[] buf, short off, short len) {
        reset();

        short lim = (short)(off + len);
        for(short cur = off; cur < lim; cur++) {
            byte chr = buf[cur];
            if(ASCII.isnull(chr)) {
                mStatistics[STAT_NULL]++;
            }
            if(ASCII.isascii(chr)) {
                mStatistics[STAT_ASCII]++;
            }
            if(ASCII.isalnum(chr)) {
                mStatistics[STAT_ALNUM]++;
            }
            if(ASCII.isalpha(chr)) {
                mStatistics[STAT_ALPHA]++;
            }
            if(ASCII.isblank(chr)) {
                mStatistics[STAT_BLANK]++;
            }
            if(ASCII.iscntrl(chr)) {
                mStatistics[STAT_CNTRL]++;
            }
            if(ASCII.isdigit(chr)) {
                mStatistics[STAT_DIGIT]++;
            }
            if(ASCII.isgraph(chr)) {
                mStatistics[STAT_GRAPH]++;
            }
            if(ASCII.islower(chr)) {
                mStatistics[STAT_LOWER]++;
            }
            if(ASCII.isprint(chr)) {
                mStatistics[STAT_PRINT]++;
            }
            if(ASCII.ispunct(chr)) {
                mStatistics[STAT_PUNCT]++;
            }
            if(ASCII.isspace(chr)) {
                mStatistics[STAT_SPACE]++;
            }
            if(ASCII.isupper(chr)) {
                mStatistics[STAT_UPPER]++;
            }
            if(ASCII.isxdigit(chr)) {
                mStatistics[STAT_XDIGIT]++;
            }
        }
    }

    private static void fillShortArray(short[] array, short off, short len, short value) {
        short lim = (short)(off + len);
        for(short cur = off; cur < lim; cur++) {
            array[cur] = value;
        }
    }

}
