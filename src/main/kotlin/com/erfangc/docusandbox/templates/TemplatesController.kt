package com.erfangc.docusandbox.templates

import com.erfangc.docusandbox.templates.models.Template
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("templates")
class TemplatesController(
    private val templatesService: TemplatesService
) {
    @PostMapping
    fun createTemplate(file: MultipartFile): Template {
        return templatesService.createTemplate(file)
    }
    
    @GetMapping("{filename}")
    fun getTemplate(@PathVariable filename: String): Template {
        return templatesService.getTemplate(filename)
    }
}