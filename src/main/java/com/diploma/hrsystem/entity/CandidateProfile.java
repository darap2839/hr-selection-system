package com.diploma.hrsystem.entity;

public class CandidateProfile {
    private Long id;
    private String fullName;
    private String email;
    private int experienceYears;
    private String citizenship;
    private String rawText;           // Сырой текст резюме
    private float[] embedding;        // Массив float[] для хранения вектора
    private String dynamicSkillsJson; // Строка JSON для динамических навыков

    // Конструктор по умолчанию
    public CandidateProfile() {
    }

    // Геттеры и сеттеры для всех полей
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public String getDynamicSkillsJson() {
        return dynamicSkillsJson;
    }

    public void setDynamicSkillsJson(String dynamicSkillsJson) {
        this.dynamicSkillsJson = dynamicSkillsJson;
    }
}
