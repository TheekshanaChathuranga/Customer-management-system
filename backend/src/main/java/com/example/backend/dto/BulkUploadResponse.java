package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadResponse {
    private String jobId;
    private String status;
    private int totalRecords;
    private int processed;
    private int successCount;
    private int failCount;
    private String errorLog;
}
