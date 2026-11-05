package com.placiora.backend.repository;

import com.placiora.backend.entity.StudentProject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentProjectRepository
        extends JpaRepository<StudentProject, Long> {

    List<StudentProject> findByStudentIdOrderByIdDesc(Long studentId);
}