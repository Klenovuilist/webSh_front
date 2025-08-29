package com.example.websh.service;

import com.example.websh.dto.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Создание и проверка jwt токена
 */
@Service
public class JwtService {

    private Key key;
    private final Duration jwtLifeTime =Duration.ofMinutes(30000)/*ofSeconds(100)*/; //время жизни токена 30000 мин

    @PostConstruct
    public void initialize() {
        //ключ шифрования токена
        String secret = "mykeyfggdfgdfewefdsdsgfdfdshhfadffdgfsfgfdhdfhd";
        key = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8)); //ключ для шифрования токена на основе секрет
    }

    /**
     * Генерация токена с информацией об usersEntity
     */
    public String generateJWToken(UserDto userDto) {
        Map<String, String> payLoadToken = new HashMap<>(); // полезная нагрузка для токена
        payLoadToken.put("login", userDto.getLogin());
        payLoadToken.put("role", userDto.getRoleUser());
        payLoadToken.put("name", userDto.getUserName());
        payLoadToken.put("mail", userDto.getMail());
        payLoadToken.put("id", userDto.getId().toString());

        Date issuedDate = new Date(); // время создания токена - текущее время
        Date expiriedDate = new Date(issuedDate.getTime() + jwtLifeTime.toMillis());  // время жизни токена



        return Jwts.builder()
                .claims(payLoadToken) // полезная нагрузка (список Map)
                .claim("sub", "userDATA") // тема токена
                .claim("iat", issuedDate.getTime() / 1000)       // время создания секунды
                .claim("exp", expiriedDate.getTime() / 1000)     // время жизни секунды
                .signWith(key)               // Подписание токена ключем
                .compact();
    }

    /**
     * Ппарсинг токена - получение Claims
     * @param token
     * @return
     */
    private Claims getAllClaimsToken(String token){
        Jws<Claims> parsedToken = Jwts.parser()
                .setSigningKey(key)        // Задаем ключ для проверки подписи
                .build()
                .parseClaimsJws(token);    // Парсим токен

        return parsedToken.getBody();

    }

    //получение имени пользователя из токена
    public String getUserLogin(String token){
        return getAllClaimsToken(token).get("login", String.class);
    }


    //получение параметра из токена
    public String getParametrToken(String token, String nameParametr){
        return getAllClaimsToken(token).get(nameParametr, String.class);
    }

    //получение роли пользователя из токена
    //todo вщзможно не так
    public String getUserRole(String token) {
        return getAllClaimsToken(token).get("role", String.class);
    }


}
