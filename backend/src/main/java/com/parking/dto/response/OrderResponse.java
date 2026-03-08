package com.parking.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 停车订单响应
 */
@Data
public class OrderResponse {

    private Long id;

    private String orderNo;

    private Long userId;

    private Long lotId;

    private Long spaceId;

    private String plateNumber;

    private String entryTime;

    private String exitTime;

    private BigDecimal duration;

    private BigDecimal amount;

    private BigDecimal actualAmount;

    private Integer paymentStatus;

    private Integer paymentMethod;

    private Long couponId;

    private Integer status;

    private String createdAt;

    private String updatedAt;

    /** 停车场名称 */
    private String lotName;

    /** 车位编号 */
    private String spaceName;

    /** 用户名 */
    private String username;
}
