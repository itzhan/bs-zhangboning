package com.parking.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.common.exception.BusinessException;
import com.parking.common.result.PageResult;
import com.parking.dto.request.AppealReplyRequest;
import com.parking.dto.request.AppealRequest;
import com.parking.dto.response.AppealResponse;
import com.parking.entity.Appeal;
import com.parking.entity.ParkingOrder;
import com.parking.entity.User;
import com.parking.mapper.AppealMapper;
import com.parking.mapper.ParkingOrderMapper;
import com.parking.mapper.UserMapper;
import com.parking.service.AppealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 申诉服务实现类
 */
@Service
@RequiredArgsConstructor
public class AppealServiceImpl extends ServiceImpl<AppealMapper, Appeal> implements AppealService {

    private final UserMapper userMapper;
    private final ParkingOrderMapper parkingOrderMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppealResponse create(Long userId, AppealRequest request) {
        Appeal appeal = new Appeal();
        BeanUtil.copyProperties(request, appeal);
        appeal.setUserId(userId);
        appeal.setStatus(0); // 待处理
        getBaseMapper().insert(appeal);
        return convertToResponse(appeal);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppealResponse reply(Long id, AppealReplyRequest request) {
        Appeal appeal = getBaseMapper().selectById(id);
        if (appeal == null) {
            throw new BusinessException("申诉不存在");
        }
        if (appeal.getStatus() != 0) {
            throw new BusinessException("该申诉已处理");
        }

        appeal.setStatus(request.getStatus());
        appeal.setReply(request.getReply());
        appeal.setReplyTime(LocalDateTime.now());
        getBaseMapper().updateById(appeal);

        return convertToResponse(appeal);
    }

    @Override
    public PageResult<AppealResponse> getByUserId(Long userId, int page, int size) {
        LambdaQueryWrapper<Appeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appeal::getUserId, userId)
                .orderByDesc(Appeal::getCreatedAt);

        Page<Appeal> pageResult = page(new Page<>(page, size), wrapper);

        List<AppealResponse> list = pageResult.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public PageResult<AppealResponse> getPage(int page, int size, Integer status) {
        LambdaQueryWrapper<Appeal> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Appeal::getStatus, status);
        }
        wrapper.orderByDesc(Appeal::getCreatedAt);

        Page<Appeal> pageResult = page(new Page<>(page, size), wrapper);

        List<AppealResponse> list = pageResult.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    /**
     * 实体转换为响应DTO
     */
    private AppealResponse convertToResponse(Appeal appeal) {
        AppealResponse response = new AppealResponse();
        BeanUtil.copyProperties(appeal, response);
        if (appeal.getCreatedAt() != null) {
            response.setCreatedAt(appeal.getCreatedAt().format(FORMATTER));
        }
        if (appeal.getUpdatedAt() != null) {
            response.setUpdatedAt(appeal.getUpdatedAt().format(FORMATTER));
        }

        // 用户名
        if (appeal.getUserId() != null) {
            User user = userMapper.selectById(appeal.getUserId());
            if (user != null) {
                response.setUsername(user.getUsername());
            }
        }
        // 关联订单号
        if (appeal.getOrderId() != null) {
            ParkingOrder order = parkingOrderMapper.selectById(appeal.getOrderId());
            if (order != null) {
                response.setOrderNo(order.getOrderNo());
            }
        }

        return response;
    }
}
