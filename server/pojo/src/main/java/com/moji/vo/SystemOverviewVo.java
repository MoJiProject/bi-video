package com.moji.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 系统管理仪表盘概览数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemOverviewVo implements Serializable {

    private Long userNumber;//用户总数
    private Long adminNumber;//管理员数
    private Long todayUserNumber;//今日新增用户
    private Long videoNumber;//视频总数
    private Long todayVideoNumber;//今日新增视频
    private Long pendingVideoNumber;//待审核视频
    private Long commentNumber;//评论总数
    private Long todayCommentNumber;//今日新增评论
    private Long dynamicNumber;//动态总数
    private Long messageNumber;//私信总数
    private Long keyWordNumber;//搜索词总数
    private Long banNumber;//封禁中用户数
    private Long totalPlayNumber;//总播放量
    private Long totalLikeNumber;//总点赞量
    private List<SystemTrendVo> userTrend;//近7日新增用户
    private List<SystemTrendVo> videoTrend;//近7日新增视频
    private List<SystemTrendVo> commentTrend;//近7日新增评论
    private List<SystemHotVideoVo> hotVideos;//播放量最高的视频
    private List<SystemHotKeyWordVo> hotKeyWords;//搜索次数最高的词

}
