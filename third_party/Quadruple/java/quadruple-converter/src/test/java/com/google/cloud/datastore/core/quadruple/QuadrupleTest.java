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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class QuadrupleTest {
  private static final int QUADRUPLE_BIAS = 0x7fff_ffff;
  private static final Quadruple OTHER_NAN = new Quadruple(true, (int) 0xFFFFFFFFL, 1, 1);

  @Test
  public void simple() {
    Quadruple q1 = Quadruple.fromLong(1);
    Quadruple q2 = Quadruple.fromDouble(1.1);

    assertEquals(1.0, toDouble(q1));
    assertEquals(-1.0, toDouble(Quadruple.fromLong(-1)));
    assertEquals(1.1, toDouble(q2));
    assertLessThan(q1, q2);
  }

  @Test
  public void orderAndEquivalence() {
    List<List<Quadruple>> orderedEquivalences = new ArrayList<>();

    add(
        orderedEquivalences,
        Quadruple.NEGATIVE_INFINITY,
        Quadruple.fromDouble(Double.NEGATIVE_INFINITY),
        Quadruple.fromString("-Infinity"));
    add(
        orderedEquivalences,
        Quadruple.fromLong(Long.MIN_VALUE),
        Quadruple.fromDouble((double) Long.MIN_VALUE),
        Quadruple.fromString(Long.toString(Long.MIN_VALUE)));
    add(orderedEquivalences, Quadruple.fromDouble(-1.125), Quadruple.fromString("-1.125"));
    add(
        orderedEquivalences,
        Quadruple.fromLong(-1),
        Quadruple.fromDouble(-1),
        Quadruple.fromString("-1"));
    add(
        orderedEquivalences,
        Quadruple.fromString("-0"),
        Quadruple.fromDouble(-0.0),
        Quadruple.NEGATIVE_ZERO);
    add(
        orderedEquivalences,
        Quadruple.fromString("0"),
        Quadruple.fromString("+0"),
        Quadruple.fromDouble(0.0),
        Quadruple.fromLong(0),
        Quadruple.POSITIVE_ZERO);
    add(
        orderedEquivalences,
        Quadruple.fromLong(325),
        Quadruple.fromDouble(325),
        Quadruple.fromString("325"));
    // Long.MAX_VALUE is not exactly representable as a double.
    add(
        orderedEquivalences,
        Quadruple.fromLong(Long.MAX_VALUE),
        Quadruple.fromString(Long.toString(Long.MAX_VALUE)));
    add(
        orderedEquivalences,
        Quadruple.POSITIVE_INFINITY,
        Quadruple.fromDouble(Double.POSITIVE_INFINITY),
        Quadruple.fromString("Infinity"),
        Quadruple.fromString("+Infinity"));
    add(orderedEquivalences, Quadruple.NaN, OTHER_NAN, Quadruple.fromString("NaN"));

    int classCount = orderedEquivalences.size();
    for (int classIndex1 = 0; classIndex1 < classCount; classIndex1++) {
      List<Quadruple> class1 = orderedEquivalences.get(classIndex1);
      for (int classIndex2 = 0; classIndex2 < classCount; classIndex2++) {
        List<Quadruple> class2 = orderedEquivalences.get(classIndex2);

        for (int index1 = 0; index1 < class1.size(); index1++) {
          Quadruple q1 = class1.get(index1);
          for (int index2 = 0; index2 < class2.size(); index2++) {
            Quadruple q2 = class2.get(index2);
            String testCase = classIndex1 + "/" + index1 + " <-> " + classIndex2 + "/" + index2;
            if (classIndex1 == classIndex2) {
              assertEquals(q1, q2, testCase);
              assertTrue(q1.compareTo(q2) == 0, testCase);
              assertTrue(q2.compareTo(q1) == 0, testCase);
              assertEquals(q1.hashCode(), q2.hashCode(), testCase);
            } else if (classIndex1 < classIndex2) {
              assertNotEquals(q1, q2, testCase);
              assertTrue(q1.compareTo(q2) < 0, testCase);
              assertTrue(q2.compareTo(q1) > 0, testCase);
            } else {
              assertNotEquals(q1, q2);
              assertTrue(q1.compareTo(q2) > 0, testCase);
              assertTrue(q2.compareTo(q1) < 0, testCase);
            }
          }
        }
      }
    }
  }

  private static void add(List<List<Quadruple>> orderedEquivalences, Quadruple... equivalent) {
    List<Quadruple> equivalences = new ArrayList<>();
    for (Quadruple q : equivalent) {
      equivalences.add(q);
    }
    orderedEquivalences.add(equivalences);
  }

  @Test
  public void infinityAndNaN() {
    assertEquals(toDouble(Quadruple.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY);
    assertEquals(toDouble(Quadruple.POSITIVE_INFINITY), Double.POSITIVE_INFINITY);

    assertEquals(toDouble(Quadruple.NaN), Double.NaN);
    assertEquals(toDouble(OTHER_NAN), Double.NaN);

    assertTrue(Quadruple.NaN.isNaN());
    assertTrue(OTHER_NAN.isNaN());
    assertFalse(Quadruple.NEGATIVE_INFINITY.isNaN());
    assertFalse(Quadruple.POSITIVE_ZERO.isNaN());
    assertFalse(Quadruple.fromLong(1).isNaN());

    assertTrue(Quadruple.NEGATIVE_INFINITY.isInfinite());
    assertTrue(Quadruple.POSITIVE_INFINITY.isInfinite());
    assertFalse(Quadruple.NaN.isInfinite());
    assertFalse(Quadruple.POSITIVE_ZERO.isInfinite());
    assertFalse(Quadruple.fromLong(1).isInfinite());
  }

  @Test
  public void zero() {
    assertEquals(toDouble(Quadruple.POSITIVE_ZERO), 0.0);
    assertEquals(toDouble(Quadruple.NEGATIVE_ZERO), -0.0);

    assertTrue(Quadruple.NEGATIVE_ZERO.isZero());
    assertTrue(Quadruple.POSITIVE_ZERO.isZero());
    assertFalse(Quadruple.NEGATIVE_INFINITY.isZero());
    assertFalse(Quadruple.POSITIVE_INFINITY.isZero());
    assertFalse(Quadruple.NaN.isZero());
    assertFalse(OTHER_NAN.isZero());
    assertFalse(Quadruple.fromLong(1).isZero());
  }

  @Test
  public void randomQuadruple() {
    Random random = new Random(4254);

    for (int i = 0; i < 1000; i++) {
      long mantissa = random.nextLong(1L << 52);
      // Avoid subnormal exponents, as it makes building identical double
      // and Quadruple values harder.
      int exponent = random.nextInt(1022 + 1023 + 1) - 1022;
      boolean negative = random.nextInt(2) == 0;
      int biasedExponent = exponent + QUADRUPLE_BIAS;
      Quadruple q = new Quadruple(negative, biasedExponent, mantissa << 12, 0);
      double d = makeDouble(negative, exponent, mantissa);
      assertEquals(q, Quadruple.fromDouble(d));

      // Make some nearby numbers.
      Quadruple qPlus1 = new Quadruple(negative, biasedExponent, mantissa << 12, 1);
      Quadruple qMinus1 = new Quadruple(negative, biasedExponent, (mantissa << 12) - 1, -1L);
      Quadruple qMinus2 = new Quadruple(negative, biasedExponent, (mantissa << 12) - 1, -2L);
      if (negative) {
        assertLessThan(qMinus1, qMinus2);
        assertLessThan(q, qMinus2);
        assertLessThan(qPlus1, qMinus2);
        assertLessThan(q, qMinus1);
        assertLessThan(qPlus1, qMinus1);
        assertLessThan(qPlus1, q);
      } else {
        assertLessThan(qMinus2, qMinus1);
        assertLessThan(qMinus2, q);
        assertLessThan(qMinus2, qPlus1);
        assertLessThan(qMinus1, q);
        assertLessThan(qMinus1, qPlus1);
        assertLessThan(q, qPlus1);
      }
    }
  }

  private static double makeDouble(boolean negative, int exponent, long mantissa) {
    if (exponent < -1022) {
      mantissa = (mantissa | 1L << 53) >>> -exponent - 1022;
      exponent = -1023;
    }
    return Double.longBitsToDouble((negative ? 1L << 63 : 0) | (exponent + 1023L) << 52 | mantissa);
  }

  @Test
  public void randomDouble() {
    Random random = new Random(5442);

    for (int i = 0; i < 1000; i++) {
      long mantissa1 = random.nextLong() & ((1L << 52) - 1);
      long mantissa2 = random.nextLong() & ((1L << 52) - 1);
      // Includes (double) subnormal exponents.
      int exponent1 = random.nextInt(1074 + 1023 + 1) - 1074;
      int exponent2 = random.nextInt(1074 + 1023 + 1) - 1074;
      double d1 = makeDouble(random.nextInt(2) == 0, exponent1, mantissa1);
      double d2 = makeDouble(random.nextInt(2) == 0, exponent2, mantissa2);
      Quadruple q1 = Quadruple.fromDouble(d1);
      Quadruple q2 = Quadruple.fromDouble(d2);

      assertEquals(d1, toDouble(q1));
      assertEquals(d2, toDouble(q2));
      assertLessThan(Quadruple.NEGATIVE_INFINITY, q1);
      assertLessThan(Quadruple.NEGATIVE_INFINITY, q2);
      if (d1 < d2) {
        assertLessThan(q1, q2);
      } else {
        assertLessThan(q2, q1);
      }
      assertLessThan(q1, Quadruple.POSITIVE_INFINITY);
      assertLessThan(q1, Quadruple.NaN);
      assertLessThan(q2, Quadruple.POSITIVE_INFINITY);
      assertLessThan(q2, Quadruple.NaN);
    }
  }

  @Test
  public void minMaxInt64() {
    Quadruple minInt64 = Quadruple.fromLong(Long.MIN_VALUE);
    Quadruple nearlyMinInt64 = Quadruple.fromLong(Long.MIN_VALUE + 1);
    assertEquals(minInt64, Quadruple.fromDouble(-Math.pow(2.0, 63)));
    assertLessThan(minInt64, nearlyMinInt64);

    Quadruple maxInt64 = Quadruple.fromLong(Long.MAX_VALUE);
    Quadruple nearlyMaxInt64 = Quadruple.fromLong(Long.MAX_VALUE - 1);
    assertLessThan(nearlyMaxInt64, maxInt64);
    assertLessThan(maxInt64, Quadruple.fromDouble(Math.pow(2.0, 63)));
  }

  @Test
  public void randomLong() {
    Random random = new Random(2445);
    for (int i = 0; i < 1000; i++) {
      long i64 = nextRandomLong(random);
      double d64 = (double) i64; // May be rounded.
      Quadruple q = Quadruple.fromLong(i64);
      if ((long) d64 == i64) {
        assertEquals(i64, toDouble(q));
      } else {
        assertNotEquals(q, Quadruple.fromDouble(d64));
      }
      assertLessThan(Quadruple.NEGATIVE_INFINITY, q);
      assertLessThan(q, Quadruple.POSITIVE_INFINITY);
      assertLessThan(q, Quadruple.NaN);

      long other = nextRandomLong(random);
      Quadruple otherQ = Quadruple.fromLong(other);
      if (i64 < other) {
        assertLessThan(q, otherQ);
      } else if (i64 > other) {
        assertLessThan(otherQ, q);
      } else {
        assertEquals(otherQ, q);
      }
    }
  }

  @Test
  public void fromString_invalid() {
    invalidString("");
    invalidString("-NaN");
    invalidString("+NaN");
    invalidString("-");
    invalidString("+");
    invalidString("blurb");
    invalidString("1a");
    invalidString("123.45a");
    invalidString("123.45e");
    invalidString("123.45ek");
    invalidString("123.45e-k");
    invalidString("123.45e+k");
    invalidString("123.45e-12z");
    invalidString("123.45e+12z");
    invalidString("123.45e1234567890");
  }

  private static void invalidString(String s) {
    assertThrows(NumberFormatException.class, () -> Quadruple.fromString(s));
  }

  @Test
  public void fromString() {
    assertEquals(Quadruple.fromDouble(2), Quadruple.fromString("2"));
    assertEquals(Quadruple.fromDouble(22), Quadruple.fromString("+22"));
    assertEquals(Quadruple.fromDouble(-25), Quadruple.fromString("-25"));
    assertEquals(Quadruple.fromDouble(1.5), Quadruple.fromString("1.5"));
    assertEquals(Quadruple.fromDouble(0.5), Quadruple.fromString(".5"));
    assertEquals(Quadruple.fromDouble(12), Quadruple.fromString("12."));
    assertEquals(Quadruple.fromDouble(420), Quadruple.fromString("42e1"));
    assertEquals(Quadruple.fromDouble(4200), Quadruple.fromString("4.2e3"));
    assertEquals(Quadruple.fromDouble(4100), Quadruple.fromString("4.1e+3"));
    assertEquals(Quadruple.fromDouble(42), Quadruple.fromString("420e-1"));
    assertEquals(Quadruple.fromDouble(100), Quadruple.fromString("1000000000000e-10"));
  }

  @Test
  public void noadjust() {
    testNoAdjust(Quadruple.fromDouble(3)); // Canary 1.
    testNoAdjust(Quadruple.fromDouble(20)); // Canary 2.

    Random random = new Random(2445);
    for (int i = 0; i < 1000; i++) {
      // Generate some random doubles between 2^27 and 2^112 - these are always exactly
      // representable as a Decimal128.
      long mantissa = random.nextLong() & ((1L << 52) - 1);
      int exponent = random.nextInt(112 - 27) + 27;
      boolean negative = random.nextInt(2) == 0;
      testNoAdjust(new Quadruple(negative, exponent + QUADRUPLE_BIAS, mantissa << 12, 0));
    }
  }

  private void testNoAdjust(Quadruple q) {
    // Check q is a double exactly convertible to Decimal128.
    double x = toDouble(q);
    assertEquals(q, Quadruple.fromDouble(x));
    BigDecimal d = new BigDecimal(x, MathContext.DECIMAL128);
    assertEquals(d, new BigDecimal(x));

    // Check there's no collision avoidance.
    assertEquals(Quadruple.fromString(d.toString()), q);
    assertEquals(Quadruple.fromStringNoDoubleCollisions(d.toString()), q);
  }

  @Test
  public void adjust() {
    // Decimal128 numbers (34-digit, IEEE decimal floating point standard) that round up or down to
    // a Quadruple with 75 trailing zero bits would be incorrectly considered equal to the
    // corresponding double. We detect this and increase or decrease the 128-bit mantissa by 1 to
    // compensate.

    // An (exact) double without an exact Decimal128 representation.
    double roundsUp = 0.5 + Math.scalb((double) 0b0011_1110_0000_0101, -53);
    BigDecimal roundedUp = new BigDecimal(roundsUp, MathContext.DECIMAL128);
    // The decimal version is an approximation:
    assertGreaterThan(roundedUp, new BigDecimal(roundsUp));
    // But the 128-bit binary mantissa approximation of roundedUp is identical to roundsUp's
    assertEquals(Quadruple.fromString(roundedUp.toString()), Quadruple.fromDouble(roundsUp));
    // ... but asQuadruple() restores the expected order (including for the complement).
    assertGreaterThan(
        Quadruple.fromStringNoDoubleCollisions(roundedUp.toString()),
        Quadruple.fromDouble(roundsUp));
    assertLessThan(
        Quadruple.fromStringNoDoubleCollisions("-" + roundedUp.toString()),
        Quadruple.fromDouble(-roundsUp));

    // Another (exact) double without an exact Decimal128 representation.
    double roundsDown = 0.5 + Math.scalb((double) 0b0010_1000_0001_0101_0010, -53);
    BigDecimal roundedDown = new BigDecimal(roundsDown, MathContext.DECIMAL128);
    // The decimal128 version is an approximation:
    assertLessThan(roundedDown, new BigDecimal(roundsDown));
    // But the 128-bit binary mantissa approximation of roundedDown is identical to roundsDown's
    assertEquals(Quadruple.fromString(roundedDown.toString()), Quadruple.fromDouble(roundsDown));
    // ... but asQuadruple() restores the expected order (including for the complement).
    assertLessThan(
        Quadruple.fromStringNoDoubleCollisions(roundedDown.toString()),
        Quadruple.fromDouble(roundsDown));
    assertGreaterThan(
        Quadruple.fromStringNoDoubleCollisions("-" + roundedDown.toString()),
        Quadruple.fromDouble(-roundsDown));

    // A very large (integral) double without an exact Decimal128 representation.
    double largeDouble = Math.scalb(1.0, 137) + Math.scalb((double) 0x7be46d5e42994L, 137 - 52);
    BigDecimal roundedDecimal = new BigDecimal(largeDouble, MathContext.DECIMAL128);
    // The decimal version is an approximation (rounded down):
    assertLessThan(roundedDecimal, new BigDecimal(largeDouble));
    // But the 128-bit binary mantissa approximation of roundedDecimal is identical to largeDouble's
    assertEquals(
        Quadruple.fromString(roundedDecimal.toString()), Quadruple.fromDouble(largeDouble));
    // ... but asQuadruple() restores the expected order (including for the complement).
    assertLessThan(
        Quadruple.fromStringNoDoubleCollisions(roundedDecimal.toString()),
        Quadruple.fromDouble(largeDouble));
    assertGreaterThan(
        Quadruple.fromStringNoDoubleCollisions("-" + roundedDecimal.toString()),
        Quadruple.fromDouble(-largeDouble));
  }

  private static long nextRandomLong(Random random) {
    int bits = random.nextInt(64) + 1;
    long i64 = random.nextLong();
    if (bits == 64) {
      return i64;
    }
    i64 &= (1L << bits) - 1;
    return i64 - (1L << (bits - 1));
  }

  private <T extends Comparable<T>> void assertLessThan(T q1, T q2) {
    assertTrue(q1.compareTo(q2) < 0, String.format("%s < %s", q1, q2));
  }

  private <T extends Comparable<T>> void assertGreaterThan(T q1, T q2) {
    assertTrue(q1.compareTo(q2) > 0, String.format("%s > %s", q1, q2));
  }

  private double toDouble(Quadruple q) {
    if (q.biasedExponent() == 0) {
      return q.negative() ? -0.0 : 0.0;
    }
    if (q.biasedExponent() == (int) 0xffffffff) {
      return (q.mantHi() | q.mantLo()) != 0
          ? Double.NaN
          : q.negative() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    }
    int doubleExponent = q.exponent();
    long mantissa = q.mantHi() >>> 12; // Does not round.
    if (doubleExponent > 1023) {
      doubleExponent = 1024;
      mantissa = 0;
    } else if (doubleExponent < -1022) {
      // subnormal
      if (doubleExponent < -1074) {
        mantissa = 0;
      } else {
        mantissa = (mantissa | 1L << 52) >>> -doubleExponent - 1022;
      }
      doubleExponent = -1023;
    }
    return Double.longBitsToDouble(
        (q.negative() ? 1L << 63 : 0) | ((doubleExponent + 1023L) << 52) | mantissa);
  }
}
