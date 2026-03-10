package org.example.tay.internassign3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
public class InternAssign3Application {

    public static void main(String[] args) {
        SpringApplication.run(InternAssign3Application.class, args);
    }

}
