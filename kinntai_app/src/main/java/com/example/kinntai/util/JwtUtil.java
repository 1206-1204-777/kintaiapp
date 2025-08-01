package com.example.kinntai.util;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}") // application.properties からシークレットキーを読み込む
    private String SECRET_KEY;

    @Value("${jwt.expiration}") // application.properties から有効期限を読み込む
    private long EXPIRATION_TIME;

    // JWT生成
 // JwtUtil.java の generateToken メソッド修正版
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        // ★修正: ROLE_プレフィックスを正しく付与
        claims.put("authorities", List.of("ROLE_" + role.toUpperCase()));
        
        // デバッグログ追加
        System.out.println("JWT生成: username=" + username + ", role=" + role + ", authorities=" + claims.get("authorities"));
        
        return createToken(claims, username);
    }
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 有効期限を設定
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // シークレットキーをデコードしてKeyオブジェクトを生成
    public Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // トークンから全てのクレームを抽出
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 特定のクレームを抽出
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // トークンからユーザー名を抽出
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // トークンの有効期限を抽出
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // トークンが有効期限切れかチェック
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // トークンが有効か検証
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}