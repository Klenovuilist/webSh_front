package com.example.websh.socket;

import java.io.*;
import java.net.Socket;

public class SocketTest {

    private static final String HOST = "localhost"; // Адрес хоста
    private static final int PORT = 8099;          // Порт
    private static final String PATH = "/api/get_list_group"; // Путь к ресурсу

    public static void main(String[] args) throws IOException {


        Socket socket2 = new Socket("localhost", 8099);
//        Socket socket2 = new Socket("smart18.ru", 80);
        DataOutputStream dataOutputStream = new DataOutputStream(socket2.getOutputStream());

        DataInputStream dataInputStream = new DataInputStream(socket2.getInputStream());

        dataOutputStream.writeBytes("POST " + "/api/ListNameImageProduct/9f07fcd8-c41d-47a5-ae18-376544f26502" + " HTTP/1.1\r\n");
        dataOutputStream.writeBytes("Host: " + "localhost" + "\r\n");

//        dataOutputStream.writeBytes("Connection: close\r\n");
        dataOutputStream.writeBytes("Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIsIm5hbWUiOiJyaXRhIiwic3ViIjoidXNlckVudGl0eSIsImlhdCI6MTc1NTA5MTI2NSwiZXhwIjoxNzU2ODkxMjY1fQ.k32r0DRqd8KsFpxXnwL23TDiDbJxB5_wys-wG9D-i74\r\n");
        dataOutputStream.writeBytes("\r\n");

        // Обязательно сброс буфера, чтобы данные были переданы немедленно
        dataOutputStream.flush();

        byte[] bytes = dataInputStream.readAllBytes();
        String str = new String(bytes);
        System.out.println(str);

    }
}
