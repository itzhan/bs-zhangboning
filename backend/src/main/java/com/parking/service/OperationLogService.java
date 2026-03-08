package com.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.common.result.PageResult;
import com.parking.entity.OperationLog;

/**
 * 操作日志服务
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 记录操作日志
     */
    void log(Long userId, String username, String module, String operation,
             String method, String params, String ip, Integer status, String errorMsg);

    /**
     * 分页查询操作日志
     */
    PageResult<OperationLog> getPage(int page, int size);
}
