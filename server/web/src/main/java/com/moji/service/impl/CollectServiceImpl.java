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
import java.util.ArrayList;
import java.util.List;

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
                for (Collects collect : collects) {
                    CollectDto collectDto=new CollectDto();
                    collectDto.setCollects(collect);
                    collectDto.setVideos(videosMapper.selectById(collect.getVideoId()));
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

        int closeCollectFlag=0;
        for (AllInFlag allInFlag : acceptCollect.getAllInFlags()) {

            if(!allInFlag.getFlag())
                closeCollectFlag++;
            //收藏夹全部取消该视频，该视频收藏数量-1(上次收藏过)
            if((!collectss.isEmpty() || waitWatchFlag)&&closeCollectFlag==acceptCollect.getAllInFlags().size())
            {
                videos.setCollectNumber(videos.getCollectNumber()-1);
                videosMapper.updateById(videos);
            }
            //查询之前是否收藏过(精确)
            LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper1=new LambdaQueryWrapper<>();
            collectsLambdaQueryWrapper1.eq(Collects::getVideoId,acceptCollect.getVideoId())
                    .eq(Collects::getUserId,acceptCollect.getUserId())
                    .eq(Collects::getCollectName,allInFlag.getName());
            Collects collects1 = collectMapper.selectOne(collectsLambdaQueryWrapper1);
            //收藏
           if(allInFlag.getFlag()&&collects1==null)
           {
               LambdaQueryWrapper<CollectsClassify> collectsClassifyLambdaQueryWrapper=new LambdaQueryWrapper<>();
               collectsClassifyLambdaQueryWrapper.eq(CollectsClassify::getUserId,acceptCollect.getUserId())
                       .eq(CollectsClassify::getCollectName,allInFlag.getName());
               CollectsClassify collectsClassify = collectClassifyMapper.selectOne(collectsClassifyLambdaQueryWrapper);
               collectsClassify.setVideoNumber(collectsClassify.getVideoNumber()+1);
               collectClassifyMapper.updateById(collectsClassify);
               Collects collects=Collects.builder()
                       .userId(acceptCollect.getUserId())
                       .collectName(allInFlag.getName())
                       .videoId(videos.getId())
                       .collectTime(LocalDateTime.now())
                       .build();
               collectMapper.insert(collects);
           }
           //取消收藏
            else if((!allInFlag.getFlag())&&collects1!=null) {
               LambdaQueryWrapper<CollectsClassify> collectsClassifyLambdaQueryWrapper=new LambdaQueryWrapper<>();
               collectsClassifyLambdaQueryWrapper.eq(CollectsClassify::getUserId,acceptCollect.getUserId())
                       .eq(CollectsClassify::getCollectName,allInFlag.getName());
               CollectsClassify collectsClassify = collectClassifyMapper.selectOne(collectsClassifyLambdaQueryWrapper);
               collectsClassify.setVideoNumber(collectsClassify.getVideoNumber()-1);
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
        for (Collects collect : collects) {
            Videos videos = videosMapper.selectById(collect.getVideoId());
            SelectVideoDto selectVideoDto=new SelectVideoDto();
            if(videos!=null) {
                videos.setCreateTime(collect.getCollectTime());
                selectVideoDto = videosService.getSelectVideo(videos, userId,true);
            }
            CollectDto collectDto=new CollectDto();
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
        for (Collects collect : collects) {
            LambdaQueryWrapper<CollectsClassify> collectsClassifyLambdaQueryWrapper=new LambdaQueryWrapper<>();
            collectsClassifyLambdaQueryWrapper.eq(CollectsClassify::getCollectName,collect.getCollectName())
                    .eq(CollectsClassify::getUserId,userId);
            CollectsClassify collectsClassify = collectClassifyMapper.selectOne(collectsClassifyLambdaQueryWrapper);
            if(collectsClassify!=null){
                collectsClassify.setVideoNumber(collectsClassify.getVideoNumber()-1);
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
        for (Collects collect : collects) {

            LambdaQueryWrapper<CollectsClassify> collectsClassifyLambdaQueryWrapper=new LambdaQueryWrapper<>();
            collectsClassifyLambdaQueryWrapper.eq(CollectsClassify::getCollectName,collect.getCollectName())
                    .eq(CollectsClassify::getUserId,userId);
            if(!collect.getUserId().equals(userId))
                return false;
            CollectsClassify collectsClassify = collectClassifyMapper.selectOne(collectsClassifyLambdaQueryWrapper);
            if(collectsClassify==null)
                return false;
            else {
                collectsClassify.setVideoNumber(collectsClassify.getVideoNumber()-1);
                collectClassifyMapper.updateById(collectsClassify);
                LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                collectsLambdaQueryWrapper.eq(Collects::getUserId,userId)
                        .eq(Collects::getVideoId,collect.getVideoId());
                //查询是否有别的收藏夹收藏该视频
                if(collectMapper.selectList(collectsLambdaQueryWrapper).size()==1) {
                    Videos videos = videosMapper.selectById(collect.getVideoId());
                    videos.setCollectNumber(videos.getCollectNumber() - 1);
                    videosMapper.updateById(videos);
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
        if(controls==1){
            for (CollectsClassify collectsClassify : collectsClassifies) {
                int i=0;
                for (Collects collect : collects) {
                    LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    collectsLambdaQueryWrapper.eq(Collects::getVideoId,collect.getVideoId())
                            .eq(Collects::getCollectName,collectsClassify.getCollectName())
                            .eq(Collects::getUserId,userId);

                    if(!collect.getUserId().equals(userId))
                      return false;

                    //如果不存在复制
                    if(collectMapper.selectOne(collectsLambdaQueryWrapper)==null)
                    {
                        Collects collects1=new Collects();
                        BeanUtils.copyProperties(collect,collects1);
                        collects1.setId(null);
                        collects1.setCollectName(collectsClassify.getCollectName());
                        collectMapper.insert(collects1);
                        i++;
                    }
                }
                collectsClassify.setVideoNumber(collectsClassify.getVideoNumber()+i);
                collectClassifyMapper.updateById(collectsClassify);
            }
        }
        //移动
        else if(controls==2){
            for (CollectsClassify collectsClassify : collectsClassifies) {
                int i=0;
                for (Collects collect : collects) {
                    LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    collectsLambdaQueryWrapper.eq(Collects::getVideoId, collect.getVideoId())
                            .eq(Collects::getCollectName, collectsClassify.getCollectName())
                            .eq(Collects::getUserId, userId);

                    if(!collect.getUserId().equals(userId))
                        return false;

                    //如果不存在移动
                    if (collectMapper.selectOne(collectsLambdaQueryWrapper) == null) {
                        Collects collects1 = new Collects();
                        BeanUtils.copyProperties(collect, collects1);
                        collects1.setId(null);
                        collects1.setCollectName(collectsClassify.getCollectName());
                        collectMapper.insert(collects1);
                        i++;
                    }
                }
                collectsClassify.setVideoNumber(collectsClassify.getVideoNumber()+i);
                collectClassifyMapper.updateById(collectsClassify);
            }

            int i = collectMapper.deleteBatchIds(collectIds);
            LambdaQueryWrapper<CollectsClassify> collectsClassifyLambdaQueryWrapper=new LambdaQueryWrapper<>();
            collectsClassifyLambdaQueryWrapper.eq(CollectsClassify::getUserId,userId)
                    .eq(CollectsClassify::getCollectName,collects.get(0).getCollectName());
            CollectsClassify collectsClassify = collectClassifyMapper.selectOne(collectsClassifyLambdaQueryWrapper);
            collectsClassify.setVideoNumber(collectsClassify.getVideoNumber()-i);
            collectClassifyMapper.updateById(collectsClassify);
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
        for (Collects collects : collectsList) {
            CollectDto collectDto=new CollectDto();
            collectDto.setCollects(collects);
            Videos videos = videosMapper.selectById(collects.getVideoId());
            if(videos==null)
                continue;
            collectDto.setSelectVideoDto(videosService.getSelectVideo(videos,0,false));
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
