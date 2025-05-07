# Multi-Language Decimal to Binary Floating-Point Conversion

The conversion source code is in `QuadrupleBuilder.m4`, written in a
pseudo-language defined via set of
[m4](https://en.wikipedia.org/wiki/M4_(computer_language)) macros.

For each supported language -- currently Java, Python and C++ -- a set of macro
definitions is provided to convert the constructs used in `QuadrupleBuilder.m4`
to an appropriate construct in the target language. A `Makefile` rule takes
these macro definitions and `QuadrupleBuilder.m4` and generates the actual
source code for each language in the top-level, per-language directories.

Adding a new language may be as simple as adding a new set of macro definitions,
but in general may require adding new abstractions to the pseudo-language and
updating `QuadrupleBuilder.m4` and all existing language macro definitions. For
example, `QuadrupleBuilder.m4` currently assumes that a hexadecimal constant can
always be introduced with the `0x` prefix. If this doesn't hold in some new
target language, then a `hex` macro could be added to abstract the use of
hexadecimal constants - for instance, `0x2a4` would become `hex(2a4)` in
`QuadrupleBuilder.m4` with all existing languages defining `hex` as

    define(hex, `0x$1')

and the new language defining it in some appropriate fashion.

## Input and Output

The input to conversion is the decimal digits and decimal exponent of the
decimal floating-point number.

The output is the 128-bit binary floating point (normalized, with leading
1 omitted) mantissa and 32-bit binary exponent -- the format of
https://github.com/m-vokhm/Quadruple

Subnormals are not supported - converting such a number will return 0.

The conversion does not handle the input's sign as it has no impact on the
conversion of the decimal digits and exponent to the mantissa and exponent.

## Overall Structure

Each language L should define:

- in the top-level directory `L`, an appropriate, language-specific
  `QuadrupleBuilder` package (for instance, Java defines this package with
  [maven](https://en.wikipedia.org/wiki/Apache_Maven)
- in `src` (this directory) and `test`, a directory `L` with `L/start.m4` and
  (optional) `L/end.m4` files that define the macros below and include the
  header and footer code for the language-specific `QuadrupleBuilder` abstraction
  (see `test/README.md` for more details on the unit test code)
- a `Makefile` rule to generate the language-specific `QuadrupleBuilder` abstraction from 
  `L/start.m4`, `QuadrupleBuilder.m4` and `L/end.m4` - this rule should put the generated
  output in the appropriate place in the top-level `L` directory
  
  this rule also needs to convert `#` comments to L's comment syntax

The language-specific abstraction should accept a sign, a string of decimal
digits and a decimal exponent and return a sign, 128-bit binary mantissa, and
32-bit biased binary exponent. The `parse` function in `QuadrupleBuilder.m4`
does the bulk of this conversion, the language-specific abstraction only needs
to expose it in a language-appropriate way.

## Assumptions and Requirements

### Data Types

The conversion code works with

- arrays of decimal digits (for the input)
- booleans
- signed 32 and 64-bit integers
- unsigned 32 and 64-bit integers
- IEEE 64-bit binary floating point numbers
- arrays of unsigned 64-bit integers

Arrays are passed by reference, the other types by value.

+, - and * are assumed to work on the numeric types, and << on the integer
types. No wrapping is assumed, the only requirement is that the low-order
32/64-bits of the result match those of the result type (signed or
unsigned). Explicit wrapping is used where overflow is possible.

The type conversions (`to_digit`, `to_exponent`, `to_uint64`) are not required
to clamp numbers to the target type's range.

## Control Structures

The pseudo-language has

- comments to the end of line introduced with #
- constant declarations for the supported data types
- variable declarations for the supported data types
- initialisation code, for pre-allocating array variables
- functions which accept arguments of the supported data types and optionally
  return a supported data type value - `return` is used to return a result
- if/else-if/else
- while loops
- for loops over a range (up and down)
- various custom operators and functions on the supported data types
- casts to supported data types

Code is indented Python-style, indentation is with spaces only. All statements
are terminated by a semicolon.

## Macros

### Constant Declarations

All declarations happen at the top-level of `QuadrupleBuilder.m4`.

- `int32_constant(name, value)` - define a 32-bit signed constant called
  `name` with value `value`
- `uint64_constant(name, value)` - define a 64-bit unsigned constant called
  `name` with value `value`
- `double_constant(name, value)` - define a 64-bit floating point constant called
  `name` with value `value`
- `uint64_array_array_constant(name, array1, array2, ...)` - define an array of
  4-element arrays containing 64-bit unsigned integers called `name`, with
  4-element arrays `array1`, `array2`, ...
- `cst_array(C1, C2, C3, C4)` - a 4-element constant array of 64-bit unsigned
  integers (used with `uint64_array_array_constant`)
  
  the code cheats and places a signed number in C1, and uses `wrap_uint64` where
  necessary to compare signed values with this array element

### Assumed Fields

The following fields are assumed to exist: `exponent`, `mantHi`, `mantLo`.

### Fields Containing Pre-Allocated Arrays

- `def_init()` - introduces the declarations of the pre-allocated arrays
- `uint64_array(name, size)` - pre-allocate an array called `name` that holds `size` 64-bit
  integers (initial contents undefined)
  
### Argument and Local Variable Declarations

- `bool_decl(name)` - declare a boolean
- `digit_decl(name)` - declare a decimal digit
- `int32_decl(name)` - declare a signed 32-bit integer
- `int64_decl(name)` - declare a signed 64-bit integer
- `uint64_decl(name)` - declare an unsigned 64-bit integer
- `double_decl(name)` - declare an IEEE 64-bit floating point number
- `uint64_array_decl(name, size)` - declare a reference to an array of `size` unsigned 64-bit
  integers
- `uint64_array_array_decl(name)` - declare a reference to an `uint64_array_array_constant` (also see `ref`)
- `new_digit_array(name, size)` - declare and allocate an array called `name` that holds `size` decimal
  digits (initial contents undefined)

Local variable declarations must be followed by an initializer (`= expression`)
  
### Function Definitions

- `def_fn(return_type, name, arg1, arg2, ...)` - declare a function called
  `name` with return type `return_type` (must be one of the `ret_X` macros) and
  arguments `arg1`, `arg2`, ... (must be one of the `X_decl` macros above)

  the body of the function must be indented within `def_fn` and must be
  terminated with `c_end`
- `def_array_fn(array_size(N1, ...), return_type, name, arg1, arg2, ...)` - like
  `def_fn`, but the declarations of `arg1`, ... can use `N1`, ... in their
  `uint64_array_decl` types; the resulting function is generic over the size of
  the corresponding arrays
- `ret_void` - a function with no result
- `ret_bool` - a function that returns a boolean
- `ret_int32` - a function that returns a signed 32-bit integer
- `ret_int64` - a function that returns a signed 64-bit integer
- `ret_double` - a function that returns an IEEE 64-bit floating point number

### Control Structures

Control structures follow Python indentation rules. All control structures (and
function definitions) end with `c_end`.

- `c_if(e)` - start of an if
- `c_elsif(e)` - "else if" within an if
- `c_else` - else part of an if 
- `e1 c_and e2` - boolean and with short-circuiting (`e2` is not evaluated if `e1` is false)
- `c_while(e)` - start of a while loop
- `c_for_range(name, e1, e2)` - a loop where `name` ranges from `e1` (inclusive)
  to `e2` (exclusive) - `name` is a 32-bit signed integer
- `c_for_range_down(name, e1, e2)` - a loop where `name` ranges down from `e1`
  (exclusive) to `e2` (inclusive) - `name` is a 32-bit signed integer

### Expressions

#### Basic Expressions

Decimal and hexadecimal constants are allowed - hexadecimal constants use the 0x prefix.

- `cst(name)` - reference constant `name`
- `cst_uint64(C)` - a 64-bit unsigned constant - may contain embedded underscores
  for readability
- `field(name)` - reference one of the assumed fields or pre-allocated arrays
- `ref(name)` - make a reference to a `uint64_array_array_constant`
- `deref(name)` - use a reference declared with `uint64_array_array_decl`
- `fn(name)(e1, e2, ...)` - invoke function `name` with arguments `e1`, `e2`, ...

### Required Functions

- `array_len(e)` - returns length of array `e`
- `to_digit(e)` - converts e to a decimal digit - no value clamping required
- `to_exponent(e)` - converts e (unsigned in the range 0 to 2^32-1) to a value storeable in `field(exponent)` - no value clamping required
- `to_uint64(e)` - converts e to an unsigned 64-bit integer - no value clamping required
- `wrap_uint64(e)` - wraps e to the unsigned 64-bit integer range
- `lsr(e, N)` - shifts unsigned 64-bit integer left by N bits
- `int_divide(e1, e2)` - integer divide (round towards 0) of 64-bit unsigned integers
- `f_number_of_leading_zeros(e)` - number of leading zeros of 64-bit unsigned integer e - can assume e is non-zero
- `f_log(e)` - IEEE 64-bit floating point natural logarithm
- `f_floor(e)` - IEEE 64-bit floating point round down to the closest signed 64-bit integer
- `f_iabs(e)` - 32-bit signed integer absolute value
