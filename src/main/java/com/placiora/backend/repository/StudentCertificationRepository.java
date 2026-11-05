package com.placiora.backend.repository;

import com.placiora.backend.entity.StudentCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentCertificationRepository
        extends JpaRepository<StudentCertification, Long> {

    List<StudentCertification> findByStudentIdOrderByIdDesc(Long studentId);
}