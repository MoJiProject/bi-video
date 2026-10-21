<template>
  <div class="message-page" :style="{backgroundImage: backgroundModel === 1 ? 'url('+backgroundImgSrc[bIndex]+')' : 'none'}">
    <headComponent class="header-stack-search message-page-head" :head2-flag="true"/>
    <main v-if="store.token" class="message-workspace">
    <asideComponent/>
    <section class="message-main" aria-label="消息内容">
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
    </section>
    </main>
    <div v-else class="message-empty">
      <AppEmptyState kind="login" title="登录后查看消息" action-text="登录" @action="store.setLoginDialogVisible(true)"/>
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
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useBodyLayout } from '../composables/useBodyLayout';

const store = useGlobalStore();
const backgroundModel = ref(parseInt(localStorage.getItem('backgroundModel')) || 0);
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
  
  backgroundModel.value=parseInt(localStorage.getItem('backgroundModel')) || 0;

  if(backgroundModel.value===1)
  {
    document.documentElement.style.setProperty('--background-color', 'rgb(255,255,255,0.7)');
    document.documentElement.style.setProperty('--line-color', 'rgb(179, 179, 179)');
  }
  else
  {
    document.documentElement.style.setProperty('--background-color', 'rgb(255,255,255,1)');
    document.documentElement.style.setProperty('--line-color', '#e5e9ef');
  }
},{deep:true,immediate:true})


 


</script>

<style lang="scss" scoped src="./messageLayout.scss"></style>