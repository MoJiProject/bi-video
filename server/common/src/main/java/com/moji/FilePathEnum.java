package com.moji;

import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

@Getter
public enum FilePathEnum {

    /** 服务器磁盘剩余容量（小于 5GB 禁止上传） */
    UPLOAD_SAFETY_CAPACITY(5L * 1024 * 1024 * 1024),
    /** 评论图片上传路径 */
    UPLOAD_COMMENT_IMG("C:\\Users\\13788\\IdeaProjects\\bi-video\\static\\public\\upload\\comment\\"),
    /** 用户头像上传路径 */
    UPLOAD_AVATAR("C:\\Users\\13788\\IdeaProjects\\bi-video\\static\\public\\upload\\avatar\\"),
    /** 用户视频封面上传路径 */
    UPLOAD_VIDEO_COVER("C:\\Users\\13788\\IdeaProjects\\bi-video\\static\\public\\upload\\video\\cover\\"),
    /** 用户视频上传路径 */
    UPLOAD_VIDEO("C:\\Users\\13788\\IdeaProjects\\bi-video\\static\\public\\upload\\video\\"),
    /** 用户聊天图片上传路径 */
    UPLOAD_IMG_MESSAGE("C:\\Users\\13788\\IdeaProjects\\bi-video\\static\\public\\upload\\message\\"),
    /** 用户收藏夹封面上传路径 */
    UPLOAD_IMG_COLLECT_CLASSIFY("C:\\Users\\13788\\IdeaProjects\\bi-video\\static\\public\\upload\\collectClassify\\"),
    /** 用户主页背景上传路径 */
    UPLOAD_IMG_BACKGROUND("C:\\Users\\13788\\IdeaProjects\\bi-video\\static\\public\\upload\\background\\"),
    /** 用户动态上传路径 */
    UPLOAD_IMG_DYNAMIC("C:\\Users\\13788\\IdeaProjects\\bi-video\\static\\public\\upload\\dynamic\\");

    private String path;
    private Long size;

    FilePathEnum(String path) {
        this.path = path;
    }

    FilePathEnum(Long size) {
        this.size = size;
    }

    /**
     * 检查 C 盘剩余空间是否大于安全容量
     * @return
     */
    public static boolean canUpload() {
        long limit = FilePathEnum.UPLOAD_SAFETY_CAPACITY.getSize(); // 5GB
        File c = new File("C:/");
        long free = c.getFreeSpace();
        return free < limit;
    }


    /**
     * 将图片字节数组转换为 WebP 并保存到指定目录
     *
     * @param imageBytes 原始图片字节数组（jpg/png/gif/webp）
     * @param uploadDir  保存目录（File 类型）
     * @param fileName  文件名（不含后缀）
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
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new IOException("创建目录失败：" + uploadDir.getAbsolutePath());
        }

        if (!uploadDir.isDirectory()) {
            throw new IOException("uploadDir 不是一个有效目录：" + uploadDir.getAbsolutePath());
        }

        // 3. 输出 WebP 文件
        File webpFile = new File(uploadDir, fileName + ".webp");
        boolean success = ImageIO.write(image, "webp", webpFile);

        if (!success) {
            throw new IOException("WebP 编码失败，请确认 webp-imageio 依赖已正确加载");
        }
    }

}
