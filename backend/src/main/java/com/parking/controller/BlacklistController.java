package com.parking.controller;

import com.parking.common.result.PageResult;
import com.parking.common.result.Result;
import com.parking.dto.request.BlacklistRequest;
import com.parking.entity.Blacklist;
import com.parking.entity.User;
import com.parking.mapper.UserMapper;
import com.parking.service.BlacklistService;
import com.parking.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 黑名单控制器
 */
@Api(tags = "黑名单管理")
@RestController
@RequestMapping("/api/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;
    private final UserMapper userMapper;

    @ApiOperation("分页查询黑名单")
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<PageResult<Blacklist>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<Blacklist> data = blacklistService.getPage(page, size, keyword);
        return Result.success(data);
    }

    @ApiOperation("新增黑名单")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Blacklist> create(@RequestBody @Valid BlacklistRequest request) {
        User currentUser = userMapper.selectByUsername(SecurityUtil.getCurrentUsername());
        Blacklist data = blacklistService.create(currentUser.getId(), request);
        return Result.success(data);
    }

    @ApiOperation("更新黑名单")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Blacklist> update(@PathVariable Long id,
                                    @RequestBody @Valid BlacklistRequest request) {
        Blacklist data = blacklistService.update(id, request);
        return Result.success(data);
    }

    @ApiOperation("删除黑名单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        blacklistService.delete(id);
        return Result.success();
    }
}
