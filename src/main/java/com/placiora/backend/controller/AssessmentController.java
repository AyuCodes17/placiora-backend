package com.placiora.backend.controller;

import com.placiora.backend.dto.AssessmentQuestionDTO;
import com.placiora.backend.dto.AssessmentSubmissionDTO;
import com.placiora.backend.entity.AssessmentResult;
import com.placiora.backend.service.AssessmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
@CrossOrigin(origins = "http://localhost:5173")
public class AssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    @GetMapping("/questions/{type}")
    public List<AssessmentQuestionDTO> getQuestions(
            @PathVariable String type) {

        return assessmentService.getQuestions(type);
    }

    @PostMapping("/submit")
    public AssessmentResult submitAssessment(
            @RequestBody AssessmentSubmissionDTO submission) {

        return assessmentService
                .submitAssessment(submission);
    }

    @GetMapping("/results/{studentId}")
    public List<AssessmentResult> getStudentResults(
            @PathVariable Long studentId) {

        return assessmentService
                .getStudentResults(studentId);
    }
}