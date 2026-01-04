package cn.iocoder.yudao.module.message.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群组信息 DO
 *
 * @author xiaolvshu
 */
@TableName("group_info")
@Data
public class GroupInfoDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String groupName;

    private String avatar;

    private Long ownerUserId;

    private String announcement;

    private String description;

    private Integer memberCount;

    private Integer maxMemberCount;

    /**
     * 0-自由加入 1-需要验证 2-禁止加入
     */
    private Integer joinType;

    /**
     * 0-正常 1-已解散
     */
    private Integer status;

    /**
     * 0-否 1-是
     */
    private Integer muteAll;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime dissolveTime;
}
