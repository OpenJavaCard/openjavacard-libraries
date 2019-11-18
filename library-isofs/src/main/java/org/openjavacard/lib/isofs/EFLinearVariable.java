package org.openjavacard.lib.isofs;

public class EFLinearVariable extends EF {

    private static final byte FDB = FDB_CATEGORY_EF_WORKING|FDB_STRUCTURE_LINEAR_VARIABLE;

    EFLinearVariable(short fid) {
        super(fid, FDB);
    }

}
