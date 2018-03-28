/*
 * openjavacard-libraries: OpenJavaCard Libraries
 * Copyright (C) 2017-2018 Ingo Albrecht, prom@berlin.ccc.de
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
 *
 */

package org.openjavacard.lib.debug;

public interface DebugProtocol {

    byte SHARE_DEBUG = (byte)0xF0;

    byte INS_SVC_READ_APPLICATIONS = (byte)0x00;
    byte INS_SVC_READ_MESSAGES = (byte)0x02;

    /** CLA of the library stub */
    byte CLA_LIB_DEBUG = (byte)0x80;
    /** INS of the library stub */
    byte INS_LIB_DEBUG = (byte)0xF0;
    /** Library command: get library stub status */
    byte CMD_LIB_STATUS = (byte)0x00;
    /** Library command: attach to service */
    byte CMD_LIB_ATTACH = (byte)0x01;
    /** Library command: detach from service */
    byte CMD_LIB_DETACH = (byte)0x02;
    /** Library command: enable debugging */
    byte CMD_LIB_ENABLE = (byte)0x03;
    /** Library command: disable debugging */
    byte CMD_LIB_DISABLE = (byte)0x04;
    /** Library command: get memory usage */
    byte CMD_LIB_MEM_USAGE = (byte)0x10;
    /** Library command: request garbage collection */
    byte CMD_LIB_MEM_COLLECT = (byte)0x12;

    short MSG_APDU_COMMAND  = (short)0x01;
    short MSG_APDU_RESPONSE = (short)0x02;
    short MSG_EXCEPTION     = (short)0x03;
    short MSG_MEMORY        = (short)0x04;
    short MSG_MESSAGE       = (short)0x05;

    // Exceptions in java.lang
    short EXC_THROWABLE                     = (short)0x00;
    short EXC_EXCEPTION                     = (short)0x01;
    short EXC_RUNTIME_EXCEPTION             = (short)0x02;
    short EXC_ARITHMETIC_EXCEPTION          = (short)0x03;
    short EXC_ARRAY_STORE_EXCEPTION         = (short)0x04;
    short EXC_CLASS_CAST_EXCEPTION          = (short)0x05;
    short EXC_NEGATIVE_ARRAY_SIZE_EXCEPTION = (short)0x06;
    short EXC_NULL_POINTER_EXCEPTION        = (short)0x07;
    short EXC_SECURITY_EXCEPTION            = (short)0x08;
    short EXC_INDEX_OOB_EXCEPTION           = (short)0x09;
    short EXC_ARRAY_INDEX_OOB_EXCEPTION     = (short)0x0A;
    // Exceptions in java.io
    short EXC_IO_EXCEPTION           = (short)0x10;
    // Exceptions in java.rmi
    short EXC_REMOTE_EXCEPTION       = (short)0x20;
    // Exceptions in javacard.framework
    short EXC_CARD_EXCEPTION         = (short)0x30;
    short EXC_CARD_RUNTIME_EXCEPTION = (short)0x31;
    short EXC_APDU_EXCEPTION         = (short)0x32;
    short EXC_ISO_EXCEPTION          = (short)0x33;
    short EXC_PIN_EXCEPTION          = (short)0x34;
    short EXC_SYSTEM_EXCEPTION       = (short)0x35;
    short EXC_TRANSACTION_EXCEPTION  = (short)0x36;
    short EXC_USER_EXCEPTION         = (short)0x37;
    // Exceptions in javacard.framework.service
    short EXC_SERVICE_EXCEPTION      = (short)0x40;
    // Exceptions in javacard.security
    short EXC_CRYPTO_EXCEPTION       = (short)0x50;
    // Exceptions in javacard.biometry
    short EXC_BIO_EXCEPTION          = (short)0x60;
    // Exceptions in javacard.external
    short EXC_EXTERNAL_EXCEPTION     = (short)0x70;
    // Exceptions in javacardx.framework.string
    short EXC_STRING_EXCEPTION       = (short)0x80;
    // Exceptions in javacardx.framework.tlv
    short EXC_TLV_EXCEPTION          = (short)0x90;
    // Exceptions in javacardx.framework.util
    short EXC_UTIL_EXCEPTION         = (short)0xA0;

}
