package org.openjavacard.lib.codecs;

public interface Encoder {

    short encode(byte[] inBuf, short inOff, short inLen,
                 byte[] outBuf, short outOff, short outMaxLen);

}
