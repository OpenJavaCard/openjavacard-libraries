package org.openjavacard.lib.codec;

public interface Decoder {

    short decode(byte[] inBuf, short inOff, short inLen,
                 byte[] outBuf, short outOff, short outMaxLen);

}
