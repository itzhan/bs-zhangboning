package com.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.entity.UserCoupon;

import java.util.List;

/**
 * 用户优惠券服务
 */
public interface UserCouponService extends IService<UserCoupon> {

    /**
     * 领取优惠券
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     */
    void claim(Long userId, Long couponId);

    /**
     * 查询用户的优惠券列表（含优惠券详情）
     */
    List<UserCoupon> getByUserId(Long userId);

    /**
     * 使用优惠券
     *
     * @param id     用户优惠券ID
     * @param userId 用户ID
     */
    void use(Long id, Long userId);
}
