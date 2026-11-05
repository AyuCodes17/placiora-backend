package com.placiora.backend.dto;

public class AssessmentAnswerDTO {

    private Long questionId;
    private String answer;

    public AssessmentAnswerDTO() {
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}