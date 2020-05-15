package org.openjavacard.lib.isofs;

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

    public byte[] getRecord(short index) {
        return (byte[])mRecords[index];
    }

}
