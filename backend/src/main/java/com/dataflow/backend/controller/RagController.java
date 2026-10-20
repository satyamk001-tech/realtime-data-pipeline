package com.dataflow.backend.controller;

import com.dataflow.backend.dto.RagQueryRequest;
import com.dataflow.backend.dto.RagQueryResponse;
import com.dataflow.backend.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/rag")
@Tag(name = "AI RAG Assistant", description = "Retrieval-Augmented Generation over customer reviews with citations")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping
    @Operation(summary = "Query customer reviews via semantic search with grounded source references")
    public ResponseEntity<RagQueryResponse> queryAssistant(@RequestBody RagQueryRequest request) {
        return ResponseEntity.ok(ragService.queryReviews(request));
    }
}
