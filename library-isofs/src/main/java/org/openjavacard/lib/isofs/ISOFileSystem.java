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
 * ISO7816-4 file system implementation
 */
public class ISOFileSystem implements ISOExtensions {

    /** The root directory */
    private final MF mMF;

    /** Transient object references */
    private final Object[] mRefs;
    private static final byte REF_SELECTED_DF = 0;
    private static final byte REF_SELECTED_EF = 1;
    private static final byte NUM_REFS = 2;

    /** Temporary storage */
    private final byte[] mTmp;
    private static final short TMP_SIZE = (short)128;

    /** BER reader for parsing */
    private final BERReader mReader;

    /** BER writer for generating */
    private final BERWriter mWriter;
    private static final short BER_MAX_LENGTH = (short)128;
    private static final byte BER_MAX_TAGS = (byte)16;
    private static final byte BER_MAX_DEPTH = (byte)4;

    /** File creator */
    private final ISOFileCreator mFileCreator;

    public ISOFileSystem() {
        mMF = new MF(FID_MF, (byte)16);
        mRefs = JCSystem.makeTransientObjectArray(NUM_REFS, JCSystem.CLEAR_ON_RESET);
        mTmp = JCSystem.makeTransientByteArray(TMP_SIZE, JCSystem.CLEAR_ON_RESET);
        mReader = new BERReader(BER_MAX_DEPTH, JCSystem.CLEAR_ON_RESET);
        mWriter = new BERWriter(BER_MAX_TAGS, BER_MAX_DEPTH, JCSystem.CLEAR_ON_RESET);
        mFileCreator = new ISOFileCreator();
    }

    public MF getMF() {
        return mMF;
    }

    public DF getSelectedDF() {
        return (DF) mRefs[REF_SELECTED_DF];
    }

    public EF getSelectedEF() {
        return (EF) mRefs[REF_SELECTED_EF];
    }

    private void accessFile(ISOFile file, byte access) {
    }

    private byte[] accessFileBinary(ISOFile file, byte access) {
        accessFile(file, access);
        return null;
    }

    private void accessFileRecord(ISOFile file, byte access) {
        accessFile(file, access);
    }

    byte TYPE_ANY = 0;
    byte TYPE_DF = 1;
    byte TYPE_EF = 2;

    private ISOFile findByFID(short fid, byte ftype) {
        return null;
    }

    private ISOFile findBySFI(short fid, byte ftype) {
        return null;
    }

    private ISOFile findByDFName(byte[] pathBuf, short pathOff, short pathLen) {
        return null;
    }

    private void selectFile(ISOFile file) {
    }

    private short checkLength(APDU apdu) {
        short lc = apdu.getIncomingLength();
        short len = apdu.setIncomingAndReceive();
        if(len != lc) {
            ISOException.throwIt(SW_WRONG_LENGTH);
        }
        return len;
    }

    /**
     * Main processing entrypoint
     *
     * @param apdu to process
     * @return true if command was handled
     */
    public boolean process(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        byte ins = buf[OFFSET_INS];

        // we only support interindustry commands
        if(!apdu.isISOInterindustryCLA()) {
            return false;
        }

        // handle supported commands
        boolean handled = true;
        switch (ins) {
            case INS_SELECT:
                processSelect(apdu);
                break;
            case INS_CREATE_FILE:
                processCreateFile(apdu);
                break;
            case INS_DELETE_FILE:
                processDeleteFile(apdu);
                break;
            case INS_READ_BINARY1:
            case INS_READ_BINARY2:
            case INS_SEARCH_BINARY1:
            case INS_SEARCH_BINARY2:
            case INS_WRITE_BINARY1:
            case INS_WRITE_BINARY2:
            case INS_UPDATE_BINARY1:
            case INS_UPDATE_BINARY2:
            case INS_ERASE_BINARY1:
            case INS_ERASE_BINARY2:
                handled = processBinary(apdu, ins);
                break;
            case INS_READ_RECORD1:
            case INS_READ_RECORD2:
            case INS_SEARCH_RECORD:
            case INS_UPDATE_RECORD1:
            case INS_UPDATE_RECORD2:
            case INS_WRITE_RECORD:
            case INS_APPEND_RECORD:
            case INS_ERASE_RECORD:
                handled = processRecord(apdu, ins);
                break;
            case INS_GET_DATA:
                processGetData(apdu);
                break;
            case INS_PUT_DATA:
                processPutData(apdu);
                break;
            default:
                handled = false;
                break;
        }

        // return true if we handled the command
        return handled;
    }

    /**
     * Process SELECT
     *
     * @param apdu to process
     */
    private void processSelect(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short off = OFFSET_CDATA;
        byte p1 = buf[OFFSET_P1];
        byte p2 = buf[OFFSET_P2];
        short lc = buf[OFFSET_LC];
        short fid;

        // get parameter fields
        byte p2Return = (byte)(p2 & SELECT_P2_RETURN_MASK);
        byte p2Iterate = (byte)(p2 & SELECT_P2_ITERATE_MASK);

        // check reserved part of P2
        if((p2 & SELECT_P2_RESERVED_MASK) != SELECT_P2_RESERVED_OKAY) {
            ISOException.throwIt(SW_INCORRECT_P1P2);
        }

        // proprietary response not supported
        if(p2Return == SELECT_P2_RETURN_PROPRIETARY) {
            ISOException.throwIt(SW_INCORRECT_P1P2);
        }

        // determine selected DF
        DF currentDF = getSelectedDF();

        // determine file to be selected
        ISOFile selected = null;
        switch (p1) {
            case SELECT_P1_SELECT_MF_DF_EF:
                // select MF/DF/EF by FID
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
            case SELECT_P1_SELECT_CHILD_DF:
                // select child DF by FID
                if (lc == 2) {
                    fid = Util.getShort(buf, off);
                    selected = currentDF.findChildByFID(fid, TYPE_DF);
                } else {
                    ISOException.throwIt(SW_WRONG_LENGTH);
                }
                break;
            case SELECT_P1_SELECT_CHILD_EF:
                // select child EF by FID
                if (lc == 2) {
                    fid = Util.getShort(buf, off);
                    selected = currentDF.findChildByFID(fid, TYPE_EF);
                } else {
                    ISOException.throwIt(SW_WRONG_LENGTH);
                }
                break;
            case SELECT_P1_SELECT_PARENT:
                // select parent DF
                selected = currentDF.getParent();
                break;
            case SELECT_P1_SELECT_DFNAME:
                // select DF by name
                selected = findByDFName(buf, off, lc);
                break;
            case SELECT_P1_SELECT_PATH_MF:
                // select by path from MF
                selected = mMF.findChildByPath(buf, off, lc);
                break;
            case SELECT_P1_SELECT_PATH_DF:
                // select by path from selected DF
                selected = currentDF.findChildByPath(buf, off, lc);
                break;
            default:
                ISOException.throwIt(SW_INCORRECT_P1P2);
                break;
        }

        // fail if nothing was selected
        if(selected == null) {
            ISOException.throwIt(SW_FILE_NOT_FOUND);
        }

        // select the file
        selectFile(selected);

        // prepare BER writer
        mWriter.begin(BER_MAX_LENGTH);

        // produce requested TLV
        switch(p2Return) {
            case SELECT_P2_RETURN_FCI:
                // return FCI
                selected.writeFCI(mWriter, mTmp);
                break;
            case SELECT_P2_RETURN_FCP:
                // return FCP
                selected.writeFCP(mWriter, mTmp);
                break;
            case SELECT_P2_RETURN_FMD:
                // return FMD
                selected.writeFMD(mWriter, mTmp);
                break;
            default:
                ISOException.throwIt(SW_INCORRECT_P1P2);
                break;
        }

        // generate TLV and send response
        mWriter.finishAndSend(apdu);
    }

    /**
     * Process CREATE FILE
     *
     * @param apdu to process
     */
    private void processCreateFile(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        byte p1 = buf[OFFSET_P1];
        byte p2 = buf[OFFSET_P2];
        byte lc = buf[OFFSET_LC];

        if(p1 != (byte)0 || p2 != (byte)0) {
            ISOException.throwIt(SW_INCORRECT_P1P2);
        }

        DF currentDF = getSelectedDF();

        mFileCreator.prepare(currentDF);

        mReader.parse(buf, OFFSET_CDATA, lc, mFileCreator);
    }

    /**
     * Process DELETE FILE
     *
     * @param apdu to process
     */
    private void processDeleteFile(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        byte p1 = buf[OFFSET_P1];
        byte p2 = buf[OFFSET_P2];

        if(p1 != (byte)0 || p2 != (byte)0) {
            ISOException.throwIt(SW_INCORRECT_P1P2);
        }
    }

    /**
     * Process a BINARY command
     *
     * All BINARY commands have a common coding for their
     * parameters in P12, so they share implementation.
     *
     * @param apdu to process
     */
    private boolean processBinary(APDU apdu, byte ins) {
        byte[] buf = apdu.getBuffer();
        short p12 = Util.getShort(buf, OFFSET_P1);

        // process parameters
        ISOFile file = processBinaryFindFile(ins, p12);
        short offset = processBinaryFindOffset(ins, p12);

        // perform the operation
        boolean handled = true;
        switch(ins & 0x7F) {
            case INS_READ_BINARY1:
                processReadBinary(apdu, file, offset);
                break;
            case INS_SEARCH_BINARY1:
                processSearchBinary(apdu, file, offset);
                break;
            case INS_WRITE_BINARY1:
                processWriteBinary(apdu, file, offset);
                break;
            case INS_UPDATE_BINARY1:
                processUpdateBinary(apdu, file, offset);
                break;
            case INS_ERASE_BINARY1:
                processEraseBinary(apdu, file, offset);
                break;
            default:
                handled = false;
                break;
        }

        // return true if handled
        return handled;
    }

    /**
     * Determine file for a BINARY command
     *
     * @param ins
     * @param p12
     * @return
     */
    private ISOFile processBinaryFindFile(byte ins, short p12) {
        ISOFile file = getSelectedEF();
        if((ins & 0x01) == 0) {
            if((p12 & 0x8000) != 0) {
                file = findBySFI((short)(p12 & 0x1F), TYPE_EF);
            }
        } else {
            file = findByFID((short)(p12 & 0x7FFF), TYPE_EF);
        }
        return file;
    }

    /**
     * Determine offset for a BINARY command
     *
     * @param ins
     * @param p12
     * @return
     */
    private short processBinaryFindOffset(byte ins, short p12) {
        short off = -1;
        if((ins & 0x01) == 0) {
            if ((p12 & 0x8000) == 0) {
                off = (short) (p12 & 0x7FFF);
            }
        }
        return off;
    }

    /**
     * Process READ BINARY
     *
     * @param apdu to process
     */
    private void processReadBinary(APDU apdu, ISOFile file, short offset) {
        byte[] buf = apdu.getBuffer();
        byte[] data = accessFileBinary(file, ACCESS_EF_READ);
    }

    /**
     * Process SEARCH BINARY
     *
     * @param apdu to process
     */
    private void processSearchBinary(APDU apdu, ISOFile file, short offset) {
        byte[] buf = apdu.getBuffer();
        byte[] data = accessFileBinary(file, ACCESS_EF_READ);
    }

    /**
     * Process WRITE BINARY
     *
     * @param apdu to process
     */
    private void processWriteBinary(APDU apdu, ISOFile file, short offset) {
        byte[] buf = apdu.getBuffer();
        byte[] data = accessFileBinary(file, ACCESS_EF_WRITE);
    }

    /**
     * Process UPDATE BINARY
     *
     * @param apdu to process
     */
    private void processUpdateBinary(APDU apdu, ISOFile file, short offset) {
        byte[] buf = apdu.getBuffer();
        byte[] data = accessFileBinary(file, ACCESS_EF_UPDATE);
    }

    /**
     * Process ERASE BINARY
     *
     * @param apdu to process
     */
    private void processEraseBinary(APDU apdu, ISOFile file, short offset) {
        byte[] buf = apdu.getBuffer();
        byte[] data = accessFileBinary(file, ACCESS_EF_UPDATE);
    }

    private boolean processRecord(APDU apdu, short ins) {
        byte[] buf = apdu.getBuffer();
        byte p1 = buf[OFFSET_P1];
        byte p2 = buf[OFFSET_P2];

        boolean handled = true;
        switch (ins & 0x7f) {
            case INS_READ_RECORD1:
                processReadRecord(apdu);
                break;
            case INS_SEARCH_RECORD:
                processSearchRecord(apdu);
                break;
            case INS_UPDATE_RECORD1:
                processUpdateRecord(apdu);
                break;
            case INS_WRITE_RECORD:
                processWriteRecord(apdu);
                break;
            case INS_APPEND_RECORD:
                processAppendRecord(apdu);
                break;
            case INS_ERASE_RECORD:
                processEraseRecord(apdu);
                break;
            default:
                handled = false;
                break;
        }

        return handled;
    }

    private void processReadRecord(APDU apdu) {
        byte[] buf = apdu.getBuffer();
    }

    private void processSearchRecord(APDU apdu) {
        byte[] buf = apdu.getBuffer();
    }

    private void processWriteRecord(APDU apdu) {
        byte[] buf = apdu.getBuffer();
    }

    private void processUpdateRecord(APDU apdu) {
        byte[] buf = apdu.getBuffer();
    }

    private void processAppendRecord(APDU apdu) {
        byte[] buf = apdu.getBuffer();
    }

    private void processEraseRecord(APDU apdu) {
        byte[] buf = apdu.getBuffer();
    }

    private void processGetData(APDU apdu) {
        byte[] buf = apdu.getBuffer();
    }

    private void processPutData(APDU apdu) {
        byte[] buf = apdu.getBuffer();
    }

}
