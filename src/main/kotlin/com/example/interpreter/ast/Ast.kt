package com.example.interpreter.ast

data class Program(
    val declarations: List<TopLevelDeclaration>,
)

sealed interface TopLevelDeclaration

data class TopLevelStatement(
    val statement: Statement,
) : TopLevelDeclaration

data class FunctionDefinition(
    val name: String,
    val parameters: List<String>,
    val body: StatementSequence,
) : TopLevelDeclaration

data class StatementSequence(
    val statements: List<Statement>,
)

sealed interface Statement

data class AssignmentStatement(
    val name: String,
    val expression: Expression,
) : Statement

data class IfStatement(
    val condition: Expression,
    val thenBranch: Statement,
    val elseBranch: Statement,
) : Statement

data class WhileStatement(
    val condition: Expression,
    val body: StatementSequence,
) : Statement

data class ReturnStatement(
    val expression: Expression,
) : Statement

sealed interface Expression

data class IntegerLiteral(
    val value: Int,
) : Expression

data class BooleanLiteral(
    val value: Boolean,
) : Expression

data class VariableReference(
    val name: String,
) : Expression

data class UnaryExpression(
    val operator: UnaryOperator,
    val expression: Expression,
) : Expression

data class BinaryExpression(
    val left: Expression,
    val operator: BinaryOperator,
    val right: Expression,
) : Expression

data class FunctionCall(
    val name: String,
    val arguments: List<Expression>,
) : Expression

enum class UnaryOperator {
    NEGATE,
}

enum class BinaryOperator {
    ADD,
    SUBTRACT,
    MULTIPLY,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    EQUAL,
    NOT_EQUAL,
}
