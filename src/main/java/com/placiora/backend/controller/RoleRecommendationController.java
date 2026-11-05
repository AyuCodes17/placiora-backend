package com.placiora.backend.controller;

import com.placiora.backend.dto.RoleRecommendationDTO;
import com.placiora.backend.service.RoleRecommendationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "http://localhost:5173")
public class RoleRecommendationController {

    @Autowired
    private RoleRecommendationService roleRecommendationService;


    @GetMapping("/recommendations/{studentId}")
    public List<RoleRecommendationDTO> getRecommendations(
            @PathVariable Long studentId
    ) {

        return roleRecommendationService
                .getRoleRecommendations(studentId);
    }
}