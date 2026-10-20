<template>
  <div class="account-aside">
      <div class="title">个人中心</div>
      <div class="menu-item" :class="{active:store.accountMenu===1}" @click="changeAccountMenu(1)">
       <span>
          <img src="/img/修改.png">
          <img src="/img/修改白.png"> 
       </span>
        个人信息</div>  
      <div class="menu-item" :class="{active:store.accountMenu===2}" @click="changeAccountMenu(2)">
        <span>
          <img src="/img/头像.png"> 
          <img src="/img/头像白.png"> 
        </span>
        我的头像</div>  
      <div class="menu-item" :class="{active:store.accountMenu===3}" @click="changeAccountMenu(3)">
       <span>
          <img src="/img/背景图片.png"> 
          <img src="/img/背景图片白.png"> 
       </span>
        我的背景</div>  
  </div>
</template>

<script setup>
import {useGlobalStore} from "../store/store";

const store = useGlobalStore();

//检查链接是否是当前页面
function changeAccountMenu(accountMenu){
    
if(store.accountMenu!==accountMenu)
  store.setAccountMenu(accountMenu,true);

}

</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.account-aside{
  position: relative;
  width: 170px;
  min-height: 500px;
  background-color: rgba(255, 255, 255,0.86);
  backdrop-filter: blur(14px);
  text-align: center;
  line-height: 50px;
  font-size: 16px;
  color: #1C2321;
  border: 1px solid rgba(224,229,227,0.72);
  border-radius: 18px;
  box-shadow: 0 18px 45px rgba(16, 41, 37, 0.08);
  user-select: none;
  flex-shrink: 0;
  overflow: hidden;

    .title{
    display: block;
    width: 100%;
    height: 50px;
    text-align: center;
    line-height: 50px;
    font-size: 16px;
    color: #1C2321;
    cursor: default;
    border-bottom: 1px solid rgba(171, 171, 171, 0.7);
    }

    .menu-item{
      display: flex;
      padding: 0px 20px 0px 16px;
      justify-content: flex-start;
      gap: 18px;
      align-items: center;
      font-size: 14px;
      transition: all 0.3s ease;
      cursor: pointer;

        span{
          display: flex;
          justify-content: center;
          align-items: center;
        }
        img{
          position: absolute;
          transition: all 0.3s ease;
          width: 16px;
          height: 16px;
        }
        img:nth-child(2){
          opacity: 0;
        }
    }
    .menu-item:hover{
     background-color: rgb(225, 228, 234, 0.7);
    }
    .active{
      background-color: rgba(14,156,133, 0.7) !important;
      color: white !important;

      img:nth-child(1){
        opacity: 0;
      }
      img:nth-child(2){
        opacity: 1;
      }
    }
}

@media (max-width: 900px){
  .account-aside{
    width: 100%;
    min-height: 0;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px;
    border-radius: 18px;
    overflow-x: auto;

    .title{
      display: none;
    }

    .menu-item{
      flex: 0 0 auto;
      min-width: 0;
      flex: 1 0 auto;
      height: 40px;
      padding: 0 14px;
      border-radius: 12px;
      justify-content: center;
      gap: 10px;
      line-height: 40px;
      white-space: nowrap;
    }
  }
}

@media (max-width: 420px) {
  .account-aside {
    gap: 4px;
    padding: 6px;
    .menu-item { padding: 0 8px; font-size: 12px; }
    .menu-item > span { flex: none; width: 16px; height: 16px; }
  }
}

</style>