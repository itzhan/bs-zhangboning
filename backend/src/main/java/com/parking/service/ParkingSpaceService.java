package com.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.common.result.PageResult;
import com.parking.dto.request.ParkingSpaceRequest;
import com.parking.dto.response.ParkingSpaceResponse;
import com.parking.entity.ParkingSpace;

import java.util.List;

/**
 * 车位服务
 */
public interface ParkingSpaceService extends IService<ParkingSpace> {

    /**
     * 分页查询车位列表（含停车场名称）
     */
    PageResult<ParkingSpaceResponse> getPage(int page, int size, Long lotId, Integer status, Integer type);

    /**
     * 根据ID查询车位详情
     */
    ParkingSpaceResponse getById(Long id);

    /**
     * 新增车位
     */
    ParkingSpaceResponse create(ParkingSpaceRequest request);

    /**
     * 更新车位
     */
    ParkingSpaceResponse update(Long id, ParkingSpaceRequest request);

    /**
     * 删除车位（硬删除）
     */
    void delete(Long id);

    /**
     * 批量创建车位
     *
     * @param lotId  停车场ID
     * @param prefix 编号前缀
     * @param count  数量
     * @param type   车位类型
     * @param floor  楼层
     * @param area   区域
     */
    void batchCreate(Long lotId, String prefix, int count, Integer type, String floor, String area);

    /**
     * 获取某停车场的空闲车位列表（用于预约）
     */
    List<ParkingSpaceResponse> getAvailableByLotId(Long lotId);
}
