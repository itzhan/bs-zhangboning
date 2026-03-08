package com.parking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.parking.entity.Blacklist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BlacklistMapper extends BaseMapper<Blacklist> {

    @Select("SELECT * FROM blacklist WHERE plate_number = #{plateNumber}")
    Blacklist selectByPlateNumber(String plateNumber);
}
