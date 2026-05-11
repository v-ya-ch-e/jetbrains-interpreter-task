package com.example.interpreter.runtime

import com.example.interpreter.lexer.SourceLocation

open class LanguageException(
    message: String,
    location: SourceLocation? = null,
) : RuntimeException(
    if (location == null) message else "$message at $location",
)

class SyntaxException(
    message: String,
    location: SourceLocation? = null,
) : LanguageException(message, location)

class EvaluationException(
    message: String,
    location: SourceLocation? = null,
) : LanguageException(message, location)
