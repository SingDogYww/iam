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

import com.cxyxh.iam.authn.dto.CaptchaDTO;

/**
 * 验证码服务接口
 *
 * @author IAM
 */
public interface CaptchaService {
    
    /**
     * 生成验证码
     *
     * @return 验证码DTO
     */
    CaptchaDTO generateCaptcha();
    
    /**
     * 验证验证码
     *
     * @param captchaId 验证码ID
     * @param captchaCode 验证码
     * @return 是否有效
     */
    boolean validateCaptcha(String captchaId, String captchaCode);
} 