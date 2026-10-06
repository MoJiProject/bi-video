<template>
  <div class="not-found-container">
        <div class="not-found-header">
            <main-head :head2-flag="true"/>
        </div>
        <div class="not-found-content">
            <div><img src="/img/404.png"></div>
            <div class="not-found-title">啊叻？视频不见了？</div>
            <div class="not-found-desc">视频内容已被创作者删除，视频无法观看，敬请谅解。</div>
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
    position: sticky;
    top: 0;
    left: 0;
    width: 100%;
    background-color: white;
    height: 64px;
    z-index: 1000;
    box-shadow: 0 2px 4px #00000014;
  }
  .not-found-content{
    max-width: var(--col-mid);
    margin: 0 auto;
    background: white;
    border-radius: 10px;
    padding: 70px var(--page-pad);
    display: flex;
    justify-content: center;
    flex-direction: column;
    background-position: center;
    background-repeat: no-repeat;
    position: relative;
    overflow: hidden;
    text-align: center;
    width: 100%;
    box-sizing: border-box;

      img{
        width: 160px;
        height: 100px;
        vertical-align: middle;
        margin-bottom: 40px;
      }
      .not-found-title{
        font-size: 26px;
        color: #1C2321;
        line-height: 26px;
        font-weight: bolder;
        padding: 0 0 12px 0;
      }
      .not-found-desc{
        font-size: 14px;
        color: #5C6664;
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
        background: #0FA68E;
        color: white;
        border-radius: 2px;
        font-size: 16px;
        transition: all 0.3s ease;
        cursor: pointer;
      }
      a:hover{
        background-color: #17A08B;
      }
  }
}

</style>