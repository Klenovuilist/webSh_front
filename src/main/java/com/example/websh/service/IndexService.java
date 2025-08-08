package com.example.websh.service;

import com.example.websh.cash.Cash;
import com.example.websh.clients.FeignForGroup;
import com.example.websh.dto.GroupProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndexService {

    private final FeignForGroup feignForGroup;

    private final Cash cash;

    /**
     * Получение листа корневых групп
     */
    public List<GroupProductDto> getListZeroGroup(){

        if (cash.getListGroups().isEmpty()){
            cash.setListGroups(feignForGroup.getGroup().getBody());
        }
         if(! cash.getListGroups().isEmpty()){
             return cash.getListGroups().stream()
                     .filter(gr -> gr.getParrentId() == null) // корневые элементы в листе
                     .collect(Collectors.toList());
         }
        return cash.getListGroups();
    }

}
