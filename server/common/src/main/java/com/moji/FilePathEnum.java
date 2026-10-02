package com.moji;

import lombok.Getter;
import org.springframework.core.env.Environment;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 上传目录配置。
 * <p>
 * 路径不再硬编码，统一由 application.yml 的 file.upload 配置项提供，
 * 服务启动时由 {@link UploadPathInitializer} 解析并缓存。
 * <p>
 * 注意：{@link #getPath()} 返回的路径保证以文件分隔符结尾，
 * 以兼容现有代码中 getPath() + fileName 的直接拼接方式。
 */
@Getter
public enum FilePathEnum {

    /** 评论图片上传目录 */
    UPLOAD_COMMENT_IMG("file.upload.comment-img", "comment", "评论图片"),
    /** 用户头像上传目录 */
    UPLOAD_AVATAR("file.upload.avatar", "avatar", "用户头像"),
    /** 用户视频封面上传目录 */
    UPLOAD_VIDEO_COVER("file.upload.video-cover", "video" + File.separator + "cover", "用户视频封面"),
    /** 用户视频上传目录 */
    UPLOAD_VIDEO("file.upload.video", "video", "用户视频"),
    /** 用户聊天图片上传目录 */
    UPLOAD_IMG_MESSAGE("file.upload.message-img", "message", "用户聊天图片"),
    /** 用户收藏夹封面上传目录 */
    UPLOAD_IMG_COLLECT_CLASSIFY("file.upload.collect-classify", "collectClassify", "用户收藏夹封面"),
    /** 用户主页背景上传目录 */
    UPLOAD_IMG_BACKGROUND("file.upload.background", "background", "用户主页背景"),
    /** 用户动态上传目录 */
    UPLOAD_IMG_DYNAMIC("file.upload.dynamic", "dynamic", "用户动态");

    /** 上传根目录配置项 */
    public static final String ROOT_KEY = "file.upload.root";
    /** 磁盘安全剩余容量配置项 */
    public static final String CAPACITY_KEY = "file.upload.safety-capacity";
    /** 安全容量默认值，配置缺失时使用 */
    private static final String DEFAULT_CAPACITY = "5GB";

    private static final Map<FilePathEnum, String> PATH_CACHE = new ConcurrentHashMap<>();
    private static volatile Long safetyCapacity;

    /** 配置项key，可单独覆盖该目录路径 */
    private final String configKey;
    /** 相对于上传根目录的子目录名 */
    private final String dirName;
    /** 中文描述，用于报错提示 */
    private final String description;

    FilePathEnum(String configKey, String dirName, String description) {
        this.configKey = configKey;
        this.dirName = dirName;
        this.description = description;
    }

    /**
     * 获取上传目录路径，保证以文件分隔符结尾
     *
     * @throws IllegalStateException 未初始化时抛出，避免静默使用错误路径
     */
    public String getPath() {
        String path = PATH_CACHE.get(this);
        if (path == null) {
            throw new IllegalStateException(
                    "上传路径未初始化，请检查配置项 " + configKey + "（" + description + "）");
        }
        return path;
    }

    /**
     * 解析配置并缓存各上传目录路径，由 {@link UploadPathInitializer} 在启动时调用
     *
     * @param env Spring配置环境
     */
    public static void init(Environment env) {
        String root = env.getProperty(ROOT_KEY);
        if (root == null || root.isBlank()) {
            throw new IllegalStateException("未配置上传根目录，请设置 " + ROOT_KEY);
        }
        root = root.trim();

        for (FilePathEnum item : values()) {
            //支持单独覆盖，未配置时使用根目录 + 子目录名
            String override = env.getProperty(item.configKey);
            String path = (override != null && !override.isBlank())
                    ? override.trim()
                    : root + File.separator + item.dirName;
            PATH_CACHE.put(item, withTrailingSeparator(path));
        }

        String capacity = env.getProperty(CAPACITY_KEY);
        safetyCapacity = parseCapacity(capacity == null || capacity.isBlank() ? DEFAULT_CAPACITY : capacity);
    }

    /**
     * 追加末尾分隔符，已有分隔符时不重复追加
     */
    private static String withTrailingSeparator(String path) {
        char last = path.charAt(path.length() - 1);
        if (last == '/' || last == '\\') {
            return path;
        }
        return path + File.separator;
    }

    /**
     * 解析容量配置，支持 5GB / 5G / 5368709120 等写法
     */
    private static Long parseCapacity(String value) {
        String text = value.trim().toUpperCase();
        try {
            if (text.endsWith("GB")) {
                return Long.parseLong(text.substring(0, text.length() - 2).trim()) * 1024 * 1024 * 1024;
            }
            if (text.endsWith("MB")) {
                return Long.parseLong(text.substring(0, text.length() - 2).trim()) * 1024 * 1024;
            }
            if (text.endsWith("G")) {
                return Long.parseLong(text.substring(0, text.length() - 1).trim()) * 1024 * 1024 * 1024;
            }
            if (text.endsWith("M")) {
                return Long.parseLong(text.substring(0, text.length() - 1).trim()) * 1024 * 1024;
            }
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("配置项 " + CAPACITY_KEY + " 格式错误: " + value, e);
        }
    }

    /**
     * 检查上传目录所在磁盘的剩余空间是否低于安全容量
     *
     * @return true 表示剩余空间不足，应禁止上传
     */
    public static boolean canUpload() {
        Long limit = safetyCapacity;
        if (limit == null) {
            limit = parseCapacity(DEFAULT_CAPACITY);
        }
        //检查上传根目录所在磁盘，而非固定C盘
        File store = getStoreRoot();
        if (store == null) {
            throw new IllegalStateException("上传路径未初始化，无法校验磁盘空间");
        }
        return store.getFreeSpace() < limit;
    }

    /**
     * 获取上传目录所在磁盘，用于查询剩余空间
     */
    private static File getStoreRoot() {
        String path = PATH_CACHE.get(UPLOAD_VIDEO);
        if (path == null) {
            return null;
        }
        File dir = new File(path);
        //逐级向上找到第一个已存在的目录，否则getFreeSpace返回0
        while (dir != null && !dir.exists()) {
            dir = dir.getParentFile();
        }
        return dir;
    }

    /**
     * 将图片字节数组转换为 WebP 并保存到指定目录
     *
     * @param imageBytes 原始图片字节数组（jpg/png/gif/webp）
     * @param uploadDir  保存目录（File 类型）
     * @param fileName   文件名（不含后缀）
     */
    public static void saveAsWebp(byte[] imageBytes, File uploadDir, String fileName)
            throws IOException {

        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("imageBytes 不能为空");
        }
        if (uploadDir == null) {
            throw new IllegalArgumentException("uploadDir 不能为空");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName 不能为空");
        }

        // 1. 解析图片字节
        BufferedImage image;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            image = ImageIO.read(bais);
        }

        if (image == null) {
            throw new IOException("无法解析图片数据，可能不是合法图片格式");
        }

        // 2. 确保目录存在
        if (!uploadDir.isDirectory() && (!uploadDir.mkdirs() && !uploadDir.isDirectory())) {
            throw new IOException("创建目录失败：" + uploadDir.getAbsolutePath());
        }

        // 3. 输出 WebP 文件
        File webpFile = new File(uploadDir, fileName + ".webp");
        boolean success = ImageIO.write(image, "webp", webpFile);

        if (!success) {
            throw new IOException("WebP 编码失败，请确认 webp-imageio 依赖已正确加载");
        }
    }

}
