package com.placiora.backend.repository;

import com.placiora.backend.entity.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentQuestionRepository
        extends JpaRepository<AssessmentQuestion, Long> {

    List<AssessmentQuestion>
            findByAssessmentTypeOrderByIdAsc(String assessmentType);
}