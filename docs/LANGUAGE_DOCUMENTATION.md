# Artificial Language Documentation

This language is a small imperative language with integer arithmetic,
conditionals, loops, functions, recursion, and explicit `return` statements.
Programs are read from standard input, executed from top to bottom, and the
final values of all global variables are printed to standard output.

The language is intentionally compact. This document defines the syntax and
semantics expected from the examples in `docs/TASK_DESCRIPTION.md` and fills in
the missing details with conservative, predictable rules.

## Program Structure

A program is a sequence of top-level statements and function definitions.

```text
x = 2
y = (x + 2) * 2
```

At the top level, each non-empty line is a separate statement or function
definition. Inside compound constructs, several statements can be written as a
comma-separated sequence:

```text
x = 0
y = 0
while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1
```

Whitespace is insignificant except where it separates tokens. The following
forms are equivalent:

```text
four = add( 2, 2)
four = add(2, 2)
```

## Values and Types

The language has two value types:

- Integers, such as `0`, `2`, `100`, and `-5`.
- Booleans, written as `true` and `false`.

Arithmetic operators work on integers and produce integers. Comparison
operators work on integers and produce booleans. Conditions in `if` and `while`
statements must evaluate to booleans.

## Identifiers and Keywords

Identifiers name variables, function parameters, and functions.

An identifier starts with a letter or underscore and may contain letters,
digits, and underscores:

```text
x
y
fact_rec
fact_iter
```

The following words are reserved and cannot be used as identifiers:

```text
fun return if then else while do true false
```

## Variables

Variables are created by assignment:

```text
x = 20
y = x + 2
```

Variables do not need declarations. Reading a variable before it has been
assigned is an execution error.

Top-level assignments create global variables. Assignments inside a function
create or update variables local to that function call, including parameters.
Local variables are not printed after the program finishes.

## Expressions

Expressions compute values.

```text
2
x
(x + 2) * 2
fact_rec(n - 1)
```

Supported expression forms are:

- Integer literals: `0`, `1`, `120`.
- Boolean literals: `true`, `false`.
- Variable references: `x`, `n`, `result`.
- Parenthesized expressions: `(x + 2)`.
- Arithmetic expressions: `a + b`, `a - b`, `a * b`.
- Comparisons: `a < b`, `a <= b`, `a > b`, `a >= b`, `a == b`, `a != b`.
- Function calls: `name(arg1, arg2)`.

Operator precedence, from highest to lowest:

| Precedence | Operators and Forms |
| --- | --- |
| 1 | Function calls, parenthesized expressions |
| 2 | Unary minus: `-x` |
| 3 | Multiplication: `*` |
| 4 | Addition and subtraction: `+`, `-` |
| 5 | Comparisons: `<`, `<=`, `>`, `>=`, `==`, `!=` |

Operators with the same precedence are evaluated from left to right.

## Statements

Statements perform actions. The language supports assignment, conditionals,
loops, function definitions, and returns.

### Assignment

```text
name = expression
```

Examples:

```text
x = 2
y = (x + 2) * 2
```

The right-hand expression is evaluated first, then its value is stored in the
variable on the left.

### Conditional

```text
if condition then statement else statement
```

Example:

```text
if x > 10 then y = 100 else y = 0
```

The condition must evaluate to a boolean. If it is `true`, the `then` statement
is executed. Otherwise, the `else` statement is executed.

In an unbraced `if`, each branch is a single statement. A comma after the
`else` statement belongs to the nearest enclosing statement sequence. For
example, in the following loop, `x = x + 1` runs after the conditional on every
iteration:

```text
while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1
```

### Loop

```text
while condition do statement-sequence
```

Example:

```text
while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1
```

The condition is evaluated before each iteration. If it is `true`, the loop body
is executed and the condition is checked again. If it is `false`, execution
continues after the loop.

For an unbraced loop body, the body is the statement sequence after `do` up to
the end of the current line or enclosing block. In the example above, both the
`if` statement and `x = x + 1` are part of the loop body.

### Function Definition

```text
fun name(parameter1, parameter2) { statement-sequence }
```

Example:

```text
fun add(a, b) { return a + b }
four = add(2, 2)
```

A function definition registers a function under its name. Function definitions
do not create printable variables and do not execute their bodies immediately.
The body runs only when the function is called.

Parameters are local variables initialized from the call arguments. Arguments
are evaluated before the function call starts.

Functions may call other functions and may call themselves recursively:

```text
fun fact_rec(n) { if n <= 0 then return 1 else return n * fact_rec(n - 1) }
a = fact_rec(5)
```

Calling an undefined function or calling a function with the wrong number of
arguments is an execution error.

### Return

```text
return expression
```

Example:

```text
return a + b
```

`return` exits the current function immediately and produces the value of its
expression as the function call result. A `return` may appear inside `if` or
`while` statements.

Using `return` outside a function is an execution error. Reaching the end of a
function without executing `return` is also an execution error.

## Function Scope

Each function call has its own local scope.

```text
fun fact_iter(n) { r = 1, while true do if n == 0 then return r else r = r * n, n = n - 1 }
b = fact_iter(5)
```

In this example, `n` and `r` are local to the function call. After execution,
only the global variable `b` is printed.

Name lookup inside a function first checks local variables and parameters.
Function names are resolved from the global function table. Global variables may
be read from a function if no local variable with the same name exists, but
assignment inside a function always writes to the function-local scope.

## Program Output

In batch mode, the command-line interpreter reads source from standard input by
default:

```bash
printf 'x = 2\ny = (x + 2) * 2\n' | mvn -q exec:java
```

For convenience, it can also read from one source file argument:

```bash
mvn -q exec:java -Dexec.args="path/to/program.txt"
```

After the program finishes successfully, the interpreter prints all global
variables and their final values to standard output.

The output format is one variable per line:

```text
name: value
```

Variables are printed in the order in which they are first assigned globally.
Functions and function-local variables are not printed.

Example:

```text
stdin:
  x = 2
  y = (x + 2) * 2

stdout:
  x: 2
  y: 8
```

Boolean values, if assigned to global variables, are printed as `true` or
`false`.

## Interactive REPL

The interpreter also provides an interactive REPL mode:

```bash
mvn -q exec:java -Dexec.args="--repl"
```

The REPL reads one top-level input at a time, executes it immediately, and keeps
the same runtime state for the next input. Global variables and function
definitions therefore remain available until the session ends.

```text
> x = 2
x: 2
> y = x + 3
x: 2
y: 5
> fun inc(value) { return value + 1 }
x: 2
y: 5
> z = inc(y)
x: 2
y: 5
z: 6
```

Enter `:quit` or `:exit` to stop the session. If a function definition spans
multiple lines, the prompt changes to `... ` until the braces are balanced.
Syntax and execution errors are printed to standard error, and the REPL then
continues with the next input.

## Complete Examples

### Arithmetic

```text
stdin:
  x = 2
  y = (x + 2) * 2

stdout:
  x: 2
  y: 8
```

### Conditional

```text
stdin:
  x = 20
  if x > 10 then y = 100 else y = 0

stdout:
  x: 20
  y: 100
```

### Loop

```text
stdin:
  x = 0
  y = 0
  while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1

stdout:
  x: 3
  y: 11
```

### Function Call

```text
stdin:
  fun add(a, b) { return a + b }
  four = add(2, 2)

stdout:
  four: 4
```

### Recursive Function

```text
stdin:
  fun fact_rec(n) { if n <= 0 then return 1 else return n * fact_rec(n - 1) }
  a = fact_rec(5)

stdout:
  a: 120
```

### Iterative Function

```text
stdin:
  fun fact_iter(n) { r = 1, while true do if n == 0 then return r else r = r * n, n = n - 1 }
  b = fact_iter(5)

stdout:
  b: 120
```

## Execution Errors

The interpreter should reject invalid programs instead of silently guessing.
The following situations are execution errors:

- Syntax that does not match the language grammar.
- Reading an undefined variable.
- Calling an undefined function.
- Calling a function with the wrong number of arguments.
- Using an arithmetic operator with a non-integer value.
- Using a non-boolean value as an `if` or `while` condition.
- Using `return` outside a function.
- Reaching the end of a function without returning a value.

The task description only specifies successful program output. This
implementation prints syntax and execution errors to standard error prefixed with
`Error:` and exits with a non-zero status.
