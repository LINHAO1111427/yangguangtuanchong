package cn.iocoder.yudao.module.content.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicListRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicOptionRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicPageReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.TopicSimpleRespVO;
import cn.iocoder.yudao.module.content.dal.dataobject.TopicDO;

import java.util.List;

public interface TopicService {

    TopicDO getTopic(Long id);

    PageResult<TopicDO> getTopicPage(TopicPageReqVO pageReqVO);

    List<TopicListRespVO> getRecommendTopics(Integer limit);

    List<TopicListRespVO> getHotTopics(Integer limit);

    List<TopicListRespVO> getTopicsByType(Integer type);

    List<TopicListRespVO> searchTopics(String keyword, Integer limit);

    List<TopicOptionRespVO> getAllTopicOptions();

    List<TopicSimpleRespVO> getAllEnabledTopics();

    TopicRespVO getTopicDetail(Long id);
}