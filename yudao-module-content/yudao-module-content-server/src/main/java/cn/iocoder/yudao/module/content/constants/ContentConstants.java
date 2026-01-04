package cn.iocoder.yudao.module.content.constants;

/**
 * 内容模块常量类
 *
 * @author 阳光团宠
 */
public class ContentConstants {

    /**
     * 热度计算权重
     */
    public static final class HotScoreWeight {
        /** 浏览权重 */
        public static final double VIEW = 1.0;
        /** 点赞权重 */
        public static final double LIKE = 3.0;
        /** 评论权重 */
        public static final double COMMENT = 5.0;
        /** 分享权重 */
        public static final double SHARE = 8.0;
    }

    /**
     * 时间常量
     */
    public static final class Time {
        /** 热门内容统计天数(天) */
        public static final int HOT_CONTENT_DAYS = 7;
        /** 一周小时数(小时) */
        public static final long HOURS_PER_WEEK = 168L;
        /** 推荐分数最小衰减系数 */
        public static final double MIN_TIME_DECAY = 0.1;
    }

    /**
     * 内容来源
     */
    public static final class Source {
        /** 首页 */
        public static final String HOME = "home";
        /** 搜索 */
        public static final String SEARCH = "search";
        /** 话题 */
        public static final String TOPIC = "topic";
        /** 用户主页 */
        public static final String PROFILE = "profile";
        /** 推荐流 */
        public static final String RECOMMEND = "recommend";
    }

    /**
     * Kafka主题名称
     */
    public static final class KafkaTopic {
        /** 内容举报主题 */
        public static final String CONTENT_REPORT = "xiaolvshu-content-report";
        /** 用户行为主题 */
        public static final String USER_BEHAVIOR = "xiaolvshu-user-behavior";
        /** 内容互动主题 */
        public static final String CONTENT_INTERACTION = "xiaolvshu-content-interaction";
    }

    /**
     * 行为类型
     */
    public static final class BehaviorType {
        /** 浏览 */
        public static final String VIEW = "view";
        /** 点赞 */
        public static final String LIKE = "like";
        /** 收藏 */
        public static final String COLLECT = "collect";
        /** 分享 */
        public static final String SHARE = "share";
        /** 举报 */
        public static final String REPORT = "report";
        /** 评论 */
        public static final String COMMENT = "comment";
    }

    /**
     * 操作动作
     */
    public static final class Action {
        /** 添加/执行 */
        public static final String ADD = "add";
        /** 取消 */
        public static final String CANCEL = "cancel";
    }
}
