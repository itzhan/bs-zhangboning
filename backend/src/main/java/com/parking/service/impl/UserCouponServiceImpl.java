package com.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.common.exception.BusinessException;
import com.parking.entity.Coupon;
import com.parking.entity.UserCoupon;
import com.parking.mapper.CouponMapper;
import com.parking.mapper.UserCouponMapper;
import com.parking.service.UserCouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户优惠券服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl extends ServiceImpl<UserCouponMapper, UserCoupon> implements UserCouponService {

    private final CouponMapper couponMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claim(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        if (coupon.getStatus() != 1) {
            throw new BusinessException("该优惠券已下架");
        }
        if (coupon.getRemainingCount() <= 0) {
            throw new BusinessException("该优惠券已领完");
        }
        // 检查是否在有效期内
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new BusinessException("该优惠券不在有效领取期内");
        }

        // 检查用户是否已领取过该优惠券
        Long claimedCount = getBaseMapper().selectCount(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getCouponId, couponId)
        );
        if (claimedCount > 0) {
            throw new BusinessException("您已领取过该优惠券");
        }

        // 创建用户优惠券记录
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(0); // 未使用
        getBaseMapper().insert(userCoupon);

        // 减少优惠券剩余数量
        coupon.setRemainingCount(coupon.getRemainingCount() - 1);
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponMapper.updateById(coupon);
    }

    @Override
    public List<UserCoupon> getByUserId(Long userId) {
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
                .orderByDesc(UserCoupon::getCreatedAt);
        return getBaseMapper().selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void use(Long id, Long userId) {
        UserCoupon userCoupon = getBaseMapper().selectById(id);
        if (userCoupon == null) {
            throw new BusinessException("用户优惠券不存在");
        }
        if (!userCoupon.getUserId().equals(userId)) {
            throw new BusinessException("该优惠券不属于当前用户");
        }
        if (userCoupon.getStatus() != 0) {
            throw new BusinessException("该优惠券已使用或已过期");
        }

        userCoupon.setStatus(1); // 已使用
        userCoupon.setUsedTime(LocalDateTime.now());
        getBaseMapper().updateById(userCoupon);
    }
}
