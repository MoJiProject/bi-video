package com.moji.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.dto.SystemCommentSearchDto;
import com.moji.dto.SystemDynamicSearchDto;
import com.moji.dto.SystemKeyWordSearchDto;
import com.moji.dto.SystemLogSearchDto;
import com.moji.dto.SystemMessageSearchDto;
import com.moji.dto.SystemOperateDto;
import com.moji.dto.SystemRecycleSearchDto;
import com.moji.dto.SystemUserSearchDto;
import com.moji.dto.SystemVideoSearchDto;
import com.moji.po.KeyWord;
import com.moji.po.RecycleBin;
import com.moji.po.SystemOperationLog;
import com.moji.po.UserBan;
import com.moji.vo.SystemCommentVo;
import com.moji.vo.SystemDynamicVo;
import com.moji.vo.SystemMessageVo;
import com.moji.vo.SystemOverviewVo;
import com.moji.vo.SystemRecycleBinVo;
import com.moji.vo.SystemUserVo;
import com.moji.vo.SystemVideoListVo;

import java.util.List;

/**
 * 系统管理后台服务
 */
public interface SystemService {

    /**
     * 校验操作人是否为管理员
     */
    Boolean checkAdmin(Integer operatorId, String token);

    /**
     * 仪表盘概览统计
     */
    SystemOverviewVo getOverview();

    /**
     * 用户管理 - 分页查询
     */
    Page<SystemUserVo> searchUsers(SystemUserSearchDto dto);

    /**
     * 用户管理 - 设置/取消管理员
     */
    Boolean putAdmin(SystemOperateDto dto, Integer targetId);

    /**
     * 用户管理 - 封禁用户
     */
    Boolean banUser(SystemOperateDto dto, Integer targetId);

    /**
     * 用户管理 - 解除封禁
     */
    Boolean unbanUser(SystemOperateDto dto, Integer targetId);

    /**
     * 用户管理 - 查询封禁记录
     */
    Page<UserBan> searchBanList(Integer operatorId, Integer pageNum, String keyword);

    /**
     * 评论管理 - 分页查询
     */
    Page<SystemCommentVo> searchComments(SystemCommentSearchDto dto);

    /**
     * 评论管理 - 删除评论(支持单个或批量)
     */
    Integer deleteComments(SystemOperateDto dto, List<Integer> commentIds);

    /**
     * 动态管理 - 分页查询
     */
    Page<SystemDynamicVo> searchDynamics(SystemDynamicSearchDto dto);

    /**
     * 动态管理 - 删除动态(支持单个或批量)
     */
    Integer deleteDynamics(SystemOperateDto dto, List<Integer> dynamicIds);

    /**
     * 私信管理 - 分页查询
     */
    Page<SystemMessageVo> searchMessages(SystemMessageSearchDto dto);

    /**
     * 私信管理 - 删除私信(支持单个或批量)
     */
    Integer deleteMessages(SystemOperateDto dto, List<Integer> messageIds);

    /**
     * 搜索热词管理 - 分页查询
     */
    Page<KeyWord> searchKeyWords(SystemKeyWordSearchDto dto);

    /**
     * 搜索热词管理 - 新增热词
     */
    Boolean addKeyWord(SystemOperateDto dto, String word);

    /**
     * 搜索热词管理 - 修改热词
     */
    Boolean putKeyWord(SystemOperateDto dto, Integer keyWordId, String word);

    /**
     * 搜索热词管理 - 修改搜索次数
     */
    Boolean putKeyWordCount(SystemOperateDto dto, Integer keyWordId, Integer count);

    /**
     * 搜索热词管理 - 删除热词(支持单个或批量)
     */
    Integer deleteKeyWords(SystemOperateDto dto, List<Integer> keyWordIds);

    /**
     * 操作日志审计 - 分页查询
     */
    Page<SystemOperationLog> searchLogs(SystemLogSearchDto dto);

    /**
     * 操作日志审计 - 导出全部日志(用于导出前的条数确认)
     */
    Long countLogs(SystemLogSearchDto dto);

    /**
     * 视频管理 - 分页查询(含各状态数量)
     */
    SystemVideoListVo searchVideos(SystemVideoSearchDto dto);

    /**
     * 视频管理 - 获取已有分区列表，供筛选下拉使用
     */
    List<String> getSubZoneKeys();

    /**
     * 视频管理 - 审核通过(复用创作中心审核逻辑，会推送粉丝动态)
     */
    Boolean examineVideo(SystemOperateDto dto, Integer videoId);

    /**
     * 视频管理 - 审核退回(仅限未审核的视频)
     */
    Boolean rejectVideo(SystemOperateDto dto, Integer videoId);

    /**
     * 视频管理 - 强制下架已通过的视频，并回滚UP主计数与已推送的粉丝动态
     */
    Boolean takeDownVideo(SystemOperateDto dto, Integer videoId);

    /**
     * 视频管理 - 删除视频(级联清理弹幕/评论/收藏等并删除磁盘文件)
     */
    Integer deleteVideos(SystemOperateDto dto, List<Integer> videoIds);

    // ==================== 回收站 ====================

    /**
     * 回收站 - 分页查询
     */
    Page<SystemRecycleBinVo> searchRecycleBin(SystemRecycleSearchDto dto);

    /**
     * 回收站 - 彻底清除(不可恢复)
     */
    Integer purgeRecycleBin(SystemOperateDto dto, List<Integer> recycleIds);

    /**
     * 回收站 - 清空已还原的记录
     */
    Integer cleanRestoredRecycleBin(SystemOperateDto dto);

    /**
     * 回收站 - 把视频移入回收站(可还原)
     */
    Integer recycleVideos(SystemOperateDto dto, List<Integer> videoIds);

    /**
     * 回收站 - 还原视频
     */
    Integer restoreVideos(SystemOperateDto dto, List<Integer> videoIds);

    /**
     * 回收站 - 把评论移入回收站(可还原)
     */
    Integer recycleComments(SystemOperateDto dto, List<Integer> commentIds);

    /**
     * 回收站 - 还原评论
     */
    Integer restoreComments(SystemOperateDto dto, List<Integer> commentIds);

    /**
     * 回收站 - 把动态移入回收站(可还原)
     */
    Integer recycleDynamics(SystemOperateDto dto, List<Integer> dynamicIds);

    /**
     * 回收站 - 还原动态
     */
    Integer restoreDynamics(SystemOperateDto dto, List<Integer> dynamicIds);

}
