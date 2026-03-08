package com.parking.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 停车场响应
 */
@Data
public class ParkingLotResponse {

    private Long id;

    private String name;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Integer totalSpaces;

    private Integer availableSpaces;

    private String image;

    private String description;

    private String openTime;

    private String closeTime;

    private String contactPhone;

    private Integer status;

    private String createdAt;

    private String updatedAt;

    /** 关联的计费规则名称 */
    private String billingRuleName;
}
