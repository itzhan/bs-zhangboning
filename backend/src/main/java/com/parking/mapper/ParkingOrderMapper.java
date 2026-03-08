package com.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.entity.ParkingOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface ParkingOrderMapper extends BaseMapper<ParkingOrder> {

    @Select("SELECT DATE(created_at) AS date, SUM(actual_amount) AS revenue, COUNT(*) AS order_count " +
            "FROM parking_order " +
            "WHERE payment_status = 1 AND created_at BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date")
    List<Map<String, Object>> selectRevenueStats(@Param("startDate") String startDate,
                                                  @Param("endDate") String endDate);

    @Select("SELECT COALESCE(SUM(actual_amount), 0) FROM parking_order " +
            "WHERE payment_status = 1 AND DATE(created_at) = CURDATE()")
    BigDecimal selectTodayRevenue();

    @Select("SELECT COUNT(*) FROM parking_order WHERE DATE(created_at) = CURDATE()")
    int selectTodayOrderCount();
}
