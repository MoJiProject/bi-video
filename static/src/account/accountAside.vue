<template>
  <div class="account-aside">
      <div class="title">个人中心</div>
      <div class="menu-item" :class="{active:store.accountMenu===1}" @click="changeAccountMenu(1)">
       <span>
          <img src="../img/修改.png">
          <img src="../img/修改白.png"> 
       </span>
        个人信息</div>  
      <div class="menu-item" :class="{active:store.accountMenu===2}" @click="changeAccountMenu(2)">
        <span>
          <img src="../img/头像.png"> 
          <img src="../img/头像白.png"> 
        </span>
        我的头像</div>  
      <div class="menu-item" :class="{active:store.accountMenu===3}" @click="changeAccountMenu(3)">
       <span>
          <img src="../img/背景图片.png"> 
          <img src="../img/背景图片白.png"> 
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
  width: 150px;
  height: 500px;
  background-color: rgba(225, 226, 229,0.7);
  text-align: center;
  line-height: 50px;
  font-size: 16px;
  color: #181818;
  border: 1px solid #e1e2e5;
  border-radius: 5px 0 0 5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, .14);
  user-select: none;

    .title{
    display: block;
    width: 150px;
    height: 50px;
    text-align: center;
    line-height: 50px;
    font-size: 16px;
    color: #181818;
    cursor: default;
    border-bottom: 1px solid rgba(171, 171, 171, 0.7);
    }

    .menu-item{
      display: flex;
      padding: 0px 25px 0px 10px;
      justify-content: space-around;
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
      background-color: rgba(0, 161, 215, 0.7) !important;
      color: white !important;

      img:nth-child(1){
        opacity: 0;
      }
      img:nth-child(2){
        opacity: 1;
      }
    }
}

</style>