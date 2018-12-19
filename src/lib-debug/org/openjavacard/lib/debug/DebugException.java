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

import javacard.framework.APDUException;
import javacard.framework.CardException;
import javacard.framework.CardRuntimeException;
import javacard.framework.ISOException;
import javacard.framework.PINException;
import javacard.framework.SystemException;
import javacard.framework.TransactionException;
import javacard.framework.UserException;
import javacard.security.CryptoException;

public class DebugException implements DebugProtocol {

    /**
     * Decode an exception and determine its type
     * @param t to decode
     * @return type of the exception
     */
    public static short exceptionType(Throwable t) {
        if(t instanceof UserException) {
            return EXC_USER_EXCEPTION;
        }
        if(t instanceof TransactionException) {
            return EXC_TRANSACTION_EXCEPTION;
        }
        if(t instanceof SystemException) {
            return EXC_SYSTEM_EXCEPTION;
        }
        if(t instanceof PINException) {
            return EXC_PIN_EXCEPTION;
        }
        if(t instanceof ISOException) {
            return EXC_ISO_EXCEPTION;
        }
        if(t instanceof CryptoException) {
            return EXC_CRYPTO_EXCEPTION;
        }
        if(t instanceof APDUException) {
            return EXC_APDU_EXCEPTION;
        }
        if(t instanceof CardRuntimeException) {
            return EXC_CARD_RUNTIME_EXCEPTION;
        }
        if(t instanceof CardException) {
            return EXC_CARD_EXCEPTION;
        }
        if(t instanceof ArrayIndexOutOfBoundsException) {
            return EXC_ARRAY_INDEX_OOB_EXCEPTION;
        }
        if(t instanceof IndexOutOfBoundsException) {
            return EXC_INDEX_OOB_EXCEPTION;
        }
        if(t instanceof SecurityException) {
            return EXC_SECURITY_EXCEPTION;
        }
        if(t instanceof NullPointerException) {
            return EXC_NULL_POINTER_EXCEPTION;
        }
        if(t instanceof NegativeArraySizeException) {
            return EXC_NEGATIVE_ARRAY_SIZE_EXCEPTION;
        }
        if(t instanceof ClassCastException) {
            return EXC_CLASS_CAST_EXCEPTION;
        }
        if(t instanceof ArrayStoreException) {
            return EXC_ARRAY_STORE_EXCEPTION;
        }
        if(t instanceof ArithmeticException) {
            return EXC_ARITHMETIC_EXCEPTION;
        }
        if(t instanceof RuntimeException) {
            return EXC_RUNTIME_EXCEPTION;
        }
        if(t instanceof Exception) {
            return EXC_EXCEPTION;
        }
        return EXC_THROWABLE;
    }

    /**
     * Determine the reason code for an exception
     * @param t to check
     * @return the reason code, 0 if undetermined
     */
    public static short exceptionCode(Throwable t) {
        if(t instanceof CardException) {
            return ((CardException)t).getReason();
        }
        if(t instanceof CardRuntimeException) {
            return ((CardRuntimeException)t).getReason();
        }
        return 0;
    }

}
