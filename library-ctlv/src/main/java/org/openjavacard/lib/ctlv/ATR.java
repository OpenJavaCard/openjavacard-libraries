package org.openjavacard.lib.ctlv;

/**
 * Definitions for ATR historical bytes
 */
public class ATR {

    /** Category indicator: historical bytes coded in non-TLV format */
    public static final byte CATEGORY_NON_TLV = (byte)0x00;
    /** Category indicator: historical bytes are a DIR data reference */
    public static final byte CATEGORY_DIR_DATA_REFERENCE = (byte)0x10;
    /** Category indicator: historical bytes coded in COMPACT-TLV */
    public static final byte CATEGORY_COMPACT_TLV = (byte)0x80;

    /** Tag for country data */
    public static final byte TAG_COUNTRY = (byte)0x10;
    /** Tag for issuer identification number (IIN) */
    public static final byte TAG_ISSUER = (byte)0x20;
    /** Coding for card service data (CSD) */
    public static final byte TAG_CARD_SERVICE_DATA = (byte)0x30;
    /** Tag for initial access data (IAD) retrieval information */
    public static final byte TAG_INITIAL_ACCESS_DATA = (byte)0x40;
    /** Tag for card issuer data (proprietary coding) */
    public static final byte TAG_CARD_ISSUER_DATA = (byte)0x50;
    /** Tag for pre-issuing data (proprietary coding) */
    public static final byte TAG_PRE_ISSUING_DATA = (byte)0x60;
    /** Tag for card capability bytes (SFT1, optional SFT2 and SFT3) */
    public static final byte TAG_CARD_CAPABILITIES = (byte)0x70;
    /** Tag for status indicator (LCS, SW or LCS+SW) */
    public static final byte TAG_STATUS_INDICATOR = (byte)0x80;

    /* Life cycle status */

    public static final byte LCS_UNKNOWN = (byte)0x00;
    public static final byte LCS_CREATION = (byte)0x01;
    public static final byte LCS_INITIALIZATION = (byte)0x03;
    public static final byte LCS_OPERATIONAL_DEACTIVATED = (byte)0x04;
    public static final byte LCS_OPERATIONAL_ACTIVATED = (byte)0x05;
    public static final byte LCS_TERMINATED = (byte)0x0C;

    /* Card service data */

    public static final byte CSD_SELECT_BY_DFNAME_FULL = (byte)0x80;
    public static final byte CSD_SELECT_BY_DFNAME_PARTIAL = (byte)0x40;
    public static final byte CSD_DO_IN_EF_DIR = (byte)0x20;
    public static final byte CSD_DO_IN_EF_ATR = (byte)0x10;
    public static final byte CSD_READ_MASK = (byte)0x08;
    public static final byte CSD_READ_RECORD = (byte)0x00;
    public static final byte CSD_READ_BINARY = (byte)0x08;
    public static final byte CSD_RESERVED_MASK = (byte)0x07;
    public static final byte CSD_RESERVED_OKAY = (byte)0x00;

    /* Card capabilities (Software function table 1) */

    public static final byte SFT1_DF_SELECT_BY_DFNAME_FULL = (byte)0x80;
    public static final byte SFT1_DF_SELECT_BY_DFNAME_PARTIAL = (byte)0x40;
    public static final byte SFT1_DF_SELECT_BY_PATH = (byte)0x20;
    public static final byte SFT1_DF_SELECT_BY_FILEID = (byte)0x10;
    public static final byte SFT1_DF_SELECT_IMPLICIT = (byte)0x08;
    public static final byte SFT1_EF_SFI_SUPPORTED = (byte)0x04;
    public static final byte SFT1_EF_RECORD_NUMBER_SUPPORTED = (byte)0x02;
    public static final byte SFT1_EF_RECORD_IDENTIFIER_SUPPORTED = (byte)0x01;

    /* Card capabilities (Software function table 2) */

    public static final byte SFT2_WRITE_MASK = (byte)0x60;
    public static final byte SFT2_WRITE_ONCE = (byte)0x00;
    public static final byte SFT2_WRITE_PROPRIETARY = (byte)0x20;
    public static final byte SFT2_WRITE_OR = (byte)0x40;
    public static final byte SFT2_WRITE_AND = (byte)0x60;
    public static final byte SFT2_DUSIZE_MASK = (byte)0x07;
    public static final byte SFT2_RESERVED_MASK = (byte)0x91;
    public static final byte SFT2_RESERVED_OKAY = (byte)0x00;

    /* Card capabilities (Software function table 3) */

    public static final byte SFT3_EXTENDED_APDU = (byte)0x40;
    public static final byte SFT3_CHANNELS_MASK = (byte)0x18;
    public static final byte SFT3_CHANNELS_UNSUPPORTED = (byte)0x00;
    public static final byte SFT3_CHANNELS_ASSIGNED_BY_CARD = (byte)0x08;
    public static final byte SFT3_CHANNELS_ASSIGNED_BY_HOST = (byte)0x10;
    public static final byte SFT3_CHANNELS_RESERVED = (byte)0x18;
    public static final byte SFT3_MAX_CHANNELS_MASK = (byte)0x03;
    public static final byte SFT3_RESERVED_MASK = (byte)0xA4;
    public static final byte SFT3_RESERVED_OKAY = (byte)0x00;

}
