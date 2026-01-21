package cn.iocoder.yudao.module.content.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentAuthorProfileRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentCreateReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentDetailRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentListRespVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentPageReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.ContentUpdateReqVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * Content service contract.
 *
 * <p>
 * The original source comments were in Chinese. They are replaced with concise English notes to
 * avoid encoding issues while keeping method semantics unchanged.
 * </p>
 *
 * @author
 */
public interface ContentService {

    Long createContent(@Valid ContentCreateReqVO createReqVO);

    void updateContent(@Valid ContentUpdateReqVO updateReqVO);

    void deleteContent(Long id);

    ContentDO getContent(Long id);

    ContentDetailRespVO getContentDetail(Long id, Long currentUserId);

    PageResult<ContentListRespVO> getContentPage(ContentPageReqVO pageReqVO, Long currentUserId);

    /**
     * Fetch content list by ids, preserving the request order.
     *
     * @param contentIds     ids to query
     * @param currentUserId  current login user, used for interaction flags
     * @return ordered content list
     */
    List<ContentListRespVO> getContentListByIds(List<Long> contentIds, Long currentUserId);

    PageResult<ContentListRespVO> getMyContentPage(ContentPageReqVO pageReqVO, Long currentUserId);

    List<ContentListRespVO> getUserDrafts(Long userId);

    PageResult<ContentListRespVO> getHotContents(ContentPageReqVO pageReqVO, Long currentUserId);

    PageResult<ContentListRespVO> getLatestContents(ContentPageReqVO pageReqVO, Long currentUserId);

    PageResult<ContentListRespVO> getFollowingContents(Long userId, ContentPageReqVO pageReqVO);

    PageResult<ContentListRespVO> searchContents(ContentPageReqVO pageReqVO, Long currentUserId);

    PageResult<ContentListRespVO> getTopicContents(ContentPageReqVO pageReqVO, Long currentUserId);

    void validateContentOwner(Long contentId, Long userId);

    void recordContentView(Long contentId, Long userId, String ipAddress, String userAgent);

    boolean toggleLike(Long contentId, Long userId, String ipAddress, String userAgent);

    boolean toggleCollect(Long contentId, Long userId, String ipAddress, String userAgent);

    void recordContentShare(Long contentId, Long userId, String platform, String ipAddress, String userAgent);

    void reportContent(Long contentId, Long userId, String reason, String description, String ipAddress, String userAgent);

    String generateShareUrl(Long contentId);

    void auditContent(Long contentId, Integer auditStatus, String auditRemark);

    void updateContentHotScore(Long contentId);

    void updateContentRecommendScore(Long contentId);

    ContentAuthorProfileRespVO getAuthorProfile(Long authorId, Long currentUserId);

    PageResult<ContentListRespVO> getAuthorContentPage(Long authorId, ContentPageReqVO pageReqVO, Long currentUserId);

    // 作者：Lin- 仅为满足 ContentApiImpl 调用，补充最小读取接口
    java.util.List<cn.iocoder.yudao.module.content.dal.dataobject.ContentDO> getContentList(java.util.Collection<Long> ids);

    java.util.List<cn.iocoder.yudao.module.content.dal.dataobject.ContentDO> getContentListByUserId(Long userId);

    /**
     * 获取用户喜欢的内容列表
     *
     * @param userId     用户ID
     * @param pageReqVO  分页参数
     * @return 喜欢的内容列表
     */
    PageResult<ContentListRespVO> getMyLikedContents(Long userId, ContentPageReqVO pageReqVO);

    /**
     * 获取用户收藏的内容列表
     *
     * @param userId     用户ID
     * @param pageReqVO  分页参数
     * @return 收藏的内容列表
     */
    PageResult<ContentListRespVO> getMyCollectedContents(Long userId, ContentPageReqVO pageReqVO);

    /**
     * 获取我的浏览历史
     *
     * @param userId    用户ID
     * @param pageReqVO 分页参数
     * @return 浏览历史分页
     */
    PageResult<ContentListRespVO> getMyViewHistory(Long userId, ContentPageReqVO pageReqVO);

    /**
     * 删除指定浏览历史
     *
     * @param userId     用户ID
     * @param contentIds 内容ID列表
     */
    void deleteMyViewHistory(Long userId, Collection<Long> contentIds);

    /**
     * 清空浏览历史
     *
     * @param userId 用户ID
     */
    void clearMyViewHistory(Long userId);
}
