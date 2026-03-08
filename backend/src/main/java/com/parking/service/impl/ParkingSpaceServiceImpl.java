package com.parking.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.common.exception.BusinessException;
import com.parking.common.result.PageResult;
import com.parking.dto.request.ParkingSpaceRequest;
import com.parking.dto.response.ParkingSpaceResponse;
import com.parking.entity.ParkingLot;
import com.parking.entity.ParkingSpace;
import com.parking.mapper.ParkingLotMapper;
import com.parking.mapper.ParkingSpaceMapper;
import com.parking.service.ParkingSpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 车位服务实现类
 */
@Service
@RequiredArgsConstructor
public class ParkingSpaceServiceImpl extends ServiceImpl<ParkingSpaceMapper, ParkingSpace> implements ParkingSpaceService {

    private final ParkingLotMapper parkingLotMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<ParkingSpaceResponse> getPage(int page, int size, Long lotId, Integer status, Integer type) {
        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<>();

        if (lotId != null) {
            wrapper.eq(ParkingSpace::getLotId, lotId);
        }
        if (status != null) {
            wrapper.eq(ParkingSpace::getStatus, status);
        }
        if (type != null) {
            wrapper.eq(ParkingSpace::getType, type);
        }
        wrapper.orderByAsc(ParkingSpace::getSpaceNo);

        Page<ParkingSpace> pageResult = page(new Page<>(page, size), wrapper);

        List<ParkingSpaceResponse> list = pageResult.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public ParkingSpaceResponse getById(Long id) {
        ParkingSpace space = getBaseMapper().selectById(id);
        if (space == null) {
            throw new BusinessException("车位不存在");
        }
        return convertToResponse(space);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingSpaceResponse create(ParkingSpaceRequest request) {
        ParkingLot lot = parkingLotMapper.selectById(request.getLotId());
        if (lot == null) {
            throw new BusinessException("停车场不存在");
        }

        ParkingSpace space = new ParkingSpace();
        BeanUtil.copyProperties(request, space);
        if (space.getStatus() == null) {
            space.setStatus(0); // 默认空闲
        }
        if (space.getType() == null) {
            space.setType(1); // 默认普通车位
        }
        getBaseMapper().insert(space);

        // 增加停车场总车位数和可用车位数
        lot.setTotalSpaces(lot.getTotalSpaces() + 1);
        lot.setAvailableSpaces(lot.getAvailableSpaces() + 1);
        parkingLotMapper.updateById(lot);

        return convertToResponse(space);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ParkingSpaceResponse update(Long id, ParkingSpaceRequest request) {
        ParkingSpace space = getBaseMapper().selectById(id);
        if (space == null) {
            throw new BusinessException("车位不存在");
        }
        BeanUtil.copyProperties(request, space, "id");
        getBaseMapper().updateById(space);
        return convertToResponse(space);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ParkingSpace space = getBaseMapper().selectById(id);
        if (space == null) {
            throw new BusinessException("车位不存在");
        }

        // 硬删除
        getBaseMapper().deleteById(id);

        // 减少停车场总车位数
        ParkingLot lot = parkingLotMapper.selectById(space.getLotId());
        if (lot != null) {
            lot.setTotalSpaces(Math.max(0, lot.getTotalSpaces() - 1));
            // 如果车位是空闲的，同时减少可用车位数
            if (space.getStatus() != null && space.getStatus() == 0) {
                lot.setAvailableSpaces(Math.max(0, lot.getAvailableSpaces() - 1));
            }
            parkingLotMapper.updateById(lot);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreate(Long lotId, String prefix, int count, Integer type, String floor, String area) {
        ParkingLot lot = parkingLotMapper.selectById(lotId);
        if (lot == null) {
            throw new BusinessException("停车场不存在");
        }

        List<ParkingSpace> spaces = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            ParkingSpace space = new ParkingSpace();
            space.setLotId(lotId);
            space.setSpaceNo(prefix + "-" + String.format("%03d", i));
            space.setType(type != null ? type : 1);
            space.setFloor(floor);
            space.setArea(area);
            space.setStatus(0); // 空闲
            spaces.add(space);
        }
        saveBatch(spaces);

        // 更新停车场车位数
        lot.setTotalSpaces(lot.getTotalSpaces() + count);
        lot.setAvailableSpaces(lot.getAvailableSpaces() + count);
        parkingLotMapper.updateById(lot);
    }

    @Override
    public List<ParkingSpaceResponse> getAvailableByLotId(Long lotId) {
        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingSpace::getLotId, lotId)
                .eq(ParkingSpace::getStatus, 0) // 空闲
                .orderByAsc(ParkingSpace::getSpaceNo);

        List<ParkingSpace> spaces = getBaseMapper().selectList(wrapper);
        return spaces.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    /**
     * 实体转换为响应DTO
     */
    private ParkingSpaceResponse convertToResponse(ParkingSpace space) {
        ParkingSpaceResponse response = new ParkingSpaceResponse();
        BeanUtil.copyProperties(space, response);
        if (space.getCreatedAt() != null) {
            response.setCreatedAt(space.getCreatedAt().format(FORMATTER));
        }
        if (space.getUpdatedAt() != null) {
            response.setUpdatedAt(space.getUpdatedAt().format(FORMATTER));
        }
        // 查询停车场名称
        ParkingLot lot = parkingLotMapper.selectById(space.getLotId());
        if (lot != null) {
            response.setLotName(lot.getName());
        }
        return response;
    }
}
