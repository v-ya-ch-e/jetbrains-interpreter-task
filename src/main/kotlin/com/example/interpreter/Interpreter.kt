package com.example.interpreter

import com.example.interpreter.eval.Evaluator
import com.example.interpreter.parser.Parser
import com.example.interpreter.runtime.OutputFormatter

class Interpreter(
    private val parser: Parser = Parser(),
    private val evaluator: Evaluator = Evaluator(),
    private val outputFormatter: OutputFormatter = OutputFormatter(),
) {
    fun run(source: String): String {
        val program = parser.parse(source)
        val result = evaluator.execute(program)

        return outputFormatter.format(result)
    }
}
