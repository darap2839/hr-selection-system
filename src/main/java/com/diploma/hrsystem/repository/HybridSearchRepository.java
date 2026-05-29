package com.diploma.hrsystem.repository;

import com.diploma.hrsystem.entity.CandidateProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class HybridSearchRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate; // Инструмент Spring для выполнения SQL-запросов

    /**
     * Выполняет гибридный поиск кандидатов в СУБД PostgreSQL
     * @param vacancyVector сгенерированный эмбеддинг требований вакансии
     * @param minExperience минимально допустимый стаж кандидата
     * @param targetCitizenship требуемое гражданство
     * @param limit количество возвращаемых записей
     * @return ранжированный список подходящих профилей соискателей
     */
    public List<CandidateProfile> executeHybridSearch(float[] vacancyVector, int minExperience, String targetCitizenship, int limit) {

        // Преобразование вектора в строковый формат для расширения pgvector: [0.12,-0.43,...]
        String vectorString = convertToVectorLiteral(vacancyVector);

        // SQL-запрос, объединяющий фильтрацию по атрибутам и векторное ранжирование.
        // Оператор <=> в pgvector вычисляет косинусное расстояние.
        // Similarity score рассчитывается как: 1 - расстояние.
        String sql = "SELECT id, candidate_name, experience_years, citizenship, " +
                "(1 - (embedding <=> ?::vector)) AS similarity_score " +
                "FROM resumes " +
                "WHERE experience_years >= ? " +
                "AND citizenship = ? " +
                "ORDER BY embedding <=> ?::vector ASC " +
                "LIMIT ?";

        // Выполнение запроса и маппинг результатов в объекты Java
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CandidateProfile candidate = new CandidateProfile();
            candidate.setId(rs.getLong("id"));
            candidate.setFullName(rs.getString("candidate_name"));
            candidate.setExperienceYears(rs.getInt("experience_years"));
            candidate.setCitizenship(rs.getString("citizenship"));
            return candidate;
        }, vectorString, minExperience, targetCitizenship, vectorString, limit);
    }

    /**
     * Сохраняет новый профиль кандидата в базу данных
     */
    public CandidateProfile saveCandidateProfile(CandidateProfile profile) {
        String sql = "INSERT INTO resumes (candidate_name, email, experience_years, citizenship, raw_text, dynamic_skills, embedding) " +
                "VALUES (?, ?, ?, ?, ?, ?::jsonb, ?::vector)";

        String vectorString = convertToVectorLiteral(profile.getEmbedding());

        jdbcTemplate.update(sql,
                profile.getFullName(),
                profile.getEmail(),
                profile.getExperienceYears(),
                profile.getCitizenship(),
                profile.getRawText(),
                profile.getDynamicSkillsJson(),
                vectorString
        );

        return profile;
    }

    private String convertToVectorLiteral(float[] vector) {
        if (vector == null) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
