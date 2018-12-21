/*
 * openjavacard-libraries: Class libraries for JavaCard
 * Copyright (C) 2018 Ingo Albrecht <copyright@promovicz.org>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.openjavacard.app.demo;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;
import org.openjavacard.lib.ber.BERReader;
import org.openjavacard.lib.ber.BERTag;
import org.openjavacard.lib.ber.BERWriter;
import org.openjavacard.lib.debug.Debug;
import org.openjavacard.lib.fortuna.FortunaRandom;
import org.openjavacard.lib.fortuna.LongNum;
import org.openjavacard.lib.string.StringStatistics;

/**
 *
 */
public final class DemoApplet extends Applet implements ISO7816 {

    private static final byte CLA_PROPRIETARY = (byte)0x80;

    private static final byte INS_FORTUNA_RESET = (byte)0x00;
    private static final byte INS_FORTUNA_SEED = (byte)0x02;
    private static final byte INS_FORTUNA_GENERATE = (byte)0x04;

    private static final byte INS_LONGNUM_GET = (byte)0x10;
    private static final byte INS_LONGNUM_SET = (byte)0x12;
    private static final byte INS_LONGNUM_ADD = (byte)0x14;
    private static final byte INS_LONGNUM_SUB = (byte)0x16;

    private static final byte INS_BER_PARSE = (byte)0x20;
    private static final byte INS_BER_WRITE = (byte)0x22;

    private static final byte INS_DEBUG_MEMORY  = (byte)0x30;
    private static final byte INS_DEBUG_MESSAGE = (byte)0x32;

    private static final byte INS_STRING_STATS  = (byte)0x40;

    /**
     * Installation method for the applet
     */
    public static void install(byte[] buf, short off, byte len) {
        short pos = off;
        // find AID (used for install)
        byte  lenAID = buf[pos++];
        short offAID = pos;
        pos += lenAID;
        // ignore the rest (control data and install data)

        // instantiate and initialize the applet
        DemoApplet applet = new DemoApplet();
        // register the applet
        applet.register(buf, offAID, lenAID);
    }

    private final Debug mDebug;

    private final TempBuffer mBuffer;

    private byte[] mTmp;

    private FortunaRandom mFortuna;

    private final LongNum mLongNum;

    private final BERReader mReader;
    private final BERWriter mWriter;
    private final ParseHandler mParseHandler;

    private final StringStatistics mStringStats;

    /**
     * Main constructor
     */
    private DemoApplet() {
        mDebug = Debug.getInstance(this);
        mBuffer = new TempBuffer((short)128, JCSystem.CLEAR_ON_DESELECT);
        mTmp = JCSystem.makeTransientByteArray((short)32, JCSystem.CLEAR_ON_DESELECT);
        mFortuna = new FortunaRandom();
        mLongNum = new LongNum((byte)8);
        mReader = new BERReader((byte)4, JCSystem.CLEAR_ON_DESELECT);
        mWriter = new BERWriter((byte)32, (byte)4, JCSystem.CLEAR_ON_DESELECT);
        mParseHandler = new ParseHandler();
        mStringStats = new StringStatistics();
    }

    /**
     * Applet select handler
     * @return true if select okay
     */
    public final boolean select() {
        return true;
    }

    /**
     * Applet deselect handler
     */
    public final void deselect() {
    }

    /**
     * Process an APDU
     * @param apdu to be processed
     * @throws ISOException on error
     */
    public final void process(APDU apdu) throws ISOException {
        byte[] buffer = apdu.getBuffer();
        byte cla = buffer[OFFSET_CLA];
        byte ins = buffer[OFFSET_INS];

        // give the debugger a chance
        if(mDebug.process(apdu)) {
            return;
        }

        // handle selection of the applet
        if(selectingApplet()) {
            return;
        }

        // secure messaging is not supported
        if(apdu.isSecureMessagingCLA()) {
            ISOException.throwIt(SW_SECURE_MESSAGING_NOT_SUPPORTED);
        }

        mBuffer.clear();

        // process commands to the applet
        if(cla == CLA_PROPRIETARY) {
            switch (ins) {
                case INS_FORTUNA_RESET:
                    processFortunaReset(apdu);
                    break;
                case INS_FORTUNA_SEED:
                    processFortunaSeed(apdu);
                    break;
                case INS_FORTUNA_GENERATE:
                    processFortunaGenerate(apdu);
                    break;
                case INS_LONGNUM_GET:
                    processLongNumGet(apdu);
                    break;
                case INS_LONGNUM_SET:
                    processLongNumSet(apdu);
                    break;
                case INS_LONGNUM_ADD:
                    processLongNumAdd(apdu);
                    break;
                case INS_LONGNUM_SUB:
                    processLongNumSub(apdu);
                    break;
                case INS_BER_PARSE:
                    processBerParse(apdu);
                    break;
                case INS_BER_WRITE:
                    processBerWrite(apdu);
                    break;
                case INS_DEBUG_MEMORY:
                    processDebugMemory(apdu);
                    break;
                case INS_DEBUG_MESSAGE:
                    processDebugMessage(apdu);
                    break;
                case INS_STRING_STATS:
                    processStringStats(apdu);
                    break;
                default:
                    ISOException.throwIt(SW_INS_NOT_SUPPORTED);
            }
        } else {
            ISOException.throwIt(SW_CLA_NOT_SUPPORTED);
        }
    }

    private final void processFortunaReset(APDU apdu) {
        mFortuna.reset();
    }

    private final void processFortunaSeed(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte length = buffer[OFFSET_LC];
        mFortuna.setSeed(buffer, (short)0, (short)length);
    }

    private final void processFortunaGenerate(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short request = Util.getShort(buffer, OFFSET_P1);
        mFortuna.generateData(buffer, (short)0, request);
        apdu.setOutgoingAndSend((short)0, request);
    }

    private final void processLongNumGet(APDU apdu) {
        sendLongNum(apdu);
    }

    private final void processLongNumSet(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte length = buffer[OFFSET_LC];
        mLongNum.set(buffer, OFFSET_CDATA, length);
        sendLongNum(apdu);
    }
    private final void processLongNumAdd(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte p1 = buffer[OFFSET_P1];
        mLongNum.add(p1);
        sendLongNum(apdu);
    }
    private final void processLongNumSub(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte p1 = buffer[OFFSET_P1];
        mLongNum.sub(p1);
        sendLongNum(apdu);
    }
    private final void sendLongNum(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short length = mLongNum.getLength();
        mLongNum.get(buffer, (short)0, length);
        apdu.setOutgoingAndSend((short)0, length);
    }

    private final void processDebugMemory(APDU apdu) {
        mDebug.logMemory();
    }

    private final void processDebugMessage(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short code = Util.getShort(buffer, ISO7816.OFFSET_P1);
        short len = apdu.setIncomingAndReceive();
        mDebug.logMessage(code, buffer, ISO7816.OFFSET_CDATA, (byte)len);
    }

    private final void processStringStats(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short len = apdu.setIncomingAndReceive();
        mStringStats.reset();
        mStringStats.update(buffer, ISO7816.OFFSET_CDATA, (byte)len);
    }

    private final void processBerParse(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte len = buffer[OFFSET_LC];
        mReader.parse(buffer, OFFSET_CDATA, len, mParseHandler);
        Util.arrayCopyNonAtomic(mBuffer.getBuffer(), (short)0, buffer, (short)0, mBuffer.getLength());
        apdu.setOutgoingAndSend((short)0, mBuffer.getLength());
    }

    private final void processBerWrite(APDU apdu) {
        byte[] buffer = apdu.getBuffer();

        Util.arrayCopyNonAtomic(buffer, (short)0, mTmp, (short)0, (short)32);

        mWriter.begin((short)128);
        mWriter.beginConstructed(BERTag.TYPE_SEQUENCE);

        mWriter.buildPrimitive(BERTag.TYPE_OCTETSTRING, mTmp, (short)0, (short)4);
        mWriter.buildPrimitive(BERTag.TYPE_OCTETSTRING, mTmp, (short)0, (short)4);

        mWriter.beginConstructed(BERTag.TYPE_SEQUENCE);
        mWriter.buildPrimitive(BERTag.TYPE_OCTETSTRING, mTmp, (short)0, (short)4);
        mWriter.buildPrimitive(BERTag.TYPE_OCTETSTRING, mTmp, (short)0, (short)4);
        mWriter.endConstructed();

        mWriter.beginConstructed(BERTag.TYPE_SEQUENCE);
        mWriter.buildPrimitive(BERTag.TYPE_OCTETSTRING, mTmp, (short)0, (short)4);
        mWriter.buildPrimitive(BERTag.TYPE_OCTETSTRING, mTmp, (short)0, (short)4);
        mWriter.endConstructed();

        mWriter.endConstructed();

        short len = mWriter.finish(buffer, (short)0, (short)128);
        apdu.setOutgoingAndSend((short)0, len);
    }

    private final class ParseHandler implements BERReader.Handler {
        public boolean handle(BERReader reader, byte depth, short tag,
                              byte[] dataBuf, short dataOff, short dataLen) {
            mBuffer.put((byte)0x53);
            mBuffer.put(depth);
            mBuffer.put(tag);
            mBuffer.put(dataLen);
            return true;
        }
    }

}
