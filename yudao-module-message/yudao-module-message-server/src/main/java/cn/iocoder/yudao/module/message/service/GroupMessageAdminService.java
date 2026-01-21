package cn.iocoder.yudao.module.message.service;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.message.controller.admin.message.vo.GroupMessagePageReqVO;
import cn.iocoder.yudao.module.message.controller.admin.message.vo.GroupMessageRespVO;
import cn.iocoder.yudao.module.message.dal.dataobject.GroupInfoDO;
import cn.iocoder.yudao.module.message.dal.dataobject.GroupMessageDO;
import cn.iocoder.yudao.module.message.dal.mapper.GroupInfoMapper;
import cn.iocoder.yudao.module.message.dal.mapper.GroupMessageMapper;
import cn.iocoder.yudao.module.message.enums.ErrorCodeConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
public class GroupMessageAdminService {

    @Resource
    private GroupMessageMapper groupMessageMapper;
    @Resource
    private GroupInfoMapper groupInfoMapper;
    @Resource
    private MemberUserApi memberUserApi;

    public PageResult<GroupMessageRespVO> getMessagePage(GroupMessagePageReqVO reqVO) {
        LambdaQueryWrapperX<GroupMessageDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eqIfPresent(GroupMessageDO::getGroupId, reqVO.getGroupId())
                .eqIfPresent(GroupMessageDO::getFromUserId, reqVO.getFromUserId())
                .eqIfPresent(GroupMessageDO::getType, reqVO.getType())
                .eqIfPresent(GroupMessageDO::getStatus, reqVO.getStatus())
                .eqIfPresent(GroupMessageDO::getDeleted, reqVO.getDeleted())
                .orderByDesc(GroupMessageDO::getCreateTime);
        if (reqVO.getCreateTime() != null && reqVO.getCreateTime().length == 2) {
            wrapper.between(GroupMessageDO::getCreateTime, reqVO.getCreateTime()[0], reqVO.getCreateTime()[1]);
        }
        PageResult<GroupMessageDO> page = groupMessageMapper.selectPage(reqVO, wrapper);
        if (CollUtil.isEmpty(page.getList())) {
            return PageResult.empty(page.getTotal());
        }
        Map<Long, MemberUserRespDTO> userMap = loadUserMap(page.getList());
        Map<Long, GroupInfoDO> groupMap = loadGroupMap(page.getList());
        List<GroupMessageRespVO> list = page.getList().stream()
                .map(item -> convert(item, userMap, groupMap))
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    public void recallMessage(Long id) {
        GroupMessageDO message = groupMessageMapper.selectById(id);
        if (message == null) {
            throw exception(ErrorCodeConstants.MESSAGE_NOT_EXISTS);
        }
        groupMessageMapper.updateStatusToRecall(id, 0L);
    }

    public void deleteMessage(Long id) {
        GroupMessageDO message = groupMessageMapper.selectById(id);
        if (message == null) {
            throw exception(ErrorCodeConstants.MESSAGE_NOT_EXISTS);
        }
        groupMessageMapper.updateStatusToDeleted(id);
    }

    private GroupMessageRespVO convert(GroupMessageDO message, Map<Long, MemberUserRespDTO> userMap,
                                       Map<Long, GroupInfoDO> groupMap) {
        GroupMessageRespVO vo = new GroupMessageRespVO();
        vo.setId(message.getId());
        vo.setGroupId(message.getGroupId());
        vo.setFromUserId(message.getFromUserId());
        vo.setType(message.getType());
        vo.setContent(message.getContent());
        vo.setExtraData(message.getExtraData());
        vo.setStatus(message.getStatus());
        vo.setDeleted(message.getDeleted());
        vo.setCreateTime(message.getCreateTime());

        GroupInfoDO group = groupMap.get(message.getGroupId());
        if (group != null) {
            vo.setGroupName(group.getGroupName());
        }
        MemberUserRespDTO sender = userMap.get(message.getFromUserId());
        if (sender != null) {
            vo.setFromUserName(sender.getNickname());
        }
        return vo;
    }

    private Map<Long, MemberUserRespDTO> loadUserMap(List<GroupMessageDO> list) {
        Set<Long> userIds = new HashSet<>();
        for (GroupMessageDO message : list) {
            if (message.getFromUserId() != null) {
                userIds.add(message.getFromUserId());
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

    private Map<Long, GroupInfoDO> loadGroupMap(List<GroupMessageDO> list) {
        Set<Long> groupIds = new HashSet<>();
        for (GroupMessageDO message : list) {
            if (message.getGroupId() != null) {
                groupIds.add(message.getGroupId());
            }
        }
        if (groupIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<GroupInfoDO> groups = groupInfoMapper.selectBatchIds(groupIds);
        if (CollUtil.isEmpty(groups)) {
            return Collections.emptyMap();
        }
        Map<Long, GroupInfoDO> map = new HashMap<>(groups.size());
        for (GroupInfoDO group : groups) {
            map.put(group.getId(), group);
        }
        return map;
    }
}
