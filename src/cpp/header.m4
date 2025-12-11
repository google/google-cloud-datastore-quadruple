define(uint64_array, `std::array<uint64_t, $2> $1;')dnl

define(def_fn, `$1 $2(shift(shift($@)));')dnl
define(def_array_fn, `template<sizes($1)> $2 $3(shift(shift(shift($@))));')dnl
define(array_size, ``$@'')
define(sizes, `ifelse($#, 1, `std::size_t $1', `std::size_t $1, sizes(shift($@))')')dnl
define(ret_void, `void')dnl
define(ret_bool, `bool')dnl
define(ret_int32, `int32_t')dnl
define(ret_int64, `int64_t')dnl
define(ret_double, `double')dnl
define(bool_decl, `bool $1')dnl
define(digit_decl, `uint8_t $1')dnl
define(digits_decl, `std::vector<uint8_t>& $1')dnl
define(int32_decl, `int32_t $1')dnl
define(int64_decl, `int64_t $1')dnl
define(uint64_decl, `uint64_t $1')dnl
define(uint64_array_decl, `std::array<uint64_t, $2>& $1')dnl
define(double_decl, `double $1')dnl

$ifndef CLOUD_DATASTORE_UTIL_QUADRUPLE_BUILDER
$define CLOUD_DATASTORE_UTIL_QUADRUPLE_BUILDER

$include <array>
$include <cstddef>
$include <cstdint>
$include <vector>

namespace cloud_datastore {

class QuadrupleBuilder {
public:
  void parseDecimal(std::vector<uint8_t>& digits, int64_t exp10) {
    parse(digits, exp10);
  }

  # The fields containing the value of the instance
  uint32_t exponent;
  uint64_t mantHi;
  uint64_t mantLo;
  int32_t rounding;

private:
