<template>
  <div class="video-list8">
    <div class="video-item" v-for="video in data" :key="video.collects.id">
      <video8
        :video="video.selectVideoDto"
        :collect="video.collects"
        :batchControls="batchControls"
        :searchContent="searchContent"
      />
    </div>
  </div>
</template>

<script setup>
import video8 from "./video8.vue";
defineProps({
  data: {
    type: Array,
    required: true,
  },
  batchControls: {
    type: Boolean,
    required: true,
  },
  searchContent: {
    type: String,
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

.video-list8 {
  position: relative;
  display: grid;
  grid-template-columns: repeat(4, 258px);
  grid-gap: 48.5px 16px;
  .video-item {
    height: 223.13px;
  }
}
@media (max-width: 1340px) {
  .video-list8 {
    grid-template-columns: repeat(3, 258px);
  }
}

@media (max-width: 1050px) {
  .video-list8 {
    grid-template-columns: repeat(2, 258px);
  }
}
@media (max-width: 780px) {
  .video-list8 {
    grid-template-columns: repeat(1, 258px);
  }
}
</style>
