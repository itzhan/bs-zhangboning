package com.parking.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.common.exception.BusinessException;
import com.parking.dto.request.VehicleRequest;
import com.parking.dto.response.VehicleResponse;
import com.parking.entity.Vehicle;
import com.parking.mapper.VehicleMapper;
import com.parking.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 车辆服务实现类
 */
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements VehicleService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<VehicleResponse> getByUserId(Long userId) {
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Vehicle::getUserId, userId)
                .orderByDesc(Vehicle::getIsDefault)
                .orderByDesc(Vehicle::getCreatedAt);
        List<Vehicle> vehicles = getBaseMapper().selectList(wrapper);
        return vehicles.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public VehicleResponse getById(Long id) {
        Vehicle vehicle = getBaseMapper().selectById(id);
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }
        return convertToResponse(vehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VehicleResponse create(Long userId, VehicleRequest request) {
        // 检查车牌号唯一性
        Long count = getBaseMapper().selectCount(
                new LambdaQueryWrapper<Vehicle>().eq(Vehicle::getPlateNumber, request.getPlateNumber())
        );
        if (count > 0) {
            throw new BusinessException("该车牌号已被注册");
        }

        Vehicle vehicle = new Vehicle();
        BeanUtil.copyProperties(request, vehicle);
        vehicle.setUserId(userId);

        // 如果设置为默认车辆，先将该用户其他车辆取消默认
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            clearDefaultVehicle(userId);
        }
        if (vehicle.getIsDefault() == null) {
            // 如果用户没有车辆，则自动设为默认
            Long vehicleCount = getBaseMapper().selectCount(
                    new LambdaQueryWrapper<Vehicle>().eq(Vehicle::getUserId, userId)
            );
            vehicle.setIsDefault(vehicleCount == 0 ? 1 : 0);
        }

        getBaseMapper().insert(vehicle);
        return convertToResponse(vehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VehicleResponse update(Long id, VehicleRequest request) {
        Vehicle vehicle = getBaseMapper().selectById(id);
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }

        // 如果修改了车牌号，检查唯一性
        if (request.getPlateNumber() != null && !request.getPlateNumber().equals(vehicle.getPlateNumber())) {
            Long count = getBaseMapper().selectCount(
                    new LambdaQueryWrapper<Vehicle>().eq(Vehicle::getPlateNumber, request.getPlateNumber())
            );
            if (count > 0) {
                throw new BusinessException("该车牌号已被注册");
            }
        }

        // 如果设置为默认车辆，先将该用户其他车辆取消默认
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            clearDefaultVehicle(vehicle.getUserId());
        }

        BeanUtil.copyProperties(request, vehicle, "id", "userId");
        getBaseMapper().updateById(vehicle);
        return convertToResponse(vehicle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Vehicle vehicle = getBaseMapper().selectById(id);
        if (vehicle == null) {
            throw new BusinessException("车辆不存在");
        }
        getBaseMapper().deleteById(id);
    }

    /**
     * 清除用户的默认车辆标记
     */
    private void clearDefaultVehicle(Long userId) {
        List<Vehicle> vehicles = getBaseMapper().selectList(
                new LambdaQueryWrapper<Vehicle>()
                        .eq(Vehicle::getUserId, userId)
                        .eq(Vehicle::getIsDefault, 1)
        );
        for (Vehicle v : vehicles) {
            v.setIsDefault(0);
            getBaseMapper().updateById(v);
        }
    }

    /**
     * 实体转换为响应DTO
     */
    private VehicleResponse convertToResponse(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        BeanUtil.copyProperties(vehicle, response);
        if (vehicle.getCreatedAt() != null) {
            response.setCreatedAt(vehicle.getCreatedAt().format(FORMATTER));
        }
        if (vehicle.getUpdatedAt() != null) {
            response.setUpdatedAt(vehicle.getUpdatedAt().format(FORMATTER));
        }
        return response;
    }
}
