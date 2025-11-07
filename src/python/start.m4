undefine(`len')dnl
changequote(`{', `}')dnl

define(cst, {self.$1})dnl
define(int32_constant, {$1 = $2})dnl
define(uint64_constant, {$1 = $2})dnl
define(double_constant, {$1 = $2})dnl
define(double_computed_constant, {$1 = $2})dnl
define(uint64_array_array_constant, {$1 = ( shift($@) )})dnl
define(cst_uint64, $1)dnl
define(cst_array, ( $@ ))dnl
define(uint64_array, {self.$1 = [0] * $2})dnl
define(new_digit_array, {$1 = [0] * $2})dnl
define(false, False)dnl
define(true, True)dnl
define(null, None)dnl

define(def_init, {def __init__(self):
    self.exponent = 0
    self.mantHi = 0
    self.mantLo = 0})dnl
define(def_fn, {def $2(self, shift(shift($@))):})dnl
define(def_array_fn, {def $3(self, shift(shift(shift($@)))):})dnl
define(array_size, {})dnl
define(fn, {self.$1})dnl
define(ret_void, {})dnl
define(ret_bool, {})dnl
define(ret_int32, {})dnl
define(ret_int64, {})dnl
define(ret_double, {})dnl
define(let, {$@})dnl
define(bool_decl, $1)dnl
define(digit_decl, $1)dnl
define(digits_decl, $1)dnl
define(int32_decl, $1)dnl
define(int64_decl, $1)dnl
define(uint64_decl, $1)dnl
define(uint64_array_decl, $1)dnl
define(uint64_array_array_decl, $1)dnl
define(double_decl, $1)dnl

define(field, {self.$1})dnl
define(ref, {(self.$1)})dnl
define(deref, {($1)})dnl
define(array_len, {len(($1))})dnl

define(c_if, {if $1:})dnl
define(c_else, {else:})dnl
define(c_elsif, {elif $1:})dnl
define(c_and, {and})dnl
define(c_while, {while $1:})dnl
define(c_for_range, {for $1 in range($2, $3):})dnl
define(c_for_range_down, {for $1 in range(($2) - 1, ($3) - 1, -1):})dnl
define(c_end, {})dnl

define(to_digit, {($1)})dnl
define(to_exponent, {($1)})dnl
define(to_double, {($1)})dnl
define(to_uint64, {($1)})dnl
define(wrap_uint64, {(($1) & 0xffffffffffffffff)})dnl
define(lsr, {(($1) >> ($2))})dnl
define(int_divide, {(($1) // ($2))})dnl

define(f_number_of_leading_zeros, {(66-len(bin(($1))))})dnl We only need this for positive non-zeros.dnl
define(f_log, {math.log(($1))})dnl
define(f_floor, {math.floor(($1))})dnl
define(f_iabs, {abs(($1))})dnl

import math

class QuadrupleBuilder(object):

  @staticmethod
  def parseDecimal(digits, exp10):
    q = QuadrupleBuilder()
    q.parse([ord(c) - 48 for c in digits], exp10)
    return q

