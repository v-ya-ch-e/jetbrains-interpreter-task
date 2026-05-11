package com.example.interpreter.runtime

sealed interface Value {
    fun render(): String
}

data class IntValue(
    val value: Int,
) : Value {
    override fun render(): String = value.toString()
}

data class BooleanValue(
    val value: Boolean,
) : Value {
    override fun render(): String = value.toString()
}
