<template>
  <div class="not-found-container">
        <div class="not-found-header">
            <main-head :head2-flag="true"/>
        </div>
        <div class="not-found-content">
            <div><img src="../img/404.png"></div>
            <div class="not-found-title">啊叻？视频不见了？</div>
            <div class="not-found-desc">视频内容已被UP主删除，视频无法观看，敬请谅解。</div>
            <a href="./">前往首页</a>
        </div>
  </div>  
</template>

<script setup>
import mainHead from "@/components/mainHead.vue";

</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.not-found-container{
  .not-found-header{
    position: relative;
    top: 0;
    width: 103%;
    left: -21px;
    background-color: white;
    height: 64px;
    z-index: 1000;
    box-shadow: 0 2px 4px #00000014;
  }
  .not-found-content{
    width: 980px;
    margin: 0 auto;
    background: white;
    border-radius: 10px;
    padding: 70px 0;
    display: flex;
    justify-content: center;
    flex-direction: column;
    background-position: center;
    background-repeat: no-repeat;
    position: relative;
    overflow: hidden;
    text-align: center;

      img{
        width: 160px;
        height: 100px;
        vertical-align: middle;
        margin-bottom: 40px;
      }
      .not-found-title{
        font-size: 26px;
        color: #18191C;
        line-height: 26px;
        font-weight: bolder;
        padding: 0 0 12px 0;
      }
      .not-found-desc{
        font-size: 14px;
        color: #61666D;
        line-height: 21px;
      }
      a{
        position: relative;
        left: 50%;
        transform: translateX(-50%);
        width: 120px;
        height: 38px;
        line-height: 32px;
        display: flex;
        justify-content: center;
        align-items: center;
        margin-top: 60px;
        background: #00AEEC;
        color: white;
        border-radius: 2px;
        font-size: 16px;
        transition: all 0.3s ease;
        cursor: pointer;
      }
      a:hover{
        background-color: #19a4e0;
      }
  }
}

</style>