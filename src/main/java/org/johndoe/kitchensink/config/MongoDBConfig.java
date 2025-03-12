package org.johndoe.kitchensink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Configuration class for MongoDB.
 * Enables MongoDB auditing.
 */
@Configuration
@EnableMongoAuditing
public class MongoDBConfig {
    /**
     * Default constructor for MongoDBConfig.
     */
    public MongoDBConfig() {
    }
}