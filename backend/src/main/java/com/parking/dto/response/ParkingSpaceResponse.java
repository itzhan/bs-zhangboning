package com.parking.dto.response;

import lombok.Data;

/**
 * 车位响应
 */
@Data
public class ParkingSpaceResponse {

    private Long id;

    private Long lotId;

    private String spaceNo;

    private Integer type;

    private String floor;

    private String area;

    private Integer status;

    private String createdAt;

    private String updatedAt;

    /** 停车场名称 */
    private String lotName;
}
