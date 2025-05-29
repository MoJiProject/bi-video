<template>
    <div class="config-container">
      <div class="head2">消息设置</div>
      <div class="config">
        <div>消息提醒<span class="tips">（关闭后，消息将不再进行提醒）</span></div>
        <div class="switch">
          <span @click=setSetting(1,1)>
            <img class="open" :src="store.userInformation.messageWarn===1? '../img/消息框开.png' : '../img/消息框关.png' ">
            <span>开启</span>
          </span>
          <span @click="setSetting(1,0)">
            <img class="close" :src="store.userInformation.messageWarn===0? '../img/消息框开.png' : '../img/消息框关.png' ">
            <span>关闭</span>
          </span>
        </div>
      </div>
      <div class="config">
        <div>回复我的消息提醒<span class="tips">（接受谁的评论消息提醒）</span></div>
        <div class="switch">
          <span @click="setSetting(2,1)">
            <img class="open" :src="store.userInformation.replyMessageWarn===1? '../img/消息框开.png' : '../img/消息框关.png' ">
            <span>所有人</span>
          </span>
          <span @click="setSetting(2,2)">
            <img class="close" :src="store.userInformation.replyMessageWarn===2? '../img/消息框开.png' : '../img/消息框关.png' ">
            <span>关注的人</span>
          </span>
          <span @click="setSetting(2,0)">
            <img class="close" :src="store.userInformation.replyMessageWarn===0? '../img/消息框开.png' : '../img/消息框关.png' ">
            <span>不接受任何消息提醒</span>
          </span>
        </div>
      </div>
      <div class="config">
        <div>@我的消息提醒<span class="tips">（接受谁的@消息提醒）</span></div>
        <div class="switch">
          <span @click="setSetting(3,1)">
            <img class="open" :src="store.userInformation.atMessageWarn===1? '../img/消息框开.png' : '../img/消息框关.png' ">
            <span>所有人</span>
          </span>
          <span @click="setSetting(3,2)">
            <img class="close" :src="store.userInformation.atMessageWarn===2? '../img/消息框开.png' : '../img/消息框关.png' ">
            <span>关注的人</span>
          </span>
          <span @click="setSetting(3,0)">
            <img class="close" :src="store.userInformation.atMessageWarn===0? '../img/消息框开.png' : '../img/消息框关.png' ">
            <span>不接受任何消息提醒</span>
          </span>
        </div>
      </div>
      <div class="config">
        <div>收到的赞消息提醒</div>
        <div class="switch">
          <span @click="setSetting(4,1)">
            <img class="open" :src="store.userInformation.likeMessageWarn===1? '../img/消息框开.png' : '../img/消息框关.png' ">
            <span>开启</span>
          </span>
          <span @click="setSetting(4,0)">
            <img class="close" :src="store.userInformation.likeMessageWarn===0? '../img/消息框开.png' : '../img/消息框关.png' ">
            <span>关闭</span>
          </span>
        </div>
      </div>
      <div class="config">
        <div>背景图片模式</div>
        <div class="switch">
          <span @click="setSetting(5,1)">
            <img class="open" :src="backgroundModel===1? '../img/消息框开.png' : '../img/消息框关.png' ">
            <span>开启</span>
          </span>
          <span @click="setSetting(5,0)">
            <img class="close" :src="backgroundModel===0? '../img/消息框开.png' : '../img/消息框关.png' ">
            <span>关闭</span>
          </span>
        </div>
      </div>
    </div>
  
  </template>
  
  <script setup>
  import { ref } from "vue";
  import {useGlobalStore} from "../store/store";
  import {putMessageSettings} from '../api/user/index'
  
  const store = useGlobalStore();
  const backgroundModel=ref(parseInt(localStorage.getItem('backgroundModel')) || 0);

  function setSetting(index, value) {

    let user=store.userInformation;

    if(index===1){
      user.messageWarn=value;
      store.setUserInformation(user);
    }
    else if(index===2){
      user.replyMessageWarn=value;
      store.setUserInformation(user);
    }
    else if(index===3){
      user.atMessageWarn=value;
      store.setUserInformation(user);
    }
    else if(index===4){
      user.likeMessageWarn=value;
      store.setUserInformation(user);
    }
    else if(index===5){
      localStorage.setItem("backgroundModel",value);
      backgroundModel.value=value;
      user.backgroundModel=value;
      store.setUserInformation(user);
    }
    putMessageSettings(store);

  }

  
  </script>
  
  <style lang="scss" scoped>
  *{
      margin: 0;
      padding: 0;
      box-sizing: border-box;
  }
  
  .config-container{
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

      .config{
        margin-top: 10px;
        position: relative;
        width: 100%;
        padding: 24px 16px;
        background-color: var(--background-color);
        user-select: none;
        border-radius: 4px;
        box-shadow: 0 2px 4px 0 rgba(121, 146, 185, 0.54);
        font-size: 14px;
        color: #333;

        .tips{
          color: #999;
        }
        .switch{
          position: relative;
          top: 5px;
        span{
          cursor: pointer;
        }  
        .open{
          position: relative;
          top: 5.5px;
          width: 22px;
          height: 22px;
          margin-right: 5px ;
        }
        .close{
          position: relative;
          top: 5.5px;
          margin-left: 40px;
          margin-right: 5px;
          width: 22px;
          height: 22px;
        }
        }
      }
  
  }
  
  </style>