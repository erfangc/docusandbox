package com.erfangc.docusandbox.templates

import com.erfangc.docusandbox.templates.models.AutoFillInstruction
import com.erfangc.docusandbox.templates.models.Field
import com.erfangc.docusandbox.templates.models.Template
import com.erfangc.docusandbox.templates.models.Type
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.form.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.*

@Service
class TemplatesService(private val objectMapper: ObjectMapper) {

    private val objectWriter = objectMapper.writerWithDefaultPrettyPrinter()
    private val log = LoggerFactory.getLogger(TemplatesService::class.java)
    private val templatesDir = System.getenv("TEMPLATES_DIR") ?: "templates"
    private val encoder = Base64.getEncoder()

    fun getTemplate(filename: String, includeTemplateBytes: Boolean = false): Template {
        val persistedFile = File(templatesDir, "$filename.template.json")
        val ret = objectMapper.readValue<Template>(persistedFile)
        return if (!includeTemplateBytes) {
            ret.copy(documentBase64 = "")
        } else {
            ret
        }
    }

    fun updateField(
        filename: String,
        fieldName: String,
        autoFillInstruction: AutoFillInstruction,
    ): Template {
        val template = getTemplate(filename, true)
        val updatedTemplate = template.copy(
            fields = template.fields.map { field: Field ->
                if (field.name == fieldName) {
                    field.copy(autoFillInstruction = autoFillInstruction)
                } else {
                    field
                }
            }
        )
        writeTemplate(filename, updatedTemplate)
        return updatedTemplate.copy(documentBase64 = "")
    }

    fun createTemplate(file: MultipartFile): Template {

        val bytes = file.bytes
        val pdDocument = PDDocument.load(bytes)
        val filename = file.originalFilename ?: file.name

        val documentCatalog = pdDocument.documentCatalog
        val acroForm = documentCatalog.acroForm

        val fields = acroForm.fields.map { field ->
            Field(
                name = field.fullyQualifiedName,
                type = type(field),
                pages = pdDocument.pagesOf(field),
            )
        }

        log.info("Found {} fields in file {}", fields.size, filename)
        for (field in fields) {
            log.info("Found field field.name={} field.pages={} field.type={}", field.name, field.pages, field.type)
        }

        pdDocument.close()

        val template = Template(
            filename = filename,
            fields = fields,
            documentBase64 = encoder.encodeToString(bytes)
        )

        writeTemplate(filename, template)

        return template
    }

    private fun writeTemplate(filename: String, template: Template) {
        val persistedFile = File(templatesDir, "$filename.template.json")
        persistedFile.writeText(objectWriter.writeValueAsString(template))
    }

    private fun PDDocument.pagesOf(field: PDField) = field
        .widgets
        .map { widget -> pages.indexOf(widget.page) }
        .distinct()

    private fun type(field: PDField) = when (field) {
        is PDTextField ->
            Type.TEXT_FIELD

        is PDRadioButton ->
            Type.RADIO_BOX

        is PDCheckBox ->
            Type.CHECK_BOX

        is PDSignatureField ->
            Type.SIGNATURE

        else ->
            Type.UNKNOWN
    }

}