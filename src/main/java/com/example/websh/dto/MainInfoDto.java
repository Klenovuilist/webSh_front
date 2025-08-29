package com.example.websh.dto;


import lombok.*;

import java.time.LocalDateTime;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor


public class MainInfoDto {


    private long mainInfoId;

    private String info;

    private LocalDateTime dataCreate;
}
