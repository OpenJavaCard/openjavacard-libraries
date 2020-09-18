package org.openjavacard.lib.tlv;

public class TLVConstructed extends TLVNode {

    private TLVNode[] mChildren;

    public TLVConstructed(short tag) {
        super(tag);
    }

    public final short getLength() {
        return 0;
    }

    public final short getChildCount() {
        return (short)mChildren.length;
    }

    public TLVNode getChild(short index) {
        return mChildren[index];
    }

    public boolean hasChild(TLVNode child) {
        return false;
    }

    public boolean removeChild(TLVNode child) {
        return false;
    }

    public short insertChild(TLVNode child, short position) {
        return 0;
    }

    public short prependChild(TLVNode child) {
        return 0;
    }

    public short appendChild(TLVNode child) {
        return 0;
    }

}
