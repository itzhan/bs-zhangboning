package com.parking.dto.response;

import lombok.Data;

/**
 * 用户信息响应
 */
@Data
public class UserResponse {

    private Long id;

    private String username;

    private String nickname;

    private String phone;

    private String email;

    private String avatar;

    private String role;

    private Integer status;

    private String createdAt;
}
