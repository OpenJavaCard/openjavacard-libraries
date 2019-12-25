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

import javacard.framework.ISOException;

public class DF extends ISOFile {

    public static final byte FIND_TYPE_ANY = 0;
    public static final byte FIND_TYPE_DF = 1;
    public static final byte FIND_TYPE_EF = 2;

    private final ISOFile[] mChildren;

    DF(DF parent, byte fdb, short fid) {
        super(parent, fdb, fid, (byte)0);
        mChildren = new ISOFile[DF_SIZE];
    }

    short getChildCount() {
        short result = 0;
        for(short i = 0; i < mChildren.length; i++) {
            if(mChildren[i] != null) {
                result++;
            }
        }
        return result;
    }

    void addChild(ISOFile file) {
        // callers must hold this rule
        if(file.getParent() != this) {
            ISOException.throwIt(SW_UNKNOWN);
        }
        // check all child slots
        short slot = -1;
        boolean conflict = false;
        for(short i = 0; i < mChildren.length; i++) {
            ISOFile child = mChildren[i];
            if(child == null) {
                if(slot < 0) {
                    // found an empty slot
                    slot = i;
                }
            } else {
                if (child.mFID == file.mFID) {
                    // found a name conflict
                    conflict = true;
                }
            }
        }
        // throw due exceptions
        if(conflict) {
            ISOException.throwIt(SW_FILE_INVALID);
        }
        if(slot < 0) {
            ISOException.throwIt(SW_FILE_FULL);
        }
        // actually add the file
        mChildren[slot] = file;
    }

    void removeChild(ISOFile file) {
        for(short i = 0; i < mChildren.length; i++) {
            ISOFile child = mChildren[i];
            if(child == file) {
                mChildren[i] = null;
            }
        }
    }

    public ISOFile findChildByFID(short fid, byte type) {
        ISOFile res = null;
        for(short i = 0; i < mChildren.length; i++) {
            ISOFile child = mChildren[i];
            if(child.mFID == fid) {
                res = child;
            }
        }
        return res;
    }

    public ISOFile findChildBySFI(byte sfi, byte type) {
        ISOFile res = null;
        for(short i = 0; i < mChildren.length; i++) {
            ISOFile child = mChildren[i];
            if(child.mSFI == sfi) {
                res = child;
            }
        }
        return res;
    }

    public ISOFile findChildByPath(byte[] pathBuf, short pathOff, short pathLen) {
        return null;
    }

}
