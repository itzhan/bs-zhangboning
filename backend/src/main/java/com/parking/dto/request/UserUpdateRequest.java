package com.parking.dto.request;

import lombok.Data;

/**
 * 用户信息更新请求
 */
@Data
public class UserUpdateRequest {

    private String nickname;

    private String phone;

    private String email;

    private String avatar;
}
