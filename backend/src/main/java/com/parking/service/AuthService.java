package com.parking.service;

import com.parking.dto.request.LoginRequest;
import com.parking.dto.request.RegisterRequest;
import com.parking.dto.response.LoginResponse;
import com.parking.dto.response.UserResponse;

/**
 * 认证服务
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应（Token + 用户信息）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 用户信息
     */
    UserResponse register(RegisterRequest request);

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    UserResponse getCurrentUser();
}
