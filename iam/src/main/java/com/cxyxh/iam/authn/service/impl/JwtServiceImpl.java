/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cxyxh.iam.authn.service.impl;

import com.cxyxh.iam.authn.dto.JwtTokenDTO;
import com.cxyxh.iam.authn.service.JwtService;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * JWT 服务实现类
 *
 * @author IAM
 */
@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String secret;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    /**
     * 构造方法
     *
     * @param redisTemplate         Redis模板
     * @param secret                密钥
     * @param accessTokenExpiration 访问令牌过期时间
     * @param refreshTokenExpiration 刷新令牌过期时间
     */
    public JwtServiceImpl(
            RedisTemplate<String, Object> redisTemplate,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.redisTemplate = redisTemplate;
        this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @Override
    public JwtTokenDTO generateToken(String username, Long userId) {
        // 生成访问令牌
        String accessToken = generateAccessToken(username, userId);
        
        // 生成刷新令牌
        String refreshToken = generateRefreshToken(username, userId);
        
        // 构建并返回令牌DTO
        return JwtTokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration)
                .build();
    }

    @Override
    public JwtTokenDTO refreshToken(String refreshToken) {
        try {
            // 验证刷新令牌
            if (!validateToken(refreshToken)) {
                throw new IllegalArgumentException("无效的刷新令牌");
            }
            
            // 从刷新令牌中获取用户名和用户ID
            String username = getUsernameFromToken(refreshToken);
            Long userId = getUserIdFromToken(refreshToken);
            
            // 检查刷新令牌是否在黑名单中
            String blacklistKey = "jwt:blacklist:" + refreshToken;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                throw new IllegalArgumentException("刷新令牌已失效");
            }
            
            // 将旧的刷新令牌加入黑名单
            redisTemplate.opsForValue().set(blacklistKey, true, refreshTokenExpiration, TimeUnit.SECONDS);
            
            // 生成新的令牌
            return generateToken(username, userId);
        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage());
            throw new IllegalArgumentException("刷新令牌失败");
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            // 解析令牌
            Jwts.parser()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token);
                
            // 检查令牌是否在黑名单中
            String blacklistKey = "jwt:blacklist:" + token;
            return !Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
        } catch (ExpiredJwtException e) {
            log.error("JWT已过期: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("JWT格式不正确: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.error("无效的JWT签名: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT声明为空: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("验证JWT时发生异常: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getSubject();
    }

    @Override
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return Long.valueOf(claims.get("userId", String.class));
    }

    @Override
    public void invalidateToken(String token) {
        try {
            // 解析令牌获取过期时间
            Claims claims = getAllClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            long expirationTime = expiration.getTime() - System.currentTimeMillis();
            
            // 如果令牌已经过期，则不需要加入黑名单
            if (expirationTime <= 0) {
                return;
            }
            
            // 将令牌加入黑名单，过期时间与令牌相同
            String blacklistKey = "jwt:blacklist:" + token;
            redisTemplate.opsForValue().set(blacklistKey, true, expirationTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("使令牌失效时发生异常: {}", e.getMessage());
        }
    }

    /**
     * 生成访问令牌
     *
     * @param username 用户名
     * @param userId   用户ID
     * @return 访问令牌
     */
    private String generateAccessToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("type", "access");
        return buildToken(claims, username, accessTokenExpiration);
    }

    /**
     * 生成刷新令牌
     *
     * @param username 用户名
     * @param userId   用户ID
     * @return 刷新令牌
     */
    private String generateRefreshToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());
        claims.put("type", "refresh");
        return buildToken(claims, username, refreshTokenExpiration);
    }

    /**
     * 构建令牌
     *
     * @param claims      声明
     * @param subject     主题（用户名）
     * @param expiration  过期时间（秒）
     * @return 令牌
     */
    private String buildToken(Map<String, Object> claims, String subject, long expiration) {
        long currentTimeMillis = System.currentTimeMillis();
        Date issuedAt = new Date(currentTimeMillis);
        Date expirationDate = new Date(currentTimeMillis + expiration * 1000);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    /**
     * 从令牌中获取所有声明
     *
     * @param token 令牌
     * @return 声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }
} 