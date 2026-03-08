package com.parking.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 停车入场请求（扫码入场）
 */
@Data
public class ParkingEntryRequest {

    @NotNull(message = "停车场ID不能为空")
    private Long lotId;

    @NotBlank(message = "车牌号不能为空")
    private String plateNumber;
}
