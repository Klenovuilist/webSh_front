package com.example.websh.controllers;

import com.example.websh.cash.Cash;
import com.example.websh.clients.FeignClient;
import com.example.websh.dto.CounterDto;
import com.example.websh.dto.GroupProductDto;
import com.example.websh.dto.ProductDto;
import com.example.websh.service.AdminService;
import com.example.websh.service.CounterServices;
import com.example.websh.service.IndexService;
import com.example.websh.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final IndexService indexService;

    private final AdminService adminService;

    private final FeignClient feignForGroup;

    private final UserService userService;

    private final Cash cash;

    private final CounterServices counterServices;

    /**
     * Получение главной страницы
     * доступ для всех
     */

    @Value("${pathForSaveImage}")
    private String pathForSave; //путь сохранения картинки

    @GetMapping("/")
    public String indexPage(Model model, HttpServletRequest request){


        model.addAttribute("groups", cash.getListZeroGroupNoPrefix());

        model.addAttribute("userInfo", userService.getUserInfoFromToken(request));

        /**
         * создание счетчика посещений
         */
        counterServices.createCounter(request, userService.getUserInfoFromToken(request), "Main", "");

//        model.addAttribute("users", userService.getAllUsers());

        List<ProductDto> productDtoList = adminService.getListProductDtoByIdGroup("1");
        indexService.delPrefixNameProduct(productDtoList);
        model.addAttribute("products", productDtoList); //список продуктов


        if(! cash.mapInfo .isEmpty()){
            model.addAttribute("mapInfo", cash.getMapInfo());

        }



        return "index.html";
    }

    /**
     * Получение страницы одного товара
     * доступ для всех
     */

    @GetMapping("/index/product/{id}")
    public String productPageAdmin(@PathVariable("id") String prodId, Model model, HttpServletRequest request){




        ProductDto product = adminService.getProductDtoById(prodId);
        indexService.delPrefixNameProduct(product);

        List<String> listNameImages = adminService.getListNameImageProduct(UUID.fromString(prodId));

        model.addAttribute("userInfo", userService.getUserInfoFromToken(request));

        model.addAttribute("product", product);
        model.addAttribute("ListNameImages", listNameImages);


        /**
         * создание счетчика посещений
         */
        counterServices.createCounter(request
                , userService.getUserInfoFromToken(request)
                , "продукт"
                , product.getProduct_name());

        //добавление тегов и описания страницы для поисковика
        try {
            if(product.getTeg() != null){
                String [] teg = product.getTeg().split(";");
                if(teg.length > 1){
                    model.addAttribute("tegDescription",product.getTeg().split(";")[0]);
                    model.addAttribute("tegKeywords",product.getTeg().split(";")[1].trim());
                }
                else {
                    model.addAttribute("tegDescription",product.getTeg().split(";")[0]);
                    model.addAttribute("tegKeywords",product.getTeg().split(";")[0]);
                }
            }
        }
        catch (RuntimeException e){

        }

        model.addAttribute("mapDescription", adminService.getMapDescriptionProduct(product, listNameImages));


        model.addAttribute("zeroGroups",cash.getListZeroGroupNoPrefix());

        if(! cash.mapInfo .isEmpty()){
            model.addAttribute("mapInfo", cash.getMapInfo());
        }

        return "product.html";
    }

    /**
     * Получение собственной страницы пользователя
     * доступ для зарегистрированных
     */

    @GetMapping("/user/{id}")
    public String userInfo(@PathVariable("id") String userId, Model model){

        //        todo ввести проверку роли, id заменить на UUID
        model.addAttribute("userId", userId);

        return "user_page.html";
    }

    /**
     * Получение картинок по uuid(запрос от браузера)
     */
    @GetMapping(value = "/image/{id}")
    public ResponseEntity<byte[]> getServerImage(@PathVariable("id") String uuid) {

        try {
            ResponseEntity<Resource> response = feignForGroup.getImageGroupById(uuid);

            byte[] imageArrByte = response.getBody().getContentAsByteArray(); //массив байт из тела(картинки)

            // Установка заголовков

            String extension = response.getHeaders().getFirst("extension");

            HttpHeaders headers = new HttpHeaders(); //заголовки от сервера

            if (Objects.isNull(extension)) {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            else {
            switch (extension) {
                case ".jpg":
                case ".jpeg":
                    headers.setContentType(MediaType.IMAGE_JPEG);
                    break;
                case ".png":
                    headers.setContentType(MediaType.IMAGE_PNG);
                    break;
                case ".gif":
                    headers.setContentType(MediaType.IMAGE_GIF);
                    break;
                case ".bmp":
                    headers.setContentType(MediaType.valueOf("image/bmp"));
                    break;
                case ".ico":
                    headers.setContentType(MediaType.valueOf("image/x-icon"));
                    break;
                case ".tiff":
                case ".tif":
                    headers.setContentType(MediaType.valueOf("image/tiff"));
                    break;
                default:
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    break;
            }
            }
            headers.setContentLength(imageArrByte.length); // Размер изображения в байтах

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageArrByte); // Возвращаем массив байтов изображения

        }
        catch (IOException io){
            return ResponseEntity.badRequest().build(); // Ошибка сервера
        }


    }


    /**
     * получение страницы группы по uuid с продуктами
     */
    @GetMapping("/index/group/{id}")
    public String groupPage(@PathVariable("id") String uuidGroup, Model model, HttpServletRequest request) {

        if (cash.getListGroups().isEmpty()) {
           cash.refreshListGroup(); //обновление кеш листа с группами
        }
        // получение  группы по id из cash
        GroupProductDto groupProductDto = cash.getListGroupsNoPrefix().stream()
                .filter(gr -> gr.getGroupId().equals(UUID.fromString(uuidGroup)))
                .findFirst().orElse(null);



        if (Objects.nonNull(groupProductDto)) {

            /**
             * создание счетчика посещений
             */
            counterServices.createCounter(request
                    , userService.getUserInfoFromToken(request)
                    , "группа"
                    , groupProductDto.getGroupName());


            model.addAttribute("zeroGroups", cash.getListZeroGroupNoPrefix()); // список нулевых групп


            model.addAttribute("groupCurrent", groupProductDto); // группа

//            indexService.delPrefixNameGroup(groupProductDto.getListUnderGroups()); //удаление префиксов в имени групп
            model.addAttribute("groups", groupProductDto.getListUnderGroups()); //список подгрупп

            List<ProductDto> productDtoList = adminService.getListProductDtoByIdGroup(uuidGroup);
            indexService.delPrefixNameProduct(productDtoList);
            model.addAttribute("products", productDtoList); //список продуктов

            if(! cash.mapInfo .isEmpty()){
                model.addAttribute("mapInfo", cash.getMapInfo());
            }

            model.addAttribute("userInfo", userService.getUserInfoFromToken(request));
        }
        return "index_group.html";
    }

        /**
         *  * Получение картинок для товаров по uuid продукта и имени(запрос от браузера)
         */
        @GetMapping("/image/product/{idProd}/{nameImage}")
        public ResponseEntity<byte[]> getServerImageProduct(@PathVariable("idProd") String uuidProduct
                , @PathVariable("nameImage") String nameImage){

            try {
                ResponseEntity<Resource> response = feignForGroup.getImageProductById(uuidProduct, nameImage);

                byte[] imageArrByte = response.getBody().getContentAsByteArray(); //массив байт из тела(картинки)

                // Получение заголовка типа расширения картинки
                String extension = response.getHeaders().getFirst("extension");

                HttpHeaders headers = new HttpHeaders(); //заголовки для браузера

                if (Objects.isNull(extension)) {
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                }
                else {
                    switch (extension) {
                        case ".jpg":
                        case ".jpeg":
                            headers.setContentType(MediaType.IMAGE_JPEG);
                            break;
                        case ".png":
                            headers.setContentType(MediaType.IMAGE_PNG);
                            break;
                        case ".gif":
                            headers.setContentType(MediaType.IMAGE_GIF);
                            break;
                        case ".bmp":
                            headers.setContentType(MediaType.valueOf("image/bmp"));
                            break;
                        case ".tiff":
                        case ".tif":
                            headers.setContentType(MediaType.valueOf("image/tiff"));
                            break;
                        default:
                            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                            break;
                    }
                }
                headers.setContentLength(imageArrByte.length); // Размер изображения в байтах

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(imageArrByte); // Возвращаем массив байтов изображения

            }
            catch (IOException io){
                return ResponseEntity.badRequest().build(); // Ошибка сервера
            }



        }
}


