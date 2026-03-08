package com.parking.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收入统计响应
 */
@Data
public class RevenueStatsResponse {

    private String date;

    private BigDecimal revenue;

    private int orderCount;
}
