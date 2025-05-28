package com.example.kinntai.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy; // SessionCreationPolicy をインポート
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // フィルターの追加位置指定のため
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.kinntai.filter.JwtAuthenticationFilter; // 新しく作成するJWTフィルターをインポート

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // JwtAuthenticationFilterをコンストラクタインジェクション
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // CSRF を無効化 (REST API の場合)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorize -> authorize
                // 認証なしでアクセス許可するパス
                // ログイン、登録、静的ファイルなど
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/signup",
                    "/css/**", "/js/**", "/images/**", "/webjars/**", "/**.ico",
                    "/", "/index.html", "/login.html", "/register.html", "/**.html",
                    "/error" // エラーページへのアクセスも許可
                ).permitAll()
                // その他の全ての /api/** パスは認証が必要 (JWTフィルターで認証される)
                .requestMatchers("/api/**").authenticated()
                // 上記以外のすべてのリクエストは認証が必要 (SPAの場合)
                // ただし、/api/** 以外のHTMLなどへのアクセスは /index.html で許可済み
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // セッションを使わない (JWT認証の場合)
            )
            .formLogin(AbstractHttpConfigurer::disable)  // フォームログインを無効化
            .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 認証を無効化
            .logout(logout -> logout // ログアウト設定（Spring SecurityのログアウトURLを無効化）
                .logoutUrl("/logout") // このURLへのアクセスでSpring Securityのセッションがクリアされるが、STATLESSなのであまり意味はない
                .logoutSuccessUrl("/login.html") // ログアウト成功時のリダイレクト先
                .permitAll() // ログアウトは認証なしでアクセス許可
            );
        
        // JWT認証フィルターをUsernamePasswordAuthenticationFilterの前に挿入
        // これにより、各リクエストがSpring Securityの認証フローに入る前にJWTを検証
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); // 本番ではフロントエンドのオリジンを明示的に設定
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*")); // Authorization ヘッダーなど
        configuration.setAllowCredentials(false); // JWTは通常Credentialsを必要としない (Cookieを使用する場合はtrue)
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManagerをBeanとして公開 (AuthServiceで使用)
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}