<template>
  <main class="detail">
    <header class="detail-heading">
      <a href="/dynamic"><span aria-hidden="true">←</span> 返回动态</a>
      <h1>动态详情</h1>
    </header>
    <loadingIndicator v-if="loading"/>
    <template v-else-if="dynamic">
      <div class="dynamic-posts"><homePost :dynamic="dynamic" detail @changed="load" @comment-count="dynamic.commentCount = $event"/></div>
    </template>
    <AppEmptyState v-else kind="unavailable" title="暂时无法查看这条动态" description="动态可能已被删除，或作者设置了访问权限"/>
  </main>
</template>
<script setup>
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { get } from '@/services/http';
import { useGlobalStore } from '@/store/store';
import homePost from '@/home/homePost.vue';
import loadingIndicator from '@/components/loadingIndicator.vue';
const store = useGlobalStore();
const dynamic = ref(null);
const loading = ref(true);
const route = useRoute();
const id = ref(null);
let generation = 0;
async function load() {
  const current = ++generation;
  loading.value = true;
  dynamic.value = null;
  try {
    if (!Number.isSafeInteger(id.value) || id.value <= 0) throw new Error('动态 ID 不正确');
    const result = await get(`/post/${id.value}`);
    if (current === generation) dynamic.value = result;
  } catch (error) { if (current === generation) ElMessage.error(error.message); }
  finally { if (current === generation) loading.value = false; }
}
watch([() => route.query.dynamicId, () => store.userId], ([value]) => { id.value = Number(value); load(); }, { immediate: true });
watch(() => store.deleteFlag, value => { if (value === id.value) { generation++; dynamic.value = null; } });
</script>
<style scoped>
.detail { --el-color-primary: #126b58; box-sizing: border-box; display: grid; gap: 18px; width: 100%; min-width: 0; max-width: var(--page-max); margin: auto; padding: 28px var(--page-pad) 48px; color: var(--ink); }
.detail-heading { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 16px; padding-bottom: 20px; border-bottom: 1px solid var(--line); }
.detail-heading > a { display: inline-flex; align-items: center; gap: 8px; min-height: 36px; color: #126b58; font-size: 13px; text-decoration: none; }
.detail-heading > a:focus-visible { outline: 2px solid #126b58; outline-offset: 3px; }
.detail-heading h1 { margin: 0; font-size: 24px; font-weight: 600; }
@media (max-width: 640px) {
  .detail { gap: 14px; padding-top: 18px; padding-bottom: 32px; }
  .detail-heading { padding-bottom: 16px; }
  .detail-heading h1 { font-size: 20px; }
}
</style>
<style lang="scss" scoped src="./dynamicPost.scss"></style>
