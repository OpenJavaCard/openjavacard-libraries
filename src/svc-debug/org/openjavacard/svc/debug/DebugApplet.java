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

package org.openjavacard.svc.debug;

import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Shareable;
import javacard.framework.Util;
import org.openjavacard.lib.ber.BERTag;
import org.openjavacard.lib.ber.BERWriter;
import org.openjavacard.lib.debug.DebugProtocol;

public final class DebugApplet extends Applet implements ISO7816, DebugProtocol {

    public static void install(byte[] buf, short off, byte len) {
        // instantiate and initialize the applet
        DebugApplet applet = new DebugApplet();
        // register the applet
        applet.register();
    }

    private final BERWriter mBerWriter;
    private final DebugLog mLog;
    private final DebugServiceImpl[] mImpls;

    private short mSeq;

    private DebugApplet() {
        mBerWriter = new BERWriter((byte)32, (byte)4, JCSystem.CLEAR_ON_DESELECT);
        mLog = new DebugLog();
        mImpls = new DebugServiceImpl[16];
        mSeq = 0;
    }

    short generateSeq() {
        return mSeq++;
    }

    DebugLog getLog() {
        return mLog;
    }

    public void process(APDU apdu) throws ISOException {
        byte[] buffer = apdu.getBuffer();
        byte cla = buffer[OFFSET_CLA];
        byte ins = buffer[OFFSET_INS];

        if(selectingApplet()) {
            return;
        }

        if(apdu.isSecureMessagingCLA()) {
            ISOException.throwIt(ISO7816.SW_SECURE_MESSAGING_NOT_SUPPORTED);
        }

        if(cla == DebugProtocol.CLA_LIB_DEBUG) {
            switch (ins) {
                case INS_SVC_READ_APPLICATIONS:
                    processReadApplications(apdu);
                    break;
                case INS_SVC_READ_MESSAGES:
                    processReadMessages(apdu);
                    break;
                default:
                    ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
            }
        } else {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
    }

    private void processReadApplications(APDU apdu) {
        mBerWriter.begin((short)128);

        mBerWriter.beginConstructed(BERTag.TYPE_SEQUENCE);
        for(short i = 0; i < mImpls.length; i++) {
            DebugServiceImpl iface = mImpls[i];
            if(iface != null) {
                mBerWriter.beginConstructed(BERTag.TYPE_SEQUENCE);
                mBerWriter.buildPrimitive(BERTag.TYPE_OCTETSTRING,
                        iface.getAIDBuffer(), (short)0, iface.getAIDLength());
                mBerWriter.endConstructed();
            }
        }
        mBerWriter.endConstructed();

        mBerWriter.finishAndSend(apdu);
    }

    private void processReadMessages(APDU apdu) {
        byte[] buf = apdu.getBuffer();
        short p12 = Util.getShort(buf, ISO7816.OFFSET_P1);
        byte[] msg = mLog.getRecord(p12);
        if(msg == null) {
            ISOException.throwIt(SW_FILE_NOT_FOUND);
        } else {
            Util.arrayCopyNonAtomic(msg, (short)0, buf, (short)0, (short)msg.length);
            apdu.setOutgoingAndSend((short)0, (short)msg.length);
        }
    }

    public Shareable getShareableInterfaceObject(AID clientAID, byte parameter) {
        if(parameter == SHARE_DEBUG) {
            return findServiceImpl(clientAID, true);
        }
        return null;
    }

    private DebugServiceImpl findServiceImpl(AID clientAID, boolean create) {
        DebugServiceImpl res;
        for(short i = 0; i < mImpls.length; i++) {
            res = mImpls[i];
            if(res != null && res.isClient(clientAID)) {
                return res;
            }
        }
        if(!create) {
            return null;
        }
        for(short i = 0; i < mImpls.length; i++) {
            if(mImpls[i] == null) {
                res = new DebugServiceImpl(this, (byte)i, clientAID);
                mImpls[i] = res;
                return res;
            }
        }
        ISOException.throwIt(ISO7816.SW_FILE_FULL);
        return null;
    }

}
