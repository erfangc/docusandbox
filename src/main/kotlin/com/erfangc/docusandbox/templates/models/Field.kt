package com.erfangc.docusandbox.templates.models

data class Field(
    val name: String,
    val type: Type,
    val pages: List<Int>,
    val autoFillInstruction: AutoFillInstruction? = null,
)

