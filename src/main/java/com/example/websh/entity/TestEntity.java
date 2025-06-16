package com.example.websh.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class TestEntity {

    public TestEntity() {
    }

    private UUID id;

    private String testName;

    private String testLogin;

    private Integer testPasswordUser;

    private String testRoleUser;

    private String testComment;

    private LocalDateTime testDataCreateUser;



//    @Transient
//    private String dataCreateParsing;



}
