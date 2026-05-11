package com.example.interpreter.runtime

class OutputFormatter {
    fun format(result: ExecutionResult): String =
        result.globalVariables.entries.joinToString(separator = "\n") { (name, value) ->
            "$name: ${value.render()}"
        }
}
