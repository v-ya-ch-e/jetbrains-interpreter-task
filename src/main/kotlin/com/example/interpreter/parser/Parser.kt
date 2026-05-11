package com.example.interpreter.parser

import com.example.interpreter.ast.AssignmentStatement
import com.example.interpreter.ast.BinaryExpression
import com.example.interpreter.ast.BinaryOperator
import com.example.interpreter.ast.BooleanLiteral
import com.example.interpreter.ast.Expression
import com.example.interpreter.ast.FunctionCall
import com.example.interpreter.ast.FunctionDefinition
import com.example.interpreter.ast.IfStatement
import com.example.interpreter.ast.IntegerLiteral
import com.example.interpreter.ast.Program
import com.example.interpreter.ast.ReturnStatement
import com.example.interpreter.ast.Statement
import com.example.interpreter.ast.StatementSequence
import com.example.interpreter.ast.TopLevelDeclaration
import com.example.interpreter.ast.TopLevelStatement
import com.example.interpreter.ast.UnaryExpression
import com.example.interpreter.ast.UnaryOperator
import com.example.interpreter.ast.VariableReference
import com.example.interpreter.ast.WhileStatement
import com.example.interpreter.lexer.Lexer
import com.example.interpreter.lexer.Token
import com.example.interpreter.lexer.TokenType
import com.example.interpreter.runtime.SyntaxException

class Parser(
    private val lexer: Lexer = Lexer(),
) {
    fun parse(source: String): Program {
        val tokens = lexer.tokenize(source)

        return parseTokens(tokens)
    }

    fun parseTokens(tokens: List<Token>): Program {
        return TokenParser(tokens).parseProgram()
    }

    private class TokenParser(
        private val tokens: List<Token>,
    ) {
        private var current = 0

        fun parseProgram(): Program {
            val declarations = mutableListOf<TopLevelDeclaration>()

            skipNewlines()
            while (!isAtEnd()) {
                declarations.add(parseTopLevelDeclaration())
                if (!check(TokenType.EOF) && !check(TokenType.NEWLINE)) {
                    throw error("Expected newline after top-level declaration")
                }
                skipNewlines()
            }

            return Program(declarations)
        }

        private fun parseTopLevelDeclaration(): TopLevelDeclaration =
            if (match(TokenType.FUN)) {
                parseFunctionDefinition()
            } else {
                TopLevelStatement(parseStatement())
            }

        private fun parseFunctionDefinition(): FunctionDefinition {
            val name = consume(TokenType.IDENTIFIER, "Expected function name").lexeme
            consume(TokenType.LEFT_PAREN, "Expected '(' after function name")
            val parameters = mutableListOf<String>()
            if (!check(TokenType.RIGHT_PAREN)) {
                do {
                    parameters.add(consume(TokenType.IDENTIFIER, "Expected parameter name").lexeme)
                } while (match(TokenType.COMMA))
            }
            consume(TokenType.RIGHT_PAREN, "Expected ')' after function parameters")
            consume(TokenType.LEFT_BRACE, "Expected '{' before function body")
            val body = parseStatementSequence(
                stopTypes = setOf(TokenType.RIGHT_BRACE, TokenType.EOF),
                allowNewlines = true,
            )
            consume(TokenType.RIGHT_BRACE, "Expected '}' after function body")
            return FunctionDefinition(name, parameters, body)
        }

        private fun parseStatementSequence(
            stopTypes: Set<TokenType>,
            allowNewlines: Boolean,
        ): StatementSequence {
            val statements = mutableListOf<Statement>()

            if (allowNewlines) {
                skipNewlines()
            }

            loop@ while (!isAtEnd() && !checkAny(stopTypes)) {
                statements.add(parseStatement())
                when {
                    match(TokenType.COMMA) -> {
                        if (allowNewlines) {
                            skipNewlines()
                        }
                        if (checkAny(stopTypes)) {
                            throw error("Expected statement after ','")
                        }
                    }
                    allowNewlines && match(TokenType.NEWLINE) -> skipNewlines()
                    else -> break@loop
                }
            }

            return StatementSequence(statements)
        }

        private fun parseStatement(): Statement =
            when {
                match(TokenType.IF) -> parseIfStatement()
                match(TokenType.WHILE) -> parseWhileStatement()
                match(TokenType.RETURN) -> ReturnStatement(parseExpression())
                else -> parseAssignment()
            }

        private fun parseIfStatement(): IfStatement {
            val condition = parseExpression()
            consume(TokenType.THEN, "Expected 'then' after if condition")
            val thenBranch = parseStatement()
            consume(TokenType.ELSE, "Expected 'else' after then branch")
            val elseBranch = parseStatement()
            return IfStatement(condition, thenBranch, elseBranch)
        }

        private fun parseWhileStatement(): WhileStatement {
            val condition = parseExpression()
            consume(TokenType.DO, "Expected 'do' after while condition")
            val body = parseStatementSequence(
                stopTypes = setOf(TokenType.NEWLINE, TokenType.RIGHT_BRACE, TokenType.EOF),
                allowNewlines = false,
            )
            if (body.statements.isEmpty()) {
                throw error("Expected statement after 'do'")
            }
            return WhileStatement(condition, body)
        }

        private fun parseAssignment(): AssignmentStatement {
            val name = consume(TokenType.IDENTIFIER, "Expected statement").lexeme
            consume(TokenType.EQUAL, "Expected '=' after assignment target")
            return AssignmentStatement(name, parseExpression())
        }

        private fun parseExpression(): Expression = parseComparison()

        private fun parseComparison(): Expression {
            var expression = parseAddition()
            while (
                match(
                    TokenType.LESS_THAN,
                    TokenType.LESS_THAN_OR_EQUAL,
                    TokenType.GREATER_THAN,
                    TokenType.GREATER_THAN_OR_EQUAL,
                    TokenType.EQUAL_EQUAL,
                    TokenType.BANG_EQUAL,
                )
            ) {
                val operator = when (previous().type) {
                    TokenType.LESS_THAN -> BinaryOperator.LESS_THAN
                    TokenType.LESS_THAN_OR_EQUAL -> BinaryOperator.LESS_THAN_OR_EQUAL
                    TokenType.GREATER_THAN -> BinaryOperator.GREATER_THAN
                    TokenType.GREATER_THAN_OR_EQUAL -> BinaryOperator.GREATER_THAN_OR_EQUAL
                    TokenType.EQUAL_EQUAL -> BinaryOperator.EQUAL
                    TokenType.BANG_EQUAL -> BinaryOperator.NOT_EQUAL
                    else -> throw error("Unexpected comparison operator")
                }
                expression = BinaryExpression(expression, operator, parseAddition())
            }
            return expression
        }

        private fun parseAddition(): Expression {
            var expression = parseMultiplication()
            while (match(TokenType.PLUS, TokenType.MINUS)) {
                val operator = when (previous().type) {
                    TokenType.PLUS -> BinaryOperator.ADD
                    TokenType.MINUS -> BinaryOperator.SUBTRACT
                    else -> throw error("Unexpected additive operator")
                }
                expression = BinaryExpression(expression, operator, parseMultiplication())
            }
            return expression
        }

        private fun parseMultiplication(): Expression {
            var expression = parseUnary()
            while (match(TokenType.STAR)) {
                expression = BinaryExpression(expression, BinaryOperator.MULTIPLY, parseUnary())
            }
            return expression
        }

        private fun parseUnary(): Expression =
            if (match(TokenType.MINUS)) {
                UnaryExpression(UnaryOperator.NEGATE, parseUnary())
            } else {
                parsePrimary()
            }

        private fun parsePrimary(): Expression =
            when {
                match(TokenType.INTEGER) -> IntegerLiteral(previous().lexeme.toInt())
                match(TokenType.TRUE) -> BooleanLiteral(true)
                match(TokenType.FALSE) -> BooleanLiteral(false)
                match(TokenType.IDENTIFIER) -> {
                    val name = previous().lexeme
                    if (match(TokenType.LEFT_PAREN)) {
                        parseFunctionCall(name)
                    } else {
                        VariableReference(name)
                    }
                }
                match(TokenType.LEFT_PAREN) -> {
                    val expression = parseExpression()
                    consume(TokenType.RIGHT_PAREN, "Expected ')' after expression")
                    expression
                }
                else -> throw error("Expected expression")
            }

        private fun parseFunctionCall(name: String): FunctionCall {
            val arguments = mutableListOf<Expression>()
            if (!check(TokenType.RIGHT_PAREN)) {
                do {
                    arguments.add(parseExpression())
                } while (match(TokenType.COMMA))
            }
            consume(TokenType.RIGHT_PAREN, "Expected ')' after function arguments")
            return FunctionCall(name, arguments)
        }

        private fun match(vararg types: TokenType): Boolean {
            for (type in types) {
                if (check(type)) {
                    advance()
                    return true
                }
            }
            return false
        }

        private fun consume(type: TokenType, message: String): Token {
            if (check(type)) {
                return advance()
            }
            throw error(message)
        }

        private fun check(type: TokenType): Boolean =
            peek().type == type

        private fun checkAny(types: Set<TokenType>): Boolean =
            types.any(::check)

        private fun advance(): Token {
            if (!isAtEnd()) {
                current += 1
            }
            return previous()
        }

        private fun skipNewlines() {
            while (match(TokenType.NEWLINE)) {
                // Keep consuming blank lines.
            }
        }

        private fun isAtEnd(): Boolean =
            peek().type == TokenType.EOF

        private fun peek(): Token =
            tokens[current]

        private fun previous(): Token =
            tokens[current - 1]

        private fun error(message: String): SyntaxException =
            SyntaxException(message, peek().location)
    }
}
