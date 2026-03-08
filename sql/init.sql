-- ======================================================
-- 景区停车引导系统 - 数据库初始化脚本
-- 数据库: parking_system
-- ======================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS `parking_system` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `parking_system`;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(200) NOT NULL COMMENT '密码（BCrypt加密）',
    `nickname` VARCHAR(50) DEFAULT '' COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT '' COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    `avatar` VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN-管理员, USER-游客',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间（软删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- 2. 停车场表
-- ----------------------------
DROP TABLE IF EXISTS `parking_lot`;
CREATE TABLE `parking_lot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '停车场ID',
    `name` VARCHAR(100) NOT NULL COMMENT '停车场名称',
    `address` VARCHAR(300) DEFAULT '' COMMENT '详细地址',
    `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
    `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
    `total_spaces` INT NOT NULL DEFAULT 0 COMMENT '总车位数',
    `available_spaces` INT NOT NULL DEFAULT 0 COMMENT '可用车位数',
    `image` VARCHAR(500) DEFAULT '' COMMENT '停车场图片',
    `description` TEXT COMMENT '描述',
    `open_time` VARCHAR(20) DEFAULT '00:00' COMMENT '开放时间',
    `close_time` VARCHAR(20) DEFAULT '23:59' COMMENT '关闭时间',
    `contact_phone` VARCHAR(20) DEFAULT '' COMMENT '联系电话',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-关闭, 1-运营中, 2-维护中',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间（软删除）',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='停车场表';

-- ----------------------------
-- 3. 车位表
-- ----------------------------
DROP TABLE IF EXISTS `parking_space`;
CREATE TABLE `parking_space` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '车位ID',
    `lot_id` BIGINT NOT NULL COMMENT '所属停车场ID',
    `space_no` VARCHAR(20) NOT NULL COMMENT '车位编号',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型: 1-普通, 2-VIP, 3-残疾人, 4-充电桩',
    `floor` VARCHAR(20) DEFAULT '' COMMENT '楼层/区域',
    `area` VARCHAR(50) DEFAULT '' COMMENT '区域描述',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-空闲, 1-已占用, 2-已预约, 3-维护中',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_lot_id` (`lot_id`),
    KEY `idx_status` (`status`),
    UNIQUE KEY `uk_lot_space` (`lot_id`, `space_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车位表';

-- ----------------------------
-- 4. 车辆表
-- ----------------------------
DROP TABLE IF EXISTS `vehicle`;
CREATE TABLE `vehicle` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '车辆ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `plate_number` VARCHAR(20) NOT NULL COMMENT '车牌号',
    `brand` VARCHAR(50) DEFAULT '' COMMENT '品牌',
    `model` VARCHAR(50) DEFAULT '' COMMENT '车型',
    `color` VARCHAR(20) DEFAULT '' COMMENT '颜色',
    `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认车辆: 0-否, 1-是',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    UNIQUE KEY `uk_plate` (`plate_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车辆表';

-- ----------------------------
-- 5. 预约表
-- ----------------------------
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `lot_id` BIGINT NOT NULL COMMENT '停车场ID',
    `space_id` BIGINT DEFAULT NULL COMMENT '车位ID',
    `vehicle_id` BIGINT NOT NULL COMMENT '车辆ID',
    `plate_number` VARCHAR(20) NOT NULL COMMENT '车牌号',
    `start_time` DATETIME NOT NULL COMMENT '预约开始时间',
    `end_time` DATETIME NOT NULL COMMENT '预约结束时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待生效, 1-已生效, 2-已完成, 3-已取消, 4-已过期',
    `cancel_reason` VARCHAR(200) DEFAULT '' COMMENT '取消原因',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_lot_id` (`lot_id`),
    KEY `idx_status` (`status`),
    KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';

-- ----------------------------
-- 6. 停车订单表
-- ----------------------------
DROP TABLE IF EXISTS `parking_order`;
CREATE TABLE `parking_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `lot_id` BIGINT NOT NULL COMMENT '停车场ID',
    `space_id` BIGINT DEFAULT NULL COMMENT '车位ID',
    `vehicle_id` BIGINT DEFAULT NULL COMMENT '车辆ID',
    `plate_number` VARCHAR(20) NOT NULL COMMENT '车牌号',
    `reservation_id` BIGINT DEFAULT NULL COMMENT '关联预约ID',
    `entry_time` DATETIME DEFAULT NULL COMMENT '入场时间',
    `exit_time` DATETIME DEFAULT NULL COMMENT '出场时间',
    `duration` INT DEFAULT 0 COMMENT '停车时长（分钟）',
    `amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '应付金额',
    `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    `actual_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '实付金额',
    `payment_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态: 0-未支付, 1-已支付, 2-已退款',
    `order_status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态: 0-已入场, 1-已出场, 2-已完成, 3-异常',
    `entry_code` VARCHAR(200) DEFAULT '' COMMENT '入场二维码',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_lot_id` (`lot_id`),
    KEY `idx_plate_number` (`plate_number`),
    KEY `idx_entry_time` (`entry_time`),
    KEY `idx_order_status` (`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='停车订单表';

-- ----------------------------
-- 7. 支付流水表
-- ----------------------------
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支付ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `payment_no` VARCHAR(32) NOT NULL COMMENT '支付流水号',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `payment_method` TINYINT NOT NULL DEFAULT 1 COMMENT '支付方式: 1-微信, 2-支付宝, 3-现金',
    `payment_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态: 0-待支付, 1-支付成功, 2-支付失败, 3-已退款',
    `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付流水表';

-- ----------------------------
-- 8. 计费规则表
-- ----------------------------
DROP TABLE IF EXISTS `billing_rule`;
CREATE TABLE `billing_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `lot_id` BIGINT DEFAULT NULL COMMENT '停车场ID（NULL表示全局规则）',
    `name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_type` TINYINT NOT NULL DEFAULT 1 COMMENT '规则类型: 1-按时计费, 2-按次计费',
    `first_hour_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '首小时价格',
    `extra_hour_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '超出每小时价格',
    `daily_max` DECIMAL(10,2) DEFAULT 0.00 COMMENT '每日最高收费',
    `free_minutes` INT DEFAULT 15 COMMENT '免费时长（分钟）',
    `flat_price` DECIMAL(10,2) DEFAULT 0.00 COMMENT '按次收费金额',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_lot_id` (`lot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计费规则表';

-- ----------------------------
-- 9. 优惠券表
-- ----------------------------
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
    `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型: 1-满减, 2-折扣, 3-免费时长',
    `discount_value` DECIMAL(10,2) NOT NULL COMMENT '优惠值（满减金额/折扣比例/免费分钟数）',
    `min_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '最低使用金额',
    `start_time` DATETIME NOT NULL COMMENT '生效时间',
    `end_time` DATETIME NOT NULL COMMENT '失效时间',
    `total_count` INT NOT NULL DEFAULT 0 COMMENT '发放总量',
    `used_count` INT NOT NULL DEFAULT 0 COMMENT '已使用数量',
    `remaining_count` INT NOT NULL DEFAULT 0 COMMENT '剩余数量',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- ----------------------------
-- 10. 用户优惠券表
-- ----------------------------
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-未使用, 1-已使用, 2-已过期',
    `used_time` DATETIME DEFAULT NULL COMMENT '使用时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';

-- ----------------------------
-- 11. 黑名单表
-- ----------------------------
DROP TABLE IF EXISTS `blacklist`;
CREATE TABLE `blacklist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `plate_number` VARCHAR(20) NOT NULL COMMENT '车牌号',
    `reason` VARCHAR(300) DEFAULT '' COMMENT '拉黑原因',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-已解除, 1-生效中',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_plate_number` (`plate_number`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名单表';

-- ----------------------------
-- 12. 异常申诉表
-- ----------------------------
DROP TABLE IF EXISTS `appeal`;
CREATE TABLE `appeal` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申诉ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型: 1-费用异常, 2-入场异常, 3-出场异常, 4-其他',
    `content` TEXT NOT NULL COMMENT '申诉内容',
    `images` VARCHAR(1000) DEFAULT '' COMMENT '图片URL（逗号分隔）',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理, 1-处理中, 2-已通过, 3-已驳回',
    `reply` TEXT COMMENT '回复内容',
    `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异常申诉表';

-- ----------------------------
-- 13. 公告表
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT NOT NULL COMMENT '内容',
    `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型: 1-系统公告, 2-停车提醒, 3-优惠活动',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-草稿, 1-已发布, 2-已下架',
    `publisher_id` BIGINT DEFAULT NULL COMMENT '发布人ID',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ----------------------------
-- 14. 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
    `username` VARCHAR(50) DEFAULT '' COMMENT '操作用户名',
    `module` VARCHAR(50) DEFAULT '' COMMENT '操作模块',
    `operation` VARCHAR(100) DEFAULT '' COMMENT '操作描述',
    `method` VARCHAR(200) DEFAULT '' COMMENT '请求方法',
    `params` TEXT COMMENT '请求参数',
    `ip` VARCHAR(50) DEFAULT '' COMMENT 'IP地址',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-失败, 1-成功',
    `error_msg` TEXT COMMENT '错误信息',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
