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

import com.cxyxh.iam.authn.service.JwtService;
import com.cxyxh.iam.config.security.userdetails.SecurityUser;
import com.cxyxh.iam.config.security.userdetails.UserDetailsServiceImpl;
import com.cxyxh.iam.user.service.UserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * API测试配置类
 */
@TestConfiguration
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "jwt.secret=apiTestSecret123456789012345678901234567890",
    "jwt.access-token-expiration=300",
    "jwt.refresh-token-expiration=3600"
})
public class ApiTestConfig {
    
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password";
    
    @Bean
    @Primary
    public UserService userService() {
        UserService mockUserService = mock(UserService.class);
        
        // 配置权限和角色
        List<String> permissions = Arrays.asList(
            "user:create", "user:read", "user:update", "user:delete", 
            "user:list", "user:view", "role:assign"
        );
        
        List<String> roles = Arrays.asList("ADMIN", "USER");
        
        when(mockUserService.getUserPermissions(anyLong())).thenReturn(permissions);
        when(mockUserService.getUserRoles(anyLong())).thenReturn(roles);
        
        return mockUserService;
    }
    
    @Bean
    @Primary
    public JwtService jwtService() {
        JwtService mockJwtService = mock(JwtService.class);
        
        // 避免循环引用和栈溢出
        doReturn(true).when(mockJwtService).validateToken(anyString());
        doReturn(TEST_USERNAME).when(mockJwtService).getUsernameFromToken(anyString());
        doReturn(TEST_USER_ID).when(mockJwtService).getUserIdFromToken(anyString());
        
        return mockJwtService;
    }
    
    @Bean
    @Primary
    public UserDetailsServiceImpl userDetailsService() {
        UserDetailsServiceImpl mockService = mock(UserDetailsServiceImpl.class);
        
        // 创建权限列表
        List<String> authorities = Arrays.asList(
            "user:create", "user:read", "user:update", "user:delete", 
            "user:list", "user:view", "role:assign",
            "ROLE_ADMIN", "ROLE_USER"
        );
        
        // 创建测试安全用户
        SecurityUser testUser = new SecurityUser(
                TEST_USER_ID, 
                TEST_USERNAME,
                passwordEncoder().encode(TEST_PASSWORD),
                true,
                authorities
        );
        
        // 配置用户详情服务返回测试用户
        when(mockService.loadUserByUsername(anyString())).thenReturn(testUser);
        
        return mockService;
    }
    
    @Bean
    public MockMvc mockMvc(WebApplicationContext context) {
        return MockMvcBuilders.webAppContextSetup(context)
                .build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * 配置测试用户
     */
    public void setupTestUser() {
        // 已经在userDetailsService bean中配置
    }
} 