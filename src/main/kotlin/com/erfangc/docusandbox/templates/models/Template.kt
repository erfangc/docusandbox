package com.erfangc.docusandbox.templates.models

data class Template(
    val filename: String,
    val fields: List<Field>,
    val documentBase64: String,
)