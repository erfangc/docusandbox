package com.erfangc.docusandbox

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.ResourceUtils
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.NoHandlerFoundException
import java.io.FileInputStream
import java.nio.charset.Charset

@ControllerAdvice
class NotFoundHandler {
    @ExceptionHandler(NoHandlerFoundException::class)
    fun renderDefaultPage(): ResponseEntity<String> {
        val file = ResourceUtils.getFile("classpath:public/index.html")
        val inputStream = FileInputStream(file)
        val body = StreamUtils.copyToString(inputStream, Charset.defaultCharset())
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(body)
    }
}