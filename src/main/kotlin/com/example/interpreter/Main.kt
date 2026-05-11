package com.example.interpreter

fun main() {
    val source = generateSequence(::readLine).joinToString(separator = "\n")
    val output = Interpreter().run(source)

    if (output.isNotEmpty()) {
        println(output)
    }
}
