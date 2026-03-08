package com.parking.controller;

import com.parking.common.result.PageResult;
import com.parking.common.result.Result;
import com.parking.dto.request.ReservationRequest;
import com.parking.dto.response.ReservationResponse;
import com.parking.entity.User;
import com.parking.mapper.UserMapper;
import com.parking.service.ReservationService;
import com.parking.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 预约控制器
 */
@Api(tags = "预约管理")
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserMapper userMapper;

    @ApiOperation("查询我的预约列表")
    @GetMapping
    public Result<PageResult<ReservationResponse>> getMyReservations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = userMapper.selectByUsername(SecurityUtil.getCurrentUsername());
        PageResult<ReservationResponse> data = reservationService.getByUserId(currentUser.getId(), page, size);
        return Result.success(data);
    }

    @ApiOperation("分页查询全部预约（管理员）")
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<PageResult<ReservationResponse>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long lotId) {
        PageResult<ReservationResponse> data = reservationService.getPage(page, size, status, lotId);
        return Result.success(data);
    }

    @ApiOperation("根据ID查询预约详情")
    @GetMapping("/{id}")
    public Result<ReservationResponse> getById(@PathVariable Long id) {
        ReservationResponse data = reservationService.getById(id);
        return Result.success(data);
    }

    @ApiOperation("创建预约")
    @PostMapping
    public Result<ReservationResponse> create(@RequestBody @Valid ReservationRequest request) {
        User currentUser = userMapper.selectByUsername(SecurityUtil.getCurrentUsername());
        ReservationResponse data = reservationService.create(currentUser.getId(), request);
        return Result.success(data);
    }

    @ApiOperation("取消预约")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        User currentUser = userMapper.selectByUsername(SecurityUtil.getCurrentUsername());
        reservationService.cancel(id, currentUser.getId());
        return Result.success();
    }
}
