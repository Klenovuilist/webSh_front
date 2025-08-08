package com.example.websh;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

/**
 * перехватчик запросов на сервер, добавляет заголовки с токеном
 */
@Component
public class Interceptor implements RequestInterceptor {

    //вставить заголовок с токеном для запроса, статичный
    String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsIm5hbWUiOiJyaXRhIiwic3ViIjoidXNlckVudGl0eSIsImlhdCI6MTc1MzI3MTY2NywiZXhwIjoxNzU1MDcxNjY3fQ.AMfxg6rCPxdToNKq2UCcISDE3F025eO6zKP0IK3fZMg";

    @Override
    public void apply(RequestTemplate requestTemplate) {
            if(requestTemplate.url().contains("/api/test_request_on_back")){
                    requestTemplate.header("Authorization",token);
        }

            //для всех запросов из страницы админ добавить токен в заголовок
        if(requestTemplate.url().startsWith("/api")){
            requestTemplate.header("Authorization",token);
        }
    }
}
