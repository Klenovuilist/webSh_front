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
    String token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU5fUm9sZSIsIm5hbWUiOiJyaXRhIiwic3ViIjoidXNlckVudGl0eSIsImlhdCI6MTc1MTQ2NTA4NSwiZXhwIjoxNzUxNDgzMDg1fQ.s4reDXkoKgezPF-d8_539cssfhdwwn0CyY2PeCiBF8U";

    @Override
    public void apply(RequestTemplate requestTemplate) {
            if(requestTemplate.url().contains("/api/test_request_on_back")){
                    requestTemplate.header("Authorization",token);
        }

    }
}
