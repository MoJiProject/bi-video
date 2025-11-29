package com.moji;

import lombok.Getter;

import java.io.File;

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
     * 返回剩余空间（GB）
     * @return
     */
    public static double getFreeSpaceGB() {
        File c = new File("C:/");
        long free = c.getFreeSpace();
        return free / 1024.0 / 1024 / 1024;
    }
}
