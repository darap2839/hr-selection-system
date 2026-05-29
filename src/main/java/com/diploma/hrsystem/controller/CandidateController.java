package com.diploma.hrsystem.controller;

import com.diploma.hrsystem.entity.CandidateProfile;
import com.diploma.hrsystem.repository.HybridSearchRepository;
import com.diploma.hrsystem.service.RecruitmentPipelineOrchestrator;
import com.diploma.hrsystem.service.VectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/candidates")
public class CandidateController {

    @Autowired
    private RecruitmentPipelineOrchestrator pipelineOrchestrator;

    @Autowired
    private HybridSearchRepository searchRepository;

    @Autowired
    private VectorService vectorService; // Нужен для векторизации текста вакансии перед поиском

    /**
     * Эндпоинт для загрузки и обработки файла резюме соискателя.
     * Принимает файл и реляционные атрибуты.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CandidateProfile> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("citizenship") String citizenship) {
        try {
            // Передача входящего потока файла в сквозной конвейер обработки
            CandidateProfile processedProfile = pipelineOrchestrator.processIncomingResume(
                    file.getInputStream(), name, email, citizenship
            );
            return ResponseEntity.ok(processedProfile);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при загрузке и обработке резюме: " + e.getMessage(), e);
        }
    }

    /**
     * Эндпоинт для выполнения гибридного поиска по требованиям вакансии.
     * Автоматически векторизует требования и ищет в PostgreSQL.
     */
    @GetMapping("/search")
    public ResponseEntity<List<CandidateProfile>> searchCandidates(
            @RequestParam("query") String queryText,
            @RequestParam("minExperience") int minExperience,
            @RequestParam("citizenship") String citizenship,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        try {
            // 1. Векторизация входящего текста требований вакансии через Ollama API
            float[] queryVector = vectorService.embedText(queryText);

            // 2. Выполнение гибридного поиска (фильтр по атрибутам + косинусное сходство)
            List<CandidateProfile> matches = searchRepository.executeHybridSearch(
                    queryVector, minExperience, citizenship, limit
            );
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при выполнении гибридного поиска: " + e.getMessage(), e);
        }
    }
}