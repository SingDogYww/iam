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
package com.cxyxh.iam.authn.service;

import com.cxyxh.iam.authn.dto.JwtTokenDTO;

/**
 * JWT服务接口
 *
 * @author IAM
 */
public interface JwtService {

    /**
     * 生成JWT令牌
     *
     * @param username 用户名
     * @param userId   用户ID
     * @return JWT令牌DTO
     */
    JwtTokenDTO generateToken(String username, Long userId);

    /**
     * 刷新JWT令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的JWT令牌DTO
     */
    JwtTokenDTO refreshToken(String refreshToken);

    /**
     * 验证JWT令牌
     *
     * @param token JWT令牌
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 从JWT令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    String getUsernameFromToken(String token);

    /**
     * 从JWT令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    Long getUserIdFromToken(String token);

    /**
     * 使JWT令牌失效
     *
     * @param token JWT令牌
     */
    void invalidateToken(String token);
} 