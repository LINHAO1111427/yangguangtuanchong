package cn.iocoder.yudao.module.content.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.content.controller.admin.topic.vo.ContentTopicRespVO;
import cn.iocoder.yudao.module.content.controller.admin.topic.vo.ContentTopicSaveReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicPageReqVO;
import cn.iocoder.yudao.module.content.dal.dataobject.TopicDO;
import cn.iocoder.yudao.module.content.dal.mysql.TopicMapper;
import cn.iocoder.yudao.module.content.enums.ErrorCodeConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
public class ContentTopicAdminService {

    @Resource
    private TopicMapper topicMapper;

    public PageResult<ContentTopicRespVO> getTopicPage(TopicPageReqVO reqVO) {
        PageResult<TopicDO> page = topicMapper.selectPage(reqVO);
        return new PageResult<>(BeanUtils.toBean(page.getList(), ContentTopicRespVO.class), page.getTotal());
    }

    public ContentTopicRespVO getTopic(Long id) {
        TopicDO topic = topicMapper.selectById(id);
        return topic == null ? null : BeanUtils.toBean(topic, ContentTopicRespVO.class);
    }

    public Long createTopic(ContentTopicSaveReqVO reqVO) {
        TopicDO topic = BeanUtils.toBean(reqVO, TopicDO.class);
        topicMapper.insert(topic);
        return topic.getId();
    }

    public void updateTopic(ContentTopicSaveReqVO reqVO) {
        TopicDO topic = topicMapper.selectById(reqVO.getId());
        if (topic == null) {
            throw exception(ErrorCodeConstants.TOPIC_NOT_EXISTS);
        }
        topic.setName(reqVO.getName());
        topic.setDescription(reqVO.getDescription());
        topic.setIcon(reqVO.getIcon());
        topic.setCover(reqVO.getCover());
        topic.setType(reqVO.getType());
        topic.setColor(reqVO.getColor());
        topic.setSort(reqVO.getSort());
        topic.setIsRecommend(reqVO.getIsRecommend());
        topic.setStatus(reqVO.getStatus());
        topicMapper.updateById(topic);
    }

    public void deleteTopic(Long id) {
        topicMapper.deleteById(id);
    }
}
