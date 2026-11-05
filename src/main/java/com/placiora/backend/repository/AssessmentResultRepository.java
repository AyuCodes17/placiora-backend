package com.placiora.backend.repository;

import com.placiora.backend.entity.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentResultRepository
        extends JpaRepository<AssessmentResult, Long> {

    List<AssessmentResult>
            findByStudentIdOrderByCompletedAtDesc(Long studentId);
}