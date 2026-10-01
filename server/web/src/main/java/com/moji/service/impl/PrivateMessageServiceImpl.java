package com.moji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moji.FilePathEnum;
import com.moji.R;
import com.moji.dto.PrivateMessageDto;
import com.moji.dto.SelectDialogue;
import com.moji.dto.SelectPrivateMessage;
import com.moji.dto.ShareVideo;
import com.moji.mapper.*;
import com.moji.po.*;
import com.moji.service.CacheService;
import com.moji.service.CommentService;
import com.moji.service.DialogueService;
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
import java.util.stream.Collectors;

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
    @Autowired
    private DialogueService dialogueService;


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

        if (!dialoguesRecords.isEmpty()) {
            List<Integer> dialogueIds = dialoguesRecords.stream()
                    .map(Dialogue::getDialogueId)
                    .distinct()
                    .collect(Collectors.toList());
            List<Users> usersList = userMapper.selectBatchIds(dialogueIds);
            Map<Integer, Users> userMap = usersList.stream()
                    .collect(Collectors.toMap(Users::getId, u -> u));
            for (Dialogue dialogue : dialoguesRecords) {
                Users users = userMap.get(dialogue.getDialogueId());
                UserInfo2 userInfo2 = new UserInfo2();
                BeanUtils.copyProperties(users, userInfo2);
                SelectDialogue selectDialogue=new SelectDialogue();
                //查询未读的数量（跳过当前用户已删除的消息）
                LambdaQueryWrapper<PrivateMessage> privateMessageLambdaQueryWrapper = new LambdaQueryWrapper<>();
                privateMessageLambdaQueryWrapper.eq(PrivateMessage::getSenderId, dialogue.getDialogueId())
                        .eq(PrivateMessage::getReceiverId, dialogue.getUserId())
                        .eq(PrivateMessage::getStatus, 0)
                        .and(wrapper->wrapper.isNull(PrivateMessage::getDeleteSign)
                                .or().ne(PrivateMessage::getDeleteSign, dialogue.getUserId()));
                List<PrivateMessage> notReadMessage = privateMessageMapper.selectList(privateMessageLambdaQueryWrapper);
                LambdaQueryWrapper<PrivateMessage> privateMessageLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
                privateMessageLambdaQueryWrapper1.eq(PrivateMessage::getSelectSign, dialogue.getSign())
                        .and(wrapper->wrapper.isNull(PrivateMessage::getDeleteSign)
                                .or().ne(PrivateMessage::getDeleteSign, dialogue.getUserId()));
                List<PrivateMessage> privateMessages = privateMessageMapper.selectList(privateMessageLambdaQueryWrapper1);
                selectDialogue.setDialogue(dialogue);
                selectDialogue.setUserInfo(userInfo2);
                selectDialogue.setAllMessageNumber(privateMessages.size());
                selectDialogue.setNotReadNumber(notReadMessage.size());
                selectDialogueList.add(selectDialogue);
            }
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
            if(!dialogues.isEmpty()) {
                for (Dialogue dialogue : dialogues) {
                    if (privateMessage.getMessageType() == 1)
                        dialogue.setNewContent(privateMessage.getContent());
                    else if (privateMessage.getMessageType() == 2)
                        dialogue.setNewContent("[图片]");
                }
            dialogueService.updateBatchById(dialogues);
        }
            if(privateMessage.getMessageType()==2){
                if(FilePathEnum.canUpload()){
                   throw new RuntimeException("服务器存储空间不足，无法上传图片，请联系管理员");
                }
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
                FilePathEnum.saveAsWebp(decodedBytes,uploadDir, String.valueOf(messageImgName));
            } catch (IOException e) {
               throw new RuntimeException("图片发送失败");
            }
            privateMessage.setContent("/upload/message/"+messageImgName+".webp");
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

            //先记下是否未读，撤回会先把状态改成2
            boolean unread=Objects.equals(privateMessage.getStatus(),0);

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

            //撤回/彻底删除未读消息时同步扣掉接收方的未读数，否则消息数会残留
            if(unread)
                this.reduceReceiverMessageNumber(privateMessage.getReceiverId());

//更新对话内容
            String sign=String.valueOf(privateMessage.getReceiverId()>privateMessage.getSenderId()?privateMessage.getSenderId():privateMessage.getReceiverId())
                    +String.valueOf(privateMessage.getReceiverId()<privateMessage.getSenderId()?privateMessage.getSenderId():privateMessage.getReceiverId());
            this.refreshDialoguePreview(sign);
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
            cacheService.deleteDialogueByUserId(userId);
            cacheService.deleteDialogueByUserId(dialogueId);
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
            //同一个人重复删除不做处理，避免把对方删掉的消息又标记回来
            if(Objects.equals(privateMessage.getDeleteSign(),userId))
                return true;
            //说明两个人都删除了该消息
            if(privateMessage.getDeleteSign()!=null&&!privateMessage.getDeleteSign().equals(userId))
                this.revocationMessage(id,userId, 2);
            else {
                privateMessage.setDeleteSign(userId);
                privateMessageMapper.updateById(privateMessage);
                //软删除也要刷新会话预览，否则左侧列表还留着已删内容
                String sign=String.valueOf(privateMessage.getSenderId()>privateMessage.getReceiverId()?privateMessage.getReceiverId():privateMessage.getSenderId())
                        +String.valueOf(privateMessage.getSenderId()<privateMessage.getReceiverId()?privateMessage.getReceiverId():privateMessage.getSenderId());
                this.refreshDialoguePreview(sign);
            }
        }else return false;
        cacheService.deleteDialogueByUserId(privateMessage.getSenderId());
        cacheService.deleteDialogueByUserId(privateMessage.getReceiverId());
        cacheService.deleteMessageByUserId(privateMessage.getReceiverId(),privateMessage.getSenderId());
        cacheService.deleteMessageByUserId(privateMessage.getSenderId(),privateMessage.getReceiverId());
        return true;
    }

    //扣减接收方未读消息数并同步总消息数，避免撤回后红点残留
    private void reduceReceiverMessageNumber(Integer receiverId) {

        if (receiverId == null)
            return;
        Users receiver = userMapper.selectById(receiverId);
        if (receiver == null)
            return;
        receiver.setMessageNumber(receiver.getMessageNumber() == null || receiver.getMessageNumber() <= 0
                ? 0
                : receiver.getMessageNumber() - 1);
        receiver.setAllMessageNumber(receiver.getMessageNumber()
                + (receiver.getReplyCommentNumber() == null ? 0 : receiver.getReplyCommentNumber())
                + (receiver.getAtNumber() == null ? 0 : receiver.getAtNumber())
                + (receiver.getLikeAllNumber() == null ? 0 : receiver.getLikeAllNumber()));
        userMapper.updateById(receiver);
    }

    //按每个会话用户可见的最新消息刷新预览文案（跳过该用户已删除的消息）
    private void refreshDialoguePreview(String sign) {

        LambdaQueryWrapper<Dialogue> dialogueLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dialogueLambdaQueryWrapper.eq(Dialogue::getSign,sign);
        List<Dialogue> dialogues = dialogueMapper.selectList(dialogueLambdaQueryWrapper);
        if(dialogues.isEmpty())
            return;

        for (Dialogue dialogue : dialogues) {
            LambdaQueryWrapper<PrivateMessage> privateMessageLambdaQueryWrapper=new LambdaQueryWrapper<>();
            privateMessageLambdaQueryWrapper.eq(PrivateMessage::getSelectSign,sign)
                    .and(wrapper->wrapper.isNull(PrivateMessage::getDeleteSign)
                            .or().ne(PrivateMessage::getDeleteSign,dialogue.getUserId()))
                    .orderByDesc(PrivateMessage::getSendTime)
                    .last("LIMIT 1");
            PrivateMessage latest = privateMessageMapper.selectOne(privateMessageLambdaQueryWrapper);
            dialogue.setNewContent(buildDialoguePreview(latest,dialogue.getUserId()));
        }
        dialogueService.updateBatchById(dialogues);
    }

    private String buildDialoguePreview(PrivateMessage message,Integer viewerId) {

        if (message == null)
            return null;
        if (Objects.equals(message.getStatus(),2))
            return Objects.equals(viewerId,message.getSenderId())?"您撤回一条消息":"对方撤回一条消息";
        if (Objects.equals(message.getMessageType(),1))
            return message.getContent();
        if (Objects.equals(message.getMessageType(),2))
            return "[图片]";
        if (Objects.equals(message.getMessageType(),3))
            return "[视频]";
        return message.getContent();
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
                //当前用户删除过的消息不再返回，避免删除后刷新又出现
                .and(wrapper->wrapper.isNull(PrivateMessage::getDeleteSign)
                        .or().ne(PrivateMessage::getDeleteSign,userId))
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
        if (!privateMessages.isEmpty()) {
            List<Integer> videoIds = privateMessages.stream()
                    .filter(privateMessage -> privateMessage.getMessageType() == 3)
                    .map(privateMessage -> Integer.valueOf(privateMessage.getContent()))
                    .distinct()
                    .collect(Collectors.toList());
            if(!videoIds.isEmpty()){
                List<Videos> videosList = videosMapper.selectBatchIds(videoIds);
                Map<Integer, Videos> videoMap = videosList.stream()
                        .collect(Collectors.toMap(Videos::getId, v -> v));
                for (PrivateMessage privateMessage : privateMessages) {
                    PrivateMessageDto privateMessageDto = new PrivateMessageDto();
                    BeanUtils.copyProperties(privateMessage, privateMessageDto);
                    if (privateMessage.getMessageType() == 3) {
                        Videos video = videoMap.get(Integer.valueOf(privateMessage.getContent()));
                        privateMessageDto.setVideos(video);
                    }
                    privateMessageDtoList.add(privateMessageDto);
                }
            }else{
                for (PrivateMessage privateMessage : privateMessages) {
                    PrivateMessageDto privateMessageDto = new PrivateMessageDto();
                    BeanUtils.copyProperties(privateMessage, privateMessageDto);
                    privateMessageDtoList.add(privateMessageDto);
                }
            }
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
