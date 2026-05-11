package com.example.interpreter.runtime

class ErrorFormatter {
    fun format(exception: LanguageException): String = buildString {
        append("Error: ${exception.message}")

        val trace = (exception as? EvaluationException)?.trace.orEmpty()
        if (trace.isNotEmpty()) {
            appendLine()
            appendLine("Trace:")
            trace.forEach { functionName ->
                appendLine("  at $functionName()")
            }
        }
    }.trimEnd()
}
