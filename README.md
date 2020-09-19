## OpenJavaCard Libraries

This is a set of reusable libraries for the JavaCard environment.

CAUTION: All completely experimental at this point.

[![Build Status](https://travis-ci.org/OpenJavaCard/openjavacard-libraries.svg?branch=master)](https://travis-ci.org/OpenJavaCard/openjavacard-libraries)

### Project

For more information about this overall project, see our [website](https://openjavacard.org/).

You can follow us on [Twitter](https://twitter.com/openjavacardorg) and chat with us on [Gitter](https://gitter.im/openjavacard/general).

### Overview

| Name                  | Description                         | Status       |
| --------------------- |------------------------------------ | ------------ |
| library-auth          | Authentication framework            | Experiment   |
| library-ber           | BER-TLV processing                  | Usable       |
| library-codec         | Codec library                       | Experiment   |
| library-ctlv          | Compact-TLV utilities               | Experiment   |
| library-cvmpin        | CVM PIN wrapper                     | Experiment   |
| library-debug         | Debug utilities                     | Experiment   |
| library-fortuna       | Fortuna PRNG                        | Usable       |
| library-isofs         | ISO7816 filesystem                  | Experiment   |
| library-oath          | OATH authentication                 | Experiment   |
| library-password      | Password authentication             | Experiment   |
| library-rsa           | RSA supplementary ciphers           | Experiment   |
| library-string        | String processing                   | Experiment   |
| library-tlv           | TLV object representation           | Experiment   |
| --------------------- |------------------------------------ | ------------ |

### Components

 * BER-TLV library
   * Callback-based TLV parsing
   * Builder-based TLV writing
   * Supports 2-byte tags
   * Flexible interface
   * Allocation-free design
 * Compact-TLV library
   * Minimalist implementation
   * Can construct historical bytes for ATR
 * Fortuna PRNG implementation
   * Well-Known algorithm
   * Refuses operation without seed
 * RSA library
   * OAEP encryption
   * MGF1 masking
 * String library
   * Character type functions
   * String statistics utility
 * Password library
   * Salt and hash

### Hacks And Intentions

 * ISO filesystem library
   * lots of code exists
   * nothing usable yet
 * Authentication library
   * some code exists
   * modularize authentication
   * support key derivation
 * TLV object representation
 * OATH cipher suite
 * Blinding ciphers (pseudonymous buddy matching)
 * CBOR implementation
   * we would love to have one
 * Codec library
   * hex, base64...
