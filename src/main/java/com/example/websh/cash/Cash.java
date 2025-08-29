package com.example.websh.cash;

import com.example.websh.clients.FeignClient;
import com.example.websh.dto.GroupProductDto;
import com.example.websh.dto.MainInfoDto;
import com.example.websh.service.AdminService;
import com.example.websh.service.IndexService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * кэш для хранения постоянных данных или обновляемых админом
 */
@Component
@Getter
@Setter
@RequiredArgsConstructor
public class Cash {

    private final FeignClient feignForGroup;

    private final IndexService indexService;

    private  final AdminService adminService;

    public List<GroupProductDto> listGroups = new ArrayList<>();

    public List<GroupProductDto> listGroupsNoPrefix = new ArrayList<>();

    public List<GroupProductDto> listZeroGroupNoPrefix = new ArrayList<>();


    @PostConstruct
    public void refreshListGroup(){
        // лист с группами для админа
//        listGroups = feignForGroup.getGroup().getBody();
        listGroups = adminService.addUnderGroupAndStructure(feignForGroup.getGroup().getBody());


        // лист с группами для пользователей (чистые имена)
//        listGroupsNoPrefix = feignForGroup.getGroup().getBody();
        listGroupsNoPrefix = adminService.addUnderGroupAndStructure(feignForGroup.getGroup().getBody());
        indexService.delPrefixNameGroup(listGroupsNoPrefix);



     // лист с корневыми элементами для пользователя (чистые имена)
        if(! listGroups.isEmpty()){
           listZeroGroupNoPrefix = listGroupsNoPrefix.stream()
                    .filter(gr -> gr.getParrentId() == null) // корневые элементы в листе
                    .collect(Collectors.toList());
        }
            }

    /**
     * map общей информации на сайте
     */
    public Map<Integer, MainInfoDto> mapInfo = new HashMap<>();


    @PostConstruct

    public void refreshMapInfo(){
        mapInfo = feignForGroup.getInfo().getBody();
    }









}
