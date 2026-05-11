## Task 1

Create an interpreter for an artificial programming language. The interpreter reads a source program from standard input, executes it, and prints the values of all variables to standard output.

There is no formal specification. You are given only several sample programs:

```
  stdin:
    x = 2
    y = (x + 2) * 2

  stdout:
    x: 2
    y: 8


  stdin:
    x = 20
    if x > 10 then y = 100 else y = 0

  stdout:
    x: 20
    y: 100


  stdin:
    x = 0
    y = 0
    while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1

  stdout:
    x: 3
    y: 11


  stdin:
    fun add(a, b) { return a + b }
    four = add( 2, 2)

  stdout:
    four: 4


  stdin:
    fun fact_rec(n) { if n <= 0 then return 1 else return n*fact_rec(n-1) }
    a = fact_rec(5)

  stdout:
    a: 120


  stdin:
    fun fact_iter(n) { r = 1, while true do if n == 0 then return r else r = r * n, n = n - 1 }
    b = fact_iter(5)

  stdout:
    b: 120
```

The goal is not to implement a minimal working prototype. You are expected to deliver a mini-project in Java or Kotlin that can be easily built and run on our side.

Any missing details of the language syntax or semantics should be reasonably resolved at your discretion.
