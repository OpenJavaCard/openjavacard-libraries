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
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;
import org.openjavacard.lib.ber.BERReader;
import org.openjavacard.lib.ber.BERWriter;

/**
 * ISO7816 file system implementation
 */
public class ISOFileSystem implements ISOConfig, ISOExtensions {

    /** The root directory */
    private final MF mMF;

    /** BER reader for parsing */
    private final BERReader mReader;

    /** BER writer for generating */
    private final BERWriter mWriter;

    /** File creator */
    private final ISOFileCreator mFileCreator;

    /**
     * Main constructor
     */
    public ISOFileSystem() {
        byte clearOn = JCSystem.CLEAR_ON_RESET;
        mMF = new MF();
        mReader = new BERReader(BER_MAX_DEPTH, clearOn);
        mWriter = new BERWriter(BER_MAX_TAGS, BER_MAX_DEPTH, BER_MAX_TEMP, clearOn);
        mFileCreator = new ISOFileCreator();
    }

    /** @return the MF */
    public MF getMF() {
        return mMF;
    }

    public DF findByDFName(byte[] pathBuf, short pathOff, short pathLen) {
        return null;
    }



    BERReader getBERReader() {
        return mReader;
    }

    BERWriter getBERWriter() {
        return mWriter;
    }

    ISOFileCreator getISOFileCreator() {
        return mFileCreator;
    }



    private void accessFile(ISOFile file, byte access) {
    }

    public void accessDirectory(DF directory, byte access) {
        accessFile(directory, access);
    }

    public EFTransparent accessFileBinary(EF file, byte access) {
        EFTransparent eft = (EFTransparent)file;
        accessFile(file, access);
        return eft;
    }

    public EFRecords accessFileRecord(EF file, byte access) {
        EFRecords efr = (EFRecords)file;
        accessFile(file, access);
        return efr;
    }


    private short checkLength(APDU apdu) {
        short lc = apdu.getIncomingLength();
        short len = apdu.setIncomingAndReceive();
        if(len != lc) {
            ISOException.throwIt(SW_WRONG_LENGTH);
        }
        return len;
    }



}
