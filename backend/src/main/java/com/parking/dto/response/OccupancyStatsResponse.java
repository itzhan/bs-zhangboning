package com.parking.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 车位占用统计响应
 */
@Data
public class OccupancyStatsResponse {

    private String lotName;

    private int totalSpaces;

    private int occupiedSpaces;

    private BigDecimal occupancyRate;
}
