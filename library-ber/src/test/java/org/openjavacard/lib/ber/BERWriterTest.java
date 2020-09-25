package org.openjavacard.lib.ber;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class BERWriterTest {

    byte[] zeroes;
    byte[] ones;
    byte[] count;
    byte[] tmp;

    BERWriter mWriter;

    @Before
    public void prepare() {
        zeroes = new byte[128];
        ones = new byte[128];
        count = new byte[128];
        Arrays.fill(ones, (byte)1);
        for(int i = 0; i < count.length; i++) {
            count[i] = (byte)i;
        }

        tmp = new byte[128];
        mWriter = new BERWriter((byte)32, (byte)32, (short)128);
    }

    @Test
    public void testEmpty() {
        mWriter.begin((short)128);
        short len = mWriter.finish(tmp, (short)0, (short)tmp.length);
        Assert.assertTrue(len == 0);
    }

    @Test
    public void testBufferedToplevel() {
        mWriter.begin((short)128);
        mWriter.primitiveByte(BERTag.TYPE_OCTETSTRING, (byte)12);
        mWriter.primitiveShort(BERTag.TYPE_OCTETSTRING, (short)1234);
        mWriter.primitiveBuffered(BERTag.TYPE_OCTETSTRING, ones, (short)0, (short)3);
        short len = mWriter.finish(tmp, (short)0, (short)tmp.length);
        Assert.assertTrue(len == 12);
    }

    @Test
    public void testBufferedNested() {
        mWriter.begin((short)128);
        mWriter.beginConstructed(BERTag.TYPE_SEQUENCE);
        mWriter.primitiveByte(BERTag.TYPE_OCTETSTRING, (byte)12);
        mWriter.primitiveShort(BERTag.TYPE_OCTETSTRING, (short)1234);
        mWriter.primitiveBuffered(BERTag.TYPE_OCTETSTRING, ones, (short)0, (short)3);
        mWriter.endConstructed();
        short len = mWriter.finish(tmp, (short)0, (short)tmp.length);
        Assert.assertTrue(len == 14);
    }

    @Test
    public void testBufferedComplex() {
        mWriter.begin((short)128);

        mWriter.beginConstructed(BERTag.TYPE_SEQUENCE);

        mWriter.beginConstructed(BERTag.TYPE_SEQUENCE);
        mWriter.primitiveByte(BERTag.TYPE_OCTETSTRING, (byte)12);
        mWriter.primitiveShort(BERTag.TYPE_OCTETSTRING, (short)1234);
        mWriter.primitiveBuffered(BERTag.TYPE_OCTETSTRING, ones, (short)0, (short)3);
        mWriter.endConstructed();

        mWriter.beginConstructed(BERTag.TYPE_SEQUENCE);
        mWriter.primitiveByte(BERTag.TYPE_OCTETSTRING, (byte)12);
        mWriter.primitiveShort(BERTag.TYPE_OCTETSTRING, (short)1234);
        mWriter.primitiveBuffered(BERTag.TYPE_OCTETSTRING, ones, (short)0, (short)3);
        mWriter.endConstructed();

        mWriter.endConstructed();

        mWriter.beginConstructed(BERTag.TYPE_SEQUENCE);

        mWriter.beginConstructed(BERTag.TYPE_SEQUENCE);
        mWriter.primitiveByte(BERTag.TYPE_OCTETSTRING, (byte)12);
        mWriter.primitiveShort(BERTag.TYPE_OCTETSTRING, (short)1234);
        mWriter.primitiveBuffered(BERTag.TYPE_OCTETSTRING, ones, (short)0, (short)3);
        mWriter.endConstructed();

        mWriter.beginConstructed(BERTag.TYPE_SEQUENCE);
        mWriter.primitiveByte(BERTag.TYPE_OCTETSTRING, (byte)12);
        mWriter.primitiveShort(BERTag.TYPE_OCTETSTRING, (short)1234);
        mWriter.primitiveBuffered(BERTag.TYPE_OCTETSTRING, ones, (short)0, (short)3);
        mWriter.endConstructed();

        mWriter.endConstructed();

        short len = mWriter.finish(tmp, (short)0, (short)tmp.length);
        Assert.assertTrue(len == 60);
    }

}
