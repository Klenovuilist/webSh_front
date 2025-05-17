package com.example.websh.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {



    @GetMapping("/")
    public String index(){
        return "index 123123132\n 123123132\n123123132\n123123132\n123123132\n";
    }

    @GetMapping("/test")
    public String testRequest(){
        return "test";
    }

}
