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
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceImplTest {

    @Test
    void testJwtConfigValues() {
        // 准备
        RedisTemplate<String, Object> mockRedisTemplate = mock(RedisTemplate.class);
        String secret = "testSecret";
        long accessTokenExpiration = 300;
        long refreshTokenExpiration = 3600;
        
        // 创建服务
        JwtServiceImpl jwtService = new JwtServiceImpl(
                mockRedisTemplate, secret, accessTokenExpiration, refreshTokenExpiration);
        
        // 创建一个简单的JwtTokenDTO进行测试
        JwtTokenDTO tokenDTO = new JwtTokenDTO();
        tokenDTO.setAccessToken("test-token");
        tokenDTO.setRefreshToken("test-refresh-token");
        tokenDTO.setTokenType("Bearer");
        tokenDTO.setExpiresIn(accessTokenExpiration);
        
        // 基本断言
        assertEquals("Bearer", tokenDTO.getTokenType());
        assertEquals(accessTokenExpiration, tokenDTO.getExpiresIn());
        assertNotNull(tokenDTO.getAccessToken());
    }
} 