package org.openjavacard.debug;

import org.openjavacard.iso.ISO7816;
import org.openjavacard.iso.SWException;
import org.openjavacard.lib.debug.DebugProtocol;
import org.openjavacard.util.APDUUtil;
import org.openjavacard.util.HexUtil;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class DebugClient {

    private final static byte[] AID_DEBUG = HexUtil.hexToBytes("a000000290fefe01");

    private final Card mCard;
    private final CardChannel mChannel;
    private final byte[] mAID;
    private boolean mConnected;

    public DebugClient(CardChannel channel, byte[] aid) {
        mCard = channel.getCard();
        mChannel = channel;
        mAID = aid;
        mConnected = false;
    }

    public DebugClient(Card card, byte[] aid) {
        this(card.getBasicChannel(), aid);
    }

    public DebugClient(CardChannel channel) {
        this(channel, AID_DEBUG);
    }

    public DebugClient(Card card) {
        this(card.getBasicChannel(), AID_DEBUG);
    }

    public Card getCard() {
        return mCard;
    }

    public CardChannel getChannel() {
        return mChannel;
    }

    public byte[] getAID() {
        return mAID;
    }

    public boolean isConnected() {
        return mConnected;
    }

    public boolean detect() {
        boolean res = false;
        try {
            connect();
            res = true;
        } catch (CardException e) {
        }
        return res;
    }

    public void connect() throws CardException {
        try {
            mCard.beginExclusive();
            performSelectByName(mAID);
            mConnected = true;
        } catch (CardException e) {
            disconnect();
            throw e;
        }
    }

    public void disconnect() {
        try {
            mConnected = false;
            mCard.endExclusive();
        } catch (CardException e) {
        }
    }

    private void performReadApplications() throws CardException {
        CommandAPDU command = APDUUtil.buildCommand(
                DebugProtocol.CLA_SVC_DEBUG,
                DebugProtocol.INS_SVC_READ_APPLICATIONS
        );
        transactAndCheck(command);
    }

    private void performReadMessage() throws CardException {
        CommandAPDU command = APDUUtil.buildCommand(
                DebugProtocol.CLA_SVC_DEBUG,
                DebugProtocol.INS_SVC_READ_MESSAGES
        );
        transactAndCheck(command);
    }

    private void performSelectByName(byte[] aid) throws CardException {
        CommandAPDU command = APDUUtil.buildCommand(
                ISO7816.CLA_ISO7816,
                ISO7816.INS_SELECT,
                ISO7816.SELECT_P1_BY_NAME,
                ISO7816.SELECT_P2_FIRST_OR_ONLY,
                aid
        );
        transactAndCheck(command);
    }

    private ResponseAPDU transactAndCheck(CommandAPDU capdu) throws CardException {
        ResponseAPDU rapdu = mChannel.transmit(capdu);
        int sw = rapdu.getSW();
        if(sw != 0x9000) {
            throw new SWException(sw);
        }
        return rapdu;
    }

}
