package com.parking.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 车辆请求
 */
@Data
public class VehicleRequest {

    @NotBlank(message = "车牌号不能为空")
    private String plateNumber;

    private String brand;

    private String model;

    private String color;

    private Integer isDefault;
}
