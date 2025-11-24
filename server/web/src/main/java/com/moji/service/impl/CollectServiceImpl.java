package com.moji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moji.dto.CollectDto;
import com.moji.dto.SelectVideoDto;
import com.moji.service.VideosService;
import com.moji.vo.CollectVo;
import com.moji.mapper.CollectClassifyMapper;
import com.moji.mapper.CollectMapper;
import com.moji.mapper.VideosMapper;
import com.moji.po.*;
import com.moji.service.CollectService;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collects> implements CollectService {

    @Autowired
    private CollectMapper collectMapper;

    @Autowired
    private VideosMapper videosMapper;

    @Autowired
    private CollectClassifyMapper collectClassifyMapper;

    @Autowired
    private VideosService videosService;

    @Override
    public List<CollectVo> getAllCollect(Integer userId) {

        List<CollectVo> collectVos=new ArrayList<>();
        int defaultCollect=0;
        int waitWatchCollect=0;
        int collectId=0;
        LambdaQueryWrapper<CollectsClassify> collectsClassifyLambdaQueryWrapper=new LambdaQueryWrapper<>();
        collectsClassifyLambdaQueryWrapper.eq(CollectsClassify::getUserId,userId);

        List<CollectsClassify> collectsClassifies = collectClassifyMapper.selectList(collectsClassifyLambdaQueryWrapper);

        if(!collectsClassifies.isEmpty()) {
            for (CollectsClassify collectsClassify : collectsClassifies) {

                if (collectsClassify.getCollectName().equals("默认收藏夹"))
                    defaultCollect++;
                if (collectsClassify.getCollectName().equals("稍后再看"))
                    waitWatchCollect++;

                LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper = new LambdaQueryWrapper<>();
                collectsLambdaQueryWrapper.eq(Collects::getCollectName, collectsClassify.getCollectName())
                        .eq(Collects::getUserId,userId)
                        .eq(Collects::getDeleteFlag,0)
                        .orderByDesc(Collects::getId)
                        .last("LIMIT 20");
                List<Collects> collects = collectMapper.selectList(collectsLambdaQueryWrapper);

                List<CollectDto> collectDtos=new ArrayList<>();
                List<Integer> videoIds = collects.stream()
                        .map(Collects::getVideoId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                Map<Integer, Videos> videoMap = new HashMap<>();
                if (!videoIds.isEmpty()) {
                    List<Videos> videos = videosMapper.selectBatchIds(videoIds);
                    videoMap = videos.stream().collect(Collectors.toMap(Videos::getId, v -> v));
                }
                for (Collects collect : collects) {
                    CollectDto collectDto = new CollectDto();
                    collectDto.setCollects(collect);
                    collectDto.setVideos(videoMap.get(collect.getVideoId()));
                    collectDtos.add(collectDto);
                }

                CollectVo collectVo = CollectVo.builder()
                        .collectsList(collectDtos)
                        .collectName(collectsClassify.getCollectName())
                        .collectNumber(collectsClassify.getVideoNumber())
                        .id(collectId++)
                        .collectClassifyId(collectsClassify.getId())
                        .build();
                collectVos.add(collectVo);
            }
        }
            if (defaultCollect==0)
            {
                CollectsClassify collectsClassify=CollectsClassify.builder()
                        .collectName("默认收藏夹")
                        .videoNumber(0)
                        .userId(userId)
                        .build();
                collectClassifyMapper.insert(collectsClassify);
            }
            if (waitWatchCollect==0)
            {
                CollectsClassify collectsClassify=CollectsClassify.builder()
                        .collectName("稍后再看")
                        .videoNumber(0)
                        .userId(userId)
                        .build();
                collectClassifyMapper.insert(collectsClassify);
            }
            return collectVos;
    }

    @Override
    @Transactional
    public void insertCollect(AcceptCollect acceptCollect) {

        Videos videos = videosMapper.selectById(acceptCollect.getVideoId());
        //查询用户之前是否收藏过这个视频(不精确)
        LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
        collectsLambdaQueryWrapper.eq(Collects::getVideoId,acceptCollect.getVideoId())
                .eq(Collects::getUserId,acceptCollect.getUserId());
        List<Collects> collectss = collectMapper.selectList(collectsLambdaQueryWrapper);
        boolean waitWatchFlag;
        if(collectss.size()==1)
            waitWatchFlag= !collectss.get(0).getCollectName().equals("稍后再看");
        else waitWatchFlag=true;

        if((collectss.isEmpty() || !waitWatchFlag)&&!acceptCollect.getAllInFlags().isEmpty()){
           videos.setCollectNumber(videos.getCollectNumber()+1);
           videosMapper.updateById(videos);
        }

        int closeCollectFlag = 0;
        List<String> names = acceptCollect.getAllInFlags()
                .stream().map(AllInFlag::getName).collect(Collectors.toList());
        if(names.isEmpty()){
            throw new RuntimeException("收藏夹不能为空");
        }
        List<Collects> oldCollects = collectMapper.selectList(
                new LambdaQueryWrapper<Collects>()
                        .eq(Collects::getVideoId, acceptCollect.getVideoId())
                        .eq(Collects::getUserId, acceptCollect.getUserId())
                        .in(Collects::getCollectName, names)
        );
        Map<String, Collects> oldCollectMap = oldCollects.stream()
                .collect(Collectors.toMap(Collects::getCollectName, c -> c));
        if(names.isEmpty())
        {
            throw new RuntimeException("收藏夹不能为空");
        }
        List<CollectsClassify> classifies = collectClassifyMapper.selectList(
                new LambdaQueryWrapper<CollectsClassify>()
                        .eq(CollectsClassify::getUserId, acceptCollect.getUserId())
                        .in(CollectsClassify::getCollectName, names)
        );
        Map<String, CollectsClassify> classifyMap = classifies.stream()
                .collect(Collectors.toMap(CollectsClassify::getCollectName, c -> c));
        for (AllInFlag allInFlag : acceptCollect.getAllInFlags()) {
            if (!allInFlag.getFlag())
                closeCollectFlag++;
            if ((!collectss.isEmpty() || waitWatchFlag) &&
                    closeCollectFlag == acceptCollect.getAllInFlags().size()) {
                videos.setCollectNumber(videos.getCollectNumber() - 1);
                videosMapper.updateById(videos);
            }
            Collects collects1 = oldCollectMap.get(allInFlag.getName());
            if (allInFlag.getFlag() && collects1 == null) {
                CollectsClassify collectsClassify = classifyMap.get(allInFlag.getName());
                collectsClassify.setVideoNumber(collectsClassify.getVideoNumber() + 1);
                collectClassifyMapper.updateById(collectsClassify);
                Collects collects = Collects.builder()
                        .userId(acceptCollect.getUserId())
                        .collectName(allInFlag.getName())
                        .videoId(videos.getId())
                        .collectTime(LocalDateTime.now())
                        .build();
                collectMapper.insert(collects);
            } else if (!allInFlag.getFlag() && collects1 != null) {
                CollectsClassify collectsClassify = classifyMap.get(allInFlag.getName());
                collectsClassify.setVideoNumber(collectsClassify.getVideoNumber() - 1);
                collectClassifyMapper.updateById(collectsClassify);
                collectMapper.deleteById(collects1);
            }
        }

    }


    @Override
    public Page<CollectDto> getCollectByName(String collectName, Integer homeUserId, Integer userId, Integer pageNum, Integer type, Integer sort, String keyWord) {

        List<Collects> collects=new ArrayList<>();
        Page<CollectDto> collectDtoPage=new Page<>();
        long total=0L;
        //没有搜索
        if(StringUtil.isNullOrEmpty(keyWord)) {
            collects=collectMapper.getCollectByName(collectName,homeUserId,sort,(pageNum-1)*20);
            total=collectMapper.getCollectByNameCount(collectName,homeUserId,sort,(pageNum-1)*20);
        }
        //有搜索
        else {
            //当前收藏夹（用收藏夹名称）
            if(type==1)
            {
                collects=collectMapper.getCollectByName2(collectName,homeUserId,sort,keyWord,(pageNum-1)*20);
                total=collectMapper.getCollectByNameCount2(collectName,homeUserId,sort,keyWord,(pageNum-1)*20);
            }
            //全局搜索（不需要收藏夹名称）
            else if (type==2) {
                collects = collectMapper.getCollectByName3(homeUserId, sort, keyWord, (pageNum-1)*20);
                total = collectMapper.getCollectByNameCount3(homeUserId, sort, keyWord, (pageNum-1)*20);
            }
        }
        List<CollectDto> collectDtos=new ArrayList<>();
        List<Integer> videoIds = collects.stream()
                .map(Collects::getVideoId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, Videos> videoMap = new HashMap<>();
        if (!videoIds.isEmpty()) {
            List<Videos> videosList = videosMapper.selectBatchIds(videoIds);
            videoMap = videosList.stream().collect(Collectors.toMap(Videos::getId, v -> v));
        }
        for (Collects collect : collects) {
            Videos videos = videoMap.get(collect.getVideoId());
            SelectVideoDto selectVideoDto = new SelectVideoDto();
            if (videos != null) {
                Videos v = new Videos();
                BeanUtils.copyProperties(videos, v);
                v.setCreateTime(collect.getCollectTime());
                selectVideoDto = videosService.getSelectVideo(v, userId, true);
            }
            CollectDto collectDto = new CollectDto();
            collectDto.setSelectVideoDto(selectVideoDto);
            collectDto.setCollects(collect);
            collectDtos.add(collectDto);
        }
        collectDtoPage.setRecords(collectDtos);
        collectDtoPage.setTotal(total);
        return collectDtoPage;
    }

    @Override
    @Transactional
    public boolean deleteFailure(Integer userId) {

        LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
        collectsLambdaQueryWrapper.eq(Collects::getUserId,userId)
                .eq(Collects::getDeleteFlag,1);
        List<Collects> collects = collectMapper.selectList(collectsLambdaQueryWrapper);
        //更新视频数量
        List<String> names = collects.stream()
                .map(Collects::getCollectName)
                .collect(Collectors.toList());
        if (names.isEmpty()) {
            return false;
        }
        List<CollectsClassify> classifies = collectClassifyMapper.selectList(
                new LambdaQueryWrapper<CollectsClassify>()
                        .eq(CollectsClassify::getUserId, userId)
                        .in(CollectsClassify::getCollectName, names)
        );
        Map<String, CollectsClassify> classifyMap = classifies.stream()
                .collect(Collectors.toMap(CollectsClassify::getCollectName, c -> c));
        for (Collects collect : collects) {
            CollectsClassify collectsClassify = classifyMap.get(collect.getCollectName());
            if (collectsClassify != null) {
                collectsClassify.setVideoNumber(collectsClassify.getVideoNumber() - 1);
                collectClassifyMapper.updateById(collectsClassify);
            }
        }
        int delete = collectMapper.delete(collectsLambdaQueryWrapper);
        return delete>0;
    }

    @Override
    @Transactional
    public boolean deleteCollect(Integer userId, List<Integer> ids) {

        if(ids.isEmpty())
            return false;

        List<Collects> collects = collectMapper.selectBatchIds(ids);
        Set<Integer> videoIds = new HashSet<>();
        Set<String> classKeys = new HashSet<>();
        for (Collects c : collects) {
            classKeys.add(c.getCollectName() + "_" + userId);
            videoIds.add(c.getVideoId());
        }
        LambdaQueryWrapper<CollectsClassify> qw1 = new LambdaQueryWrapper<>();
        qw1.eq(CollectsClassify::getUserId, userId)
                .in(CollectsClassify::getCollectName, collects.stream().map(Collects::getCollectName).collect(Collectors.toSet()));
        List<CollectsClassify> classList = collectClassifyMapper.selectList(qw1);
        Map<String, CollectsClassify> classMap = new HashMap<>();
        for (CollectsClassify cc : classList) {
            classMap.put(cc.getCollectName() + "_" + userId, cc);
        }
        LambdaQueryWrapper<Collects> qw2 = new LambdaQueryWrapper<>();
        qw2.eq(Collects::getUserId, userId)
                .in(Collects::getVideoId, videoIds);
        List<Collects> collectList = collectMapper.selectList(qw2);
        Map<Integer, Long> videoCountMap = collectList.stream()
                .collect(Collectors.groupingBy(Collects::getVideoId, Collectors.counting()));
        List<Videos> videoList = videosMapper.selectBatchIds(videoIds);
        Map<Integer, Videos> videoMap = videoList.stream().collect(Collectors.toMap(Videos::getId, v -> v));
        for (Collects c : collects) {
            if (!c.getUserId().equals(userId))
                return false;
            String k = c.getCollectName() + "_" + userId;
            CollectsClassify cc = classMap.get(k);
            if (cc == null)
                return false;
            cc.setVideoNumber(cc.getVideoNumber() - 1);
            collectClassifyMapper.updateById(cc);
            long cnt = videoCountMap.getOrDefault(c.getVideoId(), 0L);
            if (cnt == 1) {
                Videos v = videoMap.get(c.getVideoId());
                if (v != null) {
                    v.setCollectNumber(v.getCollectNumber() - 1);
                    videosMapper.updateById(v);
                }
            }
        }
        int i = collectMapper.deleteBatchIds(ids);
        return i>0;
    }

    @Override
    @Transactional
    public boolean controlCollect(Integer userId, List<Integer> collectIds, List<Integer> collectClassifyIds, Integer controls) {

        if(collectIds.isEmpty() || collectClassifyIds.isEmpty())
            return false;

        List<Collects> collects = collectMapper.selectBatchIds(collectIds);
        if(collects.isEmpty())
            return false;

        List<CollectsClassify> collectsClassifies = collectClassifyMapper.selectBatchIds(collectClassifyIds);
        if(collectsClassifies.isEmpty())
            return false;

        //复制
        if (controls == 1) {
            Set<String> existsKey = new HashSet<>();
            for (Collects c : collects) {
                existsKey.add(c.getVideoId() + "_" + c.getCollectName());
            }
            for (CollectsClassify cc : collectsClassifies) {
                int count = 0;
                for (Collects c : collects) {
                    if (!c.getUserId().equals(userId))
                        return false;
                    String key = c.getVideoId() + "_" + cc.getCollectName();
                    if (!existsKey.contains(key)) {
                        Collects newC = new Collects();
                        BeanUtils.copyProperties(c, newC);
                        newC.setId(null);
                        newC.setCollectName(cc.getCollectName());
                        collectMapper.insert(newC);
                        existsKey.add(key);
                        count++;
                    }
                }
                cc.setVideoNumber(cc.getVideoNumber() + count);
                collectClassifyMapper.updateById(cc);
            }
        }
        //移动
        else if (controls == 2) {
            Set<String> existsKey = new HashSet<>();
            for (Collects c : collects) {
                existsKey.add(c.getVideoId() + "_" + c.getCollectName());
            }
            for (CollectsClassify cc : collectsClassifies) {
                int count = 0;
                for (Collects c : collects) {
                    if (!c.getUserId().equals(userId))
                        return false;
                    String key = c.getVideoId() + "_" + cc.getCollectName();
                    if (!existsKey.contains(key)) {
                        Collects newC = new Collects();
                        BeanUtils.copyProperties(c, newC);
                        newC.setId(null);
                        newC.setCollectName(cc.getCollectName());
                        collectMapper.insert(newC);
                        existsKey.add(key);
                        count++;
                    }
                }
                cc.setVideoNumber(cc.getVideoNumber() + count);
                collectClassifyMapper.updateById(cc);
            }
            int del = collectMapper.deleteBatchIds(collectIds);
            LambdaQueryWrapper<CollectsClassify> qw = new LambdaQueryWrapper<>();
            qw.eq(CollectsClassify::getUserId, userId)
                    .eq(CollectsClassify::getCollectName, collects.get(0).getCollectName());
            CollectsClassify cc = collectClassifyMapper.selectOne(qw);
            cc.setVideoNumber(cc.getVideoNumber() - del);
            collectClassifyMapper.updateById(cc);
        }

        return true;
    }

    @Override
    public List<CollectDto> selectWaitWatch(Integer userId, Integer sort, Integer sort2, String keyWord, String startTime, String endTime, Integer pageNum) {

        List<CollectDto> collectDtoList=new ArrayList<>();
        LocalDateTime startTime2 = null;
        LocalDateTime endTime2 = null;

        if(keyWord==null)
            keyWord="";
        if(sort==5&& ch.qos.logback.core.util.StringUtil.notNullNorEmpty(startTime)&& ch.qos.logback.core.util.StringUtil.notNullNorEmpty(endTime))
        {
            startTime2= ZonedDateTime.parse(startTime).toLocalDateTime();
            endTime2= ZonedDateTime.parse(endTime).toLocalDateTime();
        }
        else if(sort==5) return collectDtoList;

        List<Collects> collectsList = collectMapper.selectWaitWatch(userId, (pageNum - 1) * 10, sort, sort2, keyWord, startTime2, endTime2);
        if(collectsList.isEmpty()) {
            return collectDtoList;
        }
        List<Integer> videoIds = collectsList.stream()
                .map(Collects::getVideoId)
                .collect(Collectors.toList());
        List<Videos> videosList = videosMapper.selectBatchIds(videoIds);
            Map<Integer, Videos> videosMap = videosList.stream()
                    .collect(Collectors.toMap(Videos::getId, v -> v));
            for (Collects collects : collectsList) {
                Videos videos = videosMap.get(collects.getVideoId());
                if (videos == null)
                    continue;
                CollectDto collectDto = new CollectDto();
                collectDto.setCollects(collects);
                collectDto.setSelectVideoDto(
                        videosService.getSelectVideo(videos, 0, false)
                );
                collectDtoList.add(collectDto);
            }
        return collectDtoList;
    }

    @Override
    @Transactional
    public int cleanAllWaitWatch(Integer userId) {

        LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
        collectsLambdaQueryWrapper.eq(Collects::getCollectName,"稍后再看")
                .eq(Collects::getUserId,userId);
        int delete = collectMapper.delete(collectsLambdaQueryWrapper);

        LambdaQueryWrapper<CollectsClassify> collectsClassifyLambdaQueryWrapper=new LambdaQueryWrapper<>();
        collectsClassifyLambdaQueryWrapper.eq(CollectsClassify::getUserId,userId)
                .eq(CollectsClassify::getCollectName,"稍后再看");
        CollectsClassify collectsClassify = collectClassifyMapper.selectOne(collectsClassifyLambdaQueryWrapper);
        if(collectsClassify==null)
            throw new RuntimeException();

        collectsClassify.setVideoNumber(collectsClassify.getVideoNumber()-delete);
        collectClassifyMapper.updateById(collectsClassify);
        return delete;
    }
}
