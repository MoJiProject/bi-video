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
import {ChecklLogin,getUserIp} from '../api/user/index';
import { onMounted } from "vue";

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
const store = useGlobalStore();

onMounted(async()=>{
  
  document.title = "个人中心-哔哩哔哩弹幕视频网 - (゜- ゜)つロ 干杯~";
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
    store.setUserId(null);
    window.location.href = "../";
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
        margin-top: 116px;
        display: flex;
        justify-content: center;
        align-items: center;
    }
}

</style>