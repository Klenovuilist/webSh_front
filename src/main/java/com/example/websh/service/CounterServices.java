package com.example.websh.service;

import com.example.websh.clients.FeignClient;
import com.example.websh.dto.CounterDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor

public class CounterServices {

    private final FeignClient feignClient;

    public List<CounterDto> getListCounter(){
        return feignClient.getListCounter().getBody();
    }


    /**
     * Сохранить посещение
     * @param counter
     */
    public void saveNewCounter(CounterDto counter) {
        feignClient.saveNewCounter(counter);
    }


    /**
     * Создать и сохранить Counter
     */
    public void createCounter(HttpServletRequest request, Map<String, String> userInfo, String enter, String parametr){
        StringBuilder userInfoBuilder = new StringBuilder();

        //role=ROLE_ADMIN, mail=null, name=rita, verify=true, id=caab7358-6cf6-4222-8626-145442697d22, login=rita

//         Не создавать Counter если "ROLE_ADMIN"
        if(userInfo.containsKey("role")){
            if(userInfo.get("role").equals("ROLE_ADMIN")){
                return;
            }
        }

        if(userInfo.containsKey("login")){
            userInfoBuilder.append("login: ").append(userInfo.get("login")).append("  ");
        }

        if(userInfo.containsKey("id")){
            userInfoBuilder.append("id: ").append(userInfo.get("id")).append("  ");
        }

        if(userInfo.containsKey("mail")){
            userInfoBuilder.append("mail: ").append(userInfo.get("mail")).append("  ");
        }

        if(userInfo.containsKey("verify")){
            userInfoBuilder.append("verify: ").append(userInfo.get("verify")).append("  ");
        }

        // IP-адрес с которого было обращение непосредственно
//        String currentIpAddress = request.getRemoteAddr();
        // получить цепочку ip адресов из заголовка запроса
//        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String xRealIP = request.getHeader("X-Real-IP");

//        userInfoBuilder.append("текущий ip: ").append(currentIpAddress).append("  ");

        userInfoBuilder.append("X-Real-IP: ").append(xRealIP).append("  ");

//        userInfoBuilder.append("X-Forwarded-For: ").append(xForwardedFor).append("  ");

        CounterDto counterDto = CounterDto.builder()
                .userInfo(userInfoBuilder.toString())
                .parameter(parametr)
                .countEnter(enter)
                .build();

        this.saveNewCounter(counterDto);
    }
}
