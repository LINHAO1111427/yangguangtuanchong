package cn.iocoder.yudao.module.message.service;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.module.content.api.ContentApi;
import cn.iocoder.yudao.module.content.api.dto.ContentRespDTO;
import cn.iocoder.yudao.module.member.api.user.MemberUserApi;
import cn.iocoder.yudao.module.member.api.user.dto.MemberUserRespDTO;
import cn.iocoder.yudao.module.message.controller.app.vo.AppShareCardRespVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.Set;

@Service
public class ShareCardServiceImpl implements ShareCardService {

    @Resource
    private ContentApi contentApi;
    @Resource
    private MemberUserApi memberUserApi;

    @Override
    public AppShareCardRespVO getShareCard(Long userId, Long contentId) {
        ContentRespDTO content = contentApi.getContent(contentId).getCheckedData();
        if (content == null) {
            throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.NOT_FOUND);
        }
        Map<Long, MemberUserRespDTO> memberMap = memberUserApi.getUserMap(Set.of(content.getUserId()));
        MemberUserRespDTO author = memberMap.get(content.getUserId());

        AppShareCardRespVO vo = new AppShareCardRespVO();
        vo.setContentId(content.getId());
        vo.setContentType(content.getContentType());
        vo.setTitle(content.getTitle());
        vo.setImages(content.getImages());
        vo.setVideoUrl(content.getVideoUrl());
        vo.setVideoCover(content.getVideoCover());
        String cover = content.getVideoCover();
        if (cover == null && CollUtil.isNotEmpty(content.getImages())) {
            cover = content.getImages().get(0);
        }
        vo.setCoverImage(cover);
        vo.setAuthorId(content.getUserId());
        if (author != null) {
            vo.setAuthorNickname(author.getNickname());
            vo.setAuthorAvatar(author.getAvatar());
        }
        vo.setPublishTime(content.getPublishTime());
        return vo;
    }
}

