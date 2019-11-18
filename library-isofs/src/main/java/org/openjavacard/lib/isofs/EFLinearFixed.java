package org.openjavacard.lib.isofs;

public class EFLinearFixed extends EF {

    private static final byte FDB = FDB_CATEGORY_EF_WORKING|FDB_STRUCTURE_LINEAR_FIXED;

    EFLinearFixed(short fid) {
        super(fid, FDB);
    }

}
