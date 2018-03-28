package org.openjavacard.debug;

public class DebugApplication {

    private final DebugClient mClient;
    private final byte[] mAID;

    DebugApplication(DebugClient client, byte[] aid) {
        mClient = client;
        mAID = aid;
    }

    public DebugClient getClient() {
        return mClient;
    }

    public byte[] getAID() {
        return mAID;
    }

}
