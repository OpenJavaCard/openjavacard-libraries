/*
 * openjavacard-libraries: Class libraries for JavaCard
 * Copyright (C) 2019 Ingo Albrecht <copyright@promovicz.org>
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

package org.openjavacard.lib.isofs;

import javacard.framework.Util;
import org.openjavacard.lib.ber.BERWriter;

public abstract class ISOFile implements ISOExtensions {

    /** File ID */
    public final short mFID;
    /** FDB byte - file descriptor byte */
    private final byte mFDB;
    /** LCS byte - life cycle status */
    private byte mLCS;
    /** CSA byte - channel security attribute */
    private byte mCSA;

    /**
     * Base constructor
     * @param fid for the file
     * @param fdb for the file
     */
    ISOFile(short fid, byte fdb) {
        mFID = fid;
        mFDB = fdb;
        mLCS = LCS_CREATION;
    }

    /**
     * Write FCI into the given writer
     * @param ber
     * @param tmp
     */
    public void writeFCI(BERWriter ber, byte[] tmp) {
        short off = (short)0;
        ber.beginConstructed(TAG_FCI);
        off = tagsFCP(ber, tmp, off);
        off = tagsFMD(ber, tmp, off);
        ber.endConstructed();
    }

    /**
     * Write FCP into the given writer
     * @param ber
     * @param tmp
     */
    public void writeFCP(BERWriter ber, byte[] tmp) {
        ber.beginConstructed(TAG_FCP);
        tagsFCP(ber, tmp, (short)0);
        ber.endConstructed();
    }

    /**
     * Write FMD into the given writer
     * @param ber
     * @param tmp
     */
    public void writeFMD(BERWriter ber, byte[] tmp) {
        ber.beginConstructed(TAG_FMD);
        tagsFMD(ber, tmp, (short)0);
        ber.endConstructed();
    }

    /**
     * Produce FCP tags
     * @param ber
     * @param tmp
     */
    private short tagsFCP(BERWriter ber, byte[] tmp, short off) {
        // 82 - File descriptor
        tmp[off] = mFDB;
        ber.buildPrimitive(TAG_FCI_FDB, tmp, off, (short)1);
        off++;
        // 83 - File identifier
        Util.setShort(tmp, off, mFID);
        ber.buildPrimitive(TAG_FCI_FILEID, tmp, off, (short)2);
        off += 2;
        // 8A - Life cycle status
        tmp[off] = mLCS;
        ber.buildPrimitive(TAG_FCI_LCS, tmp, off, (short)1);
        off++;
        // 8E - Channel security attribute
        if(mCSA != 0) {
            tmp[off] = mCSA;
            ber.buildPrimitive(TAG_FCI_CSA, tmp, off, (short)1);
            off++;
        }
        // return new offset
        return off;
    }

    /**
     * Produce FMD tags
     * @param ber
     * @param tmp
     */
    private short tagsFMD(BERWriter ber, byte[] tmp, short off) {
        return off;
    }

}
