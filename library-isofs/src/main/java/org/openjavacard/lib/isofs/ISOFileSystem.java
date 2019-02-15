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

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;
import org.openjavacard.lib.ber.BERWriter;

public class ISOFileSystem implements ISO7816 {

    public static final short FID_MF = (short)0x3F00;

    private final BERWriter mBER;
    
    private final MF mMF;

    private DF mSelectedDF;
    private EF mSelectedEF;

    public ISOFileSystem() {
        mBER = new BERWriter((byte)16, (byte)4, JCSystem.CLEAR_ON_RESET);
        mMF = new MF(FID_MF, (byte)16);
    }

    public MF getMF() {
        return mMF;
    }

    public DF getSelectedDF() {
        return mSelectedDF;
    }

    public EF getSelectedEF() {
        return mSelectedEF;
    }

    private short checkLength(APDU apdu) {
        short lc = apdu.getIncomingLength();
        short len = apdu.setIncomingAndReceive();
        if(len != lc) {
            ISOException.throwIt(SW_WRONG_LENGTH);
        }
        return len;
    }

    private void selectFile(ISOFile file) {

    }

    short TYPE_ANY = 0;
    short TYPE_DF = 1;
    short TYPE_EF = 2;

    private ISOFile findByFID(short fid, short ftype) {
        return null;
    }

    public void processSelectFile(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short off = OFFSET_CDATA;
        byte p1 = buf[OFFSET_P1];
        byte p2 = buf[OFFSET_P2];
        short fid;
        short lc = (short)0; // XXX

        DF currentDF = getSelectedDF();

        ISOFile selected = null;
        switch (p1 % 0xFF) {
            case 0x00:
                if (lc == 0) {
                    // select MF
                    selected = mMF;
                } else if (lc == 2) {
                    // select by FID
                    fid = Util.getShort(buf, off);
                    selected = findByFID(fid, TYPE_ANY);
                } else {
                    ISOException.throwIt(SW_WRONG_LENGTH);
                }
                break;
            case 0x01:
                // select child DF by FID
                if (lc == 2) {
                    fid = Util.getShort(buf, off);
                } else {
                    ISOException.throwIt(SW_WRONG_LENGTH);
                }
                break;
            case 0x02:
                // select child EF by FID
                if (lc == 2) {
                    fid = Util.getShort(buf, off);
                } else {
                    ISOException.throwIt(SW_WRONG_LENGTH);
                }
                break;
            case 0x03:
                // select parent DF
                selected = currentDF.getParent();
                break;
            case 0x04:
                // select DF by name
                break;
            case 0x08:
                // select by path from MF
                selected = mMF.findChildByPath(buf, off, lc);
                break;
            case 0x09:
                // select by path from selected DF
                selected = currentDF.findChildByPath(buf, off, lc);
                break;
            default:
                ISOException.throwIt(SW_INCORRECT_P1P2);
                break;
        }

        selectFile(selected);

        switch(p2 & 0xFC) {
            case 0x00:
                // return FCI
                break;
            case 0x04:
                // return FCP
                break;
            case 0x08:
                // return FMD
                break;
            default:
                ISOException.throwIt(SW_INCORRECT_P1P2);
                break;
        }
    }

    public void processCreateFile(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        byte p1 = buf[OFFSET_P1];
        byte p2 = buf[OFFSET_P2];

        if(p1 != (byte)0 || p2 != (byte)0) {
            ISOException.throwIt(SW_INCORRECT_P1P2);
        }
    }

    public void processDeleteFile(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        byte p1 = buf[OFFSET_P1];
        byte p2 = buf[OFFSET_P2];

        if(p1 != (byte)0 || p2 != (byte)0) {
            ISOException.throwIt(SW_INCORRECT_P1P2);
        }
    }

    public void processReadBinary(APDU apdu) {
    }

    public void processUpdateBinary(APDU apdu) {
    }

}
