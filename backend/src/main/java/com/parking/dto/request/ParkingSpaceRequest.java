package com.parking.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 车位请求
 */
@Data
public class ParkingSpaceRequest {

    @NotNull(message = "停车场ID不能为空")
    private Long lotId;

    @NotBlank(message = "车位编号不能为空")
    private String spaceNo;

    private Integer type;

    private String floor;

    private String area;

    private Integer status;
}
