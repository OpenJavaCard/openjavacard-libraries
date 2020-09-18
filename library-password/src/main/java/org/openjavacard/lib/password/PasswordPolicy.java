package org.openjavacard.lib.password;

/**
 * Interface for password policy objects
 */
public interface PasswordPolicy {

    /**
     * Validate the given password for compliance
     * <p/>
     *
     * @param buf containing password
     * @param off of password
     * @param len of password
     * @return true if compliant
     */
    boolean validate(byte[] buf, short off, short len);

}
