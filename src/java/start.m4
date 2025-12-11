define(cst, `$1')dnl
define(int32_constant, `static final int $1 = $2;')dnl
define(uint64_constant, `static final long $1 = translit($2, _)L;')dnl
define(double_constant, `static final double $1 = $2;')dnl
define(double_computed_constant, `static final double $1 = $2;')dnl
define(uint64_array_array_constant, `private static final long[][] $1 = { shift($@) };')dnl
define(cst_uint64, translit($1, _)L)dnl
define(cst_array, { $@ })dnl
define(uint64_array, `private final long[] $1 = new long[$2];')dnl
define(new_digit_array, `byte[] $1 = new byte[$2];')dnl

define(def_init, `')dnl
define(def_fn, `private $1 $2(shift(shift($@))) {')dnl
define(def_array_fn, `private $2 $3(shift(shift(shift($@)))) {')dnl
define(array_size, `')dnl
define(fn, `$1')dnl
define(ret_void, `void')dnl
define(ret_bool, `boolean')dnl
define(ret_int32, `int')dnl
define(ret_double, `double')dnl
define(let, `$@')dnl
define(bool_decl, `boolean $1')dnl
define(digit_decl, `byte $1')dnl
define(digits_decl, `byte[] $1')dnl
define(int32_decl, `int $1')dnl
define(uint64_decl, `long $1')dnl
define(uint64_array_decl, `long[] $1')dnl
define(uint64_array_array_decl, `long[][] $1')dnl
define(double_decl, `double $1')dnl

define(field, `this.$1')dnl
define(ref, `($1)')dnl
define(deref, `($1)')dnl
define(array_len, `($1).length')dnl

define(c_if, `if ($1) {')dnl
define(c_else, `} else {')dnl
define(c_elsif, `} else if ($1) {')dnl
define(c_and, `&&')dnl
define(c_not, `!($1)')dnl
define(c_while, `while ($1) {')dnl
define(c_for_range, `for (int $1 = ($2); $1 < ($3); $1++) {')dnl
define(c_for_range_down, `for (int $1 = ($2) - 1; $1 >= ($3); $1--) {')dnl
define(c_end, `}')dnl

define(to_digit, `((byte)($1))')dnl
define(to_exponent, `((int)(long)($1))')dnl
define(to_uint64, `((long)($1))')dnl
define(to_double, `((double)($1))')dnl
define(wrap_uint64, `($1)')dnl
define(lsr, `(($1) >>> ($2))')dnl
define(int_divide, `(($1) / ($2))')dnl

define(f_number_of_leading_zeros, `Long.numberOfLeadingZeros($1)')dnl
define(f_log, `Math.log($1)')dnl
define(f_floor, `((long) Math.floor($1))')dnl
define(f_iabs, `Math.abs($1)')dnl

package com.google.cloud.datastore.core.quadruple;

public class QuadrupleBuilder {

  public static QuadrupleBuilder parseDecimal(byte[] digits, int exp10) {
    QuadrupleBuilder q = new QuadrupleBuilder();
    q.parse(digits, exp10);
    return q;
  }

  public void avoidDecimal128CollisionsWithDouble() {
    doAvoidDecimal128CollisionsWithDouble();
  }

  # The fields containing the value of the instance
  public int exponent;
  public long mantHi;
  public long mantLo;
  public int rounding;
