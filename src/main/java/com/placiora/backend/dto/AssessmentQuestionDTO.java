package com.placiora.backend.dto;

import com.placiora.backend.entity.AssessmentQuestion;

public class AssessmentQuestionDTO {

    private Long id;
    private String assessmentType;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String category;
    private String difficulty;
    private Integer marks;

    public AssessmentQuestionDTO() {
    }

    public AssessmentQuestionDTO(AssessmentQuestion question) {
        this.id = question.getId();
        this.assessmentType = question.getAssessmentType();
        this.questionText = question.getQuestionText();
        this.optionA = question.getOptionA();
        this.optionB = question.getOptionB();
        this.optionC = question.getOptionC();
        this.optionD = question.getOptionD();
        this.category = question.getCategory();
        this.difficulty = question.getDifficulty();
        this.marks = question.getMarks();
    }

    public Long getId() {
        return id;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getOptionA() {
        return optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public String getCategory() {
        return category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Integer getMarks() {
        return marks;
    }
}