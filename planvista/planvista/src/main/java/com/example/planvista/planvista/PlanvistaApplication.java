package com.example.planvista.planvista;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.planvista")
public class PlanvistaApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlanvistaApplication.class, args);
    }
}
