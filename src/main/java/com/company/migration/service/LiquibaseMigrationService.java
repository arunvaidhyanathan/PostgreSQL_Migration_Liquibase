package com.company.migration.service;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.StringWriter;
import java.io.PrintWriter;

@Service
public class LiquibaseMigrationService {

    private static final Logger logger = LoggerFactory.getLogger(LiquibaseMigrationService.class);

    private final DataSource dataSource;

    @Value("${spring.liquibase.change-log:classpath:db/changelog/db.changelog-master.xml}")
    private String changeLogFile;

    public LiquibaseMigrationService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, Object> runMigration(String contexts) {
        Map<String, Object> result = new HashMap<>();
        StringWriter logWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(logWriter);
        
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            try (Liquibase liquibase = new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database)) {
                logger.info("Starting Liquibase migration with contexts: {}", contexts);
                
                Contexts contextsObj = contexts != null ? new Contexts(contexts) : new Contexts();
                liquibase.update(contextsObj);
                
                result.put("status", "SUCCESS");
                result.put("message", "Migration completed successfully");
                result.put("contexts", contexts);
                result.put("timestamp", System.currentTimeMillis());
                
                logger.info("Migration completed successfully with contexts: {}", contexts);
            }
            
        } catch (LiquibaseException | SQLException e) {
            logger.error("Migration failed: {}", e.getMessage(), e);
            e.printStackTrace(printWriter);
            
            result.put("status", "FAILED");
            result.put("message", e.getMessage());
            result.put("error", logWriter.toString());
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return result;
    }

    public Map<String, Object> rollbackMigration(int count) {
        Map<String, Object> result = new HashMap<>();
        StringWriter logWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(logWriter);
        
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            try (Liquibase liquibase = new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database)) {
                logger.info("Starting Liquibase rollback for {} changesets", count);
                
                liquibase.rollback(count, (String) null);
                
                result.put("status", "SUCCESS");
                result.put("message", String.format("Rollback of %d changesets completed successfully", count));
                result.put("rollbackCount", count);
                result.put("timestamp", System.currentTimeMillis());
                
                logger.info("Rollback completed successfully for {} changesets", count);
            }
            
        } catch (LiquibaseException | SQLException e) {
            logger.error("Rollback failed: {}", e.getMessage(), e);
            e.printStackTrace(printWriter);
            
            result.put("status", "FAILED");
            result.put("message", e.getMessage());
            result.put("error", logWriter.toString());
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return result;
    }

    public Map<String, Object> getMigrationStatus() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            try (Liquibase liquibase = new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database)) {
                List<liquibase.changelog.ChangeSet> unrunChangeSets = liquibase.listUnrunChangeSets(
                    new Contexts(), new LabelExpression());
                
                result.put("status", "SUCCESS");
                result.put("pendingChangesets", unrunChangeSets.size());
                result.put("changelogFile", changeLogFile);
                result.put("timestamp", System.currentTimeMillis());
                
                if (!unrunChangeSets.isEmpty()) {
                    result.put("nextChangeset", unrunChangeSets.get(0).toString());
                }
                
                logger.info("Migration status retrieved: {} pending changesets", unrunChangeSets.size());
            }
            
        } catch (LiquibaseException | SQLException e) {
            logger.error("Failed to get migration status: {}", e.getMessage(), e);
            
            result.put("status", "FAILED");
            result.put("message", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return result;
    }

    public Map<String, Object> validateChangelog() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            try (Liquibase liquibase = new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database)) {
                liquibase.validate();
                
                result.put("status", "SUCCESS");
                result.put("message", "Changelog validation passed");
                result.put("changelogFile", changeLogFile);
                result.put("timestamp", System.currentTimeMillis());
                
                logger.info("Changelog validation passed for: {}", changeLogFile);
            }
            
        } catch (LiquibaseException | SQLException e) {
            logger.error("Changelog validation failed: {}", e.getMessage(), e);
            
            result.put("status", "FAILED");
            result.put("message", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        
        return result;
    }
}