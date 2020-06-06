package org.openjavacard.lib.isofs;

import javacard.framework.ISOException;
import javacard.framework.Util;

public abstract class EFRecords extends EF {

    protected final short mMaxRecords;
    protected final short mMaxSize;

    private final Object[] mRecords;

    EFRecords(DF parent, byte fdb, short fid, byte sfi, short maxRecords, short maxSize) {
        super(parent, fdb, fid, sfi);
        mMaxRecords = maxRecords;
        mMaxSize = maxSize;
        mRecords = new Object[maxRecords];
    }

    public byte[] getRecordByIndex(short index) {
        return (byte[])mRecords[index];
    }

    public void readRecord(short fileOff, byte[] dstBuf, short dstOff, short dstLen) {
        // XXX Util.arrayCopy(mData, fileOff, dstBuf, dstOff, dstLen);
    }

    public void updateRecord(short fileOff, byte[] srcBuf, short srcOff, short srcLen) {
        // XXX Util.arrayCopy(srcBuf, srcOff, mData, fileOff, srcLen);
    }

    public void writeRecord(short fileOff, byte[] srcBuf, short srcOff, short srcLen) {
        byte write = (byte)(mDCB & DCB_WRITE_MASK);
        short i;
        switch(write) {
            case DCB_WRITE_ONCE:
                // XXX
                break;
            case DCB_WRITE_PROPRIETARY:
                ISOException.throwIt(SW_COMMAND_NOT_ALLOWED);
                break;
            case DCB_WRITE_OR:
                for(i = 0; i < srcLen; i++) {
                    // XXX mData[(short)(fileOff+i)] |= srcBuf[(short)(srcOff+i)];
                }
                break;
            case DCB_WRITE_AND:
                for(i = 0; i < srcLen; i++) {
                    // XXX mData[(short)(fileOff+i)] &= srcBuf[(short)(srcOff+i)];
                }
                break;
        }
    }

    public void eraseRecord(short startOff, short endOff) {
        for(short off = startOff; off < endOff; off++) {
            // XXX mData[off] = 0;
        }
    }

}
