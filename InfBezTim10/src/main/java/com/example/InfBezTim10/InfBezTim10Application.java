package com.example.InfBezTim10;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class InfBezTim10Application {

    public static void main(String[] args) {
        SpringApplication.run(InfBezTim10Application.class, args);
    }

}
