package com.example.websh.config;

import com.example.websh.filters.JwtFilter;

import com.example.websh.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * Конфигурация для Sequrity
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true) // для обработки анотаций @Secured - позволяет контролировать доступ к методам на основании ролей пользователя.

@AllArgsConstructor
public class SequrityConfig {
    /**
     * Bean BCryptPasswordEncoder - класс для хеширования паролей при сохранении и чтении из БД
     * метод .encode(password) - хеширование пароля пред сохранением
     * метод .matches(password, userService.getPassword()) - сравнение паролей, авторасшифровка пароля
     *
     */

    public final UserService userService;

    public final JwtFilter jwtFilter;

    @Bean// кодировщик паролей
    public BCryptPasswordEncoder bCryptasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean // Выполнение аутентификации, простая аутентификация работает и без него,
    //через DaoAuthenticationProvider -> UserService implements UserDetailsService обращается к базе что бы сравнить введённые пользователем данные с базой.
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean // для аутентификации пользователей, через UserService implements UserDetailsService обращается к базе.
        //что бы сравнить введённые пользователем данные с базой.
    // В простом варианте работает без него
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(bCryptasswordEncoder()); // установка кодировщика паролей
        daoAuthenticationProvider.setUserDetailsService(userService); // установка сервиса для получения данных пользователя из БД
        return daoAuthenticationProvider;
    }



    @Bean // настройка доступа к методам контроллеров. можно @PreAuthorize, @PostAuthorize  на уровне методов.
    //  В простом варианте работает без него
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests((authz) -> authz
                                .requestMatchers(HttpMethod.GET,"/athurizathion/**").permitAll()
                                .requestMatchers(HttpMethod.POST,"/athurizathion/**").permitAll()
                                .requestMatchers(HttpMethod.POST,"/register/**").permitAll()
                                .requestMatchers(HttpMethod.GET,"/users/verify/{idUser}/**").permitAll() //подтверждение регистрации


                                .requestMatchers(HttpMethod.POST,"/login/**").permitAll()

//                                .requestMatchers(HttpMethod.GET,"/index/**").permitAll()

//                                .requestMatchers(HttpMethod.GET,"/index_admin/**").permitAll()

                                .requestMatchers(HttpMethod.GET,"/css/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/js/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/js/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/image/**").permitAll()                   // Открытый доступ к публичным ресурсам

                                .requestMatchers("/index_admin/**").hasRole("ADMIN")
//                                .requestMatchers("/index_admin").hasRole("ADMIN")

                                .requestMatchers(HttpMethod.GET, "/**").permitAll()

                                .requestMatchers(HttpMethod.POST, "/authorize_user/**").permitAll()
//                                .requestMatchers(HttpMethod.PUT, "/save_test_data/**").permitAll()  // Открытый доступ к публичным ресурсам /authorize

//                                .requestMatchers(HttpMethod.GET,"/**").permitAll()

//                                .requestMatchers("/delete/test_data/**").hasRole("ADMIN")              // Доступ  только для администраторов
//                                .requestMatchers("/**").hasAnyRole("USER", "ADMIN")     // Пользователи и администраторы имеют доступ к "/api/user/**"
                        .anyRequest().authenticated()             // остальные маршруты доступны только для после аутентификации
                )
                .csrf(AbstractHttpConfigurer::disable) // отключение CSRF-защиты
                /*.csrf(AbstractHttpConfigurer::disable)*/
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))// не хранить сессию, при использовании jwt не нужно

                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(
                        new LoginUrlAuthenticationEntryPoint("/athurizathion"))) // переадресация на адрес если ошибка аутентификации(аутентифик..не выполнена)
//                .formLogin(login -> login
//                        .loginPage("/api/login/")                                             // Переадресация на страницу входа
//                        .defaultSuccessUrl("/api/get_testData/", true))         // Куда перенаправлять после успешной аутентификации
//                .logout(logout -> logout
//                        .logoutUrl("/api/logout/")                                            // URL для завершения сессии, разлогинится
//                        .logoutSuccessUrl("/api/login/"))   // Куда переадресовать после выхода
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)   //добавление фильтра на проверку заголовка с
        // токеном (перед стандартным фильтром UsernamePasswordAuthenticationFilter)
        // и получение пользователя из токена, помещение в секъюрити контекст
        ;

        return httpSecurity.build();
    }































































































}
