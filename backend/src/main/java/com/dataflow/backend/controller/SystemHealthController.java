package com.dataflow.backend.controller;

import com.dataflow.backend.dto.SystemHealthDto;
import com.dataflow.backend.service.SystemHealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "System Health", description = "Cluster health, memory metrics, and component statuses")
public class SystemHealthController {

    private final SystemHealthService systemHealthService;

    public SystemHealthController(SystemHealthService systemHealthService) {
        this.systemHealthService = systemHealthService;
    }

    @GetMapping("/health")
    @Operation(summary = "Get detailed backend, database, MinIO, and Kafka status")
    public ResponseEntity<SystemHealthDto> getHealth() {
        return ResponseEntity.ok(systemHealthService.getHealth());
    }
}
