package com.example.interpreter.lexer

import com.example.interpreter.runtime.SyntaxException

class Lexer {
    fun tokenize(source: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var index = 0
        var line = 1
        var column = 1

        fun location() = SourceLocation(line, column, index)

        fun advance(): Char {
            val char = source[index]
            index += 1
            column += 1
            return char
        }

        fun add(type: TokenType, lexeme: String, start: SourceLocation) {
            tokens.add(Token(type, lexeme, start))
        }

        fun addNewline(start: SourceLocation) {
            tokens.add(Token(TokenType.NEWLINE, "\n", start))
            line += 1
            column = 1
        }

        while (index < source.length) {
            val start = location()
            when (val char = advance()) {
                ' ', '\t' -> Unit
                '\n' -> addNewline(start)
                '\r' -> {
                    if (index < source.length && source[index] == '\n') {
                        index += 1
                    }
                    addNewline(start)
                }
                '+' -> add(TokenType.PLUS, "+", start)
                '-' -> add(TokenType.MINUS, "-", start)
                '*' -> add(TokenType.STAR, "*", start)
                '(' -> add(TokenType.LEFT_PAREN, "(", start)
                ')' -> add(TokenType.RIGHT_PAREN, ")", start)
                '{' -> add(TokenType.LEFT_BRACE, "{", start)
                '}' -> add(TokenType.RIGHT_BRACE, "}", start)
                ',' -> add(TokenType.COMMA, ",", start)
                '=' -> {
                    if (index < source.length && source[index] == '=') {
                        index += 1
                        column += 1
                        add(TokenType.EQUAL_EQUAL, "==", start)
                    } else {
                        add(TokenType.EQUAL, "=", start)
                    }
                }
                '!' -> {
                    if (index < source.length && source[index] == '=') {
                        index += 1
                        column += 1
                        add(TokenType.BANG_EQUAL, "!=", start)
                    } else {
                        throw SyntaxException("Unexpected character '!'", start)
                    }
                }
                '<' -> {
                    if (index < source.length && source[index] == '=') {
                        index += 1
                        column += 1
                        add(TokenType.LESS_THAN_OR_EQUAL, "<=", start)
                    } else {
                        add(TokenType.LESS_THAN, "<", start)
                    }
                }
                '>' -> {
                    if (index < source.length && source[index] == '=') {
                        index += 1
                        column += 1
                        add(TokenType.GREATER_THAN_OR_EQUAL, ">=", start)
                    } else {
                        add(TokenType.GREATER_THAN, ">", start)
                    }
                }
                else -> {
                    when {
                        char.isDigit() -> {
                            while (index < source.length && source[index].isDigit()) {
                                advance()
                            }
                            val lexeme = source.substring(start.index, index)
                            lexeme.toIntOrNull()
                                ?: throw SyntaxException("Integer literal is out of range", start)
                            add(TokenType.INTEGER, lexeme, start)
                        }
                        char.isLetter() || char == '_' -> {
                            while (
                                index < source.length &&
                                (source[index].isLetterOrDigit() || source[index] == '_')
                            ) {
                                advance()
                            }
                            val lexeme = source.substring(start.index, index)
                            add(keywordType(lexeme), lexeme, start)
                        }
                        else -> throw SyntaxException("Unexpected character '$char'", start)
                    }
                }
            }
        }

        tokens.add(Token(TokenType.EOF, "", SourceLocation(line, column, index)))
        return tokens
    }

    private fun keywordType(lexeme: String): TokenType =
        when (lexeme) {
            "fun" -> TokenType.FUN
            "return" -> TokenType.RETURN
            "if" -> TokenType.IF
            "then" -> TokenType.THEN
            "else" -> TokenType.ELSE
            "while" -> TokenType.WHILE
            "do" -> TokenType.DO
            "true" -> TokenType.TRUE
            "false" -> TokenType.FALSE
            else -> TokenType.IDENTIFIER
        }
}
