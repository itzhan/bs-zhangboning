package com.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.common.result.PageResult;
import com.parking.dto.request.CouponRequest;
import com.parking.entity.Coupon;

import java.util.List;

/**
 * 优惠券服务
 */
public interface CouponService extends IService<Coupon> {

    /**
     * 分页查询优惠券
     */
    PageResult<Coupon> getPage(int page, int size);

    /**
     * 新增优惠券
     */
    Coupon create(CouponRequest request);

    /**
     * 更新优惠券
     */
    Coupon update(Long id, CouponRequest request);

    /**
     * 删除优惠券
     */
    void delete(Long id);

    /**
     * 获取当前可领取的优惠券列表
     */
    List<Coupon> getAvailable();
}
