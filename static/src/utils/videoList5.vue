<template>
  <div class="video-list5">
    <div class="video-item" v-for="video in data" :key="video.videoId">
      <video5 :video="video" />
    </div>
    <div class="no-data" v-if="data.length === 0">
      <img src="/img/home_nodata.svg" />
      <div v-if="store.userId !== null && store.userId == userId">
        你还没有发布视频内容
      </div>
      <div v-else>空间主人还没投过视频内容，这里什么也没有...</div>
    </div>
  </div>
</template>

<script setup>
import video5 from "./video5.vue";
import { useGlobalStore } from "@/store/store";
defineProps({
  data: {
    type: Array,
    required: true,
  },
});
const store = useGlobalStore();
const userId =
  parseInt(new URL(window.location).searchParams.get("userId")) || null;
</script>

<style lang="scss" scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.video-list5 {
  position: relative;
  top: 6.5px;
  column-gap: 21px;
  display: grid;
  grid-template-columns: repeat(3, 327.94px);
  .video-item {
    height: 262.5px;
  }
  .no-data {
    user-select: none;
    width: 1090px;
    height: 37.5vh;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    img {
      width: 140px;
      height: 140px;
    }
    div {
      margin-top: 6px;
      font-size: 14px;
      line-height: 20px;
      font-weight: 400;
      color: #9499a0;
    }
  }
}

@media (max-width: 1150px) {
  .video-list5 {
    grid-template-columns: repeat(2, 327.94px);
  }
}
@media (max-width: 795px) {
  .video-list5 {
    grid-template-columns: repeat(1, 327.94px);
  }
}
</style>
