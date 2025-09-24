package com.example.cloudlabs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.cloudlabs.repository")
@EntityScan("com.example.cloudlabs.entity")
public class CloudLabsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudLabsApplication.class, args);
    }

}
