package com.example.websh.controllers;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    /**
     * Загрузка страницы для авторизации
     */
    @GetMapping("/auth")
    public String authUser(){
        return "athurizathion.html";
    }

    @GetMapping("/registration")
    public String regisrationUser(){
        return "users_regisration.html";
    }

    /**
     * Отправка данных для авторизации
     */
    @PostMapping("/auth")
    public String sendAuthUser(){


//        todo доделать отправку данных пользователя на сервер, и получение токена в куках

        return "redirect:/test_entity";
    }



}
