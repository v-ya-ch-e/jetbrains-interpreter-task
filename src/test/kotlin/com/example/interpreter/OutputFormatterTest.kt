package com.example.interpreter

import com.example.interpreter.runtime.BooleanValue
import com.example.interpreter.runtime.ExecutionResult
import com.example.interpreter.runtime.IntValue
import com.example.interpreter.runtime.OutputFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class OutputFormatterTest {
    private val formatter = OutputFormatter()

    @Test
    fun `formats global variables in insertion order`() {
        val result = ExecutionResult(
            linkedMapOf(
                "x" to IntValue(3),
                "done" to BooleanValue(true),
            ),
        )

        assertEquals(
            """
            x: 3
            done: true
            """.trimIndent(),
            formatter.format(result),
        )
    }

    @Test
    fun `formats an empty result as empty output`() {
        assertEquals(
            "",
            formatter.format(ExecutionResult(linkedMapOf())),
        )
    }

    @Test
    fun `does not append a trailing newline`() {
        val output = formatter.format(
            ExecutionResult(
                linkedMapOf(
                    "first" to IntValue(1),
                    "second" to BooleanValue(false),
                ),
            ),
        )

        assertEquals(
            """
            first: 1
            second: false
            """.trimIndent(),
            output,
        )
        assertFalse(output.endsWith("\n"))
    }
}
