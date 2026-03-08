package com.parking.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.common.exception.BusinessException;
import com.parking.common.result.PageResult;
import com.parking.dto.request.PaymentRequest;
import com.parking.dto.response.PaymentResponse;
import com.parking.entity.*;
import com.parking.mapper.CouponMapper;
import com.parking.mapper.ParkingOrderMapper;
import com.parking.mapper.PaymentMapper;
import com.parking.mapper.UserCouponMapper;
import com.parking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付服务实现类
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    private final ParkingOrderMapper parkingOrderMapper;
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResponse pay(Long userId, PaymentRequest request) {
        ParkingOrder order = parkingOrderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getPaymentStatus() != null && order.getPaymentStatus() == 1) {
            throw new BusinessException("订单已支付，请勿重复支付");
        }

        BigDecimal amount = order.getAmount() != null ? order.getAmount() : BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        // 使用优惠券
        if (request.getCouponId() != null) {
            UserCoupon userCoupon = userCouponMapper.selectById(request.getCouponId());
            if (userCoupon == null) {
                throw new BusinessException("优惠券不存在");
            }
            if (!userCoupon.getUserId().equals(userId)) {
                throw new BusinessException("该优惠券不属于当前用户");
            }
            if (userCoupon.getStatus() != 0) {
                throw new BusinessException("该优惠券已使用或已过期");
            }

            // 查询优惠券详情
            Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
            if (coupon != null) {
                // 检查最低消费
                if (coupon.getMinAmount() != null && amount.compareTo(coupon.getMinAmount()) < 0) {
                    throw new BusinessException("订单金额未达到优惠券使用门槛");
                }
                discountAmount = coupon.getDiscountValue() != null ? coupon.getDiscountValue() : BigDecimal.ZERO;
            }

            // 标记优惠券已使用
            userCoupon.setStatus(1);
            userCoupon.setUsedTime(LocalDateTime.now());
            userCouponMapper.updateById(userCoupon);
        }

        // 计算实际支付金额
        BigDecimal actualAmount = amount.subtract(discountAmount);
        if (actualAmount.compareTo(BigDecimal.ZERO) < 0) {
            actualAmount = BigDecimal.ZERO;
        }

        // 更新订单
        order.setDiscountAmount(discountAmount);
        order.setActualAmount(actualAmount);
        order.setPaymentStatus(1); // 已支付
        order.setOrderStatus(2);   // 已完成
        parkingOrderMapper.updateById(order);

        // 创建支付记录（模拟支付）
        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setPaymentNo("PAY" + IdUtil.simpleUUID().substring(0, 16).toUpperCase());
        payment.setAmount(actualAmount);
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : 1);
        payment.setPaymentStatus(1); // 支付成功
        payment.setPaymentTime(LocalDateTime.now());
        getBaseMapper().insert(payment);

        return convertToResponse(payment, order);
    }

    @Override
    public PaymentResponse getByOrderId(Long orderId) {
        Payment payment = getBaseMapper().selectOne(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getOrderId, orderId)
                        .orderByDesc(Payment::getCreatedAt)
                        .last("LIMIT 1")
        );
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }
        ParkingOrder order = parkingOrderMapper.selectById(orderId);
        return convertToResponse(payment, order);
    }

    @Override
    public PageResult<PaymentResponse> getPage(int page, int size) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Payment::getCreatedAt);

        Page<Payment> pageResult = page(new Page<>(page, size), wrapper);

        List<PaymentResponse> list = pageResult.getRecords().stream()
                .map(p -> {
                    ParkingOrder order = parkingOrderMapper.selectById(p.getOrderId());
                    return convertToResponse(p, order);
                })
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    /**
     * 实体转换为响应DTO
     */
    private PaymentResponse convertToResponse(Payment payment, ParkingOrder order) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setTransactionNo(payment.getPaymentNo());
        response.setStatus(payment.getPaymentStatus());
        if (payment.getPaymentTime() != null) {
            response.setPaidAt(payment.getPaymentTime().format(FORMATTER));
        }
        if (payment.getCreatedAt() != null) {
            response.setCreatedAt(payment.getCreatedAt().format(FORMATTER));
        }
        if (order != null) {
            response.setOrderNo(order.getOrderNo());
        }
        return response;
    }
}
