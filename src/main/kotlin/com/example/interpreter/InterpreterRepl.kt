package com.example.interpreter

import com.example.interpreter.runtime.ErrorFormatter
import com.example.interpreter.runtime.LanguageException
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.TerminalBuilder
import java.io.InputStream
import java.io.PrintStream

class InterpreterRepl(
    private val session: InterpreterSession = Interpreter().createSession(),
    private val primaryPrompt: String = "> ",
    private val continuationPrompt: String = "... ",
    private val errorFormatter: ErrorFormatter = ErrorFormatter(),
) {
    fun run(
        input: InputStream,
        output: PrintStream,
        error: PrintStream,
    ): Int {
        val reader = createInput(input, output)
        val buffer = StringBuilder()

        while (true) {
            val prompt = if (buffer.length == 0) primaryPrompt else continuationPrompt
            val line = reader.readLine(prompt) ?: break
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

    private fun createInput(input: InputStream, output: PrintStream): ReplInput =
        if (input === System.`in` && output === System.out && System.console() != null) {
            JLineReplInput()
        } else {
            StreamReplInput(input, output)
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
            error.println(errorFormatter.format(exception))
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

private interface ReplInput {
    fun readLine(prompt: String): String?
}

private class StreamReplInput(
    input: InputStream,
    private val output: PrintStream,
) : ReplInput {
    private val reader = input.bufferedReader()

    override fun readLine(prompt: String): String? {
        output.print(prompt)
        output.flush()
        return reader.readLine()
    }
}

private class JLineReplInput : ReplInput {
    private val terminal = TerminalBuilder.builder()
        .system(true)
        .build()
    private val reader = LineReaderBuilder.builder()
        .appName("interpreter")
        .terminal(terminal)
        .build()

    override fun readLine(prompt: String): String? =
        try {
            reader.readLine(prompt)
        } catch (_: EndOfFileException) {
            null
        } catch (_: UserInterruptException) {
            null
        }
}
