package cn.iocoder.yudao.module.content.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicListRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicOptionRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicPageReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicSimpleRespVO;
import cn.iocoder.yudao.module.content.dal.dataobject.TopicDO;
import cn.iocoder.yudao.module.content.dal.mysql.TopicMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
public class TopicServiceImpl implements TopicService {

    @Resource
    private TopicMapper topicMapper;

    @Override
    public TopicDO getTopic(Long id) {
        return topicMapper.selectById(id);
    }

    @Override
    public PageResult<TopicDO> getTopicPage(TopicPageReqVO pageReqVO) {
        return topicMapper.selectPage(pageReqVO);
    }

    @Override
    public List<TopicListRespVO> getRecommendTopics(Integer limit) {
        return BeanUtils.toBean(topicMapper.selectRecommendedTopics(limit), TopicListRespVO.class);
    }

    @Override
    public List<TopicListRespVO> getHotTopics(Integer limit) {
        return BeanUtils.toBean(topicMapper.selectHotTopics(limit), TopicListRespVO.class);
    }

    @Override
    public List<TopicListRespVO> getTopicsByType(Integer type) {
        return BeanUtils.toBean(topicMapper.selectByType(type), TopicListRespVO.class);
    }

    @Override
    public List<TopicListRespVO> searchTopics(String keyword, Integer limit) {
        return BeanUtils.toBean(topicMapper.searchTopics(keyword, limit), TopicListRespVO.class);
    }

    @Override
    public List<TopicOptionRespVO> getAllTopicOptions() {
        return BeanUtils.toBean(topicMapper.selectBasicInfo(), TopicOptionRespVO.class);
    }

    @Override
    public List<TopicSimpleRespVO> getAllEnabledTopics() {
        return BeanUtils.toBean(topicMapper.selectAllEnabledTopics(null), TopicSimpleRespVO.class);
    }

    @Override
    public TopicRespVO getTopicDetail(Long id) {
        TopicDO topic = topicMapper.selectById(id);
        return topic == null ? null : BeanUtils.toBean(topic, TopicRespVO.class);
    }
}