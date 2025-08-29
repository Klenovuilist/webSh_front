package com.example.websh.controllers;

import com.example.websh.cash.Anchor;
import com.example.websh.cash.Cash;
import com.example.websh.clients.FeignClient;
import com.example.websh.dto.GroupProductDto;
import com.example.websh.dto.ProductDto;
import com.example.websh.exceptions.ErrorMessage;
import com.example.websh.service.AdminService;
import com.example.websh.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.*;

@Controller
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;

    private final FeignClient feignForGroup;

    private final UserService userService;

    private  final Cash cash;

    private  final Anchor anchor;

//    public List<GroupProductDto> listGroups = new ArrayList<>();

    /**
     * Получение страницы администратора
     * доступ с ролью админ
     */

    @GetMapping("/index_admin")
    public String indexPageAdmin(Model model){

//        todo ввести проврку роли администратора
//        каталоги товаров, списки пользователей, комментариев

//            cash.setListGroups(feignForGroup.getGroup().getBody());
            cash.refreshListGroup();//обновление списка групп в кэш

            cash.refreshMapInfo();

        List<GroupProductDto> parentGroup = new ArrayList<>(cash.getListGroups());
        parentGroup.add(GroupProductDto.builder()
                        .groupName("MAIN")
                .build());

        model.addAttribute("parentGroup", parentGroup);
        model.addAttribute("groups", cash.getListGroups());

        model.addAttribute("products", adminService.getListProductDtoByIdGroup("1"));

//        model.addAttribute("boolean", true);

        model.addAttribute("users", userService.getAllUsers());

        model.addAttribute("errorMessage", ErrorMessage.error);
        ErrorMessage.error = null;

        if(! cash.mapInfo .isEmpty()){
            model.addAttribute("mapInfo", cash.getMapInfo());
        }


        model.addAttribute("point", anchor.point); // переход на нужную точку страницы
        anchor.cleanAnhor();

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

        anchor.point = "group_name_" + groupId; //точка перехода на странице к сохраненной группе

        //условие остаться на той же странице
        if(request.getParameter("page") != null && request.getParameter("page").equals("index_admin_group")){

            cash.refreshListGroup();
            return "redirect:/index_admin/group/" + request.getParameter("id_group");
        }

        return "redirect:/index_admin";
    }

    @PostMapping("/index_admin/create_group/{id}")
    public String createNewGroup(@PathVariable("id") String parrentGroupId, HttpServletRequest request){

        // Запрос на сервер
        UUID uuidNewgroup = feignForGroup.createGroup(parrentGroupId).getBody();

        anchor.point = "group_name_" + uuidNewgroup; //точка перехода на странице к вновь созданной группе

        // условие остаться на той же странице
        if(request.getParameter("page") != null && request.getParameter("page").equals("index_admin_group")){

            cash.refreshListGroup();
            return "redirect:/index_admin/group/" + request.getParameter("id_group");
        }

        return "redirect:/index_admin";
    }

    @PostMapping("/index_admin/del_group/{id}")
    public String deleteNewGroup(@PathVariable("id") String groupId, HttpServletRequest request){

        feignForGroup.deleteGroup(groupId);

        Optional<GroupProductDto> OptParrentUuid = cash.listGroups.stream()
                .filter(group -> group.getGroupId().toString()
                        .equals(groupId))
                .findFirst();

        if(OptParrentUuid.isPresent()){
            anchor.setPoint("group_name_" + OptParrentUuid.get().getGroupId().toString()); //точка перехода на странице к сохраненной группе
        }

        // условие остаться на той же странице
        if(request.getParameter("page") != null && request.getParameter("page").equals("index_admin_group")){

            cash.refreshListGroup();
            return "redirect:/index_admin/group/" + request.getParameter("id_group");
        }


        return "redirect:/index_admin";
    }

    /**
     * Изменение родительской группы(переместить)
     */
    @PostMapping("/index_admin/change_parrent_group")
    public String changeParrentGroup(HttpServletRequest request){

        GroupProductDto groupDto = adminService.createGroupDto(request);
        feignForGroup.changeGroup(groupDto);
        cash.refreshListGroup();

        anchor.setPoint("group_name_" + groupDto.getGroupId().toString()); //точка перехода к группе после перемещения

        return "redirect:/index_admin";
    }

    /**
     * Загрузка изображения группы на сервер
     */

    @PostMapping(value = "/upload_image_group/{id}")
    public String handleFileUpload(@RequestParam("file") MultipartFile file
            , RedirectAttributes redirectAttributes
            , @PathVariable("id") String groupId,
            HttpServletRequest request) {

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

            anchor.setPoint("group_name_" + groupId); //точка перехода к группе после загрузки сообщения

        } catch (IOException e) {
            ErrorMessage.error = "Не удалось загрузить изображение \n" + e.getMessage();
            anchor.setPoint("error"); //точка перехода к группе после загрузки сообщения
            throw new RuntimeException(e);
        }

        // условие остаться на той же странице
        if(request.getParameter("page") != null && request.getParameter("page").equals("index_admin_group")){

//            cash.refreshListGroup();
            return "redirect:/index_admin/group/" + request.getParameter("id_group");
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

        List<GroupProductDto> parentGroup = new ArrayList<>(cash.getListGroups());
        parentGroup.add(GroupProductDto.builder()
                .groupName("MAIN")
                .build());

//        model.addAttribute("parentGroup", parentGroup);
        model.addAttribute("groups", parentGroup);

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
            ErrorMessage.error = e.getMessage();
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
                , @ModelAttribute("currentGroupId") Optional<String> currentGroupIdOptional){   //@ModelAttribute("currentGroupId") Optional<String> currentGroupIdOptional  для получения параметров из другого контроллера

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

        List<GroupProductDto> parentGroup = new ArrayList<>(cash.getListGroups());
        parentGroup.add(GroupProductDto.builder()
                .groupName("MAIN")
                .build());

//        model.addAttribute("parentGroup", parentGroup);
        model.addAttribute("groups", parentGroup);

//        if(Objects.nonNull(productDto.getGroupsId())){
            model.addAttribute("currentGroupId", productDto.getGroupsId());
//        }
//        else {
//            model.addAttribute("currentGroupId", cash.getListGroups().get(0).getGroupId());
//        }

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

        // условие остаться на той же странице index_admin/group
        if(request.getParameter("page") != null && request.getParameter("page").equals("index_admin_group")){

            anchor.setPoint("product_name_" + productId); //точка перехода на странице

            return "redirect:/index_admin/group/" + request.getParameter("id_group");
        }

        // условие остаться на той же странице index_admin/group
        if(request.getParameter("page") != null && request.getParameter("page").equals("index_admin")){

            anchor.setPoint("product_name_" + productId); //точка перехода на странице

            return "redirect:/index_admin" ;
        }

        // условие остаться на той же странице product/all
        if(request.getParameter("page") != null && request.getParameter("page").equals("index_admin_All_product")){

            anchor.setPoint("product_name_" + productId); //точка перехода на странице

            return "redirect:/product/all";

        }

        return "redirect:/product_admin/" + productId;

    }


    @PostMapping("/index_admin/del_product/{productId}")
    public String deleteProduct(@PathVariable("productId") String productId){

      GroupProductDto groupDto = feignForGroup.deleteProduct(productId).getBody();

      if (groupDto == null){
          return "redirect:/index_admin";
      }

       return "redirect:/index_admin/group/" + groupDto.getGroupId();

    }

    /**
     * Удаление картинки продукта
     * @param productId
     * @param nameImage
     * @param request
     * @param redirectAttributes
     * @return
     */
    @GetMapping("/del/image/{productId}/{nameImage}")
    public String deleteProduct(@PathVariable("productId") String productId
            , @PathVariable("nameImage") String nameImage
            , HttpServletRequest request,
                                RedirectAttributes redirectAttributes){

        String parrentUuid = request.getParameter( "currentGroupId");
        redirectAttributes.addFlashAttribute("currentGroupId", parrentUuid);

        feignForGroup.deleteImageProduct(productId, nameImage);

        return "redirect:/product_admin/" + productId;

    }


    /**
     * Получить страницу со ВСЕМИ продуктами для админа
     */
    @GetMapping("/product/all")
    public String adminProductAll(Model model) {

        cash.refreshListGroup();

        adminService.getProductAllForGroup(cash.getListGroups()); // обновить кэш и записать каждой группе продукты

        model.addAttribute("groups", cash.getListGroups());

        // создание новой группы с продуктами не вошедшими ни в один раздел
       cash.getListGroups().add(GroupProductDto.builder()
                .groupName("Продукты не входящие ни в один раздел")
                .listProduct(adminService.getProductNonGroup())
                .build());

        model.addAttribute("point", anchor.point); // переход на нужную точку страницы
        anchor.cleanAnhor();

        return "index_admin_All_product.html";
    }


    /**
     * Сохранить инфо дто и вернуть id записи
     */
    @PostMapping("/saveInfo")
    public String getInfo(HttpServletRequest request){
        adminService.saveInfo(request);

     anchor.point = "info_" + request.getParameter("infoId"); //точка перехода на странице к инфо

        cash.refreshMapInfo();

        return "redirect:/index_admin";
    }




}
