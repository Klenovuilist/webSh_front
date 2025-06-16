package com.example.websh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class Main_webSh_front {

    public static void main(String[] args) {
        SpringApplication.run(Main_webSh_front.class, args);

        System.out.println("Загрузка контекста");
    }

}
