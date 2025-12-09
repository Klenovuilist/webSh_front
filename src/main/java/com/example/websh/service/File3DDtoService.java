package com.example.websh.service;


import com.example.websh.clients.FeignClient;
import com.example.websh.dto.CommentDto;
import com.example.websh.dto.File3DDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

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
        public void saveFile3DDtoIfExist(HttpServletRequest request, String fileId /*boolean isAdmin*/){

            //получение существующего файла File3DDto из БД
            File3DDto file3DDto = userService.getFile3DDtoById(fileId);
            if (file3DDto == null){
                return;
            }

            // добавить комментарий к существующему если есть
           if(request.getParameter("file_comment") != null
                   && ! request.getParameter("file_comment").isBlank()
                    && request.getParameter("isAdmin") != null){

               //установка что файл менялся
               file3DDto.setApproval(true);
               //признак автора комментария по умолчанию пользователь "&~0"
               String autorComment = "&~0";

               if(request.getParameter("isAdmin").equals("true")){
                   autorComment = "&~1";
               }

               if(! file3DDto.getComment().isBlank()){
                   file3DDto.setComment(file3DDto.getComment()
                           + "$~"
                           + autorComment
                           + request.getParameter("file_comment").trim());
               }
               else {
                   file3DDto.setComment(autorComment + request.getParameter("file_comment").trim());
               }
           }

            if(request.getParameter("file_fileName") != null){
                file3DDto.setFileName(request.getParameter("file_fileName"));
            }

            if(request.getParameter("file_status") != null){

                //установка что файл менялся
                file3DDto.setApproval(true);

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
                //установка что файл менялся
                file3DDto.setApproval(true);

                file3DDto.setCoast(request.getParameter("file_coast"));
            }

            if(request.getParameter("file_material") != null){
                file3DDto.setMaterial(request.getParameter("file_material"));
            }

            if(request.getParameter("file_isDelete") != null){
                file3DDto.setDelete(true);
            }

//            if(request.getParameter("isApproval") != null){
//                file3DDto.setDelete(true);
//            }
            //сохранить измененную File3DDto
            feignClient.saveFile3DDto(file3DDto);
        }


    /**
     * Получить список описаний File3DDto загруженных файлов 3D по id пользователя (в Postgress)
     */
    public List<File3DDto> getListFile3DDtoByUsersId(String userId, boolean isDelete) {

        List<File3DDto> listFile3DDto = feignClient.getListFile3DDescriptionUsers(userId).getBody();

        //создание листа коментариев (переписки) админа и пользователя из строки комментариев
        for (File3DDto file: listFile3DDto){
            file.setCommentList(getListComment(file.getComment()));
        }

        //в коллекции помеченные на удаленные
        if (! listFile3DDto.isEmpty() && isDelete){
            return listFile3DDto.stream().filter(file -> file.isDelete()).toList();
        }
        // убрать из коллекции помеченные на удаленные
        else if (! listFile3DDto.isEmpty()) {
            return listFile3DDto.stream().filter(file -> ! file.isDelete()).toList();

        }
        return listFile3DDto;
    }

    /**
     * Получить List<CommentDto> диалога (комментариев) к файлу - распарсить комментарии
     * и установить кто автор сообщений
     */
    public List<CommentDto> getListComment(String fullComment){

        List<CommentDto> listComment = new ArrayList<>();

        if(Objects.isNull(fullComment) || fullComment.isBlank()){
            return listComment;
        }

        String arrComment[] = fullComment.split("\\$~");
          for (String comment: arrComment){
              CommentDto commentDto = new CommentDto();
              String autorComment = "";

              if (comment.length() >= 3) {
                  autorComment = comment.substring(0,3);
              }

              if(autorComment.equals("&~0")){
                  commentDto.setAutor("user");
              }

              if(autorComment.equals("&~1")){
                  commentDto.setAutor("admin");
              }
              commentDto.setMessage(comment.substring(3));
              listComment.add(commentDto);
              }
             return listComment;
               }


}
