package com.erfangc.docusandbox

import com.docusign.esign.api.EnvelopesApi
import com.docusign.esign.client.ApiClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class DocuSignConfiguration {
    
    companion object {
        const val ACCOUNT_ID = "11676020"
    }
    
    @Bean
    fun apiClient(): ApiClient {
        val apiClient = ApiClient("https://demo.docusign.net/restapi")
        apiClient.setOAuthBasePath("account-d.docusign.com")
        val scopes = ArrayList<String>()
        scopes.add("signature")
        scopes.add("impersonation")
        val privateKeyBytes = ClassPathResource("private.key").inputStream.readAllBytes()
        val oAuthToken = apiClient.requestJWTUserToken(
            "99b5362a-7429-4f48-aee7-4d3a14c42eb1",
            "649b0028-d969-4200-b478-14456e7d571b",
            scopes,
            privateKeyBytes,
            3600
        )
        apiClient.addDefaultHeader("Authorization", "Bearer ${oAuthToken.accessToken}")
        return apiClient
    }

    @Bean
    fun envelopsApi(apiClient: ApiClient): EnvelopesApi {
        return EnvelopesApi(apiClient)
    }
}