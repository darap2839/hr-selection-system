package com.diploma.hrsystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentParsingService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Извлекает сырой текст из бинарного входящего потока файла (PDF, DOCX)
     * с помощью библиотеки автоматического определения структуры Apache Tika
     */
    public String extractRawText(InputStream fileStream) {
        try {
            // Создание парсера, который автоматически определяет формат файла
            AutoDetectParser parser = new AutoDetectParser();
            // Настройка буфера вывода (лимит -1 отключает ограничение на объем текста)
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();

            // Физический парсинг документа
            parser.parse(fileStream, handler, metadata, context);
            return handler.toString();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при парсинге документа через Apache Tika: " + e.getMessage(), e);
        }
    }

    /**
     * Разделяет текст на отдельные слова (токены) для обработки NER-моделью
     */
    public String[] tokenizeText(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new String[0];
        }
        // Разделение по пробельным символам и переносам строк
        return rawText.trim().split("\\s+");
    }

    /**
     * Преобразует нормализованную последовательность токенов в структурированную JSON-строку
     * на основе предсказанных IOB2-тегов от модели разметки сущностей
     */
    public String extractAttributes(String[] tokens, String[] tags) {
        try {
            ObjectNode attributesNode = objectMapper.createObjectNode();
            List<String> currentSkill = new ArrayList<>();
            List<String> currentExperience = new ArrayList<>();

            for (int i = 0; i < tokens.length; i++) {
                String tag = tags[i];
                String token = tokens[i];

                if (tag.equals("B-SKILL_TECH") || tag.equals("I-SKILL_TECH")) {
                    currentSkill.add(token);
                } else if (tag.equals("B-EXP") || tag.equals("I-EXP")) {
                    currentExperience.add(token);
                } else {
                    if (!currentSkill.isEmpty()) {
                        attributesNode.withArray("skills").add(String.join(" ", currentSkill));
                        currentSkill.clear();
                    }
                    if (!currentExperience.isEmpty()) {
                        attributesNode.withArray("experience_phrases").add(String.join(" ", currentExperience));
                        currentExperience.clear();
                    }
                }
            }

            if (!currentSkill.isEmpty()) {
                attributesNode.withArray("skills").add(String.join(" ", currentSkill));
            }
            if (!currentExperience.isEmpty()) {
                attributesNode.withArray("experience_phrases").add(String.join(" ", currentExperience));
            }

            return attributesNode.toString();

        } catch (Exception e) {
            throw new RuntimeException("Не удалось извлечь атрибуты в JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Извлекает числовое значение опыта (лет) из структурированного JSON-файла навыков
     */
    public int parseExperienceYears(String jsonSkills) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonSkills);
            JsonNode expNode = rootNode.get("experience_phrases");
            if (expNode != null && expNode.isArray() && expNode.size() > 0) {
                String expText = expNode.get(0).asText();
                // Извлечение всех цифр из строки с помощью регулярного выражения
                String numbers = expText.replaceAll("[^0-9]", "");
                if (!numbers.isEmpty()) {
                    return Integer.parseInt(numbers);
                }
            }
        } catch (Exception e) {
            // В случае ошибки или отсутствия фразы возвращается дефолтное значение 0
        }
        return 0;
    }
}