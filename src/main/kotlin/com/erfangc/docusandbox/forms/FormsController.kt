package com.erfangc.docusandbox.forms

import com.erfangc.docusandbox.forms.models.Form
import org.springframework.web.bind.annotation.*

@RestController
class FormsController(private val formsService: FormsService) {
    @PostMapping
    fun createForm(
        @RequestParam templateFilename: String,
        @RequestParam email: String,
        @RequestBody input: Map<String, Any>
    ): Form {
        return formsService.createForm(
            templateFilename = templateFilename,
            email = email,
            input = input,
        )
    }

    @PatchMapping("{formId}")
    fun updateForm(
        @PathVariable formId: String,
        @RequestBody input: Map<String, Any>
    ): Form {
        return formsService.updateForm(formId = formId, input = input)
    }

    @GetMapping("{formId}")
    fun getForm(@PathVariable formId: String): Form {
        return formsService.getForm(formId)
    }
}