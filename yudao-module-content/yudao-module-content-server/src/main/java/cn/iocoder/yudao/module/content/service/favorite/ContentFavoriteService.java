package cn.iocoder.yudao.module.content.service.favorite;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentFavoriteGroupDO;
import cn.iocoder.yudao.module.content.dal.dataobject.ContentFavoriteRecordDO;
import cn.iocoder.yudao.module.content.dal.mysql.ContentFavoriteGroupMapper;
import cn.iocoder.yudao.module.content.dal.mysql.ContentFavoriteRecordMapper;
import cn.iocoder.yudao.module.content.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.content.service.favorite.bo.FavoriteActionReqBO;
import cn.iocoder.yudao.module.content.service.favorite.bo.FavoriteGroupCreateReqBO;
import cn.iocoder.yudao.module.content.service.favorite.bo.FavoriteGroupUpdateReqBO;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 收藏与自定义标签分组服务。
 */
@Service
public class ContentFavoriteService {

    private static final Logger log = LoggerFactory.getLogger(ContentFavoriteService.class);

    @Resource
    private ContentFavoriteGroupMapper groupMapper;
    @Resource
    private ContentFavoriteRecordMapper recordMapper;

    public List<ContentFavoriteGroupDO> getUserGroups(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return groupMapper.selectByUserId(userId);
    }

    public ContentFavoriteGroupDO getGroup(Long userId, Long groupId) {
        if (userId == null || groupId == null) {
            return null;
        }
        ContentFavoriteGroupDO group = groupMapper.selectById(groupId);
        if (group == null || !Objects.equals(group.getUserId(), userId)) {
            return null;
        }
        return group;
    }

    public ContentFavoriteGroupDO createGroup(Long userId, FavoriteGroupCreateReqBO reqBO) {
        ContentFavoriteGroupDO group = new ContentFavoriteGroupDO();
        group.setUserId(userId);
        group.setGroupName(StrUtil.blankToDefault(reqBO.getGroupName(), defaultGroupName()));
        group.setDescription(reqBO.getDescription());
        group.setColor(reqBO.getColor());
        group.setCoverImage(reqBO.getCoverImage());
        group.setTagList(reqBO.getTagList());
        group.setExtra(reqBO.getExtra());
        group.setIsDefault(0);
        groupMapper.insert(group);
        return group;
    }

    public ContentFavoriteGroupDO updateGroup(Long userId, FavoriteGroupUpdateReqBO reqBO) {
        ContentFavoriteGroupDO group = getGroup(userId, reqBO.getGroupId());
        if (group == null) {
            throw exception(ErrorCodeConstants.FAVORITE_GROUP_NOT_EXISTS);
        }
        if (StrUtil.isNotBlank(reqBO.getGroupName())) {
            group.setGroupName(reqBO.getGroupName());
        }
        group.setDescription(reqBO.getDescription());
        group.setColor(reqBO.getColor());
        group.setCoverImage(reqBO.getCoverImage());
        group.setTagList(reqBO.getTagList());
        group.setExtra(reqBO.getExtra());
        groupMapper.updateById(group);
        return group;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long userId, Long groupId) {
        ContentFavoriteGroupDO group = getGroup(userId, groupId);
        if (group == null) {
            throw exception(ErrorCodeConstants.FAVORITE_GROUP_NOT_EXISTS);
        }
        if (Objects.equals(group.getIsDefault(), 1)) {
            return;
        }
        ContentFavoriteGroupDO defaultGroup = getOrCreateDefaultGroup(userId);
        List<ContentFavoriteRecordDO> records = recordMapper.selectByGroupIds(List.of(groupId));
        if (CollUtil.isNotEmpty(records)) {
            for (ContentFavoriteRecordDO record : records) {
                record.setGroupId(defaultGroup.getId());
                recordMapper.updateById(record);
            }
        }
        groupMapper.deleteById(groupId);
    }

    public ContentFavoriteGroupDO getOrCreateDefaultGroup(Long userId) {
        ContentFavoriteGroupDO defaultGroup = groupMapper.selectDefaultGroup(userId);
        if (defaultGroup != null) {
            return defaultGroup;
        }
        ContentFavoriteGroupDO group = new ContentFavoriteGroupDO();
        group.setUserId(userId);
        group.setGroupName(defaultGroupName());
        group.setIsDefault(1);
        groupMapper.insert(group);
        return group;
    }

    /**
     * 切换收藏状态。
     *
     * @return true if favorited after operation
     */
    public boolean toggleFavorite(Long userId, FavoriteActionReqBO reqBO) {
        if (userId == null || reqBO.getContentId() == null) {
            return false;
        }
        ContentFavoriteRecordDO record = recordMapper.selectOne(userId, reqBO.getContentId());
        if (record != null && !Boolean.TRUE.equals(record.getDeleted())) {
            record.setDeleted(1);
            record.setUpdateTime(LocalDateTime.now());
            recordMapper.updateById(record);
            return false;
        }
        Long groupId = reqBO.getGroupId();
        if (groupId == null) {
            groupId = getOrCreateDefaultGroup(userId).getId();
        } else {
            ContentFavoriteGroupDO group = getGroup(userId, groupId);
            if (group == null) {
                groupId = getOrCreateDefaultGroup(userId).getId();
            }
        }

        if (record == null) {
            record = new ContentFavoriteRecordDO();
            record.setContentId(reqBO.getContentId());
            record.setUserId(userId);
        }
        record.setGroupId(groupId);
        record.setTags(reqBO.getTags());
        record.setSource(reqBO.getSource());
        record.setNote(reqBO.getNote());
        record.setExtra(reqBO.getExtra());
        record.setDeleted(0);
        record.setUpdateTime(LocalDateTime.now());
        if (record.getId() == null) {
            recordMapper.insert(record);
        } else {
            recordMapper.updateById(record);
        }
        log.debug("User {} favorite content {} group {}", userId, reqBO.getContentId(), groupId);
        return true;
    }

    public List<ContentFavoriteRecordDO> getFavoriteRecords(Long userId) {
        return recordMapper.selectByUser(userId);
    }

    private String defaultGroupName() {
        return "默认收藏";
    }
}
