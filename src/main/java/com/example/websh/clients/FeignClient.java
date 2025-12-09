package com.example.websh.clients;


import com.example.websh.dto.*;
import jakarta.xml.soap.SOAPEnvelope;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

//@FeignClient(name = "external-service-group", url = "http://192.168.0.130:9002") // URL для локального запуска в докере
    @org.springframework.cloud.openfeign.FeignClient(name = "external-service-group", url = "${feign.url}") // URL для деплоя
    public interface FeignClient {

    /**
     * Запрос на создание группы
     */
    @PutMapping("/api/create_group/{id}")
    ResponseEntity<UUID> createGroup( @PathVariable("id") String id);


    /**
     * Запрос на получение листа групп
     */
    @GetMapping("/api/get_list_group")
    ResponseEntity<List<GroupProductDto>> getGroup();


    /**
     * Запрос на удаление группы
     */
    @DeleteMapping("/api/del_group/{id}")
    void deleteGroup(@PathVariable("id") String id);


    /**
     * Изменить группу (с любые поля)
     */
    @PutMapping("/api/change_group/")
    void changeGroup(@RequestBody GroupProductDto group);

    /**
     * Получить изображение группы
     */
    @PostMapping("/api/image_group/{id}")
    ResponseEntity<Resource> getImageGroupById(@PathVariable("id") String uuidGroup);

    /**
     * Получить изображение продукта
     */
    @PostMapping("api/image/product/{idProd}/{nameImage}")
    ResponseEntity<Resource> getImageProductById(@PathVariable("idProd") String idProd, @PathVariable("nameImage") String nameImage);



    /**
     * отправить картнку группы на сохранение
     */
    @PostMapping("/api/load_image_group/{id}")
    void loadImageGroup (@RequestBody byte[] image
            , @PathVariable("id") String uuidGroup
            , @RequestHeader("extension") String extension);

    /**
     * получить продукт по Id
     */
    @GetMapping("/api/product/{id}")
    ResponseEntity<ProductDto> getProductById(@PathVariable("id") String uuidProduct);


    /**
     * получить список имен картинок для продукта по Id продукта
     */
    @PostMapping("/api/ListNameImageProduct/{idProduct}")
    ResponseEntity<List<String>> getListNameImageProduct(@PathVariable("idProduct") String uuidProduct);


    /**
     * отправить картнку продукта на сохранение
     */
    @PostMapping("/api/load_image_product/{idprod}")
    void loadImageProduct (@RequestBody byte[] image
            , @PathVariable("idprod") String uuidGroup
            , @RequestHeader("extension") String extension);


    /**
     * получить ProductDto по idprod
     */
    @GetMapping("/api/product/{idprod}")
    ResponseEntity<ProductDto> getProductDtoById(@PathVariable("idprod") String productId);

    /**
     * получить лист ProductDto по uuidGroup
     */
    @GetMapping("/api/list_product/{uuidGroup}")
    ResponseEntity<List<ProductDto>> getListProductDtoByIdGroup(@PathVariable("uuidGroup") String uuidGroup);


    /**
     * Запрос на сохранение в БД продукта
     */
    @PostMapping("/api/save_product")
    ResponseEntity<ProductDto> saveProduct(@RequestBody ProductDto productDto);



    @PostMapping("/api/deletе_product/{idprod}")
    ResponseEntity<GroupProductDto> deleteProduct(@PathVariable("idprod") String productId);


    /**
     * Удалить картинку продукта
     * @param productId
     * @param nameImage
     * @return
     */
    @GetMapping("/api/del/image/{productId}/{nameImage}")
    ResponseEntity<?> deleteImageProduct(@PathVariable("productId") String productId
            , @PathVariable("nameImage")String nameImage);


    /**
     * Получить инфо по id
     */
    @GetMapping("/api/getInfo")
    ResponseEntity<Map<Integer, MainInfoDto>> getInfo();


    /**
     * Сщхранить инфо
     */

    @PostMapping("/api/saveInfo")
    ResponseEntity<Integer> saveInfo(@RequestBody MainInfoDto mainInfoDto);


    /**
     * Получить продукты не распределенные по группам
     */
    @GetMapping("api/list_product/non_group")
    ResponseEntity<List<ProductDto>> getProductNonGroup();


    /**
     * Получить UserDto ПО логину для аутентификации
     */
    @PostMapping("api/user/{userLogin}")
    ResponseEntity<UserDto> findUserByLogin(@PathVariable("userLogin") String userLogin);


    @PostMapping("api/save_user")
    ResponseEntity<UserDto> saveUser(@RequestBody UserDto userDto);


    @PutMapping("api/update_user")
    ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto);


    @GetMapping("api/userId/{userId}")
    ResponseEntity<UserDto> findUserById(@PathVariable("userId") String userId);

    /**
     * Получить всех пользователей
     * @return
     */
    @GetMapping("api/allUsers/")
    ResponseEntity<List<UserDto>> getAllUsers();


    /**
     * отправить файл 3Д продукта на сохранение
     */
    @PostMapping("/api/upload_file/{userId}")
    void loadFile3D (@RequestBody byte[] file
            , @PathVariable("userId") String uuidGroup
            , @RequestHeader("file_name") String fileName
            , @RequestHeader("extension") String extension);


    /**
     * получить List имен файлов по userId (непосредственно из папки)
     */
    @GetMapping("api/getAllFile3DUser/{userId}")
    ResponseEntity<List<String>> getAllFile3DByUserId(@PathVariable("userId") String userId);

    /**
     * получить List<File3DDto> по  userId
     */
    @GetMapping("api/getListFile3DDescriptionUsers/{userId}")
    ResponseEntity<List<File3DDto>> getListFile3DDescriptionUsers(@PathVariable("userId") String userId);

    /**
     * Получить File3DDto по его id
     */
    @GetMapping("api/getFile3DDto/{fileId}")
    ResponseEntity<File3DDto> getFile3DDtoByFileId(@PathVariable("fileId") String fileId);


    /**
     * Сохранить File3DDto
     */
    @PostMapping("api/saveFile3DDto")
    void saveFile3DDto(@RequestBody File3DDto file3DDto);

    /**
     * изменить имя картинки продукта
     */
    @PostMapping("api/changeNameImage/{product_Id}")
    void changeNameImageProduct(@PathVariable("product_Id")String productId
            , @RequestHeader("currentNameImage")String currentNameImage
            , @RequestHeader("newName")String newName);



    /**
     * добавить и сохранить продукты для пользователя
     */
    @PostMapping("api/order_product")
     ResponseEntity<?> orderProduct(@RequestBody UserDto userDto);


    /**
     * Удалить пользователя по id
     */
    @DeleteMapping("api/del_user/{userId}")
    void deleteUserById(@PathVariable("userId") String userId);


    @GetMapping("api/get_counters")
    ResponseEntity<List<CounterDto>> getListCounter();

    @PostMapping("api/save_counter")
    ResponseEntity<?> saveNewCounter(@RequestBody CounterDto counter);
}
