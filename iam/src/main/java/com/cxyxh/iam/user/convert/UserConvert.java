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
package com.cxyxh.iam.user.convert;

import com.cxyxh.iam.user.dto.UserDTO;
import com.cxyxh.iam.user.entity.User;
import com.cxyxh.iam.user.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户对象转换接口
 *
 * @author IAM
 */
@Mapper(componentModel = "spring")
public interface UserConvert {

    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    /**
     * UserDTO 转 User
     *
     * @param userDTO 用户DTO
     * @return 用户实体
     */
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    User dtoToEntity(UserDTO userDTO);

    /**
     * User 转 UserVO
     *
     * @param user 用户实体
     * @return 用户VO
     */
    @Mapping(target = "statusName", source = "status", qualifiedByName = "statusToName")
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "tenantName", ignore = true)
    UserVO entityToVO(User user);

    /**
     * User 列表转 UserVO 列表
     *
     * @param userList 用户实体列表
     * @return 用户VO列表
     */
    List<UserVO> entityListToVOList(List<User> userList);

    /**
     * 状态转换为状态名称
     *
     * @param status 状态值
     * @return 状态名称
     */
    @Named("statusToName")
    default String statusToName(Integer status) {
        return status != null && status == 1 ? "正常" : "禁用";
    }
} 