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
package com.cxyxh.iam.config.security.filter;

import com.cxyxh.iam.authn.service.JwtService;
import com.cxyxh.iam.config.security.userdetails.SecurityUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    
    @Mock
    private UserDetailsService userDetailsService;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    private final String TOKEN = "valid-token";
    private final String USERNAME = "testuser";
    private final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        // 清除安全上下文
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        // 设置Mock行为
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(jwtService.validateToken(TOKEN)).thenReturn(true);
        when(jwtService.getUsernameFromToken(TOKEN)).thenReturn(USERNAME);
        
        // 创建模拟用户
        SecurityUser securityUser = new SecurityUser(
                USER_ID,
                USERNAME,
                "password",
                true,
                Collections.singletonList("ROLE_USER")
        );
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(securityUser);
        
        // 执行过滤器
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // 验证过滤链被调用
        verify(filterChain).doFilter(request, response);
        
        // 验证安全上下文中有认证信息
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(USERNAME, SecurityContextHolder.getContext().getAuthentication().getName());
    }
    
    @Test
    void doFilterInternal_WithInvalidToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // 设置Mock行为
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtService.validateToken("invalid-token")).thenReturn(false);
        
        // 执行过滤器
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // 验证过滤链被调用
        verify(filterChain).doFilter(request, response);
        
        // 验证安全上下文中没有认证信息
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        
        // 验证userDetailsService没有被调用
        verifyNoInteractions(userDetailsService);
    }
    
    @Test
    void doFilterInternal_WithNoToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // 设置Mock行为
        when(request.getHeader("Authorization")).thenReturn(null);
        
        // 执行过滤器
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // 验证过滤链被调用
        verify(filterChain).doFilter(request, response);
        
        // 验证安全上下文中没有认证信息
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        
        // 验证jwtService和userDetailsService没有被调用
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }
    
    @Test
    void doFilterInternal_WithInvalidAuthHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        // 设置Mock行为
        when(request.getHeader("Authorization")).thenReturn("Invalid-Format");
        
        // 执行过滤器
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // 验证过滤链被调用
        verify(filterChain).doFilter(request, response);
        
        // 验证安全上下文中没有认证信息
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        
        // 验证jwtService和userDetailsService没有被调用
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }
    
    @Test
    void doFilterInternal_WithException_ShouldContinueChain() throws ServletException, IOException {
        // 设置Mock行为
        when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);
        when(jwtService.validateToken(TOKEN)).thenThrow(new RuntimeException("Test exception"));
        
        // 执行过滤器
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        
        // 验证过滤链被调用
        verify(filterChain).doFilter(request, response);
        
        // 验证安全上下文中没有认证信息
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
} 