package org.openjavacard.lib.oath;

import javacard.security.HMACKey;
import javacard.security.KeyBuilder;
import javacard.security.Signature;

public class OATHCipher {

    public static final byte ALG_SHA1 = Signature.ALG_HMAC_SHA1;
    public static final byte ALG_SHA256 = Signature.ALG_HMAC_SHA_256;
    public static final byte ALG_SHA512 = Signature.ALG_HMAC_SHA_512;

    private Signature mSigner;

    private HMACKey mKey;

    public OATHCipher(byte mode) {
        mSigner = Signature.getInstance(mode, false);
        mKey = (HMACKey)KeyBuilder.buildKey(KeyBuilder.TYPE_HMAC, mSigner.getLength(), false);
    }

}
