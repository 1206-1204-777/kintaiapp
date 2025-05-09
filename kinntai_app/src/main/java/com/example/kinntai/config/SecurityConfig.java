package com.example.kinntai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF保護を無効化し、すべてのエンドポイントへのアクセスを許可
        http
            .csrf(csrf -> csrf.disable())  // APIのPOSTリクエスト用にCSRF保護を無効化
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()  // すべてのリクエストを許可
            );
        
        return http.build();
    }
}