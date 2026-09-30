package com.moji.serve;

import cn.dev33.satoken.stp.StpUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Objects;
import java.util.function.Function;

public class LoginLimiterServer {


    private static final String ATTEMPTS_KEY_PREFIX_LOGIN = "login_attempts:";
    private static final String ATTEMPTS_KEY_PREFIX_SIGN = "sign_attempts:";
    private static final String ATTEMPTS_KEY_PREFIX_PUT = "put_attempts:";
    private static final String ATTEMPTS_KEY_PREFIX_AUTO_LOGIN = "auto_login_attempts:";

    // 注册限流常量
    private static final String ATTEMPTS_KEY_PREFIX_REG = "reg:attempts:";
    // 24小时 秒数
    private static final int REG_TIME_WINDOW = 86400;
    // 一天最多注册3次
    private static final int MAX_REG_ATTEMPTS = 3;
    private static final int MAX_ATTEMPTS = 10;  // 最大尝试次数
    private static final int TIME_WINDOW = 300;  // 时间窗口，单位：秒 (5分钟)

    private static final int AUTO_LOGIN_TIME_WINDOW = 604800;

    private static final String PASS_WORD="1234";

    private static final JedisPool JEDIS_POOL = createJedisPool();




    public LoginLimiterServer() {
    }

    private static JedisPool createJedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);
        return new JedisPool(poolConfig, "localhost", 6379, 2000, PASS_WORD);
    }

    private <T> T execute(Function<Jedis, T> action) {
        try (Jedis jedis = JEDIS_POOL.getResource()) {
            return action.apply(jedis);
        }
    }

    private boolean isAllowed(String prefix, int maxAttempts, int timeWindow, String... parts) {
        String key = prefix + buildKey(parts);
        Long attempts = execute(jedis -> {
            Long count = jedis.incr(key);
            if (count == 1) {
                jedis.expire(key, (long) timeWindow);
            }
            return count;
        });
        return attempts <= maxAttempts;
    }

    private String buildKey(String... parts) {
        StringBuilder key = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            if (key.length() > 0) {
                key.append(':');
            }
            key.append(part.trim().toLowerCase());
        }
        return key.length() == 0 ? "unknown" : key.toString();
    }

    /**
     * 允许登录的次数
     * @param userIp
     * @return
     */
    public boolean isAllowedToLogin(String userIp) {
        return isAllowed(ATTEMPTS_KEY_PREFIX_LOGIN + "ip:", MAX_ATTEMPTS, TIME_WINDOW, userIp);
    }

    public boolean isAllowedToLogin(String userIp, String userName) {
        return isAllowedToLogin(userIp)
                && isAllowed(ATTEMPTS_KEY_PREFIX_LOGIN + "user:", MAX_ATTEMPTS, TIME_WINDOW, userName)
                && isAllowed(ATTEMPTS_KEY_PREFIX_LOGIN + "ip_user:", MAX_ATTEMPTS, TIME_WINDOW, userIp, userName);
    }


    /**
     * 允许注册的次数
     * @param userIp
     * @return
     */
    public boolean isAllowedToSign(String userIp) {
        return isAllowed(ATTEMPTS_KEY_PREFIX_SIGN + "ip:", MAX_ATTEMPTS, TIME_WINDOW, userIp);
    }

    public boolean isAllowedToSign(String userIp, String userName) {
        return isAllowedToSign(userIp)
                && isAllowed(ATTEMPTS_KEY_PREFIX_SIGN + "user:", MAX_ATTEMPTS, TIME_WINDOW, userName)
                && isAllowed(ATTEMPTS_KEY_PREFIX_SIGN + "ip_user:", MAX_ATTEMPTS, TIME_WINDOW, userIp, userName);
    }


    /**
     * 校验IP是否允许注册
     * 规则：同一IP 24小时内最多成功注册3次
     * @param userIp 用户IP
     * @return true=允许注册 false=禁止注册
     */
    public boolean checkRegisterLimit(String userIp) {
        String attemptsKey = ATTEMPTS_KEY_PREFIX_REG + buildKey(userIp);
        String attemptsStr = execute(jedis -> jedis.get(attemptsKey));
        return attemptsStr == null || Integer.parseInt(attemptsStr) < MAX_REG_ATTEMPTS;
    }

    /**
     * 注册成功后，增加IP注册次数计数
     * @param userIp 用户IP
     */
    public void incrRegisterCount(String userIp) {
        String attemptsKey = ATTEMPTS_KEY_PREFIX_REG + buildKey(userIp);
        execute(jedis -> {
            Long count = jedis.incr(attemptsKey);
            if (count == 1) {
                jedis.expire(attemptsKey, (long) REG_TIME_WINDOW);
            }
            return count;
        });
    }


    /**
     * 允许修改的次数
     * @param userIp
     * @return
     */
    public boolean isAllowedToPut(String userIp) {
        return isAllowed(ATTEMPTS_KEY_PREFIX_PUT + "ip:", MAX_ATTEMPTS, TIME_WINDOW, userIp);
    }

    public boolean isAllowedToPut(String userIp, String userName) {
        return isAllowedToPut(userIp)
                && isAllowed(ATTEMPTS_KEY_PREFIX_PUT + "user:", MAX_ATTEMPTS, TIME_WINDOW, userName)
                && isAllowed(ATTEMPTS_KEY_PREFIX_PUT + "ip_user:", MAX_ATTEMPTS, TIME_WINDOW, userIp, userName);
    }


    /**
     * 存储自动登录键
     */
    public void setAutoLogin(String userIp,String token) {

        String attemptsKey = ATTEMPTS_KEY_PREFIX_AUTO_LOGIN + userIp;
        execute(jedis -> jedis.del(attemptsKey));

        execute(jedis -> jedis.setex(attemptsKey, (long) AUTO_LOGIN_TIME_WINDOW, token));
    }


    /**
     * 是否允许登录
     * @param userIp
     * @return
     */
    public String isAutoLogin(String userIp){

        String attemptsKey = ATTEMPTS_KEY_PREFIX_AUTO_LOGIN + userIp;

        return execute(jedis -> jedis.get(attemptsKey));

    }


    /**
     * 退出登录
     * @param userIp
     */
    public void delAutoLogin(String userIp){

        String attemptsKey = ATTEMPTS_KEY_PREFIX_AUTO_LOGIN + userIp;
        execute(jedis -> jedis.del(attemptsKey));

    }


    /**
     * 检查是否是正确对应的用户发送的请求
     * @param userId
     * @return
     */
    public boolean checkUser(Integer userId,String token){

        if(userId==null)
            return false;

        Object userId1 =  StpUtil.getLoginIdByToken(token);
        return String.valueOf(userId).equals(userId1);

    }


    /**
     * 判断用户是否是一个视频的时间才发送的请求
     * @param videoTime
     * @param remoteAddr
     * @param videoId
     * @return
     */
    public boolean updateVideoPlayer(String videoTime, String remoteAddr, Integer videoId) {

        if(remoteAddr==null||videoId==null||videoTime==null)
            return false;

        String[] split = videoTime.split(":");
        int second=0;
        if(split.length==1)
            second= Integer.parseInt(split[0]);
        else if(split.length==2)
            second=Integer.parseInt(split[0])*60+Integer.parseInt(split[1]);

        int expirationSecond = second;

        if(Objects.equals(execute(jedis -> jedis.get(remoteAddr + videoId)), "1"))
         return false;
        else {
            execute(jedis -> jedis.setex(remoteAddr+videoId, (long) expirationSecond, "1"));
        }
        return true;
    }
}
