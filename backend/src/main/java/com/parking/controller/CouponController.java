package com.parking.controller;

import com.parking.common.result.PageResult;
import com.parking.common.result.Result;
import com.parking.dto.request.CouponRequest;
import com.parking.entity.Coupon;
import com.parking.service.CouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 优惠券控制器
 */
@Api(tags = "优惠券管理")
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @ApiOperation("分页查询优惠券（管理员）")
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<PageResult<Coupon>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<Coupon> data = couponService.getPage(page, size);
        return Result.success(data);
    }

    @ApiOperation("获取当前可领取的优惠券列表")
    @GetMapping("/available")
    public Result<List<Coupon>> getAvailable() {
        List<Coupon> data = couponService.getAvailable();
        return Result.success(data);
    }

    @ApiOperation("新增优惠券")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Coupon> create(@RequestBody @Valid CouponRequest request) {
        Coupon data = couponService.create(request);
        return Result.success(data);
    }

    @ApiOperation("更新优惠券")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Coupon> update(@PathVariable Long id,
                                 @RequestBody @Valid CouponRequest request) {
        Coupon data = couponService.update(id, request);
        return Result.success(data);
    }

    @ApiOperation("删除优惠券")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        couponService.delete(id);
        return Result.success();
    }
}
