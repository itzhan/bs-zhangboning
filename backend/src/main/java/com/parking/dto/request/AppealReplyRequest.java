package com.parking.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 申诉回复请求
 */
@Data
public class AppealReplyRequest {

    /** 处理状态：2=通过，3=驳回 */
    @NotNull(message = "处理状态不能为空")
    private Integer status;

    @NotBlank(message = "回复内容不能为空")
    private String reply;
}
