define(cst, `QuadrupleBuilder.$1')dnl
define(int32_constant, `static $1 = $2;')dnl
define(uint64_constant, `static $1 = $2n;')dnl
define(double_constant, `static $1 = $2;')dnl
define(double_computed_constant, `static $1 = $2;')dnl
define(uint64_array_array_constant, `static $1: bigint[][] = [ shift($@) ];')dnl
define(cst_uint64, `$1n')dnl
define(cst_array, `[ $1n, shift($@) ]')dnl
define(uint64_array, `$1: bigint[] = new Array($2).fill(0n);')dnl
define(new_digit_array, `let $1: number[] = new Array($2).fill(0);')dnl

define(def_init, `')dnl
define(def_fn, `$2(shift(shift($@))) : $1 {')dnl
define(def_array_fn, `$3(shift(shift(shift($@)))) : $2 {')dnl
define(array_size, `')dnl
define(fn, `this.$1')dnl
define(ret_void, `void')dnl
define(ret_bool, `boolean')dnl
define(ret_int32, `number')dnl
define(ret_int64, `bigint')dnl
define(ret_double, `number')dnl
define(let, `l`'et $1')dnl
define(bool_decl, `$1: boolean')dnl
define(digit_decl, `$1: number')dnl
define(digits_decl, `$1: number[]')dnl
define(int32_decl, `$1: number')dnl
define(int64_decl, `$1: bigint')dnl
define(uint64_decl, `$1: bigint')dnl
define(uint64_array_decl, `$1: bigint[]')dnl
define(uint64_array_array_decl, `$1: bigint[][]')dnl
define(double_decl, `$1: number')dnl

define(field, `this.$1')dnl
define(ref, `QuadrupleBuilder.$1')dnl
define(deref, `($1)')dnl
define(array_len, `($1).length')dnl

define(c_if, `if ($1) {')dnl
define(c_else, `} else {')dnl
define(c_elsif, `} else if ($1) {')dnl
define(c_and, `&&')dnl
define(c_while, `while ($1) {')dnl
define(c_for_range, `for (let $1 = ($2); $1 < ($3); $1++) {')dnl
define(c_for_range_down, `for (let $1 = ($2) - 1; $1 >= ($3); $1--) {')dnl
define(c_end, `}')dnl

define(to_digit, `($1)')dnl
define(to_exponent, `Number($1)')dnl
define(to_uint64, `BigInt($1)')dnl
define(to_double, `Number($1)')dnl
define(wrap_uint64, `(($1) & 0xffffffffffffffffn)')dnl
define(lsr, `(($1) >> ($2))')dnl
define(int_divide, `(($1) / ($2))')dnl

define(f_number_of_leading_zeros, `QuadrupleBuilder.clz64($1)')dnl
define(f_log, `Math.log($1)')dnl
define(f_floor, `Math.floor($1)')dnl
define(f_iabs, `Math.abs($1)')dnl

export class QuadrupleBuilder {

  static parseDecimal(digits: number[], exp10: number): QuadrupleBuilder {
    let q = new QuadrupleBuilder();
    q.parse(digits, exp10);
    return q;
  }

  # The fields containing the value of the instance
  exponent: number = 0;
  mantHi: bigint = 0n;
  mantLo: bigint = 0n;
