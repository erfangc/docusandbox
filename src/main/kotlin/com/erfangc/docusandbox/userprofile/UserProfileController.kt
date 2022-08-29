package com.erfangc.docusandbox.userprofile

import com.erfangc.docusandbox.userprofile.models.UserProfile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("user-profiles")
class UserProfileController(private val userProfileService: UserProfileService) {
    @PutMapping
    fun upsert(@RequestBody userProfile: UserProfile) {
        return userProfileService.upsert(userProfile)
    }
    
    @GetMapping
    fun getUser(email: String): UserProfile {
        return userProfileService.getUser(email = email)
    }
}