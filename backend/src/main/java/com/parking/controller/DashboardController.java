package com.parking.controller;

import com.parking.common.result.Result;
import com.parking.dto.response.DashboardResponse;
import com.parking.dto.response.OccupancyStatsResponse;
import com.parking.dto.response.RevenueStatsResponse;
import com.parking.service.DashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 仪表盘控制器
 */
@Api(tags = "仪表盘统计")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @ApiOperation("获取仪表盘汇总统计")
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<DashboardResponse> getDashboard() {
        DashboardResponse data = dashboardService.getDashboard();
        return Result.success(data);
    }

    @ApiOperation("获取收入统计")
    @GetMapping("/revenue")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<List<RevenueStatsResponse>> getRevenueStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<RevenueStatsResponse> data = dashboardService.getRevenueStats(startDate, endDate);
        return Result.success(data);
    }

    @ApiOperation("获取车位占用统计")
    @GetMapping("/occupancy")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<List<OccupancyStatsResponse>> getOccupancyStats() {
        List<OccupancyStatsResponse> data = dashboardService.getOccupancyStats();
        return Result.success(data);
    }
}
