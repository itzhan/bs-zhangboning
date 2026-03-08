package com.parking.dto.response;

import lombok.Data;

/**
 * 申诉响应
 */
@Data
public class AppealResponse {

    private Long id;

    private Long userId;

    private Long orderId;

    private Integer type;

    private String content;

    private String images;

    private Integer status;

    private String reply;

    private String createdAt;

    private String updatedAt;

    /** 用户名 */
    private String username;

    /** 关联订单号 */
    private String orderNo;
}
