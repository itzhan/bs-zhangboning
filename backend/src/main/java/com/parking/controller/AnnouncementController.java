package com.parking.controller;

import com.parking.common.result.PageResult;
import com.parking.common.result.Result;
import com.parking.dto.request.AnnouncementRequest;
import com.parking.dto.response.AnnouncementResponse;
import com.parking.entity.User;
import com.parking.mapper.UserMapper;
import com.parking.service.AnnouncementService;
import com.parking.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 公告控制器
 */
@Api(tags = "公告管理")
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UserMapper userMapper;

    @ApiOperation("分页查询公告列表（管理员）")
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<PageResult<AnnouncementResponse>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        PageResult<AnnouncementResponse> data = announcementService.getPage(page, size, type, status);
        return Result.success(data);
    }

    @ApiOperation("根据ID查询公告详情")
    @GetMapping("/{id}")
    public Result<AnnouncementResponse> getById(@PathVariable Long id) {
        AnnouncementResponse data = announcementService.getById(id);
        return Result.success(data);
    }

    @ApiOperation("新增公告")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<AnnouncementResponse> create(@RequestBody @Valid AnnouncementRequest request) {
        User currentUser = userMapper.selectByUsername(SecurityUtil.getCurrentUsername());
        AnnouncementResponse data = announcementService.create(currentUser.getId(), request);
        return Result.success(data);
    }

    @ApiOperation("更新公告")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<AnnouncementResponse> update(@PathVariable Long id,
                                               @RequestBody @Valid AnnouncementRequest request) {
        AnnouncementResponse data = announcementService.update(id, request);
        return Result.success(data);
    }

    @ApiOperation("发布公告")
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> publish(@PathVariable Long id) {
        announcementService.publish(id);
        return Result.success();
    }

    @ApiOperation("删除公告")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.success();
    }
}
