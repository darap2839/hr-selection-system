package com.diploma.hrsystem.service;

import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class NerExtractionService {

    // Словари терминов радиоэлектронной отрасли для имитации работы NER-модели
    private final Set<String> skillVocabulary = new HashSet<>();

    public NerExtractionService() {
        // Заполнение словаря ключевыми САПР и технологиями ОПК
        skillVocabulary.add("altium");
        skillVocabulary.add("designer");
        skillVocabulary.add("pcad");
        skillVocabulary.add("cadence");
        skillVocabulary.add("allegro");
        skillVocabulary.add("quartus");
        skillVocabulary.add("vhdl");
        skillVocabulary.add("verilog");
        skillVocabulary.add("плис");
        skillVocabulary.add("fpga");
        skillVocabulary.add("микроконтроллер");
        skillVocabulary.add("схемотехника");
        skillVocabulary.add("трассировка");
    }

    /**
     * Имитирует работу модели RuBERT-NER по разметке токенов по правилам IOB2
     * @param tokens массив входных слов документа
     * @return массив предсказанных тегов той же длины
     */
    public String[] predictTags(String[] tokens) {
        if (tokens == null) {
            return new String[0];
        }

        String[] tags = new String[tokens.length];
        boolean insideSkill = false;
        boolean insideExperience = false;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].toLowerCase().replaceAll("[^a-zа-я0-9-]", "");

            // 1. Алгоритм разметки сущностей опыта работы (классы B-EXP и I-EXP)
            if (token.matches("опыт|стаж|работал|лет|года|год") || token.matches("[0-9]+")) {
                if (!insideExperience) {
                    tags[i] = "B-EXP";
                    insideExperience = true;
                } else {
                    tags[i] = "I-EXP";
                }
                insideSkill = false; // Сброс состояния другого класса
            }
            // 2. Алгоритм разметки технологических навыков (классы B-SKILL_TECH и I-SKILL_TECH)
            else if (skillVocabulary.contains(token)) {
                if (!insideSkill) {
                    tags[i] = "B-SKILL_TECH";
                    insideSkill = true;
                } else {
                    tags[i] = "I-SKILL_TECH";
                }
                insideExperience = false;
            }
            // 3. Токен не принадлежит ни к одному из классов (тег O)
            else {
                tags[i] = "O";
                insideSkill = false;
                insideExperience = false;
            }
        }
        return tags;
    }
}
