package com.placiora.backend.repository;

import com.placiora.backend.entity.StudentSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentSkillRepository
        extends JpaRepository<StudentSkill, Long> {

    List<StudentSkill> findByStudentIdOrderByIdDesc(Long studentId);
}