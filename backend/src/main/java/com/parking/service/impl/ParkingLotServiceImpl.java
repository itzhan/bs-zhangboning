package com.parking.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.common.exception.BusinessException;
import com.parking.common.result.PageResult;
import com.parking.dto.request.ParkingLotRequest;
import com.parking.dto.response.ParkingLotResponse;
import com.parking.entity.BillingRule;
import com.parking.entity.ParkingLot;
import com.parking.entity.ParkingSpace;
import com.parking.mapper.BillingRuleMapper;
import com.parking.mapper.ParkingLotMapper;
import com.parking.mapper.ParkingSpaceMapper;
import com.parking.service.ParkingLotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 停车场服务实现类
 */
@Service
@RequiredArgsConstructor
public class ParkingLotServiceImpl extends ServiceImpl<ParkingLotMapper, ParkingLot> implements ParkingLotService {

    private final BillingRuleMapper billingRuleMapper;
    private final ParkingSpaceMapper parkingSpaceMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<ParkingLotResponse> getPage(int page, int size, String keyword, Integer status) {
        LambdaQueryWrapper<ParkingLot> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w
                    .like(ParkingLot::getName, keyword)
                    .or().like(ParkingLot::getAddress, keyword)
            );
        }
        if (status != null) {
            wrapper.eq(ParkingLot::getStatus, status);
        }
        wrapper.orderByDesc(ParkingLot::getCreatedAt);

        Page<ParkingLot> pageResult = page(new Page<>(page, size), wrapper);

        List<ParkingLotResponse> list = pageResult.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public ParkingLotResponse getById(Long id) {
        ParkingLot lot = getBaseMapper().selectById(id);
        if (lot == null) {
            throw new BusinessException("停车场不存在");
        }
        return convertToResponse(lot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingLotResponse create(ParkingLotRequest request) {
        ParkingLot lot = new ParkingLot();
        BeanUtil.copyProperties(request, lot);
        // 初始时可用车位 = 总车位
        lot.setAvailableSpaces(request.getTotalSpaces());
        if (lot.getStatus() == null) {
            lot.setStatus(1);
        }
        getBaseMapper().insert(lot);
        return convertToResponse(lot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingLotResponse update(Long id, ParkingLotRequest request) {
        ParkingLot lot = getBaseMapper().selectById(id);
        if (lot == null) {
            throw new BusinessException("停车场不存在");
        }
        BeanUtil.copyProperties(request, lot, "id");
        getBaseMapper().updateById(lot);
        return convertToResponse(lot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ParkingLot lot = getBaseMapper().selectById(id);
        if (lot == null) {
            throw new BusinessException("停车场不存在");
        }
        getBaseMapper().deleteById(id);
    }

    @Override
    public List<ParkingLotResponse> getAll() {
        LambdaQueryWrapper<ParkingLot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingLot::getStatus, 1);
        wrapper.orderByDesc(ParkingLot::getCreatedAt);
        List<ParkingLot> lots = getBaseMapper().selectList(wrapper);
        return lots.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshAvailableSpaces(Long lotId) {
        ParkingLot lot = getBaseMapper().selectById(lotId);
        if (lot == null) {
            return;
        }
        // status=0 表示空闲车位
        Long available = parkingSpaceMapper.selectCount(
                new LambdaQueryWrapper<ParkingSpace>()
                        .eq(ParkingSpace::getLotId, lotId)
                        .eq(ParkingSpace::getStatus, 0)
        );
        lot.setAvailableSpaces(available.intValue());
        getBaseMapper().updateById(lot);
    }

    /**
     * 实体转换为响应DTO
     */
    private ParkingLotResponse convertToResponse(ParkingLot lot) {
        ParkingLotResponse response = new ParkingLotResponse();
        BeanUtil.copyProperties(lot, response);
        if (lot.getCreatedAt() != null) {
            response.setCreatedAt(lot.getCreatedAt().format(FORMATTER));
        }
        if (lot.getUpdatedAt() != null) {
            response.setUpdatedAt(lot.getUpdatedAt().format(FORMATTER));
        }
        // 查询关联的计费规则名称
        BillingRule rule = billingRuleMapper.selectOne(
                new LambdaQueryWrapper<BillingRule>()
                        .eq(BillingRule::getLotId, lot.getId())
                        .eq(BillingRule::getStatus, 1)
                        .last("LIMIT 1")
        );
        if (rule != null) {
            response.setBillingRuleName(rule.getName());
        }
        return response;
    }
}
