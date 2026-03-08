package com.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.common.result.PageResult;
import com.parking.dto.request.PasswordUpdateRequest;
import com.parking.dto.request.UserUpdateRequest;
import com.parking.dto.response.UserResponse;
import com.parking.entity.User;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询用户列表
     *
     * @param page    页码
     * @param size    每页大小
     * @param keyword 搜索关键词（用户名/昵称/手机号）
     * @param role    角色筛选
     * @return 分页结果
     */
    PageResult<UserResponse> getUserPage(int page, int size, String keyword, String role);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    UserResponse getUserById(Long id);

    /**
     * 更新用户信息
     *
     * @param id      用户ID
     * @param request 更新请求
     * @return 用户信息
     */
    UserResponse updateUser(Long id, UserUpdateRequest request);

    /**
     * 修改密码
     *
     * @param userId  用户ID
     * @param request 密码修改请求
     */
    void updatePassword(Long userId, PasswordUpdateRequest request);

    /**
     * 更新用户状态（启用/禁用）
     *
     * @param id     用户ID
     * @param status 状态值
     */
    void updateStatus(Long id, Integer status);

    /**
     * 删除用户（软删除）
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 实体转换为响应DTO
     *
     * @param user 用户实体
     * @return 用户响应
     */
    UserResponse convertToResponse(User user);
}
