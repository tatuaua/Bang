package com.search.bang.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DatabaseConfig {

    String URL;
    String driver;

    public DatabaseConfig(
            @Value("${spring.datasource.url}") String URL,
            @Value("${spring.datasource.driver-class-name}") String driver) {
        this.URL = URL;
        this.driver = driver;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(URL);
        return new JdbcTemplate(dataSource);
    }
}