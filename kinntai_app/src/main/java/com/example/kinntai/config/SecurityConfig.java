package com.example.kinntai.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.kinntai.filter.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http
	            .csrf(AbstractHttpConfigurer::disable)
	            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
	            .authorizeHttpRequests(authorize -> authorize
	                    // 認証なしでアクセス許可するパス
	                    .requestMatchers(
	                            "/swagger-ui/**",
	                            "/v3/api-docs/**",
	                            "/swagger-resources/**",
	                            "/webjars/**"
	                    ).permitAll()
	                    .requestMatchers(
	                            "/api/auth/login",
	                            "/api/auth/signup"
	                    ).permitAll()
	                    
	                    // ★★★ 一時的に管理者APIを開放（デバッグ用） ★★★
	                    .requestMatchers("/api/admin/**").permitAll()

	                    // 既存のAPIパス設定
	                    .requestMatchers(
	                            "/api/attendance/**",
	                            "/api/schedule/**",
	                            "/api/overtime/**",
	                            "/api/location/**",
	                            "/api/dev/**",
	                            "/api/batch/start-weekly-job"
	                    ).permitAll()
	                    
	                    // 静的ファイルやルートパス、エラーページを許可
	                    .requestMatchers(
	                            "/css/**", "/js/**", "/images/**", "/**.ico",
	                            "/", "/index.html", "/login.html", "/register.html", "/**.html",
	                            "/error"
	                    ).permitAll()
	                    
	                    // 上記以外のすべてのリクエストは認証が必要
	                    .anyRequest().authenticated()
	            )
	            .sessionManagement(session -> session
	                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	            )
	            .formLogin(AbstractHttpConfigurer::disable)
	            .httpBasic(AbstractHttpConfigurer::disable)
	            .logout(logout -> logout
	                    .logoutUrl("/logout")
	                    .logoutSuccessUrl("/login.html")
	                    .permitAll()
	            );

	    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
		configuration.setAllowCredentials(true); // ★修正: trueに変更
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
}
