package com.example.websh.service;


import com.example.websh.clients.FeignClient;
import com.example.websh.dto.File3DDto;
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

    private final JavaMailSender mailSender; //встроенныые отправщих сообщений

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
        if(Objects.nonNull(userDto) && userDto.getDataCreateUser() != null){
            userDto.setDataCreateParsing(dataFormater.format(userDto.getDataCreateUser()));
        }

        return userDto;
    }


    /**
     * Регистрация пользователя, сохранение в БД
     * @param request
     * @return
     */
    public UserDto saveUser(HttpServletRequest request) {

        UserDto userDto = getUserByLogin(request.getParameter("userLogin")); // пользователь из БД по логину

        //если пользователь с логином существует то вернуть null нового не сохранять
        if (Objects.nonNull(userDto)){
            return null;
        }

        //создать пользователя по параметрам из формы
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
     * Сохранить пользователя
     */
    public UserDto saveUser(UserDto userDto) {

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
            helper.setSubject("Регистрация на 3detail.ru");
            helper.setText("<h3>Для продолжения регистрации на 3detail.ru перейдите по ссылке ниже</h3><p>.</p>" +
                            "<a href=" + myDomeinMail + "/users/verify/" + userDto.getId() + ">Продолжить регистрацию</a>",
                    true);

            mailSender.send(message);


        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
   }

    /**
     * Отправка письма со самому себе как уведомление
     * Подключена зависимость и настройки конфигурации
     * @param
     */
    public void sendWorkEmail(String textMessage, String subject)  {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, true);

            helper.setTo("klenovuilist@yandex.ru"); // адрес куда отправить
            helper.setFrom("klenovuilist@yandex.ru");
            helper.setSubject(subject);
            helper.setText(textMessage);

            mailSender.send(message);


        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Получить данные пользователя Map<String, String> по токену или установить по умолчанию
     */

    public Map<String, String> getUserInfoFromToken(HttpServletRequest request){

        Map<String, String> mapUserInfo =new HashMap<>();

        mapUserInfo.put("login", "Регистрация");
        mapUserInfo.put("role", "ROLE_USER");
        mapUserInfo.put("name", "Нет");
        mapUserInfo.put("mail", "Не указанна");
        mapUserInfo.put("id", UUID.randomUUID().toString());

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

    /**
     * Получить данные UserDto из формы запроса или установить UserDto по умолчанию
     */
    public UserDto getUserDtoByToken(HttpServletRequest request) {

               Map<String, String> userInfoToken = getUserInfoFromToken(request);

        return UserDto.builder()
                .id(UUID.fromString(userInfoToken.get("id")))
                .login(userInfoToken.get("login"))
                .userName(userInfoToken.get("name"))
                .mail(userInfoToken.get("mail"))
                .roleUser(userInfoToken.get("role"))
                .build();
    }

    /**
     * Получить список File3DDto загруженных файлов 3D по id пользователя (непосредствено из папки)
     */
    public List<String> getListFile3DUsers(String userId) {
        return feignClient.getAllFile3DByUserId(userId).getBody();
    }

    /**
     * Получить список описаний File3DDto загруженных файлов 3D по id пользователя (в Postgress)
     */
    public List<File3DDto> getListFile3DDtoByUsersId(String userId, boolean isDelete) {

        List<File3DDto> listFile3DDto = feignClient.getListFile3DDescriptionUsers(userId).getBody();
        //в коллекции помеченные на удаленные
        if (! listFile3DDto.isEmpty() && isDelete){
            return listFile3DDto.stream().filter(file -> file.isDelete()).toList();
        }
        // убрать из коллекции помеченные на удаленные
        else if (! listFile3DDto.isEmpty()) {
            return listFile3DDto.stream().filter(file -> ! file.isDelete()).toList();
        }
        return listFile3DDto;
    }


    /**
     * Получить  описание File3DDto  по id файла File3DDto (в Postgress)
     */
    public File3DDto getFile3DDtoById(String fileId) {
        return feignClient.getFile3DDtoByFileId(fileId).getBody();
    }


    /**
     * Проверка наличия токена пользователя при запросе
     */
    public boolean isExistToken(HttpServletRequest request){

        if (Objects.nonNull(request.getCookies())){
            // получение ТОКЕНА из кук
            if(Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals("token"))
                    .findFirst().map(c -> c.getValue()).orElse(null)  != null){
                return true;
            }
        }
            return false;
    }
}
