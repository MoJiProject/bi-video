<template>
  <div class="video-list">
    <div class="video-item" v-for="video in data" :key="video.videoId">
      <videoC :video="video" />
    </div>

    <div class="no-data" v-if="data.length === 0">
      <img src="../img/home_nodata.svg" />
      <div>暂无数据</div>
    </div>
  </div>
</template>

<script setup>
import videoC from "@/utils/video";

defineProps({
  data: {
    type: Array,
    required: true,
  },
});
</script>

<style lang="scss" scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.video-list {
  display: grid;
  grid-template-columns: repeat(5, 193px);
  column-gap: 16px;
  row-gap: 23px;

  .video-item {
    height: 173.5px;
  }

  .no-data {
    user-select: none;
    width: 100%;
    grid-column: 1 / -1;
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
  .video-list {
    grid-template-columns: repeat(4, 193px);
  }
}

@media (max-width: 940px) {
  .video-list {
    grid-template-columns: repeat(3, 193px);
  }
}

@media (max-width: 730px) {
  .video-list {
    grid-template-columns: repeat(2, 193px);
  }
}
</style>
