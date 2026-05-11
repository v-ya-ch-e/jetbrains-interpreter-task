# Running And Testing

This page summarizes the commands a reviewer can use to build, test, and run
the interpreter.

## Requirements

- JDK 17 or newer.
- Maven 3.x available as `mvn`.
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
./run file path/to/program.txt
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
Use the up and down arrow keys to restore commands entered earlier in the same
REPL session.

## Direct Maven Commands

The helper delegates to these Maven commands:

```bash
mvn test
mvn clean package
printf 'x = 2\ny = (x + 2) * 2\n' | mvn -q exec:java
mvn -q exec:java -Dexec.args="path/to/program.txt"
mvn -q exec:java -Dexec.args="--repl"
mvn -q exec:java -Dexec.args="--help"
```

## Exit Behavior

- Successful batch execution exits with code `0`.
- Language and file errors in batch mode are printed to standard error and exit
  with code `1`.
- Invalid CLI usage exits with code `2`.
- REPL language errors are printed to standard error without ending the session.
