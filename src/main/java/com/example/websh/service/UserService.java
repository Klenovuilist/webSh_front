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


import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
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

        //создать пользователя по параметрам из формы если не был найден в БД
         userDto = UserDto.builder()
                .roleUser("ROLE_USER")
                .psswordUser(request.getParameter("password"))
                .userName(request.getParameter("userName"))
                .dataCreateUser(LocalDateTime.now())
                .login(request.getParameter("userLogin"))
                .mail(request.getParameter("email"))
                 .boolverify(false)
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
    public UserDto updateUserAndSave(UserDto userDto) {

        return feignClient.updateUser(userDto).getBody();
    }

    /**
     * Изменение данных пользователя

     * @return
     */
    public String updateUser(UserDto userDto, String userId, HttpServletRequest request) {

        if(!userDto.getPsswordUser().equals(request.getParameter("old_password"))){
            return "неверный пароль";
        }
        if(request.getParameter("userLogin").isBlank()){
            return "логин не должен быть пустым";
        }
        String newLogin = request.getParameter("userLogin");
        String newMail = request.getParameter("email");
        UserDto userFromBD = getUserByLogin(newLogin);

        //если логина нет то разрешено поменять
        if(userFromBD != null && ! userFromBD.getLogin().equals(userDto.getLogin())){
            return "логин: " + request.getParameter("userLogin") + " уже занят";
        }

        //обновить пароль если пришел новый в запросе
        if(!request.getParameter("password").isBlank()){
            userDto.setPsswordUser(request.getParameter("password"));
        }
//        userDto.setPsswordUser(request.getParameter("password"));
        userDto.setUserName(request.getParameter("userName"));
        userDto.setDataCreateUser(LocalDateTime.now());
        userDto.setLogin(request.getParameter("userLogin"));

        boolean isChangeMail = false; //была ли изменена почта

        //обновить почту если пришла новая в запросе и отправить письмо на подтверждение
        if(! newMail.isBlank() && !newMail.equals(userDto.getMail())){
            try {
                InternetAddress address = new InternetAddress(newMail);
                address.validate(); // проверяем синтаксически правильный email

                userDto.setMail(newMail);
                sendVerificationEmail(userDto); // отправка письма верификации
                userDto.setBoolverify(false); // обнуление верификации
            } catch (AddressException | RuntimeException ex) {
                return "почта указана не верно";
            }
        }
        feignClient.updateUser(userDto).getBody();
         if(isChangeMail){
             return "На почту: " + newMail + " направлено письмо для подтверждения, перейдите по ссылке в письме";
         }

        return "данные изменены";
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
//            helper.setFrom("klenovuilist@yandex.ru");
            helper.setFrom("info@3detail.ru");
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
//            helper.setFrom("klenovuilist@yandex.ru");
            helper.setFrom("info@3detail.ru");

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
        mapUserInfo.put("mail", "Не указана");
        mapUserInfo.put("id", UUID.randomUUID().toString());
        mapUserInfo.put("verify", "false");

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
                mapUserInfo.put("verify", jwtService.getParametrToken(token, "verify" ));
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
                .boolverify(Boolean.parseBoolean(userInfoToken.get("verify")))
                .build();
    }

    /**
     * Получить данные UserDto из Map<String, String> userInfo
     */
    public UserDto getUserDtoByMapUserInfo(Map<String, String> userInfo) {


        return UserDto.builder()
                .id(UUID.fromString(userInfo.get("id")))
                .login(userInfo.get("login"))
                .userName(userInfo.get("name"))
                .mail(userInfo.get("mail"))
                .roleUser(userInfo.get("role"))
                .boolverify(Boolean.parseBoolean(userInfo.get("verify")))
                .build();
    }


    /**
     * Получить список File3DDto загруженных файлов 3D по id пользователя (непосредствено из папки)
     */
    public List<String> getListFile3DUsers(String userId) {
        return feignClient.getAllFile3DByUserId(userId).getBody();
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

    /**
     *  продукты из БД добавить для пользователя - создать  File3DDto
     * @param userId
     * @param productId
     */
//todo не используется
    //todo  feignClient.getAllUsers() заменить на кеш
    public void orderProduct(String userId, String productId){

        File3DDto fileForSave = File3DDto.builder()
                .userId(UUID.fromString(userId))
                .coast("")
                .comment("")
                .data_create(LocalDateTime.now())
                .material("")
                .status("")
                .fileId(productId)
                .fileName(null)
                .build();


        feignClient.saveFile3DDto(fileForSave);

//            ProductDto productDto = feignClient.getProductDtoById(productId).getBody();
//            List<UserDto> listUser = feignClient.getAllUsers().getBody();
//
//            // добавить в userDto продукт ProductsDTO и сохранить в БД
//            if (!listUser.isEmpty()){
//                UserDto userDto = listUser.stream().filter(user -> user.getId().toString().equals(userId)).findFirst().orElse(null);
//                if(userDto != null && productDto != null){
//                    userDto.setProductsDTO(new ArrayList<>());
//                    userDto.getProductsDTO().add(productDto);
//
//                    feignClient.orderProduct(userDto);
//
//                }
//            }


    }

    /**
     * Проверка пароля пользователя - является ли он паролем по умолчанию
     * возвращает null если пароль не является паролем по умолчанию либо пароль по умолчанию
     */
    public String isDefoultPassword(UUID id) {
        UserDto user = getUserById(id.toString());
        if(Objects.isNull(user)){
            return null;
        }
        if(Objects.isNull(user.getPsswordUser())){
            return  null;
        }

        if (user.getPsswordUser().startsWith("psw")){
            return user.getPsswordUser();
        }
        return null;
    }
}

