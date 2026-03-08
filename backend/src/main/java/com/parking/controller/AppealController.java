package com.parking.controller;

import com.parking.common.result.PageResult;
import com.parking.common.result.Result;
import com.parking.dto.request.AppealReplyRequest;
import com.parking.dto.request.AppealRequest;
import com.parking.dto.response.AppealResponse;
import com.parking.entity.User;
import com.parking.mapper.UserMapper;
import com.parking.service.AppealService;
import com.parking.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 申诉控制器
 */
@Api(tags = "申诉管理")
@RestController
@RequestMapping("/api/appeals")
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;
    private final UserMapper userMapper;

    @ApiOperation("查询我的申诉列表")
    @GetMapping
    public Result<PageResult<AppealResponse>> getMyAppeals(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = userMapper.selectByUsername(SecurityUtil.getCurrentUsername());
        PageResult<AppealResponse> data = appealService.getByUserId(currentUser.getId(), page, size);
        return Result.success(data);
    }

    @ApiOperation("分页查询全部申诉（管理员）")
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<PageResult<AppealResponse>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        PageResult<AppealResponse> data = appealService.getPage(page, size, status);
        return Result.success(data);
    }

    @ApiOperation("创建申诉")
    @PostMapping
    public Result<AppealResponse> create(@RequestBody @Valid AppealRequest request) {
        User currentUser = userMapper.selectByUsername(SecurityUtil.getCurrentUsername());
        AppealResponse data = appealService.create(currentUser.getId(), request);
        return Result.success(data);
    }

    @ApiOperation("回复申诉（管理员）")
    @PutMapping("/{id}/reply")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<AppealResponse> reply(@PathVariable Long id,
                                        @RequestBody @Valid AppealReplyRequest request) {
        AppealResponse data = appealService.reply(id, request);
        return Result.success(data);
    }
}
