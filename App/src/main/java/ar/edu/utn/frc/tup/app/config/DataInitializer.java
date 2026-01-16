package ar.edu.utn.frc.tup.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DataInitializer - Disabled because Flyway migrations handle all initial data.
 * Initial data is inserted via V1__Initial_Schema.sql migration script.
 */
@Slf4j
// @Component - Commented out to disable this initializer
public class DataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        log.info("DataInitializer disabled - data initialization handled by Flyway migrations");
    }
}
