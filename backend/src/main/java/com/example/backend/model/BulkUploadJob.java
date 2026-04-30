package com.example.backend.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_upload_job")
@Getter
@Setter
@NoArgsConstructor
public class BulkUploadJob {

    public enum Status {
        PENDING, PROCESSING, COMPLETED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false, unique = true, length = 50)
    private String jobId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "total_records", nullable = false)
    private int totalRecords;

    @Column(nullable = false)
    private int processed;

    @Column(name = "success_count", nullable = false)
    private int successCount;

    @Column(name = "fail_count", nullable = false)
    private int failCount;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Column(name = "error_log", columnDefinition = "TEXT")
    private String errorLog;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
