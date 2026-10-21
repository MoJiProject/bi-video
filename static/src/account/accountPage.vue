<template>
  <div class="account-page" :class="{ 'is-logged-out': !store.token }"
    :style="{backgroundImage: store.token ? 'url('+backgroundImgSrc[bIndex]+')' : 'none'}">
      <accountHead class="account-head"/>
      <div v-if="store.token" class="account-body-container">
        <accountAside/>
        <accountBody/>
      </div>
      <div v-else class="account-empty">
        <AppEmptyState kind="login" title="登录后管理个人资料" description="登录账号，完善资料并管理你的个人设置"
          action-text="立即登录" @action="store.loginDialogVisible = true"/>
      </div>
  </div>
</template>

<script setup>
import accountAside from "./accountAside.vue";
import accountBody from "./accountBody.vue";
import accountHead from "./accountHead.vue";
import {useGlobalStore} from "../store/store";
import { useBodyLayout } from "../composables/useBodyLayout";
import { onBeforeUnmount, onMounted } from "vue";

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

useBodyLayout({});

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
     height: var(--head-h);
    }
    .account-body-container{
        position: relative;
        z-index: 5;
        width: calc(100% - var(--page-pad) - var(--page-pad));
        margin: 40px auto 0;
        display: flex;
        justify-content: flex-start;
        align-items: flex-start;
        flex-wrap: nowrap;
        gap: 20px;
        padding: 0;
    }
}

.account-page.is-logged-out { background-color: var(--fill-soft); }
.account-empty {
  width: calc(100% - var(--page-pad) - var(--page-pad));
  max-width: 560px;
  margin: clamp(32px, 8vh, 88px) auto;
}

@media (max-width: 900px){
  .account-page{
    min-height: 100dvh;
    overflow-y: auto;
    background-position: center top;

    .account-body-container{
      flex-direction: column;
      width: calc(100% - var(--page-pad) - var(--page-pad));
      margin-top: 18px;
      gap: 12px;
    }
  }
}

</style>