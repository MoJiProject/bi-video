<template>
  <section class="library">
    <h1>{{ isHistory ? '观看历史' : '稍后观看' }}</h1>
    <template v-if="store.token">
      <div class="tools">
        <el-input v-model="keyword" placeholder="搜索视频" clearable @keyup.enter="reload" @clear="reload"/>
        <el-button @click="reload">搜索</el-button>
        <el-button type="danger" :disabled="busy" @click="clearAll">清空</el-button>
      </div>
      <loadingIndicator v-if="loading && !rows.length"/>
      <div class="videos">
        <article v-for="video in rows" :key="video.id">
          <homeVideoCard :video="video"/>
          <p v-if="isHistory">{{ video.lastWatchedAt?.replace('T', ' ') }} · 已看 {{ video.progressSeconds || 0 }} 秒</p>
          <el-button text type="danger" :disabled="busy" @click="remove(video.id)">移除</el-button>
        </article>
      </div>
      <el-empty v-if="!loading && !rows.length" description="暂无数据"/>
      <el-button v-if="hasMore" :loading="loading" @click="load">加载更多</el-button>
    </template>
    <el-empty v-else description="请先登录"><el-button @click="store.loginDialogVisible = true">登录</el-button></el-empty>
  </section>
</template>
<script setup>
import { computed, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { userContentApi } from '@/api/product';
import { del } from '@/services/http';
import { useGlobalStore } from '@/store/store';
import homeVideoCard from './homeVideoCard.vue';
import loadingIndicator from './loadingIndicator.vue';
const props = defineProps({ kind: { type: String, required: true } });
const store = useGlobalStore();
const isHistory = computed(() => props.kind === 'history');
const rows = ref([]);
const keyword = ref('');
const cursorId = ref(null);
const cursorTime = ref(null);
const hasMore = ref(false);
const loading = ref(false);
const busy = ref(false);
let generation = 0;
async function load() {
  const current = generation;
  loading.value = true;
  try {
    const fetch = isHistory.value ? userContentApi.history : userContentApi.watchLater;
    const page = await fetch({ cursorId: cursorId.value, cursorTime: cursorTime.value, keyword: keyword.value.trim(), size: 20 });
    if (current !== generation) return;
    const ids = new Set(rows.value.map(row => row.id));
    rows.value.push(...page.records.filter(row => !ids.has(row.id)));
    cursorId.value = page.cursorId;
    cursorTime.value = page.cursorTime || null;
    hasMore.value = page.hasMore;
  } catch (error) { if (current === generation) ElMessage.error(error.message); }
  finally { if (current === generation) loading.value = false; }
}
function reload() {
  generation++;
  rows.value = [];
  cursorId.value = null;
  cursorTime.value = null;
  hasMore.value = false;
  if (store.token) load();
}
async function remove(id) {
  busy.value = true;
  try {
    if (isHistory.value) await userContentApi.deleteHistory(id);
    else await del(`/watch-later/${id}`);
    rows.value = rows.value.filter(row => row.id !== id);
  } catch (error) { ElMessage.error(error.message); }
  finally { busy.value = false; }
}
async function clearAll() {
  const confirmed = await ElMessageBox.confirm('确定清空全部记录？', '清空', { type: 'warning' })
    .then(() => true, () => false);
  if (!confirmed) return;
  busy.value = true;
  try {
    if (isHistory.value) await userContentApi.clearHistory();
    else await userContentApi.clearWatchLater();
    reload();
  } catch (error) { ElMessage.error(error.message); }
  finally { busy.value = false; }
}
watch([() => store.token, () => props.kind], reload, { immediate: true });
</script>
<style scoped>
.library { box-sizing: border-box; width: 100%; max-width: var(--page-max); margin: auto; padding: 32px var(--page-pad); }
.tools { display: flex; gap: 12px; margin-bottom: 24px; }
.tools .el-input { max-width: 300px; }
.videos { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 24px; }
article p { font-size: 12px; color: #8D9794; }
</style>
