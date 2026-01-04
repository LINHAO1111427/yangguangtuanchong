-- ============================================
-- 阳光团宠支付模块 - 业务扩展表
-- ============================================
-- 说明：
-- 1. 基础支付功能使用芋道框架的 pay_* 表（pay_app, pay_channel, pay_order, pay_refund, pay_transfer）
-- 2. 本文件仅包含阳光团宠特有的业务表
-- ============================================

-- 1. 充值套餐表（阳光团宠业务特有）
CREATE TABLE IF NOT EXISTS recharge_package (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    package_name VARCHAR(100) NOT NULL COMMENT '套餐名称',
    price DECIMAL(10,2) NOT NULL COMMENT '充值金额（单位：元）',
    coins INT NOT NULL COMMENT '赠送币数',
    discount_coins INT DEFAULT 0 COMMENT '额外赠送币数（促销活动）',
    is_recommended TINYINT DEFAULT 0 COMMENT '是否推荐（0否/1是）',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) COMMENT '创建者',
    updated_by VARCHAR(64) COMMENT '更新者',
    deleted_at TIMESTAMP NULL COMMENT '删除时间',
    INDEX idx_is_active (is_active),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值套餐表';

-- 2. 打赏分成记录表（阳光团宠业务特有）
CREATE TABLE IF NOT EXISTS reward_settlement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    settlement_id VARCHAR(50) NOT NULL UNIQUE COMMENT '分成单号',
    reward_id BIGINT NOT NULL COMMENT '打赏ID（关联 pay_order.id）',
    original_amount DECIMAL(10,2) NOT NULL COMMENT '原始打赏金额',
    platform_ratio DECIMAL(5,2) NOT NULL COMMENT '平台分成比例（%）',
    platform_income DECIMAL(10,2) NOT NULL COMMENT '平台收入',
    author_income DECIMAL(10,2) NOT NULL COMMENT '作者收入',
    settle_time TIMESTAMP COMMENT '结算时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_reward_id (reward_id),
    INDEX idx_settle_time (settle_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打赏分成记录表';

-- ============================================
-- 初始化数据
-- ============================================

-- 初始化充值套餐数据
INSERT IGNORE INTO recharge_package (package_name, price, coins, discount_coins, is_recommended, sort_order) VALUES
('新手礼包', 6.99, 100, 10, 1, 1),
('经济套餐', 12.99, 250, 0, 0, 2),
('划算套餐', 25.99, 600, 50, 1, 3),
('豪华套餐', 49.99, 1500, 200, 0, 4),
('至尊套餐', 99.99, 3500, 500, 0, 5);
