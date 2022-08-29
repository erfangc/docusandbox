package com.erfangc.docusandbox.userprofile.models

import java.time.LocalDate

data class UserProfile(
    val email: String,
    val name: String,
    val ownership: Double? = null,
    val dayPhone: String? = null,
    val eveningPhone: String? = null,
    val address: Address? = null,
    val birthDate: LocalDate? = null,
    val sex: Sex? = null,
)