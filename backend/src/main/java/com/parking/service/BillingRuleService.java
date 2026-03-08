package com.parking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.parking.common.result.PageResult;
import com.parking.dto.request.BillingRuleRequest;
import com.parking.entity.BillingRule;

/**
 * 计费规则服务
 */
public interface BillingRuleService extends IService<BillingRule> {

    /**
     * 分页查询计费规则
     */
    PageResult<BillingRule> getPage(int page, int size);

    /**
     * 根据ID查询计费规则
     */
    BillingRule getById(Long id);

    /**
     * 新增计费规则
     */
    BillingRule create(BillingRuleRequest request);

    /**
     * 更新计费规则
     */
    BillingRule update(Long id, BillingRuleRequest request);

    /**
     * 删除计费规则
     */
    void delete(Long id);

    /**
     * 获取某停车场适用的计费规则（优先场级规则，兜底全局规则）
     */
    BillingRule getByLotId(Long lotId);
}
