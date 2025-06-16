package com.example.websh.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(NoValidRequest.class)//Обработчик ошибок типа NoValidRequest
    public ResponseEntity<?> NoValidRequestException(NoValidRequest noValidRequest){
            return ResponseEntity.badRequest().body(noValidRequest.getMessage());//при ошибке вернем ResponseEntity с  сообщением ошибки
    }



}
