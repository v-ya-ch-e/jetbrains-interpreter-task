package com.example.interpreter.lexer

data class Token(
    val type: TokenType,
    val lexeme: String,
    val location: SourceLocation,
)

enum class TokenType {
    IDENTIFIER,
    INTEGER,
    TRUE,
    FALSE,
    FUN,
    RETURN,
    IF,
    THEN,
    ELSE,
    WHILE,
    DO,
    PLUS,
    MINUS,
    STAR,
    EQUAL,
    EQUAL_EQUAL,
    BANG_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACE,
    RIGHT_BRACE,
    COMMA,
    NEWLINE,
    EOF,
}
