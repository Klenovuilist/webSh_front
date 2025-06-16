package com.example.websh.clients;

import com.example.websh.entity.TestEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@FeignClient(name = "external-service", url = "http://localhost:8099") // URL
    public interface TestClient {

        @PostMapping("/api/test_request_on_back")
        HttpEntity<String> testRequestOnBack();


    @GetMapping("/api/get_testData/{id}")
    TestEntity getTestEntity(@PathVariable("id") UUID id);


    @PostMapping("/api/save_test_data")
    HttpEntity saveNewTestEntity(@RequestBody TestEntity testEntity);


    /**
     * Получение TestEntity по uuid
     * @param testEntity
     * @param id
     * @return
     */
    @PutMapping("/api/save_test_data/{id}")
    ResponseEntity<TestEntity> saveChangeTestEntity(@RequestBody TestEntity testEntity, @PathVariable("id") UUID id);

    /**
     * Получение List<TestEntity>
     * @return
     */
    @GetMapping("/api/test_entity")
    List<TestEntity> getListTestEntity();


    @GetMapping("/api/delete/test_data/{id}")
    ResponseEntity<String> deleteTestDataById(@PathVariable("id") String id);
}

