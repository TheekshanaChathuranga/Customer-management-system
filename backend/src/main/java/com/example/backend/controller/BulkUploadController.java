package com.example.backend.controller;

import com.example.backend.dto.BulkUploadResponse;
import com.example.backend.model.BulkUploadJob;
import com.example.backend.service.BulkUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/bulk-upload")
public class BulkUploadController {

    private final BulkUploadService bulkUploadService;

    public BulkUploadController(BulkUploadService bulkUploadService) {
        this.bulkUploadService = bulkUploadService;
    }

    @PostMapping
    public ResponseEntity<BulkUploadResponse> uploadFile(@RequestParam("file") MultipartFile file)
            throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new BulkUploadResponse(null, "FAILED", 0, 0, 0, 0, "File is empty"));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            return ResponseEntity.badRequest().body(
                    new BulkUploadResponse(null, "FAILED", 0, 0, 0, 0, "Only .xlsx and .xls files are supported"));
        }

        // Save to temp file
        Path tempDir = Files.createTempDirectory("bulk-upload-");
        File tempFile = new File(tempDir.toFile(), originalFilename);
        file.transferTo(tempFile);

        // Create job and start async processing
        BulkUploadJob job = bulkUploadService.createJob(originalFilename);
        bulkUploadService.processFileAsync(job.getJobId(), tempFile);

        return ResponseEntity.ok(new BulkUploadResponse(
                job.getJobId(), "PENDING", 0, 0, 0, 0, null));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<BulkUploadResponse> getJobStatus(@PathVariable String jobId) {
        BulkUploadJob job = bulkUploadService.getJobStatus(jobId);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new BulkUploadResponse(
                job.getJobId(),
                job.getStatus().name(),
                job.getTotalRecords(),
                job.getProcessed(),
                job.getSuccessCount(),
                job.getFailCount(),
                job.getErrorLog()));
    }
}
