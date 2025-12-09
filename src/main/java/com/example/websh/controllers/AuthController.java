package com.example.websh.controllers;

import com.example.websh.dto.UserDto;
import com.example.websh.service.CounterServices;
import com.example.websh.service.JwtService;
import com.example.websh.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    private final UserService userService;

    private final CounterServices counterServices;


    /**
     * Страница Регистрация нового пользователя
     * @return
     */
    @GetMapping("/registration")
    public String registrationUser(Model model
            , @ModelAttribute("loginExist") Optional<String> loginExistOpt
    , HttpServletRequest request){

        /**
         * создание счетчика посещений
         */
        counterServices.createCounter(request
                , userService.getUserInfoFromToken(request)
                , "registration"
                , "");

        String loginExist = loginExistOpt.orElseGet(null);
        model.addAttribute("loginExist", loginExist);

        return "users_regisration.html";
    }


    /**
     * Регистрация нового пользователя - отправка данных при нажатии кнопки "Зарегистрироваться"
     * @return
     */
    @PostMapping("/register")
    public String registrUser(HttpServletRequest request, Model model,  RedirectAttributes redirectAttributes){

     UserDto userDto = userService.saveUser(request);

        /**
         * создание счетчика посещений
         */
        counterServices.createCounter(request
                , userService.getUserInfoFromToken(request)
                , "зарегистрировать"
                , userDto.getLogin());

     // если логин уже существет
     if (Objects.isNull(userDto)){

         redirectAttributes.addFlashAttribute("loginExist", request.getParameter("userLogin"));

         return "redirect:/registration";
     }

     userService.sendVerificationEmail(userDto); //

     model.addAttribute("user", userDto);

        return "massege.html";
    }



    /**
     * Страница авторизации пользователя
     */
    @GetMapping("/athurizathion")
    public String athurizathion(Model model,
            HttpServletResponse response, HttpServletRequest request
            , @ModelAttribute("errorLogin") Optional<String> errorLoginOpt
            , @ModelAttribute("noVerify") Optional<String> noVerifyOpt){


        boolean showErrorMessage = errorLoginOpt.filter(s -> "true".equalsIgnoreCase(s)).isPresent();
        boolean noVerify = noVerifyOpt.filter(s -> "true".equalsIgnoreCase(s)).isPresent();

        /**
         * создание счетчика посещений
         */
        counterServices.createCounter(request
                , userService.getUserInfoFromToken(request)
                , "авторизация"
                , "");

        if (showErrorMessage) {
            // Удаление cookie
            Cookie cookie = new Cookie("token", "");
            cookie.setMaxAge(0); // Срок жизни cookie в секундах
            cookie.setPath("/");     // Путь доступности cookie для всего сайта

            // Установка cookie в ответ
            response.addCookie(cookie);
        }

        //  атрибут в модель
        model.addAttribute("errorLogin", showErrorMessage);
        model.addAttribute("noVerify", noVerify);



        return "athurizathion.html";
    }


    /**
     * Ввод данных пользователя в форме авторизации для входа
     * @param request
     * @param response
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/login")
    public String loginEnter(HttpServletRequest request, HttpServletResponse response
            , RedirectAttributes redirectAttributes){

        UserDto userDto = userService.getUserByLogin(request.getParameter("userLogin"));

        /**
         * создание счетчика посещений
         */
        counterServices.createCounter(request
                , userService.getUserInfoFromToken(request)
                , "вход пользователя"
                , userDto.getLogin());

        if (userDto == null
                || userDto.getLogin() == null
                || userDto.getPsswordUser() == null
                || ! userDto.getLogin().equals(request.getParameter("userLogin"))
                || ! userDto.getPsswordUser().equals(request.getParameter("password"))){

            redirectAttributes.addFlashAttribute("errorLogin" , "true");
            return "redirect:/athurizathion";
        }

        // если нет подтверждения записи по ссылке из письма
        if(! userDto.isBoolverify()){
            redirectAttributes.addFlashAttribute("noVerify" , "true");
            return "redirect:/athurizathion";
        }

        // Генерация токена для пользователя если логин и пароль верны
        String token = jwtService.generateJWToken(userDto);


        Cookie cookie = new Cookie("token", token);
        cookie.setMaxAge(86400); // Срок жизни cookie в секундах (пример: 1 сутки)
//        cookie.setSecure(true);  // Только HTTPS соединение
//        cookie.setHttpOnly(true); // Доступ к cookie только через HTTP(S), не JS
        cookie.setPath("/");     // Путь доступности cookie для всего сайта

        // Установка cookie в ответ
        response.addCookie(cookie);

//        todo доделать отправку данных пользователя на сервер, и получение токена в куках

        if (userDto.getRoleUser().equals("ROLE_ADMIN")){
            return "redirect:/index_admin";
        }
        return "redirect:/";


    }

    /**
     * Подтверждение регистрации по ссылке из письма пользователю
     */
    @GetMapping("/users/verify/{idUser}")
    public String verifyUser(@PathVariable("idUser") String userId, HttpServletResponse response, HttpServletRequest request){

        UserDto userDto = userService.getUserById(userId);

        /**
         * создание счетчика посещений
         */
        counterServices.createCounter(request
                , userService.getUserInfoFromToken(request)
                , "подтверждение mail"
                , userDto.getMail() + "  " + userDto.getLogin());

        //Установка подтверждения аккаунта
        if (! userDto.isBoolverify()){
            userDto.setBoolverify(true); ;
        }

        userDto = userService.updateUserAndSave(userDto);

        // Генерация токена для пользователя
        String token = jwtService.generateJWToken(userDto);

        Cookie cookie = new Cookie("token", token);
        cookie.setMaxAge(86400); // Срок жизни cookie в секундах (1 сутки)
        cookie.setPath("/");     // Путь доступности cookie для всего сайта

        // Установка cookie в ответ
        response.addCookie(cookie);

        return "redirect:/";
    }



}
