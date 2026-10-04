<template>
  <div
    v-show="onloadPage && SelectVideoByIdVo.upVideo"
    class="video-page"
    :class="{ userselect: threeAnmationBeforeFlag }"
  >
    <div class="video-head">
      <head1
        :head2Flag="true"
        :loginDialogVisibleFlag="loginDialogVisibleFlag"
      />
    </div>
    <div class="video-body-container">
      <div class="video-body">
        <el-tooltip
          popper-class="custom-tooltip"
          class="dynamicContent-item-tooltip"
          effect="light"
          :content="SelectVideoByIdVo.upVideo.title"
          :show-after="300"
          placement="top"
          :offset="6"
          :show-arrow="false"
          :hide-after="0"
        >
          <div class="up-videoTitle">{{ SelectVideoByIdVo.upVideo.title }}</div>
        </el-tooltip>
        <div
          :class="{
            titleShowFlagCss: titleShowFlag,
            allowFlagcss1: SelectVideoByIdVo.upVideo.allowTwo === 0,
            allowFlagcss2: SelectVideoByIdVo.upVideo.allowTwo === 1,
          }"
          class="up-video-infos-container"
        >
          <img
            src="/img/播放量灰.png"
            style="
              width: 16.5px;
              height: 14px;
              transform: translate(0px, 2.5px);
              margin-right: 7px;
            "
          />
          <span style="font-size: 12.5px; margin-right: 15px">{{
            SelectVideoByIdVo.upVideo.playNumber
          }}</span>
          <img
            src="/img/弹幕灰.png"
            style="
              width: 16.5px;
              height: 14px;
              transform: translate(0px, 2.5px);
              margin-right: 6px;
            "
          />
          <span style="font-size: 12.5px; margin-right: 12px">{{
            SelectVideoByIdVo.upVideo.scrollingNumber
          }}</span>
          <span style="font-size: 12.5px">{{
            SelectVideoByIdVo.upVideo.createTime
          }}</span>
          <div
            v-if="SelectVideoByIdVo.upVideo.allowTwo === 0"
            style="
              font-size: 12.5px;
              transform: translate(32px, 0px);
              display: inline-block;
            "
          >
            <span class="prohibition-sign"></span>
            <span style="margin-left: -2px">未经作者授权，禁止转载</span>
          </div>
        </div>
        <div v-if="titleShowFlag" class="expand-icon-container">
          <img src="/img/展开.png" class="expand-icon" />
          <div class="show-up-videoTitle-container">
            <div class="show-up-videoTitle">
              {{ SelectVideoByIdVo.upVideo.title }}
            </div>
            <div style="transform: translate(0px, 5px); color: #8F9794">
              <img
                src="/img/播放量灰.png"
                style="
                  width: 16.5px;
                  height: 14px;
                  transform: translate(0px, 2.5px);
                  margin-right: 7px;
                "
              />
              <span style="font-size: 12.5px; margin-right: 15px">{{
                SelectVideoByIdVo.upVideo.playNumber
              }}</span>
              <img
                src="/img/弹幕灰.png"
                style="
                  width: 16.5px;
                  height: 14px;
                  transform: translate(0px, 2.5px);
                  margin-right: 6px;
                "
              />
              <span style="font-size: 12.5px; margin-right: 12px">{{
                SelectVideoByIdVo.upVideo.scrollingNumber
              }}</span>
              <span style="font-size: 12.5px">{{
                SelectVideoByIdVo.upVideo.createTime
              }}</span>
              <div
                v-if="SelectVideoByIdVo.upVideo.allowTwo === 0"
                style="
                  font-size: 12.5px;
                  transform: translate(32px, 0px);
                  display: inline-block;
                "
              >
                <span class="prohibition-sign"></span>
                <span style="margin-left: -2px">未经作者授权，禁止转载</span>
              </div>
            </div>
          </div>
        </div>
        <div
          style="
            transform: translate(0px, 22px);
            z-index: -1;
            position: absolute;
            height: 55px;
          "
        >
          <div
            class="up-user-avatar-container"
          >
            <img
              @click="openHome(1,SelectVideoByIdVo.upUser.id)"
              :src="SelectVideoByIdVo.upUser?.avatarAddress"
              class="up-user-avatar"
              @mouseover="delayUserInfo(1)"
              @mouseleave="delayUserInfo(0)"
            />
            <div
              v-show="nameOrAvatarHoverFlag !== 0"
              :style="{
                transform:
                  nameOrAvatarHoverFlag === 1
                    ? 'translate(0px, 0px)'
                    : 'translate(55px, -35px)',
              }"
              class="up-user-info"
              @mouseover="nameOrAvatarHoverFlag2=true"
              @mouseleave="nameOrAvatarHoverFlag2=false,delayUserInfo(0)"
            >
              <div v-if="!SelectVideoByIdVo.upUser.backgroundAddress" :style="{backgroundImage:'url(/img/page-bg.png)'}" class="up-user-info-bg"></div>
              <div v-else :style="{backgroundImage:`url(${SelectVideoByIdVo.upUser.backgroundAddress})`}" class="up-user-info-bg"></div>
              <div class="up-user-info-container">
                <img
                  @click="openHome(1,SelectVideoByIdVo.upUser.id)"
                  :src="SelectVideoByIdVo.upUser?.avatarAddress"
                  class="up-user-info-avatar"
                />
                <div class="up-user-info-container-header">
                  <span
                  @click="openHome(1,SelectVideoByIdVo.upUser.id)"
                    class="up-user-info-name"
                    :style="{
                      color:
                        SelectVideoByIdVo.upUser.grade > 4 ? '#F0A03C' : '#000',
                    }"
                    >{{ SelectVideoByIdVo.upUser.userName }}</span
                  >
                  <img
                    v-if="SelectVideoByIdVo.upUser.gender === 1"
                    src="/img/man.png"
                    class="up-user-info-gender"
                  />
                  <img
                    v-if="SelectVideoByIdVo.upUser.gender === 2"
                    src="/img/man.png"
                    class="up-user-info-gender"
                  />
                  <img
                    v-if="SelectVideoByIdVo.upUser.grade"
                    :src="'/img/' + SelectVideoByIdVo.upUser.grade + '级.png'"
                    class="up-user-info-level"
                  />
                </div>
                <div class="up-user-info-container-content">
                  <span @click="openHome(8,SelectVideoByIdVo.upUser.id)" style="color: #8F9794; cursor: pointer"
                    ><span style="color: black">{{
                      SelectVideoByIdVo.upUser.followNumber
                    }}</span>
                    &nbsp;关注</span
                  >
                  <span @click="openHome(9,SelectVideoByIdVo.upUser.id)" style="margin-left: 25px; color: #8F9794; cursor: pointer"
                    ><span style="color: black">{{
                      SelectVideoByIdVo.upUser.fansNumber
                    }}</span>
                    &nbsp;粉丝</span
                  >
                  <span style="margin-left: 25px; color: #8F9794;"
                    ><span style="color: black">{{
                      SelectVideoByIdVo.upUser.likeNumber
                    }}</span>
                    &nbsp;获赞</span
                  >
                </div>
                <div class="up-user-info-container-introduce">
                  <span class="up-user-info-introduce-content">
                    {{ SelectVideoByIdVo.upUser.introduce }}
                  </span>
                </div>
                <div class="up-user-info-container-footer">
                  <span
                    v-show="SelectVideoByIdVo.isFansFlag === 0"
                    class="up-user-info-container-footer-add-follow"
                    v-debounce
                    @click="addFollowAxios(SelectVideoByIdVo.upUser.id)"
                    ><img
                      src="/img/加关注.png"
                      style="width: 14px; margin-right: 4px"
                    />关注</span
                  >
                  <span
                    v-show="SelectVideoByIdVo.isFansFlag === 1"
                    class="up-user-info-container-footer-delete-follow"
                    @click="deleteFollowAxios(SelectVideoByIdVo.upUser.id)"
                    v-debounce
                    @mouseover="isDeleteFollowFlag = 1"
                    @mouseleave="isDeleteFollowFlag = 0"
                  >
                    <span v-show="isDeleteFollowFlag === 0">已关注</span>
                    <span v-show="isDeleteFollowFlag === 1">取消关注</span></span
                  >
                  <span class="up-user-info-container-footer-message"
                  @click="addDialogueF(SelectVideoByIdVo.upUser.id)"
                    >发消息</span
                  >
                </div>
              </div>
            </div>
          </div>
          <div class="up-user-header">
            <div
              class="up-user-name"
              @click="openHome(1,SelectVideoByIdVo.upUser.id)"
              @mouseover="delayUserInfo(2)"
              @mouseleave="delayUserInfo(0)"
              :style="{
                color: SelectVideoByIdVo.upUser.grade > 4 ? '#F0A03C' : '#000',
              }"
            >
              {{ SelectVideoByIdVo.upUser.userName }}
            </div>
            <span
              class="sendMessage-container"
              @mouseover="showSendMessageFlag = true"
              @mouseleave="showSendMessageFlag = false"
            >
              <img
                class="sendUpMessageImg"
                :src="showSendMessageFlag ? sendMessageBlue : sendMessageGray"
              />
              <span class="sendUpMessage" @click="addDialogueF(SelectVideoByIdVo.upUser.id)">发消息</span>
            </span>
          </div>
          <el-tooltip
            popper-class="custom-tooltip"
            class="dynamicContent-item-tooltip"
            effect="light"
            :show-after="300"
            :content="SelectVideoByIdVo.upUser.introduce"
            placement="left"
            :offset="24"
            :show-arrow="false"
            :hide-after="0"
          >
            <div class="up-user-content">
              {{ SelectVideoByIdVo.upUser.introduce }}
            </div>
          </el-tooltip>
          <div id="up-power-container" class="up-power-container">
            <img src="/img/charge.png" />
            <span style="margin-right: 2px">充</span><span>电</span>
          </div>
          <div
            v-show="SelectVideoByIdVo.isFansFlag === 0"
            class="up-add-follow-container"
            @click="addFollowAxios(SelectVideoByIdVo.upUser.id)"
          >
            <img
              src="/img/加关注.png"
              style="width: 12px; margin-right: 7px; margin-left: 3px"
            />关注 {{ SelectVideoByIdVo.upUser.fansNumber }}
          </div>
          <div
            v-show="SelectVideoByIdVo.isFansFlag === 1"
            v-debounce
            class="up-delete-follow-container"
            @click="deleteFollowAxios(SelectVideoByIdVo.upUser.id)"
          >
            <img
              src="/img/取消关注.png"
              style="width: 12px; margin-right: 7px; margin-left: 3px"
            />已关注 {{ SelectVideoByIdVo.upUser.fansNumber }}
          </div>
        </div>
        <div id="upvideocontainer" ref="upVideoContainer" class="up-video-container">
          <video
            :class="{
              setVideoMirrorFlag: setVideoMirrorFlag,
              VideoAllDisplayIngFlagVideo: intoVideoAllDisplayIngFlag,
            }"
            ref="upVideoPlayer"
            @play="pausedOrPlayVideoFlag = true"
            @pause="pausedOrPlayVideoFlag = false"
            @dblclick="toggleFullscreen"
            @click="pausedOrPlayUpVideo"
            @timeupdate="updateProgress"
            @mousemove="videoMove"
            @canplay="onVideoCanPlay"
            @waiting="videoWaitingF"
            @canplaythrough="onCanPlayThrough"
            :src="playVideoSrc"
            referrerpolicy="no-referrer"
            class="up-user-video-player"
          ></video>
          <div
            v-show="
              !setVideoAutoRePlayFlag &&
              !pausedOrPlayVideoFlag &&
              upVideoProgress === 100
            "
            :class="{ autoPlayVideoContainer1: intoVideoAllDisplayIngFlag }"
            class="autoPlayVideoContainer"
            @mousemove="videoMove"
          >
            <autoPlayVideo
              :intoVideoAllDisplayIngFlag="intoVideoAllDisplayIngFlag"
              :likeVideoClickFlag="likeVideoClickFlag"
              :videoThrowCoinClickFlag="videoThrowCoinClickFlag"
              :videoShareClickFlag="videoShareClickFlag"
              :videoCollectClickFlag="videoCollectClickFlag"
              style="position: absolute"
            />
          </div>
          <span
            v-show="intoVideoAllDisplayIngFlag && videoFeatureShowFlag"
            @click="pausedOrPlayUpVideo"
            class="intoVideoAllDisplayIngFlagVideoTitle"
            >{{ SelectVideoByIdVo.upVideo.title }}</span
          >
          <div
            v-show="videoFeatureShowFlag"
            :class="{
              intoVideoAllDisplayIngFlagavatarandaddFollow:
                intoVideoAllDisplayIngFlag,
            }"
            v-if="SelectVideoByIdVo.isFansFlag === 0"
            @mouseover="handlerCleanTime(), (videoLeave = true)"
            @mouseleave="videoLeave = false"
            v-debounce
            @click="addFollowAxios(SelectVideoByIdVo.upUser.id)"
            class="up-video-avatar-and-addFollow-container"
          >
            <img
              :src="SelectVideoByIdVo.upUser?.avatarAddress"
              class="up-video-avatar-and-addFollow-avatar"
            />
            <img
              src="/img/加关注.png"
              style="
                width: 10px;
                height: 10px;
                transform: translate(13.2px, 11.5px);
              "
            />
            <span
              style="
                color: white;
                font-size: 11px;
                transform: translate(20px, 9px);
              "
              >关注</span
            >
          </div>
          <!-- 进度条 -->
          <progress
            v-show="videoFeatureShowFlag && !intoVideoAllDisplayIngFlag"
            @mousedown.prevent="startProgressDrag($event, false)"
            @mousemove="updateupVideoProgressImgPosition($event, false)"
            @mouseover="
              (upVideoProgressImg = true), handlerCleanTime(), (videoLeave = true)
            "
            @mouseleave="(upVideoProgressImg = false), (videoLeave = false)"
            :value="upVideoProgress"
            max="100"
            class="up-VideoProgress"
          ></progress>
          <!-- 放大视频后的 -->
          <progress
            :class="{
              progressintoVideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag,
            }"
            v-show="videoFeatureShowFlag && intoVideoAllDisplayIngFlag"
            @mousedown.prevent="startProgressDrag($event, true)"
            @mousemove="updateupVideoProgressImgPosition($event, true)"
            @mouseover="
              (upVideoProgressImg = true), handlerCleanTime(), (videoLeave = true)
            "
            @mouseleave="(upVideoProgressImg = false), (videoLeave = false)"
            :value="upVideoProgress"
            max="100"
            class="up-VideoProgress"
          ></progress>
          <!-- 进度条图片 -->
          <img
            v-show="upVideoProgressImg && !intoVideoAllDisplayIngFlag"
            @mousedown.prevent="startProgressDrag($event, false)"
            @mouseover="upVideoProgressImg = true"
            @mouseleave="upVideoProgressImg = false"
            src="/img/视频进度图片.png"
            class="up-video-progress-img"
            :style="{
              left: `${upVideoProgress*676/100-6}px`,
            }"
          />
          <!-- 放大视频后的 -->
          <img
            :class="{
              progressImgintoVideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag,
            }"
            v-show="upVideoProgressImg && intoVideoAllDisplayIngFlag"
            @mousedown.prevent="startProgressDrag($event, true)"
            @mouseover="upVideoProgressImg = true"
            @mouseleave="upVideoProgressImg = false"
            src="/img/视频进度图片.png"
            class="up-video-progress-img"
            :style="{
              left: `${upVideoProgress*0.97+1.3}%`,
            }"
          />
          <!-- 指示器 -->
          <img
            v-show="upVideoProgressImg && !intoVideoAllDisplayIngFlag"
            @mousedown.prevent="startProgressDrag($event, false)"
            @mousemove="updateupVideoProgressImgPosition($event, false)"
            @mouseover="upVideoProgressImg = true"
            @mouseleave="upVideoProgressImg = false"
            src="/img/指示器上.png"
            class="pointer-top"
            :style="{
              left: `${upVideoProgressImgPosition}px`,
            }"
          />
          <div
            class="pointer-top-container"
            v-show="upVideoProgressImg && !intoVideoAllDisplayIngFlag"
            @mousedown.prevent="startProgressDrag($event, false)"
            @mousemove="updateupVideoProgressImgPosition($event, false)"
            @mouseover="upVideoProgressImg = true"
            @mouseleave="upVideoProgressImg = false"
          ></div>
          <img
            v-show="upVideoProgressImg && !intoVideoAllDisplayIngFlag"
            @mousedown.prevent="startProgressDrag($event, false)"
            @mousemove="updateupVideoProgressImgPosition($event, false)"
            @mouseover="upVideoProgressImg = true"
            @mouseleave="upVideoProgressImg = false"
            src="/img/指示器下.png"
            class="pointer-bottom"
            :style="{
              left: `${upVideoProgressImgPosition}px`,
            }"
          />
          <div
            class="pointer-bottom-container"
            v-show="upVideoProgressImg && !intoVideoAllDisplayIngFlag"
            @mousedown.prevent="startProgressDrag($event, false)"
            @mousemove="updateupVideoProgressImgPosition($event, false)"
            @mouseover="upVideoProgressImg = true"
            @mouseleave="upVideoProgressImg = false"
          ></div>
          <!-- 放大视频后的 -->
          <img
            :class="{
              progressPointerTopintoVideoAllDisplayIngFlag:
                intoVideoAllDisplayIngFlag,
            }"
            v-show="upVideoProgressImg && intoVideoAllDisplayIngFlag"
            @mousedown.prevent="startProgressDrag($event, true)"
            @mousemove="updateupVideoProgressImgPosition($event, true)"
            @mouseover="upVideoProgressImg = true"
            @mouseleave="upVideoProgressImg = false"
            src="/img/指示器上.png"
            class="pointer-top"
            :style="{
              left: `${upVideoProgressImgPosition - 3.5}px`,
            }"
          />
          <div
            :class="{
              topContainerintoVideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag,
            }"
            class="pointer-top-container"
            v-show="upVideoProgressImg && intoVideoAllDisplayIngFlag"
            @mousedown.prevent="startProgressDrag($event, true)"
            @mousemove="updateupVideoProgressImgPosition($event, true)"
            @mouseover="upVideoProgressImg = true"
            @mouseleave="upVideoProgressImg = false"
          ></div>
          <img
            :class="{
              progressPointerBottomintoVideoAllDisplayIngFlag:
                intoVideoAllDisplayIngFlag,
            }"
            v-show="upVideoProgressImg && intoVideoAllDisplayIngFlag"
            @mousedown.prevent="startProgressDrag($event, true)"
            @mousemove="updateupVideoProgressImgPosition($event, true)"
            @mouseover="upVideoProgressImg = true"
            @mouseleave="upVideoProgressImg = false"
            src="/img/指示器下.png"
            class="pointer-bottom"
            :style="{
              left: `${upVideoProgressImgPosition - 3.5}px`,
            }"
          />
          <div
            :class="{
              bottomContainerintoVideoAllDisplayIngFlag:
                intoVideoAllDisplayIngFlag,
            }"
            class="pointer-bottom-container"
            v-show="upVideoProgressImg && intoVideoAllDisplayIngFlag"
            @mousedown.prevent="startProgressDrag($event, true)"
            @mousemove="updateupVideoProgressImgPosition($event, true)"
            @mouseover="upVideoProgressImg = true"
            @mouseleave="upVideoProgressImg = false"
          ></div>
          <!-- 指示器时间 -->
          <span
            v-show="upVideoProgressImg && !intoVideoAllDisplayIngFlag"
            class="up-video-play-bottom-video-pointer-time"
            :style="{
              left: `${upVideoProgressImgPosition}px`,
            }"
            >{{ upVideoPointerMoveTime }}</span
          >
          <!-- 放大视频后的 -->
          <span
            :class="{
              pointerTImeintoVideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag,
            }"
            v-show="upVideoProgressImg && intoVideoAllDisplayIngFlag"
            class="up-video-play-bottom-video-pointer-time"
            :style="{
              left: `${upVideoProgressImgPosition - 19.5}px`,
            }"
            >{{ upVideoPointerMoveTime }}</span
          >
          <!-- 开始播放 -->
          <img
            :class="{
              startplaywhiteintoVideoAllDisplayIngFlag:
                intoVideoAllDisplayIngFlag,
            }"
            v-show="!pausedOrPlayVideoFlag && !upVideoStartPlayFlag"
            @mouseover="upVideoStartPlayFlag = true"
            src="/img/开始播放灰.png"
            class="up-video-start-play-white"
          />
          <img
            :class="{
              startplaywhiteintoVideoAllDisplayIngFlag:
                intoVideoAllDisplayIngFlag,
            }"
            v-show="!pausedOrPlayVideoFlag && upVideoStartPlayFlag"
            @click="playUpVideo"
            @mouseleave="upVideoStartPlayFlag = false"
            src="/img/开始播放白.png"
            class="up-video-start-play-white"
          />
          <img
            :class="{
              playwhiteintoVideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag,
            }"
            v-show="videoFeatureShowFlag && !pausedOrPlayVideoFlag"
            class="up-video-play-bottom-play"
            @click="playUpVideo"
            :src="upVideoPlayFlag ? upVideoPlayWhite : upVideoPlayGray"
            @mousemove="
              (upVideoPlayFlag = true),
                handlerCleanTime(),
                video,
                (videoLeave = true)
            "
            @mouseleave="(upVideoPlayFlag = false), (videoLeave = false)"
          />
          <img
            :class="{
              playwhiteintoVideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag,
            }"
            v-show="videoFeatureShowFlag && pausedOrPlayVideoFlag"
            class="up-video-play-bottom-play"
            @click="pausedUpVideo"
            :src="videoPausedFlag ? pausedVideoWhite : pausedVideoGray"
            @mousemove="
              (videoPausedFlag = true),
                handlerCleanTime(),
                video,
                (videoLeave = true)
            "
            @mouseleave="(videoPausedFlag = false), (videoLeave = false)"
          />
          <!-- 播放时间定位 -->
          <div
            class="videoPostion"
            v-if="
              history.watchCurrentTime !== undefined &&
              history.watchCurrentTime !== 0 &&
              closePostionFlag
            "
            :class="{
              closePostionFlagAllDisplay: intoVideoAllDisplayIngFlag,
            }"
          >
            <img
              class="img1"
              @mouseover="closePostionHoverFlag = true"
              @mouseleave="closePostionHoverFlag = false"
              src="/img/关闭视频定位.png"
              :class="{ closePostionHoverFlag: !closePostionHoverFlag }"
            />
            <img
              class="img2"
              @click="closePostionFlag = false"
              @mouseover="closePostionHoverFlag = true"
              @mouseleave="closePostionHoverFlag = false"
              src="/img/关闭视频定位粉.png"
              :class="{ closePostionHoverFlag: closePostionHoverFlag }"
            />
            <span>已为您定位至{{ history.watchVideoTime }}</span>
          </div>
          <!-- 时间 -->
          <div
            :class="{
              timeintoVideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag,
            }"
            v-show="videoFeatureShowFlag && !upVideoInputTimeFlag"
            @click="reInputUpVideoTime"
            @mousemove="(videoLeave = true), handlerCleanTime()"
            @mouseleave="videoLeave = false"
            class="up-video-play-bottom-video-time-container"
          >
            <span style="margin-right: 10px">{{ formattedTime }}</span>
            <span
              style="
                font-size: 11px;
                position: absolute;
                transform: translate(-5.5px);
              "
              >/</span
            >
            <span style="margin-left: 5px">{{ videoTotalTime }}</span>
          </div>
          <div
            :class="{
              inputTimeintoVideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag,
            }"
            v-show="videoFeatureShowFlag && upVideoInputTimeFlag"
            @mousemove="(videoLeave = true), handlerCleanTime()"
            @mouseleave="videoLeave = false"
            class="up-video-play-bottom-video-time-input-container"
          >
            <input
              type="text"
              class="up-video-play-bottom-video-time-input"
              @keydown.enter="enterUpVideoTime"
              v-model="inputUpVideoTimeValue"
            />
          </div>
          <!-- 弹幕  -->
          <!-- 打开或关闭弹幕  -->
          <img
            class="up-video-play-bottom-video-close-open-scrolling"
            style="width: 22px"
            :class="{
              openOrCloseScrollingintovideoAllDisplayIngFlag:
                intoVideoAllDisplayIngFlag,
            }"
            v-show="
              openOrCloseScrollingFlag &&
              intoVideoAllDisplayIngFlag &&
              videoFeatureShowFlag
            "
            :src="openScrollingWhite"
            @click="openOrCloseScrollingFlag = false"
          />
          <span
            class="up-video-play-bottom-video-close-open-scrolling-info"
            v-show="
              !openOrCloseScrollingFlag &&
              openOrCloseScrollingInfoFlag &&
              intoVideoAllDisplayIngFlag
            "
            >打开弹幕(d)</span
          >
          <img
            class="up-video-play-bottom-video-open-close-scrolling"
            :class="{
              openOrCloseScrollingintovideoAllDisplayIngFlag:
                intoVideoAllDisplayIngFlag,
            }"
            v-show="
              !openOrCloseScrollingFlag &&
              intoVideoAllDisplayIngFlag &&
              videoFeatureShowFlag
            "
            :src="closeScrollingWhite"
            @click="openOrCloseScrollingFlag = true"
          />
          <span
            class="up-video-play-bottom-video-close-open-scrolling-info"
            v-show="
              openOrCloseScrollingFlag &&
              openOrCloseScrollingInfoFlag &&
              intoVideoAllDisplayIngFlag
            "
            >关闭弹幕(d)</span
          >
          <!-- 弹幕设置 -->
          <img
            v-show="
              !openOrCloseScrollingFlag &&
              intoVideoAllDisplayIngFlag &&
              videoFeatureShowFlag
            "
            style="width: 23px; height: 21.5px"
            :class="{
              closeSettingScrollintovideoAllDisplayIngFlag:
                intoVideoAllDisplayIngFlag,
            }"
            class="up-video-play-bottom-video-close-setting-scrolling"
            :src="scrollingSettingWhite"
          />
          <div
            v-show="
              openOrCloseScrollingFlag &&
              intoVideoAllDisplayIngFlag &&
              videoFeatureShowFlag
            "
            :class="{
              settingScrollintovideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag,
            }"
            class="up-video-play-bottom-video-setting-scrolling-container"
          >
            <img
              style="width: 23px; height: 20.5px"
              class="up-video-play-bottom-video-setting-scrolling"
              :class="{settingScrollintovideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag,}"
              :src="scrollingSettingWhite"
            />
            <div
              class="up-video-play-bottom-video-setting-scrolling-info-container"
              :class="{intoUpVideoPlayBottomVideoSettingScrollingInfoContainer: intoVideoAllDisplayIngFlag,}"
            >
              <span
                style="
                  position: absolute;
                  transform: translate(18px, 10px);
                  cursor: pointer;
                  color: white;
                "
                >按类型屏蔽</span
              >
              <!-- 屏蔽滚动弹幕 -->
              <div
                class="roll-scrolling-container"
                @mouseover="scrollingRollHoverFlag = true"
                @mouseleave="scrollingRollHoverFlag = false"
                @click="scrollingRollOpenFlag = !scrollingRollOpenFlag"
              >
                <img
                  class="roll-scrolling-img"
                  v-show="!scrollingRollOpenFlag"
                  :src="
                    scrollingRollHoverFlag
                      ? scrollingRollWhite
                      : scrollingRollGray
                  "
                />
                <img
                  class="roll-scrolling-img"
                  v-show="scrollingRollOpenFlag"
                  :src="hiddenRollScrollingBlue"
                />
                <span
                  class="roll-scrolling-text"
                  :class="{
                    rollScrollingText: scrollingRollOpenFlag,
                    scrollingRollHoverFlag: scrollingRollHoverFlag,
                  }"
                  >滚动</span
                >
              </div>
              <!-- 屏蔽固定弹幕 -->
              <div
                class="fixed-scrolling-container"
                @mouseover="scrollingFilexdHoverFlag = true"
                @mouseleave="scrollingFilexdHoverFlag = false"
                @click="scrollingFilexdOpenFlag = !scrollingFilexdOpenFlag"
              >
                <img
                  class="fixed-scrolling-img"
                  v-show="!scrollingFilexdOpenFlag"
                  :src="
                    scrollingFilexdHoverFlag
                      ? hiddenScrollingFiexdWhite
                      : hiddenScrollingFiexdGray
                  "
                />
                <img
                  class="fixed-scrolling-img"
                  v-show="scrollingFilexdOpenFlag"
                  :src="hiddenScrollingFiexdBlue"
                />
                <span
                  class="fixed-scrolling-text"
                  :class="{
                    fixedScrollingText: scrollingFilexdOpenFlag,
                    scrollingFilexdHoverFlag: scrollingFilexdHoverFlag,
                  }"
                  >固定</span
                >
              </div>
              <!-- 勾选设置  -->
              <div
                class="chckbox-container"
                @mouseover="checkBoxHoverFlag = true"
                @mouseleave="checkBoxHoverFlag = false"
                @click="checkBoxOpenFlag = !checkBoxOpenFlag"
              >
                <img
                  class="setting-scrolling-checkbox"
                  v-show="!checkBoxOpenFlag"
                  :src="checkBoxHoverFlag ? checkBoxBlue : checkBoxWhite"
                />
                <img
                  class="setting-scrolling-checkbox"
                  v-show="checkBoxOpenFlag"
                  :src="checkBoxs"
                />
                <span
                  class="setting-scrolling-text"
                  :class="{ checkBoxHoverFlag: checkBoxHoverFlag }"
                  >弹幕速度同步播放倍数</span
                >
              </div>
              <!-- 滑块属性值设置 -->
              <div class="scrolling-slider-container">
                <div>
                  <span>显示区域</span>
                  <input
                    id="volume"
                    class="scrolling-display-slider"
                    :class="{
                      scrollingDisplayAreaValue:
                        parseInt(scrollingDisplayAreaValue) === 0,
                      scrollingDisplayAreaValue1:
                        parseInt(scrollingDisplayAreaValue) === 25,
                      scrollingDisplayAreaValue2:
                        parseInt(scrollingDisplayAreaValue) === 50,
                      scrollingDisplayAreaValue3:
                        parseInt(scrollingDisplayAreaValue) === 75,
                    }"
                    type="range"
                    min="0"
                    max="100"
                    step="25"
                    v-model="scrollingDisplayAreaValue"
                  />
                  <span
                    class="silder-pointer1"
                    @click="scrollingDisplayAreaValue = '0'"
                  ></span
                  ><span
                    class="silder-pointer2"
                    @click="scrollingDisplayAreaValue = '25'"
                  ></span
                  ><span
                    class="silder-pointer3"
                    @click="scrollingDisplayAreaValue = '50'"
                  ></span
                  ><span
                    class="silder-pointer4"
                    @click="scrollingDisplayAreaValue = '75'"
                  ></span
                  ><span
                    class="silder-pointer5"
                    @click="scrollingDisplayAreaValue = '100'"
                  ></span>
                  <span
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      word-break: normal !important;
                    "
                    >{{ scrollingDisplayAreaValue }}%</span
                  >
                </div>
                <div>
                  <span>不透明度</span>
                  <input
                    id="volume"
                    class="scrolling-display-slider"
                    :style="{
                      background: `linear-gradient(to right, #0FA68E ${scrollingDisplayOpacityValue}%, #424242 ${scrollingDisplayOpacityValue}%, #424242 170px)`,
                    }"
                    type="range"
                    min="0"
                    max="100"
                    step="1"
                    v-model="scrollingDisplayOpacityValue"
                  />
                  <span
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      word-break: normal !important;
                    "
                    >{{ scrollingDisplayOpacityValue }}%</span
                  >
                </div>
                <div>
                  <span>弹幕字号</span>
                  <input
                    id="volume"
                    class="scrolling-display-slider"
                    :style="{
                      background: `linear-gradient(to right, #0FA68E ${scrollingDisplayFontSizeValue}%, #424242 ${scrollingDisplayFontSizeValue}%, #424242 170px)`,
                    }"
                    type="range"
                    min="0"
                    max="100"
                    step="1"
                    v-model="scrollingDisplayFontSizeValue"
                  />
                  <span
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      word-break: normal !important;
                    "
                    >{{ scrollingDisplayFontSizeValue }}%</span
                  >
                </div>
                <div v-show="!checkBoxOpenFlag">
                  <span>弹幕速度</span>
                  <input
                    id="volume"
                    class="scrolling-display-slider"
                    :class="{
                      scrollingDisplayAreaValue:
                        scrollingDisplaySpeedValue === '0',
                      scrollingDisplayAreaValue1:
                        scrollingDisplaySpeedValue === '25',
                      scrollingDisplayAreaValue2:
                        scrollingDisplaySpeedValue === '50' ||
                        scrollingDisplaySpeedValue === 50,
                      scrollingDisplayAreaValue3:
                        scrollingDisplaySpeedValue === '75',
                      scrollingDisplayAreaValue4:
                        scrollingDisplaySpeedValue === '100',
                    }"
                    type="range"
                    min="0"
                    max="100"
                    step="25"
                    v-model="scrollingDisplaySpeedValue"
                  />
                  <span
                    class="silder-pointer1"
                    @click="scrollingDisplayAreaValue = '0'"
                  ></span
                  ><span
                    class="silder-pointer2"
                    @click="scrollingDisplaySpeedValue = '25'"
                  ></span
                  ><span
                    class="silder-pointer3"
                    @click="scrollingDisplaySpeedValue = '50'"
                  ></span
                  ><span
                    class="silder-pointer4"
                    @click="scrollingDisplaySpeedValue = '75'"
                  ></span
                  ><span
                    class="silder-pointer5"
                    @click="scrollingDisplaySpeedValue = '100'"
                  ></span>
                  <span
                    v-show="scrollingDisplaySpeedValue === '0'"
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      width: 24px;
                    "
                    >极慢</span
                  >
                  <span
                    v-show="scrollingDisplaySpeedValue === '25'"
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      width: 24px;
                    "
                    >较慢</span
                  >
                  <span
                    v-show="
                      scrollingDisplaySpeedValue === '50' ||
                      scrollingDisplaySpeedValue === 50
                    "
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      width: 24px;
                    "
                    >正常</span
                  >
                  <span
                    v-show="scrollingDisplaySpeedValue === '75'"
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      width: 24px;
                    "
                    >较快</span
                  >
                  <span
                    v-show="scrollingDisplaySpeedValue === '100'"
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      width: 24px;
                    "
                    >极快</span
                  >
                </div>
              </div>
            </div>
          </div>
          <!-- 弹幕输入框 -->
          <!-- 关闭 发送弹幕区域 -->
          <div
            v-show="
              !openOrCloseScrollingFlag &&
              intoVideoAllDisplayIngFlag &&
              videoFeatureShowFlag
            "
            class="up-video-play-bottom-video-open-scrolling-container2"
            :class="{
              inputContainerintoVideoAllDisplayIngFlag:
                intoVideoAllDisplayIngFlag,
            }"
          >
            <span
              style="color: #b9b7b8"
              :style="{ display: store.userId ? 'initial' : 'none' }"
              >已关闭弹幕</span
            >
            <span
              v-show="store.userId === null"
              style="color: #b9b7b8; position: absolute"
              >请先
              <span
                style="color: #0FA68E; cursor: pointer"
                @click="
                  loginDialogVisibleFlag === 0
                    ? (loginDialogVisibleFlag = 1)
                    : (loginDialogVisibleFlag = 0)
                "
                >登录</span
              >
              或
              <span
                style="color: #0FA68E; cursor: pointer"
                @click="
                  loginDialogVisibleFlag === 2
                    ? (loginDialogVisibleFlag = 3)
                    : (loginDialogVisibleFlag = 2)
                "
                >注册</span
              ></span
            >
            <span style="transform: translate(276.5px, -1px)"
              ><a
                class="scrolling-regulation1"
                target="_blank"
                href="https://www.qingmang.com/blackboard/help.html#/?qid=f80ff5461cc94a53a24fd1a42ce90fe0"
                >弹幕礼仪 ></a
              ></span
            >
            <span class="send-scrolling-btn-close1">发送</span>
          </div>
          <!-- 打开 发送弹幕区域 -->
          <div
            :class="{
              inputContainerintoVideoAllDisplayIngFlag:
                intoVideoAllDisplayIngFlag,
            }"
            v-show="
              openOrCloseScrollingFlag &&
              intoVideoAllDisplayIngFlag &&
              videoFeatureShowFlag
            "
            class="up-video-play-bottom-video-open-scrolling-container1"
          >
            <div
            :style="{ display: store.userId ? 'initial' : 'none' }"
              class="up-video-play-bottom-video-open-scrolling-color-container"
            >
              <img
                class="up-video-play-bottom-video-close-scrolling-color"
                :src="
                  showScrollingColorFlag
                    ? scrollingColorBlue
                    : scrollingColorWhite
                "
                @mouseover="showScrollingColorFlag = true"
                @mouseleave="showScrollingColorFlag = false"
              />
              <div
                :class="{ predefineColorsHoverFlag: predefineColorsHoverFlag }"
                class="up-video-play-bottom-video-open-scrolling-color-info-container"
              >
                <span>字号</span>
                <div
                  class="up-video-play-bottom-video-open-scrolling-color-info-container-font"
                >
                  <span
                    class="ss"
                    v-show="sendScrollingFontSize !== 16"
                    style="margin-right: 5px"
                    @click="sendScrollingFontSize = 16"
                    >小</span
                  >
                  <span
                    v-show="sendScrollingFontSize === 16"
                    style="margin-right: 5px; background-color: #0FA68E"
                    @click="sendScrollingFontSize = 16"
                    >小</span
                  >
                  <span
                    class="ss"
                    v-show="sendScrollingFontSize !== 20"
                    style="margin-left: 5px"
                    @click="sendScrollingFontSize = 20"
                    >标准</span
                  >
                  <span
                    v-show="sendScrollingFontSize === 20"
                    style="margin-left: 5px; background-color: #0FA68E"
                    @click="sendScrollingFontSize = 20"
                    >标准</span
                  >
                </div>
                <div style="margin-top: 15px">模式</div>
                <div
                  class="up-video-play-bottom-video-open-scrolling-color-info-container-pattern"
                >
                  <div class="scrolling-pattern-roll-container">
                    <img
                      class="aa"
                      v-show="scrollingPattern !== 1"
                      :src="
                        scrollingRollHoverFlag
                          ? scrollingRollWhite
                          : scrollingRollGray
                      "
                      @mouseover="scrollingRollHoverFlag = true"
                      @mouseleave="scrollingRollHoverFlag = false"
                      @click="scrollingPattern = 1"
                    />
                    <img
                      style="width: 24.5px; height: 22px"
                      v-show="scrollingPattern === 1"
                      :src="scrollingRollBlue"
                    />
                    <div
                      v-show="scrollingPattern !== 1"
                      class="cc"
                      @click="scrollingPattern = 1"
                    >
                      滚动
                    </div>
                    <div class="cc2" v-show="scrollingPattern === 1">滚动</div>
                  </div>
                  <div class="scrolling-pattern-top-container">
                    <img
                      class="aa"
                      v-show="scrollingPattern !== 2"
                      :src="
                        scrollingTopHoverFlag
                          ? hiddenScrollingFiexdWhite
                          : hiddenScrollingFiexdGray
                      "
                      @mouseover="scrollingTopHoverFlag = true"
                      @mouseleave="scrollingTopHoverFlag = false"
                      @click="scrollingPattern = 2"
                    />
                    <img
                      style="width: 25px; height: 22px"
                      v-show="scrollingPattern === 2"
                      :src="topScrollingBlue"
                    />
                    <div
                      v-show="scrollingPattern !== 2"
                      class="cc"
                      @click="scrollingPattern = 2"
                    >
                      顶部
                    </div>
                    <div class="cc2" v-show="scrollingPattern === 2">顶部</div>
                  </div>
                  <div class="scrolling-pattern-bottom-container">
                    <img
                      class="aa"
                      v-show="scrollingPattern !== 3"
                      :src="
                        scrollingBottomHoverFlag
                          ? bottomScrollingWhite
                          : bottomScrollingGray
                      "
                      @mouseover="scrollingBottomHoverFlag = true"
                      @mouseleave="scrollingBottomHoverFlag = false"
                      @click="scrollingPattern = 3"
                    />
                    <img
                      style="width: 24.5px; height: 22px"
                      v-show="scrollingPattern === 3"
                      :src="bottomScrollingBlue"
                    />
                    <div
                      class="cc"
                      v-show="scrollingPattern !== 3"
                      @click="scrollingPattern = 3"
                    >
                      底部
                    </div>
                    <div class="cc2" v-show="scrollingPattern === 3">底部</div>
                  </div>
                  <div style="transform: translate(0px, 60px)">颜色</div>
                  <el-color-picker
                    class="scrolling-color-picker"
                    :teleported="false"
                    v-model="sendScrollingColor"
                    :sizeshow-alpha="false"
                    :predefine="predefineColors"
                    @change="sendScrollingColorful = 0"
                  />
                  <div
                    class="scrolling-colorful-picker"
                    :class="{ 'scrolling-colorful-picker-active': sendScrollingColorful === 1 }"
                    @click="sendScrollingColorful = 1"
                  >
                    <span class="scrolling-colorful-picker-text"></span>
                  </div>
                </div>
              </div>
            </div>
            <input
              :style="{ display: store.userId ? 'initial' : 'none' }"
              class="up-video-play-bottom-video-close-scrolling-input1"
              type="text"
              maxlength="50"
              @focus="sendScrollingInputStatus = true"
              @blur="sendScrollingInputStatus = false"
              v-model="sendScrollingText"
              @keydown.enter="sendScrollingAxios"
              placeholder="发个友善的弹幕见证当下"
            />
            <span
              v-show="store.userId === null"
              style="color: #b9b7b8; position: absolute"
              >请先
              <span
                style="color: #0FA68E; cursor: pointer"
                @click="
                  loginDialogVisibleFlag === 0
                    ? (loginDialogVisibleFlag = 1)
                    : (loginDialogVisibleFlag = 0)
                "
                >登录</span
              >
              或
              <span
                style="color: #0FA68E; cursor: pointer"
                @click="
                  loginDialogVisibleFlag === 2
                    ? (loginDialogVisibleFlag = 3)
                    : (loginDialogVisibleFlag = 2)
                "
                >注册</span
              ></span
            >
            <span style="transform: translate(323.5px, -1px)"
              ><a
                class="scrolling-regulation1"
                target="_blank"
                href="https://www.qingmang.com/blackboard/help.html#/?qid=f80ff5461cc94a53a24fd1a42ce90fe0"
                >弹幕礼仪 ></a
              ></span
            >
            <span class="send-scrolling-btn-open1" @click="sendScrollingAxios"
              >发送</span
            >
          </div>
          <!-- 画质没做 -->
          <span
            :class="{
              imageQualityintoVideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag,
            }"
            v-show="videoFeatureShowFlag"
            @mousemove="(videoLeave = true), handlerCleanTime()"
            @mouseleave="videoLeave = false"
            class="up-video-play-bottom-video-image-quality"
            >1080P 高清</span
          >
          <!-- 倍速 -->
          <span
            :class="{ sppedVideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag }"
            v-show="videoFeatureShowFlag"
            @mousemove="(videoLeave = true), handlerCleanTime()"
            @mouseleave="videoLeave = false"
            class="up-video-play-bottom-video-spped"
            >倍速
            <div class="up-video-play-bottom-video-speed-container">
              <span
                @click="
                  (selectVideoSpped = 1), (upVideoPlayer.playbackRate = 2.0)
                "
                :class="{ selectVideoSpped: selectVideoSpped === 1 }"
                >2.0x</span
              >
              <span
                @click="
                  (selectVideoSpped = 2), (upVideoPlayer.playbackRate = 1.5)
                "
                :class="{ selectVideoSpped: selectVideoSpped === 2 }"
                >1.5x</span
              >
              <span
                @click="
                  (selectVideoSpped = 3), (upVideoPlayer.playbackRate = 1.25)
                "
                :class="{ selectVideoSpped: selectVideoSpped === 3 }"
                >1.25x</span
              >
              <span
                @click="(selectVideoSpped = 4), (upVideoPlayer.playbackRate = 1)"
                :class="{ selectVideoSpped: selectVideoSpped === 4 }"
                >1.0x</span
              >
              <span
                @click="
                  (selectVideoSpped = 5), (upVideoPlayer.playbackRate = 0.75)
                "
                :class="{ selectVideoSpped: selectVideoSpped === 5 }"
                >0.75x</span
              >
              <span
                @click="
                  (selectVideoSpped = 6), (upVideoPlayer.playbackRate = 0.5)
                "
                :class="{ selectVideoSpped: selectVideoSpped === 6 }"
                >0.5x</span
              >
            </div>
          </span>
          <!-- 音量 -->
          <div
            :class="{ audioVideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag }"
            class="up-video-play-bottom-video-audio-container"
            v-show="videoFeatureShowFlag"
            @mousemove="(videoLeave = true), handlerCleanTime()"
            @mouseleave="videoLeave = false"
          >
            <img
              v-show="videoFeatureShowFlag && videoCloseAudioOrOpenAudioFlag"
              @click="
                (videoCloseAudioOrOpenAudioFlag = false),
                  (upVideoPlayer.muted = true),
                  (closeAudioFlag = true)
              "
              @mousemove="
                (audioFlag = true), (videoLeave = true), handlerCleanTime()
              "
              @mouseleave="(audioFlag = false), (videoLeave = false)"
              :src="audioFlag ? audioWhite : audioGray"
              class="up-video-play-bottom-video-audio"
              :class="{intoAllDisplayFlagUpVideoPlayBottomVideoAudio: intoVideoAllDisplayIngFlag}"
            />
            <img
              v-show="videoFeatureShowFlag && !videoCloseAudioOrOpenAudioFlag"
              @click="
                (videoCloseAudioOrOpenAudioFlag = true),
                  (upVideoPlayer.muted = false),
                  (audioFlag = true)
              "
              @mousemove="
                (closeAudioFlag = true), (videoLeave = true), handlerCleanTime()
              "
              @mouseleave="(closeAudioFlag = false), (videoLeave = false)"
              :src="closeAudioFlag ? closeAudioWhite : closeAudioGray"
              class="up-video-play-bottom-video-colse-audio"
              :class="{intoAllDisplayFlagUpVideoPlayBottomVideoAudio2: intoVideoAllDisplayIngFlag}"
            />
            <div class="up-video-play-bottom-video-audio-value">
              {{ videoAudio }}
            </div>
            <div
              :class="{
                videoCloseAudioOrOpenAudioFlag: !videoCloseAudioOrOpenAudioFlag,
                intoUpVideoPlayBottomVideoSetAudio: intoVideoAllDisplayIngFlag
              }"
              class="up-video-play-bottom-video-set-audio"
            >
              <div>
                <input
                  id="volume"
                  :style="{
                    background: `linear-gradient(to right, #0FA68E ${videoAudio}%, white ${videoAudio}%, white 170px)`,
                  }"
                  class="volume-slider"
                  type="range"
                  min="0"
                  max="100"
                  step="1"
                  v-model="videoAudio"
                  @input="upVideoPlayer.volume = videoAudio / 100"
                />
              </div>
            </div>
          </div>
          <!-- 设置 -->
          <div
            :class="{ setVideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag }"
            v-show="videoFeatureShowFlag"
            class="up-video-play-bottom-video-setting-container"
            @mousemove="(videoLeave = true), handlerCleanTime()"
            @mouseleave="videoLeave = false"
          >
            <img
              :src="videoSettingFlag ? videoSettingWhite : videoSettingGray"
              @mousemove="
                (videoSettingFlag = true), (videoLeave = true), handlerCleanTime()
              "
              @mouseleave="(videoSettingFlag = false), (videoLeave = false)"
              class="up-video-play-bottom-video-setting"
            />
            <div class="up-video-play-bottom-video-set-container">
              <span @click="setVideoMirrorFlag = !setVideoMirrorFlag"
                >镜像画面
                <el-switch
                  @click.stop
                  class="custom-switch"
                  v-model="setVideoMirrorFlag"
              /></span>
              <span @click="setVideoAutoRePlayFlag = !setVideoAutoRePlayFlag"
                >洗脑循环
                <el-switch
                  @click.stop
                  class="custom-switch"
                  v-model="setVideoAutoRePlayFlag"
              /></span>
              <span @click="setVideoOpenAutoPlayFlag = !setVideoOpenAutoPlayFlag"
                >自动开播
                <el-switch
                  class="custom-switch"
                  @click.stop
                  v-model="setVideoOpenAutoPlayFlag"
              /></span>
            </div>
          </div>
          <!-- 画中画 -->
          <img
            :class="{
              fullScreenExitintoVideoAllDisplayIngFlag:
                intoVideoAllDisplayIngFlag,
            }"
            v-show="videoFeatureShowFlag"
            @click="togglePictureInPicture"
            :src="fullScreenExitFlag ? fullScreenExitWhite : fullScreenExitGray"
            @mousemove="
              (fullScreenExitFlag = true),
                (videoLeave = true),
                handlerCleanTime(),
                (showFullScreenExitFlag = true)
            "
            @mouseleave="
              (fullScreenExitFlag = false),
                (videoLeave = false),
                (showFullScreenExitFlag = false)
            "
            class="up-video-play-bottom-video-fullScreenExit"
          />
          <span
            :class="{
              fullScreenExitInfointoVideoAllDisplayIngFlag:
                intoVideoAllDisplayIngFlag,
            }"
            v-show="showFullScreenExitFlag && !openOrCloseFullScreenExitFlag"
            class="up-video-play-bottom-video-fullScreenExit-text"
            >开启画中画</span
          >
          <span
            :class="{
              fullScreenExitInfointoVideoAllDisplayIngFlag:
                intoVideoAllDisplayIngFlag,
            }"
            v-show="showFullScreenExitFlag && openOrCloseFullScreenExitFlag"
            class="up-video-play-bottom-video-fullScreenExit-text"
            >退出画中画</span
          >
          <!-- 宽屏模式没做 -->
          <img
            v-show="videoFeatureShowFlag && !intoVideoAllDisplayIngFlag"
            :src="fullScreenFlag ? fullScreenWhite : fullScreenGray"
            @mousemove="
              (fullScreenFlag = true), (videoLeave = true), handlerCleanTime()
            "
            @mouseleave="(fullScreenFlag = false), (videoLeave = false)"
            class="up-video-play-bottom-video-fullScreen"
          />
          <!-- 网页全屏没做 -->
          <img
            v-show="videoFeatureShowFlag && !intoVideoAllDisplayIngFlag"
            :src="webFullScreenFlag ? webFullScreenWhite : webFullScreenGray"
            @mousemove="
              (webFullScreenFlag = true), (videoLeave = true), handlerCleanTime()
            "
            @mouseleave="(webFullScreenFlag = false), (videoLeave = false)"
            class="up-video-play-bottom-video-webFullScreen"
          />
          <!-- 视频加载中... -->
          <div v-show="videoWaitingFlag" class="video-waiting">
              <div></div>
          </div>
          <!-- 进入全屏 -->
          <img
            :class="{ IntoVideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag }"
            v-show="videoFeatureShowFlag"
            @click="toggleFullscreen"
            :src="intoAllDisplayFlag ? intoAllDisplayWhite : intoAllDisplayGray"
            @mousemove="
              (intoAllDisplayFlag = true),
                (videoLeave = true),
                handlerCleanTime(),
                (intoVideoAllDisplayFlag = true)
            "
            @mouseleave="
              (intoAllDisplayFlag = false),
                (videoLeave = false),
                (intoVideoAllDisplayFlag = false)
            "
            class="up-video-play-bottom-video-intoAllDisplay"
          />
          <span
            v-show="intoVideoAllDisplayFlag && !intoVideoAllDisplayIngFlag"
            class="up-video-play-bottom-video-intoAllDisplay-text"
            >进入全屏(f)</span
          >
          <span
            :class="{
              InfoIntoVideoAllDisplayIngFlag: intoVideoAllDisplayIngFlag,
            }"
            v-show="intoVideoAllDisplayFlag && intoVideoAllDisplayIngFlag"
            class="up-video-play-bottom-video-intoAllDisplay-text"
            >退出全屏(f)</span
          >
          <div
            v-show="videoFeatureShowFlag"
            @mousemove="(videoLeave = true), handlerCleanTime()"
            @mouseleave="videoLeave = false"
            :class="{
              falseContainerIntoVideoAllDisplayIngFlag:
                intoVideoAllDisplayIngFlag,
            }"
            class="video-false-container"
          ></div>
          <!-- 弹幕 -->
          <div v-show="!intoVideoAllDisplayIngFlag">
            <scrollingList
              :list="ScrollingDataList"
              :getCurrentTime="getScrollingCurrentTime"
              :player="upVideoPlayer"
              :innerWidth="innerWidth"
              :openFlag="openOrCloseScrollingFlag"
              :rollOpenFlag="scrollingRollOpenFlag"
              :filexdOpenFlag="scrollingFilexdOpenFlag"
              :displayAreaValue="scrollingDisplayAreaValue"
              :displayFontSizeValue="scrollingDisplayFontSizeValue"
              :displayOpacityValue="scrollingDisplayOpacityValue"
              :speedValue="scrollingDisplaySpeedValue"
              :checkBoxOpenFlag="checkBoxOpenFlag"
              :userId="store.userId"
              @videoClick="pausedOrPlayUpVideo"
              @copyScrolling="copyScrolling"
            />
          </div>
          <!-- 放大视频后的弹幕 -->
          <div v-show="intoVideoAllDisplayIngFlag">
            <scrollingList
              :list="ScrollingDataList"
              :getCurrentTime="getScrollingCurrentTime"
              :player="upVideoPlayer"
              :allDisplay="true"
              :innerWidth="innerWidth"
              :openFlag="openOrCloseScrollingFlag"
              :rollOpenFlag="scrollingRollOpenFlag"
              :filexdOpenFlag="scrollingFilexdOpenFlag"
              :displayAreaValue="scrollingDisplayAreaValue"
              :displayFontSizeValue="scrollingDisplayFontSizeValue"
              :displayOpacityValue="scrollingDisplayOpacityValue"
              :speedValue="scrollingDisplaySpeedValue"
              :checkBoxOpenFlag="checkBoxOpenFlag"
              :userId="store.userId"
              @videoClick="pausedOrPlayUpVideo"
              @copyScrolling="copyScrolling"
            />
          </div>
          <!-- 快捷键提示  -->
          <div
            :class="{ keyInfoAllDisplay: intoVideoAllDisplayIngFlag }"
            v-show="keyInfo.length > 0"
            class="key-info"
          >
            {{ keyInfo }}
          </div>
          <!-- 快捷键音量提示 -->
          <div
            :class="{ audioInfoAllDisplay: intoVideoAllDisplayIngFlag }"
            v-show="audioInfoFlag"
            class="audio-info"
          >
            <img
              class="img1"
              v-show="parseInt(videoAudio) === 0"
              :src="closeAudioBlack"
            />
            <img
              class="img2"
              v-show="parseInt(videoAudio) !== 0"
              :src="audioBlack"
            />
            <span v-show="parseInt(videoAudio) !== 0">{{ videoAudio }}%</span>
            <span v-show="parseInt(videoAudio) === 0">静音</span>
          </div>
        </div>
        <div v-show="shareHover&&store.shareHover" class="video-share-container" @mouseover="shareHoverF(true)" @mouseleave="shareHoverF(false)"><videoShareC :shareHover="shareHover"/></div>
        <!-- 发送弹幕 -->
        <div class="up-video-play-bottom-video-scrolling-container">
          <img
            v-show="likeVideoImgFlag"
            src="/img/like-burst.png"
            class="up-video-controls-img0"
          />
          <span class="watch-num"
            ><span style="font-size: 12px">{{ watchingNumber }}</span>
            人正在看，已装填
            {{ SelectVideoByIdVo.upVideo.scrollingNumber }} 条弹幕</span
          >
          <!-- 打开或关闭弹幕  -->
          <img
            class="up-video-play-bottom-video-close-open-scrolling"
            v-show="!openOrCloseScrollingFlag"
            :src="
              showCloseScrollingFlag ? closeScrollingBlue : closeScrollingGray
            "
            @mouseover="
              (showCloseScrollingFlag = true),
                (openOrCloseScrollingInfoFlag = true)
            "
            @mouseleave="
              (showCloseScrollingFlag = false),
                (openOrCloseScrollingInfoFlag = false)
            "
            @click="openOrCloseScrollingFlag = true"
          />
          <span
            class="up-video-play-bottom-video-close-open-scrolling-info"
            v-show="!openOrCloseScrollingFlag && openOrCloseScrollingInfoFlag"
            >打开弹幕(d)</span
          >
          <img
            class="up-video-play-bottom-video-open-close-scrolling"
            v-show="openOrCloseScrollingFlag"
            :src="showOpenScrollingFlag ? openScrollingBlue : openScrollingGray"
            @mouseover="
              (showOpenScrollingFlag = true),
                (openOrCloseScrollingInfoFlag = true)
            "
            @mouseleave="
              (showOpenScrollingFlag = false),
                (openOrCloseScrollingInfoFlag = false)
            "
            @click="openOrCloseScrollingFlag = false"
          />
          <span
            class="up-video-play-bottom-video-close-open-scrolling-info"
            v-show="openOrCloseScrollingFlag && openOrCloseScrollingInfoFlag"
            >关闭弹幕(d)</span
          >
          <!-- 弹幕设置 -->
          <img
            v-show="!openOrCloseScrollingFlag"
            class="up-video-play-bottom-video-close-setting-scrolling"
            :src="scrollingSettingGray"
          />
          <div
            v-show="openOrCloseScrollingFlag"
            class="up-video-play-bottom-video-setting-scrolling-container"
          >
            <img
              class="up-video-play-bottom-video-setting-scrolling"
              :src="
                showScrollingSettingFlag
                  ? scrollingSettingBlue
                  : scrollingSettingGray
              "
              @mouseover="showScrollingSettingFlag = true"
              @mouseleave="showScrollingSettingFlag = false"
            />
            <div
              class="up-video-play-bottom-video-setting-scrolling-info-container"
            >
              <span
                style="
                  position: absolute;
                  transform: translate(18px, 10px);
                  cursor: pointer;
                  color: white;
                "
                >按类型屏蔽</span
              >
              <!-- 屏蔽滚动弹幕 -->
              <div
                class="roll-scrolling-container"
                @mouseover="scrollingRollHoverFlag = true"
                @mouseleave="scrollingRollHoverFlag = false"
                @click="scrollingRollOpenFlag = !scrollingRollOpenFlag"
              >
                <img
                  class="roll-scrolling-img"
                  v-show="!scrollingRollOpenFlag"
                  :src="
                    scrollingRollHoverFlag
                      ? scrollingRollWhite
                      : scrollingRollGray
                  "
                />
                <img
                  class="roll-scrolling-img"
                  v-show="scrollingRollOpenFlag"
                  :src="hiddenRollScrollingBlue"
                />
                <span
                  class="roll-scrolling-text"
                  :class="{
                    rollScrollingText: scrollingRollOpenFlag,
                    scrollingRollHoverFlag: scrollingRollHoverFlag,
                  }"
                  >滚动</span
                >
              </div>
              <!-- 屏蔽固定弹幕 -->
              <div
                class="fixed-scrolling-container"
                @mouseover="scrollingFilexdHoverFlag = true"
                @mouseleave="scrollingFilexdHoverFlag = false"
                @click="scrollingFilexdOpenFlag = !scrollingFilexdOpenFlag"
              >
                <img
                  class="fixed-scrolling-img"
                  v-show="!scrollingFilexdOpenFlag"
                  :src="
                    scrollingFilexdHoverFlag
                      ? hiddenScrollingFiexdWhite
                      : hiddenScrollingFiexdGray
                  "
                />
                <img
                  class="fixed-scrolling-img"
                  v-show="scrollingFilexdOpenFlag"
                  :src="hiddenScrollingFiexdBlue"
                />
                <span
                  class="fixed-scrolling-text"
                  :class="{
                    fixedScrollingText: scrollingFilexdOpenFlag,
                    scrollingFilexdHoverFlag: scrollingFilexdHoverFlag,
                  }"
                  >固定</span
                >
              </div>
              <!-- 勾选设置  -->
              <div
                class="chckbox-container"
                @mouseover="checkBoxHoverFlag = true"
                @mouseleave="checkBoxHoverFlag = false"
                @click="checkBoxOpenFlag = !checkBoxOpenFlag"
              >
                <img
                  class="setting-scrolling-checkbox"
                  v-show="!checkBoxOpenFlag"
                  :src="checkBoxHoverFlag ? checkBoxBlue : checkBoxWhite"
                />
                <img
                  class="setting-scrolling-checkbox"
                  v-show="checkBoxOpenFlag"
                  :src="checkBoxs"
                />
                <span
                  class="setting-scrolling-text"
                  :class="{ checkBoxHoverFlag: checkBoxHoverFlag }"
                  >弹幕速度同步播放倍数</span
                >
              </div>
              <!-- 滑块属性值设置 -->
              <div class="scrolling-slider-container">
                <div>
                  <span>显示区域</span>
                  <input
                    id="volume"
                    class="scrolling-display-slider"
                    :class="{
                      scrollingDisplayAreaValue:
                        parseInt(scrollingDisplayAreaValue) === 0,
                      scrollingDisplayAreaValue1:
                        parseInt(scrollingDisplayAreaValue) === 25,
                      scrollingDisplayAreaValue2:
                        parseInt(scrollingDisplayAreaValue) === 50,
                      scrollingDisplayAreaValue3:
                        parseInt(scrollingDisplayAreaValue) === 75,
                    }"
                    type="range"
                    min="0"
                    max="100"
                    step="25"
                    v-model="scrollingDisplayAreaValue"
                  />
                  <span
                    class="silder-pointer1"
                    @click="scrollingDisplayAreaValue = '0'"
                  ></span
                  ><span
                    class="silder-pointer2"
                    @click="scrollingDisplayAreaValue = '25'"
                  ></span
                  ><span
                    class="silder-pointer3"
                    @click="scrollingDisplayAreaValue = '50'"
                  ></span
                  ><span
                    class="silder-pointer4"
                    @click="scrollingDisplayAreaValue = '75'"
                  ></span
                  ><span
                    class="silder-pointer5"
                    @click="scrollingDisplayAreaValue = '100'"
                  ></span>
                  <span
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      word-break: normal !important;
                    "
                    >{{ scrollingDisplayAreaValue }}%</span
                  >
                </div>
                <div>
                  <span>不透明度</span>
                  <input
                    id="volume"
                    class="scrolling-display-slider"
                    :style="{
                      background: `linear-gradient(to right, #0FA68E ${scrollingDisplayOpacityValue}%, #424242 ${scrollingDisplayOpacityValue}%, #424242 170px)`,
                    }"
                    type="range"
                    min="0"
                    max="100"
                    step="1"
                    v-model="scrollingDisplayOpacityValue"
                  />
                  <span
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      word-break: normal !important;
                    "
                    >{{ scrollingDisplayOpacityValue }}%</span
                  >
                </div>
                <div>
                  <span>弹幕字号</span>
                  <input
                    id="volume"
                    class="scrolling-display-slider"
                    :style="{
                      background: `linear-gradient(to right, #0FA68E ${scrollingDisplayFontSizeValue}%, #424242 ${scrollingDisplayFontSizeValue}%, #424242 170px)`,
                    }"
                    type="range"
                    min="0"
                    max="100"
                    step="1"
                    v-model="scrollingDisplayFontSizeValue"
                  />
                  <span
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      word-break: normal !important;
                    "
                    >{{ scrollingDisplayFontSizeValue }}%</span
                  >
                </div>
                <div v-show="!checkBoxOpenFlag">
                  <span>弹幕速度</span>
                  <input
                    id="volume"
                    class="scrolling-display-slider"
                    :class="{
                      scrollingDisplayAreaValue:
                        scrollingDisplaySpeedValue === '0',
                      scrollingDisplayAreaValue1:
                        scrollingDisplaySpeedValue === '25',
                      scrollingDisplayAreaValue2:
                        scrollingDisplaySpeedValue === '50' ||
                        scrollingDisplaySpeedValue === 50,
                      scrollingDisplayAreaValue3:
                        scrollingDisplaySpeedValue === '75',
                      scrollingDisplayAreaValue4:
                        scrollingDisplaySpeedValue === '100',
                    }"
                    type="range"
                    min="0"
                    max="100"
                    step="25"
                    v-model="scrollingDisplaySpeedValue"
                  />
                  <span
                    class="silder-pointer1"
                    @click="scrollingDisplayAreaValue = '0'"
                  ></span
                  ><span
                    class="silder-pointer2"
                    @click="scrollingDisplaySpeedValue = '25'"
                  ></span
                  ><span
                    class="silder-pointer3"
                    @click="scrollingDisplaySpeedValue = '50'"
                  ></span
                  ><span
                    class="silder-pointer4"
                    @click="scrollingDisplaySpeedValue = '75'"
                  ></span
                  ><span
                    class="silder-pointer5"
                    @click="scrollingDisplaySpeedValue = '100'"
                  ></span>
                  <span
                    v-show="scrollingDisplaySpeedValue === '0'"
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      width: 24px;
                    "
                    >极慢</span
                  >
                  <span
                    v-show="scrollingDisplaySpeedValue === '25'"
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      width: 24px;
                    "
                    >较慢</span
                  >
                  <span
                    v-show="
                      scrollingDisplaySpeedValue === '50' ||
                      scrollingDisplaySpeedValue === 50
                    "
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      width: 24px;
                    "
                    >正常</span
                  >
                  <span
                    v-show="scrollingDisplaySpeedValue === '75'"
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      width: 24px;
                    "
                    >较快</span
                  >
                  <span
                    v-show="scrollingDisplaySpeedValue === '100'"
                    style="
                      position: absolute;
                      transform: translate(200px, 1px);
                      color: #d2d3d0;
                      width: 24px;
                    "
                    >极快</span
                  >
                </div>
              </div>
            </div>
          </div>
          <!-- 关闭 发送弹幕区域 -->
          <div
            v-show="!openOrCloseScrollingFlag"
            class="up-video-play-bottom-video-close-scrolling-container"
          >
            <span
              :style="{ display: store.userId ? 'initial' : 'none' }"
              style="color: #999999"
              >已关闭弹幕</span
            >
            <span
              v-show="store.userId === null"
              style="color: #999999; position: absolute"
              >请先
              <span
                style="color: #0FA68E; cursor: pointer"
                @click="
                  loginDialogVisibleFlag === 0
                    ? (loginDialogVisibleFlag = 1)
                    : (loginDialogVisibleFlag = 0)
                "
                >登录</span
              >
              或
              <span
                style="color: #0FA68E; cursor: pointer"
                @click="
                  loginDialogVisibleFlag === 2
                    ? (loginDialogVisibleFlag = 3)
                    : (loginDialogVisibleFlag = 2)
                "
                >注册</span
              ></span
            >
            <span style="transform: translate(187px, -1px)"
              ><a
                class="scrolling-regulation"
                target="_blank"
                href="https://www.qingmang.com/blackboard/help.html#/?qid=f80ff5461cc94a53a24fd1a42ce90fe0"
                >弹幕礼仪 ></a
              ></span
            >
            <span class="send-scrolling-btn-close">发送</span>
          </div>
          <!-- 打开 发送弹幕区域 -->
          <div
            v-show="openOrCloseScrollingFlag"
            class="up-video-play-bottom-video-open-scrolling-container"
          >
            <div
              class="up-video-play-bottom-video-open-scrolling-color-container"
              :style="{ display: store.userId ? 'initial' : 'none' }"
            >
              <img
                class="up-video-play-bottom-video-close-scrolling-color"
                :src="
                  showScrollingColorFlag ? scrollingColorBlue : scrollingColorGray
                "
                @mouseover="showScrollingColorFlag = true"
                @mouseleave="showScrollingColorFlag = false"
              />
              <div
                :class="{ predefineColorsHoverFlag: predefineColorsHoverFlag }"
                class="up-video-play-bottom-video-open-scrolling-color-info-container"
              >
                <span>字号</span>
                <div
                  class="up-video-play-bottom-video-open-scrolling-color-info-container-font"
                >
                  <span
                    class="ss"
                    v-show="sendScrollingFontSize !== 16"
                    style="margin-right: 5px"
                    @click="sendScrollingFontSize = 16"
                    >小</span
                  >
                  <span
                    v-show="sendScrollingFontSize === 16"
                    style="margin-right: 5px; background-color: #0FA68E"
                    @click="sendScrollingFontSize = 16"
                    >小</span
                  >
                  <span
                    class="ss"
                    v-show="sendScrollingFontSize !== 20"
                    style="margin-left: 5px"
                    @click="sendScrollingFontSize = 20"
                    >标准</span
                  >
                  <span
                    v-show="sendScrollingFontSize === 20"
                    style="margin-left: 5px; background-color: #0FA68E"
                    @click="sendScrollingFontSize = 20"
                    >标准</span
                  >
                </div>
                <div style="margin-top: 15px">模式</div>
                <div
                  class="up-video-play-bottom-video-open-scrolling-color-info-container-pattern"
                >
                  <div class="scrolling-pattern-roll-container">
                    <img
                      class="aa"
                      v-show="scrollingPattern !== 1"
                      :src="
                        scrollingRollHoverFlag
                          ? scrollingRollWhite
                          : scrollingRollGray
                      "
                      @mouseover="scrollingRollHoverFlag = true"
                      @mouseleave="scrollingRollHoverFlag = false"
                      @click="scrollingPattern = 1"
                    />
                    <img
                      style="width: 24.5px; height: 22px"
                      v-show="scrollingPattern === 1"
                      :src="scrollingRollBlue"
                    />
                    <div
                      v-show="scrollingPattern !== 1"
                      class="cc"
                      @click="scrollingPattern = 1"
                    >
                      滚动
                    </div>
                    <div class="cc2" v-show="scrollingPattern === 1">滚动</div>
                  </div>
                  <div class="scrolling-pattern-top-container">
                    <img
                      class="aa"
                      v-show="scrollingPattern !== 2"
                      :src="
                        scrollingTopHoverFlag
                          ? hiddenScrollingFiexdWhite
                          : hiddenScrollingFiexdGray
                      "
                      @mouseover="scrollingTopHoverFlag = true"
                      @mouseleave="scrollingTopHoverFlag = false"
                      @click="scrollingPattern = 2"
                    />
                    <img
                      style="width: 25px; height: 22px"
                      v-show="scrollingPattern === 2"
                      :src="topScrollingBlue"
                    />
                    <div
                      v-show="scrollingPattern !== 2"
                      class="cc"
                      @click="scrollingPattern = 2"
                    >
                      顶部
                    </div>
                    <div class="cc2" v-show="scrollingPattern === 2">顶部</div>
                  </div>
                  <div class="scrolling-pattern-bottom-container">
                    <img
                      class="aa"
                      v-show="scrollingPattern !== 3"
                      :src="
                        scrollingBottomHoverFlag
                          ? bottomScrollingWhite
                          : bottomScrollingGray
                      "
                      @mouseover="scrollingBottomHoverFlag = true"
                      @mouseleave="scrollingBottomHoverFlag = false"
                      @click="scrollingPattern = 3"
                    />
                    <img
                      style="width: 24.5px; height: 22px"
                      v-show="scrollingPattern === 3"
                      :src="bottomScrollingBlue"
                    />
                    <div
                      class="cc"
                      v-show="scrollingPattern !== 3"
                      @click="scrollingPattern = 3"
                    >
                      底部
                    </div>
                    <div class="cc2" v-show="scrollingPattern === 3">底部</div>
                  </div>
                  <div style="transform: translate(0px, 60px)">颜色</div>
                  <el-color-picker
                    class="scrolling-color-picker"
                    v-model="sendScrollingColor"
                    :sizeshow-alpha="false"
                    :predefine="predefineColors"
                    @change="sendScrollingColorful = 0"
                  />
                  <div
                    class="scrolling-colorful-picker"
                    :class="{ 'scrolling-colorful-picker-active': sendScrollingColorful === 1 }"
                    @click="sendScrollingColorful = 1"
                  >
                    <span class="scrolling-colorful-picker-text"></span>
                  </div>
                </div>
              </div>
            </div>
            <input
              class="up-video-play-bottom-video-close-scrolling-input"
              :style="{ display: store.userId ? 'initial' : 'none' }"
              type="text"
              maxlength="50"
              @focus="sendScrollingInputStatus = true"
              @blur="sendScrollingInputStatus = false"
              v-model="sendScrollingText"
              @keydown.enter="sendScrollingAxios"
              placeholder="发个友善的弹幕见证当下"
            />
            <span
              v-show="store.userId === null"
              style="color: #999999; position: absolute"
              >请先
              <span
                style="color: #0FA68E; cursor: pointer"
                @click="
                  loginDialogVisibleFlag === 0
                    ? (loginDialogVisibleFlag = 1)
                    : (loginDialogVisibleFlag = 0)
                "
                >登录</span
              >
              或
              <span
                style="color: #0FA68E; cursor: pointer"
                @click="
                  loginDialogVisibleFlag === 2
                    ? (loginDialogVisibleFlag = 3)
                    : (loginDialogVisibleFlag = 2)
                "
                >注册</span
              ></span
            >
            <span style="transform: translate(234px, -1px)"
              ><a
                class="scrolling-regulation"
                target="_blank"
                href="https://www.qingmang.com/blackboard/help.html#/?qid=f80ff5461cc94a53a24fd1a42ce90fe0"
                >弹幕礼仪 ></a
              ></span
            >
            <span class="send-scrolling-btn-open" @click="sendScrollingAxios"
              >发送</span
            >
          </div>
        </div>
        <div
          style="
            width: 700px;
            height: 5px;
            background-color: white;
            position: absolute;
            transform: translate(10px, 77px);
          "
        ></div>
        <!-- 连击 -->
        <div class="up-video-controls-container">
          <!-- 点赞 -->
          <el-tooltip
            popper-class="custom-tooltip"
            class="dynamicContent-item-tooltip"
            effect="light"
            :show-after="300"
            content="点赞（Q）"
            placement="bottom"
            :offset="14"
            :show-arrow="false"
            :hide-after="0"
          >
            <span
              class="up-video-controls-span"
              style="margin-left: 3px; margin-top: 3px"
              @mousedown="startThree"
              @mouseup="endThree(2)"
              @mouseleave="startThree(1), (videoLikeHoverFlag = false)"
              @mouseover="videoLikeHoverFlag = true"
            >
              <div
                class="up-video-controls-div1"
                :class="{
                  dotDisplay: threeAnmationAfterFlag,
                  dotDisplay2: threeAnmationBeforeFlag,
                }"
              >
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
              </div>
              <img
                v-show="!likeVideoClickFlag"
                class="up-video-controls-img1"
                :class="{
                  threeAnmationBefore: threeAnmationBeforeFlag,
                  threeAnmationAfter: threeAnmationAfterFlag,
                }"
                :src="videoLikeHoverFlag ? likeVideoBlue : likeVideo"
              />
              <img
                v-show="likeVideoClickFlag"
                class="up-video-controls-img1"
                :class="{
                  threeAnmationBefore: threeAnmationBeforeFlag,
                  threeAnmationAfter: threeAnmationAfterFlag,
                }"
                :src="likeVideoBlue"
              />
              <span
                :class="{ likeVideoClickFlag: likeVideoClickFlag }"
                class="up-video-controls-span1"
                >{{ SelectVideoByIdVo.upVideo.likeNumber }}</span
              >
            </span>
          </el-tooltip>
          <!-- 投币 -->
          <el-tooltip
            popper-class="custom-tooltip"
            class="dynamicContent-item-tooltip"
            effect="light"
            :show-after="300"
            content="投币 （W）"
            placement="bottom"
            :offset="14"
            :show-arrow="false"
            :hide-after="0"
          >
            <span
              class="up-video-controls-span"
              style="margin-left: 76.5px; margin-top: 6px"
              @mouseover="videoThrowCoinHoverFlag = true"
              @mouseleave="videoThrowCoinHoverFlag = false"
              @click="throwCoinDialog = true"
            >
              <div
                class="up-video-controls-div2"
                :class="{
                  dotDisplay: threeAnmationAfterFlag,
                  dotDisplay2: threeAnmationBeforeFlag,
                }"
              >
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
              </div>
              <img
                v-show="!videoThrowCoinClickFlag"
                class="up-video-controls-img2"
                :class="{
                  threeAnmationAfter: threeAnmationAfterFlag,
                }"
                :src="
                  videoThrowCoinHoverFlag ? videoThrowCoinBlue : videoThrowCoin
                "
              />
              <img
                v-show="videoThrowCoinClickFlag"
                class="up-video-controls-img2"
                :class="{
                  threeAnmationAfter: threeAnmationAfterFlag,
                }"
                :src="videoThrowCoinBlue"
              />
              <span
                :class="{ videoThrowCoinClickFlag: videoThrowCoinClickFlag }"
                class="up-video-controls-span2"
                >{{ SelectVideoByIdVo.upVideo.coinThrowNumber }}</span
              >
              <!-- 投币对话框 -->
              <el-dialog
                v-model="throwCoinDialog"
                append-to-body
                width="430"
                align-center
                class="throwCoinDialog"
              >
                <template #footer>
                  <div class="throw-coin-dialog-content">
                    <div v-show="throwCoinNumber === 1" class="throw-coin-title">
                      给创作者投上<span>1</span>枚硬币
                    </div>
                    <div v-show="throwCoinNumber === 2" class="throw-coin-title">
                      给创作者投上<span>2</span>枚硬币
                    </div>
                    <div
                      :class="{
                        throwCoinNumberConten1: throwCoinNumber === 1,
                        throwCoinNumberContenImg1: throwCoinNumber === 2,
                      }"
                      @click="throwCoinNumber = 1"
                      class="throw-coin-img-content1"
                    >
                      <span>1硬币</span>
                      <div class="overlay1"></div>
                      <img
                        class="img-static"
                        v-show="throwCoinNumber === 2"
                        src="/img/22-coin-ani-static.png"
                      />
                      <div class="overlay2"></div>
                    </div>
                    <div
                      :class="{
                        throwCoinNumberConten2: throwCoinNumber === 2,
                        throwCoinNumberContenImg2: throwCoinNumber === 1,
                      }"
                      @click="throwCoinNumber = 2"
                      class="throw-coin-img-content2"
                    >
                      <span>2硬币</span>
                      <div class="overlay1"></div>
                      <img
                        class="img-static"
                        v-show="throwCoinNumber === 1"
                        src="/img/33-coin-ani-static.png"
                      />
                      <div class="overlay2"></div>
                    </div>

                    <div
                      @click="likeVideoFlag = !likeVideoFlag"
                      class="throw-coin-check-box"
                    >
                      <img v-show="!likeVideoFlag" src="/img/投币复选框.svg" />
                      <img
                        v-show="likeVideoFlag"
                        src="/img/投币复选框选中.svg"
                      />
                      <span>同时点赞内容</span>
                    </div>

                    <el-button v-debounce
                      class="throw-coin-submit"
                      @click="videoThrowCoinAxios"
                    >
                      确定
                    </el-button>

                    <div class="throw-coin-fotter">经验值+20</div>
                  </div>
                </template>
              </el-dialog>
            </span>
          </el-tooltip>
          <!-- 收藏 -->
          <el-tooltip
            popper-class="custom-tooltip"
            class="dynamicContent-item-tooltip"
            effect="light"
            :show-after="300"
            content="收藏 （E）"
            placement="bottom"
            :offset="14"
            :show-arrow="false"
            :hide-after="0"
          >
            <span
              class="up-video-controls-span"
              style="margin-left: 77px; margin-top: 6px"
              @mouseover="videoCollectHoverFlag = true"
              @mouseleave="videoCollectHoverFlag = false"
              @click="collectDialogVisible = true"
            >
              <div
                class="up-video-controls-div3"
                :class="{
                  dotDisplay: threeAnmationAfterFlag,
                  dotDisplay2: threeAnmationBeforeFlag,
                }"
              >
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
                <div class="dot"></div>
              </div>
              <img
                v-show="!videoCollectClickFlag"
                class="up-video-controls-img3"
                :class="{
                  threeAnmationAfter: threeAnmationAfterFlag,
                }"
                :src="videoCollectHoverFlag ? videoCollectBlue : videoCollect"
              />
              <img
                v-show="videoCollectClickFlag"
                class="up-video-controls-img3"
                :class="{
                  threeAnmationAfter: threeAnmationAfterFlag,
                }"
                :src="videoCollectBlue"
              />
              <span
                :class="{ videoCollectClickFlag: videoCollectClickFlag }"
                class="up-video-controls-span3"
                >{{ SelectVideoByIdVo.upVideo.collectNumber }}</span
              >
              <!-- 收藏对话框 -->
              <el-dialog
                v-model="collectDialogVisible"
                width="420.5"
                center
                append-to-body
                class="collect-dialog"
              >
                <div class="collect-dialog-title">添加到收藏夹</div>
                <template #footer>
                  <div
                    class="collect-list-container"
                    @click="collectInputButtonFlag = false"
                  >
                    <div
                      class="collect-list-item"
                      v-for="collectInfo in collectInfoList"
                      :key="collectInfo.id"
                      @mouseover="collectCheckBoxHoverFlag = collectInfo.id"
                      @mouseleave="collectCheckBoxHoverFlag = collectInfo.id"
                      @click="addCollectVideo(collectInfo)"
                    >
                      <div>
                        <img
                          v-show="!collectInfo.flag"
                          class="collect-list-item-img"
                          :src="
                            collectCheckBoxHoverFlag === collectInfo.id
                              ? collectCheckBoxBlue
                              : collectCheckBox
                          "
                        />
                        <img
                          v-show="collectInfo.flag"
                          class="collect-list-item-img"
                          src="/img/收藏复选框勾选.png"
                        />
                        <span class="collect-list-item-name">{{
                          collectInfo.collectName
                        }}<span v-if="!collectInfo.status" class="collect-unPublic">[私密]</span></span>
                        <span class="collect-list-item-num"
                          >{{ collectInfo.videoNumber }}
                          <span v-show="collectInfo.collectName !== `默认收藏夹`"
                            >/1000</span
                          >
                        </span>
                      </div>
                    </div>
                  </div>
                  <div
                    v-show="!collectInputButtonFlag"
                    @click="collectInputButtonFlag = true"
                    class="dialog-footer-input-container"
                  >
                    <img src="/img/新建收藏.png" />
                    <span>新建收藏夹</span>
                  </div>
                  <div
                    v-show="collectInputButtonFlag"
                    class="dialog-footer-input-container1"
                  >
                    <input
                      ref="collectInputRef"
                      class="dialog-footer-input"
                      maxlength="20"
                      v-model="collectName"
                      placeholder="最多可输入20个字"
                      @keydown.enter="addCollect"
                      type="text"
                    />
                    <span class="dialog-footer-input-btn" @click="addCollect"
                      >新建
                      <div
                        v-show="newCreateCollectInfoFlag"
                        class="new-create-collect-info"
                      >
                        {{ newCreateCollectInfoContent }}
                      </div>
                    </span>
                  </div>
                  <div v-show="!collectSubimtFlag" class="dialog-footer-item">
                    确定
                  </div>
                  <div
                    v-show="collectSubimtFlag"
                    class="dialog-footer-item1"
                    @click="addCollectVideoAxios"
                  >
                    确定
                  </div>
                </template>
              </el-dialog>
            </span>
          </el-tooltip>
          <!-- 分享 -->
          <span
              class="up-video-controls-span"
              @click="shareHoverF2"
              style="margin-left: 76.5px; margin-top: 7px"
              @mouseover="videoShareHoverFlag = true"
              @mouseleave="videoShareHoverFlag = false,shareHoverF(false)"
            >
              <img
                v-show="!videoShareClickFlag"
                class="up-video-controls-img4"
                :src="videoShareHoverFlag ? videoShareBlue : videoShare"
              />
              <img
                v-show="videoShareClickFlag"
                class="up-video-controls-img4"
                :src="videoShareBlue"
              />
              <span
                class="up-video-controls-span4"
                :class="{ videoShareClickFlag: videoShareClickFlag }"
                >{{ SelectVideoByIdVo.upVideo.shareNumber }}</span
              >
          </span>
        </div>
        <div
          class="addWaitWatch"
          @mouseover="addWaitWatchHoverFlag = true"
          @mouseleave="addWaitWatchHoverFlag = false"
        >
          <img
            class="wait-img"
            :src="addWaitWatchHoverFlag ? addWaitWatchBlue : addWaitWatch"
          />
          <div class="wait-watch-info" @click="waitWatchAxios">
            <img src="/img/待看清单灰.png" />
            <span>待看清单</span>
          </div>
          <div v-show="waitWatchMsg.length > 0" class="waitWatchMsg">
            {{ waitWatchMsg }}
          </div>
        </div>
        <div style="position: absolute; z-index: -5;height: 0;">
          <!-- 弹幕列表 -->
          <div class="scrolling-list-container">
            <span class="scrolling-list-title">弹幕列表</span>
            <img src="/img/弹幕列表.png" class="scrolling-list-img" />
            <el-collapse class="scrolling-list">
              <el-collapse-item name="1">
                <div class="scrolling-list-item">
                  <div class="scrolling-list-item-top">
                    <span style="margin-left: 17px; cursor: pointer">时间</span>
                    <span style="margin-left: 27px; cursor: pointer"
                      >弹幕内容</span
                    >
                    <span style="margin-left: 122px; cursor: pointer"
                      >发送时间</span
                    >
                  </div>
                  <div class="scrolling-list-item-bottom-container">
                    <div
                      class="scrolling-list-item-bottom"
                      v-for="(scrollingData, index) in ScrollingDataList"
                      :key="index"
                      @mouseover="ScrollingReocationHoverFlag = scrollingData.id"
                      @mouseleave="
                        ScrollingReocationHoverFlag = -scrollingData.id
                      "
                    >
                      <span
                        style="
                          display: inline-block;
                          transform: translate(0px, -9px);
                          margin-left: 17px;
                          color: #5D6764;
                        "
                        >{{ videoTimeFormat(scrollingData.videoTime) }}</span
                      >
                      <el-tooltip
                        popper-class="custom-tooltip"
                        class="dynamicContent-item-tooltip"
                        effect="light"
                        :content="scrollingData.content"
                        placement="bottom"
                        :show-after="300"
                        :offset="12"
                        :show-arrow="false"
                        :hide-after="0"
                      >
                        <span
                          class="scrolling-list-item-content"
                          @click="
                            upVideoPlayer.currentTime =
                              scrollingData.videoTime - 0.1
                          "
                          >{{ scrollingData.content }}
                        </span>
                      </el-tooltip>
                      <span
                        v-show="
                          ScrollingReocationHoverFlag === scrollingData.id &&
                          store.userId === scrollingData.userId
                        "
                        class="scrolling-reocation"
                        @click="revocationScrollingAxios(scrollingData.id)"
                        >撤回
                      </span>
                      <span
                        v-show="
                          ScrollingReocationHoverFlag !== scrollingData.id ||
                          store.userId !== scrollingData.userId
                        "
                        class="scrolling-list-item-sendTime"
                        >{{ scrollingData.sendTime.slice(0, 16) }}</span
                      >
                    </div>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
          <!-- 自动播放视频第二部分 -->
          <auto-play-video-2 />
        </div>
        <!-- 视频介绍 -->
        <div
          class="video-content-container"
          :class="{ videoContentFlag: videoContentFlag1 && !videoContentFlag }"
        >
          <span v-html="SelectVideoByIdVo.upVideo.contentHtml"></span>
        </div>
        <div
          v-show="videoContentFlag1"
          @click="videoContentFlag = !videoContentFlag"
          class="video-content-container-switch"
        >
          <span v-show="!videoContentFlag">展开更多</span>
          <span v-show="videoContentFlag">收起</span>
        </div>
        <!-- 视频标签 -->
        <div class="video-tage-container">
          <span
            class="video-tage-item"
            @click="tagClick(index)"
            v-for="index in SelectVideoByIdVo.upVideo.tag?.split(',')"
            :key="index"
            >{{ index }}</span
          >
        </div>
        <comment />
        <Teleport
          :to="watchTogetherHost"
          :disabled="intoVideoAllDisplayIngFlag && !upVideoContainer"
        >
          <WatchTogetherPanel
            v-if="store.userInformation.id"
            v-show="watchTogetherVisible"
            :video-element="upVideoPlayer"
            :video-id="SelectVideoByIdVo.upVideo.id"
            :fullscreen="intoVideoAllDisplayIngFlag"
          />
        </Teleport>
      </div>
      <div style="position: fixed;top: 800px;">
        <el-backtop :right="5"/>
      </div>
    </div>
  </div>
</template>

<script>
import {
  onMounted,
  onBeforeUnmount,
  reactive,
  ref,
  watch,
  watchEffect,
  computed,
  nextTick,
} from "vue";
const sendMessageGray = "/img/发消息灰.png"
const sendMessageBlue = "/img/发消息蓝.png"
import apiClient from "../services/apiClient";
import { ElMessage } from "element-plus";
const upVideoPlayGray = "/img/播放灰.png"
const upVideoPlayWhite = "/img/播放白.png"
const intoAllDisplayGray = "/img/进入全屏灰.png"
const intoAllDisplayWhite = "/img/进入全屏白.png"
const audioGray = "/img/音量灰.png"
const audioWhite = "/img/音量白.png"
const videoSettingGray = "/img/视频设置灰.png"
const videoSettingWhite = "/img/视频设置白.png"
const fullScreenGray = "/img/宽屏模式灰.png"
const fullScreenWhite = "/img/宽屏模式白.png"
const fullScreenExitGray = "/img/画中画灰.png"
const fullScreenExitWhite = "/img/画中画白.png"
const webFullScreenGray = "/img/网页全屏灰.png"
const webFullScreenWhite = "/img/网页全屏白.png"
const closeAudioGray = "/img/关闭音量灰.png"
const closeAudioWhite = "/img/关闭音量白.png"
const pausedVideoGray = "/img/暂停灰.png"
const pausedVideoWhite = "/img/暂停白.png"
const closeScrollingGray = "/img/关闭弹幕灰.png"
const closeScrollingWhite = "/img/关闭弹幕白.png"
const closeScrollingBlue = "/img/关闭弹幕蓝.png"
const openScrollingGray = "/img/打开弹幕灰.png"
const openScrollingWhite = "/img/打开弹幕白.png"
const openScrollingBlue = "/img/打开弹幕蓝.png"
const scrollingSettingGray = "/img/弹幕设置灰.png"
const scrollingSettingWhite = "/img/弹幕设置白.png"
const scrollingSettingBlue = "/img/弹幕设置蓝.png"
const scrollingColorGray = "/img/弹幕颜色灰.png"
const scrollingColorWhite = "/img/弹幕颜色白.png"
const scrollingColorBlue = "/img/弹幕颜色蓝.png"
const scrollingRollGray = "/img/滚动模式灰.png"
const scrollingRollWhite = "/img/屏蔽滚动白.png"
const scrollingRollBlue = "/img/滚动模式蓝.png"
const hiddenRollScrollingBlue = "/img/屏蔽滚动蓝.png"
const hiddenScrollingFiexdGray = "/img/屏蔽固定灰.png"
const hiddenScrollingFiexdWhite = "/img/屏蔽固定白.png"
const hiddenScrollingFiexdBlue = "/img/屏蔽固定蓝.png"
const topScrollingBlue = "/img/顶部蓝.png"
const bottomScrollingBlue = "/img/底部蓝.png"
const bottomScrollingWhite = "/img/底部白.png"
const bottomScrollingGray = "/img/底部灰.png"
const checkBoxWhite = "/img/勾选框白.png"
const checkBoxs = "/img/勾选框.png"
const checkBoxBlue = "/img/勾选框蓝.png"
const likeVideo = "/img/视频点赞.png"
const likeVideoBlue = "/img/视频点赞蓝.png"
const videoThrowCoin = "/img/视频投币.png"
const videoThrowCoinBlue = "/img/视频投币蓝.png"
const videoShare = "/img/视频分享.png"
const videoShareBlue = "/img/视频分享蓝.png"
const videoCollect = "/img/视频收藏.png"
const videoCollectBlue = "/img/视频收藏蓝.png"
const collectCheckBox = "/img/收藏复选框.png"
const collectCheckBoxBlue = "/img/收藏复选框蓝.png"
const addWaitWatch = "/img/弹幕列表.png"
const addWaitWatchBlue = "/img/弹幕列表蓝.png"
const audioBlack = "/img/音量黑.png"
const closeAudioBlack = "/img/静音黑.png"
import head1 from "../components/mainHead.vue";
import { useGlobalStore } from "../store/store";
import autoPlayVideo from "./autoPlayVideo.vue";
import autoPlayVideo2 from "./autoPlayVideo2.vue";
import comment from "../comment/comment.vue";
import {addDialogue} from '../api/privateMessage/index';
import videoShareC from '../utils/videoShare.vue';
import scrollingList from '../utils/scrollingList.vue';
import WatchTogetherPanel from './WatchTogetherPanel.vue';
export default {
  name: "videoPage",
  components: {
    head1,
    autoPlayVideo,
    autoPlayVideo2,
    comment,
    videoShareC,
    scrollingList,
    WatchTogetherPanel,
  },
  setup() {
    const store = useGlobalStore();
    const userId = ref(0);
    const onloadPage = ref(false);
    const titleShowFlag = ref(false);
    const showSendMessageFlag = ref(false);
    const upVideoPlayer = ref(null);
    const upVideoPlayerss = ref(null);
    const upVideoProgress = ref(0);
    const SelectVideoByIdVo = reactive({
      upVideo: {},
      upUser: {},
      isFansFlag: "",
      userId: 0,
      likeVideoClickFlag: false,
      videoThrowCoinClickFlag: false,
      videoShareClickFlag: false,
      videoCollectClickFlag: false,
      collectName: [],
    });
    const isDeleteFollowFlag = ref(0);
    const videoFeatureShowFlag = ref(false);
    const upVideoProgressImg = ref(false);
    const upVideoProgressRight = ref(1);
    const upVideoProgressImgPosition = ref(0);
    const upVideoPlayFlag = ref(false);
    const intoAllDisplayFlag = ref(false);
    const fontFlag = ref(false);
    const audioFlag = ref(false);
    const videoSettingFlag = ref(false);
    const fullScreenFlag = ref(false);
    const fullScreenExitFlag = ref(false);
    const webFullScreenFlag = ref(false);
    const coleseFontFlag = ref(false);
    const closeAudioFlag = ref(false);
    const pausedOrPlayVideoFlag = ref(false);
    const videoPausedFlag = ref(false);
    const upVideoStartPlayFlag = ref(false);
    const upVideoTimeDuration = ref("00:00");
    const upVideoInputTimeFlag = ref(false);
    const upVideoPointerMoveTime = ref(null);
    const inputUpVideoTimeValue = ref(null);
    const selectVideoSpped = ref(4);
    const videoCloseAudioOrOpenAudioFlag = ref(true);
    const videoAudio = ref(100);
    const setVideoMirrorFlag = ref(false);
    const setVideoAutoRePlayFlag = ref(false);
    const setVideoOpenAutoPlayFlag = ref(false);
    const showFullScreenExitFlag = ref(false);
    const openOrCloseFullScreenExitFlag = ref(false);
    const intoVideoAllDisplayFlag = ref(false);
    const intoVideoAllDisplayIngFlag = ref(false);
    const upVideoContainer = ref(null);
    //远程视频：只存一条视频直链，用同一个video元素播放，流量走源站CDN
    const isRemoteVideo = computed(
      () =>
        SelectVideoByIdVo.upVideo &&
        SelectVideoByIdVo.upVideo.videoSource === 1 &&
        !!SelectVideoByIdVo.upVideo.remoteUrl
    );
    const remoteVideoSrc = computed(() =>
      isRemoteVideo.value ? SelectVideoByIdVo.upVideo.remoteUrl : ""
    );
    const playVideoSrc = computed(() =>
      isRemoteVideo.value
        ? remoteVideoSrc.value
        : SelectVideoByIdVo.upVideo.videoAddress
    );
    const watchTogetherHost = computed(
      () => (intoVideoAllDisplayIngFlag.value ? upVideoContainer.value : null) || 'body'
    );
    //普通模式常驻显示；大屏模式下播放时隐藏、暂停后延迟淡入，避免点击进度条时闪现
    const watchTogetherVisible = ref(true);
    let watchTogetherVisibleTimer = null;
    const WATCH_TOGGLE_DELAY = 280;
    watch(
      () => [intoVideoAllDisplayIngFlag.value, pausedOrPlayVideoFlag.value],
      ([fullscreen, playing]) => {
        window.clearTimeout(watchTogetherVisibleTimer);
        if (!fullscreen) {
          watchTogetherVisible.value = true;
          return;
        }
        if (playing) {
          watchTogetherVisible.value = false;
          return;
        }
        watchTogetherVisibleTimer = window.setTimeout(() => {
          watchTogetherVisible.value = true;
        }, WATCH_TOGGLE_DELAY);
      },
      { immediate: true }
    );
    const showScrollingSettingFlag = ref(false);
    const showScrollingColorFlag = ref(false);
    const showOpenScrollingFlag = ref(false);
    const showCloseScrollingFlag = ref(false);
    const openOrCloseScrollingFlag = ref(getLocalOpenOrCloseScrollingFlag());
    //记录弹幕开关，刷新后保持
    watch(openOrCloseScrollingFlag, (value) => {
      try {
        localStorage.setItem("videoOpenScrolling", value ? "1" : "0");
      } catch (error) {}
    });
    const openOrCloseScrollingInfoFlag = ref(false);
    const scrollingRollHoverFlag = ref(false);
    const scrollingRollOpenFlag = ref(false);
    const scrollingFilexdHoverFlag = ref(false);
    const scrollingFilexdOpenFlag = ref(false);
    const checkBoxHoverFlag = ref(false);
    const checkBoxOpenFlag = ref(false);
    const scrollingTopHoverFlag = ref(false);
    const scrollingBottomHoverFlag = ref(false);
    const scrollingPattern = ref(1);
    const scrollingDisplayAreaValue = ref(100);
    const scrollingDisplayOpacityValue = ref(100);
    const scrollingDisplayFontSizeValue = ref(50);
    const scrollingDisplaySpeedValue = ref(50);
    const sendScrollingFontSize = ref(20);
    const sendScrollingColor = ref("#ffffff");
    const sendScrollingColorful = ref(0);
    const predefineColorsHoverFlag = ref(false);
    const sendScrollingText = ref("");
    const ScrollingData = ref([]);
    const ScrollingDataList = reactive([]);
    const ScrollingReocationFlag = ref(false);
    const sendScrollingInputStatus = ref(false);
    const scrollingCurrentTime = ref(0);
    let scrollingAnimationFrameId = null;
    const ScrollingReocationHoverFlag = ref(false);
    const videoLikeHoverFlag = ref(false);
    const videoThrowCoinHoverFlag = ref(false);
    const videoShareHoverFlag = ref(false);
    const videoCollectHoverFlag = ref(false);
    const sppedList = ref([2, 1.5, 1.25, 1, 0.75, 0.5]);
    const threeAnmationBeforeFlag = ref(false);
    const threeAnmationAfterFlag = ref(false);
    const likeVideoClickFlag = ref(false);
    const videoThrowCoinClickFlag = ref(false);
    const videoShareClickFlag = ref(false);
    const likeVideoImgFlag = ref(false);
    const videoCollectClickFlag = ref(false);
    const collectInputButtonFlag = ref(false);
    const collectDialogVisible = ref(false);
    const collectCheckBoxHoverFlag = ref(false);
    const collectInfoList = reactive([]);
    let collectInfoList2 = [];
    const collectName = ref("");
    const newCreateCollectInfoFlag = ref(false);
    const newCreateCollectInfoContent = ref(null);
    const collectSubimtFlag = ref(false);
    const addWaitWatchHoverFlag = ref(false);
    const waitWatchMsg = ref("");
    const keyInfo = ref("");
    const audioInfoFlag = ref(false);
    const watchingNumber = ref(0);
    const throwCoinDialog = ref(false);
    const likeVideoFlag = ref(true);
    const throwCoinNumber = ref(1);
    const loginDialogVisibleFlag = ref(0);
    const videoContentFlag = ref(false);
    const videoContentFlag1 = ref(false);
    const autoPlayVideoFlag = ref(0);
    const closePostionHoverFlag = ref(false);
    const closePostionFlag = ref(true);
    const history = reactive({});
    const nameOrAvatarHoverFlag = ref(0);
    const shareHover=ref(false);
    const predefineColors = ref([
      "#000000",
      "#ff002a",
      "#ff00ea",
      "#ff0090",
      "#00a8ff",
      "#00ff06",
      "#f6ff00",
      "#ff7200",
      "#0024ff",
      "#aeff00",
    ]);
    let scrollFlag=true;
    const videoWaitingFlag=ref(true);
    const innerWidth = ref(window.innerWidth);
    //初始化数据
    onMounted(async () => {
      window.addEventListener("wheel",handleWheel);
      setTimeout(() => {
        if(scrollFlag)
        window.scrollTo({top: 0, behavior: "smooth"});
        window.removeEventListener("wheel",handleWheel);
      }, 1000);
      const urlParams = new URLSearchParams(window.location.search);
      autoPlayVideoFlag.value = urlParams.get("autoFlag");
      await getUserIp();
      await ChecklLogin();
      getVideoAndUser(true);
      getHistory();
      getEitList();
      setTimeout(() => {
        closePostionFlag.value = false;
      }, 5000);
      window.addEventListener("click", handleClickCollectInputOutside);
      window.addEventListener("mousemove", handleMouseMove);
      window.addEventListener("beforeunload", addHistoryAxios); //关闭网页发送新增历史记录请求
      window.addEventListener("click", inputUpVideoTime); //监听鼠标点击事件
      window.addEventListener("keydown", handleKeydown); //监听键盘事件
      window.addEventListener("fullscreenchange", handleFullscreenChange);
      window.addEventListener("webkitfullscreenchange", handleFullscreenChange); // Safari
      window.addEventListener("mozfullscreenchange", handleFullscreenChange); // Firefox
      window.addEventListener("MSFullscreenChange", handleFullscreenChange); // IE/Edge
      upVideoPlayer.value.addEventListener(
        "leavepictureinpicture",
        handlePictureInPictureChange,
      );
      let volumeStr = localStorage.getItem("videoAudio");
      let volume = parseFloat(volumeStr);

      if (isNaN(volume) || volume < 0) {
        volume = 1.0; // 默认音量
        videoAudio.value = 100;
      } else if (volume > 100) {
        volume = 1.0;
        videoAudio.value = 100;
      } else {
        volume = volume / 100;
        videoAudio.value = parseInt(volume * 100);
      }
      upVideoPlayer.value.volume = volume;
      setVideoOpenAutoPlayFlag.value =
        localStorage.getItem("setVideoOpenAutoPlayFlag") === "true"
          ? true
          : false;
      upDateOpenAutoPlayVideo();
      setTimeout(() => {
        updateScrolling();
      }, 1000);
      
    });

    function handleWheel(){
      scrollFlag=null;
    }

    //卸载时移除事件监听
    onBeforeUnmount(() => {
      stopScrollingClock();
      stopMoving();
      socket.close();
      window.removeEventListener("wheel",handleWheel);
      window.removeEventListener("click", handleClickCollectInputOutside);
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("click", inputUpVideoTime);
      window.removeEventListener("keydown", handleKeydown);
      window.removeEventListener("fullscreenchange", handleFullscreenChange);
      window.removeEventListener(
        "webkitfullscreenchange",
        handleFullscreenChange,
      );
      window.removeEventListener("mozfullscreenchange", handleFullscreenChange);
      window.removeEventListener("MSFullscreenChange", handleFullscreenChange);
      upVideoPlayer.value.removeEventListener(
        "leavepictureinpicture",
        handlePictureInPictureChange,
      );
      window.removeEventListener("beforeunload", addHistoryAxios);
      window.clearTimeout(watchTogetherVisibleTimer);
      //清理剩余的定时器与连击计时器
      clearTimeout(timer);
      clearTimeout(keyInfoTime);
      clearTimeout(audioInfoTime);
      clearTimeout(playNumberTime);
      clearTimeout(delayUserInfoTime);
      clearTimeout(delayUserInfoTime2);
      clearTimeout(delayShareTime);
      clearTimeout(AutoPlayVideoListTime);
      clearInterval(intervalId);
    });

    //获取用户ip和token
    async function getUserIp() {
      const response = await apiClient.get("/userIp/getUserIp");

      if (response.data.code === 1) store.setUserIp(response.data.data.userIp);
      store.setToken(response.data.data.token);
    }

    //获取@用户列表
    async function getEitList() {
      if(!store.userId)
      return;
      try {
        const response = await apiClient.get(`/user/getEit/${store.userId}`, {
          headers: {
            "Content-Type": "application/json",
            Authorization: store.token,
          },
        });

        if (response.data.code === 1)
          Object.assign(store.eitList, response.data.data);
      } catch (error) {}
    }

    //播放视频
    function playUpVideo() {
      if (upVideoPlayer.value.paused) {
        pausedOrPlayVideoFlag.value = true;
        //吞掉play()被快速pause打断时产生的rejection
        const playResult = upVideoPlayer.value.play();
        if (playResult && typeof playResult.catch === "function") {
          playResult.catch(() => {});
        }
        store.setReVideoPlayerFlag(false);
      }
    }

    //暂停视频
    function pausedUpVideo() {
      if (!upVideoPlayer.value.paused) {
        pausedOrPlayVideoFlag.value = false;
        upVideoPlayer.value.pause();
      }
    }

    //监听进度条
    watch(upVideoProgress, (value) => {
      if (value === 0)
        if (upVideoProgressRight.value) upVideoProgressRight.value = 0;
        else if (upVideoProgressRight.value) upVideoProgressRight.value = 1;
    });

    //判断视频简介的高度
    function handleVideoContentHeight() {
      // 1. 创建元素
      const div = document.createElement('div');
      // 2. 设置样式（必须有内容或样式才能看到高度）
      div.innerHTML = SelectVideoByIdVo.upVideo.contentHtml;
      div.style.fontSize = '14px';
      div.style.lineHeight = '24px';
      div.style.width = '690px';
      div.style.paddingBottom = '5px';
      div.style.visibility = 'hidden';
      // 4. 插入 DOM（必须插入才能获取尺寸）
      document.body.appendChild(div);
      // 5. 获取高度
      const height = div.offsetHeight;
      // 6. 使用后移除（可选）
      document.body.removeChild(div);
      videoContentFlag1.value=height>80;
    }

    function clampNumber(value, min, max) {
      return Math.min(Math.max(value, min), max);
    }

    //把"mm:ss"格式的时长转成秒，无法解析时返回0
    function parseVideoTimeToSeconds(videoTime) {
      if (!videoTime || typeof videoTime !== "string") return 0;
      const parts = videoTime.split(":");
      if (parts.length < 2) return 0;
      const seconds = Number(parts[0]) * 60 + Number(parts[1]);
      return Number.isFinite(seconds) && seconds > 0 ? seconds : 0;
    }

    function getVideoDurationSeconds() {
      //优先用播放器真实的duration，加载中duration为NaN时会走下面的兜底
      if (upVideoPlayer.value && Number.isFinite(upVideoPlayer.value.duration)) {
        return upVideoPlayer.value.duration;
      }

      //远程视频库里的videoTime可能是"00:00"，用metadata里取到的真实时长兜底
      if (videoDurationSeconds.value > 0) return videoDurationSeconds.value;

      //都没有时返回0表示时长未知
      return parseVideoTimeToSeconds(SelectVideoByIdVo.upVideo.videoTime);
    }

    //播放器metadata就绪时缓存真实时长，远程视频没有本地文件，只能从这里拿到
    const videoDurationSeconds = ref(0);
    const videoTotalTime = computed(() => {
      const seconds = getVideoDurationSeconds();
      if (!seconds) return SelectVideoByIdVo.upVideo?.videoTime || "00:00";
      const minutes = Math.floor(seconds / 60);
      const rest = Math.floor(seconds % 60);
      return `${minutes < 10 ? "0" : ""}${minutes}:${rest < 10 ? "0" : ""}${rest}`;
    });

    function formatPointerTime(time) {
      const safeTime = Math.max(parseInt(time), 0);
      const minutes = parseInt(safeTime / 60);
      const seconds = safeTime % 60;

      return `${minutes < 10 ? "0" : ""}${minutes}:${seconds < 10 ? "0" : ""}${seconds}`;
    }

    function getVisibleProgressRect() {
      const progressList = Array.from(document.querySelectorAll(".up-VideoProgress"));
      const progress = progressList.find((item) => item.offsetParent !== null);
      return progress?.getBoundingClientRect() || null;
    }

    function updateProgressByPointer(event, isAllDisplay = intoVideoAllDisplayIngFlag.value) {
      const rect = getVisibleProgressRect();
      const duration = getVideoDurationSeconds();
      if (!rect || !duration) return null;

      const relativeX = clampNumber(event.clientX - rect.left, 0, rect.width);
      const percent = relativeX / rect.width;
      const pointerTime = percent * duration;

      upVideoPointerMoveTime.value = formatPointerTime(pointerTime);
      upVideoProgressImgPosition.value = isAllDisplay
        ? rect.left + relativeX
        : relativeX - 3;

      return pointerTime;
    }

    const updateupVideoProgressImgPosition = (event, isAllDisplay = false) => {
      updateProgressByPointer(event, isAllDisplay);
    };

    // 汉字的个数
    function countChineseCharacters(str) {
      const chineseRegex = /[\u4e00-\u9fa5]/g; // 汉字的正则表达式
      const matches = str.match(chineseRegex); // 匹配汉字
      return matches ? matches.length : 0; // 返回匹配的个数，或者返回 0
    }

    //检查视频标题是否超出
    function checkVideoTitle(title) {
      const chinaFontLength = countChineseCharacters(title);
      if (title.length > 38)
        if (chinaFontLength > 35 || title.length > 53) {
          titleShowFlag.value = true;
        }
    }

    //根据id查询视频和用户
    async function getVideoAndUser(isInit = false) {
      try {
        const urlParams = new URLSearchParams(window.location.search);
        let videoId = urlParams.get("videoId");
        videoId = videoId.replace("BV", "");

        if (!videoId)
        {
          setTimeout(() => {
            window.location.href = "./videoNotFound";
          }, 200);
          return;
        }

        if (store.userId === null) userId.value = 0;
        else userId.value = store.userId;

        const response = await apiClient.get("/video/selectByVideoId", {
          params: {
            videoId: videoId,
            userId: userId.value,
          },
        });
        if (response.data.code === 1) {
          Object.assign(SelectVideoByIdVo, response.data.data);
          store.setCommentNumber(response.data.data.upVideo.commentNumber);
          store.setUpUserId(response.data.data.upUser.id);
          store.setSelectUpVideo(response.data.data);
          likeVideoClickFlag.value = response.data.data.likeVideoClickFlag;
          videoThrowCoinClickFlag.value =
            response.data.data.videoThrowCoinClickFlag;
          videoShareClickFlag.value = response.data.data.videoShareClickFlag;
          videoCollectClickFlag.value =
            response.data.data.videoCollectClickFlag;
          document.title =
            SelectVideoByIdVo.upVideo.title + "-青芒视频";
          checkVideoTitle(response.data.data.upVideo.title);
          onloadPage.value = true;
          handleVideoContentHeight();
        } else if (isInit) {
          setTimeout(() => {
            window.location.href = "./videoNotFound";
          }, 200);
        }
      } catch (error) {}
    }

    //检查是否登录
    async function ChecklLogin() {
      try {
        const response = await apiClient.get(
          `/user/checkLoginFlag/${store.userIp}`,
        );
        if (response.data.code === 1) {
          store.setUserId(response.data.data.id);
          store.setUserInformation(response.data.data);
        } else store.setUserId(null);
      } catch (error) {
        store.setUserId(null);
      }
    }

    //关注用户
    async function addFollowAxios(upUserId) {
      try {
        store.setAddFollowFlag(0);

        if (store.userId === null) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          return;
        }

        if (store.userId === upUserId) {
          ElMessage({
            message: "不能关注自己哦",
            type: "info",
            plain: true,
            duration: 1700,
          });
          return;
        }

        const response = await apiClient.post(
          "/video/addFollow",
          {
            followId: upUserId,
            fansId: store.userId,
          },
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: store.token,
            },
          },
        );
        if (response.data.code === 1) getVideoAndUser();
      } catch (error) {}
    }

    //取消关注用户
    async function deleteFollowAxios(upUserId) {
      try {
        store.setAddFollowFlag(0);

        if (store.userId === null) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          return;
        }

        const response = await apiClient.post(
          "/video/deleteFollow",
          {
            followId: upUserId,
            fansId: store.userId,
          },
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: store.token,
            },
          },
        );
        if (response.data.code === 1) getVideoAndUser();
      } catch (error) {}
    }

    //更新视频进度条
    const updateProgress = () => {
      const player = upVideoPlayer.value;
      if (!player) return;
      //duration为NaN/Infinity时（还在加载）不更新进度，避免算出NaN写进进度条
      const realDuration =
        Number.isFinite(player.duration) && player.duration > 0
          ? player.duration
          : 0;
      if (realDuration > 0) {
        updateScrollingCurrentTime();
        upVideoProgress.value = Math.floor(
          (player.currentTime / realDuration) * 100,
        );
        upVideoTimeDuration.value = Math.floor(player.currentTime);
      }
      //realDuration为0说明时长未知，此时currentTime与duration比较没有意义，
      //NaN比较恒为false，交给watcher和ended事件处理
      if (realDuration <= 0) return;
      if (setVideoAutoRePlayFlag.value) {
        if (player.ended || player.currentTime >= realDuration) {
          player.currentTime = 0;
          const playResult = player.play();
          if (playResult && typeof playResult.catch === "function") {
            playResult.catch(() => {});
          }
          pausedOrPlayVideoFlag.value = true;
        }
      } else if (player.ended || player.currentTime >= realDuration) {
        player.pause();
        pausedOrPlayVideoFlag.value = false;
      }
    };

    //格式化时间（原来在 computed 里写状态，改为 watchEffect）
    const formattedTime = ref("00:00");
    watchEffect(() => {
      const minutes = Math.floor(upVideoTimeDuration.value / 60);
      const seconds = upVideoTimeDuration.value % 60;
      //用播放器真实时长判断是否播完；时长未知(远程视频未加载完)时不能判定为结束，
      //否则每播放1秒就会被强制暂停，形成播放/暂停死循环
      const duration = getVideoDurationSeconds();
      if (duration > 0 && minutes * 60 + seconds >= duration) {
        if (!setVideoAutoRePlayFlag.value) {
          pausedOrPlayVideoFlag.value = false;
        }
      }
      if (upVideoTimeDuration.value !== "00:00")
        formattedTime.value = `${minutes < 10 ? "0" : ""}${minutes}:${
          seconds < 10 ? "0" : ""
        }${seconds} `;
      else formattedTime.value = "00:00";
    });

    let timer = null; // 用于存储定时器
    let isMoving = false; // 标志位，指示是否正在移动
    const videoLeave = ref(false);
    function videoMove() {
      if (videoLeave.value) return;

      //如果是弹幕输入状态不会关闭
      if (sendScrollingInputStatus.value) {
        videoFeatureShowFlag.value = true;
        return;
      }

      if (isMoving) return; // 如果正在执行，直接返回

      isMoving = true; // 设置为正在执行
      videoFeatureShowFlag.value = true; // 显示特性

      // 清除任何已有的定时器，防止闪烁
      if (timer) {
        clearTimeout(timer);
      }

      // 使用 setTimeout
      timer = setTimeout(() => {
        videoFeatureShowFlag.value = false; // 隐藏特性
        isMoving = false; // 重新设置为可执行状态
      }, 3000);
    }

    // 鼠标移出视频区域
    function handlerCleanTime() {
      clearTimeout(timer);
      isMoving = false; // 重新设置为可执行状态
    }

    // 双击会先触发两次 click 再触发 dblclick，这里延迟处理并允许被双击取消，
    // 避免双击进全屏时误触播放/暂停（也会触发 play() 被 pause() 打断的警告）
    let upVideoClickToggleTimer = null;

    // 暂停或播放视频
    function pausedOrPlayUpVideo() {
      if (upVideoClickToggleTimer) clearTimeout(upVideoClickToggleTimer);
      upVideoClickToggleTimer = setTimeout(() => {
        upVideoClickToggleTimer = null;
        if (!upVideoPlayer.value) return;
        if (upVideoPlayer.value.paused) {
          stopMoving();
          pausedOrPlayVideoFlag.value = true;
          const playResult = upVideoPlayer.value.play();
          if (playResult && typeof playResult.catch === "function") {
            playResult.catch(() => {});
          }
        } else {
          pausedOrPlayVideoFlag.value = false;
          upVideoPlayer.value.pause();
        }
      }, 250);
    }

    // 清除待执行的单击播放/暂停切换，避免与进度条拖拽、全屏切换互相打断
    function cancelClickToggle() {
      if (upVideoClickToggleTimer) {
        clearTimeout(upVideoClickToggleTimer);
        upVideoClickToggleTimer = null;
      }
    }

    //更改视频时间进度
    function seekUpVideo(time) {
      const duration = getVideoDurationSeconds();
      if (!upVideoPlayer.value || !duration) return;

      const nextTime = clampNumber(Number(time || 0), 0, duration);
      upVideoPlayer.value.currentTime = nextTime;
      upVideoProgress.value = (nextTime / duration) * 100;
      upVideoTimeDuration.value = Math.floor(nextTime);
      updateScrollingCurrentTime();
    }

    let isProgressImgMoving = false;
    let progressWasPlayingBeforeDrag = false;
    //点击或拖动时间进度条
    function startProgressDrag(event, isAllDisplay = false) {
      if (!event) return;
      //拖拽进度条前取消视频上待执行的单击播放/暂停切换，
      //否则250ms后自动play会与拖拽的pause竞争，触发
      //"play() request was interrupted by a call to pause()"
      cancelClickToggle();
      //拖动开始就复位加载标记，避免加载中的waiting事件把加载动画留在屏幕上
      videoLoadFlag = false;
      const pointerTime = updateProgressByPointer(event, isAllDisplay);
      if (pointerTime === null) return;

      progressWasPlayingBeforeDrag = pausedOrPlayVideoFlag.value;
      isProgressImgMoving = true;
      seekUpVideo(pointerTime);
      pausedUpVideo();
      window.addEventListener("mousemove", changeUpVideoTimeImg);
      window.addEventListener("mouseup", stopMoving);
    }

    //跟随鼠标移动滑动时间进度条
    function changeUpVideoTimeImg(event) {
      if (!isProgressImgMoving) return;
      const pointerTime = updateProgressByPointer(event, intoVideoAllDisplayIngFlag.value);
      if (pointerTime !== null) seekUpVideo(pointerTime);
    }

    //点击关闭滑动时间进度条
    function stopMoving() {
      if (isProgressImgMoving && progressWasPlayingBeforeDrag && upVideoPlayer.value?.paused) {
        pausedOrPlayVideoFlag.value = true;
        //play()返回Promise，若期间再次被pause打断会reject，
        //此处吞掉异常避免控制台报错
        const playResult = upVideoPlayer.value.play();
        if (playResult && typeof playResult.catch === "function") {
          playResult.catch(() => {});
        }
      }
      isProgressImgMoving = false;
      progressWasPlayingBeforeDrag = false;
      window.removeEventListener("mousemove", changeUpVideoTimeImg);
      window.removeEventListener("mouseup", stopMoving);
    }

    //输入视频时间
    function inputUpVideoTime(event) {
      if (
        !(
          document
            .querySelector(".up-video-play-bottom-video-time-container")
            .contains(event.target) ||
          document
            .querySelector(".up-video-play-bottom-video-time-input-container")
            .contains(event.target)
        )
      ) {
        if (!upVideoInputTimeFlag.value) return;

        upVideoInputTimeFlag.value = false;
      }

      const regex = /[^0-9:]/;
      const regex1 = /[^0-9]/;
      if (
        inputUpVideoTimeValue.value !== null &&
        inputUpVideoTimeValue.value !== ""
      ) {
        // 如果输入值包含非数字字符，直接设置当前时间
        if (!regex1.test(inputUpVideoTimeValue.value)) {
          seekUpVideo(Number(inputUpVideoTimeValue.value));
        }
        // 检查输入格式是否为 "mm:ss"
        else if (
          inputUpVideoTimeValue.value.length === 5 &&
          !regex.test(inputUpVideoTimeValue.value)
        ) {
          const time = inputUpVideoTimeValue.value.split(":");
          // 确保时间数组有效
          seekUpVideo(Number(time[0]) * 60 + Number(time[1]));
        } else {
          inputUpVideoTimeValue.value = upVideoPlayer.value.currentTime; // 输入格式不正确，清空输入框
        }
      }
    }

    //回车键输入视频时间
    function enterUpVideoTime() {
      const regex = /[^0-9:]/;
      const regex1 = /[^0-9]/;
      if (
        inputUpVideoTimeValue.value !== null &&
        inputUpVideoTimeValue.value !== ""
      ) {
        // 如果输入值包含非数字字符，直接设置当前时间
        if (!regex1.test(inputUpVideoTimeValue.value)) {
          upVideoInputTimeFlag.value = false;
          seekUpVideo(Number(inputUpVideoTimeValue.value));
        }
        // 检查输入格式是否为 "mm:ss"
        else if (
          inputUpVideoTimeValue.value.length === 5 &&
          !regex.test(inputUpVideoTimeValue.value)
        ) {
          const time = inputUpVideoTimeValue.value.split(":");
          // 确保时间数组有效
          upVideoInputTimeFlag.value = false;
          seekUpVideo(Number(time[0]) * 60 + Number(time[1]));
        } else {
          upVideoInputTimeFlag.value = false;
          inputUpVideoTimeValue.value = upVideoPlayer.value.currentTime; // 输入格式不正确，清空输入框
        }
      }
    }

    // 点击重新输入视频时间
    function reInputUpVideoTime() {
      upVideoInputTimeFlag.value = true;
      inputUpVideoTimeValue.value = parseInt(upVideoPlayer.value.currentTime);
    }

    //监视是否自动播放视频
    watch(setVideoOpenAutoPlayFlag, (newValue) => {
      if (newValue) localStorage.setItem("setVideoOpenAutoPlayFlag", true);
      else localStorage.setItem("setVideoOpenAutoPlayFlag", false);
    });

    //更新自动播放视频
    function upDateOpenAutoPlayVideo() {
      try {
        if (upVideoPlayer.value === null) return;
        if (
          setVideoOpenAutoPlayFlag.value ||
          parseInt(autoPlayVideoFlag.value) === 1
        ) {
         nextTick(()=>{
          upVideoPlayer.value.muted = true;
          pausedOrPlayVideoFlag.value = true;
          videoCloseAudioOrOpenAudioFlag.value = false;
          closeAudioFlag.value = true;
         })
          // 监听页面的可见性变化
          document.addEventListener("visibilitychange", () => {
            if (
              document.visibilityState === "visible" &&
              upVideoPlayer.value.readyState >= 3
            ) {
              upVideoPlayer.value.autoplay = true;
            }
          });
        }
      } catch (error) {}
    }

    // 切换/退出 画中画模式
    const togglePictureInPicture = async () => {
      if (upVideoPlayer.value) {
        try {
          if (document.pictureInPictureElement) {
            // 如果已经处于画中画模式，退出
            await document.exitPictureInPicture();
            openOrCloseFullScreenExitFlag.value = false;
          } else {
            // 否则，进入画中画模式
            await upVideoPlayer.value.requestPictureInPicture();
            openOrCloseFullScreenExitFlag.value = true;
          }
        } catch (error) {}
      }
    };
    // 切换/退出 全屏模式
    function toggleFullscreen() {
      // 双击进入全屏，取消尚未执行的单击播放/暂停切换
      cancelClickToggle();
      const videoContainer = document.getElementById("upvideocontainer");

      if (document.fullscreenElement) {
        // 退出全屏
        if (document.exitFullscreen) {
          document.exitFullscreen();
        } else if (document.webkitExitFullscreen) {
          document.webkitExitFullscreen(); // Safari
        } else if (document.msExitFullscreen) {
          document.msExitFullscreen(); // IE/Edge
        }
        intoVideoAllDisplayIngFlag.value = false;
      } else {
        // 进入全屏
        if (videoContainer.requestFullscreen) {
          videoContainer.requestFullscreen();
          intoVideoAllDisplayIngFlag.value = true;
        } else if (videoContainer.webkitRequestFullscreen) {
          videoContainer.webkitRequestFullscreen(); // Safari
          intoVideoAllDisplayIngFlag.value = true;
        } else if (videoContainer.msRequestFullscreen) {
          videoContainer.msRequestFullscreen(); // IE/Edge
          intoVideoAllDisplayIngFlag.value = true;
        }
      }
    }
    //退出全屏
    function toggleFullscreen2() {
      if (document.fullscreenElement) {
        // 退出全屏
        if (document.exitFullscreen) {
          document.exitFullscreen();
        } else if (document.webkitExitFullscreen) {
          document.webkitExitFullscreen(); // Safari
        } else if (document.msExitFullscreen) {
          document.msExitFullscreen(); // IE/Edge
        }
        intoVideoAllDisplayIngFlag.value = false;
      }
    }

    // 检测全屏状态变化
    const handleFullscreenChange = () => {
      if (document.fullscreenElement) {
        intoVideoAllDisplayIngFlag.value = true; // 更新状态
      } else {
        intoVideoAllDisplayIngFlag.value = false; // 更新状态
      }
    };

    let keyInfoTime;
    let audioInfoTime;
    let keyNoMoreClike = true;
    // 音量、进度、弹幕、静音这几个键需要长按连发，节流比其它键更短
    const FAST_REPEAT_KEYS = [
      "ArrowUp",
      "ArrowDown",
      "ArrowRight",
      "ArrowLeft",
      "D",
      "d",
      "M",
      "m",
    ];
    const FAST_REPEAT_THROTTLE = 100;
    const NORMAL_REPEAT_THROTTLE = 500;
    // 监听键盘状态
    const handleKeydown = async (event) => {
      const isFastRepeatKey = FAST_REPEAT_KEYS.includes(event.key);
      // 检查当前焦点元素
      var activeElement = document.activeElement;
      var isInputField =
        activeElement.tagName == "INPUT" ||
        activeElement.tagName == "TEXTAREA" ||
        activeElement.isContentEditable;

      if (isInputField || !keyNoMoreClike) return;

      event.preventDefault();
      //  空格键 播放
      if (event.key === " " && !isInputField) {
        // 阻止默认的空格键行为（滚动页面）
        pausedOrPlayUpVideo();
      }
      // F键 全屏
      else if ((event.key === "F" || event.key === "f") && !isInputField) {
        toggleFullscreen();
      }
      // D键 弹幕
      else if ((event.key === "D" || event.key === "d") && !isInputField) {
        openOrCloseScrollingFlag.value = !openOrCloseScrollingFlag.value;
        if (openOrCloseScrollingFlag.value) {
          clearTimeout(keyInfoTime);
          keyInfo.value = "开启弹幕";
          keyInfoTime = setTimeout(() => {
            keyInfo.value = "";
          }, 2000);
        } else {
          clearTimeout(keyInfoTime);
          keyInfo.value = "关闭弹幕";
          keyInfoTime = setTimeout(() => {
            keyInfo.value = "";
          }, 2000);
        }
      }
      // M键 静音
      else if ((event.key === "M" || event.key === "m") && !isInputField) {
        videoCloseAudioOrOpenAudioFlag.value =
          !videoCloseAudioOrOpenAudioFlag.value;
        if (!videoCloseAudioOrOpenAudioFlag.value) {
          upVideoPlayer.value.muted = true;
          keyInfo.value = "静音";
          clearTimeout(keyInfoTime);
          keyInfoTime = setTimeout(() => {
            keyInfo.value = "";
          }, 2000);
        } else {
          upVideoPlayer.value.muted = false;
          keyInfo.value = "关闭静音";
          clearTimeout(keyInfoTime);
          keyInfoTime = setTimeout(() => {
            keyInfo.value = "";
          }, 2000);
        }
      }
      // →键 快进5秒
      else if (event.key === "ArrowRight" && !isInputField) {
        event.preventDefault();
        seekUpVideo(upVideoPlayer.value.currentTime + 5);
      }
      // ←键 回退5秒
      else if (event.key === "ArrowLeft" && !isInputField) {
        event.preventDefault();
        seekUpVideo(upVideoPlayer.value.currentTime - 5);
      }
      // ↑键 音量+10
      else if (event.key === "ArrowUp" && !isInputField) {
        event.preventDefault();
        if (parseInt(videoAudio.value) < 91)
          videoAudio.value = parseInt(videoAudio.value) + 10;
        else videoAudio.value = 100;

        clearTimeout(audioInfoTime);
        upVideoPlayer.value.volume = parseInt(videoAudio.value) / 100;
        audioInfoFlag.value = true;
        audioInfoTime = setTimeout(() => {
          audioInfoFlag.value = false;
        }, 2000);
      }
      // ↓键 音量-10
      else if (event.key === "ArrowDown" && !isInputField) {
        event.preventDefault();
        if (parseInt(videoAudio.value) > 9)
          videoAudio.value = parseInt(videoAudio.value) - 10;
        else {
          videoAudio.value = 0;
        }

        upVideoPlayer.value.volume = parseInt(videoAudio.value) / 100;
        clearTimeout(audioInfoTime);
        audioInfoFlag.value = true;
        audioInfoTime = setTimeout(() => {
          audioInfoFlag.value = false;
        }, 2000);
      }
      // Q键点赞
      else if ((event.key === "Q" || event.key === "q") && !isInputField) {
        if (store.userId === null) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          return;
        }

        await likeVideoAxios();
        if (likeVideoClickFlag.value) {
          likeVideoImgFlag.value = true;
          ThreImgDisplay();
        }

        if (likeVideoClickFlag.value) {
          clearTimeout(keyInfoTime);
          keyInfo.value = "点赞成功";
          keyInfoTime = setTimeout(() => {
            keyInfo.value = "";
          }, 2000);
        } else {
          clearTimeout(keyInfoTime);
          keyInfo.value = "取消点赞";
          keyInfoTime = setTimeout(() => {
            keyInfo.value = "";
          }, 2000);
        }
      }
      // W键投币
      else if (
        (event.key === "W" || event.key === "w") &&
        !isInputField &&
        !intoVideoAllDisplayIngFlag.value
      ) {
        collectDialogVisible.value = false;
        throwCoinDialog.value = true;
      }
      // E键收藏
      else if (
        (event.key === "E" || event.key === "e") &&
        !isInputField &&
        !intoVideoAllDisplayIngFlag.value
      ) {
        throwCoinDialog.value = false;
        collectDialogVisible.value = true;
      }

      keyNoMoreClike = false;
      setTimeout(() => {
        keyNoMoreClike = true;
      }, isFastRepeatKey ? FAST_REPEAT_THROTTLE : NORMAL_REPEAT_THROTTLE);
    };

    //监视音量
    watch(videoAudio, () => {
      if (parseInt(videoAudio.value) === 0)
        videoCloseAudioOrOpenAudioFlag.value = false;
      else videoCloseAudioOrOpenAudioFlag.value = true;
      localStorage.setItem("videoAudio", videoAudio.value);
    });

    //监视画中画是否退出
    function handlePictureInPictureChange() {
      openOrCloseFullScreenExitFlag.value = false;
    }

    //监视视频暂停或播放
    watch(pausedOrPlayVideoFlag, (newValue) => {
      //该标志同时被play/pause事件和点击、拖拽、自动重播修改，
      //这里只在播放器实际状态与标志不一致时才下发play/pause，
      //否则加载中反复下发会触发"play() request was interrupted by a call to pause()"并让状态来回翻转
      const player = upVideoPlayer.value;
      if (player && !setVideoAutoRePlayFlag.value) {
        if (newValue) {
          if (player.paused) {
            const playResult = player.play();
            if (playResult && typeof playResult.catch === "function") {
              playResult.catch(() => {});
            }
          }
        } else if (!player.paused) {
          player.pause();
        }
      }
      if (newValue) startScrollingClock();
      else {
        updateScrollingCurrentTime();
        stopScrollingClock();
      }
      if (newValue) updateVideoPlayNumberAxios();
      else clearTimeout(playNumberTime);
    });

    //监视弹幕颜色是否有值
    watch(sendScrollingColor, (newValue) => {
      if (newValue === null) sendScrollingColor.value = "#FFFFFF";
    });

    //监视是否在取色器对话框上
    let popperContainer = null;
    const handleMouseMove = (event) => {
      // 元素还在时复用，避免每次移动鼠标都查询整篇文档
      if (popperContainer === null || !popperContainer.isConnected)
        popperContainer = document.querySelector(
          '[id^="el-popper-container-"]',
        );

      const el = popperContainer;
      if (el != null) {
        if (el.contains(event.target)) {
          predefineColorsHoverFlag.value = true;
        } else {
          predefineColorsHoverFlag.value = false;
        }
      }
    };

    //发送弹幕保存弹幕请求
    async function sendScrollingAxios() {
      try {
        if (sendScrollingText.value.trim().length === 0) return;

        if (store.userId === null) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          return;
        }
        ScrollingData.value.content = sendScrollingText.value.trim();
        ScrollingData.value.color = sendScrollingColor.value;
        ScrollingData.value.size = sendScrollingFontSize.value;
        ScrollingData.value.userId = store.userId;
        ScrollingData.value.videoId = SelectVideoByIdVo.upVideo?.id;
        ScrollingData.value.videoTime = upVideoPlayer.value.currentTime;
        ScrollingData.value.location = scrollingPattern.value;
        ScrollingData.value.colorful = sendScrollingColorful.value;
        const response = await apiClient.post(
          "/video/sendScrolling",
          ScrollingData,
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: store.token,
            },
          },
        );
        if (response.data.code === 1) {
          updateScrolling();
        }
        sendScrollingText.value = "";
      } catch (error) {}
    }

    //连接websocket实时更新滚动弹幕（支持断线重连）
    class ReconnectingWebSocket {
      constructor(url, protocols) {
        this.url = url;
        this.protocols = protocols;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.messageQueue = [];
        this.shouldReconnect = true;
        this.ws = null;
        this.onopen = null;
        this.onmessage = null;
        this.onclose = null;
        this.onerror = null;
        this.connect();
      }

      connect() {
        try {
          const ws = new WebSocket(this.url, this.protocols);
          this.ws = ws;

          ws.onopen = async (e) => {
            this.reconnectAttempts = 0;
            if (typeof this.onopen === 'function') this.onopen(e);
            while (this.messageQueue.length > 0 && ws.readyState === WebSocket.OPEN) {
              try {
                ws.send(this.messageQueue.shift());
              } catch (err) {
                break;
              }
            }
          };

          ws.onmessage = (e) => {
            if (typeof this.onmessage === 'function') this.onmessage(e);
          };

          ws.onclose = (e) => {
            if (typeof this.onclose === 'function') this.onclose(e);
            if (!this.shouldReconnect) return;
            this.reconnectAttempts++;
            if (this.maxReconnectAttempts && this.reconnectAttempts > this.maxReconnectAttempts) return;
            const delay = Math.min(30000, 1000 * Math.pow(2, this.reconnectAttempts - 1));
            setTimeout(() => this.connect(), delay);
          };

          ws.onerror = (e) => {
            if (typeof this.onerror === 'function') this.onerror(e);
          };
        } catch (err) {
          this.reconnectAttempts++;
          const delay = Math.min(30000, 1000 * Math.pow(2, this.reconnectAttempts - 1));
          setTimeout(() => this.connect(), delay);
        }
      }

      send(data) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
          this.ws.send(data);
        } else {
          this.messageQueue.push(data);
        }
      }

      close(code, reason) {
        this.shouldReconnect = false;
        if (this.ws) this.ws.close(code, reason);
      }

      addEventListener(...args) {
        return this.ws && this.ws.addEventListener(...args);
      }

      removeEventListener(...args) {
        return this.ws && this.ws.removeEventListener(...args);
      }
    }

    const socket = new ReconnectingWebSocket(`${location.protocol === 'https:' ? 'wss:' : 'ws:'}//${location.host}/api/scrolling`);
    socket.onopen = async () => {
      await getVideoAndUser();
      if(SelectVideoByIdVo.upVideo?.id != null)
      socket.send(SelectVideoByIdVo.upVideo.id + ":open");
    };

    //接收websocket实时更新滚动弹幕
    socket.onmessage = (event) => {
      let data = event.data;
      try {
        data = JSON.parse(event.data);
      } catch (error) {
        watchingNumber.value = event.data;
        return;
      }

      if (Array.isArray(data)) {
        ScrollingDataList.length = 0;
        Object.assign(ScrollingDataList, data);
        SelectVideoByIdVo.upVideo.scrollingNumber = ScrollingDataList.length;
        return;
      }

      if (event.data != null)
        watchingNumber.value = event.data;
    };

    // 更新滚动弹幕
    function updateScrolling() {
      socket.send(SelectVideoByIdVo.upVideo?.id + ":" + store.userId);
    }

    //读取本地保存的弹幕开关
    function getLocalOpenOrCloseScrollingFlag() {
      try {
        return localStorage.getItem("videoOpenScrolling") === "1";
      } catch (error) {
        return false;
      }
    }

    //格式化时间
    function videoTimeFormat(time) {
      const minutes = parseInt(time / 60);
      const seconds = time % 60;
      return (
        `${minutes < 10 ? "0" : ""}${minutes}:` +
        `${parseInt(seconds) < 10 ? "0" : ""}${parseInt(seconds)}`
      );
    }

    //点击弹幕赋值弹幕
    function copyScrolling(text) {
      navigator.clipboard.writeText(text);
      clearTimeout(keyInfoTime);
      keyInfo.value = "复制成功";
      keyInfoTime = setTimeout(() => {
        keyInfo.value = "";
      }, 2000);
    }

    //撤回弹幕
    async function revocationScrollingAxios(id) {
      try {
        if (store.userId === null) return;
        const response = await apiClient.delete("/video/revocationScrolling", {
          params: { scrollingId: id},
          headers: {
            "Content-Type": "application/json",
            Authorization: store.token,
          },
        });
        if (response.data.code === 1) {
          updateScrolling();
          ElMessage({
            message: "撤回成功",
            type: "info",
            plain: true,
            duration: 1700,
          });
        } else {
          ElMessage({
            message: "撤回失败",
            type: "info",
            plain: true,
            duration: 1700,
          });
        }
      } catch (error) {}
    }

    function updateScrollingCurrentTime() {
      if (!upVideoPlayer.value) return;
      scrollingCurrentTime.value = upVideoPlayer.value.currentTime || 0;
    }

    // 以函数形式传给弹幕组件，避免父组件渲染订阅 scrollingCurrentTime
    function getScrollingCurrentTime() {
      return scrollingCurrentTime.value;
    }

    function startScrollingClock() {
      stopScrollingClock();

      const tick = () => {
        updateScrollingCurrentTime();
        if (pausedOrPlayVideoFlag.value) {
          scrollingAnimationFrameId = requestAnimationFrame(tick);
        }
      };

      tick();
    }

    function stopScrollingClock() {
      if (scrollingAnimationFrameId !== null) {
        cancelAnimationFrame(scrollingAnimationFrameId);
        scrollingAnimationFrameId = null;
      }
    }

    //连击
    const StartThreeTime = ref(0);
    let intervalId;
    let threeForCount = 0;
    function startThree(a) {
      if (a === 1) {
        clearInterval(intervalId);
        threeAnmationBeforeFlag.value = false;
        return;
      }

      StartThreeTime.value = Date.now();
      intervalId = setInterval(() => {
        endThree(intervalId);
      }, 150);
    }

    function ThreImgDisplay() {
      setTimeout(() => {
        likeVideoImgFlag.value = false;
      }, 1500);
    }

    //结束连击
    async function endThree(a) {
      const endThreeTime = Date.now() - StartThreeTime.value;
      //发送点赞请求
      if (endThreeTime >= 1 && endThreeTime <= 150) {
        clearInterval(intervalId);
        if (!likeNoMoreClick) return;
        await likeVideoAxios();
        if (likeVideoClickFlag.value) {
          likeVideoImgFlag.value = true;
          ThreImgDisplay();
        }
        return;
      } else if (endThreeTime >= 500 && a !== 2) {
        threeAnmationBeforeFlag.value = true;
      }
      //发送连击请求
      if (endThreeTime >= 3000) {
        if (store.userId === null) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          return;
        }

        //清除定时器
        clearInterval(intervalId);
        getLTCAxios();
        videoLikeHoverFlag.value = true;
        threeAnmationBeforeFlag.value = false;
        threeAnmationAfterFlag.value = true;
        likeVideoClickFlag.value = true;
        likeVideoImgFlag.value = true;
        videoCollectClickFlag.value = true;
        setTimeout(() => {
          threeAnmationAfterFlag.value = false;
          setTimeout(() => {
            likeVideoImgFlag.value = false;
          }, 2500);
        }, 500);
      } else if (endThreeTime <= 3000 && a === 2) {
        clearInterval(intervalId);
        threeAnmationBeforeFlag.value = false;
      }
    }

    //发送点赞投币收藏请求
    async function getLTCAxios() {
      try {
        threeForCount++;
        if (threeForCount === 1 || !likeNoMoreClick) return;
        likeNoMoreClick = false;
        if (store.userId === null) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          return;
        }

        //自己不可以给自己投币
        if (store.userId === parseInt(SelectVideoByIdVo.upUser.id)) {
          SelectVideoByIdVo.videoThrowCoinClickFlag = false;
          ElMessage({
            message: "不能给自己投币",
            type: "info",
            plain: true,
            duration: 1700,
          });
        } else SelectVideoByIdVo.videoThrowCoinClickFlag = true;

        SelectVideoByIdVo.throwCoinNumber = throwCoinNumber.value;
        SelectVideoByIdVo.controlsType = 3;
        SelectVideoByIdVo.likeVideoClickFlag = likeVideoClickFlag.value;
        SelectVideoByIdVo.videoCollectClickFlag = videoCollectClickFlag.value;
        SelectVideoByIdVo.videoShareClickFlag = false;
        SelectVideoByIdVo.userId = store.userId;
        const response = await apiClient.post(
          "/video/getLTCAxios",
          SelectVideoByIdVo,
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: store.token,
            },
          },
        );
        if (response.data.code === 1) {
          SelectVideoByIdVo.length = 0;
          Object.assign(SelectVideoByIdVo, response.data.data);
          if (response.data.data.throwCoinResult === 1)
            videoThrowCoinClickFlag.value = true;
          else if (
            response.data.data.throwCoinResult !== 1 &&
            store.userId !== parseInt(SelectVideoByIdVo.upUser.id)
          ) {
            videoThrowCoinClickFlag.value = false;
            ElMessage({
              message: "硬币不够...",
              type: "info",
              plain: true,
              duration: 1700,
            });
          }
        }
        threeForCount = 0;
      } catch (error) {
        ElMessage({
          message: "未知错误",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
      likeNoMoreClick=true;
    }

    //发送点赞请求
    let likeNoMoreClick = true;
    async function likeVideoAxios() {
      try {
        if(!likeNoMoreClick)
          return;
          likeNoMoreClick = false;
        store.setLTCAFlag(0);
        if (store.userId === null) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          return;
        }
        likeVideoClickFlag.value = !likeVideoClickFlag.value;
        SelectVideoByIdVo.controlsType = 1;
        SelectVideoByIdVo.likeVideoClickFlag = likeVideoClickFlag.value;
        SelectVideoByIdVo.videoShareClickFlag = false;
        SelectVideoByIdVo.userId = store.userId;
        const response = await apiClient.post(
          "/video/getLTCAxios",
          SelectVideoByIdVo,
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: store.token,
            },
          },
        );
        if (response.data.code === 1) {
          SelectVideoByIdVo.length = 0;
          Object.assign(SelectVideoByIdVo, response.data.data);
        }
      } catch (error) {
        ElMessage({
          message: "未知错误",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
      setTimeout(() => {
        likeNoMoreClick = true;
      }, 1000);
    }

    //发送投币请求
    async function videoThrowCoinAxios() {
      try {
        if (store.userId === null) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          return;
        }

        if (videoThrowCoinClickFlag.value) {
          ElMessage({
            message: "您已经投过币了",
            type: "info",
            plain: true,
            duration: 1700,
          });
          return;
        }

        if (store.userId === parseInt(SelectVideoByIdVo.upUser.id)) {
          SelectVideoByIdVo.videoThrowCoinClickFlag = false;
          ElMessage({
            message: "不能给自己投币",
            type: "info",
            plain: true,
            duration: 1700,
          });
        } else SelectVideoByIdVo.videoThrowCoinClickFlag = true;

        if (likeVideoFlag.value) {
          likeVideoClickFlag.value = false;
          await likeVideoAxios();
          if (likeVideoClickFlag.value) {
            likeNoMoreClick = true;
            likeVideoImgFlag.value = true;
            ThreImgDisplay();
          }
        }

        SelectVideoByIdVo.videoShareClickFlag = false;
        SelectVideoByIdVo.controlsType = 2;
        SelectVideoByIdVo.throwCoinNumber = throwCoinNumber.value;
        SelectVideoByIdVo.userId = store.userId;
        const response = await apiClient.post(
          "/video/getLTCAxios",
          SelectVideoByIdVo,
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: store.token,
            },
          },
        );
        if (response.data.code === 1) {
          SelectVideoByIdVo.length = 0;
          Object.assign(SelectVideoByIdVo, response.data.data);
          if (response.data.data.throwCoinResult === 1)
            videoThrowCoinClickFlag.value = true;
          else if (
            response.data.data.throwCoinResult !== 1 &&
            store.userId !== parseInt(SelectVideoByIdVo.upUser.id)
          ) {
            videoThrowCoinClickFlag.value = false;
            ElMessage({
              message: "硬币不够...",
              type: "info",
              plain: true,
              duration: 1700,
            });
          }
        }
        throwCoinDialog.value = false;
        throwCoinNumber.value = 1;
        likeVideoFlag.value = true;
      } catch (error) {
        ElMessage({
          message: "未知错误",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
    }

    //监视收藏输入按钮是否打开
    const collectInputRef = ref(null);
    watch(collectInputButtonFlag, async (newValue) => {
      if (newValue) {
        await nextTick(); // 确保 DOM 已更新
        collectInputRef.value.focus();
      } else {
        await nextTick();
        collectName.value = "";
      }
    });

    //监听鼠标是否点击输入框之外的关闭收藏输入框
    const handleClickCollectInputOutside = (event) => {
      const target = event.target;
      if (
        target.classList.contains("collect-dialog") &&
        collectInputButtonFlag.value
      ) {
        collectInputButtonFlag.value = false;
      }
      if (target.classList.contains("time-span")) {
        const timeText = event.target.getAttribute("data-time");
        const parts = timeText.split(/[：:]/).map(Number);
        let timeInSeconds = 0;
        let isTimeValid = false;

        // mm:ss 格式：分、秒 0~59
        if (parts.length === 2) {
          const [min, sec] = parts;
          if (min >= 0 && min < 60 && sec >= 0 && sec < 60) {
            timeInSeconds = min * 60 + sec;
            isTimeValid = true;
          }
        } 
        // hh:mm:ss 格式：分、秒 0~59，小时仅限制非负
        else if (parts.length === 3) {
          const [h, min, sec] = parts;
          if (h >= 0 && min >= 0 && min < 60 && sec >= 0 && sec < 60) {
            timeInSeconds = h * 3600 + min * 60 + sec;
            isTimeValid = true;
          }
        }

        if (isTimeValid) {
          seekUpVideo(timeInSeconds);
          window.scrollTo({ top: 0, behavior: "smooth" });
        }
      }
    };

    //监视关闭收藏对话框关闭收藏输入框
    watch(collectDialogVisible, (newValue) => {
      if (!newValue && collectInputButtonFlag.value) {
        collectInputButtonFlag.value = false; // 修改 flag 的值
      }
      if (newValue) {
        getAllCollectAxios();
      }
    });

    //发送查询所有收藏信息的请求
    async function getAllCollectAxios() {
      try {
        if (store.userId === null) return;
        const response = await apiClient.get("/video/getByIdCollectClassify", {
          params: {
            userId: store.userId,
            videoId: SelectVideoByIdVo.upVideo.id,
          },
          headers: {
            "Content-Type": "application/json",
            Authorization: store.token,
          },
        });
        if (response.data.code === 1) {
          Object.assign(collectInfoList, response.data.data);
          collectInfoList2 = JSON.parse(JSON.stringify(response.data.data)); // 要重新创一个数组地址
        }
      } catch (error) {
        ElMessage({
          message: "未知错误",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
    }

    //新建收藏
    async function addCollect() {
      if (collectName.value.length === 0) {
        newCreateCollectInfoContent.value = "请填写收藏夹名称";
        newCreateCollectInfoFlag.value = true;
        setTimeout(() => {
          newCreateCollectInfoFlag.value = false;
        }, 1000);
        return;
      } else {
        collectInfoList.forEach((item) => {
          if (item.collectName === collectName.value) {
            newCreateCollectInfoContent.value = "该收藏夹已存在";
            newCreateCollectInfoFlag.value = true;
            setTimeout(() => {
              newCreateCollectInfoFlag.value = false;
            }, 1000);
          }
        });
      }
      if (store.userId === null) {
        loginDialogVisibleFlag.value =
          loginDialogVisibleFlag.value === 0 ? 1 : 0;
        return;
      }
      if (newCreateCollectInfoContent.value !== "该收藏夹已存在")
        addCollectAxios();
    }

    //新建收藏夹请求
    async function addCollectAxios() {
      try {
        if (store.userId === null) return;
        let obj = {
          userId: store.userId,
          collectName: collectName.value,
          videoNumber: 0,
        };
        const res = await apiClient.post(
          "/collectClassify/addCollectClassify",
          obj,
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: store.token,
            },
          },
        );
        if (res.data.code === 1) {
          getAllCollectAxios();
          collectName.value = "";
        }
      } catch (error) {
        ElMessage({
          message: "未知错误",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
    }

    //添加或取消视频至收藏夹
    function addCollectVideo(collectInfo) {
      if (
        collectInfo.videoNumber !== 1000 &&
        collectInfo.collectName !== "默认收藏夹"
      ) {
        collectInfo.flag = !collectInfo.flag;
        collectInfo.videoNumber = collectInfo.flag
          ? collectInfo.videoNumber + 1
          : collectInfo.videoNumber - 1;
      } else {
        collectInfo.flag = !collectInfo.flag;
        collectInfo.videoNumber = collectInfo.flag
          ? collectInfo.videoNumber + 1
          : collectInfo.videoNumber - 1;
      }
    }

    //监视是否可以点击收藏按钮 即收藏夹是否更改
    watch(
      collectInfoList,
      () => {
        for (let i = 0; i < collectInfoList.length; i++) {
          if (collectInfoList[i].flag !== collectInfoList2[i].flag) {
            collectSubimtFlag.value = true;
            break;
          }
          if (
            collectInfoList[i].flag === collectInfoList2[i].flag &&
            i === collectInfoList.length - 1
          )
            collectSubimtFlag.value = false;
        }
      },
      { deep: true },
    );

    //添加视频至收藏夹请求
    async function addCollectVideoAxios() {
      try {
        if (store.userId === null) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          return;
        }

        let obj = {
          userId: store.userId,
          videoId: SelectVideoByIdVo.upVideo.id,
          allInFlags: [],
        };

        collectInfoList.forEach((item) => {
          let allInFlag = {
            name: item.collectName,
            flag: item.flag,
          };
          obj.allInFlags.push(allInFlag);
        });
        const res = await apiClient.post("/collect/addCollect", obj, {
          headers: {
            "Content-Type": "application/json",
            Authorization: store.token,
          },
        });
        if (res.data.code === 1) {
          getVideoAndUser();
          collectDialogVisible.value = false;
        }
      } catch (error) {
        ElMessage({
          message: "未知错误",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
    }

    //待看清单请求
    async function waitWatchAxios() {
      try {
        if (store.userId === null) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          return;
        }

        let dynamicDto = {
          videoId: SelectVideoByIdVo.upVideo.id,
          userId: store.userId,
        };
        const response = await apiClient.put(
          "/dynamic/updateWaitWatch",
          dynamicDto,
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: store.token,
            },
          },
        );
        if (response.data.code === 1) {
          if (response.data.data === "1")
            waitWatchMsg.value = "已添加至稍后观看";
          else if (response.data.data === "0")
            waitWatchMsg.value = "已从稍后观看中移除";
        }
        setTimeout(() => {
          waitWatchMsg.value = "";
        }, 2000);
      } catch (error) {
        ElMessage({
          message: "未知错误",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
    }

    //更新播放量
    let playNumberTime;
    async function updateVideoPlayNumberAxios() {
      try {
        if (!SelectVideoByIdVo) return;
        //时长未知时不再按0ms定时器疯狂触发播放量上报
        let videoTime = getVideoDurationSeconds();
        if (!videoTime) return;

        if (pausedOrPlayVideoFlag.value) {
          playNumberTime = setTimeout(async () => {
            await apiClient.put(
              `/video/updateVideoPlay?videoId=${SelectVideoByIdVo.upVideo.id}`,
            );
          }, videoTime * 1000);
        }
      } catch (error) {}
    }

    //监视是否处于全屏状态
    watch(intoVideoAllDisplayIngFlag, (val) => {
      if (val) window.addEventListener("wheel", wheelControlAudio);
      else window.removeEventListener("wheel", wheelControlAudio);
    });

    //方法后鼠标滚轮控制音量
    function wheelControlAudio(e) {
      if (e.deltaY > 0 && upVideoPlayer.value.volume > 0) {
        videoAudio.value = videoAudio.value - 1;
        upVideoPlayer.value.volume = videoAudio.value / 100;
        audioInfoFlag.value = true;
      } else if (e.deltaY < 0 && upVideoPlayer.value.volume < 1) {
        videoAudio.value = videoAudio.value + 1;
        upVideoPlayer.value.volume = videoAudio.value / 100;
        audioInfoFlag.value = true;
      }
      clearTimeout(audioInfoTime);
      audioInfoTime = setTimeout(() => {
        audioInfoFlag.value = false;
      }, 2000);
    }

    let AutoPlayVideoListTime = null;
    let loginLoadFlag = true;
    //监视自动播放列表
    watch(
      () => store.AutoPlayVideoList,
      (value) => {
        if (!value) clearTimeout(AutoPlayVideoListTime);
      },
    );
    //重播按钮
    watch(
      () => store.reVideoPlayerFlag,
      (value) => {
        if (value) playUpVideo();
      },
    );
    //关注按钮
    watch(
      () => store.addFollowFlag,
      (value) => {
        if (value === 1) addFollowAxios(SelectVideoByIdVo.upUser.id);
        else if (value === 2)
          deleteFollowAxios(SelectVideoByIdVo.upUser.id);
      },
    );
    //点赞 投币 收藏 按钮
    watch(
      () => store.LTCAFlag,
      async (value) => {
        if (value === 1) {
          await likeVideoAxios();
          if (likeVideoClickFlag.value) {
            likeVideoImgFlag.value = true;
            ThreImgDisplay();
          }
        } else if (value === 2) {
          if (store.userId === null) {
            loginDialogVisibleFlag.value =
              loginDialogVisibleFlag.value === 0 ? 1 : 0;
          } else {
            toggleFullscreen2();
            collectDialogVisible.value = false;
            throwCoinDialog.value = true;
          }
        } else if (value === 3) {
          if (store.userId === null) {
            loginDialogVisibleFlag.value =
              loginDialogVisibleFlag.value === 0 ? 1 : 0;
          } else {
            toggleFullscreen2();
            throwCoinDialog.value = false;
            collectDialogVisible.value = true;
          }
        }
        store.setLTCAFlag(0);
      },
    );
    //登录弹窗
    watch(
      () => store.loginDialogVisibleFlag,
      (value) => {
        if (value) {
          loginDialogVisibleFlag.value =
            loginDialogVisibleFlag.value === 0 ? 1 : 0;
          store.setLoginDialogVisibleFlag(false);
        }
      },
    );
    //登录后刷新视频与eit列表
    watch(
      [() => store.loginLoadFlag, () => store.userId],
      async ([loadFlag, userId]) => {
        if (loadFlag && loginLoadFlag && userId !== null) {
          await getVideoAndUser();
          getEitList();
          loginLoadFlag = false;
        }
      },
    );
    //视频信息更新
    watch(
      () => store.updateVideoInfoFlag,
      (value) => {
        if (value) {
          getVideoAndUser();
          store.setUpdateVideoInfoFlag(false);
        }
      },
    );
    //监视进度条 播放完毕后自动播放下一个
    watch(
      [upVideoProgress, () => store.AutoPlayVideoList],
      ([progress, autoPlayVideoList]) => {
        clearTimeout(AutoPlayVideoListTime);
        if (parseInt(progress) === 100 && autoPlayVideoList)
          AutoPlayVideoListTime = setTimeout(() => {
            if (store.AutoPlayVideoList)
              window.location.href = `./video?videoId=BV${store.autoVideoList[0].videoId}&autoFlag=1`;
          }, 4500);
      },
    );

    //点击标签跳转搜索页面
    function tagClick(tag) {
      window.open(
        `./search?keyword=${tag}&classifyIndex=`,
        "_blank",
      );
    }

    watch(loginDialogVisibleFlag, () => {
      toggleFullscreen2();
    });

    //添加历史记录
    function addHistoryAxios() {
      if (store.userIp === null || store.token === null || store.userId === null || !store.userInformation.historyFlag) return;

      let history = {
        userId: store.userId,
        videoId: SelectVideoByIdVo.upVideo.id,
        watchCurrentTime:
          upVideoPlayer.value.currentTime !== upVideoPlayer.value.duration
            ? upVideoPlayer.value.currentTime
            : 0,
        watchVideoFlag:
          upVideoPlayer.value.currentTime === upVideoPlayer.value.duration
            ? 1
            : 0,
      };

      let acceptHistory = {
        history: history,
        userIp: store.userIp,
        token: store.token,
      };

      const url = "/api/history/addHistory";

      // 将数据转换为 JSON 字符串
      const payload = JSON.stringify(acceptHistory);

      // 将 headers 追加到 Beacon 的请求体中
      const blob = new Blob([payload], { type: "text/plain" });

      // 使用 sendBeacon 发送数据
      navigator.sendBeacon(url, blob);
    }

    //查询历史记录
    async function getHistory() {
      if(!store.userId)
      return;
      const urlParams = new URLSearchParams(window.location.search);
      let videoId = urlParams.get("videoId");
      videoId = videoId.replace("BV", "");
      let videos = {
        userId: store.userId,
        id: videoId,
      };
      const response = await apiClient.post("/history/getHistoryById", videos, {
        headers: {
          "Content-Type": "application/json",
          Authorization: store.token,
        },
      });

      if (response.data.code === 1) {
        Object.assign(history, response.data.data);
        seekUpVideo(
          history.watchCurrentTime !== undefined &&
          history.watchCurrentTime !== null
            ? history.watchCurrentTime
            : 0,
        );
      }
    }

    let delayUserInfoTime = null;
    let delayUserInfoTime2 = null;
    const nameOrAvatarHoverFlag2=ref(false);
    function delayUserInfo(value){
          
      if(value===1||value===2)
      {
        clearTimeout(delayUserInfoTime);
        delayUserInfoTime2=setTimeout(() => {
          nameOrAvatarHoverFlag.value=value;
        }, 300);
      }
      else{
        clearTimeout(delayUserInfoTime2);
        delayUserInfoTime=setTimeout(()=>{
          if(!nameOrAvatarHoverFlag2.value)
          nameOrAvatarHoverFlag.value=0;
        },400)
      }
    }

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

    //延时分享窗
    let delayShareTime = null;
    function shareHoverF(flag){
      if(flag)
      {
        clearTimeout(delayShareTime);
        delayShareTime=setTimeout(()=>{
          shareHover.value=true;
          store.setShareHover(true); 
        },300)     
      }
        else{
          clearTimeout(delayShareTime);
          if(store.shareVideoFocusFlag)
            return;
          delayShareTime=setTimeout(() => {
            shareHover.value=false;
            store.setShareHover(false);      
          },300);
        }
    
    }

    function shareHoverF2(){
      if(store.shareHover)
          {
              store.setShareHover(false);
              shareHover.value=false;
          }
      else
          {
              store.setShareHover(true);
              shareHover.value=true;
          }
    }

    //打开我的主页
    function openHome(menu,id){
          window.open(
          `./home?homeMenu=${menu}&userId=${id}`,
          "_blank",
        );
    }

    //视频加载
    let videoLoadFlag=false;
    function videoWaitingF(){
      const player=upVideoPlayer.value;
      //readyState小于4说明还没缓冲完，允许显示加载中；
      //这里原先写成 !readyState===4 恒为false，导致videoLoadFlag永远解不掉
      if(player&&player.readyState<4){
        videoLoadFlag=false;
      }
      if(videoLoadFlag)
      return;
      videoWaitingFlag.value=true;
    }

//可以播放但没有加载完毕
    function onVideoCanPlay(){
       //metadata已就绪，记录真实时长供进度条与时间显示使用
       if(upVideoPlayer.value&&Number.isFinite(upVideoPlayer.value.duration)&&upVideoPlayer.value.duration>0)
         videoDurationSeconds.value=upVideoPlayer.value.duration;
       videoWaitingFlag.value=false;
    }

    //视频加载完毕
    function onCanPlayThrough(){
      if(upVideoPlayer.value&&upVideoPlayer.value.readyState===4)
      {
        videoLoadFlag=true;
      }
      videoWaitingFlag.value=false;
    }

    
    return {
      isRemoteVideo,
      remoteVideoSrc,
      playVideoSrc,
      videoTotalTime,
      onloadPage,
      titleShowFlag,
      sendMessageGray,
      sendMessageBlue,
      showSendMessageFlag,
      SelectVideoByIdVo,
      addFollowAxios,
      deleteFollowAxios,
      isDeleteFollowFlag,
      videoFeatureShowFlag,
      upVideoPlayer,
      upVideoProgress,
      updateProgress,
      upVideoProgressImg,
      upVideoProgressRight,
      updateupVideoProgressImgPosition,
      upVideoProgressImgPosition,
      upVideoPlayGray,
      upVideoPlayWhite,
      intoAllDisplayGray,
      intoAllDisplayWhite,
      audioGray,
      audioWhite,
      videoSettingGray,
      videoSettingWhite,
      fullScreenGray,
      fullScreenWhite,
      fullScreenExitGray,
      fullScreenExitWhite,
      webFullScreenGray,
      webFullScreenWhite,
      upVideoPlayFlag,
      intoAllDisplayFlag,
      fontFlag,
      audioFlag,
      videoSettingFlag,
      fullScreenFlag,
      fullScreenExitFlag,
      webFullScreenFlag,
      coleseFontFlag,
      formattedTime,
      closeAudioFlag,
      closeAudioGray,
      closeAudioWhite,
      playUpVideo,
      videoMove,
      videoLeave,
      handlerCleanTime,
      pausedVideoGray,
      pausedVideoWhite,
      pausedOrPlayVideoFlag,
      videoPausedFlag,
      pausedUpVideo,
      upVideoStartPlayFlag,
      pausedOrPlayUpVideo,
      upVideoPlayerss,
      changeUpVideoTimeImg,
      startProgressDrag,
      stopMoving,
      isProgressImgMoving,
      upVideoInputTimeFlag,
      upVideoPointerMoveTime,
      upVideoTimeDuration,
      inputUpVideoTime,
      inputUpVideoTimeValue,
      enterUpVideoTime,
      reInputUpVideoTime,
      selectVideoSpped,
      videoCloseAudioOrOpenAudioFlag,
      videoAudio,
      setVideoMirrorFlag,
      setVideoAutoRePlayFlag,
      setVideoOpenAutoPlayFlag,
      showFullScreenExitFlag,
      openOrCloseFullScreenExitFlag,
      togglePictureInPicture,
      intoVideoAllDisplayFlag,
      toggleFullscreen,
      intoVideoAllDisplayIngFlag,
      watchTogetherVisible,
      upVideoContainer,
      watchTogetherHost,
      closeScrollingBlue,
      closeScrollingGray,
      closeScrollingWhite,
      openScrollingBlue,
      openScrollingGray,
      openScrollingWhite,
      scrollingSettingGray,
      scrollingSettingWhite,
      scrollingSettingBlue,
      scrollingColorGray,
      scrollingColorWhite,
      scrollingColorBlue,
      showCloseScrollingFlag,
      showScrollingSettingFlag,
      showScrollingColorFlag,
      showOpenScrollingFlag,
      openOrCloseScrollingFlag,
      openOrCloseScrollingInfoFlag,
      scrollingRollGray,
      scrollingRollWhite,
      scrollingRollBlue,
      hiddenRollScrollingBlue,
      hiddenScrollingFiexdGray,
      hiddenScrollingFiexdWhite,
      hiddenScrollingFiexdBlue,
      topScrollingBlue,
      bottomScrollingBlue,
      bottomScrollingWhite,
      bottomScrollingGray,
      checkBoxWhite,
      checkBoxs,
      checkBoxBlue,
      scrollingRollHoverFlag,
      scrollingRollOpenFlag,
      scrollingFilexdHoverFlag,
      scrollingFilexdOpenFlag,
      checkBoxHoverFlag,
      checkBoxOpenFlag,
      scrollingTopHoverFlag,
      scrollingBottomHoverFlag,
      scrollingPattern,
      scrollingDisplayAreaValue,
      scrollingDisplayOpacityValue,
      scrollingDisplayFontSizeValue,
      scrollingDisplaySpeedValue,
      sendScrollingFontSize,
      sendScrollingColor,
      sendScrollingColorful,
      predefineColors,
      predefineColorsHoverFlag,
      sendScrollingText,
      sendScrollingAxios,
      ScrollingDataList,
      getScrollingCurrentTime,
      sppedList,
      videoTimeFormat,
      ScrollingReocationHoverFlag,
      ScrollingReocationFlag,
      revocationScrollingAxios,
      likeVideo,
      likeVideoBlue,
      videoThrowCoin,
      videoThrowCoinBlue,
      videoShare,
      videoShareBlue,
      videoCollect,
      videoCollectBlue,
      videoLikeHoverFlag,
      videoThrowCoinHoverFlag,
      videoShareHoverFlag,
      videoCollectHoverFlag,
      startThree,
      endThree,
      threeAnmationBeforeFlag,
      threeAnmationAfterFlag,
      likeVideoClickFlag,
      videoThrowCoinClickFlag,
      videoShareClickFlag,
      videoCollectClickFlag,
      likeVideoImgFlag,
      intervalId,
      videoThrowCoinAxios,
      collectDialogVisible,
      collectInputButtonFlag,
      collectInputRef,
      collectCheckBox,
      collectCheckBoxBlue,
      collectCheckBoxHoverFlag,
      collectInfoList,
      addCollectVideo,
      collectName,
      newCreateCollectInfoFlag,
      newCreateCollectInfoContent,
      addCollect,
      collectSubimtFlag,
      addCollectVideoAxios,
      addWaitWatch,
      addWaitWatchBlue,
      addWaitWatchHoverFlag,
      waitWatchMsg,
      waitWatchAxios,
      keyInfo,
      audioInfoFlag,
      audioBlack,
      closeAudioBlack,
      watchingNumber,
      throwCoinDialog,
      likeVideoFlag,
      throwCoinNumber,
      sendScrollingInputStatus,
      copyScrolling,
      store,
      loginDialogVisibleFlag,
      videoContentFlag,
      videoContentFlag1,
      tagClick,
      closePostionHoverFlag,
      closePostionFlag,
      history,
      nameOrAvatarHoverFlag,
      nameOrAvatarHoverFlag2,
      delayUserInfo,
      addDialogueF,
      shareHover,
      shareHoverF,
      shareHoverF2,
      openHome,
      videoWaitingFlag,
      onVideoCanPlay,
      videoWaitingF,
      onCanPlayThrough,
      innerWidth,
    };
  },
};
</script>

<style lang="scss">
* {
  transition: background-color 0.3s ease, color 0.3s ease;
}

.video-head {
  top: 0px;
  width: 101%;
  height: 64px;
  left: -13px;
  position: fixed;
  background-color: white;
  box-shadow: 2px 0px 4px #d3d3d3;
}

.video-body-container{
  position: relative;
  display: flex;
  justify-content: center;
  left: -206px;
  top: 63.5px;
}

.video-body {
  position: absolute;
  height: auto;
  padding-bottom: 200px;
  z-index: -1000;
}

.up-videoTitle {
  max-width: 672px;
  display: -webkit-box;
  transform: translate(8px, 23px);
  font-size: 19px;
  color: #1C2321;
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  -webkit-line-clamp: 1; /* 限制为 1 行 */
  line-clamp: 1;
  overflow: hidden; /* 隐藏超出部分 */
  word-wrap: break-word; /* 超出部分换行 */
  text-overflow: ellipsis; /* 显示省略号 */
}

.expand-icon {
  width: 12.5px;
  transition: transform 0.3s ease;
}

.custom-tooltip {
  border: 1px solid black !important; /* 修改边框颜色和宽度 */
  height: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 5px;
  border-radius: 0px !important;
}

.expand-icon-container {
  position: absolute;
  display: flex;
  justify-content: center;
  align-content: center;
  padding: 7px 5px 7px 5px;
  border-radius: 5px;
  transform: translate(688px, -0.5px);
  width: 12.5px;
  cursor: pointer;
}

.expand-icon-container:hover {
  background-color: #E0E5E3;
}

.expand-icon-container:hover .expand-icon {
  transform: rotate(180deg);
}

.titleShowFlagCss {
  position: absolute;
  margin-top: 10px;
}

.show-up-videoTitle-container {
  width: 660px;
  padding: 15px;
  position: absolute;
  visibility: hidden;
  transform: translateY(32px);
  font-size: 19px;
  border-radius: 5px;
  border: 1px solid #d3dad3;
  right: 26.5px;
  bottom: -68px;
  background-color: white;
  cursor: pointer;
  transition: visibility 0.2s ease;
  box-shadow: 2px 0px 5px #d3d3d3, -2px 0px 5px #ebe9e9;
}

.show-up-videoTitle {
  word-wrap: break-word;
}

.expand-icon-container:hover .show-up-videoTitle-container {
  visibility: visible;
}

.up-user-avatar {
  width: 47px;
  border-radius: 50%;
  display: flex;
  transform: translate(740px, -20px);
  cursor: pointer;
}

.up-user-header {
  display: flex;
  transform: translate(800px, -79px);
}

.up-user-name {
  font-size: 14.5px;
  cursor: pointer;
}

.up-user-name:hover {
  color: #0FA68E;
}

.up-user-info {
  position: absolute;
  top: 100%;
  left: 210%;
  transform: translate(0, 0);
  background-color: white;
  color: #a6acaf;
  border-radius: 10px;
  z-index: 1000;
  width: 350px;
  height: 220px;
  border: 1px solid #d3d3d3; /* 浅灰色边框 */
  box-shadow: 1px 0px 5px #e5e5e5;
  overflow: hidden;
  padding-bottom: 15px;
}

.up-user-avatar-container {
  display: flex;
  background-color: yellow;
  width: 0px;
}

.up-user-info-bg {
  width: 100%;
  height: 40%;
  background-size: cover;
  background-position: 50% 50%;
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
  line-clamp: 2;
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
  height: 32px;
  background-color: #0FA68E;
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
  height: 32px;
  background-color: #E0E5E3;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 5px;
  margin-right: 5px;
  color: #8F9794;
  cursor: pointer;
}

.up-user-info-container-footer-delete-follow:hover {
  background-color: #EFF3F2;
}

.up-user-info-container-footer-message {
  width: 100px;
  font-size: 14px;
  height: 30px;
  border: 1px solid #5D6764;
  border-radius: 5px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #5D6764;
  cursor: pointer;
}

.up-user-info-container-footer-add-follow:hover {
  opacity: 0.9;
}

.up-user-info-container-footer-message:hover {
  color: #0FA68E;
  border: 1px solid #0FA68E;
}

.sendUpMessageImg {
  width: 13px;
  height: 12px;
  margin-left: 15px;
  margin-top: 4px;
  cursor: pointer;
}

.sendUpMessage {
  font-size: 13px;
  color: #64686e;
  cursor: pointer;
  margin-left: 5px;
  margin-top: 1px;
  transition: none;
}

.sendMessage-container:hover .sendUpMessage {
  color: #0FA68E;
}

.up-user-content {
  transform: translate(800px, -77px);
  color: #8F9794;
  width: 280px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12.5px;
}

.up-power-container {
  width: 106px;
  height: 27.5px;
  transform: translate(800px, -72px);
  font-size: 13px;
  color: #EE7A31;
  border-radius: 5px;
  border: 1px solid #EE7A31;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  user-select: none;
  text-shadow: 0 0 1px rgba(238,122,49, 0.6);
  z-index: -500000;
  position: relative;
}

.up-power-container img {
  width: 20px;
  margin-right: 4.5px;
  margin-left: -2px;
}

.up-power-container:hover {
  background-color: #FDEEE4;
}

.up-add-follow-container {
  width: 169px;
  height: 30px;
  transform: translate(920px, -102px);
  color: white;
  font-size: 13.5px;
  background-color: #0FA68E;
  border-radius: 5px;
  display: flex;
  justify-content: center;
  align-items: center;
  user-select: none;
  cursor: pointer;
  transition: opacity 0.3s ease;
  position: relative;
  z-index: -500000;
}

.up-delete-follow-container {
  width: 169px;
  height: 30px;
  transform: translate(920px, -102px);
  color: #8F9794;
  font-size: 13.5px;
  background-color: #E0E5E3;
  border-radius: 5px;
  display: flex;
  justify-content: center;
  align-items: center;
  user-select: none;
  cursor: pointer;
  position: relative;
  z-index: -500000;
}

.up-delete-follow-container:hover {
  background-color: #EFF3F2;
}

.up-add-follow-container:hover {
  opacity: 0.9;
}

.prohibition-sign {
  width: 9px; /* 标识的宽度 */
  height: 9px; /* 标识的高度 */
  border: 2.5px solid #E2603C; /* 圆圈的边框颜色 */
  border-radius: 50%; /* 使其成为圆形 */
  position: relative; /* 相对定位，便于内部斜杠定位 */
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: white; /* 背景颜色 */
  transform: translate(-20px, 15px);
}

.prohibition-sign::after {
  content: "";
  position: absolute;
  width: 2.5px; /* 斜杠的宽度 */
  height: 100%; /* 斜杠的高度 */
  background-color: #E2603C; /* 斜杠的颜色 */
  transform: rotate(-50deg); /* 旋转斜杠 */
}

.up-video-infos-container {
  width: 500px;
  position: relative;
  transform: translate(11px, 20px);
  color: #8F9794;
}

.up-video-container {
  user-select: none;
  display: flex;
  width: 700px;
  height: 419px;
  transform: translate(9.5px, 77.5px);
  position: relative;
  z-index: -5;
  box-shadow: 2px 0 3px rgba(0, 0, 0, 0.03),
    /* 右侧阴影 */ -2px 0 3px rgba(0, 0, 0, 0.03); /* 左侧阴影 */
  overflow: hidden;
}

.up-user-video-player {
  width: 100%;
  height: 94%;
  z-index: -3;
  background-color: #000;
  object-fit: contain;
}

.up-video-avatar-and-addFollow-container {
  display: flex;
  position: absolute;
  width: 89px;
  height: 32px;
  background-color: rgba(0, 0, 0, 0.25);
  border-radius: 17px;
  opacity: 0;
  transition: opacity 0.3s ease;
  transform: translate(11.5px, 20px);
  cursor: pointer;
}

.up-video-container:hover .up-video-avatar-and-addFollow-container,
.up-video-container:hover .up-VideoProgress,
.up-video-container:hover .up-video-play-bottom-play,
.up-video-container:hover .up-video-play-bottom-video-time-container,
.up-video-container:hover .up-video-play-bottom-video-image-quality,
.up-video-container:hover .up-video-play-bottom-video-spped,
.up-video-container:hover .up-video-play-bottom-video-audio-container,
.up-video-container:hover .up-video-play-bottom-video-setting-container,
.up-video-container:hover .up-video-play-bottom-video-fullScreenExit,
.up-video-container:hover .up-video-play-bottom-video-fullScreen,
.up-video-container:hover .up-video-play-bottom-video-webFullScreen,
.up-video-container:hover .up-video-play-bottom-video-intoAllDisplay,
.up-video-container:hover .up-video-play-bottom-video-time-input-container,
.up-video-container:hover .openOrCloseScrollingintovideoAllDisplayIngFlag {
  opacity: 1;
}

.up-video-avatar-and-addFollow-container:hover {
  background-color: rgba(0, 0, 0, 0.5);
}

.up-video-avatar-and-addFollow-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  transform: translate(5px, 4px);
}

.up-VideoProgress {
  appearance: none;
  width: 676px;
  height: 3px;
  position: absolute;
  transform: translate(11px, 344px);
  opacity: 0;
  transition: opacity 0.3s ease;
  cursor: pointer;
}

.up-VideoProgress::-webkit-progress-bar {
  background-color: #a6acaf; /* 未加载的颜色 */
}

.up-VideoProgress::-webkit-progress-value {
  background-color: #0FA68E; /* 加载的颜色 */
  border-radius: 1px 1px 1px 1px !important;
}

.up-video-progress-img {
  position: absolute;
  width: 14px;
  height: 14px;
  transform: translate(11px, 337.5px);
  z-index: 2000;
  cursor: pointer;
}

.pointer-top {
  position: absolute;
  width: 7px;
  height: 4px;
  transform: translate(10px, 338px);
  z-index: 100;
  cursor: pointer;
}

.pointer-bottom {
  position: absolute;
  width: 7px;
  height: 4px;
  transform: translate(10px, 349px);
  z-index: 100;
  cursor: pointer;
}

.pointer-top-container {
  position: absolute;
  width: 676px;
  height: 5.5px;
  background-color: aqua;
  transform: translate(10px, 339px);
  opacity: 0;
  z-index: 1000;
  cursor: pointer;
}

.pointer-bottom-container {
  position: absolute;
  width: 676px;
  height: 5.5px;
  background-color: aqua;
  transform: translate(10px, 347px);
  opacity: 0;
  z-index: 1000;
  cursor: pointer;
}

.up-video-play-bottom-play {
  position: absolute;
  width: 14.93px;
  height: 17.4px;
  transform: translate(22px, 362.5px);
  opacity: 0;
  transition: opacity 0.3s ease;
  cursor: pointer;
}

.up-video-play-bottom-video-time-container {
  position: absolute;
  transform: translate(54px, 363px);
  color: rgba(187, 190, 191, 0.9);
  font-size: 12px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.up-video-play-bottom-video-time-container:hover {
  color: white;
}

.up-video-play-bottom-video-image-quality {
  position: absolute;
  transform: translate(340px, 362.5px);
  color: rgba(187, 190, 191, 0.9);
  font-size: 14px;
  opacity: 0;
  cursor: pointer;
  transition: opacity 0.3s ease;
}

.up-video-play-bottom-video-image-quality:hover {
  color: white;
}

.up-video-play-bottom-video-spped {
  position: absolute;
  transform: translate(434.5px, 362.5px);
  color: rgba(187, 190, 191, 0.9);
  font-size: 14px;
  opacity: 0;
  cursor: pointer;
  transition: opacity 0.3s ease;
}

.up-video-play-bottom-video-spped:hover {
  color: white;
}

.up-video-play-bottom-video-audio {
  width: 15px;
  height: 15px;
  position: absolute;
  cursor: pointer;
  transform: translate(0px, -18px);
}

.intoAllDisplayFlagUpVideoPlayBottomVideoAudio{
  transform: translate(0px, 3.5px);
}

.intoAllDisplayFlagUpVideoPlayBottomVideoAudio2{
  transform: translate(-2px, 2.5px) !important;
}

.up-video-play-bottom-video-colse-audio {
  width: 17px;
  height: 18px;
  cursor: pointer;
  position: absolute;
  transform: translate(-2px, -19px);
}

.up-video-play-bottom-video-fullScreenExit {
  position: absolute;
  width: 16px;
  height: 15px;
  cursor: pointer;
  transform: translate(555px, 365px);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.up-video-play-bottom-video-fullScreen {
  position: absolute;
  width: 16px;
  height: 15px;
  cursor: pointer;
  transform: translate(590px, 364.5px);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.up-video-play-bottom-video-webFullScreen {
  position: absolute;
  width: 16px;
  height: 15px;
  cursor: pointer;
  transform: translate(625px, 364.5px);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.up-video-play-bottom-video-intoAllDisplay {
  position: absolute;
  width: 16px;
  height: 15px;
  cursor: pointer;
  transform: translate(661px, 364.5px);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.up-video-start-play-white {
  position: absolute;
  cursor: pointer;
  transform: translate(606.5px, 276px);
  width: 54px;
  height: 50px;
}

.up-video-play-bottom-video-pointer-time {
  position: absolute;
  transform: translate(-6.5px, 315px);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 40px;
  height: 18px;
  font-size: 12px;
  z-index: 100;
  background-color: rgba(0, 0, 0, 0.8);
}

.up-video-play-bottom-video-time-input {
  width: 34px;
  height: 22px;
  border: none;
  border-radius: 1px;
  color: white;
  background-color: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  padding-left: 20px;
  padding-right: 20px;
}

.up-video-play-bottom-video-time-input-container {
  position: absolute;
  transform: translate(54px, 359px);
  color: rgba(255, 255, 255, 1);
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.up-video-play-bottom-video-speed-container {
  display: flex;
  flex-direction: column;
  left: 0%;
  position: absolute;
  transform: translate(-27%, -115%);
  color: white;
  background-color: rgba(0, 0, 0, 0.8);
  transition: visibility 0.3s ease;
  visibility: hidden;
}

.up-video-play-bottom-video-speed-container span {
  font-size: 14px;
  width: 50px;
  height: 16px;
  padding: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.up-video-play-bottom-video-spped:hover
  .up-video-play-bottom-video-speed-container {
  transition-delay: 0.3s;
  visibility: visible;
}

.up-video-play-bottom-video-speed-container span:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.selectVideoSpped {
  color: #0FA68E;
}

.up-video-play-bottom-video-set-audio {
  position: absolute;
  cursor: pointer;
  background-color: rgba(0, 0, 0, 0.8);
  width: 100px;
  height: 32.5px;
  border-radius: 2px;
  transform: translate(-42.5px, -100.5px) rotate(-90deg);
  display: flex;
  visibility: hidden;
  transition: visibility 0.3s ease;
  align-items: center;
}

.intoUpVideoPlayBottomVideoSetAudio{
  transform: translate(-42.5px, -86.5px) rotate(-90deg) !important;
}

.volume-slider {
  margin-bottom: 15px;
  margin-left: 11px;
  appearance: none;
  -webkit-appearance: none; /* Safari */
  width: 62px; /* 设置宽度 */
  height: 2px; /* 滑块高度 */
  background: #ddd; /* 背景色 */
  border-radius: 3px; /* 圆角 */
}

.volume-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 12px; /* 滑块圆点宽度 */
  height: 12px; /* 滑块圆点高度 */
  background: #0FA68E; /* 滑块圆点颜色 */
  border-radius: 50%; /* 圆形 */
  cursor: pointer;
}

.up-video-play-bottom-video-audio-container {
  position: absolute;
  width: 15px;
  height: 15px;
  cursor: pointer;
  transform: translate(482px, 382.5px);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.up-video-play-bottom-video-audio-value {
  width: 32.5px;
  text-align: center;
  color: white;
  font-size: 12px;
  position: absolute;
  transform: translate(-9px, -127px);
  visibility: hidden;
  transition: visibility 0.3s ease;
  z-index: 10;
}

.up-video-play-bottom-video-audio-container:hover
  .up-video-play-bottom-video-set-audio {
  visibility: visible;
  transition-delay: 0.3s;
}

.up-video-play-bottom-video-audio-container:hover
  .up-video-play-bottom-video-audio-value {
  visibility: visible;
  transition-delay: 0.3s;
}

.up-video-play-bottom-video-setting {
  width: 16px;
  height: 16px;
}

.up-video-play-bottom-video-setting-container {
  position: absolute;
  cursor: pointer;
  transform: translate(518.5px, 362px);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.up-video-play-bottom-video-set-container {
  position: absolute;
  width: 122px;
  height: 100px;
  padding: 5px;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  font-size: 11px;
  justify-content: center;
  align-items: center;
  color: white;
  border-radius: 2px;
  flex-direction: column;
  visibility: hidden; /* 初始不可见 */
  transform: translate(-60px, -145px);
  transition: visibility 0.3s; /* 延迟visibility的变化 */
}

.up-video-play-bottom-video-set-container span:hover {
  color: #0FA68E;
}

.video-false-container {
  position: absolute;
  width: 100%;
  height: 47px;
  transform: translate(0px, 347px);
  background-color: #000000;
  transition: opacity 0.3s ease;
  z-index: -2;
  opacity: 0;
}

.up-video-play-bottom-video-setting-container:hover
  .up-video-play-bottom-video-set-container,
.up-video-play-bottom-video-setting-container:hover
  .custom-switch
  .el-switch__action {
  opacity: 1; /* 鼠标悬停时完全可见 */
  visibility: visible; /* 设置为可见 */
}

.custom-switch {
  width: 20px;
  --el-switch-height: 20px; /* 修改高度 */
  --el-switch-width: 30px; /* 修改宽度 */
  --el-switch-on-color: #0FA68E !important;
  margin-left: 10px;
  margin-right: 10px;
}

.custom-switch .el-switch__core {
  height: 18px;
  min-width: 30px;
  border-radius: 15px; /* 修改圆角 */
  transition: left 0.3s ease !important; /* 切换时平滑过渡 */
}

.custom-switch .el-switch__action {
  width: 20px; /* 修改滑块的宽度 */
  height: 20px; /* 修改滑块的高度 */
  background-color: white; /* 滑块的颜色 */
  border-radius: 50%; /* 滑块的圆角 */
  transition: left 0.3s ease !important;
}

.setVideoMirrorFlag {
  transform: scaleX(-1);
}

.up-video-play-bottom-video-fullScreenExit-text {
  position: absolute;
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  font-size: 11px;
  border-radius: 2px;
  width: 68px;
  height: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  transform: translate(529px, 323px);
}

.up-video-play-bottom-video-intoAllDisplay-text {
  position: absolute;
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  font-size: 11px;
  border-radius: 2px;
  width: 68px;
  height: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  transform: translate(630px, 323px);
}

.intoVideoAllDisplayIngFlagavatarandaddFollow {
  position: absolute;
  transform: translate(31px, 61px) scale(1.2);
}

.intoVideoAllDisplayIngFlagVideoTitle {
  position: absolute;
  width: 96.5%;
  transform: translate(30px, 25px);
  color: white;
  font-size: 19px;
}

.startplaywhiteintoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1);
  left: 93.7%;
  top: 80%;
}

.VideoAllDisplayIngFlagVideo {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.playwhiteintoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1.2);
  left: 2.1%;
  top: 94.23%;
}

.timeintoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1.2);
  left: 7%;
  top: 94.2%;
}

.inputTimeintoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1.2);
  left: 7%;
  top: 94%;
}

.imageQualityintoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1.08);
  left: 73%;
  top: 93.9%;
}

.sppedVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1.08);
  left: 81%;
  top: 93.9%;
}

.audioVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1.2);
  left: 85.6%;
  top: 93.9%;
}

.setVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1.2);
  left: 89.2%;
  top: 94.1%;
}

.fullScreenExitintoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1.2);
  left: 92.8%;
  top: 94.3%;
}

.fullScreenExitInfointoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1);
  left: 91%;
  top: 88%;
}

.IntoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1.2);
  left: 96.3%;
  top: 94.3%;
}

.InfoIntoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1);
  left: 94.5%;
  top: 88%;
}

.progressintoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1);
  width: 97%;
  height: 3px;
  left: 1.5%;
  top: 90%;
}

.progressImgintoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1);
  top: 89.25%;
}

.progressPointerTopintoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1);
  top: 89%;
}

.progressPointerBottomintoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1);
  top: 91%;
}

.topContainerintoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(3);
  width: 97%;
  top: 89.5%;
  left: 1.5%;
}

.bottomContainerintoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(3);
  width: 97%;
  top: 90.3%;
  left: 1.5%;
}

.pointerTImeintoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1);
  top: 85%;
}

.falseContainerIntoVideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1);
  height: 80px;
  left: 0;
  bottom: 0px;
}

.up-video-play-bottom-video-scrolling-container {
  position: absolute;
  width: 700px;
  height: 50px;
  box-shadow: 2px 1px 3px rgba(0, 0, 0, 0.06),
    /* 右下阴影 */ -2px -1px 3px rgba(0, 0, 0, 0.06),
    /* 左上阴影 */ 0 1px 2px rgba(0, 0, 0, 0.03); /* 下方阴影 */
  transform: translate(9.5px, 50px);
  display: flex;
  z-index: 20;
  align-items: center;
}

.watch-num {
  position: absolute;
  transform: translate(13px);
  color: #676b71;
  font-size: 13px;
}

.allowFlagcss1 {
  position: absolute;
  margin-top: 0px;
}

.allowFlagcss2 {
  position: absolute;
  margin-top: 10px;
}

.up-video-play-bottom-video-close-open-scrolling {
  position: absolute;
  width: 20px;
  cursor: pointer;
  transform: translate(229.5px, 1px);
}

.up-video-play-bottom-video-open-close-scrolling {
  position: absolute;
  width: 21px;
  cursor: pointer;
  transform: translate(229.5px, 1.5px);
}

.up-video-play-bottom-video-close-setting-scrolling {
  position: absolute;
  width: 21.5px;
  cursor: pointer;
  transform: translate(265px, 1px);
  opacity: 0.4;
}

.up-video-play-bottom-video-setting-scrolling {
  position: absolute;
  width: 21.5px;
  cursor: pointer;
  transform: translate(265px, 1px);
}

.up-video-play-bottom-video-close-scrolling-container {
  position: absolute;
  transform: translate(299px, 1px);
  width: 321px;
  height: 32px;
  font-size: 12.5px;
  background-color: #EFF3F2;
  display: flex;
  align-items: center;
  padding-left: 6.5px;
  border-radius: 8px 0px 0px 8px;
}

.up-video-play-bottom-video-open-scrolling-container {
  position: absolute;
  transform: translate(299px, 1px);
  width: 325px;
  height: 32px;
  font-size: 12.5px;
  background-color: #EFF3F2;
  display: flex;
  align-items: center;
  padding-left: 6.5px;
  border-radius: 8px 0px 0px 8px;
}

.up-video-play-bottom-video-open-scrolling-container2 {
  position: absolute;
  transform: translate(299px, 1px);
  width: 325px;
  height: 32px;
  font-size: 12.5px;
  background-color: rgba(77, 76, 76, 0.7);
  display: flex;
  align-items: center;
  padding-left: 6.5px;
  border-radius: 8px 0px 0px 8px;
}

.up-video-play-bottom-video-open-scrolling-container1 {
  position: absolute;
  transform: translate(299px, 1px);
  width: 325px;
  height: 32px;
  font-size: 12.5px;
  background-color: rgba(77, 76, 76, 0.7);
  display: flex;
  align-items: center;
  padding-left: 6.5px;
  border-radius: 8px 0px 0px 8px;
}

.send-scrolling-btn-close {
  transform: translate(195px);
  color: #999999;
  background-color: #E0E5E3;
  width: 62px;
  height: 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 0px 8px 8px 0px;
  cursor: not-allowed; /* 显示不可用的光标 */
}

.send-scrolling-btn-close1 {
  transform: translate(285px);
  color: #999999;
  background-color: #E0E5E3;
  width: 62px;
  height: 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 0px 8px 8px 0px;
  cursor: not-allowed; /* 显示不可用的光标 */
}

.send-scrolling-btn-open {
  transform: translate(242px);
  color: white;
  background-color: #0FA68E;
  width: 62px;
  height: 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 0px 8px 8px 0px;
  cursor: pointer;
}

.send-scrolling-btn-open1 {
  transform: translate(332px);
  color: white;
  background-color: #0FA68E;
  width: 62px;
  height: 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 0px 8px 8px 0px;
  cursor: pointer;
}

.up-video-play-bottom-video-close-open-scrolling-info {
  position: absolute;
  font-size: 11px;
  width: 70px;
  height: 30px;
  color: white;
  background-color: rgba(0, 0, 0);
  display: flex;
  justify-content: center;
  align-items: center;
  transform: translate(208px, -30px);
}

.up-video-play-bottom-video-close-scrolling-color {
  width: 15.5px;
  cursor: pointer;
  transform: translate(4px, 2px);
}

.up-video-play-bottom-video-close-scrolling-input {
  position: absolute;
  width: 211px;
  transform: translate(27px);
  font-size: 13px;
  border: none;
  background-color: #EFF3F2;
}

.up-video-play-bottom-video-close-scrolling-input:focus {
  outline: none;
}

.up-video-play-bottom-video-close-scrolling-input1 {
  position: absolute;
  width: 302px;
  transform: translate(27px);
  font-size: 13px;
  border: none;
  background-color: rgba(77, 76, 76, 0);
}

.up-video-play-bottom-video-close-scrolling-input1::placeholder {
  color: #a7a6a6;
}

.up-video-play-bottom-video-close-scrolling-input1:focus {
  color: white;
  outline: none;
}

.scrolling-regulation {
  color: #999999;
  text-decoration: none;
}

.scrolling-regulation1 {
  color: #a6a5a5;
  text-decoration: none;
}

.scrolling-regulation:hover {
  color: #0FA68E;
}

.scrolling-regulation1:hover {
  color: white;
}

.up-video-play-bottom-video-setting-scrolling-container {
  transform: translate(0px, -10.5px);
}

.up-video-play-bottom-video-setting-scrolling-info-container {
  width: 320px;
  height: 235px;
  position: absolute;
  transform: translate(117px, -257px);
  background-color: rgba(0, 0, 0, 0.8);
  user-select: none;
  transition: visibility 0.3s ease;
  font-size: 12px;
  visibility: hidden;
  color: #d0d0d0;
  z-index: 10;
}

.intoUpVideoPlayBottomVideoSettingScrollingInfoContainer{
  transform: translate(-50%, -257px) !important;
}

.up-video-play-bottom-video-setting-scrolling-container:hover
  .up-video-play-bottom-video-setting-scrolling-info-container {
  transition-delay: 0.3s;
  visibility: visible;
}

.roll-scrolling-img {
  position: absolute;
  width: 26.5px;
  height: 22.5px;
  transform: translate(20px, 34px);
  cursor: pointer;
}

.roll-scrolling-text {
  position: absolute;
  transform: translate(20px, 58px);
  cursor: pointer;
  font-size: 12.5px;
}

.scrollingRollHoverFlag {
  color: white;
}

.rollScrollingText {
  color: #0FA68E;
}

.fixed-scrolling-text {
  position: absolute;
  transform: translate(70px, 58px);
  cursor: pointer;
  font-size: 12.5px;
}

.fixed-scrolling-img {
  position: absolute;
  width: 26.5px;
  height: 22.5px;
  transform: translate(70px, 34px);
  cursor: pointer;
}

.scrollingFilexdHoverFlag {
  color: white;
}

.fixedScrollingText {
  color: #0FA68E;
}

.setting-scrolling-checkbox {
  position: absolute;
  transform: translate(20px, 88px);
  width: 13.5px;
  cursor: pointer;
}

.setting-scrolling-text {
  position: absolute;
  transform: translate(39px, 86px);
  font-size: 12px;
  color: white;
  cursor: pointer;
}

.checkBoxHoverFlag {
  color: #0FA68E;
}

.scrolling-slider-container {
  display: flex;
  position: absolute;
  flex-direction: column;
  color: white;
  transform: translate(20px, 100px);
}

.scrolling-slider-container div{
  margin-top: 12px;
}

.scrolling-display-slider {
  appearance: none;
  -webkit-appearance: none; /* Safari */
  position: absolute;
  transform: translate(10px, 3.5px);
  width: 170px;
  height: 6px;
  border-radius: 8px;
  background-color: #0FA68E;
}

.silder-pointer1 {
  position: absolute;
  transform: translate(15px, 6px);
  width: 5.5px;
  height: 5.5px;
  border-radius: 50%;
  background-color: white;
  cursor: pointer;
}

.silder-pointer2 {
  position: absolute;
  transform: translate(55px, 6px);
  width: 5.5px;
  height: 5.5px;
  border-radius: 50%;
  background-color: white;
  cursor: pointer;
}

.silder-pointer3 {
  position: absolute;
  transform: translate(95px, 6px);
  width: 5.5px;
  height: 5.5px;
  border-radius: 50%;
  background-color: white;
  cursor: pointer;
}

.silder-pointer4 {
  position: absolute;
  transform: translate(133px, 6px);
  width: 5.5px;
  height: 5.5px;
  border-radius: 50%;
  background-color: white;
  cursor: pointer;
}

.silder-pointer5 {
  position: absolute;
  transform: translate(173px, 6px);
  width: 5.5px;
  height: 5.5px;
  border-radius: 50%;
  background-color: white;
  cursor: pointer;
}

.scrollingDisplayAreaValue {
  background-color: #424242;
}

.scrollingDisplayAreaValue1 {
  background: linear-gradient(
    to right,
    #0FA68E 50px,
    /* 第一个颜色填充到 55px */ #424242 50px,
    /* 第二个颜色从 55px 开始 */ #424242 170px
  ); /* 第二个颜色填充到 170px */
}

.scrollingDisplayAreaValue2 {
  background: linear-gradient(
    to right,
    #0FA68E 90px,
    /* 第一个颜色填充到 55px */ #424242 90px,
    /* 第二个颜色从 55px 开始 */ #424242 170px
  ); /* 第二个颜色填充到 170px */
}

.scrollingDisplayAreaValue3 {
  background: linear-gradient(
    to right,
    #0FA68E 130px,
    /* 第一个颜色填充到 55px */ #424242 130px,
    /* 第二个颜色从 55px 开始 */ #424242 170px
  ); /* 第二个颜色填充到 170px */
}

.scrolling-display-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 15px; /* 滑块圆点宽度 */
  height: 15px; /* 滑块圆点高度 */
  background: white; /* 滑块圆点颜色 */
  border-radius: 50%; /* 圆形 */
  cursor: pointer;
}

.up-video-play-bottom-video-open-scrolling-color-info-container {
  width: 170px;
  height: 190px;
  position: absolute;
  transform: translate(-85px, -252.5px);
  background-color: rgba(0, 0, 0, 0.8);
  user-select: none;
  visibility: hidden;
  transition: visibility 0.3s ease;
  font-size: 12px;
  padding: 10px 18px 10px 18px;
  color: #ffffff;
  z-index: 10;
}

.up-video-play-bottom-video-open-scrolling-color-info-container-font {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

.up-video-play-bottom-video-open-scrolling-color-info-container-font span {
  width: 80px;
  height: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #424242;
  border-radius: 2px;
  cursor: pointer;
}

.up-video-play-bottom-video-open-scrolling-color-info-container-font .ss:hover {
  background-color: #80807f;
}

.up-video-play-bottom-video-open-scrolling-color-info-container-pattern
  div
  .aa {
  width: 24.5px;
  height: 22px;
}

.scrolling-pattern-roll-container {
  position: absolute;
  color: #cecbcc;
  cursor: pointer;
  transform: translate(0px, 8px);
}

.scrolling-pattern-roll-container:hover {
  color: white;
}

.scrolling-pattern-top-container {
  position: absolute;
  color: #cecbcc;
  cursor: pointer;
  transform: translate(50px, 8px);
}

.scrolling-pattern-top-container:hover {
  color: white;
}

.scrolling-pattern-bottom-container {
  position: absolute;
  color: #cecbcc;
  cursor: pointer;
  transform: translate(100px, 8px);
}

.scrolling-pattern-bottom-container:hover {
  color: white;
}

.up-video-play-bottom-video-open-scrolling-color-info-container-pattern
  div
  .cc2 {
  color: #0FA68E;
}

.scrolling-color-picker {
  position: absolute;
  transform: translate(0px, 68px);
}

/* 颜色选择器：外框 32x32 白色边框，内部色块 22x22 */
.scrolling-color-picker .el-color-picker__trigger {
  box-sizing: border-box;
  width: 32px;
  height: 32px;
  padding: 4px;
  border: 1px solid #ffffff;
  border-radius: 4px;
}

.scrolling-color-picker .el-color-picker__color {
  box-sizing: border-box;
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 2px;
}

/* 颜色选择器旁的彩色弹幕按钮，与选择器间隔 8px
   结构和颜色选择器一致：32x32 白色外框 + 4px 空隙 + 22x22 内盒 */
.scrolling-colorful-picker {
  position: absolute;
  transform: translate(40px, 68px);
  box-sizing: border-box;
  width: 32px;
  height: 32px;
  border: 1px solid #ffffff;
  border-radius: 4px;
  cursor: pointer;
  transition: border-color 0.2s ease;
}

.scrolling-colorful-picker::before {
  content: '';
  position: absolute;
  left: 4px;
  top: 4px;
  width: 22px;
  height: 22px;
  border-radius: 2px;
  background-image: linear-gradient(
    135deg,
    #ff5fa2 0%,
    #ffd166 25%,
    #4dd4e6 50%,
    #6aa8ff 72%,
    #b18cff 88%,
    #ff7ad9 100%
  );
}

.scrolling-colorful-picker-text {
  position: relative;
  z-index: 1;
  display: block;
  width: 100%;
  height: 100%;
  line-height: 30px;
  text-align: center;
  font-size: 15px;
  color: #ffffff;
}

.scrolling-colorful-picker:hover {
  border-color: #cccccc;
}

/* 选中态保持 1px 边框，只换颜色；
   写成双类选择器是为了在:hover 时也能压过上面的 hover 规则 */
.scrolling-colorful-picker.scrolling-colorful-picker-active {
  border-color: #0FA68E;
}

.up-video-play-bottom-video-open-scrolling-color-container:hover
  .up-video-play-bottom-video-open-scrolling-color-info-container {
  transition-delay: 0.3s;
  visibility: visible;
}

.predefineColorsHoverFlag {
  visibility: visible;
}

.inputContainerintoVideoAllDisplayIngFlag {
  position: absolute;
  width: 412px;
  transform: scale(1);
  left: 32%;
  top: 93.2%;
}

.settingScrollintovideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1);
  left: 29.5%;
  top: 93.9%;
}

.closeSettingScrollintovideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1);
  left: 29.5%;
  top: 93.8%;
}

.openOrCloseScrollingintovideoAllDisplayIngFlag {
  position: absolute;
  transform: scale(1);
  left: 27%;
  top: 93.8%;
}

.scrolling-container {
  position: relative;
  /* 弹幕层浮在视频之上，默认不拦截鼠标事件，否则双击视频无法触发全屏 */
  pointer-events: none;
}

.scrolling-boder {
  white-space: nowrap;
  display: flex;
  justify-content: center;
  margin-bottom: 5px;
  left: 100%;
  text-shadow: 1px 1px 0px rgba(0, 0, 0, 0.1); /* 设置阴影：水平偏移，垂直偏移，模糊度，颜色 */
}

.scrollingLocationTop {
  position: absolute;
  width: 700px;
  left: -700px;
  z-index: -2;
}

.scrollingLocationRoll {
  position: absolute;
  z-index: -1;
}

.scrollingLocationBottom {
  position: absolute;
  width: 700px;
  left: -700px;
  z-index: -2;
}

.scrollingLocationTop1 {
  position: absolute;
  width: 100vw;
  left: -100vw;
  top: 5px;
  z-index: -2;
}

.scrollingLocationRoll1 {
  top: 5px;
  position: absolute;
  z-index: -1;
}

.scrollingLocationBottom1 {
  top: 5px;
  width: 100vw;
  left: -100vw;
  position: absolute;
  z-index: -2;
}

@keyframes slideRight {
  from {
    transform: translateX(0px); /* 从下方进入 */
  }
  to {
    transform: translateX(-2350px); /* 到达目标位置 */
  }
}

@keyframes slideRight1 {
  from {
    transform: translateX(0px); /* 从下方进入 */
  }
  to {
    transform: translateX(-3100px); /* 到达目标位置 */
  }
}

.scrolling-list-container {
  position: relative;
  transform: translate(739px, -398px);
  z-index: -5;
  width: 348.5px;
  border-radius: 8px;
  border: 1px solid #EFF3F2;
}

.scrolling-list {
  width: 349px;
}

.scrolling-list-item {
  height: 330px;
  overflow: hidden;
}

.el-collapse-item__header {
  background-color: #EFF3F2 !important;
  border-radius: 7px !important;
  height: 42.5px !important;
  padding: 13px !important;
  border: none;
}

.el-collapse {
  border-top: none !important;
  border-bottom: none !important; /* 去掉整体组件的底部边框 */
}

.el-collapse-item__wrap {
  border-bottom: none !important; /* 去掉每个折叠项的底部边框 */
  border-top: none !important; /* 去掉每个折叠项的顶部边框 */
}

.el-collapse-item:last-child {
  margin-bottom: 0 !important; /* 确保最后一个折叠项没有底部 margin */
  border-top: 0 !important; /* 去掉最后一个折叠项的底部边框 */
}

.scrolling-list-title {
  position: absolute;
  font-size: 14.5px;
  transform: translate(17px, 12.5px);
  cursor: pointer;
}

.scrolling-list-img {
  position: absolute;
  width: 3px;
  height: 13.5px;
  transform: translate(94.5px, 18px);
  cursor: pointer;
}

.scrolling-list-item-top {
  height: 31px;
  color: #5D6764;
  font-size: 11.5px;
  line-height: 31px;
  margin-bottom: 10px;
}

.scrolling-list-item-bottom {
  font-size: 11.5px;
  height: 35px;
  margin-bottom: -11px;
  overflow: hidden;
  cursor: pointer;
}

.scrolling-list-item-bottom-container {
  transform: translate(0px, -6px);
  height: 315px;
  overflow-y: scroll;
}

.scrolling-list-item-bottom-container::-webkit-scrollbar {
  width: 5px; /* 滚动条的宽度 */
  border-radius: 30px; /* 滚动条滑块的圆角 */
}

.scrolling-list-item-bottom-container::-webkit-scrollbar-thumb {
  background: #bcbcbc; /* 滚动条的滑块 */
  height: 10px;
  border-radius: 10px;
}

.scrolling-list-item-content {
  margin-top: 3px;
  display: inline-block;
  color: black;
  margin-left: 21px;
  width: 160px;
  white-space: nowrap;
  text-overflow: ellipsis;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  overflow: hidden;
  transform: translate(0px, -4px);
}

.scrolling-list-item-sendTime {
  display: flex;
  transform: translate(235px, -30px);
}

.scrolling-reocation {
  display: flex;
  justify-content: center;
  line-height: 16px;
  font-size: 11.5px;
  width: 50px;
  border-radius: 2px;
  user-select: none;
  height: 16px;
  transform: translate(235px, -29px);
  border: 1px solid #0FA68E;
  color: #0FA68E;
}

.scrolling-reocation:hover {
  background-color: #0FA68E;
  color: white;
}

.scrolling-list-item-bottom-end {
  transform: translate(0px, 40px);
  width: 50px;
  background-color: #0FA68E;
  height: 30px;
}

.up-video-controls-container {
  display: flex;
  align-items: center;
  width: 700.5px;
  height: 55.5px;
  user-select: none;
  transform: translate(9.5px, 100px);
  border-bottom: 1px solid #E0E5E3;
}

.up-video-controls-img0 {
  position: absolute;
  left: -30px;
  top: 5px;
  width: 75px;
  z-index: 100;
  height: 55px;
}

.up-video-controls-img1 {
  width: 23px;
  height: 23px;
}

.up-video-controls-img2 {
  width: 23px;
  height: 23px;
  transition: none;
}

.up-video-controls-img3 {
  width: 23px;
  height: 23px;
}

.up-video-controls-img4 {
  height: 20px;
  width: 24px;
}

.up-video-controls-img5 {
  width: 3.5px;
  height: 16.5px;
  position: absolute;
  left: 686px;
  top: 21px;
  cursor: pointer;
}

.up-video-controls-container .up-video-controls-span {
  color: #686b71;
  font-size: 12.5px;
  transition: none;
  cursor: pointer;
}

.up-video-controls-container .up-video-controls-span:hover {
  color: #0FA68E;
}

.video-share-container{
  position: absolute;
  top: 280px;
  right: 80px;
  z-index: 1000;
}

.up-video-controls-span1 {
  position: absolute;
  left: 36px;
  top: 21.5px;
}

.up-video-controls-span2 {
  position: absolute;
  left: 136px;
  top: 21.5px;
}

.up-video-controls-span3 {
  position: absolute;
  left: 236px;
  top: 21.5px;
}

.up-video-controls-span4 {
  position: absolute;
  left: 336px;
  top: 21.5px;
}

@keyframes likeVideoMove {
  10% {
    transform: translate(1px, -1px);
  }
  20% {
    transform: translate(1.5px, 1.5px);
  }
  30% {
    transform: translate(-1.5px, 1.5px);
  }
  40% {
    transform: translate(-1.5px, -1.5px);
  }
  50% {
    transform: translate(1.5px, -1.5px);
  }
  60% {
    transform: translate(1.5px, 1.5px);
  }
  70% {
    transform: translate(-1.5px, 1.5px);
  }
  80% {
    transform: translate(-1.5px, -1.5px);
  }
  90% {
    transform: translate(1.5px, -1.5px);
  }
  100% {
    transform: translate(1.5px, 1.5px);
  }
}

.up-video-controls-div1 {
  position: absolute;
  width: 30px;
  height: 30px;
  left: -3px;
  top: 12.5px;
}

.up-video-controls-div2 {
  position: absolute;
  width: 28px;
  height: 28px;
  left: 98.5px;
  top: 13.5px;
}

.up-video-controls-div3 {
  position: absolute;
  width: 30px;
  height: 30px;
  left: 197px;
  top: 13.5px;
}

@keyframes round {
  0% {
    border-radius: 50%;
    border: none; /* 初始没有边框 */
  }
  20% {
    border-radius: 50%;
    border-top: 1.5px solid rgba(60,192,166, 0.3); /* 先设置透明边框 */
  }
  40% {
    border-radius: 50%;
    border-top: 1.5px solid #0FA68E;
    border-right: 1.5px solid rgba(60,192,166, 0.3); /* 右边框透明 */
  }
  60% {
    border-radius: 50%;
    border-top: 1.5px solid #0FA68E;
    border-right: 1.5px solid #0FA68E;
    border-bottom: 1.5px solid rgba(60,192,166, 0.3); /* 底边框透明 */
  }
  80% {
    border-radius: 50%;
    border-top: 1.5px solid #0FA68E;
    border-right: 1.5px solid #0FA68E;
    border-bottom: 1.5px solid #0FA68E; /* 底边框透明 */
    border-left: 1.5px solid rgba(60,192,166, 0.3); /* 左边框透明 */
  }
  100% {
    border-radius: 50%;
    border: 1.5px solid #0FA68E;
  }
}

.dot {
  position: absolute;
  width: 4px; /* 小圆点大小 */
  height: 4px; /* 小圆点大小 */
  background-color: #0FA68E; /* 小圆点颜色 */
  border-radius: 50%; /* 圆形 */
  opacity: 0;
  visibility: hidden;
}

.dot:nth-child(1) {
  top: 101%;
  left: 50%;
  animation: dotMoveBottom 0.5s ease-in-out infinite;
  background-color: #E2603C;
}
.dot:nth-child(2) {
  top: -4%;
  left: 50%;
  animation: dotMoveTop 0.5s ease-in-out infinite;
  background-color: rgb(248, 255, 43);
}
.dot:nth-child(3) {
  top: 4%;
  left: 25%;
  animation: dotMoveTopLeft 0.5s ease-in-out infinite;
  background-color: red;
}
.dot:nth-child(4) {
  top: 4%;
  left: 75%;
  animation: dotMoveTopRight 0.5s ease-in-out infinite;
  background-color: #0FA68E;
}
.dot:nth-child(5) {
  top: 50%;
  left: 100%;
  animation: dotMoveRight 0.5s ease-in-out infinite;
  background-color: rgb(25, 245, 25);
}
.dot:nth-child(6) {
  top: 50%;
  left: -3%;
  animation: dotMoveLeft 0.5s ease-in-out infinite;
  background-color: #fc20b3;
}
.dot:nth-child(7) {
  top: 96%;
  left: 25%;
  animation: dotMoveBottomLeft 0.5s ease-in-out infinite;
  background-color: #c921f8;
}
.dot:nth-child(8) {
  top: 96%;
  left: 75%;
  animation: dotMoveBottomRight 0.5s ease-in-out infinite;
  background-color: coral;
}
.dot:nth-child(9) {
  top: 77%;
  left: 95%;
  animation: dotMoveRightBottom 0.5s ease-in-out infinite;
  background-color: #47fdc6;
}
.dot:nth-child(10) {
  top: 23%;
  left: 3%;
  animation: dotMoveLeftTop 0.5s ease-in-out infinite;
  background-color: #882dfe;
}
.dot:nth-child(11) {
  top: 78%;
  left: 3%;
  animation: doMoveLeftBottom 0.5s ease-in-out infinite;
  background-color: #2436ff;
}
.dot:nth-child(12) {
  top: 22%;
  left: 97%;
  animation: dotMoveRightTop 0.5s ease-in-out infinite;
  background-color: #23d46f;
}

@keyframes dotMoveBottom {
  25% {
    transform: translate(0px, 4px) scale(1.5);
    height: 5px;
    width: 3px;
  }
  50% {
    transform: translate(0px, 8px) scale(2);
    height: 6px;
    width: 2.5px;
  }
  75% {
    transform: translate(0px, 12px) scale(1.5);
    height: 5px;
    width: 3px;
  }
  100% {
    transform: translate(0px, 16px) scale(1);
    height: 4px;
    width: 2.5px;
  }
}

@keyframes dotMoveTop {
  25% {
    transform: translate(0px, -4px) scale(1.5);
    height: 5px;
    width: 3px;
  }
  50% {
    transform: translate(0px, -8px) scale(2);
    height: 6px;
    width: 2.5px;
  }
  75% {
    transform: translate(0px, -12px) scale(1.5);
    height: 5px;
    width: 3px;
  }
  100% {
    transform: translate(0px, -16px) scale(1);
    height: 4px;
    width: 2.5px;
  }
}

@keyframes dotMoveLeft {
  25% {
    transform: translate(-4px, 0px) scale(1.5);
    height: 3px;
    width: 5px;
  }
  50% {
    transform: translate(-8px, 0px) scale(2);
    height: 2.5px;
    width: 6px;
  }
  75% {
    transform: translate(-12px, 0px) scale(1.5);
    height: 3px;
    width: 5px;
  }
  100% {
    transform: translate(-16px, 0px) scale(1);
    height: 2.5px;
    width: 4px;
  }
}

@keyframes dotMoveRight {
  25% {
    transform: translate(4px, 0px) scale(1.5);
    height: 3px;
    width: 5px;
  }
  50% {
    transform: translate(8px, 0px) scale(2);
    height: 2.5px;
    width: 6px;
  }
  75% {
    transform: translate(12px, 0px) scale(1.5);
    height: 3px;
    width: 5px;
  }
  100% {
    transform: translate(16px, 0px) scale(1);
    height: 2.5px;
    width: 4px;
  }
}

@keyframes dotMoveRightTop {
  25% {
    transform: translate(4px, -4px) scale(1.5) rotate(-30deg);
    height: 3px;
    width: 5px;
  }
  50% {
    transform: translate(8px, -8px) scale(2) rotate(-30deg);
    height: 2.5px;
    width: 6px;
  }
  75% {
    transform: translate(12px, -12px) scale(1.5) rotate(-30deg);
    height: 3px;
    width: 5px;
  }
  100% {
    transform: translate(16px, -16px) scale(1) rotate(-30deg);
    height: 2.5px;
    width: 4px;
  }
}

@keyframes dotMoveLeftTop {
  25% {
    transform: translate(-4px, -4px) scale(1.5) rotate(45deg);
    height: 3px;
    width: 5px;
  }
  50% {
    transform: translate(-8px, -8px) scale(2) rotate(45deg);
    height: 2.5px;
    width: 6px;
  }
  75% {
    transform: translate(-12px, -12px) scale(1.5) rotate(45deg);
    height: 3px;
    width: 5px;
  }
  100% {
    transform: translate(-16px, -16px) scale(1) rotate(45deg);
    height: 2.5px;
    width: 4px;
  }
}

@keyframes dotMoveRightBottom {
  25% {
    transform: translate(4px, 4px) scale(1.5) rotate(45deg);
    height: 3px;
    width: 5px;
  }
  50% {
    transform: translate(8px, 8px) scale(2) rotate(45deg);
    height: 2.5px;
    width: 6px;
  }
  75% {
    transform: translate(12px, 12px) scale(1.5) rotate(45deg);
    height: 3px;
    width: 5px;
  }
  100% {
    transform: translate(16px, 16px) scale(1) rotate(45deg);
    height: 2.5px;
    width: 4px;
  }
}

@keyframes dotMoveBottomLeft {
  25% {
    transform: translate(-4px, 4px) scale(1.5) rotate(-45deg);
    height: 3px;
    width: 5px;
  }
  50% {
    transform: translate(-8px, 8px) scale(2) rotate(-45deg);
    height: 2.5px;
    width: 6px;
  }
  75% {
    transform: translate(-12px, 12px) scale(1.5) rotate(-45deg);
    height: 3px;
    width: 5px;
  }
  100% {
    transform: translate(-16px, 16px) scale(1) rotate(-45deg);
    height: 2.5px;
    width: 4px;
  }
}

@keyframes dotMoveTopRight {
  25% {
    transform: translate(4px, -4px) scale(1.5) rotate(-45deg);
    height: 3px;
    width: 5px;
  }
  50% {
    transform: translate(8px, -8px) scale(2) rotate(-45deg);
    height: 2.5px;
    width: 6px;
  }
  75% {
    transform: translate(12px, -12px) scale(1.5) rotate(-45deg);
    height: 3px;
    width: 5px;
  }
  100% {
    transform: translate(16px, -16px) scale(1) rotate(-45deg);
    height: 2.5px;
    width: 4px;
  }
}

@keyframes dotMoveTopLeft {
  25% {
    transform: translate(-4px, -4px) scale(1.5) rotate(60deg);
    height: 3px;
    width: 5px;
  }
  50% {
    transform: translate(-8px, -8px) scale(2) rotate(60deg);
    height: 2.5px;
    width: 6px;
  }
  75% {
    transform: translate(-12px, -12px) scale(1.5) rotate(60deg);
    height: 3px;
    width: 5px;
  }
  100% {
    transform: translate(-16px, -16px) scale(1) rotate(60deg);
    height: 2.5px;
    width: 4px;
  }
}

@keyframes dotMoveBottomRight {
  25% {
    transform: translate(4px, 4px) scale(1.5) rotate(60deg);
    height: 3px;
    width: 5px;
  }
  50% {
    transform: translate(8px, 8px) scale(2) rotate(60deg);
    height: 2.5px;
    width: 6px;
  }
  75% {
    transform: translate(12px, 12px) scale(1.5) rotate(60deg);
    height: 3px;
    width: 5px;
  }
  100% {
    transform: translate(16px, 16px) scale(1) rotate(60deg);
    height: 2.5px;
    width: 4px;
  }
}

@keyframes doMoveLeftBottom {
  25% {
    transform: translate(-4px, 4px) scale(1.5) rotate(-30deg);
    height: 3px;
    width: 5px;
  }
  50% {
    transform: translate(-8px, 8px) scale(2) rotate(-30deg);
    height: 2.5px;
    width: 6px;
  }
  75% {
    transform: translate(-12px, 12px) scale(1.5) rotate(-30deg);
    height: 3px;
    width: 5px;
  }
  100% {
    transform: translate(-16px, 16px) scale(1) rotate(-30deg);
    height: 2.5px;
    width: 4px;
  }
}

@keyframes becomeBigger {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2) rotate(-15deg);
  }
  100% {
    transform: scale(1);
  }
}

.threeAnmationBefore {
  animation: likeVideoMove 0.4s linear infinite;
}

.threeAnmationAfter {
  animation: becomeBigger 0.5s linear forwards;
}

.dotDisplay2 {
  animation: round 2.5s linear infinite;
}

.dotDisplay .dot {
  opacity: 1;
  visibility: visible;
}

.key-info {
  position: absolute;
  width: 56px;
  height: 24px;
  padding: 3px 4px 3px 4px;
  border-radius: 2px;
  font-size: 12px;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: black;
  transform: translate(318px, 182px);
  z-index: 10;
}

.keyInfoAllDisplay {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

.audio-info {
  position: absolute;
  background-color: #dbd4d3;
  border-radius: 5px;
  width: 100px;
  height: 47px;
  font-size: 19px;
  color: black;
  display: flex;
  justify-content: center;
  align-items: center;
  transform: translate(300px, 174px);
}

.audio-info span {
  position: absolute;
  transform: translate(17px);
}

.audioInfoAllDisplay {
 position: absolute;
 left: 50%;
 top: 50%;
 transform: translate(-50%, -50%);
}

.audio-info .img1 {
  position: absolute;
  transform: translate(-26.5px);
  width: 26px;
  height: 27.5px;
}

.audio-info .img2 {
  position: absolute;
  transform: translate(-26.5px);
  width: 24px;
  height: 22.5px;
}

.userselect {
  user-select: none;
}

.likeVideoClickFlag {
  color: #0FA68E;
}

.videoThrowCoinClickFlag {
  color: #0FA68E;
}

.videoCollectClickFlag {
  color: #0FA68E;
}

.videoShareClickFlag {
  color: #0FA68E;
}

.collect-dialog {
  transform: translate(-8px, 32px);
  height: 427px;
}

.collect-dialog-title {
  width: 100px;
  font-size: 15.5px;
  transform: translate(148px, -17px);
  color: black;
}

.dialog-footer-input-container {
  width: 346px;
  height: 34.5px;
  transform: translate(20.5px, 245px);
  margin-bottom: -36px;
  border: 1px solid #8F9794;
  border-radius: 5px;
  cursor: pointer;
  line-height: 29.5px;
}

.dialog-footer-input-container1 {
  width: 346px;
  height: 34.5px;
  transform: translate(20.5px, 245px);
  margin-bottom: -36px;
  border: 1px solid #0FA68E;
  border-radius: 5px;
  cursor: pointer;
  line-height: 29.5px;
}

.dialog-footer-input-container:focus {
  border: 1px solid #0FA68E;
}

.dialog-footer-input-container:hover {
  border: 1px solid #0FA68E;
}

.dialog-footer-input-container img {
  width: 14px;
  height: 14px;
  transform: translate(-240px, 3.5px);
  margin-right: -230px;
}

.dialog-footer-input-container span {
  font-size: 12px;
  color: #5C6664;
}

.collect-list-container {
  width: 384.5px;
  height: 254px;
  transform: translate(20px, -18px);
  cursor: pointer;
  margin-bottom: -254px;
  overflow-x: hidden;
}

.collect-list-item {
  width: 342px;
  height: 20px;
  transform: translate(0px, 25px);
  margin-bottom: 27px;
}

.collect-list-item-img {
  transform: translate(-161px, 0.5px);
}

.collect-list-item-name {
  display: flex;
  font-size: 13.5px;
  transform: translate(43px, -23px);
}

.collect-unPublic{
  position: relative;
  left: 5px;
  top: -1px;
  color: #8D9794;
  font-size: 14px;
}

.collect-list-item:hover .collect-list-item-name {
  color: #0FA68E;
}

.collect-list-item-num {
  display: flex;
  font-size: 11.5px;
  color: #5D6764;
  transform: translate(322.5px, -41.5px);
}

.collect-list-container::-webkit-scrollbar {
  width: 5px;
}

.collect-list-container::-webkit-scrollbar-thumb {
  background-color: #8F9794;
  border-radius: 5px;
}

.dialog-footer-item {
  background-color: #E0E5E3;
  color: #8D9794;
  border-radius: 5px;
  width: 160px;
  height: 40px;
  font-size: 13.5px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transform: translate(114.5px, 299.5px);
}

.dialog-footer-item1 {
  background-color: #0FA68E;
  color: white;
  border-radius: 5px;
  width: 160px;
  height: 40px;
  font-size: 13.5px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transform: translate(114.5px, 299.5px);
}

.dialog-footer-item1:hover {
  opacity: 0.9;
}

.dialog-footer-input {
  width: 240px;
  height: 31px;
  transform: translate(8px, -0.5px);
  border: none;
  font-size: 12.5px;
}

.dialog-footer-input:focus {
  outline: none;
}

.dialog-footer-input::-webkit-input-placeholder {
  font-size: 12px;
}

.dialog-footer-input-btn {
  display: inline-block;
  width: 89px;
  border-radius: 0px 5px 5px 0px;
  margin-left: 12px;
  font-size: 13.5px;
  background-color: #d9f1f9;
  height: 34.5px;
  color: #0FA68E;
  line-height: 34px;
  border-left: 1px solid #0FA68E;
}

.new-create-collect-info {
  position: absolute;
  color: white;
  background-color: rgb(255, 21, 0, 0.8);
  transform: translate(-20px, -80px);
  width: 120px;
  height: 36px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 5px;
}

.addWaitWatch {
  position: absolute;
  width: 26px;
  height: 20px;
  user-select: none;
  cursor: pointer;
  transform: translate(683.5px, 64px);
  z-index: 100;
}

.addWaitWatch .wait-img {
  width: 3.5px;
  height: 16.5px;
  margin-left: 12px;
}

.addWaitWatch:hover .wait-watch-info {
  opacity: 1;
  visibility: visible;
}

.wait-watch-info {
  left: 50%;
  transform: translate(-50%);
  position: absolute;
  top: -70px;
  width: 120px;
  cursor: pointer;
  padding: 10px 0 10px 0;
  border-radius: 10px;
  background-color: white;
  box-shadow: 2px 1px 3px rgba(0, 0, 0, 0.06),
    /* 右下阴影 */ -2px -1px 3px rgba(0, 0, 0, 0.06),
    /* 左上阴影 */ 0 1px 2px rgba(0, 0, 0, 0.03); /* 下方阴影 */
  opacity: 0;
  overflow: hidden;
  visibility: hidden;
  transition: visibility 0.3s ease, opacity 0.3s ease;
}

.wait-watch-info span {
  height: 32px;
  display: flex;
  margin-right: -20px;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: black;
}

.wait-watch-info span:hover {
  background-color: #EFF3F2;
}

.wait-watch-info img {
  position: absolute;
  width: 20px;
  height: 19px;
  transform: translate(20px, 6.5px);
}

.waitWatchMsg {
  position: absolute;
  width: 120px;
  font-size: 12px;
  height: 26px;
  border-radius: 5px;
  left: -47px;
  top: -58px;
  padding: 2px 0 2px 0;
  background-color: rgb(0, 0, 0, 0.8);
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
}

.throwCoinDialog {
  height: 475.5px;
  transform: translate(-8px);
}

.throw-coin-title {
  position: absolute;
  left: 49.7%;
  transform: translate(-50%, -23px);
  font-size: 15.5px;
}

.throw-coin-title span {
  font-size: 28px;
  margin: 0 5px 0 5px;
  color: #0FA68E;
}

.throw-coin-submit {
  position: absolute;
  width: 66px;
  height: 31px !important;
  font-size: 15.5px;
  background-color: #0FA68E !important;
  color: white !important;
  left: 50%;
  transform: translate(-50%, 343px);
  border: none !important;
}

.throw-coin-img-content1 {
  position: absolute;
  width: 160px;
  height: 230px;
  cursor: pointer;
  border: 2px dashed #cdd0d5;
  left: 14%;
  transform: translate(-14%, 52px);
  border-radius: 5px;
  background-image: url("/img/22-coin-ani.png");
  background-repeat: no-repeat;
  background-position: 20px 18px;
  background-size: auto 193px;
  animation: throwCoinImg1 1s steps(24) infinite;
}

.throw-coin-img-content1 .overlay1 {
  position: absolute;
  width: 20px;
  height: 30%;
  top: 50%;
  left: 87.5%;
  background-color: white;
}

.throw-coin-img-content1 .overlay2 {
  position: absolute;
  width: 20px;
  height: 70%;
  top: 15%;
  left: 0%;
  background-color: white;
}

.throw-coin-img-content1:hover {
  border: 2px dashed #0FA68E;
}

.throw-coin-img-content1 .img-static {
  width: 120px;
  height: 193px;
  position: absolute;
  left: 50%;
  transform: translate(-50%);
}

.throw-coin-img-content1 span {
  color: #8F9794;
  font-size: 13.5px;
  display: block;
  transform: translate(-110px, 10px);
}

.throw-coin-img-content2 {
  position: absolute;
  width: 160px;
  cursor: pointer;
  height: 230px;
  border: 2px dashed #cdd0d5;
  left: 88.4%;
  transform: translate(-88.4%, 52px);
  border-radius: 5px;
  background-image: url("/img/33-coin-ani.png");
  background-repeat: no-repeat;
  background-position: 19px 17.5px;
  background-size: auto 193px;
  animation: throwCoinImg2 1s steps(24) infinite;
}

.throw-coin-img-content2 .overlay1 {
  position: absolute;
  width: 20px;
  height: 30%;
  top: 15%;
  left: 87.5%;
  background-color: white;
}

.throw-coin-img-content2 .overlay2 {
  position: absolute;
  width: 20px;
  height: 55%;
  top: 15%;
  left: 0%;
  background-color: white;
}

@keyframes throwCoinImg1 {
  0% {
    background-position: 20px 17.5px;
  }
  100% {
    background-position: -2867.27px 17.5px;
  }
}

@keyframes throwCoinImg2 {
  0% {
    background-position: 19px 18px;
  }
  100% {
    background-position: -2868.27px 18px;
  }
}

.throw-coin-img-content2:hover {
  border: 2px dashed #0FA68E;
}

.throw-coin-img-content2 .img-static {
  width: 122px;
  height: 193px;
  position: absolute;
  left: 50%;
  transform: translate(-50%);
}

.throw-coin-img-content2 span {
  color: #8F9794;
  font-size: 13px;
  display: block;
  transform: translate(-110px, 10px);
}

.throw-coin-check-box {
  position: absolute;
  transform: translate(21.5px, 302px);
  font-size: 12px;
  display: flex;
  cursor: pointer;
  align-items: center;
}

.throw-coin-check-box img {
  width: 16px;
  height: 16px;
  margin-right: 5px;
}

.throw-coin-fotter {
  position: absolute;
  left: 50%;
  font-size: 12px;
  color: #8F9794;
  transform: translate(-50%, 390px);
}

.throwCoinNumberConten1 {
  border: 2px solid #0FA68E;
}

.throwCoinNumberConten1:hover {
  border: 2px solid #0FA68E;
}

.throwCoinNumberConten2 {
  border: 2px solid #0FA68E;
}

.throwCoinNumberConten2:hover {
  border: 2px solid #0FA68E;
}

.throwCoinNumberContenImg1 {
  background-image: none;
}

.throwCoinNumberContenImg2 {
  background-image: none;
}

.scrolling-content-container {
  cursor: pointer;
  /* 仅文字可点击，保留点击复制弹幕的功能 */
  pointer-events: auto;
}

/* 彩色弹幕：8 段霓虹渐变过渡柔和不刺眼。
   渐变必须作用在文字 span 上而不是外层盒子——
   顶部/底部弹幕的外层宽度是 700px，渐变铺满整条就会把颜色拉得很稀，
   而滚动弹幕外层宽度就等于文字宽度，两种表现会不一致。 */
.scrolling-boder.scrollingColorful {
  color: transparent;
}

.scrolling-boder.scrollingColorful .scrolling-content-container {
  background-image: linear-gradient(
    100deg,
    #ff5fa2 0%,
    #ff9a6b 14%,
    #ffd166 28%,
    #8ce99a 42%,
    #4dd4e6 56%,
    #6aa8ff 70%,
    #b18cff 84%,
    #ff7ad9 100%
  );
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  text-shadow: none;
  font-weight: 700;
  /* 发光半径收到最小：1px 白色高光 + 3px 冷色辉光 + 暗色描边，
     半径越小边缘越锐利，越大越容易在深色画面上糊成雾。 */
  filter: drop-shadow(0 0 1px rgba(255, 255, 255, 0.6))
    drop-shadow(0 0 3px rgba(111,210,187, 0.3))
    drop-shadow(0 1px 1px rgba(0, 0, 0, 0.6));
}

.video-content-container {
  position: relative;
  top: 115px;
  left: 9px;
  font-size: 14px;
  height: auto;
  width: 690px;
  color: #1C2321;
  line-height: 24px;
  padding-bottom: 5px;
}

.video-content-container-switch {
  position: relative;
  padding-top: 8px;
  top: 117px;
  left: 9px;
  font-size: 12.5px;
  line-height: 18px;
  color: #5C6664;
  span {
    cursor: pointer;
  }
  span:hover {
    color: #0FA68E;
  }
}

.videoContentFlag {
  height: 80px;
  overflow: hidden;
}

.video-tage-container {
  position: relative;
  width: 700px;
  top: 132px;
  left: 9px;
  padding-bottom: 14px;
  border-bottom: 1px solid #E0E5E3;
  span {
    margin-right: 15px;
    color: #5C6664;
    background: #EFF3F2;
    height: 28px;
    line-height: 28px;
    border-radius: 14px;
    font-size: 13px;
    padding: 0 12px;
    box-sizing: border-box;
    transition: all 0.3s;
    display: inline-flex;
    align-items: center;
    cursor: pointer;
  }
}

a {
  text-decoration: none;
  color: #0A8271;
}

a:hover {
  color: #0FA68E;
}

.autoPlayVideoContainer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 94%;
  background-color: #000000;
}

.autoPlayVideoContainer1 {
  height: 100%;
}

.videoPostion {
  position: absolute;
  top: 295px;
  left: 10px;
  width: 155px;
  height: 25px;
  font-size: 14px;
  padding: 10px;
  border-radius: 5px;
  display: flex;
  align-items: center;
  background-color: rgb(0, 0, 0, 0.5);
  color: white;
  user-select: none;
  img {
    position: absolute;
    width: 13px;
    height: 13px;
    top: 17.5px;
    left: 18px;
    cursor: pointer;
    transition: opacity 0.3s ease;
  }
  .img2 {
    opacity: 0;
  }

  .closePostionHoverFlag {
    opacity: 1;
    transition: opacity 0.3s ease;
  }

  span {
    margin-left: 32px;
  }
}

.closePostionFlagAllDisplay {
  left: 15px;
  top: 678px;
  transform: scale(1.05);
}

.video-waiting{
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  div{
    animation: bpx-animation-loading .94s steps(1) infinite;
    background: url(#) no-repeat;
    height: 184px;
    transform: scale(.5);
    width: 320px;
  }
}

@keyframes bpx-animation-loading {
  0% {
    background-position: 0 0;
  } 
  6.25% {
    background-position: -320px 0;
  }
  12.5% {
      background-position: -640px 0;
  }
  18.75% {
      background-position: -960px 0;
  }
  25% {
      background-position: -1280px 0;
  }
  31.25% {
      background-position: -1600px 0;
  }
  37.5% {
      background-position: -1920px 0;
  }
  43.75% {
      background-position: -2240px 0;
  }
  50% {
      background-position: -2560px 0;
  }
  56.25% {
      background-position: -2880px 0;
  }
  62.5% {
      background-position: -3200px 0;
  }
  68.75% {
      background-position: -3520px 0;
  }
  75% {
      background-position: -3840px 0;
  }
  81.25% {
      background-position: -4160px 0;
  }
  87.5% {
      background-position: -4480px 0;
  }
  93.75% {
      background-position: 0 -184px;
  }
  100% {
      background-position: -320px -184px;
  }
}


</style>
