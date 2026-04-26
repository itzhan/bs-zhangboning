package com.parking.controller;

import com.parking.common.result.PageResult;
import com.parking.common.result.Result;
import com.parking.dto.response.AnnouncementResponse;
import com.parking.dto.response.ParkingLotResponse;
import com.parking.dto.response.ScenicSpotResponse;
import com.parking.entity.Coupon;
import com.parking.service.AnnouncementService;
import com.parking.service.CouponService;
import com.parking.service.ParkingLotService;
import com.parking.service.ScenicSpotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公开接口控制器（无需认证）
 */
@Api(tags = "公开接口")
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final AnnouncementService announcementService;
    private final ParkingLotService parkingLotService;
    private final CouponService couponService;
    private final ScenicSpotService scenicSpotService;

    @ApiOperation("获取已发布的公告列表")
    @GetMapping("/announcements")
    public Result<PageResult<AnnouncementResponse>> getPublishedAnnouncements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<AnnouncementResponse> data = announcementService.getPublished(page, size);
        return Result.success(data);
    }

    @ApiOperation("获取全部停车场列表")
    @GetMapping("/parking-lots")
    public Result<List<ParkingLotResponse>> getAllParkingLots() {
        List<ParkingLotResponse> data = parkingLotService.getAll();
        return Result.success(data);
    }

    @ApiOperation("获取停车场详情")
    @GetMapping("/parking-lots/{id}")
    public Result<ParkingLotResponse> getParkingLotDetail(@PathVariable Long id) {
        ParkingLotResponse data = parkingLotService.getById(id);
        return Result.success(data);
    }

    @ApiOperation("获取可领取的优惠券列表")
    @GetMapping("/coupons")
    public Result<List<Coupon>> getAvailableCoupons() {
        List<Coupon> data = couponService.getAvailable();
        return Result.success(data);
    }

    @ApiOperation("获取景区景点列表（含附近停车场实时统计）")
    @GetMapping("/scenic-spots")
    public Result<List<ScenicSpotResponse>> getScenicSpots() {
        List<ScenicSpotResponse> data = scenicSpotService.getAllWithRealtime();
        return Result.success(data);
    }
}
