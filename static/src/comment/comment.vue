<template>
  <div class="comment-container"  v-loading.fullscreen="loading" element-loading-background="rgba(122, 122, 122, 0)">
    <div class="comment-header">
      <div class="comment-title">
        评论
        <span class="comment-number">{{ store.commentNumber }}</span>
        <span
          class="comment-sort-fire"
          :class="{ commentSortFlag: commentSortFlag === 1 }"
          @click="store.userId!==null ? commentSortFlag = 1 : store.setLoginDialogVisibleFlag(true)"
          >最热</span
        >
        <span class="comment-sort-line">|</span>
        <span
          class="comment-sort-new"
          :class="{ commentSortFlag: commentSortFlag === 2 }"
          @click="store.userId!==null ? commentSortFlag = 2 : store.setLoginDialogVisibleFlag(true)"
          >最新</span
        >
      </div>
      <img
        class="comment-avatar"
        v-show="store.userId === null"
        src="/默认头像.gif"
      />
      <img
        @click="openHome(1,store.userId)"
        class="comment-avatar"
        v-show="store.userId !== null"
        :src="store.userInformation?.avatarAddress"
      />
      <div
        v-show="store.userId === null"
        class="not-login-commit-comment-container"
      >
        <span
          >请先<button @click="store.setLoginDialogVisibleFlag(true)">
            登录</button
          >后发表评论 (・ω・)</span
        >
      </div>
      <div
        style="width: 0px"
        @click="(commentFocusFlag = true), commentRef.focus()"
        @mousemove="commentHoverFlag = true"
        @mouseleave="commentHoverFlag = false"
      >
        <span
          class="comment-placeholder"
          v-show="store.userId !== null && !commentLengthFlag"
          @mouseover="
            (commentPlaceholderHoverFlag = true), (commentHoverFlag = true)
          "
          @mouseout="
            (commentPlaceholderHoverFlag = false), (commentHoverFlag = false)
          "
          >{{ commentPlaceholderr }}</span
        >
        <div
          class="comment-content-container"
          :class="{
            commentImgFiles1: commentImg.length > 0,
            commentFocusFlag: commentFocusFlag || commentLengthFlag,
          }"
        >
          <div
            v-show="store.userId !== null"
            ref="commentRef"
            class="login-commit-comment-container"
            contenteditable
            :class="{
              commentPlaceholderHoverFlag: commentPlaceholderHoverFlag,
            }"
            @click="commentRef.focus(), handleContent()"
            @focus="handleContent"
            @blur="getAfterBlurIndex"
            @input="handleContent"
            @keyup.left="handleContent"
            @keyup.right="handleContent"
            @keyup.up="handleContent"
            @keyup.down="handleContent"
            @mousemove="commentHoverFlag = true"
            @mouseleave="commentHoverFlag = false"
          ></div>
          <div v-show="commentImg.length > 0" class="comment-img-container">
            <div v-for="(file,index) in commentImg" :key="file">
              <span
                v-show="file === null || file === undefined"
                class="loading-span"
              >
                <span @click="deleteCommentImg(index)"
                  ><img src="../img/关闭视频定位.png"
                /></span>
                <img src="../img/loading-blue.gif" />
              </span>
              <span
                v-show="file !== undefined && file !== null"
                class="loading-before-span"
              >
                <span @click="deleteCommentImg(index)"
                  ><img src="../img/关闭视频定位.png"
                /></span>
                <img :src="file" />
              </span>
            </div>
            <img
              class="left"
              :class="{ left2: commentImg.length === 8 }"
              v-show="commentImgLocation === 'left' && commentImg.length > 7"
              @click="postionImg('left')"
              src="../img/图片向左按钮.png"
            />
            <img
              class="right"
              v-show="commentImgLocation === 'right' && commentImg.length > 7"
              @click="postionImg('right')"
              src="../img/图片向右按钮.png"
            />
            <div
              v-show="commentImg.length > 7 && commentImgLocation === 'left'"
              class="shadowEdgeLeft"
              :class="{
                shadowEdgeLeft2:
                  commentImg.length === 8 && commentImgLocation === 'left',
              }"
            ></div>
            <div
              v-show="commentImg.length > 7 && commentImgLocation === 'right'"
              class="shadowEdgeRight"
            ></div>
          </div>
        </div>
      </div>
      <div
        class="comment-footer"
        v-show="commentFocusFlag || commentLengthFlag"
        @click="(commentFocusFlag = true), commentRef.focus()"
        @mouseover="(commentFooterFlag = true), (commentHoverFlag = true)"
        @mouseleave="(commentFooterFlag = false), (commentHoverFlag = false)"
      >
        <span
          class="comment-emoji-btn"
          @click.stop="commentRef.focus(), (emojiFlag = !emojiFlag)"
        >
          <img class="comment-emoji-img" src="../img/emoji.png" />
          <div v-show="emojiFlag" class="comment-emoji-container" @click.stop>
            <span class="head-span">小黄脸</span>
            <span
              class="comment-emoji-content"
              v-for="index in 76"
              :key="index"
              @click="commentRef.focus(), addEmoji(index)"
            >
              <img :src="'../img/emoji/' + index + '.png'" />
            </span>
          </div>
        </span>
        <span class="comment-at-btn" @click="addEit()">
          <img src="../img/@.png" />
          <eit
            v-show="eitFlag"
            :eitList="store.eitList"
            :eitKeyWord="eitKeyWord"
            @click.stop
          />
        </span>
        <span class="comment-img-btn" @click="addImg">
          <input
            type="file"
            id="fileInput"
            @change="handleImg"
            multiple
            accept="image/jpeg,image/png,image/webp"
            style="display: none"
          />
          <img src="../img/img.png" />
        </span>
        <span
          class="comment-dynamic-btn"
          @click="dynamicCheckFlag = dynamicCheckFlag === 1 ? 0 : 1"
        >
          <img
            class="dynamic-check-img1"
            src="../img/投币复选框.svg"
            :class="{ dynamicCheckFlag: dynamicCheckFlag === 0 }"
          />
          <img
            class="dynamic-check-img2"
            src="../img/投币复选框选中.svg"
            :class="{ dynamicCheckFlag: dynamicCheckFlag === 1 }"
          />
          <span>同时转发到我的动态</span>
        </span>
        <span
          class="comment-publish-btn"
          :class="{
            'comment-publish-btn-disabled':
              !commentLengthFlag || !commentImg.length === 0,
          }"
          @click="publishComment"
          @mousemove="
            !commentLengthFlag || !commentImg.length === 0
              ? (commentHoverFlag = true)
              : (commentHoverFlag = false)
          "
        >
          发布
        </span>
      </div>
    </div>
    <div
      class="comment-body"
      :style="{
        transform:
          commentFocusFlag || commentLengthFlag
            ? 'translateY(-58px)'
            : 'translateY(0px)',
        }"
      >
      <div v-if="store.userId === null" class="overlay-comment"></div>
      <div v-if="store.userId === null&&store.commentNumber>1" class="limit-mask-tip" @click="store.setLoginDialogVisibleFlag(true)">登录后查看 {{ store.commentNumber }} 条评论</div>
      <div
        class="comment-list"
        :class="{commentAnimationClass: comment.comments.id === searchCommentId&&searchReplyId===null}"
        v-for="comment in store.userId!==null? commentList : commentList.slice(0,2) "
        :key="comment.comments.id"
        @mouseleave="deleteCommentHoverFlag2 = -1"
      >
        <div class="comment-item">
          <img
          @click="openHome(1,comment.userInfo.id)"
          @mouseover="delayUserInfo(comment.comments.id), selectFollow(comment)"
          @mouseleave="delayUserInfo(0)"
          class="comment-user-avatar"
          :src="comment.userInfo.avatarAddress"
        />
        <span
          @click="openHome(1,comment.userInfo.id)"
          class="comment-user-name"
          :style="{ color: comment.userInfo.grade > 4 ? '#fb7299' : '#000' }"
          @mouseover="
            delayUserInfo(-comment.comments.id), selectFollow(comment)
          "
          @mouseleave="delayUserInfo(0)"
          >{{ comment.userInfo.userName }}</span
        >
        <img
          v-if="comment"
          class="comment-user-level"
          :src="'../img/' + comment.userInfo.grade + '级.png'"
        />
        <img v-if="comment.comments.userId===store.upUserId" class="comment-up-icon" src="../img/up_pb.svg">
        <div
          v-show="
            nameOrAvatarHoverFlag === -comment.comments.id ||
            nameOrAvatarHoverFlag === comment.comments.id
          "
          class="up-user-info"
          :style="{
            transform:
              nameOrAvatarHoverFlag === comment.comments.id
                ? 'translate(0px, 0px)'
                : 'translate(55px, -15px)',
          }"
          @mouseover="nameOrAvatarHoverFlag2 = true"
          @mouseleave="(nameOrAvatarHoverFlag2 = false), delayUserInfo(0)"
        >
          <div v-if="!comment.userInfo.backgroundAddress" :style="{backgroundImage:'url(../img/主页背景图.webp)'}" class="up-user-info-bg"></div>
          <div v-else :style="{backgroundImage:`url(${comment.userInfo.backgroundAddress})`}" class="up-user-info-bg"></div>
          <div class="up-user-info-container">
            <img
              @click="openHome(1,comment.userInfo.id)"
              :src="comment.userInfo.avatarAddress"
              class="up-user-info-avatar"
            />
            <div class="up-user-info-container-header">
              <span
                @click="openHome(1,comment.userInfo.id)"
                class="up-user-info-name"
                :style="{
                  color: comment.userInfo.grade > 4 ? '#fb7299' : '#000',
                }"
                >{{ comment.userInfo.userName }}</span
              >
              <img
                v-if="comment.userInfo.gender === 1"
                src="../img/man.png"
                class="up-user-info-gender"
              />
              <img
                v-if="comment.userInfo.gender === 2"
                src="../img/man.png"
                class="up-user-info-gender"
              />
              <img
                v-if="comment.userInfo.grade"
                :src="'../img/' + comment.userInfo.grade + '级.png'"
                class="up-user-info-level"
              />
            </div>
            <div class="up-user-info-container-content">
              <span @click="openHome(8,comment.userInfo.id)" style="color: #95999f; cursor: pointer"
                ><span style="color: black">{{
                  comment.userInfo.followNumber
                }}</span>
                &nbsp;关注</span
              >
              <span @click="openHome(9,comment.userInfo.id)" style="margin-left: 25px; color: #95999f; cursor: pointer"
                ><span style="color: black">{{
                  comment.userInfo.fansNumber
                }}</span>
                &nbsp;粉丝</span
              >
              <span style="margin-left: 25px; color: #95999f; cursor: pointer"
                ><span style="color: black">{{
                  comment.userInfo.likeNumber
                }}</span>
                &nbsp;获赞</span
              >
            </div>
            <div class="up-user-info-container-introduce">
              <span class="up-user-info-introduce-content">
                {{ comment.userInfo.introduce }}
              </span>
            </div>
            <div class="up-user-info-container-footer">
              <span
                v-show="comment.userInfo.isFansFlag===0"
                class="up-user-info-container-footer-add-follow"
                @click="addFollowAxios(comment)"
                ><img
                  src="../img/加关注.png"
                  style="width: 14px; margin-right: 4px"
                />关注</span
              >
              <span
                v-show="comment.userInfo.isFansFlag===1"
                class="up-user-info-container-footer-delete-follow"
                @click="deleteFollowAxios(comment)"
                @mouseover="isDeleteFollowFlag = -comment.comments.id"
                @mouseleave="isDeleteFollowFlag = comment.comments.id"
              >
                <span v-show="isDeleteFollowFlag !== -comment.comments.id"
                  >已关注</span
                >
                <span v-show="isDeleteFollowFlag === -comment.comments.id"
                  >取消关注</span
                ></span
              >
              <span class="up-user-info-container-footer-message" @click="addDialogueF(comment.userInfo.id)">发消息</span>
            </div>
          </div>
        </div>
        <span
          class="comment-content"
          :class="{
            commentContent: comment.comments.imgAddress.split(',').length > 1,
            commentContent2: comment.comments.upFlag===1,
            commentContent3: comment.comments.upFlag===1&&comment.comments.imgAddress.split(',').length > 1,
          }"
          v-html="comment.comments.content"
        ></span>
        <span v-if="comment.comments.upFlag===1" class="up-icon">置顶</span>
        <img
          class="comment-note-img"
          :style="{left: comment.comments.upFlag===1?'115px':'79.5px'}"
          v-show="comment.comments.imgAddress.split(',').length > 1"
          src="../img/笔记.png"
        />
        <span
          v-show="comment.comments.imgAddress.split(',').length > 1"
          class="comment-img-container"
        >
          <el-image
            v-for="(commentImg, index) in comment.comments.imgAddress
              .split(',')
              .slice(0, comment.comments.imgAddress.split(',').length - 1)"
            :key="commentImg"
            style="width: 88px; height: 88px; border-radius: 5px;background-color: #f1f2f3;"
            :hide-on-click-modal="true"
            :src="commentImg"
            :zoom-rate="1.2"
            :max-scale="7"
            :min-scale="0.2"
            :preview-teleported="true"
            :preview-src-list="
              comment.comments.imgAddress
                .split(',')
                .slice(0, comment.comments.imgAddress.split(',').length - 1)
            "
            :initial-index="index"
            fit="cover"
          />
        </span>
        <img
          class="comment-id-icon"
          :src="'../img/NOImg' + (comment.userInfo.id % 10) + '.webp'"
        />
        <span
          class="comment-id-number"
          :style="{ color: commentImgNumColor[comment.userInfo.id % 10] }"
          >NO.<br />{{ comment.userInfo.id }}</span
        >
        <div
          class="comment-footer"
          :class="{
            commentFooter: comment.comments.imgAddress.split(',').length > 7,
          }"
        >
          <span class="comment-time">{{
            computedCommentTime(comment.comments.commentTime)
          }}</span>
          <span
            @click="controlsCommentAxios(comment, 1)"
            class="comment-like-number-container"
          >
            <img
              v-if="comment.commentControls?.controls !== 1"
              @mousemove="commentLikeHoverFlag = comment.comments.id,commentDislikeHoverFlag = -1"
              @mouseleave="commentLikeHoverFlag = -1"
              class="comment-like-btn"
              :src="
                commentLikeHoverFlag === comment.comments.id
                  ? '../img/评论点赞hover.png'
                  : '../img/评论点赞灰.png'
              "
            />
            <img
              v-show="comment.commentControls?.controls === 1"
              class="comment-like-btn"
              src="../img/评论点赞蓝.png"
            />
            <span
              v-show="comment.comments.likeCommentNumber > 0"
              class="comment-like-number"
              >{{ comment.comments.likeCommentNumber }}</span
            >
          </span>
          <span
            @click="controlsCommentAxios(comment, 0)"
            class="comment-dont-like-number-container"
          >
            <img
              v-if="comment.commentControls?.controls !== 0"
              @mousemove="commentDislikeHoverFlag = comment.comments.id,commentLikeHoverFlag = -1"
              @mouseleave="commentDislikeHoverFlag = -1"
              class="comment-dont-like-btn"
              :src="
                commentDislikeHoverFlag === comment.comments.id
                  ? '../img/评论踩hover.png'
                  : '../img/评论踩灰.png'
              "
            />
            <img
              v-show="comment.commentControls?.controls === 0"
              class="comment-dont-like-btn"
              src="../img/评论踩蓝.png"
            />
          </span>
          <span class="comment-reply-btn" @click="replyCommentF(comment,comment)"
            >回复</span
          >
          <span
            v-if="store.userId === comment.userInfo.id || store.userId===store.selectUpVideo?.upVideo?.userId"
            class="comment-delete-btn-container"
            @mouseover="deleteCommentHoverFlag = comment.comments.id"
            @mouseleave="deleteCommentHoverFlag = -1"
            @click="deleteCommentHoverFlag2 = comment.comments.id"
          >
            <img
              :src="
                deleteCommentHoverFlag === comment.comments.id
                  ? '../img/弹幕列表蓝.png'
                  : '../img/弹幕列表.png'
              "
            />
           <div @mouseleave="deleteCommentHoverFlag2 = -1">
            <span
              v-show="deleteCommentHoverFlag2 === comment.comments.id"
              class="delete-btn"
              @click="
                (deleteDialogVisible = true), (commentId = comment.comments.id)
              "
              >删除</span>
              <span
              v-if="deleteCommentHoverFlag2 === comment.comments.id && comment.comments.upFlag===0  && store.userId === store.upUserId"
              class="up-btn"
              @click="putUpStatusAxios(comment.comments.id,0)"
              >设为置顶</span>
              <span
              v-if="deleteCommentHoverFlag2 === comment.comments.id && comment.comments.upFlag===1  && store.userId === store.upUserId"
              class="up-btn"
              @click="putUpStatusAxios(comment.comments.id,1)"
              >取消置顶</span>
           </div>
          </span>
        </div>
        <div v-if="comment.upLikeFlag" class="up-like-comment" :style="{marginBottom: comment.replyFlag ? '0px' : '5px'}">UP主觉得很赞</div>
        <div v-show="comment.userInfo?.isFansFlag === 1 && comment.userInfo?.isFollowFlag === 1" class="up-like-comment" :style="{marginBottom: comment.replyFlag ? '0px' : '5px'}">互相关注</div>
        <div v-if="comment.replyNumber>0&&(!comment.replyFlag||comment.replyFlag2)" class="view-reply-btn">
            共 {{ comment.replyNumber }} 条回复，<button
              @click="selectReplyComment(comment,1)"
            >
              点击查看
            </button>
        </div>
        <div
            v-show="replyComment === comment.comments.id"
            :style="{top: comment.replyFlag&&replyComment!==comment.comments.id ? '0px' : '15px'}"
            class="reply-container"
          >
            <img
              class="reply-avatar"
              :src="store.userInformation?.avatarAddress"
            />
            <span
              class="comment-placeholder"
              v-show="store.userId !== null && replyPlaceholderFlag"
              >回复 @{{ comment.userInfo.userName }} :</span
            >
            <div
              class="comment-content-container"
              :class="{
                commentImgFiles1: commentImg2.length > 0,
              }"
            >
              <div
                v-show="store.userId !== null"
                :id="'reply' + comment.comments.id"
                class="reply-commit-comment-container"
                contenteditable="true"
                @click="handleContent2"
                @focus="handleContent2"
                @blur="getAfterBlurIndex2"
                @input="handleContent2"
                @keyup.left="handleContent2"
                @keyup.right="handleContent2"
                @keyup.up="handleContent2"
                @keyup.down="handleContent2"
              ></div>
              <div
                v-show="commentImg2.length > 0"
                :id="'replyImg' + comment.comments.id"
                class="comment-img-container1"
              >
                <div v-for="(file,index) in commentImg2" :key="file">
                  <span
                    v-show="file === null || file === undefined"
                    class="loading-span"
                  >
                    <span @click="deleteCommentImg2(index)"
                      ><img src="../img/关闭视频定位.png"
                    /></span>
                    <img src="../img/loading-blue.gif" />
                  </span>
                  <span
                    v-show="file !== undefined && file !== null"
                    class="loading-before-span"
                  >
                    <span @click="deleteCommentImg2(index)"
                      ><img src="../img/关闭视频定位.png"
                    /></span>
                    <img :src="file" />
                  </span>
                </div>
                <img
                  class="left"
                  :class="{
                    left2: commentImg2.length === 8,
                    left3: commentImg2.length === 7,
                  }"
                  v-show="
                    commentImgLocation2 === 'left' && commentImg2.length > 6
                  "
                  @click="postionImg2('left', comment.comments.id)"
                  src="../img/图片向左按钮.png"
                />
                <img
                  class="right"
                  v-show="
                    commentImgLocation2 === 'right' && commentImg2.length > 6
                  "
                  @click="postionImg2('right', comment.comments.id)"
                  src="../img/图片向右按钮.png"
                />
                <div
                  v-show="
                    commentImg2.length > 6 && commentImgLocation2 === 'left'
                  "
                  class="shadowEdgeLeft"
                  :class="{
                    shadowEdgeLeft2:
                      commentImg2.length === 8 &&
                      commentImgLocation2 === 'left',
                    shadowEdgeLeft3:
                      commentImg2.length === 7 &&
                      commentImgLocation2 === 'left',
                  }"
                ></div>
                <div
                  v-show="
                    commentImg2.length > 6 && commentImgLocation2 === 'right'
                  "
                  class="shadowEdgeRight"
                ></div>
              </div>
            </div>
            <div
              class="comment-footer"
              @mouseover="commentFooterFlag = true"
              @mouseleave="commentFooterFlag = false"
            >
              <span
                class="comment-emoji-btn"
                @click.stop="emojiFlag2 = !emojiFlag2"
              >
                <img class="comment-emoji-img" src="../img/emoji.png" />
                <div
                  v-show="emojiFlag2"
                  class="comment-emoji-container"
                  @click.stop
                >
                  <span class="head-span">小黄脸</span>
                  <span
                    class="comment-emoji-content"
                    v-for="index in 76"
                    :key="index"
                    @click="addEmoji2(index)"
                  >
                    <img :src="'../img/emoji/' + index + '.png'" />
                  </span>
                </div>
              </span>
              <span class="comment-at-btn" @click="addEit2()">
                <img src="../img/@.png" />
                <eit
                  v-show="eitFlag2"
                  :eitList="store.eitList"
                  :eitKeyWord="eitKeyWord"
                  @click.stop
                />
              </span>
              <span class="comment-img-btn" @click="addImg2">
                <input
                  type="file"
                  id="fileInput2"
                  @change="handleImg2"
                  multiple
                  accept="image/jpeg,image/png,image/webp"
                  style="display: none"
                />
                <img src="../img/img.png" />
              </span>
              <span
                class="comment-dynamic-btn"
                @click="dynamicCheckFlag2 = dynamicCheckFlag2 === 1 ? 0 : 1"
              >
                <img
                  class="dynamic-check-img1"
                  src="../img/投币复选框.svg"
                  :class="{ dynamicCheckFlag: dynamicCheckFlag2 === 0 }"
                />
                <img
                  class="dynamic-check-img2"
                  src="../img/投币复选框选中.svg"
                  :class="{ dynamicCheckFlag: dynamicCheckFlag2 === 1 }"
                />
                <span>同时转发到我的动态</span>
              </span>
              <span
                class="comment-publish-btn"
                :class="{
                  'comment-publish-btn-disabled':
                    replyPlaceholderFlag || !commentImg2.length === 0,
                }"
                @click="publishComment2"
              >
                发布
              </span>
            </div>
        </div>
        </div>
        <div
            v-show="comment.replyCommentList.length>0&&comment.replyFlag"
            class="comment-list"
            v-for="(remark,index) in comment.replyCommentList"
             :style="{marginBottom: comment.replyFlag&&remark.comments.imgAddress.split(',').length>1? '40px' : '8px'}"
            :class="{ replyCommentClass: comment.replyFlag,replyCommentClass2: remark.comments.imgAddress.split(',').length>1&&comment.replyFlag,commentAnimationClass: remark.comments.id === searchReplyId}"
            :key="remark.comments.id"
            @mouseleave="deleteCommentHoverFlag2 = -1"
          >
           <div class="comment-item">
            <img
              @click="openHome(1,remark.userInfo.id)"
              @mouseover="
                delayUserInfo(remark.comments.id), selectFollow(remark)
              "
              @mouseleave="delayUserInfo(0)"
              class="comment-user-avatar"
              :src="remark.userInfo.avatarAddress"
            />
           <div class="reply-content" >
            <span
               @click="openHome(1,remark.userInfo.id)"
              :id="'replyName'+remark.comments.id"
              class="comment-user-name"
              :style="{ color: remark.userInfo.grade > 4 ? '#fb7299' : '#000' }"
              @mouseover="
                delayUserInfo(-remark.comments.id), selectFollow(remark)
              "
              @mouseleave="delayUserInfo(0)"
              >{{ remark.userInfo.userName }}</span
            >
            <div
          v-show="
            (nameOrAvatarHoverFlag === -remark.comments.id ||
            nameOrAvatarHoverFlag === remark.comments.id)&&(remark.userInfo.isFansFlag===0||remark.userInfo.isFansFlag===1)
          "
          class="up-user-info2"
          :style="{
            transform:
              nameOrAvatarHoverFlag === remark.comments.id
                ? 'translate(0px, 0px)'
                : 'translate(40px, -5px)',
          }"
          @mouseover="nameOrAvatarHoverFlag2 = true"
          @mouseleave="(nameOrAvatarHoverFlag2 = false), delayUserInfo(0)"
        >
          <div v-if="!remark.userInfo.backgroundAddress" :style="{backgroundImage:'url(../img/主页背景图.webp)'}" class="up-user-info-bg"></div>
          <div v-else :style="{backgroundImage:`url(${remark.userInfo.backgroundAddress})`}" class="up-user-info-bg"></div>
          <div class="up-user-info-container">
            <img
              @click="openHome(1,remark.userInfo.id)"
              :src="remark.userInfo.avatarAddress"
              class="up-user-info-avatar"
            />
            <div class="up-user-info-container-header">
              <span
                @click="openHome(1,remark.userInfo.id)"
                class="up-user-info-name"
                :style="{
                  color: remark.userInfo.grade > 4 ? '#fb7299' : '#000',
                }"
                >{{ remark.userInfo.userName }}</span
              >
              <img
                v-if="remark.userInfo.gender === 1"
                src="../img/man.png"
                class="up-user-info-gender"
              />
              <img
                v-if="remark.userInfo.gender === 2"
                src="../img/man.png"
                class="up-user-info-gender"
              />
              <img
                v-if="remark.userInfo.grade"
                :src="'../img/' + remark.userInfo.grade + '级.png'"
                class="up-user-info-level"
              />
            </div>
            <div class="up-user-info-container-content">
              <span @click="openHome(8,remark.userInfo.id)" style="color: #95999f; cursor: pointer"
                ><span style="color: black">{{
                  remark.userInfo.followNumber
                }}</span>
                &nbsp;关注</span
              >
              <span @click="openHome(9,remark.userInfo.id)" style="margin-left: 25px; color: #95999f; cursor: pointer"
                ><span style="color: black">{{
                  remark.userInfo.fansNumber
                }}</span>
                &nbsp;粉丝</span
              >
              <span style="margin-left: 25px; color: #95999f; cursor: pointer"
                ><span style="color: black">{{
                  remark.userInfo.likeNumber
                }}</span>
                &nbsp;获赞</span
              >
            </div>
            <div class="up-user-info-container-introduce">
              <span class="up-user-info-introduce-content">
                {{ remark.userInfo.introduce }}
              </span>
            </div>
            <div class="up-user-info-container-footer">
              <span
                v-show="remark.userInfo.isFansFlag===0"
                class="up-user-info-container-footer-add-follow"
                @click="addFollowAxios(remark)"
                ><img
                  src="../img/加关注.png"
                  style="width: 14px; margin-right: 4px"
                />关注</span
              >
              <span
                v-show="remark.userInfo.isFansFlag===1"
                class="up-user-info-container-footer-delete-follow"
                @click="deleteFollowAxios(remark)"
                @mouseover="isDeleteFollowFlag = -remark.comments.id"
                @mouseleave="isDeleteFollowFlag = remark.comments.id"
              >
                <span v-show="isDeleteFollowFlag !== -remark.comments.id"
                  >已关注</span
                >
                <span v-show="isDeleteFollowFlag === -remark.comments.id"
                  >取消关注</span
                ></span
              >
              <span class="up-user-info-container-footer-message" @click="addDialogueF(remark.userInfo.id)">发消息</span>
            </div>
          </div>
           </div>
            <img
              v-if="remark"
              class="comment-user-level"
              :src="'../img/' + remark.userInfo.grade + '级.png'"
            />
            <img v-if="remark.comments.userId===store.upUserId&&remark.comments.imgAddress.split(',').length>1" class="comment-up-icon" src="../img/up_pb.svg">
            <span
              class="comment-content"
              :class="{
                commentContent:
                  remark.comments.imgAddress.split(',').length > 1,
              }"
              :style="{textIndent: remark.comments.imgAddress.split(',').length < 2? getUserNameIndent(remark.comments.userName)+3+'em' : '3.8em'}"
              v-html="remark.comments.content"
            ></span>
            <img
              class="comment-note-img"
              v-show="remark.comments.imgAddress.split(',').length > 1"
              src="../img/笔记.png"
            />
            <span
              v-show="remark.comments.imgAddress.split(',').length > 1"
              class="comment-img-container"
            >
              <el-image
                v-for="(remarkImg, index) in remark.comments.imgAddress
                  .split(',')
                  .slice(0, remark.comments.imgAddress.split(',').length - 1)"
                :key="remarkImg"
                style="width: 88px; height: 88px; border-radius: 5px;background-color: #f1f2f3;"
                :hide-on-click-modal="true"
                :src="remarkImg"
                :zoom-rate="1.2"
                :max-scale="7"
                :min-scale="0.2"
                :preview-teleported="true"
                :preview-src-list="
                  remark.comments.imgAddress
                    .split(',')
                    .slice(0, remark.comments.imgAddress.split(',').length - 1)
                "
                :initial-index="index"
                fit="cover"
              />
            </span>
            <div
              class="comment-footer"
              :class="{
                commentFooter: remark.comments.imgAddress.split(',').length > 7,
              }"
              :style="{top:  remark.comments.imgAddress.split(',').length <2 ? '-7px' : '28px'}"
            >
              <span class="comment-time">{{
                computedCommentTime(remark.comments.commentTime)
              }}</span>
              <span
                @click="controlsCommentAxios(remark, 1)"
                class="comment-like-number-container"
              >
                <img
                  v-if="remark.commentControls?.controls !== 1"
                  @mousemove="commentLikeHoverFlag = remark.comments.id,commentDislikeHoverFlag = -1"
                  @mouseleave="commentLikeHoverFlag = -1"
                  class="comment-like-btn"
                  :src="
                    commentLikeHoverFlag === remark.comments.id
                      ? '../img/评论点赞hover.png'
                      : '../img/评论点赞灰.png'
                  "
                />
                <img
                  v-show="remark.commentControls?.controls === 1"
                  class="comment-like-btn"
                  src="../img/评论点赞蓝.png"
                />
                <span
                  v-show="remark.comments.likeCommentNumber > 0"
                  class="comment-like-number"
                  >{{ remark.comments.likeCommentNumber }}</span
                >
              </span>
              <span
                @click="controlsCommentAxios(remark, 0)"
                class="comment-dont-like-number-container"
              >
                <img
                  v-if="remark.commentControls?.controls !== 0"
                  @mousemove="commentDislikeHoverFlag = remark.comments.id,commentLikeHoverFlag = -1"
                  @mouseleave="commentDislikeHoverFlag = -1"
                  class="comment-dont-like-btn"
                  :src="
                    commentDislikeHoverFlag === remark.comments.id
                      ? '../img/评论踩hover.png'
                      : '../img/评论踩灰.png'
                  "
                />
                <img
                  v-show="remark.commentControls?.controls === 0"
                  class="comment-dont-like-btn"
                  src="../img/评论踩蓝.png"
                />
              </span>
              <span class="comment-reply-btn" @click="replyCommentF(remark,comment)"
                >回复</span
              >
              <span
                v-if="store.userId === remark.userInfo.id || store.userId===store.selectUpVideo?.upVideo?.userId"
                class="comment-delete-btn-container"
                @mouseover="deleteCommentHoverFlag = remark.comments.id"
                @mouseleave="deleteCommentHoverFlag = -1"
                @click="deleteCommentHoverFlag2 = remark.comments.id"
              >
                <img
                  :src="
                    deleteCommentHoverFlag === remark.comments.id
                      ? '../img/弹幕列表蓝.png'
                      : '../img/弹幕列表.png'
                  "
                />
                <span
                  v-show="deleteCommentHoverFlag2 === remark.comments.id"
                  class="delete-btn"
                  @click="
                    (deleteDialogVisible = true),
                      (commentId = remark.comments.id),
                      (mainCommentId = comment.comments.id)
                  "
                  @mouseleave="deleteCommentHoverFlag2 = -1"
                  >删除</span
                >
              </span>
            </div>
            <div
            v-show="replyComment === remark.comments.id"
            class="reply-container"
            style="left:-33px;"
          >
            <img
              class="reply-avatar"
              :src="store.userInformation?.avatarAddress"
            />
            <span
              class="comment-placeholder"
              v-show="store.userId !== null && replyPlaceholderFlag"
              >回复 @{{ remark.userInfo.userName }} :</span
            >
            <div
              class="comment-content-container"
              :class="{
                commentImgFiles1: commentImg2.length > 0,
              }"
            >
              <div
                v-show="store.userId !== null"
                :id="'reply' + remark.comments.id"
                class="reply-commit-comment-container"
                contenteditable="true"
                @click="handleContent2"
                @focus="handleContent2"
                @blur="getAfterBlurIndex2"
                @input="handleContent2"
                @keyup.left="handleContent2"
                @keyup.right="handleContent2"
                @keyup.up="handleContent2"
                @keyup.down="handleContent2"
              ></div>
              <div
                v-show="commentImg2.length > 0"
                :id="'replyImg' + remark.comments.id"
                class="comment-img-container1"
              >
                <div v-for="(file,index) in commentImg2" :key="file">
                  <span
                    v-show="file === null || file === undefined"
                    class="loading-span"
                  >
                    <span @click="deleteCommentImg2(index)"
                      ><img src="../img/关闭视频定位.png"
                    /></span>
                    <img src="../img/loading-blue.gif" />
                  </span>
                  <span
                    v-show="file !== undefined && file !== null"
                    class="loading-before-span"
                  >
                    <span @click="deleteCommentImg2(index)"
                      ><img src="../img/关闭视频定位.png"
                    /></span>
                    <img :src="file" />
                  </span>
                </div>
                <img
                  class="left"
                  :class="{
                    left2: commentImg2.length === 8,
                    left3: commentImg2.length === 7,
                  }"
                  v-show="
                    commentImgLocation2 === 'left' && commentImg2.length > 6
                  "
                  @click="postionImg2('left', remark.comments.id)"
                  src="../img/图片向左按钮.png"
                />
                <img
                  class="right"
                  v-show="
                    commentImgLocation2 === 'right' && commentImg2.length > 6
                  "
                  @click="postionImg2('right', remark.comments.id)"
                  src="../img/图片向右按钮.png"
                />
                <div
                  v-show="
                    commentImg2.length > 6 && commentImgLocation2 === 'left'
                  "
                  class="shadowEdgeLeft"
                  :class="{
                    shadowEdgeLeft2:
                      commentImg2.length === 8 &&
                      commentImgLocation2 === 'left',
                    shadowEdgeLeft3:
                      commentImg2.length === 7 &&
                      commentImgLocation2 === 'left',
                  }"
                ></div>
                <div
                  v-show="
                    commentImg2.length > 6 && commentImgLocation2 === 'right'
                  "
                  class="shadowEdgeRight"
                ></div>
              </div>
            </div>
            <div
               :style="{marginBottom: index===comment.replyCommentList.length-1? '30px' : '0px'}"
              class="comment-footer"
              @mouseover="commentFooterFlag = true"
              @mouseleave="commentFooterFlag = false"
            >
              <span
                class="comment-emoji-btn"
                @click.stop="emojiFlag2 = !emojiFlag2"
              >
                <img class="comment-emoji-img" src="../img/emoji.png" />
                <div
                  v-show="emojiFlag2"
                  class="comment-emoji-container"
                  @click.stop
                >
                  <span class="head-span">小黄脸</span>
                  <span
                    class="comment-emoji-content"
                    v-for="index in 76"
                    :key="index"
                    @click="addEmoji2(index)"
                  >
                    <img :src="'../img/emoji/' + index + '.png'" />
                  </span>
                </div>
              </span>
              <span class="comment-at-btn" @click="addEit2()">
                <img src="../img/@.png" />
                <eit
                  v-show="eitFlag2"
                  :eitList="store.eitList"
                  :eitKeyWord="eitKeyWord"
                  @click.stop
                />
              </span>
              <span class="comment-img-btn" @click="addImg2">
                <input
                  type="file"
                  id="fileInput2"
                  @change="handleImg2"
                  multiple
                  accept="image/jpeg,image/png,image/webp"
                  style="display: none"
                />
                <img src="../img/img.png" />
              </span>
              <span
                class="comment-dynamic-btn"
                @click="dynamicCheckFlag2 = dynamicCheckFlag2 === 1 ? 0 : 1"
              >
                <img
                  class="dynamic-check-img1"
                  src="../img/投币复选框.svg"
                  :class="{ dynamicCheckFlag: dynamicCheckFlag2 === 0 }"
                />
                <img
                  class="dynamic-check-img2"
                  src="../img/投币复选框选中.svg"
                  :class="{ dynamicCheckFlag: dynamicCheckFlag2 === 1 }"
                />
                <span>同时转发到我的动态</span>
              </span>
              <span
                class="comment-publish-btn"
                :class="{
                  'comment-publish-btn-disabled':
                    replyPlaceholderFlag || !commentImg2.length === 0,
                }"
                @click="publishComment2"
              >
                发布
              </span>
            </div>
            </div>
           </div>
           </div>
        </div>
        <div v-if="comment.replyFlag&&!comment.replyFlag2&&comment.replyNumber>10" class="reply-page-container">
          <span class="page-sum">共{{ Math.ceil(comment.replyNumber/10) }}页</span>  
          <span v-show="comment.page&&comment.page>1" class="page-prev" @click="moreReplyComment(comment,comment.page-1)">上一页</span>
          <span :style="{color: comment.page === index ? '#00aeec' : '#000'}" @click="moreReplyComment(comment,index)" class="page-num" v-for="index in Math.ceil(comment.replyNumber/10)" :key="index">{{ index }}</span>
          <span v-show="comment.page&&comment.page<Math.ceil(comment.replyNumber/10)" class="page-next" @click="moreReplyComment(comment,comment.page+1)">下一页</span>
          <span @click="comment.replyFlag=false" class="page-show">收起</span> 
        </div>
        <div class="comment-line"></div>
      </div>
    </div>
    <div v-if="(scrollFooterFlag&&store.userId!==null)||store.commentNumber===0" class="comment-footer-info">没有更多评论</div>
    <div v-show="scrollInputCommentFlag>920&&store.userId!==null" class="comment-input-container2">
      <img
        @click="openHome(1,store.userId)"
        class="comment-avatar"
        v-show="store.userId !== null"
        :src="store.userInformation?.avatarAddress"
      />
      <div
        style="width: 0px"
        @click="(commentFocusFlag = true), commentRef2.focus()"
        @mousemove="commentHoverFlag = true"
        @mouseleave="commentHoverFlag = false"
      >
        <span
          class="comment-placeholder"
          v-show="store.userId !== null && !commentLengthFlag"
          @mouseover="
            (commentPlaceholderHoverFlag = true), (commentHoverFlag = true)
          "
          @mouseout="
            (commentPlaceholderHoverFlag = false), (commentHoverFlag = false)
          "
          >{{ commentPlaceholderr }}</span
        >
        <div
          class="comment-content-container"
          :class="{
            commentImgFiles1: commentImg.length > 0,
            commentFocusFlag: commentFocusFlag || commentLengthFlag,
          }"
        >
          <div
            v-show="store.userId !== null"
            ref="commentRef2"
            class="login-commit-comment-container"
            contenteditable
            :class="{
              commentPlaceholderHoverFlag: commentPlaceholderHoverFlag,
            }"
            @click="commentRef2.focus(), handleContent()"
            @focus="handleContent(),commentFocus2Flag=true"
            @blur="getAfterBlurIndex(),commentFocus2Flag=false"
            @input="handleContent"
            @keyup.left="handleContent"
            @keyup.right="handleContent"
            @keyup.up="handleContent"
            @keyup.down="handleContent"
            @mousemove="commentHoverFlag = true"
            @mouseleave="commentHoverFlag = false"
          ></div>
          <div v-show="commentImg.length > 0" class="comment-img-container" id="commentImg">
            <div v-for="(file,index) in commentImg" :key="file">
              <span
                v-show="file === null || file === undefined"
                class="loading-span"
              >
                <span @click="deleteCommentImg(index)"
                  ><img src="../img/关闭视频定位.png"
                /></span>
                <img src="../img/loading-blue.gif" />
              </span>
              <span
                v-show="file !== undefined && file !== null"
                class="loading-before-span"
              >
                <span @click="deleteCommentImg(index)"
                  ><img src="../img/关闭视频定位.png"
                /></span>
                <img :src="file" />
              </span>
            </div>
            <img
              class="left"
              :class="{ left2: commentImg.length === 8 }"
              v-show="commentImgLocation === 'left' && commentImg.length > 7"
              @click="postionImg('left')"
              src="../img/图片向左按钮.png"
            />
            <img
              class="right"
              v-show="commentImgLocation === 'right' && commentImg.length > 7"
              @click="postionImg('right')"
              src="../img/图片向右按钮.png"
            />
            <div
              v-show="commentImg.length > 7 && commentImgLocation === 'left'"
              class="shadowEdgeLeft"
              :class="{
                shadowEdgeLeft2:
                  commentImg.length === 8 && commentImgLocation === 'left',
              }"
            ></div>
            <div
              v-show="commentImg.length > 7 && commentImgLocation === 'right'"
              class="shadowEdgeRight"
            ></div>
          </div>
        </div>
      </div>
      <div
        class="comment-footer"
        v-show="commentFocusFlag || commentLengthFlag"
        @click="(commentFocusFlag = true), commentRef2.focus()"
        @mouseover="(commentFooterFlag = true), (commentHoverFlag = true)"
        @mouseleave="(commentFooterFlag = false), (commentHoverFlag = false)"
      >
        <span
          class="comment-emoji-btn"
          @click.stop="commentRef2.focus(), (emojiFlag3 = !emojiFlag3)"
        >
          <img class="comment-emoji-img" src="../img/emoji.png" />
          <div v-show="emojiFlag3" class="comment-emoji-container"
          :style="{top: commentImg.length>0 ? -424-inputTopValue+'px' : -345-inputTopValue+'px'}"
          @click.stop>
            <span class="head-span">小黄脸</span>
            <span
              class="comment-emoji-content"
              v-for="index in 76"
              :key="index"
              @click="commentRef2.focus(), addEmoji(index)"
            >
              <img :src="'../img/emoji/' + index + '.png'" />
            </span>
          </div>
        </span>
        <span class="comment-at-btn" @click="addEit()">
          <img src="../img/@.png" />
          <eit
            v-show="eitFlag"
            :style="{top: commentImg.length>0 ? -356-inputTopValue+'px' : -277-inputTopValue+'px'}"
            :eitList="store.eitList"
            :eitKeyWord="eitKeyWord"
            @click.stop
          />
        </span>
        <span class="comment-img-btn" @click="addImg3">
          <input
            type="file"
            id="fileInput3"
            @change="handleImg"
            multiple
            accept="image/jpeg,image/png,image/webp"
            style="display: none"
          />
          <img src="../img/img.png" />
        </span>
        <span
          class="comment-dynamic-btn"
          @click="dynamicCheckFlag = dynamicCheckFlag === 1 ? 0 : 1"
        >
          <img
            class="dynamic-check-img1"
            src="../img/投币复选框.svg"
            :class="{ dynamicCheckFlag: dynamicCheckFlag === 0 }"
          />
          <img
            class="dynamic-check-img2"
            src="../img/投币复选框选中.svg"
            :class="{ dynamicCheckFlag: dynamicCheckFlag === 1 }"
          />
          <span>同时转发到我的动态</span>
        </span>
        <span
          class="comment-publish-btn"
          :class="{
            'comment-publish-btn-disabled':
              !commentLengthFlag || !commentImg.length === 0,
          }"
          @click="publishComment"
          @mousemove="
            !commentLengthFlag || !commentImg.length === 0
              ? (commentHoverFlag = true)
              : (commentHoverFlag = false)
          "
        >
          发布
        </span>
      </div>
    </div>
    <user-info  :style="{left:eitUserInfoLeft+'px',top:eitUserInfoTop+'px'}"/>
    <el-dialog
      v-model="deleteDialogVisible"
      append-to-body
      width="320"
      :lock-scroll="false"
      align-center
      style="height: 188px; border-radius: 8px"
    >
      <div class="delete-dialog-title">删除评论</div>
      <div class="delete-dialog-content">
        删除评论后，评论下所有回复都会被删除,是否继续?
      </div>
      <button
        class="delete-dialog-cancel-btn"
        @click="deleteDialogVisible = false"
      >
        取消
      </button>
      <button class="delete-dialog-confirm-btn" @click="deleteCommentAxios">
        确定
      </button>
    </el-dialog>
  </div>
</template>

<script>
import { onMounted, onUnmounted, reactive, ref, watch } from "vue";
import eit from "../components/eit";
import { ElMessage } from "element-plus";
import apiClient from "../services/apiClient";
import { useGlobalStore } from "../store/store";
import userInfo from "../utils/userInfo";
import {addDialogue} from '../api/privateMessage/index';
import {putUpStatus,selectOneComment} from "@/api/comment/index"
export default {
  name: "Comment",
  components: {
    eit,
    userInfo,
  },
  setup() {
    const store = useGlobalStore();
    const commentSortFlag = ref(1); //1:最热 2:最新
    const commentPlaceholderHoverFlag = ref(false);
    const commentFocusFlag = ref(false);
    const commentFocus2Flag = ref(false);
    const commentRef = ref(null);
    const commentRef2 = ref(null);
    const commentPlaceholder = reactive([
      "下面我简单喵两句",
      "与其赞同别人的话语，不如自己畅所欲言",
      "轻轻敲响沉睡的心灵，让我看看你的点评",
      "这里是评论区，不是无人区;-)",
      "万水千山总是情，评论两句行不行",
      "天青色等烟雨，评论区在等你",
      "尊重是评论打动人心的入场券",
      "这里需要一条查重率0%的评论",
      "哎呀，不错哦，发条评论吧",
    ]);
    const commentPlaceholderr = ref("");
    const commentContent = ref("");
    const commentContent2 = ref("");
    const dynamicCheckFlag = ref(0);
    const dynamicCheckFlag2 = ref(0);
    const eitKeyWord = ref(" ");
    const commentFooterFlag = ref(false);
    const commentLengthFlag = ref(false);
    const eitFlag = ref(false);
    const eitFlag2 = ref(false);
    const commentHoverFlag = ref(false);
    const commentImgFiles = reactive([]);
    const commentImgFiles2 = reactive([]);
    const commentImg = reactive([]);
    const commentImg2 = reactive([]);
    const commentImgLocation = ref("right");
    const commentImgLocation2 = ref("right");
    const emojiFlag = ref(false);
    const emojiFlag2 = ref(false);
    const emojiFlag3 = ref(false);
    const commentImgNumColor = ref([
      "rgb(241 144 55)",
      "rgb(226, 157, 47)",
      "rgb(184, 199, 208)",
      "rgb(7, 182, 213)",
      "rgb(184, 199, 208)",
      "rgb(184, 199, 208)",
      "rgb(61, 197, 236)",
      "rgb(246, 126, 149)",
      "rgb(68, 174, 159)",
      "rgb(236, 61, 61)",
    ]);
    const commentLikeHoverFlag = ref(-1);
    const commentDislikeHoverFlag = ref(-1);
    const nameOrAvatarHoverFlag = ref(0);
    const deleteCommentHoverFlag = ref(-1);
    const deleteCommentHoverFlag2 = ref(-1);
    const commentList = reactive([]);
    const commentId = ref(null);
    const mainCommentId = ref(null);
    const deleteDialogVisible = ref(false);
    const isDeleteFollowFlag = ref(0);
    const pageNum = ref(1);
    const replyComment = ref(null);
    const mainComment = ref(null);
    const replyUserName=ref(null);
    const replyUserId=ref(null);
    const replyPlaceholderFlag = ref(true);
    const eitUserInfoLeft = ref(0);
    const eitUserInfoTop = ref(0);
    const scrollInputCommentFlag = ref(false);
    const scrollFooterFlag = ref(false);
    const searchCommentId=ref(parseInt(new URLSearchParams(location.search).get("commentId"))||null);
    const searchReplyId=ref(parseInt(new URLSearchParams(location.search).get("replyId"))||null);
    const videoId = new URLSearchParams(window.location.search).get("videoId").replace("BV", "");
    const loading=ref(false);

    onMounted(() => {
      commentPlaceholderr.value =
        commentPlaceholder[
          Math.floor(Math.random() * commentPlaceholder.length)
        ];
      window.addEventListener("click", handleClickOutside);
      window.addEventListener("keyup", deleteEit);
      window.addEventListener("mouseover", handleMouseover);
      window.addEventListener("scroll", handleScroll1);
    });

    onUnmounted(() => {
      window.removeEventListener("click", handleClickOutside);
      window.removeEventListener("scroll", handleScroll1);
      window.removeEventListener("keyup", deleteEit);
      window.removeEventListener("mouseover", handleMouseover);
    });

    //下拉增加评论
    const handleScroll1 = () => {

      if(scrollFooterFlag.value){
        return;
      }

      const scrollHeight = document.documentElement.scrollHeight; // 文档总高度
      const scrollTop = window.scrollY || document.documentElement.scrollTop; // 当前滚动高度
      scrollInputCommentFlag.value=scrollTop;
      if(scrollInputCommentFlag.value<920)
        emojiFlag3.value=false;
      
      const clientHeight =
        window.innerHeight || document.documentElement.clientHeight; // 可视区域高度

      if (scrollTop + clientHeight + 400 >= scrollHeight) {
        if(store.userId!==null||pageNum.value===1)
        {
          selectComment();
        }
      }
    };

    function handleClickOutside(event) {
      if (!commentHoverFlag.value) commentFocusFlag.value = false;
      //解决表情中的光标问题
      if (event.target.classList.contains("comment-emoji-img")) {
        const selection = window.getSelection();
        const range = document.createRange();

        // 找到图片的上一个兄弟节点
        const previousNode = event.target.previousSibling;

        // 如果上一个兄弟节点存在
        if (previousNode) {
          range.setStartAfter(previousNode); // 将光标设置到上一个节点之后
          range.setEndAfter(previousNode);
        } else {
          // 如果没有上一个节点，光标设置到图片前
          range.setStartAfter(event.target);
          range.setEndAfter(event.target);
        }
        selection.removeAllRanges();
        selection.addRange(range);
      }
    }

    const inputTopValue=ref(0);
    // 输入评论
    function handleContent() {
      let inputDom=null;
      if(!commentFocus2Flag.value)
       {inputDom = document.querySelector(
        ".login-commit-comment-container",
      );}else
       inputDom = commentRef2.value;


      if (inputDom.innerText.length > 2000)
        inputDom.innerText = inputDom.innerText.slice(0, 2000);
      if (
        inputDom.innerText.trim().length > 0 ||
        inputDom.innerHTML.includes('<img src="../img/emoji/')
      ) {
        commentContent.value = inputDom.innerHTML;
        commentLengthFlag.value = true;
      } else {
        commentContent.value = "";
        commentLengthFlag.value = false;
      }

      // 获取当前选区和光标位置
      const selection = window.getSelection();
      if (!selection.rangeCount) return; // 如果没有选区，直接返回

      const range = selection.getRangeAt(0);
      if (range.endContainer.parentNode.className === "at-msg") return;
      // 获取光标前的所有文本
      const textBeforeCursor = getTextBeforeCursor(range);

      // 检测是否包含 @ 并提取关键字
      const atIndex = textBeforeCursor.lastIndexOf("@");
      if (atIndex !== -1) {
        const text = textBeforeCursor.substring(atIndex);
        if (text.length > 20 || text.includes(" ")) {
          eitFlag.value = false;
          return;
        }
        eitFlag.value = true;
        eitKeyWord.value = text.substring(1).trim();
      } else {
        eitFlag.value = false;
      }
    }

    watch(commentContent,()=>{
      if(commentFocus2Flag.value)
      commentRef.value.innerHTML = commentContent.value;
      else
      commentRef2.value.innerHTML = commentContent.value;

    })
    
    // 输入评论
    function handleContent2() {
      const inputDom = document.getElementById("reply" + replyComment.value);
      if (inputDom.innerText.length > 2000)
        inputDom.innerText = inputDom.innerText.slice(0, 2000);
      if (
        inputDom.innerText.trim().length > 0 ||
        inputDom.innerHTML.includes('<img src="../img/emoji/')
      ) {
        commentContent2.value = inputDom.innerHTML;
        replyPlaceholderFlag.value = false;
      } else {
        commentContent2.value = "";
        replyPlaceholderFlag.value = true;
      }

      // 获取当前选区和光标位置
      const selection = window.getSelection();
      if (!selection.rangeCount) return; // 如果没有选区，直接返回

      const range = selection.getRangeAt(0);
      if (range.endContainer.parentNode.className === "at-msg") return;
      // 获取光标前的所有文本
      const textBeforeCursor = getTextBeforeCursor(range);

      // 检测是否包含 @ 并提取关键字
      const atIndex = textBeforeCursor.lastIndexOf("@");
      if (atIndex !== -1) {
        const text = textBeforeCursor.substring(atIndex);
        if (text.length > 20 || text.includes(" ")) {
          eitFlag2.value = false;
          return;
        }
        eitFlag2.value = true;
        eitKeyWord.value = text.substring(1).trim();
      } else {
        eitFlag2.value = false;
      }
    }

    let range = null;
    // 聊天输入框失焦时获取失焦前的光标位置
    function getAfterBlurIndex() {
      if (window.getSelection) {
        let sel = window.getSelection();
        if (sel.getRangeAt && sel.rangeCount) {
          range = sel.getRangeAt(0);
        }
      }
    }

    let range2 = null;
    // 聊天输入框失焦时获取失焦前的光标位置
    function getAfterBlurIndex2() {
      if (window.getSelection) {
        let sel = window.getSelection();
        if (sel.getRangeAt && sel.rangeCount) {
          range2 = sel.getRangeAt(0);
        }
      }
    }

    // 获取光标之前的文本，支持嵌套子节点
    function getTextBeforeCursor(range) {
      let textBeforeCursor = "";
      let currentNode = range.startContainer;
      // 遍历节点，拼接光标前的文本
      while (currentNode) {
        if (currentNode.nodeType === Node.TEXT_NODE) {
          // 如果是文本节点，拼接到结果中
          textBeforeCursor =
            currentNode.textContent.slice(0, range.startOffset) +
            textBeforeCursor;
          break;
        }
        // 如果是元素节点，继续向上遍历
        currentNode = currentNode.previousSibling || currentNode.parentNode;
      }
      return textBeforeCursor;
    }

    //添加@用户
    function insertEit() {
      if (eitKeyWord.value.trim() === "" && store.eitUserName === "") return;

      // 找到最近的 `@` 并删除关键字
      const containerText = range.startContainer.textContent;
      const startOffset = range.startOffset;
      const atIndex = containerText.lastIndexOf("@", startOffset - 1);

      if (atIndex !== -1) {
        // 构建新内容，删除 @关键字
        const beforeAt = containerText.slice(0, atIndex); // `@` 之前的内容
        const afterAt = containerText.slice(startOffset); // 光标之后的内容
        range.startContainer.textContent = beforeAt + afterAt;

        // 更新 Range 位置
        const newOffset = atIndex; // 新光标位置在 `@` 之前
        range.setStart(range.startContainer, newOffset);
        range.setEnd(range.startContainer, newOffset);
      }

          // **创建新的按钮元素**
        const buttonHtml = `<a m1a,s,a2href="./home?homeMenu=1&userId=${store.eitUserId}" target="_blank" data-eitt-userId="${store.eitUserId}" 
                              contentEditable="false" class="at-msg" style="color:#008ac5;margin-right:3px;margin-top:2px;">
                              @${store.eitUserName}</a>`;

        // **确保页面中只有一个占位符**
        const oldPlaceholder = document.getElementById("cursor-placeholder");
        if (oldPlaceholder) oldPlaceholder.remove();

        // **创建占位符**（零宽字符，标记光标位置）
        const placeholder = document.createElement("span");
        placeholder.id = "cursor-placeholder";
        placeholder.textContent = "\u200B"; // 零宽字符，防止影响排版
        range.insertNode(placeholder);

        // **插入按钮**
        const fragment = range.createContextualFragment(buttonHtml);
        range.insertNode(fragment);

        // **保存 innerHTML**
        if(scrollInputCommentFlag.value<920)
       { 
        commentContent.value = commentRef.value.innerHTML;
        commentRef.value.innerHTML = commentContent.value; // 触发 DOM 更新
       }else{
        commentContent2.value = commentRef2.value.innerHTML;
        commentRef2.value.innerHTML = commentContent2.value; // 触发 DOM 更新
        commentRef.value.innerHTML = commentContent2.value;

       }

        // **恢复光标**
        setTimeout(() => {
          const newPlaceholder = document.getElementById("cursor-placeholder");
          if (newPlaceholder) {
            const newRange = document.createRange();
            const newSelection = window.getSelection();

            newRange.setStartAfter(newPlaceholder);
            newRange.setEndAfter(newPlaceholder);
            newSelection.removeAllRanges();
            newSelection.addRange(newRange);

            // **移除占位符**
            newPlaceholder.remove();
          }
        }, 0);

      // 清空输入框
      eitKeyWord.value = "";
      eitFlag.value = false;
      eitFlag2.value = false;
      store.setEitUserName("");
      store.setEitUserId(null);
      handleContent();
    }

    //添加@用户
    function insertEit2() {
      if (eitKeyWord.value.trim() === "" && store.eitUserName === "") return;

      // 找到最近的 `@` 并删除关键字
      const containerText = range2.startContainer.textContent;
      const startOffset = range2.startOffset;
      const atIndex = containerText.lastIndexOf("@", startOffset - 1);

      if (atIndex !== -1) {
        // 构建新内容，删除 @关键字
        const beforeAt = containerText.slice(0, atIndex); // `@` 之前的内容
        const afterAt = containerText.slice(startOffset); // 光标之后的内容
        range2.startContainer.textContent = beforeAt + afterAt;

        // 更新 Range 位置
        const newOffset = atIndex; // 新光标位置在 `@` 之前
        range2.setStart(range2.startContainer, newOffset);
        range2.setEnd(range2.startContainer, newOffset);
      }

        // **创建新的按钮元素**
        const buttonHtml = `<a m1a,s,a2href="./home?homeMenu=1&userId=${store.eitUserId}" target="_blank" data-eitt-userId="${store.eitUserId}" 
                              contentEditable="false" class="at-msg" style="color:#008ac5;margin-right:3px;">
                              @${store.eitUserName}</a>`;

        // **确保页面中只有一个占位符**
        const oldPlaceholder = document.getElementById("cursor-placeholder");
        if (oldPlaceholder) oldPlaceholder.remove();

        // **创建占位符**（零宽字符，标记光标位置）
        const placeholder = document.createElement("span");
        placeholder.id = "cursor-placeholder";
        placeholder.textContent = "\u200B"; // 零宽字符，防止影响排版
        range2.insertNode(placeholder);

        // **插入按钮**
        const fragment = range2.createContextualFragment(buttonHtml);
        range2.insertNode(fragment);

        // **保存 innerHTML**
        const inputDom = document.getElementById("reply" + replyComment.value);
        commentContent2.value = inputDom.innerHTML;
        inputDom.innerHTML = commentContent2.value; // 触发 DOM 更新

        // **恢复光标**
        setTimeout(() => {
          const newPlaceholder = document.getElementById("cursor-placeholder");
          if (newPlaceholder) {
            const newRange = document.createRange();
            const newSelection = window.getSelection();

            newRange.setStartAfter(newPlaceholder);
            newRange.setEndAfter(newPlaceholder);
            newSelection.removeAllRanges();
            newSelection.addRange(newRange);

            // **移除占位符**
            newPlaceholder.remove();
          }
        }, 0);

      // 清空输入框
      eitKeyWord.value = "";
      eitFlag.value = false;
      eitFlag2.value = false;
      store.setEitUserName("");
      store.setEitUserId(null);
    }

    let onceSelectComment = true;
    watch([()=>store.eitUserName,()=>store.userId],
      async() => {
        if (store.eitUserName.trim() !== "" && commentFocusFlag.value) {
          insertEit();
        } else {
          insertEit2();
        }

        if (onceSelectComment && store.userId !== null) {
         await selectComment();
          setTimeout(()=>{
            selectCommentFromAxios();
          },1000)
          onceSelectComment = false;
        }
      },
      { deep: true },
    );

    //判断失去焦点是否点击的是footer
    function commnetBlur() {
      if (commentFooterFlag.value) commentFocusFlag.value = true;
      else commentFocusFlag.value = false;
    }

    //添加eit
    function addEit() {
      // 创建一个文本节点
      const atSymbol = document.createTextNode("@");
      // 获取光标位置并插入 @
      const selection = window.getSelection();
      range.insertNode(atSymbol);
      // 将光标移动到 @ 后面
      range.setStartAfter(atSymbol);
      range.setEndAfter(atSymbol);
      selection.removeAllRanges();
      selection.addRange(range);
      eitKeyWord.value = "";
      emojiFlag.value = false;
      emojiFlag2.value = false;
      emojiFlag3.value = false;
      handleContent();
      eitFlag.value = true;
      handleContent();
    }

    //添加eit
    function addEit2() {
      // 创建一个文本节点
      const atSymbol = document.createTextNode("@");
      // 获取光标位置并插入 @
      const selection = window.getSelection();
      range2.insertNode(atSymbol);
      // 将光标移动到 @ 后面
      range2.setStartAfter(atSymbol);
      range2.setEndAfter(atSymbol);
      selection.removeAllRanges();
      selection.addRange(range2);
      eitKeyWord.value = "";
      emojiFlag.value = false;
      emojiFlag2.value = false;
      emojiFlag3.value = false;
      handleContent2();
      eitFlag2.value = true;
      handleContent2();
    }

    //删除eit
    function deleteEit(event) {
      if(scrollInputCommentFlag.value>920&&commentFocus2Flag.value)
        inputTopValue.value=commentRef2.value.offsetHeight-48;

      if (event.key === "Backspace") {
        if(commentFocusFlag.value)
          handleContent();
      }
    }

    //添加图片
    function addImg() {
      const inputDom = document.getElementById("fileInput");
      inputDom.click();
    }

    //添加图片
    function addImg2() {
      const inputDom = document.getElementById("fileInput2");
      inputDom.click();
    }

    //添加图片
    function addImg3() {
      const inputDom = document.getElementById("fileInput3");
      inputDom.click();
    }

    //上传图片触发
    function handleImg(event) {
      let files = event.target.files;
      for (let i = 0; i < files.length; i++) {
        if (
          files[i].type.startsWith("image/") &&
          files[i].size <= 10 * 1024 * 1024&&!files[i].type.endsWith("gif")
        ) {
          commentImgFiles.push(files[i]);
        }
        if (!files[i].type.startsWith("image/")) continue;
        if (files[i].size > 10 * 1024 * 1024) {
          ElMessage({
            message: "图片不能超过10MB",
            type: "info",
            plain: true,
            duration: 1700,
          });
        }
      }

      if (commentImgFiles.length + commentImg.length > 9) {
        ElMessage({
          message: "上传图片不能超过9张",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }

      commentImgFiles.forEach((file) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => {
          if (commentImg.length < 9) commentImg.push(reader.result);
        };
      });
      event.target.value = "";
      commentImgFiles.length = 0;
    }

    //上传图片触发
    function handleImg2(event) {
      let files = event.target.files;
      for (let i = 0; i < files.length; i++) {
        if (
          files[i].type.startsWith("image/") &&
          files[i].size <= 10 * 1024 * 1024&&!files[i].type.endsWith("gif")
        ) {
          commentImgFiles2.push(files[i]);
        }
        if (!files[i].type.startsWith("image/")) continue;
        if (files[i].size > 10 * 1024 * 1024) {
          ElMessage({
            message: "图片不能超过10MB",
            type: "info",
            plain: true,
            duration: 1700,
          });
        }
      }

      if (commentImgFiles2.length + commentImg2.length > 9) {
        ElMessage({
          message: "上传图片不能超过9张",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }

      commentImgFiles2.forEach((file) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => {
          if (commentImg2.length < 9) commentImg2.push(reader.result);
        };
      });
      event.target.value = "";
      commentImgFiles2.length = 0;
    }

    //删除评论图片
    function deleteCommentImg(index) {
        commentImg.splice(index, 1);
    }

    //删除评论图片
    function deleteCommentImg2(index) {
        commentImg2.splice(index, 1);
    }

    //移动评论图片滑块
    function postionImg(direction) {
      const imgContainer = document.querySelector(".comment-img-container");
      const imgContainer2 = document.getElementById("commentImg");
      if (direction === "left") {
        commentImgLocation.value = "right";
        imgContainer.scrollLeft -= 150;
        imgContainer2.scrollLeft -= 150;
      } else {
        imgContainer.scrollLeft += 150;
        imgContainer2.scrollLeft += 150;
        commentImgLocation.value = "left";
      }
    }

    //移动评论图片滑块
    let imgContainer = null;
    function postionImg2(direction, id) {
      imgContainer = document.getElementById("replyImg" + id);
      if (direction === "left") {
        commentImgLocation2.value = "right";
        imgContainer.scrollLeft -= 200;
      } else {
        imgContainer.scrollLeft += 200;
        commentImgLocation2.value = "left";
      }
    }

    //评论图片数量变化
    watch(commentImg, () => {
      if (commentImg.length < 8) {
        const imgContainer = document.querySelector(".comment-img-container");
        const imgContainer2 = document.getElementById("commentImg");
        imgContainer.scrollLeft -= 150;
        imgContainer2.scrollLeft -= 150;
        commentImgLocation.value = "right";
      }
      if (commentImg.length === 9 && commentImgLocation.value === "left") {
        commentImgLocation.value = "right";
        const imgContainer = document.querySelector(".comment-img-container");
        const imgContainer2 = document.getElementById("commentImg");
        imgContainer.scrollLeft -= 150;
        imgContainer2.scrollLeft -= 150;
      }
    });

    //评论图片数量变化
    let commentImg3 = 0;
    watch(commentImg2, () => {
      if (!imgContainer) return;
      if (commentImg2.length < 7) {
        imgContainer.scrollLeft -= 200;
        commentImgLocation2.value = "right";
      }
      if (
        (commentImg2.length === 9 && commentImgLocation2.value === "left") ||
        (commentImg3 < commentImg2.length &&
          commentImg2.length === 8 &&
          commentImgLocation2.value === "left")
      ) {
        commentImgLocation2.value = "right";
        imgContainer.scrollLeft -= 200;
      }
      commentImg3 = commentImg2.length;
    });

    //自动重置表情开关
    watch(commentFocusFlag, (newValue) => {
      if (!newValue) {
        emojiFlag.value = false;
        emojiFlag3.value = false;
        eitFlag.value = false;
      }
    });

    //添加表情
    function addEmoji(index) {
      // 获取当前选区和光标位置
      const selection = window.getSelection();
      // 创建新的按钮元素
      const button = `<img src="../img/emoji/${index}.png" style="width:21px;height:21px;display:inline-block;vertical-align:middle;transform:translateY(-3px);" class="comment-emoji-img" />`;
      // 插入新按钮到光标位置
      const fragment = range.createContextualFragment(button); // 创建 DOM 片段
      const lastChild = fragment.lastChild;
      range.insertNode(lastChild); // 插入按钮
      // 设置光标位置到按钮后面
      const newRange = document.createRange();
      newRange.setStartAfter(lastChild);
      newRange.setEndAfter(lastChild);
      selection.removeAllRanges();
      selection.addRange(newRange);
      handleContent();
    }

    //添加表情
    function addEmoji2(index) {
      // 获取当前选区和光标位置
      const selection = window.getSelection();
      // 创建新的按钮元素
      const button = `<img src="../img/emoji/${index}.png" style="width:21px;height:21px;display:inline-block;vertical-align:middle;transform:translateY(-3px);" class="comment-emoji-img" />`;
      // 插入新按钮到光标位置
      const fragment = range2.createContextualFragment(button); // 创建 DOM 片段
      const lastChild = fragment.lastChild;
      range2.insertNode(lastChild); // 插入按钮
      // 设置光标位置到按钮后面
      const newRange = document.createRange();
      newRange.setStartAfter(lastChild);
      newRange.setEndAfter(lastChild);
      selection.removeAllRanges();
      selection.addRange(newRange);
      handleContent();
    }

    //发布评论
    async function publishComment() {
      if (commentContent.value.trim() === "" || !commentImg.length === 0)
        return;

      if(commentImg.length)
          loading.value=true;

      // 网址超链接
      commentContent.value = commentContent.value.replace(
        /(https?:\/\/[^\s<>"]+?)(?=\s|&nbsp;|<div>|<\/div>|$)\b/g,
        '<a href="$1" target="_blank"><img style="width: 14px;height: 14px;padding-right: 2px;" src="../img/网页链接.png">$1<br></a>',
      );
      // @样式
      commentContent.value = commentContent.value.replace(
        /color:#008ac5;/g,
        "color:#008ac5;cursor:pointer;",
      );

      // 替换时间样式
      commentContent.value = commentContent.value.replace(
        /(?<!<span[^>]*?>)\b\d{1,2}[：:]\d{2}(:\d{2})?(?!<\/span>)/g,
        `<span class="time-span" style="color:#008ac5;cursor:pointer;" data-time="$&">$&</span>`,
      );

      commentContent.value = commentContent.value.replace(/m1a,s,a2/g,"");
      commentContent.value = commentContent.value.replace(/data-eitt-userid/g,"data-eit-userid");

      // 替换本站链接格式
      const container = document.createElement("div");
      container.innerHTML = commentContent.value; // 使用 DOM 操作管理 HTML
      
      let atUserList = [];

      const links = container.querySelectorAll("a");
      for (const link of links) {
        const match = link.href.match(/BV([0-9]+)/); // 检查链接是否匹配 BV 格式
        if (match) {
          try {
            const res = await apiClient.get(`/video/getVideoTitle/${match[1]}`);
            if (res.data.code === 1) {
              // 动态替换 HTML 中的链接
              link.innerHTML = `<img style="width: 14px;height: 14px;padding-right: 2px;transform: translateY(1px);" src="../img/视频标题.png">${res.data.data}<br>`;
            }
          } catch (error) {}
        }
        const userId = link.getAttribute("data-eit-userid");
        const index=atUserList.findIndex((item) => item.atUserId === userId);
        if(index===-1&&userId!==store.userId)
        {
          let at={
            userId: store.userId,
            atUserId: userId,
            videoId,
         }
        atUserList.push(at);
        }
        
      }
      //去除多余的<br>
      commentContent.value = container.innerHTML.replace(/(<div>\s*<br\s*\/?>\s*<\/div>\s*)+$/, '');

      let comments = {
        content: commentContent.value,
        imgAddress: commentImg.length > 0 ? commentImg.join(",,") : null,
        userId: store.userId,
        videoId,
        userName: store.userInformation.userName,
        dynamicFlag: dynamicCheckFlag.value,
        replyUserId: store.selectUpVideo.upVideo.userId,
        upFlag: 0
      };

      let addComment={
        comments,
        ats:atUserList,
      }

      commentRef.value.innerHTML = "";
      commentImg.length = 0;
      commentContent.value = "";
      dynamicCheckFlag.value = 0;
      commentImgFiles.length = 0;
      commentLengthFlag.value = false;
      commentFocusFlag.value = false;
      emojiFlag.value = false;
      emojiFlag3.value = false;

      const res = await apiClient.post("/comment/addComment", addComment, {
        headers: {
          "Content-Type": "application/json",
          Authorization: store.token,
        },
      });

      if (res.data.code === 1) {
        ElMessage({
          message: "发送成功",
          type: "info",
          plain: true,
          duration: 1700,
        });

        store.updateVideoInfoFlag = true;
        commentList.unshift(res.data.data);
      } else {
        ElMessage({
          message: "发送失败",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
      loading.value=false;
    }

    //发布评论
    async function publishComment2() {
      if (commentContent2.value.trim() === "" || !commentImg2.length === 0)
        return;

      if(commentImg2.length)
          loading.value=true;

      // 网址超链接
      commentContent2.value = commentContent2.value.replace(
        /(https?:\/\/[^\s<>"]+?)(?=\s|&nbsp;|<div>|<\/div>|$)\b/g,
        '<a href="$1" target="_blank"><img style="width: 14px;height: 14px;padding-right: 2px;" src="../img/网页链接.png">$1<br></a>',
      );
      // @样式
      commentContent2.value = commentContent2.value.replace(
        /color:#008ac5;/g,
        "color:#008ac5;cursor:pointer;",
      );

      // 替换时间样式
      commentContent2.value = commentContent2.value.replace(
        /(?<!<span[^>]*?>)\b\d{1,2}[：:]\d{2}(:\d{2})?(?!<\/span>)/g,
        `<span class="time-span" style="color:#008ac5;cursor:pointer;" data-time="$&">$&</span>`,
      );

      commentContent2.value = commentContent2.value.replace("m1a,s,a2","")
      commentContent2.value = commentContent2.value.replace("data-eitt-userid","data-eit-userid")

      // 替换本站链接格式
      const container = document.createElement("div");
      container.innerHTML = commentContent2.value; // 使用 DOM 操作管理 HTML

      let atUserList = [];

      const links = container.querySelectorAll("a");
      for (const link of links) {
        const match = link.href.match(/BV([0-9]+)/); // 检查链接是否匹配 BV 格式
        if (match) {
          try {
            const res = await apiClient.get(`/video/getVideoTitle/${match[1]}`);
            if (res.data.code === 1) {
              // 动态替换 HTML 中的链接
              link.innerHTML = `<img style="width: 14px;height: 14px;padding-right: 2px;transform: translateY(1px);" src="../img/视频标题.png">${res.data.data}<br>`;
            }
          } catch (error) {}
        }
        const userId = link.getAttribute("data-eit-userid");
        const index=atUserList.findIndex((item) => item.atUserId === userId);
        if(index===-1&&userId!==store.userId)
        {
          let at={
            userId: store.userId,
            atUserId: userId,
            videoId,
         }
        atUserList.push(at);
        }
      }
      commentContent2.value = container.innerHTML.replace(/(<div>\s*<br\s*\/?>\s*<\/div>\s*)+$/, '');
      
        
      //如果是二级回复
      if(replyComment.value!==mainComment.value){
      commentContent2.value= `&nbsp;回复 <a href='./home?homeMenu=1&userId=${replyUserId.value}' target='_blank' data-eit-userid='${replyUserId.value}' class='at-msg' style='color:#008ac5;'>@${replyUserName.value}</a> ：`+commentContent2.value;

      }  

      //如果发布评论的是视频作者
      if(store.upUserId===store.userId&&commentImg2.length === 0){
        commentContent2.value = "<img style='width: 24px;height: 24px;display: inline-block;vertical-align: middle;transform: translateY(-1.5px);user-select: none;' src='../img/up_pb.svg'>&nbsp;"+commentContent2.value;
      }

      let comments = {
        content: commentContent2.value,
        imgAddress: commentImg2.length > 0 ? commentImg2.join(",,") : null,
        userId: store.userId,
        videoId,
        dynamicFlag: dynamicCheckFlag2.value,
        userName: store.userInformation.userName,
        replyCommentId: replyComment.value,
        mainCommentId: mainComment.value,
        replyUserId: replyUserId.value,
      };

      let addComment={
        comments,
        ats:atUserList,
      }

      commentContent2.value = "";
      emojiFlag2.value = false;
      eitFlag2.value = false;
      commentImgFiles2.length = 0;
      commentImg2.length = 0;
      dynamicCheckFlag2.value = 0;
      replyComment.value = null;

      const res = await apiClient.post("/comment/addComment", addComment, {
        headers: {
          "Content-Type": "application/json",
          Authorization: store.token,
        },
      });

      if (res.data.code === 1) {
        ElMessage({
          message: "发送成功",
          type: "info",
          plain: true,
          duration: 1700,
        });
        store.updateVideoInfoFlag = true;

        const index=commentList.findIndex((item)=> item.comments.id === comments.mainCommentId);
        if(index!==-1)
        {
          selectReplyComment(commentList[index],1);
          commentList[index].replyNumber++;
        }
      } else {
        ElMessage({
          message: "发送失败",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
      loading.value=false;
    }

    //查询评论
    let selectFlag = true;
    async function selectComment() {

      if(selectFlag){
        selectFlag=false;
      }
      else{
        return;
      }

      let res = await apiClient.get(`/comment/selectCommentByVideoId`, {
        params: {
          videoId: videoId,
          userId: store.userId !== null ? store.userId : 0,
          commentSortFlag: commentSortFlag.value,
          pageNum: pageNum.value++,
        },
      });
      if (res.data.code === 1) {
        pushData(res.data.data.records);
        selectFlag=true;
        if(!res.data.data.records||res.data.data.records.length===0){
          scrollFooterFlag.value=true;
        }
      } else {
        ElMessage({
          message: res.data.msg,
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
    }

    //去除重复数据
    function pushData(newData) {
    if(newData===null)
      return;
      const idSet = new Set(commentList.map(item => item.comments.id)); // 获取已有数据的 ID
      const uniqueData = newData.filter(item => !idSet.has(item.comments.id)); // 过滤掉重复的
      commentList.push(...uniqueData); // Vue 响应式更新
    }

    //评论时间显示
    function computedCommentTime(time) {
      const date = Date.now();
      const timeStamp = new Date(time);
      if (date - timeStamp < 10 * 1000) return "刚刚";
      else if (date - timeStamp < 60 * 1000)
        return ((date - timeStamp) / 1000).toFixed(0) + "秒前";
      else if (date - timeStamp < 60 * 60 * 1000)
        return ((date - timeStamp) / 60 / 1000).toFixed(0) + "分钟前";
      else if (date - timeStamp < 24 * 60 * 60 * 1000)
        return ((date - timeStamp) / 60 / 60 / 1000).toFixed(0) + "小时前";
      else return time;
    }

    //更新评论排序
    watch(commentSortFlag, () => {
      commentList.length = 0;
      pageNum.value = 1;
      selectComment();
    });

    //删除评论
    async function deleteCommentAxios() {
      const res = await apiClient.delete("/comment/deleteComment", {
        params: { 
          commentId: commentId.value,
          userId: store.userId,
         },
        headers: {
          "Content-Type": "application/json",
          Authorization: store.token,
        },
      });
      if (res.data.code === 1) {
        ElMessage({
          message: res.data.data,
          type: "info",
          plain: true,
          duration: 1700,
        });
        store.updateVideoInfoFlag = true;
        deleteDialogVisible.value = false;
        if(mainCommentId.value===null)
        commentList.splice(
          commentList.findIndex((item) => item.comments.id === commentId.value),
          1,
        );
        else{
          const index=commentList.findIndex((item)=> item.comments.id === mainCommentId.value);
          if(index!==-1)
          commentList[index].replyCommentList.splice(
            commentList[index].replyCommentList.findIndex((item) => item.comments.id === commentId.value),
            1,
          );
          commentList[index].replyNumber--;
        }
        commentId.value = null;
        mainCommentId.value = null;
      } else {
        ElMessage({
          message: res.data.msg,
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
    }

    //操作评论请求(点赞，踩)
    async function controlsCommentAxios(comment,controls) {
      if (store.userId === null) {
        store.setLoginDialogVisibleFlag(true);
        return;
      }
      let commentControls = {
        commentId: comment.comments.id,
        userId: store.userId,
        videoId: comment.comments.videoId,
        controls: controls,
      };
      let res = await apiClient.post(
        "/comment/controlsComment",
        commentControls,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: store.token,
          },
        },
      );
      if (res.data.code === 1) {
        
        if(comment.comments.mainCommentId===null)
      {  
        const index = commentList.findIndex(
          (item) => item.comments.id === comment.comments.id,
        );
        if (index !== -1) {
          if(comment.replyCommentList)
          res.data.data.replyCommentList=comment.replyCommentList;
          res.data.data.replyNumber=comment.replyNumber;
          if(comment.replyFlag)
          res.data.data.replyFlag=comment.replyFlag;
          res.data.data.userInfo.isFansFlag=comment.userInfo.isFansFlag;
          res.data.data.userInfo.isFollowFlag=comment.userInfo.isFollowFlag;
          if(store.userId===store.upUserId)
          res.data.data.upLikeFlag=res.data.data.commentControls?.controls;
          else
          res.data.data.upLikeFlag=comment.upLikeFlag;
          commentList[index] = res.data.data;
        }
      }else{
          const index = commentList.findIndex(
          (item) => item.comments.id === comment.comments.mainCommentId,
        );
        if (index !== -1) {
          const index2 = commentList[index].replyCommentList.findIndex(
          (item) => item.comments.id === comment.comments.id,
        );
          if (index2 !== -1) {
            commentList[index].replyCommentList[index2] = res.data.data;
          }
        }
      } 
      } else if (res.data.code === 0) {
        ElMessage({
          message: res.data.msg,
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
    }

    //延迟显示用户信息
    let delayUserInfoTime = null;
    let delayUserInfoTime2 = null;
    const nameOrAvatarHoverFlag2 = ref(false);
    function delayUserInfo(value) {
      if (value !== 0) {
        clearTimeout(delayUserInfoTime);
        delayUserInfoTime2 = setTimeout(() => {
          nameOrAvatarHoverFlag.value = value;
        }, 300);
      } else {
        clearTimeout(delayUserInfoTime2);
        delayUserInfoTime = setTimeout(() => {
          if (!nameOrAvatarHoverFlag2.value) {
            nameOrAvatarHoverFlag.value = 0;
            selectFollowFlag = false;
          }
        }, 400);
      }
    }

    //关注用户
    async function addFollowAxios(comment) {
      try {
        if (store.userId === null) {
          store.setLoginDialogVisibleFlag(true);
          return;
        }

        if (store.userId === comment.userInfo.id) {
          ElMessage({
            message: "不能关注自己哦",
            type: "info",
            plain: true,
            duration: 1700,
          });
          return;
        }

        const res = await apiClient.post(
          "/video/addFollow",
          {
            followId: comment.userInfo.id,
            fansId: store.userId,
          },
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: store.token,
            },
          },
        );
        if (res.data.code === 1) comment.userInfo.isFansFlag = 1;
      } catch (error) {}
    }

    //取消关注用户
    async function deleteFollowAxios(comment) {
      try {
        if (store.userId === null) {
          store.setLoginDialogVisibleFlag(true);
          return;
        }

        const res = await apiClient.post(
          "/video/deleteFollow",
          {
            followId: comment.userInfo.id,
            fansId: store.userId,
          },
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: store.token,
            },
          },
        );
        if (res.data.code === 1) comment.userInfo.isFansFlag = 0;
      } catch (error) {}
    }

    //查询是否关注该用户
    let selectFollowFlag = false;
    async function selectFollow(comment) {
      if (selectFollowFlag) return;
      selectFollowFlag = true;
      const response = await apiClient.get("/user/selectFollow", {
        params: {
          userId: store.userId,
          viewUserId: comment.userInfo.id,
        },
        headers: {
          "Content-Type": "application/json",
          Authorization: store.token,
        },
      });
      if (response.data.code === 1) {
        isDeleteFollowFlag.value = comment.comments.id;
        comment.userInfo.isFansFlag = response.data.data;
      }
    }

    //回复评论
    let replyInputDom = null;
    function replyCommentF(comment,mainCommentt) {

      if(store.userId===null)
      {
        store.setLoginDialogVisibleFlag(true);
        return;
      }

      if (replyInputDom === null) {
        replyInputDom = document.getElementById("reply" + comment.comments.id);
        setTimeout(() => {
          replyInputDom.focus();
          if (replyInputDom.getSelection) {
            range2 = replyInputDom.getSelection().getRangeAt(0);
          }
        }, 100);
        mainComment.value = mainCommentt.comments.id;
        replyUserName.value = comment.userInfo.userName;
        replyUserId.value = comment.userInfo.id;
        replyComment.value = comment.comments.id;
        return;
      }
      if (replyComment.value === comment.comments.id) {
        replyComment.value = null;
        mainComment.value = null;
        replyUserName.value = null;
        replyUserId.value = null;
        replyInputDom.innerHTML = null;
      } else {
        if (replyInputDom !== null) replyInputDom.innerHTML = null;
        replyInputDom = document.getElementById("reply" + comment.comments.id);
        setTimeout(() => {
          replyInputDom.focus();
          if (replyInputDom.getSelection) {
            range2 = replyInputDom.getSelection().getRangeAt(0);
          }
        }, 100);
        replyComment.value = comment.comments.id;
        mainComment.value = mainCommentt.comments.id;
        replyUserName.value = comment.userInfo.userName;
        replyUserId.value = comment.userInfo.id;
        replyPlaceholderFlag.value = true;
      }
      eitFlag2.value = false;
      emojiFlag2.value = false;
      dynamicCheckFlag2.value = 0;
      commentImg2.length = 0;
    }

    //查询点击回复的评论
    async function selectReplyComment(comment,pageNum){

      const response=await apiClient.get("/comment/selectReplyById",{
        headers:{
          "Content-Type": "application/json",
          Authorization: store.token,
          },
        params:{
          videoId: comment.comments.videoId,
          commentId: comment.comments.id,
          userId: store.userId !== null ? store.userId : 0,
          pageNum,
        }
      })

      if(response.data.code === 1){
        comment.replyFlag=true;
        comment.replyFlag2=false;
        comment.replyCommentList = response.data.data.records;
        comment.page=1;
      }
      
    }

    // 获取用户名宽度的函数
    function getUserNameIndent(userName){
     // 创建临时元素
     const tempElement = document.createElement('span');
            tempElement.style.visibility = 'hidden';
            tempElement.style.position = 'absolute';
            tempElement.style.fontFamily = "微软雅黑";
            tempElement.style.fontSize = "12.5px";
            tempElement.style.padding = 0;
            tempElement.textContent = userName;

            // 将临时元素添加到文档中
            document.body.appendChild(tempElement);

            // 获取字符串的宽度
            const width = tempElement.offsetWidth;

            // 移除临时元素
            document.body.removeChild(tempElement);

            return width/12.5;
    }

    //更多回复评论
    async function moreReplyComment(comment,index){

      if(index===comment.page)
      return;
      else
      comment.page=index;

      const response=await apiClient.get("/comment/selectReplyById",{
        headers:{
          "Content-Type": "application/json",
          Authorization: store.token,
          },
        params:{
          videoId: comment.comments.videoId,
          commentId: comment.comments.id,
          userId: store.userId !== null ? store.userId : 0,
          pageNum: comment.page,
        }
      })

      if(response.data.code === 1){
        comment.replyCommentList=response.data.data.records;
      }else{
        ElMessage({
          message: response.data.msg,
          type: "info",
          plain: true,
          duration: 1700,
        });
      }

    }

    //监听显示eit的用户信息
    let eitUserInfoTime = null;
    async function handleMouseover(event) {
         
      clearTimeout(eitUserInfoTime);

      if(event.target.classList.contains("at-msg")&&store.eitUserInfo===null)
      {
        const eitUserId=event.target.getAttribute("data-eit-userid");

        if(!eitUserId)
        return;
        const res=await apiClient.get(`/user/selectUserInfoById`,{
          params:{
            userId: store.userId !== null ? store.userId : 0,
            eitUserId,
          }
        });
        if(res.data.code === 1)
        eitUserInfoTime=setTimeout(() => {
          eitUserInfoLeft.value=event.clientX+15;
          eitUserInfoTop.value=event.clientY+15;
          if(event.clientY>430)
            eitUserInfoTop.value=event.clientY/2;
          store.setEitUserInfo(res.data.data);
        }, 300);
      }else{
        eitUserInfoTime=setTimeout(()=>{
          if(!store.eitUserInfoHover)
          store.setEitUserInfo(null);
        },300)
      }
      
    }

    //监视登录后查询评论
    let loginSelectCommentFlag=true;
    watch([()=>store.userId,()=>store.loginLoadFlag],()=>{

      if(store.userId!==null&&loginSelectCommentFlag&&store.loginLoadFlag)
      {
        loginSelectCommentFlag=false;
        pageNum.value=1;
        commentList.length=0;
        selectComment();
      }

    })
    
    //添加私信对话并跳转
    async function addDialogueF(dialogueId) {

    if (store.userId === null) {
        loginDialogVisibleFlag.value =
          loginDialogVisibleFlag.value === 0 ? 1 : 0;
        return;
      }

    let dialogue = {
      userId: store.userId,
      dialogueId,
    }
    addDialogue(store.token,dialogue).then(res=>{

      if(res.data.code===1)
      {
        window.open(
        `./message?dialogueId=${dialogueId}`,
        "_blank",
        );
      }

    })


    }

    //修改评论置顶状态
    async function putUpStatusAxios(commentId,up){

      if (store.userId === null) {
        return;
      }

    putUpStatus(store.token,store.userId,commentId).then(res=>{
        if(res.data.code===1){
          putUpStatusF(commentId,up);
          ElMessage({
            message: up===0 ? "置顶成功" : "取消置顶成功",
            type: "info",
            plain: true,
            duration: 1700,
          });
        }
      })
    }

    async function putUpStatusF(commentId,up){
      
      if (store.userId === null) {
        return;
      }

      const index = commentList.findIndex(
        (item) => item.comments.id === commentId,
      );
      if (index !== -1) {
        if(up===0){
          commentList.forEach(item=>item.comments.upFlag=0);
        }
        commentList[index].comments.upFlag=up===1 ? 0 : 1;
      }

      if(up===0){
        commentList.unshift(commentList[index]);
        commentList.splice(index+1,1);
      }else if(up===1){
        commentList.push(commentList[index]);
        commentList.splice(index,1);
      }
    }

    //从消息评论点进来的
    async function selectCommentFromAxios(){
     
      if(searchCommentId.value===null)
      return;
      selectOneComment(store.token,store.userId,searchCommentId.value,searchReplyId.value).then(res=>{
        if(res.data.code===1){
          if(res.data.data!==null){
            const index=commentList.findIndex(item=>item.comments.id===searchCommentId.value);
            if(index!==-1)
              commentList.splice(index,1);

            if(commentList.length>0){
              if(commentList[0].comments.upFlag===0)
                commentList.unshift(res.data.data);
              else if(commentList[0].comments.upFlag===1)
                commentList.splice(1,0,res.data.data) 
            }
            else
              commentList.push(res.data.data);

            if(res.data.data.replyCommentList!==null&&commentList[0].comments.upFlag===0)
             { 
              commentList[0].replyFlag=true;
              commentList[0].replyFlag2=true;
             }else if(res.data.data.replyCommentList!==null&&commentList[0].comments.upFlag===1){
              commentList[1].replyFlag=true;
              commentList[1].replyFlag2=true;
             }
             else{
              searchReplyId.value=null;
             }
             setTimeout(() => {
              window.scrollTo({ top: 800, behavior: "smooth" });
             }, 500);
          }
        }
        })

    }

    //打开我的主页
    function openHome(menu,id){
          window.open(
          `./home?homeMenu=${menu}&userId=${id}`,
          "_blank",
        );
    }


    return {
      store,
      commentSortFlag,
      commentPlaceholderr,
      replyPlaceholderFlag,
      commentPlaceholderHoverFlag,
      commentFocusFlag,
      commentFocus2Flag,
      commentRef,
      commentRef2,
      handleContent,
      handleContent2,
      commentContent,
      commentContent2,
      dynamicCheckFlag,
      dynamicCheckFlag2,
      eitKeyWord,
      commnetBlur,
      commentFooterFlag,
      commentLengthFlag,
      eitFlag,
      eitFlag2,
      addEit,
      addEit2,
      getAfterBlurIndex,
      getAfterBlurIndex2,
      commentHoverFlag,
      addImg,
      addImg2,
      addImg3,
      handleImg,
      handleImg2,
      commentImgFiles,
      deleteCommentImg,
      deleteCommentImg2,
      commentImg,
      commentImg2,
      commentImgLocation,
      commentImgLocation2,
      postionImg,
      postionImg2,
      emojiFlag,
      emojiFlag2,
      emojiFlag3,
      addEmoji,
      addEmoji2,
      publishComment,
      publishComment2,
      commentImgNumColor,
      commentLikeHoverFlag,
      commentDislikeHoverFlag,
      deleteCommentHoverFlag,
      deleteCommentHoverFlag2,
      commentList,
      computedCommentTime,
      deleteDialogVisible,
      deleteCommentAxios,
      commentId,
      mainCommentId,
      controlsCommentAxios,
      nameOrAvatarHoverFlag,
      nameOrAvatarHoverFlag2,
      delayUserInfo,
      addFollowAxios,
      deleteFollowAxios,
      isDeleteFollowFlag,
      selectFollow,
      replyComment,
      replyCommentF,
      selectReplyComment,
      getUserNameIndent,
      moreReplyComment,
      eitUserInfoTop,
      eitUserInfoLeft,
      scrollFooterFlag,
      scrollInputCommentFlag,
      inputTopValue,
      addDialogueF,
      putUpStatusAxios,
      searchCommentId,
      searchReplyId,
      openHome,
      loading,
    };
  },
};
</script>

<style lang="scss" scoped>
@font-face {
  font-family: "fans-num"; /* 字体名称 */
  src: url("../assets/font/Digital Dare.ttf"); /* 优先加载更高效的格式 */
  font-weight: 100;
  font-style: normal;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.comment-container {
  position: relative;
  width: 700px;
  left: 10px;
  top: 153px;

  .comment-header,
  .reply-container,
  .comment-input-container2
  {
    position: relative;


    .comment-title {
      font-size: 19px;
      color: #18191c;
      font-weight: 900;
      font-family: Microsoft YaHei UI Light;
      user-select: none;
      transition: color 0.3s ease;
      .comment-number {
        margin-left: 2px;
        font-weight: 600;
        font-size: 13px;
        color: #9499a0;
      }

      .comment-sort-fire {
        margin-left: 37px;
        font-size: 12.5px;
        color: #9499a0;
        font-family: 微软雅黑;
        font-weight: 500;
        cursor: pointer;
      }
      .comment-sort-fire:hover {
        color: #00aeec;
      }
      .comment-sort-line {
        display: inline-block;
        transform: translateY(-2px);
        margin-left: 11px;
        font-size: 11px;
        color: #9499a0;
        font-family: 黑体;
        font-weight: 500;
      }
      .comment-sort-new {
        margin-left: 11px;
        font-size: 12.5px;
        color: #9499a0;
        font-family: 微软雅黑;
        font-weight: 500;
        cursor: pointer;
      }
      .comment-sort-new:hover {
        color: #00aeec;
      }

      .commentSortFlag {
        color: #18191c;
      }
      .commentSortFlag:hover {
        color: #18191c;
      }
    }

    .comment-avatar {
      position: absolute;
      left: 15.5px;
      top: 50.5px;
      border-radius: 50%;
      width: 48px;
      height: 48px;
      aspect-ratio: auto 48 / 48;
      cursor: pointer;
    }

    .not-login-commit-comment-container {
      position: absolute;
      width: 620px;
      height: 50px;
      left: 79.5px;
      top: 50px;
      border-radius: 6px;
      font-size: 12px;
      z-index: 100;
      color: #9499a0;
      background-color: #f1f2f3;
      display: flex;
      align-items: center;
      justify-content: center;

      span {
        button {
          box-sizing: border-box;
          padding: 4px 9px;
          margin: 0px 3px;
          border-radius: 4px;
          color: white;
          background-color: #00aeec;
          outline: none;
          border: none;
          cursor: pointer;
          font-size: 12px;
        }
        button:hover {
          background-color: #40c5f1;
        }
      }
    }

    .login-commit-comment-container {
      position: relative;
      padding: 11.5px 8px;
      width: 620px;
      left: 0px;
      top: 0px;
      border-radius: 6px;
      font-size: 14px;
      color: #18191c;
      outline: none;
      z-index: 10;
      overflow: hidden;
      overflow-y: auto;
      min-height: 48px;
      max-height: 118px;
    }

    .comment-content-container:hover {
      background-color: white;
      border: 1px solid #c9ccd0;
    }

    .comment-placeholder {
      position: absolute;
      width: 300px;
      color: #9499a0;
      font-size: 13.5px;
      top: 64px;
      left: 89px;
      user-select: none;
      z-index: 10;
      pointer-events: none;
    }

    .commentPlaceholderHoverFlag {
      background-color: white;
      border: 1px solid #c9ccd0;
    }

    .comment-emoji-btn {
      position: relative;
      left: 79.5px;
      top: 37.5px;
      width: 32px;
      height: 26px;
      background-color: white;
      border: 1px solid rgb(241, 242, 243);
      border-radius: 4px;
      display: flex;
      justify-content: center;
      align-items: center;
      cursor: pointer;
      .comment-emoji-img {
        width: 13.5px;
        height: 13.5px;
        transform: translateY(0px);
        user-select: none;
      }
      .comment-emoji-container {
        position: absolute;
        width: 363px;
        height: 271px;
        left: -1px;
        top: 28px;
        overflow: hidden;
        background-color: white;
        border: 1px solid #e3e5e7;
        border-radius: 8px;
        padding-left: 11px;
        padding-bottom: 120px;
        z-index: 1000;
        overflow-y: auto;
        cursor: default;
        .head-span {
          position: absolute;
          left: 15px;
          top: 13px;
          color: #61666d;
          font-size: 12px;
        }

        .comment-emoji-content {
          position: relative;
          top: 37px;
          width: 32px;
          height: 32px;
          display: inline-flex;
          justify-content: center;
          align-items: center;
          cursor: pointer;
          background-color: white;
          border-radius: 4px;
          transition: background-color 0.1s ease;
          img {
            width: 24px;
            height: 24px;
          }
        }

        .comment-emoji-content:hover {
          background-color: #e3e5e7;
        }
      }

      .comment-emoji-container::-webkit-scrollbar {
        width: 5px; /* 滚动条的宽度 */
        border-radius: 30px; /* 滚动条滑块的圆角 */
      }

      .comment-emoji-container::-webkit-scrollbar-thumb {
        background: #e3e5e7; /* 滚动条的滑块 */
        border-radius: 10px;
      }
    }

    .comment-at-btn {
      position: relative;
      left: 117.5px;
      top: 11.5px;
      width: 32px;
      height: 26px;
      background-color: white;
      border: 1px solid rgb(241, 242, 243);
      border-radius: 4px;
      display: flex;
      justify-content: center;
      align-items: center;
      cursor: pointer;
      img {
        width: 13.5px;
        height: 13.5px;
      }
    }

    .comment-img-btn {
      position: relative;
      left: 155.5px;
      top: -14.5px;
      width: 32px;
      height: 26px;
      background-color: white;
      border: 1px solid rgb(241, 242, 243);
      border-radius: 4px;
      display: flex;
      justify-content: center;
      align-items: center;
      cursor: pointer;
      img {
        width: 13.5px;
        height: 11.5px;
      }
    }

    .comment-dynamic-btn {
      position: relative;
      width: 120px;
      left: 208.5px;
      top: -41px;
      height: 26px;
      display: flex;
      align-items: center;
      cursor: pointer;
      user-select: none;
      img {
        position: absolute;
        width: 12.8px;
        height: 12.8px;
      }

      .dynamic-check-img1 {
        opacity: 0;
        transition: opacity 0.3s ease;
      }

      .dynamic-check-img2 {
        opacity: 0;
        transition: opacity 0.3s ease;
      }

      .dynamicCheckFlag {
        opacity: 1;
        transition: opacity 0.3s ease;
      }

      span {
        position: absolute;
        font-size: 11px;
        left: 19px;
        color: #61666d;
      }
    }

    .comment-publish-btn {
      position: relative;
      left: 629.5px;
      top: -69.5px;
      background-color: #00aeec;
      color: white;
      display: flex;
      justify-content: center;
      align-items: center;
      width: 70px;
      height: 32px;
      border-radius: 4px;
      cursor: pointer;
      user-select: none;
    }

    .comment-publish-btn-disabled {
      opacity: 0.5;
      cursor: no-drop;
    }

    .eit {
      position: absolute;
      left: 0px;
      top: 29px;
    }

    .comment-footer {
      position: relative;
      z-index: 100;
      height: 100px;
    }

    .comment-img-container {
      position: relative;
      top: -2px;
      left: 0px;
      width: 610px;
      padding-left: 8px;
      height: 72px;
      display: flex;
      flex-direction: row;
      gap: 8px; /* 子元素之间的间距 */
      overflow: hidden;
      z-index: 10;

      .loading-span {
        position: relative;
        top: -1px;
        left: 0px;
        width: 72px;
        height: 72px;
        display: flex;
        justify-content: center;
        align-items: center;
        background-color: rgba(0, 0, 0, 0.333);
        border-radius: 5px;
        z-index: 1;
        img {
          width: 24px;
          height: 24px;
        }
        span {
          position: absolute;
          right: 0px;
          border-radius: 4px;
          top: 0px;
          width: 20px;
          height: 20px;
          display: flex;
          justify-content: center;
          align-items: center;
          background-color: rgba(0, 0, 0, 0.2);
          cursor: pointer;
          img {
            width: 7px;
            height: 7px;
          }
        }
      }
      .loading-before-span {
        position: relative;
        top: 0px;
        left: 0px;
        width: 72px;
        height: 72px;
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 5;
        img {
          object-fit: cover;
          width: 72px;
          height: 72px;
          background-color: #f1f2f3;
          border-radius: 5px;
        }
        span {
          position: absolute;
          right: 0px;
          border-radius: 4px;
          top: 0px;
          width: 20px;
          height: 20px;
          display: flex;
          justify-content: center;
          align-items: center;
          background-color: rgba(0, 0, 0, 0.2);
          cursor: pointer;
          img {
            width: 7px;
            height: 7px;
            transform: scale(1.3);
            background-color: inherit !important;
          }
        }
      }

      .right {
        position: absolute;
        top: 23px;
        right: 0px;
        width: 26px;
        height: 26px;
        z-index: 10;
        cursor: pointer;
      }

      .left {
        position: absolute;
        top: 23px;
        left: 118px;
        width: 26px;
        height: 26px;
        z-index: 10;
        cursor: pointer;
      }

      .left2 {
        left: 38px;
      }
    }

    .comment-img-container2 {
      padding-left: 16px;
    }

    .comment-content-container {
      position: relative;
      width: 620px;
      left: 79.5px;
      top: 24.5px;
      border-radius: 6px;
      font-size: 14px;
      color: #9499a0;
      border: 1px solid #f1f2f3;
      background-color: #f1f2f3;
      outline: none;
      min-height: 48px;
      max-height: 118px;
    }

    .commentFocusFlag {
      color: #18191c;
      background-color: white;
      border: 1px solid #c9ccd0;
    }

    .commentImgFiles1 {
      min-height: 130px;
      padding-bottom: 6px;
      max-height: 198px;
    }

    .shadowEdgeLeft {
      position: absolute;
      width: 20px;
      height: 100%;
      left: 110px;
      z-index: 5;
      background: linear-gradient(
        to right,
        rgb(255, 255, 255),
        rgb(255, 255, 255) 28.5%,
        rgba(255, 255, 255, 0) 100%
      );
    }

    .shadowEdgeLeft2 {
      left: 30px;
    }

    .shadowEdgeRight {
      position: absolute;
      width: 25px;
      height: 100%;
      left: 594px;
      z-index: 5;
      background: linear-gradient(
        to left,
        rgb(255, 255, 255),
        rgb(255, 255, 255) 28.5%,
        rgba(255, 255, 255, 0) 100%
      );
    }
  }

  .comment-body {
    position: relative;
    width: 700px;
    left: 0px;
    top: 60px;
    margin-bottom: 168px;

    .comment-list {
      position: relative;
      margin-bottom: 52.5px;
      img {
        user-select: none;
      }

      .comment-user-avatar {
        position: absolute;
        left: 19.5px;
        top: 0.5px;
        width: 40px;
        height: 40px;
        border-radius: 50%;
        cursor: pointer;
      }
      .comment-user-name {
        position: relative;
        display: inline-block;
        left: 80px;
        top: 4px;
        font-size: 12.5px;
        cursor: pointer;
        color: #61666d;
      }

      .comment-up-icon{
        position: relative;
        vertical-align: middle;
        left: 95px;
        top: 3px;
      }

      .comment-user-level {
        position: relative;
        width: 20px;
        height: 10px;
        left: 88px;
        top: 4px;
      }

      .comment-content {
        width: 620px;
        display: block;
        word-wrap: break-word;
        transform: translate(79.5px, 8px);
        line-height: 1.6;
        font-size: 14.5px;
      }

      .commentContent {
        transform: translate(82.5px, 12px);
        text-indent: 3.5em;
      }

      .commentContent2 {
        transform: translate(82.5px, 9px);
        text-indent: 2.4em;
      }

      .commentContent3 {
        transform: translate(82.5px, 9px);
        text-indent: 5.9em;
      }

      .commentContent4{
        animation: commentAnimation 3s ease forwards;
      }

      .comment-id-icon {
        position: absolute;
        width: 288px;
        height: 48px;
        right: 0.5px;
        top: -14.5px;
      }

      .comment-id-number {
        position: absolute;
        width: 50px;
        right: -2px;
        top: -2.5px;
        font-size: 9.5px;
        cursor: default;
        user-select: none;
        font-family: fans-num;
      }

      .comment-footer {
        position: relative;
        top: 14px;
        left: 0px;
        color: #9499a0;
        font-size: 12.5px;
        .comment-time {
          position: relative;
          top: -1px;
          left: 80px;
        }

        .comment-like-number-container {
          position: relative;
          top: 1px;
          left: 102.5px;
          cursor: pointer;
          .comment-like-btn {
            position: absolute;
            width: 14px;
            height: 13px;
          }

          .comment-like-number {
            position: relative;
            top: -2px;
            left: 20px;
            font-size: 12.5px;
          }
        }

        .comment-like-number-container:hover {
          .comment-like-number {
            color: #00aeec;
          }
        }

        .comment-dont-like-number-container {
          position: relative;
          margin: 0px;
          top: 1.5px;
          left: 143.5px;
          cursor: pointer;
          .comment-dont-like-btn {
            position: absolute;
            width: 14px;
            height: 13px;
          }
        }

        .comment-reply-btn {
          position: relative;
          top: -0.5px;
          left: 178px;
          font-size: 13px;
          user-select: none;
          cursor: pointer;
        }

        .comment-reply-btn:hover {
          color: #00aeec;
        }

        .comment-delete-btn-container {
          position: absolute;
          top: -10px;
          right: 20px;
          padding: 10px;
          visibility: hidden;
          cursor: pointer;
          img {
            width: 2.5px;
            height: 10px;
          }
          .delete-btn {
            position: absolute;
            width: 120px;
            height: 36px;
            top: 25px;
            right: 0px;
            color: #61666d;
            background-color: white;
            border: 1px solid #e3e5e7;
            display: flex;
            align-items: center;
            padding: 0px 15px;
            border-radius: 4px;
            z-index: 10;
            transition: none;
            cursor: pointer;
            user-select: none;
          }

          .delete-btn:hover {
            background-color: #f1f2f3;
          }

          .up-btn {
            position: absolute;
            width: 120px;
            height: 36px;
            top: 61px;
            right: 0px;
            color: #61666d;
            background-color: white;
            border: 1px solid #e3e5e7;
            border-top: none;
            display: flex;
            align-items: center;
            padding: 0px 15px;
            border-radius: 4px;
            z-index: 10;
            transition: none;
            cursor: pointer;
            user-select: none;
          }

          .up-btn:hover {
            background-color: #f1f2f3;
          }
        }
      }

      .commentFooter {
        margin-top: 117px;
      }

      .up-like-comment{
        position: relative;
        top: 22px;
        left: 80px;
        color: #757575;
        display: inline-block;
        background-color: #f4f4f4;
        font-size: 11.5px;
        padding: 6px;
        border-radius: 2px;
        z-index: 100;
        line-height: 1;
        margin-right: 5px;
      }

      .up-icon{
      display: inline-flex;
      justify-content: center;
      align-items: center;
      position: absolute;
      top: 35px;
      left: 79.5px;
      width: 30px;
      font-size: 12px;
      height: 18px;
      border: 1px solid #FF6699;
      border-radius: 3px;
      color: #FF6699;
      vertical-align: text-bottom;
      margin-bottom: 0.0666em;
      }

      .comment-note-img {
        position: absolute;
        top: 34.5px;
        left: 79.5px;
        width: 48px;
        height: 22px;
      }

      .comment-img-container {
        position: relative;
        top: 15px;
        left: 71.5px;
        width: 610px;
        height: auto;
        padding-left: 8px;
        height: 72px;
        display: flex;
        flex-wrap: wrap; /* 允许换行 */
        flex-direction: row;
        gap: 8px; /* 子元素之间的间距 */
        margin-bottom: 24px;
      }

      .view-reply-btn {
        position: relative;
        left: 80px;
        font-size: 12.5px;
        color: #9499a0;
        margin-bottom: 8px;
        top: 25px;

        button {
          border: none;
          user-select: none;
          background-color: white;
          color: #9499a0;
          font-size: 12.5px;
          cursor: pointer;
        }

        button:hover {
          color: #00aeec;
        }
      }

      .reply-container {
        position: relative;
        width: 100%;
        padding: 0px;
        min-height: 118px;


        .reply-avatar {
          position: absolute;
          left: 96px;
          top: 31px;
          border-radius: 50%;
          width: 48px;
          height: 48px;
          aspect-ratio: auto 48 / 48;
          cursor: pointer;
        }

        .comment-content-container {
          position: relative;
          width: 540px;
          padding: 0px;
          left: 159.5px;
          top: 30px;
          border: 1px solid #c9ccd0;
          background-color: white;
          outline: none;
          min-height: 48px;
          max-height: 118px;
        }

        .comment-placeholder {
          left: 169px;
          top: 45px;
        }

        .reply-commit-comment-container {
          position: relative;
          padding: 11.5px 8px;
          width: 540px;
          left: 0px;
          top: 0px;
          z-index: 10;
          border-radius: 6px;
          font-size: 14px;
          color: #18191c;
          outline: none;
          overflow: hidden;
          overflow-y: auto;
          min-height: 48px;
          max-height: 118px;
        }

        .commentImgFiles1 {
          min-height: 130px;
          padding-bottom: 6px;
          max-height: 198px;
        }

        .comment-footer {
          top: 5px;
          height: 68px;
          left: 80px;
          z-index: 1;
          .comment-publish-btn {
            left: 549px;
            font-size: 15.5px;
          }
        }

        .comment-img-container1 {
          position: relative;
          top: -2px;
          left: 0px;
          width: 529px;
          padding-left: 8px;
          height: 72px;
          display: flex;
          flex-direction: row;
          gap: 8px; /* 子元素之间的间距 */
          overflow: hidden;
          z-index: 10;

          .loading-span {
            position: relative;
            top: -1px;
            left: 0px;
            width: 72px;
            height: 72px;
            display: flex;
            justify-content: center;
            align-items: center;
            background-color: rgba(0, 0, 0, 0.333);
            border-radius: 5px;
            z-index: 1;
            img {
              width: 24px;
              height: 24px;
            }
            span {
              position: absolute;
              right: 0px;
              border-radius: 4px;
              top: 0px;
              width: 20px;
              height: 20px;
              display: flex;
              justify-content: center;
              align-items: center;
              background-color: rgba(0, 0, 0, 0.2);
              cursor: pointer;
              img {
                width: 7px;
                height: 7px;
              }
            }
          }
          .loading-before-span {
            position: relative;
            top: 0px;
            left: 0px;
            width: 72px;
            height: 72px;
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 5;
            img {
              object-fit: cover;
              width: 72px;
              height: 72px;
              border-radius: 5px;
              background-color: #f1f2f3;
            }
            span {
              position: absolute;
              right: 0px;
              border-radius: 4px;
              top: 0px;
              width: 20px;
              height: 20px;
              display: flex;
              justify-content: center;
              align-items: center;
              background-color: rgba(0, 0, 0, 0.2);
              cursor: pointer;
              img {
                width: 7px;
                height: 7px;
                transform: scale(1.3);
                background-color: inherit !important;
              }
            }
          }

          .right {
            position: absolute;
            top: 23px;
            right: 0px;
            width: 26px;
            height: 26px;
            z-index: 10;
            cursor: pointer;
          }

          .left {
            position: absolute;
            top: 23px;
            left: 200px;
            width: 26px;
            height: 26px;
            z-index: 10;
            cursor: pointer;
          }

          .left2 {
            left: 120px;
          }

          .left3 {
            left: 40px;
          }
        }

        .shadowEdgeLeft {
          position: absolute;
          width: 20px;
          height: 100%;
          left: 191px;
          z-index: 5;
          background: linear-gradient(
            to right,
            rgb(255, 255, 255),
            rgb(255, 255, 255) 28.5%,
            rgba(255, 255, 255, 0) 100%
          );
        }

        .shadowEdgeLeft2 {
          left: 111px;
        }

        .shadowEdgeLeft3 {
          left: 30px;
        }

        .shadowEdgeRight {
          position: absolute;
          width: 25px;
          height: 100%;
          left: 512px;
          z-index: 5;
          background: linear-gradient(
            to left,
            rgb(255, 255, 255),
            rgb(255, 255, 255) 28.5%,
            rgba(255, 255, 255, 0) 100%
          );
        }

       
      }

      .replyCommentClass {
        left: 60px;
        top: 32px;

        .comment-img-container{
          top: 22.5px !important;
        }

        .comment-user-avatar {
          width: 24px;
          height: 24px;
          z-index: 30;
        }

        .comment-user-name{
          z-index: 30;
        }

        .reply-content{
          position: relative;
          top: -5px;
          left: -27px;

          .comment-content{
            color: #18191c;
            width: 580px;
            line-height: 2;
            transform: translate(78px,-20.5px);
          }

          .comment-delete-btn-container{
            left: 605px;
            width: 22.5px;
            display: flex;
            justify-content: center;
          }

        }
      }

      .replyCommentClass2 {
        left: 60px;
        top: 32px;

        .comment-user-avatar {
          width: 24px;
          height: 24px;
        }

        .reply-content{
          position: relative;
          top: -5px;
          left: -27px;

          .comment-content{
            color: #18191c;
            width: 580px;
            transform: translate(78px,9px);
          }

          .reply-container{
            top: 30px;
          }

          .comment-delete-btn-container{
            left: 605px;
            width: 22.5px;
            display: flex;
            justify-content: center;
          }

        }
      }

      .commentAnimationClass{
          animation: commentAnimation 6s ease forwards;
      }

      .reply-page-container{
        position: relative;
        top: 21px;
        left: 80px;
        font-size: 12.5px;
        margin-bottom: 5px;

        .page-sum{
          margin-right: 15px;
        }

        .page-prev{
          cursor: pointer;
          margin-right: 8px;
          user-select: none;

        }
        .page-num{
          cursor: pointer;
          user-select: none;
          margin-right: 8px;
        }
        .page-next{
          cursor: pointer;
          user-select: none;
          margin-right: 8px;
        }
        .page-show{
          cursor: pointer;
          user-select: none;
        }

        .page-prev:hover{
          color: #00aeec;
        }
        .page-num:hover{
          color: #00aeec !important;
        }
        .page-next:hover{
          color: #00aeec;
        }
        .page-show:hover{
          color: #00aeec;
        }
      }

      .comment-line {
          position: relative;
          width: 620px;
          height: 1px;
          top: 32px;
          left: 80px;
          border-bottom: 1px solid #e3e5e7;
      }
      


    }

    .comment-list {
      
      .comment-item:hover  .comment-delete-btn-container{
        visibility: visible;
      }
    }

    .commentAnimationClass{
         animation: commentAnimation 6s ease forwards;
    }
    
  }
}

.delete-dialog-title {
  position: relative;
  top: 22px;
  font-size: 15.5px;
  font-weight: 600;
  text-align: center;
  margin-top: -20px;
  color: #18191c;
}

.delete-dialog-content {
  position: relative;
  padding: 10px;
  top: 24px;
  font-size: 14px;
  text-align: center;
  color: #61666d;
  margin-bottom: 37px;
  line-height: 1.5;
}

.delete-dialog-cancel-btn {
  -webkit-font-smoothing: antialiased;
  border: 1px solid #e3e5e7;
  background-color: white;
  color: #18191c;
  border-radius: 6px;
  box-sizing: border-box;
  cursor: pointer;
  height: 32px;
  font-size: 12.5px;
  outline: none;
  transition: all 0.2s;
  width: 126px;
  margin-left: 12px;
}

.delete-dialog-confirm-btn {
  -webkit-font-smoothing: antialiased;
  border: 1px solid #00aeec;
  background-color: #00aeec;
  color: white;
  border-radius: 6px;
  box-sizing: border-box;
  cursor: pointer;
  height: 32px;
  font-size: 12.5px;
  outline: none;
  transition: all 0.2s;
  width: 126px;
  margin-left: 12px;
}

.up-user-info {
  position: absolute;
  top: 40px;
  left: 38px;
  background-color: white;
  color: #a6acaf;
  border-radius: 10px;
  z-index: 1000;
  width: 350px;
  height: 235px;
  border: 1px solid #d3d3d3; /* 浅灰色边框 */
  box-shadow: 1px 0px 5px #e5e5e5;
  overflow: hidden;
  padding-bottom: 15px;
  margin-bottom: 50px;
}

.up-user-info2 {
  position: absolute;
  top: 35px;
  left: 55px;
  background-color: white;
  color: #a6acaf;
  border-radius: 10px;
  z-index: 1000;
  width: 350px;
  height: 235px;
  border: 1px solid #d3d3d3; /* 浅灰色边框 */
  box-shadow: 1px 0px 5px #e5e5e5;
  overflow: hidden;
  padding-bottom: 15px;
  margin-bottom: 50px;
}

.up-user-info-bg {
  width: 100%;
  height: 40%;
  background-size: cover;
  background-position: 50% 70%;
  background-repeat: no-repeat;
}

.up-user-info-container {
  height: 120px;
  padding-top: 5px;
  padding-left: 10px;
  padding-right: 20px;
  padding-bottom: 15px;
}

.up-user-info-avatar {
  display: flex;
  width: 47px;
  border-radius: 50%;
  cursor: pointer;
}

.up-user-info-container-header {
  display: flex;
  transform: translate(60px, -42px);
}

.up-user-info-name {
  font-size: 14.5px;
  cursor: pointer;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 4px;
}

.up-user-info-gender {
  width: 16px;
  height: 15px;
  margin-top: 2.5px;
  margin-right: 5px;
}

.up-user-info-level {
  width: 22px;
  height: 11px;
  margin-top: 3.5px;
}

.up-user-info-container-content {
  display: flex;
  transform: translate(60px, -30px);
  font-size: 12px;
}

.up-user-info-container-introduce {
  display: flex;
  transform: translate(60px, -20px);
  font-size: 12px;
  padding-right: 60px;
}

.up-user-info-introduce-content {
  display: -webkit-box; /* 必须使用这个 */
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  -webkit-line-clamp: 2;
  overflow: hidden; /* 隐藏超出部分 */
  word-wrap: break-word; /* 超出部分换行 */
  text-overflow: ellipsis; /* 显示省略号 */
}

.up-user-info-container-footer {
  display: flex;
  transform: translate(60px, -5px);
  user-select: none;
  width: 100%;
}

.up-user-info-container-footer-add-follow {
  width: 102px;
  font-size: 14px;
  height: 30px;
  background-color: #00aeec;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 5px;
  margin-right: 5px;
  color: white;
  cursor: pointer;
}

.up-user-info-container-footer-delete-follow {
  width: 102px;
  font-size: 14px;
  height: 30px;
  background-color: #e3e5e7;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 5px;
  margin-right: 5px;
  color: #959595;
  cursor: pointer;
}

.up-user-info-container-footer-delete-follow:hover {
  background-color: #f1f2f3;
}

.up-user-info-container-footer-message {
  width: 100px;
  font-size: 14px;
  height: 30px;
  border: 1px solid #62666c;
  border-radius: 5px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #62666c;
  cursor: pointer;
}

.up-user-info-container-footer-add-follow:hover {
  opacity: 0.9;
}

.up-user-info-container-footer-message:hover {
  color: #00aeec;
  border: 1px solid #00aeec;
}

.comment-footer-info{
  position: relative;
  width: 100%;
  display: flex;
  top: -55px;
  font-size: 13px;
  color: #9499a0;
  z-index: -10;
  justify-content: center;

}

.overlay-comment{
  position: absolute;
  width: 100%;
  top: -65px;
  height: 440px;
  z-index: 1000;
}

.overlay-comment::before{
  display: block;
  content: "";
  height: 80%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0) 0%,#fff 100%);
}

.overlay-comment::after{
  display: block;
  content: "";
  height: 20%;
  background: white;
}

.limit-mask-tip{
  position: absolute;
  top: 302px;
  left: -0.5px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 80px;
  width: calc(100% - 80px);
  height: 50px;
  border-radius: 6px;
  font-size: 13.5px;
  color: #00aeec;
  background-color: #dff6fd;
  transition: 0.2s;
  cursor: pointer;
  z-index: 1000;
}

.comment-input-container2{
  position: fixed !important;
  left: 172px !important;
  bottom: 0px !important;
  width: 700px;
  border-top: 0.5px solid #e3e5e7;
  min-height: 80.5px;
  background-color: white;
  z-index: 1000;

  .comment-avatar{
    top: 15px !important;
  }

  .comment-content-container{
    top: 15px !important;
  }

  .comment-placeholder{
    top: 30px !important;

  }

  .eit{
    height: 203px !important;
  }

}

@keyframes commentAnimation {
  0% {
    background-color: #DFF6FD;
  }
  100% {
    background-color: white;
  }

}

</style>
