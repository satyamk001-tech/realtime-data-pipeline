package com.dataflow.backend.controller;

import com.dataflow.backend.dto.PipelineJobDto;
import com.dataflow.backend.dto.PipelineRunRequest;
import com.dataflow.backend.service.PipelineJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pipelines")
@Tag(name = "Pipelines", description = "Asynchronous data pipeline lifecycle and execution monitoring APIs")
public class PipelineController {

    private final PipelineJobService pipelineJobService;

    public PipelineController(PipelineJobService pipelineJobService) {
        this.pipelineJobService = pipelineJobService;
    }

    @GetMapping
    @Operation(summary = "List all pipeline execution runs sorted by start time")
    public ResponseEntity<List<PipelineJobDto>> getAllJobs() {
        return ResponseEntity.ok(pipelineJobService.getAllJobs());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed execution metrics and status for a pipeline job")
    public ResponseEntity<PipelineJobDto> getJobById(@PathVariable String id) {
        return ResponseEntity.ok(pipelineJobService.getJobById(id));
    }

    @PostMapping("/run")
    @Operation(summary = "Trigger an asynchronous pipeline execution ticket (non-blocking 202 Accepted)")
    public ResponseEntity<PipelineJobDto> triggerPipeline(@RequestBody(required = false) PipelineRunRequest request) {
        PipelineRunRequest req = request != null ? request : new PipelineRunRequest();
        PipelineJobDto ticket = pipelineJobService.triggerPipeline(req);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ticket);
    }
}
