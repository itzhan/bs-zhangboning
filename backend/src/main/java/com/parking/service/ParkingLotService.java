package com.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.common.result.PageResult;
import com.parking.dto.request.ParkingLotRequest;
import com.parking.dto.response.ParkingLotResponse;
import com.parking.entity.ParkingLot;

import java.util.List;

/**
 * 停车场服务
 */
public interface ParkingLotService extends IService<ParkingLot> {

    /**
     * 分页查询停车场列表
     */
    PageResult<ParkingLotResponse> getPage(int page, int size, String keyword, Integer status);

    /**
     * 根据ID查询停车场详情（含计费规则名称）
     */
    ParkingLotResponse getById(Long id);

    /**
     * 新增停车场
     */
    ParkingLotResponse create(ParkingLotRequest request);

    /**
     * 更新停车场
     */
    ParkingLotResponse update(Long id, ParkingLotRequest request);

    /**
     * 删除停车场（软删除）
     */
    void delete(Long id);

    /**
     * 获取全部停车场（下拉选择用）
     */
    List<ParkingLotResponse> getAll();

    /**
     * 刷新停车场可用车位数（从 parking_space 表重新计算）
     */
    void refreshAvailableSpaces(Long lotId);
}
