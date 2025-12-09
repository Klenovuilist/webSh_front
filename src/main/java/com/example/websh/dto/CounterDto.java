package com.example.websh.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class CounterDto {

    private String id;


    private String parameter;


    private String userInfo;


    private String countOrder;


    private String countEnter;


    private LocalDateTime dataCreate;

    private String dataCreateParsing;


}

