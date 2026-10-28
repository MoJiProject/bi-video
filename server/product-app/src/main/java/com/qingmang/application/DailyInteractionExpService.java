package com.qingmang.application;

import com.qingmang.common.constant.BizConstants;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 每日互动经验奖励：点赞 / 收藏 / 分享 每天前 {@link BizConstants#DAILY_INTERACTION_LIMIT} 次给操作者加经验。
 *
 * <p>用 Redis INCR 按天计数，key 形如
 * {@code product:exp:daily:{userId}:{yyyyMMdd}:{type}}，
 * TTL 设 48 小时（跨两个自然日），到期自动清理，无需定时任务。</p>
 */
@Service
public class DailyInteractionExpService {

    private static final DateTimeFormatter DAY_KEY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Duration KEY_TTL = Duration.ofHours(48);
    private static final String KEY_PREFIX = "product:exp:daily:";

    public static final String TYPE_LIKE = "like";
    public static final String TYPE_FAVORITE = "favorite";
    public static final String TYPE_SHARE = "share";

    private final StringRedisTemplate redisTemplate;
    private final AuthService authService;

    public DailyInteractionExpService(StringRedisTemplate redisTemplate, AuthService authService) {
        this.redisTemplate = redisTemplate;
        this.authService = authService;
    }

    /** 当天该类互动尚未达上限时给操作者加经验；超过次数不再奖励。 */
    public void awardOnDailyInteraction(Long userId, String type, int exp) {
        if (userId == null || exp <= 0) {
            return;
        }
        String key = KEY_PREFIX + userId + ":" + LocalDate.now().format(DAY_KEY) + ":" + type;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, KEY_TTL);
        }
        if (count != null && count <= BizConstants.DAILY_INTERACTION_LIMIT) {
            authService.addExp(userId, exp);
        }
    }

    /** 每日点赞奖励（每天前 3 次）。 */
    public void awardLike(Long userId) {
        awardOnDailyInteraction(userId, TYPE_LIKE, BizConstants.DAILY_LIKE_EXP);
    }

    /** 每日收藏奖励（每天前 3 次）。 */
    public void awardFavorite(Long userId) {
        awardOnDailyInteraction(userId, TYPE_FAVORITE, BizConstants.DAILY_FAVORITE_EXP);
    }

    /** 每日分享奖励（每天前 3 次）。 */
    public void awardShare(Long userId) {
        awardOnDailyInteraction(userId, TYPE_SHARE, BizConstants.DAILY_SHARE_EXP);
    }
}
