<template>
  <main class="feed">
    <h1>动态</h1>
    <el-card v-if="store.token" class="composer">
      <el-input v-model="title" maxlength="20" show-word-limit placeholder="好的标题更容易获得支持（选填）"/>
      <div ref="editor" class="editor" contenteditable="true" role="textbox" aria-label="动态内容"
        data-placeholder="有什么想和大家分享的？" @input="updateContent" @keyup="saveSelection"
        @mouseup="saveSelection" @blur="saveSelection" @paste.prevent="paste"/>
      <div class="tools">
        <el-popover trigger="click" :width="300">
          <template #reference><el-button text @mousedown.prevent>表情</el-button></template>
          <div class="emoji-list">
            <button v-for="number in 176" :key="number" type="button" @click="insertEmoji(number)">
              <img :src="`/img/emoji/${number}.png`" :alt="`表情 ${number}`" loading="lazy">
            </button>
          </div>
        </el-popover>
        <el-popover trigger="click" :width="280">
          <template #reference><el-button text @mousedown.prevent>@ 用户</el-button></template>
          <el-input v-model="mentionKeyword" placeholder="搜索你想 @ 的用户"/>
          <div class="mention-list">
            <button v-for="user in filteredMentions" :key="user.id" type="button" @click="insertMention(user)">
              <img :src="user.avatarAddress" alt="">{{ user.userName }}
            </button>
          </div>
        </el-popover>
        <span>{{ contentLength }} / 2000</span>
      </div>
      <div v-if="images.length" class="attachments">
        <div v-for="(image, index) in images" :key="image" class="attachment">
          <el-image :src="image" :preview-src-list="images" preview-teleported fit="cover"/>
          <el-button text type="danger" @click="images.splice(index, 1)">移除</el-button>
        </div>
      </div>
      <div class="publish">
        <label class="image-upload">添加图片（{{ images.length }}/9）
          <input type="file" multiple accept="image/png,image/jpeg,image/gif" :disabled="uploading || images.length >= 9" @change="upload">
        </label>
        <el-select v-model="visibility" class="visibility"><el-option label="公开" :value="0"/><el-option label="仅自己" :value="1"/></el-select>
        <el-button type="primary" :loading="publishing || uploading" @click="publish">发布</el-button>
      </div>
    </el-card>
    <section class="feed-layout">
      <aside v-if="store.userId" class="authors">
        <h2>关注的 UP 主</h2>
        <button :class="{ active: scope === 'following' && !targetUserId }" @click="selectAuthor(null)">全部关注</button>
        <button :class="{ active: targetUserId === store.userId }" @click="selectAuthor(store.userId)">我的动态</button>
        <button v-for="author in authors" :key="author.userId" :class="{ active: targetUserId === author.userId }" @click="selectAuthor(author.userId)">
          <img :src="author.avatarUrl" alt=""><span>{{ author.nickname }}</span>
          <el-tag v-if="author.unreadCount" size="small" type="danger">{{ author.unreadCount }}</el-tag>
        </button>
      </aside>
      <section class="timeline">
        <div class="filters">
          <el-radio-group v-model="scope">
            <el-radio-button value="following" :disabled="!store.userId">关注动态</el-radio-button>
            <el-radio-button value="public">公开动态</el-radio-button>
          </el-radio-group>
          <el-radio-group v-model="type">
            <el-radio-button :value="1">全部</el-radio-button><el-radio-button :value="2">视频</el-radio-button><el-radio-button :value="3">图文</el-radio-button>
          </el-radio-group>
        </div>
        <el-button v-if="scope === 'following' && unreadCount" class="new-posts" @click="reload">{{ unreadCount }} 条新动态，点击刷新</el-button>
        <el-alert v-if="reminderError" :title="reminderError" type="error" :closable="false" show-icon/>
        <homePost v-for="item in rows" :key="item.id" :dynamic="item" @changed="reload"/>
        <loadingIndicator v-if="loading"/>
        <el-empty v-if="!loading && !rows.length" description="暂无动态"/>
        <el-button v-if="hasMore" :loading="loading" @click="load">加载更多</el-button>
      </section>
    </section>
  </main>
</template>
<script setup>
import { computed, onUnmounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { fetchMentionUsers, socialApi } from '@/api/product';
import { post } from '@/services/http';
import { useGlobalStore } from '@/store/store';
import homePost from '@/home/homePost.vue';
import loadingIndicator from '@/components/loadingIndicator.vue';

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
const mentions = ref([]);
const mentionKeyword = ref('');
const reminderError = ref('');
const filteredMentions = computed(() => mentions.value.filter(user => user.userName.includes(mentionKeyword.value)));
const unreadCount = computed(() => authors.value
  .filter(author => !targetUserId.value || author.userId === targetUserId.value)
  .reduce((total, author) => total + Number(author.unreadCount || 0), 0));
let generation = 0;
let reminderTimer;
let savedRange;

function saveSelection() {
  const selection = window.getSelection();
  if (selection?.rangeCount && editor.value?.contains(selection.anchorNode)) savedRange = selection.getRangeAt(0).cloneRange();
}
function updateContent() {
  content.value = editor.value?.innerHTML || '';
  contentLength.value = (editor.value?.innerText.trim().length || 0) + (editor.value?.querySelectorAll('img').length || 0) * 5;
}
function insertNode(node) {
  const element = editor.value;
  if (!element) return;
  element.focus();
  const selection = window.getSelection();
  const range = savedRange && element.contains(savedRange.commonAncestorContainer) ? savedRange : document.createRange();
  if (range !== savedRange) { range.selectNodeContents(element); range.collapse(false); }
  range.deleteContents();
  range.insertNode(node);
  range.setStartAfter(node);
  range.collapse(true);
  selection.removeAllRanges();
  selection.addRange(range);
  savedRange = range.cloneRange();
  updateContent();
}
function insertEmoji(number) {
  const image = document.createElement('img');
  image.src = `/img/emoji/${number}.png`;
  image.alt = '[表情]';
  insertNode(image);
}
function insertMention(user) {
  const link = document.createElement('a');
  link.href = `/home?homeMenu=1&userId=${user.id}`;
  link.textContent = `@${user.userName}`;
  link.contentEditable = 'false';
  insertNode(link);
  insertNode(document.createTextNode(' '));
}
function paste(event) {
  const text = event.clipboardData.getData('text/plain');
  if (/^https?:\/\/\S+$/.test(text)) {
    const link = document.createElement('a');
    link.href = text; link.textContent = text;
    insertNode(link);
  } else insertNode(document.createTextNode(text));
}
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
  if (store.userId === userId) authors.value = result;
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
async function publish() {
  updateContent();
  if (!contentLength.value && !images.value.length) { ElMessage.warning('请输入内容或添加图片'); return; }
  if (contentLength.value > 2000) { ElMessage.warning('动态正文不能超过 2000 字'); return; }
  publishing.value = true;
  try {
    await socialApi.createPost({ title: title.value, content: content.value, imageUrls: images.value, visibility: visibility.value });
    title.value = ''; content.value = ''; contentLength.value = 0; images.value = []; editor.value.innerHTML = ''; savedRange = null;
    await reload();
    ElMessage.success('发布成功');
  } catch (error) { ElMessage.error(error.message); }
  finally { publishing.value = false; }
}
watch([scope, targetUserId, type], reload);
watch(scope, value => { if (value === 'public') targetUserId.value = null; });
watch(() => store.userId, async id => {
  clearInterval(reminderTimer);
  authors.value = []; targetUserId.value = null; mentions.value = [];
  scope.value = id ? 'following' : 'public';
  await reload();
  if (!id || store.userId !== id) return;
  try {
    const [users] = await Promise.all([fetchMentionUsers(), refreshAuthors()]);
    if (store.userId !== id) return;
    mentions.value = [...users.friendList, ...users.otherList];
    reminderTimer = setInterval(async () => {
      try { await refreshAuthors(); reminderError.value = ''; }
      catch (error) { reminderError.value = error.message; }
    }, 30000);
  } catch (error) { reminderError.value = error.message; }
}, { immediate: true });
watch(() => store.deleteFlag, id => { rows.value = rows.value.filter(row => row.id !== id); });
onUnmounted(() => { generation++; clearInterval(reminderTimer); });
</script>
<style scoped>
.feed { box-sizing: border-box; width: 100%; max-width: var(--page-max); margin: auto; padding: var(--gap-5) var(--page-pad); }
.composer { margin-bottom: 24px; }
.editor { min-height: 100px; max-height: 350px; overflow: auto; padding: 16px 0; outline: none; white-space: pre-wrap; overflow-wrap: anywhere; line-height: 1.7; }
.editor:empty::before { content: attr(data-placeholder); color: #8d9794; }
.editor :deep(img) { width: 28px; height: 28px; vertical-align: middle; }
.tools, .publish { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.tools > span { margin-left: auto; color: #8d9794; font-size: 12px; }
.publish { margin-top: 12px; }
.visibility { width: 120px; margin-left: auto; }
.image-upload { color: #0a8271; font-size: 13px; max-width: 100%; }
.image-upload input { display: block; max-width: 100%; margin-top: 8px; }
.attachments { display: flex; gap: 12px; flex-wrap: wrap; margin-top: 12px; }
.attachment { width: 100px; }
.attachment .el-image { width: 100px; height: 100px; border-radius: 6px; }
.emoji-list { display: grid; grid-template-columns: repeat(7, 1fr); max-height: 240px; overflow: auto; }
.emoji-list button, .mention-list button, .authors button { border: 0; background: transparent; cursor: pointer; }
.emoji-list img { width: 28px; height: 28px; }
.mention-list { max-height: 240px; overflow: auto; }
.mention-list button { display: flex; align-items: center; gap: 8px; width: 100%; padding: 8px; text-align: left; }
.mention-list img, .authors img { width: 28px; height: 28px; border-radius: 50%; }
.feed-layout { display: grid; grid-template-columns: 220px minmax(0, 1fr); gap: 24px; }
.feed-layout:not(:has(.authors)) { grid-template-columns: minmax(0, 1fr); }
.authors { align-self: start; padding: 16px; border-radius: 12px; background: white; }
.authors h2 { margin: 0 0 12px; font-size: 15px; }
.authors button { display: flex; align-items: center; gap: 8px; width: 100%; padding: 10px 8px; border-radius: 6px; text-align: left; }
.authors button span { min-width: 0; overflow-wrap: anywhere; }
.authors button.active { color: #0a8271; background: #e8f4f1; }
.timeline { min-width: 0; }
.filters { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 20px; }
.new-posts { width: 100%; margin-bottom: 16px; }
@media (max-width: 960px) {
  .feed-layout { grid-template-columns: minmax(0, 1fr); gap: 16px; }
  .authors { display: flex; gap: 8px; overflow-x: auto; }
  .authors h2 { display: none; }
  .authors button { flex: none; width: auto; white-space: nowrap; }
}
@media (max-width: 640px) {
  .feed { padding-top: var(--gap-4); }
  h1 { margin-top: 0; font-size: 24px; }
  .composer :deep(.el-card__body) { padding: var(--gap-4) var(--gap-3); }
  .image-upload { flex-basis: 100%; }
}
</style>
