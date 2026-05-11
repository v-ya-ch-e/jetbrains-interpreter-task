package com.example.interpreter.runtime

import com.example.interpreter.ast.FunctionDefinition

class RuntimeState {
    val globalVariables: LinkedHashMap<String, Value> = linkedMapOf()
    val functions: MutableMap<String, FunctionDefinition> = linkedMapOf()
}

class CallFrame(
    parameters: Map<String, Value>,
) {
    val localVariables: MutableMap<String, Value> = parameters.toMutableMap()
}
