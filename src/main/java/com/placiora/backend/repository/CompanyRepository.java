package com.placiora.backend.repository;

import com.placiora.backend.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByActiveTrueOrderByCompanyNameAsc();
}