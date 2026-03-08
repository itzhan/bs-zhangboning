package com.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.common.result.PageResult;
import com.parking.dto.request.AnnouncementRequest;
import com.parking.dto.response.AnnouncementResponse;
import com.parking.entity.Announcement;

/**
 * 公告服务
 */
public interface AnnouncementService extends IService<Announcement> {

    /**
     * 分页查询公告列表
     */
    PageResult<AnnouncementResponse> getPage(int page, int size, Integer type, Integer status);

    /**
     * 获取已发布的公告列表（公开接口）
     */
    PageResult<AnnouncementResponse> getPublished(int page, int size);

    /**
     * 根据ID查询公告详情（含发布者姓名）
     */
    AnnouncementResponse getById(Long id);

    /**
     * 新增公告
     */
    AnnouncementResponse create(Long publisherId, AnnouncementRequest request);

    /**
     * 更新公告
     */
    AnnouncementResponse update(Long id, AnnouncementRequest request);

    /**
     * 删除公告
     */
    void delete(Long id);

    /**
     * 发布公告
     */
    void publish(Long id);
}
