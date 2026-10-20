package com.dataflow.backend.repository;

import com.dataflow.backend.entity.PipelineJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PipelineJobRepository extends JpaRepository<PipelineJobEntity, String> {
    List<PipelineJobEntity> findAllByOrderByStartedAtDesc();
    Optional<PipelineJobEntity> findByRunId(String runId);
    List<PipelineJobEntity> findByStatus(String status);
}
