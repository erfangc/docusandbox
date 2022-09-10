package com.erfangc.docusandbox.templates

import com.erfangc.docusandbox.templates.models.Field
import com.erfangc.docusandbox.templates.models.Template
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/templates")
class TemplatesController(
    private val templatesService: TemplatesService
) {
    @PostMapping
    fun createTemplate(file: MultipartFile): Template {
        return templatesService.createTemplate(file)
    }
    
    @GetMapping("{filename}")
    fun getTemplate(@PathVariable filename: String): Template? {
        return templatesService.getTemplate(filename)
    }

    @PatchMapping("{filename}/{fieldName}")
    fun updateAutoFillInstruction(
        @PathVariable fieldName: String, 
        @PathVariable filename: String,
        @RequestBody field: Field,
    ): Template? {
        return templatesService.updateField(
            filename = filename,
            fieldName = fieldName,
            field = field,
        )
    }
}