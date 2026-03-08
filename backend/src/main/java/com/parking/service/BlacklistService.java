package com.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.common.result.PageResult;
import com.parking.dto.request.BlacklistRequest;
import com.parking.entity.Blacklist;

/**
 * 黑名单服务
 */
public interface BlacklistService extends IService<Blacklist> {

    /**
     * 分页查询黑名单
     */
    PageResult<Blacklist> getPage(int page, int size, String keyword);

    /**
     * 新增黑名单
     */
    Blacklist create(Long operatorId, BlacklistRequest request);

    /**
     * 更新黑名单
     */
    Blacklist update(Long id, BlacklistRequest request);

    /**
     * 删除黑名单
     */
    void delete(Long id);

    /**
     * 判断车牌号是否在黑名单中
     */
    boolean isBlacklisted(String plateNumber);
}
