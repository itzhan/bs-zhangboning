package com.parking.controller;

import com.parking.common.result.PageResult;
import com.parking.common.result.Result;
import com.parking.dto.request.PaymentRequest;
import com.parking.dto.response.PaymentResponse;
import com.parking.entity.User;
import com.parking.mapper.UserMapper;
import com.parking.service.PaymentService;
import com.parking.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 支付控制器
 */
@Api(tags = "支付管理")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserMapper userMapper;

    @ApiOperation("支付订单")
    @PostMapping("/pay")
    public Result<PaymentResponse> pay(@RequestBody @Valid PaymentRequest request) {
        User currentUser = userMapper.selectByUsername(SecurityUtil.getCurrentUsername());
        PaymentResponse data = paymentService.pay(currentUser.getId(), request);
        return Result.success(data);
    }

    @ApiOperation("根据订单ID查询支付记录")
    @GetMapping("/order/{orderId}")
    public Result<PaymentResponse> getByOrderId(@PathVariable Long orderId) {
        PaymentResponse data = paymentService.getByOrderId(orderId);
        return Result.success(data);
    }

    @ApiOperation("分页查询支付记录（管理员）")
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<PageResult<PaymentResponse>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<PaymentResponse> data = paymentService.getPage(page, size);
        return Result.success(data);
    }
}
