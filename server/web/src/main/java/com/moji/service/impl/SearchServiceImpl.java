package com.moji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.dto.AcceptSearchDto;
import com.moji.dto.SelectUserDto;
import com.moji.dto.SelectVideoDto;
import com.moji.mapper.FollowMapper;
import com.moji.mapper.UserMapper;
import com.moji.mapper.VideosMapper;
import com.moji.po.*;
import com.moji.service.SearchService;
import com.moji.service.VideosService;
import com.moji.vo.ResponseSearchVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    //以下取数方法用于排序，计数/时间类字段在老数据里可能为null，统一兜底避免排序时NPE
    private int playNumberOf(Videos videos) {
        return videos.getPlayNumber()==null?0:videos.getPlayNumber();
    }

    private int scrollingNumberOf(Videos videos) {
        return videos.getScrollingNumber()==null?0:videos.getScrollingNumber();
    }

    private int collectNumberOf(Videos videos) {
        return videos.getCollectNumber()==null?0:videos.getCollectNumber();
    }

    private LocalDateTime createTimeOf(Videos videos) {
        return videos.getCreateTime();
    }

    @Override
    public ResponseSearchVo selectVideoByKeyWord(AcceptSearchDto acceptSearchData) {

        Page<Videos> page=new Page<>(acceptSearchData.getVideoPageNum(),20);
        ResponseSearchVo responseSearchVo = new ResponseSearchVo();
        List<SelectVideoDto> selectVideoDto;
        LambdaQueryWrapper<Videos> videosLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //搜索只返回审核通过的视频
        videosLambdaQueryWrapper.eq(Videos::getStatus,1);

        //「全部分区」且不按时长过滤时，标签命中也算一次有效搜索。
        //原来是把标签查询的结果整表selectList再与分页结果做并集，导致：
        //每页都多带一份全量数据、翻页永远收敛不了、总数与实际返回对不上。
        //这里直接把标签条件并入主查询的条件组，变成一次正常的分页查询。
        boolean matchTag="全部".equals(acceptSearchData.getClassify())&&acceptSearchData.getTime()==0;

        if (!acceptSearchData.getKeyWord().isEmpty() || !acceptSearchData.getClassifyIndex().isEmpty())
            videosLambdaQueryWrapper.and(wrapper->{
                wrapper.like(Videos::getTitle,acceptSearchData.getKeyWord())
                        .or()
                        .apply("LOWER({0}) LIKE CONCAT('%', LOWER(title), '%')",acceptSearchData.getKeyWord())
                        .or()
                        .like(Videos::getUserName,acceptSearchData.getKeyWord())
                        .or()
                        .apply("LOWER({0}) LIKE CONCAT('%', LOWER(user_name), '%')",acceptSearchData.getKeyWord());
                if(matchTag)
                    wrapper.or().like(Videos::getTag,acceptSearchData.getKeyWord());
            });
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

        if(!"全部".equals(acceptSearchData.getClassify()))
            videosLambdaQueryWrapper.eq(Videos::getSubZoneKey,acceptSearchData.getClassify());

        Page<Videos> videosPage = videosMapper.selectPage(page, videosLambdaQueryWrapper);
        List<Videos> videos = videosPage.getRecords();
        responseSearchVo.setVideoTotal(videosPage.getTotal());

        List<Videos> videosList1=new ArrayList<>(videos);

        //排序（计数类字段可能为null，比较器统一做空值兜底）
            if(acceptSearchData.getSort()==1)
                videosList1.sort(Comparator.comparingInt(this::playNumberOf).reversed());
            else if (acceptSearchData.getSort()==2)
                videosList1.sort(Comparator.comparing(this::createTimeOf,
                        Comparator.nullsLast(Comparator.reverseOrder())).reversed());
            else if (acceptSearchData.getSort()==3)
                videosList1.sort(Comparator.comparingInt(this::scrollingNumberOf).reversed());
            else if (acceptSearchData.getSort()==4)
                videosList1.sort(Comparator.comparingInt(this::collectNumberOf).reversed());

        if (acceptSearchData.getTime()==0)
        {
            if(acceptSearchData.getUserId()!=0)
             selectVideoDto = videosService.getSelectVideoDto(videosList1,acceptSearchData.getUserId(),false);
            else
             selectVideoDto= videosService.getSelectVideoDto(videosList1);

            responseSearchVo.setSelectVideoDtoList(selectVideoDto);
            return responseSearchVo;
        }

        //按时长过滤的场景不参与标签匹配，标签条件没有并入主查询，这里直接复用同一批数据
        Set<Videos> videosSet2 = new LinkedHashSet<>(videos);
        List<Videos> videosList2=new ArrayList<>(videosSet2);

        //排序
        if(acceptSearchData.getSort()==1)
            videosList2.sort(Comparator.comparingInt(this::playNumberOf).reversed());
        else if (acceptSearchData.getSort()==2)
            videosList2.sort(Comparator.comparing(this::createTimeOf,
                    Comparator.nullsLast(Comparator.reverseOrder())).reversed());
        else if (acceptSearchData.getSort()==3)
            videosList2.sort(Comparator.comparingInt(this::scrollingNumberOf).reversed());
        else if (acceptSearchData.getSort()==4)
            videosList2.sort(Comparator.comparingInt(this::collectNumberOf).reversed());

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

        responseSearchVo.setSelectVideoDtoList(selectVideoDtos1);
        return responseSearchVo;
    }

    @Override
    public ResponseSearchVo selectUserByKeyWord(AcceptSearchDto acceptSearchData) {

        Page<Users> page=new Page<>(acceptSearchData.getUserPageNum(),20);
        ResponseSearchVo responseSearchVo = new ResponseSearchVo();
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

        Page<Users> usersPage = userMapper.selectPage(page, usersLambdaQueryWrapper);
        List<Users> users = usersPage.getRecords();
        List<SelectUserDto> selectUserDtoList=new ArrayList<>();
        if (!users.isEmpty()) {
            List<Integer> userIds = users.stream()
                    .map(Users::getId)
                    .collect(Collectors.toList());
            List<Follow> follows = followMapper.selectList(
                    new LambdaQueryWrapper<Follow>()
                            .eq(Follow::getUserId, acceptSearchData.getUserId())
                            .in(Follow::getFollowId, userIds)
            );
            Map<Integer, Follow> followMap = follows.stream()
                    .collect(Collectors.toMap(Follow::getFollowId, f -> f));
            for (Users user : users) {
                Follow follow = followMap.get(user.getId());
                SelectUserDto userDto = SelectUserDto.builder()
                        .userId(user.getId())
                        .userName(user.getUserName())
                        .avatarAddress(user.getAvatarAddress())
                        .grade(user.getGrade())
                        .fansNumber(user.getFansNumber())
                        .introduce(user.getIntroduce())
                        .videoNumber(user.getVideoNumber())
                        .build();

                if (follow != null)
                    userDto.setFollow(follow);

                selectUserDtoList.add(userDto);
            }
        }

        responseSearchVo.setUserTotal(usersPage.getTotal());
        responseSearchVo.setSelectUserDtoList(selectUserDtoList);
        return responseSearchVo;
    }
}
