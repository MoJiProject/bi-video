<template>
  <section class="library" :aria-label="title">
    <header class="library-head">
      <h1><img v-if="isHistory" src="/img/历史记录图标.png" alt=""/>{{ title }}
        <span v-if="!isHistory && store.token" class="total">· {{ total }}</span>
      </h1>
      <div v-if="store.token" class="head-actions">
        <template v-if="isHistory">
          <el-switch :model-value="recordHistory" :loading="savingPreference" :disabled="preferenceLoading || !!preferenceError" @change="saveRecording"/>
          <span>记录浏览历史</span>
          <el-button v-if="preferenceError" text type="danger" @click="loadRecording">重试读取设置</el-button>
        </template>
        <el-select v-else v-model="order" aria-label="添加顺序" :disabled="busy" @change="reload">
          <el-option label="最近添加" value="newest"/>
          <el-option label="最早添加" value="oldest"/>
        </el-select>
      </div>
    </header>
    <template v-if="store.token">
      <div class="control-container">
        <div class="tools">
          <template v-if="!managing">
            <el-button :aria-expanded="showDates" @click="showDates = !showDates">时间筛选 {{ showDates ? '⌃' : '⌄' }}</el-button>
            <el-input v-model="keyword" placeholder="搜索标题/UP主昵称" maxlength="100" clearable aria-label="搜索标题或UP主昵称"
              @keyup.enter="search" @clear="search">
              <template #append><el-button @click="search">搜索</el-button></template>
            </el-input>
            <el-button :disabled="busy" @click="clearAll">清空{{ isHistory ? '历史' : '稍后再看' }}</el-button>
          </template>
          <template v-else>
            <el-checkbox :model-value="allSelected" :indeterminate="partlySelected" :disabled="busy || !rows.length" @change="selectAll">全选</el-checkbox>
            <span class="selection-count">已经选择 {{ selected.length }} 条{{ isHistory ? '历史记录' : '稍后再看' }}</span>
            <el-button type="danger" :disabled="busy || !selected.length" @click="removeSelected">删除</el-button>
          </template>
          <el-button class="manage" :disabled="busy" @click="toggleManagement">{{ managing ? '退出管理' : '批量管理' }}</el-button>
        </div>
        <div v-if="showDates && !managing" class="date-tools">
          <el-button v-for="option in dateOptions" :key="option.value" :type="timeFilter === option.value ? 'primary' : ''"
            :plain="timeFilter === option.value" :disabled="busy" @click="chooseTime(option.value)">{{ option.label }}</el-button>
          <el-date-picker v-model="customDates" type="daterange" value-format="YYYY-MM-DD" range-separator="至"
            start-placeholder="开始日期" end-placeholder="结束日期" aria-label="自定义日期范围" :disabled="busy" @change="chooseCustomDates"/>
        </div>
      </div>
      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon>
        <el-button text type="danger" :disabled="loading" @click="retry">重新加载</el-button>
      </el-alert>
      <loadingIndicator v-if="loading && !rows.length"/>
      <div class="library-content" :class="{ timeline: isHistory }">
        <section v-for="group in groups" :key="group.label" class="video-group">
          <h2 v-if="isHistory" class="group-title">{{ group.label }}</h2>
          <div class="videos">
            <article v-for="video in group.videos" :key="video.id" class="library-video" :class="{ selected: selected.includes(video.id) }">
              <div class="cover">
                <a v-if="!managing && video.available !== false" :href="videoUrl(video)" target="_blank" rel="noopener noreferrer" :aria-label="video.title">
                  <img :src="video.coverUrl || '/img/cover-fallback.png'" :alt="video.title" loading="lazy" @error="coverFallback"/>
                </a>
                <button v-else-if="managing" type="button" class="select-cover" :aria-label="`${selected.includes(video.id) ? '取消选择' : '选择'} ${video.title}`"
                  :aria-pressed="selected.includes(video.id)" :disabled="busy" @click="toggleSelection(video.id)">
                  <img :src="video.coverUrl || '/img/cover-fallback.png'" :alt="video.title" loading="lazy" @error="coverFallback"/>
                </button>
                <div v-else class="unavailable">视频已失效</div>
                <el-checkbox v-if="managing" class="video-checkbox" :model-value="selected.includes(video.id)" :disabled="busy"
                  :aria-label="`选择 ${video.title}`" @change="toggleSelection(video.id)"/>
                <div v-if="video.available !== false" class="video-overlay">
                  <span v-if="!isHistory">播放 {{ formatCount(video.playCount) }} · 弹幕 {{ formatCount(video.danmakuCount) }}</span>
                  <span>{{ isHistory ? progressText(video) : duration(video.durationSeconds) }}</span>
                </div>
                <div v-if="video.progressSeconds && video.available !== false" class="progress" :style="{ width: `${progressPercent(video)}%` }"/>
              </div>
              <div class="video-heading">
                <a v-if="video.available !== false" :href="videoUrl(video)" target="_blank" rel="noopener noreferrer" :title="video.title" class="video-title" @click="selectInstead($event, video.id)">
                  <template v-for="(part, index) in highlight(video.title)" :key="index"><mark v-if="part.match">{{ part.text }}</mark><template v-else>{{ part.text }}</template></template>
                </a>
                <span v-else class="video-title unavailable-title">视频已失效</span>
                <el-button text type="danger" :disabled="busy" :aria-label="`删除 ${video.title}`" @click="remove([video.id])">删除</el-button>
              </div>
              <a v-if="video.ownerId && video.available !== false" :href="`/home?userId=${video.ownerId}`" class="author" target="_blank" rel="noopener noreferrer" @click="selectInstead($event, video.id)">
                <img src="/img/author-badge-default.png" alt="UP主"/>
                <template v-for="(part, index) in highlight(video.ownerNickname)" :key="index"><mark v-if="part.match">{{ part.text }}</mark><template v-else>{{ part.text }}</template></template>
              </a>
              <p class="entry-time">{{ isHistory ? '观看于' : '添加于' }} {{ displayTime(isHistory ? video.lastWatchedAt : video.addedAt) }}</p>
            </article>
          </div>
        </section>
      </div>
      <el-empty v-if="!loading && !error && !rows.length" :description="appliedKeyword || timeFilter !== 'all' ? '没有符合条件的记录' : isHistory ? '还没有观看记录' : '这里什么都没有哦~'"/>
      <div ref="loadAnchor" class="load-more">
        <el-button v-if="hasMore" :loading="loading" :disabled="busy" @click="load">加载更多</el-button>
        <span v-else-if="!loading && !error && rows.length">{{ isHistory ? '已经到底啦~' : '已经探索到底啦~' }}</span>
      </div>
    </template>
    <el-empty v-else description="请先登录"><el-button @click="store.loginDialogVisible = true">登录</el-button></el-empty>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { selectHistoryList, deleteHistory, deleteAllHistory } from '@/api/history';
import { selectWaitWatch, deleteWaitWatch, cleanAllWaitWatch } from '@/api/collect';
import { authApi } from '@/api/product';
import { put } from '@/services/http';
import { useGlobalStore } from '@/store/store';
import loadingIndicator from './loadingIndicator.vue';

const props = defineProps({ kind: { type: String, required: true } });
const store = useGlobalStore();
const isHistory = computed(() => props.kind === 'history');
const title = computed(() => isHistory.value ? '历史记录' : '稍后再看');
const rows = ref([]);
const total = ref(0);
const keyword = ref('');
const appliedKeyword = ref('');
const order = ref('newest');
const showDates = ref(false);
const timeFilter = ref('all');
const customDates = ref(null);
const dateOptions = [
  { value: 'all', label: '全部时间' }, { value: 'today', label: '今天' },
  { value: 'yesterday', label: '昨天' }, { value: 'week', label: '近一周' },
];
const managing = ref(false);
const selected = ref([]);
const cursorId = ref(null);
const cursorTime = ref(null);
const hasMore = ref(false);
const loading = ref(false);
const busy = ref(false);
const error = ref('');
const recordHistory = ref(true);
const preferenceLoading = ref(false);
const preferenceError = ref('');
const savingPreference = ref(false);
const loadAnchor = ref(null);
let generation = 0;
let observer;
let activeRequest = null;
const allSelected = computed(() => rows.value.length > 0 && selected.value.length === rows.value.length);
const partlySelected = computed(() => selected.value.length > 0 && !allSelected.value);

function localDate(date) {
  const pad = value => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}
function midnight(date) { return `${localDate(date)}T00:00:00`; }
function dateRange() {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const end = new Date(today);
  end.setDate(end.getDate() + 1);
  const start = new Date(today);
  if (timeFilter.value === 'all') return {};
  if (timeFilter.value === 'custom' && customDates.value?.length === 2) {
    const lastDay = new Date(`${customDates.value[1]}T00:00:00`);
    lastDay.setDate(lastDay.getDate() + 1);
    return { startTime: `${customDates.value[0]}T00:00:00`, endTime: midnight(lastDay) };
  }
  if (timeFilter.value === 'yesterday') {
    start.setDate(start.getDate() - 1);
    return { startTime: midnight(start), endTime: midnight(today) };
  }
  if (timeFilter.value === 'week') start.setDate(start.getDate() - 7);
  return { startTime: midnight(start), endTime: midnight(end) };
}

const groups = computed(() => {
  if (!isHistory.value) return [{ label: '稍后再看', videos: rows.value }];
  const labels = ['今天', '昨天', '近一周', '一周前', '一个月前', '一年前'];
  const buckets = labels.map(label => ({ label, videos: [] }));
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const before = (unit, value) => {
    const date = new Date(today);
    if (unit === 'day') date.setDate(date.getDate() - value);
    if (unit === 'month') date.setMonth(date.getMonth() - value);
    if (unit === 'year') date.setFullYear(date.getFullYear() - value);
    return date.getTime();
  };
  const boundaries = [today.getTime(), before('day', 1), before('day', 7), before('month', 1), before('year', 1)];
  for (const video of rows.value) {
    const time = new Date(video.lastWatchedAt).getTime();
    const index = boundaries.findIndex(boundary => time >= boundary);
    buckets[index < 0 ? 5 : index].videos.push(video);
  }
  return buckets.filter(group => group.videos.length);
});

async function load() {
  if (!store.token || loading.value || busy.value) return;
  const current = generation;
  const request = new AbortController();
  activeRequest = request;
  loading.value = true;
  error.value = '';
  try {
    const fetchPage = isHistory.value ? selectHistoryList : selectWaitWatch;
    const response = await fetchPage({
      cursorId: cursorId.value, cursorTime: cursorTime.value, keyword: appliedKeyword.value,
      order: order.value, size: 20, ...dateRange(),
    }, { signal: request.signal });
    if (current !== generation) return;
    const page = response.data.data;
    const ids = new Set(rows.value.map(row => row.id));
    rows.value.push(...page.records.filter(row => !ids.has(row.id)));
    cursorId.value = page.cursorId;
    cursorTime.value = page.cursorTime || null;
    hasMore.value = page.hasMore;
    if (!isHistory.value) total.value = page.total;
  } catch (failure) {
    if (current === generation && !request.signal.aborted) {
      error.value = failure.message || '加载失败，请重试';
      ElMessage.error(error.value);
    }
  } finally {
    if (current === generation) {
      loading.value = false;
      activeRequest = null;
    }
  }
}
function reload() {
  activeRequest?.abort();
  generation++;
  rows.value = [];
  selected.value = [];
  total.value = 0;
  cursorId.value = null;
  cursorTime.value = null;
  hasMore.value = false;
  error.value = '';
  loading.value = false;
  if (store.token) load();
}
function search() { appliedKeyword.value = keyword.value.trim(); reload(); }
function chooseTime(value) { timeFilter.value = value; customDates.value = null; reload(); }
function chooseCustomDates(value) { timeFilter.value = value?.length === 2 ? 'custom' : 'all'; reload(); }
function toggleManagement() { managing.value = !managing.value; selected.value = []; }
function selectAll(value) { selected.value = value ? rows.value.map(row => row.id) : []; }
function toggleSelection(id) {
  selected.value = selected.value.includes(id) ? selected.value.filter(value => value !== id) : [...selected.value, id];
}
function selectInstead(event, id) {
  if (!managing.value) return;
  event.preventDefault();
  if (!busy.value) toggleSelection(id);
}
async function confirm(message, caption) {
  return ElMessageBox.confirm(message, caption, { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消' })
    .then(() => true, () => false);
}
async function removeSelected() { await remove([...selected.value]); }
async function remove(ids) {
  if (busy.value || !ids.length) return;
  const token = store.token;
  const kind = props.kind;
  let attempted = false;
  busy.value = true;
  try {
    if (!await confirm(`确认要删除${ids.length === 1 ? '这条' : `选中的 ${ids.length} 条`}${title.value}？记录删除后将不可恢复。`, '删除记录')) return;
    if (store.token !== token || props.kind !== kind) return;
    // Requests stay within the server's batch limit even after many pages have loaded.
    for (let offset = 0; offset < ids.length; offset += 1000) {
      if (store.token !== token || props.kind !== kind) return;
      const chunk = ids.slice(offset, offset + 1000);
      attempted = true;
      await (kind === 'history' ? deleteHistory(chunk) : deleteWaitWatch(chunk));
      if (store.token !== token || props.kind !== kind) return;
      rows.value = rows.value.filter(row => !chunk.includes(row.id));
      selected.value = selected.value.filter(id => !chunk.includes(id));
      if (!isHistory.value) total.value = Math.max(0, total.value - chunk.length);
    }
    ElMessage.success('删除成功');
  } catch (failure) { ElMessage.error(failure.message || '删除失败，请重试'); }
  finally {
    busy.value = false;
    if (attempted || store.token !== token || props.kind !== kind) reload();
  }
}
async function clearAll() {
  if (busy.value) return;
  const token = store.token;
  const kind = props.kind;
  let attempted = false;
  busy.value = true;
  try {
    if (!await confirm(`确认要清空所有${title.value}？记录清空后将不可恢复。`, '清空记录')) return;
    if (store.token !== token || props.kind !== kind) return;
    attempted = true;
    await (kind === 'history' ? deleteAllHistory() : cleanAllWaitWatch());
    if (store.token !== token || props.kind !== kind) return;
    ElMessage.success('清空成功');
  } catch (failure) { ElMessage.error(failure.message || '清空失败，请重试'); }
  finally {
    busy.value = false;
    if (attempted || store.token !== token || props.kind !== kind) reload();
  }
}
async function loadRecording() {
  if (!store.token || !isHistory.value) return;
  const token = store.token;
  preferenceLoading.value = true;
  preferenceError.value = '';
  try {
    const profile = await authApi.me();
    if (store.token === token && isHistory.value) recordHistory.value = profile.recordHistory !== false;
  } catch (failure) {
    if (store.token === token && isHistory.value) {
      preferenceError.value = failure.message || '读取历史记录设置失败';
      ElMessage.error(preferenceError.value);
    }
  } finally { if (store.token === token) preferenceLoading.value = false; }
}
async function saveRecording(value) {
  if (savingPreference.value || preferenceLoading.value) return;
  const token = store.token;
  savingPreference.value = true;
  try {
    await put('/auth/privacy', { recordHistory: value });
    if (store.token === token) {
      recordHistory.value = value;
      ElMessage.success(value ? '已开启浏览历史记录' : '已暂停浏览历史记录');
    }
  } catch (failure) { if (store.token === token) ElMessage.error(failure.message || '设置保存失败'); }
  finally { if (store.token === token) savingPreference.value = false; }
}
function retry() { if (rows.value.length) load(); else reload(); }
function videoUrl(video) { return `/video?videoId=BV${video.id}`; }
function duration(value) {
  const seconds = Math.max(0, Math.floor(Number(value) || 0));
  const pad = number => String(number).padStart(2, '0');
  const hours = Math.floor(seconds / 3600);
  return `${hours ? `${hours}:` : ''}${pad(Math.floor(seconds / 60) % 60)}:${pad(seconds % 60)}`;
}
function progressPercent(video) {
  return video.durationSeconds > 0 ? Math.min(100, Math.max(0, (video.progressSeconds || 0) / video.durationSeconds * 100)) : 0;
}
function progressText(video) {
  return video.durationSeconds > 0 && video.progressSeconds >= video.durationSeconds
    ? '已看完' : `${duration(video.progressSeconds)} / ${duration(video.durationSeconds)}`;
}
function displayTime(value) { return value ? value.replace('T', ' ').slice(0, 19) : '时间未知'; }
function formatCount(value) {
  const number = Number(value) || 0;
  return number >= 10000 ? `${(number / 10000).toFixed(1).replace(/\.0$/, '')}万` : String(number);
}
function coverFallback(event) {
  event.target.onerror = null;
  event.target.src = '/img/cover-fallback.png';
}
function highlight(value) {
  const text = value || '';
  const query = appliedKeyword.value.toLocaleLowerCase();
  if (!query) return [{ text, match: false }];
  const parts = [];
  let position = 0;
  let index = text.toLocaleLowerCase().indexOf(query);
  while (index !== -1) {
    if (index > position) parts.push({ text: text.slice(position, index), match: false });
    parts.push({ text: text.slice(index, index + query.length), match: true });
    position = index + query.length;
    index = text.toLocaleLowerCase().indexOf(query, position);
  }
  if (position < text.length) parts.push({ text: text.slice(position), match: false });
  return parts;
}
watch([() => store.token, () => props.kind], () => {
  managing.value = false;
  keyword.value = '';
  appliedKeyword.value = '';
  timeFilter.value = 'all';
  customDates.value = null;
  preferenceLoading.value = false;
  savingPreference.value = false;
  preferenceError.value = '';
  reload();
  loadRecording();
}, { immediate: true });
watch(title, value => { document.title = value; }, { immediate: true });
watch(loadAnchor, async element => {
  observer?.disconnect();
  await nextTick();
  if (element) observer?.observe(element);
});
onMounted(() => {
  observer = new IntersectionObserver(entries => {
    if (entries.some(entry => entry.isIntersecting) && hasMore.value && !error.value) load();
  }, { rootMargin: '300px' });
  if (loadAnchor.value) observer.observe(loadAnchor.value);
});
onBeforeUnmount(() => { generation++; activeRequest?.abort(); observer?.disconnect(); });
</script>

<style scoped>
.library { box-sizing: border-box; width: 100%; max-width: var(--page-max); margin: auto; padding: 32px var(--page-pad); color: #18191c; }
.library-head, .library-head h1, .head-actions, .tools, .date-tools { display: flex; align-items: center; gap: 12px; }
.library-head { justify-content: space-between; flex-wrap: wrap; margin-bottom: 24px; }
.library-head h1 { margin: 0; font-size: 24px; font-weight: 500; }
.library-head h1 img { width: 28px; height: 28px; object-fit: contain; }
.head-actions { font-size: 14px; color: #61666d; flex-wrap: wrap; }
.head-actions .el-select { width: 140px; }
.total { font-size: 18px; color: #9499a0; }
.control-container { position: sticky; top: var(--header-height, 64px); z-index: 5; padding: 16px 0; background: var(--bg, #fff); border-bottom: 1px solid #e3e5e7; }
.tools, .date-tools { flex-wrap: wrap; }
.tools .el-input { width: 280px; max-width: 100%; }
.tools :deep(.el-button + .el-button), .date-tools :deep(.el-button + .el-button) { margin-left: 0; }
.manage { margin-left: auto; }
.selection-count { color: #61666d; font-size: 14px; }
.date-tools { margin-top: 16px; }
.date-tools :deep(.el-date-editor) { flex: 0 1 290px; max-width: 100%; box-sizing: border-box; }
.library-content { padding-top: 28px; }
.timeline { padding-left: 22px; }
.video-group { position: relative; margin-bottom: 32px; }
.timeline .video-group { border-left: 1px solid #e3e5e7; padding: 0 0 28px 28px; margin-bottom: 0; }
.group-title { margin: 0 0 22px; color: #61666d; font-size: 16px; font-weight: 500; }
.group-title::before { content: ''; position: absolute; left: -6px; top: 5px; height: 10px; width: 10px; border-radius: 50%; border: 1px solid #9499a0; background: #fff; }
.videos { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 28px 20px; }
.library-video { min-width: 0; border-radius: 8px; }
.cover { position: relative; overflow: hidden; border-radius: 8px; aspect-ratio: 16 / 9; background: #f1f2f3; }
.cover a, .select-cover { display: block; width: 100%; height: 100%; padding: 0; border: 0; cursor: pointer; }
.cover img { display: block; width: 100%; height: 100%; object-fit: cover; transition: transform .2s; }
.cover a:hover img { transform: scale(1.04); }
.selected .cover { outline: 2px solid #00aeec; outline-offset: -2px; }
.video-checkbox { position: absolute; top: 5px; left: 10px; z-index: 1; }
.video-overlay { position: absolute; bottom: 0; left: 0; right: 0; display: flex; justify-content: space-between; gap: 8px; padding: 20px 8px 8px; color: #fff; font-size: 12px; background: linear-gradient(transparent, #0009); pointer-events: none; }
.progress { position: absolute; left: 0; bottom: 0; height: 3px; background: #00aeec; pointer-events: none; }
.video-heading { display: flex; align-items: flex-start; gap: 4px; margin-top: 8px; }
.video-title { flex: 1; min-width: 0; min-height: 40px; color: #18191c; font-size: 14px; line-height: 20px; text-decoration: none; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.video-heading .el-button { flex-shrink: 0; height: 24px; padding: 2px 4px; }
.video-title:hover, .author:hover { color: #00aeec; }
.author { display: block; margin-top: 5px; color: #9499a0; font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; text-decoration: none; }
.author img { width: 16px; height: 16px; vertical-align: middle; margin-right: 4px; }
.entry-time { margin: 6px 0 0; color: #9499a0; font-size: 12px; }
mark { color: #00aeec; background: none; }
.unavailable { display: grid; place-items: center; height: 100%; color: #9499a0; }
.unavailable-title { color: #9499a0; }
.load-more { display: flex; justify-content: center; min-height: 48px; margin-top: 24px; color: #9499a0; font-size: 14px; }
@media (max-width: 640px) {
  .library { padding-top: 20px; }
  .library-head h1 { font-size: 21px; }
  .tools { gap: 8px; }
  .tools .el-input { width: 100%; order: 4; }
  .selection-count { flex: 1; }
  .timeline { padding-left: 6px; }
  .timeline .video-group { padding-left: 14px; }
  .videos { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 22px 12px; }
  .video-overlay { font-size: 10px; flex-wrap: wrap; gap: 2px; padding: 16px 6px 6px; }
}
@media (max-width: 360px) { .videos { grid-template-columns: 1fr; } }
</style>
