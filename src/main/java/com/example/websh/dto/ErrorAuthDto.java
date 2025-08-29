package com.example.websh.dto;

import lombok.Data;

import java.util.Date;

/**
 * ответ на неудачную аутентификацию пользователя
 */
@Data
public class ErrorAuthDto {
     private int status;
     private String message;
     private Date timeStamp; // время ответа


    public ErrorAuthDto(int status, String message) {
        this.status = status;
        this.message = message;
        this.timeStamp = new Date();
    }
}
