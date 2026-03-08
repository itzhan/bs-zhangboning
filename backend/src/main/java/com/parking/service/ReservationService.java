package com.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.common.result.PageResult;
import com.parking.dto.request.ReservationRequest;
import com.parking.dto.response.ReservationResponse;
import com.parking.entity.Reservation;

/**
 * 预约服务
 */
public interface ReservationService extends IService<Reservation> {

    /**
     * 创建预约
     */
    ReservationResponse create(Long userId, ReservationRequest request);

    /**
     * 取消预约
     */
    void cancel(Long id, Long userId);

    /**
     * 查询用户的预约列表
     */
    PageResult<ReservationResponse> getByUserId(Long userId, int page, int size);

    /**
     * 分页查询全部预约（管理员）
     */
    PageResult<ReservationResponse> getPage(int page, int size, Integer status, Long lotId);

    /**
     * 根据ID查询预约详情
     */
    ReservationResponse getById(Long id);
}
