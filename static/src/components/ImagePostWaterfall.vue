<template>
  <div class="image-post-waterfall">
    <article v-for="item in posts" :key="item.id" class="image-post-card">
      <a :href="`/dynamicDetail?dynamicId=${item.id}`" :aria-label="item.title?.trim() || '查看图片动态'">
        <img v-if="!failedImages.has(item.imageList[0])" :src="item.imageList[0]"
          :alt="item.title?.trim() || '动态图片'" loading="lazy" decoding="async"
          @error="failedImages.add(item.imageList[0])">
        <div v-else class="image-error" role="img" aria-label="图片加载失败">图片加载失败</div>
        <div class="image-post-copy">
          <h3 v-if="item.title?.trim()" :title="item.title">{{ item.title }}</h3>
          <div class="image-post-meta">
            <time :datetime="item.createdAt" :title="item.createdAt?.replace('T', ' ')">
              {{ item.createdAt?.replace('T', ' ').slice(0, 16) }}
            </time>
            <span class="image-post-likes" :title="`${item.likeCount ?? 0} 个赞`" :aria-label="`${item.likeCount ?? 0} 个赞`">
              <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/></svg>
              {{ item.likeCount ?? 0 }}
            </span>
          </div>
        </div>
      </a>
    </article>
  </div>
</template>

<script setup>
import { ref } from 'vue';

defineProps({
  posts: { type: Array, required: true },
});

const failedImages = ref(new Set());
</script>

<style scoped>
.image-post-waterfall { min-width: 0; column-width: 220px; column-gap: 16px; font-family: var(--el-font-family); }
.image-post-card { box-sizing: border-box; display: inline-block; break-inside: avoid; width: 100%; margin: 0 0 16px; overflow: hidden; border: 1px solid var(--line); border-radius: 14px; background: #fff; vertical-align: top; }
.image-post-card > a { display: block; color: var(--ink); text-decoration: none; }
.image-post-card > a:focus-visible { outline: 2px solid var(--brand); outline-offset: -2px; border-radius: 14px; }
.image-post-card img { display: block; width: 100%; height: auto; }
.image-error { display: grid; place-items: center; min-height: 160px; padding: 12px; background: var(--fill-soft); color: var(--ink-3); font-size: 12px; }
.image-post-copy { padding: 12px; }
.image-post-copy h3 { display: -webkit-box; margin: 0 0 10px; overflow: hidden; -webkit-box-orient: vertical; -webkit-line-clamp: 2; color: var(--ink); font-size: 14px; font-weight: 600; line-height: 1.5; overflow-wrap: anywhere; }
.image-post-card a:hover h3 { color: var(--brand-hover); }
.image-post-meta { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 6px 10px; color: var(--ink-3); font-size: 12px; line-height: 20px; }
.image-post-meta time { min-width: 0; overflow-wrap: anywhere; }
.image-post-likes { display: inline-flex; align-items: center; gap: 4px; margin-left: auto; font-variant-numeric: tabular-nums; }
.image-post-likes svg { flex: none; width: 16px; height: 16px; fill: none; stroke: currentColor; stroke-width: 1.6; stroke-linecap: round; stroke-linejoin: round; }
@media (max-width: 640px) {
  .image-post-waterfall { column-width: 140px; column-gap: 12px; }
  .image-post-card { margin-bottom: 12px; border-radius: 12px; }
  .image-post-copy { padding: 10px; }
  .image-post-copy h3 { font-size: 13px; }
  .image-post-meta { font-size: 11px; }
}
</style>
