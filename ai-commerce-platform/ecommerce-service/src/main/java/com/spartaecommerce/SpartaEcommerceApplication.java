package com.spartaecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SpartaEcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpartaEcommerceApplication.class, args);
    }

}
