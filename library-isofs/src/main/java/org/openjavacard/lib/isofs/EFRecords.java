package org.openjavacard.lib.isofs;

public class EFRecords extends EF {

    private final Object[] mRecords;

    EFRecords(DF parent, byte fdb, short fid, byte sfi, short maxRecords, short maxSize) {
        super(parent, fdb, fid, sfi);
        mRecords = new Object[maxRecords];
    }

    public byte[] getRecord(short index) {
        return (byte[])mRecords[index];
    }

}
