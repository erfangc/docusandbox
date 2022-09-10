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
        val docuSignUserId = System.getenv("DOCU_SIGN_USER_ID") ?: "649b0028-d969-4200-b478-14456e7d571b"
        val docuSignPrivateKey = System.getenv("DOCU_SIGN_PRIVATE_KEY") ?: error("environment variable DOCU_SIGN_PRIVATE_KEY not set")
    }

    @Bean
    fun apiClient(): ApiClient {
        val apiClient = ApiClient(docuSignEndpoint)
        apiClient.setOAuthBasePath(docuSignOAuthBasePath)
        val scopes = ArrayList<String>()
        scopes.add("signature")
        scopes.add("impersonation")

        val pk = """
-----BEGIN RSA PRIVATE KEY-----
${docuSignPrivateKey.replace(" ", "\n")}
-----END RSA PRIVATE KEY-----
        """.trimIndent()
        val privateKeyBytes = pk.encodeToByteArray()
        
        val oAuthToken = apiClient.requestJWTUserToken(
            docuSignClientId,
            docuSignUserId,
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