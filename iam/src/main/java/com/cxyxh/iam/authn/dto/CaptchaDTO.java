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
package com.cxyxh.iam.authn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码DTO
 *
 * @author IAM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaDTO {

    /**
     * 验证码ID
     */
    private String captchaId;

    /**
     * 验证码图片（Base64编码）
     */
    private String captchaImage;
    
    /**
     * 过期时间（毫秒时间戳）
     */
    private long expireTime;
} 