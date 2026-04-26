package com.parking.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 景区景点响应（含实时统计）
 */
@Data
public class ScenicSpotResponse {

    private Long id;

    private String name;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String description;

    private String image;

    private BigDecimal radiusKm;

    private Integer sortOrder;

    private Integer status;

    /** 半径范围内运营中的停车场数量 */
    private Integer nearbyLotCount;

    /** 半径范围内停车场实时可用车位总数 */
    private Integer nearbyAvailableSpaces;

    /** 半径范围内停车场总车位数 */
    private Integer nearbyTotalSpaces;
}
