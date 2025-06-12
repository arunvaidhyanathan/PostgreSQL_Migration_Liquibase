package com.company.migration.controller;

import com.company.migration.service.LiquibaseMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/migration")
@Tag(name = "Migration API", description = "PostgreSQL database migration operations using Liquibase")
public class MigrationController {

    private final LiquibaseMigrationService migrationService;

    public MigrationController(LiquibaseMigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @PostMapping("/run")
    @Operation(
        summary = "Run database migration",
        description = "Execute pending Liquibase changesets for the specified environment contexts"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Migration completed successfully"),
        @ApiResponse(responseCode = "500", description = "Migration failed")
    })
    public ResponseEntity<Map<String, Object>> runMigration(
            @Parameter(description = "Environment contexts (e.g., 'dev', 'uat', 'production')")
            @RequestParam(required = false) String contexts) {
        
        Map<String, Object> result = migrationService.runMigration(contexts);
        
        if ("SUCCESS".equals(result.get("status"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @PostMapping("/rollback")
    @Operation(
        summary = "Rollback database migration",
        description = "Rollback the specified number of changesets"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rollback completed successfully"),
        @ApiResponse(responseCode = "500", description = "Rollback failed")
    })
    public ResponseEntity<Map<String, Object>> rollbackMigration(
            @Parameter(description = "Number of changesets to rollback", required = true)
            @RequestParam int count) {
        
        if (count <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "FAILED",
                "message", "Count must be a positive integer",
                "timestamp", System.currentTimeMillis()
            ));
        }
        
        Map<String, Object> result = migrationService.rollbackMigration(count);
        
        if ("SUCCESS".equals(result.get("status"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @GetMapping("/status")
    @Operation(
        summary = "Get migration status",
        description = "Get current migration status including pending changesets"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Failed to retrieve status")
    })
    public ResponseEntity<Map<String, Object>> getMigrationStatus() {
        Map<String, Object> result = migrationService.getMigrationStatus();
        
        if ("SUCCESS".equals(result.get("status"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @GetMapping("/validate")
    @Operation(
        summary = "Validate changelog",
        description = "Validate the Liquibase changelog syntax and structure"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Changelog validation passed"),
        @ApiResponse(responseCode = "500", description = "Changelog validation failed")
    })
    public ResponseEntity<Map<String, Object>> validateChangelog() {
        Map<String, Object> result = migrationService.validateChangelog();
        
        if ("SUCCESS".equals(result.get("status"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Simple health check endpoint for the migration service"
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "PostgreSQL Migration Service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}