# QuadrupleConverter

This repository contains a multi-language library to convert decimal floating point
numbers to the 128-bit mantissa floating point format of https://github.com/m-vokhm/Quadruple

The Quadruples that result from this conversion can be used, e.g., as a super-type of [decima128](https://en.wikipedia.org/wiki/Decimal128_floating-point_format) and IEEE 64-bit binary for the purposes of comparing numbers.

This library is derived from the string to binary floating point conversion in https://github.com/m-vokhm/Quadruple

## Repository Organization

The multi-language source code is in the `src` directory, and the multi-language tests are in the `test` directory. See the README.md files in those directories for more details on how the source code for each language is generated.

There is one directory per supported languages -- currently, Java and Python. The code in these directories can be re-generated following the instructions in `src/README.md` and `test/README.md`.


