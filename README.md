# JetBrains Interpreter Task

Kotlin/Maven project for the artificial language interpreter described in
`docs/TASK_DESCRIPTION.md` and `docs/LANGUAGE_DOCUMENTATION.md`.

## Build

```bash
mvn test
```

## Run

```bash
mvn exec:java
```

The final interpreter will read the source program from standard input and print
global variables to standard output.

## Current Structure

- `lexer`: converts source text into tokens.
- `parser`: converts tokens into the AST.
- `ast`: immutable representation of programs, statements, and expressions.
- `eval`: executes the AST.
- `runtime`: runtime values, state, errors, and output formatting.

Parser and evaluator behavior is intentionally left as explicit `TODO` blocks.
