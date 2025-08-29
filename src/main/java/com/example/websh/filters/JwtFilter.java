package com.example.websh.filters;


import com.example.websh.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * Класс фильтр, перехватывает запрос от клиента, считывает токен, достает из него пользователя
 * помещает пользователя в контекст security, далее когда запрос дойдет до защищенного контроллера
 * в контексте будет пользователь с определенным набором ролей
 */

@Component

public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private String userLogin = null;
    private String userRole = null;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        if (Objects.nonNull(request.getCookies())){
            token = Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals("token"))
                    .findFirst().map(c -> c.getValue()).orElse(null); // получение ТОКЕНА из кук

        }

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // обзезка из заголовка "Bearer " и получение чистого токена
        }
        if (token != null) {
        //считывание из заголовка имени и роли
            try {

                //для установки в UsernamePasswordAuthenticationToken
                userLogin = jwtService.getUserLogin(token);
                userRole = jwtService.getUserRole(token);

            }
            catch (io.jsonwebtoken.SignatureException ex){
                response.addHeader("JWToken","no_valid");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "токен некорректный"); // отправка ответа клиенту при ошибке
                Cookie cookie = new Cookie("token", "");
                cookie.setMaxAge(0); // Срок жизни cookie в секундах
                cookie.setPath("/");     // Путь доступности cookie для всего сайта

                // Установка cookie в ответ
                response.addCookie(cookie);

                return;
//                throw new NoValidRequest("токен некорректный");

            }
            catch (ExpiredJwtException e) {
                response.addHeader("JWToken","out_of_time");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "время жизни токена вышло");

                Cookie cookie = new Cookie("token", "");
                cookie.setMaxAge(0); // Срок жизни cookie в секундах
                cookie.setPath("/");     // Путь доступности cookie для всего сайта

                // Установка cookie в ответ
                response.addCookie(cookie);
                return;
//                throw new NoValidRequest("время жизни токена вышло");
            }

            // проверка наличия в контексте секьюрити объекта Authentication, если нет то создаем новго
            if (userLogin != null && SecurityContextHolder.getContext().getAuthentication() == null){

                //Объект для аутентификации
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userLogin
                  ,null
                  , Collections.singletonList(new SimpleGrantedAuthority(userRole))) {
                };

                // установка в контекст секъюрити Authentication
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);// передача обработки запроса на следующий фильтр или контроллеру
    }
}
