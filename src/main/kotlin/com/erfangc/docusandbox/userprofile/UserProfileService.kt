package com.erfangc.docusandbox.userprofile

import com.erfangc.docusandbox.userprofile.models.UserProfile
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import java.io.File

@Service
class UserProfileService(private val objectMapper: ObjectMapper) {

    private val objectWriter = objectMapper.writerWithDefaultPrettyPrinter()
    private val userProfilesDir = System.getenv("USER_PROFILES_DIR") ?: "userprofiles"

    fun upsert(userProfile: UserProfile) {
        val file = File(userProfilesDir, "${userProfile.email}.json")
        objectWriter.writeValue(file, userProfile)
    }

    fun getUser(email: String): UserProfile {
        val file = File(userProfilesDir, "$email.json")
        return objectMapper.readValue(file)
    }
}