package org.openjavacard.lib.string;

public class StringWriter {

    private final short mMaxChunks;

    private final short[] mVars;
    /** Number of transient variables */
    private static final byte NUM_VAR = 3;
    /** Variable: maximum allowed length */
    private static final byte VAR_MAX_LENGTH = 0;
    /** Variable: current running length */
    private static final byte VAR_LENGTH     = 1;
    /** Variable: current index */
    private static final byte VAR_INDEX      = 2;

    private final Object[] mBufStk;
    private final short[]  mOffStk;
    private final short[]  mLenStk;

    public StringWriter(short maxChunks) {
        mMaxChunks = maxChunks;
        mVars = new short[NUM_VAR];
        mBufStk = new Object[maxChunks];
        mOffStk = new short[maxChunks];
        mLenStk = new short[maxChunks];
    }

    void begin(short maxLength) {
        mVars[VAR_MAX_LENGTH] = maxLength;
        mVars[VAR_LENGTH] = 0;
        mVars[VAR_INDEX] = 0;
    }

    short finish(byte[] buf, short off, short len) {
        return 0;
    }

}
