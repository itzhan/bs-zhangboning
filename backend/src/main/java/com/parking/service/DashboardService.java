package com.parking.service;

import com.parking.dto.response.DashboardResponse;
import com.parking.dto.response.OccupancyStatsResponse;
import com.parking.dto.response.RevenueStatsResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * 仪表盘统计服务
 */
public interface DashboardService {

    /**
     * 获取仪表盘汇总统计
     */
    DashboardResponse getDashboard();

    /**
     * 获取收入统计
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 每日收入统计列表
     */
    List<RevenueStatsResponse> getRevenueStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取车位占用统计
     */
    List<OccupancyStatsResponse> getOccupancyStats();
}
