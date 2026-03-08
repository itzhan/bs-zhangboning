package com.parking.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 预约响应
 */
@Data
public class ReservationResponse {

    private Long id;

    private Long userId;

    private Long lotId;

    private Long spaceId;

    private Long vehicleId;

    private String startTime;

    private String endTime;

    private Integer status;

    private BigDecimal fee;

    private String createdAt;

    private String updatedAt;

    /** 车牌号 */
    private String plateNumber;

    /** 用户名 */
    private String username;

    /** 停车场名称 */
    private String lotName;

    /** 车位编号 */
    private String spaceName;
}
