package com.parking.controller;

import com.parking.common.result.Result;
import com.parking.dto.request.LoginRequest;
import com.parking.dto.request.RegisterRequest;
import com.parking.dto.response.LoginResponse;
import com.parking.dto.response.UserResponse;
import com.parking.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证控制器
 */
@Api(tags = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse data = authService.login(request);
        return Result.success(data);
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<UserResponse> register(@RequestBody @Valid RegisterRequest request) {
        UserResponse data = authService.register(request);
        return Result.success(data);
    }

    @ApiOperation("获取当前登录用户信息")
    @GetMapping("/info")
    public Result<UserResponse> getCurrentUser() {
        UserResponse data = authService.getCurrentUser();
        return Result.success(data);
    }
}
