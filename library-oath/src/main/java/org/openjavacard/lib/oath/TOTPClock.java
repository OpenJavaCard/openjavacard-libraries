package org.openjavacard.lib.oath;

public interface TOTPClock {

    short getLength();

    short getTime(byte[] buf, short off, short len);

}
