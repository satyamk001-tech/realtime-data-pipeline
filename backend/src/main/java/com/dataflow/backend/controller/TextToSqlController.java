package com.dataflow.backend.controller;

import com.dataflow.backend.dto.TextToSqlRequest;
import com.dataflow.backend.dto.TextToSqlResponse;
import com.dataflow.backend.service.TextToSqlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/text-to-sql")
@Tag(name = "AI Text-to-SQL", description = "Secure, AST-validated natural-language analytical SQL query execution")
public class TextToSqlController {

    private final TextToSqlService textToSqlService;

    public TextToSqlController(TextToSqlService textToSqlService) {
        this.textToSqlService = textToSqlService;
    }

    @PostMapping
    @Operation(summary = "Translate natural-language query to safe AST-verified read-only analytical SQL")
    public ResponseEntity<TextToSqlResponse> executeNaturalLanguageQuery(@RequestBody TextToSqlRequest request) {
        return ResponseEntity.ok(textToSqlService.processQuestion(request));
    }
}
