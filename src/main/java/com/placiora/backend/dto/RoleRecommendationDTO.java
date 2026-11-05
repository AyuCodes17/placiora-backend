package com.placiora.backend.dto;

import java.util.List;

public class RoleRecommendationDTO {

    private String roleName;
    private int fitScore;
    private String fitLevel;

    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> relevantProjects;
    private List<String> reasons;

    public RoleRecommendationDTO() {
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getFitScore() {
        return fitScore;
    }

    public void setFitScore(int fitScore) {
        this.fitScore = fitScore;
    }

    public String getFitLevel() {
        return fitLevel;
    }

    public void setFitLevel(String fitLevel) {
        this.fitLevel = fitLevel;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public List<String> getRelevantProjects() {
        return relevantProjects;
    }

    public void setRelevantProjects(List<String> relevantProjects) {
        this.relevantProjects = relevantProjects;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }
}