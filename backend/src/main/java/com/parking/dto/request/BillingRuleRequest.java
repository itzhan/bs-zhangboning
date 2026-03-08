package com.parking.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 计费规则请求
 */
@Data
public class BillingRuleRequest {

    private Long lotId;

    @NotBlank(message = "规则名称不能为空")
    private String name;

    @NotNull(message = "规则类型不能为空")
    private Integer ruleType;

    private BigDecimal firstHourPrice;

    private BigDecimal extraHourPrice;

    private BigDecimal dailyMax;

    private Integer freeMinutes;

    private BigDecimal flatPrice;

    private Integer status;
}
