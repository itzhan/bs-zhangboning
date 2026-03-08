package com.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.common.result.PageResult;
import com.parking.dto.request.AppealReplyRequest;
import com.parking.dto.request.AppealRequest;
import com.parking.dto.response.AppealResponse;
import com.parking.entity.Appeal;

/**
 * 申诉服务
 */
public interface AppealService extends IService<Appeal> {

    /**
     * 创建申诉
     */
    AppealResponse create(Long userId, AppealRequest request);

    /**
     * 管理员回复申诉
     */
    AppealResponse reply(Long id, AppealReplyRequest request);

    /**
     * 查询用户的申诉列表
     */
    PageResult<AppealResponse> getByUserId(Long userId, int page, int size);

    /**
     * 分页查询全部申诉（管理员）
     */
    PageResult<AppealResponse> getPage(int page, int size, Integer status);
}
