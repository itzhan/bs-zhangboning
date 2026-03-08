package com.parking.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.common.exception.BusinessException;
import com.parking.common.result.PageResult;
import com.parking.dto.request.BlacklistRequest;
import com.parking.entity.Blacklist;
import com.parking.mapper.BlacklistMapper;
import com.parking.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 黑名单服务实现类
 */
@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl extends ServiceImpl<BlacklistMapper, Blacklist> implements BlacklistService {

    @Override
    public PageResult<Blacklist> getPage(int page, int size, String keyword) {
        LambdaQueryWrapper<Blacklist> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Blacklist::getPlateNumber, keyword);
        }
        wrapper.orderByDesc(Blacklist::getCreatedAt);

        Page<Blacklist> pageResult = page(new Page<>(page, size), wrapper);
        return PageResult.of(pageResult);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Blacklist create(Long operatorId, BlacklistRequest request) {
        // 检查是否已在黑名单中
        Long count = getBaseMapper().selectCount(
                new LambdaQueryWrapper<Blacklist>()
                        .eq(Blacklist::getPlateNumber, request.getPlateNumber())
                        .eq(Blacklist::getStatus, 1)
        );
        if (count > 0) {
            throw new BusinessException("该车牌号已在黑名单中");
        }

        Blacklist blacklist = new Blacklist();
        BeanUtil.copyProperties(request, blacklist);
        blacklist.setOperatorId(operatorId);
        if (blacklist.getStatus() == null) {
            blacklist.setStatus(1);
        }
        getBaseMapper().insert(blacklist);
        return blacklist;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Blacklist update(Long id, BlacklistRequest request) {
        Blacklist blacklist = getBaseMapper().selectById(id);
        if (blacklist == null) {
            throw new BusinessException("黑名单记录不存在");
        }
        BeanUtil.copyProperties(request, blacklist, "id");
        getBaseMapper().updateById(blacklist);
        return blacklist;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Blacklist blacklist = getBaseMapper().selectById(id);
        if (blacklist == null) {
            throw new BusinessException("黑名单记录不存在");
        }
        getBaseMapper().deleteById(id);
    }

    @Override
    public boolean isBlacklisted(String plateNumber) {
        Long count = getBaseMapper().selectCount(
                new LambdaQueryWrapper<Blacklist>()
                        .eq(Blacklist::getPlateNumber, plateNumber)
                        .eq(Blacklist::getStatus, 1)
        );
        return count > 0;
    }
}
