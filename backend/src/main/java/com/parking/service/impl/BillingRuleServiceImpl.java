package com.parking.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.common.exception.BusinessException;
import com.parking.common.result.PageResult;
import com.parking.dto.request.BillingRuleRequest;
import com.parking.entity.BillingRule;
import com.parking.mapper.BillingRuleMapper;
import com.parking.service.BillingRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 计费规则服务实现类
 */
@Service
@RequiredArgsConstructor
public class BillingRuleServiceImpl extends ServiceImpl<BillingRuleMapper, BillingRule> implements BillingRuleService {

    @Override
    public PageResult<BillingRule> getPage(int page, int size) {
        LambdaQueryWrapper<BillingRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(BillingRule::getCreatedAt);

        Page<BillingRule> pageResult = page(new Page<>(page, size), wrapper);
        return PageResult.of(pageResult);
    }

    @Override
    public BillingRule getById(Long id) {
        BillingRule rule = getBaseMapper().selectById(id);
        if (rule == null) {
            throw new BusinessException("计费规则不存在");
        }
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillingRule create(BillingRuleRequest request) {
        BillingRule rule = new BillingRule();
        BeanUtil.copyProperties(request, rule);
        if (rule.getStatus() == null) {
            rule.setStatus(1);
        }
        getBaseMapper().insert(rule);
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillingRule update(Long id, BillingRuleRequest request) {
        BillingRule rule = getBaseMapper().selectById(id);
        if (rule == null) {
            throw new BusinessException("计费规则不存在");
        }
        BeanUtil.copyProperties(request, rule, "id");
        getBaseMapper().updateById(rule);
        return rule;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        BillingRule rule = getBaseMapper().selectById(id);
        if (rule == null) {
            throw new BusinessException("计费规则不存在");
        }
        getBaseMapper().deleteById(id);
    }

    @Override
    public BillingRule getByLotId(Long lotId) {
        // 优先查找该停车场专属规则
        BillingRule rule = getBaseMapper().selectOne(
                new LambdaQueryWrapper<BillingRule>()
                        .eq(BillingRule::getLotId, lotId)
                        .eq(BillingRule::getStatus, 1)
                        .last("LIMIT 1")
        );
        if (rule != null) {
            return rule;
        }

        // 兜底：查找全局规则（lotId 为 null 的规则）
        return getBaseMapper().selectOne(
                new LambdaQueryWrapper<BillingRule>()
                        .isNull(BillingRule::getLotId)
                        .eq(BillingRule::getStatus, 1)
                        .last("LIMIT 1")
        );
    }
}
