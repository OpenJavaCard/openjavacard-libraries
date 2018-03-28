## OpenJavaCard Libraries

This is a set of libraries for the JavaCard environment.

CAUTION: All completely experimental at this point.

[![Build Status](https://travis-ci.org/OpenJavaCard/openjavacard-libraries.svg?branch=master)](https://travis-ci.org/OpenJavaCard/openjavacard-libraries)

### Components

 * BER TLV library
   * Callback-based TLV parsing
   * Builder-based TLV writing
   * Supports 2-byte tags
 * Debug library
   * Determine exception types
   * See memory usage
   * Debug service for event recording
   * Records APDUs, exceptions, log messages
 * Fortuna PRNG library
   * Well-Known algorithm
   * Refuses operation without seed
 * RSA Cipher library
   * OAEP Encryption
