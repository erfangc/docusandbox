package com.erfangc.docusandbox.templates.models

data class Template(
    val filename: String,
    val fields: List<Field>,
    val bytesAsBase64: String,
)