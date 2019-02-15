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

public class StringWriter {

    private final short mMaxChunks;

    private final short[] mVars;
    /** Number of transient variables */
    private static final byte NUM_VAR = 3;
    /** Variable: maximum allowed length */
    private static final byte VAR_MAX_LENGTH = 0;
    /** Variable: current running length */
    private static final byte VAR_LENGTH     = 1;
    /** Variable: current index */
    private static final byte VAR_INDEX      = 2;

    private final Object[] mBufStk;
    private final short[]  mOffStk;
    private final short[]  mLenStk;

    public StringWriter(short maxChunks) {
        mMaxChunks = maxChunks;
        mVars = new short[NUM_VAR];
        mBufStk = new Object[maxChunks];
        mOffStk = new short[maxChunks];
        mLenStk = new short[maxChunks];
    }

    void begin(short maxLength) {
        mVars[VAR_MAX_LENGTH] = maxLength;
        mVars[VAR_LENGTH] = 0;
        mVars[VAR_INDEX] = 0;
    }

    short finish(byte[] buf, short off, short len) {
        return 0;
    }

}
