<template>
  <section class="discussion">
    <div class="heading"><h2>评论 {{ total }}</h2>
      <el-radio-group v-model="sort" size="small"><el-radio-button value="hot">最热</el-radio-button><el-radio-button value="new">最新</el-radio-button></el-radio-group>
    </div>
    <form class="comment-form" @submit.prevent="send">
      <el-tag v-if="replyTo" closable @close="replyTo = null">回复 @{{ replyTo.authorNickname }}</el-tag>
      <el-input ref="input" v-model="content" type="textarea" :rows="3" maxlength="1000" show-word-limit placeholder="发一条友善的评论"/>
      <div v-if="images.length" class="attachments">
        <div v-for="(image, index) in images" :key="image"><el-image :src="image" :preview-src-list="images" preview-teleported fit="cover"/>
          <el-button text type="danger" @click="images.splice(index, 1)">移除</el-button></div>
      </div>
      <div class="actions">
        <label>添加图片<input type="file" accept="image/png,image/jpeg,image/gif" multiple :disabled="uploading || images.length >= 9" @change="upload"></label>
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
        <header><a :href="`/home?homeMenu=1&userId=${comment.authorId}`"><img :src="comment.authorAvatar" alt="">{{ comment.authorNickname }}</a>
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
    <AppEmptyState v-if="!loading && !rows.length" compact title="还没有评论" description="分享你的想法，成为第一个留言的人"/>
    <el-button v-if="hasMore" :loading="loading" @click="load">加载更多评论</el-button>
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
.discussion { padding: 24px; background: #fff; border-top: 1px solid #eff3f2; }
.heading, header, footer, .actions { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.heading { justify-content: space-between; margin-bottom: 20px; }
h2 { margin: 0; font-size: 18px; }
.comment-form .el-textarea { margin: 10px 0; }
.actions { justify-content: flex-end; font-size: 13px; color: #0a8271; }
.actions label { margin-right: auto; max-width: 100%; }
.actions input { display: block; max-width: 100%; }
.attachments, .comment-images { display: flex; gap: 8px; flex-wrap: wrap; margin: 12px 0; }
.attachments > div { width: 90px; }
.el-image { width: 90px; height: 90px; border-radius: 6px; }
.comment { padding: 20px 0; border-bottom: 1px solid #eff3f2; }
.reply { margin: 16px 0 0 36px; }
header a { display: flex; align-items: center; gap: 8px; color: inherit; text-decoration: none; }
header img { width: 32px; height: 32px; border-radius: 50%; }
time, small { color: #8d9794; font-size: 12px; }
.rich-content { margin: 10px 0; }
.emoji-list { display: grid; grid-template-columns: repeat(6, 1fr); max-height: 240px; overflow: auto; }
.emoji-list button { border: 0; background: transparent; cursor: pointer; }
.emoji-list img { width: 28px; height: 28px; }
@media (max-width: 640px) { .discussion { padding: 16px 12px; } .reply { margin-left: 16px; } }
</style>
