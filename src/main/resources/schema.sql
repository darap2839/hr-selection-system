-- Подключение расширения векторов (если еще не подключено)
CREATE EXTENSION IF NOT EXISTS vector;

-- Создание таблицы вакансий (если не создана)
CREATE TABLE IF NOT EXISTS vacancies (
                                         id BIGSERIAL PRIMARY KEY,
                                         title VARCHAR(255) NOT NULL,
    department VARCHAR(150) NOT NULL,
    min_experience INT NOT NULL DEFAULT 0,
    citizenship_req VARCHAR(100) NOT NULL,
    attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    embedding VECTOR(384)
    );

-- Создание таблицы резюме (если не создана)
CREATE TABLE IF NOT EXISTS resumes (
                                       id BIGSERIAL PRIMARY KEY,
                                       candidate_name VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    experience_years INT NOT NULL DEFAULT 0,
    citizenship VARCHAR(100) NOT NULL,
    dynamic_skills JSONB NOT NULL DEFAULT '{}'::jsonb,
    embedding VECTOR(384)
    );