package com.parking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("parking_order")
public class ParkingOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long lotId;

    private Long spaceId;

    private Long vehicleId;

    private String plateNumber;

    private Long reservationId;

    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    private Integer duration;

    private BigDecimal amount;

    private BigDecimal discountAmount;

    private BigDecimal actualAmount;

    private Integer paymentStatus;

    private Integer orderStatus;

    private String entryCode;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
