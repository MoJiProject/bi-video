<template>
  <div v-if="store.token" class="account-page" :style="{backgroundImage: 'url('+backgroundImgSrc[bIndex]+')'}">
      <accountHead class="account-head"/>
      <div class="account-body-container">
        <accountAside/>
        <accountBody/>
      </div>
  </div>
</template>

<script setup>
import accountAside from "./accountAside.vue";
import accountBody from "./accountBody.vue";
import accountHead from "./accountHead.vue";
import {useGlobalStore} from "../store/store";
import { useBodyLayout } from "../composables/useBodyLayout";

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
const store = useGlobalStore();

useBodyLayout({ overflowY: 'hidden' });

function handlePopState(){
  const urlParams = new URLSearchParams(window.location.search);
  store.setAccountMenu(parseInt(urlParams.get("accountMenu")) || 1,false);
}

onMounted(async()=>{
  
  document.title = "个人中心 - 青芒视频";
  const accountMenu = parseInt(new URLSearchParams(window.location.search).get("accountMenu"));
  if(!accountMenu)
    store.setAccountMenu(1,true);
  else
    store.setAccountMenu(accountMenu,false);

  window.addEventListener('popstate', handlePopState);
})

onBeforeUnmount(()=>{
  window.removeEventListener('popstate', handlePopState);
})

//获取用户ip
// token 由 /auth/login 返回后存在本地，这里不再向后端要 IP + token。
async function getUserIp(){
  store.setUserIp("0.0.0.0");
}
</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.account-page{
  position: relative;
  width: 100%;
  min-height: 100vh;
  background-size: cover;

    .account-head{
     position: relative;
     z-index: 100;
     height: 64px; 
    }
    .account-body-container{
        position: relative;
        z-index: 5;
        margin-top: 50px;
        display: flex;
        justify-content: center;
        align-items: flex-start;
        flex-wrap: wrap;
        gap: 20px;
        padding: 0 var(--page-pad);
    }
}

</style>