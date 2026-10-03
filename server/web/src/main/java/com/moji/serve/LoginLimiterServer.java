package com.moji.serve;

import cn.dev33.satoken.stp.StpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Objects;
import java.util.function.Function;

public class LoginLimiterServer {

    private static final Logger logger = LoggerFactory.getLogger(LoginLimiterServer.class);

    //播放量去重窗口的兜底时长(秒)，用于时长未知(如远程视频"00:00")的场景
    private static final int DEFAULT_PLAY_DEDUPE_SECOND = 60;


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

    private static String redisHost = "localhost";

    private static int redisPort = 6379;

    private static String redisPassword;

    private static JedisPool jedisPool;




    public LoginLimiterServer() {
    }

    public static synchronized void configureRedis(String host, int port, String password) {
        redisHost = host;
        redisPort = port;
        redisPassword = password;

        if (jedisPool != null) {
            jedisPool.close();
            jedisPool = null;
        }
    }

    private static synchronized JedisPool getJedisPool() {
        if (jedisPool == null) {
            jedisPool = createJedisPool();
        }
        return jedisPool;
    }

    private static JedisPool createJedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);
        if (redisPassword == null || redisPassword.isBlank()) {
            return new JedisPool(poolConfig, redisHost, redisPort, 2000);
        }
        return new JedisPool(poolConfig, redisHost, redisPort, 2000, redisPassword);
    }

    private <T> T execute(Function<Jedis, T> action) {
        try (Jedis jedis = getJedisPool().getResource()) {
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

        if(remoteAddr==null||videoId==null||videoTime==null||videoTime.trim().isEmpty())
            return false;

        //远程视频的时长是占位的"00:00"，解析出来是0。
        //原来直接 setex(key,0,...) 会让Redis抛 invalid expire time，
        //导致远程视频的播放量接口每次都500，播放量永远加不上。这里兜一个默认去重窗口。
        int expirationSecond=parseVideoSeconds(videoTime);
        if(expirationSecond<=0)
            expirationSecond=DEFAULT_PLAY_DEDUPE_SECOND;

        String key=remoteAddr+"_"+videoId;
        //lambda里只能引用 Effectively Final 的局部变量
        final long expireSeconds=expirationSecond;
        try {
            if(Objects.equals(execute(jedis -> jedis.get(key)), "1"))
                return false;
            execute(jedis -> jedis.setex(key, expireSeconds, "1"));
        }catch (Exception e){
            //Redis异常不应该让播放量接口整体失败，放行本次计数
            logger.warn("播放去重写入失败 videoId={} err={}",videoId,e.getMessage());
            return true;
        }
        return true;
    }

    /**
     * 把 mm:ss / ss 形式的时长解析成秒，解析不了返回0
     */
    private int parseVideoSeconds(String videoTime) {

        String[] split = videoTime.trim().split(":");
        try {
            if(split.length==1)
                return Integer.parseInt(split[0].trim());
            if(split.length==2)
                return Integer.parseInt(split[0].trim())*60+Integer.parseInt(split[1].trim());
        }catch (NumberFormatException e){
            return 0;
        }
        return 0;
    }
}
