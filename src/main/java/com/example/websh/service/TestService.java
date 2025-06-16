package com.example.websh.service;

import com.example.websh.clients.TestClient;
import com.example.websh.entity.TestEntity;
import com.example.websh.exceptions.ErrorMessage;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TestService {


    private final TestCach testCach;

    private final TestClient testClient;

//    private final Messages messages;

//    private  final ErrorMessage errorMessage;

    @Autowired
    public TestService(TestCach testCach, TestClient testClient, ErrorMessage errorMessage, Messages messages) {
        this.testCach = testCach;
        this.testClient = testClient;
//        this.errorMessage = errorMessage;
//        this.messages = messages;
    }



    /**
     * Получение данных из тестовой формы
     */
    public Map<String, String> getTestMapFromForm(HttpServletRequest request) {

        /**
         * Получение formMap из testCach для итераций по ключам и заполнению
         */
        Map<String, String> formMap = new HashMap<>(); //данные из тестовой формы

        for (Map.Entry<String, String> entry : testCach.getCashMap().entrySet()) {
            formMap.put(entry.getKey(), request.getParameter(entry.getKey()));
        }
        return formMap;
    }

    public TestEntity getTestEntityFromForm(HttpServletRequest request) {

        Map<String, String> formMap = getTestMapFromForm(request);

        if (formMap.equals(testCach.getCashMap())){
            return testCach.getTestEntityCash();
        }
        else {
            return TestEntity.builder()
                    .id(UUID.fromString(formMap.get("UUID")))
                    .testName(formMap.get("testName"))
                    .testLogin(formMap.get("testLogin"))
                    .testRoleUser(formMap.get("testRoleUser"))
                    .testPasswordUser(Integer.parseInt(formMap.get("testPasswordUser")))
                    .testDataCreateUser(LocalDateTime.now())
                    .testComment(formMap.get("testComment"))
                    .build();
        }
    }

    public TestEntity getTestEntity(){
        if(testCach.getTestEntityCash() != null){
            return testCach.getTestEntityCash();
        }

        //Todo убарть UUID.randomUUID()
        TestEntity testEntity = testClient.getTestEntity(UUID.randomUUID());
        testCach.refreshTestEntityCash(testEntity);

        return testEntity;
    }

    public boolean saveTestEntity(HttpServletRequest request) {

        // проверка параметров из формы с параметрами в кэш
        if (! getTestMapFromForm(request).equals(testCach.getCashMap())) {
            TestEntity newTestEntity = getTestEntityFromForm(request);

            // запрос на сохранение сущности на бэке при возможности ошибки на сохранение
            try {
                ResponseEntity<TestEntity> responseEntity = testClient.saveChangeTestEntity(newTestEntity, newTestEntity.getId());// запрос на бэк на сохранение сущности

                TestEntity saveTestEntity = responseEntity.getBody();
                testCach.refreshTestEntityCash(testClient.getTestEntity(saveTestEntity.getId()));//обновление кэш
                ErrorMessage.errorSave = null;

            } catch (FeignException f) {
                String message = f.getMessage();

                int indexSubstring = message.indexOf("Ошибка");
                    if(indexSubstring != (-1)) {
                        String q = message.substring(message.indexOf("Ошибка"));
                        ErrorMessage.errorSave = q.substring(0, q.indexOf("]"));
                    }
                    else {
                        ErrorMessage.errorSave = "Сервер не смог сохранить " + f.getMessage();
                    }
                return false;
            }
        }
            return true;
        }

        public List<TestEntity> getListTestEntity(){
            List<TestEntity> resultList;

//                    for (int i = 0; i < 5; i++) {
//                        resultList.add(TestEntity.builder().testComment("нет записи")
//                                .id(UUID.randomUUID())
//                                .testLogin(UUID.randomUUID().toString().substring(0, 4))
//                                .testName(UUID.randomUUID().toString().substring(0, 4))
//                                .testPasswordUser((int) (Math.random() * 1000))
//                                .testRoleUser("Role")
//                                .testDataCreateUser(LocalDateTime.now())
//                                .build());
//                    }
                    resultList = testClient.getListTestEntity();
                    return resultList;
//        return testClient.getListTestEntity();
        }

    public boolean deleteTestDataById(String id) {

        try {
            ResponseEntity<String> responseEntity = testClient.deleteTestDataById(id);
            HttpHeaders headers = responseEntity.getHeaders();
            Messages.httpMessagesDelete = responseEntity.getBody(); // сообщение удачного удаления от бэк

        } catch (RuntimeException e) {
            Messages.httpMessagesDelete = "не удалось удалить объект с id = " + id;
            return false;

        }
        return true; // удалось удалить объект
    }
}


