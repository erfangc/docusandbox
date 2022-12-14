package com.erfangc.docusandbox.forms

import com.erfangc.docusandbox.docusign.DocuSignService
import com.erfangc.docusandbox.forms.models.Form
import com.erfangc.docusandbox.templates.TemplatesService
import com.erfangc.docusandbox.templates.models.Template
import com.erfangc.docusandbox.userprofile.UserProfileService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

@Service
class FormsService(
    private val templatesService: TemplatesService,
    private val userProfileService: UserProfileService,
    private val objectMapper: ObjectMapper,
    private val docuSignService: DocuSignService,
    private val formFiller: FormFiller,
) {

    private val objectWriter = objectMapper.writerWithDefaultPrettyPrinter()
    private val formsDir = System.getenv("FORMS_DIR") ?: "forms"
    
    fun createForm(
        templateFilename: String,
        email: String,
        input: Map<String, Any> = emptyMap(),
    ): Form {
        val (template, data) = prepare(templateFilename, email)
        val documentBase64 = formFiller.createDocumentBase64(template, data + input)
        val formId = UUID.randomUUID().toString().substring(0, 6)
        val form = Form(
            formId = formId,
            documentBase64 = documentBase64,
            filename = "$formId.pdf",
            input = input,
            templateFilename = templateFilename,
            recipientEmail = email,
            recipientName = data["userProfile.name"].toString(),
        )
        saveForm(form)
        return form
    }
    
    fun signForm(formId: String): Form {
        val form = getForm(formId)
        val envelopeSummary = docuSignService.sendDocumentForSigning(form)
        val ret = form.copy(envelopeId = envelopeSummary.envelopeId)
        saveForm(ret)
        return ret
    }

    fun getForm(formId: String): Form {
        val file = File(formsDir, "$formId.json")
        return objectMapper.readValue(file)
    }

    fun updateForm(formId: String, input: Map<String, Any>): Form {
        val form = getForm(formId)
        val (template, data) = prepare(templateFilename = form.templateFilename, email = form.recipientEmail)
        val newInput = form.input + data + input
        val documentBase64 = formFiller.createDocumentBase64(template, newInput)
        val updatedForm = form.copy(
            documentBase64 = documentBase64,
            input = newInput,
        )
        saveForm(updatedForm)
        return updatedForm
    }

    private fun prepare(
        templateFilename: String, email: String
    ): Pair<Template, Map<String, Any>> {
        val template = templatesService.getTemplate(
            filename = templateFilename,
            includeTemplateBytes = true,
        ) ?: error("cannot find template $templateFilename")

        val userProfile = userProfileService.getUser(email)

        // create 'data' by merging all kinds of data ...
        val data: Map<String, Any> = listOf(
            "userProfile.name" to userProfile.name,
            "userProfile.birthDate" to userProfile.birthDate?.toString(),
            "userProfile.sex" to userProfile.sex?.toString(),
            "userProfile.email" to userProfile.email,
            "userProfile.ownership" to userProfile.ownership,
            "userProfile.eveningPhone" to userProfile.eveningPhone,
            "userProfile.dayPhone" to userProfile.dayPhone,
            "userProfile.address.line1" to userProfile.address?.line1,
            "userProfile.address.line2" to userProfile.address?.line2,
            "userProfile.address.city" to userProfile.address?.city,
            "userProfile.address.state" to userProfile.address?.state,
            "userProfile.address.country" to userProfile.address?.country,
            "userProfile.address.zipCode" to userProfile.address?.zipCode,
        ).filter { it.second != null }.associate { it.first to it.second!! }
        
        return Pair(template, data)
    }

    private fun saveForm(form: Form) {
        val file = File(formsDir, form.formId + ".json")
        objectWriter.writeValue(file, form)
    }
}

