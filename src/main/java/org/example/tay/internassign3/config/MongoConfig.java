package org.example.tay.internassign3.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "org.example.tay.internassign3.repository")
@EnableMongoAuditing
public class MongoConfig {
    // This class centralizes MongoDB configuration.
    // By keeping it here, slice tests like @WebMvcTest will not
    // trigger a search for mongoTemplate.
}
