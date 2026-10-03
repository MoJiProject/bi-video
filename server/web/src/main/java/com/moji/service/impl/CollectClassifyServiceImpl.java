package com.moji.service.impl;

import ch.qos.logback.core.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moji.FilePathEnum;
import com.moji.R;
import com.moji.mapper.CollectClassifyMapper;
import com.moji.mapper.CollectMapper;
import com.moji.mapper.VideosMapper;
import com.moji.po.Collects;
import com.moji.po.CollectsClassify;
import com.moji.po.Videos;
import com.moji.service.CollectClassifyService;
import com.moji.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CollectClassifyServiceImpl extends ServiceImpl<CollectClassifyMapper, CollectsClassify> implements CollectClassifyService {

    @Autowired
    private CollectClassifyMapper collectClassifyMapper;
    @Autowired
    private CommentService commentService;
    @Autowired
    private CollectMapper collectMapper;
    @Autowired
    private VideosMapper videosMapper;

    @Override
    public List<CollectsClassify> getCollectsClassify(Integer homeUserId, Integer userId) {

        LambdaQueryWrapper<CollectsClassify> collectsClassifyLambdaQueryWrapper=new LambdaQueryWrapper<>();
        collectsClassifyLambdaQueryWrapper.eq(CollectsClassify::getUserId,homeUserId)
                .ne(CollectsClassify::getCollectName,"稍后再看");

        //判断是否是自己的主页
        if(!Objects.equals(userId, homeUserId))
            collectsClassifyLambdaQueryWrapper.eq(CollectsClassify::getStatus,1);

        collectsClassifyLambdaQueryWrapper.orderByAsc(CollectsClassify::getId);
        List<CollectsClassify> collectsClassifies = collectClassifyMapper.selectList(collectsClassifyLambdaQueryWrapper);

        //设置收藏夹封面
        List<CollectsClassify> collectsClassifyList = collectsClassifies.stream()
                .filter(c -> c.getCoverAddress() == null && c.getVideoNumber() > 0)
                .toList();
        if (collectsClassifyList.isEmpty()) {
            return collectsClassifies;
        }
        List<String> collectNames = collectsClassifyList.stream()
                .map(CollectsClassify::getCollectName)
                .collect(Collectors.toList());
        if(collectNames.isEmpty()){
            return collectsClassifies;
        }
        List<Collects> latestCollects = collectMapper.selectList(
                new LambdaQueryWrapper<Collects>()
                        .eq(Collects::getUserId, homeUserId)
                        .in(Collects::getCollectName, collectNames)
                        .orderByDesc(Collects::getId)
        );
        Map<String, Collects> latestCollectMap =
                latestCollects.stream()
                        .collect(Collectors.groupingBy(
                                Collects::getCollectName,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> list.get(0)
                                )
                        ));
        List<Integer> videoIds = latestCollectMap.values().stream()
                .map(Collects::getVideoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Map<Integer, Videos> videoMap = new HashMap<>();
        if (!videoIds.isEmpty()) {
            List<Videos> videosList = videosMapper.selectBatchIds(videoIds);
            videoMap = videosList.stream().collect(Collectors.toMap(Videos::getId, v -> v));
        }
        for (CollectsClassify collectsClassify : collectsClassifyList) {
            Collects collects = latestCollectMap.get(collectsClassify.getCollectName());
            if (collects == null) continue;
            Videos videos = videoMap.get(collects.getVideoId());
            if (videos != null) {
                collectsClassify.setCoverAddress(videos.getCoverAddress());
            }
        }
        return collectsClassifies;
    }

    @Override
    @Transactional
    public boolean addCollectClassify(CollectsClassify collectsClassify) {

        if(StringUtil.notNullNorEmpty(collectsClassify.getCoverAddress())) {

            UUID coverName = UUID.randomUUID();
            String base64 = null;
                if(collectsClassify.getCoverAddress().contains(","))
                    base64=collectsClassify.getCoverAddress().split(",")[1];
                else
                    return false;

                if(FilePathEnum.canUpload()){
                    throw new RuntimeException("服务器存储空间不足，无法上传文件");
                }

                File dir=new File(FilePathEnum.UPLOAD_IMG_COLLECT_CLASSIFY.getPath());
                byte[] decode = Base64.getDecoder().decode(base64);
                try{
                    FilePathEnum.saveAsWebp(decode,dir, String.valueOf(coverName));
                }
            catch (IOException e){
               throw new RuntimeException("封面上传失败");
            }
            collectsClassify.setCoverAddress("/upload/collectClassify/"+coverName+".webp");
        }
        if(collectsClassify.getId()==null)
            collectClassifyMapper.insert(collectsClassify);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteCollectClassify(Integer id) {

        CollectsClassify collectsClassify = collectClassifyMapper.selectById(id);
        if(collectsClassify==null)
            return false;

        if(collectsClassify.getCoverAddress()!=null){

            String cover=collectsClassify.getCoverAddress();
            int lastIndexOf = cover.lastIndexOf("/");
            String collectClassifyFile=(lastIndexOf!=-1)? cover.substring(lastIndexOf+1):cover;
            Path collectClassifyPath= Paths.get(FilePathEnum.UPLOAD_IMG_COLLECT_CLASSIFY.getPath()+collectClassifyFile);
            try {
                Files.delete(collectClassifyPath);
            }catch (Exception e){
                System.out.println(e.getMessage());
                return false;
            }
        }
        collectClassifyMapper.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public boolean putCollectClassify(CollectsClassify collectsClassify) {

        CollectsClassify collectsClassify1 = collectClassifyMapper.selectById(collectsClassify.getId());
        if(collectsClassify1==null)
            return false;

        if(collectsClassify.getCoverAddress()!=null){
            if(!collectsClassify.getCoverAddress().equals(collectsClassify1.getCoverAddress())){
                if(StringUtil.notNullNorEmpty(collectsClassify1.getCoverAddress())){
                    //删除原图
                    String cover=collectsClassify1.getCoverAddress();
                    int lastIndexOf = cover.lastIndexOf("/");
                    String collectClassifyFile=(lastIndexOf!=-1)? cover.substring(lastIndexOf+1):cover;
                    Path collectClassifyPath= Paths.get(FilePathEnum.UPLOAD_IMG_COLLECT_CLASSIFY.getPath()+collectClassifyFile);
                    try {
                        Files.delete(collectClassifyPath);
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                        return false;
                    }
                }
                //新增图
                if(StringUtil.notNullNorEmpty(collectsClassify.getCoverAddress())) {

                    UUID coverName = UUID.randomUUID();
                    String base64 = null;
                    if(collectsClassify.getCoverAddress().contains(","))
                        base64=collectsClassify.getCoverAddress().split(",")[1];

                    File dir=new File(FilePathEnum.UPLOAD_IMG_COLLECT_CLASSIFY.getPath());
                    byte[] decode = Base64.getDecoder().decode(base64);
                    try{
                        FilePathEnum.saveAsWebp(decode,dir, String.valueOf(coverName));
                    }catch (IOException e){
                        throw new RuntimeException("封面上传失败");
                    }
                    collectsClassify.setCoverAddress("/upload/collectClassify/"+coverName+".webp");
                }
            }
        }
        //只允许修改归属本用户的收藏夹，且只落库可编辑字段
        //user_id/video_number 这类归属与统计字段以库里的记录为准，避免被前端改写
        CollectsClassify dbClassify=collectClassifyMapper.selectById(collectsClassify.getId());
        if(dbClassify==null||!Objects.equals(dbClassify.getUserId(),collectsClassify.getUserId()))
            return false;

        CollectsClassify update=CollectsClassify.builder()
                .id(dbClassify.getId())
                .userId(dbClassify.getUserId())
                .collectName(collectsClassify.getCollectName())
                .content(collectsClassify.getContent())
                .status(collectsClassify.getStatus())
                .coverAddress(collectsClassify.getCoverAddress()!=null
                        ?collectsClassify.getCoverAddress():dbClassify.getCoverAddress())
                .videoNumber(dbClassify.getVideoNumber())
                .build();

        collectClassifyMapper.updateById(update);
        return true;
    }


}
