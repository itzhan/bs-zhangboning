package com.parking.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 申诉请求
 */
@Data
public class AppealRequest {

    private Long orderId;

    @NotNull(message = "申诉类型不能为空")
    private Integer type;

    @NotBlank(message = "申诉内容不能为空")
    private String content;

    private String images;
}
