package com.example.websh.service;


import com.example.websh.clients.FeignClient;
import com.example.websh.dto.File3DDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Класс для работы с файлами описаний из БД для сохраненных на сервере файлов
 */
@Service
@RequiredArgsConstructor
    public class File3DDtoService {

    private final UserService userService;

    private final FeignClient feignClient;

    /**
     * Сохранить изменения в File3DDto если она существует по параметрам из запроса
     */
        public void saveFile3DDtoIfExist(HttpServletRequest request, String fileId){

            //получени существующего файла File3DDto из БД
            File3DDto file3DDto = userService.getFile3DDtoById(fileId);
            if (file3DDto == null){
                return;
            }

           if(request.getParameter("file_comment") != null){
               file3DDto.setComment(request.getParameter("file_comment"));
           }

            if(request.getParameter("file_fileName") != null){
                file3DDto.setFileName(request.getParameter("file_fileName"));
            }

            if(request.getParameter("file_status") != null){
                file3DDto.setStatus(request.getParameter("file_status"));

                //Отправить письмо админу если направлено на рачет стоимости
                if(request.getParameter("file_status").equals("отправлен на расчет стоимости")){
                    String text = "Пользователь: " + request.getParameter("user_login") + "\n для расчета стоимости " +
                            "отправил файл: \n" +
                            request.getParameter("file_fileName");
                    String subject = "Расчет стоимости: " + request.getParameter("file_fileName");

                    userService.sendWorkEmail(text, subject);
                }

            }

            if(request.getParameter("file_coast") != null){
                file3DDto.setCoast(request.getParameter("file_coast"));
            }

            if(request.getParameter("file_material") != null){
                file3DDto.setMaterial(request.getParameter("file_material"));
            }

            if(request.getParameter("file_isDelete") != null){
                file3DDto.setDelete(true);
            }

            if(request.getParameter("isApproval") != null){
                file3DDto.setDelete(true);
            }
            //сохранить измененную File3DDto
            feignClient.saveFile3DDto(file3DDto);
        }



}
