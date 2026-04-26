package com.parking.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.dto.response.ScenicSpotResponse;
import com.parking.entity.ParkingLot;
import com.parking.entity.ScenicSpot;
import com.parking.mapper.ParkingLotMapper;
import com.parking.mapper.ScenicSpotMapper;
import com.parking.service.ScenicSpotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 景区景点服务实现
 */
@Service
@RequiredArgsConstructor
public class ScenicSpotServiceImpl extends ServiceImpl<ScenicSpotMapper, ScenicSpot> implements ScenicSpotService {

    private final ParkingLotMapper parkingLotMapper;

    @Override
    public List<ScenicSpotResponse> getAllWithRealtime() {
        LambdaQueryWrapper<ScenicSpot> spotWrapper = new LambdaQueryWrapper<>();
        spotWrapper.eq(ScenicSpot::getStatus, 1).orderByAsc(ScenicSpot::getSortOrder);
        List<ScenicSpot> spots = getBaseMapper().selectList(spotWrapper);

        // 仅查询运营中的停车场参与聚合
        LambdaQueryWrapper<ParkingLot> lotWrapper = new LambdaQueryWrapper<>();
        lotWrapper.eq(ParkingLot::getStatus, 1);
        List<ParkingLot> lots = parkingLotMapper.selectList(lotWrapper);

        List<ScenicSpotResponse> result = new ArrayList<>(spots.size());
        for (ScenicSpot spot : spots) {
            ScenicSpotResponse resp = new ScenicSpotResponse();
            BeanUtil.copyProperties(spot, resp);

            double radiusKm = spot.getRadiusKm() != null
                    ? spot.getRadiusKm().doubleValue() : 5.0;
            int lotCount = 0;
            int totalAvailable = 0;
            int totalSpaces = 0;
            for (ParkingLot lot : lots) {
                if (lot.getLongitude() == null || lot.getLatitude() == null) continue;
                double dist = haversineKm(
                        spot.getLatitude().doubleValue(),
                        spot.getLongitude().doubleValue(),
                        lot.getLatitude().doubleValue(),
                        lot.getLongitude().doubleValue()
                );
                if (dist <= radiusKm) {
                    lotCount++;
                    totalAvailable += lot.getAvailableSpaces() == null ? 0 : lot.getAvailableSpaces();
                    totalSpaces += lot.getTotalSpaces() == null ? 0 : lot.getTotalSpaces();
                }
            }
            resp.setNearbyLotCount(lotCount);
            resp.setNearbyAvailableSpaces(totalAvailable);
            resp.setNearbyTotalSpaces(totalSpaces);
            result.add(resp);
        }
        return result;
    }

    /**
     * Haversine 球面距离（km）
     */
    private static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * R * Math.asin(Math.sqrt(a));
    }
}
