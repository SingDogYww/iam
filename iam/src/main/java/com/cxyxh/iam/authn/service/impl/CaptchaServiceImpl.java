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

import com.cxyxh.iam.authn.dto.CaptchaDTO;
import com.cxyxh.iam.authn.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 *
 * @author IAM
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    @Value("${captcha.expiration:180}")
    private long captchaExpiration; // 验证码过期时间，默认3分钟
    
    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final Random RANDOM = new Random();

    @Override
    public CaptchaDTO generateCaptcha() {
        // 生成随机验证码
        String code = generateRandomCode(CODE_LENGTH);
        
        // 生成验证码图片
        String base64Image = generateImageBase64(code);
        
        // 生成验证码ID
        String captchaId = UUID.randomUUID().toString();
        
        // 存储验证码到Redis，设置过期时间
        redisTemplate.opsForValue().set(
                CAPTCHA_PREFIX + captchaId,
                code,
                captchaExpiration,
                TimeUnit.SECONDS
        );
        
        // 计算过期时间（毫秒时间戳）
        long expireTime = System.currentTimeMillis() + (captchaExpiration * 1000);
        
        // 返回验证码DTO
        return CaptchaDTO.builder()
                .captchaId(captchaId)
                .captchaImage(base64Image)
                .expireTime(expireTime)
                .build();
    }

    @Override
    public boolean validateCaptcha(String captchaId, String captchaCode) {
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            return false;
        }
        
        // 从Redis获取验证码
        String key = CAPTCHA_PREFIX + captchaId;
        String storedCode = (String) redisTemplate.opsForValue().get(key);
        
        if (storedCode == null) {
            return false; // 验证码不存在或已过期
        }
        
        // 验证后，删除验证码
        redisTemplate.delete(key);
        
        // 不区分大小写比较验证码
        return storedCode.equalsIgnoreCase(captchaCode);
    }
    
    /**
     * 生成随机验证码
     *
     * @param length 验证码长度
     * @return 随机验证码
     */
    private String generateRandomCode(int length) {
        // 包含数字和字母，排除容易混淆的字符如0和O、1和I等
        String chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return code.toString();
    }
    
    /**
     * 生成验证码图片的Base64编码
     *
     * @param code 验证码
     * @return Base64编码的图片
     */
    private String generateImageBase64(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 绘制干扰线
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 20; i++) {
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = x1 + RANDOM.nextInt(20) - 10;
            int y2 = y1 + RANDOM.nextInt(20) - 10;
            g.drawLine(x1, y1, x2, y2);
        }
        
        // 绘制验证码
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(new Color(0, 0, 0));
        
        for (int i = 0; i < code.length(); i++) {
            // 随机旋转角度
            double theta = (RANDOM.nextInt(60) - 30) * Math.PI / 180;
            g.rotate(theta, 25 + i * 20, 25);
            g.drawString(String.valueOf(code.charAt(i)), 20 + i * 20, 28);
            g.rotate(-theta, 25 + i * 20, 25);
        }
        
        g.dispose();
        
        // 转换为Base64编码
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.error("生成验证码图片失败", e);
            return "";
        }
    }
} 