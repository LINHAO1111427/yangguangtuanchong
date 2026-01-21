package cn.iocoder.yudao.module.content.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.content.controller.admin.channel.vo.ContentChannelPageReqVO;
import cn.iocoder.yudao.module.content.controller.admin.channel.vo.ContentChannelRespVO;
import cn.iocoder.yudao.module.content.controller.admin.channel.vo.ContentChannelSaveReqVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentChannelDO;
import cn.iocoder.yudao.module.content.dal.mysql.ContentChannelMapper;
import cn.iocoder.yudao.module.content.enums.ErrorCodeConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
public class ContentChannelAdminService {

    @Resource
    private ContentChannelMapper contentChannelMapper;

    public PageResult<ContentChannelRespVO> getChannelPage(ContentChannelPageReqVO reqVO) {
        PageResult<ContentChannelDO> page = contentChannelMapper.selectPage(reqVO);
        return new PageResult<>(BeanUtils.toBean(page.getList(), ContentChannelRespVO.class), page.getTotal());
    }

    public ContentChannelRespVO getChannel(Long id) {
        ContentChannelDO channel = contentChannelMapper.selectById(id);
        return channel == null ? null : BeanUtils.toBean(channel, ContentChannelRespVO.class);
    }

    public Long createChannel(ContentChannelSaveReqVO reqVO) {
        ContentChannelDO channel = BeanUtils.toBean(reqVO, ContentChannelDO.class);
        contentChannelMapper.insert(channel);
        return channel.getId();
    }

    public void updateChannel(ContentChannelSaveReqVO reqVO) {
        ContentChannelDO channel = contentChannelMapper.selectById(reqVO.getId());
        if (channel == null) {
            throw exception(ErrorCodeConstants.CHANNEL_NOT_EXISTS);
        }
        channel.setCode(reqVO.getCode());
        channel.setName(reqVO.getName());
        channel.setDescription(reqVO.getDescription());
        channel.setIcon(reqVO.getIcon());
        channel.setColor(reqVO.getColor());
        channel.setSort(reqVO.getSort());
        channel.setStatus(reqVO.getStatus());
        channel.setIsDefault(reqVO.getIsDefault());
        channel.setIsRequired(reqVO.getIsRequired());
        channel.setKeywordHints(reqVO.getKeywordHints());
        contentChannelMapper.updateById(channel);
    }

    public void deleteChannel(Long id) {
        contentChannelMapper.deleteById(id);
    }
}
