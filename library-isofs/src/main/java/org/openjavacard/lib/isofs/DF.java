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

public class DF extends ISOFile {

    private static final byte FDB = FDB_CATEGORY_SPECIAL|FDB_SPECIAL_DF;

    private final DF mParent;
    private final ISOFile[] mChildren;

    DF(short fid, DF parent, byte maxChildren) {
        super(fid, FDB);
        mParent = parent;
        mChildren = new ISOFile[maxChildren];
    }

    DF getParent() {
        return mParent;
    }

    void addChild(ISOFile child) {
    }

    void removeChild(ISOFile child) {
    }

    ISOFile findChildByFID(short fid, byte type) {
        ISOFile res = null;
        for(short i = 0; i < mChildren.length; i++) {
            ISOFile child = mChildren[i];
            if(child.mFID == fid) {
                res = child;
            }
        }
        return res;
    }

    ISOFile findChildBySFI(short fid) {
        ISOFile res = null;
        for(short i = 0; i < mChildren.length; i++) {
            ISOFile child = mChildren[i];
            if(child.mFID == fid) {
                res = child;
            }
        }
        return res;
    }

    ISOFile findChildByPath(byte[] pathBuf, short pathOff, short pathLen) {
        return null;
    }

}
