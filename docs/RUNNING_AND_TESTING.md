# Running And Testing

This page summarizes the commands a reviewer can use to build, test, and run
the interpreter.

## Requirements

- JDK 17 or newer.
- The included Maven Wrapper (`./mvnw`).
- A POSIX-compatible shell for the optional root `./run` helper.

## Wrapper Commands

The root `./run` helper is the shortest way to exercise the project:

```bash
./run help
./run test
./run package
./run sample
./run repl
./run stdin < path/to/program.txt
./run file examples/factorials.txt
./run cli-help
```

`./run repl` starts a persistent interactive session. See
[REPL Usage](#repl-usage) below for a walkthrough.

`./run stdin` reads a complete program from standard input. This is useful for
pipes and redirected files:

```bash
printf 'x = 2\ny = (x + 2) * 2\n' | ./run stdin
```

Expected output:

```text
x: 2
y: 8
```

## Example Programs

The `examples/` directory contains runnable versions of the original task
samples plus a combined factorial demo:

```bash
./run file examples/arithmetic.txt
./run file examples/conditional.txt
./run file examples/loop.txt
./run file examples/add-function.txt
./run file examples/factorial-recursive.txt
./run file examples/factorial-iterative.txt
./run file examples/factorials.txt
```

## REPL Usage

The REPL lets you run one input at a time while keeping previously assigned
variables and defined functions available. Start it with:

```bash
./run repl
```

Use the prompt like this:

```text
> x = 2
x: 2
> y = x + 3
x: 2
y: 5
> :quit
```

What happens in that session:

- `>` means the interpreter is ready for a new top-level input.
- After each successful input, the REPL prints the current global variables.
- The second input can use `x` because REPL state persists between inputs.
- Enter `:quit` or `:exit` on an empty prompt to stop the session.

Function definitions also persist. A function definition by itself does not
print output because functions are not global variables:

```text
> fun add(a, b) { return a + b }
> result = add(2, 3)
result: 5
> :exit
```

For multi-line function definitions, the `...` prompt means the REPL is waiting
for the closing brace:

```text
> fun double(n) {
... return n * 2
... }
> answer = double(21)
answer: 42
> :quit
```

If an input has a language error, the REPL prints the error and keeps running.
The last successful variables and functions remain available.

## Direct Maven Wrapper Commands

The helper delegates to these Maven commands:

```bash
./mvnw test
./mvnw clean package
printf 'x = 2\ny = (x + 2) * 2\n' | ./mvnw -q exec:java
./mvnw -q exec:java -Dexec.args="examples/factorials.txt"
./mvnw -q exec:java -Dexec.args="--repl"
./mvnw -q exec:java -Dexec.args="--help"
```

## Exit Behavior

- Successful batch execution exits with code `0`.
- Language and file errors in batch mode are printed to standard error and exit
  with code `1`.
- Invalid CLI usage exits with code `2`.
- REPL language errors are printed to standard error without ending the session.
