package com.parking.controller;

import com.parking.common.result.PageResult;
import com.parking.common.result.Result;
import com.parking.dto.request.ParkingEntryRequest;
import com.parking.dto.request.ParkingExitRequest;
import com.parking.dto.response.OrderResponse;
import com.parking.entity.User;
import com.parking.mapper.UserMapper;
import com.parking.service.ParkingOrderService;
import com.parking.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 停车订单控制器
 */
@Api(tags = "停车订单管理")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class ParkingOrderController {

    private final ParkingOrderService parkingOrderService;
    private final UserMapper userMapper;

    @ApiOperation("查询我的订单列表")
    @GetMapping
    public Result<PageResult<OrderResponse>> getMyOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = userMapper.selectByUsername(SecurityUtil.getCurrentUsername());
        PageResult<OrderResponse> data = parkingOrderService.getByUserId(currentUser.getId(), page, size);
        return Result.success(data);
    }

    @ApiOperation("分页查询全部订单（管理员）")
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<PageResult<OrderResponse>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long lotId,
            @RequestParam(required = false) String keyword) {
        PageResult<OrderResponse> data = parkingOrderService.getPage(page, size, status, lotId, keyword);
        return Result.success(data);
    }

    @ApiOperation("根据ID查询订单详情")
    @GetMapping("/{id}")
    public Result<OrderResponse> getById(@PathVariable Long id) {
        OrderResponse data = parkingOrderService.getById(id);
        return Result.success(data);
    }

    @ApiOperation("车辆入场（扫码入场）")
    @PostMapping("/entry")
    public Result<OrderResponse> entry(@RequestBody @Valid ParkingEntryRequest request) {
        User currentUser = userMapper.selectByUsername(SecurityUtil.getCurrentUsername());
        OrderResponse data = parkingOrderService.entry(currentUser.getId(), request);
        return Result.success(data);
    }

    @ApiOperation("车辆出场")
    @PostMapping("/{id}/exit")
    public Result<OrderResponse> exit(@PathVariable Long id) {
        ParkingExitRequest request = new ParkingExitRequest();
        request.setOrderId(id);
        OrderResponse data = parkingOrderService.exit(request);
        return Result.success(data);
    }

    @ApiOperation("生成入场二维码")
    @GetMapping("/{id}/qrcode")
    public Result<String> generateQrCode(@PathVariable Long id) {
        String data = parkingOrderService.generateEntryQrCode(id);
        return Result.success(data);
    }
}
