package com.parking.controller;

import com.parking.common.result.Result;
import com.parking.entity.User;
import com.parking.entity.UserCoupon;
import com.parking.mapper.UserMapper;
import com.parking.service.UserCouponService;
import com.parking.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户优惠券控制器
 */
@Api(tags = "用户优惠券管理")
@RestController
@RequestMapping("/api/user-coupons")
@RequiredArgsConstructor
public class UserCouponController {

    private final UserCouponService userCouponService;
    private final UserMapper userMapper;

    @ApiOperation("查询我的优惠券列表")
    @GetMapping
    public Result<List<UserCoupon>> getMyUserCoupons() {
        User currentUser = userMapper.selectByUsername(SecurityUtil.getCurrentUsername());
        List<UserCoupon> data = userCouponService.getByUserId(currentUser.getId());
        return Result.success(data);
    }

    @ApiOperation("领取优惠券")
    @PostMapping("/claim/{couponId}")
    public Result<Void> claim(@PathVariable Long couponId) {
        User currentUser = userMapper.selectByUsername(SecurityUtil.getCurrentUsername());
        userCouponService.claim(currentUser.getId(), couponId);
        return Result.success();
    }
}
