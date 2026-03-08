package com.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.common.result.PageResult;
import com.parking.dto.request.PaymentRequest;
import com.parking.dto.response.PaymentResponse;
import com.parking.entity.Payment;

/**
 * 支付服务
 */
public interface PaymentService extends IService<Payment> {

    /**
     * 支付订单（模拟支付）
     */
    PaymentResponse pay(Long userId, PaymentRequest request);

    /**
     * 根据订单ID查询支付记录
     */
    PaymentResponse getByOrderId(Long orderId);

    /**
     * 分页查询支付记录（管理员）
     */
    PageResult<PaymentResponse> getPage(int page, int size);
}
