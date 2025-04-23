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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

public class QuadrupleTest {
  private static final int QUADRUPLE_BIAS = 0x7fff_ffff;

  private void assertLessThan(Quadruple q1, Quadruple q2) {
    assertTrue(q1.compareTo(q2) < 0);
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
  public void infinityAndNaN() {
    assertEquals(Quadruple.fromDouble(Double.NaN), Quadruple.NaN);
    assertEquals(toDouble(Quadruple.NaN), Double.NaN);

    assertEquals(Quadruple.fromDouble(Double.NEGATIVE_INFINITY), Quadruple.NEGATIVE_INFINITY);
    assertEquals(toDouble(Quadruple.NEGATIVE_INFINITY), Double.NEGATIVE_INFINITY);

    assertEquals(Quadruple.fromDouble(Double.POSITIVE_INFINITY), Quadruple.POSITIVE_INFINITY);
    assertEquals(toDouble(Quadruple.POSITIVE_INFINITY), Double.POSITIVE_INFINITY);

    assertLessThan(Quadruple.NEGATIVE_INFINITY, Quadruple.POSITIVE_INFINITY);
    assertLessThan(Quadruple.NEGATIVE_INFINITY, Quadruple.NaN);
    assertLessThan(Quadruple.POSITIVE_INFINITY, Quadruple.NaN);
  }

  @Test
  public void zero() {
    assertEquals(toDouble(Quadruple.POSITIVE_ZERO), 0.0);
    assertEquals(Quadruple.fromDouble(0.0), Quadruple.POSITIVE_ZERO);
    assertEquals(toDouble(Quadruple.NEGATIVE_ZERO), -0.0);
    assertEquals(Quadruple.fromDouble(-0.0), Quadruple.NEGATIVE_ZERO);
    assertEquals(Quadruple.fromLong(0), Quadruple.POSITIVE_ZERO);

    assertLessThan(Quadruple.NEGATIVE_INFINITY, Quadruple.NEGATIVE_ZERO);
    assertLessThan(Quadruple.NEGATIVE_ZERO, Quadruple.POSITIVE_ZERO);
    assertLessThan(Quadruple.POSITIVE_ZERO, Quadruple.POSITIVE_INFINITY);
  }

  // @Test
  public void randomQuadruple() {
    Random random = new Random(4254);

    for (int i = 0; i < 1000; i++) {
      long mantissa = random.nextLong(1L << 52);
      // Avoid subnormal exponents, as it makes building identical double
      // and Quadruple values harder.
      int exponent = random.nextInt(1022 + 1023 + 1) - 1022;
      boolean negative = random.nextInt(1) == 0;
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
      double d1 = makeDouble(random.nextInt(1) == 0, exponent1, mantissa1);
      double d2 = makeDouble(random.nextInt(1) == 0, exponent2, mantissa2);
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

  private static long nextRandomLong(Random random) {
    int bits = random.nextInt(64) + 1;
    long i64 = random.nextLong();
    if (bits == 64) {
      return i64;
    }
    i64 &= (1L << bits) - 1;
    return i64 - (1L << (bits - 1));
  }
}
