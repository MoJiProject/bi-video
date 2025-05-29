<template>
  <div class="aside-container">
    <div class="title">
        <img src="../img/消息中心.png">
        <span>消息中心</span>
    </div>
    <ul>
        <li :class="{liColor: store.messageMenu === 1}"><span @click="jumpPage(1)" class="active">我的消息<span v-show="store?.userInformation.messageNumber > 0" class="message-num">{{ store.userInformation.messageNumber<100? store.userInformation.messageNumber : '99+' }}</span></span></li>
        <li :class="{liColor: store.messageMenu === 2}"><span @click="jumpPage(2)" class="active">回复我的<span v-show="store?.userInformation.replyCommentNumber > 0" class="message-num">{{ store.userInformation.replyCommentNumber<100? store.userInformation.replyCommentNumber : '99+' }}</span></span></li>
        <li :class="{liColor: store.messageMenu === 3}"><span @click="jumpPage(3)" class="active">@ 我的<span v-show="store?.userInformation.atNumber > 0" class="message-num">{{ store.userInformation.atNumber<100? store.userInformation.atNumber : '99+' }}</span></span></li>
        <li :class="{liColor: store.messageMenu === 4}"><span @click="jumpPage(4)" class="active">收到的赞<span v-show="store?.userInformation.likeAllNumber > 0" class="message-num">{{ store.userInformation.likeAllNumber<100? store.userInformation.likeAllNumber : '99+' }}</span></span></li>
    </ul>
    <div :class="{liColor: store.messageMenu === 5}" @click="jumpPage(5)" class="msg-setting" @mouseover="msgSettingHoverFlag = true" @mouseleave="msgSettingHoverFlag = false">
        <img :src="msgSettingHoverFlag||store.messageMenu===5 ?'../img/消息设置蓝.png' :  '../img/消息设置.png'">
        <span class="active" >消息设置</span>
    </div>
  </div>

</template>

<script setup>
import { ref } from 'vue';
import {useGlobalStore} from "../store/store";

const msgSettingHoverFlag = ref(false);
const store= useGlobalStore();

//跳转页面
function jumpPage(messageMenu){

    if(messageMenu!==store.messageMenu)
      store.setMessageMenu(messageMenu,true);
}

</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.aside-container{
  position: absolute;
  padding-top: 15px;
  top: 56px;
  left: clamp(10.5vw, 2.4vw + 7.2%, 34vw);
  width: 9.5vw;
  height: 100vh;
  background-color: rgba(255,255,255,0.7);

  .title{
    display: flex;
    justify-content: center;
    user-select: none;
    img{
        display: inline-block;
        transform: translateY(18px);
        width: 14px;
        height: 10.5px;
        margin-right: 9.5px;
    }
    span{
        position: relative;
        top: 14px;
        left: 0.5px;
        font-size: 14px;
        font-weight: 600;
    }
  }

 ul{
    position: relative;
    width: 100px;
    top: 37.5px;
    left: 37.5px;
    user-select: none;
    li{
        width: 100px;
        font-weight: 600;
        color: #6b757b;
    }

    li::marker{
        font-size: 18px;
    }

    .active{
        position: relative;
        display: block;
        top: -2px;
        color: #6b757b;
        cursor: pointer;
        left: 2.5px;
        font-size: 14px;
        line-height: 40px;
    }
    
    li:hover{
        color: #2faee3;
        .active{
            color: #2faee3;
        }
    }
   

 }

 .msg-setting{
    
    position: relative;
    width: 116px;
    left: 24px;
    top: 40px;
    user-select: none;
    
img{
    position: relative;
    width: 12px;
    height: 12px;
    top: -5px;
    left: -3px;

}

.active{
    position: relative;
    font-weight: 600;
    top: -5px;
    left: 4.5px;
    text-decoration: none;
    font-size: 14px;
    color: #6b757b;
    line-height: 40px;
  }
  
}

.msg-setting:hover .active{
    color: #2faee3;
}
    
}


.liColor{
    color: #2faee3 !important;
    .active{
        color: #2faee3 !important;
    }
}

.message-num{
    position: absolute;
    background-color: #fb7299;
    line-height: 16px;
    height: 16px;
    font-size: 12px;
    border-radius: 10px;
    width: 28px;
    top: 12px;
    right: 11px;
    text-align: center;
    color: #fff;
}

</style>

