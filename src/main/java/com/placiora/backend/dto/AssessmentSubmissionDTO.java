package com.placiora.backend.dto;

import java.util.List;

public class AssessmentSubmissionDTO {

    private Long studentId;
    private String assessmentType;
    private List<AssessmentAnswerDTO> answers;

    public AssessmentSubmissionDTO() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public List<AssessmentAnswerDTO> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AssessmentAnswerDTO> answers) {
        this.answers = answers;
    }
}