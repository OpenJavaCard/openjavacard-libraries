## OpenJavaCard Libraries

This is a set of libraries for the JavaCard environment.

CAUTION: All completely experimental at this point.

[![Build Status](https://travis-ci.org/OpenJavaCard/openjavacard-libraries.svg?branch=master)](https://travis-ci.org/OpenJavaCard/openjavacard-libraries)

### Project

For more information about this overall project, see our [website](https://openjavacard.org/).

You can follow us on [Twitter](https://twitter.com/openjavacardorg) and chat with us on [Gitter](https://gitter.im/openjavacard/general).

### Components

 * BER-TLV library
   * Callback-based TLV parsing
   * Builder-based TLV writing
   * Supports 2-byte tags
   * Flexible interface
   * Compact design
 * Compact-TLV library
   * Minimalist implementation
   * Can construct historical bytes for ATR
 * GlobalPlatform CVM utilities
   * Wraps a CVM as an OwnerPIN
 * Fortuna PRNG library
   * Well-Known algorithm
   * Refuses operation without seed
 * RSA library
   * OAEP encryption
   * MGF1 masking
 * Password library
   * Password hashing for authentication
 * String library
   * Character type functions
   * String statistics utility
 * Debug library
   * Determine exception types
   * See memory usage
   * Debug service for event recording
   * Records APDUs, exceptions, log messages
   * Needs polish and documentation

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
