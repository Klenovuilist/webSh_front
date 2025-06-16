package com.example.websh.exceptions;

public class NoValidRequest extends RuntimeException{
    public NoValidRequest(String message){
        super(message);
    }
}
