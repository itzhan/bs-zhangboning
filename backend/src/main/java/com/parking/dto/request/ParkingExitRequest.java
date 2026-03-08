package com.parking.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 停车出场请求
 */
@Data
public class ParkingExitRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;
}
