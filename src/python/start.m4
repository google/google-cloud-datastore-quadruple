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
undefine(`len')
changequote(`{', `}')

define(cst, {self.$1})
define(int32_constant, {$1 = $2})
define(uint64_constant, {$1 = translit($2, _)})
define(double_constant, {$1 = $2})
define(uint64_array_array_constant, {$1 = ( shift($@) )})
define(cst_uint64, translit($1, _))
define(cst_array, ( $@ ))
define(uint64_array, {self.$1 = [0] * $2})
define(digit_array, {self.$1 = [0] * $2})
define(false, False)
define(true, True)
define(null, None)

define(def_init, {def __init__(self):
    self.negative = False
    self.exponent = 0
    self.mantHi = 0
    self.mantLo = 0})
define(def_fn, {def $2(self, shift(shift($@))):})
define(fn, {self.$1})
define(ret_void, {})
define(ret_bool, {})
define(ret_int32, {})
define(ret_int64, {})
define(ret_uint64_array, {})
define(ret_double, {})
define(bool_decl, $1)
define(digit_decl, $1)
define(digits_decl, $1)
define(int32_decl, $1)
define(int64_decl, $1)
define(uint64_decl, $1)
define(uint64_array_decl, $1)
define(uint64_array_array_decl, $1)
define(double_decl, $1)

define(field, {self.$1})
define(array_len, {len(($1))})

define(c_if, {if $1:})
define(c_else, {else:})
define(c_elsif, {elif $1:})
define(c_and, {and})
define(c_while, {while $1:})
define(c_for_range, {for $1 in range($2, $3):})
define(c_for_range_down, {for $1 in range(($2) - 1, ($3) - 1, -1):})
define(c_end, {})

define(to_digit, {($1)})
define(to_exponent, {($1)})
define(to_uint64, {($1)})
define(wrap_uint64, (($1) & 0xffffffffffffffff))
define(lsr, {(($1) >> ($2))})
define(int_divide, {(($1) // ($2))})

define(f_number_of_leading_zeros, {(66-len(bin(($1))))})dnl We only need this for positive non-zeros.
define(f_log, {math.log(($1))})
define(f_floor, {math.floor(($1))})
define(f_iabs, {abs(($1))})

import math

class QuadrupleBuilder(object):

  @staticmethod
  def parseDecimal(negative, digits, exp10):
    q = QuadrupleBuilder()
    q.parse(negative, [ord(c) - 48 for c in digits], exp10)
    return q

