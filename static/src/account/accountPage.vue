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
import { ChecklLogin } from '../api/user/index';
import { onMounted } from "vue";

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

onMounted(async()=>{
  
  document.title = "个人中心 - 青芒视频";
  document.body.style.overflowY = 'hidden';
  document.body.style.display = 'flex';
  document.body.style.justifyContent = 'center';
  const accountMenu = parseInt(new URLSearchParams(window.location.search).get("accountMenu"));
  if(!accountMenu)
    store.setAccountMenu(1,true);
  else
    store.setAccountMenu(accountMenu,false);

  window.addEventListener('popstate', () => {
  const urlParams = new URLSearchParams(window.location.search);
  store.setAccountMenu(parseInt(urlParams.get("accountMenu")) || 1,false);
  });

  await getUserIp();
  await ChecklLoginF();
})


//检查是否登录
async function ChecklLoginF(){

ChecklLogin(store.userIp).then(response=>{
if (response.data.code === 1) {
    store.setUserId(response.data.data.id);
    store.setUserInformation(response.data.data);
} else {
    store.setUserId(null);
    window.location.href = "../";
}
})
}

//获取用户ip
// token 由 /auth/login 返回后存在本地，这里不再向后端要 IP + token。
async function getUserIp(){
  store.setUserIp("");
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
  left: -11px;
  width: 100.5%;
  height: 100vh;
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
        align-items: center;
    }
}

</style>