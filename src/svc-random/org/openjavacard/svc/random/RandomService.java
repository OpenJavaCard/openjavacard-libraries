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

import javacard.framework.Shareable;
import javacard.security.CryptoException;
import javacard.security.RandomData;

public class RandomService extends RandomData implements Shareable {

    private RandomData   mRandom;

    RandomService(RandomData random) {
        mRandom = random;
    }

    public void generateData(byte[] buf, short off, short len) throws CryptoException {
        mRandom.generateData(buf, off, len);
    }

    public void setSeed(byte[] buf, short off, short len) {
        mRandom.setSeed(buf, off, len);
    }

}
