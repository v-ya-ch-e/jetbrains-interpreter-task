package com.example.interpreter.eval

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
import com.example.interpreter.ast.TopLevelStatement
import com.example.interpreter.ast.UnaryExpression
import com.example.interpreter.ast.UnaryOperator
import com.example.interpreter.ast.VariableReference
import com.example.interpreter.ast.WhileStatement
import com.example.interpreter.runtime.BooleanValue
import com.example.interpreter.runtime.CallFrame
import com.example.interpreter.runtime.EvaluationException
import com.example.interpreter.runtime.ExecutionResult
import com.example.interpreter.runtime.IntValue
import com.example.interpreter.runtime.RuntimeState
import com.example.interpreter.runtime.Value

class Evaluator {
    fun execute(program: Program): ExecutionResult {
        val state = RuntimeState()

        for (declaration in program.declarations) {
            when (declaration) {
                is FunctionDefinition -> state.functions[declaration.name] = declaration
                is TopLevelStatement -> {
                    val returned = executeStatement(declaration.statement, state, frame = null)
                    if (returned != null) {
                        throw EvaluationException("Cannot return from top-level code")
                    }
                }
            }
        }

        return ExecutionResult(LinkedHashMap(state.globalVariables))
    }

    private fun executeStatement(
        statement: Statement,
        state: RuntimeState,
        frame: CallFrame?,
    ): Value? {
        return when (statement) {
            is AssignmentStatement -> {
                val value = evaluateExpression(statement.expression, state, frame)
                if (frame == null) {
                    state.globalVariables[statement.name] = value
                } else {
                    frame.localVariables[statement.name] = value
                }
                null
            }
            is IfStatement -> {
                val condition = requireBoolean(evaluateExpression(statement.condition, state, frame), "if condition")
                val branch = if (condition.value) statement.thenBranch else statement.elseBranch
                executeStatement(branch, state, frame)
            }
            is WhileStatement -> {
                while (requireBoolean(evaluateExpression(statement.condition, state, frame), "while condition").value) {
                    val returned = executeSequence(statement.body, state, frame)
                    if (returned != null) {
                        return returned
                    }
                }
                null
            }
            is ReturnStatement -> {
                if (frame == null) {
                    throw EvaluationException("Cannot return from top-level code")
                }
                evaluateExpression(statement.expression, state, frame)
            }
        }
    }

    private fun executeSequence(
        sequence: StatementSequence,
        state: RuntimeState,
        frame: CallFrame?,
    ): Value? {
        for (statement in sequence.statements) {
            val returned = executeStatement(statement, state, frame)
            if (returned != null) {
                return returned
            }
        }
        return null
    }

    private fun evaluateExpression(
        expression: Expression,
        state: RuntimeState,
        frame: CallFrame?,
    ): Value {
        return when (expression) {
            is IntegerLiteral -> IntValue(expression.value)
            is BooleanLiteral -> BooleanValue(expression.value)
            is VariableReference -> resolveVariable(expression.name, state, frame)
            is UnaryExpression -> evaluateUnary(expression, state, frame)
            is BinaryExpression -> evaluateBinary(expression, state, frame)
            is FunctionCall -> evaluateFunctionCall(expression, state, frame)
        }
    }

    private fun resolveVariable(
        name: String,
        state: RuntimeState,
        frame: CallFrame?,
    ): Value {
        if (frame != null && frame.localVariables.containsKey(name)) {
            return frame.localVariables.getValue(name)
        }
        return state.globalVariables[name]
            ?: throw EvaluationException("Undefined variable '$name'")
    }

    private fun evaluateUnary(
        expression: UnaryExpression,
        state: RuntimeState,
        frame: CallFrame?,
    ): Value {
        val value = evaluateExpression(expression.expression, state, frame)
        return when (expression.operator) {
            UnaryOperator.NEGATE -> IntValue(-requireInt(value, "unary '-'").value)
        }
    }

    private fun evaluateBinary(
        expression: BinaryExpression,
        state: RuntimeState,
        frame: CallFrame?,
    ): Value {
        val left = evaluateExpression(expression.left, state, frame)
        val right = evaluateExpression(expression.right, state, frame)

        return when (expression.operator) {
            BinaryOperator.ADD -> IntValue(
                requireInt(left, "'+' left operand").value + requireInt(right, "'+' right operand").value,
            )
            BinaryOperator.SUBTRACT -> IntValue(
                requireInt(left, "'-' left operand").value - requireInt(right, "'-' right operand").value,
            )
            BinaryOperator.MULTIPLY -> IntValue(
                requireInt(left, "'*' left operand").value * requireInt(right, "'*' right operand").value,
            )
            BinaryOperator.LESS_THAN -> BooleanValue(
                requireInt(left, "'<' left operand").value < requireInt(right, "'<' right operand").value,
            )
            BinaryOperator.LESS_THAN_OR_EQUAL -> BooleanValue(
                requireInt(left, "'<=' left operand").value <= requireInt(right, "'<=' right operand").value,
            )
            BinaryOperator.GREATER_THAN -> BooleanValue(
                requireInt(left, "'>' left operand").value > requireInt(right, "'>' right operand").value,
            )
            BinaryOperator.GREATER_THAN_OR_EQUAL -> BooleanValue(
                requireInt(left, "'>=' left operand").value >= requireInt(right, "'>=' right operand").value,
            )
            BinaryOperator.EQUAL -> BooleanValue(
                requireInt(left, "'==' left operand").value == requireInt(right, "'==' right operand").value,
            )
            BinaryOperator.NOT_EQUAL -> BooleanValue(
                requireInt(left, "'!=' left operand").value != requireInt(right, "'!=' right operand").value,
            )
        }
    }

    private fun evaluateFunctionCall(
        expression: FunctionCall,
        state: RuntimeState,
        frame: CallFrame?,
    ): Value {
        val function = state.functions[expression.name]
            ?: throw EvaluationException("Undefined function '${expression.name}'")
        if (function.parameters.size != expression.arguments.size) {
            throw EvaluationException(
                "Function '${expression.name}' expects ${function.parameters.size} arguments, " +
                    "got ${expression.arguments.size}",
            )
        }

        val arguments = expression.arguments.map { evaluateExpression(it, state, frame) }
        val parameters = function.parameters.zip(arguments).toMap()
        val returned = executeSequence(function.body, state, CallFrame(parameters))

        return returned
            ?: throw EvaluationException("Function '${expression.name}' completed without return")
    }

    private fun requireInt(value: Value, context: String): IntValue =
        value as? IntValue
            ?: throw EvaluationException("Expected integer value for $context")

    private fun requireBoolean(value: Value, context: String): BooleanValue =
        value as? BooleanValue
            ?: throw EvaluationException("Expected boolean value for $context")
}
