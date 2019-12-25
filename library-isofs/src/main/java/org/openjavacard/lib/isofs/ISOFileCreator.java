package org.openjavacard.lib.isofs;

import javacard.framework.JCSystem;
import javacard.framework.Util;
import org.openjavacard.lib.ber.BERHandler;
import org.openjavacard.lib.ber.BERReader;

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
    private static final short NUM_VARS = 6;

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
                }
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
                ef = new EFTransparent(parent, fdb, fid, sfi, (short)128);
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
        DF parent = (DF) mRefs[REF_PARENT];
        short fid = mVars[VAR_FID];
        return null;
    }

    public boolean handlePrimitive(BERReader reader, byte depth, short tag,
                                   byte[] dataBuf, short dataOff, short dataLen) {
        if(depth == 1) {
            if(tag == TAG_FCI_FDB) {
                if(dataLen >= 1 && dataLen <= 5) {
                    mVars[VAR_FDB] = dataBuf[dataOff++];
                    if(dataLen > 1) {
                        mVars[VAR_DCB] = dataBuf[dataOff++];
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

    public boolean handleBeginConstructed(BERReader reader, byte depth, short tag) {
        if(depth == 0 && tag == TAG_FCI) {
            return true;
        }
        if(depth == 0 && tag == TAG_FCP) {
            return true;
        }
        return false;
    }

    public boolean handleFinishConstructed(BERReader reader, byte depth, short tag) {
        if(depth == 0 && tag == TAG_FCI) {
            return true;
        }
        if(depth == 0 && tag == TAG_FCP) {
            return true;
        }
        return false;
    }

}
