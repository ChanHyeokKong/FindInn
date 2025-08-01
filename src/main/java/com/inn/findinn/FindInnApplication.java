package com.inn.findinn;

import jakarta.persistence.Entity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.inn")
@ComponentScan(basePackages = "com.inn")
@EnableJpaRepositories(basePackages = "com.inn")
public class FindInnApplication {

    public static void main(String[] args) {
        SpringApplication.run(FindInnApplication.class, args);
    }

}
