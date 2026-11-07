<template>
    <div class="reply-container" v-mention-hover>
      <div class="head2">回复我的</div>
      <div class="reply-content" @scroll="handleScroll">
        <div v-show="replyList.length>0" class="reply-item" v-for="reply in replyList" :key="reply.replyComments.id">
           <div class="reply-item-content">
              <UserHoverCard :user-id="Number(reply.userId) || 0" :avatar="reply.avatarAddress || ''" :nickname="reply.userName || ''" :avatar-size="32">
                <template #reference>
                  <a :href="'./home?homeMenu=1&userId='+reply.userId" target="_blank"><img class="reply-user-avatar" :src="reply.avatarAddress"></a>
                </template>
              </UserHoverCard>
              <div class="reply-right-content">
                 <div class="reply-right-content-top">
                  <UserHoverCard :user-id="Number(reply.userId) || 0" :avatar="reply.avatarAddress || ''" :nickname="reply.userName || ''" :avatar-size="14">
                    <template #reference>
                      <a :href="'./home?homeMenu=1&userId='+reply.userId" target="_blank">{{ reply.userName }}</a>
                    </template>
                  </UserHoverCard>
                  <span v-if="reply.mainComments&&!reply.dynamic">回复了我的评论</span>
                  <span v-else-if="!reply.mainComments&&reply.dynamic">对我的动态发表了评论</span>
                  <span v-else>对我的视频发表了评论</span>
                </div>
                <div class="reply-right-content-center">
                 <a v-if="reply.mainComments&&!reply.dynamic&&reply.replyComments.videoId&&!reply.replyComments.dynamicId&&(reply.replyComments.deleteSign===0&&reply.replyComments.status===0)" class="reply-message-content" :href="'./video?videoId=BV'+reply.mainComments?.videoId+'&commentId='+reply.replyComments.mainCommentId+'&replyId='+reply.replyComments.id" target="videoWindow">
                  <div class="reply-message-content">
                    <span v-if="reply.replyComments.replyCommentId!==reply.replyComments.mainCommentId">回复 </span>
                    <el-tooltip
                    v-if="reply.replyComments.replyCommentId!==reply.replyComments.mainCommentId"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="store?.userInformation.userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                    >
                      <a class="add-reply-user-name" v-if="reply.replyComments.replyCommentId!==reply.replyComments.mainCommentId" :href="'./home?homeMenu=1&userId='+store.userId" target="_blank">@{{ store?.userInformation.userName }} :</a>
                    </el-tooltip>
                    <span v-html="addImgFlagF(reply.replyComments,false)"></span> 
                  </div>
                   <div class="reply-message-content2">
                       <span>{{ store?.userInformation.userName }} : </span>
                       <span class="reply-message-content1" v-html="addImgFlagF(reply.mainComments,true)"></span>
                   </div>
                 </a>
                 <a v-else-if="reply.mainComments&&!reply.dynamic&&reply.replyComments.videoId&&!reply.replyComments.dynamicId&&(reply.replyComments.deleteSign===1||reply.replyComments.status===1)" class="reply-message-content" :href="'./video?videoId=BV'+reply.mainComments?.videoId" target="videoWindow">
                  <div class="reply-message-content">
                    <span v-if="reply.replyComments.replyCommentId!==reply.replyComments.mainCommentId">回复 </span>
                    <el-tooltip
                    v-if="reply.replyComments.replyCommentId!==reply.replyComments.mainCommentId"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="store?.userInformation.userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                    >
                      <a class="add-reply-user-name" v-if="reply.replyComments.replyCommentId!==reply.replyComments.mainCommentId" :href="'./home?homeMenu=1&userId='+store.userId" target="_blank">@{{ store?.userInformation.userName }} :</a>
                    </el-tooltip>
                    <span v-html="addImgFlagF(reply.replyComments,false)"></span> 
                  </div>
                   <div class="reply-message-content2">
                       <span>{{ store?.userInformation.userName }} : </span>
                       <span class="reply-message-content1" v-html="addImgFlagF(reply.mainComments,true)"></span>
                   </div>
                 </a>
                 <a v-else-if="!reply.mainComments&&reply.replyComments.videoId&&!reply.dynamic&&!reply.replyComments.dynamicId" class="reply-message-content" :href="'./video?videoId=BV'+reply.replyComments?.videoId+'&commentId='+reply.replyComments?.id" target="videoWindow">
                  <div class="reply-message-content">
                    <span v-html="addImgFlagF(reply.replyComments,false)"></span> 
                  </div>
                 </a>
                 <a v-else-if="reply.dynamic||reply.replyComments.dynamicId" class="reply-message-content" :href="'./dynamicDetail?dynamicId='+reply.replyComments.dynamicId+'&commentId='+(reply.replyComments?.mainCommentId||reply.replyComments?.id)+'&replyId='+reply.replyComments?.id" target="dynamicWindow">
                  <div class="reply-message-content">
                    <span v-html="addImgFlagF(reply.replyComments,false)"></span> 
                  </div>
                 </a>
                </div>
                <div class="reply-right-content-bottom">
                    <span class="reply-time">{{ reply.replyComments.commentTime }}</span>
                    <button type="button" class="reply-comment" @click="openReplyComment(reply)"><svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M7 18 3 21V6a3 3 0 0 1 3-3h12a3 3 0 0 1 3 3v9a3 3 0 0 1-3 3H7Z"/><path d="M7 8h10M7 12h7"/></svg> <span>回复</span></button>
                    <button type="button" v-show="reply.replyCommentControls?.controls!==1" class="love-comment" v-debounce @click="commentControlesF(reply)"><svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/></svg> <span>点赞</span></button>
                    <button type="button" v-show="reply.replyCommentControls?.controls===1" class="love-comment2" v-debounce @click="commentControlesF(reply)"><svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/></svg> <span>已赞</span></button>
                    <button type="button" class="delete-comment-notification" @click="deleteCommentDialogFlag=true,deleteCommentNotificaitonId=reply.replyComments.id"><svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/><path d="M10 11v6"/><path d="M14 11v6"/></svg> <span>删除</span></button>
                </div>
                <div v-if="activeReplyId===reply.id" class="reply-form">
                  <PostEditor :ref="setReplyInput" v-model="replyContent" compact :max-length="1000" :disabled="replySending"
                    :placeholder="`回复 @${reply.replyComments.userName}`" @length="replyContentLength = $event">
                    <template #tools>
                      <label class="editor-tool comment-upload" :class="{ disabled: replySending || replyUploading || replyImages.length >= 9 }">
                        <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="3" width="18" height="18" rx="4"/><circle cx="8" cy="8" r="1.5"/><path d="m3 17 5-5 4 4 4-6 5 7"/></svg>
                        <span>图片</span><span v-if="replyImages.length">{{ replyImages.length }}/9</span>
                        <input type="file" accept="image/png,image/jpeg,image/gif" multiple
                          aria-label="添加回复图片，最多九张" :disabled="replySending || replyUploading || replyImages.length >= 9" @change="uploadReply">
                      </label>
                    </template>
                    <template #after-tools>
                      <button type="button" class="editor-tool share-tool" :class="{ checked: replyShareToDynamic }"
                        :aria-pressed="replyShareToDynamic" :disabled="replySending" @click="replyShareToDynamic = !replyShareToDynamic">
                        <svg class="coin" viewBox="0 0 48 48" aria-hidden="true"><rect x="4" y="4" width="40" height="40" rx="12" fill="none" stroke="currentColor" stroke-width="4"/><path v-if="replyShareToDynamic" d="M15 25 L24 33 L35 17" fill="none" stroke="currentColor" stroke-width="6" stroke-linecap="round" stroke-linejoin="round"/></svg>
                        <span>同时转发到我的动态</span>
                      </button>
                    </template>
                    <template #actions>
                      <el-button type="primary" :loading="replySending" @click="replayCommentF(reply)">回复</el-button>
                    </template>
                  </PostEditor>
                  <div v-if="replyImages.length" class="attachments">
                    <div v-for="(image, index) in replyImages" :key="image">
                      <el-image :src="image" :preview-src-list="replyImages" preview-teleported fit="cover"/>
                      <el-button text type="danger" @click="replyImages.splice(index, 1)">移除</el-button>
                    </div>
                  </div>
                </div>
                <el-tooltip
                    v-if="!reply.mainComments&&!reply.dynamic&&reply.replyComments.videoId"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="reply.videoTitle"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a v-if="!reply.mainComments&&!reply.dynamic&&reply.replyComments.videoId" class="reply-video-cover-container" :href="'./video?videoId=BV'+reply.replyComments.videoId" target="videoWindow"><img class="reply-video-cover" :src="reply.coverAddress"></a>
                </el-tooltip>
                <el-tooltip
                    v-if="!reply.mainComments&&(reply.dynamic||reply.replyComments.dynamicId)&&reply.dynamic.imgAddress"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="reply.dynamic.title"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a v-if="!reply.mainComments&&(reply.dynamic||reply.replyComments.dynamicId)&&reply.dynamic.imgAddress" class="reply-video-cover-container" :href="'./dynamicDetail?dynamicId='+reply.replyComments.dynamicId+'&commentId='+(reply.replyComments?.mainCommentId||reply.replyComments?.id)+'&replyId='+reply.replyComments?.id" target="dynamicWindow"><img class="reply-video-cover" :src="reply.dynamic.imgAddress.split(',')[0].split('?')[0]"></a>
                </el-tooltip>
<el-tooltip
    v-if="reply.mainComments&&(reply.replyComments.videoId||reply.replyComments.dynamicId)"
    popper-class="custom-tooltip"
    effect="light"
    :show-after="300"
    placement="bottom-end"
    :offset="12"
    :show-arrow="false"
    :hide-after="0"
  >
    <template #content>
          <div v-html="addImgFlagF(reply.mainComments,false)"></div>
    </template>
    <a v-if="(reply.mainComments?.deleteSign===0&&reply.mainComments?.status===0)&&reply.replyComments.videoId&&!reply.replyComments.dynamicId" class="comment-content" :href="'./video?videoId=BV'+reply.replyComments.videoId+'&commentId='+reply.replyComments.mainCommentId+'&replyId='+reply.mainComments.id" target="videoWindow"><span v-html="addImgFlagF(reply.mainComments,false)"></span></a>
    <a v-else-if="(reply.mainComments?.deleteSign===1||reply.mainComments?.status===1)&&reply.replyComments.videoId&&!reply.replyComments.dynamicId" class="comment-content" :href="'./video?videoId=BV'+reply.replyComments.videoId" target="videoWindow"><span v-html="addImgFlagF(reply.mainComments,false)"></span></a>
    <a v-else-if="(reply.mainComments?.deleteSign===0&&reply.mainComments?.status===0)&&reply.replyComments.dynamicId" class="comment-content" :href="'./dynamicDetail?dynamicId='+reply.replyComments.dynamicId+'&commentId='+reply.replyComments.mainCommentId+'&replyId='+reply.mainComments.id" target="dynamicWindow"><span v-html="addImgFlagF(reply.mainComments,false)"></span></a>
    <a v-else-if="(reply.mainComments?.deleteSign===1||reply.mainComments?.status===1)&&reply.replyComments.dynamicId" class="comment-content" :href="'./dynamicDetail?dynamicId='+reply.replyComments.dynamicId" target="dynamicWindow"><span v-html="addImgFlagF(reply.mainComments,false)"></span></a>
</el-tooltip>
                <div class="reply-line"></div>
              </div>
           </div>
        </div>
        <div v-show="replyList.length===0&&selectReplayCommentFFlag" class="no-reply-data">
          <AppEmptyState :title="loadError || '暂无回复通知'" :kind="loadError ? 'unavailable' : undefined" :action-text="loadError ? '重新加载' : undefined" compact @action="selectReplayCommentF"/>
        </div>
        <div v-show="replyList.length===0&&!selectReplayCommentFFlag" class="reply-loading">
          <img src="/img/loading.png">
        </div>
      </div>
      <el-dialog
      v-model="deleteCommentDialogFlag"
      append-to-body
      width="min(464px, calc(100vw - 32px))"
      class="message-confirm-dialog"
      :lock-scroll="false"
      align-center
    >
      <span class="delete-dialog-title">删除</span>
      <div class="delete-dialog-content">
        删除该条通知后将无法恢复，是否继续？
      </div>
      <div class="delete-dialog-btn-container">
        <button v-debounce class="delete-dialog-confirm-btn" @click="deleteCommentNotificationF">
        确定
      </button>
      <button v-debounce
        class="delete-dialog-cancel-btn"
        @click="deleteCommentDialogFlag=false"
      >
        取消
      </button>
      </div>
      </el-dialog>
    </div>
  
  </template>
  
  <script setup>
import UserHoverCard from '@/components/UserHoverCard.vue';
   import { nextTick, onMounted, reactive, ref } from 'vue'
   import {useGlobalStore} from "../store/store";
   import { ElMessage } from "element-plus";   
   import {selectReplayComment,commentControles,deleteCommentNotification}  from '../api/comment/index';
   import {putMessageNumber}  from '../api/privateMessage/index';
   import { sanitizeRichText } from "../utils/richText";
   import { commentApi, socialApi } from '../api/product';
   import { post } from '../services/http';
   import PostEditor from '@/components/PostEditor.vue';

   const store=useGlobalStore();
  const replyCommentHoverFlag = ref(null); // eslint-disable-line no-unused-vars -- 图标改用 CSS hover 后不再需要
  const loveCommentHoverFlag = ref(null); // eslint-disable-line no-unused-vars -- 同上
  const deleteCommentNotificaitonHoverFlag = ref(null); // eslint-disable-line no-unused-vars -- 同上
   const pageNum=ref(1);
   const replyList=reactive([]);
   const deleteCommentDialogFlag=ref(false);
   const deleteCommentNotificaitonId=ref(null);
   const selectReplayCommentFFlag=ref(false);
   const activeReplyId=ref(null);
   const replyInput=ref(null);
   const replyContent=ref("");
   const replyContentLength=ref(0);
   const replyImages=ref([]);
   const replyUploading=ref(false);
   const replySending=ref(false);
   const replyShareToDynamic=ref(false);
   const setReplyInput=el=>{ if(el) replyInput.value=el; };
   function signedIn(){
    if(store.token) return true;
    store.loginDialogVisible=true;
    return false;
   }
   const Scrolled=ref(true);
   const loadError=ref("");
   const loading=ref(false);
   const showError = error => ElMessage.error(error.message || "消息操作失败");

   onMounted(()=>{

    putMessageNumberF();
    setTimeout(()=>{
      selectReplayCommentF();
    },500);
   
   })

   //修改消息数量
    function putMessageNumberF(){
    putMessageNumber(store).then(res=>{
      if(res.data.code===1){
        let userInformation=store.userInformation;
        userInformation.replyCommentNumber=0;
        userInformation.allMessageNumber=userInformation.messageNumber+userInformation.replyCommentNumber+userInformation.atNumber+userInformation.likeAllNumber;
        store.setUserInformation(userInformation);
      }
    }).catch(showError)
    }

   //查询回复评论
    function selectReplayCommentF(){
      if(pageNum.value>1000)
      {
        ElMessage({
          message: "记录最多显示10000条",
          type: "warning",
          duration: 1700,
        });
        return;
      }
    if (loading.value) return;
    loading.value=true;
    loadError.value="";
    selectReplayComment(store.token,store.userId,pageNum.value).then(res=>{
      if(res.data.code===1){
        pushData(res.data.data);
        if(res.data.data===null||res.data.data.length===0)
        Scrolled.value=false;
        selectReplayCommentFFlag.value=true;
        pageNum.value++;
      }
    }).catch(error=>{
      loadError.value=error.message || "回复通知加载失败";
      selectReplayCommentFFlag.value=true;
      showError(error);
    }).finally(()=>{ loading.value=false; })
   }

   //判断是否添加图片
function addImgFlagF(comment,flag){
      if(!comment)
      return "[该评论已被删除]";
      //用户自己删除的评论
      if(comment.deleteSign===1)
      return "[该评论已被删除]";
      //被管理员下架的评论
      if(comment.status===1)
      return "[该评论已被下架]";
      if(!comment.content)
      return "[该评论已被删除]";
    let content=comment.content.replace("style='width: 24px;height: 24px;display: inline-block;","style='width: 24px;height: 24px;display: none;");
    if(flag)
    content = content.replace(/width:21px;height:21px;display:inline-block;/g, "width:16px;height:16px;display:inline-block;");
    if(comment.replyCommentId!==comment.mainCommentId && content.includes('：'))
      content=content.substring(content.indexOf('：')+1);
    const imgArr=(comment.imgAddress || "").split(',');
    for(let i=0;i<imgArr.length-1;i++){
        content=content+"[图片]";
    }
    return sanitizeRichText(content);
  }

    //点赞
    function commentControlesF(reply){

      let commentControls = {
        commentId: reply.replyComments.id,
        userId: store.userId,
        videoId: reply.replyComments.videoId,
        controls: 1,
      };
      commentControles(store.token, commentControls).then(res => {
        if(res.data.code===1){
          if(reply.replyCommentControls&&reply.replyCommentControls.controls===1)
            reply.replyCommentControls.controls=0;
          else
            {
              let replyCommentControls={
                controls:1,
              };
              reply.replyCommentControls=replyCommentControls;
              ElMessage({
              message: "点赞成功",
              type: "success",
              duration: 1700,
               });
            }
        }
      }).catch(showError);
    }

    //删除通知
    function deleteCommentNotificationF(){

      deleteCommentNotification(store.token,deleteCommentNotificaitonId.value,store.userId,store.messageMenu,0).then(res=>{
        if(res.data.code===1){
          ElMessage({
            message: "删除成功",
            type: "success",
            duration: 1700,
          });
          replyList.splice(replyList.findIndex(item=>item.replyComments.id===deleteCommentNotificaitonId.value),1);
          deleteCommentDialogFlag.value=false;
          deleteCommentNotificaitonId.value=null; 

        }else{
          ElMessage({
            message: res.data.msg,
            type: "warning",
            duration: 1700,
          });
        }
      }).catch(showError)

    }

    //回复评论
    async function replayCommentF(reply){
      if(!signedIn() || replySending.value) return;
      if(!replyContentLength.value && !replyImages.value.length){
        ElMessage.warning("请输入回复内容");
        return;
      }
      if(replyContent.value.length>1000){
        ElMessage.warning("回复内容（含表情和 @ 用户）过长，请适当缩短");
        return;
      }
      replySending.value=true;
      try{
        const content=sanitizeRichText(replyContent.value) || (replyImages.value.length?"分享图片":"");
        if(!content && !replyImages.value.length){
          ElMessage.warning("请输入回复内容");
          return;
        }
        // 后端会把楼中楼自动归位到一级评论下
        await commentApi.create({
          videoId: reply.replyComments.videoId || undefined,
          postId: reply.replyComments.dynamicId || undefined,
          content,
          imageUrls: replyImages.value,
          rootId: reply.replyComments.id,
        });
        if(replyShareToDynamic.value){
          try{
            await socialApi.createPost({
              content, imageUrls: replyImages.value,
              ...(reply.replyComments.videoId ? { videoId: reply.replyComments.videoId } : {}),
              visibility: 0,
            });
          }catch(error){
            ElMessage.warning(`回复已发送，转发失败：${error.message}`);
          }
        }
        replyContent.value="";
        replyContentLength.value=0;
        replyImages.value=[];
        replyUploading.value=false;
        replyShareToDynamic.value=false;
        activeReplyId.value=null;
        ElMessage.success("回复成功");
      }catch(error){
        ElMessage.error(error.message || "回复失败");
      }finally{
        replySending.value=false;
      }
    }

    //打开回复评论
    function openReplyComment(reply){
      if(activeReplyId.value===reply.id){
        activeReplyId.value=null;
        return;
      }
      activeReplyId.value=reply.id;
      replyContent.value="";
      replyContentLength.value=0;
      replyShareToDynamic.value=false;
      nextTick(()=>{
        replyInput.value?.focus();
      })
    }

    //上传回复图片
    async function uploadReply(event){
      if(!signedIn()){ event.target.value=""; return; }
      const files=Array.from(event.target.files);
      if(files.length + replyImages.value.length>9){
        ElMessage.warning("最多 9 张图片");
        event.target.value="";
        return;
      }
      replyUploading.value=true;
      try{
        for(const file of files){
          const body=new FormData();
          body.append("file", file);
          const image=await post("/media/image", body);
          replyImages.value.push(image.url);
        }
      }catch(error){
        ElMessage.error(error.message);
      }finally{
        replyUploading.value=false;
        event.target.value="";
      }
    }

    //滚动加载
    function handleScroll(e){
      if(Scrolled.value&&e.target.scrollHeight - e.target.scrollTop <= e.target.clientHeight + 40){
        selectReplayCommentF();
      }
    }

    //去除重复数据
    function pushData(newData) {
    const idSet = new Set(replyList.map(item => item.id)); // 获取已有数据的 ID
    const uniqueData = newData.filter(item => !idSet.has(item.id)); // 过滤掉重复的
    replyList.push(...uniqueData); // Vue 响应式更新
    }

  </script>
  
  <style lang="scss" scoped>
  *{
      margin: 0;
      padding: 0;
      box-sizing: border-box;
  }
  
  
  .reply-container{
      position: absolute;
      top: 74px;
      width: 67%;
      min-width: 983px;
      left: clamp(20.7vw, 2.4vw + 15.2%, 44vw);

      .head2{
      user-select: none;
      height: 42px;
      padding: 0 16px;
      font-size: 15px;
      background-color: white;
      color: #666;
      line-height: 42px;
      border-radius: 4px;
      box-shadow: 0 2px 4px 0 rgba(121, 146, 185, 0.54);
     }

     .reply-content{
      position: relative;
      top: 10px;
      width: 100%;
      height: 100vh;
      background-color: var(--background-color);
      border-radius: 4px;
      box-shadow: 0 2px 4px 0 rgba(121, 146, 185, 0.54);
      margin-bottom: 10px;
      padding: 24px 16px;
      overflow-y: auto;
      padding-bottom: 120px;

      .reply-item{
       position: relative;
       width: 100%;
       background-color: rgba(255, 255, 255, 0);
       padding-bottom: 20px;
       cursor: pointer;
       height: auto;
       max-height: 220.5px;
       margin-bottom: 8.5px;

       .reply-item-content{
         position: relative;
         top: 0;
         left: 0;
         width: 100%;
         background-color: rgba(255, 255, 255, 0);
         height: auto;

         .reply-user-avatar{
          position: absolute;
          user-select: none;
          display: block;
          width: 46px;
          height: 46px;
          border-radius: 50%;        
         }

         .reply-right-content{
          position: relative;
          top: 0px;
          left: 59.5px;
          width: calc(100% - 59.5px);
          height: auto;
          background-color: rgb(255, 255, 255,0);

          .reply-right-content-top{
            position: relative;
            width: calc(100% - 119.5px);
            font-size: 14px;
            line-height: 1.1;
            margin-bottom: 10px;
            overflow: hidden;
            text-overflow: ellipsis;
            word-break: break-all;
            display: -webkit-box;
            line-height: 15px;
            max-height: 4.285714285714286em;
            a{
              color: #222;
              font-weight: 600;
              transition: color 0.3s ease;
            }
            a:hover{
              color: #2BB2A0;
              transition: all 0.3s ease;
}
            span{
              color: #505050;
              margin-left: 12px;
            }
          }

          .reply-right-content-center{
            position: relative;
            top: 0.5px;
            width: calc(100% - 119.5px);
            margin-bottom: 10px;
            margin-right: 14px;
            font-size: 14px;
            color: #222;
            .reply-message-content{
              color: #1C2321;
              .reply-message-content{
              display: -webkit-box;
              -webkit-box-orient: vertical; /* 必须设置垂直排列 */
              overflow: hidden;
              text-overflow: ellipsis;
              word-break: break-all;
              -webkit-line-clamp: 2;
              line-height: 18px;
              max-height: 2.571428571428572em;
            }
            .reply-message-content2{
              display: -webkit-box;
              -webkit-box-orient: vertical; /* 必须设置垂直排列 */
              color: #999;
              border-left: 2px solid #e7e7e7;
              margin: 8px 0 10px;
              padding-left: 4px;
              overflow: hidden;
              text-overflow: ellipsis;
              word-break: break-all;
              -webkit-line-clamp: 2;
              font-size: 12px;
              line-height: 16.5px;
              max-height: 2.666666666666667em;
            }
            }
            .add-reply-user-name{
              color: #0F7E77;
              transition: color 0.3s ease;
            }
            .add-reply-user-name:hover{
              color: #2BB2A0;
              transition: all 0.3s ease;
}
            
          }

          .reply-right-content-bottom{
            width: calc(100% - 119.5px);
            user-select: none;
            position: relative;
            display: flex;
            flex-direction: row;
            justify-content: flex-start;
            align-items: center;
            color: #999;
            font-size: 12px;
            line-height: 22px;
            margin-bottom: 20px;
            .reply-time{
              position: relative;
              top: 0.5px;
              left: 0;
            }

            .reply-comment{
              position: relative;
              display: flex;
              left: 16px;
              top: -0.5px;
              font-size: 13.5px;
               img{
                 position: relative;
                 top: 5px;
                 width: 16px;
                 height: 14px;
                 margin-right: 4.5px;
               }
            }
            .reply-comment:hover{
             color: #2BB2A0;
             transition: all 0.3s ease;
}
            .love-comment{
              position: relative;
              display: flex;
              left: 30px;
              top: -0.5px;
              font-size: 13.5px;
               img{
                 position: relative;
                 top: 5.5px;
                 width: 14px;
                 height: 13px;
                 margin-right: 5.5px;
               }
            }
            .love-comment:hover{
              color: #2BB2A0;
              transition: all 0.3s ease;
}
            .love-comment2{
              position: relative;
              display: flex;
              left: 30px;
              top: -0.5px;
              color: #2BB2A0;
              font-size: 13.5px;
               img{
                 position: relative;
                 top: 5.5px;
                 width: 14px;
                 height: 13px;
                 margin-right: 5.5px;
               }
            }
            .delete-comment-notification{
              position: relative;
              display: flex;
              left: 45px;
              top: -0.5px;
              font-size: 13.5px;
              opacity: 0;
               img{
                 position: relative;
                 top: 5.5px;
                 width: 13px;
                 height: 13px;
                 margin-right: 5px;
               }
            }
            .delete-comment-notification:hover{
              color: #2BB2A0;
              transition: all 0.3s ease;
}

          }

          .reply-form{
            position: relative;
            width: 100%;
            margin: 14px 0 4px;
          }
          .comment-upload{
            position: relative;
          }
          .comment-upload > span + span{
            color: var(--ink-3);
            font-size: 11px;
          }
          .comment-upload.disabled{
            opacity: .55;
            cursor: not-allowed;
          }
          .comment-upload input{
            position: absolute;
            width: 1px;
            height: 1px;
            overflow: hidden;
            clip-path: inset(50%);
            white-space: nowrap;
          }
          .share-tool{
            cursor: pointer;
          }
          .share-tool .coin{
            display: block;
            flex: none;
            width: 16px !important;
            height: 16px !important;
            align-self: center;
            stroke-width: 4 !important;
          }
          .share-tool .coin path{
            stroke-width: 6 !important;
          }
          .attachments{
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
            margin: 12px 0;
          }
          .attachments > div{
            width: 90px;
          }
          .attachments .el-image{
            width: 90px;
            max-width: 100%;
            height: 90px;
            border-radius: 10px;
          }
          .attachments .el-button{
            margin: 0;
            font-size: 12px;
          }
          .comment-content {
            position: absolute;
            top: -8px;
            right: 0px;
            width: 60px;
            height: 50px;
            color: #999;
            cursor: pointer;
            display: -webkit-box;
            -webkit-box-orient: vertical;
            overflow: hidden; /* 必须要有 */
            text-overflow: ellipsis; /* 省略号生效 */
            -webkit-line-clamp: 3;
            word-break: break-all; /* 避免单词中间断行 */
            font-size: 14px;
            line-height: 16.5px;
          }

          .reply-video-cover-container{
            position: absolute;
            top: -8px;
            right: 0px;
            .reply-video-cover{
              width: 60px;
              height: 60px;
              object-fit: cover;
          }
          }

          .reply-line{
          display: block;
          top: 4px;
          width: 100%;
          height: 1px;
          border-bottom: 1px solid var(--line-color);
          position: relative;
          }

         }
       }
      }

      .reply-item:hover{ 
        .reply-right-content{
          .reply-right-content-bottom{
            .delete-comment-notification{
            opacity: 1;
            visibility: visible;
            }
          }
        }
        
      }

      .reply-loading{
        position: absolute;
        left: 50%;
        transform: translate(-50%,);
      }
       

     }

     .reply-content::-webkit-scrollbar{
      width: 6px; /* 滚动条的宽度 */
      border-radius: 30px; /* 滚动条滑块的圆角 */
      }

      .reply-content::-webkit-scrollbar-thumb {
      background: #aaa; /* 滚动条的滑块 */
      height: 10px;
      border-radius: 10px;
      }

      .no-reply-data{
        position: absolute;
        left: 50%;
        transform: translate(-50%);
      }
  
  }

  .delete-dialog-title{
    position: relative;
    left: 16px;
    top: -14px;
    font-size: 18px;
    color: #20AF99;
  }

  .delete-dialog-content{
    position: relative;
    top: 12px;
    left: 16px;
  }

  .delete-dialog-btn-container{
    position: relative;
    left: 92px;
    top: 40px;
}

.delete-dialog-cancel-btn{
    border: 1px solid #20AF99;
    color: #20AF99;
    border-radius: 4px;
    min-width: 104px;
    height: 32px;
    cursor: pointer;
    font-size: 14px;
    background-color: white;
    margin: 0 10px;
    transition: all 0.2s ease;
}

.delete-dialog-cancel-btn:hover{
    background-color: #20AF99;
    color: white;
    transition: all 0.3s ease;
}

.delete-dialog-confirm-btn{
    background-color: #20AF99;
    border: 1px solid #20AF99;
    color: #fff;
    border-radius: 4px;
    cursor: pointer;
    min-width: 104px;
    height: 32px;
    font-size: 14px;
    margin: 0 10px;
}

.delete-dialog-confirm-btn:hover{
    background-color: #2FB4A1;
    transition: all 0.3s ease;
}

@media (min-width: 901px){
  .reply-container{
    --message-shell-left: var(--page-pad);
    --message-aside-width: 180px;
    position: fixed;
    top: 76px;
    left: calc(var(--message-shell-left) + var(--message-aside-width) + 16px);
    right: var(--page-pad);
    width: auto;
    max-width: none;
    min-width: 0;
    z-index: 40;
  }
}

@media (max-width: 900px){
  .reply-container{
    position: fixed;
    left: 12px;
    right: 12px;
    top: 136px;
    width: auto;
    min-width: 0;
    z-index: 40;

    .head2{
      border-radius: 16px;
    }

    .reply-content{
      height: calc(100vh - 198px);
      min-height: 420px;
      border-radius: 16px;
    }
  }
}
    .user-hover { display: inline; vertical-align: middle; }
    :deep(.at-msg) { color: #0A8271; text-decoration: none; }
    :deep(.at-msg:hover) { color: var(--brand); }
</style>