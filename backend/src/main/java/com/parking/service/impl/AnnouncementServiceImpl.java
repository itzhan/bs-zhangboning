package com.parking.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.parking.common.exception.BusinessException;
import com.parking.common.result.PageResult;
import com.parking.dto.request.AnnouncementRequest;
import com.parking.dto.response.AnnouncementResponse;
import com.parking.entity.Announcement;
import com.parking.entity.User;
import com.parking.mapper.AnnouncementMapper;
import com.parking.mapper.UserMapper;
import com.parking.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务实现类
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    private final UserMapper userMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageResult<AnnouncementResponse> getPage(int page, int size, Integer type, Integer status) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(Announcement::getType, type);
        }
        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }
        wrapper.orderByDesc(Announcement::getCreatedAt);

        Page<Announcement> pageResult = page(new Page<>(page, size), wrapper);

        List<AnnouncementResponse> list = pageResult.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public PageResult<AnnouncementResponse> getPublished(int page, int size) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getStatus, 1) // 已发布
                .orderByDesc(Announcement::getPublishTime);

        Page<Announcement> pageResult = page(new Page<>(page, size), wrapper);

        List<AnnouncementResponse> list = pageResult.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(list, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public AnnouncementResponse getById(Long id) {
        Announcement announcement = getBaseMapper().selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        return convertToResponse(announcement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementResponse create(Long publisherId, AnnouncementRequest request) {
        Announcement announcement = new Announcement();
        BeanUtil.copyProperties(request, announcement);
        announcement.setPublisherId(publisherId);
        if (announcement.getStatus() == null) {
            announcement.setStatus(0); // 草稿
        }
        getBaseMapper().insert(announcement);
        return convertToResponse(announcement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementResponse update(Long id, AnnouncementRequest request) {
        Announcement announcement = getBaseMapper().selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        BeanUtil.copyProperties(request, announcement, "id");
        getBaseMapper().updateById(announcement);
        return convertToResponse(announcement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Announcement announcement = getBaseMapper().selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        getBaseMapper().deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id) {
        Announcement announcement = getBaseMapper().selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        announcement.setStatus(1); // 已发布
        announcement.setPublishTime(LocalDateTime.now());
        getBaseMapper().updateById(announcement);
    }

    /**
     * 实体转换为响应DTO
     */
    private AnnouncementResponse convertToResponse(Announcement announcement) {
        AnnouncementResponse response = new AnnouncementResponse();
        BeanUtil.copyProperties(announcement, response);
        if (announcement.getCreatedAt() != null) {
            response.setCreatedAt(announcement.getCreatedAt().format(FORMATTER));
        }
        if (announcement.getUpdatedAt() != null) {
            response.setUpdatedAt(announcement.getUpdatedAt().format(FORMATTER));
        }

        // 发布者姓名
        if (announcement.getPublisherId() != null) {
            User publisher = userMapper.selectById(announcement.getPublisherId());
            if (publisher != null) {
                response.setPublisherName(publisher.getNickname() != null ? publisher.getNickname() : publisher.getUsername());
            }
        }

        return response;
    }
}
