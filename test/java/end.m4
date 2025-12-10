
  private void check(String mantissa, int exp10, long mantHi, long mantLo, long exponent) {
    byte[] digits = new byte[mantissa.length()];
    for (int i = 0; i < digits.length; i++) {
      digits[i] = (byte) (mantissa.charAt(i) - '0');
    }

    var quadruple = QuadrupleBuilder.parseDecimal(digits, exp10);
    assertEquals(mantHi, quadruple.mantHi);
    assertEquals(mantLo, quadruple.mantLo);
    assertEquals(exponent, Integer.toUnsignedLong(quadruple.exponent));

    // avoidDecimal128CollisionsWithDouble doesn't change any of the test results.
    quadruple.avoidDecimal128CollisionsWithDouble();
    assertEquals(mantHi, quadruple.mantHi);
    assertEquals(mantLo, quadruple.mantLo);
    assertEquals(exponent, Integer.toUnsignedLong(quadruple.exponent));
  }
}
