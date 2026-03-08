package com.parking.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.common.exception.BusinessException;
import com.parking.common.result.PageResult;
import com.parking.dto.request.ReservationRequest;
import com.parking.dto.response.ReservationResponse;
import com.parking.entity.ParkingLot;
import com.parking.entity.ParkingSpace;
import com.parking.entity.Reservation;
import com.parking.entity.User;
import com.parking.entity.Vehicle;
import com.parking.mapper.ParkingLotMapper;
import com.parking.mapper.ParkingSpaceMapper;
import com.parking.mapper.ReservationMapper;
import com.parking.mapper.UserMapper;
import com.parking.mapper.VehicleMapper;
import com.parking.service.BlacklistService;
import com.parking.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预约服务实现类
 */
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    private final ParkingSpaceMapper parkingSpaceMapper;
    private final ParkingLotMapper parkingLotMapper;
    private final VehicleMapper vehicleMapper;
    private final UserMapper userMapper;
    private final BlacklistService blacklistService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReservationResponse create(Long userId, ReservationRequest request) {
        // 查询车辆信息
        Vehicle vehicle = vehicleMapper.selectById(request.getVehicleId());
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }

        // 检查黑名单
        if (blacklistService.isBlacklisted(vehicle.getPlateNumber())) {
            throw new BusinessException("该车辆已被列入黑名单，无法预约");
        }

        // 查询停车场
        ParkingLot lot = parkingLotMapper.selectById(request.getLotId());
        if (lot == null) {
            throw new BusinessException("停车场不存在");
        }
        if (lot.getStatus() != null && lot.getStatus() != 1) {
            throw new BusinessException("停车场已关闭，无法预约");
        }

        // 检查时间冲突：该车辆在该时段是否已有有效预约
        Long conflictCount = getBaseMapper().selectCount(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getVehicleId, request.getVehicleId())
                        .in(Reservation::getStatus, 0, 1) // 待使用、已使用
                        .and(w -> w
                                .between(Reservation::getStartTime, request.getStartTime(), request.getEndTime())
                                .or().between(Reservation::getEndTime, request.getStartTime(), request.getEndTime())
                                .or(w2 -> w2
                                        .le(Reservation::getStartTime, request.getStartTime())
                                        .ge(Reservation::getEndTime, request.getEndTime())
                                )
                        )
        );
        if (conflictCount > 0) {
            throw new BusinessException("该车辆在该时段已有预约，请勿重复预约");
        }

        // 查找空闲车位
        ParkingSpace space = parkingSpaceMapper.selectOne(
                new LambdaQueryWrapper<ParkingSpace>()
                        .eq(ParkingSpace::getLotId, request.getLotId())
                        .eq(ParkingSpace::getStatus, 0) // 空闲
                        .last("LIMIT 1")
        );
        if (space == null) {
            throw new BusinessException("该停车场暂无可用车位");
        }

        // 创建预约
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setLotId(request.getLotId());
        reservation.setSpaceId(space.getId());
        reservation.setVehicleId(request.getVehicleId());
        reservation.setPlateNumber(vehicle.getPlateNumber());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setStatus(0); // 待使用
        getBaseMapper().insert(reservation);

        // 更新车位状态为"已预约"
        space.setStatus(2);
        parkingSpaceMapper.updateById(space);

        // 更新停车场可用车位数
        lot.setAvailableSpaces(Math.max(0, lot.getAvailableSpaces() - 1));
        parkingLotMapper.updateById(lot);

        return convertToResponse(reservation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, Long userId) {
        Reservation reservation = getBaseMapper().selectById(id);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new BusinessException("无权取消他人预约");
        }
        if (reservation.getStatus() != 0) {
            throw new BusinessException("仅待使用的预约可以取消");
        }

        // 取消预约
        reservation.setStatus(3); // 已取消
        getBaseMapper().updateById(reservation);

        // 释放车位
        ParkingSpace space = parkingSpaceMapper.selectById(reservation.getSpaceId());
        if (space != null) {
            space.setStatus(0); // 空闲
            parkingSpaceMapper.updateById(space);
        }

        // 更新停车场可用车位数
        ParkingLot lot = parkingLotMapper.selectById(reservation.getLotId());
        if (lot != null) {
            lot.setAvailableSpaces(lot.getAvailableSpaces() + 1);
            parkingLotMapper.updateById(lot);
        }
    }

    @Override
    public PageResult<ReservationResponse> getByUserId(Long userId, int page, int size) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getUserId, userId)
                .orderByDesc(Reservation::getCreatedAt);

        Page<Reservation> pageResult = page(new Page<>(page, size), wrapper);

        List<ReservationResponse> list = pageResult.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public PageResult<ReservationResponse> getPage(int page, int size, Integer status, Long lotId) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Reservation::getStatus, status);
        }
        if (lotId != null) {
            wrapper.eq(Reservation::getLotId, lotId);
        }
        wrapper.orderByDesc(Reservation::getCreatedAt);

        Page<Reservation> pageResult = page(new Page<>(page, size), wrapper);

        List<ReservationResponse> list = pageResult.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public ReservationResponse getById(Long id) {
        Reservation reservation = getBaseMapper().selectById(id);
        if (reservation == null) {
            throw new BusinessException("预约不存在");
        }
        return convertToResponse(reservation);
    }

    /**
     * 实体转换为响应DTO
     */
    private ReservationResponse convertToResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        BeanUtil.copyProperties(reservation, response);
        if (reservation.getStartTime() != null) {
            response.setStartTime(reservation.getStartTime().format(FORMATTER));
        }
        if (reservation.getEndTime() != null) {
            response.setEndTime(reservation.getEndTime().format(FORMATTER));
        }
        if (reservation.getCreatedAt() != null) {
            response.setCreatedAt(reservation.getCreatedAt().format(FORMATTER));
        }
        if (reservation.getUpdatedAt() != null) {
            response.setUpdatedAt(reservation.getUpdatedAt().format(FORMATTER));
        }

        // 车牌号 (already copied by BeanUtil from reservation.plateNumber)

        // 用户名
        if (reservation.getUserId() != null) {
            User user = userMapper.selectById(reservation.getUserId());
            if (user != null) {
                response.setUsername(user.getNickname() != null && !user.getNickname().isEmpty()
                        ? user.getNickname() : user.getUsername());
            }
        }

        // 停车场名称
        ParkingLot lot = parkingLotMapper.selectById(reservation.getLotId());
        if (lot != null) {
            response.setLotName(lot.getName());
        }
        // 车位编号
        ParkingSpace space = parkingSpaceMapper.selectById(reservation.getSpaceId());
        if (space != null) {
            response.setSpaceName(space.getSpaceNo());
        }

        return response;
    }
}
