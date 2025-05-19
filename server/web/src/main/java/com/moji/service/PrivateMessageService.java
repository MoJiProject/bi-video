package com.moji.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moji.dto.SelectDialogue;
import com.moji.dto.SelectPrivateMessage;
import com.moji.dto.ShareVideo;
import com.moji.po.Dialogue;
import com.moji.po.PrivateMessage;

import java.util.List;

public interface PrivateMessageService extends IService<PrivateMessage> {
   List<SelectDialogue> selectDialogueList(Integer userId, Integer pageNum);

    Boolean putUpStatus(Integer userId, Integer id);

    Boolean putDndStatus(Integer userId, Integer id);

    Boolean addDialogue(Dialogue dialogue);

    Boolean deleteDialogue(Integer id);

    Boolean sendMessage(PrivateMessage privateMessage);

    Boolean revocationMessage(Integer id, Integer userId, int i);

    Boolean putMessageStatus(Integer userId, Integer dialogueId);

    Boolean deleteMessage(Integer id, Integer userId);

    SelectPrivateMessage selectMessage(Integer userId, Integer dialogueId,Integer pageNum);

    Boolean putMessageNumber(Integer userId, Integer messageMenu);

    Boolean checkFollowAndFans(PrivateMessage privateMessage);

    Boolean shareVideo(ShareVideo shareVideo);
}
