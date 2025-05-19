<template>
  <div class="background-container" @mousemove="handleMouseMove">
    <div
  class="background-image"
  :style="{
    backgroundPositionX: (store.mouseX * 0.1 - 200) + 'px',
    opacity: store.mouseX < 476.67
      ? (0.6 + 0.5 * (1 - Math.abs(store.mouseX - 238.33) / 238.33)).toFixed(2)
      : 0
  }"
    ></div>

    <div
      class="background-image2"
      :style="{
        backgroundPositionX: (store.mouseX * 0.1 - 200) + 'px',
        opacity: store.mouseX >= 476.67 && store.mouseX < 953.33
          ? (0.6 + 0.5 * (1 - Math.abs(store.mouseX - 715) / 238.33)).toFixed(2)
          : 0
      }"
    ></div>

    <div
      class="background-image3"
      :style="{
        backgroundPositionX: (store.mouseX * 0.1 - 200) + 'px',
        opacity: store.mouseX >= 953.33 && store.mouseX <= 1430
          ? (0.6 + 0.5 * (1 - Math.abs(store.mouseX - 1191.67) / 238.33)).toFixed(2)
          : 0
      }"
    ></div>
    <div class="content">
      <img
        src="../img/BILIBILI_LOGO.png"
        alt="logo"
        height="45px"
        class="logo"
        style="transform: translate(-25px, 69px)"
      />
    </div>
  </div>
</template>

<script>
import {useGlobalStore} from "../store/store";

export default {
  name: "banner",
 
  setup() {

    const store = useGlobalStore();

    function handleMouseMove(event) {
      store.setMouseX(event.clientX);
    }


    return {
      handleMouseMove,
      store,
      
    };
  },
};
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  height: 100%;
  overflow: hidden; /* 防止滚动条 */
}

.background-container {
  position: absolute;
  width: 1500px;
  height: 160px;
  z-index: inherit;
  overflow: hidden; /* 隐藏超出容器的内容 */
  transform: translate(0px, -70px);
}

.background-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 110%; /* 背景图宽度，根据实际情况调整 */
  height: 165px; /* 背景图高度与容器一致 */
  background-image: url("../img/background.png"); /* 替换为你的背景图 URL */
  background-size: cover; /* 确保背景图覆盖整个区域 */
  background-repeat: no-repeat; /* 背景图不重复 */
  background-position-y: 70%;
  transition: opacity 0.3s linear,transform 0.1s linear; /* 平滑过渡效果 */
}

.background-image2 {
  position: absolute;
  top: 0;
  left: 0;
  width: 110%; /* 背景图宽度，根据实际情况调整 */
  height: 165px; /* 背景图高度与容器一致 */
  background-image: url("../img/background2.png"); /* 替换为你的背景图 URL */
  background-size: cover; /* 确保背景图覆盖整个区域 */
  background-repeat: no-repeat; /* 背景图不重复 */
  background-position-y: 60%;
  transition: opacity 0.3s linear,transform 0.1s linear; /* 平滑过渡效果 */
}


.background-image3 {
  position: absolute;
  top: 0;
  left: 0;
  width: 110%; /* 背景图宽度，根据实际情况调整 */
  height: 165px; /* 背景图高度与容器一致 */
  background-image: url("../img/background3.jpg"); /* 替换为你的背景图 URL */
  background-size: cover; /* 确保背景图覆盖整个区域 */
  background-repeat: no-repeat; /* 背景图不重复 */
  background-position-y: 90%;
  transition: opacity 0.3s linear,transform 0.1s linear; /* 平滑过渡效果 */
}

.content {
  position: relative;
  z-index: 1; /* 确保内容层在背景图之上 */
}

.logo {
  margin-left: 135px;
  margin-top: 15px;
}
</style>
