package com.parking.controller;

import com.parking.common.result.PageResult;
import com.parking.common.result.Result;
import com.parking.dto.request.PasswordUpdateRequest;
import com.parking.dto.request.UserUpdateRequest;
import com.parking.dto.response.UserResponse;
import com.parking.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @ApiOperation("分页查询用户列表")
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<PageResult<UserResponse>> getUserPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        PageResult<UserResponse> data = userService.getUserPage(page, size, keyword, role);
        return Result.success(data);
    }

    @ApiOperation("根据ID查询用户")
    @GetMapping("/{id}")
    public Result<UserResponse> getById(@PathVariable Long id) {
        UserResponse data = userService.getUserById(id);
        return Result.success(data);
    }

    @ApiOperation("更新用户信息")
    @PutMapping("/{id}")
    public Result<UserResponse> updateUser(@PathVariable Long id,
                                           @RequestBody @Valid UserUpdateRequest request) {
        UserResponse data = userService.updateUser(id, request);
        return Result.success(data);
    }

    @ApiOperation("修改密码")
    @PutMapping("/{id}/password")
    public Result<Void> updatePassword(@PathVariable Long id,
                                       @RequestBody @Valid PasswordUpdateRequest request) {
        userService.updatePassword(id, request);
        return Result.success();
    }

    @ApiOperation("更新用户状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
