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

package org.openjavacard.svc.random;

import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Shareable;
import javacard.framework.Util;
import javacard.security.RandomData;
import org.openjavacard.lib.fortuna.FortunaRandom;

public class RandomApplet extends Applet implements ISO7816 {

    private static final byte SVC_RANDOM = (byte)0x01;

    private static final byte INS_TAKE_RANDOM = (byte) 0x84;
    private static final byte INS_GIVE_RANDOM = (byte) 0x86;

    public static void install(byte[] buf, short off, byte len) {
        short pos = off;
        // find AID
        byte  lenAID = buf[pos++];
        short offAID = pos;
        pos += lenAID;
        // find control information (ignored)
        byte  lenCI = buf[pos++];
        short offCI = pos;
        pos += lenCI;
        // find applet data
        byte  lenAD = buf[pos++];
        short offAD = pos;
        pos += lenAD;

        // instantiate and initialize the applet
        RandomApplet applet = new RandomApplet(buf, offAD, lenAD);
        // register the applet
        applet.register(buf, offAID, lenAID);
    }

    private final FortunaRandom mFortuna;
    private final RandomService mService;

    RandomApplet(byte[] buf, short off, byte len) {
        mFortuna = new FortunaRandom();
        mService = new RandomService(mFortuna);
        if(len > 0) {
            mFortuna.setSeed(buf, off, len);
        }
    }

    public RandomData getRandom() {
        return mFortuna;
    }

    public Shareable getShareableInterfaceObject(AID clientAID, byte parameter) {
        switch (parameter) {
            case SVC_RANDOM:
                return mService;
            default:
                return null;
        }
    }

    public void process(APDU apdu) throws ISOException {
        byte[] buffer = apdu.getBuffer();
        byte ins = buffer[ISO7816.OFFSET_INS];

        // handle selection of the applet
        if (selectingApplet()) {
            return;
        }

        // secure messaging is not supported
        if (apdu.isSecureMessagingCLA()) {
            ISOException.throwIt(ISO7816.SW_SECURE_MESSAGING_NOT_SUPPORTED);
        }

        // process commands to the applet
        if (!apdu.isISOInterindustryCLA()) {
            if (ins == INS_TAKE_RANDOM) {
                processTakeRandom(apdu);
            } else if (ins == INS_GIVE_RANDOM) {
                processGiveRandom(apdu);
            } else {
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
            }
        } else {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
    }

    private void processTakeRandom(APDU apdu) throws ISOException {
        byte[] buf = apdu.getBuffer();
        short len = Util.getShort(buf, OFFSET_P1);
        if(len < 0) {
            ISOException.throwIt(SW_WRONG_P1P2);
        }
        apdu.setOutgoingLength(len);
        mFortuna.generateData(buf, (short)0, len);
        apdu.sendBytes((short)0, len);
    }

    private void processGiveRandom(APDU apdu) throws ISOException {
        byte[] buf = apdu.getBuffer();
        short len = (short)(buf[OFFSET_LC] & 0xFF);
        mFortuna.setSeed(buf, OFFSET_CDATA, len);
    }

}
