package com.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.common.result.PageResult;
import com.parking.dto.request.ParkingEntryRequest;
import com.parking.dto.request.ParkingExitRequest;
import com.parking.dto.response.OrderResponse;
import com.parking.entity.ParkingOrder;

/**
 * 停车订单服务
 */
public interface ParkingOrderService extends IService<ParkingOrder> {

    /**
     * 车辆入场
     */
    OrderResponse entry(Long userId, ParkingEntryRequest request);

    /**
     * 车辆出场
     */
    OrderResponse exit(ParkingExitRequest request);

    /**
     * 查询用户的订单列表
     */
    PageResult<OrderResponse> getByUserId(Long userId, int page, int size);

    /**
     * 分页查询全部订单（管理员）
     */
    PageResult<OrderResponse> getPage(int page, int size, Integer status, Long lotId, String keyword);

    /**
     * 根据ID查询订单详情
     */
    OrderResponse getById(Long id);

    /**
     * 生成入场二维码数据
     */
    String generateEntryQrCode(Long orderId);
}
