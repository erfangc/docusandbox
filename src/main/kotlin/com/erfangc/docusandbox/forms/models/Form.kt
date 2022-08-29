package com.erfangc.docusandbox.forms.models

data class Form(
    val formId: String,
    val templateFilename: String,
    val envelopeId: String? = null,
    val documentBase64: String,
    val filename: String,
    val input: Map<String, Any?>,
    val recipientEmail: String,
    val recipientName: String,
)