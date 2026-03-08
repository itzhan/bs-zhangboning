package com.parking.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 仪表盘统计响应
 */
@Data
public class DashboardResponse {

    /** 停车场总数 */
    private int totalLots;

    /** 车位总数 */
    private int totalSpaces;

    /** 可用车位数 */
    private int availableSpaces;

    /** 车位占用率 */
    private BigDecimal occupancyRate;

    /** 今日订单数 */
    private int todayOrders;

    /** 今日收入 */
    private BigDecimal todayRevenue;

    /** 今日预约数 */
    private int todayReservations;

    /** 活跃告警数 */
    private int activeAlerts;
}
