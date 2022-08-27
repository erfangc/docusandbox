package com.erfangc.docusandbox

import com.docusign.esign.api.EnvelopesApi
import com.docusign.esign.client.ApiException
import com.docusign.esign.model.*
import com.erfangc.docusandbox.DocuSignConfiguration.Companion.ACCOUNT_ID
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.web.bind.annotation.*
import java.awt.Desktop
import java.net.URI
import java.util.*

@RestController
@RequestMapping("sales-contracts")
class SalesContractsController(private val envelopesApi: EnvelopesApi) {

    private val log = LoggerFactory.getLogger(SalesContractsController::class.java)

    @PostMapping
    fun createContract(@RequestBody request: CreateContractRequest): CreateContractResponse {
        log.info("Creating sales contract for email={}", request.email)
        // create the sign here for our recipient
        val signHere = SignHere()
        signHere.documentId = "1"
        signHere.anchorString = "/SIG_NAME/"
        // add the sign here to the tabs that will belong to the recipient
        val tabs = Tabs()
        tabs.signHereTabs = listOf(signHere)

        // create the signer
        val signer = Signer()
        signer.email = request.email
        signer.name = request.name
        signer.recipientId = "1"
        signer.tabs = tabs

        // create the document to put into the envelope
        val document = Document()
        document.documentId = "1"
        document.name = "Sales_contract.pdf"
        document.fileExtension = "pdf"
        document.documentBase64 = salesQuoteBase64()

        // create the envelope
        val envelopeDefinition = EnvelopeDefinition()
        envelopeDefinition.emailSubject = request.email
        envelopeDefinition.status = "sent"
        val recipients = Recipients()
        recipients.signers = listOf(signer)
        envelopeDefinition.recipients = recipients
        envelopeDefinition.documents = listOf(document)

        // send the envelope
        try {
            log.info("Sending envelope signer.email={} ACCOUNT_ID={}", signer.email, ACCOUNT_ID)
            val envelopeSummary = envelopesApi.createEnvelope(ACCOUNT_ID, envelopeDefinition)
            val createContractResponse = CreateContractResponse(
                envelopId = envelopeSummary.envelopeId,
                email = signer.email,
            )
            log.info("Envelope sent to signer.email={} envelopeSummary.envelopeId={}", signer.email, envelopeSummary.envelopeId)
            return createContractResponse
        } catch (exception: ApiException) {
            log.error(
                "Exception occurred message={} code={} responseBody={}",
                exception.message,
                exception.code,
                exception.responseBody
            )
            if (exception.message?.contains("consent_required") == true) {
                log.info("Consent required accept consent in browser window and run this request again")
                try {
                    Desktop.getDesktop()
                        .browse(URI("https://account-d.docusign.com/oauth/auth?response_type=code&scope=impersonation%20signature&client_id=99b5362a-7429-4f48-aee7-4d3a14c42eb1&redirect_uri=https://developers.docusign.com/platform/auth/consent"));
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
            exception.printStackTrace()
            throw RuntimeException(exception)
        }
    }

    private fun salesQuoteBase64(): String? {
        val classPathResource = ClassPathResource("Sales_contract.pdf")
        return Base64.getEncoder().encodeToString(classPathResource.inputStream.readAllBytes())
    }

    @PutMapping
    fun signContract() {

    }

}