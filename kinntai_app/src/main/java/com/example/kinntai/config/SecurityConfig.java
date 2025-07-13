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
				.csrf(AbstractHttpConfigurer::disable) // CSRF を無効化 (REST API の場合)
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeHttpRequests(authorize -> authorize
						// 認証なしでアクセス許可するパス
						// Swagger UI および OpenAPI ドキュメントのエンドポイントを許可
						.requestMatchers(
								"/swagger-ui/**",
								"/v3/api-docs/**",
								"/swagger-resources/**",
								"/webjars/**"
						).permitAll()
						// 認証関連のエンドポイントを許可
						.requestMatchers(
								"/api/auth/login",
								"/api/auth/signup"
						).permitAll()
						// React開発用に一時的に全ての/api/**を許可 (本番移行時に削除または認証を追加)
						// または、認証が必要なAPIはJWTフィルターで認証されるようにする
						// 例: /api/schedule/submit や /api/schedule/week など、Reactから呼び出すAPIのパスをここに含める
						.requestMatchers(
								"/api/attendance/**", // 既存の勤怠API
								"/api/schedule/**", // 新しいスケジュールAPI (例: /api/schedule/submit, /api/schedule/week)
								"/api/overtime/**", // 残業申請APIなど、今後追加されるAPI
								"/api/location/**", // 勤務地登録APIなど、今後追加されるAPI
								"/api/dev/**", // 開発用API
								"/api/batch/start-weekly-job" // バッチジョブAPI
						).permitAll() // 開発中は一時的にpermitAll()にするのが便利です
						// 静的ファイルやルートパス、エラーページを許可
						.requestMatchers(
								"/css/**", "/js/**", "/images/**", "/**.ico",
								"/", "/index.html", "/login.html", "/register.html", "/**.html",
								"/error"
						).permitAll()
						// 上記以外のすべてのリクエストは認証が必要 (JWTフィルターで認証される)
						// 開発中は一時的に上記でpermitAll()にすることで、認証なしでAPIを叩けるようにしています。
						// 本番環境では、認証が必要なAPIパスはここで保護されるべきです。
						.anyRequest().authenticated()
				)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // セッションを使わない (JWT認証の場合)
				)
				.formLogin(AbstractHttpConfigurer::disable) // フォームログインを無効化
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
		// ここをReact開発サーバーのオリジンに明示的に変更する
		// 例: React開発サーバーが http://localhost:3000 で動作している場合
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // 本番ではフロントエンドのオリジンを明示的に設定
		// ワイルドカード (*) はセキュリティリスクがあるため、本番環境では使用しない
		// configuration.setAllowedOrigins(Arrays.asList("*")); // 元のコード

		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type")); // 必要に応じてヘッダーを明示的に指定
		// configuration.setAllowedHeaders(Arrays.asList("*")); // 元のコード

		configuration.setAllowCredentials(false); // JWTは通常Credentialsを必要としない (Cookieを使用する場合はtrue)

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
