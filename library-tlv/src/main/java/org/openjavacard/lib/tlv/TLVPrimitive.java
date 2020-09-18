package org.openjavacard.lib.tlv;

import javacard.framework.Util;

public class TLVPrimitive extends TLVNode {

    private short mLength;
    private byte[] mValue;

    public TLVPrimitive(short tag, short len) {
        super(tag);
        mLength = len;
        if(len > 0) {
            mValue = new byte[len];
        }
    }

    public TLVPrimitive(short tag) {
        super(tag);
    }

    public final short getLength() {
        return mLength;
    }

    public short readValue(byte[] buf, short off, short maxLen) {
        return Util.arrayCopyNonAtomic(mValue, (short)0, buf, off, maxLen);
    }

    public short updateValue(byte[] buf, short off, short len) {
        return Util.arrayCopyNonAtomic(buf, off, mValue, (short)0, len);
    }

}
