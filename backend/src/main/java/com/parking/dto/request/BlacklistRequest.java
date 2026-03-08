package com.parking.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 黑名单请求
 */
@Data
public class BlacklistRequest {

    @NotBlank(message = "车牌号不能为空")
    private String plateNumber;

    private String reason;

    private Integer status;
}
