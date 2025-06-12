package com.company.migration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "PostgreSQL Migration API",
        version = "1.0.0",
        description = "RESTful API for managing PostgreSQL database migrations using Liquibase",
        contact = @Contact(
            name = "Migration Team",
            email = "migration@company.com"
        )
    )
)
public class PostgreSQLMigrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostgreSQLMigrationApplication.class, args);
    }
}