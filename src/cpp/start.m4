define(cst, `$1')dnl
define(int32_constant, `static constexpr int32_t $1 = translit($2, _);')dnl
define(uint64_constant, `static constexpr uint64_t $1 = translit($2, _)L;')dnl
define(double_constant, `static constexpr double $1 = $2;')dnl
define(double_computed_constant, `static double $1 = $2;')dnl
define(uint64_array_array_constant, `static std::array<std::array<uint64_t, 4>, 33> $1 = {{ shift($@) }};')dnl
define(cst_uint64, `translit($1, _)LL')dnl
define(cst_array, ` {{ static_cast<uint64_t>($1), $2, $3, $4 }}')dnl
define(uint64_array, `')dnl
define(new_digit_array, `std::vector<uint8_t> $1($2)')dnl

define(def_init, `')dnl
define(def_fn, `$1 QuadrupleBuilder::$2(shift(shift($@))) {')dnl
define(def_array_fn, `template<sizes($1)> $2 QuadrupleBuilder::$3(shift(shift(shift($@)))) {')dnl
define(array_size, ``$@'')
define(sizes, `ifelse($#, 1, `std::size_t $1', `std::size_t $1, sizes(shift($@))')')dnl
define(fn, `$1')dnl
define(ret_void, `void')dnl
define(ret_bool, `bool')dnl
define(ret_int32, `int32_t')dnl
define(ret_double, `double')dnl
define(let, `$@')dnl
define(bool_decl, `bool $1')dnl
define(digit_decl, `uint8_t $1')dnl
define(digits_decl, `std::vector<uint8_t>& $1')dnl
define(int32_decl, `int32_t $1')dnl
define(uint64_decl, `uint64_t $1')dnl
define(uint64_array_decl, `std::array<uint64_t, $2>& $1')dnl
define(uint64_array_array_decl, `std::array<std::array<uint64_t, 4>, 33>* $1')dnl
define(double_decl, `double $1')dnl

define(field, `this->$1')dnl
define(ref, `(&($1))')dnl
define(deref, `(*($1))')dnl
define(array_len, `static_cast<int32_t>(($1).size())')dnl

define(c_if, `if ($1) {')dnl
define(c_else, `} else {')dnl
define(c_elsif, `} else if ($1) {')dnl
define(c_and, `&&')dnl
define(c_while, `while ($1) {')dnl
define(c_for_range, `for (int32_t $1 = ($2); $1 < ($3); $1++) {')dnl
define(c_for_range_down, `for (int32_t $1 = ($2) - 1; $1 >= ($3); $1--) {')dnl
define(c_end, `}')dnl

define(to_digit, `(static_cast<uint8_t>($1))')dnl
define(to_exponent, `(static_cast<uint32_t>($1))')dnl
define(to_double, `(static_cast<double>($1))')dnl
define(to_uint64, `(static_cast<uint64_t>($1))')dnl
define(wrap_uint64, `(static_cast<uint64_t>($1))')dnl
define(lsr, `(($1) >> ($2))')dnl
define(int_divide, `(($1) / ($2))')dnl

define(f_number_of_leading_zeros, `__builtin_clzll($1)')dnl
define(f_log, `log($1)')dnl
define(f_floor, `floor($1)')dnl
define(f_iabs, `static_cast<int32_t>(labs($1))')dnl

$include "quadruple_builder.h"
$include <stdlib.h>
$include <math.h>
$include <array>
$include <cstdint>
$include <cstddef>
$include <vector>

namespace cloud_datastore {
