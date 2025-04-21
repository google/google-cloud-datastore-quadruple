# Multi-Language Decimal to Binary Floating-Point Conversion Tests

Multi-language tests for QuadrupleBuilder, based on exemplars of expected
input/output pairs found in the `testcases.m4` and `edgecases.m4` files.

As with the source code, each supported language defines a set of macros to
convert the exemplars to language-specific unit tests. The `Makefile` takes
these macros and the exemplars to generate the actual source code for the tests
in the top-level, per-language directories.

Adding a new language L requires:

- writing `L/start.m4` and `L/end.m4` files that define the macros and unit test
  header and footers
- updating `Makefile` to add rules to generate the actual unit test in L's
  top-level directory

The `start.m4` file should define the following macros:

- `test_case(name)` - start the definition of a unit test called `name`
- `test_case_end()` - end the definition of a unit test
- `quadruple_test(mantissa, exp10, mantHi, mantLo, exponent)` - confirm that
  parsing the decimal digits in string `mantissa` with decimal exponent `exp10`
  gives a Quadruple with 128-bit mantissa `mantHi`, `mantLo` (both 64-bit
  unsigned integers) and biased binary exponent `exponent`


