<template>
  <div v-if="data.length" class="home-videos" :class="{ 'list-layout': layout === 'list' }">
    <homeVideoCard v-for="video in data" :key="video.id" :video="video"/>
  </div>
  <AppEmptyState v-else compact title="暂无视频" description="有新的视频时，会在这里展示"/>
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
  grid-template-columns: repeat(auto-fill, minmax(min(100%, 193px), 1fr));
  gap: 20px 16px;
}
.list-layout { grid-template-columns: minmax(0, 1fr); }
.list-layout :deep(.vcard) { flex-direction: row; }
.list-layout :deep(.vcard-cover) { width: 240px; flex-shrink: 0; }
.list-layout :deep(.vcard-body) { flex: 1; min-width: 0; }
@media (max-width: 600px) {
  .list-layout :deep(.vcard-cover) { width: 45%; }
}
</style>
