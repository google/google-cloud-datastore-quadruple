
  private void check(String mantissa, int exp10, long mantHi, long mantLo, long exponent) {
    byte[] digits = new byte[mantissa.length()];
    for (int i = 0; i < digits.length; i++) {
      digits[i] = (byte) (mantissa.charAt(i) - '0');
    }
    check(false, digits, exp10, mantHi, mantLo, exponent);
    check(true, digits, exp10, mantHi, mantLo, exponent);
  }

  private void check(
      boolean negative, byte[] mantissa, int exp10, long mantHi, long mantLo, long exponent) {
    var quadruple = QuadrupleBuilder.parseDecimal(negative, mantissa, exp10);
    assertEquals(negative, quadruple.negative);
    assertEquals(mantHi, quadruple.mantHi);
    assertEquals(mantLo, quadruple.mantLo);
    assertEquals(exponent, Integer.toUnsignedLong(quadruple.exponent));
  }
}
