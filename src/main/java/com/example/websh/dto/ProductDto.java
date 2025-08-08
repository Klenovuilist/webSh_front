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


public class ProductDto {


    private UUID productId;

    private String product_name;

    private String productCategory;

    private String productArticul;

    private String productReference;

    private String productDescription;

    private String productCoast;

    private String productCount;

    private int productReserv;

    private UUID groupsId;

    @Builder.Default
    private List<UUID> usersEntity = new ArrayList<>();

    private LocalDateTime data_create_product;
}
