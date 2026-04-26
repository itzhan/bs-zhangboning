package com.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.dto.response.ScenicSpotResponse;
import com.parking.entity.ScenicSpot;

import java.util.List;

/**
 * 景区景点服务
 */
public interface ScenicSpotService extends IService<ScenicSpot> {

    /**
     * 获取所有启用的景点，并附带实时附近停车场聚合数据
     */
    List<ScenicSpotResponse> getAllWithRealtime();
}
