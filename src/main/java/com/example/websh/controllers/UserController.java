package com.example.websh.controllers;

import com.example.websh.dto.UserDto;
import com.example.websh.service.JwtService;
import com.example.websh.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class UserController {

    private final JwtService jwtService;

    private final UserService userService;


    /**
     * Страница пользователя
     * @return
     */
    @GetMapping("/index_admin/user/{userId}")
    public String userPage(Model model, @PathVariable("userId") String userId){

        model.addAttribute("user", userService.getUserById(userId));

        return "user_page.html";
    }



}
