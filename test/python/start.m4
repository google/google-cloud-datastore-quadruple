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

define(test_case, `  def test_$1(self):')
define(quadruple_test, `  self.check($1, $2, translit($3, _), translit($4, _), translit($5, _));')
define(test_case_end, `
')

import unittest
from quadruple_builder import QuadrupleBuilder

class TestQuadupleBuilder(unittest.TestCase):

  def check(self, mantissa, exp10, mant_hi, mant_lo, exponent):
    self.signedCheck(False, mantissa, exp10, mant_hi, mant_lo, exponent)
    self.signedCheck(True, mantissa, exp10, mant_hi, mant_lo, exponent)

  def signedCheck(self, negative, mantissa, exp10, mant_hi, mant_lo, exponent):
    q = QuadrupleBuilder.parseDecimal(negative, mantissa, exp10)
    self.assertEqual(q.negative, negative)
    self.assertEqual(q.mantHi, mant_hi)
    self.assertEqual(q.mantLo, mant_lo)
    self.assertEqual(q.exponent, exponent)

