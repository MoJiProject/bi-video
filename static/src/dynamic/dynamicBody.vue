<template>
  <main class="feed">
    <header class="feed-heading">
      <h1>动态</h1>
    </header>
    <section class="feed-layout" :class="{ 'is-public': !store.userId }">
      <aside v-if="store.userId" class="authors" aria-label="动态作者筛选">
        <div class="authors-heading"><h2>关注的创作者</h2><span>{{ authors.length }}</span></div>
        <nav class="author-list">
          <button type="button" :class="{ active: scope === 'following' && !targetUserId }"
                  :aria-pressed="scope === 'following' && !targetUserId" @click="selectAuthor(null)">
            <span class="author-symbol" aria-hidden="true"><svg viewBox="0 0 24 24"><circle cx="9" cy="8" r="3"/><path d="M3 20v-2a6 6 0 0 1 12 0v2M16 5a3 3 0 0 1 0 6M18 14a5 5 0 0 1 3 4v2"/></svg></span>
            <span class="author-name">全部关注</span>
          </button>
          <button type="button" :class="{ active: targetUserId === store.userId }"
                  :aria-pressed="targetUserId === store.userId" @click="selectAuthor(store.userId)">
            <span class="author-symbol" aria-hidden="true"><svg viewBox="0 0 24 24"><circle cx="12" cy="8" r="3"/><path d="M5 21v-2a7 7 0 0 1 14 0v2"/></svg></span>
            <span class="author-name">我的动态</span>
          </button>
          <button v-for="author in authors" :key="author.userId" type="button"
                  :class="{ active: targetUserId === author.userId }" :aria-pressed="targetUserId === author.userId"
                  @click="selectAuthor(author.userId)">
            <img :src="author.avatarUrl" alt=""><span class="author-name" :title="author.nickname">{{ author.nickname }}</span>
            <el-tag v-if="author.unreadCount" size="small" type="danger" round>{{ author.unreadCount }}</el-tag>
          </button>
        </nav>
      </aside>
      <section class="timeline" aria-label="动态时间线">
        <section v-if="store.token" class="composer" aria-labelledby="composer-heading">
          <header class="composer-heading">
            <h2 id="composer-heading">发布动态</h2>
          </header>
          <el-input v-model="title" class="composer-title" maxlength="20" show-word-limit aria-label="动态标题（选填）" placeholder="动态标题（选填）"/>
          <PostEditor ref="editor" v-model="content" class="composer-editor" :disabled="publishing"
            placeholder="输入动态内容…" :max-length="2000" @length="contentLength = $event"/>
          <div v-if="images.length" class="attachments">
            <div v-for="(image, index) in images" :key="image" class="attachment">
              <span class="attachment-order">{{ index + 1 }}</span>
              <el-image :src="image" :preview-src-list="images" preview-teleported fit="cover"/>
              <div class="image-actions">
                <el-button text :disabled="index === 0" @click="moveImage(index, -1)">前移</el-button>
                <el-button text :disabled="index === images.length - 1" @click="moveImage(index, 1)">后移</el-button>
                <el-button text type="danger" @click="images.splice(index, 1)">移除</el-button>
              </div>
            </div>
          </div>
          <div class="publish">
            <label class="image-upload" :class="{ disabled: uploading || images.length >= 9 }">
              <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="3" width="18" height="18" rx="4"/><circle cx="8" cy="8" r="1.5"/><path d="m3 17 5-5 4 4 4-6 5 7"/></svg>
              添加图片 <span>{{ images.length }}/9</span>
              <input type="file" multiple accept="image/png,image/jpeg,image/gif" aria-label="添加动态图片，最多九张" :disabled="uploading || images.length >= 9" @change="upload">
            </label>
            <div class="publish-options">
              <el-select v-model="visibility" class="visibility" aria-label="动态可见范围"><el-option label="公开" :value="0"/><el-option label="仅自己" :value="1"/></el-select>
              <el-button type="primary" :loading="publishing || uploading" @click="publish">发布动态</el-button>
            </div>
          </div>
        </section>
        <section class="timeline-controls" aria-labelledby="timeline-heading">
          <div class="timeline-heading"><h2 id="timeline-heading">动态列表</h2></div>
          <div class="filters">
            <el-radio-group v-model="scope" class="scope-tabs" aria-label="动态范围">
              <el-radio-button value="following" :disabled="!store.userId">关注动态</el-radio-button>
              <el-radio-button value="public">公开动态</el-radio-button>
            </el-radio-group>
            <el-radio-group v-model="type" class="type-tabs" aria-label="动态类型">
              <el-radio-button :value="1">全部</el-radio-button><el-radio-button :value="2">视频</el-radio-button><el-radio-button :value="3">图文</el-radio-button>
              <el-radio-button :value="4">图片</el-radio-button>
            </el-radio-group>
          </div>
        </section>
        <el-button v-if="scope === 'following' && unreadCount" class="new-posts" @click="reload">
          <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M20 7v5h-5M4 17v-5h5M6 7a7 7 0 0 1 12-1l2 6M4 12l2 6a7 7 0 0 0 12-1"/></svg>
          {{ unreadCount }} 条新动态，点击刷新
        </el-button>
        <el-alert v-if="reminderError" :title="reminderError" type="error" :closable="false" show-icon/>
        <ImagePostWaterfall v-if="type === 4" :posts="rows"/>
        <div v-else class="dynamic-posts">
          <homePost v-for="item in rows" :key="item.id" :dynamic="item" @changed="reload"/>
        </div>
        <loadingIndicator v-if="loading"/>
        <AppEmptyState v-if="!loading && !rows.length" title="暂无动态" description="可切换筛选条件查看其他动态"/>
        <el-button v-if="hasMore" class="load-more" :loading="loading" @click="load">加载更多动态</el-button>
        <p v-if="!loading && rows.length && !hasMore" class="feed-end">没有更多动态了</p>
      </section>
    </section>
  </main>
</template>
<script setup>
import { computed, onUnmounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { socialApi } from '@/api/product';
import { post } from '@/services/http';
import { useGlobalStore } from '@/store/store';
import homePost from '@/home/homePost.vue';
import loadingIndicator from '@/components/loadingIndicator.vue';
import PostEditor from '@/components/PostEditor.vue';
import ImagePostWaterfall from '@/components/ImagePostWaterfall.vue';

const store = useGlobalStore();
const rows = ref([]);
const authors = ref([]);
const scope = ref(store.userId ? 'following' : 'public');
const targetUserId = ref(null);
const type = ref(1);
const cursorId = ref(null);
const hasMore = ref(false);
const loading = ref(false);
const title = ref('');
const content = ref('');
const contentLength = ref(0);
const editor = ref(null);
const images = ref([]);
const visibility = ref(0);
const publishing = ref(false);
const uploading = ref(false);
const reminderError = ref('');
const unreadCount = computed(() => authors.value
  .filter(author => !targetUserId.value || author.userId === targetUserId.value)
  .reduce((total, author) => total + Number(author.unreadCount || 0), 0));
let generation = 0;
let active = true;
let reminderTimer;
async function load() {
  if (loading.value) return;
  const current = generation;
  loading.value = true;
  try {
    const page = await socialApi.posts({ targetUserId: targetUserId.value || undefined,
      followingOnly: scope.value === 'following', type: type.value, cursorId: cursorId.value, size: 20 });
    if (current !== generation) return;
    const ids = new Set(rows.value.map(item => item.id));
    rows.value.push(...page.records.filter(item => !ids.has(item.id)));
    cursorId.value = page.cursorId;
    hasMore.value = page.hasMore;
    return true;
  } catch (error) { if (current === generation) ElMessage.error(error.message); }
  finally { if (current === generation) loading.value = false; }
}
async function refreshAuthors() {
  if (!store.userId) return;
  const userId = store.userId;
  const result = [];
  let cursor;
  let more = true;
  while (more) {
    const page = await socialApi.authors({ cursorId: cursor });
    result.push(...page.records);
    cursor = page.cursorId;
    more = page.hasMore;
  }
  if (active && store.userId === userId) authors.value = result;
}
async function reload() {
  generation++; rows.value = []; cursorId.value = null; hasMore.value = false; loading.value = false;
  const current = generation;
  const loaded = await load();
  if (!loaded || current !== generation) return;
  if (!store.userId || scope.value !== 'following') return;
  try {
    await socialApi.read(targetUserId.value || undefined);
    await refreshAuthors();
  } catch (error) { reminderError.value = error.message; }
}
function selectAuthor(id) {
  targetUserId.value = id;
  scope.value = 'following';
}
async function upload(event) {
  const files = Array.from(event.target.files);
  if (files.length > 9 - images.value.length) { ElMessage.warning('最多上传 9 张图片'); event.target.value = ''; return; }
  uploading.value = true;
  try {
    for (const file of files) {
      const body = new FormData();
      body.append('file', file);
      const result = await post('/media/image', body);
      images.value.push(result.url);
    }
  } catch (error) { ElMessage.error(error.message); }
  finally { uploading.value = false; event.target.value = ''; }
}
function moveImage(index, direction) {
  const target = index + direction;
  if (target < 0 || target >= images.value.length) return;
  [images.value[index], images.value[target]] = [images.value[target], images.value[index]];
}
async function publish() {
  if (publishing.value || uploading.value) return;
  if (!contentLength.value && !images.value.length) { ElMessage.warning('请输入内容或添加图片'); return; }
  if (contentLength.value > 2000) { ElMessage.warning('动态正文不能超过 2000 字'); return; }
  publishing.value = true;
  try {
    await socialApi.createPost({ title: title.value, content: content.value, imageUrls: images.value, visibility: visibility.value });
    title.value = ''; content.value = ''; contentLength.value = 0; images.value = [];
    await reload();
    ElMessage.success('发布成功');
  } catch (error) { ElMessage.error(error.message); }
  finally { publishing.value = false; }
}
watch([scope, targetUserId, type], reload);
watch(scope, value => { if (value === 'public') targetUserId.value = null; });
watch(() => store.userId, async id => {
  clearInterval(reminderTimer);
  authors.value = []; targetUserId.value = null;
  scope.value = id ? 'following' : 'public';
  await reload();
  if (!id || store.userId !== id) return;
  try {
    await refreshAuthors();
    if (!active || store.userId !== id) return;
    reminderTimer = setInterval(async () => {
      try { await refreshAuthors(); reminderError.value = ''; }
      catch (error) { reminderError.value = error.message; }
    }, 30000);
  } catch (error) { reminderError.value = error.message; }
}, { immediate: true });
watch(() => store.deleteFlag, id => { rows.value = rows.value.filter(row => row.id !== id); });
onUnmounted(() => { active = false; generation++; clearInterval(reminderTimer); });
</script>
<style scoped>
.feed { --el-color-primary: #126b58; box-sizing: border-box; width: 100%; min-width: 0; max-width: var(--page-max); margin: auto; padding: 32px var(--page-pad) 56px; color: var(--ink); }
.feed *, .feed *::before, .feed *::after { box-sizing: border-box; }
.feed-heading { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 20px; margin-bottom: 28px; padding: 4px 0 24px; border-bottom: 1px solid var(--line); }
.feed-heading h1 { margin: 0; font-size: clamp(28px, 3vw, 38px); letter-spacing: -.04em; }
.write-button { display: inline-flex; flex: none; align-items: center; justify-content: center; gap: 8px; min-height: 42px; padding: 0 18px; border: 1px solid #126b58; border-radius: 12px; background: #126b58; color: #fff; font: inherit; font-size: 13px; cursor: pointer; }
.write-button:hover { background: #0d5545; }
.write-button svg, .image-upload svg, .new-posts svg { width: var(--icon-sm); height: var(--icon-sm); fill: none; stroke: currentColor; stroke-width: 1.6; stroke-linecap: round; stroke-linejoin: round; }
.feed-layout { display: grid; grid-template-columns: clamp(190px, 16vw, 240px) minmax(0, 1fr); align-items: start; gap: 24px; }
.feed-layout.is-public { grid-template-columns: minmax(0, 1fr); }
.authors { position: sticky; top: calc(var(--head-h) + 24px); min-width: 0; padding: 18px 12px 14px; border: 1px solid var(--line); border-radius: 18px; background: #fff; }
.authors-heading { display: flex; align-items: center; justify-content: flex-start; gap: 8px; min-height: 24px; padding: 0 4px 12px; }
.authors-heading h2 { min-width: 0; margin: 0; font-size: 14px; font-weight: 600; line-height: 22px; }
.authors-heading > span { box-sizing: border-box; display: inline-flex; flex: none; align-items: center; justify-content: center; min-width: 24px; height: 24px; margin: 0; padding: 0 7px; border-radius: 7px; background: var(--fill-soft); color: var(--ink-3); font-size: 12px; font-weight: 400; line-height: 1; font-variant-numeric: tabular-nums; }
.author-list { display: flex; flex-direction: column; gap: 5px; max-height: max(180px, calc(100dvh - var(--head-h) - 120px)); overflow-y: auto; overscroll-behavior: contain; scrollbar-width: thin; }
.author-list button { box-sizing: border-box; display: flex; flex: none; align-items: center; gap: 10px; width: 100%; min-width: 0; min-height: 48px; margin: 0; padding: 7px 10px; border: 1px solid transparent; border-radius: 10px; background: transparent; color: var(--ink-2); font-family: var(--el-font-family, sans-serif); font-size: 13px; font-weight: 400; line-height: 20px; text-align: left; cursor: pointer; }
.author-list button:hover { background: var(--fill-soft); }
.author-list button.active { border-color: #cce3d8; background: #edf6f0; color: #126b58; font-weight: 600; }
.author-list img, .author-symbol { flex: none; width: 32px; height: 32px; border-radius: 10px; object-fit: cover; }
.author-symbol { display: grid; place-items: center; background: var(--fill-soft); color: #618575; }
.author-symbol svg { display: block; width: 18px; height: 18px; margin: 0; fill: none; stroke: currentColor; stroke-width: 1.5; stroke-linecap: round; stroke-linejoin: round; }
.author-name { display: block; flex: 1; min-width: 0; margin: 0; padding: 0; overflow: hidden; line-height: 20px; text-overflow: ellipsis; white-space: nowrap; }
.author-list .el-tag { flex: none; max-width: 64px; overflow: hidden; }
.timeline { display: flex; flex-direction: column; gap: 16px; min-width: 0; }
.composer { min-width: 0; padding: 24px; border: 1px solid #dbe7df; border-radius: 20px; background: #fff; box-shadow: 0 4px 20px rgba(29, 64, 43, .025); font-family: var(--el-font-family); }
.composer-heading { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 20px; }
.composer-heading h2 { margin: 0; font-size: 17px; font-weight: 600; }
.composer-title :deep(.el-input__wrapper) { min-height: 42px; padding: 0 12px; border-radius: 10px; background: var(--fill-soft); box-shadow: none; }
.composer-title :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1px #8bb7a1 inset; }
.composer-title :deep(.el-input__count-inner) { background: transparent; }
.composer-editor { margin-top: 12px; }
.publish, .publish-options { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.publish { justify-content: space-between; margin-top: 14px; padding-top: 16px; border-top: 1px solid var(--line); }
.publish-options { margin-left: auto; }
.publish-options > .el-button { flex: none; width: auto; max-width: 100%; min-height: 36px; margin: 0; padding: 0 20px; border-radius: 10px; }
.visibility { width: 112px; }
.visibility :deep(.el-select__wrapper) { border-radius: 10px; }
.image-upload { position: relative; display: inline-flex; align-items: center; gap: 8px; min-height: 36px; max-width: 100%; padding: 6px 10px; border: 1px solid var(--line); border-radius: 10px; color: #126b58; font-size: 12px; cursor: pointer; }
.image-upload > span { color: var(--ink-3); font-size: 11px; }
.image-upload:hover { background: var(--fill-soft); }
.image-upload.disabled { opacity: .55; cursor: not-allowed; }
.image-upload input { position: absolute; width: 1px; height: 1px; overflow: hidden; clip-path: inset(50%); white-space: nowrap; }
.image-upload:focus-within, .write-button:focus-visible, .author-list button:focus-visible { outline: 2px solid #126b58; outline-offset: 3px; }
.author-list button:focus-visible { outline-offset: -2px; }
.attachments { display: grid; grid-template-columns: repeat(auto-fill, minmax(108px, 1fr)); gap: 10px; margin-top: 14px; }
.attachment { position: relative; min-width: 0; padding: 5px; border: 1px solid var(--line); border-radius: 12px; background: var(--fill-soft); }
.attachment-order { position: absolute; top: 10px; left: 10px; display: grid; place-items: center; width: 20px; height: 20px; border-radius: 6px; background: rgba(26, 43, 34, .65); color: #fff; font-size: 11px; pointer-events: none; z-index: 1; }
.attachment .el-image { display: block; width: 100%; height: 112px; border-radius: 8px; }
.image-actions { display: flex; justify-content: space-between; flex-wrap: wrap; padding-top: 5px; }
.image-actions .el-button { margin: 0; padding: 4px; font-size: 12px; }
.timeline-controls { padding: 20px 24px; border: 1px solid var(--line); border-radius: 18px; background: #fff; }
.timeline-heading { display: flex; align-items: baseline; justify-content: space-between; flex-wrap: wrap; gap: 8px; margin-bottom: 16px; }
.timeline-heading h2 { margin: 0; font-size: 16px; font-weight: 600; }
.filters { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; }
.scope-tabs, .type-tabs { display: inline-flex; flex-wrap: wrap; gap: 4px; padding: 4px; border-radius: 12px; background: var(--fill-soft); }
.filters :deep(.el-radio-button__inner) { padding: 9px 14px; border: 0; border-radius: 9px; background: transparent; box-shadow: none; font-size: 12px; }
.filters :deep(.el-radio-button:first-child .el-radio-button__inner), .filters :deep(.el-radio-button:last-child .el-radio-button__inner) { border: 0; border-radius: 9px; }
.scope-tabs :deep(.el-radio-button) { --el-radio-button-checked-bg-color: #126b58; --el-radio-button-checked-border-color: #126b58; }
.type-tabs :deep(.el-radio-button) { --el-radio-button-checked-bg-color: #fff; --el-radio-button-checked-text-color: #126b58; --el-radio-button-checked-border-color: transparent; }
.filters :deep(.el-radio-button.is-active .el-radio-button__inner) { background: var(--el-radio-button-checked-bg-color); color: var(--el-radio-button-checked-text-color, #fff); box-shadow: 0 1px 3px rgba(27, 54, 39, .08); }
.filters :deep(.el-radio-button__original-radio:focus-visible + .el-radio-button__inner) { outline: 2px solid #126b58; outline-offset: 2px; }
.new-posts { width: 100%; min-height: 42px; margin: 0; border: 1px solid #cce3d8; border-radius: 12px; background: #edf6f0; color: #126b58; }
.new-posts svg { margin-right: 8px; }
.load-more { align-self: center; min-width: 180px; min-height: 42px; margin: 4px 0; border-radius: 12px; color: var(--ink-2); }
.feed-end { margin: 8px 0; color: var(--ink-3); font-size: 12px; text-align: center; line-height: 1.8; }
@media (min-width: 961px) and (max-height: 600px) {
  .authors { position: static; }
}
@media (max-width: 960px) {
  .feed-layout { grid-template-columns: minmax(0, 1fr); gap: 16px; }
  .authors { position: static; padding: 14px; }
  .authors-heading { padding: 0 2px 12px; }
  .author-list { flex-direction: row; align-items: center; gap: 8px; max-height: none; padding: 4px 2px; overflow-x: auto; overflow-y: hidden; }
  .author-list button { justify-content: center; gap: 8px; width: auto; max-width: 210px; height: 40px; min-height: 40px; padding: 0 12px; }
  .author-list img, .author-symbol { width: 24px; height: 24px; border-radius: 7px; }
  .author-name { flex: initial; max-width: 110px; }
}
@media (max-width: 640px) {
  .feed { padding-top: 20px; padding-bottom: 32px; }
  .feed-heading { gap: 14px; margin-bottom: 18px; padding-bottom: 18px; }
  .write-button { min-height: 36px; padding: 0 12px; font-size: 12px; }
  .authors { padding: 12px 10px 6px; border-radius: 14px; }
  .composer, .timeline-controls { padding: 16px; border-radius: 16px; }
  .composer-heading { margin-bottom: 16px; }
  .filters { gap: 10px; }
  .filters :deep(.el-radio-button__inner) { padding: 9px 12px; }
  .publish-options { flex: 0 1 auto; max-width: 100%; justify-content: flex-end; }
}
@media (max-width: 380px) {
  .publish { gap: 12px; }
  .publish-options { flex-basis: 100%; justify-content: flex-start; margin-left: 0; }
  .attachments { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
@media (prefers-reduced-motion: no-preference) {
  .write-button, .author-list button { transition: background-color .18s, border-color .18s; }
}
</style>
<style lang="scss" scoped src="./dynamicPost.scss"></style>
