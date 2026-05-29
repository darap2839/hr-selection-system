package com.diploma.hrsystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class VectorService {

    @Value("${ollama.api.url}")
    private String ollamaApiUrl; // Инъекция адреса Ollama из файла application.properties

    // Использование встроенного в Java 17 высокопроизводительного HTTP-клиента
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper(); // Утилита для работы с JSON

    /**
     * Генерирует многомерный вектор (эмбеддинг) для переданного текста
     * @param text сырой текст (резюме или вакансия)
     * @return массив float[] размерности 384
     */
    public float[] embedText(String text) {
        try {
            // Подготовка JSON-тела запроса: {"model": "all-minilm", "prompt": "ваш текст"}
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("model", "all-minilm");
            requestBody.put("prompt", text);

            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            // Создание HTTP-запроса к API Ollama
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaApiUrl + "/api/embeddings"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            // Отправка запроса и получение текстового ответа
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Ollama вернула ошибку, статус-код: " + response.statusCode());
            }

            // Парсинг JSON-ответа и извлечение массива "embedding"
            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode embeddingNode = rootNode.get("embedding");

            if (embeddingNode == null || !embeddingNode.isArray()) {
                throw new RuntimeException("В ответе Ollama отсутствует вектор");
            }

            // Преобразование JSON-массива в массив вещественных чисел Java (float[])
            float[] vector = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                vector[i] = (float) embeddingNode.get(i).asDouble();
            }

            return vector;

        } catch (Exception e) {
            throw new RuntimeException("Не удалось сгенерировать эмбеддинг: " + e.getMessage(), e);
        }
    }
}