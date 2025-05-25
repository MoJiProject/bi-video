package com.moji.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class CacheService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;



    /**
     * 根据视频id删除评论缓存
     * @param videoId
     * @param userId
     */
    public void deleteCommentCacheByVideoId(Integer videoId,Integer dynamicId,Integer userId){
        Set<String> keys = null;
        if(userId!=null) {
            if(videoId!=null)
                keys = stringRedisTemplate.keys("selectCommentByVideoId::videoId:" + videoId + "dynamicId:null" + "userId:" + userId + "commentSortFlag:*pageNum:*");
            else if (dynamicId != null) {
                keys = stringRedisTemplate.keys("selectCommentByVideoId::videoId:null" + "dynamicId:" + dynamicId + "userId:" + userId + "commentSortFlag:*pageNum:*");
            }
            else{
                keys = stringRedisTemplate.keys("selectCommentByVideoId::videoId:*" + "dynamicId:*" + "userId:" + userId + "commentSortFlag:*pageNum:*");
            }
        }
        else{
            if(videoId!=null)
                keys = stringRedisTemplate.keys("selectCommentByVideoId::videoId:"+videoId+"*");
            else if(dynamicId!=null)
                keys = stringRedisTemplate.keys("selectCommentByVideoId::videoId:null"+"dynamicId:"+dynamicId+"*");
        }
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);  // 批量删除
        }
    }


    /**
     * 根据评论id删除回复评论缓存
     * @param commentId
     * @param userId
     */
    public void deleteReplyCommentCacheByCommentId(Integer videoId,Integer dynamicId,Integer commentId,Integer userId){
        Set<String> keys=null;
        if(commentId!=null&&userId!=null) {
            if (videoId != null)
                keys = stringRedisTemplate.keys("selectReplyById::videoId:null"+"dynamicId:"+dynamicId+"commentId:*");
            else if (dynamicId != null)
                keys = stringRedisTemplate.keys("selectReplyById::videoId:null"+"dynamicId:"+dynamicId+"commentId:"+commentId+"userId:"+userId+"pageNum:*");
        }
        else{
            if(videoId!=null)
                keys = stringRedisTemplate.keys("selectReplyById::videoId:"+videoId+"dynamicId:*");
            else if (dynamicId != null)
                keys = stringRedisTemplate.keys("selectReplyById::videoId:null"+"dynamicId:"+dynamicId+"commentId:*");
        }
        if(commentId==null&&videoId==null&&dynamicId==null)
        {
            keys = stringRedisTemplate.keys("selectReplyById::videoId:*"+"dynamicId:*"+"commentId:*userId:"+userId+"pageNum:*");
        }
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);  // 批量删除
        }
    }


    /**
     * 根据用户id删除私信列表对话框缓存
     * @param userId
     */
    public void deleteDialogueByUserId(Integer userId){
        Set<String> keys = stringRedisTemplate.keys("selectDialogue::userId:"+userId+"*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);  // 批量删除
        }
    }


    /**
     * 根据用户id删除消息缓存
     * @param userId
     * @param dialogueId
     */
    public void deleteMessageByUserId(Integer userId,Integer dialogueId){
        Set<String> keys = stringRedisTemplate.keys("selectMessage::userId:"+userId+"dialogueId:"+dialogueId+"*");
        if(dialogueId==null)
        {
            keys = stringRedisTemplate.keys("selectMessage::userId:"+userId+"dialogueId:*");
        }
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);  // 批量删除
        }
    }





}
