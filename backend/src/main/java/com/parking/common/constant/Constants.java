package com.parking.common.constant;

/**
 * 系统常量
 */
public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }

    // ==================== 角色常量 ====================

    /** 管理员角色 */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /** 普通用户角色 */
    public static final String ROLE_USER = "ROLE_USER";

    // ==================== 状态常量 ====================

    /** 启用 */
    public static final int STATUS_ENABLE = 1;

    /** 禁用 */
    public static final int STATUS_DISABLE = 0;

    // ==================== 车位状态 ====================

    /** 空闲 */
    public static final int SPOT_FREE = 0;

    /** 已占用 */
    public static final int SPOT_OCCUPIED = 1;

    /** 已预约 */
    public static final int SPOT_RESERVED = 2;

    /** 维护中 */
    public static final int SPOT_MAINTENANCE = 3;

    // ==================== 订单状态 ====================

    /** 待支付 */
    public static final int ORDER_PENDING = 0;

    /** 停车中 */
    public static final int ORDER_PARKING = 1;

    /** 已完成 */
    public static final int ORDER_COMPLETED = 2;

    /** 已取消 */
    public static final int ORDER_CANCELLED = 3;

    // ==================== 通用常量 ====================

    /** JWT Token 前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** Authorization 请求头 */
    public static final String AUTH_HEADER = "Authorization";

    /** 默认分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 最大分页大小 */
    public static final int MAX_PAGE_SIZE = 100;
}
