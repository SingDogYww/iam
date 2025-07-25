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
package com.cxyxh.iam.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cxyxh.iam.user.controller.UserController;
import com.cxyxh.iam.user.dto.UserDTO;
import com.cxyxh.iam.user.entity.User;
import com.cxyxh.iam.user.service.UserService;
import com.cxyxh.iam.user.vo.UserVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户API测试
 */
@ExtendWith(MockitoExtension.class)
public class UserApiTest {

    @Mock
    private UserService userService;
    
    @InjectMocks
    private UserController userController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    private final Long TEST_USER_ID = 1L;
    private final String TEST_USERNAME = "testuser";
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .build();
    }
    
    @Test
    void register_WithValidData_ShouldReturnSuccess() throws Exception {
        // 准备数据
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("newuser");
        userDTO.setPassword("password");
        userDTO.setEmail("newuser@example.com");
        userDTO.setNickname("New User");
        
        when(userService.register(any(UserDTO.class))).thenReturn(true);
        
        // 执行请求
        mockMvc.perform(post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(true));
    }
    
    @Test
    void getUserDetail_WithValidId_ShouldReturnUserDetail() throws Exception {
        // 准备数据
        UserVO userVO = new UserVO();
        userVO.setId(TEST_USER_ID);
        userVO.setUsername(TEST_USERNAME);
        userVO.setEmail("test@example.com");
        userVO.setNickname("Test User");
        userVO.setStatus(1);
        userVO.setRoles(Arrays.asList("ADMIN", "USER"));
        userVO.setPermissions(Arrays.asList("user:create", "user:read"));
        
        when(userService.getUserDetail(TEST_USER_ID)).thenReturn(userVO);
        
        // 执行请求
        mockMvc.perform(get("/user/" + TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.username").exists())
            .andExpect(jsonPath("$.data.roles").isArray())
            .andExpect(jsonPath("$.data.permissions").isArray());
    }
    
    @Test
    void updateUser_WithValidData_ShouldReturnSuccess() throws Exception {
        // 准备数据
        UserDTO userDTO = new UserDTO();
        userDTO.setId(TEST_USER_ID);
        userDTO.setUsername(TEST_USERNAME);
        userDTO.setEmail("updated@example.com");
        userDTO.setNickname("Updated User");
        
        when(userService.updateUser(any(UserDTO.class))).thenReturn(true);
        
        // 执行请求
        mockMvc.perform(put("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(true));
    }
    
    @Test
    void removeUser_WithValidId_ShouldReturnSuccess() throws Exception {
        // 准备数据
        when(userService.removeUser(TEST_USER_ID)).thenReturn(true);
        
        // 执行请求
        mockMvc.perform(delete("/user/" + TEST_USER_ID)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(true));
    }
    
    @Test
    void updateStatus_WithValidIdAndStatus_ShouldReturnSuccess() throws Exception {
        // 准备数据
        when(userService.updateStatus(eq(TEST_USER_ID), anyInt())).thenReturn(true);
        
        // 执行请求
        mockMvc.perform(put("/user/" + TEST_USER_ID + "/status/0")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(true));
    }
    
    @Test
    void resetPassword_WithValidIdAndPassword_ShouldReturnSuccess() throws Exception {
        // 准备数据
        when(userService.resetPassword(eq(TEST_USER_ID), anyString())).thenReturn(true);
        
        // 执行请求
        mockMvc.perform(put("/user/" + TEST_USER_ID + "/password")
                .param("password", "newpassword")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(true));
    }
    
    @Test
    void pageUser_WithValidParameters_ShouldReturnUserList() throws Exception {
        // 准备数据
        List<UserVO> userVOList = new ArrayList<>();
        UserVO userVO = new UserVO();
        userVO.setId(TEST_USER_ID);
        userVO.setUsername(TEST_USERNAME);
        userVO.setEmail("test@example.com");
        userVO.setNickname("Test User");
        userVO.setStatus(1);
        userVOList.add(userVO);
        
        Page<UserVO> page = new Page<>();
        page.setRecords(userVOList);
        page.setCurrent(1);
        page.setSize(10);
        page.setTotal(1);
        
        when(userService.pageUser(1, 10, null, null)).thenReturn(page);
        
        // 执行请求
        mockMvc.perform(get("/user/page")
                .param("current", "1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records").isArray())
            .andExpect(jsonPath("$.data.total").exists());
    }
    
    @Test
    void assignRoles_WithValidIdAndRoleIds_ShouldReturnSuccess() throws Exception {
        // 准备数据
        List<Long> roleIds = Arrays.asList(1L, 2L);
        
        when(userService.assignRoles(eq(TEST_USER_ID), anyList())).thenReturn(true);
        
        // 执行请求
        mockMvc.perform(post("/user/" + TEST_USER_ID + "/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleIds)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(true));
    }
    
    @Test
    void getUserRoles_WithValidId_ShouldReturnRolesList() throws Exception {
        // 准备数据
        List<String> roles = Arrays.asList("ADMIN", "USER");
        
        when(userService.getUserRoles(TEST_USER_ID)).thenReturn(roles);
        
        // 执行请求
        mockMvc.perform(get("/user/" + TEST_USER_ID + "/roles")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray());
    }
    
    @Test
    void getUserPermissions_WithValidId_ShouldReturnPermissionsList() throws Exception {
        // 准备数据
        List<String> permissions = Arrays.asList("user:create", "user:read", "user:update", "user:delete");
        
        when(userService.getUserPermissions(TEST_USER_ID)).thenReturn(permissions);
        
        // 执行请求
        mockMvc.perform(get("/user/" + TEST_USER_ID + "/permissions")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").isArray());
    }
} 