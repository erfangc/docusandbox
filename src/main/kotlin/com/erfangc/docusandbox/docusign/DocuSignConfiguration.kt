package com.erfangc.docusandbox.docusign

import com.docusign.esign.api.EnvelopesApi
import com.docusign.esign.client.ApiClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource

@Configuration
class DocuSignConfiguration {

    companion object {
        val docuSignAccountId = System.getenv("DOCU_SIGN_ACCOUNT_ID") ?: "11676020"
        val docuSignEndpoint = System.getenv("DOCU_SIGN_ENDPOINT") ?: "https://demo.docusign.net/restapi"
        val docuSignOAuthBasePath = System.getenv("DOCU_SIGN_OAUTH_BASE_PATH") ?: "account-d.docusign.com"
        val docuSignClientId = System.getenv("DOCU_SIGN_CLIENT_ID") ?: "99b5362a-7429-4f48-aee7-4d3a14c42eb1"
    }

    @Bean
    fun apiClient(): ApiClient {
        val apiClient = ApiClient(docuSignEndpoint)
        apiClient.setOAuthBasePath(docuSignOAuthBasePath)
        val scopes = ArrayList<String>()
        scopes.add("signature")
        val privateKeyBytes = ClassPathResource("private.key").inputStream.readAllBytes()
        val oAuthToken = apiClient.requestJWTApplicationToken(
            docuSignClientId,
            scopes,
            // FIXME do not store this inline, somehow derive from env var
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