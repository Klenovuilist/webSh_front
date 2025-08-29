package com.example.websh.service;


import com.example.websh.clients.FeignClient;
import com.example.websh.dto.UserDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Класс для работы с пользователями для security
 * берет пользователя из БД
 */
@Service
@RequiredArgsConstructor
    public class UserService implements UserDetailsService {


    private final FeignClient feignClient;

    private final JavaMailSender mailSender;

    private final JwtService jwtService;

    @org.springframework.beans.factory.annotation.Value("${my.domein.mail}")
    private String myDomeinMail;




    /**
     * Переопределенный метод получения данных пользователя из БД и перевод их в UserDetails
     * метод будет вызван автоматически при вводе логина в форме ввода "логин - пароль" от security
      */
    @Override
    public UserDetails loadUserByUsername(String userLogin) throws UsernameNotFoundException {

         UserDto userDto = feignClient.findUserByLogin(userLogin).getBody();// пользователь из БД

           return new User( // наследник от UserDetails

                    userDto.getLogin(),//имя
                    userDto.getPsswordUser(), // пароль, будет автоматически дехеширован
                    Collections.singletonList(new SimpleGrantedAuthority(userDto.getRoleUser())));// коллекция объектов GrantedAuthority, содержащих роли пользователя


    }



    // пользователь из БД
    public UserDto getUserByLogin(String userLogin){

        UserDto userDto = feignClient.findUserByLogin(userLogin).getBody();// пользователь из БД
        return userDto;
    }

    // пользователь из БД  по id
    public UserDto getUserById(String userId){

        UserDto userDto = feignClient.findUserById(userId).getBody();// пользователь из БД

        DateTimeFormatter dataFormater = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        if(Objects.nonNull(userDto)){
            userDto.setDataCreateParsing(dataFormater.format(userDto.getDataCreateUser()));
        }

        return userDto;
    }


    /**
     * Регистрация пользователя, сохранение в БД
     * @param request
     * @return
     */
    public UserDto registrUser(HttpServletRequest request) {

        UserDto userDto = getUserByLogin(request.getParameter("userLogin"));

        //если пользователь с логином существует то вернуть этого пользователя нового не сохранять
        if (Objects.nonNull(userDto)){
            return null;
        }

         userDto = UserDto.builder()
                .roleUser("ROLE_USER")
                .psswordUser(request.getParameter("password"))
                .userName("noName")
                .dataCreateUser(LocalDateTime.now())
                .login(request.getParameter("userLogin"))
                .mail(request.getParameter("email"))
                .build();

        return feignClient.saveUser(userDto).getBody();
    }

    /**
     * Изменение данных пользователя

     * @return
     */
    public UserDto updateUser(UserDto userDto) {

        return feignClient.updateUser(userDto).getBody();
    }

    /**
     * Отправка письма со ссылкой для подтверждения почты пользователя
     * @param
     */
    public void sendVerificationEmail(UserDto userDto)  {
        MimeMessage message = mailSender.createMimeMessage();


        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true);

            helper.setTo(userDto.getMail());
            helper.setFrom("klenovuilist@yandex.ru");
            helper.setSubject("Регистрация на smart18.ru");
            helper.setText("<h3>Для продолжения регистрации на smart18.ru перейдите по ссылке ниже</h3><p>.</p>" +
                            "<a href=" + myDomeinMail + "/users/verify/" + userDto.getId() + ">Продолжить регистрацию</a>",
                    true);

            mailSender.send(message);


        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
   }

    /**
     * Получить данные пользователя по токену
     */

    public Map<String, String> getUserInfoFromToken(HttpServletRequest request){

        Map<String, String> mapUserInfo =new HashMap<>();

        mapUserInfo.put("login", "Войти");
        mapUserInfo.put("role", "Нет");
        mapUserInfo.put("name", "Нет");
        mapUserInfo.put("mail", "Нет");
        mapUserInfo.put("id", "Нет");

        String token = null;

        if (Objects.nonNull(request.getCookies())){
            token = Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals("token"))
                    .findFirst().map(c -> c.getValue()).orElse(null); // получение ТОКЕНА из кук

        }

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // обзезка из заголовка "Bearer " и получение чистого токена
        }
        if (Objects.nonNull(token)){
            try {
                mapUserInfo.put("login", jwtService.getParametrToken(token, "login"));
                mapUserInfo.put("role", jwtService.getParametrToken(token, "role"));
                mapUserInfo.put("name", jwtService.getParametrToken(token, "name"));
                mapUserInfo.put("mail", jwtService.getParametrToken(token, "mail"));
                mapUserInfo.put("id", jwtService.getParametrToken(token, "id"));
            }
            catch (RuntimeException e){
                return mapUserInfo;
            }

        }
        return mapUserInfo;
    }

    /**
     * Получить список всех user
     * @return
     */
    public List<UserDto> getAllUsers() {

        return feignClient.getAllUsers().getBody();
    }
}
