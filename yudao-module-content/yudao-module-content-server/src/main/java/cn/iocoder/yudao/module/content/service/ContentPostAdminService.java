package cn.iocoder.yudao.module.content.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.content.controller.admin.content.vo.ContentPostAuditReqVO;
import cn.iocoder.yudao.module.content.controller.admin.content.vo.ContentPostDetailRespVO;
import cn.iocoder.yudao.module.content.controller.admin.content.vo.ContentPostPageReqVO;
import cn.iocoder.yudao.module.content.controller.admin.content.vo.ContentPostRespVO;
import cn.iocoder.yudao.module.content.controller.admin.content.vo.ContentPostUpdateReqVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentChannelDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentDO;
import cn.iocoder.yudao.module.content.dal.mysql.ContentMapper;
import cn.iocoder.yudao.module.content.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.content.service.channel.ContentChannelService;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
public class ContentPostAdminService {

    @Resource
    private ContentMapper contentMapper;
    @Resource
    private ContentService contentService;
    @Resource
    private ContentChannelService contentChannelService;
    @Resource
    private MemberUserApi memberUserApi;

    public PageResult<ContentPostRespVO> getPostPage(ContentPostPageReqVO reqVO) {
        PageResult<ContentDO> page = contentMapper.selectAdminPage(reqVO);
        if (CollUtil.isEmpty(page.getList())) {
            return PageResult.empty(page.getTotal());
        }
        Map<Long, MemberUserRespDTO> userMap = loadUserMap(page.getList());
        List<ContentPostRespVO> list = page.getList().stream()
                .map(content -> convert(content, userMap.get(content.getUserId())))
                .collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    public ContentPostDetailRespVO getPostDetail(Long id) {
        ContentDO content = contentMapper.selectById(id);
        if (content == null) {
            return null;
        }
        ContentPostDetailRespVO vo = BeanUtils.toBean(content, ContentPostDetailRespVO.class);
        MemberUserRespDTO author = getAuthor(content.getUserId());
        if (author != null) {
            vo.setAuthorNickname(StrUtil.blankToDefault(author.getNickname(), null));
        }
        return vo;
    }

    public void auditPost(ContentPostAuditReqVO reqVO) {
        contentService.auditContent(reqVO.getId(), reqVO.getAuditStatus(), reqVO.getAuditRemark());
    }

    public void updatePost(ContentPostUpdateReqVO reqVO) {
        ContentDO content = contentMapper.selectById(reqVO.getId());
        if (content == null) {
            throw exception(ErrorCodeConstants.CONTENT_NOT_EXISTS);
        }
        if (reqVO.getStatus() != null) {
            content.setStatus(reqVO.getStatus());
            if (Objects.equals(reqVO.getStatus(), ContentDO.StatusEnum.PUBLISHED.getStatus())
                    && content.getPublishTime() == null) {
                content.setPublishTime(LocalDateTime.now());
            }
        }
        if (reqVO.getIsPublic() != null) {
            content.setIsPublic(reqVO.getIsPublic());
        }
        if (reqVO.getAllowComment() != null) {
            content.setAllowComment(reqVO.getAllowComment());
        }
        if (reqVO.getAllowDownload() != null) {
            content.setAllowDownload(reqVO.getAllowDownload());
        }
        if (reqVO.getIsTop() != null) {
            content.setIsTop(reqVO.getIsTop());
        }
        if (reqVO.getIsHot() != null) {
            content.setIsHot(reqVO.getIsHot());
        }
        if (reqVO.getIsRecommend() != null) {
            content.setIsRecommend(reqVO.getIsRecommend());
        }
        if (reqVO.getChannelId() != null) {
            content.setChannelId(reqVO.getChannelId());
            ContentChannelDO channel = contentChannelService.getChannel(reqVO.getChannelId());
            content.setChannelName(channel != null ? channel.getName() : null);
        }
        if (reqVO.getPublishTopicId() != null) {
            content.setPublishTopicId(reqVO.getPublishTopicId());
        }
        content.setUpdateTime(LocalDateTime.now());
        contentMapper.updateById(content);
    }

    public void deletePost(Long id) {
        contentService.deleteContent(id);
    }

    private ContentPostRespVO convert(ContentDO content, MemberUserRespDTO author) {
        ContentPostRespVO vo = BeanUtils.toBean(content, ContentPostRespVO.class);
        if (author != null) {
            vo.setAuthorNickname(StrUtil.blankToDefault(author.getNickname(), null));
        }
        return vo;
    }

    private Map<Long, MemberUserRespDTO> loadUserMap(List<ContentDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyMap();
        }
        List<Long> userIds = list.stream()
                .map(ContentDO::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        try {
            return memberUserApi.getUserMap(userIds);
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private MemberUserRespDTO getAuthor(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            return memberUserApi.getUser(userId).getCheckedData();
        } catch (Exception ex) {
            return null;
        }
    }
}
