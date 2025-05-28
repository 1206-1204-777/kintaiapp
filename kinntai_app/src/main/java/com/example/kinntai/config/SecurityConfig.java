package com.example.kinntai.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorize -> authorize
                // 認証なしでアクセス許可するパスを明示的に指定
                // 静的リソース
                .requestMatchers(
                    "/css/**", "/js/**", "/images/**", "/webjars/**", "/**.ico"
                ).permitAll()
                // HTMLファイル
                .requestMatchers(
                    "/", "/index.html", "/login.html", "/register.html", "/**.html"
                ).permitAll()
                // 認証関連のAPI
                .requestMatchers(
                    "/api/auth/login", "/api/auth/signup"
                ).permitAll()
                // その他のすべてのリクエストは一時的に許可（デバッグ用）
                // ログイン問題が解決したら、認証が必要なパスを設定します
                .anyRequest().permitAll() // ここを許可する
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(logout -> logout // ログアウト設定を追加（既存のログアウトボタンに対応）
                .logoutUrl("/logout") // ログアウト処理を行うURL
                .logoutSuccessUrl("/login.html") // ログアウト成功時のリダイレクト先
                .permitAll() // ログアウトは認証なしでアクセス許可
            );
        
        return http.build();
    }
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false); // 認証情報は不要 (JWT導入時にtrueを検討)
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}