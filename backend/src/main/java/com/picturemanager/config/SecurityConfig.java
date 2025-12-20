// src/main/java/com/picturemanager/config/SecurityConfig.java
package com.picturemanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // 禁用 CSRF
                .cors(cors -> cors.configure(http))  // 启用 CORS
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // 允许所有请求，无需认证
                );

        return http.build();
    }
}