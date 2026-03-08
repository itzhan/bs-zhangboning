package com.parking.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.common.exception.BusinessException;
import com.parking.common.result.PageResult;
import com.parking.dto.request.ParkingEntryRequest;
import com.parking.dto.request.ParkingExitRequest;
import com.parking.dto.response.OrderResponse;
import com.parking.entity.*;
import com.parking.mapper.*;
import com.parking.service.BillingRuleService;
import com.parking.service.ParkingOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 停车订单服务实现类
 */
@Service
@RequiredArgsConstructor
public class ParkingOrderServiceImpl extends ServiceImpl<ParkingOrderMapper, ParkingOrder> implements ParkingOrderService {

    private final ParkingSpaceMapper parkingSpaceMapper;
    private final ParkingLotMapper parkingLotMapper;
    private final UserMapper userMapper;
    private final BillingRuleService billingRuleService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse entry(Long userId, ParkingEntryRequest request) {
        ParkingLot lot = parkingLotMapper.selectById(request.getLotId());
        if (lot == null) {
            throw new BusinessException("停车场不存在");
        }
        if (lot.getStatus() != null && lot.getStatus() != 1) {
            throw new BusinessException("停车场已关闭");
        }

        // 查找空闲车位
        ParkingSpace space = parkingSpaceMapper.selectOne(
                new LambdaQueryWrapper<ParkingSpace>()
                        .eq(ParkingSpace::getLotId, request.getLotId())
                        .eq(ParkingSpace::getStatus, 0) // 空闲
                        .last("LIMIT 1")
        );
        if (space == null) {
            throw new BusinessException("该停车场暂无可用车位");
        }

        // 生成订单号：PO + yyyyMMdd + 3位序号
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        Long todayCount = getBaseMapper().selectCount(
                new LambdaQueryWrapper<ParkingOrder>()
                        .likeRight(ParkingOrder::getOrderNo, "PO" + dateStr)
        );
        String orderNo = "PO" + dateStr + String.format("%03d", todayCount + 1);

        // 创建订单
        ParkingOrder order = new ParkingOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setLotId(request.getLotId());
        order.setSpaceId(space.getId());
        order.setPlateNumber(request.getPlateNumber());
        order.setEntryTime(LocalDateTime.now());
        order.setOrderStatus(0); // 停车中
        order.setPaymentStatus(0); // 未支付
        order.setEntryCode(cn.hutool.core.util.IdUtil.simpleUUID());
        getBaseMapper().insert(order);

        // 更新车位状态为"占用"
        space.setStatus(1);
        parkingSpaceMapper.updateById(space);

        // 更新停车场可用车位数
        lot.setAvailableSpaces(Math.max(0, lot.getAvailableSpaces() - 1));
        parkingLotMapper.updateById(lot);

        return convertToResponse(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse exit(ParkingExitRequest request) {
        ParkingOrder order = getBaseMapper().selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() != 0) {
            throw new BusinessException("订单状态异常，无法出场");
        }

        // 设置出场时间
        LocalDateTime exitTime = LocalDateTime.now();
        order.setExitTime(exitTime);

        // 计算停车时长（分钟）
        long durationMinutes = Duration.between(order.getEntryTime(), exitTime).toMinutes();
        order.setDuration((int) durationMinutes);

        // 计算费用
        BigDecimal fee = calculateFee(order.getLotId(), durationMinutes);
        order.setAmount(fee);
        order.setActualAmount(fee);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setOrderStatus(1); // 待支付
        getBaseMapper().updateById(order);

        // 释放车位
        ParkingSpace space = parkingSpaceMapper.selectById(order.getSpaceId());
        if (space != null) {
            space.setStatus(0); // 空闲
            parkingSpaceMapper.updateById(space);
        }

        // 更新停车场可用车位数
        ParkingLot lot = parkingLotMapper.selectById(order.getLotId());
        if (lot != null) {
            lot.setAvailableSpaces(lot.getAvailableSpaces() + 1);
            parkingLotMapper.updateById(lot);
        }

        return convertToResponse(order);
    }

    @Override
    public PageResult<OrderResponse> getByUserId(Long userId, int page, int size) {
        LambdaQueryWrapper<ParkingOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingOrder::getUserId, userId)
                .orderByDesc(ParkingOrder::getCreatedAt);

        Page<ParkingOrder> pageResult = page(new Page<>(page, size), wrapper);

        List<OrderResponse> list = pageResult.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public PageResult<OrderResponse> getPage(int page, int size, Integer status, Long lotId, String keyword) {
        LambdaQueryWrapper<ParkingOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ParkingOrder::getOrderStatus, status);
        }
        if (lotId != null) {
            wrapper.eq(ParkingOrder::getLotId, lotId);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w
                    .like(ParkingOrder::getOrderNo, keyword)
                    .or().like(ParkingOrder::getPlateNumber, keyword)
            );
        }
        wrapper.orderByDesc(ParkingOrder::getCreatedAt);

        Page<ParkingOrder> pageResult = page(new Page<>(page, size), wrapper);

        List<OrderResponse> list = pageResult.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public OrderResponse getById(Long id) {
        ParkingOrder order = getBaseMapper().selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return convertToResponse(order);
    }

    @Override
    public String generateEntryQrCode(Long orderId) {
        ParkingOrder order = getBaseMapper().selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        // 返回二维码数据字符串（订单号 + 入场码）
        return "PARKING:" + order.getOrderNo() + ":" + order.getEntryCode();
    }

    /**
     * 根据停车场计费规则计算费用
     *
     * @param lotId           停车场ID
     * @param durationMinutes 停车时长（分钟）
     * @return 应付金额
     */
    private BigDecimal calculateFee(Long lotId, long durationMinutes) {
        BillingRule rule = billingRuleService.getByLotId(lotId);
        if (rule == null) {
            return BigDecimal.ZERO;
        }

        // 按时计费
        if (rule.getRuleType() == 1) {
            // 免费时间内不收费
            if (rule.getFreeMinutes() != null && durationMinutes <= rule.getFreeMinutes()) {
                return BigDecimal.ZERO;
            }

            // 计算收费时长（扣除免费时间）
            long chargeMinutes = durationMinutes - (rule.getFreeMinutes() != null ? rule.getFreeMinutes() : 0);

            // 首小时费用
            BigDecimal fee = rule.getFirstHourPrice() != null ? rule.getFirstHourPrice() : BigDecimal.ZERO;

            // 超出首小时部分
            if (chargeMinutes > 60) {
                long extraMinutes = chargeMinutes - 60;
                // 向上取整到小时
                long extraHours = (extraMinutes + 59) / 60;
                BigDecimal extraPrice = rule.getExtraHourPrice() != null ? rule.getExtraHourPrice() : BigDecimal.ZERO;
                fee = fee.add(extraPrice.multiply(BigDecimal.valueOf(extraHours)));
            }

            // 每日封顶
            if (rule.getDailyMax() != null && rule.getDailyMax().compareTo(BigDecimal.ZERO) > 0) {
                long days = durationMinutes / (24 * 60);
                long remainMinutes = durationMinutes % (24 * 60);

                if (days > 0) {
                    BigDecimal dailyFee = rule.getDailyMax().multiply(BigDecimal.valueOf(days));
                    // 不足一天的部分按正常计算，取最小值
                    BigDecimal remainFee = fee; // fee 已是按上面逻辑算的总费用
                    // 重新计算：整天费用 + 剩余部分费用
                    fee = dailyFee;
                    if (remainMinutes > 0) {
                        BigDecimal partialFee = calculatePartialFee(rule, remainMinutes);
                        fee = fee.add(partialFee.min(rule.getDailyMax()));
                    }
                } else {
                    fee = fee.min(rule.getDailyMax());
                }
            }

            return fee.setScale(2, RoundingMode.HALF_UP);
        }

        // 按次计费
        if (rule.getRuleType() == 2) {
            return rule.getFlatPrice() != null ? rule.getFlatPrice().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        }

        return BigDecimal.ZERO;
    }

    /**
     * 计算不足一天的部分费用
     */
    private BigDecimal calculatePartialFee(BillingRule rule, long minutes) {
        if (rule.getFreeMinutes() != null && minutes <= rule.getFreeMinutes()) {
            return BigDecimal.ZERO;
        }
        long chargeMinutes = minutes - (rule.getFreeMinutes() != null ? rule.getFreeMinutes() : 0);
        BigDecimal fee = rule.getFirstHourPrice() != null ? rule.getFirstHourPrice() : BigDecimal.ZERO;
        if (chargeMinutes > 60) {
            long extraHours = (chargeMinutes - 60 + 59) / 60;
            BigDecimal extraPrice = rule.getExtraHourPrice() != null ? rule.getExtraHourPrice() : BigDecimal.ZERO;
            fee = fee.add(extraPrice.multiply(BigDecimal.valueOf(extraHours)));
        }
        return fee;
    }

    /**
     * 实体转换为响应DTO
     */
    private OrderResponse convertToResponse(ParkingOrder order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setUserId(order.getUserId());
        response.setLotId(order.getLotId());
        response.setSpaceId(order.getSpaceId());
        response.setPlateNumber(order.getPlateNumber());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setStatus(order.getOrderStatus());
        response.setAmount(order.getAmount());
        response.setActualAmount(order.getActualAmount());

        if (order.getDuration() != null) {
            response.setDuration(BigDecimal.valueOf(order.getDuration()));
        }
        if (order.getEntryTime() != null) {
            response.setEntryTime(order.getEntryTime().format(FORMATTER));
        }
        if (order.getExitTime() != null) {
            response.setExitTime(order.getExitTime().format(FORMATTER));
        }
        if (order.getCreatedAt() != null) {
            response.setCreatedAt(order.getCreatedAt().format(FORMATTER));
        }
        if (order.getUpdatedAt() != null) {
            response.setUpdatedAt(order.getUpdatedAt().format(FORMATTER));
        }

        // 停车场名称
        ParkingLot lot = parkingLotMapper.selectById(order.getLotId());
        if (lot != null) {
            response.setLotName(lot.getName());
        }
        // 车位编号
        ParkingSpace space = parkingSpaceMapper.selectById(order.getSpaceId());
        if (space != null) {
            response.setSpaceName(space.getSpaceNo());
        }
        // 用户名
        if (order.getUserId() != null) {
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                response.setUsername(user.getUsername());
            }
        }

        return response;
    }
}
