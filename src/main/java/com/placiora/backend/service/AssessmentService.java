package com.placiora.backend.service;

import com.placiora.backend.dto.AssessmentAnswerDTO;
import com.placiora.backend.dto.AssessmentQuestionDTO;
import com.placiora.backend.dto.AssessmentSubmissionDTO;
import com.placiora.backend.entity.AssessmentQuestion;
import com.placiora.backend.entity.AssessmentResult;
import com.placiora.backend.entity.Student;
import com.placiora.backend.repository.AssessmentQuestionRepository;
import com.placiora.backend.repository.AssessmentResultRepository;
import com.placiora.backend.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssessmentService {

    @Autowired
    private AssessmentQuestionRepository questionRepository;

    @Autowired
    private AssessmentResultRepository resultRepository;

    @Autowired
    private StudentRepository studentRepository;


    // =========================================================
    // GET QUESTIONS
    // =========================================================

    public List<AssessmentQuestionDTO> getQuestions(
            String assessmentType) {

        if (assessmentType == null ||
                assessmentType.isBlank()) {

            throw new RuntimeException(
                    "Assessment type is required."
            );
        }

        String type =
                assessmentType
                        .trim()
                        .toUpperCase();

        return questionRepository
                .findByAssessmentTypeOrderByIdAsc(type)
                .stream()
                .map(AssessmentQuestionDTO::new)
                .toList();
    }


    // =========================================================
    // SUBMIT ASSESSMENT
    // =========================================================

    public AssessmentResult submitAssessment(
            AssessmentSubmissionDTO submission) {

        if (submission.getStudentId() == null) {

            throw new RuntimeException(
                    "Student ID is required."
            );
        }

        if (submission.getAssessmentType() == null ||
                submission.getAssessmentType().isBlank()) {

            throw new RuntimeException(
                    "Assessment type is required."
            );
        }

        if (submission.getAnswers() == null ||
                submission.getAnswers().isEmpty()) {

            throw new RuntimeException(
                    "Assessment answers are required."
            );
        }


        // -----------------------------------------------------
        // FIND STUDENT
        // -----------------------------------------------------

        Student student =
                studentRepository
                        .findById(
                                submission.getStudentId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Student not found."
                                )
                        );


        String assessmentType =
                submission
                        .getAssessmentType()
                        .trim()
                        .toUpperCase();


        // -----------------------------------------------------
        // SCORE VARIABLES
        // -----------------------------------------------------

        int score = 0;
        int totalMarks = 0;
        int correctAnswers = 0;
        int totalQuestions = 0;


        // -----------------------------------------------------
        // CHECK EVERY ANSWER
        // -----------------------------------------------------

        for (AssessmentAnswerDTO submittedAnswer :
                submission.getAnswers()) {

            if (submittedAnswer.getQuestionId() == null) {
                continue;
            }


            AssessmentQuestion question =
                    questionRepository
                            .findById(
                                    submittedAnswer
                                            .getQuestionId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Question not found: "
                                                    + submittedAnswer
                                                    .getQuestionId()
                                    )
                            );


            // Prevent questions from another assessment
            // being submitted with the wrong assessment.

            if (!assessmentType.equalsIgnoreCase(
                    question.getAssessmentType())) {

                throw new RuntimeException(
                        "Question does not belong to this assessment."
                );
            }


            /*
             * All current Placiora assessments
             * are automatically scored MCQs:
             *
             * SKILL
             * COMPANY
             * SITUATIONAL
             */

            if (question.getCorrectAnswer() == null ||
                    question.getCorrectAnswer().isBlank()) {

                continue;
            }


            int marks =
                    question.getMarks() == null
                            ? 1
                            : question.getMarks();


            totalMarks += marks;
            totalQuestions++;


            String submitted =
                    submittedAnswer.getAnswer() == null
                            ? ""
                            : submittedAnswer
                                    .getAnswer()
                                    .trim();


            String correct =
                    question
                            .getCorrectAnswer()
                            .trim();


            if (submitted.equalsIgnoreCase(correct)) {

                score += marks;
                correctAnswers++;
            }
        }


        // -----------------------------------------------------
        // VALIDATE SCORABLE QUESTIONS
        // -----------------------------------------------------

        if (totalQuestions == 0) {

            throw new RuntimeException(
                    "No automatically scorable questions were submitted."
            );
        }


        // -----------------------------------------------------
        // CALCULATE PERCENTAGE
        // -----------------------------------------------------

        double percentage =
                totalMarks == 0
                        ? 0
                        : ((double) score / totalMarks)
                        * 100.0;


        // Keep percentage to two decimal places.

        percentage =
                Math.round(
                        percentage * 100.0
                ) / 100.0;


        // -----------------------------------------------------
        // CREATE RESULT
        // -----------------------------------------------------

        AssessmentResult result =
                new AssessmentResult();


        result.setStudentId(
                student.getId()
        );

        result.setAssessmentType(
                assessmentType
        );

        result.setScore(
                score
        );

        result.setTotalMarks(
                totalMarks
        );

        result.setPercentage(
                percentage
        );

        result.setCorrectAnswers(
                correctAnswers
        );

        result.setTotalQuestions(
                totalQuestions
        );


        // -----------------------------------------------------
        // SAVE RESULT
        // -----------------------------------------------------

        AssessmentResult saved =
                resultRepository.save(result);


        // -----------------------------------------------------
        // UPDATE OVERALL READINESS
        // -----------------------------------------------------

        updateReadinessScore(student);


        return saved;
    }


    // =========================================================
    // GET STUDENT ASSESSMENT HISTORY
    // =========================================================

    public List<AssessmentResult> getStudentResults(
            Long studentId) {

        return resultRepository
                .findByStudentIdOrderByCompletedAtDesc(
                        studentId
                );
    }


    // =========================================================
    // UPDATE PLACEMENT READINESS SCORE
    // =========================================================

    private void updateReadinessScore(
            Student student) {

        /*
         * Results are returned newest first.
         *
         * Therefore, the first result we find
         * for each assessment type is the
         * student's latest attempt.
         */

        List<AssessmentResult> results =
                resultRepository
                        .findByStudentIdOrderByCompletedAtDesc(
                                student.getId()
                        );


        Double skillScore = null;
        Double companyScore = null;
        Double situationalScore = null;


        // -----------------------------------------------------
        // FIND LATEST SCORE FOR EACH ASSESSMENT
        // -----------------------------------------------------

        for (AssessmentResult result : results) {

            String type =
                    result
                            .getAssessmentType()
                            .toUpperCase();


            if (skillScore == null &&
                    "SKILL".equals(type)) {

                skillScore =
                        result.getPercentage();
            }


            if (companyScore == null &&
                    "COMPANY".equals(type)) {

                companyScore =
                        result.getPercentage();
            }


            if (situationalScore == null &&
                    "SITUATIONAL".equals(type)) {

                situationalScore =
                        result.getPercentage();
            }


            /*
             * Once all three latest results
             * are found, there is no reason
             * to continue searching older attempts.
             */

            if (skillScore != null &&
                    companyScore != null &&
                    situationalScore != null) {

                break;
            }
        }


        // -----------------------------------------------------
        // CALCULATE OVERALL READINESS
        // -----------------------------------------------------

        double total = 0;
        int completedAssessments = 0;


        if (skillScore != null) {

            total += skillScore;
            completedAssessments++;
        }


        if (companyScore != null) {

            total += companyScore;
            completedAssessments++;
        }


        if (situationalScore != null) {

            total += situationalScore;
            completedAssessments++;
        }


        /*
         * MVP readiness formula:
         *
         * Overall Readiness =
         *
         * Skill + Company + Situational
         * -----------------------------
         *      Assessments Completed
         *
         * Example:
         *
         * Skill       = 70
         * Company     = 100
         * Situational = 80
         *
         * Readiness =
         * (70 + 100 + 80) / 3
         *
         * = 83
         */

        int readiness =
                completedAssessments == 0
                        ? 0
                        : (int) Math.round(
                                total /
                                completedAssessments
                        );


        // Safety bounds.

        readiness =
                Math.max(
                        0,
                        Math.min(
                                readiness,
                                100
                        )
                );


        // -----------------------------------------------------
        // SAVE UPDATED STUDENT
        // -----------------------------------------------------

        student.setReadinessScore(
                readiness
        );

        studentRepository.save(
                student
        );
    }
}