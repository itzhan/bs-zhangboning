package com.parking.controller;

import com.parking.common.result.Result;
import com.parking.dto.request.VehicleRequest;
import com.parking.dto.response.VehicleResponse;
import com.parking.entity.User;
import com.parking.mapper.UserMapper;
import com.parking.service.VehicleService;
import com.parking.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 车辆控制器
 */
@Api(tags = "车辆管理")
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final UserMapper userMapper;

    @ApiOperation("查询我的车辆列表")
    @GetMapping
    public Result<List<VehicleResponse>> getMyVehicles() {
        User currentUser = userMapper.selectByUsername(SecurityUtil.getCurrentUsername());
        List<VehicleResponse> data = vehicleService.getByUserId(currentUser.getId());
        return Result.success(data);
    }

    @ApiOperation("根据ID查询车辆")
    @GetMapping("/{id}")
    public Result<VehicleResponse> getById(@PathVariable Long id) {
        VehicleResponse data = vehicleService.getById(id);
        return Result.success(data);
    }

    @ApiOperation("新增车辆")
    @PostMapping
    public Result<VehicleResponse> create(@RequestBody @Valid VehicleRequest request) {
        User currentUser = userMapper.selectByUsername(SecurityUtil.getCurrentUsername());
        VehicleResponse data = vehicleService.create(currentUser.getId(), request);
        return Result.success(data);
    }

    @ApiOperation("更新车辆")
    @PutMapping("/{id}")
    public Result<VehicleResponse> update(@PathVariable Long id,
                                          @RequestBody @Valid VehicleRequest request) {
        VehicleResponse data = vehicleService.update(id, request);
        return Result.success(data);
    }

    @ApiOperation("删除车辆")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return Result.success();
    }
}
