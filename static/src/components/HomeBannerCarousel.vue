<template>
  <div class="home-banner" role="region" aria-label="首页Banner">
    <el-carousel height="100%" trigger="click" :interval="5000" :autoplay="banners.length > 1"
      :arrow="banners.length > 1 ? 'always' : 'never'" :indicator-position="banners.length > 1 ? '' : 'none'">
      <el-carousel-item v-for="(banner, index) in banners" :key="banner.id">
        <component :is="banner.linkUrl ? 'a' : 'div'" class="banner-slide" :href="banner.linkUrl || undefined"
          :target="banner.linkUrl ? '_blank' : undefined" :rel="banner.linkUrl ? 'noopener noreferrer' : undefined"
          :aria-label="banner.content || '首页Banner ' + (index + 1)">
          <img :src="banner.imageUrl" :alt="banner.content || '首页Banner ' + (index + 1)" />
          <span v-if="banner.content" class="banner-content">{{ banner.content }}</span>
        </component>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup>
defineProps({ banners: { type: Array, required: true } });
</script>

<style scoped>
.home-banner { position: absolute; inset: 0; z-index: 2; }
.home-banner :deep(.el-carousel) { height: 100%; }
.banner-slide { position: relative; display: block; width: 100%; height: 100%; overflow: hidden; color: white; }
.banner-slide img { display: block; width: 100%; height: 100%; object-fit: cover; }
.banner-slide:focus-visible { outline: 2px solid var(--brand); outline-offset: -2px; }
.banner-content {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  padding: 32px var(--page-pad) 36px;
  background: linear-gradient(transparent, rgba(0, 0, 0, .75));
  font-size: 16px;
  line-height: 1.6;
  overflow-wrap: anywhere;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
@media (max-width: 640px) {
  .banner-content { font-size: 14px; }
}
</style>
