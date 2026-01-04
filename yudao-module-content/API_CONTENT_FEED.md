# 内容 Feed & 收藏 API 对接指南

> 所有接口均遵循项目强制规则，路径统一以 `/api/v1.0.1` 开头，参数全部使用 `snake_case`。

## Feed 流接口

| 功能 | Method | Path | 说明 |
| --- | --- | --- | --- |
| 获取首页 Feed 流 | GET | `/api/v1.0.1/content/feed/stream` | 返回内容卡片 + 广告混排 |
| 浏览记录 | GET | `/api/v1.0.1/content/feed/history` | 读取最近浏览的内容，默认 20 条 |
| 快捷收藏 | POST | `/api/v1.0.1/content/feed/quick_collect` | 一键收藏到指定分组 |
| 打赏汇总 | GET | `/api/v1.0.1/content/feed/reward_summary` | 返回作品打赏&作者收益 |

### Feed 请求参数

```
GET /api/v1.0.1/content/feed/stream?page_no=1&page_size=20&scene=home&include_ads=true&ad_interval=5
```

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `page_no` | int | 页码，>=1 |
| `page_size` | int | 每页数量，1~50 |
| `scene` | string | 场景标识：`home` / `topic` / `search` |
| `include_ads` | bool | 是否混入广告 |
| `ad_interval` | int | 广告间隔，>=3 |

### Feed 返回结构（关键字段）

```
{
  "cards": [
    {
      "card_type": "content",
      "layout": "vertical-video",
      "strategy": "tag_match",
      "score": 0.93,
      "reward_amount": 1200,
      "content": { ...ContentListRespVO... }
    },
    {
      "card_type": "ad",
      "layout": "single-ad",
      "strategy": "ad_slot",
      "ad": {
        "ad_id": 1001,
        "title": "品牌推广",
        "cover_image": "...",
        "jump_url": "https://...",
        "call_to_action": "去看看"
      }
    }
  ],
  "total": 200,
  "strategy_summary": "tag_match:8|follow:4|hot:6|explore:2"
}
```

### 浏览记录

```
GET /api/v1.0.1/content/feed/history?limit=30
```
返回 `ContentListRespVO` 列表，按照最近浏览倒序。

### 快捷收藏

```
POST /api/v1.0.1/content/feed/quick_collect
{
  "content_id": 123,
  "group_id": 456   // 可空，自动落入默认分组
}
```

### 打赏汇总

```
GET /api/v1.0.1/content/feed/reward_summary?content_id=123&author_id=456
```

返回：

```
{
  "post_reward_amount": 3600,   // 作品累计打赏金额（分）
  "author_income_amount": 4200  // 作者收益（分）
}
```

## 收藏管理接口

| 功能 | Method | Path |
| --- | --- | --- |
| 查询分组 | GET | `/api/v1.0.1/content/favorite/groups` |
| 创建分组 | POST | `/api/v1.0.1/content/favorite/groups` |
| 更新分组 | PUT | `/api/v1.0.1/content/favorite/groups/{id}` |
| 删除分组 | DELETE | `/api/v1.0.1/content/favorite/groups/{id}` |
| 查询收藏记录 | GET | `/api/v1.0.1/content/favorite/records` |
| 收藏/取消收藏（带标签） | POST | `/api/v1.0.1/content/favorite/records/toggle` |

### 典型请求体

**创建/更新分组**
```json
{
  "group_name": "我的口红墙",
  "description": "口红测评收藏夹",
  "color": "#FF6B81",
  "cover_image": "https://cdn.xxx/cover.png",
  "tag_list": ["种草", "买前必看"],
  "extra": {
    "display_mode": "two-column"
  }
}
```

**收藏记录（附带标签）**
```json
{
  "content_id": 123,
  "group_id": 456,
  "tags": ["穿搭灵感", "秋冬"],
  "note": "准备双十一再看",
  "source": 2,
  "extra": {
    "from_feed": "home"
  }
}
```

返回的 `FavoriteRecordRespVO` 包含 `content_id/group_id/tags/note/source/extra/create_time` 等字段，方便前端直接同步展示。
