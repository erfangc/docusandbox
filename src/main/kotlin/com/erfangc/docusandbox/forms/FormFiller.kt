package com.erfangc.docusandbox.forms

import com.erfangc.docusandbox.templates.models.AutoCheckIf
import com.erfangc.docusandbox.templates.models.Field
import com.erfangc.docusandbox.templates.models.Operator
import com.erfangc.docusandbox.templates.models.Template
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox
import org.apache.pdfbox.pdmodel.interactive.form.PDField
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.*

@Service
class FormFiller {

    private val decoder = Base64.getDecoder()
    private val encoder = Base64.getEncoder()
    private val log = LoggerFactory.getLogger(FormFiller::class.java)

    fun createDocumentBase64(
        template: Template,
        data: Map<String, Any>,
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

    private fun PDDocument.fillField(field: Field, data: Map<String, Any>) {
        val acroForm = documentCatalog.acroForm
        val pdField = acroForm.getField(field.name)
        pdField.fillField(field, data)
    }

    private fun PDField.fillField(
        field: Field,
        data: Map<String, Any>,
    ) {
        when (this) {
            is PDTextField -> fillTextField(field, data)
            is PDRadioButton -> fillRadioButton(field, data)
            is PDCheckBox -> fillCheckBox(field, data)
            else -> error("Unsupported field type for autofill")
        }
    }

    private fun PDTextField.fillTextField(
        field: Field?,
        data: Map<String, Any>,
    ) {
        // first check if 'data' explicitly provide a value
        val explicitValue = data[fullyQualifiedName]
        if (explicitValue != null) {
            value = explicitValue.toString()
        } else {
            // attempt to auto-fill
            val autoFillFormula = field?.autoFillFormula
            if (autoFillFormula != null && data[autoFillFormula] != null) {
                value = data[autoFillFormula].toString()
            }
        }
        log.info("Text field {} value set to {}", fullyQualifiedName, value)
    }

    private fun PDRadioButton.fillRadioButton(
        field: Field,
        data: Map<String, Any>,
    ) {
        val radioOptions = field.radioOptions ?: emptyList()
        // Find the first instance in which autoCheckIf is true and then select it
        val idx = radioOptions.indexOfFirst { radioOption ->
            evaluateAutoCheck(radioOption.autoCheckIf, data, fullyQualifiedName)
        }
        if (idx != -1) {
            value = radioOptions[idx].value
        } else {
            log.error("Unable to autofill radio field $fullyQualifiedName, no option valued autoCheckIf conditions")
        }
    }

    private fun PDCheckBox.fillCheckBox(
        field: Field?,
        data: Map<String, Any>,
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
            val autoCheckIf = field?.autoCheckIf
            val shouldCheck = evaluateAutoCheck(autoCheckIf, data, fullyQualifiedName)
            if (shouldCheck) {
                check()
            } else {
                unCheck()
            }
        }
    }

    private fun evaluateAutoCheck(
        autoCheckIf: AutoCheckIf? = null,
        data: Map<String, Any?>,
        fullyQualifiedName: String
    ): Boolean {
        if (autoCheckIf == null) {
            log.error("Unable to autofill checkbox field $fullyQualifiedName, missing autoCheckIf")
            return false
        }

        val formula = autoCheckIf.formula
        if (formula == null) {
            log.error("Unable to autofill checkbox field $fullyQualifiedName, missing autoCheckIf.formula")
            return false
        }

        val formulaValue = data[formula]
        val formulaValueAsDouble = formulaValue.toString().toDoubleOrNull()
        if (formulaValue == null) {
            log.error("Unable to autofill checkbox field $fullyQualifiedName, unable to determine the value of formula=$formula")
            return false
        }

        when (autoCheckIf.operator) {
            Operator.EQUALS -> {
                if (autoCheckIf.equals == null) {
                    log.error("Unable to autofill checkbox field $fullyQualifiedName, missing autoCheckIf.greaterThan")
                    return false
                }
                if (formulaValue == autoCheckIf.equals) {
                    return true
                }
            }

            Operator.IS_ONE_OF -> {
                if (autoCheckIf.isOneOf == null) {
                    log.error("Unable to autofill checkbox field $fullyQualifiedName, missing autoCheckIf.isOneOf")
                    return false
                } else {
                    if (autoCheckIf.isOneOf.contains(formulaValue)) {
                        return true
                    }
                }
            }

            Operator.IS_BETWEEN -> {
                if (autoCheckIf.greaterThan == null) {
                    log.error("Unable to autofill checkbox field $fullyQualifiedName, missing autoCheckIf.greaterThan")
                    return false
                }

                if (autoCheckIf.lessThan == null) {
                    log.error("Unable to autofill checkbox field $fullyQualifiedName, missing autoCheckIf.lessThan")
                    return false
                }

                if (formulaValueAsDouble == null) {
                    log.error("Unable to autofill checkbox field $fullyQualifiedName, formula value $formulaValue is not a number")
                    return false
                }

                if (formulaValueAsDouble > autoCheckIf.greaterThan && formulaValueAsDouble < autoCheckIf.lessThan) {
                    return true
                }
            }

            Operator.GREATER_THAN -> {
                if (autoCheckIf.greaterThan == null) {
                    log.error("Unable to autofill checkbox field $fullyQualifiedName, missing autoCheckIf.greaterThan")
                    return false
                }
                if (formulaValueAsDouble == null) {
                    log.error("Unable to autofill checkbox field $fullyQualifiedName, formula value $formulaValue is not a number")
                    return false
                }
                if (formulaValueAsDouble > autoCheckIf.greaterThan) {
                    return true
                }
            }

            Operator.LESS_THAN -> {
                if (autoCheckIf.lessThan == null) {
                    log.error("Unable to autofill checkbox field $fullyQualifiedName, missing autoCheckIf.lessThan")
                    return false
                }
                if (formulaValueAsDouble == null) {
                    log.error("Unable to autofill checkbox field $fullyQualifiedName, formula value $formulaValue is not a number")
                    return false
                }
                if (formulaValueAsDouble < autoCheckIf.lessThan) {
                    return true
                }
            }

            null -> {
                log.error("Unable to autofill checkbox field $fullyQualifiedName, missing autoCheckIf.operator")
                return false
            }
        }
        return false
    }

}