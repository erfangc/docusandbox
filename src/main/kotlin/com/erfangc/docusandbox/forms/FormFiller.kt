package com.erfangc.docusandbox.forms

import com.erfangc.docusandbox.templates.models.AutoFillInstruction
import com.erfangc.docusandbox.templates.models.Field
import com.erfangc.docusandbox.templates.models.Template
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox
import org.apache.pdfbox.pdmodel.interactive.form.PDField
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.*

@Service
class FormFiller {

    private val decoder = Base64.getDecoder()
    private val encoder = Base64.getEncoder()

    fun createDocumentBase64(
        template: Template,
        data: Map<String, Any?>,
    ): String {
        val templateBytes = decoder.decode(template.documentBase64)
        val pdDocument = PDDocument.load(templateBytes)
        // fill out each field
        val fields = template.fields
        for (field in fields) {
            pdDocument.fillField(field, data)
        }
        val outputStream = ByteArrayOutputStream()
        pdDocument.save(outputStream)
        pdDocument.close()
        return encoder.encodeToString(outputStream.toByteArray())
    }

    private fun PDDocument.fillField(field: Field, data: Map<String, Any?>) {
        val acroForm = documentCatalog.acroForm
        val pdField = acroForm.getField(field.name)
        pdField.fillField(field.autoFillInstruction, data)
    }

    private fun PDField.fillField(
        autoFillInstruction: AutoFillInstruction?,
        data: Map<String, Any?>,
    ) {
        when (this) {
            is PDTextField -> fillTextField(autoFillInstruction, data)
            is PDRadioButton -> fillRadioButton(autoFillInstruction, data)
            is PDCheckBox -> fillCheckBox(autoFillInstruction, data)
            else -> TODO()
        }
    }

    private fun PDTextField.fillTextField(
        autoFillInstruction: AutoFillInstruction?,
        data: Map<String, Any?>,
    ) {
        // first check if 'data' explicitly provide a value
        val explicitValue = data[fullyQualifiedName]
        value = if (explicitValue != null) {
            explicitValue.toString()
        } else {
            // attempt to autofill
            if (autoFillInstruction == null) {
                error("field $fullyQualifiedName must contain a autoFillInstruction")
            }
            val copyFrom = autoFillInstruction.copyFrom
            // TODO apply transforms and conditional
            data[copyFrom].toString()
        }
    }

    private fun PDRadioButton.fillRadioButton(
        autoFillInstruction: AutoFillInstruction?,
        data: Map<String, Any?>,
    ) {
        TODO("Not yet implemented")
    }

    private fun PDCheckBox.fillCheckBox(
        autoFillInstruction: AutoFillInstruction?,
        data: Map<String, Any?>,
    ) {
        // first check if 'data' explicitly provide a value
        val explicitValue = data[fullyQualifiedName]
        if (explicitValue != null) {
            if (explicitValue != false) {
                check()
            } else {
                unCheck()
            }
        } else {
            // attempt to autofill
            if (autoFillInstruction == null) {
                error("field $fullyQualifiedName must contain a autoFillInstruction")
            }
            val onlyIf = autoFillInstruction.onlyIf
                ?: error("field $fullyQualifiedName must contain a autoFillInstruction.onlyIf")
            // TODO
            val dataProperty = onlyIf.dataProperty
            val dataPropertyValue = data[dataProperty]
                ?: error("field $fullyQualifiedName cannot be autofilled, unable to determine the value of $dataProperty")

            if (onlyIf.equals != null && dataPropertyValue == onlyIf.equals) {
                check()
            } else if (onlyIf.isOneOf != null) {
                if (onlyIf.isOneOf.contains(dataPropertyValue)) {
                    check()
                }
            } else if (onlyIf.greaterThan != null) {
                // TODO
            } else if (onlyIf.lessThan != null) {
                // TODO
            } else if (onlyIf.isBetween != null) {
                // TODO
            }
        }
    }

}