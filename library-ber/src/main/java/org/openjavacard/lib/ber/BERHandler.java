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

package org.openjavacard.lib.ber;

/**
 * Interface for BER-TLV processing handlers
 */
public interface BERHandler {

    /**
     * Handle a primitive tag
     * @param source feeding the tag
     * @param depth of occurrence
     * @param tag that occurred
     * @param dataBuf containing tag data
     * @param dataOff of data in dataBuf
     * @param dataLen of data in dataBuf
     * @return true if accepted
     */
    boolean handlePrimitive(BERSource source, byte depth, short tag,
                            byte[] dataBuf, short dataOff, short dataLen);

    /**
     * Handle start of a constructed tag
     * @param source feeding the tag
     * @param depth of occurrence
     * @param tag that occurred
     * @return true if accepted
     */
    boolean handleBeginConstructed(BERSource source, byte depth, short tag);

    /**
     * Handle end of a constructed tag
     * @param source feeding the tag
     * @param depth of occurrence
     * @param tag that occurred
     * @return true if accepted
     */
    boolean handleFinishConstructed(BERSource source, byte depth, short tag);

}
