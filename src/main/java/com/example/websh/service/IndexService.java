package com.example.websh.service;

import com.example.websh.cash.Cash;
import com.example.websh.clients.FeignClient;
import com.example.websh.dto.GroupProductDto;
import com.example.websh.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndexService {

    private final FeignClient feignForGroup;

    private final AdminService adminService;




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

    /**
     *Получение листа моделей (продуктов) сайта доступных для заказа
     */
    public List<ProductDto> getProductsFromOrder(List<GroupProductDto> listGroup){
        List<ProductDto> listProduct = new ArrayList<>();

        //получить все продукты для которых IdGroup == null
        listProduct.addAll(adminService.getListProductDtoByIdGroup("1"));

        //получить продукты все продукты всех групп кроме главной(id = null)
        for (GroupProductDto group: listGroup){
            listProduct.addAll(adminService.getListProductDtoByIdGroup(group.getGroupId().toString()));
        }

        List<ProductDto> listLoadProduct = listProduct.stream()
                .filter(productDto -> Objects.nonNull(productDto.getIsLoad()))
                .filter(productDto -> productDto.getIsLoad().equals("load")).collect(Collectors.toList());

        delPrefixNameProduct(listLoadProduct);
        return listLoadProduct;
    }
}
