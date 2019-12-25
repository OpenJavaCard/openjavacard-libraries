package org.openjavacard.lib.isofs;

public class EFLinearFixed extends EFRecords {

    EFLinearFixed(DF parent, byte fdb, short fid, byte sfi, short maxRecords, short maxSize) {
        super(parent, fdb, fid, sfi, maxRecords, maxSize);
    }

}
