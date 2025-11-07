#  Copyright 2025 Google LLC
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

define(quadruple_test, `check($1, translit($2, _), translit($3, _)LL, translit($4, _)LL, translit($5, _)LL);')dnl
define(test_case, `TEST(QuadrupleBuilderTest, $1) {')dnl
define(test_case_end, `}
')dnl

$include "quadruple_builder.h"
$include <cstdint>
$include <string>
$include <vector>
$include <gtest/gtest.h>

void check(const std::string& digits,
           int64_t exp10, uint64_t mant_hi, uint64_t mant_lo, uint64_t exponent) {
  std::vector<uint8_t> vdigits(digits.size());
  for (int i = 0; i < digits.size(); i++) {
    vdigits[i] = digits[i] - '0';
  }

  cloud_datastore::QuadrupleBuilder parser;
  parser.parseDecimal(vdigits, exp10);

  std::string name = digits + "e" + std::to_string(exp10);
  EXPECT_EQ(parser.mantHi, mant_hi) << name + " (hi)";
  EXPECT_EQ(parser.mantLo, mant_lo) << name + " (lo)";
  EXPECT_EQ(parser.exponent, exponent) << name + " (exp)";
}

