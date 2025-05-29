<template>
    <div class="at-container">
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
                  <a :href="'./home?homeMenu=1&userId='+at.userId" target="_blank"><img class="at-user-avatar" :src="at.avatarAddress"></a>
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
                  <a :href="'./home?homeMenu=1&userId='+at.userId" target="_blank">{{ at.userName }}</a>
                  </el-tooltip>
                  <span>在评论中@了我</span>
                </div>
                <a v-if="at.comments.deleteSign===0&&!at.comments.dynamicId" :href="'./video?videoId=BV'+at.at.videoId+'&commentId='+`${at.comments.mainCommentId?at.comments.mainCommentId:at.comments.id}`+'&replyId='+at.comments.id" target="videoWindow">
                  <div class="at-right-content-center">
                    <div class="at-message-content">
                      <span v-html="addImgFlagF(at.comments)"></span> 
                    </div>
                  </div>
                </a>
                <a v-else-if="at.comments.deleteSign===1&&!at.comments.dynamicId" :href="'./video?videoId=BV'+at.at.videoId" target="videoWindow">
                  <div class="at-right-content-center">
                    <div class="at-message-content">
                      <span v-html="addImgFlagF(at.comments)"></span> 
                    </div>
                  </div>
                </a>
                <a v-else-if="at.comments.deleteSign===0&&at.comments.dynamicId" :href="'./dynamicDetail?dynamicId='+at.comments.dynamicId+'&commentId='+`${at.comments.mainCommentId?at.comments.mainCommentId:at.comments.id}`+'&replyId='+at.comments.id" target="dynamicWindow">
                  <div class="at-right-content-center">
                    <div class="at-message-content">
                      <span v-html="addImgFlagF(at.comments)"></span> 
                    </div>
                  </div>
                </a>
                <a v-else-if="at.comments.deleteSign===1&&at.comments.dynamicId" :href="'./dynamicDetail?dynamicId='+at.comments.dynamicId" target="dynamicWindow">
                  <div class="at-right-content-center">
                    <div class="at-message-content">
                      <span v-html="addImgFlagF(at.comments)"></span> 
                    </div>
                  </div>
                </a>
                <div class="at-right-content-bottom">
                    <span class="at-time">{{ at.comments.commentTime }}</span>
                    <span class="at-comment" @click="openReplyComment(at)" @mouseover="replyCommentHoverFlag=true" @mouseleave="replyCommentHoverFlag=false"><img :src="replyCommentHoverFlag?'../img/回复蓝.png':'../img/回复.png'"> <span>回复</span></span>
                    <span v-show="at.commentControls?.controls!==1" class="love-comment" @click="commentControlesF(at)" @mouseover="loveCommentHoverFlag=true" @mouseleave="loveCommentHoverFlag=false"><img :src="loveCommentHoverFlag?'../img/评论点赞hover.png':'../img/评论点赞灰.png'"> <span>点赞</span></span>
                    <span v-show="at.commentControls?.controls===1" class="love-comment2" @click="commentControlesF(at)"><img src="../img/评论点赞蓝.png"> <span>已赞</span></span>
                    <span class="delete-comment-notification" @click="deleteCommentDialogFlag=true,deleteCommentNotificaitonId=at.at.id" @mouseover="deleteCommentNotificaitonHoverFlag=true" @mouseleave="deleteCommentNotificaitonHoverFlag=false"><img :src="deleteCommentNotificaitonHoverFlag?'../img/删除通知蓝.png':'../img/删除通知.png'"> <span>删除该通知</span></span>
                </div>
                <div v-show="at?.replyFlag" class="at-comment-container">
                  <img src="/默认头像.gif">
                  <textarea :id="'at'+at.at.id" class="at-comment-input" v-model="commentContent" placeholder="请自觉遵守互联网相关的政策法规，严禁发布色情、暴力、反动的言论。"></textarea>
                  <button class="commit-comment" @click="replayCommentF(at)">发表<br>评论</button>
                </div>
                <el-tooltip
                    v-if="at.comments.mainCommentId!==null"
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
                  <a v-if="at.comments.mainCommentId!==null&&at.comments.deleteSign===0&&!at.comments.dynamicId" class="comment-content" :href="'./video?videoId=BV'+at.at.videoId+'&commentId='+`${at.comments.mainCommentId?at.comments.mainCommentId:at.comments.id}`+'&replyId='+at.comments.id" target="videoWindow"><span v-html="addImgFlagF(at.mainComments)"></span></a>
                  <a v-else-if="at.comments.mainCommentId!==null&&at.comments.deleteSign===1&&!at.comments.dynamicId" class="comment-content" :href="'./video?videoId=BV'+at.at.videoId" target="videoWindow"><span v-html="addImgFlagF(at.mainComments)"></span></a>
                  <a v-else-if="at.comments.mainCommentId!==null&&at.comments.deleteSign===0&&at.comments.dynamicId" class="comment-content" :href="'./dynamic?dynamicId='+at.comments.dynamicId+'&commentId='+`${at.comments.mainCommentId?at.comments.mainCommentId:at.comments.id}`+'&replyId='+at.comments.id" target="dynamicWindow"><span v-html="addImgFlagF(at.mainComments)"></span></a>
                  <a v-else-if="at.comments.mainCommentId!==null&&at.comments.deleteSign===1&&at.comments.dynamicId" class="comment-content" :href="'./dynamic?dynamicId='+at.comments.dynamicId" target="dynamicWindow"><span v-html="addImgFlagF(at.mainComments)"></span></a>
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
                  <a v-if="at.comments.mainCommentId===null&&at.comments.deleteSign===0&&!at.comments.dynamicId" class="at-video-cover-container" :href="'./video?videoId=BV'+at.at.videoId+'&commentId='+`${at.comments.mainCommentId?at.comments.mainCommentId:at.comments.id}`+'&replyId='+at.comments.id" target="videoWindow"><img class="at-video-cover" :src="at.videos.coverAddress"></a>
                  <a v-else-if="at.comments.mainCommentId===null&&at.comments.deleteSign===1&&!at.comments.dynamicId" class="at-video-cover-container" :href="'./video?videoId=BV'+at.at.videoId" target="videoWindow"><img class="at-video-cover" :src="at.videos.coverAddress"></a>
                </el-tooltip>
                <el-tooltip
                    v-if="at.comments.mainCommentId===null&&at.comments.dynamicId&&at.dynamic.imgAddress"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="at.dynamic.title"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a v-if="at.comments.mainCommentId===null&&at.comments.deleteSign===0&&at.comments.dynamicId&&at.dynamic.imgAddress" class="at-video-cover-container" :href="'./dynamicDetail?dynamicId='+at.comments.dynamicId+'&commentId='+`${at.comments.mainCommentId?at.comments.mainCommentId:at.comments.id}`+'&replyId='+at.comments.id" target="dynamicWindow"><img class="at-video-cover" :src="at.dynamic.imgAddress.split(',')[0]"></a>
                  <a v-else-if="at.comments.mainCommentId===null&&at.comments.deleteSign===1&&at.comments.dynamicId&&at.dynamic.imgAddress" class="at-video-cover-container" :href="'./dynamicDetail?dynamicId='+at.comments.dynamicId" target="dynamicWindow"><img class="at-video-cover" :src="at.dynamic.imgAddress.split(',')[0]"></a>
                </el-tooltip>
                <div class="at-line"></div>
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
                  <a :href="'./home?homeMenu=1&userId='+at.userId" target="_blank">{{ at.userName }}</a>
                  </el-tooltip>
                  <span>在动态中@了我</span>
                </div>
                <a :href="'./dynamicDetail?dynamicId='+at.at.dynamicId" target="dynamicWindow">
                  <div class="at-right-content-center">
                    <div class="at-message-content">
                      <span v-html="at.dynamic?.content||'该评论已被删除'"></span> 
                    </div>
                  </div>
                </a>
                <div class="at-right-content-bottom">
                    <span class="at-time">{{ at.dynamic?.publishTime }}</span>
                    <span class="delete-comment-notification" @click="deleteCommentDialogFlag=true,deleteCommentNotificaitonId=at.at.id" @mouseover="deleteCommentNotificaitonHoverFlag=true" @mouseleave="deleteCommentNotificaitonHoverFlag=false"><img :src="deleteCommentNotificaitonHoverFlag?'../img/删除通知蓝.png':'../img/删除通知.png'"> <span>删除该通知</span></span>
                </div>
                <el-tooltip
                    v-if="at.dynamic?.imgAddress"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="at.dynamic?.title"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a v-if="at.dynamic?.imgAddress" class="at-video-cover-container" :href="'./dynamicDetail?dynamicId='+at.at.dynamicId" target="dynamicWindow"><img class="at-video-cover" :src="at.dynamic?.imgAddress.split(',')[0]"></a>
                </el-tooltip>
                <div class="at-line"></div>
              </div>
           </div>
        </div>
        <div v-show="atList.length===0&&selectAtCommentFFlag" class="no-at-data">
          <img src="../img/没有消息数据.png">
        </div>
        <div v-show="atList.length===0&&!selectAtCommentFFlag" class="at-loading">
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
  import {selectAtComment,commentControles,deleteCommentNotification,replayComment}  from '../api/comment/index';
  import {putMessageNumber}  from '../api/privateMessage/index';


  const store = useGlobalStore();
  const atList = reactive([]);
  const pageNum = ref(1);
  const replyCommentHoverFlag = ref(false);
  const loveCommentHoverFlag = ref(false);
  const deleteCommentNotificaitonHoverFlag = ref(false);
  const selectAtCommentFFlag = ref(false);
  const deleteCommentDialogFlag=ref(false);
  const deleteCommentNotificaitonId=ref(null);
  const commentContent=ref("");
  const Scrolled=ref(true);

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
    })
  }

  //获取@我的评论
  function selectAtCommentF(){

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
    selectAtComment(store.token,store.userId,pageNum.value++).then(res => {
      if(res.data.code === 1)
       { 
        pushData(res.data.data);
        selectAtCommentFFlag.value = true;
        if(res.data.data===null||res.data.data.length===0)
        Scrolled.value=false;
       }
    })
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
      if(comment.deleteSign===1||!comment.content)
      return "[该评论已被删除]";
      let content=comment.content.replace("style='width: 24px;height: 24px;display: inline-block;","style='width: 24px;height: 24px;display: none;");
  
      if(comment.replyCommentId!==comment.mainCommentId)
       content=content.replace("&nbsp;&nbsp;","");
      const imgArr=comment.imgAddress.split(',');
      for(let i=0;i<imgArr.length-1;i++){
          content=content+"[图片]";
      }
      return content;
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
          atList.splice(atList.findIndex(item=>item.at.id===deleteCommentNotificaitonId.value),1);
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
  function replayCommentF(at){

      if(at.comments.mainCommentId!==null)
      commentContent.value=`&nbsp;回复 <a href='https://baidu.com' data-eit-userid='${at.comments.userId}' class='at-msg' style='color:#008ac5;'>@${at.comments.userName}</a> ：`+commentContent.value;
      if(at.isAuthorFlag===1)
      commentContent.value = "<img style='width: 24px;height: 24px;display: inline-block;vertical-align: middle;transform: translateY(-1.5px);user-select: none;' src='../img/up_pb.svg'>&nbsp;"+commentContent.value;
     
      let comments={
        content: commentContent.value,
        imgAddress: null,
        userId: store.userId,
        videoId: at.comments.videoId,
        dynamicId: at.comments.dynamicId,
        userName: store.userInformation.userName,
        dynamicFlag: 0,
        replyCommentId: at.comments.id,
        mainCommentId: !at.comments.mainCommentId?at.comments.id:at.comments.mainCommentId,
        replyUserId: at.at.userId,
      }

      let addComment={
        comments,
        ats: []
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
          at.replyFlag=false;
        }
      })
  }

  //打开回复评论
  function openReplyComment(at){

      at.replyFlag=!at.replyFlag;
      if(at.replyFlag){
        nextTick(()=>{
          document.getElementById("at"+at.at.id).focus();
        })
      }else{
        commentContent.value="";
      }
  }

  //滚动加载
  function handleScroll(e){
      if(Scrolled.value&&e.target.scrollHeight - e.target.scrollTop <= e.target.clientHeight + 1){
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
              color: #2faee3;
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
              color: #1389bf;
              transition: color 0.3s ease;
            }
            a:hover{
              color: #2faee3;
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
                 top: 5.5px;
                 width: 14px;
                 height: 13px;
                 margin-right: 4.5px;
               }
            }
            .at-comment:hover{
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

          .at-comment-container{
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
            .at-comment-input{
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
            .at-comment-input:focus{
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
          width: 102%;
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
    cursor: pointer;
    height: 32px;
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