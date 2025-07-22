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
package com.cxyxh.iam.authn.controller;

import com.cxyxh.iam.authn.dto.JwtTokenDTO;
import com.cxyxh.iam.authn.dto.LoginDTO;
import com.cxyxh.iam.authn.dto.LoginUserVO;
import com.cxyxh.iam.authn.service.AuthService;
import com.cxyxh.iam.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 *
 * @author IAM
 */
@Tag(name = "认证管理", description = "认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AuthController {

    private final AuthService authService;

    @Value("${security.jwt.header}")
    private String tokenHeader;

    @Value("${security.jwt.prefix}")
    private String tokenPrefix;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginUserVO> login(@RequestBody @Validated LoginDTO loginDTO) {
        try {
            LoginUserVO loginUserVO = authService.login(loginDTO);
            return Result.success(loginUserVO, "登录成功");
        } catch (IllegalArgumentException e) {
            return Result.failed(e.getMessage());
        }
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Boolean> logout(@RequestHeader(name = "${security.jwt.header}") String token) {
        if (token.startsWith(tokenPrefix)) {
            token = token.substring(tokenPrefix.length()).trim();
        }
        boolean result = authService.logout(token);
        return result ? Result.success(true, "登出成功") : Result.failed("登出失败");
    }

    @Operation(summary = "刷新令牌")
    @PostMapping("/refresh")
    public Result<JwtTokenDTO> refreshToken(@Parameter(description = "刷新令牌") @RequestParam String refreshToken) {
        try {
            JwtTokenDTO jwtTokenDTO = authService.refreshToken(refreshToken);
            return Result.success(jwtTokenDTO, "刷新令牌成功");
        } catch (IllegalArgumentException e) {
            return Result.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        Map<String, String> captcha = authService.getCaptcha();
        return Result.success(captcha);
    }
} 