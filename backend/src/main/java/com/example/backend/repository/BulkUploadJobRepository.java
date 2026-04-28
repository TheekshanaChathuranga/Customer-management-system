package com.example.backend.repository;

import com.example.backend.model.BulkUploadJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BulkUploadJobRepository extends JpaRepository<BulkUploadJob, Long> {
    Optional<BulkUploadJob> findByJobId(String jobId);
}
