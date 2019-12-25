package org.openjavacard.lib.isofs;

public class EFCyclicFixed extends EFRecords {

    EFCyclicFixed(DF parent, byte fdb, short fid, byte sfi, short maxRecords, short maxSize) {
        super(parent, fdb, fid, sfi, maxRecords, maxSize);
    }

}
