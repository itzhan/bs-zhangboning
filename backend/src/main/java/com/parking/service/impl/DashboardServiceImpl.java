package com.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.parking.dto.response.DashboardResponse;
import com.parking.dto.response.OccupancyStatsResponse;
import com.parking.dto.response.RevenueStatsResponse;
import com.parking.entity.*;
import com.parking.mapper.*;
import com.parking.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仪表盘统计服务实现类
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ParkingLotMapper parkingLotMapper;
    private final ParkingSpaceMapper parkingSpaceMapper;
    private final ParkingOrderMapper parkingOrderMapper;
    private final ReservationMapper reservationMapper;
    private final PaymentMapper paymentMapper;

    @Override
    public DashboardResponse getDashboard() {
        DashboardResponse response = new DashboardResponse();

        // 停车场总数
        Long totalLots = parkingLotMapper.selectCount(new LambdaQueryWrapper<>());
        response.setTotalLots(totalLots.intValue());

        // 车位总数和可用车位数
        List<ParkingLot> lots = parkingLotMapper.selectList(new LambdaQueryWrapper<>());
        int totalSpaces = lots.stream().mapToInt(l -> l.getTotalSpaces() != null ? l.getTotalSpaces() : 0).sum();
        int availableSpaces = lots.stream().mapToInt(l -> l.getAvailableSpaces() != null ? l.getAvailableSpaces() : 0).sum();
        response.setTotalSpaces(totalSpaces);
        response.setAvailableSpaces(availableSpaces);

        // 车位占用率
        if (totalSpaces > 0) {
            int occupied = totalSpaces - availableSpaces;
            BigDecimal rate = BigDecimal.valueOf(occupied)
                    .divide(BigDecimal.valueOf(totalSpaces), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            response.setOccupancyRate(rate);
        } else {
            response.setOccupancyRate(BigDecimal.ZERO);
        }

        // 今日订单数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        Long todayOrders = parkingOrderMapper.selectCount(
                new LambdaQueryWrapper<ParkingOrder>()
                        .between(ParkingOrder::getCreatedAt, todayStart, todayEnd)
        );
        response.setTodayOrders(todayOrders.intValue());

        // 今日收入
        List<Payment> todayPayments = paymentMapper.selectList(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getPaymentStatus, 1)
                        .between(Payment::getPaymentTime, todayStart, todayEnd)
        );
        BigDecimal todayRevenue = todayPayments.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTodayRevenue(todayRevenue.setScale(2, RoundingMode.HALF_UP));

        // 今日预约数
        Long todayReservations = reservationMapper.selectCount(
                new LambdaQueryWrapper<Reservation>()
                        .between(Reservation::getCreatedAt, todayStart, todayEnd)
        );
        response.setTodayReservations(todayReservations.intValue());

        // 活跃告警数（黑名单相关，暂设为0）
        response.setActiveAlerts(0);

        return response;
    }

    @Override
    public List<RevenueStatsResponse> getRevenueStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        // 查询时间范围内的成功支付记录
        List<Payment> payments = paymentMapper.selectList(
                new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getPaymentStatus, 1)
                        .between(Payment::getPaymentTime, start, end)
        );

        // 按日期分组统计
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, List<Payment>> grouped = payments.stream()
                .filter(p -> p.getPaymentTime() != null)
                .collect(Collectors.groupingBy(p -> p.getPaymentTime().toLocalDate().format(dateFormatter)));

        // 生成完整日期范围的统计数据
        List<RevenueStatsResponse> result = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            String dateStr = current.format(dateFormatter);
            RevenueStatsResponse stats = new RevenueStatsResponse();
            stats.setDate(dateStr);

            List<Payment> dayPayments = grouped.getOrDefault(dateStr, new ArrayList<>());
            BigDecimal revenue = dayPayments.stream()
                    .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.setRevenue(revenue.setScale(2, RoundingMode.HALF_UP));
            stats.setOrderCount(dayPayments.size());

            result.add(stats);
            current = current.plusDays(1);
        }

        return result;
    }

    @Override
    public List<OccupancyStatsResponse> getOccupancyStats() {
        List<ParkingLot> lots = parkingLotMapper.selectList(
                new LambdaQueryWrapper<ParkingLot>().eq(ParkingLot::getStatus, 1)
        );

        return lots.stream().map(lot -> {
            OccupancyStatsResponse stats = new OccupancyStatsResponse();
            stats.setLotName(lot.getName());
            int total = lot.getTotalSpaces() != null ? lot.getTotalSpaces() : 0;
            int available = lot.getAvailableSpaces() != null ? lot.getAvailableSpaces() : 0;
            int occupied = total - available;
            stats.setTotalSpaces(total);
            stats.setOccupiedSpaces(occupied);

            if (total > 0) {
                BigDecimal rate = BigDecimal.valueOf(occupied)
                        .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                stats.setOccupancyRate(rate);
            } else {
                stats.setOccupancyRate(BigDecimal.ZERO);
            }
            return stats;
        }).collect(Collectors.toList());
    }
}
