package com.example.interpreter.eval

import com.example.interpreter.ast.Expression
import com.example.interpreter.ast.Program
import com.example.interpreter.ast.Statement
import com.example.interpreter.runtime.ExecutionResult
import com.example.interpreter.runtime.RuntimeState
import com.example.interpreter.runtime.Value

class Evaluator {
    fun execute(program: Program): ExecutionResult {
        TODO("Execute top-level declarations and return final global variables")
    }

    private fun executeStatement(statement: Statement, state: RuntimeState): Value? {
        TODO("Execute assignment, if, while, and return statements")
    }

    private fun evaluateExpression(expression: Expression, state: RuntimeState): Value {
        TODO("Evaluate literals, references, arithmetic, comparisons, and function calls")
    }
}
