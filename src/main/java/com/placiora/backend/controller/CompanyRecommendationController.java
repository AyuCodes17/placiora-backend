package com.placiora.backend.controller;

import com.placiora.backend.dto.CompanyRecommendationDTO;
import com.placiora.backend.service.CompanyRecommendationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company-recommendations")
@CrossOrigin(origins = "http://localhost:5173")
public class CompanyRecommendationController {

    @Autowired
    private CompanyRecommendationService companyRecommendationService;


    @GetMapping("/{studentId}")
    public List<CompanyRecommendationDTO> getRecommendations(
            @PathVariable Long studentId
    ) {

        return companyRecommendationService
                .getRecommendations(studentId);
    }
}