<template>
    <div class="at-container" v-mention-hover>
      <div class="head2">@ 我的</div>
      <div class="at-content" @scroll="handleScroll">
        <div v-show="atList.length>0" class="at-item" v-for="at in atList" :key="at.at.id">
           <div class="at-item-content">
              <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="at.userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <UserHoverCard :user-id="Number(at.userId) || 0" :avatar="at.avatarAddress || ''" :nickname="at.userName || ''" :avatar-size="32">
                     <template #reference>
                       <a :href="'./home?homeMenu=1&userId='+at.userId" target="_blank"><img class="at-user-avatar" :src="at.avatarAddress"></a>
                     </template>
                   </UserHoverCard>
              </el-tooltip>
              <div v-if="at.comments" class="at-right-content">
                 <div class="at-right-content-top">
                  <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="at.userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <UserHoverCard :user-id="Number(at.userId) || 0" :avatar="at.avatarAddress || ''" :nickname="at.userName || ''" :avatar-size="14">
                     <template #reference>
                       <a :href="'./home?homeMenu=1&userId='+at.userId" target="_blank">{{ at.userName }}</a>
                     </template>
                   </UserHoverCard>
                  </el-tooltip>
                  <span>在评论中@了我</span>
                </div>
                <a v-if="(at.comments.deleteSign===0&&at.comments.status===0)&&!at.comments.dynamicId" :href="'./video?videoId=BV'+at.at.videoId+'&commentId='+`${at.comments.mainCommentId?at.comments.mainCommentId:at.comments.id}`+'&replyId='+at.comments.id" target="videoWindow">
                  <div class="at-right-content-center">
                    <div class="at-message-content">
                      <span v-html="addImgFlagF(at.comments)"></span> 
                    </div>
                  </div>
                </a>
                <a v-else-if="(at.comments.deleteSign===1||at.comments.status===1)&&!at.comments.dynamicId" :href="'./video?videoId=BV'+at.at.videoId" target="videoWindow">
                  <div class="at-right-content-center">
                    <div class="at-message-content">
                      <span v-html="addImgFlagF(at.comments)"></span> 
                    </div>
                  </div>
                </a>
                <a v-else-if="(at.comments.deleteSign===0&&at.comments.status===0)&&at.comments.dynamicId" :href="'./dynamicDetail?dynamicId='+at.comments.dynamicId+'&commentId='+`${at.comments.mainCommentId?at.comments.mainCommentId:at.comments.id}`+'&replyId='+at.comments.id" target="dynamicWindow">
                  <div class="at-right-content-center">
                    <div class="at-message-content">
                      <span v-html="addImgFlagF(at.comments)"></span> 
                    </div>
                  </div>
                </a>
                <a v-else-if="(at.comments.deleteSign===1||at.comments.status===1)&&at.comments.dynamicId" :href="'./dynamicDetail?dynamicId='+at.comments.dynamicId" target="dynamicWindow">
                  <div class="at-right-content-center">
                    <div class="at-message-content">
                      <span v-html="addImgFlagF(at.comments)"></span> 
                    </div>
                  </div>
                </a>
                <div class="at-right-content-bottom">
                    <span class="at-time">{{ at.comments.commentTime }}</span>
                    <button type="button" class="at-comment" @click="openReplyComment(at)"><svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M7 18 3 21V6a3 3 0 0 1 3-3h12a3 3 0 0 1 3 3v9a3 3 0 0 1-3 3H7Z"/><path d="M7 8h10M7 12h7"/></svg> <span>回复</span></button>
                    <button type="button" v-show="at.commentControls?.controls!==1" class="love-comment" v-debounce @click="commentControlesF(at)"><svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/></svg> <span>点赞</span></button>
                    <button type="button" v-show="at.commentControls?.controls===1" class="love-comment2" v-debounce @click="commentControlesF(at)"><svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/></svg> <span>已赞</span></button>
                    <button type="button" class="delete-comment-notification" @click="deleteCommentDialogFlag=true,deleteCommentNotificaitonId=at.at.id"><svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/><path d="M10 11v6"/><path d="M14 11v6"/></svg> <span>删除</span></button>
                </div>
                <div v-if="activeReplyId===at.at.id" class="reply-form">
                  <PostEditor :ref="setReplyInput" v-model="replyContent" compact :max-length="1000" :disabled="replySending"
                    :placeholder="`回复 @${at.userName}`" @length="replyContentLength = $event">
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
                      <el-button type="primary" :loading="replySending" @click="replayCommentF(at)">回复</el-button>
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
                    v-if="at.comments.mainCommentId!==null && at.mainComments"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <template #content>
                      <div v-if="at.comments.mainCommentId!==null" v-html="addImgFlagF(at.mainComments)"></div>
                  </template>
                  <a v-if="at.comments.mainCommentId!==null&&(at.comments.deleteSign===0&&at.comments.status===0)&&!at.comments.dynamicId" class="comment-content" :href="'./video?videoId=BV'+at.at.videoId+'&commentId='+`${at.comments.mainCommentId?at.comments.mainCommentId:at.comments.id}`+'&replyId='+at.mainComments.id" target="videoWindow"><span v-html="addImgFlagF(at.mainComments)"></span></a>
                  <a v-else-if="at.comments.mainCommentId!==null&&(at.comments.deleteSign===1||at.comments.status===1)&&!at.comments.dynamicId" class="comment-content" :href="'./video?videoId=BV'+at.at.videoId" target="videoWindow"><span v-html="addImgFlagF(at.mainComments)"></span></a>
                  <a v-else-if="at.comments.mainCommentId!==null&&(at.comments.deleteSign===0&&at.comments.status===0)&&at.comments.dynamicId" class="comment-content" :href="'./dynamicDetail?dynamicId='+at.comments.dynamicId+'&commentId='+`${at.comments.mainCommentId?at.comments.mainCommentId:at.comments.id}`+'&replyId='+at.mainComments.id" target="dynamicWindow"><span v-html="addImgFlagF(at.mainComments)"></span></a>
                  <a v-else-if="at.comments.mainCommentId!==null&&(at.comments.deleteSign===1||at.comments.status===1)&&at.comments.dynamicId" class="comment-content" :href="'./dynamicDetail?dynamicId='+at.comments.dynamicId" target="dynamicWindow"><span v-html="addImgFlagF(at.mainComments)"></span></a>
                </el-tooltip>
                <el-tooltip
                    v-if="at.comments.mainCommentId===null&&!at.comments.dynamicId"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="at.videos.title"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a v-if="at.comments.mainCommentId===null&&(at.comments.deleteSign===0&&at.comments.status===0)&&!at.comments.dynamicId" class="at-video-cover-container" :href="'./video?videoId=BV'+at.at.videoId" target="videoWindow"><img class="at-video-cover" :src="at.videos.coverAddress"></a>
                  <a v-else-if="at.comments.mainCommentId===null&&(at.comments.deleteSign===1||at.comments.status===1)&&!at.comments.dynamicId" class="at-video-cover-container" :href="'./video?videoId=BV'+at.at.videoId" target="videoWindow"><img class="at-video-cover" :src="at.videos.coverAddress"></a>
                </el-tooltip>
                <el-tooltip
                    v-if="at.comments.mainCommentId===null&&at.comments.dynamicId&&at.dynamic.imgAddress"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                    <template #content>
                      <div v-html="sanitizeRichText(at.dynamic?.content || at.dynamic?.title || '')"></div>
                    </template>
                  <a v-if="at.comments.mainCommentId===null&&(at.comments.deleteSign===0&&at.comments.status===0)&&at.comments.dynamicId&&at.dynamic.imgAddress" class="at-video-cover-container" :href="'./dynamicDetail?dynamicId='+at.comments.dynamicId" target="dynamicWindow"><img class="at-video-cover" :src="at.dynamic.imgAddress.split(',')[0]"></a>
                  <a v-else-if="at.comments.mainCommentId===null&&(at.comments.deleteSign===1||at.comments.status===1)&&at.comments.dynamicId&&at.dynamic.imgAddress" class="at-video-cover-container" :href="'./dynamicDetail?dynamicId='+at.comments.dynamicId" target="dynamicWindow"><img class="at-video-cover" :src="at.dynamic.imgAddress.split(',')[0]"></a>
                </el-tooltip>
                <div class="at-line"></div>
              </div>
              <div v-else-if="at.at.videoId" class="at-right-content">
                <div class="at-right-content-top">
                  <UserHoverCard :user-id="Number(at.userId) || 0" :avatar="at.avatarAddress || ''" :nickname="at.userName || ''" :avatar-size="14">
                     <template #reference>
                       <a :href="'./home?homeMenu=1&userId='+at.userId" target="_blank">{{ at.userName }}</a>
                     </template>
                   </UserHoverCard>
                  <span>在视频中@了我</span>
                </div>
                <a :href="'./video?videoId=BV'+at.at.videoId" target="videoWindow">
                  <div class="at-right-content-center">{{ at.videos.title }}</div>
                </a>
              <div class="at-right-content-bottom">
                <button type="button" class="at-comment" @click="openReplyComment(at)"><svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M7 18 3 21V6a3 3 0 0 1 3-3h12a3 3 0 0 1 3 3v9a3 3 0 0 1-3 3H7Z"/><path d="M7 8h10M7 12h7"/></svg> <span>回复</span></button>
                <button type="button" class="delete-comment-notification" @click="deleteCommentDialogFlag=true,deleteCommentNotificaitonId=at.at.id"><svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/><path d="M10 11v6"/><path d="M14 11v6"/></svg> <span>删除</span></button>
              </div>
              <div v-if="activeReplyId===at.at.id" class="reply-form">
                <PostEditor :ref="setReplyInput" v-model="replyContent" compact :max-length="1000" :disabled="replySending"
                  :placeholder="`回复 @${at.userName}`" @length="replyContentLength = $event">
                  <template #actions>
                    <el-button type="primary" :loading="replySending" @click="replayCommentF(at)">回复</el-button>
                  </template>
                </PostEditor>
              </div>
              </div>
              <div v-else class="at-right-content">
                 <div class="at-right-content-top">
                  <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="at.userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <UserHoverCard :user-id="Number(at.userId) || 0" :avatar="at.avatarAddress || ''" :nickname="at.userName || ''" :avatar-size="14">
                     <template #reference>
                       <a :href="'./home?homeMenu=1&userId='+at.userId" target="_blank">{{ at.userName }}</a>
                     </template>
                   </UserHoverCard>
                  </el-tooltip>
                  <span>在动态中@了我</span>
                </div>
                <a :href="'./dynamicDetail?dynamicId='+at.at.dynamicId" target="dynamicWindow">
                  <div class="at-right-content-center">
                    <div class="at-message-content">
                      <span v-html="at.dynamic?.content || (at.dynamic?.status===1 ? '该动态已被下架' : '该动态已被删除')"></span> 
                    </div>
                  </div>
                </a>
                <div class="at-right-content-bottom">
                  <span class="at-time">{{ at.dynamic?.publishTime }}</span>
                  <button type="button" class="at-comment" @click="openReplyComment(at)"><svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M7 18 3 21V6a3 3 0 0 1 3-3h12a3 3 0 0 1 3 3v9a3 3 0 0 1-3 3H7Z"/><path d="M7 8h10M7 12h7"/></svg> <span>回复</span></button>
                  <button type="button" class="delete-comment-notification" @click="deleteCommentDialogFlag=true,deleteCommentNotificaitonId=at.at.id"><svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/><path d="M10 11v6"/><path d="M14 11v6"/></svg> <span>删除</span></button>
              </div>
                 <el-tooltip
                    v-if="at.dynamic?.imgAddress"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                    <template #content>
                      <div v-html="sanitizeRichText(at.dynamic?.content || at.dynamic?.title || '')"></div>
                    </template>
<a v-if="at.dynamic?.imgAddress" class="at-video-cover-container" :href="'./dynamicDetail?dynamicId='+at.at.dynamicId" target="dynamicWindow"><img class="at-video-cover" :src="at.dynamic?.imgAddress.split(',')[0]"></a>
</el-tooltip>
<div v-if="activeReplyId===at.at.id" class="reply-form">
  <PostEditor :ref="setReplyInput" v-model="replyContent" compact :max-length="1000" :disabled="replySending"
    :placeholder="`回复 @${at.userName}`" @length="replyContentLength = $event">
    <template #actions>
      <el-button type="primary" :loading="replySending" @click="replayCommentF(at)">回复</el-button>
    </template>
  </PostEditor>
</div>
<div class="at-line"></div>
              </div>
           </div>
        </div>
        <div v-show="atList.length===0&&selectAtCommentFFlag" class="no-at-data">
          <AppEmptyState :title="loadError || '暂无提及通知'" :kind="loadError ? 'unavailable' : undefined" :action-text="loadError ? '重新加载' : undefined" compact @action="selectAtCommentF"/>
        </div>
        <div v-show="atList.length===0&&!selectAtCommentFFlag" class="at-loading">
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
  import {selectAtComment,commentControles,deleteCommentNotification}  from '../api/comment/index';
  import {putMessageNumber}  from '../api/privateMessage/index';
  import { sanitizeRichText } from "../utils/richText";
  import { commentApi, socialApi } from '../api/product';
  import { post } from '../services/http';
  import PostEditor from '@/components/PostEditor.vue';


  const store = useGlobalStore();
  const atList = reactive([]);
  const pageNum = ref(1);
  const replyCommentHoverFlag = ref(null); // eslint-disable-line no-unused-vars -- 图标改用 CSS hover 后不再需要
  const loveCommentHoverFlag = ref(null); // eslint-disable-line no-unused-vars -- 同上
  const deleteCommentNotificaitonHoverFlag = ref(null); // eslint-disable-line no-unused-vars -- 同上
  const selectAtCommentFFlag = ref(false);
  const deleteCommentDialogFlag=ref(false);
  const deleteCommentNotificaitonId=ref(null);
  const activeReplyId=ref(null);
  const replyInput=ref(null);
  const replyContent=ref("");
  const replyContentLength=ref(0);
  const replyImages=ref([]);
  const replyUploading=ref(false);
  const replySending=ref(false);
  const replyShareToDynamic=ref(false);
  const setReplyInput=el=>{
    if(el) replyInput.value=el;
  };
  const signedIn=()=>{
    if(store.token) return true;
    store.loginDialogVisible=true;
    return false;
  };
  const Scrolled=ref(true);
  const loading=ref(false);
  const loadError=ref("");
  const showError = error => ElMessage.error(error.message || "消息操作失败");

  onMounted(() => {
    putMessageNumberF();
    setTimeout(() => {
      selectAtCommentF();
    }, 500);

  });

  //修改消息数量
  function putMessageNumberF(){
    putMessageNumber(store).then(res=>{
      if(res.data.code===1){
        let userInformation=store.userInformation;
        userInformation.atNumber=0;
        userInformation.allMessageNumber=userInformation.messageNumber+userInformation.replyCommentNumber+userInformation.atNumber+userInformation.likeAllNumber;
        store.setUserInformation(userInformation);
      }
    }).catch(showError)
  }

  //获取@我的评论
  function selectAtCommentF(){

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
    selectAtComment(store.token,store.userId,pageNum.value).then(res => {
      if(res.data.code === 1)
       { 
        pushData(res.data.data);
        selectAtCommentFFlag.value = true;
        pageNum.value++;
        if(res.data.data===null||res.data.data.length===0)
        Scrolled.value=false;
       }
    }).catch(error=>{
      loadError.value=error.message || "提及通知加载失败";
      selectAtCommentFFlag.value=true;
      showError(error);
    }).finally(()=>{ loading.value=false; })
  }

  //去除重复数据
  function pushData(newData) {
    if(newData===null)
      return;
    const idSet = new Set(atList.map(item => item.at.id)); // 获取已有数据的 ID
    const uniqueData = newData.filter(item => !idSet.has(item.at.id)); // 过滤掉重复的
    atList.push(...uniqueData); // Vue 响应式更新
  }

  //判断是否添加图片
function addImgFlagF(comment){
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
  
      if(comment.replyCommentId!==comment.mainCommentId)
       content=content.replace("&nbsp;&nbsp;","");
      const imgArr=(comment.imgAddress||'').split(',');
      for(let i=0;i<imgArr.length-1;i++){
          content=content+"[图片]";
      }
      return sanitizeRichText(content);
  }

  //点赞
  function commentControlesF(at){

      let commentControls = {
        commentId: at.comments.id,
        userId: store.userId,
        videoId: at.comments.videoId,
        controls: 1,
      };
      commentControles(store.token, commentControls).then(res => {
        if(res.data.code===1){
          if(at.commentControls&&at.commentControls.controls===1)
            at.commentControls.controls=0;
          else
            {
              let commentControls={
                controls:1,
              };
              at.commentControls=commentControls;
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
          atList.splice(atList.findIndex(item=>item.at.id===deleteCommentNotificaitonId.value),1);
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
  async function replayCommentF(at){
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
const comment = at.comments || {};
const videoId = comment.videoId || at.at?.videoId || undefined;
const postId = comment.dynamicId || at.at?.dynamicId || undefined;
const rootId = comment.id || undefined;
// 后端会把楼中楼自动归位到一级评论下
await commentApi.create({
  videoId,
  postId,
  content,
  imageUrls: replyImages.value,
  rootId,
});
      if(replyShareToDynamic.value){
        try{
          await socialApi.createPost({
            content, imageUrls: replyImages.value,
            ...(at.comments.videoId ? { videoId: at.comments.videoId } : {}),
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
  function openReplyComment(at){
    if(activeReplyId.value===at.at.id){
      activeReplyId.value=null;
      return;
    }
    activeReplyId.value=at.at.id;
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
        selectAtCommentF();
      }
  }



  </script>
  
  <style lang="scss" scoped>
  *{
      margin: 0;
      padding: 0;
      box-sizing: border-box;
  }
  
  .at-container{
      position: absolute;
      width: 67%;
      min-width: 983px;
      left: clamp(20.7vw, 2.4vw + 15.2%, 44vw);
      top: 74px;
      
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

      .at-content{
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

      .at-item{
       position: relative;
       width: 100%;
       background-color: rgba(255, 255, 255, 0);
       padding-bottom: 20px;
       cursor: pointer;
       height: auto;
       max-height: 220.5px;
       margin-bottom: 8.5px;

       .at-item-content{
         position: relative;
         top: 0;
         left: 0;
         width: 100%;
         background-color: rgba(255, 255, 255, 0);
         height: auto;

         .at-user-avatar{
          position: absolute;
          user-select: none;
          display: block;
          width: 46px;
          height: 46px;
          border-radius: 50%;        
         }

         .at-right-content{
          position: relative;
          top: 0px;
          left: 59.5px;
          width: calc(100% - 59.5px);
          height: auto;
          background-color: rgb(255, 255, 255,0);

          .at-right-content-top{
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

          .at-right-content-center{
            position: relative;
            top: 0.5px;
            width: calc(100% - 119.5px);
            margin-bottom: 10px;
            margin-right: 14px;
            font-size: 14px;
            color: #222;
            
            a{
              color: #0F7E77;
              transition: color 0.3s ease;
            }
            a:hover{
              color: #2BB2A0;
              transition: all 0.3s ease;
}
            .at-message-content{
              display: -webkit-box;
              -webkit-box-orient: vertical; /* 必须设置垂直排列 */
              overflow: hidden;
              text-overflow: ellipsis;
              word-break: break-all;
              -webkit-line-clamp: 2;
              line-height: 18px;
              max-height: 2.571428571428572em;
            }
  
          }

          .at-right-content-bottom{
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
            .at-time{
              position: relative;
              top: 0.5px;
              left: 0;
            }

            .at-comment{
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
            .at-comment:hover{
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

          .at-video-cover-container{
            position: absolute;
            top: -8px;
            right: 0px;
            .at-video-cover{
              width: 60px;
              height: 60px;
              object-fit: cover;
          }
          }

          .at-line{
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

      .at-item:hover{ 
        .at-right-content{
          .at-right-content-bottom{
            .delete-comment-notification{
            opacity: 1;
            visibility: visible;
            }
          }
        }
        
      }

      .at-loading{
        position: absolute;
        left: 50%;
        transform: translate(-50%,);
      }

      .no-at-data{
        position: absolute;
        left: 50%;
        transform: translate(-50%);
      }
       

     }

     .at-content::-webkit-scrollbar{
      width: 6px; /* 滚动条的宽度 */
      border-radius: 30px; /* 滚动条滑块的圆角 */
      }

      .at-content::-webkit-scrollbar-thumb {
      background: #aaa; /* 滚动条的滑块 */
      height: 10px;
      border-radius: 10px;
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
    cursor: pointer;
    height: 32px;
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
    .at-container{
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
    .at-container{
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

      .at-content{
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