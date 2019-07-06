## OpenJavaCard Libraries

This is a set of libraries for the JavaCard environment.

CAUTION: All completely experimental at this point.

[![Build Status](https://travis-ci.org/OpenJavaCard/openjavacard-libraries.svg?branch=master)](https://travis-ci.org/OpenJavaCard/openjavacard-libraries)

### Project

For more information about this overall project, see our [website](https://openjavacard.org/).

You can follow us on [Twitter](https://twitter.com/openjavacardorg) and chat with us on [Gitter](https://gitter.com/openjavacard).

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
   * Needs polish and documentation
 * Fortuna PRNG library
   * Well-Known algorithm
   * Refuses operation without seed
 * RSA Cipher library
   * OAEP Encryption

### Hacks And Intentions

 * ISO filesystem library
   * some code exists
   * nothing usable yet
   * could use BER library
 * Authentication library
   * some code exists
   * modularize authentication
   * support key derivation
 * CBOR implementation
   * we would love to have one
 * String library
   * currently does basic character classes
