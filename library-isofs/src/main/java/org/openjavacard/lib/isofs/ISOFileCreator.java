package org.openjavacard.lib.isofs;

import javacard.framework.JCSystem;
import javacard.framework.Util;
import org.openjavacard.lib.ber.BERHandler;
import org.openjavacard.lib.ber.BERReader;

/**
 * ISO7816-4 file creator
 */
public class ISOFileCreator implements BERHandler, ISOExtensions {

    private final Object[] mRefs;
    private static final short REF_PARENT = 0;
    private static final short NUM_REFS = 1;

    private final short[] mVars;
    private static final short VAR_FID = 0;
    private static final short VAR_FDB = 1;
    private static final short VAR_LCS = 2;
    private static final short VAR_CSA = 3;
    private static final short VAR_SFI = 4;
    private static final short NUM_VARS = 5;

    ISOFileCreator() {
        mRefs = JCSystem.makeTransientObjectArray(NUM_REFS, JCSystem.CLEAR_ON_RESET);
        mVars = JCSystem.makeTransientShortArray(NUM_VARS, JCSystem.CLEAR_ON_RESET);
    }

    public void prepare(DF parent) {
        mRefs[REF_PARENT] = parent;
    }

    public boolean handlePrimitive(BERReader reader, byte depth, short tag,
                                   byte[] dataBuf, short dataOff, short dataLen) {
        if(depth == 1) {
            if(tag == TAG_FCI_FDB) {
                if(dataLen >= 1) {
                    mVars[VAR_FDB] = dataBuf[dataOff];
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
