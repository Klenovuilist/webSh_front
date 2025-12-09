package com.example.websh.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class File3DDto {


    private UUID id;

    private UUID userId;

    private String fileName;

    private String status;

    private String coast;

    private String material;

    private String comment;

    private boolean isDelete;

    private boolean isApproval;

    private LocalDateTime data_create;

    private String dataCreatePars;

    private String fileId;

    private List<CommentDto> commentList = new ArrayList<>();


}
