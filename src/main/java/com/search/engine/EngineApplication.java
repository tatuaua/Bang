package com.search.engine;

import com.search.engine.repository.DatabaseRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class EngineApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(EngineApplication.class, args);
        DatabaseRepository databaseRepository = context.getBean(DatabaseRepository.class);
        databaseRepository.init();
    }
}
