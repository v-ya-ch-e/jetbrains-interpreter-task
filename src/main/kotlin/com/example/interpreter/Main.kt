package com.example.interpreter

import com.example.interpreter.runtime.LanguageException
import java.io.File
import java.io.InputStream
import java.io.PrintStream
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    exitProcess(InterpreterCli().run(args, System.`in`, System.out, System.err))
}

class InterpreterCli(
    private val interpreter: Interpreter = Interpreter(),
) {
    fun run(
        args: Array<String>,
        input: InputStream,
        output: PrintStream,
        error: PrintStream,
    ): Int {
        if (args.singleOrNull() in setOf("-h", "--help")) {
            output.println(usage())
            return 0
        }

        if (args.singleOrNull() in setOf("-i", "--repl")) {
            return InterpreterRepl(interpreter.createSession()).run(input, output, error)
        }

        if (args.size > 1) {
            error.println("Error: Expected zero or one argument.")
            error.println(usage())
            return 2
        }

        return try {
            val source = args.singleOrNull()
                ?.let { File(it).readText() }
                ?: input.bufferedReader().readText()
            val formattedResult = interpreter.run(source)

            if (formattedResult.isNotEmpty()) {
                output.println(formattedResult)
            }
            0
        } catch (exception: LanguageException) {
            error.println("Error: ${exception.message}")
            1
        } catch (exception: java.io.IOException) {
            error.println("Error: ${exception.message}")
            1
        }
    }

    private fun usage(): String =
        """
        Usage: interpreter [source-file]
               interpreter --repl

        Reads source from standard input when no file is provided.
        In REPL mode, enter :quit or :exit to stop.
        """.trimIndent()
}
