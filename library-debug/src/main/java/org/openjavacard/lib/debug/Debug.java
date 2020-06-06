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

package org.openjavacard.lib.debug;

import javacard.framework.AID;
import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.Util;

public final class Debug implements DebugProtocol {

    public static Debug getInstance(Applet applet) {
        return new Debug(applet);
    }

    /** Reference to containing applet */
    private final Applet mApplet;

    /** Reference to service implementation (null if detached) */
    private DebugService mService;

    /** Binary representation of service AID */
    private final byte[] mServiceAID;

    /** Internal flags (clear-on-reset) */
    private final boolean[] mFlags;
    private final static byte FLAG_ACTIVE = 0;
    private final static byte NUM_FLAGS = 1;

    /** True if debugging is enabled (persistent) */
    private boolean mEnabled;
    /** Type of last exception (persistent) */
    private short mExceptionType;
    /** Code of last exception (persistent */
    private short mExceptionCode;

    private Debug(Applet applet) {
        // initialize fields
        mApplet = applet;
        mService = null;
        mServiceAID = newServiceAID();
        mFlags = JCSystem.makeTransientBooleanArray(NUM_FLAGS, JCSystem.CLEAR_ON_RESET);
        // we start disabled
        mEnabled = false;
        // no exception yet
        mExceptionType = 0;
        mExceptionCode = 0;
    }

    /** @return true if running in debugger */
    public boolean isActive() {
        return mFlags[FLAG_ACTIVE];
    }

    /** @return true if debugging is enabled */
    public boolean isEnabled() {
        return mEnabled;
    }

    /** @return true if the debug service is attached */
    public boolean isAttached() {
        return mService != null;
    }

    /** Enable debugging */
    public void enable() {
        mEnabled = true;
    }

    /** Disable debugging */
    public void disable() {
        mEnabled = false;
    }

    /**
     * Return the debug service AID
     *
     * @return AID as byte array
     */
    private byte[] newServiceAID() {
        // D276000177   - signal interrupt
        //   10         - OpenJavaCard
        //     011020   - OpenJavaCard Libraries (Debug service)
        return new byte[] {
                (byte)0xD2, (byte)0x76, (byte)0x00, (byte)0x01, (byte)0x77,
                (byte)0x10, (byte)0x01, (byte)0x10, (byte)0x20,
        };
    }

    /**
     * Attach to debug service
     */
    public void attach() {
        AID aid = JCSystem.lookupAID(mServiceAID, (short)0, (byte) mServiceAID.length);
        if(aid != null) {
            Object obj = JCSystem.getAppletShareableInterfaceObject(aid, SHARE_DEBUG);
            if(obj instanceof DebugService) {
                mService = (DebugService)obj;
            }
        }
    }

    /**
     * Detach from debug service
     */
    public void detach() {
        mService = null;
    }

    /**
     * Log memory usage in debug log
     */
    public void logMemory() {
        if(isAttached()) {
            short persistent = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
            short clearReset = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
            short clearDesel = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
            mService.logMemory(persistent, clearReset, clearDesel);
        }
    }

    /**
     * Log message in debug log
     *
     * @param code of message
     * @param buf containing additional data
     * @param off of data
     * @param len of data
     */
    public void logMessage(short code, byte[] buf, short off, byte len) {
        if(isAttached()) {
            mService.logMessage(code, buf, off, len);
        }
    }

    /**
     * Log message in debug log
     *
     * @param code of message
     * @param buf containing additional data
     */
    public void logMessage(short code, byte[] buf) {
        logMessage(code, null, (short)0, (byte)buf.length);
    }

    /**
     * Log message in debug log
     *
     * @param code of message
     */
    public void logMessage(short code) {
        logMessage(code, null, (short)0, (byte)0);
    }

    public boolean process(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte cla = buffer[ISO7816.OFFSET_CLA];
        byte ins = buffer[ISO7816.OFFSET_INS];

        // prevent recursion
        if(mFlags[FLAG_ACTIVE]) {
            return false;
        }

        // handle debugger commands
        if(cla == CLA_LIB_DEBUG && ins == INS_LIB_DEBUG) {
            processDbgCommand(apdu);
            return true;
        }

        // bail out if not enabled
        if(!mEnabled) {
            return false;
        }

        // reset exception state
        mExceptionType = 0;
        mExceptionCode = 0;

        // determine if we are attached
        boolean attached = isAttached();

        // log the C-APDU
        if(attached) {
            mService.logAPDUCommand(APDU.getProtocol(), apdu);
        }

        // call user handler with exceptions caught
        Throwable caught = null;
        try {
            mFlags[FLAG_ACTIVE] = true;
            mApplet.process(apdu);
        } catch (RuntimeException ex) {
            caught = ex;
        } catch (Exception ex) {
            caught = ex;
        } catch (Throwable ex) {
            caught = ex;
        } finally {
            mFlags[FLAG_ACTIVE] = false;
        }

        // log the R-APDU
        if(attached) {
            mService.logAPDUResponse(apdu);
        }

        // handle exceptions
        if(caught != null) {
            // remember exception information
            mExceptionType = DebugException.exceptionType(caught);
            mExceptionCode = DebugException.exceptionCode(caught);
            // log the exception
            if(attached) {
                mService.logException(mExceptionType, mExceptionCode);
            }
            // rethrow or raise generic exception
            if(caught instanceof RuntimeException) {
                throw (RuntimeException)caught;
            } else {
                ISOException.throwIt(ISO7816.SW_UNKNOWN);
            }
        }

        // we have handled this APDU
        return true;
    }

    public boolean isDbgCommand(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte cla = buffer[ISO7816.OFFSET_CLA];
        byte ins = buffer[ISO7816.OFFSET_INS];
        return cla == CLA_LIB_DEBUG && ins == INS_LIB_DEBUG;
    }

    public void processDbgCommand(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        byte cmd = buffer[ISO7816.OFFSET_P1];
        switch(cmd) {
            case CMD_LIB_STATUS:
                processDbgStatus(apdu);
                break;
            case CMD_LIB_ATTACH:
                processDbgAttach(apdu);
                break;
            case CMD_LIB_DETACH:
                processDbgDetach(apdu);
                break;
            case CMD_LIB_ENABLE:
                processDbgEnable(apdu);
                break;
            case CMD_LIB_DISABLE:
                processDbgDisable(apdu);
                break;
            case CMD_LIB_MEM_USAGE:
                processMemStatus(apdu);
                break;
            case CMD_LIB_MEM_COLLECT:
                processMemCollect(apdu);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }
    }

    private void processDbgStatus(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short val, off = 0;
        buffer[off++] = (byte)(isAttached() ? 1 : 0);
        buffer[off++] = (byte)(isEnabled() ? 1 : 0);
        buffer[off++] = (byte)(isActive() ? 1 : 0);
        val = mExceptionType;
        off = Util.setShort(buffer, off, val);;
        val = mExceptionCode;
        off = Util.setShort(buffer, off, val);;
        apdu.setOutgoingAndSend((short)0, off);
    }

    private void processDbgAttach(APDU apdu) {
        attach();
        enable();
        processDbgStatus(apdu);
    }

    private void processDbgDetach(APDU apdu) {
        disable();
        detach();
        processDbgStatus(apdu);
    }

    private void processDbgEnable(APDU apdu) {
        enable();
        processDbgStatus(apdu);
    }

    private void processDbgDisable(APDU apdu) {
        disable();
        processDbgStatus(apdu);
    }

    private void processMemStatus(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short v, off = 0;
        v = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);
        Util.setShort(buffer, off, v); off += 2;
        v = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);
        Util.setShort(buffer, off, v); off += 2;
        v = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);
        Util.setShort(buffer, off, v); off += 2;
        v = JCSystem.getUnusedCommitCapacity();
        Util.setShort(buffer, off, v); off += 2;
        apdu.setOutgoingAndSend((short)0, off);
    }

    private void processMemCollect(APDU apdu) {
        if(!JCSystem.isObjectDeletionSupported()) {
            ISOException.throwIt(ISO7816.SW_FUNC_NOT_SUPPORTED);
        }
        JCSystem.requestObjectDeletion();
        processMemStatus(apdu);
    }

}
