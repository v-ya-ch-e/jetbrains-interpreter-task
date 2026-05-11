package com.example.interpreter

import com.example.interpreter.ast.AssignmentStatement
import com.example.interpreter.ast.BinaryExpression
import com.example.interpreter.ast.BinaryOperator
import com.example.interpreter.ast.FunctionCall
import com.example.interpreter.ast.FunctionDefinition
import com.example.interpreter.ast.IfStatement
import com.example.interpreter.ast.IntegerLiteral
import com.example.interpreter.ast.Program
import com.example.interpreter.ast.ReturnStatement
import com.example.interpreter.ast.StatementSequence
import com.example.interpreter.ast.TopLevelStatement
import com.example.interpreter.ast.UnaryExpression
import com.example.interpreter.ast.UnaryOperator
import com.example.interpreter.ast.VariableReference
import com.example.interpreter.ast.WhileStatement
import com.example.interpreter.parser.Parser
import com.example.interpreter.runtime.SyntaxException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ParserTest {
    private val parser = Parser()

    @Test
    fun `preserves arithmetic precedence and left associativity in AST`() {
        val program = parser.parse("result = 1 + 2 * 3 - -foo(4, bar)")

        assertEquals(
            Program(
                listOf(
                    TopLevelStatement(
                        AssignmentStatement(
                            "result",
                            BinaryExpression(
                                BinaryExpression(
                                    IntegerLiteral(1),
                                    BinaryOperator.ADD,
                                    BinaryExpression(
                                        IntegerLiteral(2),
                                        BinaryOperator.MULTIPLY,
                                        IntegerLiteral(3),
                                    ),
                                ),
                                BinaryOperator.SUBTRACT,
                                UnaryExpression(
                                    UnaryOperator.NEGATE,
                                    FunctionCall(
                                        "foo",
                                        listOf(IntegerLiteral(4), VariableReference("bar")),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            program,
        )
    }

    @Test
    fun `preserves comparison precedence and left associativity in AST`() {
        val program = parser.parse("flag = a + 1 < b * 2 != c")

        assertEquals(
            Program(
                listOf(
                    TopLevelStatement(
                        AssignmentStatement(
                            "flag",
                            BinaryExpression(
                                BinaryExpression(
                                    BinaryExpression(
                                        VariableReference("a"),
                                        BinaryOperator.ADD,
                                        IntegerLiteral(1),
                                    ),
                                    BinaryOperator.LESS_THAN,
                                    BinaryExpression(
                                        VariableReference("b"),
                                        BinaryOperator.MULTIPLY,
                                        IntegerLiteral(2),
                                    ),
                                ),
                                BinaryOperator.NOT_EQUAL,
                                VariableReference("c"),
                            ),
                        ),
                    ),
                ),
            ),
            program,
        )
    }

    @Test
    fun `parses multiline function bodies into statement sequences`() {
        val program = parser.parse(
            """
            fun choose(a, b) {
                total = a + b
                if total > 0 then return total else return -total
            }
            """.trimIndent(),
        )

        assertEquals(
            Program(
                listOf(
                    FunctionDefinition(
                        "choose",
                        listOf("a", "b"),
                        StatementSequence(
                            listOf(
                                AssignmentStatement(
                                    "total",
                                    BinaryExpression(
                                        VariableReference("a"),
                                        BinaryOperator.ADD,
                                        VariableReference("b"),
                                    ),
                                ),
                                IfStatement(
                                    BinaryExpression(
                                        VariableReference("total"),
                                        BinaryOperator.GREATER_THAN,
                                        IntegerLiteral(0),
                                    ),
                                    ReturnStatement(VariableReference("total")),
                                    ReturnStatement(
                                        UnaryExpression(
                                            UnaryOperator.NEGATE,
                                            VariableReference("total"),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            program,
        )
    }

    @Test
    fun `binds nested else to the nearest if statement`() {
        val program = parser.parse(
            "if outer then if inner then x = 1 else x = 2 else x = 3",
        )

        assertEquals(
            Program(
                listOf(
                    TopLevelStatement(
                        IfStatement(
                            VariableReference("outer"),
                            IfStatement(
                                VariableReference("inner"),
                                AssignmentStatement("x", IntegerLiteral(1)),
                                AssignmentStatement("x", IntegerLiteral(2)),
                            ),
                            AssignmentStatement("x", IntegerLiteral(3)),
                        ),
                    ),
                ),
            ),
            program,
        )
    }

    @Test
    fun `keeps following top-level declarations out of while bodies`() {
        val program = parser.parse(
            """
            while x < 2 do y = y + 1, x = x + 1
            z = y
            """.trimIndent(),
        )

        assertEquals(
            Program(
                listOf(
                    TopLevelStatement(
                        WhileStatement(
                            BinaryExpression(
                                VariableReference("x"),
                                BinaryOperator.LESS_THAN,
                                IntegerLiteral(2),
                            ),
                            StatementSequence(
                                listOf(
                                    AssignmentStatement(
                                        "y",
                                        BinaryExpression(
                                            VariableReference("y"),
                                            BinaryOperator.ADD,
                                            IntegerLiteral(1),
                                        ),
                                    ),
                                    AssignmentStatement(
                                        "x",
                                        BinaryExpression(
                                            VariableReference("x"),
                                            BinaryOperator.ADD,
                                            IntegerLiteral(1),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    TopLevelStatement(AssignmentStatement("z", VariableReference("y"))),
                ),
            ),
            program,
        )
    }

    @Test
    fun `attaches comma sequence after nested if to while body`() {
        val program = parser.parse(
            "while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1",
        )

        val statement = (program.declarations.single() as TopLevelStatement).statement as WhileStatement

        assertEquals(2, statement.body.statements.size)
    }

    @Test
    fun `rejects trailing comma in function statement sequences`() {
        assertFailsWith<SyntaxException> {
            parser.parse("fun bad() { x = 1, }")
        }
    }
}
