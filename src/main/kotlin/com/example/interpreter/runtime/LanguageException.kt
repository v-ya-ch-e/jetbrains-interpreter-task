package com.example.interpreter.runtime

import com.example.interpreter.lexer.SourceLocation

open class LanguageException(
    val description: String,
    val location: SourceLocation? = null,
    cause: Throwable? = null,
) : RuntimeException(
    if (location == null) description else "$description at $location",
    cause,
)

class SyntaxException(
    message: String,
    location: SourceLocation? = null,
) : LanguageException(message, location)

class EvaluationException(
    message: String,
    location: SourceLocation? = null,
    val trace: List<String> = emptyList(),
    cause: Throwable? = null,
) : LanguageException(message, location, cause) {
    fun withCall(functionName: String): EvaluationException =
        EvaluationException(description, location, trace + functionName, this)
}
