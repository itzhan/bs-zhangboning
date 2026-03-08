package com.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.dto.request.VehicleRequest;
import com.parking.dto.response.VehicleResponse;
import com.parking.entity.Vehicle;

import java.util.List;

/**
 * 车辆服务
 */
public interface VehicleService extends IService<Vehicle> {

    /**
     * 查询用户的车辆列表
     */
    List<VehicleResponse> getByUserId(Long userId);

    /**
     * 根据ID查询车辆
     */
    VehicleResponse getById(Long id);

    /**
     * 新增车辆
     */
    VehicleResponse create(Long userId, VehicleRequest request);

    /**
     * 更新车辆
     */
    VehicleResponse update(Long id, VehicleRequest request);

    /**
     * 删除车辆
     */
    void delete(Long id);
}
