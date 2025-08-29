package com.example.websh.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserDto {

    private UUID id;

    private String userName;

    private String login;

    private String psswordUser;

    private String roleUser;

    private String comment;

    private LocalDateTime dataCreateUser;

    private boolean boolverify;

    private String mail;

    private String DataCreateParsing;
}
