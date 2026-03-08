package com.parking.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.common.exception.BusinessException;
import com.parking.common.result.PageResult;
import com.parking.dto.request.CouponRequest;
import com.parking.entity.Coupon;
import com.parking.mapper.CouponMapper;
import com.parking.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券服务实现类
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    @Override
    public PageResult<Coupon> getPage(int page, int size) {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Coupon::getCreatedAt);

        Page<Coupon> pageResult = page(new Page<>(page, size), wrapper);
        return PageResult.of(pageResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Coupon create(CouponRequest request) {
        Coupon coupon = new Coupon();
        BeanUtil.copyProperties(request, coupon);
        coupon.setRemainingCount(request.getTotalCount());
        coupon.setUsedCount(0);
        if (coupon.getStatus() == null) {
            coupon.setStatus(1);
        }
        getBaseMapper().insert(coupon);
        return coupon;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Coupon update(Long id, CouponRequest request) {
        Coupon coupon = getBaseMapper().selectById(id);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        BeanUtil.copyProperties(request, coupon, "id");
        getBaseMapper().updateById(coupon);
        return coupon;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Coupon coupon = getBaseMapper().selectById(id);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }
        getBaseMapper().deleteById(id);
    }

    @Override
    public List<Coupon> getAvailable() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getStatus, 1)
                .le(Coupon::getStartTime, now)
                .ge(Coupon::getEndTime, now)
                .gt(Coupon::getRemainingCount, 0)
                .orderByDesc(Coupon::getCreatedAt);
        return getBaseMapper().selectList(wrapper);
    }
}
