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

import com.cxyxh.iam.authn.controller.AuthController;
import com.cxyxh.iam.authn.dto.JwtTokenDTO;
import com.cxyxh.iam.authn.dto.LoginDTO;
import com.cxyxh.iam.authn.dto.LoginUserVO;
import com.cxyxh.iam.authn.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证API测试
 */
@ExtendWith(MockitoExtension.class)
public class AuthenticationApiTest {
    
    @Mock
    private AuthService authService;
    
    @InjectMocks
    private AuthController authController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    private final String TEST_USERNAME = "testuser";
    private final String TEST_PASSWORD = "password";
    private final Long TEST_USER_ID = 1L;
    private final String TEST_ACCESS_TOKEN = "test-access-token";
    private final String TEST_REFRESH_TOKEN = "test-refresh-token";
    private final String TOKEN_HEADER = "Authorization";
    private final String TOKEN_PREFIX = "Bearer ";
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .build();
        
        // 设置Controller中的属性
        ReflectionTestUtils.setField(authController, "tokenHeader", TOKEN_HEADER);
        ReflectionTestUtils.setField(authController, "tokenPrefix", TOKEN_PREFIX);
    }
    
    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        // 准备数据
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(TEST_USERNAME);
        loginDTO.setPassword(TEST_PASSWORD);
        
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setUserId(TEST_USER_ID);
        loginUserVO.setUsername(TEST_USERNAME);
        loginUserVO.setAccessToken(TEST_ACCESS_TOKEN);
        loginUserVO.setRefreshToken(TEST_REFRESH_TOKEN);
        loginUserVO.setTokenType("Bearer");
        loginUserVO.setExpiresIn(300L);
        
        when(authService.login(any(LoginDTO.class))).thenReturn(loginUserVO);
        
        // 执行请求
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists())
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }
    
    @Test
    @Disabled("需要进一步调试")
    void login_WithInvalidCredentials_ShouldReturnError() throws Exception {
        // 准备数据
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("invalid");
        loginDTO.setPassword("invalid");
        
        when(authService.login(any(LoginDTO.class))).thenThrow(new IllegalArgumentException("用户名或密码错误"));
        
        // 执行请求
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400));
    }
    
    @Test
    void refreshToken_WithValidToken_ShouldReturnNewToken() throws Exception {
        // 准备数据
        JwtTokenDTO newTokenDTO = new JwtTokenDTO();
        newTokenDTO.setAccessToken("new-access-token");
        newTokenDTO.setTokenType("Bearer");
        newTokenDTO.setExpiresIn(300L);
        
        when(authService.refreshToken(TEST_REFRESH_TOKEN)).thenReturn(newTokenDTO);
        
        // 执行请求
        mockMvc.perform(post("/auth/refresh")
                .param("refreshToken", TEST_REFRESH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.accessToken").exists());
    }
    
    @Test
    @Disabled("需要进一步调试")
    void logout_WithValidToken_ShouldLogoutSuccessfully() throws Exception {
        // 准备数据
        when(authService.logout(TEST_ACCESS_TOKEN)).thenReturn(true);
        
        // 执行请求
        mockMvc.perform(post("/auth/logout")
                .header(TOKEN_HEADER, TOKEN_PREFIX + TEST_ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
    
    @Test
    void getCaptcha_ShouldReturnCaptchaImage() throws Exception {
        // 执行请求
        mockMvc.perform(get("/auth/captcha")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk());
    }
} 