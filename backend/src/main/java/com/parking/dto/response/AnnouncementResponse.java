package com.parking.dto.response;

import lombok.Data;

/**
 * 公告响应
 */
@Data
public class AnnouncementResponse {

    private Long id;

    private String title;

    private String content;

    private Integer type;

    private Long publisherId;

    private Integer status;

    private String createdAt;

    private String updatedAt;

    /** 发布者姓名 */
    private String publisherName;
}
