package org.openjavacard.lib.tlv;

import org.openjavacard.lib.ber.BERHandler;
import org.openjavacard.lib.ber.BERSource;

public class TLVBuilder implements BERHandler {

    Object[] mObjs;
    private static final short OBJ_ROOT = 0;
    private static final short OBJ_CURRENT = 1;
    private static final short NUM_OBJS = 2;

    public TLVBuilder() {
    }

    private boolean handle(BERSource source, byte depth, TLVNode node) {
        if(depth == 0) {
            mObjs[OBJ_ROOT] = node;
        }
        return false;
    }

    public boolean handlePrimitive(BERSource source, byte depth, short tag,
                                   byte[] dataBuf, short dataOff, short dataLen) {
        TLVPrimitive node = new TLVPrimitive(tag, dataLen);
        node.updateValue(dataBuf, dataOff, dataLen);
        return handle(source, depth, node);
    }

    public boolean handleBeginConstructed(BERSource source, byte depth, short tag) {
        TLVConstructed node = new TLVConstructed(tag);
        return handle(source, depth, node);
    }

    public boolean handleFinishConstructed(BERSource source, byte depth, short tag) {
        return true;
    }

}
