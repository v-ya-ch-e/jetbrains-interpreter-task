package com.example.interpreter

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MainTest {
    @Test
    fun `runs source from standard input`() {
        val result = runCli(
            input = """
                x = 2
                y = (x + 2) * 2
            """.trimIndent(),
        )

        assertEquals(0, result.exitCode)
        assertEquals(
            "x: 2\ny: 8\n",
            result.stdout,
        )
        assertEquals("", result.stderr)
    }

    @Test
    fun `runs source from a file argument`() {
        val sourceFile = Files.createTempFile("interpreter-main-test", ".txt")

        try {
            sourceFile.writeText("value = 40 + 2")

            val result = runCli(args = arrayOf(sourceFile.toString()))

            assertEquals(0, result.exitCode)
            assertEquals("value: 42\n", result.stdout)
            assertEquals("", result.stderr)
        } finally {
            Files.deleteIfExists(sourceFile)
        }
    }

    @Test
    fun `prints language errors to standard error`() {
        val result = runCli(input = "value = missing")

        assertEquals(1, result.exitCode)
        assertEquals("", result.stdout)
        assertTrue(result.stderr.startsWith("Error: Undefined variable 'missing'"))
    }

    @Test
    fun `prints usage for help and invalid argument counts`() {
        val helpResult = runCli(args = arrayOf("--help"))
        val invalidResult = runCli(args = arrayOf("first.txt", "second.txt"))

        assertEquals(0, helpResult.exitCode)
        assertTrue(helpResult.stdout.contains("Usage: interpreter [source-file]"))
        assertEquals("", helpResult.stderr)

        assertEquals(2, invalidResult.exitCode)
        assertEquals("", invalidResult.stdout)
        assertTrue(invalidResult.stderr.contains("Expected zero or one source file argument."))
        assertTrue(invalidResult.stderr.contains("Usage: interpreter [source-file]"))
    }

    private fun runCli(
        args: Array<String> = emptyArray(),
        input: String = "",
    ): CliResult {
        val stdout = ByteArrayOutputStream()
        val stderr = ByteArrayOutputStream()
        val exitCode = InterpreterCli().run(
            args,
            ByteArrayInputStream(input.toByteArray(Charsets.UTF_8)),
            PrintStream(stdout, true, Charsets.UTF_8.name()),
            PrintStream(stderr, true, Charsets.UTF_8.name()),
        )

        return CliResult(
            exitCode = exitCode,
            stdout = stdout.toString(Charsets.UTF_8.name()),
            stderr = stderr.toString(Charsets.UTF_8.name()),
        )
    }

    private data class CliResult(
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
    )
}
