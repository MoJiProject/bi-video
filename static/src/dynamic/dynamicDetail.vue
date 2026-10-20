<template>
  <main class="detail">
    <loadingIndicator v-if="loading"/>
    <template v-else-if="dynamic">
      <homePost :dynamic="dynamic"/>
      <section class="discussion">
        <a v-if="dynamic.videoId" :href="'/video?videoId=BV' + dynamic.videoId">前往原视频参与讨论 →</a>
        <p v-else>动态评论功能暂未开放</p>
      </section>
    </template>
    <el-empty v-else description="动态不存在或无权查看"/>
  </main>
</template>
<script setup>
import { onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { get } from '@/services/http';
import { useGlobalStore } from '@/store/store';
import homePost from '@/home/homePost.vue';
import loadingIndicator from '@/components/loadingIndicator.vue';
const store = useGlobalStore();
const dynamic = ref(null);
const loading = ref(true);
const id = Number(new URLSearchParams(window.location.search).get('dynamicId'));
onMounted(async () => {
  try {
    if (!Number.isSafeInteger(id) || id <= 0) throw new Error('动态 ID 不正确');
    dynamic.value = await get(`/post/${id}`);
  } catch (error) { ElMessage.error(error.message); }
  finally { loading.value = false; }
});
watch(() => store.deleteFlag, value => { if (value === id) dynamic.value = null; });
</script>
<style scoped>
.detail { box-sizing: border-box; width: 100%; max-width: var(--page-max); margin: auto; padding: 24px var(--page-pad); }
.detail :deep(.home-post > p) { max-width: var(--col-narrow); }
.discussion { padding: 20px 24px; background: #fff; border-top: 1px solid #eff3f2; }
.discussion a { color: #126b58; text-decoration: none; }
.discussion p { margin: 0; color: #8d9794; font-size: 13px; }
</style>
