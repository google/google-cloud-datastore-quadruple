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

#include "quadruple.h"

#include <stdint.h>

#include <cmath>
#include <cstdint>
#include <limits>
#include <random>
#include <gtest/gtest.h>

#include "third_party/absl/random/random.h"

namespace cloud_datastore {
namespace {

Quadruple kMinusInfinity(-INFINITY);
Quadruple kPlusInfinity(INFINITY);
Quadruple kNaN(NAN);
Quadruple kPlusZero(0.0);
Quadruple kMinusZero(-0.0);

static void ExpectLessThan(Quadruple q1, Quadruple q2) {
  EXPECT_EQ(q1.Compare(q1), 0) << q1.DebugString() << " == itself";
  EXPECT_EQ(q1.Compare(q2), -1)
      << q1.DebugString() << " < " << q2.DebugString();
  EXPECT_EQ(q2.Compare(q1), 1) << q2.DebugString() << " > " << q1.DebugString();
  EXPECT_EQ(q2.Compare(q2), 0) << q2.DebugString() << " == itself";
}

static Quadruple Quadruple64(int64_t x) { return Quadruple(x); }

TEST(QuadrupleTest, Simple) {
  Quadruple q1 = Quadruple64(1);
  Quadruple q2(static_cast<double>(1.1));
  EXPECT_EQ(static_cast<double>(q1), 1.0);
  EXPECT_EQ(static_cast<double>(Quadruple64(-1)), -1.0);
  EXPECT_EQ(static_cast<double>(q2), 1.1);
  ExpectLessThan(q1, q2);
}

TEST(QuadrupleTest, InfinityAndNaN) {
  EXPECT_TRUE(std::isnan(static_cast<double>(kNaN)));
  EXPECT_EQ(static_cast<double>(kPlusInfinity), INFINITY);
  EXPECT_EQ(static_cast<double>(kMinusInfinity), -INFINITY);

  ExpectLessThan(kMinusInfinity, kPlusInfinity);
  ExpectLessThan(kMinusInfinity, kNaN);
  ExpectLessThan(kPlusInfinity, kNaN);
}

TEST(QuadrupleTest, Zero) {
  EXPECT_EQ(static_cast<double>(kPlusZero), 0.0);
  EXPECT_EQ(static_cast<double>(kMinusZero), 0.0);
  EXPECT_EQ(std::signbit(static_cast<double>(kPlusZero)), false);
  EXPECT_EQ(std::signbit(static_cast<double>(kMinusZero)), true);
  EXPECT_EQ(Quadruple64(0).Compare(kPlusZero), 0);

  ExpectLessThan(kMinusZero, kPlusZero);
  ExpectLessThan(kMinusZero, Quadruple64(0));
}

TEST(QuadrupleTest, RandomQuadruple) {
  absl::BitGen gen;
  std::uniform_int_distribution<uint64_t> mantissa_gen(0, (1LL << 52) - 1);
  // Avoid subnormal exponents, as it makes building identical double
  // and Quadruple values harder.
  std::uniform_int_distribution<> exponent_gen(-1022, 1023);
  std::uniform_int_distribution<> negative_gen(0, 1);
  for (int i = 0; i < 1000; i++) {
    uint64_t mantissa = mantissa_gen(gen);
    int exponent = exponent_gen(gen);
    bool negative = negative_gen(gen);
    uint64_t exponentAndSign = (negative ? 1ULL << 63 : 0) |
                               (static_cast<uint32_t>(exponent) + 0x7fffffffU);
    Quadruple q(exponentAndSign, mantissa << 12, 0);
    double d = ldexp(1 + ldexp(mantissa, -52), exponent) * (negative ? -1 : 1);
    EXPECT_EQ(q.Compare(Quadruple(d)), 0) << q.DebugString() << " == " << d;

    // Make some nearby numbers.
    Quadruple q_plus1(exponentAndSign, mantissa << 12, 1);
    Quadruple q_minus1(exponentAndSign, (mantissa << 12) - 1,
                       static_cast<uint64_t>(-1));
    Quadruple q_minus2(exponentAndSign, (mantissa << 12) - 1,
                       static_cast<uint64_t>(-2));
    if (negative) {
      ExpectLessThan(q_minus1, q_minus2);
      ExpectLessThan(q, q_minus2);
      ExpectLessThan(q_plus1, q_minus2);
      ExpectLessThan(q, q_minus1);
      ExpectLessThan(q_plus1, q_minus1);
      ExpectLessThan(q_plus1, q);
    } else {
      ExpectLessThan(q_minus2, q_minus1);
      ExpectLessThan(q_minus2, q);
      ExpectLessThan(q_minus2, q_plus1);
      ExpectLessThan(q_minus1, q);
      ExpectLessThan(q_minus1, q_plus1);
      ExpectLessThan(q, q_plus1);
    }
  }
}

TEST(QuadrupleTest, RandomDouble) {
  absl::BitGen gen;
  std::uniform_real_distribution<> mantissa(0.5, 1.0);
  // Includes (double) subnormal exponents.
  std::uniform_int_distribution<> exponent(-1074, 1023);
  std::uniform_int_distribution<> negative(0, 1);
  for (int i = 0; i < 1000; i++) {
    double d1 = scalb(mantissa(gen), exponent(gen)) * (negative(gen) ? -1 : 1);
    double d2 = scalb(mantissa(gen), exponent(gen)) * (negative(gen) ? -1 : 1);
    Quadruple q1 = Quadruple(d1);
    Quadruple q2 = Quadruple(d2);

    EXPECT_EQ(static_cast<double>(q1), d1);
    EXPECT_EQ(static_cast<double>(q2), d2);
    ExpectLessThan(kMinusInfinity, q1);
    ExpectLessThan(kMinusInfinity, q2);
    if (d1 < d2) {
      ExpectLessThan(q1, q2);
    } else {
      ExpectLessThan(q2, q1);
    }
    ExpectLessThan(q1, kPlusInfinity);
    ExpectLessThan(q1, kNaN);
    ExpectLessThan(q2, kPlusInfinity);
    ExpectLessThan(q2, kNaN);
  }
}

TEST(QuadrupleTest, MinMaxInt64) {
  Quadruple min_int64(std::numeric_limits<int64_t>::min());
  Quadruple nearly_min_int64(std::numeric_limits<int64_t>::min() + 1);
  EXPECT_EQ(min_int64.Compare(Quadruple(-pow(2.0, 63))), 0);
  ExpectLessThan(min_int64, nearly_min_int64);

  Quadruple max_int64(std::numeric_limits<int64_t>::max());
  Quadruple nearly_max_int64(std::numeric_limits<int64_t>::max() - 1);
  ExpectLessThan(nearly_max_int64, max_int64);
  ExpectLessThan(max_int64, Quadruple(pow(2.0, 63)));
}

TEST(QuadrupleTest, LargeAndSmall) {
  // 2^65536, which is far beyond the Decimal128 range of 10^6111 =~ 2^20300.
  uint32_t kLargeExponent = Quadruple::kExponentBias + 65536;
  // 2^-65535, which is far below the Decimal128 range of 10^-6176 =~ 2^20516.
  uint32_t kSmallExponent = Quadruple::kExponentBias - 65535;

  Quadruple large(kLargeExponent, 0, 0);
  EXPECT_EQ(static_cast<double>(large), HUGE_VAL);
  Quadruple small(kSmallExponent, 0, 0);
  EXPECT_EQ(static_cast<double>(small), 0);
  EXPECT_FALSE(std::signbit(static_cast<double>(small)));

  Quadruple large_negative(1ULL << 63 | kLargeExponent, 0, 0);
  EXPECT_EQ(static_cast<double>(large_negative), -HUGE_VAL);
  Quadruple small_negative(1ULL << 63 | kSmallExponent, 0, 0);
  EXPECT_EQ(static_cast<double>(small_negative), 0);
  EXPECT_TRUE(std::signbit(static_cast<double>(small_negative)));
}

TEST(QuadrupleTest, RandomLong) {
  absl::BitGen gen;
  std::uniform_int_distribution<> bitCount(0, 63);
  for (int i = 0; i < 1000; i++) {
    int bits = bitCount(gen);
    int64_t min = -1LL << bits;
    int64_t max =
        bits != 63 ? (1LL << bits) - 1 : std::numeric_limits<int64_t>::max();
    std::uniform_int_distribution<int64_t> int64_gen(min, max);
    int64_t i64 = int64_gen(gen);
    double d64 = static_cast<double>(i64);  // May be rounded.
    Quadruple q(i64);
    if (static_cast<int64_t>(d64) == i64) {
      EXPECT_EQ(static_cast<double>(q), i64);
    } else {
      EXPECT_NE(q.Compare(Quadruple(d64)), 0);
    }
    ExpectLessThan(kMinusInfinity, q);
    ExpectLessThan(q, kPlusInfinity);
    ExpectLessThan(q, kNaN);

    int64_t other = int64_gen(gen);
    if (i64 < other) {
      ExpectLessThan(q, Quadruple(other));
    } else if (i64 > other) {
      ExpectLessThan(Quadruple(other), q);
    } else {
      EXPECT_EQ(Quadruple(other).Compare(q), 0);
    }
  }
}

TEST(QuadrupleTest, ParseInvalid) {
  Quadruple q;
  EXPECT_FALSE(q.Parse(""));
  EXPECT_FALSE(q.Parse("-NaN"));
  EXPECT_FALSE(q.Parse("+NaN"));
  EXPECT_FALSE(q.Parse("-"));
  EXPECT_FALSE(q.Parse("+"));
  EXPECT_FALSE(q.Parse("blurb"));
  EXPECT_FALSE(q.Parse("1a"));
  EXPECT_FALSE(q.Parse("123.45a"));
  EXPECT_FALSE(q.Parse("123.45e"));
  EXPECT_FALSE(q.Parse("123.45ek"));
  EXPECT_FALSE(q.Parse("123.45e-k"));
  EXPECT_FALSE(q.Parse("123.45e+k"));
  EXPECT_FALSE(q.Parse("123.45e-12z"));
  EXPECT_FALSE(q.Parse("123.45e+12z"));
  EXPECT_FALSE(q.Parse("123.45e1234567890"));
}

TEST(QuadrupleTest, ParseSpecial) {
  Quadruple q;
  EXPECT_TRUE(q.Parse("NaN"));
  EXPECT_EQ(q.Compare(kNaN), 0);

  EXPECT_TRUE(q.Parse("Infinity"));
  EXPECT_EQ(q.Compare(kPlusInfinity), 0);

  EXPECT_TRUE(q.Parse("+Infinity"));
  EXPECT_EQ(q.Compare(kPlusInfinity), 0);

  EXPECT_TRUE(q.Parse("-Infinity"));
  EXPECT_EQ(q.Compare(kMinusInfinity), 0);
}

TEST(Quadruple, Parse) {
  Quadruple q;
  EXPECT_TRUE(q.Parse("2"));
  EXPECT_EQ(q.Compare(Quadruple(2.0)), 0);

  EXPECT_TRUE(q.Parse("+22"));
  EXPECT_EQ(q.Compare(Quadruple(22.0)), 0);

  EXPECT_TRUE(q.Parse("-25"));
  EXPECT_EQ(q.Compare(Quadruple(-25.0)), 0);

  EXPECT_TRUE(q.Parse("1.5"));
  EXPECT_EQ(q.Compare(Quadruple(1.5)), 0);

  EXPECT_TRUE(q.Parse(".5"));
  EXPECT_EQ(q.Compare(Quadruple(0.5)), 0);

  EXPECT_TRUE(q.Parse("12."));
  EXPECT_EQ(q.Compare(Quadruple(12.0)), 0);

  EXPECT_TRUE(q.Parse("42e1"));
  EXPECT_EQ(q.Compare(Quadruple(420.0)), 0);

  EXPECT_TRUE(q.Parse("4.2e3"));
  EXPECT_EQ(q.Compare(Quadruple(4200.0)), 0);

  EXPECT_TRUE(q.Parse("4.1e+3"));
  EXPECT_EQ(q.Compare(Quadruple(4100.0)), 0);

  EXPECT_TRUE(q.Parse("420e-1"));
  EXPECT_EQ(q.Compare(Quadruple(42.0)), 0);

  EXPECT_TRUE(q.Parse("1000000000000e-10"));
  EXPECT_EQ(q.Compare(Quadruple(100.0)), 0);
}

}  // namespace
}  // namespace cloud_datastore

int main(int argc, char** argv) {
  ::testing::InitGoogleTest(&argc, argv);
  return RUN_ALL_TESTS();
}

