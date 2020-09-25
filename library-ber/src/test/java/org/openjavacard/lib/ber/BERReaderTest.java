package org.openjavacard.lib.ber;

import javacard.framework.JCSystem;
import org.junit.Before;
import org.junit.Test;

public class BERReaderTest {

    byte[] tmp;

    BERReader mReader;

    @Before
    public void prepare() {
        tmp = new byte[128];
        mReader = new BERReader((byte)32, JCSystem.CLEAR_ON_DESELECT);
    }

    @Test
    public void testEmpty() {
        mReader.parse(tmp, (short) 0, (short) 0, new BERHandler() {
            public boolean handlePrimitive(BERSource source, byte depth, short tag, byte[] dataBuf, short dataOff, short dataLen) {
                return false;
            }
            public boolean handleBeginConstructed(BERSource source, byte depth, short tag) {
                return false;
            }
            public boolean handleFinishConstructed(BERSource source, byte depth, short tag) {
                return false;
            }
        });
    }

}
