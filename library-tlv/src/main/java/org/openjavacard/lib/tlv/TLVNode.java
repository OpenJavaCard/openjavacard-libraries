package org.openjavacard.lib.tlv;

public abstract class TLVNode {

    private short mTag;

    protected TLVNode(short tag) {
        mTag = tag;
    }

    public final short getTag() {
        return mTag;
    }

    public final void setTag(short tag) {
        mTag = tag;
    }

    public final boolean isConstructed() {
        return this instanceof TLVConstructed;
    }

    public final boolean isPrimitive() {
        return this instanceof TLVPrimitive;
    }

    public abstract short getLength();

}
