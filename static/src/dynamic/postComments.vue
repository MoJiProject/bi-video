<template>
  <section class="discussion">
    <div class="heading"><h2>评论 <span class="comment-total">{{ total }}</span></h2>
      <el-radio-group v-model="sort" size="small" aria-label="评论排序"><el-radio-button value="hot">最热</el-radio-button><el-radio-button value="new">最新</el-radio-button></el-radio-group>
    </div>
    <form class="comment-form" @submit.prevent="send">
      <el-tag v-if="replyTo" closable @close="replyTo = null">回复 @{{ replyTo.authorNickname }}</el-tag>
      <PostEditor ref="input" v-model="content" compact :max-length="1000" :disabled="sending"
        placeholder="发一条友善的评论…" @length="contentLength = $event">
        <template #tools>
          <label class="editor-tool comment-upload" :class="{ disabled: sending || uploading || images.length >= 9 }">
            <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="3" width="18" height="18" rx="4"/><circle cx="8" cy="8" r="1.5"/><path d="m3 17 5-5 4 4 4-6 5 7"/></svg>
            <span>图片</span><span v-if="images.length">{{ images.length }}/9</span>
            <input type="file" accept="image/png,image/jpeg,image/gif" multiple aria-label="添加评论图片，最多九张" :disabled="sending || uploading || images.length >= 9" @change="upload">
          </label>
        </template>
        <template #actions>
          <el-button type="primary" native-type="submit" :loading="sending || uploading">发布评论</el-button>
        </template>
      </PostEditor>
      <div v-if="images.length" class="attachments">
        <div v-for="(image, index) in images" :key="image"><el-image :src="image" :preview-src-list="images" preview-teleported fit="cover"/>
          <el-button text type="danger" @click="images.splice(index, 1)">移除</el-button></div>
      </div>
    </form>
    <div v-for="root in rows" :key="root.id" class="comment">
      <article v-for="comment in [root, ...(root.replies || [])]" :key="comment.id"
        class="content-action-surface" :class="{ reply: comment.rootId }">
        <header><UserHoverCard :user-id="comment.authorId" :avatar="comment.authorAvatar"
            :nickname="comment.authorNickname" :avatar-size="32"/>
          <time>{{ comment.createdAt?.replace('T', ' ') }}</time>
          <ContentMoreMenu v-if="comment.authorId === store.userId" label="更多评论操作" :disabled="busy.has(comment.id)">
            <el-dropdown-item class="is-danger" :disabled="busy.has(comment.id)" @click="remove(comment)">删除</el-dropdown-item>
          </ContentMoreMenu>
        </header>
        <small v-if="comment.replyToNickname">回复 @{{ comment.replyToNickname }}</small>
        <richContent :content="comment.content"/>
        <div v-if="comment.imageUrls?.length" class="comment-images">
          <el-image v-for="image in comment.imageUrls" :key="image" :src="image" :preview-src-list="comment.imageUrls" preview-teleported fit="cover"/>
        </div>
        <footer>
          <el-button text :type="comment.liked ? 'primary' : ''" :aria-pressed="!!comment.liked" :disabled="busy.has(comment.id)" @click="like(comment, false)">
            <svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/></svg>
            <span>{{ comment.likeCount || 0 }} 赞</span>
          </el-button>
          <el-button text :type="comment.disliked ? 'primary' : ''" :aria-pressed="!!comment.disliked" :disabled="busy.has(comment.id)" @click="like(comment, true)">
            <svg class="comment-action-icon is-dislike" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/></svg>
            <span>踩</span>
          </el-button>
          <el-button text @click="reply(comment)">
            <svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M7 18 3 21V6a3 3 0 0 1 3-3h12a3 3 0 0 1 3 3v9a3 3 0 0 1-3 3H7Z"/><path d="M7 8h10M7 12h7"/></svg>
            <span>回复</span>
          </el-button>
        </footer>
      </article>
    </div>
    <loadingIndicator v-if="loading"/>
    <div v-if="loadError" class="comment-load-error" role="alert">
      <span>{{ loadError }}</span><el-button text type="primary" @click="load">重新加载</el-button>
    </div>
    <AppEmptyState v-if="!loading && !loadError && !rows.length" compact title="暂无评论"/>
    <el-button v-if="hasMore && !loadError" class="load-more" :loading="loading" @click="load">加载更多评论</el-button>
  </section>
</template>
<script setup>
import { onUnmounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { commentApi } from '@/api/product';
import { post } from '@/services/http';
import { useGlobalStore } from '@/store/store';
import richContent from '@/components/richContent.vue';
import ContentMoreMenu from '@/components/ContentMoreMenu.vue';
import UserHoverCard from '@/components/UserHoverCard.vue';
import PostEditor from '@/components/PostEditor.vue';
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
const loadError = ref('');
const content = ref('');
const contentLength = ref(0);
const images = ref([]);
const replyTo = ref(null);
const sending = ref(false);
const uploading = ref(false);
const busy = ref(new Set());
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
  loadError.value = '';
  try {
    const page = await commentApi.list({ postId: props.postId, cursorId: cursorId.value, size: 10, sort: sort.value });
    if (current !== generation) return;
    rows.value.push(...page.records);
    total.value = page.total;
    emit('count', page.total);
    cursorId.value = page.cursorId;
    hasMore.value = page.hasMore;
  } catch (error) {
    if (current === generation) {
      loadError.value = error.message || '评论加载失败';
      ElMessage.error(loadError.value);
    }
  }
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
async function send() {
  if (!signedIn() || sending.value || uploading.value) return;
  if (!contentLength.value) { ElMessage.warning('请输入评论内容'); return; }
  const body = content.value;
  if (body.length > 1000) { ElMessage.warning('评论内容（含表情和 @ 用户）过长，请适当缩短'); return; }
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
.discussion { box-sizing: border-box; min-width: 0; padding: 24px; border: 1px solid var(--line); border-radius: 20px; background: #fff; color: var(--ink); font-family: var(--el-font-family); overflow-wrap: anywhere; }
.heading, header, footer { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.heading { justify-content: space-between; margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid var(--line); }
.heading .el-radio-group { gap: 4px; padding: 3px; border-radius: 8px; background: var(--fill-soft); }
.heading :deep(.el-radio-button__inner) { padding: 6px 10px; border: 0; border-radius: 6px; background: transparent; color: var(--ink-2); box-shadow: none; font-size: 12px; }
.heading :deep(.el-radio-button.is-active .el-radio-button__inner) { background: #fff; color: var(--brand-hover); box-shadow: 0 1px 4px rgba(28, 35, 33, .08); }
.heading :deep(.el-radio-button__original-radio:focus-visible + .el-radio-button__inner) { outline: 2px solid var(--brand); outline-offset: 1px; }
h2 { display: flex; align-items: center; gap: 8px; margin: 0; font-size: 16px; font-weight: 600; }
.comment-total { padding: 3px 8px; border-radius: 7px; background: var(--fill-soft); color: var(--ink-2); font-size: 12px; font-weight: 400; }
.comment-form { width: 100%; min-width: 0; margin: 0; }
.comment-form > .el-tag { margin-bottom: 10px; }
.comment-upload { position: relative; }
.comment-upload > span + span { color: var(--ink-3); font-size: 11px; }
.comment-upload.disabled { opacity: .55; cursor: not-allowed; }
.comment-upload:focus-within { outline: 2px solid #126b58; outline-offset: 3px; }
.comment-upload input { position: absolute; width: 1px; height: 1px; overflow: hidden; clip-path: inset(50%); white-space: nowrap; }
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
footer .el-button { height: 34px; margin: 0; padding: 0 10px; border-radius: 8px; font-size: 12px; }
.comment-action-icon { display: inline-block; flex: none; width: 18px; height: 18px; margin-right: 6px; fill: none; stroke: currentColor; stroke-width: 1.7; stroke-linecap: round; stroke-linejoin: round; }
.comment-action-icon.is-dislike { transform: rotate(180deg); }
.load-more { display: flex; min-height: 38px; margin: 20px auto 0; border-radius: 10px; }
.comment-load-error { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; padding: 16px 0; color: var(--ink-2); font-size: 13px; }
@media (max-width: 640px) {
  .discussion { padding: 16px; border-radius: 16px; }
  .heading { gap: 10px; padding-bottom: 14px; }
  .reply { margin-left: 12px; padding-left: 12px; }
  header { gap: 8px; }
  time { margin-left: 42px; }
}
</style>
