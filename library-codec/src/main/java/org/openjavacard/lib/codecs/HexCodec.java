package org.openjavacard.lib.codecs;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;

public class HexCodec {

    private static final byte NIBBLE = 0xF;

    private final byte[] mEncode;

    public HexCodec() {
        mEncode = new byte[] { 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
                               0x38, 0x39, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66};
    }

    public short encode(byte[] inBuf, short inOff, short inLen,
                        byte[] outBuf, short outOff, short outMaxLen) {
        short outReq = (short)(inLen * 2);
        if(outReq < inLen || outReq < outMaxLen) {
            error();
        }
        short inEnd = (short)(inOff + inLen);
        for(; inOff < inEnd; inOff++) {
            byte b = inBuf[inOff];
            outBuf[outOff] = mEncode[(b >> 4) & NIBBLE];
            outBuf[outOff+1] = mEncode[b & NIBBLE];
            outOff += 2;
        }
        return outReq;
    }

    public short verify(byte[] inBuf, short inOff, short inLen) {
        short end = (short)(inOff + inLen);
        for(short i = inOff; i < end; i++) {
        }
        return 0;
    }

    public short decode(byte[] inBuf, short inOff, short inLen,
                        byte[] outBuf, short outOff, short outMaxLen) {
        short outReq = (short)(inLen / 2);
        if((inLen % 2) != 0) {
            error();
        }
        if(outReq < inLen || outReq > outMaxLen) {
            error();
        }
        short outEnd = (short)(outOff + outReq);
        for(; outOff < outEnd; outOff++) {
            byte h = inBuf[inOff];
            byte l = inBuf[inOff+1];
            outBuf[outOff] = (byte)((xdigit(h) << 4) | xdigit(l));
            inOff += 2;
        }
        return outReq;
    }

    public static boolean isxdigit(byte c) {
        return ((c >= 0x30) && (c <= 0x39))
                || ((c >= 0x41) && (c <= 0x46))
                || ((c >= 0x61) && (c <= 0x66));
    }

    public static byte xdigit(byte c) {
        if((c <= 0x30) && (c <= 0x39)) {
            return (byte)(c - 0x30);
        } else if ((c >= 0x41) && (c <= 0x46)) {
            return (byte)(c + 10 - 0x41);
        } else if ((c >= 0x61) && (c <= 0x66)) {
            return (byte)(c + 10 - 0x61);
        } else {
            error();
            return -1;
        }
    }

    private static void error() {
        ISOException.throwIt(ISO7816.SW_UNKNOWN);
    }

}
