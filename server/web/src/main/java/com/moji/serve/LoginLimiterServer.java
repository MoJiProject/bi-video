package com.moji.serve;

import cn.dev33.satoken.stp.StpUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Objects;

public class LoginLimiterServer {


    private static final String ATTEMPTS_KEY_PREFIX_LOGIN = "login_attempts:";
    private static final String TIMESTAMP_KEY_PREFIX_LOGIN = "login_timestamp:";
    private static final String ATTEMPTS_KEY_PREFIX_SIGN = "sign_attempts:";
    private static final String TIMESTAMP_KEY_PREFIX_SIGN = "sign_timestamp:";
    private static final String ATTEMPTS_KEY_PREFIX_PUT = "put_attempts:";
    private static final String TIMESTAMP_KEY_PREFIX_PUT = "put_timestamp:";
    private static final String ATTEMPTS_KEY_PREFIX_AUTO_LOGIN = "auto_login_attempts:";
    private static final int MAX_ATTEMPTS = 10;  // 最大尝试次数
    private static final int TIME_WINDOW = 300;  // 时间窗口，单位：秒 (1小时)

    private static final int AUTO_LOGIN_TIME_WINDOW = 604800;

    private static final String PASS_WORD="";

    private Jedis jedis;




    public LoginLimiterServer() {

        this.jedis = new Jedis("localhost",6379);  // 创建连接
        this.jedis.auth(PASS_WORD);
    }

    /**
     * 允许登录的次数
     * @param userIp
     * @return
     */
    public boolean isAllowedToLogin(String userIp) {
        String attemptsKey = ATTEMPTS_KEY_PREFIX_LOGIN + userIp;  // 登录尝试次数键
        String timestampKey = TIMESTAMP_KEY_PREFIX_LOGIN + userIp;  // 登录时间戳键

        // 获取当前时间戳
        long currentTime = System.currentTimeMillis() / 1000;  // 当前时间，单位：秒

        // 获取当前的尝试次数和时间戳
        String attemptsStr = jedis.get(attemptsKey);
        String timestampStr = jedis.get(timestampKey);

        // 初始化 SetParams，用于设置过期时间
        SetParams setParams = new SetParams();
        setParams.ex(TIME_WINDOW);  // 设置过期时间为 TIME_WINDOW 秒

        // 判断用户的尝试次数和时间戳
        if (attemptsStr != null && timestampStr != null) {
            int attempts = Integer.parseInt(attemptsStr);
            long timestamp = Long.parseLong(timestampStr);

            // 如果时间窗口已过，重置尝试次数
            if (currentTime - timestamp > TIME_WINDOW) {
                // 重置尝试次数和时间戳，并设置过期时间
                jedis.set(attemptsKey, "1", setParams);  // 重置为1次尝试
                jedis.set(timestampKey, String.valueOf(currentTime), setParams);  // 更新时间戳
                return true;  // 允许登录
            } else {
                // 时间窗口内，检查是否超出最大尝试次数
                if (attempts >= MAX_ATTEMPTS) {
                    return false;  // 达到最大尝试次数，拒绝登录
                } else {
                    // 增加尝试次数
                    jedis.set(attemptsKey, String.valueOf(attempts + 1), setParams);  // 增加尝试次数
                    return true;  // 允许登录
                }
            }
        } else {
            // 第一次尝试登录，初始化尝试次数和时间戳
            jedis.set(attemptsKey, "1", setParams);  // 初次尝试
            jedis.set(timestampKey, String.valueOf(currentTime), setParams);  // 设置当前时间戳
            return true;  // 允许登录
        }
    }


    /**
     * 允许注册的次数
     * @param userIp
     * @return
     */
    public boolean isAllowedToSign(String userIp) {
        String attemptsKey = ATTEMPTS_KEY_PREFIX_SIGN + userIp;  // 登录尝试次数键
        String timestampKey = TIMESTAMP_KEY_PREFIX_SIGN + userIp;  // 登录时间戳键

        // 获取当前时间戳
        long currentTime = System.currentTimeMillis() / 1000;  // 当前时间，单位：秒

        // 获取当前的尝试次数和时间戳
        String attemptsStr = jedis.get(attemptsKey);
        String timestampStr = jedis.get(timestampKey);

        // 初始化 SetParams，用于设置过期时间
        SetParams setParams = new SetParams();
        setParams.ex(TIME_WINDOW);  // 设置过期时间为 TIME_WINDOW 秒

        // 判断用户的尝试次数和时间戳
        if (attemptsStr != null && timestampStr != null) {
            int attempts = Integer.parseInt(attemptsStr);
            long timestamp = Long.parseLong(timestampStr);

            // 如果时间窗口已过，重置尝试次数
            if (currentTime - timestamp > TIME_WINDOW) {
                // 重置尝试次数和时间戳，并设置过期时间
                jedis.set(attemptsKey, "1", setParams);  // 重置为1次尝试
                jedis.set(timestampKey, String.valueOf(currentTime), setParams);  // 更新时间戳
                return true;  // 允许登录
            } else {
                // 时间窗口内，检查是否超出最大尝试次数
                if (attempts >= MAX_ATTEMPTS) {
                    return false;  // 达到最大尝试次数，拒绝登录
                } else {
                    // 增加尝试次数
                    jedis.set(attemptsKey, String.valueOf(attempts + 1), setParams);  // 增加尝试次数
                    return true;  // 允许登录
                }
            }
        } else {
            // 第一次尝试登录，初始化尝试次数和时间戳
            jedis.set(attemptsKey, "1", setParams);  // 初次尝试
            jedis.set(timestampKey, String.valueOf(currentTime), setParams);  // 设置当前时间戳
            return true;  // 允许登录
        }
    }


    /**
     * 允许修改的次数
     * @param userIp
     * @return
     */
    public boolean isAllowedToPut(String userIp) {
        String attemptsKey = ATTEMPTS_KEY_PREFIX_PUT + userIp;  // 登录尝试次数键
        String timestampKey = TIMESTAMP_KEY_PREFIX_PUT + userIp;  // 登录时间戳键

        // 获取当前时间戳
        long currentTime = System.currentTimeMillis() / 1000;  // 当前时间，单位：秒

        // 获取当前的尝试次数和时间戳
        String attemptsStr = jedis.get(attemptsKey);
        String timestampStr = jedis.get(timestampKey);

        // 初始化 SetParams，用于设置过期时间
        SetParams setParams = new SetParams();
        setParams.ex(TIME_WINDOW);  // 设置过期时间为 TIME_WINDOW 秒

        // 判断用户的尝试次数和时间戳
        if (attemptsStr != null && timestampStr != null) {
            int attempts = Integer.parseInt(attemptsStr);
            long timestamp = Long.parseLong(timestampStr);

            // 如果时间窗口已过，重置尝试次数
            if (currentTime - timestamp > TIME_WINDOW) {
                // 重置尝试次数和时间戳，并设置过期时间
                jedis.set(attemptsKey, "1", setParams);  // 重置为1次尝试
                jedis.set(timestampKey, String.valueOf(currentTime), setParams);  // 更新时间戳
                return true;  // 允许登录
            } else {
                // 时间窗口内，检查是否超出最大尝试次数
                if (attempts >= MAX_ATTEMPTS) {
                    return false;  // 达到最大尝试次数，拒绝登录
                } else {
                    // 增加尝试次数
                    jedis.set(attemptsKey, String.valueOf(attempts + 1), setParams);  // 增加尝试次数
                    return true;  // 允许登录
                }
            }
        } else {
            // 第一次尝试登录，初始化尝试次数和时间戳
            jedis.set(attemptsKey, "1", setParams);  // 初次尝试
            jedis.set(timestampKey, String.valueOf(currentTime), setParams);  // 设置当前时间戳
            return true;  // 允许登录
        }
    }


    /**
     * 存储自动登录键
     */
    public void setAutoLogin(String userIp,String token) {

        String attemptsKey = ATTEMPTS_KEY_PREFIX_AUTO_LOGIN + userIp;
        jedis.del(attemptsKey);

        // 初始化 SetParams，用于设置过期时间
        SetParams setParams = new SetParams();
        setParams.ex(AUTO_LOGIN_TIME_WINDOW);  // 设置过期时间
        jedis.set(attemptsKey, token, setParams);
    }


    /**
     * 是否允许登录
     * @param userIp
     * @return
     */
    public String isAutoLogin(String userIp){

        String attemptsKey = ATTEMPTS_KEY_PREFIX_AUTO_LOGIN + userIp;

        return jedis.get(attemptsKey);

    }


    /**
     * 退出登录
     * @param userIp
     */
    public void delAutoLogin(String userIp){

        String attemptsKey = ATTEMPTS_KEY_PREFIX_AUTO_LOGIN + userIp;
        jedis.del(attemptsKey);

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

        if(Objects.equals(jedis.get(remoteAddr + videoId), "1"))
         return false;
        else {
            // 初始化 SetParams，用于设置过期时间
            SetParams setParams = new SetParams();
            setParams.ex(second);  // 设置过期时间
            jedis.set(remoteAddr+videoId,"1",setParams);
        }
        return true;
    }
}
