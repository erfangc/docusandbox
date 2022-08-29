package com.erfangc.docusandbox.templates.models

data class OnlyIf(
    val dataProperty: String,
    val greaterThan: Double? = null,
    val lessThan: Double? = null,
    val isBetween: IsBetween? = null,
    val equals: String? = null,
    val isOneOf: List<String>? = null,
)