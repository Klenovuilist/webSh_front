package com.example.websh.controllers;

import com.example.websh.cash.Cash;
import com.example.websh.clients.FeignForGroup;
import com.example.websh.dto.GroupProductDto;
import com.example.websh.service.AdminService;
import com.example.websh.service.IndexService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.core.io.InputStreamResource;

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

    private final FeignForGroup feignForGroup;

    private final Cash cash;

    /**
     * Получение главной страницы
     * доступ для всех
     */

    @Value("${pathForSaveImage}")
    private String pathForSave; //путь сохранения картинки

    @GetMapping("/index")
    public String indexPage(Model model){

        List<GroupProductDto> listZeroGroup = indexService.getListZeroGroup();
        model.addAttribute("groups", listZeroGroup);

        //todo  только для админов
//        adminService.getAndSaveImageGroups(listZeroGroup);


        return "index.html";
    }

    /**
     * Получение страницы одного товара
     * доступ для всех
     */

    @GetMapping("/index/product/{id}")
    public String productPageAdmin(@PathVariable("id") String prodId, Model model){

        model.addAttribute("product", adminService.getProductDtoById(prodId));
        model.addAttribute("ListNameImages", adminService.getListNameImageProduct(UUID.fromString(prodId)));
        model.addAttribute("zeroGroups",indexService.getListZeroGroup());

        return "product.html";
    }

    /**
     * Получение собственной страницы пользователя
     * доступ для зарегистрированных
     */

    @GetMapping("/user/{id}")
    public String userInfo(@PathVariable("id") int userId, Model model){

        //        todo ввести проверку роли, id заменить на UUID
        model.addAttribute("userId", String.valueOf(userId));

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
    public String groupPage(@PathVariable("id") String uuidGroup, Model model) {

        if (cash.getListGroups().isEmpty()) {
            cash.setListGroups(feignForGroup.getGroup().getBody()); //обновление кеш листа с группами
        }
        GroupProductDto groupProductDto = cash.getListGroups().stream()
                .filter(gr -> gr.getGroupId().equals(UUID.fromString(uuidGroup)))
                .findFirst().orElse(null);
        if (Objects.nonNull(groupProductDto)) {
            model.addAttribute("zeroGroups", indexService.getListZeroGroup()); // список нулевых групп
            model.addAttribute("group", groupProductDto); // группа
            model.addAttribute("groups", groupProductDto.getListUnderGroups()); //список подгрупп

            model.addAttribute("products", adminService.getListProductDtoByIdGroup(uuidGroup)); //список продуктов

        }
        return "index_group.html";
    }

        /**
         *  * Получение картинок для товаров по uuid и имени(запрос от браузера)
         */
        @GetMapping("/image/product/{idProd}/{nameImage}")
        public ResponseEntity<byte[]> getServerImageProduct(@PathVariable("idProd") String uuidProduct
                , @PathVariable("nameImage") String nameImage){

            try {
                ResponseEntity<Resource> response = feignForGroup.getImageProductById(uuidProduct, nameImage);

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


