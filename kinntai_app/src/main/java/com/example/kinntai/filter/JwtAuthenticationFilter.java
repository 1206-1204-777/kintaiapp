package com.example.kinntai.filter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.kinntai.util.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // Bearer トークンの抽出
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                System.err.println("JWT解析エラー: " + e.getMessage());
            }
        }

        // ユーザー名があり、まだ認証されていない場合
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // UserDetailsを取得
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // トークンが有効かチェック
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // JWTからクレームを取得
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(jwtUtil.getSignKey())
                            .build()
                            .parseClaimsJws(jwt)
                            .getBody();

                    // 権限情報を取得
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) claims.get("authorities");
                    
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    // 認証トークンを作成
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, // ★重要: userDetailsを設定（以前はuserDetailsServiceだった）
                            null,
                            authorities);
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    System.out.println("JWT認証成功: " + username + ", 権限: " + authorities);
                } else {
                    System.err.println("無効なJWTトークン: " + username);
                }
            } catch (Exception e) {
                System.err.println("JWT認証処理エラー: " + e.getMessage());
                e.printStackTrace();
            }
        }

        filterChain.doFilter(request, response);
    }
}