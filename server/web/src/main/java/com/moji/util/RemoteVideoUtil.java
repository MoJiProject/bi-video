package com.moji.util;

/**
 * 远程视频直链校验
 */
public class RemoteVideoUtil {

    private static final int MAX_URL_LENGTH = 1000;

    private RemoteVideoUtil() {
    }

    /**
     * 校验远程视频直链，仅允许http/https
     *
     * @return 规范化后的地址，不合法返回null
     */
    public static String parseRemoteUrl(String raw) {
        if (raw == null) {
            return null;
        }
        //去掉首尾空白以及复制链接时可能带进来的逗号、分号、句号
        String value = raw.trim().replaceAll("^[,;.\\s]+|[,;.\\s]+$", "");
        if (value.isEmpty() || value.length() > MAX_URL_LENGTH) {
            return null;
        }
        if (value.startsWith("//")) {
            value = "https:" + value;
        }
        String lower = value.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            return null;
        }
        // 拒绝包含空白与控制字符的地址
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i)) || Character.isISOControl(value.charAt(i))) {
                return null;
            }
        }
        return value;
    }

    public static boolean isValidRemoteUrl(String url) {
        return parseRemoteUrl(url) != null;
    }
}
