package com.example.websh.controllers;

import com.example.websh.cash.Anchor;
import com.example.websh.cash.Cash;
import com.example.websh.clients.FeignClient;
import com.example.websh.dto.ProductDto;
import com.example.websh.dto.UserDto;
import com.example.websh.exceptions.ErrorMessage;
import com.example.websh.service.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class UserController {

    private final JwtService jwtService;

    private final UserService userService;

    private final AdminService adminService;

    private final IndexService indexService;

    private final FeignClient feignClient;

    private final File3DDtoService file3DDtoService;

    private final Cash cash;

    private final Anchor anchor;

    private final CounterServices counterServices;


    /**
     * Страница пользователя и его заказов для админа
     * @return
     */
    @GetMapping("/index_admin/user/{userId}")
    public String userPage(Model model, @PathVariable("userId") String userId){

        model.addAttribute("user", userService.getUserById(userId));

//        model.addAttribute(userId)

        model.addAttribute("listFile", file3DDtoService.getListFile3DDtoByUsersId(userId, false));
        model.addAttribute("listFileDelete", file3DDtoService.getListFile3DDtoByUsersId(userId, true));

        model.addAttribute("listStatus", cash.getListStatus());

        return "admin_user_page.html";
    }

    /**
     * изменить данные пользователя (самим пользователем)
     */
    @PostMapping("/index_user_update/{userId}")
    public String saveUserPage(@PathVariable("userId") String userId, HttpServletRequest request
            , HttpServletResponse response, RedirectAttributes redirectAttributes){

        UserDto userDto = userService.getUserById(userId);

        /**
         * создание счетчика посещений
         */
        counterServices.createCounter(request
                , userService.getUserInfoFromToken(request)
                , "обновление user"
                , userDto.getLogin());

       String message = userService.updateUser(userDto, userId, request);

       if(!message.isBlank()){
           redirectAttributes.addFlashAttribute("message" , message);

       }
        // Установка токена user в куки
        Cookie cookie = new Cookie("token", jwtService.generateJWToken(userDto));
        cookie.setMaxAge(1200000); // Срок жизни cookie в секундах
        cookie.setPath("/");     // Путь доступности cookie для всего сайта

        // Установка cookie в ответ с данными пользователя реальными или по умолчанию
        response.addCookie(cookie);


        return "redirect:/index_user_page";
    }


    @GetMapping("/index_user_page")
    public String userPage(Model model, HttpServletRequest request
                            , @ModelAttribute("message") Optional<String> messageOpt){

        Map<String, String> mapUserInfo = userService.getUserInfoFromToken(request);

        model.addAttribute("userInfo", mapUserInfo);

        UserDto userDto = userService.getUserDtoByMapUserInfo(mapUserInfo); // пользователь по токену

        /**
         * создание счетчика посещений
         */
        counterServices.createCounter(request
                , userService.getUserInfoFromToken(request)
                , "user_page"
                , userDto.getLogin());

        String isPasswordUser = userService.isDefoultPassword(userDto.getId()); // пароль из БД для определения пароля по умолчанию

        model.addAttribute("passwordUser", isPasswordUser);

        messageOpt.ifPresent(message ->{
            model.addAttribute("message", message);
        });

        return "user_page.html";
    }


    /**
     * Страница заказа на печать
     * @return
     */
    @GetMapping("/index/order3d")
    public String orderPage(Model model, HttpServletRequest request, HttpServletResponse response){

        Map<String, String> mapUserInfo = userService.getUserInfoFromToken(request);

        model.addAttribute("userInfo", mapUserInfo);

        UserDto userDto = userService.getUserDtoByMapUserInfo(mapUserInfo); // пользователь по токену
        //получить пароль если он по умолчанию сгенерированный

        /**
         * создание счетчика посещений
         */
        counterServices.createCounter(request
                , userService.getUserInfoFromToken(request)
                , "order_page"
                , userDto.getLogin());

        String isPasswordUser = userService.isDefoultPassword(userDto.getId()); // пароль из БД для определения пароля по умолчанию
        model.addAttribute("passwordUser", isPasswordUser);



        // Установка токена в куки
        Cookie cookie = new Cookie("token", jwtService.generateJWToken(userDto));
        cookie.setMaxAge(1200000); // Срок жизни cookie в секундах
        cookie.setPath("/");     // Путь доступности cookie для всего сайта

        // Установка cookie в ответ с данными пользователя реальными или по умолчанию
        response.addCookie(cookie);

        model.addAttribute("listFile", file3DDtoService.getListFile3DDtoByUsersId(userDto.getId().toString(), false));

        //получить лист моделей сайта доступных для заказа
        List<ProductDto> listProduct = indexService.getProductsFromOrder(cash.getListGroups());
        model.addAttribute("list_product", listProduct);

        //

        System.out.println();

        return "index_order_page.html";
    }

    /**
     * Загрузить файл 3Д на печать
     */
    @PostMapping("/upload_file/{userId}")
    public String orderPage(@PathVariable("userId") String userId
        ,@RequestParam("file") MultipartFile file, HttpServletRequest request
    , HttpServletResponse response){

        // проверка наличия токена
       if(! userService.isExistToken(request)){
           return "redirect:/index/order3d";
       }
       //сохранить или получить существующего пользователя из БД по токену
      UserDto userDto = userService.getUserById(userId);

       //если пользователя в БД не нашлось создать и сохранить нового user
       if (Objects.isNull(userDto)){
           userDto = userService.saveUser(userService.getUserDtoByToken(request));

           // Установка токена в куки
           Cookie cookie = new Cookie("token", jwtService.generateJWToken(userDto));
           cookie.setMaxAge(1200000); // Срок жизни cookie в секундах
           cookie.setPath("/");     // Путь доступности cookie для всего сайта

           // Установка cookie в ответ с данными пользователя реальными или по умолчанию
           response.addCookie(cookie);
       }

        /**
         * создание счетчика посещений
         */
        counterServices.createCounter(request
                , userService.getUserInfoFromToken(request)
                , "load file"
                , file.getOriginalFilename());
           try {

               //todo возможно определять расширение здесь не понадобится
                //тип файла(расширение)
                String extension = file.getOriginalFilename()
                        .substring(file.getOriginalFilename().lastIndexOf("."))
                        .toLowerCase();

                byte[] fileByte = file.getBytes();

                //загрузка файла на сервер
                feignClient.loadFile3D(fileByte // массив файла
                        , userDto.getId().toString() // userId всегда реальный из БД
                        , file.getOriginalFilename() //имя файла
                        , extension); // расширение файла



//                anchor.setPoint("group_name_" + groupId); //точка перехода к группе после загрузки сообщения

            } catch (IOException e) {
                ErrorMessage.error = "Не удалось загрузить изображение \n" + e.getMessage();
//                anchor.setPoint("error"); //точка перехода к группе после загрузки сообщения
                throw new RuntimeException(e);
            }

            return "redirect:/index/order3d";
        }


    /**
     * Сохранение файла с описанием в БД в т.ч. с изменениями
     */
    @PostMapping("/save_file3DDto/{fileId}")
    public String saveFile3DDto(@PathVariable("fileId") String fileId
            , HttpServletRequest request){

        file3DDtoService.saveFile3DDtoIfExist(request, fileId);

        /**
         * создание счетчика посещений
         */
        counterServices.createCounter(request
                , userService.getUserInfoFromToken(request)
                , "save file3DDto"
                , "fileId: " + fileId);

        //на какую страницу вернуться
        if (request.getParameter("page") != null && request.getParameter("page").equals("index_order_page")){
            return "redirect:/index/order3d";
        }
        if (request.getParameter("page") != null && request.getParameter("page").equals("admin_user_page")){
            return "redirect:/index_admin/user/" + request.getParameter("user_id");
        }
        return "redirect:/";
    }

    /**
     * Удалить файл (поставить флаг на удаление, из БД не удаляется)
     */
    @GetMapping("/delete_file/{fileId}")
    public String deleteFile3DDto (@PathVariable("fileId") String fileId
            , HttpServletRequest request){
// сохранить изменения флага на удаленн в записи БД
        file3DDtoService.saveFile3DDtoIfExist(request, fileId);


        /**
         * создание счетчика посещений
         */
        counterServices.createCounter(request
                , userService.getUserInfoFromToken(request)
                , "delete_file"
                , "fileId: " + fileId);

        if (request.getParameter("page") != null && request.getParameter("page").equals("index_order_page")){
            return "redirect:/index/order3d";
        }
        if (request.getParameter("page") != null && request.getParameter("page").equals("admin_user_page")){
            return "redirect:/index_admin/user/" + request.getParameter("user_id");
        }
        return "redirect:/";
    }

    /**
     * Добавить продукты для пользоваьтеля из БД
     */
    @GetMapping("/order_product/{userId}/{productId}")
    public String orderProduct(@PathVariable("userId") String userId,
                               @PathVariable("productId") String productId
                            , HttpServletRequest request
                            , HttpServletResponse response ){

        //получить существующего пользователя из БД по токену
        UserDto userDto = userService.getUserById(userId);

        /**
         * создание счетчика посещений
         */
        counterServices.createCounter(request
                , userService.getUserInfoFromToken(request)
                , "order prod from BD"
                , "product_Id: " + productId);

        //если пользователя в БД не нашлось создать и сохранить нового user
        if (Objects.isNull(userDto)){
            userDto = userService.saveUser(userService.getUserDtoByToken(request));

            // Установка токена в куки
            Cookie cookie = new Cookie("token", jwtService.generateJWToken(userDto));
            cookie.setMaxAge(1200000); // Срок жизни cookie в секундах
            cookie.setPath("/");     // Путь доступности cookie для всего сайта

            // Установка cookie в ответ с данными пользователя реальными или по умолчанию
            response.addCookie(cookie);
        }

       userService.orderProduct(userDto.getId().toString(), productId);

        return "redirect:/index/order3d";
    }

    /**
     * Выйти из профиля (обнулить все куки)
     */
    @GetMapping("/logout_user")
    public String logoutUser(HttpServletRequest request, HttpServletResponse response){

        for(Cookie cookie: request.getCookies()){
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        return "redirect:/";
    }

    }








