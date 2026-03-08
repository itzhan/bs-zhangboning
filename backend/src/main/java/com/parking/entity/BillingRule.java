package com.parking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("billing_rule")
public class BillingRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long lotId;

    private String name;

    private Integer ruleType;

    private BigDecimal firstHourPrice;

    private BigDecimal extraHourPrice;

    private BigDecimal dailyMax;

    private Integer freeMinutes;

    private BigDecimal flatPrice;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
