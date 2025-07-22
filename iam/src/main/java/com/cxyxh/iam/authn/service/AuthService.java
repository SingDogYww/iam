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
import com.cxyxh.iam.authn.dto.LoginDTO;
import com.cxyxh.iam.authn.dto.LoginUserVO;

import java.util.Map;

/**
 * 认证服务接口
 *
 * @author IAM
 */
public interface AuthService {

    /**
     * 登录
     *
     * @param loginDTO 登录DTO
     * @return 登录用户视图对象
     */
    LoginUserVO login(LoginDTO loginDTO);

    /**
     * 登出
     *
     * @param token 令牌
     * @return 是否成功
     */
    boolean logout(String token);

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return JWT令牌DTO
     */
    JwtTokenDTO refreshToken(String refreshToken);

    /**
     * 获取验证码
     *
     * @return 验证码信息（key和图片Base64）
     */
    Map<String, String> getCaptcha();

    /**
     * 验证验证码
     *
     * @param captchaKey  验证码Key
     * @param captchaCode 验证码
     * @return 是否有效
     */
    boolean validateCaptcha(String captchaKey, String captchaCode);
} 