<template>
  <div v-if="store.userId!== null" class="message-page" :style="{backgroundImage: 'url('+backgroundImgSrc[bIndex]+')'}">
    <div class="message-head">
    <headComponent :head2-flag="true"/>
    </div>
  <asideComponent/>
  <div v-if="store.messageMenu === 1" class="message-body">
    <whisper/>
  </div>
  <div v-else-if="store.messageMenu === 2" class="message-body">
    <reply/>
  </div>
  <div v-else-if="store.messageMenu === 3" class="message-body">
    <at/>
  </div>
  <div v-else-if="store.messageMenu === 4" class="message-body">
    <love/>
  </div>
  <div v-else-if="store.messageMenu === 5" class="message-body">
    <config/>
  </div>
  </div>
</template>

<script setup>
import headComponent from '../components/mainHead.vue';
import asideComponent from './asideComponent.vue';
import whisper from './whisper.vue';
import reply from './reply.vue';
import at from './at.vue';
import love from './love.vue';
import config from './config.vue';
import {useGlobalStore} from "../store/store";
import { onMounted, watch } from 'vue';
import {ChecklLogin,getUserIp} from '../api/user/index';

const store = useGlobalStore();
const backgroundImgSrc=[
  '/background.jpg',
  '/background2.png',
  '/background3.jpg',
  '/background4.jpg',
  '/background5.jpg',
  '/background6.jpg',
  '/background7.png',
  '/background8.jpg',
];
let bIndex=Math.floor(Math.random()*backgroundImgSrc.length);

onMounted(async()=>{
  
  document.body.style.overflowY = 'hidden';
  document.title = "消息中心-哔哩哔哩弹幕视频网 - (゜- ゜)つロ 干杯~";
  localStorage.setItem('backgroundModel', parseInt(localStorage.getItem('backgroundModel')) || 0);
  const urlParams = new URLSearchParams(window.location.search);
  store.setMessageMenu(parseInt(urlParams.get("messageMenu")) || 1,true);
  window.addEventListener('popstate', () => {
  const urlParams = new URLSearchParams(window.location.search);
  store.setMessageMenu(parseInt(urlParams.get("messageMenu")) || 1,false);
  });
  await getUserIpF();
  await ChecklLoginF();
})

//检查是否登录
async function ChecklLoginF(){

    ChecklLogin(store.userIp).then(response=>{
    if (response.data.code === 1) {
        store.setUserId(response.data.data.id);
        store.setUserInformation(response.data.data);
    } else {
        window.location.href = "./";
    }
    })
}

//获取用户ip
async function getUserIpF(){

   await getUserIp().then(response=>{
    if(response.data.code === 1)
    store.setUserIp(response.data.data.userIp);
    store.setToken(response.data.data.token);
    })

}

watch(()=>store.userInformation,()=>{
  
  const backgroundModel=parseInt(localStorage.getItem('backgroundModel')) || 0;

  if(backgroundModel===1)
  {
    document.documentElement.style.setProperty('--background-color', 'rgb(255,255,255,0.7)');
    document.documentElement.style.setProperty('--line-color', 'rgb(179, 179, 179)');
  }
  else
  {
    document.documentElement.style.setProperty('--background-color', 'rgb(255,255,255,1)');
    document.documentElement.style.setProperty('--line-color', '#e5e9ef');
  }
},{deep:true})


 


</script>

<style lang="scss" scoped>

*{
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.message-page{
  position: relative;
  width: 102%;
  height: 100vh;
  background-position: 0px 50px;
  background-repeat: no-repeat;
  background-size: cover;
}

.message-head {
  top: 0px;
  width: 101%;
  height: 64px;
  left: -13px;
  position: fixed;
  background-color: white;
  box-shadow: 2px 0px 4px #d3d3d3;
  z-index: 999;
}
.message-body {
  position: relative;
}



</style>