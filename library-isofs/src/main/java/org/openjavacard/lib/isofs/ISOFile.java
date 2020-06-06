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

import org.openjavacard.lib.ber.BERWriter;

public abstract class ISOFile implements ISOConfig, ISOExtensions {

    final DF mParent;
    final short mFID;
    final byte  mSFI;
    final byte  mFDB;
    private byte mLCS;
    private byte mCSA;

    /**
     * Base constructor
     * @param fid for the file
     * @param fdb for the file
     */
    ISOFile(DF parent, byte fdb, short fid, byte sfi) {
        mParent = parent;
        mFID = fid;
        mSFI = sfi;
        mFDB = fdb;
        mLCS = LCS_CREATION;
    }

    /** @return parent DF of this file */
    public DF getParent() {
        return mParent;
    }

    /** @return file ID of this file */
    public short getFID() {
        return mFID;
    }

    /** @return short EF identifier */
    public byte getSFI() {
        return mSFI;
    }

    /** @return channel security attribute */
    public byte getCSA() {
        return mCSA;
    }

    /** @return assigned life cycle state */
    public byte getAssignedLCS() {
        return mLCS;
    }

    /** @return effective life cycle state */
    public byte getEffectiveLCS() {
        // start with the assigned LCS
        byte effective = mLCS;
        // termination is always effective
        if(effective != LCS_TERMINATED) {
            // skip processing if no parent (MF)
            if(mParent != null) {
                // get parent LCS
                byte parent = mParent.getEffectiveLCS();
                // if parent is deactivated then so are we
                if (parent == LCS_OPERATIONAL_DEACTIVATED) {
                    effective = LCS_OPERATIONAL_DEACTIVATED;
                }
                // if parent is terminated then so are we
                if (parent == LCS_TERMINATED) {
                    effective = LCS_TERMINATED;
                }
            }
        }
        // return result
        return effective;
    }

    /**
     * Activate the file
     */
    public void activate() {
        byte effectiveLCS = getEffectiveLCS();
        if(effectiveLCS != LCS_TERMINATED) {
            mLCS = LCS_OPERATIONAL_ACTIVATED;
        }
    }

    /**
     * Deactivate the file
     */
    public void deactivate() {
        byte effectiveLCS = getEffectiveLCS();
        if(effectiveLCS != LCS_TERMINATED) {
            mLCS = LCS_OPERATIONAL_DEACTIVATED;
        }
    }

    /**
     * Terminate the file
     */
    public void terminate() {
        byte effectiveLCS = getEffectiveLCS();
        if(effectiveLCS != LCS_TERMINATED) {
            mLCS = LCS_TERMINATED;
        }
    }

    /**
     * Write FCI into the given writer
     * @param ber
     */
    public void writeFCI(BERWriter ber) {
        ber.beginConstructed(TAG_FCI);
        tagsFCP(ber);
        tagsFMD(ber);
        ber.endConstructed();
    }

    /**
     * Write FCP into the given writer
     * @param ber
     */
    public void writeFCP(BERWriter ber) {
        ber.beginConstructed(TAG_FCP);
        tagsFCP(ber);
        ber.endConstructed();
    }

    /**
     * Write FMD into the given writer
     * @param ber
     */
    public void writeFMD(BERWriter ber) {
        ber.beginConstructed(TAG_FMD);
        tagsFMD(ber);
        ber.endConstructed();
    }

    /**
     * Produce FCP tags
     * @param ber
     */
    protected void tagsFCP(BERWriter ber) {
        // 82 - File descriptor
        ber.primitiveByte(TAG_FCI_FDB, mFDB);
        // 83 - File identifier
        ber.primitiveShort(TAG_FCI_FILEID, mFID);
        // 8A - Life cycle status
        ber.primitiveByte(TAG_FCI_LCS, mLCS);
        // 8E - Channel security attribute
        if(mCSA != 0) {
            ber.primitiveByte(TAG_FCI_CSA, mCSA);
        }
    }

    /**
     * Produce FMD tags
     * @param ber
     */
    protected void tagsFMD(BERWriter ber) {
    }

}
