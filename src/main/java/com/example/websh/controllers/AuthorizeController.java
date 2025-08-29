package com.example.websh.controllers;


import com.example.websh.dto.ErrorAuthDto;
import com.example.websh.dto.JwtDto;

import com.example.websh.dto.UserDto;
import com.example.websh.service.JwtService;
import com.example.websh.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * Контроллер авторизации и регистрации,получения токена
 */
@RestController
@AllArgsConstructor
public class AuthorizeController {

    private final UserService userService;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager; // проводит аутентификацию пользовотеля

//    @PreAuthorize("\"permitAll()\"")
    @GetMapping("/authorize_user")
    public ResponseEntity<?> authorize(/*@RequestBody UserDtoAuthen userDtoAuthen*/) {

/**
 * аутентификация пользовотеля по данным из запроса (UserDto)
 */
        try {
            //authenticationManager проверяет существует пользователь в базе с данным логином и паролем(используется UserService implements UserDetailsService)
            // если пользователь найден то возврщ. Authentication содержащий все данные о пользаователе
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
//                    userDtoAuthen.getUserLogin(), // имя пользователя
//                    userDtoAuthen.getPasswordUser()// пароль пользователя
                    "rita", "123"
            ));
            System.out.println(authentication.getName());//имя пользователя ппрошедшего аутентифик
            System.out.println(authentication.getAuthorities());// роли

            UserDto userDto = UserDto.builder()
                    .userName(authentication.getName())
                    .roleUser(authentication.getAuthorities().stream().findAny().orElseGet(null).toString())
                    .build();

            // генерировать токен с данными User, полученному из БД
            String token = jwtService.generateJWToken(userDto);

            //ответ new JwtDto с token, статус ok
            return ResponseEntity.ok(new JwtDto(token));

        }
        catch (BadCredentialsException e) { // если пользователь не найден
           return new ResponseEntity<>(new ErrorAuthDto(HttpStatus.UNAUTHORIZED.value(), "Неверный логин или пароль"), HttpStatus.UNAUTHORIZED);
        }
    }


    @PostMapping("/authorize_user")
    public ResponseEntity<?> authorize(HttpServletRequest request) {

/**
 * аутентификация пользовотеля по данным из запроса (UserDto)
 */
        try {
            //authenticationManager проверяет существует пользователь в базе с
            // данным логином и паролем(используется UserService implements UserDetailsService)
            // если пользователь найден то возврщ. Authentication содержащий все данные о пользаователе
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getParameter("login"), // имя пользователя
                    request.getParameter("password")// пароль пользователя
//                    "rita", "123"
            ));
            System.out.println(authentication.getName());//имя пользователя ппрошедшего аутентифик
            System.out.println(authentication.getAuthorities());// роли

            UserDto userDto = UserDto.builder()
                    .userName(authentication.getName())
                    .roleUser(authentication.getAuthorities().stream().findAny().orElseGet(null).toString())
                    .build();

            // генерировать токен с данными User, полученному из БД
            String token = jwtService.generateJWToken(userDto);

            //ответ new JwtDto с token, статус ok
            return ResponseEntity.ok(new JwtDto(token));

        }
        catch (BadCredentialsException e) { // если пользователь не найден
            return new ResponseEntity<>(new ErrorAuthDto(HttpStatus.UNAUTHORIZED.value(), "Неверный логин или пароль"), HttpStatus.UNAUTHORIZED);
        }
    }
//        @PreAuthorize("permitAll()")
        @GetMapping("/api/login")
        public String loginPage(){

        return "form login";
        }
}
