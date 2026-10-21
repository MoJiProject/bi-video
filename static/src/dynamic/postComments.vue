<template>
  <section class="discussion">
    <div class="heading"><h2>评论 <span class="comment-total">{{ total }}</span></h2>
      <el-radio-group v-model="sort" size="small" aria-label="评论排序"><el-radio-button value="hot">最热</el-radio-button><el-radio-button value="new">最新</el-radio-button></el-radio-group>
    </div>
    <form class="comment-form" @submit.prevent="send">
      <el-tag v-if="replyTo" closable @close="replyTo = null">回复 @{{ replyTo.authorNickname }}</el-tag>
      <el-input ref="input" v-model="content" type="textarea" :rows="3" maxlength="1000" show-word-limit placeholder="发一条友善的评论"/>
      <div v-if="images.length" class="attachments">
        <div v-for="(image, index) in images" :key="image"><el-image :src="image" :preview-src-list="images" preview-teleported fit="cover"/>
          <el-button text type="danger" @click="images.splice(index, 1)">移除</el-button></div>
      </div>
      <div class="actions">
        <label class="comment-upload" :class="{ disabled: uploading || images.length >= 9 }">添加图片 <span>{{ images.length }}/9</span><input type="file" accept="image/png,image/jpeg,image/gif" multiple aria-label="添加评论图片，最多九张" :disabled="uploading || images.length >= 9" @change="upload"></label>
        <el-popover trigger="click" :width="260">
          <template #reference><el-button text>表情</el-button></template>
          <div class="emoji-list"><button v-for="number in 176" :key="number" type="button" @click="emoji = number; sendEmoji()">
            <img :src="`/img/emoji/${number}.png`" :alt="`表情 ${number}`" loading="lazy">
          </button></div>
        </el-popover>
        <el-button type="primary" native-type="submit" :loading="sending || uploading">发布评论</el-button>
      </div>
    </form>
    <div v-for="root in rows" :key="root.id" class="comment">
      <article v-for="comment in [root, ...(root.replies || [])]" :key="comment.id" :class="{ reply: comment.rootId }">
        <header><a :href="`/home?homeMenu=1&userId=${comment.authorId}`"><img :src="comment.authorAvatar" alt=""><span>{{ comment.authorNickname }}</span></a>
          <time>{{ comment.createdAt?.replace('T', ' ') }}</time></header>
        <small v-if="comment.replyToNickname">回复 @{{ comment.replyToNickname }}</small>
        <richContent :content="comment.content"/>
        <div v-if="comment.imageUrls?.length" class="comment-images">
          <el-image v-for="image in comment.imageUrls" :key="image" :src="image" :preview-src-list="comment.imageUrls" preview-teleported fit="cover"/>
        </div>
        <footer>
          <el-button text :type="comment.liked ? 'primary' : ''" :disabled="busy.has(comment.id)" @click="like(comment, false)">{{ comment.likeCount || 0 }} 赞</el-button>
          <el-button text :type="comment.disliked ? 'primary' : ''" :disabled="busy.has(comment.id)" @click="like(comment, true)">踩</el-button>
          <el-button text @click="reply(comment)">回复</el-button>
          <el-button v-if="comment.authorId === store.userId" text type="danger" :disabled="busy.has(comment.id)" @click="remove(comment)">删除</el-button>
        </footer>
      </article>
    </div>
    <loadingIndicator v-if="loading"/>
    <AppEmptyState v-if="!loading && !rows.length" compact title="暂无评论"/>
    <el-button v-if="hasMore" class="load-more" :loading="loading" @click="load">加载更多评论</el-button>
  </section>
</template>
<script setup>
import { onUnmounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { commentApi } from '@/api/product';
import { post } from '@/services/http';
import { useGlobalStore } from '@/store/store';
import richContent from '@/components/richContent.vue';
import loadingIndicator from '@/components/loadingIndicator.vue';

const props = defineProps({ postId: { type: Number, required: true } });
const emit = defineEmits(['count']);
const store = useGlobalStore();
const input = ref(null);
const rows = ref([]);
const total = ref(0);
const sort = ref('hot');
const cursorId = ref(null);
const hasMore = ref(false);
const loading = ref(false);
const content = ref('');
const images = ref([]);
const replyTo = ref(null);
const sending = ref(false);
const uploading = ref(false);
const busy = ref(new Set());
const emoji = ref(null);
let generation = 0;
function signedIn() {
  if (store.token) return true;
  store.loginDialogVisible = true;
  return false;
}
async function load() {
  if (loading.value) return;
  const current = generation;
  loading.value = true;
  try {
    const page = await commentApi.list({ postId: props.postId, cursorId: cursorId.value, size: 20, sort: sort.value });
    if (current !== generation) return;
    rows.value.push(...page.records);
    total.value = page.total;
    emit('count', page.total);
    cursorId.value = page.cursorId;
    hasMore.value = page.hasMore;
  } catch (error) { if (current === generation) ElMessage.error(error.message); }
  finally { if (current === generation) loading.value = false; }
}
function reload() { generation++; rows.value = []; cursorId.value = null; loading.value = false; hasMore.value = false; return load(); }
function reply(comment) { if (!signedIn()) return; replyTo.value = comment; input.value.focus(); }
async function upload(event) {
  if (!signedIn()) { event.target.value = ''; return; }
  const files = Array.from(event.target.files);
  if (files.length + images.value.length > 9) { ElMessage.warning('最多 9 张图片'); event.target.value = ''; return; }
  uploading.value = true;
  try {
    for (const file of files) {
      const body = new FormData(); body.append('file', file);
      const image = await post('/media/image', body);
      images.value.push(image.url);
    }
  } catch (error) { ElMessage.error(error.message); }
  finally { uploading.value = false; event.target.value = ''; }
}
function sendEmoji() {
  if (!signedIn()) return;
  if (emoji.value) {
    content.value += `[表情${emoji.value}]`;
    emoji.value = null;
    input.value.focus();
  }
}
function commentContent() {
  const container = document.createElement('div');
  container.textContent = content.value.trim();
  return container.innerHTML.replace(/\[表情(\d{1,3})\]/g, '<img src="/img/emoji/$1.png" alt="[表情]">');
}
async function send() {
  if (!signedIn() || sending.value || uploading.value) return;
  if (!content.value.trim()) { ElMessage.warning('请输入评论内容'); return; }
  const body = commentContent();
  if (body.length > 1000) { ElMessage.warning('评论内容（含表情）过长，请适当缩短'); return; }
  sending.value = true;
  try {
    await commentApi.create({ postId: props.postId, content: body, imageUrls: images.value, rootId: replyTo.value?.id });
    content.value = ''; images.value = []; replyTo.value = null;
    await reload();
    ElMessage.success('评论发布成功');
  } catch (error) { ElMessage.error(error.message); }
  finally { sending.value = false; }
}
async function like(comment, dislike) {
  if (!signedIn()) return;
  busy.value.add(comment.id);
  try {
    const result = await commentApi.like(comment.id, dislike);
    comment.likeCount = result.likeCount;
    comment.liked = !dislike && result.active;
    comment.disliked = dislike && result.active;
  } catch (error) { ElMessage.error(error.message); }
  finally { busy.value.delete(comment.id); }
}
async function remove(comment) {
  const confirmed = await ElMessageBox.confirm('确定删除这条评论及其回复？', '删除评论', { type: 'warning' }).then(() => true, () => false);
  if (!confirmed) return;
  busy.value.add(comment.id);
  try { await commentApi.remove(comment.id); await reload(); }
  catch (error) { ElMessage.error(error.message); }
  finally { busy.value.delete(comment.id); }
}
watch([() => props.postId, sort, () => store.userId], reload, { immediate: true });
onUnmounted(() => { generation++; });
</script>
<style scoped>
.discussion { box-sizing: border-box; min-width: 0; padding: 24px; border: 1px solid var(--line); border-radius: 20px; background: #fff; color: var(--ink); overflow-wrap: anywhere; }
.heading, header, footer, .actions { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.heading { justify-content: space-between; margin-bottom: 20px; padding-bottom: 18px; border-bottom: 1px solid var(--line); }
h2 { display: flex; align-items: center; gap: 8px; margin: 0; font-size: 18px; font-weight: 600; }
.comment-total { padding: 3px 8px; border-radius: 7px; background: var(--fill-soft); color: var(--ink-2); font-size: 12px; font-weight: 400; }
.comment-form { padding: 14px; border: 1px solid var(--line); border-radius: 14px; background: var(--fill-soft); }
.comment-form .el-textarea { margin: 10px 0; }
.comment-form :deep(.el-textarea__inner) { border-radius: 10px; background: #fff; font-size: 13px; line-height: 1.8; }
.comment-form :deep(.el-input__count) { background: transparent; font-size: 11px; }
.actions { justify-content: flex-end; gap: 8px; font-size: 12px; color: #126b58; }
.actions > .el-button { min-height: 34px; margin: 0; border-radius: 9px; }
.comment-upload { position: relative; display: inline-flex; align-items: center; gap: 6px; min-height: 34px; margin-right: auto; cursor: pointer; }
.comment-upload > span { color: var(--ink-3); font-size: 11px; }
.comment-upload.disabled { opacity: .55; cursor: not-allowed; }
.comment-upload:focus-within { outline: 2px solid #126b58; outline-offset: 3px; }
.actions input { position: absolute; width: 1px; height: 1px; overflow: hidden; clip-path: inset(50%); white-space: nowrap; }
.attachments, .comment-images { display: flex; gap: 8px; flex-wrap: wrap; margin: 12px 0; }
.attachments > div { width: 90px; }
.el-image { width: 90px; max-width: 100%; height: 90px; border-radius: 10px; }
.attachments .el-button { margin: 0; font-size: 12px; }
.comment { min-width: 0; padding: 24px 0; border-bottom: 1px solid var(--line); }
.comment article { min-width: 0; }
.reply { margin: 18px 0 0 40px; padding-left: 16px; border-left: 2px solid #edf2ef; }
header a { display: flex; align-items: center; gap: 10px; min-width: 0; max-width: 100%; color: inherit; text-decoration: none; font-size: 13px; font-weight: 500; }
header a span { min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
header img { flex: none; width: 32px; height: 32px; border-radius: 10px; object-fit: cover; }
time, small { color: #8d9794; font-size: 12px; }
time { margin-left: auto; }
.rich-content { margin: 12px 0; color: #3e4c45; font-size: 13px; }
footer { gap: 6px; }
footer .el-button { margin: 0; padding: 6px 8px; font-size: 12px; }
.load-more { display: flex; min-height: 38px; margin: 20px auto 0; border-radius: 10px; }
.emoji-list { display: grid; grid-template-columns: repeat(6, 1fr); max-height: 240px; overflow: auto; }
.emoji-list button { border: 0; background: transparent; cursor: pointer; }
.emoji-list img { width: 28px; height: 28px; }
@media (max-width: 640px) {
  .discussion { padding: 16px; border-radius: 16px; }
  .heading { gap: 10px; padding-bottom: 14px; }
  .comment-form { padding: 10px; }
  .reply { margin-left: 12px; padding-left: 12px; }
  header { gap: 8px; }
  time { margin-left: 42px; }
}
@media (max-width: 380px) {
  .actions > .el-button[type="submit"] { flex-basis: 100%; }
}
</style>
