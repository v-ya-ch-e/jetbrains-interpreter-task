# Architecture Overview

This project implements the interpreter pipeline for the artificial language
defined in `docs/LANGUAGE_DOCUMENTATION.md`.

## Implemented Pipeline

```text
source text -> lexer -> parser -> AST -> evaluator -> output formatter
```

`Main.kt` is the command-line adapter. It reads the full source program from
standard input by default, reads from one source file argument when provided, or
starts an interactive REPL with `--repl`. Batch execution delegates to
`Interpreter`, which wires together the parser, evaluator, and output formatter.
REPL execution uses `InterpreterSession` to keep one runtime state across
multiple inputs.

## Lexer

`lexer/Lexer.kt` converts source text into tokens with source locations for
syntax errors. It recognizes identifiers, integer literals, boolean keywords,
control-flow keywords, arithmetic and comparison operators, parentheses,
braces, commas, newlines, and EOF.

Newlines are significant at the top level: they separate top-level statements
and function definitions. Whitespace otherwise only separates tokens.

## Parser And AST

`parser/Parser.kt` is a recursive-descent parser that builds the immutable AST
types from `ast/Ast.kt`.

Supported syntax includes:

- Top-level assignments and function definitions.
- `if condition then statement else statement`.
- `while condition do statement-sequence`.
- `return expression`.
- Comma-separated statement sequences inside compound constructs.
- Integer and boolean literals, variable references, unary minus, arithmetic,
  comparisons, parenthesized expressions, and function calls.

Expression precedence follows the language documentation: calls and
parentheses, unary minus, multiplication, addition/subtraction, then
comparisons.

## Evaluator

`eval/Evaluator.kt` executes AST declarations from top to bottom. Batch
execution creates a fresh runtime state for each program; session execution
passes an existing `RuntimeState` back into the evaluator.

Implemented runtime behavior includes:

- Ordered global variables stored in first-assignment order.
- Function registration and calls with arity checks.
- Recursive function calls.
- Function-local variables and parameters.
- Global variable reads from functions when no local variable shadows the name.
- Return propagation through `if` and `while` statements.
- Execution errors for undefined variables/functions, invalid argument counts,
  invalid operand types, top-level `return`, and functions that finish without
  returning.

The evaluator returns an `ExecutionResult` instead of formatted text. This keeps
execution independent from presentation and makes evaluator tests assert directly
against runtime values.

## Output Formatter

`runtime/OutputFormatter.kt` formats the final `ExecutionResult` as one global
variable per line:

```text
name: value
```

Only global variables are printed. Function definitions and function-local
variables are omitted.

An empty result formats as an empty string. Non-empty output does not include a
trailing newline; `Main.kt` is responsible for printing the returned text.

## Command-Line Entry Point

`InterpreterCli` keeps `Main.kt` testable by accepting explicit argument,
input, output, and error streams. The top-level `main(args)` passes the real
process streams to this adapter and exits with the returned code.

CLI behavior:

- `interpreter` reads source from standard input.
- `interpreter path/to/program.txt` reads source from that file.
- `interpreter --repl` or `interpreter -i` starts an interactive session.
- `interpreter --help` prints usage information.
- Syntax and evaluation errors are printed to standard error with exit code `1`.
- Invalid CLI usage is printed to standard error with exit code `2`.

In REPL mode, `InterpreterRepl` prompts with `> ` for a new top-level input and
`... ` while a multi-line function definition has unclosed braces. Each
successful input prints the current formatted global state. Language errors are
printed to standard error without ending the session, so the previous successful
state remains available for later input.

## Verification

Tests document the parser, evaluator, formatter, and integrated interpreter
behavior:

- `ParserTest` verifies AST construction and statement grouping.
- `EvaluatorTest` verifies execution semantics, scope, returns, recursion, and
  runtime errors.
- `OutputFormatterTest` verifies final text rendering.
- `InterpreterTest` verifies the complete in-memory pipeline before `Main.kt`.
- `InterpreterTest` also verifies persistent session behavior.
- `MainTest` verifies command-line stdin input, file input, REPL mode, help text,
  stderr, and exit codes.
