#  Copyright 2021 M.Vokhmentsev
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

define(cst, `$1')
define(int32_constant, `private static final int $1 = $2;')
define(uint64_constant, `private static final long $1 = translit($2, _)L;')
define(double_constant, `private static final double $1 = $2;')
define(uint64_array_array_constant, `private static final long[][] $1 = { shift($@) };')
define(cst_uint64, translit($1, _)L)
define(cst_array, { $@ })
define(uint64_array, `private final long[] $1 = new long[$2];')
define(digit_array, `private final byte[] $1 = new byte[$2];')

define(def_init, `')
define(def_fn, `private $1 $2(shift(shift($@))) {')
define(fn, `$1')
define(ret_void, `void')
define(ret_bool, `boolean')
define(ret_int32, `int')
define(ret_int64, `long')
define(ret_uint64_array, `long[]')
define(ret_double, `double')
define(bool_decl, `boolean $1')
define(digit_decl, `byte $1')
define(digits_decl, `byte[] $1')
define(int32_decl, `int $1')
define(int64_decl, `long $1')
define(uint64_decl, `long $1')
define(uint64_array_decl, `long[] $1')
define(uint64_array_array_decl, `long[][] $1')
define(double_decl, `double $1')

define(field, `this.$1')
define(array_len, `($1).length')

define(c_if, `if ($1) {')
define(c_else, `} else {')
define(c_elsif, `} else if ($1) {')
define(c_and, `&&')
define(c_while, `while ($1) {')
define(c_for_range, `for (int $1 = ($2); $1 < ($3); $1++) {')
define(c_for_range_down, `for (int $1 = ($2) - 1; $1 >= ($3); $1--) {')
define(c_end, `}')

define(to_digit, `((byte)($1))')
define(to_exponent, `((int)($1))')
define(to_uint64, `((long)($1))')
define(wrap_uint64, `($1)')
define(lsr, `(($1) >>> ($2))')
define(int_divide, `(($1) / ($2))')

define(f_number_of_leading_zeros, `Long.numberOfLeadingZeros($1)')
define(f_log, `Math.log($1)')
define(f_floor, `((long) Math.floor($1))')
define(f_iabs, `Math.abs($1)')

package com.google.cloud.datastore.core.quadruple;

public class QuadrupleBuilder {

  public static QuadrupleBuilder parseDecimal(boolean negative, byte[] digits, long exp10) {
    QuadrupleBuilder q = new QuadrupleBuilder();
    q.parse(negative, digits, exp10);
    return q;
  }

  # The fields containing the value of the instance
  public boolean negative;
  public int exponent;
  public long mantHi;
  public long mantLo;
