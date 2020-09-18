package org.openjavacard.lib.isofs;

import javacard.framework.JCSystem;
import javacard.framework.Util;
import org.openjavacard.lib.ber.BERHandler;
import org.openjavacard.lib.ber.BERReader;
import org.openjavacard.lib.ber.BERSource;

/**
 * ISO7816 file creator
 */
public class ISOFileCreator implements BERHandler, ISOExtensions {

    private final Object[] mRefs;
    private static final short REF_PARENT = 0;
    private static final short NUM_REFS = 1;

    private final short[] mVars;
    private static final short VAR_FID = 0;
    private static final short VAR_FDB = 1;
    private static final short VAR_DCB = 2;
    private static final short VAR_LCS = 3;
    private static final short VAR_CSA = 4;
    private static final short VAR_SFI = 5;
    private static final short VAR_MAX_CONTENT = 6;
    private static final short VAR_MAX_RECORD_COUNT = 7;
    private static final short VAR_MAX_RECORD_SIZE = 8;
    private static final short NUM_VARS = 9;

    ISOFileCreator() {
        mRefs = JCSystem.makeTransientObjectArray(NUM_REFS, JCSystem.CLEAR_ON_RESET);
        mVars = JCSystem.makeTransientShortArray(NUM_VARS, JCSystem.CLEAR_ON_RESET);
    }

    public void prepare(DF parent) {
        mRefs[REF_PARENT] = parent;
    }

    public ISOFile create() {
        DF parent = (DF) mRefs[REF_PARENT];
        byte fdb = (byte)mVars[VAR_FDB];

        ISOFile file = null;

        switch(fdb & FDB_CATEGORY_MASK) {
            case FDB_CATEGORY_EF_WORKING:
            case FDB_CATEGORY_EF_INTERNAL:
                file = createEF(fdb);
                break;
            case FDB_CATEGORY_SPECIAL:
                switch (fdb & FDB_SPECIAL_MASK) {
                    case FDB_SPECIAL_DF:
                        file = createDF(fdb);
                        break;
                    case FDB_SPECIAL_TLV_BER:
                    case FDB_SPECIAL_TLV_SIMPLE:
                        file = createDO(fdb);
                        break;
                    default:
                        // XXX error
                        break;
                }
                break;
            default:
                // XXX error
                break;
        }

        parent.addChild(file);

        return file;
    }

    private EF createEF(byte fdb) {
        DF parent = (DF) mRefs[REF_PARENT];
        short fid = mVars[VAR_FID];
        byte sfi = (byte)mVars[VAR_SFI];
        EF ef = null;

        switch(fdb & FDB_STRUCTURE_MASK) {
            case FDB_STRUCTURE_TRANSPARENT:
                ef = new EFTransparent(parent, fdb, fid, sfi, mVars[VAR_MAX_CONTENT]);
                break;
            case FDB_STRUCTURE_LINEAR_FIXED:
            case FDB_STRUCTURE_LINEAR_FIXED_TLV:
                ef = new EFLinearFixed(parent, fdb, fid, sfi, (short)0, (short)0);
                break;
            case FDB_STRUCTURE_LINEAR_VARIABLE:
            case FDB_STRUCTURE_LINEAR_VARIABLE_TLV:
                ef = new EFLinearVariable(parent, fdb, fid, sfi, (short)0, (short)0);
                break;
            case FDB_STRUCTURE_CYCLIC_FIXED:
            case FDB_STRUCTURE_CYCLIC_FIXED_TLV:
                ef = new EFCyclicFixed(parent, fdb, fid, sfi, (short)0, (short)0);
                break;
            default:
                // XXX error
                break;
        }

        return ef;
    }

    private DF createDF(byte fdb) {
        DF parent = (DF) mRefs[REF_PARENT];
        short fid = mVars[VAR_FID];
        DF df = new DF(parent, fdb, fid);
        return df;
    }

    private EF createDO(byte fdb) {
        return null;
    }

    public boolean handlePrimitive(BERSource source, byte depth, short tag,
                                   byte[] dataBuf, short dataOff, short dataLen) {
        if(depth == 1) {
            if(tag == TAG_FCI_SIZE_CONTENT) {
                if(dataLen == 1) {
                    mVars[VAR_MAX_CONTENT] = (short)(dataBuf[dataOff] & 0xFF);
                    return true;
                }
                if(dataLen == 2) {
                    mVars[VAR_MAX_CONTENT] = Util.getShort(dataBuf, dataOff);
                    return true;
                }
            }
            if(tag == TAG_FCI_FDB) {
                if(dataLen >= 1 && dataLen <= 6) {
                    // first byte is FCB
                    mVars[VAR_FDB] = dataBuf[dataOff++];
                    if(dataLen > 1) {
                        // second byte is DCB
                        mVars[VAR_DCB] = dataBuf[dataOff++];
                        // now decode the max record size
                        if(dataLen == 3) {
                            mVars[VAR_MAX_RECORD_SIZE] = (short)(dataBuf[dataOff++] & 0xFF);
                        }
                        if(dataLen >= 4) {
                            mVars[VAR_MAX_RECORD_SIZE] = Util.getShort(dataBuf, dataOff);
                            dataOff += 2;
                            // there might also be a max record count
                            if(dataLen == 5) {
                                mVars[VAR_MAX_RECORD_COUNT] = (short)(dataBuf[dataOff++] & 0xFF);
                            }
                            if(dataLen == 6) {
                                mVars[VAR_MAX_RECORD_COUNT] = Util.getShort(dataBuf, dataOff);
                                dataOff += 2;
                            }
                        }
                    }
                    return true;
                }
            }
            if(tag == TAG_FCI_FILEID && dataLen == 2) {
                mVars[VAR_FID] = Util.getShort(dataBuf, dataOff);
                return true;
            }
            if(tag == TAG_FCI_LCS && dataLen == 1) {
                mVars[VAR_LCS] = dataBuf[dataOff];
                return true;
            }
            if(tag == TAG_FCI_CSA && dataLen == 1) {
                mVars[VAR_CSA] = dataBuf[dataOff];
                return true;
            }
            if(tag == TAG_FCI_SFI && dataLen == 1) {
                mVars[VAR_SFI] = dataBuf[dataOff];
                return true;
            }
        }
        return false;
    }

    public boolean handleBeginConstructed(BERSource source, byte depth, short tag) {
        if(depth == 0) {
            if (tag == TAG_FCI) {
                return true;
            }
            if (tag == TAG_FCP) {
                return true;
            }
        }
        return false;
    }

    public boolean handleFinishConstructed(BERSource source, byte depth, short tag) {
        if(depth == 0) {
            if (tag == TAG_FCI) {
                return true;
            }
            if (tag == TAG_FCP) {
                return true;
            }
        }
        return false;
    }

}
