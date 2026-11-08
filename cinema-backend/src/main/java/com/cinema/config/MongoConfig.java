package com.cinema.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * MongoDB Configuration
 * Enables @CreatedDate and @LastModifiedDate auditing on all Documents.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
    // Spring Boot auto-configures the MongoClient from application.properties.
    // @EnableMongoAuditing activates automatic population of createdAt/updatedAt fields.
}
