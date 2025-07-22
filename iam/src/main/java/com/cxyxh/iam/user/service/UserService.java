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
package com.cxyxh.iam.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cxyxh.iam.user.dto.UserDTO;
import com.cxyxh.iam.user.entity.User;
import com.cxyxh.iam.user.vo.UserVO;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author IAM
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 注册用户
     *
     * @param userDTO 用户DTO
     * @return 是否成功
     */
    boolean register(UserDTO userDTO);

    /**
     * 更新用户信息
     *
     * @param userDTO 用户DTO
     * @return 是否成功
     */
    boolean updateUser(UserDTO userDTO);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean removeUser(Long userId);

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long userId, Integer status);

    /**
     * 重置密码
     *
     * @param userId   用户ID
     * @param password 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long userId, String password);

    /**
     * 分页查询用户
     *
     * @param page     分页参数
     * @param username 用户名
     * @param status   状态
     * @return 分页结果
     */
    IPage<UserVO> pageUser(Page<User> page, String username, Integer status);
    
    /**
     * 分页查询用户
     *
     * @param current  当前页
     * @param size     每页大小
     * @param username 用户名
     * @param status   状态
     * @return 分页结果
     */
    Page<UserVO> pageUser(Integer current, Integer size, String username, Integer status);

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 用户VO
     */
    UserVO getUserDetail(Long userId);

    /**
     * 分配角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @return 是否成功
     */
    boolean assignRoles(Long userId, List<Long> roleIds);

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    List<String> getUserPermissions(Long userId);
} 