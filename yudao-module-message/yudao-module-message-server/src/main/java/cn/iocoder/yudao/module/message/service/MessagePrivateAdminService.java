package cn.iocoder.yudao.module.message.service;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.message.controller.admin.message.vo.MessagePrivatePageReqVO;
import cn.iocoder.yudao.module.message.controller.admin.message.vo.MessagePrivateRespVO;
import cn.iocoder.yudao.module.message.dal.dataobject.MessagePrivateDO;
import cn.iocoder.yudao.module.message.dal.mapper.MessagePrivateMapper;
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
public class MessagePrivateAdminService {

    @Resource
    private MessagePrivateMapper messagePrivateMapper;
    @Resource
    private MemberUserApi memberUserApi;

    public PageResult<MessagePrivateRespVO> getMessagePage(MessagePrivatePageReqVO reqVO) {
        LambdaQueryWrapperX<MessagePrivateDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eqIfPresent(MessagePrivateDO::getFromUserId, reqVO.getFromUserId())
                .eqIfPresent(MessagePrivateDO::getToUserId, reqVO.getToUserId())
                .eqIfPresent(MessagePrivateDO::getType, reqVO.getType())
                .eqIfPresent(MessagePrivateDO::getStatus, reqVO.getStatus())
                .eqIfPresent(MessagePrivateDO::getDeleted, reqVO.getDeleted())
                .orderByDesc(MessagePrivateDO::getCreateTime);
        if (reqVO.getCreateTime() != null && reqVO.getCreateTime().length == 2) {
            wrapper.between(MessagePrivateDO::getCreateTime, reqVO.getCreateTime()[0], reqVO.getCreateTime()[1]);
        }
        PageResult<MessagePrivateDO> page = messagePrivateMapper.selectPage(reqVO, wrapper);
        if (CollUtil.isEmpty(page.getList())) {
            return PageResult.empty(page.getTotal());
        }
        Map<Long, MemberUserRespDTO> userMap = loadUserMap(page.getList());
        List<MessagePrivateRespVO> list = page.getList().stream()
                .map(item -> convert(item, userMap))
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    public void recallMessage(Long id) {
        MessagePrivateDO message = messagePrivateMapper.selectById(id);
        if (message == null) {
            throw exception(ErrorCodeConstants.MESSAGE_NOT_EXISTS);
        }
        messagePrivateMapper.updateStatusToRecallByAdmin(id);
    }

    public void deleteMessage(Long id) {
        MessagePrivateDO message = messagePrivateMapper.selectById(id);
        if (message == null) {
            throw exception(ErrorCodeConstants.MESSAGE_NOT_EXISTS);
        }
        messagePrivateMapper.markAsDeletedByAdmin(id);
    }

    private MessagePrivateRespVO convert(MessagePrivateDO message, Map<Long, MemberUserRespDTO> userMap) {
        MessagePrivateRespVO vo = new MessagePrivateRespVO();
        vo.setId(message.getId());
        vo.setFromUserId(message.getFromUserId());
        vo.setToUserId(message.getToUserId());
        vo.setType(message.getType());
        vo.setContent(message.getContent());
        vo.setExtraData(message.getExtraData());
        vo.setStatus(message.getStatus());
        vo.setDeleted(message.getDeleted());
        vo.setReadTime(message.getReadTime());
        vo.setCreateTime(message.getCreateTime());

        MemberUserRespDTO fromUser = userMap.get(message.getFromUserId());
        if (fromUser != null) {
            vo.setFromUserName(fromUser.getNickname());
        }
        MemberUserRespDTO toUser = userMap.get(message.getToUserId());
        if (toUser != null) {
            vo.setToUserName(toUser.getNickname());
        }
        return vo;
    }

    private Map<Long, MemberUserRespDTO> loadUserMap(List<MessagePrivateDO> list) {
        Set<Long> userIds = new HashSet<>();
        for (MessagePrivateDO message : list) {
            if (message.getFromUserId() != null) {
                userIds.add(message.getFromUserId());
            }
            if (message.getToUserId() != null) {
                userIds.add(message.getToUserId());
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
