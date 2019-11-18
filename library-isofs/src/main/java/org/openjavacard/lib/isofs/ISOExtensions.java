package org.openjavacard.lib.isofs;

import javacard.framework.ISO7816;

interface ISOExtensions extends ISO7816 {

    /* INS - Instruction bytes */

    byte INS_SELECT = ISO7816.INS_SELECT;
    byte SELECT_P1_SELECT_MASK     = (byte)0xFF;
    byte SELECT_P1_SELECT_MF_DF_EF = (byte)0x00;
    byte SELECT_P1_SELECT_CHILD_DF = (byte)0x01;
    byte SELECT_P1_SELECT_CHILD_EF = (byte)0x02;
    byte SELECT_P1_SELECT_PARENT   = (byte)0x03;
    byte SELECT_P1_SELECT_DFNAME   = (byte)0x04;
    byte SELECT_P1_SELECT_PATH_MF  = (byte)0x08;
    byte SELECT_P1_SELECT_PATH_DF  = (byte)0x09;
    byte SELECT_P2_RESERVED_MASK = (byte)0xF0;
    byte SELECT_P2_RESERVED_OKAY = (byte)0x00;
    byte SELECT_P2_RETURN_MASK        = (byte)0x0C;
    byte SELECT_P2_RETURN_FCI         = (byte)0x00;
    byte SELECT_P2_RETURN_FCP         = (byte)0x04;
    byte SELECT_P2_RETURN_FMD         = (byte)0x08;
    byte SELECT_P2_RETURN_PROPRIETARY = (byte)0x0C;
    byte SELECT_P2_ITERATE_MASK     = (byte)0x03;
    byte SELECT_P2_ITERATE_FIRST    = (byte)0x00;
    byte SELECT_P2_ITERATE_LAST     = (byte)0x01;
    byte SELECT_P2_ITERATE_NEXT     = (byte)0x02;
    byte SELECT_P2_ITERATE_PREVIOUS = (byte)0x03;

    byte INS_CREATE_FILE = (byte)0xE0;
    byte INS_DELETE_FILE = (byte)0xE4;
    byte INS_READ_BINARY1 = (byte)0xB0;
    byte INS_READ_BINARY2 = (byte)0xB1;
    byte INS_UPDATE_BINARY1 = (byte)0xD6;
    byte INS_UPDATE_BINARY2 = (byte)0xD7;
    byte INS_WRITE_BINARY1 = (byte)0xD0;
    byte INS_WRITE_BINARY2 = (byte)0xD1;
    byte INS_SEARCH_BINARY1 = (byte)0xA0;
    byte INS_SEARCH_BINARY2 = (byte)0xA1;
    byte INS_ERASE_BINARY1 = (byte)0x0E;
    byte INS_ERASE_BINARY2 = (byte)0x0F;
    byte INS_READ_RECORD1 = (byte)0xB2;
    byte INS_READ_RECORD2 = (byte)0xB3;
    byte INS_UPDATE_RECORD1 = (byte)0xDC;
    byte INS_UPDATE_RECORD2 = (byte)0xDD;
    byte INS_WRITE_RECORD = (byte)0xD2;
    byte INS_APPEND_RECORD = (byte)0xE2;
    byte INS_SEARCH_RECORD = (byte)0xA2;
    byte INS_ERASE_RECORD = (byte)0x0C;
    byte INS_GET_DATA = (byte)0xCA;
    byte INS_PUT_DATA = (byte)0xDA;

    /* FID - File identifiers */

    /** FID of the Master File */
    short FID_MF = (short)0x3F00;
    /** FID of EF.DIR */
    short FID_EF_DIR = (short)0x2F00;
    /** FID of EF.ATR */
    short FID_EF_ATR = (short)0x2F01;

    /* TAG - Tag values for TLV structures */

    /** Tag for File Control Parameter template */
    short TAG_FCP = (short)0x6200;
    /** Tag for File Management Data template */
    short TAG_FMD = (short)0x6400;
    /** Tag for File Control Information template */
    short TAG_FCI = (short)0x6F00;

    short TAG_FCI_SIZE_CONTENT = (short)0x8000;
    short TAG_FCI_SIZE_TOTAL = (short)0x8100;
    short TAG_FCI_FDB = (short)0x8200;
    short TAG_FCI_FILEID = (short)0x8300;
    short TAG_FCI_DFNAME = (short)0x8400;
    short TAG_FCI_PROPRIETARY = (short)0x8500;
    short TAG_FCI_SECURITY_PROPRIETARY = (short)0x8600;
    short TAG_FCI_EXTENSION = (short)0x8700;
    short TAG_FCI_SFI = (short)0x8800;
    short TAG_FCI_LCS = (short)0x8A00;
    short TAG_FCI_SECURITY_EXPANDED = (short)0x8B00;
    short TAG_FCI_SECURITY_COMPACT = (short)0x8C00;
    short TAG_FCI_SECENV_TEMPLATE = (short)0x8D00;
    short TAG_FCI_CSA = (short)0x8E00;

    /* FDB - File descriptor byte */

    byte FDB_SHAREABLE = (byte)0x40;

    byte FDB_CATEGORY_MASK        = (byte)0x38;
    byte FDB_CATEGORY_SPECIAL     = (byte)0x38;
    byte FDB_CATEGORY_EF_WORKING  = (byte)0x00;
    byte FDB_CATEGORY_EF_INTERNAL = (byte)0x08;

    byte FDB_SPECIAL_MASK         = (byte)0x03;
    byte FDB_SPECIAL_DF           = (byte)0x00;
    byte FDB_SPECIAL_TLV_BER      = (byte)0x01;
    byte FDB_SPECIAL_TLV_SIMPLE   = (byte)0x02;

    byte FDB_STRUCTURE_MASK        = (byte)0x07;
    byte FDB_STRUCTURE_UNKNOWN     = (byte)0x00;
    byte FDB_STRUCTURE_TRANSPARENT = (byte)0x01;
    byte FDB_STRUCTURE_LINEAR_FIXED     = (byte)0x02;
    byte FDB_STRUCTURE_LINEAR_FIXED_TLV = (byte)0x03;
    byte FDB_STRUCTURE_LINEAR_VARIABLE     = (byte)0x04;
    byte FDB_STRUCTURE_LINEAR_VARIABLE_TLV = (byte)0x05;
    byte FDB_STRUCTURE_CYCLIC_FIXED     = (byte)0x06;
    byte FDB_STRUCTURE_CYCLIC_FIXED_TLV = (byte)0x07;

    /* LCS - Life cycle state */

    byte LCS_UNKNOWN = (byte)0x00;
    byte LCS_CREATION = (byte)0x01;
    byte LCS_INITIALIZATION = (byte)0x03;
    byte LCS_OPERATIONAL_ACTIVATED = (byte)0x05;
    byte LCS_OPERATIONAL_DEACTIVATED = (byte)0x04;
    byte LCS_TERMINATED = (byte)0x0C;

    /* CSA - Channel security attribute */

    byte CSA_RESERVED_MASK = (byte)0xF8;
    byte CSA_RESERVED_OKAY = (byte)0x00;
    byte CSA_NONSHARED     = (byte)0x04;
    byte CSA_SECURED       = (byte)0x02;
    byte CSA_AUTHENTICATED = (byte)0x01;


    byte ACCESS_DF_DELETE = 6;
    byte ACCESS_DF_TERMINATE = 5;
    byte ACCESS_DF_ACTIVATE = 4;
    byte ACCESS_DF_DEACTIVATE = 3;
    byte ACCESS_DF_CREATE_DF = 2;
    byte ACCESS_DF_CREATE_EF = 1;
    byte ACCESS_DF_DELETE_CHILD = 0;

    byte ACCESS_EF_DELETE = 6;
    byte ACCESS_EF_TERMINATE = 5;
    byte ACCESS_EF_ACTIVATE = 4;
    byte ACCESS_EF_DEACTIVATE = 3;
    byte ACCESS_EF_WRITE = 2;
    byte ACCESS_EF_UPDATE = 1;
    byte ACCESS_EF_READ = 0;

    byte ACCESS_DO_MANAGE = 2;
    byte ACCESS_DO_PUT_DATA = 1;
    byte ACCESS_DO_GET_DATA = 0;

    byte SECURITY_ALWAYS = (byte)0x00;
    byte SECURITY_NEVER  = (byte)0xFF;

    byte SECURITY_MODE_MASK = (byte)0x80;
    byte SECURITY_MODE_OR   = (byte)0x00;
    byte SECURITY_MODE_AND  = (byte)0x80;

    byte SECURITY_CHECK_MASK = (byte)0x70;
    byte SECURITY_CHECK_SM   = (byte)0x40;
    byte SECURITY_CHECK_EA   = (byte)0x20;
    byte SECURITY_CHECK_UA   = (byte)0x10;

    byte SECURITY_ENVIRONMENT_MASK = (byte)0x0F;
    byte SECURITY_ENVIRONMENT_NONE = (byte)0x00;
    
}
