package org.openjavacard.lib.isofs;

import javacard.framework.ISO7816;

public interface ISOExtensions extends ISO7816 {

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
    byte INS_DELETE = (byte)0xE4;
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
    byte INS_CREATE = (byte)0xE1;
    byte INS_GET_DATA = (byte)0xCA;
    byte INS_PUT_DATA = (byte)0xDA;
    byte INS_MANAGE_DATA = (byte)0xCF;
    byte INS_ACTIVATE = (byte)0x44;
    byte INS_DEACTIVATE = (byte)0x04;
    byte INS_ACTIVATE_RECORD = (byte)0x08;
    byte INS_DEACTIVATE_RECORD = (byte)0x06;
    byte INS_TERMINATE_DF = (byte)0xE6;
    byte INS_TERMINATE_EF = (byte)0xE8;
    byte INS_TERMINATE_CARD_USAGE = (byte)0xFE;
    byte INS_IMPORT_CARD_SECRET = (byte)0x48;
    byte INS_COMPARE = (byte)0x33;

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

    /* DCB Data coding byte */

    byte DCB_BERSUPPORT_FLAG = (byte)0x80;

    byte DCB_WRITE_MASK = (byte)0x60;
    byte DCB_WRITE_ONCE = (byte)0x00;
    byte DCB_WRITE_PROPRIETARY = (byte)0x20;
    byte DCB_WRITE_OR = (byte)0x40;
    byte DCB_WRITE_AND = (byte)0x60;

    byte DCB_TLVFF_MASK = (byte)0x10;
    byte DCB_TLVFF_INVALID = (byte)0x00;
    byte DCB_TLVFF_VALID = (byte)0x10;

    byte DCB_DUSIZE_MASK = (byte)0x0F;

    /* LCS - Life cycle state */

    byte LCS_UNKNOWN = (byte)0x00;
    byte LCS_CREATION = (byte)0x01;
    byte LCS_INITIALIZATION = (byte)0x03;
    byte LCS_OPERATIONAL_DEACTIVATED = (byte)0x04;
    byte LCS_OPERATIONAL_ACTIVATED = (byte)0x05;
    byte LCS_TERMINATED = (byte)0x0C;

    /* CSA - Channel security attribute */

    byte CSA_RESERVED_MASK = (byte)0xF8;
    byte CSA_RESERVED_OKAY = (byte)0x00;
    byte CSA_NONSHARED     = (byte)0x04;
    byte CSA_SECURED       = (byte)0x02;
    byte CSA_AUTHENTICATED = (byte)0x01;

    /* SCB - Security condition byte */

    byte SCB_ALWAYS = (byte)0x00;
    byte SCB_NEVER = (byte)0xFF;

    byte SCB_MODE_MASK = (byte)0x80;
    byte SCB_MODE_OR = (byte)0x00;
    byte SCB_MODE_AND  = (byte)0x80;

    byte SCB_CHECK_MASK = (byte)0x70;
    byte SCB_CHECK_SM   = (byte)0x40;
    byte SCB_CHECK_EA   = (byte)0x20;
    byte SCB_CHECK_UA   = (byte)0x10;

    byte SCB_ENVIRONMENT_MASK = (byte)0x0F;
    byte SCB_ENVIRONMENT_NONE = (byte)0x00;






    byte ACCESS_DF_DELETE_SELF = 6;
    byte ACCESS_DF_TERMINATE = 5;
    byte ACCESS_DF_ACTIVATE_FILE = 4;
    byte ACCESS_DF_DEACTIVATE_FILE = 3;
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
}
