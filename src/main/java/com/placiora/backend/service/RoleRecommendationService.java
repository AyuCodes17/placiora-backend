package com.placiora.backend.service;

import com.placiora.backend.dto.RoleRecommendationDTO;
import com.placiora.backend.entity.AssessmentResult;
import com.placiora.backend.entity.Student;
import com.placiora.backend.entity.StudentCertification;
import com.placiora.backend.entity.StudentProject;
import com.placiora.backend.entity.StudentSkill;

import com.placiora.backend.repository.AssessmentResultRepository;
import com.placiora.backend.repository.StudentCertificationRepository;
import com.placiora.backend.repository.StudentProjectRepository;
import com.placiora.backend.repository.StudentRepository;
import com.placiora.backend.repository.StudentSkillRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoleRecommendationService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentSkillRepository skillRepository;

    @Autowired
    private StudentProjectRepository projectRepository;

    @Autowired
    private StudentCertificationRepository certificationRepository;

    @Autowired
    private AssessmentResultRepository assessmentResultRepository;


    /*
     * ROLE BLUEPRINTS
     *
     * These represent the skills Placiora currently
     * considers useful for each role.
     */
    private final Map<String, List<String>> roleSkills =
            new LinkedHashMap<>();


    public RoleRecommendationService() {

        roleSkills.put(
                "Backend Developer",
                List.of(
                        "Java",
                        "Python",
                        "Spring Boot",
                        "SQL",
                        "REST API",
                        "Git",
                        "DBMS"
                )
        );

        roleSkills.put(
                "Frontend Developer",
                List.of(
                        "HTML",
                        "CSS",
                        "JavaScript",
                        "React",
                        "Git"
                )
        );

        roleSkills.put(
                "Full-Stack Developer",
                List.of(
                        "HTML",
                        "CSS",
                        "JavaScript",
                        "React",
                        "Java",
                        "Spring Boot",
                        "SQL",
                        "REST API",
                        "Git"
                )
        );

        roleSkills.put(
                "Software Engineer",
                List.of(
                        "Java",
                        "Python",
                        "OOP",
                        "DSA",
                        "SQL",
                        "Git",
                        "Problem Solving"
                )
        );

        roleSkills.put(
                "Data Analyst",
                List.of(
                        "SQL",
                        "Python",
                        "Excel",
                        "Pandas",
                        "Statistics",
                        "Power BI",
                        "Data Visualization"
                )
        );

        roleSkills.put(
                "Data Scientist",
                List.of(
                        "Python",
                        "SQL",
                        "Pandas",
                        "NumPy",
                        "Statistics",
                        "Machine Learning",
                        "Data Visualization"
                )
        );

        roleSkills.put(
                "AI/ML Engineer",
                List.of(
                        "Python",
                        "Machine Learning",
                        "Pandas",
                        "NumPy",
                        "Statistics",
                        "Model Evaluation",
                        "Data Preprocessing"
                )
        );

        roleSkills.put(
                "Data Engineer",
                List.of(
                        "SQL",
                        "Python",
                        "DBMS",
                        "ETL",
                        "Data Pipelines",
                        "Git"
                )
        );

        roleSkills.put(
                "Cloud/DevOps Engineer",
                List.of(
                        "Linux",
                        "Git",
                        "Docker",
                        "Cloud",
                        "CI/CD",
                        "Networking"
                )
        );

        roleSkills.put(
                "QA/Test Engineer",
                List.of(
                        "Testing",
                        "Debugging",
                        "SQL",
                        "Git",
                        "Problem Solving",
                        "Automation Testing"
                )
        );
    }


    public List<RoleRecommendationDTO> getRoleRecommendations(
            Long studentId
    ) {

        Student student = studentRepository
                .findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found.")
                );


        List<StudentSkill> skills =
                skillRepository
                        .findByStudentIdOrderByIdDesc(studentId);


        List<StudentProject> projects =
                projectRepository
                        .findByStudentIdOrderByIdDesc(studentId);


        List<StudentCertification> certifications =
                certificationRepository
                        .findByStudentIdOrderByIdDesc(studentId);


        List<AssessmentResult> assessments =
                assessmentResultRepository
                        .findByStudentIdOrderByCompletedAtDesc(studentId);


        Set<String> studentSkills =
                new HashSet<>();


        for (StudentSkill skill : skills) {

            if (skill.getName() != null) {
                studentSkills.add(
                        normalize(skill.getName())
                );
            }
        }


        /*
         * Latest SKILL assessment.
         */
        double skillAssessmentScore = 0;

        for (AssessmentResult result : assessments) {

            if (
                    "SKILL".equalsIgnoreCase(
                            result.getAssessmentType()
                    )
            ) {

                if (result.getPercentage() != null) {
                    skillAssessmentScore =
                            result.getPercentage();
                }

                break;
            }
        }


        List<RoleRecommendationDTO> recommendations =
                new ArrayList<>();


        for (
                Map.Entry<String, List<String>> entry :
                roleSkills.entrySet()
        ) {

            RoleRecommendationDTO recommendation =
                    calculateRoleFit(
                            entry.getKey(),
                            entry.getValue(),
                            studentSkills,
                            projects,
                            certifications,
                            skillAssessmentScore
                    );


            recommendations.add(recommendation);
        }


        recommendations.sort(
                Comparator.comparingInt(
                        RoleRecommendationDTO::getFitScore
                ).reversed()
        );


        return recommendations;
    }


    private RoleRecommendationDTO calculateRoleFit(
            String roleName,
            List<String> requiredSkills,
            Set<String> studentSkills,
            List<StudentProject> projects,
            List<StudentCertification> certifications,
            double skillAssessmentScore
    ) {

        RoleRecommendationDTO dto =
                new RoleRecommendationDTO();


        dto.setRoleName(roleName);


        /*
         * ---------------------------------
         * 1. SKILLS — 55%
         * ---------------------------------
         */

        List<String> matchedSkills =
                new ArrayList<>();

        List<String> missingSkills =
                new ArrayList<>();


        for (String requiredSkill : requiredSkills) {

            if (
                    studentSkills.contains(
                            normalize(requiredSkill)
                    )
            ) {

                matchedSkills.add(requiredSkill);

            } else {

                missingSkills.add(requiredSkill);
            }
        }


        double skillMatchPercentage =
                requiredSkills.isEmpty()
                        ? 0
                        : (
                                (double) matchedSkills.size()
                                / requiredSkills.size()
                        ) * 100;


        /*
         * ---------------------------------
         * 2. PROJECT RELEVANCE — 25%
         * ---------------------------------
         */

        List<String> relevantProjects =
                new ArrayList<>();


        for (StudentProject project : projects) {

            String searchableText =
                    normalize(
                            safe(project.getTitle())
                            + " "
                            + safe(project.getDescription())
                            + " "
                            + safe(project.getTechnologies())
                    );


            boolean relevant = false;


            for (String requiredSkill : requiredSkills) {

                if (
                        searchableText.contains(
                                normalize(requiredSkill)
                        )
                ) {

                    relevant = true;
                    break;
                }
            }


            if (relevant) {

                relevantProjects.add(
                        project.getTitle()
                );
            }
        }


        /*
         * For MVP:
         *
         * 0 projects = 0
         * 1 relevant project = 70
         * 2+ relevant projects = 100
         */

        double projectScore = 0;


        if (relevantProjects.size() == 1) {

            projectScore = 70;

        } else if (relevantProjects.size() >= 2) {

            projectScore = 100;
        }


        /*
         * ---------------------------------
         * 3. SKILL ASSESSMENT — 15%
         * ---------------------------------
         */

        double assessmentScore =
                Math.max(
                        0,
                        Math.min(
                                100,
                                skillAssessmentScore
                        )
                );


        /*
         * ---------------------------------
         * 4. CERTIFICATIONS — 5%
         * ---------------------------------
         */

        int relevantCertificationCount = 0;


        for (
                StudentCertification certification :
                certifications
        ) {

            String certificationText =
                    normalize(
                            safe(certification.getName())
                            + " "
                            + safe(certification.getIssuer())
                    );


            for (String requiredSkill : requiredSkills) {

                if (
                        certificationText.contains(
                                normalize(requiredSkill)
                        )
                ) {

                    relevantCertificationCount++;
                    break;
                }
            }
        }


        double certificationScore = 0;


        if (relevantCertificationCount == 1) {

            certificationScore = 70;

        } else if (relevantCertificationCount >= 2) {

            certificationScore = 100;
        }


        /*
         * ---------------------------------
         * FINAL ROLE FIT
         *
         * Skills          55%
         * Projects        25%
         * Assessment      15%
         * Certifications   5%
         * ---------------------------------
         */

        double calculatedFit =
                (skillMatchPercentage * 0.55)
                +
                (projectScore * 0.25)
                +
                (assessmentScore * 0.15)
                +
                (certificationScore * 0.05);


        int finalFit =
                (int) Math.round(calculatedFit);


        finalFit =
                Math.max(
                        0,
                        Math.min(
                                100,
                                finalFit
                        )
                );


        dto.setFitScore(finalFit);

        dto.setFitLevel(
                determineFitLevel(finalFit)
        );

        dto.setMatchedSkills(matchedSkills);
        dto.setMissingSkills(missingSkills);
        dto.setRelevantProjects(relevantProjects);


        /*
         * ---------------------------------
         * EXPLANATION
         * ---------------------------------
         */

        List<String> reasons =
                new ArrayList<>();


        if (!matchedSkills.isEmpty()) {

            reasons.add(
                    "You match "
                    + matchedSkills.size()
                    + " of "
                    + requiredSkills.size()
                    + " core skills for this role."
            );
        }


        if (!relevantProjects.isEmpty()) {

            reasons.add(
                    "You have project experience related to this role."
            );
        }


        if (assessmentScore >= 70) {

            reasons.add(
                    "Your skill assessment provides additional evidence of technical readiness."
            );
        }


        if (relevantCertificationCount > 0) {

            reasons.add(
                    "You have relevant certification evidence."
            );
        }


        if (reasons.isEmpty()) {

            reasons.add(
                    "Build more skills and projects related to this role to improve your fit."
            );
        }


        dto.setReasons(reasons);


        return dto;
    }


    private String determineFitLevel(
            int score
    ) {

        if (score >= 80) {
            return "Strong Current Fit";
        }

        if (score >= 65) {
            return "Good Current Fit";
        }

        if (score >= 45) {
            return "Developing Fit";
        }

        return "Early Stage";
    }


    private String normalize(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase()
                .trim()
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "");
    }


    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }
}