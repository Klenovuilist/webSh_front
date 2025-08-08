package com.example.websh.controllers;

import com.example.websh.cash.Cash;
import com.example.websh.clients.FeignForGroup;
import com.example.websh.dto.GroupProductDto;
import com.example.websh.dto.ProductDto;
import com.example.websh.exceptions.ErrorMessage;
import com.example.websh.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Controller
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;

    private final FeignForGroup feignForGroup;

    private  final Cash cash;

//    public List<GroupProductDto> listGroups = new ArrayList<>();

    /**
     * Получение страницы администратора
     * доступ с ролью админ
     */

    @GetMapping("/index_admin")
    public String indexPageAdmin(Model model){

//        todo ввести проврку роли администратора
//        каталоги товаров, списки пользователей, комментариев

            cash.setListGroups(feignForGroup.getGroup().getBody()); //обновление списка групп в кэш

        model.addAttribute("parentGroup", cash.getListGroups());
        model.addAttribute("groups", cash.getListGroups());

        model.addAttribute("boolean", true);

        model.addAttribute("errorMessage", ErrorMessage.errorSave);
        ErrorMessage.errorSave = null;

        return "index_admin.html";
    }


    /**
     * Получение страницы одного товара
     * доступ с ролью админ
     */

//    @GetMapping("/product_admin/{id}")
//    public String productPageAdmin(@PathVariable("id") int prodId, Model model){
//
//        //        todo ввести проврку роли администратора, id заменить на UUID
//        model.addAttribute("productId", String.valueOf(prodId));
//
//        return "product_admin.html";
//    }

    /**
     * Получение страницы пользователя
     * доступ с ролью админ
     */

    @GetMapping("/user_info/{id}")
    public String userInfo(@PathVariable("id") int prodId, Model model){

        //        todo ввести проврку роли администратора, id заменить на UUID
        model.addAttribute("userId", String.valueOf(prodId));

        return "user_info.html";
    }

    /**
     * страница создания товара
     * доступ с ролью админ
     */

    @GetMapping("/product_admin/new")
    public String newProductPageAdmin(Model model){

        //        todo ввести проврку роли администратора, id заменить на UUID


        return "new_product.html";
    }


    @PostMapping("/index_admin/save_group/{id}")
    public String saveNameGroup(@PathVariable("id") String groupId, HttpServletRequest request){
//       String newNameGroup = request.getParameter("name_group" + groupId); //параметр запроса

        feignForGroup.changeGroup(adminService.createGroupDto(request, groupId));
//        adminService.changeNameGroup(listGroups, newNameGroup, UUID.fromString(groupId));

        return "redirect:/index_admin";
    }

    @PostMapping("/index_admin/create_group/{id}")
    public String createNewGroup(@PathVariable("id") String parrentGroupId){
//
//        if(parrentGroupId.equals("0")){
//            adminService.addUnderGroup(listGroups, null);
//        } else {
//            adminService.addUnderGroup(listGroups, UUID.fromString(parrentGroupId));
//        }
// Запрос на сервер
        feignForGroup.createGroup(parrentGroupId);

        return "redirect:/index_admin";
    }

    @PostMapping("/index_admin/del_group/{id}")
    public String deleteNewGroup(@PathVariable("id") String groupId){

        feignForGroup.deleteGroup(groupId);

//        adminService.deleteGroup(listGroups, UUID.fromString(groupId));
        return "redirect:/index_admin";
    }

    /**
     * Изменение родительской группы
     */
    @PostMapping("/index_admin/change_parrent_group")
    public String changeParrentGroup(HttpServletRequest request){

        feignForGroup.changeGroup(adminService.createGroupDto(request));

        return "redirect:/index_admin";
    }

    /**
     * Загрузка изображения группы на сервер
     */

    @PostMapping(value = "/upload_image_group/{id}" )
    public String handleFileUpload(@RequestParam("file") MultipartFile file
            , RedirectAttributes redirectAttributes
            , @PathVariable("id") String groupId) {

        try {
            //тип файла
            String extension = file.getOriginalFilename()
                    .substring(file.getOriginalFilename().lastIndexOf("."))
                    .toLowerCase();

             byte[] imageByte = file.getBytes();

            //загрузка изображений на сервер
            feignForGroup.loadImageGroup(imageByte // массив файла
                    , groupId //имя файла
                    , extension); // расширение файла

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return "redirect:/index_admin";
    }


    /**
     * получение группы по uuid
     */
    @GetMapping("/index_admin/group/{id}")
    public String adminGroupPage(@PathVariable("id") String uuidGroup, Model model){

        if (cash.getListGroups().isEmpty()) {
            cash.setListGroups(feignForGroup.getGroup().getBody()); //обновление кеш листа с группами
        }
        // Поиск в списке групп нужной по id
        GroupProductDto groupProductDto = cash.getListGroups().stream()
                .filter(gr -> gr.getGroupId().equals(UUID.fromString(uuidGroup)))
                .findFirst().orElse(null);
        if (Objects.nonNull(groupProductDto)){
            model.addAttribute("group", groupProductDto);
            model.addAttribute("groups",groupProductDto.getListUnderGroups());
            model.addAttribute("products", adminService.getListProductDtoByIdGroup(uuidGroup));

        }

//        model.addAttribute("products", adminService.getListProductDtoByIdGroup(uuidGroup));



        return "index_admin_group.html";


    }

    /**
     * создание нового продукта
     */
    @GetMapping("/product_admin/add_new")
    public String adminProductNew(Model model, HttpServletRequest request){

       ProductDto productDto =  adminService.createNewProduct();
//        feignForGroup.getImageProductById("0"); //получение картинки по умолчанию



        // список картинок для продукта
        List<String> images = adminService.getListNameImageProduct(productDto.getProductId());

//        List<String> images = new ArrayList<>();
//        images.add("1----0");
        model.addAttribute("product", productDto);
        model.addAttribute("ListNameImages", images); //лист с именами картинок
        model.addAttribute("groups", cash.getListGroups());

        model.addAttribute("currentGroupId", request.getParameter("id_group")); //текущее значение группы для товара

        return "new_product.html";
    }

    /**
     * Загрузка изображения продукта на сервер
     */


        @PostMapping(value = "/upload_image_product/{id}" )
        public String uploadImageProduct(@RequestParam("file") MultipartFile file
                , HttpServletRequest request
                , RedirectAttributes redirectAttributes
                , @PathVariable("id") String productId) {

            String currentGroupId = request.getParameter("currentGroupId");

            if (currentGroupId != null){
                redirectAttributes.addAttribute("currentGroupId", currentGroupId);
            }
        try {
            //тип файла
            String extension = file.getOriginalFilename()
                    .substring(file.getOriginalFilename().lastIndexOf("."))
                    .toLowerCase();

            byte[] imageByte = file.getBytes();

            //загрузка изображений на сервер
            feignForGroup.loadImageProduct(imageByte // массив файла
                    , productId //имя файла
                    , extension); // расширение файла

        } catch (IOException e) {
            ErrorMessage.errorSave = e.getMessage();
            return "redirect:/product_admin/" + productId;

//            throw new RuntimeException(e);
        }

        return "redirect:/product_admin/" + productId;
    }

    /**
     * Получить страницу продукта для админа
     */
    @GetMapping("/product_admin/{productId}")
    public String adminProductById(@PathVariable("productId") String productId, Model model
                , @ModelAttribute("currentGroupId") Optional<String> currentGroupIdOptional){

        ProductDto productDto =  adminService.getProductDtoById(productId);
//        feignForGroup.getImageProductById("0"); //получение картинки по умолчанию

        // список картинок для продукта
        List<String> images = adminService.getListNameImageProduct(productDto.getProductId());

        if (cash.getListGroups().isEmpty()){ //обновить группы
            cash.refreshListGroup();
        }

//        List<String> images = new ArrayList<>();
//        images.add("1----0");
        model.addAttribute("product", productDto);
        model.addAttribute("ListNameImages", images); //лист с именами картинок
        model.addAttribute("groups", cash.getListGroups());

        if(Objects.nonNull(productDto.getGroupsId())){
            model.addAttribute("currentGroupId", productDto.getGroupsId());
        }
        else {
            model.addAttribute("currentGroupId", cash.getListGroups().get(0).getGroupId());
        }

        if (currentGroupIdOptional.isPresent() && ! currentGroupIdOptional.get().isEmpty()){
            model.addAttribute("currentGroupId", currentGroupIdOptional.get());
        }



        // Получение flash attribute
//        if(currentGroupIdOptional.isPresent()){
//            model.addAttribute("currentGroupId", currentGroupIdOptional.get()); // добавляем currentGroupId в модель
//        }
        return "new_product.html";
    }

    /**
     * Сохранить изменения в продукте
     */
    @PostMapping("/index_admin/save_product/{productId}")
    public String saveProduct(@PathVariable("productId") String productId, HttpServletRequest request,
                              RedirectAttributes redirectAttributes){

        ProductDto productDto = new ProductDto();
        productDto.setProductId(UUID.fromString(productId));

        adminService.setParamfromFORMForProductDto(productDto, request);

        feignForGroup.saveProduct(productDto);

        redirectAttributes.addFlashAttribute("currentGroupId" , productDto.getGroupsId());

        return "redirect:/product_admin/" + productId;

    }


    @PostMapping("/index_admin/del_product/{productId}")
    public String deleteProduct(@PathVariable("productId") String productId){


       String parrentUuid = feignForGroup.deleteProduct(productId)
               .getBody()
               .getGroupId()
               .toString();


        return "redirect:/index_admin/group/" + parrentUuid;

    }


    /**
     * Получить страницу со ВСЕМИ продуктами для админа
     */
    @GetMapping("/product/all")
    public String adminProductAll(Model model) {

        adminService.getProductAllForGroup();

        model.addAttribute("groups", cash.getListGroups());

        return "index_admin_All_product.html";
    }
}
