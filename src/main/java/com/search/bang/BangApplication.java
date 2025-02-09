package com.search.bang;

import com.search.bang.repository.DatabaseRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class BangApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(BangApplication.class, args);
        DatabaseRepository databaseRepository = context.getBean(DatabaseRepository.class);
        databaseRepository.init();
    }
}
