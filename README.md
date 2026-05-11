# JetBrains Interpreter Task

Kotlin implementation of a small interpreter for the artificial language from
the JetBrains internship task. The project is intentionally kept as a focused
CLI mini-project: it parses source code, executes it, and prints the final
global variables.

## What It Supports

- Integer and boolean values.
- Assignments and global variable output.
- `if ... then ... else ...` conditionals.
- `while ... do ...` loops.
- Function definitions, calls, local variables, and recursion.
- Batch execution from standard input or a source file.
- Interactive REPL mode with persistent variables and functions.

## Requirements

- JDK 17 or newer.
- Maven 3.x.

There is no Maven wrapper in this repository. The root [`./run`](./run) helper
wraps the same system `mvn` commands used by the project and keeps reviewer
commands short.

## Quick Reviewer Commands

Show every available helper command:

```bash
./run help
```

Run the automated tests:

```bash
./run test
```

Run a built-in sample program:

```bash
./run sample
```

Start an interactive REPL session:

```bash
./run repl
```

Run a program from standard input or a source file:

```bash
printf 'x = 2\ny = (x + 2) * 2\n' | ./run stdin
./run file path/to/program.txt
```

## Build And Test

Run the full test suite:

```bash
./run test
```

Compile, test, and package the project:

```bash
./run package
```

The package command creates build artifacts under `target/`. If you prefer
running Maven directly, use `mvn test` and `mvn clean package`.

## Run

The recommended review commands use the root helper:

```bash
./run sample
printf 'x = 2\ny = (x + 2) * 2\n' | ./run stdin
./run file path/to/program.txt
./run repl
./run cli-help
```

`./run repl` starts an interactive session. Type one statement or function
definition, press Enter, and the REPL prints the current global variables.
Variables and functions stay available for later inputs:

```text
> x = 2
x: 2
> y = x + 3
x: 2
y: 5
> :quit
```

Enter `:quit` or `:exit` to stop. See
[`docs/RUNNING_AND_TESTING.md`](docs/RUNNING_AND_TESTING.md#repl-usage) for a
longer REPL walkthrough with functions and multi-line input.

The sample and stdin commands above print:

```text
x: 2
y: 8
```

Equivalent direct Maven commands are:

```bash
printf 'x = 2\ny = (x + 2) * 2\n' | mvn -q exec:java
mvn -q exec:java -Dexec.args="path/to/program.txt"
mvn -q exec:java -Dexec.args="--repl"
mvn -q exec:java -Dexec.args="--help"
```

## Runtime Behavior

On successful batch execution, the interpreter prints all global variables to
standard output in first-assignment order. Function definitions and local
function variables are not printed.

Syntax and execution errors are printed to standard error. Batch mode returns a
non-zero exit code for language or file errors. REPL mode keeps running after a
language error, so the last successful session state remains available.

## Project Structure

- [`src/main/kotlin/com/example/interpreter/Main.kt`](src/main/kotlin/com/example/interpreter/Main.kt):
  command-line entry point for stdin, file execution, help output, and REPL mode.
- [`src/main/kotlin/com/example/interpreter/Interpreter.kt`](src/main/kotlin/com/example/interpreter/Interpreter.kt):
  in-memory interpreter facade that connects parsing, evaluation, and formatting.
- [`src/main/kotlin/com/example/interpreter/lexer`](src/main/kotlin/com/example/interpreter/lexer):
  source text to tokens.
- [`src/main/kotlin/com/example/interpreter/parser`](src/main/kotlin/com/example/interpreter/parser):
  tokens to AST.
- [`src/main/kotlin/com/example/interpreter/ast`](src/main/kotlin/com/example/interpreter/ast):
  immutable program model.
- [`src/main/kotlin/com/example/interpreter/eval`](src/main/kotlin/com/example/interpreter/eval):
  AST execution.
- [`src/main/kotlin/com/example/interpreter/runtime`](src/main/kotlin/com/example/interpreter/runtime):
  values, runtime state, errors, execution results, and output formatting.
- [`src/test/kotlin/com/example/interpreter`](src/test/kotlin/com/example/interpreter):
  parser, evaluator, formatter, interpreter, and CLI tests.

The pipeline is:

```text
source text -> lexer -> parser -> AST -> evaluator -> output formatter
```

## Documentation

- [`docs/TASK_DESCRIPTION.md`](docs/TASK_DESCRIPTION.md): original assignment
  and sample programs.
- [`docs/LANGUAGE_DOCUMENTATION.md`](docs/LANGUAGE_DOCUMENTATION.md): language
  syntax and semantic decisions.
- [`docs/ARCHITECTURE_OVERVIEW.md`](docs/ARCHITECTURE_OVERVIEW.md): implemented
  pipeline, component
  responsibilities, CLI behavior, and verification strategy.
- [`docs/RUNNING_AND_TESTING.md`](docs/RUNNING_AND_TESTING.md): reviewer-focused
  wrapper and Maven commands.

For a technical review, start with this README, then read
[`docs/LANGUAGE_DOCUMENTATION.md`](docs/LANGUAGE_DOCUMENTATION.md) for expected
language behavior and [`docs/RUNNING_AND_TESTING.md`](docs/RUNNING_AND_TESTING.md)
for command details.
