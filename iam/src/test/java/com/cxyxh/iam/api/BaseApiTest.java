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

import com.cxyxh.iam.authn.dto.JwtTokenDTO;
import com.cxyxh.iam.authn.service.JwtService;
import com.cxyxh.iam.config.security.userdetails.SecurityUser;
import com.cxyxh.iam.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * API测试基类
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ApiTestConfig.class)
@ActiveProfiles("test")
public abstract class BaseApiTest {
    
    @Autowired
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    @Autowired
    protected UserService userService;
    
    @Autowired
    protected JwtService jwtService;
    
    @Autowired
    protected ApiTestConfig apiTestConfig;
    
    protected final String TEST_USERNAME = "testuser";
    protected final String TEST_PASSWORD = "password";
    protected final Long TEST_USER_ID = 1L;
    protected final String TEST_ACCESS_TOKEN = "test-access-token";
    protected final String TEST_REFRESH_TOKEN = "test-refresh-token";
    
    @BeforeEach
    void setUp() {
        // 配置测试用户
        apiTestConfig.setupTestUser();
        
        // 配置JWT服务的行为
        JwtTokenDTO tokenDTO = new JwtTokenDTO();
        tokenDTO.setAccessToken(TEST_ACCESS_TOKEN);
        tokenDTO.setRefreshToken(TEST_REFRESH_TOKEN);
        tokenDTO.setTokenType("Bearer");
        tokenDTO.setExpiresIn(300L);
        
        when(jwtService.generateToken(anyString(), anyLong())).thenReturn(tokenDTO);
        when(jwtService.validateToken(anyString())).thenReturn(true);
        when(jwtService.getUsernameFromToken(anyString())).thenReturn(TEST_USERNAME);
        when(jwtService.getUserIdFromToken(anyString())).thenReturn(TEST_USER_ID);
    }
    
    /**
     * 设置安全上下文，模拟用户已经登录
     */
    protected void setAuthenticationContext() {
        // 添加用户需要的角色和权限
        List<String> authorities = Arrays.asList(
            "user:create", "user:read", "user:update", "user:delete", 
            "user:list", "user:view", "role:assign",
            "ROLE_ADMIN", "ROLE_USER"
        );
        
        Collection<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        
        SecurityUser securityUser = new SecurityUser(TEST_USER_ID, TEST_USERNAME, TEST_PASSWORD, true, authorities);
        
        Authentication auth = new UsernamePasswordAuthenticationToken(
                securityUser, null, grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
    
    /**
     * 清除安全上下文
     */
    protected void clearAuthenticationContext() {
        SecurityContextHolder.clearContext();
    }
    
    /**
     * 执行GET请求
     * 
     * @param url URL
     * @param authenticated 是否需要认证
     * @return ResultActions
     * @throws Exception 异常
     */
    protected ResultActions performGet(String url, boolean authenticated) throws Exception {
        if (authenticated) {
            setAuthenticationContext();
        }
        
        MockHttpServletRequestBuilder request = get(url)
                .contentType(MediaType.APPLICATION_JSON);
                
        if (authenticated) {
            request.header("Authorization", "Bearer " + TEST_ACCESS_TOKEN);
        }
        
        return mockMvc.perform(request);
    }
    
    /**
     * 执行POST请求
     * 
     * @param url URL
     * @param content 请求体
     * @param authenticated 是否需要认证
     * @return ResultActions
     * @throws Exception 异常
     */
    protected ResultActions performPost(String url, Object content, boolean authenticated) throws Exception {
        if (authenticated) {
            setAuthenticationContext();
        }
        
        MockHttpServletRequestBuilder request = post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content != null ? objectMapper.writeValueAsString(content) : "");
                
        if (authenticated) {
            request.header("Authorization", "Bearer " + TEST_ACCESS_TOKEN);
        }
        
        return mockMvc.perform(request);
    }
    
    /**
     * 执行PUT请求
     * 
     * @param url URL
     * @param content 请求体
     * @param authenticated 是否需要认证
     * @return ResultActions
     * @throws Exception 异常
     */
    protected ResultActions performPut(String url, Object content, boolean authenticated) throws Exception {
        if (authenticated) {
            setAuthenticationContext();
        }
        
        MockHttpServletRequestBuilder request = put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content != null ? objectMapper.writeValueAsString(content) : "");
                
        if (authenticated) {
            request.header("Authorization", "Bearer " + TEST_ACCESS_TOKEN);
        }
        
        return mockMvc.perform(request);
    }
    
    /**
     * 执行DELETE请求
     * 
     * @param url URL
     * @param authenticated 是否需要认证
     * @return ResultActions
     * @throws Exception 异常
     */
    protected ResultActions performDelete(String url, boolean authenticated) throws Exception {
        if (authenticated) {
            setAuthenticationContext();
        }
        
        MockHttpServletRequestBuilder request = delete(url)
                .contentType(MediaType.APPLICATION_JSON);
                
        if (authenticated) {
            request.header("Authorization", "Bearer " + TEST_ACCESS_TOKEN);
        }
        
        return mockMvc.perform(request);
    }
} 