package com.example.websh.cash;

import com.example.websh.clients.FeignForGroup;
import com.example.websh.dto.GroupProductDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * кэш для хранения постоянных данных или обновляемых админом
 */
@Component
@Getter
@Setter
@RequiredArgsConstructor
public class Cash {

    private final FeignForGroup feignForGroup;

    public List<GroupProductDto> listGroups = new ArrayList<>();

    public void refreshListGroup(){
        listGroups = feignForGroup.getGroup().getBody();
    }


}
