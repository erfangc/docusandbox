package com.erfangc.docusandbox.templates.models

data class AutoCheckIf(
    val formula: String? = null,
    val operator: Operator? = null,
    val greaterThan: Double? = null,
    val lessThan: Double? = null,
    val equals: String? = null,
    val isOneOf: List<String>? = null,
)