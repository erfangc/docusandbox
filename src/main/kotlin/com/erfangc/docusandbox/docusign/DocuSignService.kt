package com.erfangc.docusandbox.docusign

import com.docusign.esign.api.EnvelopesApi
import com.docusign.esign.client.ApiException
import com.docusign.esign.model.*
import com.erfangc.docusandbox.forms.models.Form
import com.erfangc.docusandbox.docusign.DocuSignConfiguration.Companion.docuSignAccountId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DocuSignService(private val envelopesApi: EnvelopesApi) {

    private val log = LoggerFactory.getLogger(DocuSignService::class.java)

    fun sendDocumentForSigning(input: Form): EnvelopeSummary {
        log.info("Creating sales contract for email={}", input.recipientEmail)
        // create the sign here for our recipient
        val signHere = SignHere()
        signHere.documentId = "1"
        signHere.anchorString = "/SIG_NAME/"
        // add the sign here to the tabs that will belong to the recipient
        val tabs = Tabs()
        tabs.signHereTabs = listOf(signHere)

        // create the signer
        val signer = Signer()
        signer.email = input.recipientEmail
        signer.name = input.recipientName
        signer.recipientId = "1"
        signer.tabs = tabs

        // create the document to put into the envelope
        val document = Document()
        document.documentId = "1"
        document.name = input.filename
        document.fileExtension = "pdf"
        document.documentBase64 = input.documentBase64

        // create the envelope
        val envelopeDefinition = EnvelopeDefinition()
        envelopeDefinition.emailSubject = "Your document is ready for signing"
        envelopeDefinition.status = "sent"
        val recipients = Recipients()
        recipients.signers = listOf(signer)
        envelopeDefinition.recipients = recipients
        envelopeDefinition.documents = listOf(document)

        // send the envelope
        try {
            log.info("Sending envelope signer.email={} ACCOUNT_ID={}", signer.email, docuSignAccountId)
            val envelopeSummary = envelopesApi.createEnvelope(docuSignAccountId, envelopeDefinition)
            log.info(
                "Envelope sent to signer.email={} envelopeSummary.envelopeId={}",
                signer.email,
                envelopeSummary.envelopeId
            )
            return envelopeSummary
        } catch (exception: ApiException) {
            log.error(
                "Exception occurred message={} code={} responseBody={}",
                exception.message,
                exception.code,
                exception.responseBody
            )
            exception.printStackTrace()
            throw RuntimeException(exception)
        }
    }

}