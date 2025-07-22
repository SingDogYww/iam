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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cxyxh.iam.user.convert.UserConvert;
import com.cxyxh.iam.user.dto.UserDTO;
import com.cxyxh.iam.user.entity.User;
import com.cxyxh.iam.user.mapper.UserMapper;
import com.cxyxh.iam.user.service.UserService;
import com.cxyxh.iam.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author IAM
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserConvert userConvert;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(UserDTO userDTO) {
        // 检查用户名是否已存在
        if (this.getUserByUsername(userDTO.getUsername()) != null) {
            log.warn("用户名已存在: {}", userDTO.getUsername());
            return false;
        }

        // 转换DTO为实体
        User user = userConvert.dtoToEntity(userDTO);
        
        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getNickname() == null) {
            user.setNickname(user.getUsername());
        }
        
        // 加密密码
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // 保存用户
        boolean saved = this.save(user);
        
        // 如果有角色ID列表，分配角色
        if (saved && userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            this.assignRoles(user.getId(), userDTO.getRoleIds());
        }
        
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserDTO userDTO) {
        if (userDTO.getId() == null) {
            log.error("更新用户时ID不能为空");
            return false;
        }
        
        // 检查用户是否存在
        User existingUser = this.getById(userDTO.getId());
        if (existingUser == null) {
            log.error("用户不存在，ID: {}", userDTO.getId());
            return false;
        }
        
        // 如果修改了用户名，检查新用户名是否已存在
        if (StringUtils.hasText(userDTO.getUsername()) && !userDTO.getUsername().equals(existingUser.getUsername())) {
            if (this.getUserByUsername(userDTO.getUsername()) != null) {
                log.warn("用户名已存在: {}", userDTO.getUsername());
                return false;
            }
        }
        
        // 转换DTO为实体
        User user = userConvert.dtoToEntity(userDTO);
        
        // 如果密码不为空，加密密码
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // 不更新密码
            user.setPassword(null);
        }
        
        // 更新用户
        boolean updated = this.updateById(user);
        
        // 如果有角色ID列表，更新角色
        if (updated && userDTO.getRoleIds() != null) {
            this.assignRoles(user.getId(), userDTO.getRoleIds());
        }
        
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeUser(Long userId) {
        if (userId == null) {
            return false;
        }
        
        // 删除用户角色关联
        // 这里假设有一个删除用户角色关联的方法，实际实现可能不同
        // roleService.removeUserRoles(userId);
        
        // 删除用户
        return this.removeById(userId);
    }

    @Override
    public boolean updateStatus(Long userId, Integer status) {
        if (userId == null || status == null) {
            return false;
        }
        
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        
        return this.updateById(user);
    }

    @Override
    public boolean resetPassword(Long userId, String password) {
        if (userId == null || !StringUtils.hasText(password)) {
            return false;
        }
        
        User user = new User();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(password));
        
        return this.updateById(user);
    }

    @Override
    public IPage<UserVO> pageUser(Page<User> page, String username, Integer status) {
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(username), User::getUsername, username)
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getCreateTime);
        
        // 分页查询
        IPage<User> userPage = this.page(page, queryWrapper);
        
        // 转换为VO
        IPage<UserVO> userVOPage = userPage.convert(user -> {
            UserVO userVO = userConvert.entityToVO(user);
            // 可以在这里设置其他属性，如租户名称等
            return userVO;
        });
        
        return userVOPage;
    }
    
    @Override
    public Page<UserVO> pageUser(Integer current, Integer size, String username, Integer status) {
        Page<User> page = new Page<>(current, size);
        return (Page<UserVO>) pageUser(page, username, status);
    }

    @Override
    public UserVO getUserDetail(Long userId) {
        if (userId == null) {
            return null;
        }
        
        User user = this.getById(userId);
        if (user == null) {
            return null;
        }
        
        // 转换为VO
        UserVO userVO = userConvert.entityToVO(user);
        
        // 设置角色和权限
        userVO.setRoles(this.getUserRoles(userId));
        userVO.setPermissions(this.getUserPermissions(userId));
        
        // 可以在这里设置租户名称等
        // userVO.setTenantName(tenantService.getTenantName(user.getTenantId()));
        
        return userVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(Long userId, List<Long> roleIds) {
        if (userId == null) {
            return false;
        }
        
        // 这里应该实现分配角色的逻辑
        // 通常是先删除用户的所有角色，然后重新分配
        // 实际实现可能依赖于其他服务或mapper
        
        // 示例：
        // 1. 删除用户的所有角色
        // userRoleMapper.deleteByUserId(userId);
        
        // 2. 添加新的角色
        // if (roleIds != null && !roleIds.isEmpty()) {
        //     List<UserRole> userRoles = roleIds.stream()
        //             .map(roleId -> new UserRole(userId, roleId))
        //             .collect(Collectors.toList());
        //     userRoleMapper.batchInsert(userRoles);
        // }
        
        return true;
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return baseMapper.selectUserRoles(userId);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        return baseMapper.selectUserPermissions(userId);
    }
} 