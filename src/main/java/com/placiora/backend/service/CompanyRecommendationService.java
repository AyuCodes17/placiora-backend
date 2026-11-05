package com.placiora.backend.service;

import com.placiora.backend.dto.CompanyRecommendationDTO;
import com.placiora.backend.dto.RoleRecommendationDTO;
import com.placiora.backend.entity.Company;
import com.placiora.backend.entity.Student;
import com.placiora.backend.entity.StudentSkill;
import com.placiora.backend.repository.CompanyRepository;
import com.placiora.backend.repository.StudentRepository;
import com.placiora.backend.repository.StudentSkillRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CompanyRecommendationService {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentSkillRepository studentSkillRepository;

    @Autowired
    private RoleRecommendationService roleRecommendationService;


    public List<CompanyRecommendationDTO> getRecommendations(Long studentId) {

        Student student = studentRepository
                .findById(studentId)
                .orElseThrow(() ->
                        new RuntimeException("Student not found.")
                );

        List<StudentSkill> skills =
                studentSkillRepository
                        .findByStudentIdOrderByIdDesc(studentId);

        Set<String> studentSkills = new HashSet<>();

        for (StudentSkill skill : skills) {
            if (skill.getName() != null) {
                studentSkills.add(normalize(skill.getName()));
            }
        }

        /*
         * Get role-fit results from the engine
         * we already created.
         */
        List<RoleRecommendationDTO> roleRecommendations =
                roleRecommendationService
                        .getRoleRecommendations(studentId);

        Map<String, Integer> roleScores =
                new HashMap<>();

        for (RoleRecommendationDTO role : roleRecommendations) {
            roleScores.put(
                    normalize(role.getRoleName()),
                    role.getFitScore()
            );
        }


        List<Company> companies =
                companyRepository
                        .findByActiveTrueOrderByCompanyNameAsc();

        List<CompanyRecommendationDTO> results =
                new ArrayList<>();


        for (Company company : companies) {

            results.add(
                    calculateMatch(
                            student,
                            studentSkills,
                            roleScores,
                            company
                    )
            );
        }


        /*
         * Eligible opportunities first.
         * Then highest match first.
         */
        results.sort((a, b) -> {

            if (a.isEligible() != b.isEligible()) {
                return Boolean.compare(
                        b.isEligible(),
                        a.isEligible()
                );
            }

            return Integer.compare(
                    b.getMatchScore(),
                    a.getMatchScore()
            );
        });


        return results;
    }


    private CompanyRecommendationDTO calculateMatch(
            Student student,
            Set<String> studentSkills,
            Map<String, Integer> roleScores,
            Company company
    ) {

        CompanyRecommendationDTO dto =
                new CompanyRecommendationDTO();

        dto.setCompanyId(company.getId());
        dto.setCompanyName(company.getCompanyName());
        dto.setJobRole(company.getJobRole());
        dto.setLocation(company.getLocation());
        dto.setEmploymentType(company.getEmploymentType());
        dto.setDescription(company.getDescription());
        dto.setCareersUrl(company.getCareersUrl());

        dto.setMinimumCgpa(company.getMinimumCgpa());
        dto.setMinimumReadiness(company.getMinimumReadiness());

        dto.setReadinessScore(
                student.getReadinessScore()
        );


        /*
         * ---------------------------------
         * ELIGIBILITY
         * ---------------------------------
         */

        double studentCgpa =
                student.getCgpa() == null
                        ? 0
                        : student.getCgpa();

        double minimumCgpa =
                company.getMinimumCgpa() == null
                        ? 0
                        : company.getMinimumCgpa();


        boolean cgpaEligible =
                studentCgpa >= minimumCgpa;


        boolean departmentEligible =
                departmentMatches(
                        student.getDepartment(),
                        company.getEligibleDepartments()
                );


        boolean eligible =
                cgpaEligible && departmentEligible;

        dto.setEligible(eligible);


        /*
         * ---------------------------------
         * REQUIRED SKILLS
         * ---------------------------------
         */

        List<String> requiredSkills =
                split(company.getRequiredSkills());

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


        double requiredSkillScore =
                requiredSkills.isEmpty()
                        ? 100
                        : (
                                (double) matchedSkills.size()
                                / requiredSkills.size()
                        ) * 100;


        /*
         * ---------------------------------
         * ROLE FIT
         * ---------------------------------
         */

        int roleFitScore =
                roleScores.getOrDefault(
                        normalize(company.getJobRole()),
                        0
                );

        dto.setRoleFitScore(roleFitScore);


        /*
         * ---------------------------------
         * READINESS
         * ---------------------------------
         */

        double readiness =
                Math.max(
                        0,
                        Math.min(
                                100,
                                student.getReadinessScore()
                        )
                );


        /*
         * ---------------------------------
         * FINAL OPPORTUNITY MATCH
         *
         * Required Skills = 50%
         * Role Fit        = 30%
         * Readiness       = 20%
         *
         * CGPA and department are used
         * as eligibility gates instead of
         * artificially boosting fit.
         * ---------------------------------
         */

        double calculatedMatch =
                (requiredSkillScore * 0.50)
                +
                (roleFitScore * 0.30)
                +
                (readiness * 0.20);


        /*
         * If basic eligibility fails,
         * cap the displayed match.
         */
        if (!eligible) {
            calculatedMatch =
                    Math.min(calculatedMatch, 59);
        }


        int matchScore =
                (int) Math.round(calculatedMatch);

        matchScore =
                Math.max(
                        0,
                        Math.min(
                                100,
                                matchScore
                        )
                );


        dto.setMatchScore(matchScore);

        dto.setMatchLevel(
                matchLevel(matchScore, eligible)
        );

        dto.setMatchedSkills(matchedSkills);
        dto.setMissingSkills(missingSkills);

        dto.setPreferredSkills(
                split(company.getPreferredSkills())
        );


        /*
         * ---------------------------------
         * EXPLANATIONS
         * ---------------------------------
         */

        List<String> reasons =
                new ArrayList<>();

        List<String> improvements =
                new ArrayList<>();


        if (cgpaEligible) {

            reasons.add(
                    "You meet the listed CGPA eligibility."
            );

        } else {

            improvements.add(
                    "The listed minimum CGPA is "
                            + minimumCgpa
                            + "."
            );
        }


        if (departmentEligible) {

            reasons.add(
                    "Your department matches the listed eligibility."
            );

        } else {

            improvements.add(
                    "Your department is not included in the listed eligibility."
            );
        }


        if (!matchedSkills.isEmpty()) {

            reasons.add(
                    "You match "
                            + matchedSkills.size()
                            + " of "
                            + requiredSkills.size()
                            + " required skills."
            );
        }


        if (!missingSkills.isEmpty()) {

            improvements.add(
                    "Build these required skills: "
                            + String.join(", ", missingSkills)
                            + "."
            );
        }


        if (roleFitScore >= 65) {

            reasons.add(
                    company.getJobRole()
                            + " is currently one of your stronger role fits."
            );

        } else {

            improvements.add(
                    "Strengthen your profile for the "
                            + company.getJobRole()
                            + " role."
            );
        }


        if (
                company.getMinimumReadiness() == null ||
                student.getReadinessScore()
                        >= company.getMinimumReadiness()
        ) {

            reasons.add(
                    "Your current readiness meets the suggested level."
            );

        } else {

            improvements.add(
                    "Increase your readiness score toward "
                            + company.getMinimumReadiness()
                            + "%."
            );
        }


        dto.setReasons(reasons);
        dto.setImprovements(improvements);

        return dto;
    }


    private boolean departmentMatches(
            String studentDepartment,
            String eligibleDepartments
    ) {

        if (
                eligibleDepartments == null ||
                eligibleDepartments.isBlank() ||
                eligibleDepartments.equalsIgnoreCase("ALL")
        ) {
            return true;
        }


        if (
                studentDepartment == null ||
                studentDepartment.isBlank()
        ) {
            return false;
        }


        String studentValue =
                normalize(studentDepartment);


        for (
                String department :
                split(eligibleDepartments)
        ) {

            if (
                    normalize(department)
                            .equals(studentValue)
            ) {
                return true;
            }
        }


        return false;
    }


    private List<String> split(String value) {

        if (
                value == null ||
                value.isBlank()
        ) {
            return new ArrayList<>();
        }


        return Arrays
                .stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }


    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toLowerCase()
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "");
    }


    private String matchLevel(
            int score,
            boolean eligible
    ) {

        if (!eligible) {
            return "Currently Not Eligible";
        }

        if (score >= 85) {
            return "Excellent Match";
        }

        if (score >= 70) {
            return "Strong Match";
        }

        if (score >= 55) {
            return "Good Match";
        }

        return "Developing Match";
    }
}