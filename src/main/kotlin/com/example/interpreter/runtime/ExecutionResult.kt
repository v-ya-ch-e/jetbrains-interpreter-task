package com.example.interpreter.runtime

data class ExecutionResult(
    val globalVariables: LinkedHashMap<String, Value>,
)
