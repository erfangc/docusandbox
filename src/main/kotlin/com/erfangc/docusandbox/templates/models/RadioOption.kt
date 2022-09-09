package com.erfangc.docusandbox.templates.models

data class RadioOption(
    val value: String,
    val autoCheckIf: AutoCheckIf? = null,
)