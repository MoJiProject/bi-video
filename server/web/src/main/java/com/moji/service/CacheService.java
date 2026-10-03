package com.moji.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CacheService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //单批扫描/删除的key数量，避免一次性把大量key堆进内存
    private static final int DELETE_BATCH_SIZE = 1000;

    //单次SCAN的游标步长
    private static final int SCAN_COUNT = 1000;


    /**
     * 根据视频id删除评论缓存
     * @param videoId
     * @param dynamicId
     * @param userId
     */
    public void deleteCommentCacheByVideoId(Integer videoId,Integer dynamicId,Integer userId){
        //评论列表的缓存key里带userId，因为缓存内容与「当前查看者是否点赞/是否UP」有关，属于按人维度缓存。
        //评论一旦被删除或修改，所有查看者的列表都会受影响，
        //原来只清当前调用者与userId:0两个维度，其余人的缓存会带着脏数据留满7天，所以这里按 userId:* 全量失效。
        if(videoId!=null) {
            deleteByPattern("selectCommentByVideoId::videoId:" + videoId + "dynamicId:null*");
            deleteByPattern("selectReplyById::videoId:" + videoId + "*");
        }
        else if (dynamicId != null) {
            deleteByPattern("selectCommentByVideoId::videoId:nulldynamicId:" + dynamicId + "*");
            deleteByPattern("selectReplyById::videoId:nulldynamicId:" + dynamicId + "*");
        }
        else {
            deleteByPattern("selectCommentByVideoId::videoId:*dynamicId:*userId:" + userId + "*");
            deleteByPattern("selectReplyById::videoId:*dynamicId:*userId:" + userId + "*");
        }
    }


    /**
     * 根据评论id删除回复评论缓存
     * @param commentId
     */
    public void deleteReplyCommentCacheByCommentId(Integer videoId,Integer dynamicId,Integer commentId,Integer userId){
        if(commentId==null)
            return;

        //回复列表同样是按查看者维度缓存，主评论内容变化会影响所有查看者看到的回复，所以要带上 userId:*
        if (videoId != null) {
            deleteByPattern("selectReplyById::videoId:" + videoId + "dynamicId:*commentId:" + commentId + "userId:*");
        }  else if (dynamicId != null) {
            deleteByPattern("selectReplyById::videoId:nulldynamicId:" + dynamicId + "commentId:" + commentId + "userId:*");
        }
        else{
            //原来这里错用了 selectCommentById 这个缓存名前缀，
            //结果是删掉了不相关的评论列表缓存，真正的回复列表缓存反而留着脏数据
            deleteByPattern("selectReplyById::videoId:*dynamicId:*commentId:" + commentId + "userId:*");
        }
    }


    /**
     * 根据用户id删除私信列表对话框缓存
     * @param userId
     */
    public void deleteDialogueByUserId(Integer userId){
        deleteByPattern("selectDialogue::userId:"+userId+"*");
    }


    /**
     * 根据用户id删除消息缓存
     * @param userId
     * @param dialogueId
     */
    public void deleteMessageByUserId(Integer userId,Integer dialogueId){
        if(dialogueId==null)
            deleteByPattern("selectMessage::userId:"+userId+"dialogueId:*");
        else
            deleteByPattern("selectMessage::userId:"+userId+"dialogueId:"+dialogueId+"*");
    }


    /**
     * 按模式批量删除缓存key。
     * 用SCAN而不是KEYS：KEYS是O(N)且会阻塞Redis主线程，
     * 评论/私信这类缓存量大的场景下足以拖慢整个缓存服务。
     *
     * @param pattern 匹配模式
     */
    private void deleteByPattern(String pattern) {

        if (pattern == null || pattern.isEmpty())
            return;

        try {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(SCAN_COUNT)
                    .build();

            List<String> batch = new ArrayList<>(DELETE_BATCH_SIZE);
            try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
                while (cursor.hasNext()) {
                    batch.add(cursor.next());
                    if (batch.size() >= DELETE_BATCH_SIZE) {
                        stringRedisTemplate.delete(batch);
                        batch.clear();
                    }
                }
            }
            if (!batch.isEmpty())
                stringRedisTemplate.delete(batch);
        } catch (Exception e) {
            //缓存清理失败不应该让主流程报错
            System.out.println("缓存清理失败 pattern=" + pattern + " err=" + e.getMessage());
        }
    }

}
