package org.openjavacard.lib.codec;

public interface Encoder {

    short encode(byte[] inBuf, short inOff, short inLen,
                 byte[] outBuf, short outOff, short outMaxLen);

}
