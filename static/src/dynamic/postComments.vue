<template>
  <section class="discussion">
    <div class="heading"><h2>评论 <span class="comment-total">{{ total }}</span></h2>
      <el-radio-group v-model="sort" size="small" aria-label="评论排序"><el-radio-button value="hot">最热</el-radio-button><el-radio-button value="new">最新</el-radio-button></el-radio-group>
    </div>
    <form class="comment-form" @submit.prevent="send">
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
          <el-button text type="danger" @click="URL.revokeObjectURL(images[index]); images.splice(index, 1); imageFiles.splice(index, 1)">移除</el-button></div>
      </div>
    </form>
    <div v-for="root in rows" :key="root.id" class="comment">
      <article v-for="comment in (root.repliesExpanded ? [root, ...(root.replies || [])] : [root])" :key="comment.id"
        class="content-action-surface" :data-id="comment.id" :class="{ reply: comment.rootId, 'comment-highlight': highlightId === comment.id }">
        <header><UserHoverCard :user-id="comment.authorId" :avatar="comment.authorAvatar"
            :nickname="comment.authorNickname" :avatar-size="32"/>
          <time>{{ comment.createdAt?.replace('T', ' ') }}</time>
          <ContentMoreMenu v-if="comment.authorId === store.userId" label="更多评论操作" :disabled="busy.has(comment.id)">
            <el-dropdown-item class="is-danger" :disabled="busy.has(comment.id)" @click="remove(comment)">删除</el-dropdown-item>
          </ContentMoreMenu>
        </header>
        <small v-if="comment.replyToNickname" class="reply-target">
          <span>回复</span>
          <UserHoverCard
            v-if="getReplyTarget(comment, root)"
            :user-id="getReplyTarget(comment, root).userId"
            :avatar="getReplyTarget(comment, root).avatar"
            :nickname="comment.replyToNickname"
            :avatar-size="14"
          >
            <template #reference>
              <a class="reply-target-link" :href="`/home?userId=${getReplyTarget(comment, root).userId}`" target="_blank" rel="noopener noreferrer">
                @{{ comment.replyToNickname }}
              </a>
            </template>
          </UserHoverCard>
          <span v-else class="reply-target-name">@{{ comment.replyToNickname }}</span>
        </small>
        <richContent :content="comment.content"/>
        <div v-if="comment.imageUrls?.length" class="comment-images">
          <el-image v-for="image in comment.imageUrls" :key="image" :src="image" :preview-src-list="comment.imageUrls" preview-teleported fit="cover"/>
        </div>
        <footer>
          <el-button text :type="comment.liked ? 'primary' : ''" :aria-pressed="!!comment.liked" :disabled="busy.has(comment.id)" @click="like(comment, false)">
            <svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/></svg>
            <transition name="count-pop" mode="out-in"><span :key="comment.likeCount" class="count-value">{{ comment.likeCount || 0 }} 赞</span></transition>
          </el-button>
          <el-button text :type="comment.disliked ? 'primary' : ''" :aria-pressed="!!comment.disliked" :disabled="busy.has(comment.id)" @click="like(comment, true)">
            <svg class="comment-action-icon is-dislike" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/></svg>
            <span>踩</span>
          </el-button>
          <el-button text @click="toggleReplyBox(comment)">
            <svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M7 18 3 21V6a3 3 0 0 1 3-3h12a3 3 0 0 1 3 3v9a3 3 0 0 1-3 3H7Z"/><path d="M7 8h10M7 12h7"/></svg>
            <span>回复</span>
          </el-button>
        </footer>
        <div v-if="activeReplyId === comment.id" class="reply-form">
          <PostEditor :ref="setReplyInput" v-model="replyContent" compact :max-length="1000" :disabled="replySending"
            placeholder="发一条友善的评论…" @length="replyContentLength = $event">
            <template #tools>
              <label class="editor-tool comment-upload" :class="{ disabled: replySending || replyUploading || replyImages.length >= 9 }">
                <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="3" width="18" height="18" rx="4"/><circle cx="8" cy="8" r="1.5"/><path d="m3 17 5-5 4 4 4-6 5 7"/></svg>
                <span>图片</span><span v-if="replyImages.length">{{ replyImages.length }}/9</span>
                <input type="file" accept="image/png,image/jpeg,image/gif" multiple aria-label="添加回复图片，最多九张" :disabled="replySending || replyUploading || replyImages.length >= 9" @change="uploadReply">
              </label>
            </template>
            <template #actions>
              <el-button type="primary" :loading="replySending" @click="sendReply(comment)">回复</el-button>
            </template>
          </PostEditor>
          <div v-if="replyImages.length" class="attachments">
            <div v-for="(image, index) in replyImages" :key="image">
              <el-image :src="image" :preview-src-list="replyImages" preview-teleported fit="cover"/>
              <el-button text type="danger" @click="URL.revokeObjectURL(replyImages[index]); replyImages.splice(index, 1); replyImageFiles.splice(index, 1)">移除</el-button>
            </div>
          </div>
        </div>
      </article>
      <div v-if="root.replyCount" class="replies-meta">
        <button type="button" class="reply-toggle" @click="toggleReplies(root)">
          {{ root.repliesExpanded ? '收起回复' : `展开 ${root.replyCount} 条回复` }}
        </button>
        <el-button v-if="root.repliesExpanded && root.replyHasMore" class="load-more-replies" :loading="root.replyLoading" @click="loadMoreReplies(root)">加载更多回复</el-button>
      </div>
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
import { nextTick, onUnmounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { commentApi } from '@/api/product';
import { post, del, get } from '@/services/http';
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
const imageFiles = ref([]);
const activeReplyId = ref(null);
const replyInput = ref(null);
const replyContent = ref('');
const replyContentLength = ref(0);
const replyImages = ref([]);
const replyImageFiles = ref([]);
const replyUploading = ref(false);
const replySending = ref(false);
const sending = ref(false);
const uploading = ref(false);
const busy = ref(new Set());
const highlightId = ref(null);
const deepParams = new URLSearchParams(window.location.search);
const deepCommentId = parseInt(deepParams.get('commentId')) || null;
const deepReplyId = parseInt(deepParams.get('replyId')) || null;
let deepLinkHandled = false;
let generation = 0;
function setReplyInput(el) {
  if (el) replyInput.value = el;
}
function getReplyTarget(comment, root) {
  const replies = root.replies || [];
  const targetId = comment.replyToId ?? comment.replyCommentId;
  const targetComment = [root, ...replies].find(
    (item) => targetId != null && String(item.id) === String(targetId),
  ) || [root, ...replies].find(
    (item) => item.authorNickname === comment.replyToNickname,
  );
  const userId = targetComment?.authorId ?? comment.replyToUserId ?? comment.replyToAuthorId;
  if (!userId) return null;

  return {
    userId: Number(userId),
    avatar: targetComment?.authorAvatar ?? comment.replyToAvatar ?? comment.replyToAvatarAddress ?? '',
  };
}
function normalizeRoot(root) {
  root.replies = root.replies || [];
  root.repliesExpanded = (root.replyCount || 0) <= 2 && root.replies.length > 0;
  root.replyPage = 1;
  root.replyHasMore = (root.replyCount || 0) > root.replies.length;
  root.replyLoading = false;
}
function toggleReplies(root) {
  root.repliesExpanded = !root.repliesExpanded;
}
async function loadMoreReplies(root) {
  if (root.replyLoading || !root.replyHasMore) return;
  root.replyLoading = true;
  root.replyPage += 1;
  try {
    const page = await commentApi.replies(root.id, { pageNum: root.replyPage, size: 10 });
    root.replies.push(...(page.records || []));
    root.replyHasMore = !!page.hasMore;
  } catch (error) {
    ElMessage.error(error?.message || '加载回复失败');
  } finally {
    root.replyLoading = false;
  }
}
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
    for (const root of page.records) normalizeRoot(root);
    total.value = page.total;
    emit('count', page.total);
    cursorId.value = page.cursorId;
    hasMore.value = page.hasMore;
    handleDeepLink();
  } catch (error) {
    if (current === generation) {
      loadError.value = error.message || '评论加载失败';
      ElMessage.error(loadError.value);
    }
  }
  finally { if (current === generation) loading.value = false; }
}
function reload() { generation++; rows.value = []; cursorId.value = null; loading.value = false; hasMore.value = false; return load(); }
async function handleDeepLink() {
  if (deepLinkHandled || !deepCommentId || !rows.value.length) return;
  deepLinkHandled = true;
  try {
    let target = await get(`/comment/${deepCommentId}`);
    const targetPostId = target.dynamicId ?? target.postId ?? target.post_id;
    if (String(targetPostId) !== String(props.postId)) return;
    const rootId = target.rootId ?? target.mainCommentId ?? target.root_id;
    if (rootId) {
      const root = await get(`/comment/${rootId}`);
      const rootPostId = root.dynamicId ?? root.postId ?? root.post_id;
      if (String(rootPostId) !== String(props.postId)) return;
      target = root;
    }
    if (!rows.value.some((row) => row.id === target.id || (row.replies || []).some((r) => r.id === target.id))) {
      rows.value.unshift(target);
      normalizeRoot(target);
    }
    await highlightAndScroll(deepReplyId || deepCommentId, deepReplyId ? deepCommentId : null);
  } catch (error) {
    /* ignore missing/deleted deep-linked comment */
  }
}
async function highlightAndScroll(id, fallbackId = null) {
  if (!id) return;
  highlightId.value = null;
  await nextTick();
  highlightId.value = id;
  await nextTick();
  const el = document.querySelector(`.discussion [data-id="${id}"]`)
    || (fallbackId && document.querySelector(`.discussion [data-id="${fallbackId}"]`));
  el?.scrollIntoView({ behavior: 'smooth', block: 'center' });
}
function toggleReplyBox(comment) {
  if (!signedIn()) return;
  if (activeReplyId.value === comment.id) {
    activeReplyId.value = null;
    return;
  }
  activeReplyId.value = comment.id;
  replyContent.value = '';
  replyContentLength.value = 0;
  replyImages.value = [];
  replyUploading.value = false;
  nextTick(() => replyInput.value?.focus());
}
async function uploadReply(event) {
  if (!signedIn()) { event.target.value = ''; return; }
  const files = Array.from(event.target.files);
  if (files.length + replyImages.value.length > 9) { ElMessage.warning('最多 9 张图片'); event.target.value = ''; return; }
  for (const file of files) {
    if (!file.type.startsWith('image/')) { ElMessage.warning('请上传图片'); continue; }
    if (file.size > 10 * 1024 * 1024) { ElMessage.warning('图片大小不能超过10M'); continue; }
    const url = URL.createObjectURL(file);
    replyImages.value.push(url);
    replyImageFiles.value.push(file);
  }
  event.target.value = '';
}
async function sendReply(target) {
  if (!signedIn() || replySending.value) return;
  if (!replyContentLength.value && !replyImages.value.length) { ElMessage.warning('请输入评论内容'); return; }
  if (replyContent.value.length > 1000) { ElMessage.warning('评论内容（含表情和 @ 用户）过长，请适当缩短'); return; }
  replySending.value = true;
  replyUploading.value = true;
  let uploaded = [];
  try {
    uploaded = [];
    for (const file of replyImageFiles.value) {
      const body = new FormData(); body.append('file', file);
      const image = await post('/media/image', body);
      uploaded.push(image.url);
    }
    const finalContent = replyContent.value || (uploaded.length ? '分享图片' : '');
    await commentApi.create({ postId: props.postId, content: finalContent, imageUrls: uploaded, rootId: target.id });
    replyImages.value.forEach(url => URL.revokeObjectURL(url));
    replyContent.value = ''; replyContentLength.value = 0; replyImages.value = []; replyImageFiles.value = []; replyUploading.value = false; activeReplyId.value = null;
    await reload();
    const expandedRoot = rows.value.find((row) => row.id === (target.rootId ?? target.id));
    if (expandedRoot) expandedRoot.repliesExpanded = true;
    ElMessage.success('回复发布成功');
  } catch (error) {
    for (const url of uploaded) { try { await del('/media', { params: { url } }); } catch (e) {} }
    ElMessage.error(error.message);
  } finally { replySending.value = false; replyUploading.value = false; }
}
async function upload(event) {
  if (!signedIn()) { event.target.value = ''; return; }
  const files = Array.from(event.target.files);
  if (files.length + images.value.length > 9) { ElMessage.warning('最多 9 张图片'); event.target.value = ''; return; }
  for (const file of files) {
    if (!file.type.startsWith('image/')) { ElMessage.warning('请上传图片'); continue; }
    if (file.size > 10 * 1024 * 1024) { ElMessage.warning('图片大小不能超过10M'); continue; }
    const url = URL.createObjectURL(file);
    images.value.push(url);
    imageFiles.value.push(file);
  }
  event.target.value = '';
}
async function send() {
  if (!signedIn() || sending.value || uploading.value) return;
  if (!contentLength.value && !images.value.length) { ElMessage.warning('请输入评论内容'); return; }
  if (content.value.length > 1000) { ElMessage.warning('评论内容（含表情和 @ 用户）过长，请适当缩短'); return; }
  sending.value = true;
  uploading.value = true;
  let uploaded = [];
  try {
    uploaded = [];
    for (const file of imageFiles.value) {
      const body = new FormData(); body.append('file', file);
      const image = await post('/media/image', body);
      uploaded.push(image.url);
    }
    const body = content.value;
    await commentApi.create({ postId: props.postId, content: body || (uploaded.length ? '分享图片' : ''), imageUrls: uploaded, rootId: null });
    images.value.forEach(url => URL.revokeObjectURL(url));
    content.value = ''; images.value = []; imageFiles.value = [];
    await reload();
    ElMessage.success('评论发布成功');
  } catch (error) {
    for (const url of uploaded) { try { await del('/media', { params: { url } }); } catch (e) {} }
    ElMessage.error(error.message);
  } finally { sending.value = false; uploading.value = false; }
}
async function like(comment, dislike) {
  if (!signedIn()) return;
  busy.value.add(comment.id);
  try {
    const result = await commentApi.like(comment.id, dislike);
    if (result) {
      comment.likeCount = typeof result.likeCount === 'number' ? result.likeCount : comment.likeCount;
      comment.liked = !dislike && !!result.active;
      comment.disliked = dislike && !!result.active;
    }
  } catch (error) {
    ElMessage.error(error?.message || '操作失败');
  } finally {
    busy.value.delete(comment.id);
  }
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
.comment-upload input { position: absolute; width: 1px; height: 1px; overflow: hidden; clip-path: inset(50%); white-space: nowrap; }
.attachments, .comment-images { display: flex; gap: 8px; flex-wrap: wrap; margin: 12px 0; }
.attachments > div { width: 90px; }
.el-image { width: 90px; max-width: 100%; height: 90px; border-radius: 10px; }
.attachments .el-button { margin: 0; font-size: 12px; }
.comment { min-width: 0; margin-left: 24px; padding: 20px 0; border-bottom: 1px solid var(--line); }
.comment article { min-width: 0; }
.reply { margin: 14px 0 0; padding-left: 12px; border-left: 2px solid #edf2ef; }
.reply-target { display: flex; align-items: center; gap: 5px; width: fit-content; max-width: 100%; margin: 10px 0 4px; color: var(--ink-3); font-size: 12px; line-height: 1.5; }
.reply-target :deep(.user-hover) { display: inline-flex; }
.reply-target-link,
.reply-target-name { border-radius: 4px; color: var(--brand-hover); font-weight: 500; text-decoration: none; transition: color .18s ease, background-color .18s ease; }
.reply-target-link { padding: 1px; }
.reply-target + :deep(.rich-content) { margin-top: 4px; }
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
.replies-meta { display: flex; align-items: center; gap: 8px; margin: 8px 0 0 12px; }
.reply-toggle { padding: 4px 8px; border: 0; background: none; color: var(--brand-hover); font-size: 12px; cursor: pointer; }
.reply-toggle:hover { text-decoration: underline; }
.load-more-replies { min-height: 32px; border-radius: 8px; }
.comment-load-error { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; padding: 16px 0; color: var(--ink-2); font-size: 13px; }
.comment-highlight { animation: post-comment-highlight 6s ease forwards; }
@keyframes post-comment-highlight {
  0% { background-color: rgba(43, 178, 160, 0.22); }
  100% { background-color: transparent; }
}
@media (max-width: 640px) {
  .discussion { padding: 16px; border-radius: 16px; }
  .heading { gap: 10px; padding-bottom: 14px; }
  .comment { margin-left: 12px; padding: 18px 0; }
  .reply { padding-left: 10px; }
  header { gap: 8px; }
time { margin-left: 42px; }
  }
</style>
