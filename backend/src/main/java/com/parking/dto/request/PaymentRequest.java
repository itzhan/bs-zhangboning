package com.parking.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 支付请求
 */
@Data
public class PaymentRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    private Integer paymentMethod;

    private Long couponId;
}
