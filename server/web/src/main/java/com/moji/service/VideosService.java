package com.moji.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.moji.dto.SelectVideoDto;
import com.moji.dto.VideosDto;
import com.moji.po.ResponseCollectClassify;
import com.moji.po.Videos;
import com.moji.vo.SelectVideoByIdVo;
import com.moji.vo.UsersVideosVo;

import java.util.List;

public interface VideosService extends IService<Videos> {
    Boolean insertVideo(String videoName, String coverName, Videos videos);

    VideosDto selectDtoData(Integer userId);

    UsersVideosVo selectByUserIdVideo(Integer userId, String videoTitle, String subZoneKey, String sortWay, Integer videoStatus, Integer pageNum);
    UsersVideosVo selectByUserExamineVideo(Integer userId, String videoTitle, String subZoneKey, String sortWay, Integer videoStatus, Integer pageNum);

    Boolean deleteVideo(Integer videoId);

    Boolean updateVideo(String videoName, String coverName, Videos videos, Boolean vFlag, Boolean cFlag);

    List<SelectVideoDto> getVideo(Integer userId, Integer sort, Integer pageNum);

    List<SelectVideoDto> getSelectVideoDto(List<Videos> videos,Integer userId,boolean flag);

    List<SelectVideoDto> getSelectVideoDto2(List<Videos> videos,Integer userId,boolean flag);

    SelectVideoDto getSelectVideo(Videos videos,Integer userId,boolean flag);


    List<SelectVideoDto> getSelectVideoDto(List<Videos> videos);

    Boolean examineVideo(int videoId);

    SelectVideoByIdVo selectByVideoId(Integer videoId, Integer userId);

    SelectVideoByIdVo getLTCAxios(SelectVideoByIdVo selectVideoByIdVo);

    List<ResponseCollectClassify> getByIdCollectClassify(Integer userId, Integer videoId);

    List<SelectVideoDto> getVideoPageByVideo(Videos videos);

    String formatDuration(double second);

    Page<SelectVideoDto> homeContributeVideos(Integer userId, Integer sort, Integer pageNum, String keyWord, Integer homeUserId);

    List<SelectVideoDto> homeThrowCoinVideos(Integer userId, Integer homeUserId);

    List<SelectVideoDto> homeLoveVideos(Integer userId, Integer homeUserId);

    List<SelectVideoDto> magnum(Integer homeUserId, Integer userId);
}
