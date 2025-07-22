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
import com.cxyxh.iam.authn.dto.LoginDTO;
import com.cxyxh.iam.authn.dto.LoginUserVO;
import com.cxyxh.iam.authn.service.AuthService;
import com.cxyxh.iam.authn.service.JwtService;
import com.cxyxh.iam.config.security.userdetails.SecurityUser;
import com.cxyxh.iam.user.entity.User;
import com.cxyxh.iam.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 *
 * @author IAM
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${captcha.expiration}")
    private long captchaExpiration;

    @Value("${captcha.width}")
    private int captchaWidth;

    @Value("${captcha.height}")
    private int captchaHeight;

    @Value("${captcha.length}")
    private int captchaLength;

    @Override
    public LoginUserVO login(LoginDTO loginDTO) {
        // 验证验证码
        if (StringUtils.hasText(loginDTO.getCaptchaKey()) && StringUtils.hasText(loginDTO.getCaptcha())) {
            boolean valid = validateCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptcha());
            if (!valid) {
                throw new IllegalArgumentException("验证码错误或已过期");
            }
        }

        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));
            
            // 设置认证信息到上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 获取用户信息
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
            User user = userService.getUserByUsername(securityUser.getUsername());
            
            // 生成JWT令牌
            JwtTokenDTO jwtTokenDTO = jwtService.generateToken(user.getUsername(), user.getId());
            
            // 获取用户角色和权限
            List<String> roles = userService.getUserRoles(user.getId());
            List<String> permissions = userService.getUserPermissions(user.getId());
            
            // 构建并返回登录用户视图对象
            return LoginUserVO.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .tenantId(user.getTenantId())
                    .roles(roles)
                    .permissions(permissions)
                    .accessToken(jwtTokenDTO.getAccessToken())
                    .refreshToken(jwtTokenDTO.getRefreshToken())
                    .tokenType(jwtTokenDTO.getTokenType())
                    .expiresIn(jwtTokenDTO.getExpiresIn())
                    .build();
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
    }

    @Override
    public boolean logout(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        
        try {
            // 使令牌失效
            jwtService.invalidateToken(token);
            return true;
        } catch (Exception e) {
            log.error("登出失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public JwtTokenDTO refreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new IllegalArgumentException("刷新令牌不能为空");
        }
        
        return jwtService.refreshToken(refreshToken);
    }

    @Override
    public Map<String, String> getCaptcha() {
        // 生成验证码
        String captchaCode = generateCaptchaCode(captchaLength);
        
        // 生成验证码图片
        String captchaImage = generateCaptchaImage(captchaCode);
        
        // 生成验证码Key
        String captchaKey = UUID.randomUUID().toString();
        
        // 将验证码存入Redis
        redisTemplate.opsForValue().set("captcha:" + captchaKey, captchaCode, captchaExpiration, TimeUnit.SECONDS);
        
        // 返回验证码信息
        Map<String, String> result = new HashMap<>();
        result.put("key", captchaKey);
        result.put("image", captchaImage);
        result.put("expireIn", String.valueOf(captchaExpiration));
        
        return result;
    }

    @Override
    public boolean validateCaptcha(String captchaKey, String captchaCode) {
        if (!StringUtils.hasText(captchaKey) || !StringUtils.hasText(captchaCode)) {
            return false;
        }
        
        // 从Redis获取验证码
        String key = "captcha:" + captchaKey;
        Object value = redisTemplate.opsForValue().get(key);
        
        // 验证
        boolean valid = value != null && captchaCode.equalsIgnoreCase(value.toString());
        
        // 无论验证成功与否，都删除验证码
        redisTemplate.delete(key);
        
        return valid;
    }

    /**
     * 生成随机验证码
     *
     * @param length 验证码长度
     * @return 验证码
     */
    private String generateCaptchaCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }

    /**
     * 生成验证码图片
     *
     * @param code 验证码
     * @return Base64编码的图片
     */
    private String generateCaptchaImage(String code) {
        try {
            BufferedImage image = new BufferedImage(captchaWidth, captchaHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            
            // 设置背景色
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, captchaWidth, captchaHeight);
            
            // 设置字体
            g.setFont(new Font("Arial", Font.BOLD, captchaHeight / 2));
            
            // 添加干扰线
            Random random = new Random();
            g.setColor(new Color(160, 160, 160));
            for (int i = 0; i < 10; i++) {
                int x1 = random.nextInt(captchaWidth);
                int y1 = random.nextInt(captchaHeight);
                int x2 = random.nextInt(captchaWidth);
                int y2 = random.nextInt(captchaHeight);
                g.drawLine(x1, y1, x2, y2);
            }
            
            // 添加噪点
            for (int i = 0; i < 50; i++) {
                int x = random.nextInt(captchaWidth);
                int y = random.nextInt(captchaHeight);
                image.setRGB(x, y, random.nextInt(256));
            }
            
            // 绘制验证码
            int x = (captchaWidth - code.length() * captchaHeight / 2) / 2;
            int y = captchaHeight * 3 / 4;
            
            for (int i = 0; i < code.length(); i++) {
                g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
                g.drawString(String.valueOf(code.charAt(i)), x + i * captchaHeight / 2, y);
            }
            
            g.dispose();
            
            // 转换为Base64
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (Exception e) {
            log.error("生成验证码图片失败: {}", e.getMessage());
            return null;
        }
    }
} 