package com.example.interpreter

import com.example.interpreter.runtime.BooleanValue
import com.example.interpreter.runtime.ExecutionResult
import com.example.interpreter.runtime.IntValue
import com.example.interpreter.runtime.OutputFormatter
import kotlin.test.Test
import kotlin.test.assertEquals

class OutputFormatterTest {
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
            OutputFormatter().format(result),
        )
    }
}
