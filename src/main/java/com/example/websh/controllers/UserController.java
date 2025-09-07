package com.example.websh.controllers;

import com.example.websh.cash.Anchor;
import com.example.websh.cash.Cash;
import com.example.websh.clients.FeignClient;
import com.example.websh.dto.UserDto;
import com.example.websh.exceptions.ErrorMessage;
import com.example.websh.service.File3DDtoService;
import com.example.websh.service.JwtService;
import com.example.websh.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@Controller
@AllArgsConstructor
public class UserController {

    private final JwtService jwtService;

    private final UserService userService;

    private final FeignClient feignClient;

    private final File3DDtoService file3DDtoService;

    private final Cash cash;

    private final Anchor anchor;


    /**
     * Страница пользователя и его заказов для админа
     * @return
     */
    @GetMapping("/index_admin/user/{userId}")
    public String userPage(Model model, @PathVariable("userId") String userId){

        model.addAttribute("user", userService.getUserById(userId));

//        model.addAttribute(userId)

        model.addAttribute("listFile", userService.getListFile3DDtoByUsersId(userId, false));
        model.addAttribute("listFileDelete", userService.getListFile3DDtoByUsersId(userId, true));

        model.addAttribute("listStatus", cash.getListStatus());

        return "admin_user_page.html";
    }


    /**
     * Страница заказа на печать
     * @return
     */
    @GetMapping("/index/order3d")
    public String orderPage(Model model, HttpServletRequest request, HttpServletResponse response){


        model.addAttribute("userInfo", userService.getUserInfoFromToken(request));

        UserDto userDto = userService.getUserDtoByToken(request);

        // Установка токена в куки
        Cookie cookie = new Cookie("token", jwtService.generateJWToken(userDto));
        cookie.setMaxAge(1200000); // Срок жизни cookie в секундах
        cookie.setPath("/");     // Путь доступности cookie для всего сайта

        // Установка cookie в ответ с данными пользователя реальными или по умолчанию
        response.addCookie(cookie);

        // список файлов по userDto.getId()
//        model.addAttribute("listFile", userService.getListFile3DUsers(userDto.getId().toString()));

        model.addAttribute("listFile", userService.getListFile3DDtoByUsersId(userDto.getId().toString(), false));
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

       //если пользователя в БД не нашлось создать нового
       if (Objects.isNull(userDto)){
           userDto = userService.saveUser(userService.getUserDtoByToken(request));

           // Установка токена в куки
           Cookie cookie = new Cookie("token", jwtService.generateJWToken(userDto));
           cookie.setMaxAge(1200000); // Срок жизни cookie в секундах
           cookie.setPath("/");     // Путь доступности cookie для всего сайта

           // Установка cookie в ответ с данными пользователя реальными или по умолчанию
           response.addCookie(cookie);
       }
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

        if (request.getParameter("page") != null && request.getParameter("page").equals("index_order_page")){
            return "redirect:/index/order3d";
        }
        if (request.getParameter("page") != null && request.getParameter("page").equals("admin_user_page")){
            return "redirect:/index_admin/user/" + request.getParameter("user_id");
        }
        return "redirect:/";
    }
    }




