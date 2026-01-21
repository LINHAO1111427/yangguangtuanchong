package cn.iocoder.yudao.module.message.service;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.message.controller.admin.message.vo.NotificationPageReqVO;
import cn.iocoder.yudao.module.message.controller.admin.message.vo.NotificationRespVO;
import cn.iocoder.yudao.module.message.dal.dataobject.NotificationDO;
import cn.iocoder.yudao.module.message.dal.mapper.NotificationMapper;
import cn.iocoder.yudao.module.message.enums.ErrorCodeConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
public class NotificationAdminService {

    @Resource
    private NotificationMapper notificationMapper;
    @Resource
    private MemberUserApi memberUserApi;

    public PageResult<NotificationRespVO> getNotificationPage(NotificationPageReqVO reqVO) {
        LambdaQueryWrapperX<NotificationDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eqIfPresent(NotificationDO::getUserId, reqVO.getUserId())
                .eqIfPresent(NotificationDO::getType, reqVO.getType())
                .eqIfPresent(NotificationDO::getIsRead, reqVO.getIsRead())
                .eq(NotificationDO::getDeleted, 0)
                .orderByDesc(NotificationDO::getCreateTime);
        if (reqVO.getCreateTime() != null && reqVO.getCreateTime().length == 2) {
            wrapper.between(NotificationDO::getCreateTime, reqVO.getCreateTime()[0], reqVO.getCreateTime()[1]);
        }
        PageResult<NotificationDO> page = notificationMapper.selectPage(reqVO, wrapper);
        if (CollUtil.isEmpty(page.getList())) {
            return PageResult.empty(page.getTotal());
        }
        Map<Long, MemberUserRespDTO> userMap = loadUserMap(page.getList());
        List<NotificationRespVO> list = page.getList().stream()
                .map(item -> convert(item, userMap))
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    public void deleteNotification(Long id) {
        NotificationDO notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw exception(ErrorCodeConstants.NOTIFICATION_NOT_EXISTS);
        }
        notificationMapper.markAsDeletedByAdmin(id);
    }

    private NotificationRespVO convert(NotificationDO notification, Map<Long, MemberUserRespDTO> userMap) {
        NotificationRespVO vo = new NotificationRespVO();
        vo.setId(notification.getId());
        vo.setUserId(notification.getUserId());
        vo.setType(notification.getType());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setLink(notification.getLink());
        vo.setIsRead(notification.getIsRead());
        vo.setDeleted(notification.getDeleted());
        vo.setReadTime(notification.getReadTime());
        vo.setCreateTime(notification.getCreateTime());

        MemberUserRespDTO user = userMap.get(notification.getUserId());
        if (user != null) {
            vo.setUserName(user.getNickname());
        }
        return vo;
    }

    private Map<Long, MemberUserRespDTO> loadUserMap(List<NotificationDO> list) {
        Set<Long> userIds = new HashSet<>();
        for (NotificationDO notification : list) {
            if (notification.getUserId() != null) {
                userIds.add(notification.getUserId());
            }
        }
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return memberUserApi.getUserMap(new ArrayList<>(userIds));
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }
}
