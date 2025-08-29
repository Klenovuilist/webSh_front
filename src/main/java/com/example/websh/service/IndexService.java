package com.example.websh.service;

import com.example.websh.clients.FeignClient;
import com.example.websh.dto.GroupProductDto;
import com.example.websh.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexService {

    private final FeignClient feignForGroup;


    /**
     * Удаление префикса от имени группы для отображения на главной странице
     */
    public void delPrefixNameGroup(List<GroupProductDto> groupList){

        for (GroupProductDto group: groupList){
            if(group.getGroupName().isEmpty()){
                continue;
            }
            char[] arrName = group.getGroupName().toCharArray();

                StringBuilder currentName = new StringBuilder();
                boolean isFoundPrefix = false;
            for (char ch: arrName){

                if((ch == ' ' || ch == '.' || ch == ',') && currentName.isEmpty()){
                    continue;
                }

                if(! isFoundPrefix && ! currentName.isEmpty() && (ch == '.' || ch == ',')){
                    currentName.delete(0, currentName.length());
                    isFoundPrefix = true;
                    continue;
                }
                currentName.append(ch);
            }
            group.setGroupName(currentName.toString());
        }

    }

    /**
     * Удаление префикса от имени группы
     */
    public void delPrefixNameGroup(GroupProductDto group){

            if(group == null){
                return;
            }
            char[] arrName = group.getGroupName().toCharArray();

            StringBuilder currentName = new StringBuilder();
            boolean isFoundPrefix = false;
            for (char ch: arrName){

                if((ch == ' ' || ch == '.' || ch == ',') && currentName.isEmpty()){
                    continue;
                }

                if(! isFoundPrefix && ! currentName.isEmpty() && (ch == '.' || ch == ',')){
                    currentName.delete(0, currentName.length());
                    isFoundPrefix = true;
                    continue;
                }
                currentName.append(ch);
            }
            group.setGroupName(currentName.toString());
        }


    /**
     * Удаление префикса от имени продукта
     */
    public void delPrefixNameProduct(List<ProductDto> productList){

        for (ProductDto group: productList){
            if(group.getProduct_name().isEmpty()){
                continue;
            }
            char[] arrName = group.getProduct_name().toCharArray();

            StringBuilder currentName = new StringBuilder();
            boolean isFoundPrefix = false;
            for (char ch: arrName){

                if((ch == ' ' || ch == '.' || ch == ',') && currentName.isEmpty()){
                    continue;
                }

                if(! isFoundPrefix && ! currentName.isEmpty() && (ch == '.' || ch == ',')){
                    currentName.delete(0, currentName.length());
                    isFoundPrefix = true;
                    continue;
                }
                currentName.append(ch);
            }
            group.setProduct_name(currentName.toString());
        }

    }

    /**
     * Удаление префикса от имени продукта
     */
    public void delPrefixNameProduct(ProductDto product){

        if(product == null){
            return;
        }
        char[] arrName = product.getProduct_name().toCharArray();

        StringBuilder currentName = new StringBuilder();
        boolean isFoundPrefix = false;
        for (char ch: arrName){

            if((ch == ' ' || ch == '.' || ch == ',') && currentName.isEmpty()){
                continue;
            }

            if(! isFoundPrefix && ! currentName.isEmpty() && (ch == '.' || ch == ',')){
                currentName.delete(0, currentName.length());
                isFoundPrefix = true;
                continue;
            }
            currentName.append(ch);
        }
        product.setProduct_name(currentName.toString());
    }
}
