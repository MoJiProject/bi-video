package com.qingmang.common.constant;

/**
 * 业务常量。
 *
 * <p>老代码里散落着 {@code "全部"}、{@code "发布时间排序"}、{@code "稍后再看"} 这类中文魔法值，
 * 前后端各写一遍，改一次漏一处就出线上故障。这里集中收口，
 * 需要进数据库的值请改用外键，不要再用字符串。</p>
 */
public final class BizConstants {

    private BizConstants() {
    }

    /** 每个新用户自动创建的默认收藏夹名称。 */
    public static final String DEFAULT_FOLDER_NAME = "默认收藏夹";

    /** 默认收藏夹描述。 */
    public static final String DEFAULT_FOLDER_DESC = "自动创建";

    /** 默认头像。 */
    public static final String DEFAULT_AVATAR = "/img/avatar-default.png";

    /** 视频封面占位图。 */
    public static final String DEFAULT_COVER = "/img/cover-fallback.png";

    /** 用户最大等级，与前端等级徽章一一对应。 */
    public static final int MAX_USER_LEVEL = 6;

    /** 单个视频单个用户最多可投的硬币数。 */
    public static final int MAX_COIN_PER_VIDEO_PER_USER = 2;

    /** 投币一次的数量。 */
    public static final int COIN_PER_THROW = 1;

    /** 注册赠送硬币。 */
    public static final int REGISTER_REWARD_COIN = 5;

    /** 每日登录赠送硬币。 */
    public static final int LOGIN_REWARD_COIN = 1;

    /** 等级经验阈值：升到 N 级所需累计经验。 */
    public static final int LEVEL_EXP_BASE = 100;

    /** 列表接口默认页大小。 */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** 列表接口最大页大小，防止 pageSize 被传成 100000 拖垮数据库。 */
    public static final int MAX_PAGE_SIZE = 100;

    /** 系统配置缓存 key 前缀。 */
    public static final String CONFIG_CACHE_PREFIX = "product:config:";

    /** 分类列表缓存 key。 */
    public static final String CATEGORY_CACHE_KEY = "product:category:list";

    /** 热门搜索词缓存 key。 */
    public static final String HOT_KEYWORD_CACHE_KEY = "product:search:hot";

    /** 分类缓存时长（秒）。 */
    public static final long CACHE_TTL_CATEGORY = 1800L;

    /** 系统配置缓存时长（秒）。 */
    public static final long CACHE_TTL_CONFIG = 600L;

    /** 热门搜索词缓存时长（秒）。 */
    public static final long CACHE_TTL_HOT_KEYWORD = 300L;
}