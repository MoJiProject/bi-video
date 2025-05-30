package com.moji;

import lombok.Getter;

@Getter
public enum FilePathEnum {
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

    private final String path;

    FilePathEnum(String path) {
        this.path = path;
    }

}
