package com.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.entity.ParkingSpace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ParkingSpaceMapper extends BaseMapper<ParkingSpace> {

    @Select("SELECT COUNT(*) FROM parking_space WHERE lot_id = #{lotId} AND status = #{status}")
    int countByLotIdAndStatus(@Param("lotId") Long lotId, @Param("status") Integer status);
}
