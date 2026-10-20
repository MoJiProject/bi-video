<template>
  <div v-if="data.length" class="home-videos" :class="{ 'list-layout': layout === 'list' }">
    <homeVideoCard v-for="video in data" :key="video.id" :video="video"/>
  </div>
  <div v-else class="no-data">
    <img src="/img/home_nodata.svg" alt="">
    <div>暂无数据</div>
  </div>
</template>

<script setup>
import homeVideoCard from '@/components/homeVideoCard.vue';

defineProps({
  data: { type: Array, required: true },
  layout: { type: String, default: 'grid' },
});
</script>

<style scoped>
.home-videos {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(193px, 1fr));
  gap: 20px 16px;
}
.no-data {
  display: flex;
  align-items: center;
  flex-direction: column;
  padding: 24px;
  color: #8D9794;
}
.no-data img { width: 140px; }
.list-layout { grid-template-columns: minmax(0, 1fr); }
.list-layout :deep(.vcard) { flex-direction: row; }
.list-layout :deep(.vcard-cover) { width: 240px; flex-shrink: 0; }
.list-layout :deep(.vcard-body) { flex: 1; }
@media (max-width: 600px) {
  .list-layout :deep(.vcard-cover) { width: 45%; }
}
</style>
