package com.example.interpreter

import com.example.interpreter.lexer.Lexer
import com.example.interpreter.lexer.SourceLocation
import com.example.interpreter.lexer.TokenType
import com.example.interpreter.runtime.SyntaxException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LexerTest {
    private val lexer = Lexer()

    @Test
    fun `tokenizes keywords identifiers operators and punctuation`() {
        val tokens = lexer.tokenize(
            """
            fun add(a, b) { return a + b }
            if true then x = 1 else x = 2
            while x <= 10 do x = x * 2
            flag = x >= 8 != false == true
            value = x < 12 > 3 - 1
            """.trimIndent(),
        )

        assertEquals(
            listOf(
                TokenType.FUN,
                TokenType.IDENTIFIER,
                TokenType.LEFT_PAREN,
                TokenType.IDENTIFIER,
                TokenType.COMMA,
                TokenType.IDENTIFIER,
                TokenType.RIGHT_PAREN,
                TokenType.LEFT_BRACE,
                TokenType.RETURN,
                TokenType.IDENTIFIER,
                TokenType.PLUS,
                TokenType.IDENTIFIER,
                TokenType.RIGHT_BRACE,
                TokenType.NEWLINE,
                TokenType.IF,
                TokenType.TRUE,
                TokenType.THEN,
                TokenType.IDENTIFIER,
                TokenType.EQUAL,
                TokenType.INTEGER,
                TokenType.ELSE,
                TokenType.IDENTIFIER,
                TokenType.EQUAL,
                TokenType.INTEGER,
                TokenType.NEWLINE,
                TokenType.WHILE,
                TokenType.IDENTIFIER,
                TokenType.LESS_THAN_OR_EQUAL,
                TokenType.INTEGER,
                TokenType.DO,
                TokenType.IDENTIFIER,
                TokenType.EQUAL,
                TokenType.IDENTIFIER,
                TokenType.STAR,
                TokenType.INTEGER,
                TokenType.NEWLINE,
                TokenType.IDENTIFIER,
                TokenType.EQUAL,
                TokenType.IDENTIFIER,
                TokenType.GREATER_THAN_OR_EQUAL,
                TokenType.INTEGER,
                TokenType.BANG_EQUAL,
                TokenType.FALSE,
                TokenType.EQUAL_EQUAL,
                TokenType.TRUE,
                TokenType.NEWLINE,
                TokenType.IDENTIFIER,
                TokenType.EQUAL,
                TokenType.IDENTIFIER,
                TokenType.LESS_THAN,
                TokenType.INTEGER,
                TokenType.GREATER_THAN,
                TokenType.INTEGER,
                TokenType.MINUS,
                TokenType.INTEGER,
                TokenType.EOF,
            ),
            tokens.map { it.type },
        )
        assertEquals("add", tokens[1].lexeme)
        assertEquals(SourceLocation(line = 1, column = 5, index = 4), tokens[1].location)
    }

    @Test
    fun `tracks source locations across LF and CRLF newlines`() {
        val tokens = lexer.tokenize("x = 1\r\ny = 2\nz = 3")

        assertEquals(SourceLocation(line = 1, column = 1, index = 0), tokens[0].location)
        assertEquals(SourceLocation(line = 1, column = 6, index = 5), tokens[3].location)
        assertEquals(SourceLocation(line = 2, column = 1, index = 7), tokens[4].location)
        assertEquals(SourceLocation(line = 2, column = 6, index = 12), tokens[7].location)
        assertEquals(SourceLocation(line = 3, column = 1, index = 13), tokens[8].location)
        assertEquals(SourceLocation(line = 3, column = 6, index = 18), tokens.last().location)
    }

    @Test
    fun `rejects bang without equals at its location`() {
        val exception = assertFailsWith<SyntaxException> {
            lexer.tokenize("x = !true")
        }

        assertEquals("Unexpected character '!'", exception.description)
        assertEquals(SourceLocation(line = 1, column = 5, index = 4), exception.location)
    }

    @Test
    fun `rejects unexpected characters at their location`() {
        val exception = assertFailsWith<SyntaxException> {
            lexer.tokenize("x = 1 # comment")
        }

        assertEquals("Unexpected character '#'", exception.description)
        assertEquals(SourceLocation(line = 1, column = 7, index = 6), exception.location)
    }

    @Test
    fun `rejects integer literals outside the supported range`() {
        val exception = assertFailsWith<SyntaxException> {
            lexer.tokenize("x = 2147483648")
        }

        assertEquals("Integer literal is out of range", exception.description)
        assertEquals(SourceLocation(line = 1, column = 5, index = 4), exception.location)
    }
}
