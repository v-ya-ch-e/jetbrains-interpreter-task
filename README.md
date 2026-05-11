# JetBrains Interpreter Task

Kotlin/Maven project for the artificial language interpreter described in
`docs/TASK_DESCRIPTION.md` and `docs/LANGUAGE_DOCUMENTATION.md`.

## Build

```bash
mvn test
```

## Run

Read a program from standard input:

```bash
printf 'x = 2\ny = (x + 2) * 2\n' | mvn -q exec:java
```

Run a program from a source file:

```bash
mvn -q exec:java -Dexec.args="path/to/program.txt"
```

Print CLI usage:

```bash
mvn -q exec:java -Dexec.args="--help"
```

On success, the interpreter prints global variables to standard output in their
first-assignment order. Syntax and execution errors are printed to standard error
with a non-zero exit code.

## Current Structure

- `lexer`: converts source text into tokens.
- `parser`: converts tokens into the AST.
- `ast`: immutable representation of programs, statements, and expressions.
- `eval`: executes the AST.
- `runtime`: runtime values, state, errors, and output formatting.
- `Main.kt`: command-line entry point for stdin and file-based execution.

The implemented pipeline is documented in `docs/ARCHITECTURE_OVERVIEW.md`.
