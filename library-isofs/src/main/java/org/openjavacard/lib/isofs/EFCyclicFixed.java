package org.openjavacard.lib.isofs;

public class EFCyclicFixed extends EF {

    private static final byte FDB = FDB_CATEGORY_EF_WORKING|FDB_STRUCTURE_CYCLIC_FIXED;

    EFCyclicFixed(short fid) {
        super(fid, FDB);
    }

}
