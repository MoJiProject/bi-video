<template>
    <div class="reply-container">
      <div class="head2">回复我的</div>
      <div class="reply-content" @scroll="handleScroll">
        <div v-show="replyList.length>0" class="reply-item" v-for="reply in replyList" :key="reply.replyComments.id">
           <div class="reply-item-content">
              <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="reply.userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a :href="'./home?homeMenu=1&userId='+reply.userId" target="_blank"><img class="reply-user-avatar" :src="reply.avatarAddress"></a>
              </el-tooltip>
              <div class="reply-right-content">
                 <div class="reply-right-content-top">
                  <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="reply.userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a :href="'./home?homeMenu=1&userId='+reply.userId" target="_blank">{{ reply.userName }}</a>
                  </el-tooltip>
                  <span v-if="reply.mainComments&&!reply.dynamic">回复了我的评论</span>
                  <span v-else-if="!reply.mainComments&&reply.dynamic">对我的动态发表了评论</span>
                  <span v-else>对我的视频发表了评论</span>
                </div>
                <div class="reply-right-content-center">
                 <a v-if="reply.mainComments&&!reply.dynamic&&reply.replyComments.videoId&&reply.replyComments.deleteSign===0" class="reply-message-content" :href="'./video?videoId=BV'+reply.mainComments?.videoId+'&commentId='+reply.replyComments.mainCommentId+'&replyId='+reply.replyComments.id" target="videoWindow">
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
                 <a v-else-if="reply.mainComments&&!reply.dynamic&&reply.replyComments.videoId&&reply.replyComments.deleteSign===1" class="reply-message-content" :href="'./video?videoId=BV'+reply.mainComments?.videoId" target="videoWindow">
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
                 <a v-else-if="!reply.mainComments&&reply.replyComments.videoId&&!reply.dynamic" class="reply-message-content" :href="'./video?videoId=BV'+reply.replyComments?.videoId+'&commentId='+reply.replyComments?.id" target="videoWindow">
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
                    <span class="reply-comment" @click="openReplyComment(reply)" @mouseover="replyCommentHoverFlag=true" @mouseleave="replyCommentHoverFlag=false"><img :src="replyCommentHoverFlag?'../img/回复蓝.png':'../img/回复.png'"> <span>回复</span></span>
                    <span v-show="reply.replyCommentControls?.controls!==1" class="love-comment" @click="commentControlesF(reply)" @mouseover="loveCommentHoverFlag=true" @mouseleave="loveCommentHoverFlag=false"><img :src="loveCommentHoverFlag?'../img/评论点赞hover.png':'../img/评论点赞灰.png'"> <span>点赞</span></span>
                    <span v-show="reply.replyCommentControls?.controls===1" class="love-comment2" @click="commentControlesF(reply)"><img src="../img/评论点赞蓝.png"> <span>已赞</span></span>
                    <span class="delete-comment-notification" @click="deleteCommentDialogFlag=true,deleteCommentNotificaitonId=reply.replyComments.id" @mouseover="deleteCommentNotificaitonHoverFlag=true" @mouseleave="deleteCommentNotificaitonHoverFlag=false"><img :src="deleteCommentNotificaitonHoverFlag?'../img/删除通知蓝.png':'../img/删除通知.png'"> <span>删除该通知</span></span>
                </div>
                <div v-show="reply?.replyFlag" class="reply-comment-container">
                  <img src="/默认头像.gif">
                  <textarea :id="'reply'+reply.replyComments.id" class="reply-comment-input" v-model="commentContent" placeholder="请自觉遵守互联网相关的政策法规，严禁发布色情、暴力、反动的言论。"></textarea>
                  <button class="commit-comment" @click="replayCommentF(reply)">发表<br>评论</button>
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
                  <a v-if="!reply.mainComments&&(reply.dynamic||reply.replyComments.dynamicId)&&reply.dynamic.imgAddress" class="reply-video-cover-container" :href="'./dynamicDetail?dynamicId='+reply.replyComments.dynamicId+'&commentId='+(reply.replyComments?.mainCommentId||reply.replyComments?.id)+'&replyId='+reply.replyComments?.id" target="dynamicWindow"><img class="reply-video-cover" :src="reply.dynamic.imgAddress.split('?')[0]"></a>
                </el-tooltip>
                <el-tooltip
                    v-if="reply.mainComments&&reply.replyComments.videoId"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <template #content>
                        <div v-html="addImgFlagF(reply.replyComments,false)"></div>
                  </template>
                  <a v-if="reply.mainComments?.deleteSign===0&&reply.replyComments.videoId" class="comment-content" :href="'./video?videoId=BV'+reply.replyComments.videoId+'&commentId='+reply.replyComments.mainCommentId+'&replyId='+reply.replyComments.id" target="videoWindow"><span v-html="addImgFlagF(reply.mainComments,false)"></span></a>
                  <a v-else-if="reply.mainComments?.deleteSign===1&&reply.replyComments.videoId" class="comment-content" :href="'./video?videoId=BV'+reply.replyComments.videoId" target="videoWindow"><span v-html="addImgFlagF(reply.mainComments,false)"></span></a>
                </el-tooltip>
                <div class="reply-line"></div>
              </div>
           </div>
        </div>
        <div v-show="replyList.length===0&&selectReplayCommentFFlag" class="no-reply-data">
          <img src="../img/没有消息数据.png">
        </div>
        <div v-show="replyList.length===0&&!selectReplayCommentFFlag" class="reply-loading">
          <img src="../img/加载消息.gif">
        </div>
      </div>
      <el-dialog
      v-model="deleteCommentDialogFlag"
      append-to-body
      width="464"
      :lock-scroll="false"
      align-center
      style="height: 169.8px; border-radius: 12px"
    >
      <span class="delete-dialog-title">删除</span>
      <div class="delete-dialog-content">
        删除该条通知后将无法恢复，是否继续？
      </div>
      <div class="delete-dialog-btn-container">
        <button class="delete-dialog-confirm-btn" @click="deleteCommentNotificationF">
        确定
      </button>
      <button
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
   import { nextTick, onMounted, reactive, ref } from 'vue'
   import {useGlobalStore} from "../store/store";
   import { ElMessage } from "element-plus";   
   import {selectReplayComment,commentControles,deleteCommentNotification,replayComment}  from '../api/comment/index';
   import {putMessageNumber}  from '../api/privateMessage/index';

   const store=useGlobalStore();
   const replyCommentHoverFlag = ref(false);
   const loveCommentHoverFlag = ref(false);
   const deleteCommentNotificaitonHoverFlag = ref(false);
   const pageNum=ref(1);
   const replyList=reactive([]);
   const deleteCommentDialogFlag=ref(false);
   const deleteCommentNotificaitonId=ref(null);
   const selectReplayCommentFFlag=ref(false);
   const commentContent=ref("");
   const Scrolled=ref(true);

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
    })
    }

   //查询回复评论
    function selectReplayCommentF(){
      if(pageNum.value>1000)
      {
        ElMessage({
          message: "记录最多显示10000条",
          type: "info",
          plain: true,
          duration: 1700,
        });
        return;
      }
    selectReplayComment(store.token,store.userId,pageNum.value++).then(res=>{
      if(res.data.code===1){
        console.log(res.data.data);
        pushData(res.data.data);
        if(res.data.data===null||res.data.data.length===0)
        Scrolled.value=false;
        selectReplayCommentFFlag.value=true;
      }
    })
   }

   //判断是否添加图片
  function addImgFlagF(comment,flag){
    if(comment.deleteSign===1||!comment.content)
    return "[该评论已被删除]";
    let content=comment.content.replace("style='width: 24px;height: 24px;display: inline-block;","style='width: 24px;height: 24px;display: none;");
    if(flag)
    content = content.replace(/width:21px;height:21px;display:inline-block;/g, "width:16px;height:16px;display:inline-block;");
    if(comment.replyCommentId!==comment.mainCommentId)
      content=content.split('：')[1];
    const imgArr=comment.imgAddress.split(',');
    for(let i=0;i<imgArr.length-1;i++){
        content=content+"[图片]";
    }
    return content;
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
              type: "info",
              plain: true,
              duration: 1700,
               });
            }
        }
      });



    }

    //删除通知
    function deleteCommentNotificationF(){

      deleteCommentNotification(store.token,deleteCommentNotificaitonId.value,store.userId,store.messageMenu,0).then(res=>{
        if(res.data.code===1){
          ElMessage({
            message: "删除成功",
            type: "info",
            plain: true,
            duration: 1700,
          });
          replyList.splice(replyList.findIndex(item=>item.replyComments.id===deleteCommentNotificaitonId.value),1);
          deleteCommentDialogFlag.value=false;
          deleteCommentNotificaitonId.value=null; 

        }else{
          ElMessage({
            message: res.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
          });
        }
      })

    }

    //回复评论
    function replayCommentF(reply){

      commentContent.value=`&nbsp;回复 <a href='https://baidu.com' data-eit-userid='${reply.replyComments.userId}' class='at-msg' style='color:#008ac5;'>@${reply.replyComments.userName}</a> ：`+commentContent.value;
      if(reply.isAuthorFlag===1)
      commentContent.value = "<img style='width: 24px;height: 24px;display: inline-block;vertical-align: middle;transform: translateY(-1.5px);user-select: none;' src='../img/up_pb.svg'>&nbsp;"+commentContent.value;
     
      let comments={
        content: commentContent.value,
        imgAddress: null,
        userId: store.userId,
        videoId: reply.replyComments.videoId,
        dynamicId: reply.replyComments.dynamicId,
        userName: store.userInformation.userName,
        dynamicFlag: 0,
        replyCommentId: reply.replyComments.id,
        mainCommentId: !reply.replyComments.mainCommentId?reply.replyComments.id:reply.replyComments.mainCommentId,
        replyUserId: reply.replyComments.userId,
      }

      let addComment={
        comments,
        ats:[],
      }

      replayComment(store.token,addComment).then(res=>{

        if(res.data.code===1){
          ElMessage({
            message: "评论成功",
            type: "info",
            plain: true,
            duration: 1700,
          });
          commentContent.value="";
          reply.replyFlag=false;
        }
      })
    }

    //打开回复评论
    function openReplyComment(reply){

      reply.replyFlag=!reply.replyFlag;
      if(reply.replyFlag){
        nextTick(()=>{
          document.getElementById("reply"+reply.replyComments.id).focus();
        })
      }else{
        commentContent.value="";
      }
    }

    //滚动加载
    function handleScroll(e){
      if(Scrolled.value&&e.target.scrollHeight - e.target.scrollTop <= e.target.clientHeight + 1){
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
              color: #2faee3;
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
              color: #18191C;
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
              color: #1389bf;
              transition: color 0.3s ease;
            }
            .add-reply-user-name:hover{
              color: #2faee3;
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
                 top: 5.5px;
                 width: 14px;
                 height: 13px;
                 margin-right: 4.5px;
               }
            }
            .reply-comment:hover{
             color: #2faee3;
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
              color: #2faee3;
            }
            .love-comment2{
              position: relative;
              display: flex;
              left: 30px;
              top: -0.5px;
              color: #2faee3;
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
              color: #2faee3;
            }

          }

          .reply-comment-container{
            position: relative;
            height: 66.5px;
            width: 100%;
            margin-bottom: 20px;

            img{
            position: absolute;
            width: 46px;
            height: 46px;
            border-radius: 50%;
            left: -60px;
            cursor: pointer;
            }
            .reply-comment-input{
              position: relative;
              height: 65px;
              width: 790px;
              resize: none;
              background-color: rgba(255, 255, 255, 0.5);
              border: 1px solid #e9eaec;
              overflow: auto;
              border-radius: 4px;
              word-break: break-all;
              color: #555;
              padding: 5px 10px;
              font-size: 12px;
              vertical-align: top; /* 确保文本从顶部开始 */
              outline: none;
              margin: 0px;
            }
            .reply-comment-input:focus{
              border-color: #2faee3;
            }
            .commit-comment{
              position: absolute;
              right: 15px;
              top: 0;
              width: 70px;
              height: 64px;
              display: flex;
              -webkit-box-pack: center;
              -ms-flex-pack: center;
              justify-content: center;
              -webkit-box-align: center;
              -ms-flex-align: center;
              align-items: center;
              background-color: #00a1d6;
              border-radius: 4px;
              color: #fff;
              border: none;
              cursor: pointer;
            }
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
          width: 102%;
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
    color: #23ade5;
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
    border: 1px solid #23ade5;
    color: #23ade5;
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
    background-color: #23ade5;
    color: white;
}

.delete-dialog-confirm-btn{
    background-color: #23ade5;
    border: 1px solid #23ade5;
    color: #fff;
    border-radius: 4px;
    cursor: pointer;
    min-width: 104px;
    height: 32px;
    font-size: 14px;
    margin: 0 10px;
}

.delete-dialog-confirm-btn:hover{
    background-color: #39b5e7;
}
  </style>