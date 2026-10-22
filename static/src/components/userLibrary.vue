<template>
  <section class="library" :class="{ 'library--logged-out': !store.token }" :aria-label="title">
    <header class="library-head">
      <h1><span class="library-icon" aria-hidden="true">
        <svg v-if="isHistory" viewBox="0 0 24 24"><path d="M3 11a9 9 0 1 1 2.6 7.4M3 4v7h7M12 7v5l3 2"/></svg>
        <svg v-else viewBox="0 0 24 24"><rect x="3" y="3" width="18" height="18" rx="5"/><path d="m10 8 6 4-6 4Z"/></svg>
      </span>{{ title }}
        <span v-if="!isHistory && store.token" class="total">{{ total }}</span>
      </h1>
      <div v-if="store.token" class="head-actions">
        <template v-if="isHistory">
          <el-switch :model-value="recordHistory" aria-label="记录浏览历史" :loading="savingPreference" :disabled="preferenceLoading || !!preferenceError" @change="saveRecording"/>
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
        <div class="tools" :class="{ 'tools--managing': managing }">
          <template v-if="!managing">
            <div class="library-search-row">
              <el-input v-model="keyword" class="library-search" placeholder="搜索标题或创作者昵称" maxlength="100" clearable aria-label="搜索标题或创作者昵称"
                @keyup.enter="search" @clear="search">
                <template #append><el-button @click="search">搜索</el-button></template>
              </el-input>
            </div>
            <el-button class="date-toggle" :class="{ 'is-active': showDates || timeFilter !== 'all' }" :aria-expanded="showDates" @click="showDates = !showDates">
              <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="5" width="18" height="16" rx="3"/><path d="M7 3v4M17 3v4M3 11h18"/></svg>
              时间筛选 <span aria-hidden="true">{{ showDates ? '⌃' : '⌄' }}</span>
            </el-button>
            <el-button class="clear-button" :disabled="busy" @click="clearAll">清空{{ isHistory ? '历史' : '稍后再看' }}</el-button>
          </template>
          <template v-else>
            <el-checkbox :model-value="allSelected" :indeterminate="partlySelected" :disabled="busy || !rows.length" @change="selectAll">全选</el-checkbox>
            <span class="selection-count">已选择 <b>{{ selected.length }}</b> 条{{ isHistory ? '历史记录' : '稍后再看' }}</span>
            <el-button type="danger" :disabled="busy || !selected.length" @click="removeSelected">删除</el-button>
          </template>
          <el-button class="manage" :disabled="busy" @click="toggleManagement">{{ managing ? '退出管理' : '批量管理' }}</el-button>
        </div>
        <div v-if="showDates && !managing" class="date-tools">
          <div class="date-presets">
            <el-button v-for="option in dateOptions" :key="option.value" :type="timeFilter === option.value ? 'primary' : ''"
              :plain="timeFilter === option.value" :disabled="busy" @click="chooseTime(option.value)">{{ option.label }}</el-button>
          </div>
          <el-date-picker v-model="customDates" type="daterange" value-format="YYYY-MM-DD" range-separator="至"
            start-placeholder="开始日期" end-placeholder="结束日期" aria-label="自定义日期范围"
            popper-class="library-date-picker" :disabled="busy" @change="chooseCustomDates"/>
        </div>
      </div>
      <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon>
        <el-button text type="danger" :disabled="loading" @click="retry">重新加载</el-button>
      </el-alert>
      <loadingIndicator v-if="loading && !rows.length"/>
      <div class="library-content" :class="{ timeline: isHistory }">
        <section v-for="group in groups" :key="group.label" class="video-group">
          <h2 v-if="isHistory" class="group-title">{{ group.label }}<span>{{ group.videos.length }}</span></h2>
          <div class="videos">
            <article v-for="video in group.videos" :key="video.id" class="library-video content-action-surface" :class="{ selected: selected.includes(video.id) }">
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
              <div class="video-heading content-card-actions">
                <a v-if="video.available !== false" :href="videoUrl(video)" target="_blank" rel="noopener noreferrer" :title="video.title" class="video-title" @click="selectInstead($event, video.id)">
                  <template v-for="(part, index) in highlight(video.title)" :key="index"><mark v-if="part.match">{{ part.text }}</mark><template v-else>{{ part.text }}</template></template>
                </a>
                <span v-else class="video-title unavailable-title">视频已失效</span>
                <ContentMoreMenu v-if="!managing" :disabled="busy" :label="`${video.title}的记录操作`">
                  <el-dropdown-item class="is-danger" :disabled="busy" @click="remove([video.id])">
                    {{ isHistory ? '删除记录' : '移出稍后再看' }}
                  </el-dropdown-item>
                </ContentMoreMenu>
              </div>
              <a v-if="video.ownerId && video.available !== false" :href="`/home?userId=${video.ownerId}`" class="author" target="_blank" rel="noopener noreferrer" @click="selectInstead($event, video.id)">
                <img src="/img/author-badge-default.png" alt="创作者"/>
                <template v-for="(part, index) in highlight(video.ownerNickname)" :key="index"><mark v-if="part.match">{{ part.text }}</mark><template v-else>{{ part.text }}</template></template>
              </a>
              <p class="entry-time">{{ isHistory ? '观看于' : '添加于' }} {{ displayTime(isHistory ? video.lastWatchedAt : video.addedAt) }}</p>
            </article>
          </div>
        </section>
      </div>
      <AppEmptyState v-if="!loading && !error && !rows.length"
        :kind="appliedKeyword || timeFilter !== 'all' ? 'search' : 'empty'"
        :title="appliedKeyword || timeFilter !== 'all' ? '没有符合条件的记录' : isHistory ? '还没有观看记录' : '稍后再看列表为空'"
        :description="appliedKeyword || timeFilter !== 'all' ? '可调整关键词或时间筛选条件' : isHistory ? '观看视频后会显示记录' : '添加的视频会显示在这里'"/>
      <div ref="loadAnchor" class="load-more">
        <el-button v-if="hasMore" :loading="loading" :disabled="busy" @click="load">加载更多</el-button>
        <span v-else-if="!loading && !error && rows.length">没有更多记录了</span>
      </div>
    </template>
    <AppEmptyState v-else kind="login" :title="`登录后查看${title}`"
      action-text="立即登录" @action="store.loginDialogVisible = true"/>
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
import ContentMoreMenu from './ContentMoreMenu.vue';

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
.library { box-sizing: border-box; width: 100%; min-width: 0; max-width: var(--page-max); margin: auto; padding: 32px var(--page-pad) 48px; color: var(--ink); }
.library *, .library *::before, .library *::after { box-sizing: border-box; }
.library--logged-out { min-height: calc(100dvh - var(--head-h)); background: var(--fill-soft); }
.library--logged-out > .app-empty-state { max-width: 560px; margin: clamp(24px, 6vh, 64px) auto; }
.library-head, .library-head h1, .head-actions, .date-presets { display: flex; align-items: center; gap: 12px; }
.library-head { justify-content: space-between; flex-wrap: wrap; gap: 18px; margin-bottom: 24px; padding-bottom: 24px; border-bottom: 1px solid var(--line); }
.library-head h1 { flex-wrap: wrap; min-width: 0; margin: 0; font-size: clamp(24px, 2.4vw, 32px); font-weight: 600; letter-spacing: -.03em; }
.library-icon { display: grid; flex: none; place-items: center; width: 48px; height: 48px; border: 1px solid var(--brand-light-8); border-radius: 14px; background: var(--brand-light-9); color: var(--brand-hover); }
.library-icon svg { width: var(--icon-lg); height: var(--icon-lg); fill: none; stroke: currentColor; stroke-width: 1.6; stroke-linecap: round; stroke-linejoin: round; }
.head-actions { min-width: 0; padding: 8px 12px; border: 1px solid var(--line); border-radius: 12px; background: #fff; font-size: 13px; color: var(--ink-2); flex-wrap: wrap; }
.head-actions .el-select { width: 140px; }
.head-actions :deep(.el-select__wrapper) { box-shadow: none; }
.total { padding: 4px 10px; border: 1px solid var(--line); border-radius: 8px; background: #fff; font-size: 13px; color: var(--ink-2); font-weight: 400; letter-spacing: 0; font-variant-numeric: tabular-nums; }
.control-container { position: sticky; top: calc(var(--head-h) + 12px); z-index: var(--z-sticky); padding: 18px; background: #fff; border: 1px solid var(--line); border-radius: 16px; box-shadow: 0 4px 20px rgba(29, 64, 43, .035); }
.tools { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; }
.library-search-row { flex: 0 0 100%; min-width: 0; }
.tools .library-search { width: 340px; max-width: 100%; min-width: 0; }
.tools > .el-button { flex: none; width: auto; max-width: 100%; min-width: 0; min-height: 40px; height: auto; margin: 0; padding: 8px 14px; border-radius: 10px; line-height: 1.5; white-space: normal; }
.library-search :deep(.el-input__wrapper) { min-height: 40px; padding-left: 14px; border-radius: 10px 0 0 10px; background: var(--fill-soft); box-shadow: 0 0 0 1px var(--line) inset; }
.library-search :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px var(--brand) inset; }
.library-search :deep(.el-input-group__append) { padding: 0 16px; border-radius: 0 10px 10px 0; background: var(--fill-soft); color: var(--brand-hover); }
.date-toggle svg { flex: none; width: var(--icon-sm); height: var(--icon-sm); fill: none; stroke: currentColor; stroke-width: 1.5; stroke-linecap: round; stroke-linejoin: round; }
.date-toggle svg { margin-right: 6px; }
.date-toggle.is-active, .tools > .manage { border-color: var(--brand-light-7); background: var(--brand-light-9); color: var(--brand-hover); }
.tools > .clear-button { color: var(--ink-2); }
.tools > .clear-button:hover { color: var(--el-color-danger); border-color: var(--el-color-danger-light-7); background: var(--el-color-danger-light-9); }
.tools :deep(.el-button + .el-button), .date-tools :deep(.el-button + .el-button) { margin-left: 0; }
.selection-count { min-width: 0; color: var(--ink-2); font-size: 14px; overflow-wrap: anywhere; }
.selection-count b { color: var(--brand-hover); font-weight: 600; }
.date-tools { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--line); }
.date-presets { min-width: 0; flex-wrap: wrap; gap: 8px; }
.date-presets .el-button { min-height: 34px; padding: 7px 12px; border-radius: 8px; font-size: 12px; }
.date-tools :deep(.el-date-editor) { flex: none; width: 320px; min-width: 0; max-width: 100%; box-sizing: border-box; }
.date-tools :deep(.el-range-input) { min-width: 0; }
.library > .el-alert { margin-top: 16px; border-radius: 12px; }
.library-content { padding-top: 24px; }
.video-group { position: relative; margin-bottom: 28px; }
.group-title { display: flex; align-items: center; gap: 10px; margin: 0 0 16px; color: var(--ink); font-size: 16px; font-weight: 600; }
.group-title::before { content: ''; flex: none; height: 16px; width: 4px; border-radius: 4px; background: var(--brand); }
.group-title::after { content: ''; flex: 1; height: 1px; margin-left: 6px; background: var(--line); }
.group-title > span { padding: 2px 7px; border-radius: 6px; background: #fff; color: var(--ink-3); font-size: 11px; font-weight: 400; }
.videos { display: grid; grid-template-columns: repeat(auto-fill, minmax(min(100%, 240px), 1fr)); gap: 20px; }
.library-video { min-width: 0; padding: 8px 8px 14px; border: 1px solid var(--line); border-radius: 16px; background: #fff; }
.library-video.selected { border-color: var(--brand); background: var(--brand-light-9); box-shadow: 0 0 0 1px var(--brand); }
.cover { position: relative; overflow: hidden; border-radius: 10px; aspect-ratio: 16 / 9; background: var(--fill); }
.cover a, .select-cover { display: block; width: 100%; height: 100%; padding: 0; border: 0; cursor: pointer; }
.cover img { display: block; width: 100%; height: 100%; object-fit: cover; }
.cover a:focus-visible, .select-cover:focus-visible { outline: 3px solid var(--brand); outline-offset: -3px; }
.video-title:focus-visible, .author:focus-visible { outline: 2px solid var(--brand); outline-offset: 2px; }
.selected .cover { outline: 2px solid var(--brand); outline-offset: -2px; }
.video-checkbox { position: absolute; top: 8px; left: 8px; height: 28px; padding: 4px 6px; border-radius: 7px; background: rgba(255, 255, 255, .95); z-index: 1; }
.video-overlay { position: absolute; bottom: 0; left: 0; right: 0; display: flex; justify-content: space-between; gap: 8px; padding: 20px 8px 8px; color: #fff; font-size: 12px; background: linear-gradient(transparent, #0009); pointer-events: none; }
.progress { position: absolute; left: 0; bottom: 0; height: 3px; background: var(--brand); pointer-events: none; }
.video-heading { display: flex; align-items: flex-start; gap: 6px; margin: 12px 4px 0; }
.video-title { flex: 1; min-width: 0; min-height: 44px; color: var(--ink); font-size: 14px; font-weight: 500; line-height: 22px; text-decoration: none; overflow-wrap: anywhere; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.video-heading .content-more { margin-top: -2px; }
.video-title:hover, .author:hover { color: var(--brand); }
.author { display: block; margin: 8px 4px 0; color: var(--ink-2); font-size: 12px; line-height: 18px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; text-decoration: none; }
.author img { width: 16px; height: 16px; vertical-align: middle; margin-right: 4px; }
.entry-time { margin: 8px 4px 0; color: var(--ink-3); font-size: 11px; line-height: 1.6; overflow-wrap: anywhere; font-variant-numeric: tabular-nums; }
mark { color: var(--brand); background: none; }
.unavailable { display: grid; place-items: center; height: 100%; color: var(--ink-3); font-size: 13px; }
.unavailable-title { color: var(--ink-3); }
.load-more { display: flex; align-items: center; justify-content: center; min-height: 48px; margin-top: 20px; color: var(--ink-3); font-size: 12px; }
.load-more .el-button { min-width: 160px; min-height: 40px; border-radius: 10px; }
@media (max-width: 768px) {
  .control-container { position: static; }
}
@media (max-width: 640px) {
  .library { padding-top: 20px; }
  .library-head { gap: 14px; margin-bottom: 16px; padding-bottom: 18px; }
  .library-head h1 { gap: 10px; font-size: 24px; }
  .library-icon { width: 40px; height: 40px; border-radius: 12px; }
  .head-actions { max-width: 100%; gap: 8px; padding: 6px 10px; font-size: 12px; }
  .control-container { padding: 12px; border-radius: 14px; box-shadow: none; }
  .tools { gap: 8px; }
  .selection-count { font-size: 13px; }
  .date-tools :deep(.el-date-editor) { max-width: 100%; }
  .library-content { padding-top: 20px; }
  .videos { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px 12px; }
  .library-video { padding: 6px 6px 12px; border-radius: 13px; }
  .cover { border-radius: 8px; }
  .video-heading { margin-top: 10px; }
  .video-title { font-size: 13px; }
  .video-overlay { font-size: 11px; flex-wrap: wrap; gap: 2px; padding: 16px 6px 6px; }
}
@media (max-width: 480px) { .videos { grid-template-columns: minmax(0, 1fr); } }
@media (prefers-reduced-motion: no-preference) {
  .cover img { transition: transform .2s; }
  .cover a:hover img { transform: scale(1.035); }
  .library-video { transition: border-color .18s, box-shadow .18s; }
  .library-video:hover { border-color: var(--brand-light-5); box-shadow: 0 4px 18px rgba(29, 64, 43, .05); }
}
</style>

<style>
.library-date-picker { --el-color-primary: var(--brand); --el-color-primary-light-9: var(--brand-light-9); }
@media (max-width: 640px) {
  .library-date-picker .el-date-range-picker { width: min(360px, calc(100vw - 24px)); min-width: 0; }
  .library-date-picker .el-date-range-picker__body { min-width: 0; max-height: 70dvh; overflow-y: auto; }
  .library-date-picker .el-date-range-picker__content { box-sizing: border-box; float: none; width: 100%; padding: 12px; }
  .library-date-picker .el-date-range-picker__content.is-left { border-right: 0; border-bottom: 1px solid var(--line); }
}
</style>
