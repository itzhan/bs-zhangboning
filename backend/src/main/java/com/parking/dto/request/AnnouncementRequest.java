package com.parking.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 公告请求
 */
@Data
public class AnnouncementRequest {

    @NotBlank(message = "公告标题不能为空")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    private String content;

    private Integer type;

    private Integer status;
}
