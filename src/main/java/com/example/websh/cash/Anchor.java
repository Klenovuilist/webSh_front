package com.example.websh.cash;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;


/**
 * Класс для хранения точек перехода к нужному месту на странице html при ее загрузке
 */
@Component
@Getter
@Setter
public class Anchor {

   public String point = "";

    public void cleanAnhor(){
        this.point = "";
    }
}
