package com.parking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("parking_space")
public class ParkingSpace {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long lotId;

    private String spaceNo;

    private Integer type;

    private String floor;

    private String area;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
