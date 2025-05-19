package com.moji.service.impl;

import ch.qos.logback.core.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moji.FilePathEnum;
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
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

        List<CollectsClassify> collectsClassifies = collectClassifyMapper.selectList(collectsClassifyLambdaQueryWrapper);

        //设置收藏夹封面
        for (CollectsClassify collectsClassify : collectsClassifies) {
            if(collectsClassify.getCoverAddress()==null&&collectsClassify.getVideoNumber()>0)
            {
                LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                collectsLambdaQueryWrapper.eq(Collects::getUserId,homeUserId)
                        .eq(Collects::getCollectName,collectsClassify.getCollectName())
                        .orderByDesc(Collects::getId)
                        .last("LIMIT 1");
                Collects collects = collectMapper.selectOne(collectsLambdaQueryWrapper);
                Videos videos = videosMapper.selectById(collects.getVideoId());
                if(videos!=null)
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
            String imageFormat = commentService.getImageFormat(collectsClassify.getCoverAddress());
            if(imageFormat == null||imageFormat.equals(".gif"))
                return false;

            try{
                String base64 = null;
                if(collectsClassify.getCoverAddress().contains(","))
                    base64=collectsClassify.getCoverAddress().split(",")[1];
                else
                    return false;

                File dir=new File(FilePathEnum.UPLOAD_IMG_COLLECT_CLASSIFY.getPath());
                byte[] decode = Base64.getDecoder().decode(base64);
                File dest=new File(dir,coverName+imageFormat);
                try(FileOutputStream fos=new FileOutputStream(dest)){
                    fos.write(decode);
                }
            }catch (IOException e){
                e.printStackTrace();
                return false;
            }
            collectsClassify.setCoverAddress("/upload/collectClassify/"+coverName+imageFormat);
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
                    String imageFormat = commentService.getImageFormat(collectsClassify.getCoverAddress());
                    if(imageFormat == null||imageFormat.equals(".gif"))
                        return false;

                    try{
                        String base64 = null;
                        if(collectsClassify.getCoverAddress().contains(","))
                            base64=collectsClassify.getCoverAddress().split(",")[1];

                        File dir=new File(FilePathEnum.UPLOAD_IMG_COLLECT_CLASSIFY.getPath());
                        byte[] decode = Base64.getDecoder().decode(base64);
                        File dest=new File(dir,coverName+imageFormat);
                        try(FileOutputStream fos=new FileOutputStream(dest)){
                            fos.write(decode);
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                        return false;
                    }
                    collectsClassify.setCoverAddress("/upload/collectClassify/"+coverName+imageFormat);
                }
            }
        }
        collectClassifyMapper.updateById(collectsClassify);
        return true;
    }


}
