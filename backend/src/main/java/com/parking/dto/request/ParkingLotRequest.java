package com.parking.dto.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 停车场请求
 */
@Data
public class ParkingLotRequest {

    @NotBlank(message = "停车场名称不能为空")
    private String name;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    @NotNull(message = "总车位数不能为空")
    @Min(value = 1, message = "总车位数至少为1")
    private Integer totalSpaces;

    private String image;

    private String description;

    private String openTime;

    private String closeTime;

    private String contactPhone;

    private Integer status;
}
