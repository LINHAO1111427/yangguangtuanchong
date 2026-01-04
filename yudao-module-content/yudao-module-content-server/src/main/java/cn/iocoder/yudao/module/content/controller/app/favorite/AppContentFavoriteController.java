package cn.iocoder.yudao.module.content.controller.app.favorite;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.content.controller.app.favorite.vo.FavoriteActionReqVO;
import cn.iocoder.yudao.module.content.controller.app.favorite.vo.FavoriteGroupRespVO;
import cn.iocoder.yudao.module.content.controller.app.favorite.vo.FavoriteGroupSaveReqVO;
import cn.iocoder.yudao.module.content.controller.app.favorite.vo.FavoriteRecordRespVO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentFavoriteGroupDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentFavoriteRecordDO;
import cn.iocoder.yudao.module.content.service.favorite.ContentFavoriteService;
import cn.iocoder.yudao.module.content.service.favorite.bo.FavoriteActionReqBO;
import cn.iocoder.yudao.module.content.service.favorite.bo.FavoriteGroupCreateReqBO;
import cn.iocoder.yudao.module.content.service.favorite.bo.FavoriteGroupUpdateReqBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Validated
@Tag(name = "APP - 收藏管理")
@RestController
@RequestMapping("/content/favorite")
public class AppContentFavoriteController {

    @Resource
    private ContentFavoriteService contentFavoriteService;

    @GetMapping("/groups")
    @Operation(summary = "查询收藏分组")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<List<FavoriteGroupRespVO>> getGroups() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<FavoriteGroupRespVO> resp = contentFavoriteService.getUserGroups(userId).stream()
                .map(this::convertGroup)
                .collect(Collectors.toList());
        return success(resp);
    }

    @PostMapping("/groups")
    @Operation(summary = "创建收藏分组")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<FavoriteGroupRespVO> createGroup(@Valid @RequestBody FavoriteGroupSaveReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        FavoriteGroupCreateReqBO bo = new FavoriteGroupCreateReqBO();
        bo.setGroupName(reqVO.getGroupName());
        bo.setDescription(reqVO.getDescription());
        bo.setColor(reqVO.getColor());
        bo.setCoverImage(reqVO.getCoverImage());
        bo.setTagList(reqVO.getTagList());
        bo.setExtra(reqVO.getExtra());
        ContentFavoriteGroupDO group = contentFavoriteService.createGroup(userId, bo);
        return success(convertGroup(group));
    }

    @PutMapping("/groups/{id}")
    @Operation(summary = "更新收藏分组")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<FavoriteGroupRespVO> updateGroup(@PathVariable("id") Long id,
                                                         @Valid @RequestBody FavoriteGroupSaveReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        FavoriteGroupUpdateReqBO bo = new FavoriteGroupUpdateReqBO();
        bo.setGroupId(id);
        bo.setGroupName(reqVO.getGroupName());
        bo.setDescription(reqVO.getDescription());
        bo.setColor(reqVO.getColor());
        bo.setCoverImage(reqVO.getCoverImage());
        bo.setTagList(reqVO.getTagList());
        bo.setExtra(reqVO.getExtra());
        ContentFavoriteGroupDO group = contentFavoriteService.updateGroup(userId, bo);
        return success(convertGroup(group));
    }

    @DeleteMapping("/groups/{id}")
    @Operation(summary = "删除收藏分组")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> deleteGroup(@PathVariable("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        contentFavoriteService.deleteGroup(userId, id);
        return success(Boolean.TRUE);
    }

    @GetMapping("/records")
    @Operation(summary = "查询收藏记录")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<List<FavoriteRecordRespVO>> getFavoriteRecords(
            @RequestParam(value = "group_id", required = false) Long groupId) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        List<FavoriteRecordRespVO> resp = contentFavoriteService.getFavoriteRecords(userId).stream()
                .filter(record -> groupId == null || groupId.equals(record.getGroupId()))
                .map(this::convertRecord)
                .collect(Collectors.toList());
        return success(resp);
    }

    @PostMapping("/records/toggle")
    @Operation(summary = "收藏/取消收藏并附带标签")
    @PreAuthorize("isAuthenticated()")
    public CommonResult<Boolean> toggleFavorite(@Valid @RequestBody FavoriteActionReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        FavoriteActionReqBO bo = new FavoriteActionReqBO();
        bo.setContentId(reqVO.getContentId());
        bo.setGroupId(reqVO.getGroupId());
        bo.setTags(reqVO.getTags());
        bo.setNote(reqVO.getNote());
        bo.setSource(reqVO.getSource());
        bo.setExtra(reqVO.getExtra());
        boolean collected = contentFavoriteService.toggleFavorite(userId, bo);
        return success(collected);
    }

    private FavoriteGroupRespVO convertGroup(ContentFavoriteGroupDO group) {
        if (group == null) {
            return null;
        }
        FavoriteGroupRespVO vo = new FavoriteGroupRespVO();
        vo.setId(group.getId());
        vo.setGroupName(group.getGroupName());
        vo.setDescription(group.getDescription());
        vo.setColor(group.getColor());
        vo.setCoverImage(group.getCoverImage());
        vo.setIsDefault(group.getIsDefault());
        vo.setTagList(group.getTagList());
        vo.setExtra(group.getExtra());
        vo.setCreateTime(group.getCreateTime());
        return vo;
    }

    private FavoriteRecordRespVO convertRecord(ContentFavoriteRecordDO record) {
        FavoriteRecordRespVO vo = new FavoriteRecordRespVO();
        vo.setId(record.getId());
        vo.setContentId(record.getContentId());
        vo.setGroupId(record.getGroupId());
        vo.setTags(record.getTags());
        vo.setNote(record.getNote());
        vo.setSource(record.getSource());
        vo.setExtra(record.getExtra());
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }
}
