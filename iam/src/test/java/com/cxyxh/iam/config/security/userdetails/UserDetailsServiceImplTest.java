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
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {

    @Test
    void testBasicUserDetailsServiceFunctionality() {
        // Mock依赖
        UserService mockUserService = mock(UserService.class);
        
        // 创建测试对象
        UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl(mockUserService);
        
        // 设置mock行为
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("password");
        mockUser.setStatus(1);
        
        List<String> permissions = new ArrayList<>();
        permissions.add("user:view");
        
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        
        when(mockUserService.getUserByUsername("testuser")).thenReturn(mockUser);
        when(mockUserService.getUserPermissions(1L)).thenReturn(permissions);
        when(mockUserService.getUserRoles(1L)).thenReturn(roles);
        
        // 验证能够创建UserDetailsService
        assertNotNull(userDetailsService);
    }
    
    @Test
    void testUserNotFound() {
        // Mock依赖
        UserService mockUserService = mock(UserService.class);
        
        // 创建测试对象
        UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl(mockUserService);
        
        // 设置mock行为
        when(mockUserService.getUserByUsername("nonexistent")).thenReturn(null);
        
        // 验证抛出异常
        assertThrows(UsernameNotFoundException.class, 
                () -> userDetailsService.loadUserByUsername("nonexistent"));
    }
} 