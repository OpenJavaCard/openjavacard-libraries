package org.openjavacard.lib.cvmpin;

import javacard.framework.PIN;
import javacard.framework.Shareable;
import org.globalplatform.CVM;
import org.globalplatform.GPSystem;

/**
 * Wraps a CVM for use as an OwnerPIN
 */
public class CVMPIN implements PIN, Shareable {

    /** The CVM that we wrap */
    private CVM mCVM;

    /**
     * Get a CVMPIN representing a system CVM
     *
     * @param cvmIdentifier to look for
     * @return resulting wrapper
     */
    public static CVMPIN get(byte cvmIdentifier) {
        CVM cvm = GPSystem.getCVM(cvmIdentifier);
        return new CVMPIN(cvm);
    }

    /**
     * Construct a CVM wrapper
     *
     * @param cvm to wrap
     */
    public CVMPIN(CVM cvm) {
        mCVM = cvm;
    }

    public byte getTriesRemaining() {
        return mCVM.getTriesRemaining();
    }

    public boolean isValidated() {
        return mCVM.isVerified();
    }

    public boolean check(byte[] buf, short off, byte len) throws ArrayIndexOutOfBoundsException, NullPointerException {
        short res = mCVM.verify(buf, off, len, CVM.FORMAT_ASCII);
        return res == CVM.CVM_SUCCESS;
    }

    public void reset() {
        mCVM.resetAndUnblockState();
    }

}
