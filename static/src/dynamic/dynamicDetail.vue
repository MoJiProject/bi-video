<template>
  <main class="detail">
    <loadingIndicator v-if="loading"/>
    <template v-else-if="dynamic">
      <homePost :dynamic="dynamic" @changed="load"/>
      <postComments :post-id="id" @count="dynamic.commentCount = $event"/>
    </template>
    <el-empty v-else description="动态不存在或无权查看"/>
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
import postComments from './postComments.vue';
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
.detail { box-sizing: border-box; width: 100%; max-width: var(--page-max); margin: auto; padding: 24px var(--page-pad); }
.detail :deep(.home-post > p) { max-width: var(--col-narrow); }
.discussion { padding: 20px 24px; background: #fff; border-top: 1px solid #eff3f2; }
.discussion a { color: #126b58; text-decoration: none; }
.discussion p { margin: 0; color: #8d9794; font-size: 13px; }
</style>
