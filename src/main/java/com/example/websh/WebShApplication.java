package com.example.websh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebShApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebShApplication.class, args);

        System.out.println("Загрузка контекста");
    }

}
