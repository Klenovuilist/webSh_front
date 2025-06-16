package com.example.websh.controllers;

import com.example.websh.clients.TestClient;
import com.example.websh.entity.TestEntity;
import com.example.websh.exceptions.ErrorMessage;
import com.example.websh.service.Messages;
import com.example.websh.service.TestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller

public class TestController {

    private boolean isHasError = false;

    private boolean wasDeleted = false; // было ли удаление

    private final TestClient testClient;

    private final TestService testService;



    @Autowired
    public TestController(TestClient testClient, TestService testService, Messages messages) {
        this.testClient = testClient;
        this.testService = testService;

    }

    @GetMapping("/")
    public String index() {
        return "index FRONT 123123132\n 123123132\n123123132\n123123132\n123123132\n";
    }

    @GetMapping("/test")
    public String testRequest() {
        return "test FRONT";
    }

    @GetMapping("/info_back")  // тестовый запрос на бэк
    public String testRequestOnBack() {

        String request = testClient.testRequestOnBack().getBody(); // пост запрос на бэк
        return request;
    }

    /**
     * Метод для получения тестовой сущности  (Главная страница)
     * @param model
     * @return
     */
    @GetMapping("/test_entity")
    public String getTestEntity(Model model, HttpServletResponse response) {
        TestEntity entity = testService.getTestEntity();

        model.addAttribute("entity", entity);

        //Проверка на наличи ошибок
        if (isHasError) {
            model.addAttribute("errorMessage", ErrorMessage.errorSave);
        }
        else {
            model.addAttribute("errorMessage", null);
            ErrorMessage.errorSave = null;
        }
        model.addAttribute("listEntity", testService.getListTestEntity());
        
        //Установка сообщений если было удаление объекта
        if (wasDeleted){
            response.addHeader("deleteMessage", "delete object true");
            model.addAttribute("deleteMessage", Messages.httpMessagesDelete);
            wasDeleted = false;
        }


        return "test.html"; // несуществующий id в запросе на back
    }

    /**
     * Метод для сохранения тестовой сущности
     * @param request
     * @return
     */
    @PostMapping("/save_test_data")
    public String saveTestData(HttpServletRequest request){

//        если удалось сохранить -> нет ошибки
       if (testService.saveTestEntity(request)){
           isHasError = false;
       }
       else {
           isHasError = true;
       }
        return "redirect:/test_entity";
    }

    @GetMapping("/delete/test_data/{id}")
    public String deletTestDataById(@PathVariable("id") String id){

        testService.deleteTestDataById(id);// удаление объекта
            wasDeleted = true;
        return "redirect:/test_entity";
    }

}
