package com.example.interpreter

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
import com.example.interpreter.eval.Evaluator
import com.example.interpreter.runtime.EvaluationException
import com.example.interpreter.runtime.IntValue
import com.example.interpreter.runtime.Value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EvaluatorTest {
    private val evaluator = Evaluator()

    @Test
    fun `executes assignments conditionals and loops preserving global assignment order`() {
        val program = programOf(
            assignTopLevel("x", int(0)),
            assignTopLevel("y", int(0)),
            topLevel(
                WhileStatement(
                    binary(ref("x"), BinaryOperator.LESS_THAN, int(3)),
                    StatementSequence(
                        listOf(
                            IfStatement(
                                binary(ref("x"), BinaryOperator.EQUAL, int(1)),
                                assign("y", int(10)),
                                assign("y", binary(ref("y"), BinaryOperator.ADD, int(1))),
                            ),
                            assign("x", binary(ref("x"), BinaryOperator.ADD, int(1))),
                        ),
                    ),
                ),
            ),
        )

        assertEquals(
            linkedMapOf<String, Value>(
                "x" to IntValue(3),
                "y" to IntValue(11),
            ),
            evaluator.execute(program).globalVariables,
        )
    }

    @Test
    fun `evaluates recursive functions and omits local variables from result`() {
        val program = programOf(
            FunctionDefinition(
                "fact",
                listOf("n"),
                StatementSequence(
                    listOf(
                        IfStatement(
                            binary(ref("n"), BinaryOperator.LESS_THAN_OR_EQUAL, int(0)),
                            ReturnStatement(int(1)),
                            ReturnStatement(
                                binary(
                                    ref("n"),
                                    BinaryOperator.MULTIPLY,
                                    FunctionCall(
                                        "fact",
                                        listOf(binary(ref("n"), BinaryOperator.SUBTRACT, int(1))),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            assignTopLevel("result", FunctionCall("fact", listOf(int(5)))),
        )

        assertEquals(
            linkedMapOf<String, Value>("result" to IntValue(120)),
            evaluator.execute(program).globalVariables,
        )
    }

    @Test
    fun `resolves globals from functions but writes assignments to local scope`() {
        val program = programOf(
            assignTopLevel("offset", int(3)),
            FunctionDefinition(
                "addOffset",
                listOf("value"),
                StatementSequence(
                    listOf(
                        assign("offset", binary(ref("value"), BinaryOperator.ADD, ref("offset"))),
                        ReturnStatement(ref("offset")),
                    ),
                ),
            ),
            assignTopLevel("result", FunctionCall("addOffset", listOf(int(4)))),
        )

        assertEquals(
            linkedMapOf<String, Value>(
                "offset" to IntValue(3),
                "result" to IntValue(7),
            ),
            evaluator.execute(program).globalVariables,
        )
    }

    @Test
    fun `propagates return values through while bodies`() {
        val program = programOf(
            FunctionDefinition(
                "firstAtLeast",
                listOf("limit"),
                StatementSequence(
                    listOf(
                        assign("current", int(0)),
                        WhileStatement(
                            BooleanLiteral(true),
                            StatementSequence(
                                listOf(
                                    IfStatement(
                                        binary(
                                            ref("current"),
                                            BinaryOperator.GREATER_THAN_OR_EQUAL,
                                            ref("limit"),
                                        ),
                                        ReturnStatement(ref("current")),
                                        assign("current", binary(ref("current"), BinaryOperator.ADD, int(1))),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            assignTopLevel("answer", FunctionCall("firstAtLeast", listOf(int(4)))),
        )

        assertEquals(
            linkedMapOf<String, Value>("answer" to IntValue(4)),
            evaluator.execute(program).globalVariables,
        )
    }

    @Test
    fun `rejects invalid control flow and function calls`() {
        assertFailsWith<EvaluationException> {
            evaluator.execute(programOf(topLevel(ReturnStatement(int(1)))))
        }

        assertFailsWith<EvaluationException> {
            evaluator.execute(
                programOf(
                    FunctionDefinition("missingReturn", emptyList(), StatementSequence(emptyList())),
                    assignTopLevel("x", FunctionCall("missingReturn", emptyList())),
                ),
            )
        }

        assertFailsWith<EvaluationException> {
            evaluator.execute(
                programOf(
                    FunctionDefinition(
                        "one",
                        listOf("value"),
                        StatementSequence(listOf(ReturnStatement(ref("value")))),
                    ),
                    assignTopLevel("x", FunctionCall("one", emptyList())),
                ),
            )
        }
    }

    @Test
    fun `rejects invalid runtime values`() {
        assertFailsWith<EvaluationException> {
            evaluator.execute(programOf(assignTopLevel("x", ref("missing"))))
        }

        assertFailsWith<EvaluationException> {
            evaluator.execute(programOf(assignTopLevel("x", FunctionCall("missing", emptyList()))))
        }

        assertFailsWith<EvaluationException> {
            evaluator.execute(
                programOf(
                    assignTopLevel(
                        "x",
                        UnaryExpression(UnaryOperator.NEGATE, BooleanLiteral(true)),
                    ),
                ),
            )
        }

        assertFailsWith<EvaluationException> {
            evaluator.execute(
                programOf(
                    topLevel(
                        IfStatement(
                            int(1),
                            assign("x", int(1)),
                            assign("x", int(0)),
                        ),
                    ),
                ),
            )
        }
    }

    private fun programOf(vararg declarations: TopLevelDeclaration): Program =
        Program(declarations.toList())

    private fun topLevel(statement: Statement): TopLevelStatement =
        TopLevelStatement(statement)

    private fun assignTopLevel(name: String, expression: Expression): TopLevelStatement =
        topLevel(assign(name, expression))

    private fun assign(name: String, expression: Expression): AssignmentStatement =
        AssignmentStatement(name, expression)

    private fun int(value: Int): IntegerLiteral =
        IntegerLiteral(value)

    private fun ref(name: String): VariableReference =
        VariableReference(name)

    private fun binary(
        left: Expression,
        operator: BinaryOperator,
        right: Expression,
    ): BinaryExpression =
        BinaryExpression(left, operator, right)
}
