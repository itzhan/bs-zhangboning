package com.parking.controller;

import com.parking.common.result.PageResult;
import com.parking.common.result.Result;
import com.parking.dto.request.ParkingLotRequest;
import com.parking.dto.response.ParkingLotResponse;
import com.parking.service.ParkingLotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 停车场控制器
 */
@Api(tags = "停车场管理")
@RestController
@RequestMapping("/api/parking-lots")
@RequiredArgsConstructor
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    @ApiOperation("分页查询停车场列表")
    @GetMapping
    public Result<PageResult<ParkingLotResponse>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        PageResult<ParkingLotResponse> data = parkingLotService.getPage(page, size, keyword, status);
        return Result.success(data);
    }

    @ApiOperation("获取全部停车场（下拉选择用）")
    @GetMapping("/all")
    public Result<List<ParkingLotResponse>> getAll() {
        List<ParkingLotResponse> data = parkingLotService.getAll();
        return Result.success(data);
    }

    @ApiOperation("根据ID查询停车场详情")
    @GetMapping("/{id}")
    public Result<ParkingLotResponse> getById(@PathVariable Long id) {
        ParkingLotResponse data = parkingLotService.getById(id);
        return Result.success(data);
    }

    @ApiOperation("新增停车场")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<ParkingLotResponse> create(@RequestBody @Valid ParkingLotRequest request) {
        ParkingLotResponse data = parkingLotService.create(request);
        return Result.success(data);
    }

    @ApiOperation("更新停车场")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<ParkingLotResponse> update(@PathVariable Long id,
                                             @RequestBody @Valid ParkingLotRequest request) {
        ParkingLotResponse data = parkingLotService.update(id, request);
        return Result.success(data);
    }

    @ApiOperation("删除停车场")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        parkingLotService.delete(id);
        return Result.success();
    }
}
