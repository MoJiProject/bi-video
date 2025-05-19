package com.moji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moji.dto.AcceptSearchDto;
import com.moji.dto.SelectUserDto;
import com.moji.dto.SelectVideoDto;
import com.moji.mapper.FollowMapper;
import com.moji.mapper.UserMapper;
import com.moji.mapper.VideosMapper;
import com.moji.po.*;
import com.moji.service.SearchService;
import com.moji.service.VideosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {


    @Autowired
    private UserMapper userMapper;
    @Autowired
    private VideosMapper videosMapper;
    @Autowired
    private VideosService videosService;
    @Autowired
    private FollowMapper followMapper;

    @Override
    public List<SelectVideoDto> selectVideoByKeyWord(AcceptSearchDto acceptSearchData) {

        List<SelectVideoDto> selectVideoDto;

        LambdaQueryWrapper<Videos> videosLambdaQueryWrapper=new LambdaQueryWrapper<>();
        if (!acceptSearchData.getKeyWord().isEmpty() || !acceptSearchData.getClassifyIndex().isEmpty())
        videosLambdaQueryWrapper.and(wrapper->wrapper
                .like(Videos::getTitle,acceptSearchData.getKeyWord())
                .or()
                .apply("LOWER({0}) LIKE CONCAT('%', LOWER(title), '%')",acceptSearchData.getKeyWord())
                .or()
                .like(Videos::getUserName,acceptSearchData.getKeyWord())
                .or()
                .apply("LOWER({0}) LIKE CONCAT('%', LOWER(user_name), '%')",acceptSearchData.getKeyWord()));
        else return null;

        if(acceptSearchData.getDate()==1)
            videosLambdaQueryWrapper.between(Videos::getCreateTime, LocalDateTime.now().minusDays(1),LocalDateTime.now());
        else if (acceptSearchData.getDate()==2)
            videosLambdaQueryWrapper.between(Videos::getCreateTime, LocalDateTime.now().minusDays(7),LocalDateTime.now());
        else if (acceptSearchData.getDate()==3)
            videosLambdaQueryWrapper.between(Videos::getCreateTime, LocalDateTime.now().minusDays(180),LocalDateTime.now());
        else if (acceptSearchData.getDate()==4) {
            LocalDateTime startTime = ZonedDateTime.parse(acceptSearchData.getStartTime()).toLocalDateTime();
            LocalDateTime endTime = ZonedDateTime.parse(acceptSearchData.getEndTime()).toLocalDateTime();
            videosLambdaQueryWrapper.between(Videos::getCreateTime, startTime, endTime);
        }

        List<Videos> videosList=new ArrayList<>();
        if(!acceptSearchData.getClassify().equals("全部"))
            videosLambdaQueryWrapper.eq(Videos::getSubZoneKey,acceptSearchData.getClassify());

        List<Videos> videos = videosMapper.selectList(videosLambdaQueryWrapper);

        if(acceptSearchData.getClassify().equals("全部")){
            LambdaQueryWrapper<Videos> videosLambdaQueryWrapper1=new LambdaQueryWrapper<>();
            if(acceptSearchData.getDate()==1)
                videosLambdaQueryWrapper1.between(Videos::getCreateTime, LocalDateTime.now().minusDays(1),LocalDateTime.now());
            else if (acceptSearchData.getDate()==2)
                videosLambdaQueryWrapper1.between(Videos::getCreateTime, LocalDateTime.now().minusDays(7),LocalDateTime.now());
            else if (acceptSearchData.getDate()==3)
                videosLambdaQueryWrapper1.between(Videos::getCreateTime, LocalDateTime.now().minusDays(180),LocalDateTime.now());
            else if (acceptSearchData.getDate()==4) {
                LocalDateTime startTime = ZonedDateTime.parse(acceptSearchData.getStartTime()).toLocalDateTime();
                LocalDateTime endTime = ZonedDateTime.parse(acceptSearchData.getEndTime()).toLocalDateTime();
                videosLambdaQueryWrapper1.between(Videos::getCreateTime, startTime, endTime);
            }
            videosLambdaQueryWrapper1.like(Videos::getTag,acceptSearchData.getKeyWord());
            videosList=videosMapper.selectList(videosLambdaQueryWrapper1);
        }

        Set<Videos> videosSet = new LinkedHashSet<>();
        videosSet.addAll(videos);
        videosSet.addAll(videosList);
        List<Videos> videosList1=new ArrayList<>(videosSet);

        //排序
            if(acceptSearchData.getSort()==1)
                videosList1.sort(Comparator.comparingInt(Videos::getPlayNumber).reversed());
            else if (acceptSearchData.getSort()==2)
                videosList1.sort(Comparator.comparing(Videos::getCreateTime).reversed());
            else if (acceptSearchData.getSort()==3)
                videosList1.sort(Comparator.comparingInt(Videos::getScrollingNumber).reversed());
            else if (acceptSearchData.getSort()==4)
                videosList1.sort(Comparator.comparingInt(Videos::getCollectNumber).reversed());

        if (acceptSearchData.getTime()==0)
        {
            if(acceptSearchData.getUserId()!=0)
             selectVideoDto = videosService.getSelectVideoDto(videosList1,acceptSearchData.getUserId(),false);
            else
             selectVideoDto= videosService.getSelectVideoDto(videosList1);
            return selectVideoDto;
        }

        //添加标签搜索
        Set<Videos> videosSet2 = new LinkedHashSet<>();
        videosSet2.addAll(videos);
        videosSet2.addAll(videosList);
        List<Videos> videosList2=new ArrayList<>(videosSet2);

        //排序
        if(acceptSearchData.getSort()==1)
            videosList2.sort(Comparator.comparingInt(Videos::getPlayNumber).reversed());
        else if (acceptSearchData.getSort()==2)
            videosList2.sort(Comparator.comparing(Videos::getCreateTime).reversed());
        else if (acceptSearchData.getSort()==3)
            videosList2.sort(Comparator.comparingInt(Videos::getScrollingNumber).reversed());
        else if (acceptSearchData.getSort()==4)
            videosList2.sort(Comparator.comparingInt(Videos::getCollectNumber).reversed());

        List<SelectVideoDto> selectVideoDtos=videosService.getSelectVideoDto(videosList2,acceptSearchData.getUserId(),false);
        List<SelectVideoDto> selectVideoDtos1=new ArrayList<>();
        for (SelectVideoDto videoDto : selectVideoDtos) {

            int minutes = Integer.parseInt(videoDto.getMinutes());
            int second = Integer.parseInt(videoDto.getSecond());

            if ((second+minutes*60)<(10*60)&&acceptSearchData.getTime()==1&&videoDto.getHour()==null)
                selectVideoDtos1.add(videoDto);
            else if ((second+minutes*60)>(10*60)&&(second+minutes*60)<(30*60)&&acceptSearchData.getTime()==2&&videoDto.getHour()==null)
                selectVideoDtos1.add(videoDto);
            else if ((second+minutes*60)>(30*60)&&(second+minutes*60)<(60*60)&&acceptSearchData.getTime()==3&&videoDto.getHour()==null)
                selectVideoDtos1.add(videoDto);
            else if(videoDto.getHour()!=null&&acceptSearchData.getTime()==4)
                selectVideoDtos1.add(videoDto);
        }

        return selectVideoDtos1;
    }

    @Override
    public List<SelectUserDto> selectUserByKeyWord(AcceptSearchDto acceptSearchData) {


        LambdaQueryWrapper<Users> usersLambdaQueryWrapper=new LambdaQueryWrapper<>();
        if (!acceptSearchData.getKeyWord().isEmpty())
        usersLambdaQueryWrapper
                .like(Users::getUserName,acceptSearchData.getKeyWord())
                .or()
                .apply("LOWER({0}) LIKE CONCAT('%', LOWER(user_name), '%')",acceptSearchData.getKeyWord());

        else return null;

        if (acceptSearchData.getUserSort()==1)
            usersLambdaQueryWrapper.orderByDesc(Users::getFansNumber);

        if (acceptSearchData.getUserSort()==2)
            usersLambdaQueryWrapper.orderByAsc(Users::getFansNumber);

        if (acceptSearchData.getUserSort()==3)
            usersLambdaQueryWrapper.orderByDesc(Users::getGrade);

        if (acceptSearchData.getUserSort()==4)
            usersLambdaQueryWrapper.orderByAsc(Users::getGrade);

        List<Users> users = userMapper.selectList(usersLambdaQueryWrapper);
        List<SelectUserDto> selectUserDtoList=new ArrayList<>();
        for (Users user : users) {

            LambdaQueryWrapper<Follow> followLambdaQueryWrapper=new LambdaQueryWrapper<>();
            followLambdaQueryWrapper.eq(Follow::getUserId,acceptSearchData.getUserId())
                    .eq(Follow::getFollowId,user.getId());
            Follow follow = followMapper.selectOne(followLambdaQueryWrapper);

            SelectUserDto userDto=SelectUserDto.builder()
                    .userId(user.getId())
                    .userName(user.getUserName())
                    .avatarAddress(user.getAvatarAddress())
                    .grade(user.getGrade())
                    .fansNumber(user.getFansNumber())
                    .introduce(user.getIntroduce())
                    .videoNumber(user.getVideoNumber())
                    .build();

                if (follow!=null)
                 userDto.setFollow(follow);

            selectUserDtoList.add(userDto);

        }

        return selectUserDtoList;
    }
}
