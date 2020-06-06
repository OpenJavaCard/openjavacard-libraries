package org.openjavacard.lib.isofs;

public interface ISOConfig {

    /**
     * Maximum number of children in a DF
     */
    short DF_SIZE = 16;

    /**
     * Maximum length of FCI/FCP/FMD records
     */
    short BER_MAX_LENGTH = (short)128;

    /**
     * Maximum number of tags in TLV output
     */
    byte  BER_MAX_TAGS = (byte)16;

    /**
     * Maximum depth of tags in TLV output
     */
    byte  BER_MAX_DEPTH = (byte)4;

    /**
     * Maximum number of temp bytes for TLV output
     */
    short BER_MAX_TEMP = 128;

}
