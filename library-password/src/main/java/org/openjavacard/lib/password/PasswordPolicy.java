package org.openjavacard.lib.password;

public interface PasswordPolicy {

    boolean validate(byte[] buf, short off, short len);

}
