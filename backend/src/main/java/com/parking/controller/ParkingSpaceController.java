package com.parking.controller;

import com.parking.common.result.PageResult;
import com.parking.common.result.Result;
import com.parking.dto.request.ParkingSpaceRequest;
import com.parking.dto.response.ParkingSpaceResponse;
import com.parking.service.ParkingSpaceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 车位控制器
 */
@Api(tags = "车位管理")
@RestController
@RequestMapping("/api/parking-spaces")
@RequiredArgsConstructor
public class ParkingSpaceController {

    private final ParkingSpaceService parkingSpaceService;

    @ApiOperation("分页查询车位列表")
    @GetMapping
    public Result<PageResult<ParkingSpaceResponse>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long lotId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type) {
        PageResult<ParkingSpaceResponse> data = parkingSpaceService.getPage(page, size, lotId, status, type);
        return Result.success(data);
    }

    @ApiOperation("根据ID查询车位详情")
    @GetMapping("/{id}")
    public Result<ParkingSpaceResponse> getById(@PathVariable Long id) {
        ParkingSpaceResponse data = parkingSpaceService.getById(id);
        return Result.success(data);
    }

    @ApiOperation("获取停车场空闲车位列表")
    @GetMapping("/available/{lotId}")
    public Result<List<ParkingSpaceResponse>> getAvailable(@PathVariable Long lotId) {
        List<ParkingSpaceResponse> data = parkingSpaceService.getAvailableByLotId(lotId);
        return Result.success(data);
    }

    @ApiOperation("新增车位")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<ParkingSpaceResponse> create(@RequestBody @Valid ParkingSpaceRequest request) {
        ParkingSpaceResponse data = parkingSpaceService.create(request);
        return Result.success(data);
    }

    @ApiOperation("批量创建车位")
    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> batchCreate(@RequestParam Long lotId,
                                    @RequestParam String prefix,
                                    @RequestParam int count,
                                    @RequestParam Integer type,
                                    @RequestParam(required = false) String floor,
                                    @RequestParam(required = false) String area) {
        parkingSpaceService.batchCreate(lotId, prefix, count, type, floor, area);
        return Result.success();
    }

    @ApiOperation("更新车位")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<ParkingSpaceResponse> update(@PathVariable Long id,
                                               @RequestBody @Valid ParkingSpaceRequest request) {
        ParkingSpaceResponse data = parkingSpaceService.update(id, request);
        return Result.success(data);
    }

    @ApiOperation("删除车位")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        parkingSpaceService.delete(id);
        return Result.success();
    }
}
