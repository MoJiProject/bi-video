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
        Set<String> keys2 = null;
        if(videoId!=null) {
            keys = stringRedisTemplate.keys("selectCommentByVideoId::videoId:" + videoId + "dynamicId:null" + "userId:" + userId + "commentSortFlag:*pageNum:*");
            keys2 = stringRedisTemplate.keys("selectCommentByVideoId::videoId:" + videoId + "dynamicId:null" + "userId:0" + "commentSortFlag:*pageNum:*");
        }
        else if (dynamicId != null) {
            keys = stringRedisTemplate.keys("selectCommentByVideoId::videoId:null" + "dynamicId:" + dynamicId + "userId:" + userId + "commentSortFlag:*pageNum:*");
            keys2 = stringRedisTemplate.keys("selectCommentByVideoId::videoId:null" + "dynamicId:" + dynamicId + "userId:0" + "commentSortFlag:*pageNum:*");
        }
        else{
            keys = stringRedisTemplate.keys("selectCommentByVideoId::videoId:*" + "dynamicId:*" + "userId:" + userId + "commentSortFlag:*pageNum:*");
            keys2 = stringRedisTemplate.keys("selectCommentByVideoId::videoId:*" + "dynamicId:*" + "userId:0" + "commentSortFlag:*pageNum:*");
        }
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);  // 批量删除
        }
        if(keys2 !=null && !keys2.isEmpty()){
            stringRedisTemplate.delete(keys2);
        }
    }


    /**
     * 根据评论id删除回复评论缓存
     * @param commentId
     * @param userId
     */
    public void deleteReplyCommentCacheByCommentId(Integer videoId,Integer dynamicId,Integer commentId,Integer userId){
        Set<String> keys=null;
        if(commentId!=null) {
            if (videoId != null) {
                keys = stringRedisTemplate.keys("selectReplyById::videoId:" + videoId + "dynamicId:*");
            }  else if (dynamicId != null) {
                keys = stringRedisTemplate.keys("selectReplyById::videoId:null" + "dynamicId:" + dynamicId + "commentId:" + commentId + "userId:" + userId + "pageNum:*");
            }
            else{
                keys = stringRedisTemplate.keys("selectCommentByVideoId::videoId:*" + "dynamicId:*" + "userId:" + userId + "commentSortFlag:*pageNum:*");
            }
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
