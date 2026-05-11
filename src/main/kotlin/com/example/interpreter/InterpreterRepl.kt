package com.example.interpreter

import com.example.interpreter.runtime.LanguageException
import java.io.InputStream
import java.io.PrintStream

class InterpreterRepl(
    private val session: InterpreterSession = Interpreter().createSession(),
    private val primaryPrompt: String = "> ",
    private val continuationPrompt: String = "... ",
) {
    fun run(
        input: InputStream,
        output: PrintStream,
        error: PrintStream,
    ): Int {
        val reader = input.bufferedReader()
        val buffer = StringBuilder()

        while (true) {
            output.print(if (buffer.length == 0) primaryPrompt else continuationPrompt)
            output.flush()

            val line = reader.readLine() ?: break
            if (buffer.length == 0 && line.trim() in EXIT_COMMANDS) {
                break
            }
            if (buffer.length == 0 && line.isBlank()) {
                continue
            }

            if (buffer.isNotEmpty()) {
                buffer.append('\n')
            }
            buffer.append(line)

            if (hasUnclosedBraces(buffer.toString())) {
                continue
            }

            execute(buffer.toString(), output, error)
            buffer.setLength(0)
        }

        if (buffer.toString().isNotBlank()) {
            execute(buffer.toString(), output, error)
        }

        return 0
    }

    private fun execute(
        source: String,
        output: PrintStream,
        error: PrintStream,
    ) {
        try {
            val formattedResult = session.run(source)
            if (formattedResult.isNotEmpty()) {
                output.println(formattedResult)
            }
        } catch (exception: LanguageException) {
            error.println("Error: ${exception.message}")
        }
    }

    private fun hasUnclosedBraces(source: String): Boolean {
        var balance = 0
        for (char in source) {
            when (char) {
                '{' -> balance += 1
                '}' -> balance -= 1
            }
        }
        return balance > 0
    }

    private companion object {
        val EXIT_COMMANDS = setOf(":quit", ":exit")
    }
}
