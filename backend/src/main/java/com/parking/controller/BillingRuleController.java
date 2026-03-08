package com.parking.controller;

import com.parking.common.result.PageResult;
import com.parking.common.result.Result;
import com.parking.dto.request.BillingRuleRequest;
import com.parking.entity.BillingRule;
import com.parking.service.BillingRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 计费规则控制器
 */
@Api(tags = "计费规则管理")
@RestController
@RequestMapping("/api/billing-rules")
@RequiredArgsConstructor
public class BillingRuleController {

    private final BillingRuleService billingRuleService;

    @ApiOperation("分页查询计费规则")
    @GetMapping
    public Result<PageResult<BillingRule>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<BillingRule> data = billingRuleService.getPage(page, size);
        return Result.success(data);
    }

    @ApiOperation("根据ID查询计费规则")
    @GetMapping("/{id}")
    public Result<BillingRule> getById(@PathVariable Long id) {
        BillingRule data = billingRuleService.getById(id);
        return Result.success(data);
    }

    @ApiOperation("根据停车场ID查询适用的计费规则")
    @GetMapping("/lot/{lotId}")
    public Result<BillingRule> getByLotId(@PathVariable Long lotId) {
        BillingRule data = billingRuleService.getByLotId(lotId);
        return Result.success(data);
    }

    @ApiOperation("新增计费规则")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<BillingRule> create(@RequestBody @Valid BillingRuleRequest request) {
        BillingRule data = billingRuleService.create(request);
        return Result.success(data);
    }

    @ApiOperation("更新计费规则")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<BillingRule> update(@PathVariable Long id,
                                     @RequestBody @Valid BillingRuleRequest request) {
        BillingRule data = billingRuleService.update(id, request);
        return Result.success(data);
    }

    @ApiOperation("删除计费规则")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        billingRuleService.delete(id);
        return Result.success();
    }
}
