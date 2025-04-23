//  Copyright 2025 Google LLC
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      https://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package com.google.cloud.datastore.core.quadruple;

/**
 * A 128-bit binary floating point number which supports comparisons and creation from long, double
 * and string.
 *
 * @param negative the sign of the number.
 * @param biasedExponent the biased (by 0x7FFF_FFFF) binary exponent.
 * @param mantHi the high 64 bits of the mantissa (leading 1 omitted).
 * @param mantLo the low 64 bits of the mantissa.
 */
public record Quadruple(boolean negative, int biasedExponent, long mantHi, long mantLo)
    implements Comparable<Quadruple> {

  public static final Quadruple POSITIVE_ZERO = new Quadruple(false, 0, 0, 0);
  public static final Quadruple NEGATIVE_ZERO = new Quadruple(true, 0, 0, 0);
  public static final Quadruple NaN =
      new Quadruple(false, QuadrupleBuilder.EXPONENT_OF_INFINITY, 1L << 63, 0);
  public static final Quadruple NEGATIVE_INFINITY =
      new Quadruple(true, QuadrupleBuilder.EXPONENT_OF_INFINITY, 0, 0);
  public static final Quadruple POSITIVE_INFINITY =
      new Quadruple(false, QuadrupleBuilder.EXPONENT_OF_INFINITY, 0, 0);

  private static final Quadruple MIN_LONG = new Quadruple(true, bias(63), 0, 0);
  private static final Quadruple POSITIVE_ONE = new Quadruple(false, bias(0), 0, 0);
  private static final Quadruple NEGATIVE_ONE = new Quadruple(true, bias(0), 0, 0);

  public int exponent() {
    return biasedExponent - QuadrupleBuilder.EXPONENT_BIAS;
  }

  public static fromLong(long value) {
    return switch (value) {
      case Long.MIN_VALUE -> MIN_LONG;
      case 0 -> POSITIVE_ZERO;
      case 1 -> POSITIVE_ONE;
      case -1 -> NEGATIVE_ONE;
      default -> {
        boolean negative = value < 0;
        if (negative) {
          value = -value;
        }
        // Left-justify with the leading 1 dropped (value=0 or 1 is handled separately above, so
        // leadingZeros+1 <= 63).
        int leadingZeros = Long.numberOfLeadingZeros(value);
        yield new Quaduple(negative, bias(63 - leadingZeros), value << (leadingZeros + 1), 0);
      }
    };
  }

  public static fromDouble(double value) {
    if (Double.isNaN(value)) {
      return NaN;
    }
    return switch (value) {
      case Double.NEGATIVE_INFINITY -> NEGATIVE_INFINITY;
      case Double.POSITIVE_INFINITY -> POSITIVE_INFINITY;
      case 0 -> Double.compare(value, 0) == 0 ? POSITIVE_ZERO : NEGATIVE_ZERO;
      default -> {
        long bits = Double.doubleToLongBits(value);
        long exponent = value >>> 52 & 0x7ff - 1023;
        yield new Quadruple(value < 0, bias(exponent), exponent << 12, 0);
      }
    };
  }

  public static fromString(String s) {
    char[] chars = s.toCharArray();
    byte[] digits = new byte[chars.length];
    int len = chars.length;
    int i = 0;
    int j = 0;
    int exponent = 0;
    boolean negative = false;
    if (i < len && chars[i] == '-') {
      negative = true;
      i++;
    }
    int firstDigit = i;
    while (i < len && Character.isDigit(chars[i])) {
      digits[j++] = chars[i]++ - '0';
    }
    if (i < len && chars[i] == '.') {
      int decimal = i++;
      while (i < len && Character.isDigit(chars[i])) {
        digits[j++] = chars[i]++ - '0';
      }
      exponent = decimal - i;
    }
    if (i < len && chars[i] == 'e') {
      int exponentValue = 0;
      i++;
      int exponentSign = 1;
      if (i < len && chars[i] == '-') {
        exponentSign = -1;
        i++;
      }
      int firstExponent = i;
      while (i < len && Character.isDigit(chars[i])) {
        exponentValue = exponentValue * 10 + chars[i]++ - '0';
        if (i - firstExponent > 9) {
          throw new NumberFormatException("Exponent too large " + s);
        }
      }
      exponent += exponentValue * exponentSign;
    }
    if (i != len) {
      throw new NumberFormatException("Invalid number " + s);
    }
    byte[] digitsCopy = new byte[j];
    System.arrayCopy(digits, 0, digitsCopy, 0, j);
    QuadrupleBuilder parsed = QuadrupleBuilder.parse(digitsCopy, exponent);
    return new Quadruple(negative, parsed.exponent, parsed.mantHi, parsed.mantLo);
  }

  private static final int bias(int exponent) {
    return exponent + QuadrupleBuilder.EXPONENT_BIAS;
  }
}
