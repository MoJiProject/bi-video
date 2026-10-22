<template>
  <article class="home-post content-action-surface" :class="{ 'is-navigable': canOpenDetail }" @click="openDetail">
    <header>
      <UserHoverCard class="author" :user-id="dynamic.authorId"
        :avatar="dynamic.authorAvatar" :nickname="dynamic.authorNickname"/>
      <el-tag v-if="dynamic.isPinned" size="small">置顶</el-tag>
      <span v-if="dynamic.sourceType === 2">图文评论</span>
      <a v-if="canOpenDetail" class="post-permalink" :href="detailHref" aria-label="查看动态详情">
        <time>{{ dynamic.createdAt?.replace('T', ' ') }}</time>
      </a>
      <time v-else>{{ dynamic.createdAt?.replace('T', ' ') }}</time>
      <ContentMoreMenu v-if="dynamic.videoId || (dynamic.isSelf && dynamic.sourceType !== 2)"
        label="更多动态操作" :disabled="saving || pinning || deleting">
        <el-dropdown-item v-if="dynamic.videoId" :disabled="saving || pinning || deleting" @click="toggleWatchLater">
          {{ watchLater ? '移出稍后再看' : '稍后再看' }}
        </el-dropdown-item>
        <template v-if="dynamic.isSelf && dynamic.sourceType !== 2">
          <el-dropdown-item :divided="!!dynamic.videoId" :disabled="saving || pinning || deleting" @click="togglePin">{{ dynamic.isPinned ? '取消置顶' : '置顶' }}</el-dropdown-item>
          <el-dropdown-item class="is-danger" :disabled="saving || pinning || deleting" @click="remove">删除</el-dropdown-item>
        </template>
      </ContentMoreMenu>
    </header>
    <h3 v-if="dynamic.title">{{ dynamic.title }}</h3>
    <richContent :content="dynamic.content"/>
    <div v-if="dynamic.imageList?.length" class="images">
      <el-image v-for="image in dynamic.imageList" :key="image" :src="image"
        :preview-src-list="dynamic.imageList" preview-teleported fit="cover"/>
    </div>
    <a v-if="dynamic.videoId" class="post-video" :href="'./video?videoId=BV' + dynamic.videoId" target="_blank" rel="noopener noreferrer">
      <img :src="dynamic.videoCover" alt="">
      <div class="post-video-info">
        <span class="post-video-title">{{ dynamic.videoTitle }}</span>
        <p v-if="dynamic.videoDescription?.trim()" class="post-video-description">{{ dynamic.videoDescription }}</p>
        <div class="post-video-stats">
          <span v-if="dynamic.videoPlayCount != null" title="播放量" :aria-label="`${dynamic.videoPlayCount} 次播放`">
            <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="5" width="18" height="14" rx="3"/><path d="m10 9 5 3-5 3V9Z"/></svg>
            {{ dynamic.videoPlayCount.toLocaleString() }}
          </span>
          <span v-if="dynamic.videoDanmakuCount != null" title="弹幕数" :aria-label="`${dynamic.videoDanmakuCount} 条弹幕`">
            <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="5" width="18" height="14" rx="3"/><path d="M7 10h10M7 14h6"/></svg>
            {{ dynamic.videoDanmakuCount.toLocaleString() }}
          </span>
        </div>
      </div>
    </a>
    <footer v-if="dynamic.sourceType !== 2">
      <el-button class="post-action" :class="{ 'is-liked': liked }" text :type="liked ? 'primary' : ''"
        :aria-pressed="liked" :loading="liking" @click="toggleLike">
        <svg class="post-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/></svg>
        <span>{{ likeCount }} 赞</span>
      </el-button>
      <el-button class="post-action" :class="{ 'is-active': commentsExpanded }" text
        :aria-expanded="commentsExpanded" :aria-controls="commentsId" @click="toggleComments">
        <svg class="post-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M7 18 3 21V6a3 3 0 0 1 3-3h12a3 3 0 0 1 3 3v9a3 3 0 0 1-3 3H7Z"/><path d="M7 8h10M7 12h7"/></svg>
        <span>{{ commentCount }} 评论</span>
      </el-button>
      <el-button class="post-action" text @click="share">
        <svg class="post-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="m14 3 7 7-7 7v-4C8 13 5 15 3 20c0-9 4-13 11-13V3Z"/></svg>
        <span>分享</span>
      </el-button>
    </footer>
    <footer v-else>
      <span class="post-like-summary">
        <svg class="post-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/></svg>
        {{ dynamic.likeCount || 0 }} 赞
      </span>
    </footer>
    <postComments v-if="commentsMounted && dynamic.sourceType !== 2" v-show="commentsExpanded"
      :id="commentsId" :key="dynamic.id" class="inline-discussion" :post-id="dynamic.id" @click.stop @count="updateCommentCount"/>
  </article>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { socialApi, videoApi } from '@/api/product';
import richContent from '@/components/richContent.vue';
import ContentMoreMenu from '@/components/ContentMoreMenu.vue';
import UserHoverCard from '@/components/UserHoverCard.vue';
import postComments from '@/dynamic/postComments.vue';
import { useGlobalStore } from '@/store/store';
import { post } from '@/services/http';

const props = defineProps({
  dynamic: { type: Object, required: true },
  detail: { type: Boolean, default: false },
});
const emit = defineEmits(['changed', 'comment-count']);
const store = useGlobalStore();
const router = useRouter();
const canOpenDetail = computed(() => !props.detail && props.dynamic.sourceType !== 2);
const detailHref = computed(() => `/dynamicDetail?dynamicId=${props.dynamic.id}`);
const commentsId = computed(() => `post-comments-${props.dynamic.id}`);
const commentsExpanded = ref(props.detail);
const commentsMounted = ref(props.detail);
const commentCount = ref(props.dynamic.commentCount || 0);
function toggleComments() {
  commentsExpanded.value = !commentsExpanded.value;
  if (commentsExpanded.value) commentsMounted.value = true;
}
function updateCommentCount(count) {
  commentCount.value = count;
  emit('comment-count', count);
}
watch(() => props.dynamic.commentCount, value => { commentCount.value = value || 0; });
watch(() => props.dynamic.id, () => {
  commentsExpanded.value = props.detail;
  commentsMounted.value = props.detail;
  commentCount.value = props.dynamic.commentCount || 0;
});
function openDetail(event) {
  if (!canOpenDetail.value || event.defaultPrevented || event.button !== 0 ||
    event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;
  if (!(event.target instanceof Element) ||
    event.target.closest('a, button, input, textarea, select, label, [role="button"], [role="menu"], [role="menuitem"], .el-image, .el-image-viewer__wrapper')) return;
  if (window.getSelection()?.toString()) return;
  router.push({ path: '/dynamicDetail', query: { dynamicId: props.dynamic.id } });
}
const deleting = ref(false);
const liked = ref(!!props.dynamic.liked);
const likeCount = ref(props.dynamic.likeCount || 0);
const liking = ref(false);
const pinning = ref(false);
const saving = ref(false);
const watchLater = ref(!!props.dynamic.watchLater);
watch(() => [props.dynamic.id, props.dynamic.watchLater], () => { watchLater.value = !!props.dynamic.watchLater; });
async function togglePin() {
  if (pinning.value || deleting.value || saving.value) return;
  pinning.value = true;
  try { await socialApi.pin(props.dynamic.id); emit('changed'); }
  catch (error) { ElMessage.error(error.message); }
  finally { pinning.value = false; }
}
async function toggleWatchLater() {
  if (saving.value || pinning.value || deleting.value) return;
  if (!store.token) { store.loginDialogVisible = true; return; }
  saving.value = true;
  try { watchLater.value = await videoApi.watchLater(props.dynamic.videoId); }
  catch (error) { ElMessage.error(error.message); }
  finally { saving.value = false; }
}
async function share() {
  const url = `${location.origin}${detailHref.value}`;
  try {
    if (!navigator.clipboard) {
      await ElMessageBox.alert(url, '复制动态链接');
      return;
    }
    await navigator.clipboard.writeText(url);
    ElMessage.success('动态链接已复制');
  } catch (error) { ElMessage.error(error.message || '复制失败，请手动复制地址栏中的链接'); }
}
watch(() => [props.dynamic.id, props.dynamic.liked, props.dynamic.likeCount], () => {
  liked.value = !!props.dynamic.liked;
  likeCount.value = props.dynamic.likeCount || 0;
});
async function toggleLike() {
  if (!store.token) { store.loginDialogVisible = true; return; }
  liking.value = true;
  try {
    const result = await post(`/post/${props.dynamic.id}/like`);
    likeCount.value = result.likeCount;
    liked.value = result.liked;
  } catch (error) { ElMessage.error(error.message); }
  finally { liking.value = false; }
}

async function remove() {
  if (pinning.value || deleting.value || saving.value) return;
  const confirmed = await ElMessageBox.confirm('确定删除这条动态？', '删除动态', {
    type: 'warning',
  }).then(() => true, () => false);
  if (!confirmed) return;
  deleting.value = true;
  try {
    await socialApi.deletePost(props.dynamic.id);
    store.deleteFlag = props.dynamic.id;
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    deleting.value = false;
  }
}
</script>

<style scoped>
.home-post { container: post-card / inline-size; box-sizing: border-box; min-width: 0; margin: 0; padding: 22px; border: 1px solid var(--line); border-radius: 16px; background: #fff; color: var(--ink); font-family: var(--el-font-family); font-size: 14px; line-height: 1.6; overflow-wrap: anywhere; }
.home-post.is-navigable { cursor: pointer; }
.home-post > header { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.home-post > header .author { flex: 1 1 160px; min-width: 0; margin: 0; }
.home-post > header time { color: var(--ink-3); font-size: 12px; }
.home-post > h3 { margin: 18px 0 12px; font-size: 18px; font-weight: 600; line-height: 1.6; }
.home-post > .rich-content { max-width: none; margin: 16px 0; color: var(--ink-2); font-size: 14px; line-height: 1.8; }
.home-post > .images { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px; max-width: 780px; }
.images .el-image { min-width: 0; width: 100%; aspect-ratio: 1; border-radius: 10px; }
.images .el-image:only-child { grid-column: 1 / -1; max-width: 440px; aspect-ratio: 4 / 3; }
.post-video { box-sizing: border-box; display: grid; grid-template-columns: min(40%, 180px) minmax(0, 1fr); align-items: stretch; gap: 16px; width: 100%; min-width: 0; margin: 16px 0; padding: 10px; border: 1px solid var(--line); border-radius: 12px; background: var(--fill-soft); color: var(--ink); font-size: 14px; font-weight: 500; line-height: 1.7; text-decoration: none; }
.post-video > img { align-self: start; width: 100%; height: auto; aspect-ratio: 16 / 10; border-radius: 8px; object-fit: cover; }
.post-video-info { display: flex; flex-direction: column; gap: 4px; min-width: 0; min-height: 0; overflow: hidden; overflow-wrap: anywhere; }
.post-video-title { display: -webkit-box; flex: none; overflow: hidden; -webkit-box-orient: vertical; -webkit-line-clamp: 2; line-height: 20px; }
.post-video-description { display: -webkit-box; flex: none; margin: 0; overflow: hidden; -webkit-box-orient: vertical; -webkit-line-clamp: 4; color: var(--ink-2); font-size: 12px; font-weight: 400; line-height: 20px; white-space: pre-line; }
.post-video-stats { display: flex; flex: none; gap: 8px; margin-top: auto; color: var(--ink-3); font-size: 12px; font-weight: 400; line-height: 20px; }
.post-video-stats > span { display: inline-flex; align-items: center; gap: 5px; min-width: 0; white-space: nowrap; }
.post-video-stats svg { display: block; flex: none; width: 16px; height: 16px; fill: none; stroke: currentColor; stroke-width: 1.6; stroke-linecap: round; stroke-linejoin: round; }
.post-video-stats:empty { display: none; }
.post-video:hover .post-video-title { color: var(--brand-hover); }
.home-post > header .content-more { margin-left: auto; }
.home-post > footer { display: flex; align-items: center; gap: 8px; margin-top: 18px; padding-top: 12px; border-top: 1px solid var(--line); color: var(--ink-3); font-size: 13px; }
.home-post .post-action { display: inline-flex; flex: 1; justify-content: center; align-items: center; gap: 0; min-width: 0; min-height: 38px; height: auto; margin: 0; padding: 8px 10px; border-radius: 9px; color: var(--ink-2); font-size: 13px; line-height: 1.5; text-decoration: none; }
.home-post .post-action:hover { background: var(--fill-soft); color: var(--brand-hover); }
.home-post .post-action.is-liked { color: var(--brand-hover); }
.home-post .post-action.is-active { color: var(--brand-hover); background: var(--brand-light-9); }
.home-post > .inline-discussion { margin-top: 16px; padding: 20px 0 0; border: 0; border-top: 1px solid var(--line); border-radius: 0; cursor: auto; }
.post-action:focus-visible, .post-video:focus-visible { outline: 2px solid var(--brand); outline-offset: 2px; }
.home-post .post-action-icon { display: inline-block; flex: none; width: 18px; height: 18px; margin-right: 7px; fill: none; stroke: currentColor; stroke-width: 1.7; stroke-linecap: round; stroke-linejoin: round; vertical-align: middle; }
.post-like-summary { display: inline-flex; align-items: center; color: var(--ink-2); }
.post-permalink { display: inline-flex; margin: 0; color: var(--ink-3); font-size: 13px; text-decoration: none; }
.post-permalink:hover { color: var(--brand-hover); }
.post-permalink:focus-visible { outline: 2px solid var(--brand); outline-offset: 3px; border-radius: 3px; }
@container post-card (max-width: 560px) {
  .post-video { grid-template-columns: min(40%, 140px) minmax(0, 1fr); gap: 10px; }
  .post-video-description { -webkit-line-clamp: 3; }
}
@container post-card (max-width: 400px) {
  .post-video { grid-template-columns: min(40%, 120px) minmax(0, 1fr); padding: 8px; }
  .post-video-description { -webkit-line-clamp: 2; }
}
@media (max-width: 640px) {
  .home-post { padding: 16px; border-radius: 14px; }
  .home-post > header { gap: 8px; }
  .home-post > header .author { flex-basis: 140px; }
  .home-post > h3 { margin-top: 16px; font-size: 16px; }
  .home-post > .images { gap: 6px; }
  .post-video { gap: 10px; padding: 8px; font-size: 13px; }
  .home-post .post-action { padding: 8px 4px; font-size: 12px; }
  .home-post > .inline-discussion { padding: 16px 0 0; border-radius: 0; }
}
</style>
