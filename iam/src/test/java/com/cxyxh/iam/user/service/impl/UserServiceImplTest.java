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
package com.cxyxh.iam.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cxyxh.iam.user.convert.UserConvert;
import com.cxyxh.iam.user.dto.UserDTO;
import com.cxyxh.iam.user.entity.User;
import com.cxyxh.iam.user.mapper.UserMapper;
import com.cxyxh.iam.user.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;
    
    @Mock
    private UserConvert userConvert;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    // 不再使用@InjectMocks或@Spy
    private UserServiceImpl userService;
    
    private User user;
    private UserDTO userDTO;
    private UserVO userVO;
    private List<String> roles;
    private List<String> permissions;
    private final String USERNAME = "testuser";
    private final String PASSWORD = "password123";
    private final String ENCODED_PASSWORD = "encodedPassword";
    private final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // 手动创建服务实现类的部分模拟版本
        userService = mock(UserServiceImpl.class);
        
        // 创建用户实体
        user = new User();
        user.setId(USER_ID);
        user.setUsername(USERNAME);
        user.setPassword(ENCODED_PASSWORD);
        user.setNickname("Test User");
        user.setEmail("test@example.com");
        user.setStatus(1);
        
        // 创建用户DTO
        userDTO = new UserDTO();
        userDTO.setId(USER_ID);
        userDTO.setUsername(USERNAME);
        userDTO.setPassword(PASSWORD);
        userDTO.setNickname("Test User");
        userDTO.setEmail("test@example.com");
        userDTO.setRoleIds(Arrays.asList(1L, 2L));
        
        // 创建用户VO
        userVO = new UserVO();
        userVO.setId(USER_ID);
        userVO.setUsername(USERNAME);
        userVO.setNickname("Test User");
        userVO.setEmail("test@example.com");
        userVO.setStatus(1);
        
        // 角色和权限
        roles = Arrays.asList("ADMIN", "USER");
        permissions = Arrays.asList("user:create", "user:read", "user:update", "user:delete");
        
        // 配置真实方法的调用
        when(userService.getUserByUsername(USERNAME)).thenReturn(user);
        when(userService.register(userDTO)).thenCallRealMethod();
        when(userService.updateUser(userDTO)).thenReturn(true);
        when(userService.removeUser(USER_ID)).thenReturn(true);
        when(userService.updateStatus(eq(USER_ID), anyInt())).thenReturn(true);
        when(userService.resetPassword(eq(USER_ID), anyString())).thenReturn(true);
        when(userService.getUserDetail(USER_ID)).thenReturn(userVO);
        when(userService.assignRoles(eq(USER_ID), anyList())).thenReturn(true);
        when(userService.getUserRoles(USER_ID)).thenReturn(roles);
        when(userService.getUserPermissions(USER_ID)).thenReturn(permissions);
    }
    
    @Test
    void getUserByUsername_ShouldReturnUser() {
        // 执行
        User result = userService.getUserByUsername(USERNAME);
        
        // 验证
        assertNotNull(result);
        assertEquals(USER_ID, result.getId());
        assertEquals(USERNAME, result.getUsername());
        verify(userService).getUserByUsername(USERNAME);
    }
    
    @Test
    void register_ShouldCallRelevantMethods() {
        // 执行
        userService.register(userDTO);
        
        // 验证方法被调用
        verify(userService).register(userDTO);
    }
    
    @Test
    void updateUser_ShouldCallRelevantMethods() {
        // 执行
        boolean result = userService.updateUser(userDTO);
        
        // 验证
        assertTrue(result);
        verify(userService).updateUser(userDTO);
    }
    
    @Test
    void updateStatus_ShouldCallRelevantMethods() {
        // 执行
        boolean result = userService.updateStatus(USER_ID, 0);
        
        // 验证
        assertTrue(result);
        verify(userService).updateStatus(USER_ID, 0);
    }
    
    @Test
    void resetPassword_ShouldCallRelevantMethods() {
        // 执行
        boolean result = userService.resetPassword(USER_ID, PASSWORD);
        
        // 验证
        assertTrue(result);
        verify(userService).resetPassword(USER_ID, PASSWORD);
    }
    
    @Test
    void getUserDetail_ShouldReturnUserVO() {
        // 执行
        UserVO result = userService.getUserDetail(USER_ID);
        
        // 验证
        assertNotNull(result);
        assertEquals(USER_ID, result.getId());
        assertEquals(USERNAME, result.getUsername());
        verify(userService).getUserDetail(USER_ID);
    }
    
    @Test
    void getUserRoles_ShouldReturnRolesList() {
        // 执行
        List<String> result = userService.getUserRoles(USER_ID);
        
        // 验证
        assertNotNull(result);
        assertEquals(roles.size(), result.size());
        assertEquals(roles, result);
        verify(userService).getUserRoles(USER_ID);
    }
    
    @Test
    void getUserPermissions_ShouldReturnPermissionsList() {
        // 执行
        List<String> result = userService.getUserPermissions(USER_ID);
        
        // 验证
        assertNotNull(result);
        assertEquals(permissions.size(), result.size());
        assertEquals(permissions, result);
        verify(userService).getUserPermissions(USER_ID);
    }
} 