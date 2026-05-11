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

There is no Maven wrapper in this repository, so the commands below use the
system `mvn` installation.

## Build And Test

Run the full test suite:

```bash
mvn test
```

Compile, test, and package the project:

```bash
mvn clean package
```

The package command creates build artifacts under `target/`. The recommended
way to run the interpreter during review is through the configured Maven exec
plugin, shown below.

## Run

Read a program from standard input:

```bash
printf 'x = 2\ny = (x + 2) * 2\n' | mvn -q exec:java
```

Expected output:

```text
x: 2
y: 8
```

Run a program from a source file:

```bash
mvn -q exec:java -Dexec.args="path/to/program.txt"
```

Start an interactive REPL session:

```bash
mvn -q exec:java -Dexec.args="--repl"
```

In REPL mode, enter `:quit` or `:exit` to stop the session.

Print CLI usage:

```bash
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

- `src/main/kotlin/com/example/interpreter/Main.kt`: command-line entry point
  for stdin, file execution, help output, and REPL mode.
- `src/main/kotlin/com/example/interpreter/Interpreter.kt`: in-memory
  interpreter facade that connects parsing, evaluation, and formatting.
- `src/main/kotlin/com/example/interpreter/lexer`: source text to tokens.
- `src/main/kotlin/com/example/interpreter/parser`: tokens to AST.
- `src/main/kotlin/com/example/interpreter/ast`: immutable program model.
- `src/main/kotlin/com/example/interpreter/eval`: AST execution.
- `src/main/kotlin/com/example/interpreter/runtime`: values, runtime state,
  errors, execution results, and output formatting.
- `src/test/kotlin/com/example/interpreter`: parser, evaluator, formatter,
  interpreter, and CLI tests.

The pipeline is:

```text
source text -> lexer -> parser -> AST -> evaluator -> output formatter
```

## Documentation

- `docs/TASK_DESCRIPTION.md`: original assignment and sample programs.
- `docs/LANGUAGE_DOCUMENTATION.md`: language syntax and semantic decisions.
- `docs/ARCHITECTURE_OVERVIEW.md`: implemented pipeline, component
  responsibilities, CLI behavior, and verification strategy.

For a technical review, start with this README, then read
`docs/LANGUAGE_DOCUMENTATION.md` for expected language behavior and
`docs/ARCHITECTURE_OVERVIEW.md` for implementation boundaries.
