package com.erfangc.docusandbox.userprofile.models

data class Address(
    val line1: String,
    val line2: String,
    val zipCode: String,
    val state: String,
    val city: String,
    val country: String,
)