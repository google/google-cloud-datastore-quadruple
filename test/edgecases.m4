test_case(edge)
    # Infinity.
  quadruple_test("1", 1_000_000_000, 0, 0, 0xFFFFFFFF)
  quadruple_test("9", 646456993, 0, 0, 0xFFFFFFFF)
    # Subnormal - rounded to 0.
  quadruple_test("1", -646457000, 0, 0, 0)
    # Underflow - rounded to 0.
  quadruple_test("1", -1_000_000_000, 0, 0, 0)
test_case_end()
