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
package com.cxyxh.iam.config.security.userdetails;

import com.cxyxh.iam.user.entity.User;
import com.cxyxh.iam.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户详情服务实现类
 *
 * @author IAM
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;
    
    // 定义用户状态常量
    private static final int STATUS_ENABLED = 1;
    private static final int STATUS_DISABLED = 0;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        User user = userService.getUserByUsername(username);
        if (user == null) {
            log.error("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在");
        }
        
        // 如果用户被禁用，抛出异常
        if (user.getStatus() == STATUS_DISABLED) {
            log.error("用户已禁用: {}", username);
            throw new UsernameNotFoundException("用户已禁用");
        }
        
        // 查询用户角色和权限
        List<String> authorities = userService.getUserPermissions(user.getId());
        
        // 添加角色权限（ROLE_ 前缀）
        List<String> roles = userService.getUserRoles(user.getId());
        roles.forEach(role -> authorities.add("ROLE_" + role));
        
        // 创建安全用户
        return new SecurityUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == STATUS_ENABLED,
                authorities
        );
    }
} 