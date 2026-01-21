package cn.iocoder.yudao.module.content.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.content.controller.app.vo.CommentCreateReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.CommentPageReqVO;
import cn.iocoder.yudao.module.content.controller.app.vo.CommentRespVO;

public interface CommentService {

    PageResult<CommentRespVO> getCommentPage(CommentPageReqVO pageReqVO, Long currentUserId);

    CommentRespVO createComment(CommentCreateReqVO createReqVO, Long userId, String ipAddress, String userAgent);

    boolean toggleLike(Long commentId, Long userId, String ipAddress, String userAgent);

    void deleteComment(Long commentId, Long operatorUserId);
}
