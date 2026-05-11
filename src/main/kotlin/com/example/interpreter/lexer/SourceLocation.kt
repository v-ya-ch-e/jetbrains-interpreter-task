package com.example.interpreter.lexer

data class SourceLocation(
    val line: Int,
    val column: Int,
    val index: Int,
) {
    override fun toString(): String = "$line:$column"
}
