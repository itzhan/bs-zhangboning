package com.parking.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付响应
 */
@Data
public class PaymentResponse {

    private Long id;

    private Long orderId;

    private BigDecimal amount;

    private Integer paymentMethod;

    private String transactionNo;

    private Integer status;

    private String paidAt;

    private String createdAt;

    /** 关联订单号 */
    private String orderNo;
}
