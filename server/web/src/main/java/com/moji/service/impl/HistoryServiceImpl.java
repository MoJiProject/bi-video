package com.moji.service.impl;

import ch.qos.logback.core.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moji.dto.AcceptHistory;
import com.moji.dto.HistoryDto;
import com.moji.dto.SelectHistoryDto;
import com.moji.mapper.HistoryMapper;
import com.moji.mapper.UserMapper;
import com.moji.mapper.VideosMapper;
import com.moji.po.History;
import com.moji.po.UserInfo2;
import com.moji.po.Users;
import com.moji.po.Videos;
import com.moji.serve.LoginLimiterServer;
import com.moji.service.HistoryService;
import com.moji.service.VideosService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class HistoryServiceImpl extends ServiceImpl<HistoryMapper, History> implements HistoryService {

    @Autowired
    private HistoryMapper historyMapper;
    @Autowired
    private VideosMapper videosMapper;
    @Autowired
    private VideosService videosService;
    @Autowired
    private UserMapper userMapper;


    @Override
    public List<SelectHistoryDto> getHistory(Integer userId) {

        LambdaQueryWrapper<History> historyLambdaQueryWrapper=new LambdaQueryWrapper<>();
        historyLambdaQueryWrapper.eq(History::getUserId,userId)
                .orderByDesc(History::getWatchVideoDate);
        List<History> histories = historyMapper.selectList(historyLambdaQueryWrapper);

        List<SelectHistoryDto> selectHistoryDtos=new ArrayList<>();
        int count=0;
        if(!histories.isEmpty()){
            for (History history : histories) {
                Videos video = videosMapper.selectById(history.getVideoId());
                if (video.getStatus()!=1)
                    continue;

                String substring=null;
                Duration duration=Duration.between(history.getWatchVideoDate(),LocalDateTime.now());
                if (duration.toDays()<1){
                    if(LocalDateTime.now().getDayOfMonth()==history.getWatchVideoDate().getDayOfMonth())
                      substring="今天";
                    else
                        substring="昨天";
                }
                else if (duration.toDays()==1) {
                    substring="昨天";
                }
                else{
                    //判断是否是今年
                        int year = LocalDateTime.now().getYear();
                        int year1 = history.getWatchVideoDate().getYear();
                        if(year==year1)
                            substring=String.valueOf(history.getWatchVideoDate()).substring(5,10);
                        else substring=String.valueOf(history.getWatchVideoDate()).substring(0,10);
                }

                SelectHistoryDto selectHistoryDto=SelectHistoryDto.builder()
                        .videoTitle(video.getTitle())
                        .historyId(history.getId())
                        .videoUserName(video.getUserName())
                        .videoCoverAddress(video.getCoverAddress())
                        .watchVideoDate(substring)
                        .watchVideoDateTime(String.valueOf(history.getWatchVideoDate()).substring(11,16))
                        .videoTime(video.getVideoTime())
                        .watchVideoTime(history.getWatchVideoTime())
                        .videoId(video.getId())
                        .watchVideoFlag(history.getWatchVideoFlag())
                        .build();

                if(count++<=20)
                    selectHistoryDtos.add(selectHistoryDto);
                else break;
            }
            return selectHistoryDtos;
        }
        return null;
    }

    @Override
    public void addHistory(String acceptHistory1) {

        //将json字符串转换成对象
        ObjectMapper objectMapper=new ObjectMapper();
        AcceptHistory acceptHistory;
        try {
            acceptHistory = objectMapper.readValue(acceptHistory1, AcceptHistory.class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //如果没有登录的token则请求失败
        LoginLimiterServer limiterServer=new LoginLimiterServer();
        String token = limiterServer.isAutoLogin(acceptHistory.getUserIp());
        if(!acceptHistory.getToken().equals(token))
            return;

        Users users = userMapper.selectById(acceptHistory.getHistory().getUserId());
        if(users==null||users.getHistoryFlag()==0)
            return;

        //如果视频已经删除则不能进行该操作
        if(videosMapper.selectById(acceptHistory.getHistory().getVideoId())==null)
            return;

        History history=acceptHistory.getHistory();
        if (history != null) {
            String watchVideoTime = videosService.formatDuration(history.getWatchCurrentTime());
            history.setWatchVideoTime(watchVideoTime);
            history.setWatchVideoDate(LocalDateTime.now());
            LambdaQueryWrapper<History> historyLambdaQueryWrapper=new LambdaQueryWrapper<>();
            historyLambdaQueryWrapper.eq(History::getUserId,history.getUserId())
                            .eq(History::getVideoId,history.getVideoId());
            History history1 = historyMapper.selectOne(historyLambdaQueryWrapper);
            if(history1==null)
             historyMapper.insert(history);
            else {
                history.setId(history1.getId());
                if(history1.getWatchVideoFlag()==1)
                    history.setWatchVideoFlag(1);
                historyMapper.updateById(history);
            }
        }
    }

    @Override
    public History getHistryById(Videos videos) {

        LambdaQueryWrapper<History> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(History::getUserId,videos.getUserId())
                .eq(History::getVideoId,videos.getId());
        return historyMapper.selectOne(lambdaQueryWrapper);
    }

    @Override
    @Transactional
    public int deleteAllHistory(Integer userId) {

        LambdaQueryWrapper<History> historyLambdaQueryWrapper=new LambdaQueryWrapper<>();
        historyLambdaQueryWrapper.eq(History::getUserId,userId);
        return historyMapper.delete(historyLambdaQueryWrapper);
    }

    @Override
    public List<HistoryDto> selectHistoryList(Integer userId, Integer pageNum, Integer sort, String keyWord, String startTime, String endTime) {

        List<HistoryDto> historyDtoList=new ArrayList<>();
        LocalDateTime startTime2 = null;
        LocalDateTime endTime2 = null;
        if(keyWord==null)
            keyWord="";
        if(sort==5&&StringUtil.notNullNorEmpty(startTime)&&StringUtil.notNullNorEmpty(endTime))
        {
            startTime2= ZonedDateTime.parse(startTime).toLocalDateTime();
            endTime2= ZonedDateTime.parse(endTime).toLocalDateTime();
        }
        else if(sort==5) return historyDtoList;

        List<History> histories = historyMapper.selectHistoryList(userId, (pageNum - 1) * 10, sort, keyWord, startTime2, endTime2);

        for (History history : histories) {

            HistoryDto historyDto=new HistoryDto();
            Videos videos = videosMapper.selectById(history.getVideoId());
            videos.setCreateTime(history.getWatchVideoDate());
            historyDto.setSelectVideoDto(videosService.getSelectVideo(videos,userId,false));
            historyDto.setHistory(history);
            if(historyDto.getSelectVideoDto()!=null)
            {
                Users users = userMapper.selectById(historyDto.getSelectVideoDto().getUserId());
                UserInfo2 userInfo2=new UserInfo2();
                BeanUtils.copyProperties(users,userInfo2);
                historyDto.setUserInfo2(userInfo2);
            }
            historyDtoList.add(historyDto);
        }
        return historyDtoList;
    }

    @Override
    @Transactional
    public boolean deleteHistory(Integer userId, List<Integer> ids) {

        if(ids.isEmpty())
            return false;

        List<History> histories = historyMapper.selectBatchIds(ids);
        if(histories.isEmpty())
            return false;

        for (History history : histories) {
            if(!history.getUserId().equals(userId))
                return false;
        }
        return historyMapper.deleteBatchIds(ids)==ids.size();
    }

}
