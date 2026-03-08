package com.parking.dto.response;

import lombok.Data;

/**
 * 车辆响应
 */
@Data
public class VehicleResponse {

    private Long id;

    private Long userId;

    private String plateNumber;

    private String brand;

    private String model;

    private String color;

    private Integer isDefault;

    private String createdAt;

    private String updatedAt;
}
