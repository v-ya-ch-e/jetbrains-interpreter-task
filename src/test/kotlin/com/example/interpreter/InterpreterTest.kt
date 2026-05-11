package com.example.interpreter

import com.example.interpreter.runtime.EvaluationException
import com.example.interpreter.runtime.SyntaxException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InterpreterTest {
    private val interpreter = Interpreter()

    @Test
    fun `evaluates arithmetic and preserves global assignment order`() {
        val source = """
            x = 2
            y = (x + 2) * 2
            z = -y + 3
        """.trimIndent()

        assertEquals(
            """
            x: 2
            y: 8
            z: -5
            """.trimIndent(),
            interpreter.run(source),
        )
    }

    @Test
    fun `evaluates conditionals`() {
        val source = """
            x = 20
            if x > 10 then y = 100 else y = 0
        """.trimIndent()

        assertEquals(
            """
            x: 20
            y: 100
            """.trimIndent(),
            interpreter.run(source),
        )
    }

    @Test
    fun `evaluates while body sequences after nested if statements`() {
        val source = """
            x = 0
            y = 0
            while x < 3 do if x == 1 then y = 10 else y = y + 1, x = x + 1
        """.trimIndent()

        assertEquals(
            """
            x: 3
            y: 11
            """.trimIndent(),
            interpreter.run(source),
        )
    }

    @Test
    fun `evaluates recursive and iterative functions`() {
        val source = """
            fun fact_rec(n) { if n <= 0 then return 1 else return n * fact_rec(n - 1) }
            fun fact_iter(n) { r = 1, while true do if n == 0 then return r else r = r * n, n = n - 1 }
            a = fact_rec(5)
            b = fact_iter(5)
        """.trimIndent()

        assertEquals(
            """
            a: 120
            b: 120
            """.trimIndent(),
            interpreter.run(source),
        )
    }

    @Test
    fun `keeps function locals out of formatted global output`() {
        val source = """
            offset = 3
            fun add_offset(value) { temp = value + offset, return temp }
            result = add_offset(4)
        """.trimIndent()

        assertEquals(
            """
            offset: 3
            result: 7
            """.trimIndent(),
            interpreter.run(source),
        )
    }

    @Test
    fun `session preserves variables and functions between runs`() {
        val session = interpreter.createSession()

        assertEquals("x: 2", session.run("x = 2"))
        assertEquals("x: 2", session.run("fun inc(value) { return value + 1 }"))
        assertEquals(
            """
            x: 2
            y: 3
            """.trimIndent(),
            session.run("y = inc(x)"),
        )
    }

    @Test
    fun `rejects syntax without top-level line separators`() {
        assertFailsWith<SyntaxException> {
            interpreter.run("x = 1 y = 2")
        }
    }

    @Test
    fun `rejects undefined variables`() {
        assertFailsWith<EvaluationException> {
            interpreter.run("x = missing")
        }
    }

    @Test
    fun `reports function trace for runtime errors inside calls`() {
        val exception = assertFailsWith<EvaluationException> {
            interpreter.run(
                """
                fun inner() { return missing }
                fun outer() { return inner() }
                result = outer()
                """.trimIndent(),
            )
        }

        assertEquals("Undefined variable 'missing'", exception.description)
        assertEquals(listOf("inner", "outer"), exception.trace)
    }
}
