package com.example.websh.service;

import com.example.websh.clients.FeignClient;
import com.example.websh.dto.GroupProductDto;
import com.example.websh.dto.MainInfoDto;
import com.example.websh.dto.ProductDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final FeignClient feignForGroup;

    private final JwtService jwtService;

//    private final Cash cash;




    /**
     * Развертывание листа групп с подгруппами для вывода на фронт
     */
    public List<GroupProductDto> getStructureListGroup(List<GroupProductDto> list) {
        List<GroupProductDto> resultList = new ArrayList<>();
        structureList(list, resultList);
        return resultList;
    }

    /**
     * Структурирование листа с рекурсией (плоский список из групп и подгрупп)
     */
    private void structureList(List<GroupProductDto> list, List<GroupProductDto> resultList) {

        int i = 0;

        if (list.isEmpty()) {
            return;
        }

        while (i < list.size()) {
            GroupProductDto currentGroup = list.get(i);
            resultList.add(currentGroup); // добавление текущей группы в лист

            if (!currentGroup.getListUnderGroups().isEmpty()) { //проверка есть ли в листе групп подгруппы
                structureList(currentGroup.getListUnderGroups(), resultList); // рекурсия функции для листа с подгруппами
            }
            i++;
        }
    }

    public List<GroupProductDto> addUnderGroupAndStructure(List<GroupProductDto> listGroup){

        //добавление в листы родителей дочерних групп
        for (GroupProductDto groupDto: listGroup){
                    if (Objects.isNull(groupDto.getParrentId())) {
                        continue;
                    }
                    //поиск родителя
                    Optional<GroupProductDto> optionalGroupDto = listGroup.stream()
                            .filter(gr -> gr.getGroupId().equals(groupDto.getParrentId()))
                            .findFirst();

                     //добавление группы в родителя
                            optionalGroupDto.ifPresent(gr -> gr.getListUnderGroups().add(groupDto));
        }
        //лист GroupProductDto только из начальных групп
         List<GroupProductDto> listGroupDtoResult = listGroup.stream()
                 .filter(gr -> Objects.isNull(gr.getParrentId())).toList();

     return getStructureListGroup(listGroupDtoResult);

//        return listGroupDtoResult;
    }


    /**
     * Получение развернутого листа групп начального по умолчанию
     */
    public List<GroupProductDto> getListGroupDtoDefoult() {

//        todo сделать получение через базу
        List<GroupProductDto> listGroupProduct = new ArrayList<>();

        GroupProductDto cats = GroupProductDto.builder().groupName("Кошки").groupId(UUID.randomUUID()).levelGroup(0).build();

        GroupProductDto catsRadio = GroupProductDto.builder()
                .groupId(UUID.randomUUID())
                .groupName("Радио Кошки")
                .parrentId(cats.getGroupId())
                .levelGroup(1).build();
        GroupProductDto catsMehunic = GroupProductDto.builder()
                .groupId(UUID.randomUUID())
                .parrentId(cats.getGroupId())
                .groupName("Механические Кошки")
                .levelGroup(1).build();

        GroupProductDto catsMehunicSimple = GroupProductDto.builder()
                .groupId(UUID.randomUUID())
                .parrentId(catsMehunic.getGroupId())
                .groupName("простые Механические Кошки")
                .levelGroup(2).build();
        catsMehunic.getListUnderGroups().add(catsMehunicSimple);

        cats.getListUnderGroups().add(catsRadio);
        cats.getListUnderGroups().add(catsMehunic);

        listGroupProduct.add(cats);
        listGroupProduct.add(GroupProductDto.builder().groupId(UUID.randomUUID()).groupName("Акустические мыши").levelGroup(0).build());
        listGroupProduct.add(GroupProductDto.builder().groupId(UUID.randomUUID()).groupName("Звонилки").levelGroup(0).build());

        return getStructureListGroup(listGroupProduct);  // развернутый лист групп с подгуппами
    }


    /**
     * получение Map из листа групп
     */
    private LinkedHashMap<UUID, GroupProductDto> convertGroupsToMap(List<GroupProductDto> listGroup) {
        LinkedHashMap<UUID, GroupProductDto> linckedMap = listGroup.stream()
                .collect(Collectors.toMap(r -> r.getGroupId(), Function.identity(), (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return linckedMap;
    }

    /**
     * Изменение имени в группе по ключу
     */
    public List<GroupProductDto> changeNameGroup(List<GroupProductDto> listGroup, String name, UUID groupId) {
        LinkedHashMap<UUID, GroupProductDto> linckedMap = convertGroupsToMap(listGroup);
        if (linckedMap.containsKey(groupId)) {
            linckedMap.get(groupId).setGroupName(name); //установка нового имени группы
        }

        List<GroupProductDto> group = new ArrayList<>(linckedMap.values());

        return group;
    }

    /**
     * Добавление новой подгруппы в группу
     */
    public void addUnderGroup(List<GroupProductDto> listGroup, UUID parrentUUID) { // развернутый список listGroup

        if (Objects.isNull(parrentUUID)) {  // если родительской группы нет
            listGroup.add(GroupProductDto.builder() // добавление новой группы в подгруппу
                    .groupName("Новая группа").groupId(UUID.randomUUID())
                    .levelGroup(0)
                    .build());
            return;
        }

        Optional<GroupProductDto> parrentGroupOptional = listGroup.stream()
                .filter(grp -> grp.getGroupId().equals(parrentUUID))
                .findFirst();

        parrentGroupOptional.ifPresent(parrentGroup -> {  // если найдена род. группа
            parrentGroup.getListUnderGroups().add(GroupProductDto.builder() // добавление новой группы в подгруппу
                    .groupName("Новая группа").groupId(UUID.randomUUID())
                    .parrentId(parrentGroup.getGroupId())
                    .levelGroup(parrentGroup.getLevelGroup() + 1)
                    .build());
            int i = listGroup.indexOf(parrentGroup);  // получение индекса род. группы

            //добавление новой подгруппы следующей по списку после родительской
            listGroup.add(i + 1, parrentGroup.getListUnderGroups().get((parrentGroup.getListUnderGroups().size() - 1))); //
        });


//         return structureListGroup(listGroup);

    }

    /**
     * Удаление группы из структурированного листа групп
     * с переопределением уровней групп
     */
    public void deleteGroup(List<GroupProductDto> listGroup, UUID groupUUID) {

        Optional<GroupProductDto> groupOptional = listGroup.stream()
                .filter(grp -> grp.getGroupId().equals(groupUUID))
                .findFirst();

        groupOptional.ifPresent(group -> {
            if (group.getParrentId() != null) {   // если группа содержит uuid родителя
                listGroup.stream()  // поиск родителя в исходном листе
                        .filter(gr -> gr.getGroupId().equals(group.getParrentId()))// поиск родителя по uuid
                        .findFirst()
                        .ifPresent(parrent -> {  // если родитель найден

//                                group.getListUnderGroups().forEach(child -> child.setLevelGroup(child.getLevelGroup() - 1));

                            parrent.getListUnderGroups().addAll(group.getListUnderGroups()); // добавление подгрупп удаляемой группы к его родителю
                            parrent.getListUnderGroups().remove(group); // удаление из подгруппы родителя удаляемой группы
                            parrent.getListUnderGroups().forEach(child -> child.setParrentId(parrent.getGroupId())); // установка для child uuid родителя
                        });
            }
            listGroup.remove(group); // удаления группы из списка для отображения удаляемой группы
            listGroup.forEach(elmentGr -> {  //проход по списку отображения
                listGroup.stream()  // получение уровня родителя
                        .filter(g -> ObjectUtils.equals(g.getGroupId(), elmentGr.getParrentId())) //получение родителя по uuid если есть
                        .findAny()
                        .ifPresentOrElse(parr -> { // если найден родитель
                                    elmentGr.setLevelGroup(parr.getLevelGroup() + 1);
                                },
                                () -> elmentGr.setLevelGroup(0)); // если не найден родитель
            });
        });


    }


    /**
     * Изменение родителя группы
     */
    public List<GroupProductDto> changeParrentGroup(List<GroupProductDto> listGroup, String parrentUuid, String childUuid) {
        GroupProductDto parrentGroup = listGroup.stream()
                .filter(group -> group.getGroupId().equals(UUID.fromString(parrentUuid)))
                .findAny().orElse(null); // получение родительской группы по uuid

        GroupProductDto childGroup = listGroup.stream()
                .filter(group -> group.getGroupId().equals(UUID.fromString(childUuid)))
                .findAny().orElse(null); // получение подгруппы по uuid

        Optional<GroupProductDto> oldParrent = listGroup.stream()
                .filter(group -> Objects.equals(group.getGroupId(), childGroup.getParrentId()))
                .findAny(); // получение подгруппы по uuid


        if (Objects.isNull(parrentGroup) || Objects.isNull(childGroup)) {
            return listGroup; // если нет род. или подгруппы
        }

//        listGroup
//                .stream()
//                .filter(group -> group.getGroupId().equals(UUID.fromString(childUuid)))
//                .findFirst()
//                        .ifPresent(group -> {
//                            group.getListUnderGroups().remove(childGroup); // удаление дочерней группы из прежнего родителя
//                        });

        oldParrent.ifPresent(o -> o.getListUnderGroups().remove(childGroup));

        childGroup.setParrentId(parrentGroup.getGroupId()); // установка нового родительского uuid для child
        childGroup.setLevelGroup(parrentGroup.getLevelGroup() + 1); // установка нового уровня для child

        parrentGroup.getListUnderGroups().add(childGroup); //добавление подгруппы в родителя

        List<GroupProductDto> listChild = getStructureListGroup(Arrays.asList(childGroup)); //лист с child

        // установка уровней для групп
        listChild.forEach(elmentGr ->
                listChild.stream()  // получение уровня родителя
                        .filter(g -> ObjectUtils.equals(elmentGr.getParrentId(), g.getGroupId())) //получение родителя по uuid если есть
                        .findAny()
                        .ifPresent(parr -> { // если найден родитель
                                    elmentGr.setLevelGroup(parr.getLevelGroup() + 1);
                                } //установка уровней подгрупп
                        ));

     /*   //получение новой структуры подгруппы с ее дочерними группами
        getStructureListGroup(getStructureListGroup(Arrays.asList(childGroup)));*/

        // Удаление из исходного листа всех подгруп перемещяемой группы
        listGroup = listGroup.stream().filter(group -> {
            if (listChild.contains(group)) {
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.toList());

        int i = listGroup.indexOf(parrentGroup);  // получение индекса род. группы

//        listGroup.remove(childGroup); //удаление подгруппы
        //добавление новой подгруппы следующей по списку после родительской с ее дочерними группами
        listGroup.addAll(i + 1, listChild);


        return listGroup; //новый структурированный лист с новой группой
    }


    /**
     * Создание ДТО на отправку изменений по группе
     */
    public GroupProductDto changeParrentIdGroupDto(HttpServletRequest request) {
        GroupProductDto newDto = new GroupProductDto();

        String parrentUuid = request.getParameter("parrent_group_Id");
        String groupId = request.getParameter("id_group");

        if (parrentUuid != null && !parrentUuid.isBlank() && ! parrentUuid.equals("null")) {
            newDto.setParrentId(UUID.fromString(parrentUuid));
        }
        else {
            newDto.setParrentId(null);
        }

        if (groupId != null) {
            newDto.setGroupId(UUID.fromString(groupId));
        }
        return newDto;
    }


    /**
     * Создание ДТО на отправку изменений по группе (перегруженный)
     */
    public GroupProductDto changeParrentIdGroupDto(HttpServletRequest request, String groupUuid) {
        GroupProductDto newDto = new GroupProductDto();

        String parrentUuid = request.getParameter("parrent_group_Id");
        String nameGroup = request.getParameter("name_group");
        String groupId = groupUuid;

        if (parrentUuid != null && ! parrentUuid.isBlank()) {
            newDto.setParrentId(UUID.fromString(parrentUuid));
        }

        if (groupId != null) {
            newDto.setGroupId(UUID.fromString(groupId));
        }

        if (nameGroup != null) {
            newDto.setGroupName(nameGroup);
        }
        return newDto;
    }


    @Value("${pathForSaveImage}")
    private String pathForSave; //путь для сохранения картинки

    /**
     * Получение изображения
     */
    public void getAndSaveImageGroups(List<GroupProductDto> listGruopDTO) {

        for (GroupProductDto group : listGruopDTO) {
            //Получение изображения по uuid группы
            // получить поток байт с сервера
            ResponseEntity<Resource> responseEntity = feignForGroup.getImageGroupById(group.getGroupId().toString());

            try {
                if (Objects.nonNull(responseEntity)) {
                    Resource resource = responseEntity.getBody(); //тело запроса

                    ByteArrayResource byteArrayResource = (ByteArrayResource) resource; //кастинг до ByteArrayResource
                    byte[] imageBytes = byteArrayResource.getByteArray(); // Преобразование Resource в массив байт

                    Path tempFile = Paths.get(pathForSave, group.getGroupId().toString() + ".jpg"); //путь сохранения
//                Path tempFile = Paths.get("E:\\JON\\webSh_front\\src\\main\\resources\\static\\image_group", "gr_im_" + ".jpg");

                    Files.write(tempFile, imageBytes); // запись в файл ".png" по указанному пути
                    System.out.println("Изображение успешно скачано и сохранено: " + tempFile.toAbsolutePath());
                }
            } catch (IOException e) {
                System.out.println("Изображение НЕ сохранено" + e.getMessage());
            }
        }
    }

    /**
     * Получение группы из списка групп по id
     */
    public GroupProductDto getGroupById() {

        return GroupProductDto.builder().build();
    }

    /**
     * Создание нового продукта - параметр "0" - id продукта
     * @return
     */
    public ProductDto createNewProduct() {

        ResponseEntity<ProductDto> response = feignForGroup.getProductById("0");

        return response.getBody();
    }

    /**
     * получение списка картинок для продукта от сервера
     */
    public List<String> getListNameImageProduct(UUID uuidProduct) {

        return feignForGroup.getListNameImageProduct(uuidProduct.toString()).getBody();
    }

    /**
     * получение продукта по id
     */
    public ProductDto getProductDtoById(String productId) {
        return feignForGroup.getProductDtoById(productId).getBody();
    }

    /**
     * получение списка продуктов по id группы
     */
    public List<ProductDto> getListProductDtoByIdGroup(String uuidGroup) {
        if (uuidGroup != null) {
            return feignForGroup.getListProductDtoByIdGroup(uuidGroup).getBody();
        } else {
            return new ArrayList<>();
        }
    }


    /**
     * Установка параметров из формы в ДТО на продукта
     */
    public void setParamfromFORMForProductDto(ProductDto productDto, HttpServletRequest request) {

        List<String> listNameImages = feignForGroup.getListNameImageProduct(productDto.getProductId().toString()).getBody();

        String uuidGroupId = request.getParameter("group_Id");
        String productName = request.getParameter("product_name");
//        String productDescription = request.getParameter("product_description");
//        String productDescription = "";  // пустая строка по умолчанию описание продукта
        String productCount = request.getParameter("count_product");
        String productCoast = request.getParameter("coast_product");
        String productTeg = request.getParameter("teg_product");
        String productView = request.getParameter("no_image_view");
        String productIsload = request.getParameter("is_load");



        if(uuidGroupId != null && ! uuidGroupId.isBlank() && ! uuidGroupId.equals("null")){
            productDto.setGroupsId(UUID.fromString(uuidGroupId));
        }
        else {
            productDto.setGroupsId(null);
        }

        if(uuidGroupId != null && uuidGroupId.isBlank() && ! uuidGroupId.equals("null")){
            productDto.setGroupsId(null);
        }

        if(productName != null){
            productDto.setProduct_name(productName);
        }

//        if(productDescription != null){
//            productDto.setProductDescription(productDescription);
//        }

//        Установка описаний к картинкам
        StringBuilder currentDescription = new StringBuilder();
        for (String imageName: listNameImages){


//            описание к картинке из формы браузера по имени картинки
            String description = request.getParameter("product_description$" + imageName);
            if (description != null) {
                // разделитель по имени картинки
                currentDescription.append(imageName).append("!name!");
                currentDescription.append(description);

                //разделитель описания для картинок
                currentDescription.append("!new!");
            }

        }
        //сохранить no_image в описании если оно существует в форме
        String description = request.getParameter("product_description$no_image");
        if(Objects.nonNull(description)){
            currentDescription.append("no_image!name!").append(description);
        }
        if(! currentDescription.isEmpty()){
            productDto.setProductDescription(currentDescription.toString());
        }


        if(productCount != null){
            productDto.setProductCount(productCount);
        }

        if(productCoast != null){
            productDto.setProductCoast(productCoast);
        }

        if(productTeg != null){
            productDto.setTeg(productTeg);
        }

        if(productView != null){
            productDto.setView_image(productView);
        }
        else {
            productDto.setView_image("image_view");
        }

        if(productIsload != null){
            productDto.setIsLoad(productIsload);
        }
        else {
            productDto.setIsLoad("no_load");
        }


    }

    /**
     *Получить список всех продуктов
     */
    public void getProductAllForGroup(List<GroupProductDto> listGroup) {

//       List<ProductDto> productDtoList = new ArrayList<>();

       for (GroupProductDto grop: listGroup){
           List<ProductDto> currentList = getListProductDtoByIdGroup(grop.getGroupId().toString());

           if(! currentList.isEmpty()){
               grop.setListProduct(currentList);
           }
       }
    }

    /**
     * Получить инфо от формы браузера и сохранить инфо на сервере
     */
    public void saveInfo(HttpServletRequest request) {
        MainInfoDto mainInfoDto = new MainInfoDto();
                Optional.ofNullable(request.getParameter("info"))
                        .ifPresent(mainInfoDto::setInfo);

        Optional.ofNullable(request.getParameter("infoId"))
                .ifPresent(i ->{
                    mainInfoDto.setMainInfoId(Integer.parseInt(i));
                });
        feignForGroup.saveInfo(mainInfoDto);
    }


    /**
     * Получить инфо на сервере
     */
    public Map<Integer, MainInfoDto>  getInfo() {

        return feignForGroup.getInfo().getBody();
    }

    /**
     * Получить список продуктов не входящих ни в какую группу
     */
    public List<ProductDto> getProductNonGroup() {
        return feignForGroup.getProductNonGroup().getBody();
    }


    /**
     * Получить Map описаний картинок из поля String Description  описания продукта - распарсить String Description
     */
    public Map<String, String> getMapDescriptionProduct(ProductDto product, List<String> listNameImages) {

        if(product.getProductDescription() == null){
            product.setProductDescription("");
        }

        String[] parseDescriptionByCountImages = product.getProductDescription().split("!new!");

        //если описание отсутствует
        if (parseDescriptionByCountImages.length == 0) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("no_image", "");
            return map;
        }
        //создание map из описания продукта, ключ имя картинки
        Map<String, String> mapDescription = Arrays.stream(parseDescriptionByCountImages)
                .map(desc -> desc.split("!name!"))
                .filter(parts -> parts.length == 2) // фильтрация элементов с неправильным форматом
                .collect(Collectors.toMap(
                        parts -> parts[0], // ключ
                        parts -> parts[1]  // значение
                ));


        if(mapDescription.isEmpty()){
            mapDescription.put("no_image", product.getProductDescription());
            return mapDescription;
        }

        // builder для сбора описаний для которых нет картинок(удалены)
        StringBuilder no_imageDescription = new StringBuilder();


        //проход по списку описаний с помощью итератора и удалить и удалить не соответствующие описания(переложить)
        Iterator<Map.Entry<String, String>> iterator = mapDescription.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();

            // если имя описания есть в списке картинок, продолжаем следующую итерацию
            if (listNameImages.contains(entry.getKey())) {
                continue;
            }
            // положить в "no_image" описания без картинок
            no_imageDescription.append(entry.getValue()).append("\n");

            // удаляем элемент из мапы через итератор
            iterator.remove(); // безопасно удаляется элемент
        }

        // положить в мап все описания без картинок с ключом "no_image"
        if (! no_imageDescription.isEmpty()){
            mapDescription.put("no_image", no_imageDescription.toString());
        }

        return mapDescription;

     }

    /**
     * Получить лист имен картинок продукта без расширений типа .jpg
     */
    public List<String> getListNameImageProductNoExpansion(List<String> listName) {

        return listName.stream().map(name -> {
            String[] arrName = name.split("\\.");
            if(arrName.length > 1){
                name = arrName[0];  //после разделении полного имени берется только первая часть без расширения
            }
            return name;
        }).toList();
    }

    /**
     * изменить имя картинки продукта
     */
        public void changeNameImageProduct(String productId, String currentNameImage, String newName) {
            feignForGroup.changeNameImageProduct(productId, currentNameImage, newName);
    }
}
