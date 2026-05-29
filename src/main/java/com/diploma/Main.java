package com.diploma.hrsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        // Запуск движка Spring Boot и автоконфигурации подключения к Postgres
        SpringApplication.run(Main.class, args);
    }
}
