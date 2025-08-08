package com.example.websh.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Builder
@Getter
@Setter
@AllArgsConstructor


public class GroupProductDto implements Serializable {

    public GroupProductDto() {

    }
    private UUID groupId;

    private String groupName;

    private UUID parrentId;

    private UUID slaveId;

    private int levelGroup;

    @Builder.Default
    private List<GroupProductDto> listUnderGroups = new ArrayList<>();

    @Builder.Default
    private List<ProductDto> listProduct = new ArrayList<>();


    @Override
    public String toString() {
        return "Group{" +
                "Name ='" + groupName + '\'' +
                ", level =" + levelGroup +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GroupProductDto that = (GroupProductDto) obj;
        return groupId.equals(that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId);
    }

}
