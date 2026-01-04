package cn.iocoder.yudao.module.message.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群消息 DO
 *
 * @author xiaolvshu
 */
@TableName("group_message")
@Data
public class GroupMessageDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long groupId;

    private Long fromUserId;

    private Integer type;

    private String content;

    private String extraData;

    /**
     * 0-正常 1-撤回 2-删除
     */
    private Integer status;

    /**
     * 删除标记：0-未删除 1-已删除
     */
    private Integer deleted;

    private LocalDateTime recallTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
