package com.example.interpreter.parser

import com.example.interpreter.ast.Program
import com.example.interpreter.lexer.Lexer
import com.example.interpreter.lexer.Token

class Parser(
    private val lexer: Lexer = Lexer(),
) {
    fun parse(source: String): Program {
        val tokens = lexer.tokenize(source)

        return parseTokens(tokens)
    }

    fun parseTokens(tokens: List<Token>): Program {
        TODO("Parse tokens into the AST described in docs/LANGUAGE_DOCUMENTATION.md")
    }
}
