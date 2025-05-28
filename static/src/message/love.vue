<template>
    <div class="love-container">
      <div v-show="likeUserList.length===0" class="head2">收到的赞</div>
      <div v-show="likeUserList.length>0" class="head2"><span class="detail" @click="likeUserList.length=0">收到的赞</span> > 点赞详情</div>
      <a v-if="likeUserList.length!==0&&loves&&loves.likes.likeType===1" :href="'./video?videoId=BV'+loves.videos.id" target="videoWindow"><div class="head2" :class="{'head3':likeUserList.length!==0}"><span class="detail">视频：<span v-html="loves.comments!==null?addImgFlagF(loves.comments):loves.videos.title"></span></span></div></a>
      <a v-else-if="likeUserList.length!==0&&loves&&loves.likes.likeType===2&&loves.comments.deleteSign===0&&!loves.comments.dynamicId" :href="'./video?videoId=BV'+loves.comments.videoId+'&commentId='+`${loves.comments.mainCommentId?loves.comments.mainCommentId:loves.comments.id}`+'&replyId='+loves.comments.id" target="videoWindow"><div class="head2" :class="{'head3':likeUserList.length!==0}"><span class="detail">评论：<span v-html="loves.comments!==null?addImgFlagF(loves.comments):loves.videos.title"></span></span></div></a>
      <a v-else-if="likeUserList.length!==0&&loves&&loves.likes.likeType===2&&loves.comments.deleteSign===1&&!loves.comments.dynamicId" :href="'./video?videoId=BV'+loves.comments.videoId" target="videoWindow"><div class="head2" :class="{'head3':likeUserList.length!==0}"><span class="detail">评论：<span v-html="loves.comments!==null?addImgFlagF(loves.comments):loves.videos.title"></span></span></div></a>
      <a v-else-if="likeUserList.length!==0&&loves&&loves.likes.likeType===2&&loves.comments.deleteSign===0&&loves.comments.dynamicId" :href="'./dynamicDetail?dynamicId='+loves.comments.dynamicId+'&commentId='+`${loves.comments.mainCommentId?loves.comments.mainCommentId:loves.comments.id}`+'&replyId='+loves.comments.id" target="dynamicWindow"><div class="head2" :class="{'head3':likeUserList.length!==0}"><span class="detail">评论：<span v-html="loves.comments!==null?addImgFlagF(loves.comments):loves.dynamic.title"></span></span></div></a>
      <a v-else-if="likeUserList.length!==0&&loves&&loves.likes.likeType===2&&loves.comments.deleteSign===1&&loves.comments.dynamicId" :href="'./dynamicDetail?dynamicId='+loves.comments.dynamicId" target="dynamicWindow"><div class="head2" :class="{'head3':likeUserList.length!==0}"><span class="detail">评论：<span v-html="loves.comments!==null?addImgFlagF(loves.comments):loves.dynamic.title"></span></span></div></a>
      <a v-else-if="likeUserList.length!==0&&loves&&loves.likes.likeType===3" :href="'./dynamicDetail?dynamicId='+loves.dynamic.id" target="dynamicWindow"><div class="head2" :class="{'head3':likeUserList.length!==0}"><span class="detail">动态：<span v-html="loves.comments!==null?addImgFlagF(loves.comments):loves.dynamic.title"></span></span></div></a>
      <div v-show="likeUserList.length===0" class="love-content" @scroll="handleScroll">
        <div v-show="loveList.length>0" class="love-item" v-for="love in loveList" :key="love.id">
           <div v-if="love.userInfoList?.length===1" class="love-item-content">
              <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.userInfoList[0].userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a :href="'./home?homeMenu=1&userId='+love.userInfoList[0].id" target="_blank"><img class="love-user-avatar" :src="love.userInfoList[0].avatarAddress"></a>
              </el-tooltip>
              <div class="love-right-content">
                 <div class="love-right-content-top">
                  <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.userInfoList[0].userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a :href="'./home?homeMenu=1&userId='+love.userInfoList[0].id" target="_blank">{{ love.userInfoList[0].userName }}</a>
                  </el-tooltip>
                  <span v-if="love.likes.likeType===1" class="love-list" @click="selectLikeUserF(love)">赞了我的视频</span>
                  <span v-else-if="love.likes.likeType===2" class="love-list" @click="selectLikeUserF(love)">赞了我的评论</span>
                  <span v-else-if="love.likes.likeType===3" class="love-list" @click="selectLikeUserF(love)">赞了我的动态</span>
                </div>
                <div class="love-right-content-bottom">
                    <span class="love-time">{{ love.likeTime }}</span>
                    <span class="delete-comment-notification" @click="deleteLoveNotificationDialogF(love)" 
                    @mouseover="deleteCommentNotificaitonHoverFlag=true" 
                    @mouseleave="deleteCommentNotificaitonHoverFlag=false">
                    <img :src="deleteCommentNotificaitonHoverFlag?'../img/删除通知蓝.png':'../img/删除通知.png'"> <span>删除该通知</span></span>
                    <span v-show="love.comments?.likeWarn===1||love.videos?.likeWarn===1||love.dynamic?.likeWarn===1" class="comment-notification" @click="dndLikeNotificationF(love)"><img :src="commentNotificationHoverFlag?'../img/屏蔽通知蓝.png':'../img/屏蔽通知灰.png'"> <span>不再通知</span></span>
                    <span v-show="love.comments?.likeWarn===0||love.videos?.likeWarn===0||love.dynamic?.likeWarn===0" class="comment-disable-notification" @click="dndLikeNotificationF(love)" @mouseover="commentDisableificationHoverFlag=true" @mouseleave="commentDisableificationHoverFlag=false"><img :src="commentDisableificationHoverFlag?'../img/开启通知蓝.png':'../img/开启通知灰.png'"> <span>接收通知</span></span>
                </div>
                <img v-show="love.comments?.likeWarn===0||love.videos?.likeWarn===0||love.dynamic?.likeWarn===0" class="love-dnd" src="../img/dnd.svg">
                <el-tooltip
                    v-if="love.comments!==null"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <template #content>
                      <div v-if="love.comments!==null" v-html="addImgFlagF(love.comments)"></div>
                  </template>
                  <a v-if="love.comments!==null&&love.comments.deleteSign===0" class="comment-content" :href="'./video?videoId=BV'+love.comments.videoId+'&commentId='+`${love.comments.mainCommentId?love.comments.mainCommentId:love.comments.id}`+'&replyId='+love.comments.id" target="videoWindow"><span v-html="addImgFlagF(love.comments)"></span></a>
                  <a v-else-if="love.comments!==null&&love.comments.deleteSign===1" class="comment-content" :href="'./video?videoId=BV'+love.comments.videoId" target="videoWindow"><span v-html="addImgFlagF(love.comments)"></span></a>
                </el-tooltip>
                <el-tooltip
                    v-if="love.likes.likeType===1"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.videos.title"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a v-if="love.likes.likeType===1" class="love-video-cover-container" :href="'./video?videoId=BV'+love.videos.id" target="videoWindow"><img class="love-video-cover" :src="love.videos.coverAddress"></a>
                </el-tooltip>
                <el-tooltip
                    v-if="love.likes.likeType===3&&love.dynamic.imgAddress"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.dynamic.title"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a v-if="love.likes.likeType===3&&love.dynamic.imgAddress" class="love-video-cover-container" :href="'./video?videoId=BV'+love.videos.id" target="videoWindow"><img class="love-video-cover" :src="love.dynamic.imgAddress.split(',')[0]"></a>
                </el-tooltip>
                <div class="love-line"></div>
              </div>
           </div>
           <div v-else-if="love.comments?.likeCommentNumber===2||love.videos?.likeNumber===2||love.dynamic?.likeNumber===2" class="love-item-content">
              <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.userInfoList[0].userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a :href="'./home?homeMenu=1&userId='+love.userInfoList[0].id" target="_blank"><img class="love-user-avatar1" :src="love.userInfoList[0].avatarAddress"></a>
              </el-tooltip>
              <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.userInfoList[1].userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a :href="'./home?homeMenu=1&userId='+love.userInfoList[1].id" target="_blank"><img class="love-user-avatar2" :src="love.userInfoList[1].avatarAddress"></a>
              </el-tooltip>
              <div class="love-right-content">
                 <div class="love-right-content-top">
                  <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.userInfoList[0].userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a :href="'./home?homeMenu=1&userId='+love.userInfoList[0].id" target="_blank">{{ love.userInfoList[0].userName }}、</a>
                  </el-tooltip>
                  <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.userInfoList[1].userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a :href="'./home?homeMenu=1&userId='+love.userInfoList[1].id" target="_blank">{{ love.userInfoList[1].userName }}</a>
                  </el-tooltip>
                  <span v-if="love.comments!==null" class="love-list" @click="selectLikeUserF(love)">赞了我的评论</span>
                  <span v-else class="love-list" @click="selectLikeUserF(love)">赞了我的视频</span>
                </div>
                <div class="love-right-content-center">
                </div>
                <div class="love-right-content-bottom">
                    <span class="love-time">{{ love.likeTime }}</span>
                    <span class="delete-comment-notification" @click="deleteLoveNotificationDialogF(love)" @mouseover="deleteCommentNotificaitonHoverFlag=true" @mouseleave="deleteCommentNotificaitonHoverFlag=false"><img :src="deleteCommentNotificaitonHoverFlag?'../img/删除通知蓝.png':'../img/删除通知.png'"> <span>删除该通知</span></span>
                    <span v-show="love.comments?.likeWarn===1||love.videos?.likeWarn===1||love.dynamic?.likeWarn===1" class="comment-notification" @click="dndLikeNotificationF(love)" @mouseover="commentNotificationHoverFlag=true" @mouseleave="commentNotificationHoverFlag=false"><img :src="commentNotificationHoverFlag?'../img/屏蔽通知蓝.png':'../img/屏蔽通知灰.png'"> <span>不再通知</span></span>
                    <span v-show="love.comments?.likeWarn===0||love.videos?.likeWarn===0||love.dynamic?.likeWarn===0" class="comment-disable-notification" @click="dndLikeNotificationF(love)" @mouseover="commentDisableificationHoverFlag=true" @mouseleave="commentDisableificationHoverFlag=false"><img :src="commentDisableificationHoverFlag?'../img/开启通知蓝.png':'../img/开启通知灰.png'"> <span>接收通知</span></span>
                </div>
                <img v-show="love.comments?.likeWarn===0||love.videos?.likeWarn===0||love.dynamic?.likeWarn===0" class="love-dnd" src="../img/dnd.svg">
                <el-tooltip
                    v-if="love.likes.likeType===2"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <template #content>
                      <div v-if="love.likes.likeType===2" v-html="addImgFlagF(love.comments)"></div>
                  </template>
                  <a v-if="love.comments!==null&&love.comments.deleteSign===0" class="comment-content" :href="'./video?videoId=BV'+love.comments.videoId+'&commentId='+`${love.comments.mainCommentId?love.comments.mainCommentId:love.comments.id}`+'&replyId='+love.comments.id" target="videoWindow"><span v-html="addImgFlagF(love.comments)"></span></a>
                  <a v-else-if="love.comments!==null&&love.comments.deleteSign===1" class="comment-content" :href="'./video?videoId=BV'+love.comments.videoId" target="videoWindow"><span v-html="addImgFlagF(love.comments)"></span></a>                </el-tooltip>
                <el-tooltip
                    v-if="love.likes.likeType===1"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.videos.title"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a v-if="love.likes.likeType===1" class="love-video-cover-container" :href="'./video?videoId=BV'+love.videos.id" target="videoWindow"><img class="love-video-cover" :src="love.videos.coverAddress"></a>
                </el-tooltip>
                <el-tooltip
                    v-if="love.likes.likeType===3&&love.dynamic.imgAddress"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.dynamic.title"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a v-if="love.likes.likeType===3&&love.dynamic.imgAddress" class="love-video-cover-container" :href="'./video?videoId=BV'+love.videos.id" target="videoWindow"><img class="love-video-cover" :src="love.dynamic.imgAddress.split(',')[0]"></a>
                </el-tooltip>
                <div class="love-line"></div>
              </div>
           </div>
           <div v-else-if="love.comments?.likeCommentNumber>2||love.videos?.likeNumber>2||love.dynamic?.likeNumber>2" class="love-item-content">
            <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.userInfoList[0].userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a :href="'./home?homeMenu=1&userId='+love.userInfoList[0].id" target="_blank"><img class="love-user-avatar1" :src="love.userInfoList[0].avatarAddress"></a>
              </el-tooltip>
              <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.userInfoList[1].userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a :href="'./home?homeMenu=1&userId='+love.userInfoList[1].id" target="_blank"><img class="love-user-avatar2" :src="love.userInfoList[1].avatarAddress"></a>
              </el-tooltip>
              <div class="love-right-content">
                 <div class="love-right-content-top">
                  <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.userInfoList[0].userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a :href="'./home?homeMenu=1&userId='+love.userInfoList[0].id" target="_blank">{{ love.userInfoList[0].userName }}、</a>
                  </el-tooltip>
                  <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.userInfoList[1].userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a :href="'./home?homeMenu=1&userId='+love.userInfoList[1].id" target="_blank">{{ love.userInfoList[1].userName }}</a>
                  </el-tooltip>
                  <span v-if="love.comments!==null" class="love-list" @click="selectLikeUserF(love)">等总计{{ love.comments.likeCommentNumber }}人赞了我的评论</span>
                  <span v-else class="love-list" @click="selectLikeUserF(love)">等总计{{ love.videos.likeNumber }}人赞了我的视频</span>
                </div>
                <div class="love-right-content-center">
                </div>
                <div class="love-right-content-bottom">
                    <span class="love-time">{{ love.likeTime }}</span>
                    <span class="delete-comment-notification" @click="deleteLoveNotificationDialogF(love)"><img :src="deleteCommentNotificaitonHoverFlag?'../img/删除通知蓝.png':'../img/删除通知.png'"> <span>删除该通知</span></span>
                    <span v-show="love.comments?.likeWarn===1||love.videos?.likeWarn===1||love.dynamic?.likeWarn===1" class="comment-notification" @click="dndLikeNotificationF(love)" @mouseover="commentNotificationHoverFlag=true" @mouseleave="commentNotificationHoverFlag=false"><img :src="commentNotificationHoverFlag?'../img/屏蔽通知蓝.png':'../img/屏蔽通知灰.png'"> <span>不再通知</span></span>
                    <span v-show="love.comments?.likeWarn===0||love.videos?.likeWarn===0||love.dynamic?.likeWarn===0" class="comment-disable-notification" @click="dndLikeNotificationF(love)" @mouseover="commentDisableificationHoverFlag=true" @mouseleave="commentDisableificationHoverFlag=false"><img :src="commentDisableificationHoverFlag?'../img/开启通知蓝.png':'../img/开启通知灰.png'"> <span>接收通知</span></span>
                </div>
                <img v-show="love.comments?.likeWarn===0||love.videos?.likeWarn===0||love.dynamic?.likeWarn===0" class="love-dnd" src="../img/dnd.svg">
                <el-tooltip
                    v-if="love.likes.likeType===2"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <template #content>
                      <div v-if="love.likes.likeType===2" v-html="addImgFlagF(love.comments)"></div>
                  </template>
                  <a v-if="love.comments!==null&&love.comments.deleteSign===0" class="comment-content" :href="'./video?videoId=BV'+love.comments.videoId+'&commentId='+`${love.comments.mainCommentId?love.comments.mainCommentId:love.comments.id}`+'&replyId='+love.comments.id" target="videoWindow"><span v-html="addImgFlagF(love.comments)"></span></a>
                  <a v-else-if="love.comments!==null&&love.comments.deleteSign===1" class="comment-content" :href="'./video?videoId=BV'+love.comments.videoId" target="videoWindow"><span v-html="addImgFlagF(love.comments)"></span></a>                </el-tooltip>
                <el-tooltip
                    v-if="love.likes.likeType===1"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.videos.title"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a v-if="love.likes.likeType===1" class="love-video-cover-container" :href="'./video?videoId=BV'+love.videos.id" target="videoWindow"><img class="love-video-cover" :src="love.videos.coverAddress"></a>
                </el-tooltip>
                <el-tooltip
                    v-if="love.likes.likeType===3&&love.dynamic.imgAddress"
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="love.dynamic.title"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                  <a v-if="love.likes.likeType===3&&love.dynamic.imgAddress" class="love-video-cover-container" :href="'./video?videoId=BV'+love.videos.id" target="videoWindow"><img class="love-video-cover" :src="love.dynamic.imgAddress.split(',')[0]"></a>
                </el-tooltip>
                <div class="love-line"></div>
              </div>
           </div>
        </div>
        <div v-show="loveList.length===0&&selectLikeCommentFFlag" class="no-love-data">
          <img src="../img/没有消息数据.png">
        </div>
        <div v-show="loveList.length===0&&!selectLikeCommentFFlag" class="love-loading">
          <img src="../img/加载消息.gif">
        </div>
      </div>
      <div class="love-content" style="padding-bottom: 200px;" v-show="likeUserList.length>0" @scroll="handleScroll2">
        <div v-show="loveList.length>0" class="love-item" v-for="love in likeUserList" :key="love?.id">
          <div class="love-item-content" style="cursor: default;">
                <el-tooltip
                      popper-class="custom-tooltip"
                      effect="light"
                      :show-after="300"
                      :content="love.userInfo2.userName"
                      placement="bottom-end"
                      :offset="12"
                      :show-arrow="false"
                      :hide-after="0"
                    >
                    <a :href="'./home?homeMenu=1&userId='+love.userInfo2.id" target="_blank"><img class="love-user-avatar" :src="love.userInfo2.avatarAddress"></a>
                </el-tooltip>
                <div class="love-right-content">
                  <div class="love-right-content-top">
                    <el-tooltip
                      popper-class="custom-tooltip"
                      effect="light"
                      :show-after="300"
                      :content="love.userInfo2.userName"
                      placement="bottom-end"
                      :offset="12"
                      :show-arrow="false"
                      :hide-after="0"
                    >
                    <a :href="'./home?homeMenu=1&userId='+love.userInfo2.id" target="_blank">{{ love.userInfo2.userName }}</a>
                    </el-tooltip>
                    <span>赞了我</span>
                  </div>
                  <div class="love-right-content-center">
                     <button v-show="love.isFansFlag===0" @click="addFollowAxios(love)">关注</button>
                     <button style="background-color: #e5e9ef;" v-show="love.isFansFlag===1&&!deleteFollowHoverFlag!==love.id" @mouseover="deleteFollowHoverFlag=love.id" @click="deleteFollowAxios(love)">已关注</button>
                     <button style="background-color: #e5e9ef;" v-show="love.isFansFlag===1&&deleteFollowHoverFlag===love.id"  @mouseleave="deleteFollowHoverFlag=null" @click="deleteFollowAxios(love)">取消关注</button>
                  </div>
                  <div class="love-right-content-bottom">
                    <span class="love-time">{{ love.likeTime }}</span>
                  </div>
                  <div class="love-line"></div>
                </div>
          </div>
        </div> 
      </div> 
      <el-dialog
        v-model="deleteLoveDialogFlag"
        append-to-body
        width="464"
        :lock-scroll="false"
        align-center
        style="height: 169.8px; border-radius: 12px"
      >
      <span class="delete-dialog-title">删除</span>
      <div class="delete-dialog-content">
        该条通知删除后，当有新点赞时会重新出现在列表，是否继续？
      </div>
      <div class="delete-dialog-btn-container">
        <button class="delete-dialog-confirm-btn" @click="deleteLoveNotificationF">
        确定
      </button>
      <button
        class="delete-dialog-cancel-btn"
        @click="deleteLoveDialogFlag=false"
      >
        取消
      </button>
      </div>
      </el-dialog>
    </div>
  </template>
  
  <script setup>
  import { onMounted, reactive, ref, watch } from 'vue'
  import {useGlobalStore} from "../store/store";
  import { ElMessage } from "element-plus";   
  import {putMessageNumber}  from '../api/privateMessage/index';
  import {selectLike,deleteCommentNotification,dndLikeNotification,selectLikeUser}  from '../api/comment/index';
  import {addFollow,deleteFollow}  from '../api/user/index';


  const store = useGlobalStore();
  const loveList = reactive([]);
  const pageNum = ref(1);
  const selectLikeCommentFFlag = ref(false);
  const deleteLoveDialogFlag=ref(false);
  const deleteLoveNotificaitonId=ref(null);
  const deleteLoveNotificaitonType=ref(null);
  const deleteCommentNotificaitonHoverFlag = ref(false);
  const commentNotificationHoverFlag = ref(false);
  const commentDisableificationHoverFlag = ref(false);
  const Scrolled=ref(true);
  const likeUserList=reactive([]);
  const pageNum2=ref(1);
  const loves=ref(null);
  const deleteFollowHoverFlag=ref(null);

  onMounted(()=>{

    putMessageNumberF();
    setTimeout(() => {
      selectLikeF();
    }, 500);
  })


  //修改消息数量
  function putMessageNumberF(){
      putMessageNumber(store).then(res=>{
        if(res.data.code===1){
          let userInformation=store.userInformation;
          userInformation.likeAllNumber=0;
          userInformation.allMessageNumber=userInformation.messageNumber+userInformation.replyCommentNumber+userInformation.atNumber+userInformation.likeAllNumber;
          store.setUserInformation(userInformation);
        }
      })
  }

  //查询收到的赞
  function selectLikeF(){
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
      selectLike(store.token,store.userId,pageNum.value++).then(res=>{
        if(res.data.code===1){
          pushData(res.data.data);
          selectLikeCommentFFlag.value = true;
          if(res.data.data===null||res.data.data.length===0)
          Scrolled.value=false;
        }
      })
  }

  //去除重复数据
  function pushData(newData) {
    const idSet = new Set(loveList.map(item => item.id)); // 获取已有数据的 ID
    const uniqueData = newData.filter(item => !idSet.has(item.id)); // 过滤掉重复的
    loveList.push(...uniqueData); // Vue 响应式更新
  }

  //去除重复数据
  function pushData2(newData) {
    const idSet = new Set(likeUserList.map(item => item.id)); // 获取已有数据的 ID
    const uniqueData = newData.filter(item => !idSet.has(item.id)); // 过滤掉重复的
    likeUserList.push(...uniqueData); // Vue 响应式更新
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

  //删除喜欢通知对话框
  function deleteLoveNotificationDialogF(love){
    deleteLoveDialogFlag.value=true;
    deleteLoveNotificaitonType.value=love.likes.likeType;
    if(deleteLoveNotificaitonType.value===1)
      deleteLoveNotificaitonId.value=love.videos.id;
    else if(deleteLoveNotificaitonType.value===2)
      deleteLoveNotificaitonId.value=love.comments.id;
    else if(deleteLoveNotificaitonType.value===3)
      deleteLoveNotificaitonId.value=love.dynamic.id;
      console.log(deleteLoveNotificaitonType.value);
          console.log(deleteLoveNotificaitonId.value);
          console.log(loveList);
  }

  //删除通知
  function deleteLoveNotificationF(){
      deleteCommentNotification(store.token,deleteLoveNotificaitonId.value,store.userId,store.messageMenu,deleteLoveNotificaitonType.value).then(res=>{
        if(res.data.code===1){
          ElMessage({
            message: "删除成功",
            type: "info",
            plain: true,
            duration: 1700,
          });
          loveList.splice(loveList.findIndex(item=>
          (deleteLoveNotificaitonType.value===1&&item.videos?.id===deleteLoveNotificaitonId.value)
          ||(deleteLoveNotificaitonType.value===2&&item.comments?.id===deleteLoveNotificaitonId.value)
          ||(deleteLoveNotificaitonType.value===3&&item.dynamic?.id===deleteLoveNotificaitonId.value)
          ),1);
          deleteLoveDialogFlag.value=false;
          deleteLoveNotificaitonId.value=null; 
          deleteLoveNotificaitonType.value=null;
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

  //滚动加载
  function handleScroll(e){
      if(Scrolled.value&&e.target.scrollHeight - e.target.scrollTop <= e.target.clientHeight + 1){
        selectLikeF();
      }
  }

  //滚动加载
  function handleScroll2(e){
      if(Scrolled.value&&e.target.scrollHeight - e.target.scrollTop <= e.target.clientHeight + 1){
        selectLikeUserF(loves.value);
      }
  }

  //更改点赞消息提示状态
  function dndLikeNotificationF(love){
      
      let id=null;

      if(love.likes.likeType===1)
        id=love.videos.id;
      else if(love.likes.likeType===2)
        id=love.comments.id;
      else if(love.likes.likeType===3)
        id=love.dynamic.id;

      dndLikeNotification(store.token,store.userId,id,love.likes.likeType).then(res=>{

        if(res.data.code===1){
          if(love.likes.likeType===1)
            love.videos.likeWarn=love.videos.likeWarn===1?0:1;
          else if(love.likes.likeType===2)
            love.comments.likeWarn=love.comments.likeWarn===1?0:1;
          else if(love.likes.likeType===3)
            love.dynamic.likeWarn=love.dynamic.likeWarn===1?0:1;
        }
      })
  }

  //查询喜欢评论或视频的用户
  function selectLikeUserF(love){
    loves.value=love;
    let id=null;
    if(love.likes.likeType===1)
      id=love.videos.id;
    else if(love.likes.likeType===2)
      id=love.comments.id;
    else if(love.likes.likeType===3)
      id=love.dynamic.id;

    selectLikeUser(store.token,store.userId,id,pageNum2.value++,love.likes.likeType).then(res=>{
      pushData2(res.data.data);
    })
  }

  //重置页码
  watch(likeUserList,()=>{
    if(likeUserList.length===0)
    {
      pageNum2.value=1;
      loves.value=null;
    }
  })

  //关注用户
  async function addFollowAxios(love) {

        if (store.userId === love.userInfo2.id) {
          ElMessage({
            message: "不能关注自己哦",
            type: "info",
            plain: true,
            duration: 1700,
          });
          return;
        }
        addFollow(store.token,store.userId,love.userInfo2.id).then(res=>{
          love.isFansFlag=1;
        })
   
  }

  //取消关注用户
  async function deleteFollowAxios(love) {

       deleteFollow(store.token,store.userId,love.userInfo2.id).then(res=>{
         love.isFansFlag=0;
       })

  }


  </script>
  
<style lang="scss" scoped>
*{
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
  
.love-container{
      position: absolute;
      width: 983px;
      top: 74px;
      left: 299px;
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
      .detail{
        cursor: pointer;
        transition: color 0.3s ease;
      }
      .detail:hover{
        color: #2faee3;
      }
      } 
      .head3{
        margin-top: 10px;
        padding: 16px;
        height: auto;
        .detail{
          color: #222;
          flex: 1;
          padding-right: 16px;
          margin-top: 8px;
          margin-bottom: 8px;
          overflow: hidden;
          text-overflow: ellipsis;
          word-break: break-word;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          font-size: 14px;
          line-height: 19px;
          max-height: 2.714285714285714em;
        }
      }
      .love-content{
      position: relative;
      top: 10px;
      width: 983px;
      height: 100vh;
      background-color: var(--background-color);
      border-radius: 4px;
      box-shadow: 0 2px 4px 0 rgba(121, 146, 185, 0.54);
      margin-bottom: 10px;
      padding: 24px 16px;
      overflow-y: auto;
      padding-bottom: 120px;

      .love-item{
       position: relative;
       width: 100%;
       background-color: rgba(255, 255, 255, 0);
       padding-bottom: 20px;
       cursor: pointer;
       height: auto;
       max-height: 220.5px;
       margin-bottom: 8.5px;

       .love-item-content{
         position: relative;
         top: 0;
         left: 0;
         width: 100%;
         background-color: rgba(255, 255, 255, 0);
         height: auto;

         .love-user-avatar{
          position: absolute;
          user-select: none;
          display: block;
          width: 46px;
          height: 46px;
          border-radius: 50%;        
         }

         .love-user-avatar1{
          position: absolute;
          user-select: none;
          display: block;
          width: 32px;
          height: 32px;
          border-radius: 50%;
          left: 16px;
          top: 12px;
          z-index: 10; 
         }

         .love-user-avatar2{
          position: absolute;
          user-select: none;
          display: block;
          width: 32px;
          height: 32px;
          border-radius: 50%;        
         }

         .love-right-content{
          position: relative;
          top: 0px;
          left: 59.5px;
          width: calc(100% - 59.5px);
          height: auto;
          background-color: rgb(255, 255, 255,0);

          .love-right-content-top{
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

            .love-list{
              transition: color 0.3s ease;
            }

            .love-list:hover{
                color: #2faee3;
             }
            
          }

          .love-right-content-center{
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
            .love-message-content{
              display: -webkit-box;
              -webkit-box-orient: vertical; /* 必须设置垂直排列 */
              overflow: hidden;
              text-overflow: ellipsis;
              word-break: break-all;
              -webkit-line-clamp: 2;
              line-height: 18px;
              max-height: 2.571428571428572em;
            }

            button{
              position: absolute;
              top: -13px;
              right: -119.5px;
              width: 72px;
              height: 24px;
              color: #6d757a;
              font-size: 12px;
              cursor: pointer;
              border-radius: 4px;
              background: #fff;
              border: 1px solid #ccd0d7;
            }
  
          }

          .love-right-content-bottom{
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
            .love-time{
              position: relative;
              top: 0.5px;
              left: 0;
            }

            .love-comment{
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
            .love-comment:hover{
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
              left: 19px;
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

            .comment-notification{
              position: relative;
              display: flex;
              left: 33.5px;
              top: -1px;
              font-size: 13.5px;
              opacity: 0;
               img{
                 position: relative;
                 top: 5px;
                 width: 12px;
                 height: 15px;
                 margin-right: 5px;
               }
            }
            .comment-notification:hover{
              color: #2faee3;
            }

            .comment-disable-notification{
              position: relative;
              display: flex;
              left: 33.5px;
              top: -1px;
              font-size: 13.5px;
              opacity: 0;
               img{
                 position: relative;
                 top: 5px;
                 width: 12px;
                 height: 15px;
                 margin-right: 5px;
               }
            }
            .comment-disable-notification:hover{
              color: #2faee3;
            }

          }

          .love-dnd{
            position: absolute;
            right: 77px;
            top: 30%;
            transform: translateY(-30%);
          }

          .love-comment-container{
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
            .love-comment-input{
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
            .love-comment-input:focus{
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

          .love-video-cover-container{
            position: absolute;
            top: -8px;
            right: 0px;
            .love-video-cover{
              width: 60px;
              height: 60px;
              object-fit: cover;
            }
          }
          

          .love-line{
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

      .love-item:hover{ 
        .love-right-content{
          .love-right-content-bottom{
            .delete-comment-notification{
            opacity: 1;
            visibility: visible;
            }
            .comment-notification{
            opacity: 1;
            visibility: visible;
            }
            .comment-disable-notification{
            opacity: 1;
            visibility: visible;
            }
          }
        }
        
      }

      .love-loading{
        position: absolute;
        left: 50%;
        transform: translate(-50%,);
      }

      .no-love-data{
        position: absolute;
        left: 50%;
        transform: translate(-50%);
      }
       

      }

     .love-content::-webkit-scrollbar{
      width: 6px; /* 滚动条的宽度 */
      border-radius: 30px; /* 滚动条滑块的圆角 */
      }

      .love-content::-webkit-scrollbar-thumb {
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
    cursor: pointer;
    border-radius: 4px;
    min-width: 104px;
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
    cursor: pointer;
    border-radius: 4px;
    min-width: 104px;
    height: 32px;
    font-size: 14px;
    margin: 0 10px;
}

.delete-dialog-confirm-btn:hover{
    background-color: #39b5e7;
}
  
</style>