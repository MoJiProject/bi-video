package com.moji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moji.FilePathEnum;
import com.moji.dto.PrivateMessageDto;
import com.moji.dto.SelectDialogue;
import com.moji.dto.SelectPrivateMessage;
import com.moji.dto.ShareVideo;
import com.moji.mapper.*;
import com.moji.po.*;
import com.moji.service.CacheService;
import com.moji.service.CommentService;
import com.moji.service.PrivateMessageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PrivateMessageServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage> implements PrivateMessageService {


    @Autowired
    private PrivateMessageMapper privateMessageMapper;
    @Autowired
    private DialogueMapper dialogueMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CommentService commentService;
    @Autowired
    private FansMapper fansMapper;
    @Autowired
    private VideosMapper videosMapper;
    @Autowired
    private CacheService cacheService;


    @Override
    public List<SelectDialogue> selectDialogueList(Integer userId, Integer pageNum) {

        Page<Dialogue> dialoguePage=new Page<>(pageNum,20);

        LambdaQueryWrapper<Dialogue> dialogueLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dialogueLambdaQueryWrapper.eq(Dialogue::getUserId,userId)
                .orderByDesc(Dialogue::getUpDateTime)
                .orderByDesc(Dialogue::getId);
        Page<Dialogue> dialogues = dialogueMapper.selectPage(dialoguePage,dialogueLambdaQueryWrapper);
        List<Dialogue> dialoguesRecords = dialogues.getRecords();

        List<SelectDialogue> selectDialogueList=new ArrayList<>();

        for (Dialogue dialogue : dialoguesRecords) {

            SelectDialogue selectDialogue=new SelectDialogue();

            Users users = userMapper.selectById(dialogue.getDialogueId());
            UserInfo2 userInfo2=new UserInfo2();
            BeanUtils.copyProperties(users,userInfo2);
            //查询未读的数量
            LambdaQueryWrapper<PrivateMessage> privateMessageLambdaQueryWrapper=new LambdaQueryWrapper<>();
            privateMessageLambdaQueryWrapper.eq(PrivateMessage::getSenderId,dialogue.getDialogueId())
                            .eq(PrivateMessage::getReceiverId,dialogue.getUserId())
                            .eq(PrivateMessage::getStatus,0);
            List<PrivateMessage> notReadMessage = privateMessageMapper.selectList(privateMessageLambdaQueryWrapper);
            LambdaQueryWrapper<PrivateMessage> privateMessageLambdaQueryWrapper1=new LambdaQueryWrapper<>();
            privateMessageLambdaQueryWrapper1.eq(PrivateMessage::getSelectSign,dialogue.getSign());
            List<PrivateMessage> privateMessages = privateMessageMapper.selectList(privateMessageLambdaQueryWrapper1);
            selectDialogue.setDialogue(dialogue);
            selectDialogue.setUserInfo(userInfo2);
            selectDialogue.setAllMessageNumber(privateMessages.size());
            selectDialogue.setNotReadNumber(notReadMessage.size());
            selectDialogueList.add(selectDialogue);
        }

        return selectDialogueList;
    }

    @Override
    @Transactional
    public Boolean putUpStatus(Integer userId, Integer id) {

        LambdaQueryWrapper<Dialogue> dialogueLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dialogueLambdaQueryWrapper.eq(Dialogue::getId,id);
        Dialogue dialogue = dialogueMapper.selectOne(dialogueLambdaQueryWrapper);

        if(dialogue!=null){
            dialogue.setUpDateTime(dialogue.getUpDateTime()!=null? null:LocalDateTime.now());
            int i = dialogueMapper.deleteById(dialogue);
            int insert = dialogueMapper.insert(dialogue);
            cacheService.deleteDialogueByUserId(userId);
            return i != 0 && insert != 0;
        }else return false;
    }

    @Override
    @Transactional
    public Boolean putDndStatus(Integer userId, Integer id) {

        LambdaQueryWrapper<Dialogue> dialogueLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dialogueLambdaQueryWrapper.eq(Dialogue::getId,id);
        Dialogue dialogue = dialogueMapper.selectOne(dialogueLambdaQueryWrapper);

        if(dialogue!=null){
            dialogue.setDnd(dialogue.getDnd()==0? 1:0);
            int i = dialogueMapper.updateById(dialogue);
            cacheService.deleteDialogueByUserId(userId);
            return i != 0;
        }else return false;
    }

    @Override
    @Transactional
    public Boolean addDialogue(Dialogue dialogue) {

        //判断对话是否存在
        LambdaQueryWrapper<Dialogue> dialogueLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dialogueLambdaQueryWrapper.eq(Dialogue::getUserId,dialogue.getUserId())
                .eq(Dialogue::getDialogueId,dialogue.getDialogueId());
        Dialogue dialogue1 = dialogueMapper.selectOne(dialogueLambdaQueryWrapper);

        if(dialogue1==null){
            dialogue.setSign(String.valueOf(dialogue.getUserId()>dialogue.getDialogueId()? dialogue.getDialogueId():dialogue.getUserId())
                    +String.valueOf(dialogue.getUserId()<dialogue.getDialogueId()? dialogue.getDialogueId():dialogue.getUserId()));
            int insert = dialogueMapper.insert(dialogue);
            if(insert>0) {
                cacheService.deleteDialogueByUserId(dialogue.getUserId());
                cacheService.deleteDialogueByUserId(dialogue.getDialogueId());
            }
            return insert > 0;
        }
        return true;
    }

    @Override
    @Transactional
    public Boolean deleteDialogue(Integer id) {

        Dialogue dialogue = dialogueMapper.selectById(id);
        if(dialogue==null)
            return false;
        cacheService.deleteDialogueByUserId(dialogue.getUserId());
        int i = dialogueMapper.deleteById(id);
        return i>0;
    }

    @Override
    @Transactional
    public Boolean sendMessage(PrivateMessage privateMessage) {

        String sign =String.valueOf(privateMessage.getReceiverId()>privateMessage.getSenderId()?privateMessage.getSenderId():privateMessage.getReceiverId())
                +String.valueOf(privateMessage.getReceiverId()<privateMessage.getSenderId()?privateMessage.getSenderId():privateMessage.getReceiverId());
        if(sign.isEmpty())
            return false;

        privateMessage.setSendTime(LocalDateTime.now());
        privateMessage.setSelectSign(sign);
            //确保创建对话
            LambdaQueryWrapper<Dialogue> dialogueLambdaQueryWrapper2=new LambdaQueryWrapper<>();
            dialogueLambdaQueryWrapper2.eq(Dialogue::getUserId,privateMessage.getReceiverId())
                    .eq(Dialogue::getDialogueId,privateMessage.getSenderId());
        Dialogue dialogue2 = dialogueMapper.selectOne(dialogueLambdaQueryWrapper2);
        if(dialogue2==null)
        {
            Dialogue dialogue1 = new Dialogue();
            dialogue1.setUserId(privateMessage.getReceiverId());
            dialogue1.setDialogueId(privateMessage.getSenderId());
            dialogue1.setSign(sign);
            dialogueMapper.insert(dialogue1);
        }
        LambdaQueryWrapper<Dialogue> dialogueLambdaQueryWrapper3=new LambdaQueryWrapper<>();
            dialogueLambdaQueryWrapper3.eq(Dialogue::getUserId,privateMessage.getSenderId())
                    .eq(Dialogue::getDialogueId,privateMessage.getReceiverId());
            Dialogue dialogue3 = dialogueMapper.selectOne(dialogueLambdaQueryWrapper3);
        if(dialogue3==null)
        {
            Dialogue dialogue1 = new Dialogue();
            dialogue1.setUserId(privateMessage.getReceiverId());
            dialogue1.setDialogueId(privateMessage.getSenderId());
            dialogue1.setSign(sign);
            dialogueMapper.insert(dialogue1);
        }

        //更新私信数量并且没有开免打扰
        Users users = userMapper.selectById(privateMessage.getReceiverId());
        if(users!=null&&dialogue2!=null&&dialogue2.getDnd()==0&&users.getMessageWarn()==1)
        {
            users.setAllMessageNumber(users.getAllMessageNumber()+1);
            users.setMessageNumber(users.getMessageNumber()+1);
            userMapper.updateById(users);
        }else if(users==null)
            return false;

        //更新对话内容
            LambdaQueryWrapper<Dialogue> dialogueLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dialogueLambdaQueryWrapper.eq(Dialogue::getSign, sign);
            List<Dialogue> dialogues = dialogueMapper.selectList(dialogueLambdaQueryWrapper);
            for (Dialogue dialogue : dialogues) {
                if(privateMessage.getMessageType()==1)
                 dialogue.setNewContent(privateMessage.getContent());
                else if(privateMessage.getMessageType()==2)
                    dialogue.setNewContent("[图片]");
                dialogueMapper.updateById(dialogue);
            }
            if(privateMessage.getMessageType()==2){

            String imageFormat = commentService.getImageFormat(privateMessage.getContent());
            UUID messageImgName = UUID.randomUUID();
            // 创建上传目录
            File uploadDir;
            uploadDir = new File(FilePathEnum.UPLOAD_IMG_MESSAGE.getPath());
            if (!uploadDir.exists())
                uploadDir.mkdirs();
            //上传
            try {
                String base64Body = privateMessage.getContent().contains(",") ? privateMessage.getContent().split(",")[1] : privateMessage.getContent();
                byte[] decodedBytes = Base64.getDecoder().decode(base64Body);
                // 写入文件
                File dest = new File(uploadDir, messageImgName + imageFormat);
                try (FileOutputStream fos = new FileOutputStream(dest)) {
                    fos.write(decodedBytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            privateMessage.setContent("/upload/message/"+messageImgName+imageFormat);
        }
        int insert = privateMessageMapper.insert(privateMessage);
            cacheService.deleteDialogueByUserId(privateMessage.getSenderId());
            cacheService.deleteDialogueByUserId(privateMessage.getReceiverId());
            cacheService.deleteMessageByUserId(privateMessage.getSenderId(),privateMessage.getReceiverId());
            cacheService.deleteMessageByUserId(privateMessage.getReceiverId(),privateMessage.getSenderId());
        return insert>0;
    }

    @Override
    @Transactional
    public Boolean revocationMessage(Integer id, Integer userId, int flag) {

        PrivateMessage privateMessage = privateMessageMapper.selectById(id);

        if (privateMessage != null) {
            if(!Objects.equals(privateMessage.getSenderId(), userId)&&flag==1)
                return false;
            Duration duration=Duration.between(privateMessage.getSendTime(),LocalDateTime.now());
            if (duration.toMinutes()>3&&flag==1)
                return false;

            //删除图片
            if(privateMessage.getMessageType()==2){
                int lastIndexOf = privateMessage.getContent().lastIndexOf("/");
                String messageFile=(lastIndexOf!=-1)? privateMessage.getContent().substring(lastIndexOf+1):privateMessage.getContent();
                Path messagePath= Paths.get(FilePathEnum.UPLOAD_IMG_MESSAGE.getPath()+messageFile);
                try {
                    Files.delete(messagePath);
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }

            //撤回
            if(flag==1) {
                privateMessage.setContent("");
                privateMessage.setStatus(2);
                privateMessageMapper.updateById(privateMessage);
            }
            //删除
            else if(flag==2)
                privateMessageMapper.deleteById(id);

            //更新对话内容
            String sign =String.valueOf(privateMessage.getReceiverId()>privateMessage.getSenderId()?privateMessage.getSenderId():privateMessage.getReceiverId())
                    +String.valueOf(privateMessage.getReceiverId()<privateMessage.getSenderId()?privateMessage.getSenderId():privateMessage.getReceiverId());
            LambdaQueryWrapper<Dialogue> dialogueLambdaQueryWrapper=new LambdaQueryWrapper<>();
            dialogueLambdaQueryWrapper.eq(Dialogue::getSign,sign);
            List<Dialogue> dialogues = dialogueMapper.selectList(dialogueLambdaQueryWrapper);

            //查询上一句
            LambdaQueryWrapper<PrivateMessage> privateMessageLambdaQueryWrapper=new LambdaQueryWrapper<>();
            privateMessageLambdaQueryWrapper.eq(PrivateMessage::getSelectSign,sign)
                    .orderByDesc(PrivateMessage::getSendTime)
                    .last("LIMIT 1");
            PrivateMessage privateMessage1 = privateMessageMapper.selectOne(privateMessageLambdaQueryWrapper);

            for (Dialogue dialogue : dialogues) {
                if(privateMessage1!=null)
                {
                    if(privateMessage1.getStatus()==2){
                       if(Objects.equals(dialogue.getUserId(), privateMessage1.getSenderId()))
                           dialogue.setNewContent("您撤回一条消息");
                       else dialogue.setNewContent("对方撤回一条消息");
                    }else{
                        if(privateMessage1.getMessageType()==1)
                            dialogue.setNewContent(privateMessage1.getContent());
                        else if(privateMessage1.getMessageType()==2)
                            dialogue.setNewContent("[图片]");
                    }
                }
                else
                    dialogue.setNewContent(null);
                dialogueMapper.updateById(dialogue);
            }
        } else
            return false;
           cacheService.deleteDialogueByUserId(privateMessage.getSenderId());
           cacheService.deleteDialogueByUserId(privateMessage.getReceiverId());
           cacheService.deleteMessageByUserId(privateMessage.getSenderId(),privateMessage.getReceiverId());
           cacheService.deleteMessageByUserId(privateMessage.getReceiverId(),privateMessage.getSenderId());
        return true;
    }

    @Override
    @Transactional
    public Boolean putMessageStatus(Integer userId, Integer dialogueId) {

        LambdaQueryWrapper<PrivateMessage> privateMessageLambdaQueryWrapper=new LambdaQueryWrapper<>();
        privateMessageLambdaQueryWrapper.eq(PrivateMessage::getSenderId,dialogueId)
                .eq(PrivateMessage::getReceiverId,userId)
                .eq(PrivateMessage::getStatus,0);

        List<PrivateMessage> privateMessages = privateMessageMapper.selectList(privateMessageLambdaQueryWrapper);
        Users users = userMapper.selectById(userId);
        if(users!=null)
        {
            users.setMessageNumber(users.getMessageNumber()<privateMessages.size()?0:users.getMessageNumber()-privateMessages.size());
            users.setAllMessageNumber(users.getMessageNumber() + users.getAtNumber() + users.getLikeAllNumber() + users.getReplyCommentNumber());
            userMapper.updateById(users);
        }else return false;

        for (PrivateMessage privateMessage : privateMessages) {
            privateMessage.setStatus(1);
        }
        boolean b = this.updateBatchById(privateMessages);
        if(b)
        {
            cacheService.deleteMessageByUserId(userId,dialogueId);
            cacheService.deleteMessageByUserId(dialogueId,userId);
        }
        return b;
    }

    @Override
    @Transactional
    public Boolean deleteMessage(Integer id, Integer userId) {

        PrivateMessage privateMessage = privateMessageMapper.selectById(id);
        if(privateMessage!=null)
        {
            if(!(userId.equals(privateMessage.getSenderId())||userId.equals(privateMessage.getReceiverId())))
                return false;
            //说明两个人都删除了该消息
            if(privateMessage.getDeleteSign()!=null&&!privateMessage.getDeleteSign().equals(userId))
                this.revocationMessage(id,userId, 2);
            else {
                privateMessage.setDeleteSign(userId);
                privateMessageMapper.updateById(privateMessage);
            }
        }else return false;
        cacheService.deleteDialogueByUserId(privateMessage.getSenderId());
        cacheService.deleteDialogueByUserId(privateMessage.getReceiverId());
        cacheService.deleteMessageByUserId(privateMessage.getReceiverId(),privateMessage.getSenderId());
        cacheService.deleteMessageByUserId(privateMessage.getSenderId(),privateMessage.getReceiverId());
        return true;
    }

    @Override
    public SelectPrivateMessage selectMessage(Integer userId, Integer dialogueId,Integer pageNum) {

        Page<PrivateMessage> page=new Page<>(pageNum,20);

      String sign =String.valueOf(userId>dialogueId?dialogueId:userId)
              +String.valueOf(userId<dialogueId?dialogueId:userId);

      if(sign.isEmpty())
          return null;

        LambdaQueryWrapper<PrivateMessage> privateMessageLambdaQueryWrapper=new LambdaQueryWrapper<>();
        privateMessageLambdaQueryWrapper.eq(PrivateMessage::getSelectSign,sign)
                .orderByAsc(PrivateMessage::getSendTime);

        //查询用户信息
        Users users = userMapper.selectById(userId);
        Users dialogueUser = userMapper.selectById(dialogueId);
        UserInfo2 userInfo=new UserInfo2();
        BeanUtils.copyProperties(users,userInfo);
        UserInfo2 dialogueUserInfo=new UserInfo2();
        BeanUtils.copyProperties(dialogueUser,dialogueUserInfo);

        Page<PrivateMessage> privateMessagePage = privateMessageMapper.selectPage(page, privateMessageLambdaQueryWrapper);
        List<PrivateMessage> privateMessages = privateMessagePage.getRecords();
        SelectPrivateMessage selectPrivateMessages=new SelectPrivateMessage();

        List<PrivateMessageDto> privateMessageDtoList=new ArrayList<>();
        for (PrivateMessage privateMessage : privateMessages) {
            PrivateMessageDto privateMessageDto=new PrivateMessageDto();
            BeanUtils.copyProperties(privateMessage,privateMessageDto);
            if(privateMessage.getMessageType()==3)
            {
                Videos videos = videosMapper.selectById(privateMessage.getContent());
                privateMessageDto.setVideos(videos);
            }
            privateMessageDtoList.add(privateMessageDto);
        }
        selectPrivateMessages.setPrivateMessage(privateMessageDtoList);
        selectPrivateMessages.setUserInfo(userInfo);
        selectPrivateMessages.setDialogueUserInfo(dialogueUserInfo);

        return selectPrivateMessages;
    }

    @Override
    @Transactional
    public Boolean putMessageNumber(Integer userId, Integer messageMenu) {

        if(messageMenu!=2&&messageMenu!=3&&messageMenu!=4)
            return false;
        Users users = userMapper.selectById(userId);
        if(users==null)
            return false;
        if(messageMenu==2){
            users.setReplyCommentNumber(0);
            users.setAllMessageNumber(users.getMessageNumber()+users.getAtNumber()+users.getLikeAllNumber()+users.getReplyCommentNumber());
            userMapper.updateById(users);
        }else if(messageMenu==3)
        {
            users.setAtNumber(0);
            users.setAllMessageNumber(users.getMessageNumber()+users.getAtNumber()+users.getLikeAllNumber()+users.getReplyCommentNumber());
            userMapper.updateById(users);
        }else {
            users.setLikeAllNumber(0);
            users.setAllMessageNumber(users.getMessageNumber()+users.getAtNumber()+users.getLikeAllNumber()+users.getReplyCommentNumber());
            userMapper.updateById(users);
        }
        return true;
    }

    @Override
    public Boolean checkFollowAndFans(PrivateMessage privateMessage) {

        LambdaQueryWrapper<PrivateMessage> privateMessageLambdaQueryWrapper2=new LambdaQueryWrapper<>();
        privateMessageLambdaQueryWrapper2.eq(PrivateMessage::getSenderId,privateMessage.getReceiverId())
                .eq(PrivateMessage::getReceiverId,privateMessage.getSenderId())
                .last("LIMIT 1");
        PrivateMessage privateMessage2 = privateMessageMapper.selectOne(privateMessageLambdaQueryWrapper2);

        //判断对话是否回复
        if(privateMessage2!=null)
            return true;

        LambdaQueryWrapper<PrivateMessage> privateMessageLambdaQueryWrapper=new LambdaQueryWrapper<>();
        privateMessageLambdaQueryWrapper.eq(PrivateMessage::getSenderId,privateMessage.getSenderId())
                .eq(PrivateMessage::getReceiverId,privateMessage.getReceiverId())
                .last("LIMIT 1");
        PrivateMessage privateMessage1 = privateMessageMapper.selectOne(privateMessageLambdaQueryWrapper);

        //判断是否互相关注
        LambdaQueryWrapper<Fans> fansLambdaQueryWrapper=new LambdaQueryWrapper<>();
        fansLambdaQueryWrapper.eq(Fans::getUserId,privateMessage.getSenderId())
                .eq(Fans::getFansId,privateMessage.getReceiverId());
        Fans fans = fansMapper.selectOne(fansLambdaQueryWrapper);
        if(fans==null&&privateMessage1!=null)
            return false;

        LambdaQueryWrapper<Fans> fansLambdaQueryWrapper1=new LambdaQueryWrapper<>();
        fansLambdaQueryWrapper1.eq(Fans::getUserId,privateMessage.getReceiverId())
                .eq(Fans::getFansId,privateMessage.getSenderId());
        Fans fans1 = fansMapper.selectOne(fansLambdaQueryWrapper1);
        if(fans1==null&&privateMessage1!=null)
            return false;
        return true;

    }

    @Override
    @Transactional
    public Boolean shareVideo(ShareVideo shareVideo) {

        List<Integer> userIdList = shareVideo.getUserIdList();
        Integer userId=shareVideo.getUserId();

        //遍历分享视频
        for (Integer dialogueId : userIdList) {

            String sign=String.valueOf(userId>dialogueId? dialogueId:userId)
                    +String.valueOf(userId>dialogueId? userId:dialogueId);

            //查询是否存在对话框，没有则创建或者更新
            this.addDialogue(sign,userId,dialogueId,shareVideo.getContent());
            this.addDialogue(sign,dialogueId,userId,shareVideo.getContent());

            //新增分享视频的消息
            PrivateMessage privateMessage=PrivateMessage
                    .builder()
                    .senderId(userId)
                    .receiverId(dialogueId)
                    .content(String.valueOf(shareVideo.getVideoId()))
                    .selectSign(sign)
                    .messageType(3)
                    .sendTime(LocalDateTime.now())
                    .build();
            privateMessageMapper.insert(privateMessage);

            //判断用户是否连带发送一条消息
            if(!shareVideo.getContent().isEmpty()){
                //新增分享视频的消息
                PrivateMessage privateMessage1=PrivateMessage
                        .builder()
                        .senderId(userId)
                        .receiverId(dialogueId)
                        .content(shareVideo.getContent())
                        .selectSign(sign)
                        .messageType(1)
                        .sendTime(LocalDateTime.now())
                        .build();
                privateMessageMapper.insert(privateMessage1);
            }
            cacheService.deleteMessageByUserId(userId,dialogueId);
        }

        //更新分享视频的数量
        Videos videos = videosMapper.selectById(shareVideo.getVideoId());
        videos.setShareNumber(videos.getShareNumber()+userIdList.size());
        videosMapper.updateById(videos);

        cacheService.deleteDialogueByUserId(userId);
        return true;
    }


    public void addDialogue(String sign,Integer userId,Integer dialogueId,String content){

        LambdaQueryWrapper<Dialogue> dialogueLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dialogueLambdaQueryWrapper.eq(Dialogue::getUserId,userId)
                .eq(Dialogue::getDialogueId,dialogueId);
        Dialogue dialogue = dialogueMapper.selectOne(dialogueLambdaQueryWrapper);
        if(dialogue==null)
        {
            Dialogue dialogue1=Dialogue
                    .builder()
                    .userId(userId)
                    .dialogueId(dialogueId)
                    .newContent(content.isEmpty()? "[视频]":content)
                    .upDateTime(LocalDateTime.now())
                    .sign(sign)
                    .build();
            dialogueMapper.insert(dialogue1);
        }
        else {
            dialogue.setNewContent(content.isEmpty()? "[视频]":content);
            dialogue.setUpDateTime(LocalDateTime.now());
            dialogueMapper.updateById(dialogue);
        }

    }
}
