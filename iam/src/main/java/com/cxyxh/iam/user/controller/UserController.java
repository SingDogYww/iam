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
package com.cxyxh.iam.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cxyxh.iam.common.api.PageVO;
import com.cxyxh.iam.common.api.Result;
import com.cxyxh.iam.user.dto.UserDTO;
import com.cxyxh.iam.user.entity.User;
import com.cxyxh.iam.user.service.UserService;
import com.cxyxh.iam.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 *
 * @author IAM
 */
@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Boolean> register(@RequestBody @Validated UserDTO userDTO) {
        boolean result = userService.register(userDTO);
        return result ? Result.success(true) : Result.failed("注册失败，用户名可能已存在");
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<UserVO> getUserDetail(@Parameter(description = "用户ID") @PathVariable Long id) {
        UserVO userVO = userService.getUserDetail(id);
        return userVO != null ? Result.success(userVO) : Result.failed("用户不存在");
    }

    @Operation(summary = "更新用户信息")
    @PutMapping
    @PreAuthorize("hasAuthority('user:update')")
    public Result<Boolean> updateUser(@RequestBody @Validated UserDTO userDTO) {
        boolean result = userService.updateUser(userDTO);
        return result ? Result.success(true) : Result.failed("更新失败");
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public Result<Boolean> removeUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        boolean result = userService.removeUser(id);
        return result ? Result.success(true) : Result.failed("删除失败");
    }

    @Operation(summary = "修改用户状态")
    @PutMapping("/{id}/status/{status}")
    @PreAuthorize("hasAuthority('user:update')")
    public Result<Boolean> updateStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "状态：1-正常，0-禁用") @PathVariable Integer status) {
        boolean result = userService.updateStatus(id, status);
        return result ? Result.success(true) : Result.failed("修改状态失败");
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('user:update')")
    public Result<Boolean> resetPassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "新密码") @RequestParam String password) {
        boolean result = userService.resetPassword(id, password);
        return result ? Result.success(true) : Result.failed("重置密码失败");
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<PageVO<UserVO>> pageUser(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<UserVO> page = userService.pageUser(current, size, username, status);
        return Result.success(new PageVO<>(page));
    }

    @Operation(summary = "分配角色")
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('user:assign')")
    public Result<Boolean> assignRoles(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "角色ID列表") @RequestBody List<Long> roleIds) {
        boolean result = userService.assignRoles(id, roleIds);
        return result ? Result.success(true) : Result.failed("分配角色失败");
    }

    @Operation(summary = "获取用户角色列表")
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<List<String>> getUserRoles(@Parameter(description = "用户ID") @PathVariable Long id) {
        List<String> roles = userService.getUserRoles(id);
        return Result.success(roles);
    }

    @Operation(summary = "获取用户权限列表")
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<List<String>> getUserPermissions(@Parameter(description = "用户ID") @PathVariable Long id) {
        List<String> permissions = userService.getUserPermissions(id);
        return Result.success(permissions);
    }
} 