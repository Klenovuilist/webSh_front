package com.example.websh.service;

import com.example.websh.entity.TestEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TestCach {

    private TestEntity testEntityCash;

    private Map<String, String> cashMap = new HashMap<>();


    /**
     * Получить кэш мап, если пустой то заполнить по testEntityCash
     * @return
     */
    public Map<String, String> getCashMap() {
        if (cashMap.isEmpty()){
            cashMap.put("UUID", null);
            cashMap.put("testName", null);
            cashMap.put("testLogin", null);
            cashMap.put("testPasswordUser", null);
            cashMap.put("testRoleUser", null);
            cashMap.put("testComment", null);
            cashMap.put("testDataCreateUser", null);
        }
        return cashMap;
    }

    /**
     * Обновить cashMap
     */
    private void refreshCashMap(TestEntity testEntity){
        fillMap(testEntity);
    }

    /**
     * Обновить TestEntity и cashMap
     */
    public void refreshTestEntityCash(TestEntity newtestEntity){
        testEntityCash = newtestEntity;
        refreshCashMap(newtestEntity);
    }


    /**
     * Заполнить cashMap по данным TestEntity
     */
    private Map<String, String> fillMap (TestEntity testEntity){
        if (testEntity != null) {

            cashMap.put("UUID", Optional.of(testEntity).map(TestEntity::getId)
                    .map(UUID::toString)
                    .orElse(null));
            cashMap.put("testName", testEntity.getTestName());
            cashMap.put("testLogin", testEntity.getTestLogin());
            cashMap.put("testPasswordUser", Optional.ofNullable(testEntity.getTestPasswordUser())
                    .map(String::valueOf)
                    .orElse(null));
            cashMap.put("testRoleUser", testEntity.getTestRoleUser());
            cashMap.put("testComment", testEntity.getTestComment());
            cashMap.put("testDataCreateUser", Optional.ofNullable(testEntity.getTestDataCreateUser())
                    .map(LocalDateTime::toString)
                    .orElse(null));
        }
        return cashMap;
    }


    public TestEntity getTestEntityCash(){
        if (testEntityCash == null){
            return null;
        }
        else {
            return testEntityCash;
        }
    }

}
