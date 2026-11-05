package com.placiora.backend.controller;

import com.placiora.backend.entity.StudentSkill;
import com.placiora.backend.entity.StudentProject;
import com.placiora.backend.entity.StudentCertification;

import com.placiora.backend.repository.StudentSkillRepository;
import com.placiora.backend.repository.StudentProjectRepository;
import com.placiora.backend.repository.StudentCertificationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class ProfileDataController {

    @Autowired
    private StudentSkillRepository skillRepository;

    @Autowired
    private StudentProjectRepository projectRepository;

    @Autowired
    private StudentCertificationRepository certificationRepository;


    // ==================== SKILLS ====================

    @GetMapping("/{studentId}/skills")
    public List<StudentSkill> getSkills(@PathVariable Long studentId) {
        return skillRepository.findByStudentIdOrderByIdDesc(studentId);
    }

    @PostMapping("/{studentId}/skills")
    public StudentSkill addSkill(
            @PathVariable Long studentId,
            @RequestBody StudentSkill skill) {

        skill.setId(null);
        skill.setStudentId(studentId);

        return skillRepository.save(skill);
    }

    @DeleteMapping("/{studentId}/skills/{skillId}")
    public void deleteSkill(
            @PathVariable Long studentId,
            @PathVariable Long skillId) {

        StudentSkill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        if (!skill.getStudentId().equals(studentId)) {
            throw new RuntimeException(
                    "Skill does not belong to this student"
            );
        }

        skillRepository.delete(skill);
    }


    // ==================== PROJECTS ====================

    @GetMapping("/{studentId}/projects")
    public List<StudentProject> getProjects(
            @PathVariable Long studentId) {

        return projectRepository
                .findByStudentIdOrderByIdDesc(studentId);
    }

    @PostMapping("/{studentId}/projects")
    public StudentProject addProject(
            @PathVariable Long studentId,
            @RequestBody StudentProject project) {

        project.setId(null);
        project.setStudentId(studentId);

        return projectRepository.save(project);
    }

    @DeleteMapping("/{studentId}/projects/{projectId}")
    public void deleteProject(
            @PathVariable Long studentId,
            @PathVariable Long projectId) {

        StudentProject project =
                projectRepository.findById(projectId)
                        .orElseThrow(() ->
                                new RuntimeException("Project not found")
                        );

        if (!project.getStudentId().equals(studentId)) {
            throw new RuntimeException(
                    "Project does not belong to this student"
            );
        }

        projectRepository.delete(project);
    }


    // ================= CERTIFICATIONS =================

    @GetMapping("/{studentId}/certifications")
    public List<StudentCertification> getCertifications(
            @PathVariable Long studentId) {

        return certificationRepository
                .findByStudentIdOrderByIdDesc(studentId);
    }

    @PostMapping("/{studentId}/certifications")
    public StudentCertification addCertification(
            @PathVariable Long studentId,
            @RequestBody StudentCertification certification) {

        certification.setId(null);
        certification.setStudentId(studentId);

        return certificationRepository.save(certification);
    }

    @DeleteMapping(
            "/{studentId}/certifications/{certificationId}"
    )
    public void deleteCertification(
            @PathVariable Long studentId,
            @PathVariable Long certificationId) {

        StudentCertification certification =
                certificationRepository
                        .findById(certificationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Certification not found"
                                )
                        );

        if (!certification.getStudentId().equals(studentId)) {
            throw new RuntimeException(
                    "Certification does not belong to this student"
            );
        }

        certificationRepository.delete(certification);
    }
}
