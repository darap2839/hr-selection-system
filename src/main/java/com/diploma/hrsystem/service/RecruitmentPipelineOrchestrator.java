package com.diploma.hrsystem.service;

import com.diploma.hrsystem.entity.CandidateProfile;
import com.diploma.hrsystem.repository.HybridSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

@Service
public class RecruitmentPipelineOrchestrator {

    @Autowired
    private DocumentParsingService parserService;       // Модуль Tika извлечения текста

    @Autowired
    private NerExtractionService nerService;            // Модуль RuBERT-NER разметки

    @Autowired
    private VectorService vectorService;                // Модуль SBERT векторизации

    @Autowired
    private HybridSearchRepository searchRepository;    // Репозиторий гибридного поиска

    /**
     * Запускает сквозной конвейер обработки и интеграции данных резюме соискателя
     * @param fileStream входной поток бинарного файла резюме (PDF/DOCX)
     * @param candidateName ФИО соискателя
     * @param email контактный email соискателя
     * @param citizenship гражданство соискателя
     * @return сохраненный и полностью обработанный профиль кандидата
     */
    @Transactional
    public CandidateProfile processIncomingResume(InputStream fileStream, String candidateName, String email, String citizenship) throws Exception {
        try {
            // Шаг 1: Извлечение сырого текста из бинарного файла через Apache Tika
            String rawText = parserService.extractRawText(fileStream);

            // Шаг 2: Токенизация и извлечение жестких и мягких атрибутов через NER-модель
            String[] tokens = parserService.tokenizeText(rawText);
            String[] nerTags = nerService.predictTags(tokens);
            String structuredSkillsJson = parserService.extractAttributes(tokens, nerTags);

            // Шаг 3: Генерация семантического вектора (эмбеддинга) текста через Ollama API
            float[] textEmbedding = vectorService.embedText(rawText);

            // Шаг 4: Консолидация данных в единую сущность профиля кандидата
            CandidateProfile profile = new CandidateProfile();
            profile.setFullName(candidateName);
            profile.setEmail(email);
            profile.setCitizenship(citizenship);
            profile.setRawText(rawText);
            profile.setEmbedding(textEmbedding);
            profile.setDynamicSkillsJson(structuredSkillsJson);

            // Расчет предварительного стажа на основе извлеченных фраз
            int calculatedExperience = parserService.parseExperienceYears(structuredSkillsJson);
            profile.setExperienceYears(calculatedExperience);

            // Шаг 5: Запись транзакционной сущности в базу данных PostgreSQL
            return searchRepository.saveCandidateProfile(profile);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка в работе сквозного конвейера обработки резюме: " + e.getMessage(), e);
        }
    }
}