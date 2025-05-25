<template>
  <div class="header" v-external-link>
    <ul class="v-header-ul" :class="{ head2Flag: head2Flag }">
      <li>
        <a class="head-li1" href="../">
          <img v-show="!head2Flag" src="../img/首页.png" alt="首页" style="width: 18px;margin-top: 1px;" />
          <img v-show="head2Flag" src="../img/bilibili蓝.png" alt="首页" style="width: 60px;margin-top: 2px;margin-right: 23px;animation: none;transform: translate(-2px);"/>
            首页
          <img v-show="head2Flag" src="../img/头首页.png" class="head-home" />
          </a
        >
      </li>
      <li>
        <a href="https://www.bilibili.com/anime/?spm_id_from=333.1007.0.0"
          >番剧</a
        >
      </li>
      <li>
        <a href="https://live.bilibili.com/?spm_id_from=333.1007.0.0">直播</a>
      </li>
      <li>
        <a href="https://game.bilibili.com/platform?spm_id_from=333.1007.0.0"
          >游戏中心</a
        >
      </li>
      <li>
        <a
          href="https://show.bilibili.com/platform/home?msource=pc_web&spm_id_from=333.1007.0.0"
          >会员购</a
        >
      </li>
      <li>
        <a
          href="https://manga.bilibili.com/?from=bill_top_mnav&spm_id_from=333.1007.0.0"
          >漫画</a
        >
      </li>
      <li>
        <a href="https://www.bilibili.com/match/home/?spm_id_from=333.1007.0.0"
          >赛事</a
        >
      </li>
      <li>
        <a href="https://app.bilibili.com/?spm_id_from=333.1007.0.0"
          >
          <img
            v-show="!head2Flag"
            src="../img/下载.png"
            alt="下载客户端"
            height="14px"
          />
          <img
            v-show="head2Flag"
            src="../img/下载黑.png"
            height="14px"
          />
          下载客户端</a
        >
      </li>
      <li>
        <search v-if="!searchFlag" class="head-search" :head2Flag="head2Flag" :class="{ head2SearchFlag: head2Flag } "/>
        <div v-if="searchFlag" class="head-search" :class="{ head2SearchFlag: head2Flag } "></div>
      </li>
      <li>
        <div v-if="user.userName !== null" class="avatar-container">
          <div class="avatar1" @click="openHome(1)">
            <img
              :src="store.userInformation?.avatarAddress"
              alt="Avatar"
              style="
                border-radius: 50%;
                cursor: pointer;
                width: 31.5px;
                height: 31.5px;
                border: 1.5px solid #ffffff;
              "
            />
          </div>
          <div class="user-info">
            <div style="margin-top: 15px; font-size: 18px;" @click="openHome(1)">{{store.userInformation?.userName}}</div>
            <div>
              <span style="font-size: 12px; color: #95999f;"
              >硬币：<span style="color: black">{{ user.coinNumber }}</span>
              &nbsp;&nbsp;b币：<span style="color: black">0</span>
              </span>
            </div>
            <div
              style="display: flex; justify-content: center; margin-top: 10px"
              class="grade"
            >
              <span class="grade1" v-if="user.grade === 0">
                <img class="exp" src="../img/0级.png" alt="0级" />
                <el-progress
                  :percentage="user.exp"
                  color="#edcc8e"
                  :show-text="false"
                  style="width: 180px; transform: translateX(-6%)" /><img
                  class="exp2"
                  src="../img/1级.png"
                  alt="1级" /></span
              ><span
                v-if="user.grade === 0"
                style="
                  font-size: 12px;
                  color: #caccd0;
                  transform: translateX(-5%);
                  margin-top: 5px;
                "
                >当前成长{{ user.exp * 10 }}，距离升级Lv.1还需要{{
                  100 * 10 - user.exp * 10
                }}</span
              >
              <span class="grade1" v-if="user.grade === 1">
                <img class="exp" src="../img/1级.png" alt="1级" />
                <el-progress
                  :percentage="user.exp"
                  color="#edcc8e"
                  :show-text="false"
                  style="width: 180px; transform: translateX(-6%)" /><img
                  class="exp2"
                  src="../img/2级灰色.png"
                  alt="2级" /></span
              ><span
                v-if="user.grade === 1"
                style="
                  font-size: 12px;
                  color: #caccd0;
                  transform: translateX(-5%);
                  margin-top: 5px;
                "
                >当前成长{{ user.exp * 20 }}，距离升级Lv.2还需要{{
                  100 * 20 - user.exp * 20
                }}</span
              >
              <span class="grade1" v-if="user.grade === 2">
                <img class="exp" src="../img/2级.png" alt="2级" />
                <el-progress
                  :percentage="user.exp"
                  color="#edcc8e"
                  :show-text="false"
                  style="width: 180px; transform: translateX(-6%)" /><img
                  class="exp2"
                  src="../img/3级灰色.png"
                  alt="3级" /></span
              ><span
                v-if="user.grade === 2"
                style="
                  font-size: 12px;
                  color: #caccd0;
                  transform: translateX(-5%);
                  margin-top: 5px;
                "
                >当前成长{{ user.exp * 30 }}，距离升级Lv.3还需要{{
                  100 * 30 - user.exp * 30
                }}</span
              >
              <span class="grade1" v-if="user.grade === 3">
                <img class="exp" src="../img/3级.png" alt="3级" />
                <el-progress
                  :percentage="user.exp"
                  color="#edcc8e"
                  :show-text="false"
                  style="width: 180px; transform: translateX(-6%)" /><img
                  class="exp2"
                  src="../img/4级灰色.png"
                  alt="4级" /></span
              ><span
                v-if="user.grade === 3"
                style="
                  font-size: 12px;
                  color: #caccd0;
                  transform: translateX(-5%);
                  margin-top: 5px;
                "
                >当前成长{{ user.exp * 40 }}，距离升级Lv.4还需要{{
                  100 * 40 - user.exp * 40
                }}</span
              >
              <span class="grade1" v-if="user.grade === 4">
                <img class="exp" src="../img/4级.png" alt="4级" />
                <el-progress
                  :percentage="user.exp"
                  color="#edcc8e"
                  :show-text="false"
                  style="width: 180px; transform: translateX(-6%)" /><img
                  class="exp2"
                  src="../img/5级灰色.png"
                  alt="5级" /></span
              ><span
                v-if="user.grade === 4"
                style="
                  font-size: 12px;
                  color: #caccd0;
                  transform: translateX(-5%);
                  margin-top: 5px;
                "
                >当前成长{{ user.exp * 50 }}，距离升级Lv.5还需要{{
                  100 * 50 - user.exp * 50
                }}</span
              >
              <span class="grade1" v-if="user.grade === 5">
                <img class="exp" src="../img/5级.png" alt="5级" />
                <el-progress
                  :percentage="user.exp"
                  color="#edcc8e"
                  :show-text="false"
                  style="width: 180px; transform: translateX(-6%)" /><img
                  class="exp2"
                  src="../img/6级灰色.png"
                  alt="6级" /></span
              ><span
                v-if="user.grade === 5"
                style="
                  font-size: 12px;
                  color: #caccd0;
                  transform: translateX(-5%);
                  margin-top: 5px;
                "
                >当前成长{{ user.exp * 60 }}，距离升级Lv.6还需要{{
                  100 * 60 - user.exp * 60
                }}</span
              >
              <span class="grade1" v-if="user.grade === 6">
                <img class="exp" src="../img/6级.png" alt="6级" />
                <el-progress
                  :percentage="user.exp"
                  color="red"
                  :show-text="false"
                  style="width: 180px; transform: translateX(-6%)" /></span
              ><span
                v-if="user.grade === 6"
                style="font-size: 12px; color: #caccd0; margin-top: 5px"
                >你已经满级了！</span
              >
            </div>
             <div class="user-info-number">
               <span @click="openHome(8)">
                 <div>{{ user.followNumber }}</div>
                 <div>关注</div>
              </span>
               <span @click="openHome(9)">
                 <div>{{ user.fansNumber }} <span v-if="NewFansNumber.value">+{{ NewFansNumber.value }}11</span></div>
                 <div>粉丝</div>
              </span>
               <span @click="openDynamic">
                 <div>{{ user.ownDynamicNumber }}</div>
                 <div>动态</div>
               </span>
             </div>
            <a
              style="animation: none !important"
              target="_blank"
              rel="noopener noreferrer"
              href="https://www.bilibili.com/#:~:text=%E5%8A%A8%E6%80%81-,%E6%88%90%E4%B8%BA%E5%A4%A7%E4%BC%9A%E5%91%98,-%E4%BA%86%E8%A7%A3%E6%9B%B4%E5%A4%9A%E6%9D%83%E7%9B%8A"
            >
              <div
                style="
                  width: 220px;
                  display: flex;
                  flex-direction: column;
                  height: 50px;
                  background-color: #fcedf1;
                  margin-left: 16px;
                  margin-top: 10px;
                  border-radius: 5px;
                  margin-bottom: 10px;
                "
              >
                <span
                  style="
                    color: #ed7098;
                    font-size: 13px;
                    transform: translate(-65px, 10px);
                  "
                  >成为大会员
                  <div
                    style="
                      background-color: white;
                      width: 70px;
                      border-radius: 5px;
                      height: 25px;
                      font-size: 12px;
                      color: black;
                      z-index: 10;
                      transform: translate(205px, -10px);
                      margin-bottom: 2px;
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      color: #ea7999;
                    "
                  >
                    立即开通
                  </div></span
                >

                <span
                  style="
                    color: #746e71;
                    font-size: 13px;
                    transform: translate(-59px, -10px);
                  "
                  >了解更多权益</span
                >
              </div>
            </a>
            <div
              class="feature"
              style="
                display: flex;
                flex-direction: column;
                justify-content: center;
                align-items: center;
                margin-top: 5px;
              "
            >
              <a href="./account" target="_blank"
                >
                <div>
                  <img src="../img/个人中心.png" style="animation: none !important"/> <span>个人中心 </span
                  ><span style="transform: translateX(120px)"> > </span>
                </div>
              </a>
              <a
                href="/contribute/subpage2"
                target="_blank"
                ><div>
                  <img src="../img/投稿管理.png" style="animation: none !important"/> <span>投稿管理 </span
                  ><span style="transform: translateX(120px)"> > </span>
                </div></a
              >
              <div>
                <img src="../img/推荐服务.png" /> <span>推荐服务 </span
                ><span style="transform: translateX(120px)"> > </span>
              </div>
              <hr
                style="
                  color: #95999f;
                  width: 220px;
                  margin-top: 10px;
                  height: 1px;
                "
              />
              <div style="margin-top: 10px; cursor: pointer" @click="logout">
                <img src="../img/退出登录.png" /> <span>退出登录</span>
              </div>
            </div>
          </div>
        </div>
        <div
          v-if="!user.userName"
          class="avatar"
        >
          <span v-if="!user.userName"  @click="loginDialogVisible = true,flag=1">登录 </span>

          <div v-if="loginDialogVisible === false" class="login-info">
            <div class="login-can">登录后你可以:</div>
            <ul class="fuli">
              <li style="margin-left:11px;"><img src="../img/HD.png" alt="HD" />免费看高清视频</li>
              <li>
                <img
                  src="../img/同步记录.png"
                  alt="多端同步播放记录"
                />多端同步播放记录
              </li>
              <li style="margin-left:15px;">
                <img
                  src="../img/评论弹幕.png"
                  alt="发表弹幕/评论"
                />发表弹幕/评论
              </li>
              <li 
                style="
                  position: absolute;
                  display: flex;
                  transform: translate(177px, 34px);
                "
              >
                <img
                  src="../img/番剧影视.png"
                  alt="热门番剧影视看不停"
                />热门番剧影视看不停
              </li>
            </ul>
            <div style="display: flex; width: 100%; justify-content: center">
              <div class="login">
                <el-button
                  plain
                  @click="(loginDialogVisible = true), (flag = 1)"
                  class="el-bu"
                >
                  <span  @click="(loginDialogVisible = true), (flag = 1)" style="margin-bottom: 10px">立即登录</span>
                </el-button>
              </div>
            </div>
            <div class="sign-in">
              首次使用？&nbsp;&nbsp;<a
                href="#"
                style="animation: none !important;"
                ><div style="transform:translateY(3.5px);" @click="(loginDialogVisible = true), (flag = 2)">
                  点我注册
                </div></a
              >
            </div>
          </div>
       
        </div>
      </li>
      <li>
        <a href="#">
          <img v-show="!head2Flag" style="width: 17px;height: 17px;" src="../img/大会员.png" alt="大会员" />
          <img v-show="head2Flag" style="width: 17px;height: 17px;" src="../img/大会员黑.png" alt="大会员" />
           大会员
        </a>
        <div class="transparent-div12"></div>
        <div v-if="user.userName !== null" class="vip-introduce">
          <a
            style="animation: none !important"
            href="#"
            ><span
              style="
                color: black;
                font-size: 16px;
                font-weight: 900;
                margin-top: 10px;
                transform: translateX(-135px);
              "
              >大会员特权</span
            ></a
          >
          <div style="margin-top: 15px; display: flex">
            <span
              style="
                transform: translateX(15px);
                display: flex;
                justify-content: center;
                align-items: center;
              "
            >
              <img src="../img/热剧抢先看.png" />
              <a
                style="color: black; font-size: 14px"
                href="#"
                >热剧抢先看</a
              ></span
            ><span
              style="
                transform: translateX(40px);
                display: flex;
                justify-content: center;
                align-items: center;
              "
              ><img src="../img/会员免费看.png" /><a
                style="color: black; margin-bottom: 2px; font-size: 14px"
                href="#"
                >会员免费看</a
              ></span
            >
          </div>
          <div style="margin-top: 15px; display: flex">
            <span
              style="
                font-size: 14px;
                transform: translateX(17px);
                display: flex;
                justify-content: center;
                align-items: center;
              "
            >
              <img src="../img/4K超清画质.png" />
              <a
                style="color: black; font-size: 14px"
                href="#"
                >4K超清画质</a
              ></span
            ><span
              style="
                font-size: 14px;
                transform: translateX(37px);
                display: flex;
                justify-content: center;
                align-items: center;
              "
              ><img src="../img/会员专属装扮.png" /><a
                style="margin-bottom: 2px; color: black; font-size: 14px"
                href="#"
                >会员专属装扮
              </a></span
            >
          </div>
          <div
            style="
              margin-top: 15px;
              background-color: #fcedf1;
              width: 380px;
              height: 60px;
            "
          >
            <span
              style="
                display: flex;
                font-size: 14px;
                color: #ed7098;
                transform: translate(23px, 10px);
              "
              >首月15元</span
            ><br />
            <div
              style="
                display: flex;
                font-size: 10px;
                background: linear-gradient(to right, #fceecb, #f6c970);
                width: 42px;
                height: 14px;
                align-items: center;
                transform: translate(329px, -38px);
                position: absolute;
                z-index: 10;
                border-radius: 3px;
              "
            >
              限时特惠
            </div>
            <div
              style="
                cursor: pointer;
                position: relative;
                display: flex;
                background-color: #ea7999;
                width: 120px;
                border-radius: 5px;
                height: 40px;
                color: white;
                justify-content: center;
                align-items: center;
                font-size: 14px;
                transform: translate(250px, -30px);
              "
            >
              成为大会员
            </div>
            <span
              style="
                font-size: 12px;
                color: #ed7098;
                display: flex;
                transform: translate(23px, -48px);
              "
              >海量热播内容免费看</span
            >
          </div>
        </div>
      </li>
      <li>
        <a v-if="user.userName !== null" target="_blank" :href='`./message`'>
          <div
            v-if="store.userInformation.allMessageNumber>0"
            class="number-style"
          >
            {{ store.userInformation.allMessageNumber>99? '99+' : store.userInformation.allMessageNumber }}
          </div>
          <img v-show="!head2Flag" style="width:18px;" src="../img/消息.png" alt="消息" /> 
          <img v-show="head2Flag" style="width:18px;" src="../img/消息黑.png" alt="消息" />
          消息
        </a>
        <a v-if="!user.userName" @click="loginDialogVisible = true">
          <img v-show="!head2Flag" style="width:18px;" src="../img/消息.png" alt="消息" />
          <img v-show="head2Flag" style="width:18px;" src="../img/消息黑.png" alt="消息" />
           消息
        </a>
        <div class="login-info2" v-if="!user.userName">
          <span style="font-size: 14px; margin-top: 20px"
            >登录即可查看消息记录</span
          >
          <el-button
            plain
            @click="sa"
            class="el-bu"
          >
            立即登录
          </el-button>
        </div>
        <div class="transparent-div13"></div>
        <div class="message-info" v-if="user.userName !== null">
          <span @click="openMessage(1)" style="margin-top: 15px; cursor: pointer">
          <div class="text">我的消息</div>
          <div v-if="store.userInformation.messageNumber>0" class="message-number-style">{{ store.userInformation.messageNumber>99? '99+' : store.userInformation.messageNumber }}</div>
          </span>
          <span @click="openMessage(2)" style="cursor: pointer">
            <div class="text">回复我的</div>
            <div v-if="store.userInformation.replyCommentNumber>0" class="message-number-style">{{ store.userInformation.replyCommentNumber>99? '99+' : store.userInformation.replyCommentNumber }}</div>
          </span>
          <span @click="openMessage(3)" style="cursor: pointer">
            <div class="text">@我的</div>
            <div v-if="store.userInformation.atNumber>0" class="message-number-style">{{ store.userInformation.atNumber>99? '99+' : store.userInformation.atNumber }}</div>
          </span>
          <span @click="openMessage(4)" style="cursor: pointer">
            <div class="text">收到的赞</div>
            <div v-if="store.userInformation.likeAllNumber>0" class="message-number-style">{{ store.userInformation.likeAllNumber>99? '99+' : store.userInformation.likeAllNumber }}</div>
          </span>
          <span @click="openMessage(5)" style="cursor: pointer">
            <div class="text">消息设置</div>
          </span>
        </div>
      </li>
      <li>
        <a v-if="!user.userName" @click="loginDialogVisible = true">
          <img v-show="!head2Flag" style="width:18px;" src="../img/动态.png" alt="动态" />
          <img v-show="head2Flag" style="width:18px;" src="../img/动态黑.png" alt="动态" /> 动态
        </a>
        <a v-if="user.userName !== null" href="./dynamic" target="_blank">
          <div
            v-if="store.userInformation.dynamicNumber>0"
            class="number-style"
          >
            {{ store.userInformation.dynamicNumber>99? '99+' : store.userInformation.dynamicNumber }}
          </div>
          <img v-show="!head2Flag" style="width:18px;" src="../img/动态.png" alt="动态" />
          <img v-show="head2Flag" style="width:18px;" src="../img/动态黑.png" alt="动态" />
          动态
        </a>
        <div class="login-info2" v-if="!user.userName">
          <span style="font-size: 14px; margin-top: 20px"
            >登录即可查看关注动态</span
          >
          <el-button
            plain
            @click="sa"
            class="el-bu"
          >
            立即登录
          </el-button>
        </div>
        <div class="transparent-div14"></div>
        <div
          v-if="user.userName !== null"
          class="dynamic-info"
          @mouseleave="cleanAllDynamicAxios"
        >
          <div style="width: 32px">
            <a
              href="./dynamic" target="_blank"
              style="
                font-weight: 600;
                animation: none !important;
                color: black;
                cursor: pointer;
                font-size: 16px;
              "
              >动态</a
            >
          </div>
          <div
            v-if="notWatchDynamicFlag"
            class="newDynamic"
            style="margin-top: 10px"
          >
            <div
              class="dynamicContent-item"
              v-for="dynamic in notHistoryDynamic"
              :key="dynamic.id"
            >
              <div @click="locationHerfVideo(dynamic.videoId)">
                <el-tooltip
                  popper-class="custom-tooltip"
                  class="dynamicContent-item-tooltip"
                  effect="light"
                  :show-after="300"
                  :content="dynamic.userName"
                  placement="left"
                  :offset="24"
                  :show-arrow="false"
                  :hide-after="0"
                >
                    <img
                    @click.stop="openHome(1,dynamic.userId)"
                      :src="dynamic.avatarAddress"
                      style="
                        position: absolute;
                        border-radius: 50%;
                        width: 34px;
                        transform: translate(20px, 12px);
                      "
                    />
                </el-tooltip>
                <el-tooltip
                  popper-class="custom-tooltip"
                  class="dynamicContent-item-tooltip"
                  effect="light"
                  :show-after="300"
                  :content="dynamic.userName"
                  placement="right-start"
                  :offset="28"
                  :show-arrow="false"
                  :hide-after="0"
                >
                  <span class="dynamicUserName" @click.stop="openHome(1,dynamic.userId)">{{ dynamic.userName }}</span>
                </el-tooltip>
                <el-tooltip
                  popper-class="custom-tooltip"
                  class="dynamicContent-item-tooltip"
                  effect="light"
                  :content="dynamic.title"
                  :show-after="300"
                  placement="right-start"
                  :offset="28"
                  :show-arrow="false"
                  :hide-after="0"
                >
                  <span class="dynamicVideoTitle">{{ dynamic.title }}</span>
                </el-tooltip>
                <img
                  @mouseover="waitWatch = dynamic.videoId"
                  @mouseleave="waitWatch = 0"
                  :src="dynamic.coverAddress"
                  style="
                    width: 85px;
                    height: 45px;
                    transform: translate(279px, -23px);
                    object-fit: cover;
                    border-radius: 5px;
                  "
                />
                <span
                  style="
                    display: flex;
                    transform: translate(65px, -35px);
                    font-size: 12px;
                    color: #999da3;
                  "
                  >{{ dynamic.createTime }}</span
                >
                <div
                  v-if="dynamic.waitWatch === 0"
                  @mouseover="waitWatch = dynamic.videoId"
                  @click.stop="waitWatchAxios(dynamic)"
                  v-show="waitWatch === dynamic.videoId"
                  style="
                    background-color: black;
                    opacity: 0.8;
                    width: 28px;
                    height: 28px;
                    border-radius: 5px;
                    position: absolute;
                    transform: translate(307px, -79px);
                  "
                >
                  <img
                    src="../img/稍后再看.png"
                    style="
                      width: 20px;
                      height: 20px;
                      display: flex;
                      transform: translate(4.5px, 3.5px);
                    "
                  />
                </div>
                <div
                  v-if="dynamic.waitWatch === 1"
                  @mouseover="waitWatch = dynamic.videoId"
                  @click.stop="waitWatchAxios(dynamic)"
                  v-show="waitWatch === dynamic.videoId"
                  style="
                    background-color: black;
                    opacity: 0.8;
                    width: 28px;
                    height: 28px;
                    border-radius: 5px;
                    position: absolute;
                    transform: translate(307px, -79px);
                  "
                >
                  <img
                    src="../img/添加成功.png"
                    style="
                      width: 18px;
                      height: 16px;
                      display: flex;
                      transform: translate(5px, 4.5px);
                    "
                  />
                </div>
              </div>
            </div>
          </div>
          <div 
          v-if="notHistoryDynamic.length === 0 && dynamicss.length  === 0"
           class="dynamic-dataNull">动态什么都没有啊，快去关注一些喜欢的UP主吧！</div>

          <div style="display: flex; transform: translate(0px, 15px)">
            <span
              style="
                background-color: #dcdcdc;
                width: 140px;
                height: 1px;
                margin-top: 10px;
              "
            ></span
            ><span
              style="
                font-size: 12px;
                margin-left: 10px;
                margin-right: 0px;
                width: 60px;
              "
              >历史动态</span
            ><span
              style="
                background-color: #dcdcdc;
                width: 140px;
                height: 1px;
                margin-top: 10px;
              "
            ></span
            ><span style="font-size: 14px"></span>
          </div>
        
          <div class="dynamicContent">
            <div
              class="dynamicContent-item"
              v-for="dynamic in dynamicss"
              :key="dynamic.id"
            >
              <div
                v-if="dynamic.newDynamicNumber === 0"
                @click="locationHerfVideo(dynamic.videoId)"
              >
                <el-tooltip
                  popper-class="custom-tooltip"
                  class="dynamicContent-item-tooltip"
                  effect="light"
                  :show-after="300"
                  :content="dynamic.userName"
                  placement="left"
                  :offset="24"
                  :show-arrow="false"
                  :hide-after="0"
                >
                  <img
                    @click.stop="openHome(1,dynamic.userId)"
                    :src="dynamic.avatarAddress"
                    style="
                      position: absolute;
                      border-radius: 50%;
                      width: 34px;
                      transform: translate(20px, 12px);
                    "
                  />
                </el-tooltip>
                <el-tooltip
                  popper-class="custom-tooltip"
                  class="dynamicContent-item-tooltip"
                  effect="light"
                  :show-after="300"
                  :content="dynamic.userName"
                  placement="right-start"
                  :offset="12"
                  :show-arrow="false"
                  :hide-after="0"
                >
                  <span class="dynamicUserName" @click.stop="openHome(1,dynamic.userId)">{{ dynamic.userName }}</span>
                </el-tooltip>
                <el-tooltip
                  popper-class="custom-tooltip"
                  class="dynamicContent-item-tooltip"
                  effect="light"
                  :show-after="300"
                  :content="dynamic.title"
                  placement="right-start"
                  :offset="20"
                  :show-arrow="false"
                  :hide-after="0"
                >
                  <span class="dynamicVideoTitle">{{ dynamic.title }}</span>
                </el-tooltip>
                <img
                  @mouseover="waitWatch = dynamic.videoId"
                  @mouseleave="waitWatch = 0"
                  :src="dynamic.coverAddress"
                  style="
                    width: 85px;
                    height: 45px;
                    object-fit: cover;
                    transform: translate(279px, -23px);
                    border-radius: 5px;
                  "
                />
                <span
                  style="
                    display: flex;
                    transform: translate(65px, -35px);
                    font-size: 12px;
                    color: #999da3;
                  "
                  >{{ dynamic.createTime }}</span
                >
                <div
                  v-if="dynamic.waitWatch === 0"
                  @mouseover="waitWatch = dynamic.videoId"
                  @click.stop="waitWatchAxios(dynamic)"
                  v-show="waitWatch === dynamic.videoId"
                  style="
                    background-color: black;
                    opacity: 0.8;
                    width: 28px;
                    height: 28px;
                    border-radius: 5px;
                    position: absolute;
                    transform: translate(307px, -79px);
                  "
                >
                  <img
                    src="../img/稍后再看.png"
                    style="
                      width: 20px;
                      height: 20px;
                      display: flex;
                      transform: translate(4.5px, 3.5px);
                    "
                  />
                </div>
                <div
                  v-if="dynamic.waitWatch === 1"
                  @mouseover="waitWatch = dynamic.videoId"
                  @click.stop="waitWatchAxios(dynamic)"
                  v-show="waitWatch === dynamic.videoId"
                  style="
                    background-color: black;
                    opacity: 0.8;
                    width: 28px;
                    height: 28px;
                    border-radius: 5px;
                    position: absolute;
                    transform: translate(307px, -79px);
                  "
                >
                  <img
                    src="../img/添加成功.png"
                    style="
                      width: 18px;
                      height: 16px;
                      display: flex;
                      transform: translate(5px, 4.5px);
                    "
                  />
                </div>
              </div>
            </div>
            <div v-if="dynamics.length !== 0" class="checkAllDynamic">
              查看全部动态 >
            </div>
            <div style="height: 20px"></div>
          </div>
        </div>
      </li>
      <li @mouseover="setCollectStore">
        <a v-if="!user.userName" @click="loginDialogVisible = true">
          <img v-show="!head2Flag" style="width: 18px;height: 18px;" src="../img/收藏.png" alt="收藏" />
          <img v-show="head2Flag" style="width: 18px;height: 18px;" src="../img/收藏黑.png" alt="收藏" />
           收藏
        </a>
        <a v-if="user.userName !== null" :href="'./home?userId='+store.userInformation.id+'&homeMenu=5'" target="_blank">
          <img v-show="!head2Flag" style="width: 18px;height: 18px;" src="../img/收藏.png" alt="收藏" />
          <img v-show="head2Flag" style="width: 18px;height: 18px;" src="../img/收藏黑.png" alt="收藏" /> 收藏
        </a>
        <div class="login-info2" v-if="!user.userName">
          <span style="font-size: 14px; margin-top: 20px"
            >登录即可查看我的收藏</span
          >
          <el-button
            plain
            @click="sa"
            class="el-bu"
          >
            立即登录
          </el-button>
        </div>
        <div class="transparent-div15"></div>
        <div v-if="user.userName !== null" class="collect-info">
          <div class="collectAside">
            <div
              class="collectAdideContent"
              v-for="collectDto in collectDtoList"
              :key="collectDto.id"
            >
              <div
                class="collectAsideTitleItem"
                @click="selectOneCollect(collectDto.id)"
              >
                <div
                  v-if="changerCollect !== collectDto.id"
                  class="collectAsideTitle1"
                >
                  <el-tooltip
                    popper-class="custom-tooltip"
                    class="dynamicContent-item-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="collectDto.collectName"
                    placement="left"
                    :offset="24"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                    <span
                      ><span class="collectAsideName">{{
                        collectDto.collectName
                      }}</span
                      ><span
                        style="
                          display: flex;
                          transform: translate(93px, -45px);
                          font-size: 12px;
                          color: #9499A0;
                        "
                        >{{ collectDto.collectNumber }}</span
                      ></span
                    >
                  </el-tooltip>
                </div>
                <div
                  v-if="changerCollect === collectDto.id"
                  class="collectAsideTitle2"
                >
                  <el-tooltip
                    popper-class="custom-tooltip"
                    class="dynamicContent-item-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="collectDto.collectName"
                    placement="left"
                    :offset="24"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                    <span
                      ><span class="collectAsideName">{{
                        collectDto.collectName
                      }}</span
                      ><span
                        style="
                          display: flex;
                          transform: translate(93px, -45px);
                          font-size: 12px;
                          color: white;
                        "
                        >{{ collectDto.collectNumber }}</span
                      ></span
                    >
                  </el-tooltip>
                </div>
              </div>
            </div>
          </div>

          <div v-if="collectDtoList[changerCollect]" class="collectContent">
            <div
              class="collectContent-item"
              v-for="collect in collectDtoList[changerCollect].collectsList"
              :key="collect.id"
            >
              <div
                v-if="collectDtoList[changerCollect].collectNumber !== 0"
                @click="locationHerfVideo(collect.videos.id)"
              >
                <img
                  :src="collect.videos.coverAddress"
                  style="
                    width: 138px;
                    height: 70px;
                    object-fit: cover;
                    border-radius: 5px;
                    cursor: pointer;
                  "
                />
                <span class="collectVideoTime">{{ collect.videos.videoTime }}</span>
                <el-tooltip
                  popper-class="custom-tooltip"
                  class="dynamicContent-item-tooltip"
                  effect="light"
                  :show-after="300"
                  :content="collect.videos.title"
                  placement="left"
                  :offset="24"
                  :show-arrow="false"
                  :hide-after="0"
                >
                  <span class="collectVideoTitle">{{
                    collect.videos.title
                  }}</span>
                </el-tooltip>
                <img
                  src="../img/up.png"
                  style="
                    width: 14px;
                    height: 12px;
                    transform: translate(148px, -73px);
                  "
                />
                <el-tooltip
                  popper-class="custom-tooltip"
                  class="dynamicContent-item-tooltip"
                  effect="light"
                  :show-after="300"
                  :content="collect.videos.userName"
                  placement="left"
                  :offset="28"
                  :show-arrow="false"
                  :hide-after="0"
                >
                  <span class="collectVideoUpName">{{
                    collect.videos.userName
                  }}</span>
                </el-tooltip>
              </div>
            </div>
            <div
              v-if="collectDtoList[changerCollect]?.collectNumber===0"
              class="collectIsNull"
            >
              该收藏夹还没有视频哦~
            </div>
          </div>
          <div v-if="collectDtoList[changerCollect]?.collectNumber !== 0" class="collectFrature">
            <span class="checkAllCollect" @click="openHome(5,store.userInformation.id,collectDtoList[changerCollect])">查看全部</span>
            <span class="playAllCollect"
              ><img src="../img/播放.png" /> 播放全部</span
            >
          </div>
        </div>
      </li>
      <li>
        <a v-if="!user.userName" @click="loginDialogVisible = true">
          <img v-show="!head2Flag" style="width:18px;" src="../img/历史.png" alt="历史" /> 
          <img v-show="head2Flag" style="width:18px;" src="../img/历史黑.png" alt="历史" /> 
          历史
        </a>
        <a v-if="user.userName !== null" href="/history" target="_blank" @mouseover="selectHistoryAxios" @mouseleave="historyAxiosTimeF">
          <img v-show="!head2Flag" style="width:18px;" src="../img/历史.png" alt="历史" /> 
          <img v-show="head2Flag" style="width:18px;" src="../img/历史黑.png" alt="历史" /> 历史
        </a>
        <div class="login-info2" v-if="!user.userName">
          <span style="font-size: 14px; margin-top: 20px"
            >登录即可查看历史记录</span
          >
          <el-button
            plain
            @click="sa"
            class="el-bu"
          >
            立即登录
          </el-button>
        </div>
        <div class="transparent-div16"></div>
        <div v-if="user.userName !== null" class="history-info">
          <div class="historyAside">
            <span v-show="historyAsideFlag !== 0" @click="selectHistoryAxios"
              >视频</span
            >
            <span v-show="historyAsideFlag === 0" style="color: #00aeec"
              >视频</span
            >
            <span
              v-show="historyAsideFlag !== 1"
              @click="historyAsideFlag = 1"
              style="margin-right: 20px; margin-left: 20px"
              >直播</span
            >
            <span
              v-show="historyAsideFlag === 1"
              style="margin-right: 20px; margin-left: 20px; color: #00aeec"
              >直播</span
            >
            <span v-show="historyAsideFlag !== 2" @click="historyAsideFlag = 2"
              >专栏</span
            >
            <span v-show="historyAsideFlag === 2" style="color: #00aeec"
              >专栏</span
            >
            <span
              v-show="historyAsideFlag === 0"
              style="
                position: absolute;
                width: 126.6px;
                height: 3px;
                background-color: #00aeec;
                transform: translate(-126px, 24px);
              "
            ></span>
            <span
              v-show="historyAsideFlag === 1"
              style="
                position: absolute;
                width: 126.6px;
                height: 3px;
                background-color: #00aeec;
                transform: translate(0px, 24px);
              "
            ></span>
            <span
              v-show="historyAsideFlag === 2"
              style="
                position: absolute;
                width: 126.6px;
                height: 3px;
                background-color: #00aeec;
                transform: translate(126px, 24px);
              "
            ></span>
          </div>
          <div v-show="historyAsideFlag === 0" class="historyContent">
            <div
              v-if="historyToday.length !== 0"
              style="color: black; font-size: 14px; margin-left: 20px"
            >
              今天
            </div>
            <div v-if="historyToday.length !== 0">
              <div
                class="historyContent-item-today"
                v-for="history in historyToday"
                :key="history.historyId"
              >
                <div @click="locationHerfVideo(history.videoId)">
                  <img
                    :src="history.videoCoverAddress"
                    style="
                      width: 138px;
                      height: 70px;
                      object-fit: cover;
                      border-radius: 5px;
                      cursor: pointer;
                    "
                  />
                  <progress
                    :value="
                      histotyVideoProgress(
                        history.videoTime,
                        history.watchVideoTime,
                      )
                    "
                    max="100"
                    class="historyVideoProgress"
                  ></progress>
                  <span class="historyVideoTime"
                    ><span>{{ history.watchVideoTime }}</span
                    ><span
                      style="
                        font-size: 10px;
                        transform: translate(-0.5px, -0.5px);
                      "
                      >/</span
                    >
                    <span>{{ history.videoTime }}</span></span
                  >
                  <el-tooltip
                    popper-class="custom-tooltip"
                    class="dynamicContent-item-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="history.videoTitle"
                    placement="left"
                    :offset="24"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                    <span class="historyVideoTitle">{{
                      history.videoTitle
                    }}</span>
                  </el-tooltip>
                  <span class="history-system"
                    ><img src="../img/电脑端.png" />
                    <span style="margin-right: 5px">{{
                      history.watchVideoDate
                    }}</span
                    ><span>{{ history.watchVideoDateTime }}</span>
                  </span>
                  <img
                    src="../img/up.png"
                    style="
                      width: 14px;
                      height: 12px;
                      transform: translate(148px, -92px);
                    "
                  />
                  <el-tooltip
                    popper-class="custom-tooltip"
                    class="dynamicContent-item-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="history.videoUserName"
                    placement="left"
                    :offset="28"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                    <span class="historyVideoUpName">{{
                      history.videoUserName
                    }}</span>
                  </el-tooltip>
                </div>
              </div>
            </div>
            <div
              v-if="historyYesterday.length !== 0"
              style="
                color: black;
                font-size: 14px;
                margin-left: 20px;
                height: 30px;
                line-height: 30px;
              "
            >
              昨天
            </div>
            <div v-if="historyYesterday.length !== 0">
              <div
                class="historyContent-item-yesterday"
                v-for="history in historyYesterday"
                :key="history.historyId"
              >
                <div @click="locationHerfVideo(history.videoId)">
                  <img
                  :src="history.videoCoverAddress"
                    style="
                      width: 138px;
                      height: 70px;
                      object-fit: cover;
                      border-radius: 5px;
                      cursor: pointer;
                    "
                  />
                  <progress
                    :value="
                      histotyVideoProgress(
                        history.videoTime,
                        history.watchVideoTime,
                      )
                    "
                    max="100"
                    class="historyVideoProgress"
                  ></progress>
                  <span class="historyVideoTime"
                    ><span>{{ history.watchVideoTime }}</span
                    ><span
                      style="
                        font-size: 10px;
                        transform: translate(-1px, -0.5px);
                      "
                      >/</span
                    >
                    <span>{{ history.videoTime }}</span></span
                  >
                  <el-tooltip
                    popper-class="custom-tooltip"
                    class="dynamicContent-item-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="history.videoTitle"
                    placement="left"
                    :offset="24"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                    <span class="historyVideoTitle">{{
                      history.videoTitle
                    }}</span>
                  </el-tooltip>
                  <span class="history-system"
                    ><img src="../img/电脑端.png" />
                    <span style="margin-right: 5px">{{
                      history.watchVideoDate
                    }}</span
                    ><span>{{ history.watchVideoDateTime }}</span>
                  </span>
                  <img
                    src="../img/up.png"
                    style="
                      width: 14px;
                      height: 12px;
                      transform: translate(148px, -92px);
                    "
                  />
                  <el-tooltip
                    popper-class="custom-tooltip"
                    class="dynamicContent-item-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="history.videoUserName"
                    placement="left"
                    :offset="28"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                    <span class="historyVideoUpName">{{
                      history.videoUserName
                    }}</span>
                  </el-tooltip>
                </div>
              </div>
            </div>
            <div
              v-if="historyEarlier.length !== 0"
              style="
                color: black;
                font-size: 14px;
                margin-left: 20px;
                height: 30px;
                line-height: 30px;
              "
            >
              更早
            </div>
            <div v-if="historyEarlier.length !== 0">
              <div
                class="historyContent-item-yesterday"
                v-for="history in historyEarlier"
                :key="history.historyId"
              >
                <div @click="locationHerfVideo(history.videoId)">
                  <img
                  :src="history.videoCoverAddress"
                    style="
                      width: 138px;
                      height: 70px;
                      object-fit: cover;
                      border-radius: 5px;
                      cursor: pointer;
                    "
                  />
                  <progress
                    :value="
                      histotyVideoProgress(
                        history.videoTime,
                        history.watchVideoTime,
                      )
                    "
                    max="100"
                    class="historyVideoProgress"
                  ></progress>
                  <span class="historyVideoTime"
                    ><span>{{ history.watchVideoTime }}</span
                    ><span
                      style="
                        font-size: 10px;
                        transform: translate(-1px, -0.5px);
                      "
                      >/</span
                    >
                    <span>{{ history.videoTime }}</span></span
                  >
                  <el-tooltip
                    popper-class="custom-tooltip"
                    class="dynamicContent-item-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="history.videoTitle"
                    placement="left"
                    :offset="24"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                    <span class="historyVideoTitle">{{
                      history.videoTitle
                    }}</span>
                  </el-tooltip>
                  <span class="history-system"
                    ><img src="../img/电脑端.png" />
                    <span style="margin-right: 5px">{{
                      history.watchVideoDate
                    }}</span
                    ><span>{{ history.watchVideoDateTime }}</span>
                  </span>
                  <img
                    src="../img/up.png"
                    style="
                      width: 14px;
                      height: 12px;
                      transform: translate(148px, -92px);
                    "
                  />
                  <el-tooltip
                    popper-class="custom-tooltip"
                    class="dynamicContent-item-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="history.videoUserName"
                    placement="left"
                    :offset="28"
                    :show-arrow="false"
                    :hide-after="0"
                  >
                    <span class="historyVideoUpName">{{
                      history.videoUserName
                    }}</span>
                  </el-tooltip>
                </div>
              </div>
            </div>
            <div style="display: flex; justify-content: center">
              <div class="checkAllHistory" @click="openHistory">查看全部</div>
            </div>
          </div>
          <div
            v-show="historyAsideFlag === 0"
            style="
              height: 15px;
              display: flex;
              background-color: #ffffff;
              transform: translateY(-12px);
              width: 100%;
            "
          ></div>
          <div
            v-show="historyAsideFlag === 1 || historyAsideFlag === 2"
            style="
              position: absolute;
              transform: translate(140px, 160px);
              font-size: 14px;
            "
          >
            这里没有开发哦~
          </div>
        </div>
      </li>
      <li>
        <a v-if="!user.userName" @click="loginDialogVisible = true">
          <img v-show="!head2Flag" style="width:15px;height:18px;" src="../img/创作中心.png" alt="创作中心" />
          <img v-show="head2Flag" style="width:15px;height:18px;" src="../img/创作中心黑.png" alt="创作中心" />
          创作中心
        </a>
        <a v-if="user.userName !== null" href="./contribute" target="_blank">
          <img v-show="!head2Flag" style="width:15px;height:18px;" src="../img/创作中心.png" alt="创作中心" />
          <img v-show="head2Flag" style="width:15px;height:18px;" src="../img/创作中心黑.png" alt="创作中心" /> 创作中心
        </a>
      </li>
      <li>
        <div v-if="user.userName !== null" class="upload">
          <a
            href="./contribute"
            style="
              animation: none !important;
              width: 100%;
              height: 100%;
              display: flex;
              justify-content: center;
            "
            target="_blank"
          >
            <img src="../img/投稿.png" alt="投稿" /> <span>投稿</span>
          </a>
        </div>
        <div
          v-if="!user.userName"
          class="upload"
          @click="loginDialogVisible = true"
        >
          <a style="animation: none !important" target="_blank">
            <img src="../img/投稿.png" alt="投稿" /> <span>投稿</span>
          </a>
        </div>
      </li>
    </ul>
      <el-dialog
                style="
                  height: 480px !important;
                  overflow-x:hidden;
                  overflow-y:hidden !important;
                "
                @close="handleClose"
                append-to-body
                v-model="loginDialogVisible"
                :close-on-click-modal="false"
                width="700"
                custom-class="my-dialog"
              >
                <div class="dialogin-info">
                  <div
                    class="dialogin-title"
                    style="transform: translate(-50%, 20px);left:50%;position: absolute;"
                  >
                    <button
                      :class="{ active: flag === 1 }"
                      style="
                        margin-right: 24px;
                        border: none;
                        background-color: white;
                        cursor: pointer;
                      "
                      @click="passwordLogin"
                    >
                      密码登录</button
                    ><span
                      style="
                        margin-right: 24px;
                        margin-top: 5px;
                        font-weight: 100;
                        color: darkgrey;
                      "
                      >|</span
                    ><button
                      :class="{ active: flag === 2 }"
                      style="border: none; background-color: white; cursor: pointer"
                      @click="siginLogin"
                    >
                      账号注册</button
                    ><span
                      style="
                        margin-left: 24px;
                        font-weight: 100;
                        margin-top: 5px;
                        color: darkgrey;
                      "
                      >|</span
                    ><button
                      :class="{ active: flag === 3 }"
                      style="
                        border: none;
                        background-color: white;
                        margin-left: 24px;
                        cursor: pointer;
                      "
                      @click="findPassword"
                    >
                      找回密码
                    </button>
                  </div>
                  <!--密码登录 -->
                  <div
                    v-if="flag === 1"
                    style="transform: translate(-50%, 80px);position: absolute;left:50%;"
                  >
                    <el-form
                      :model="loginForm"
                      label-width="auto"
                      style="max-width: 300px; transform:translate(11px); "
                     
                    >
                      <el-form-item label="账号" style="width: 300px">
                        <el-input
                          v-model="loginForm.userName"
                          @input="removeSpaces"
                          maxlength="10"
                          placeholder="请输入账号"
                          style="border: none"
                          @keydown.enter="loginAxios"
                        />
                      </el-form-item>
                      <el-form-item label="密码">
                        <div style="position: absolute; width: 260px">
                          <el-input
                            v-model="loginForm.password"
                            style="background-color: white"
                            type="password"
                            maxlength="16"
                            placeholder="请输入密码"
                            show-password
                            @input="removeSpaces"
                            @keydown.enter="loginAxios"
                          >
                          </el-input>
                          <span
                            @click="handleForgotPassword"
                            style="
                              position: absolute;
                              right: 25px;
                              top: 50%;
                              transform: translateY(-50%);
                              font-size: 14px;
                              color: #409eff; /* Element Plus 默认蓝色 */
                              cursor: pointer;
                              border: none; /* 确保没有边框 */
                              padding: 0; /* 确保没有内边距 */
                              margin: 0; /* 确保没有外边距 */
                              /* 根据需要添加其他样式 */
                            "
                          >
                            忘记密码？
                          </span>
                        </div>
                      </el-form-item>
                      <div class="submit-div">
                        <el-button
                          @click="handlerSign"
                          class="zhu-ce"
                          style="
                            background-color: rgb(255, 255, 255);
                            border: 1px solid #dcdcdc;
                            margin-right: 10px;
                          "
                          ><span style="color: black">注册</span></el-button
                        >
                        <el-button
                          :disabled="loginForm.userName.length===0 || loginForm.password.length===0"
                          @click="loginAxios"
                          class="deng-lu"
                          style="
                            background-color: #00aeec;
                            border: none;
                            color:white;
                            margin-left: 10px;
                          "
                          >登录</el-button
                        >
                      </div>
                      <span
                        style="
                          color: rgb(159, 156, 156);
                          position: absolute;
                          margin-top: 150px;
                          left:50%;
                          width:109%;
                          transform:translate(-50%)
                        "
                        >登录或完成注册即代表你同意&nbsp;
                        <a
                          href="#"
                          style="color: #00aeec; animation: none !important"
                          >用户协议</a
                        >&nbsp;和&nbsp;<a
                          href="#"
                          style="color: #00aeec; animation: none !important"
                          >隐私政策</a
                        >
                      </span>
                      <img
                        :src="loginForm.password.length > 0 ? left2 : left1"
                        style="
                          width: 120px;
                          height: 100px;
                          transform: translate(-212px, 132px);
                        "
                      />
                      <img
                        :src="loginForm.password.length > 0 ? right2 : right1"
                        style="
                          width: 135px;
                          height: 100px;
                          transform: translate(235px, 132px);
                        "
                      />
                    </el-form>
                  </div>

                  <!-- 账号注册 -->
                  <div        
                    v-if="flag === 2"
                    style="transform: translate(-50%, 80px);position: absolute;left:50%;"
                  >
                    <el-form
                      :model="signinForm"
                      label-width="auto"
                      style="max-width: 334px"
                    >
                      <el-form-item label="账号" style="width: 328px">
                        <el-input
                          v-model="signinForm.userName"
                          placeholder="请输入账号"
                          @input="removeSpaces1"
                          maxlength="10"
                          style="border: none"
                          @keydown.enter="sigininAxios"
                        />
                      </el-form-item>
                      <el-form-item label="密码" style="width: 328px">
                        <el-input
                          v-model="signinForm.password"
                          type="password"
                          placeholder="请输入密码"
                          maxlength="16"
                          autocomplete="off"
                          @input="removeSpaces1"
                          @keydown.enter="sigininAxios"
                          show-password
                        />
                      </el-form-item>
                      <el-form-item label="确认密码" style="width: 328px">
                        <el-input
                          v-model="signinForm.againPassword"
                          type="password"
                          placeholder="请再次输入密码"
                          maxlength="16"
                          autocomplete="off"
                          @input="removeSpaces1"
                          @keydown.enter="sigininAxios"
                        />
                      </el-form-item>
                      <el-form-item label="手机号" style="width: 328px">
                        <el-input
                          v-model="signinForm.phone"
                          type="text"
                          :formatter="formatPhone"
                          placeholder="请输入手机号"
                          maxlength="11"
                          @input="removeSpaces1"
                          @keydown.enter="sigininAxios"
                          style="border: none; width: 328px"
                        />
                      </el-form-item>
                      <el-form-item label="密保" style="width: 328px">
                        <el-input
                          @input="removeSpaces1"
                          v-model="signinForm.question"
                          maxlength="10"
                          placeholder="最喜欢的动漫人物"
                          @keydown.enter="sigininAxios"
                          style="border: none"
                        />
                      </el-form-item>

                      <div class="submit-div">
                        <el-button
                        :disabled="signinForm.userName.length===0
                          || signinForm.password.length ===0
                          || signinForm.againPassword.length ===0
                          || signinForm.question.length ===0
                          || signinForm.phone.length===0
                          "
                          class="zhu-ce"
                          @click="sigininAxios"
                          style="
                            background-color: #00aeec;
                            border: 1px solid #dcdcdc;
                            margin-right: 10px;
                            transform: translateX(1%);
                          "
                        >
                          <span style="color: white">注册</span>
                        </el-button>
                        <el-button
                          class="deng-lu"
                          style="
                            background-color: white;
                            border: none;
                            border: 1px solid #dcdcdc;
                            margin-left: 10px;
                            transform: translateX(2%);
                          "
                          @click="passwordLogin"
                        >
                          <span style="color: black">登录</span>
                        </el-button>
                      </div>
                      <span
                      style="
                          color: rgb(159, 156, 156);
                          position: absolute;
                          margin-top: 24px;
                          left:58.8%;
                          width:109%;
                          transform:translate(-50%)
                        "
                      >
                        登录或完成注册即代表你同意&nbsp;
                        <a
                          href="#"
                          style="color: #00aeec; animation: none !important"
                          >用户协议</a
                        >&nbsp;和
                        <a
                          href="#"
                          style="color: #00aeec; animation: none !important"
                          >隐私政策</a
                        >
                      </span>
                      <img
                        :src="
                          signinForm.password.length > 0 ||
                          signinForm.againPassword.length > 0
                            ? left2
                            : left1
                        "
                        style="
                          width: 120px;
                          height: 100px;
                          transform: translate(-184px, 6px);
                        "
                      />
                      <img
                        :src="
                          signinForm.password.length > 0 ||
                          signinForm.againPassword.length > 0
                            ? right2
                            : right1
                        "
                        style="
                          width: 135px;
                          height: 100px;
                          transform: translate(263px, 6px);
                        "
                      />
                    </el-form>
                  </div>

                  <!-- 忘记密码 -->
                  <div
                    v-if="flag === 3"
                    style="transform: translate(-50%, 80px);position: absolute;left:50%;"
                  >
                    <el-form
                      :model="forgetPasswordForm"
                      label-width="auto"
                      style="max-width: 342px;transform:translate(-10px);"
                    >
                      <el-form-item label="账号" style="width: 342px">
                        <el-input
                          @input="removeSpaces2"
                          maxlength="10"
                          v-model="forgetPasswordForm.userName"
                          placeholder="请输入账号"
                          @keydown.enter="putPasswordAxios"
                          style="border: none"
                        />
                      </el-form-item>

                      <el-form-item label="手机号" style="width: 342px">
                        <el-input
                          v-model="forgetPasswordForm.phone"
                          type="text"
                          :formatter="formatPhone"
                          placeholder="请输入手机号"
                          maxlength="11"
                          @input="removeSpaces2"
                          @keydown.enter="putPasswordAxios"
                          style="border: none; width: 342px"
                        />
                      </el-form-item>
                      <el-form-item label="密保" style="width: 342px">
                        <el-input
                          @input="removeSpaces2"
                          maxlength="10"
                          v-model="forgetPasswordForm.question"
                          placeholder="最喜欢的动漫人物"
                          @keydown.enter="putPasswordAxios"
                          style="border: none"
                        />
                      </el-form-item>
                      <el-form-item label="密码" style="width: 342px">
                        <el-input
                          v-model="forgetPasswordForm.password"
                          type="password"
                          placeholder="请输入密码"
                          autocomplete="off"
                          maxlength="16"
                          @input="removeSpaces2"
                          @keydown.enter="putPasswordAxios"
                          show-password
                        />
                      </el-form-item>
                      <el-form-item label="确认新密码" style="width: 342px">
                        <el-input
                          v-model="forgetPasswordForm.againpassword"
                          type="password"
                          placeholder="请再次输入密码"
                          autocomplete="off"
                          maxlength="16"
                          @input="removeSpaces2"
                          @keydown.enter="putPasswordAxios"
                        />
                      </el-form-item>
                      <div class="submit-div">
                        <el-button
                          :disabled="forgetPasswordForm.userName.length===0
                          || forgetPasswordForm.password.length ===0
                          || forgetPasswordForm.againpassword.length ===0
                          || forgetPasswordForm.question.length ===0
                          || forgetPasswordForm.phone.length===0
                          "
                          class="deng-lu"
                          style="
                            background-color: #00aeec;
                            border: none;
                            color:white;
                            margin-left: 10px;
                            transform: translateX(10%);
                          "
                          @click="putPasswordAxios"
                        >
                          确认
                        </el-button>
                      </div>
                      <span
                        style="
                           color: rgb(159, 156, 156);
                          position: absolute;
                          margin-top: 24px;
                          left:62.8%;
                          width:109%;
                          transform:translate(-50%)
                        "
                      >
                        登录或完成注册即代表你同意&nbsp;
                        <a
                          href="#"
                          style="color: #00aeec; animation: none !important"
                          >用户协议</a
                        >&nbsp;和
                        <a
                          href="#"
                          style="color: #00aeec; animation: none !important"
                          >隐私政策</a
                        >
                      </span>
                      <img
                        :src="
                          forgetPasswordForm.password.length > 0 ||
                          forgetPasswordForm.againpassword.length > 0
                            ? left2
                            : left1
                        "
                        style="
                          width: 120px;
                          height: 100px;
                          transform: translate(-170px, 6px);
                        "
                      />
                      <img
                        :src="
                          forgetPasswordForm.password.length > 0 ||
                          forgetPasswordForm.againpassword.length > 0
                            ? right2
                            : right1
                        "
                        style="
                          width: 135px;
                          height: 100px;
                          transform: translate(277px, 6px);
                        "
                      />
                    </el-form>
                  </div>
                </div>
      </el-dialog>
    </div>
</template>

<script>
import { ref, reactive, onMounted,watch } from "vue";
import "element-plus/theme-chalk/el-message.css";
import apiClient from "../services/apiClient";
import { ElMessage } from "element-plus";
import search from "./search.vue";
import "element-plus/dist/index.css";
import left1 from "../img/loginLeft.png";
import left2 from "../img/loginPasswordLeft.png";
import right1 from "../img/loginRight.png";
import right2 from "../img/loginPasswordRight.png";
import { ElLoading } from "element-plus";
import {useGlobalStore} from "../store/store";
import {ChecklLogin} from '../api/user/index'
export default {
  name: "mainHead",
  props:{
    head2Flag: {
      type: Boolean,
      default: false,
    },
    searchFlag: {
      type: Boolean,
      default: false,
    },
    loginDialogVisibleFlag:{
      type: Number,
      default: 0,
    },
   
  },
  components: {
    search,
  },
  setup(props) {
    const propsS=reactive(props);
    const head2Flag= ref(props.head2Flag);
    const searchFlag= ref(props.searchFlag);
    const store = useGlobalStore();
    const historyToday = reactive([]);
    const historyYesterday = reactive([]);
    const historyEarlier = reactive([]);
    const historyList = reactive([]);
    const historyAsideFlag = ref(0);
    const collectDtoList = reactive([
      {
        id:0,
        collectsList:[],
        collectNumber:0,
        collectName:"默认收藏夹"
      },
      {
        id:1,
        collectsList:[],
        collectNumber:0,
        collectName:"稍后再看"
      }
    ]);
    const changerCollect = ref(0);
    const notWatchDynamicFlag = ref(false);
    const loginDialogVisible = ref(false); //关闭登录对话框
    const waitWatch = ref(false);
    const fullscreenLoading = ref(true);
    const loginForm = reactive({
      //登录表单
      userName: "",
      password: "",
    });
    const notHistoryDynamic = reactive([]);
    const signinForm = reactive({
      //注册表单
      userName: "",
      phone: "",
      password: "",
      againPassword: "",
      question: "",
    });

    const forgetPasswordForm = reactive({
      userName: "",
      phone: "",
      question: "",
      password: "",
      againpassword: "",
    });

    const user = reactive({
      avatarAddress: "",
      coinNumber: 0,
      collectNumber: 0,
      createTime: "",
      dynamicNumber: 0,
      grade: "",
      id: 0,
      messageNumber: 0,
      phone: "",
      gender: "",
      userName: null,
      videoNumber: 0,
      token: "",
      exp: 0,
      followNumber: 0,
      ownDynamicNumber: 0,
      fansNumber: 0,
      introduce: "",
    });
    const dynamics = reactive([]);
    const dynamicss = reactive([]);
    const NewFansNumber = ref(0);
    let loginDelayFlag= false;
    let signinDelayFlag= false;
    let forgetPasswordDelayFlag= null;
    const flag = ref(1);

    function passwordLogin() {
      flag.value = 1;
    }

    //修改密码请求
    async function putPasswordAxios() {

      try {
    
        if(forgetPasswordDelayFlag)
        return;

        forgetPasswordDelayFlag= true;

        setTimeout(() => {
        forgetPasswordDelayFlag= false;
        }, 1800);

        if (forgetPasswordForm.userName === "") {
          ElMessage({
            message: "请输入账号",
            type: "info",
            plain: true,
            duration: 1700,
          });

          return (loginDialogVisible.value = true);
        }
        if (forgetPasswordForm.phone === "") {
          ElMessage({
            message: "请输入手机号",
            type: "info",
            plain: true,
            duration: 1700,
          });

          return (loginDialogVisible.value = true);
        }
        if (
          forgetPasswordForm.phone.length !== 11 ||
          forgetPasswordForm.phone.indexOf("1") !== 0
        ) {
          ElMessage({
            message: "手机号格式错误",
            type: "info",
            plain: true,
            duration: 1700,
          });
          return (loginDialogVisible.value = true);
        }
        if (forgetPasswordForm.question === "") {
          ElMessage({
            message: "请输入密保",
            type: "info",
            plain: true,
            duration: 1700,
          });

          return (loginDialogVisible.value = true);
        }
        if (forgetPasswordForm.password === "") {
          ElMessage({
            message: "请输入密码",
            type: "info",
            plain: true,
          });

          return (loginDialogVisible.value = true);
        }
        if (forgetPasswordForm.password.length < 4) {
          ElMessage({
            message: "密码过短",
            type: "info",
            plain: true,
            duration: 1700,
          });

          return (loginDialogVisible.value = true);
        }
        if (forgetPasswordForm.againpassword === "") {
          ElMessage({
            message: "请输入密码",
            type: "info",
            plain: true,
            duration: 1700,
          });

          return (loginDialogVisible.value = true);
        }

        if (forgetPasswordForm.password !== forgetPasswordForm.againpassword) {
          ElMessage({
            message: "两次密码不一致",
            type: "info",
            plain: true,
            duration: 1700,
          });

          return (loginDialogVisible.value = true);
        }

        let limiterLoginDto={
           user: forgetPasswordForm,
           userIp: store.userIp,
        };
        const response = await apiClient.put(
          "/user/forget",
          limiterLoginDto,
        );

        if (response.data.code === 0) {
          ElMessage({
            message: response.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
          });
        } else {
          flag.value = 1;
          handleClose();
          ElMessage({
            message: response.data.data,
            type: "info",
            plain: true,
            duration: 1700,
          });
        }
      } catch (error) {
        ElMessage({
          message: response.data.data,
          type: "info",
          plain: true,
          duration: 1700,
        });
      }

    }

    const siginLogin = () => {
      flag.value = 2;
    };

    const findPassword = () => {
      flag.value = 3;
    };
    function formatPhone(value) {
      // Remove any non-digit characters
      const cleanValue = value.replace(/\D/g, "");
      // Format the cleaned value (you can customize this as needed)
      return cleanValue;
    }

    function handleClose() {
      // Reset form data when the dialog closes
      (signinForm.userName = ""),
        (signinForm.phone = ""),
        (signinForm.password = ""),
        (signinForm.againPassword = ""),
        (signinForm.question = ""),
        (loginForm.userName = ""),
        (loginForm.password = ""),
        (forgetPasswordForm.userName = ""),
        (forgetPasswordForm.phone = ""),
        (forgetPasswordForm.question = ""),
        (forgetPasswordForm.password = ""),
        (forgetPasswordForm.againpassword = "");
    }

    function handleForgotPassword() {
      flag.value = 3;
    }
    function handlerSign() {
      flag.value = 2;
    }
    function sa() {
      flag.value = 1;
      loginDialogVisible.value = true;
    }
    //登录请求
    async function loginAxios() { 
      try {
        if(loginDelayFlag)
        return;

        loginDelayFlag= true;

        setTimeout(() => {
        loginDelayFlag= false;
        }, 1800);
        
        if (loginForm.userName === "") {
          ElMessage({
            message: "请输入账号",
            type: "info",
            plain: true,
            duration: 1700,
          });

          return (loginDialogVisible.value = true);
        }
        if (loginForm.password === "") {
          ElMessage({
            message: "请输入密码",
            type: "info",
            plain: true,
            duration: 1700,
          });

          return (loginDialogVisible.value = true);
        }
       
        let limiterLoginDto={
           user: loginForm,
           userIp: store.userIp,
        };
        const response = await apiClient.post("/user/login", limiterLoginDto);

        if (response.data.code === 0) {
          ElMessage({
            message: response.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
          });
        } else {
          openFullScreen2();
          Object.assign(user, response.data.data);
          store.setUserInformation(response.data.data);
          await getUserIp();
          timewait();
          loginDialogVisible.value = false;
        }
      } catch (error) {
        ElMessage({
          message: "未知错误",
          type: "info",
          plain: true,
          duration: 1700,
        });

        // 处理错误
      }
    }

    //登出
    async function logout() {

      let limiterLoginDto={
           user: user,
           userIp: store.userIp,
        };
      try {
        const response = await apiClient.post("/user/signOut",limiterLoginDto,{
          headers: {
            "Content-Type": "application/json",
            "Authorization": store.token,
          },          
        });
        if(response.data.code === 1){
          location.reload(); 
        }
        else{
          ElMessage({
            message: response.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
          });
          if(response.data.msg==="您还没有登录")
          location.reload(); 
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
    //加载动画
    const openFullScreen2 = () => {
      store.setLoginLoadFlag(true);
      const loading = ElLoading.service({
        lock: true,
        text: "登录中",
        background: "rgba(0, 0, 0, 0.5)",
      });
      setTimeout(() => {
        loading.close();
      }, 1500);

    };

    const timewait = () => {
      // 在3秒后执行
      setTimeout(() => {
        ElMessage({
          message: "登录成功",
          type: "info",
          plain: true,
          duration: 1700,
        });
        store.setLoginLoadFlag(false);

      }, 1700);
    };

    const notLogin = () => {
      ElMessage({
        message: "您还没有登录",
        type: "info",
        plain: true,
        duration: 1700,
      });
      window.location.href = "../";
    };
    //注册请求
    async function sigininAxios() {
      try {

        if(signinDelayFlag)
        return;

        signinDelayFlag= true;

        setTimeout(() => {
        signinDelayFlag= false;
        }, 1800);

        if (signinForm.userName === "") {
          ElMessage({
            message: "请输入账号",
            type: "info",
            plain: true,
            duration: 1700,
          });

          return (loginDialogVisible.value = true);
        }
        if (signinForm.password === "") {
          ElMessage({
            message: "请输入密码",
            type: "info",
            plain: true,
          });

          return (loginDialogVisible.value = true);
        }
        if (signinForm.password.length < 4) {
          ElMessage({
            message: "密码过短",
            type: "info",
            plain: true,
          });

          return (loginDialogVisible.value = true);
        }
        if (signinForm.againPassword === "") {
          ElMessage({
            message: "请输入密码",
            type: "info",
            plain: true,
          });

          return (loginDialogVisible.value = true);
        }
        if (signinForm.phone === "") {
          ElMessage({
            message: "请输入手机号",
            type: "info",
            plain: true,
          });

          return (loginDialogVisible.value = true);
        }
        if (
          signinForm.phone.length !== 11 ||
          signinForm.phone.indexOf("1") !== 0
        ) {
          ElMessage({
            message: "手机号格式错误",
            type: "info",
            plain: true,
          });
          return (loginDialogVisible.value = true);
        }
        if (signinForm.question === "") {
          ElMessage({
            message: "请输入密保",
            type: "info",
            plain: true,
          });

          return (loginDialogVisible.value = true);
        }
        if (signinForm.password !== signinForm.againPassword) {
          ElMessage({
            message: "两次密码不一致",
            type: "info",
            plain: true,
            duration: 1700,
          });

          return (loginDialogVisible.value = true);
        }

        let limiterLoginDto={
           user: signinForm,
           userIp: store.userIp,
        };
        const response = await apiClient.post("/user/sign", limiterLoginDto);

        if (response.data.code === 0) {
          ElMessage({
            message: response.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
          });
        } else {
          flag.value = 1;
          handleClose();
          ElMessage({
            message: response.data.data,
            type: "info",
            plain: true,
            duration: 1700,
          });
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

    function togglePasswordVisibility() {
      showPassword.value = !showPassword.value;
    }
    //去除输入框空格
    function removeSpaces() {
      loginForm.password = loginForm.password.replace(/\s+/g, ""); // 去掉所有空格
      loginForm.userName = loginForm.userName.replace(/\s+/g, "");
    }

    function removeSpaces1() {
      signinForm.againPassword = signinForm.againPassword.replace(/\s+/g, "");
      signinForm.password = signinForm.password.replace(/\s+/g, "");
      signinForm.userName = signinForm.userName.replace(/\s+/g, "");
      signinForm.phone = signinForm.phone.replace(/\s+/g, "");
      signinForm.question = signinForm.question.replace(/\s+/g, "");
    }

    function removeSpaces2() {
      forgetPasswordForm.againpassword =
        forgetPasswordForm.againpassword.replace(/\s+/g, "");
      forgetPasswordForm.userName = forgetPasswordForm.userName.replace(
        /\s+/g,
        "",
      );
      forgetPasswordForm.phone = forgetPasswordForm.phone.replace(/\s+/g, "");
      forgetPasswordForm.question = forgetPasswordForm.question.replace(
        /\s+/g,
        "",
      );
      forgetPasswordForm.password = forgetPasswordForm.password.replace(
        /\s+/g,
        "",
      );
    }
    //初始化时执行检查是否登录过，有token则免登录
    onMounted(async() => {
      await getUserIp();
      autoLogin();

      setInterval(() => {
        ChecklLoginF();
      },60000);
    });

    //检查是否登录
    async function ChecklLoginF(){

    ChecklLogin(store.userIp).then(response=>{
    if (response.data.code === 1) {
        if(store.userId!==response.data.data.id||!store.userId)
          autoLogin();
        else{
          user.length = 0;
          store.setUserId(response.data.data.id);
          Object.assign(user, response.data.data);
          store.setUserInformation(response.data.data);
        }
    } else {
        store.setUserId(null);
        if(window.location.href.indexOf("message")>0)
          window.location.href = "../";
    }
    })
    }

    //自动登录
    async function autoLogin() {
      try {

        const response = await apiClient.get(`/user/checkLogin/${store.userIp}`);
        if (response.data.code === 1) {
          store.setUserId(response.data.data.id);
          Object.assign(user, response.data.data);
          store.setUserInformation(response.data.data)
          dynamics.length = 0;
          await getDynamicAxiso();
          await getNewFansNumberAxiso();
          await selectCollect();
          store.setToken(response.data.data.token);
          if(collectDtoList.length!==0)
          changerCollect.value=collectDtoList[0].id;
          selectHistoryAxios();
        }
        else{
          user.length = 0;
          store.userInformation={};
          dynamics.length = 0;
          dynamicss.length = 0;
          notHistoryDynamic.length = 0;
          collectDtoList.length = 0;
          changerCollect.value=0;
          historyList.length = 0;
          historyToday.length = 0;
          historyYesterday.length = 0;
          historyEarlier.length = 0;
          store.setToken(null);
        }

      } catch (error) {

      }
    }
    
    //刷新user
    async function refreshUser() {
      try {

        const response = await apiClient.get(`/user/checkLoginFlag/${store.userIp}`);
        if (response.data.code === 1) {
          user.length = 0;
          Object.assign(user, response.data.data);
          store.setUserInformation(response.data.data)
          store.setToken(response.data.data.token);
        }
      } catch (error) {

      }
    }

    //获取新粉丝数量
    async function getNewFansNumberAxiso() {
      try {
        const response = await apiClient.get(
          `/user/getNewFansNumber/${store.userId}`,
          {
            headers: {
            "Content-Type": "application/json",
            "Authorization": store.token,
          },        
          }
        );

        if (response.data.code === 1) {
          NewFansNumber.value = response.data.data;
        }
      } catch (error) {}
    }

    const sortItems = () => {
      dynamics.sort((a, b) => b.videoId - a.videoId); // 降序排序
    };

    //获取动态
    async function getDynamicAxiso() {
      try {
        const response = await apiClient.get(
          `/dynamic/getFollowDynamic/${store.userId}`,
          {
            headers: {
            "Content-Type": "application/json",
            "Authorization": store.token,
          },        
          }
        );

        if (response.data.code === 1) {
          dynamics.length = 0;
          dynamicss.length = 0;
          notHistoryDynamic.length = 0;
          Object.assign(dynamics, response.data.data);
          sortItems();
          dynamics.forEach((item) => {
            if (item.newDynamicNumber !== 0) {
              // 根据条件查找
              notWatchDynamicFlag.value = true; // 获取对应的值
              notHistoryDynamic.push(item);
            } else dynamicss.push(item);
          });
        }
      } catch (error) {}
    }

    //清空所有动态
    async function cleanAllDynamicAxios() {
      try {

        if (user.dynamicNumber !== 0) {
          const response = await apiClient.get(
            `/dynamic/cleanFollowDynamicAllNumber/${store.userId}`,
            {
            headers: {
            "Content-Type": "application/json",
            "Authorization": store.token,
          },        
          }
          );
          if (response.data.code === 1) dynamics.length = 0;
          dynamicss.length = 0;
          notHistoryDynamic.length = 0;
          getDynamicAxiso();
          refreshUser();
        }
      } catch (error) {}
    }

    //查询所有收藏
    async function selectCollect() {
      try {

        const response = await apiClient.get(
          `/collect/getCollect/${store.userId}`,
          {
            headers: {
            "Content-Type": "application/json",
            "Authorization": store.token,
          },        
          }
        );
        if (response.data.code === 1) {
          collectDtoList.length = 0;
          Object.assign(collectDtoList, response.data.data);

          const index=response.data.data.findIndex(item=>item.collectName==='稍后再看');
          if(index!==-1)
          {
            store.setCollectNumber(response.data.data[index].collectNumber);
          }

        }
      } catch (error) {}
    }

    //稍后观看
    async function waitWatchAxios(dynamic) {
      try {

        let dynamicDto={
            videoId: dynamic.videoId,
            userId: store.userId,
        }
        const response = await apiClient.put(
          "/dynamic/updateWaitWatch",dynamicDto,
            {
            headers: {
            "Content-Type": "application/json",
            "Authorization": store.token,
          },        
          }
        );
        if (response.data.code === 1) {
         dynamic.waitWatch=dynamic.waitWatch===1?0:1;
        }
        else{
          ElMessage({
            message: response.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
          });
        }
      } catch (error) {
      }
    }

    //查询单个收藏
    function selectOneCollect(collectId) {
      changerCollect.value = collectId;
      store.setCollectSelect(collectId);
      selectCollect();
    }

    //查询历史记录
    async function selectHistoryAxios() {
      historyAsideFlag.value = 0;
      if(historyAxiosTimeFlag)
      return;
      historyAxiosTimeFlag=true;
      try {
        const response = await apiClient.get(
          `/history/getHistory/${store.userId}`,
          {
            headers: {
            "Content-Type": "application/json",
            "Authorization": store.token,
          },        
          }
        );
        if (response.data.code === 1) {
          historyList.length = 0;
          Object.assign(historyList, response.data.data);
          const today = historyList.filter(
            (item) => item.watchVideoDate === "今天",
          );
          const yesterday = historyList.filter(
            (item) => item.watchVideoDate === "昨天",
          );
          const earlier = historyList.filter(
            (item) => item.watchVideoDate !== "今天"&&item.watchVideoDate !== "昨天",
          );
          historyToday.length = 0;
          historyYesterday.length = 0;
          historyEarlier.length = 0;
          Object.assign(historyToday, today);
          Object.assign(historyYesterday, yesterday);
          Object.assign(historyEarlier, earlier);
        }
      } catch (error) {}
    }

    //节流查询历史
    let historyAxiosTimeFlag=false;
    let historyTimeInterval=null;
    function historyAxiosTimeF() { 
        clearTimeout(historyTimeInterval);
        historyTimeInterval=setTimeout(() => {
           historyAxiosTimeFlag=false;
         }, 5000);
    }


    function histotyVideoProgress(videoTime, watchVideoTime) {
      const [videoMinutes, videoSeconds] = videoTime.split(":").map(Number);
      const [watchedMinutes, watchedSeconds] = watchVideoTime
        .split(":")
        .map(Number);

      const totalVideoTime = videoMinutes * 60 + videoSeconds;
      const totalWatchedTime = watchedMinutes * 60 + watchedSeconds;

      // 处理可能的除以零的情况
      if (totalVideoTime === 0) return 0;

      return (totalWatchedTime / totalVideoTime) * 100; // 返回百分比
    }

    function locationHerfVideo(videoId) {
      window.open(
        `./video?videoId=BV${videoId}`,
        "videoWindow",
      );
    }

    //统一收藏对话框的收藏夹
    function setCollectStore(){
      if(store.collectSelect)
      changerCollect.value=store.collectSelect;
    }

    //获取用户ip和token
    async function getUserIp(){
      
      const response = await apiClient.get("/userIp/getUserIp");

      if(response.data.code === 1)
        store.setUserIp(response.data.data.userIp);
        store.setToken(response.data.data.token);

    }

    watch(loginDialogVisible,(newValue)=>{
       
       store.setLoginDialogVisible(newValue);  
    })

    let beforeLoginDialogVisibleFlag=0;
    watch(propsS,()=>{
         
      if(beforeLoginDialogVisibleFlag!==propsS.loginDialogVisibleFlag)
      {
        loginDialogVisible.value=true;
        if(propsS.loginDialogVisibleFlag<2)
        {
          flag.value=1;
          beforeLoginDialogVisibleFlag=propsS.loginDialogVisibleFlag;
        }
        else
          flag.value=2;
        
      }
      

    },{deep:true})
   
    let loginLoadFlag=true;
    watch(()=>store.loginLoadFlag,()=>{

      if(store.loginLoadFlag&&loginLoadFlag)
      { 
        loginLoadFlag=false;
        autoLogin();
      }

    })


    //打开消息页面
    function openMessage(menu){

      if(store.userInformation.allMessageNumber>0)
      {
        let userInformation=store.userInformation;
        userInformation.allMessageNumber=0;
        if(menu===1)
        userInformation.messageNumber=0;
        if(menu===2)
        userInformation.replyCommentNumber=0;
        if(menu===3)
        userInformation.atNumber=0;
        if(menu===4)
        userInformation.likeAllNumber=0;
        store.setUserInformation(userInformation);
      }

      window.open(
        `./message?messageMenu=${menu}`,
        "_blank",
      );
    }

    //打开我的主页
    function openHome(menu,userId,collect){
      if(collect&&collect.collectName!=='稍后再看')
        {
          window.open(
        `./home?homeMenu=${menu}&userId=${userId||store.userId}`+`&collectId=${collect.collectClassifyId}`,
        "_blank",);
        }
      else if(collect){
          window.open("./waitWatch","_blank");
        } 
      else{
        window.open(`./home?homeMenu=${menu}&userId=${userId||store.userId}`)
      }  
    }

    //打开历史记录
    function openHistory(){
      window.open("./history","_blank",);
    }

    //打开动态
    function openDynamic(){
      window.open("./dynamic","_blank",);
    }
    

    return {
      loginDialogVisible,
      loginForm,
      signinForm,
      forgetPasswordForm,
      passwordLogin,
      siginLogin,
      findPassword,
      flag,
      formatPhone,
      handleClose,
      handleForgotPassword,
      handlerSign,
      loginAxios,
      logout,
      sigininAxios,
      putPasswordAxios,
      sa,
      user,
      left1,
      left2,
      right1,
      right2,
      togglePasswordVisibility,
      removeSpaces,
      removeSpaces1,
      removeSpaces2,
      openFullScreen2,
      timewait,
      notLogin,
      fullscreenLoading,
      NewFansNumber,
      waitWatch,
      dynamics,
      cleanAllDynamicAxios,
      notWatchDynamicFlag,
      notHistoryDynamic,
      dynamicss,
      changerCollect,
      collectDtoList,
      selectCollect,
      getDynamicAxiso,
      waitWatchAxios,
      selectOneCollect,
      historyAsideFlag,
      selectHistoryAxios,
      historyAxiosTimeF,
      historyToday,
      historyYesterday,
      historyEarlier,
      histotyVideoProgress,
      locationHerfVideo,
      head2Flag,
      searchFlag,
      setCollectStore,
      store,
      openMessage,
      openHome,
      openHistory,
      openDynamic,
    };
  },
};
</script>

<style lang="scss" scoped>

@font-face {
  font-family: "SimHei";
  src: url("../assets/font/MSYH.TTC") format("truetype"); /* 指定字体文件的位置和格式 */
  font-weight: normal;
  font-style: normal;
}
* {
  padding: 0; /* 移除内边距 */
  margin: 0; /* 移除外边距 */
  box-sizing: border-box; /* 包括内边距和边框在元素的总宽度和高度中 */
  transition: background-color 0.3s ease, color 0.3s ease;
}

a {
  text-decoration: none;
}

.header {
  width: 1450px;
  height: 70px;
  margin: 0 auto; /* 居中对齐 */
  display: flex;
  transform: translate(28px);
  align-items: center; /* 垂直居中对齐内容 */
  justify-content: center;
}

.el-overlay {
  width: 400%;
  height: 160%;
  display: flex;
  transform: translate(-1310px, -120px);
}

.v-header-ul {
  display: flex;
  list-style-type: none;
  margin-right: 36px;
  margin-bottom: 3px;
  position: absolute;
  width: 1410px;
  justify-content: space-around;
}

.v-header-ul li {
  display: flex;
  align-items: center; /* 垂直居中对齐列表项中的内容 */
  cursor: pointer;
}

.v-header-ul li a img:hover {
  animation: bounce 0.5s ease forwards;
}

.number-style {
  position: absolute;
  z-index: 1;
  top: -6px;
  right: -4.5px;
  padding: 0 3px;
  min-width: 15px;
  border-radius: 10px;
  text-align: center;
  background-color: #fa5a57;
  color: #fff;
  font-size: 12px;
  line-height: 15px;
}


.v-header-ul li a {
  font-size: 13.5px;
  display: flex;
  align-items: center; /* 垂直对齐图片和文字 */
  margin-top: -5px;
  line-height: 10px;
  color:white;
}

.head2Flag li a{
  color:#18191c;
}


.v-header-ul li img {
  margin-right: 6px; /* 图片与文本之间的间距 */
  animation-delay: 0.3s !important;
  
}

.avatar-container {
  position: relative;
  display: inline-block;
}

.avatar1 {
  position: relative;
  width: 32.5px;
  height: 32.5px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease, width 0.3s ease, height 0.3s ease;
  z-index: 100000;
  backface-visibility: hidden;
  perspective: 1000px;
}

.avatar-container:hover .avatar1 {
  transform: translate(-14px, 20px) scale(2.1);
}
.avatar-container:hover .avatar1 img {
  transform: translate(3.5px, 0px) scale(1.1);
}

.avatar1 img {
  transform: translateX(3.5px) scale(1.17);
  width: 100%; /* Adjusted width to fit inside the avatar */
  height: 100%; /* Adjusted height to fit inside the avatar */
}

.avatar-container:hover .avatar1 img {
  width: 40px; /* Scaled up image width on hover */
  height: 40px; /* Scaled up image height on hover */
}

.user-info {
  cursor: pointer;
  text-align: center;
  position: absolute;
  display: flex;
  flex-direction: column;
  top: 100%;
  left: 50%;
  transform: translate(-55%, -10px);
  background-color: white;
  color: rgb(0, 0, 0);
  padding: 10px;
  border-radius: 10px;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.4s ease, visibility 0s ease;
  width: 275px;
  height: 425px;
  z-index: 10000;
  border: 1px solid #d3d3d3; /* 浅灰色边框 */
}
.grade .grade1 .exp {
  margin-right: 20px;

  width: 22px;
  height: 14px;
}
.grade {
  display: flex;
  flex-direction: column;
}
.grade span {
  transform: translateX(3px);
}
.grade .grade1 .exp2 {
  width: 22px;
  height: 14px;
}
.grade .grade1 {
  display: flex;
  justify-content: center;
}
.avatar-container:hover .user-info {
  transition-delay: 0.2s;
  opacity: 1;
  visibility: visible;
  top: 60px; /* Adjust as needed to position the box */
}

.feature div {
  margin-right: 82px;
  font-size: 14px;
  width: 220px;
  height: auto;
  display: flex;
  transform: translateX(40px);
  align-items: center;
  border-radius: 5px;
}

.feature div:hover {
  background-color: #e3e5e7;
}

.feature div span {
  height: 35px;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: translateX(27px);
  color: #62666c;
}
.feature div img {
  width: 18px;
  height: 16px;
  transform: translateX(14px);
}

.avatar {
  position: relative; /* 使子元素相对定位 */
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background-color: #00aeec;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.avatar span {
  font-size: 13px;
  color: white;
}
.vip-introduce {
  text-align: center;
  position: absolute;
  display: flex;
  flex-direction: column;
  top: 148%;
  left: 50%;
  transform: translate(-55%, 0px);
  background-color: white;
  color: rgb(0, 0, 0);
  padding: 10px;
  border-radius: 10px;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.4s ease, visibility 0.4s ease;
  z-index: 5000 !important;
  width: 400px;
  height: 220px;
  border: 1px solid #d3d3d3; /* 浅灰色边框 */
}
.transparent-div12 {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translate(-50%, 0px);
  visibility: hidden;
  width: 30px;
  height: 22px;
  opacity: 0;
}
.v-header-ul li:nth-child(11):hover .transparent-div12 {
  visibility: visible;
}
.vip-introduce div span img {
  width: 26px;
  height: 24px;
}
.vip-introduce div span a {
  animation: none !important;
}
.v-header-ul li:nth-child(11):hover .vip-introduce {
  transition-delay: 0.2s;
  visibility: visible;
  opacity: 1;
}
.login-info {
  position: absolute;
  top: 100%; 
  z-index: 10; 
  width: 364px;
  height: 210px;
  margin-top: 15px;
  border-radius: 5px;
  background-color: white;
  visibility: hidden; /* 默认隐藏 */
  opacity: 0; /* 默认透明 */
  transition: opacity 0.4s ease, visibility 0.4s ease; /* 过渡效果 */
  border: 1px solid #d3d3d3; /* 浅灰色边框 */
}
.message-info {
  text-align: center;
  position: absolute;
  display: flex;
  flex-direction: column;
  top: 148%;
  left: 50%;
  transform: translate(-52%, 0px);
  background-color: white;
  color: rgb(0, 0, 0);
  padding: 10px;
  border-radius: 10px;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s ease, visibility 0.3s ease;
  z-index: 5;
  width: 130px;
  height: 220px;
  border: 1px solid #d3d3d3; /* 浅灰色边框 */
}
.transparent-div13 {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translate(-50%, 0px);
  visibility: hidden;
  width: 25px;
  height: 20px;
  opacity: 0;
}
.v-header-ul li:nth-child(12):hover .transparent-div13 {
  visibility: visible;
}
.message-info span {
  height: 40px;
  width: 128px;
  font-size: 14px;
  display: flex;
  align-items: center;
  transform: translate(-10px, -7px);
  color: #62666c;
}
.message-info span .text {
  display: flex;
  transform: translateX(20px);
}

.message-info span:hover {
  background-color: #e3e5e7;
}
.v-header-ul li:nth-child(12):hover .message-info {
  transition-delay: 0.2s;
  visibility: visible;
  opacity: 1;
}
.login-can {
  color: black;
  font-family: "SimHei";
  font-size: 14px;
  margin-top: 20px;
  margin-bottom: 10px;
  margin-left: 20px;
}

.avatar:hover .login-info {
  transition-delay: 0.2s;
  visibility: visible;
  opacity: 1;
}

.fuli {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 5px; /* Optional: Adjusts the space between grid items */
  list-style-type: none;
  margin-left: 10px;
  margin-top: 10px;
}

.fuli li {
  display: flex;
  align-items: center; /* 确保图片和文字垂直居中对齐 */
  font-size: 14px;
  padding: 5px 0; /* 可选: 添加上下内边距 */
}

.fuli li img {
  width: 20px;
  margin-right: 8px;
}

.fuli li:nth-child(2n) {
  transform: translateX(-10%);
}

.login {
  display: flex;
  width: 320px;
  height: 36px;
  background-color: #00aeec;
  margin-top: 12px;
  border-radius: 5px;
  justify-content: center;
  overflow: hidden;
}
.login .el-bu {
  display: flex;
  align-content: center;
  transform: translate(0,-9px);
  width: 320px;
  height: 43px;
  background-color: #00aeec;
  font-size: 14px;
  color: white;
  border: none;
}
.login .el-bu:hover {
 
  background-color: #49bbf0;
  
}

.zhu-ce {
  width: 200px;
  color: black;
}
.deng-lu {
  width: 200px;
  color: black;
}
.dialogin-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 20px;
}

.dialogin-info .dialogin-title {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}
.submit-div {
  display: flex;
  justify-content: center;
}
.submit-div el-button {
  width: 140px;
  height: 36px;
  border-radius: 5px;
}


.dialogin-info .dialogin-title button {
  color: black;
  font-size: 20px;
}

.sign-in {
  height: 20px;
  margin-top: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 14px;
}
.sign-in a div {
  font-size: 14px;
  color: #00aeec;
}
.active {
  color: #00aeec !important;
}

.v-header-ul li:nth-child(n + 11):nth-child(-n + 16) a {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transform: translate(-3px);
  margin-top: 5px;
}

.v-header-ul li:nth-child(2){
   transform: translate(-2px);
}
.v-header-ul li:nth-child(3){
   transform: translate(-3.5px);
}

.v-header-ul li:nth-child(4){
   transform: translate(-5.5px);
}

.v-header-ul li:nth-child(5){
   transform: translate(-6px);
}

.head2Flag li:nth-child(5){
  transform: translate(-7px);
}

.v-header-ul li:nth-child(6){
   transform: translate(-7px);
}

.head2Flag li:nth-child(6){
  transform: translate(-9px);
}


.v-header-ul li:nth-child(7){
   transform: translate(-9px);
}

.head2Flag li:nth-child(7){
  transform: translate(-11px);
}

.v-header-ul li:nth-child(8){
   transform: translate(-11px);
}

.head2Flag li:nth-child(9){
  margin-right: -40px;
  transform: translate(-17.5px);
}

.head2Flag li:nth-child(8){
  transform: translate(-13px);
}


.v-header-ul li:nth-child(10){
   transform: translate(-8px,-2px);
   z-index: 10000000;
}

.v-header-ul li:nth-child(11){
  transform: translate(-3px,-4px);
  z-index: 10000;
}

.v-header-ul li:nth-child(12){
  transform: translate(-3px,-4px);
}
.v-header-ul li:nth-child(13){
  transform: translate(0px,-4px);
}
.v-header-ul li:nth-child(14){
  transform: translate(4.5px,-4px);
  z-index: 10000;

}
.v-header-ul li:nth-child(15){
  transform: translate(9px,-4px);
}

.v-header-ul li:nth-child(16){
  transform: translate(3px,-4px);
}

.head2Flag li:nth-child(17){
  transform: translate(-3.5px);
}

.head2Flag li:nth-child(n + 11):nth-child(-n + 16){
  a{
    color: #62666c;
    font-size: 12px;
  }
}

.v-header-ul li:nth-child(n + 11):nth-child(-n + 16){
   position: relative;
}

.v-header-ul li:nth-child(n + 11):nth-child(-n + 16) a img {
  margin-left: 4px;
  height: 17px;
  width: 16px;
  margin-bottom: 5px;
}

.login-info2 {
  display: flex;
  flex-direction: column; /* 使内容垂直排列 */
  align-items: center; /* 水平居中对齐内容 */
  position: absolute; /* 使 `.login-info2` 相对于 `.v-header-ul li` 定位 */
  top: 100%; /* 让 `.login-info2` 在 `li` 的下方 */
  left: 50%; /* 设置 `left` 为 50% 使其相对于 `li` 居中 */
  transform: translateX(
    -50%
  ); /* 使用 `translateX` 将 `.login-info2` 水平居中 */
  z-index: 10; /* 确保 `.login-info2` 在其他元素之上 */
  width: 364px;
  height: 120px;
  margin-top: 15px;
  border-radius: 5px;
  background-color: white;
  color: #a6acaf;
  visibility: hidden; /* 默认隐藏 */
  opacity: 0; /* 默认透明 */
  transition: opacity 0.3s ease, visibility 0.3s ease; /* 过渡效果 */
  text-align: center; /* 文本居中对齐 */
  border: 1px solid #d3d3d3; /* 浅灰色边框 */
}

.el-bu {
  margin-top: 10px; /* 按钮与文本之间的间距 */
  width: 320px;
  height: 38px;
  background-color: #00aeec;
  font-size: 14px;
  color: white;
  border: none;
  transform: translateY(50%);
}

.el-bu:hover{
  background-color: #49bbf0;

}

.v-header-ul li:nth-child(12):hover .login-info2,
.v-header-ul li:nth-child(13):hover .login-info2,
.v-header-ul li:nth-child(14):hover .login-info2,
.v-header-ul li:nth-child(15):hover .login-info2 {
  visibility: visible; /* 悬停时可见 */
  opacity: 1; /* 悬停时不透明 */
}

.v-header-ul li:nth-child(13) a img {
  height: 17px;
}
.v-header-ul li:nth-child(14) a img {
  margin-left: 4px;
}
.v-header-ul li:nth-child(15) a img {
  height: 12px;
}

.v-header-ul li:nth-child(16) a img {
  height: 10px;
}

.upload {
  margin-top: -1px;
  margin-right: 10px;
  transform: translate(-4px);
  width: 90px;
  height: 33px;
  background-color: #fb7299;
  border-radius: 7px;
  display: flex;
  justify-content: center;
  align-items: center;
  border: none; /* 去掉默认边框 */
  cursor: pointer;
}

.upload:hover{
  background-color: #ef80a0;

}

.upload a {
  text-decoration: none; /* 去掉链接下划线 */
  color: inherit; /* 继承父元素颜色 */
  display: flex;
  align-items: center;
  transform: translateY(1px);
}

.upload img {
  width: 15px;
  height: 15px; /* 确保图标的高度与宽度一致 */
  margin-right: 4px; /* 增加图标和文本之间的间距 */
  animation: none !important;
}

.upload span {
  font-size: 13.5px; /* 设置文本字体大小 */
  margin-bottom: 0; /* 确保文本没有底部间距 */
  color: white;
}
@keyframes bounce {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px); /* 上升10px */
  }
  100% {
    transform: translateY(0); /* 返回原位 */
  }
}
.v-header-ul li:nth-child(9):hover,
.v-header-ul li:nth-child(10):hover,
.v-header-ul li:nth-child(17):hover {
  animation: none !important;
}

.newFans {
  position: absolute;
  font-size: 14px;
  transform: translate(2px, 8px);
  color: #e1678d;
}

.dynamic-info {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translate(-48%, 20px);
  background-color: white;
  color: #a6acaf;
  padding: 20px;
  border-radius: 10px;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s ease, visibility 0.3s ease;
  z-index: 5;
  width: 380px;
  height: 535px;
  max-height: 535px;
  border: 1px solid #d3d3d3; /* 浅灰色边框 */
  overflow-y: auto; /* 允许垂直滚动 */
  overflow-x: hidden; /* 禁止水平滚动 */
}
.transparent-div14 {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translate(-50%, 0px);
  visibility: hidden;
  width: 25px;
  height: 20px;
  opacity: 0;
}
.v-header-ul li:nth-child(13):hover .transparent-div14 {
  visibility: visible;
}

.v-header-ul li:nth-child(13):hover .dynamic-info {
  transition-delay: 0.2s;
  opacity: 1;
  visibility: visible;
}

.dynamicContent {
  margin-top: 20px;
  height: 535px;
  max-height: 535px;
}

.dynamicContent-item {
  margin-left: -20px;
  width: 112%;
  height: 100px;
  background-color: white;
  cursor: pointer;
}

.dynamicContent-item:hover {
  background-color: #e3e5e7;
}

.dynamicVideoTitle {
  font-size: 13px;
  color: black;
  display: flex;
  transform: translate(65px, 13px);
  width: 200px;
  -webkit-line-clamp: 2; /* 限制为 2 行 */
  overflow: hidden; /* 隐藏超出部分 */
  text-overflow: ellipsis; /* 显示省略号 */
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  display: -webkit-box; /* 使用 flexbox 布局 */
  word-wrap: break-word; /* 超出部分换行 */
  height: 35px;
}
.dynamic-info::-webkit-scrollbar {
  width: 5px; /* 滚动条的宽度 */
  border-radius: 30px; /* 滚动条滑块的圆角 */
}

.dynamic-info::-webkit-scrollbar-thumb {
  background: #bcbcbc; /* 滚动条的滑块 */
  height: 10px;
  border-radius: 10px;
}

.checkAllDynamic {
  color: #818489;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 30px;
  font-size: 14px;
  cursor: pointer;
  border-radius: 5px;
  background-color: #f6f7f8;
  margin-top: 20px;
}

.checkAllDynamic:hover .dynamicContent-item {
  background-color: white;
}

.checkAllDynamic:hover {
  color: black;
}

.collect-info {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translate(-65%, 20px);
  background-color: white;
  color: #a6acaf;
  border-radius: 10px;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s ease, visibility 0.3s ease;
  z-index: 5;
  width: 510px;
  height: 535px;
  max-height: 535px;
  border: 1px solid #d3d3d3; /* 浅灰色边框 */
  display: flex;
  overflow: hidden;
}

.v-header-ul li:nth-child(14):hover .collect-info {
  transition-delay: 0.2s;
  opacity: 1;
  visibility: visible;
}
.transparent-div15 {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translate(-50%, 0px);
  visibility: hidden;
  width: 25px;
  height: 20px;
  opacity: 0;
}
.v-header-ul li:nth-child(14):hover .transparent-div15 {
  visibility: visible;
}
.collectContent {
  padding-top: 12px;
  overflow-y: auto; /* 允许垂直滚动 */
  overflow-x: hidden; /* 禁止水平滚动 */
  width: 100%;
  height: 545px;
  padding-bottom: 10px;
}

.collectContent::-webkit-scrollbar {
  width: 3.5px; /* 滚动条的宽度 */
  border-radius: 30px; /* 滚动条滑块的圆角 */
}

.collectContent::-webkit-scrollbar-thumb {
  background: #bcbcbc; /* 滚动条的滑块 */
  height: 10px;
  border-radius: 10px;
}

.collectAside {
  overflow-y: auto; /* 允许垂直滚动 */
  overflow-x: hidden; /* 禁止水平滚动 */
  border-right: 1px solid #d3d3d3;
  width: 180px;
  height: 100%;
  padding-top: 12px;
}
.collectAside::-webkit-scrollbar {
  width: 3.5px; /* 滚动条的宽度 */
  border-radius: 30px; /* 滚动条滑块的圆角 */
}

.collectAside::-webkit-scrollbar-thumb {
  background: #bcbcbc; /* 滚动条的滑块 */
  height: 10px;
  border-radius: 10px;
}
.collectAsideTitle1 {
  width: 100%;
  height: 45px;
  background-color: #ffffff;
  cursor: pointer;
  line-height: 45px;
  padding-left: 15px;
  padding-right: 15px;
}

.collectAsideTitle2 {
  width: 100%;
  height: 45px;
  background-color: #00aeec;
  cursor: pointer;
  line-height: 45px;
  padding-left: 15px;
  padding-right: 15px;
}

.collectAsideTitle1 .collectAsideName {
  display: -webkit-box; /* 使用 flexbox 布局 */
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  width: 85px;
  font-size: 14px;
  color: black;
  -webkit-line-clamp: 1; /* 限制为 2 行 */
  overflow: hidden; /* 隐藏超出部分 */
  text-overflow: ellipsis; /* 显示省略号 */
}
.collectAsideTitle2 .collectAsideName {
  display: -webkit-box; /* 使用 flexbox 布局 */
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  width: 85px;
  font-size: 14px;
  color: white;
  -webkit-line-clamp: 1; /* 限制为 2 行 */
  overflow: hidden; /* 隐藏超出部分 */
  text-overflow: ellipsis; /* 显示省略号 */
}

.collectContent-item {
  width: 100%;
  height: 90px;
  padding-top: 10px;
  padding-bottom: 10px;
  padding-left: 20px;
  padding-right: 20px;
}
.collectContent-item:hover {
  background-color: #e3e5e7;
}

.collectVideoTime {
  font-size: 10px;
  color: white;
  background-color: rgb(0, 0, 0, 0.65);
  border-radius: 3px;
  height: 15px;
  width: 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  transform: translate(102px, -22px);
  cursor: pointer;
}
.collectVideoTitle {
  display: -webkit-box; /* 使用 flexbox 布局 */
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  width: 195px;
  color: black;
  font-size: 14px;
  -webkit-line-clamp: 2; /* 限制为 2 行 */
  overflow: hidden; /* 隐藏超出部分 */
  word-wrap: break-word; /* 超出部分换行 */
  text-overflow: ellipsis; /* 显示省略号 */
  transform: translate(145px, -90px);
  cursor: pointer;
  height: 35px;
}
.collectVideoUpName {
  display: -webkit-box; /* 使用 flexbox 布局 */
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  width: 150px;
  font-size: 12px;
  -webkit-line-clamp: 1; /* 限制为 1行 */
  overflow: hidden; /* 隐藏超出部分 */
  text-overflow: ellipsis; /* 显示省略号 */
  font-size: 12px;
  color: #95999f;
  transform: translate(169px, -91.5px);
  cursor: pointer;
}
.dynamicUserName {
  width: 80px;
  font-size: 13px;
  color: #62666c;
  display: flex;
  transform: translate(65px, 10px);
  display: -webkit-box; /* 使用 flexbox 布局 */
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  -webkit-line-clamp: 1; /* 限制为 1行 */
  overflow: hidden; /* 隐藏超出部分 */
  text-overflow: ellipsis; /* 显示省略号 */
}
.collectFrature {
  width: 376px;
  position: absolute;
  display: flex;
  transform: translate(132px, 487.5px);
  background-color: white;
  height: 45px;
  line-height: 30px;
  border-left: 1px solid #d3d3d3;
  border-top: 1px solid #d3d3d3;
}
.checkAllCollect {
  width: 188px;
  font-size: 15px;
  border-right: 1px solid #d3d3d3;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  color: black;
}
.playAllCollect {
  width: 188px;
  font-size: 15px;
  text-align: center;
  line-height: 45px;
  color: #00aeec;
}
.playAllCollect img {
  width: 15px;
  transform: translate(0px, 3px);
}

.collectIsNull {
  position: absolute;
  color: #62666c !important;
  transform: translate(115px, 160px);
  font-size: 14px;
}

.v-header-ul li:nth-child(15):hover .history-info {
  transition-delay: 0.2s;
  opacity: 1;
  visibility: visible;
}
.transparent-div16 {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translate(-50%, 0px);
  visibility: hidden;
  width: 25px;
  height: 20px;
  opacity: 0;
}
.v-header-ul li:nth-child(15):hover .transparent-div16 {
  visibility: visible;
}
.history-info {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translate(-64%, 20px);
  background-color: white;
  color: #a6acaf;
  border-radius: 10px;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s ease, visibility 0.3s ease;
  z-index: 5;
  width: 380px;
  height: 535px;
  max-height: 535px;
  border: 1px solid #d3d3d3; /* 浅灰色边框 */
  overflow: hidden;
}

.historyAside {
  width: 100%;
  height: 50px;
  display: flex;
  justify-content: space-around;
  align-items: center;
  color: black;
  font-size: 14px;
  border-bottom: 1px solid #d3d3d3;
  cursor: pointer;
  overflow: hidden;
}

.historyContent {
  overflow-y: auto; /* 允许垂直滚动 */
  overflow-x: hidden; /* 禁止水平滚动 */
  width: 100%;
  height: 480px;
  padding-top: 10px;
  padding-bottom: 10px;
}

.historyContent::-webkit-scrollbar {
  width: 3.5px; /* 滚动条的宽度 */
  border-radius: 30px; /* 滚动条滑块的圆角 */
}

.historyContent::-webkit-scrollbar-thumb {
  background: #bcbcbc; /* 滚动条的滑块 */
  height: 10px;
  border-radius: 10px;
}

.historyContent-item-today {
  width: 100%;
  height: 90px;
  padding-top: 10px;
  padding-bottom: 10px;
  padding-left: 20px;
  padding-right: 20px;
  margin-top: 5px;
  margin-bottom: 5px;
  cursor: pointer;
}
.historyContent-item-yesterday {
  width: 100%;
  height: 90px;
  padding-top: 10px;
  padding-bottom: 10px;
  padding-left: 20px;
  padding-right: 20px;
  margin-top: 5px;
  cursor: pointer;
}
.historyContent-item-today:hover {
  background-color: #e3e5e7;
}
.historyContent-item-yesterday:hover {
  background-color: #e3e5e7;
}
.historyVideoTime {
  font-size: 11px;
  color: white;
  background-color: rgb(0, 0, 0, 0.65);
  border-radius: 3px;
  height: 16px;
  width: 68px;
  display: flex;
  justify-content: center;
  align-items: center;
  transform: translate(67px, -26px);
  cursor: pointer;
}

.historyVideoTitle {
  display: -webkit-box; /* 使用 flexbox 布局 */
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  width: 195px;
  color: black;
  font-size: 14px;
  -webkit-line-clamp: 2; /* 限制为 2 行 */
  overflow: hidden; /* 隐藏超出部分 */
  word-wrap: break-word; /* 超出部分换行 */
  text-overflow: ellipsis; /* 显示省略号 */
  transform: translate(145px, -93px);
  cursor: pointer;
  height: 36px;
}

.history-system {
  display: flex;
  transform: translate(145px, -92px);
}

.history-system img {
  width: 14px;
  height: 12px;
  transform: translate(2.5px, 3px);
}
.history-system span {
  font-size: 12px;
  color: #95999f;
}
.historyVideoUpName {
  display: -webkit-box; /* 使用 flexbox 布局 */
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  width: 150px;
  font-size: 12px;
  -webkit-line-clamp: 1; /* 限制为 1行 */
  overflow: hidden; /* 隐藏超出部分 */
  text-overflow: ellipsis; /* 显示省略号 */
  font-size: 12px;
  color: #95999f;
  transform: translate(169px, -110px);
  cursor: pointer;
}

.checkAllHistory {
  color: #818489;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 88%;
  height: 34px;
  font-size: 14px;
  border-radius: 5px;
  background-color: #f6f7f8;
  margin-top: 20px;
  cursor: pointer;
}
.checkAllHistory:hover {
  color: black;
}

.historyVideoProgress {
  appearance: none;
  width: 136px;
  height: 3px;
  display: flex;
  transform: translate(1px, -7.3px);
}

.historyVideoProgress::-webkit-progress-bar {
  background-color: #000000; /* 未加载的颜色 */
}

.historyVideoProgress::-webkit-progress-value {
  background-color: #e1678d; /* 加载的颜色 */
  border-radius: 0 0 5px 5px !important;
}

.head-search{
  width: 413px;
  height: 38.5px; 
  margin-right: 15px;
  transform: translate(3px);
}

.el-form-item{
  margin-bottom: 10px;
}

.v-header-ul li:nth-child(n + 11):nth-child(-n + 16) a{
  font-size: 13px;
}

.head2SearchFlag{
  top: -0.5px;
  width: 337px;
  height: 39px;
  margin-right: 47px;
 
}

.head-home{
  width: 8px;
  margin-left: 6px;                                                                                                                  
  margin-top: 2px;
  animation: none !important;
  transition: 0.3s ease-in-out;
}

.head-li1:hover .head-home{
  transform: rotate(180deg);
}

.dynamic-dataNull{
  position: absolute;
  width: 200px;
  height: 50px;
  left: 50%;
  transform: translate(-50%, 200px);
  font-size: 14px;
  color: #a6acaf;
  z-index: 1000;
}

.message-number-style{
  position: absolute;
  z-index: 1;
  right: 15px;
  padding: 0 3px;
  min-width: 15px;
  border-radius: 10px;
  text-align: center;
  background-color: #fa5a57;
  color: #fff;
  font-size: 12px;
  line-height: 15px;
}

.user-info-number{
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 0 30px;

  span{
    display: flex;
    align-items: center;
    flex-direction: column;
    justify-content: space-between;
    transition: color .3s ease;
    line-height: 1.6;

    div:nth-child(1){
    font-weight: 550;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #18191C;

    span{
      font-size: 12px;
      color: #e1678d;
    }
    }
    div:nth-child(2){
    font-size: 12px;
    color: #9499A0;
    }
  }
  span:hover{
    div{
      color: #00AEEC;
    }
  }

}
</style>
