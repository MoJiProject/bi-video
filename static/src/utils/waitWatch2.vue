<template>
    <div
      v-show="store.userId !== null"
      class="wait-watch"
      style="user-select: none;"
      @click="waitWatch(video)"
      @mouseover="waitFont = 1"
      @mouseleave="waitFont = 0"
    >
      <img
        v-show="video.waitWatch === 0"
        src="../img/稍后再看.png"
        style="
          width: 20px;
          height: 18px;
          opacity: 1 !important;
          margin-left: 4px;
        "
      />
      <img
        v-show="video.waitWatch === 1"
        src="../img/添加成功.png"
        style="
          width: 17px;
          height: 14px;
          opacity: 1 !important;
          margin-left: 5px;
        "
      />
      <span
        v-show="waitFont === 1 && video.waitWatch === 0"
        style="
          width: 110px;
          color: white;
          z-index: 10;
          font-size: 12px;
          transform: translate(28.5px, -0.5px);
          position: absolute;
        "
        >添加至稍后观看</span
      >
      <span
        v-show="waitFont === 1 && video.waitWatch === 1"
        style="
          width: 110px;
          color: white;
          z-index: 10;
          font-size: 12px;
          transform: translate(28.5px, -0.5px);
          position: absolute;
        "
        >已添加稍后观看</span
      >
    </div>
</template>

<script setup>
import {useGlobalStore} from "@/store/store";
import { ref } from "vue";
import { updateWaitWatch } from "@/api/video/index";
import { ElMessage } from "element-plus";

defineProps({
  video: {
    type: Object,
    required: true,
  },
});

const store = useGlobalStore();
const waitFont = ref(0);

async function waitWatch(video) {
  
  let dynamicDto={
      videoId: video.videoId,
      userId: store.userId,
  }
  updateWaitWatch(store.token,dynamicDto).then(res => {
    if (res.data.code === 1) {
        video.waitWatch= video.waitWatch === 0 ? 1 : 0;
    }
    else{
      ElMessage({
        message: res.data.msg,
        type: "info",
        plain: true,
        duration: 1700,
      });
    }
  });
}

</script>

<style scoped lang="scss">
*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}


</style>