package com.moji.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 清理残留的视频分片。
 * <p>
 * 前端异常退出、断网、浏览器关闭等情况下分片目录不会被主动删除，
 * 只能由服务端定时清理，否则会持续占用磁盘。
 */
@Component
@Slf4j
public class ChunkCleaner {

    /**
     * 分片目录的最大保留时间（毫秒），默认1小时。
     * 正常上传在1小时内必定完成，超时的即为残留。
     */
    private static final long MAX_AGE_MILLIS = 60L * 60 * 1000;

    private final UploadController uploadController;

    public ChunkCleaner(UploadController uploadController) {
        this.uploadController = uploadController;
    }

    /**
     * 每10分钟清理一次超时的分片目录
     */
    @Scheduled(initialDelay = 10 * 60 * 1000, fixedDelay = 10 * 60 * 1000)
    public void cleanTimeoutChunks() {
        File root = uploadController.getChunkRootForClean();
        if (root == null || !root.isDirectory()) {
            return;
        }
        File[] chunkDirs = root.listFiles();
        if (chunkDirs == null) {
            return;
        }
        long now = System.currentTimeMillis();
        int cleaned = 0;
        for (File chunkDir : chunkDirs) {
            if (!chunkDir.isDirectory()) {
                continue;
            }
            long lastModified = chunkDir.lastModified();
            if (now - lastModified <= MAX_AGE_MILLIS) {
                //仍在上传中，保留
                continue;
            }
            try {
                uploadController.deleteChunkDirForClean(chunkDir);
                cleaned++;
            } catch (Exception e) {
                log.warn("清理残留分片失败: {} , {}", chunkDir.getName(), e.getMessage());
            }
        }
        if (cleaned > 0) {
            log.info("已清理 {} 个残留视频分片目录", cleaned);
        }
    }
}
