package com.erfangc.docusandbox.templates.models

data class Field(
    val name: String,
    val type: Type,
    val pages: List<Int>,
    val radioOptions: List<RadioOption>?=null,
    val autoFillFormula: String? = null,
    val autoCheckIf: AutoCheckIf? = null,
)