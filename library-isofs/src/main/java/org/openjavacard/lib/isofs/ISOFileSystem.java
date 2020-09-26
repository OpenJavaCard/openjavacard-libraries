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

    /** Transient object references */
    private final Object[] mRefs;
    private static final byte REF_SELECTED_DF = 0;
    private static final byte REF_SELECTED_EF = 1;
    private static final byte NUM_REFS = 2;

    /** Transient variables */
    private final short[] mVars;
    private static final byte VAR_CURRENT_RECORD = 0;
    private static final byte VAR_CURRENT_OFFSET = 1;
    private static final byte NUM_VARS = 2;

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
        mRefs = JCSystem.makeTransientObjectArray(NUM_REFS, clearOn);
        mVars = JCSystem.makeTransientShortArray(NUM_VARS, clearOn);
        mReader = new BERReader(BER_MAX_DEPTH, clearOn);
        mWriter = new BERWriter(BER_MAX_TAGS, BER_MAX_DEPTH, BER_MAX_TEMP, clearOn);
        mFileCreator = new ISOFileCreator();
    }

    /** @return the MF */
    public MF getMF() {
        return mMF;
    }

    /** @return current selected DF */
    public DF getSelectedDF() {
        return (DF) mRefs[REF_SELECTED_DF];
    }

    /** @return current selected EF */
    public EF getSelectedEF() {
        return (EF) mRefs[REF_SELECTED_EF];
    }

    /** @return current selected file */
    public ISOFile getSelected() {
        if(mRefs[REF_SELECTED_EF] != null) {
            return (ISOFile)mRefs[REF_SELECTED_EF];
        }
        if(mRefs[REF_SELECTED_DF] != null) {
            return (ISOFile)mRefs[REF_SELECTED_DF];
        }
        return null;
    }

    /**
     * Select the given file
     * @param file to select
     */
    private void selectFile(ISOFile file) {
        mVars[VAR_CURRENT_OFFSET] = 0;
        mVars[VAR_CURRENT_RECORD] = 0;
        if(file instanceof DF) {
            mRefs[REF_SELECTED_EF] = null;
            mRefs[REF_SELECTED_DF] = file;
        } else {
            mRefs[REF_SELECTED_EF] = file;
            mRefs[REF_SELECTED_DF] = file.getParent();
        }
    }

    private void accessFile(ISOFile file, byte access) {
    }

    private void accessDirectory(DF directory, byte access) {
        accessFile(directory, access);
    }

    private EFTransparent accessFileBinary(EF file, byte access) {
        EFTransparent eft = (EFTransparent)file;
        accessFile(file, access);
        return eft;
    }

    private void accessFileRecord(EF file, byte access) {
        accessFile(file, access);
    }

    private DF findByDFName(byte[] pathBuf, short pathOff, short pathLen) {
        return null;
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

        // must be an unwrapped APDU
        if(apdu.isSecureMessagingCLA()) {
            return false;
        }

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
            case INS_DELETE:
                processDelete(apdu);
                break;
            case INS_ACTIVATE:
                processActivate(apdu);
                break;
            case INS_DEACTIVATE:
                processDeactivate(apdu);
                break;
            case INS_TERMINATE:
                processTerminate(apdu);
                break;
            case INS_TERMINATE_EF:
                processTerminateEF(apdu);
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
            case INS_ACTIVATE_RECORD:
            case INS_DEACTIVATE_RECORD:
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
            case INS_MANAGE_DATA:
                handled = false;
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

        // only "first or only" is supported
        if(p2Iterate != SELECT_P2_ITERATE_FIRST) {
            ISOException.throwIt(SW_INCORRECT_P1P2);
        }

        // proprietary response not supported
        if(p2Return == SELECT_P2_RETURN_PROPRIETARY) {
            ISOException.throwIt(SW_INCORRECT_P1P2);
        }

        // check reserved part of P2
        if((p2 & SELECT_P2_RESERVED_MASK) != SELECT_P2_RESERVED_OKAY) {
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
                    if(fid == FID_MF) {
                        selected = mMF;
                    } else {
                        selected = currentDF.findUpwardsByFID(fid, DF.FIND_TYPE_ANY);
                    }
                } else {
                    ISOException.throwIt(SW_WRONG_LENGTH);
                }
                break;
            case SELECT_P1_SELECT_CHILD_DF:
                // select child DF by FID
                if (lc == 2) {
                    fid = Util.getShort(buf, off);
                    selected = currentDF.findChildByFID(fid, DF.FIND_TYPE_DF);
                } else {
                    ISOException.throwIt(SW_WRONG_LENGTH);
                }
                break;
            case SELECT_P1_SELECT_CHILD_EF:
                // select child EF by FID
                if (lc == 2) {
                    fid = Util.getShort(buf, off);
                    selected = currentDF.findChildByFID(fid, DF.FIND_TYPE_EF);
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
                selected.writeFCI(mWriter);
                break;
            case SELECT_P2_RETURN_FCP:
                // return FCP
                selected.writeFCP(mWriter);
                break;
            case SELECT_P2_RETURN_FMD:
                // return FMD
                selected.writeFMD(mWriter);
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
        // prepare file creation logic
        mFileCreator.prepare(currentDF);
        // process provided FCP data
        mReader.parse(buf, OFFSET_CDATA, lc, mFileCreator);
        // actually create the file
        ISOFile newFile = mFileCreator.create();
        // select the file
        selectFile(newFile);
    }

    private ISOFile processLifecycleFindFile(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        byte p1 = buf[OFFSET_P1];
        byte p2 = buf[OFFSET_P2];
        short off = OFFSET_CDATA;
        short len = buf[OFFSET_LC];
        short fid;

        if(p2 != 0) {
            ISOException.throwIt(SW_WRONG_P1P2);
        }

        // determine selected DF
        DF currentDF = getSelectedDF();

        ISOFile selected = null;
        byte p1Select = (byte)(p1 & LIFECYCLE_P1_SELECT_MASK);
        switch (p1Select) {
            case LIFECYCLE_P1_SELECT_FILE_SELECTED:
                selected = getSelected();
                break;
            case LIFECYCLE_P1_SELECT_FILE_CHILD_DF:
                if (len == 2) {
                    fid = Util.getShort(buf, off);
                    selected = currentDF.findChildByFID(fid, DF.FIND_TYPE_DF);
                } else {
                    ISOException.throwIt(SW_WRONG_LENGTH);
                }
                break;
            case LIFECYCLE_P1_SELECT_FILE_CHILD_EF:
                if (len == 2) {
                    fid = Util.getShort(buf, off);
                    selected = currentDF.findChildByFID(fid, DF.FIND_TYPE_EF);
                } else {
                    ISOException.throwIt(SW_WRONG_LENGTH);
                }
                break;
            case LIFECYCLE_P1_SELECT_FILE_DFNAME:
                selected = findByDFName(buf, off, len);
                break;
            case LIFECYCLE_P1_SELECT_FILE_PATH_MF:
                selected = mMF.findChildByPath(buf, off, len);
                break;
            case LIFECYCLE_P1_SELECT_FILE_PATH_DF:
                selected = currentDF.findChildByPath(buf, off, len);
                break;
            default:
                ISOException.throwIt(SW_WRONG_P1P2);
                break;
        }

        return selected;
    }

    /**
     * Process DELETE
     *
     * @param apdu to process
     */
    private void processDelete(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        byte p1 = buf[OFFSET_P1];
        byte p1Type = (byte)(p1 & LIFECYCLE_P1_TYPE_MASK);
        if (p1Type == LIFECYCLE_P1_TYPE_FILE) {
            ISOFile file = processLifecycleFindFile(apdu);
            // XXX implement
        } else {
            ISOException.throwIt(SW_WRONG_P1P2);
        }
    }

    /**
     * Process ACTIVATE
     *
     * @param apdu to process
     */
    private void processActivate(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        byte p1 = buf[OFFSET_P1];
        byte p1Type = (byte)(p1 & LIFECYCLE_P1_TYPE_MASK);
        if (p1Type == LIFECYCLE_P1_TYPE_FILE) {
            ISOFile file = processLifecycleFindFile(apdu);
            file.activate();
        } else {
            ISOException.throwIt(SW_WRONG_P1P2);
        }
    }

    /**
     * Process DEACTIVATE
     *
     * @param apdu to process
     */
    private void processDeactivate(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        byte p1 = buf[OFFSET_P1];
        byte p1Type = (byte)(p1 & LIFECYCLE_P1_TYPE_MASK);
        if (p1Type == LIFECYCLE_P1_TYPE_FILE) {
            ISOFile file = processLifecycleFindFile(apdu);
            file.deactivate();
        } else {
            ISOException.throwIt(SW_WRONG_P1P2);
        }
    }

    /**
     * Process TERMINATE
     *
     * @param apdu to process
     */
    private void processTerminate(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        byte p1 = buf[OFFSET_P1];
        byte p1Type = (byte)(p1 & LIFECYCLE_P1_TYPE_MASK);
        if (p1Type == LIFECYCLE_P1_TYPE_FILE) {
            ISOFile file = processLifecycleFindFile(apdu);
            file.terminate();
        } else {
            ISOException.throwIt(SW_WRONG_P1P2);
        }
    }

    /**
     * Process TERMINATE EF
     *
     * @param apdu to process
     */
    private void processTerminateEF(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        byte p1 = buf[OFFSET_P1];
        byte p1Type = (byte)(p1 & LIFECYCLE_P1_TYPE_MASK);
        if (p1Type == LIFECYCLE_P1_TYPE_FILE) {
            ISOFile file = processLifecycleFindFile(apdu);
            if(!(file instanceof EF)) {
                ISOException.throwIt(SW_CONDITIONS_NOT_SATISFIED);
            }
            file.terminate();
        } else {
            ISOException.throwIt(SW_WRONG_P1P2);
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
        EF file = processBinaryFindFile(ins, p12);
        short offset = processBinaryFindOffset(ins, p12);

        // perform the operation
        boolean handled = true;
        switch(ins & (byte)0xFE) {
            case INS_READ_BINARY1:
                processReadBinary(apdu, ins, file, offset);
                break;
            case INS_SEARCH_BINARY1:
                processSearchBinary(apdu, ins, file, offset);
                break;
            case INS_WRITE_BINARY1:
                processWriteBinary(apdu, ins, file, offset);
                break;
            case INS_UPDATE_BINARY1:
                processUpdateBinary(apdu, ins, file, offset);
                break;
            case INS_ERASE_BINARY1:
                processEraseBinary(apdu, ins, file, offset);
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
    private EF processBinaryFindFile(byte ins, short p12) {
        DF current = getSelectedDF();
        EF selected = getSelectedEF();
        EF file = selected;
        if((ins & 0x01) == 0) {
            if((p12 & (short)0x8000) != 0) {
                byte sfi = (byte)(p12 & 0x1F);
                file = (EF)current.findChildBySFI(sfi, DF.FIND_TYPE_EF);
            }
        } else {
            short fid = (short)(p12 & (short)0x7FFF);
            file = (EF)current.findChildByFID(fid, DF.FIND_TYPE_EF);
        }
        if(file != selected) {
            selectFile(file);
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
            if ((p12 & (short)0x8000) == 0) {
                off = (short) (p12 & (short)0x7FFF);
            }
        } else {
            // process offset DO?
            ISOException.throwIt(SW_FUNC_NOT_SUPPORTED);
        }
        return off;
    }

    /**
     * Process READ BINARY
     *
     * @param apdu to process
     */
    private void processReadBinary(APDU apdu, byte ins, EF file, short offset) {
        // access the file
        EFTransparent eft = accessFileBinary(file, ACCESS_EF_READ);
        byte[] data = eft.getData();

        // determine output length
        short le = apdu.setOutgoingNoChaining();

        // adjust for end of file
        short limit = (short)(offset + le);
        if(limit < 0) {
            ISOException.throwIt(SW_WRONG_LENGTH);
        }
        if(limit >= data.length) {
            le = (short)(data.length - offset);
        }

        // perform the operation
        if((ins & 0x01) == 0) {
            // send the response
            apdu.setOutgoingLength(le);
            apdu.sendBytesLong(data, offset, le);
        } else {
            // unsupported
            ISOException.throwIt(SW_INS_NOT_SUPPORTED);
        }
    }

    /**
     * Process SEARCH BINARY
     *
     * @param apdu to process
     */
    private void processSearchBinary(APDU apdu, byte ins, EF file, short offset) {
        byte[] buf = apdu.getBuffer();
        EFTransparent eft = accessFileBinary(file, ACCESS_EF_READ);

        // unsupported
        ISOException.throwIt(SW_INS_NOT_SUPPORTED);
    }

    /**
     * Process WRITE BINARY
     *
     * @param apdu to process
     */
    private void processWriteBinary(APDU apdu, byte ins, EF file, short offset) {
        byte[] buf = apdu.getBuffer();
        EFTransparent eft = accessFileBinary(file, ACCESS_EF_WRITE);

        short lc = apdu.getIncomingLength();
        short oc = apdu.getOffsetCdata();

        // check for end of file
        short limit = (short)(offset + lc);
        if(limit < 0 || limit >= eft.getLength()) {
            ISOException.throwIt(SW_WRONG_LENGTH);
        }

        // perform the operation
        if((ins & 0x01) == 0) {
            // perform the update
            eft.writeData(offset, buf, oc, lc);
        } else {
            // unsupported
            ISOException.throwIt(SW_INS_NOT_SUPPORTED);
        }
    }

    /**
     * Process UPDATE BINARY
     *
     * @param apdu to process
     */
    private void processUpdateBinary(APDU apdu, byte ins, EF file, short offset) {
        byte[] buf = apdu.getBuffer();
        EFTransparent eft = accessFileBinary(file, ACCESS_EF_UPDATE);

        short lc = apdu.getIncomingLength();
        short oc = apdu.getOffsetCdata();

        // check for end of file
        short limit = (short)(offset + lc);
        if(limit < 0 || limit >= eft.getLength()) {
            ISOException.throwIt(SW_WRONG_LENGTH);
        }

        // perform the operation
        if((ins & 0x01) == 0) {
            // perform the update
            eft.updateData(offset, buf, oc, lc);
        } else {
            // unsupported
            ISOException.throwIt(SW_INS_NOT_SUPPORTED);
        }
    }

    /**
     * Process ERASE BINARY
     *
     * @param apdu to process
     */
    private void processEraseBinary(APDU apdu, byte ins, EF file, short offset) {
        byte[] buf = apdu.getBuffer();
        EFTransparent eft = accessFileBinary(file, ACCESS_EF_UPDATE);

        short endOffset = -1;
        // XXX !!?
        if(endOffset == -1) {
            endOffset = eft.getLength();
        }

        eft.eraseData(offset, endOffset);
    }

    /**
     * Process a RECORD command
     *
     * All RECORD commands have a common coding for their
     * parameters in parts of P2, so they share implementation.
     *
     * @param apdu to process
     */
    private boolean processRecord(APDU apdu, short ins) {
        byte[] buf = apdu.getBuffer();
        byte p1 = buf[OFFSET_P1];
        byte p2 = buf[OFFSET_P2];

        EF file = processRecordFindFile(apdu, p2);

        boolean handled = true;
        switch (ins & (byte)0xFE) {
            case INS_READ_RECORD1:
                processReadRecord(apdu, file, p1, p2);
                break;
            case INS_SEARCH_RECORD:
                processSearchRecord(apdu, file, p1, p2);
                break;
            case INS_UPDATE_RECORD1:
                processUpdateRecord(apdu, file, p1, p2);
                break;
            case INS_WRITE_RECORD:
                processWriteRecord(apdu, file, p1, p2);
                break;
            case INS_APPEND_RECORD:
                processAppendRecord(apdu, file, p1, p2);
                break;
            case INS_ERASE_RECORD:
                processEraseRecord(apdu, file, p1, p2);
                break;
            default:
                handled = false;
                break;
        }

        return handled;
    }

    private EF processRecordFindFile(APDU apdu, byte p2) {
        DF current = getSelectedDF();
        EF file = getSelectedEF();
        byte sfi = (byte)(((byte)(p2 >> 3)) & 0x1f);
        if(sfi != 0 && sfi != 0x1f) {
            file = (EF)current.findChildBySFI(sfi, DF.FIND_TYPE_EF);
        }
        return file;
    }

    private void processReadRecord(APDU apdu, EF file, byte p1, byte p2) {
        byte[] buf = apdu.getBuffer();
        byte firstRecord, lastRecord;
        byte p2Select = (byte)(p2 & READ_RECORD_P2_SELECT_MASK);
        switch (p2Select) {
            // XXX implement SELECT BY RID
            case READ_RECORD_P2_SELECT_NUMBER:
                firstRecord = p1;
                lastRecord = p1;
                break;
            case READ_RECORD_P2_SELECT_NUMBER_DOWN:
                firstRecord = p1;
                lastRecord = -1;
                break;
            case READ_RECORD_P2_SELECT_NUMBER_UP:
                firstRecord = -1;
                lastRecord = p1;
                break;
            default:
                ISOException.throwIt(SW_INCORRECT_P1P2);
                break;
        }

        // XXX implement
        ISOException.throwIt(SW_INS_NOT_SUPPORTED);
    }

    private void processSearchRecord(APDU apdu, EF file, byte p1, byte p2) {
        byte[] buf = apdu.getBuffer();

        // XXX implement
        ISOException.throwIt(SW_INS_NOT_SUPPORTED);
    }

    private void processWriteRecord(APDU apdu, EF file, byte p1, byte p2) {
        byte[] buf = apdu.getBuffer();
        byte record;
        byte p2Select = (byte)(p2 & WRITE_RECORD_P2_SELECT_MASK);
        switch (p2Select) {
            case WRITE_RECORD_P2_SELECT_FIRST:
                break;
            case WRITE_RECORD_P2_SELECT_LAST:
                break;
            case WRITE_RECORD_P2_SELECT_NEXT:
                break;
            case WRITE_RECORD_P2_SELECT_PREV:
                break;
            case WRITE_RECORD_P2_SELECT_NUMBER:
                record = p1;
                break;
            default:
                ISOException.throwIt(SW_INCORRECT_P1P2);
                break;
        }

        // XXX implement
        ISOException.throwIt(SW_INS_NOT_SUPPORTED);
    }

    private void processUpdateRecord(APDU apdu, EF file, byte p1, byte p2) {
        byte[] buf = apdu.getBuffer();
        byte p2Write = (byte)(p2 & UPDATE_RECORD_P2_WRITE_MASK);
        switch (p2Write) {
            default:
                ISOException.throwIt(SW_INCORRECT_P1P2);
                break;
        }

        // XXX implement
        ISOException.throwIt(SW_INS_NOT_SUPPORTED);
    }

    private void processAppendRecord(APDU apdu, EF file, byte p1, byte p2) {
        byte[] buf = apdu.getBuffer();
        // P1 must be 0
        if(p1 != 0) {
            ISOException.throwIt(SW_INCORRECT_P1P2);
        }

        // XXX implement
        ISOException.throwIt(SW_INS_NOT_SUPPORTED);
    }

    private void processEraseRecord(APDU apdu, EF file, byte p1, byte p2) {
        byte[] buf = apdu.getBuffer();
    }

    private void processGetData(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        // XXX implement
        ISOException.throwIt(SW_INS_NOT_SUPPORTED);
    }

    private void processPutData(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        // XXX implement
        ISOException.throwIt(SW_INS_NOT_SUPPORTED);
    }

}
