package org.openjavacard.lib.ctlv;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 * Compact-TLV processing functions
 * <p/>
 * Compact-TLV (or CTLV) is a special TLV format defined in ISO7816 for use in ATR historical bytes.
 * <p/>
 * This class provides functions and definitions for processing and producing Compact-TLV data.
 * <p/>
 */
public class CTLV {

    /** Mask for a COMPACT-TLV tag */
    public static final byte MASK_TAG = (byte)0xF0;
    /** Mask for a COMPACT-TLV length */
    public static final byte MASK_LENGTH = (byte)0x0F;
    /** Maximum length of a COMPACT-TLV value */
    public static final byte MAX_LENGTH = (byte)0x0F;

    /**
     * Retrieve a C-TLV tag
     * @param inBuf
     * @param inOff
     * @return
     */
    public static byte getTag(byte[] inBuf, short inOff) {
        return (byte)(inBuf[inOff]& MASK_TAG);
    }

    /**
     * Retrieve a C-TLV length
     * @param inBuf
     * @param inOff
     * @return
     */
    public static byte getLength(byte[] inBuf, short inOff) {
        return (byte)(inBuf[inOff] & MASK_LENGTH);
    }

    /**
     * Find offset of a C-TLV tag, if present
     * <p/>
     * @param inBuf containing C-TLV data
     * @param inOff offset of data
     * @param inLen length of data
     * @param tag to search for
     * @return offset of tag, -1 if not found
     */
    public static short find(byte[] inBuf, short inOff, short inLen, byte tag) {
        // start at given offset
        short off = inOff;
        // determine end of data
        short end = (short)(inOff + inLen - 1);
        // search for the tag
        while(off < end) {
            // get fields of current tag
            byte t = getTag(inBuf, off);
            byte l = getLength(inBuf, off);
            // is this the requested tag?
            if(t == tag) {
                // return offset of TL byte
                return off;
            }
            // skip TL byte and data
            off += 1 + l;
        }
        // tag not found
        return -1;
    }

    /**
     * Put a C-TLV object containing one byte
     * <p/>
     * @param outBuf output buffer
     * @param outOff offset in buffer
     * @param outLen maximum length
     * @param tag to put
     * @param value for tag
     * @return length of output
     */
    public static short putByte(byte[] outBuf, short outOff, short outLen,
                               byte tag, byte value) {
        short off = outOff;
        byte t = (byte)(tag & MASK_TAG);
        byte l = 1;
        // check parameters
        if(t != tag) {
            error();
        }
        // check space
        if(outLen < 2) {
            error();
        }
        // put TL byte
        outBuf[off++] = (byte)(t | l);
        // put data
        outBuf[off++] = value;
        // return new offset
        return (short)(off - outOff);
    }

    /**
     * Put a C-TLV object containing one short
     * <p/>
     * @param outBuf output buffer
     * @param outOff offset in buffer
     * @param outLen maximum length
     * @param tag to put
     * @param value for tag
     * @return length of output
     */
    public static short putShort(byte[] outBuf, short outOff, short outLen,
                                byte tag, short value) {
        short off = outOff;
        byte t = (byte)(tag & MASK_TAG);
        byte l = 2;
        // check parameters
        if(t != tag) {
            error();
        }
        // check space
        if(outLen < 3) {
            error();
        }
        // put TL byte
        outBuf[off++] = (byte)(t | l);
        // put data
        off = Util.setShort(outBuf, off, value);
        // return new offset
        return (short)(off - outOff);
    }

    /**
     * Put a C-TLV object containing several bytes
     * <p/>
     * @param outBuf output buffer
     * @param outOff offset in buffer
     * @param outLen maximum length
     * @param tag to put
     * @param valueBuf buffer containing value
     * @param valueOff offset of value
     * @param valueLen length of value
     * @return length of output
     */
    public static short putBytes(byte[] outBuf, short outOff, short outLen,
                                byte tag, byte[] valueBuf, short valueOff, short valueLen) {
        short off = outOff;
        byte t = (byte)(tag & MASK_TAG);
        byte l = (byte)(valueLen & MASK_LENGTH);
        // check parameters
        if((t != tag) || (l != valueLen) || (valueLen > MAX_LENGTH)) {
            error();
        }
        // check space
        if(outLen < (short)(l+1)) {
            error();
        }
        // put TL byte
        outBuf[off++] = (byte)(t | l);
        // put data
        off = Util.arrayCopyNonAtomic(valueBuf, valueOff, outBuf, off, valueLen);
        // return new offset
        return (short)(off - outOff);
    }

    /**
     * Internal: throw exception because of an error
     */
    private static void error() {
        ISOException.throwIt(ISO7816.SW_UNKNOWN);
    }

}
