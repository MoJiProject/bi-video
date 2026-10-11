<template>
  <div v-if="store.token" class="message-page" :style="{backgroundImage: 'url('+backgroundImgSrc[bIndex]+')'}">
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
import { onBeforeUnmount, onMounted, watch } from 'vue';
import { useBodyLayout } from '../composables/useBodyLayout';

const store = useGlobalStore();
const backgroundImgSrc=[
  '/img/page-bg-1.png',
  '/img/page-bg-2.png',
  '/img/page-bg-3.png',
  '/img/page-bg-4.png',
  '/img/page-bg-5.png',
  '/img/page-bg-6.png',
  '/img/page-bg-7.png',
  '/img/page-bg-8.png',
];
let bIndex=Math.floor(Math.random()*backgroundImgSrc.length);

useBodyLayout({ overflowY: 'hidden' });

function handlePopState(){
  const urlParams = new URLSearchParams(window.location.search);
  store.setMessageMenu(parseInt(urlParams.get("messageMenu")) || 1,false);
}

onMounted(async()=>{

  document.title = "消息中心 - 青芒视频";
  localStorage.setItem('backgroundModel', parseInt(localStorage.getItem('backgroundModel')) || 0);
  const urlParams = new URLSearchParams(window.location.search);
  store.setMessageMenu(parseInt(urlParams.get("messageMenu")) || 1,true);
  window.addEventListener('popstate', handlePopState);
})

onBeforeUnmount(()=>{
  window.removeEventListener('popstate', handlePopState);
  document.documentElement.style.removeProperty('--background-color');
  document.documentElement.style.removeProperty('--line-color');
})

//获取用户ip
// token 由 /auth/login 返回后存在本地，这里不再向后端要 IP + token。
async function getUserIp(){
  store.setUserIp("0.0.0.0");
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
  width: 100%;
  min-height: 100vh;
  background-position: 0px 50px;
  background-repeat: no-repeat;
  background-size: cover;

}


.message-head {
  top: 0px;
  left: 0;
  width: 100%;
  height: 64px;
  position: fixed;
  background-color: white;
  box-shadow: 2px 0px 4px #d3d3d3;
  z-index: 999;
}
.message-body {
  position: relative;
}



</style>