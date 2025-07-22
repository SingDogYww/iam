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
package com.cxyxh.iam.config.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Redis 键过期事件监听器
 *
 * @author IAM
 */
@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    /**
     * 构造方法
     *
     * @param listenerContainer Redis消息监听容器
     */
    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 处理过期事件
     *
     * @param message 消息
     * @param pattern 模式
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.debug("Redis key expired: {}", expiredKey);
        
        // 根据不同的键前缀处理不同的过期事件
        if (expiredKey.startsWith("jwt:blacklist:")) {
            // JWT黑名单过期事件
            handleJwtBlacklistExpired(expiredKey);
        } else if (expiredKey.startsWith("captcha:")) {
            // 验证码过期事件
            handleCaptchaExpired(expiredKey);
        } else if (expiredKey.startsWith("user:online:")) {
            // 在线用户过期事件
            handleUserOnlineExpired(expiredKey);
        } else if (expiredKey.startsWith("role:permission:")) {
            // 角色权限缓存过期事件
            handleRolePermissionExpired(expiredKey);
        }
    }

    /**
     * 处理JWT黑名单过期事件
     *
     * @param key 过期的键
     */
    private void handleJwtBlacklistExpired(String key) {
        // 可以在这里添加处理逻辑，比如记录日志等
        log.debug("JWT blacklist expired: {}", key);
    }

    /**
     * 处理验证码过期事件
     *
     * @param key 过期的键
     */
    private void handleCaptchaExpired(String key) {
        // 可以在这里添加处理逻辑，比如记录日志等
        log.debug("Captcha expired: {}", key);
    }

    /**
     * 处理在线用户过期事件
     *
     * @param key 过期的键
     */
    private void handleUserOnlineExpired(String key) {
        // 可以在这里添加处理逻辑，比如更新用户状态等
        String userId = key.substring("user:online:".length());
        log.debug("User offline: {}", userId);
    }

    /**
     * 处理角色权限缓存过期事件
     *
     * @param key 过期的键
     */
    private void handleRolePermissionExpired(String key) {
        // 可以在这里添加处理逻辑，比如重新加载权限等
        String roleId = key.substring("role:permission:".length());
        log.debug("Role permission cache expired: {}", roleId);
    }
} 