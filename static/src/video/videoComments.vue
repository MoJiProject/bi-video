<template>
  <section class="discussion video-discussion">
    <div class="heading">
      <h2>评论 <span class="comment-total">{{ total }}</span></h2>
      <el-radio-group v-model="sort" size="small" aria-label="评论排序">
        <el-radio-button value="hot">最热</el-radio-button>
        <el-radio-button value="new">最新</el-radio-button>
      </el-radio-group>
    </div>

    <form v-if="loggedIn" class="comment-form" @submit.prevent="send">
      <PostEditor ref="input" v-model="content" compact :max-length="1000" :disabled="sending"
        :placeholder="placeholder" @length="contentLength = $event">
        <template #tools>
          <label class="editor-tool comment-upload" :class="{ disabled: sending || uploading || images.length >= 9 }">
            <svg viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="3" width="18" height="18" rx="4"/><circle cx="8" cy="8" r="1.5"/><path d="m3 17 5-5 4 4 4-6 5 7"/></svg>
            <span>图片</span><span v-if="images.length">{{ images.length }}/9</span>
            <input type="file" accept="image/png,image/jpeg,image/gif" multiple
              aria-label="添加评论图片，最多九张" :disabled="sending || uploading || images.length >= 9" @change="upload">
          </label>
          <button type="button" class="editor-tool share-tool" :class="{ checked: shareToDynamic }"
            :aria-pressed="shareToDynamic" :disabled="sending" @click="shareToDynamic = !shareToDynamic">
            <img class="coin" :src="shareToDynamic ? '/img/投币复选框选中.svg' : '/img/投币复选框.svg'" alt="">
            <span>同时转发到我的动态</span>
          </button>
        </template>
        <template #actions>
          <el-button type="primary" native-type="submit" :loading="sending || uploading">发布评论</el-button>
        </template>
      </PostEditor>
      <div v-if="images.length" class="attachments">
        <div v-for="(image, index) in images" :key="image">
          <el-image :src="image" :preview-src-list="images" preview-teleported fit="cover"/>
          <el-button text type="danger" @click="images.splice(index, 1)">移除</el-button>
        </div>
      </div>
    </form>
    <p v-else class="login-hint">请先
      <button type="button" class="login-hint-btn" @click="store.setLoginDialogVisibleFlag(true)">登录</button>
      后发表评论
    </p>

    <div class="comment-list">
      <div v-for="root in visibleRoots" :key="root.id" class="comment">
        <article class="content-action-surface" :data-id="root.id" :class="{ 'comment-animation': highlightId === root.id }">
          <header>
            <UserHoverCard :user-id="root.authorId" :avatar="root.authorAvatar"
              :nickname="root.authorNickname" :avatar-size="32"/>
            <LevelBadge v-if="root.authorLevel" class="author-level" :level="root.authorLevel" size="xs"/>
            <span v-if="isVideoAuthor(root)" class="author-badge">作者</span>
            <span v-if="root.pinned" class="pin-badge">置顶</span>
            <time>{{ formatTime(root.createdAt) }}</time>
            <ContentMoreMenu v-if="canManage(root)" label="更多评论操作" :disabled="busy.has(root.id)">
              <el-dropdown-item v-if="canPin" :disabled="busy.has(root.id)" @click="togglePin(root)">
                {{ root.pinned ? '取消置顶' : '置顶' }}
              </el-dropdown-item>
              <el-dropdown-item v-if="root.authorId === store.userId" class="is-danger"
                :disabled="busy.has(root.id)" @click="remove(root)">删除</el-dropdown-item>
            </ContentMoreMenu>
          </header>
          <richContent :content="root.content"/>
          <img v-if="(root.imageUrls?.length || 0) > 1" class="note-mark" src="/img/笔记.png" alt="">
          <div v-if="root.imageUrls?.length" class="comment-images">
            <el-image v-for="image in root.imageUrls" :key="image" :src="image"
              :preview-src-list="root.imageUrls" preview-teleported fit="cover"/>
          </div>
          <footer>
            <el-button text :type="root.liked ? 'primary' : ''" :aria-pressed="!!root.liked"
              :disabled="busy.has(root.id)" @click="like(root, false)">
              <svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/></svg>
              <span>{{ root.likeCount || 0 }} 赞</span>
            </el-button>
            <el-button text :type="root.disliked ? 'primary' : ''" :aria-pressed="!!root.disliked"
              :disabled="busy.has(root.id)" @click="like(root, true)">
              <svg class="comment-action-icon is-dislike" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/></svg>
              <span>踩</span>
            </el-button>
            <el-button text :type="activeReplyId === root.id ? 'primary' : ''" @click="toggleReplyBox(root)">
              <svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M7 18 3 21V6a3 3 0 0 1 3-3h12a3 3 0 0 1 3 3v9a3 3 0 0 1-3 3H7Z"/><path d="M7 8h10M7 12h7"/></svg>
              <span>回复</span>
            </el-button>
          </footer>
          <div v-if="activeReplyId === root.id" class="reply-form">
            <PostEditor :ref="setReplyInput" v-model="replyContent" compact :max-length="1000" :disabled="replySending"
              :placeholder="`回复 @${root.authorNickname}`" @length="replyContentLength = $event">
              <template #tools>
                <button type="button" class="editor-tool share-tool" :class="{ checked: replyShareToDynamic }"
                  :aria-pressed="replyShareToDynamic" :disabled="replySending" @click="replyShareToDynamic = !replyShareToDynamic">
                  <img class="coin" :src="replyShareToDynamic ? '/img/投币复选框选中.svg' : '/img/投币复选框.svg'" alt="">
                  <span>同时转发到我的动态</span>
                </button>
              </template>
              <template #actions>
                <el-button type="primary" :loading="replySending" @click="sendReply(root)">回复</el-button>
              </template>
            </PostEditor>
          </div>

          <article v-for="child in root.replies" :key="child.id"
            class="content-action-surface reply" :data-id="child.id" :class="{ 'comment-animation': highlightId === child.id }">
            <header>
              <UserHoverCard :user-id="child.authorId" :avatar="child.authorAvatar"
                :nickname="child.authorNickname" :avatar-size="28"/>
              <LevelBadge v-if="child.authorLevel" class="author-level" :level="child.authorLevel" size="xs"/>
              <span v-if="isVideoAuthor(child)" class="author-badge">作者</span>
              <time>{{ formatTime(child.createdAt) }}</time>
              <ContentMoreMenu v-if="child.authorId === store.userId" label="更多回复操作" :disabled="busy.has(child.id)">
                <el-dropdown-item class="is-danger" :disabled="busy.has(child.id)" @click="remove(child)">删除</el-dropdown-item>
              </ContentMoreMenu>
            </header>
            <small v-if="child.replyToNickname">回复 @{{ child.replyToNickname }}</small>
            <richContent :content="child.content"/>
            <img v-if="(child.imageUrls?.length || 0) > 1" class="note-mark" src="/img/笔记.png" alt="">
            <div v-if="child.imageUrls?.length" class="comment-images">
              <el-image v-for="image in child.imageUrls" :key="image" :src="image"
                :preview-src-list="child.imageUrls" preview-teleported fit="cover"/>
            </div>
            <footer>
              <el-button text :type="child.liked ? 'primary' : ''" :aria-pressed="!!child.liked"
                :disabled="busy.has(child.id)" @click="like(child, false)">
                <svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/></svg>
                <span>{{ child.likeCount || 0 }} 赞</span>
              </el-button>
              <el-button text :type="child.disliked ? 'primary' : ''" :aria-pressed="!!child.disliked"
                :disabled="busy.has(child.id)" @click="like(child, true)">
                <svg class="comment-action-icon is-dislike" viewBox="0 0 24 24" aria-hidden="true"><path d="M8 10H4v10h4m0-10 4-7h1a2 2 0 0 1 2 2l-1 5h5a2 2 0 0 1 2 2l-2 7a2 2 0 0 1-2 1H8V10Z"/></svg>
                <span>踩</span>
              </el-button>
              <el-button text :type="activeReplyId === child.id ? 'primary' : ''" @click="toggleReplyBox(child)">
                <svg class="comment-action-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M7 18 3 21V6a3 3 0 0 1 3-3h12a3 3 0 0 1 3 3v9a3 3 0 0 1-3 3H7Z"/><path d="M7 8h10M7 12h7"/></svg>
                <span>回复</span>
              </el-button>
            </footer>
            <div v-if="activeReplyId === child.id" class="reply-form">
              <PostEditor :ref="setReplyInput" v-model="replyContent" compact :max-length="1000" :disabled="replySending"
                :placeholder="`回复 @${child.authorNickname}`" @length="replyContentLength = $event">
                <template #tools>
                  <button type="button" class="editor-tool share-tool" :class="{ checked: replyShareToDynamic }"
                    :aria-pressed="replyShareToDynamic" :disabled="replySending" @click="replyShareToDynamic = !replyShareToDynamic">
                    <img class="coin" :src="replyShareToDynamic ? '/img/投币复选框选中.svg' : '/img/投币复选框.svg'" alt="">
                    <span>同时转发到我的动态</span>
                  </button>
                </template>
                <template #actions>
                  <el-button type="primary" :loading="replySending" @click="sendReply(child)">发布评论</el-button>
                </template>
              </PostEditor>
            </div>
          </article>
        </article>
      </div>

      <div v-if="!loggedIn && rows.length" class="login-mask">
        <button type="button" class="login-mask-tip" @click="store.setLoginDialogVisibleFlag(true)">
          登录后查看 {{ total }} 条评论
        </button>
      </div>
    </div>

    <loadingIndicator v-if="loading"/>
    <div v-if="loadError" class="comment-load-error" role="alert">
      <span>{{ loadError }}</span>
      <el-button text type="primary" @click="load">重新加载</el-button>
    </div>
    <AppEmptyState v-if="!loading && !loadError && !rows.length" compact title="暂无评论"/>
    <el-button v-if="loggedIn && hasMore && !loadError" class="load-more" :loading="loading" @click="load">加载更多评论</el-button>
  </section>
</template>

<script setup>
import { computed, nextTick, onUnmounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { commentApi, socialApi } from '@/api/product';
import { get, post } from '@/services/http';
import { sanitizeRichText } from '@/utils/richText';
import { useGlobalStore } from '@/store/store';
import richContent from '@/components/richContent.vue';
import ContentMoreMenu from '@/components/ContentMoreMenu.vue';
import UserHoverCard from '@/components/UserHoverCard.vue';
import PostEditor from '@/components/PostEditor.vue';
import LevelBadge from '@/components/LevelBadge.vue';
import loadingIndicator from '@/components/loadingIndicator.vue';
import AppEmptyState from '@/components/AppEmptyState.vue';

const props = defineProps({ videoId: { type: Number, default: 0 } });
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
const activeReplyId = ref(null);
const replyInput = ref(null);
/** 函数式模板 ref：同一时刻只展开一个回复框，避免 v-for 内字符串 ref 变成数组 */
function setReplyInput(el) {
  if (el) replyInput.value = el;
}
const replyContent = ref('');
const replyContentLength = ref(0);
const replySending = ref(false);
const replyShareToDynamic = ref(false);
const sending = ref(false);
const uploading = ref(false);
const shareToDynamic = ref(false);
const busy = ref(new Set());
const highlightId = ref(null);
let generation = 0;

// 视频页评论独有的趣味占位文案
const placeholder = [
  '下面我简单喵两句',
  '与其赞同别人的话语，不如自己畅所欲言',
  '轻轻敲响沉睡的心灵，让我看看你的点评',
  '这里是评论区，不是无人区;-)',
  '万水千山总是情，评论两句行不行',
  '天青色等烟雨，评论区在等你',
  '尊重是评论打动人心的入场券',
  '这里需要一条查重率0%的评论',
  '哎呀，不错哦，发条评论吧',
][Math.floor(Math.random() * 9)];

// 深链：?commentId=&replyId= 从消息中心点进来
const deepParams = new URLSearchParams(window.location.search);
const deepCommentId = parseInt(deepParams.get('commentId')) || null;
const deepReplyId = parseInt(deepParams.get('replyId')) || null;
let deepLinkHandled = false;

const loggedIn = computed(() => store.userId !== null);
const canPin = computed(() => store.userId !== null && String(store.userId) === String(store.upUserId));
const visibleRoots = computed(() => (loggedIn.value ? rows.value : rows.value.slice(0, 2)));

function isVideoAuthor(comment) {
  return store.upUserId !== null && String(comment.authorId) === String(store.upUserId);
}
function canManage(comment) {
  return comment.authorId === store.userId || canPin.value;
}
function formatTime(value) {
  return (value || '').replace('T', ' ');
}
function signedIn() {
  if (store.token) return true;
  store.loginDialogVisible = true;
  return false;
}

async function load() {
  if (loading.value || !props.videoId) return;
  const current = generation;
  loading.value = true;
  loadError.value = '';
  try {
    const page = await commentApi.list({
      videoId: props.videoId, cursorId: cursorId.value, size: 10, sort: sort.value,
    });
    if (current !== generation) return;
    rows.value.push(...page.records);
    total.value = page.total;
    store.setCommentNumber(page.total);
    emit('count', page.total);
    cursorId.value = page.cursorId;
    hasMore.value = page.hasMore;
    handleDeepLink();
  } catch (error) {
    if (current === generation) {
      loadError.value = error.message || '评论加载失败';
      ElMessage.error(loadError.value);
    }
  } finally {
    if (current === generation) loading.value = false;
  }
}
function reload() {
  generation++;
  rows.value = [];
  cursorId.value = null;
  loading.value = false;
  hasMore.value = false;
  return load();
}

/** 深链评论定位：插入目标根评论并高亮，滚动到评论区。 */
async function handleDeepLink() {
  if (deepLinkHandled || !deepCommentId || !rows.value.length) return;
  deepLinkHandled = true;
  try {
    let target = await get(`/comment/${deepCommentId}`);
    if (String(target.videoId) !== String(props.videoId)) return;
    if (target.rootId) {
      const root = await get(`/comment/${target.rootId}`);
      if (String(root.videoId) !== String(props.videoId)) return;
      target = root;
    }
    if (!rows.value.some((row) => row.id === target.id)) {
      let insertAt = rows.value.findIndex((row) => !row.pinned);
      if (insertAt === -1) insertAt = rows.value.length;
      rows.value.splice(insertAt, 0, target);
    }
    await highlightAndScroll(deepReplyId || deepCommentId, deepReplyId ? deepCommentId : null);
  } catch (error) {
    /* 深链评论可能已被删除，静默忽略 */
  }
}

/** 展开/收起某条评论下方的回复输入框（视频页独有交互，替代"回到顶部回复"）。 */
function toggleReplyBox(comment) {
  if (!signedIn()) return;
  if (activeReplyId.value === comment.id) {
    activeReplyId.value = null;
    return;
  }
  activeReplyId.value = comment.id;
  replyContent.value = '';
  replyContentLength.value = 0;
  replyShareToDynamic.value = false;
  nextTick(() => replyInput.value?.focus());
}

/** 高亮指定评论并平滑滚动到可视区（发送、深链后定位用）。 */
async function highlightAndScroll(id, fallbackId = null) {
  if (!id) return;
  highlightId.value = null;
  await nextTick();
  highlightId.value = id;
  await nextTick();
  const el = document.querySelector(`.video-discussion [data-id="${id}"]`)
    || (fallbackId && document.querySelector(`.video-discussion [data-id="${fallbackId}"]`));
  el?.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

/** 在某条评论下方直接发布回复，发送后定位到新回复。 */
async function sendReply(target) {
  if (!signedIn() || replySending.value) return;
  if (!replyContentLength.value) {
    ElMessage.warning('请输入回复内容');
    return;
  }
  if (replyContent.value.length > 1000) {
    ElMessage.warning('回复内容（含表情和 @ 用户）过长，请适当缩短');
    return;
  }
  replySending.value = true;
  try {
    const finalContent = sanitizeRichText(await transformContent(replyContent.value));
    if (!finalContent) {
      ElMessage.warning('请输入回复内容');
      return;
    }
    // 后端会把楼中楼自动归位到一级评论下
    const { commentId } = await commentApi.create({
      videoId: props.videoId, content: finalContent, imageUrls: [], rootId: target.id,
    });
    if (replyShareToDynamic.value) {
      try {
        await socialApi.createPost({
          content: finalContent, imageUrls: [], videoId: props.videoId, visibility: 0,
        });
      } catch (error) {
        ElMessage.warning(`回复已发送，转发失败：${error.message}`);
      }
    }
    replyContent.value = '';
    replyContentLength.value = 0;
    replyShareToDynamic.value = false;
    activeReplyId.value = null;
    store.updateVideoInfoFlag = true;
    await reload();
    await highlightAndScroll(commentId);
    ElMessage.success('回复成功');
  } catch (error) {
    ElMessage.error(error.message || '回复失败');
  } finally {
    replySending.value = false;
  }
}

async function upload(event) {
  if (!signedIn()) { event.target.value = ''; return; }
  const files = Array.from(event.target.files);
  if (files.length + images.value.length > 9) {
    ElMessage.warning('最多 9 张图片');
    event.target.value = '';
    return;
  }
  uploading.value = true;
  try {
    for (const file of files) {
      const body = new FormData();
      body.append('file', file);
      const image = await post('/media/image', body);
      images.value.push(image.url);
    }
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    uploading.value = false;
    event.target.value = '';
  }
}

/** 发布前内容管线：网址转链接、时间转可跳转 time-span、BV 链接转视频标题（视频页独有）。 */
async function transformContent(html) {
  let result = (html || '').replace(
    /(https?:\/\/[^\s<>"]+?)(?=\s|&nbsp;|<div>|<\/div>|$)/g,
    '<a href="$1" target="_blank">$1</a>',
  );
  // 时间文本 → 可点击跳转的 time-span（非法时间保持原样），跳转行为由 videoPage 全局处理
  result = result.replace(
    /(?<!<span[^>]*?>)\b\d{1,2}[：:]\d{2}(:\d{2})?(?!<\/span>)/g,
    (match) => {
      const parts = match.split(/[：:]/).map(Number);
      let isValid = false;
      if (parts.length === 2) {
        isValid = parts[0] >= 0 && parts[0] <= 59 && parts[1] >= 0 && parts[1] <= 59;
      } else if (parts.length === 3) {
        isValid = parts[0] >= 0 && parts[1] >= 0 && parts[1] <= 59 && parts[2] >= 0 && parts[2] <= 59;
      }
      return isValid
        ? `<span class="time-span" style="color:#0A8271;cursor:pointer;" data-time="${match}">${match}</span>`
        : match;
    },
  );
  // BV 链接 → 视频标题 + 标题图标（视频页独有）
  const container = document.createElement('div');
  container.innerHTML = result;
  for (const link of container.querySelectorAll('a')) {
    const match = (link.getAttribute('href') || '').match(/BV([0-9]+)/);
    if (!match) continue;
    try {
      const title = await get(`/video/getVideoTitle/${match[1]}`);
      if (title) {
        link.innerHTML = `<img style="width: 14px;height: 14px;padding-right: 2px;transform: translateY(1px);" src="/img/视频标题.png">${title}<br>`;
        link.target = '_blank';
      }
    } catch (error) { /* 标题获取失败时保留原链接 */ }
  }
  return container.innerHTML.replace(/(<div>\s*<br\s*\/?>\s*<\/div>\s*)+$/, '');
}

async function send() {
  if (!signedIn() || sending.value || uploading.value) return;
  if (!contentLength.value && !images.value.length) {
    ElMessage.warning('请输入评论内容');
    return;
  }
  if (content.value.length > 1000) {
    ElMessage.warning('评论内容（含表情和 @ 用户）过长，请适当缩短');
    return;
  }
  sending.value = true;
  try {
    const finalContent = sanitizeRichText(await transformContent(content.value))
      || (images.value.length ? '分享图片' : '');
    const { commentId } = await commentApi.create({
      videoId: props.videoId,
      content: finalContent,
      imageUrls: images.value,
      rootId: null,
    });
    if (shareToDynamic.value) {
      try {
        await socialApi.createPost({
          content: finalContent, imageUrls: images.value, videoId: props.videoId, visibility: 0,
        });
      } catch (error) {
        ElMessage.warning(`评论已发送，转发失败：${error.message}`);
      }
    }
    content.value = '';
    images.value = [];
    shareToDynamic.value = false;
    store.updateVideoInfoFlag = true;
    await reload();
    await highlightAndScroll(commentId);
    ElMessage.success('发送成功');
  } catch (error) {
    ElMessage.error(error.message || '发送失败');
  } finally {
    sending.value = false;
  }
}

async function like(comment, dislike) {
  if (!signedIn()) return;
  busy.value.add(comment.id);
  try {
    const result = await commentApi.like(comment.id, dislike);
    comment.likeCount = result.likeCount;
    comment.liked = !dislike && result.active;
    comment.disliked = dislike && result.active;
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    busy.value.delete(comment.id);
  }
}

async function remove(comment) {
  const confirmed = await ElMessageBox.confirm('确定删除这条评论及其回复？', '删除评论', { type: 'warning' })
    .then(() => true, () => false);
  if (!confirmed) return;
  busy.value.add(comment.id);
  try {
    await commentApi.remove(comment.id);
    await reload();
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    busy.value.delete(comment.id);
  }
}

/** 仅视频作者可置顶根评论；后端排序自动把置顶评论排最前。 */
async function togglePin(comment) {
  busy.value.add(comment.id);
  try {
    const result = await post(`/comment/${comment.id}/pin`);
    ElMessage({
      message: result.pinned ? '置顶成功' : '取消置顶成功',
      type: 'info',
      plain: true,
      duration: 1700,
    });
    await reload();
  } catch (error) {
    ElMessage.error(error.message || '置顶失败');
  } finally {
    busy.value.delete(comment.id);
  }
}

watch([() => props.videoId, sort, () => store.userId], () => {
  if (!props.videoId) return;
  reload();
}, { immediate: true });
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
.comment-upload { position: relative; }
.comment-upload > span + span { color: var(--ink-3); font-size: 11px; }
.comment-upload.disabled { opacity: .55; cursor: not-allowed; }
.comment-upload input { position: absolute; width: 1px; height: 1px; overflow: hidden; clip-path: inset(50%); white-space: nowrap; }
.share-tool { cursor: pointer; }
.share-tool .coin { display: block; flex: none; width: 16px; height: 16px; align-self: center; }
.login-hint { margin: 0 0 16px; color: var(--ink-2); font-size: 13px; }
.login-hint-btn { margin: 0 4px; padding: 0; border: 0; background: none; color: var(--brand-hover); font: inherit; cursor: pointer; }
.login-hint-btn:hover { text-decoration: underline; }
.attachments, .comment-images { display: flex; gap: 8px; flex-wrap: wrap; margin: 12px 0; }
.attachments > div { width: 90px; }
.el-image { width: 90px; max-width: 100%; height: 90px; border-radius: 10px; }
.attachments .el-button { margin: 0; font-size: 12px; }
.comment-list { position: relative; }
.comment { min-width: 0; padding: 24px 0; border-bottom: 1px solid var(--line); }
.comment article { position: relative; min-width: 0; border-radius: 8px; }
.reply { margin: 18px 0 0 40px; padding-left: 16px; border-left: 2px solid #edf2ef; }
header a { display: flex; align-items: center; gap: 10px; min-width: 0; max-width: 100%; color: inherit; text-decoration: none; font-size: 13px; font-weight: 500; }
header a span { min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
header img { flex: none; width: 32px; height: 32px; border-radius: 10px; object-fit: cover; }
.reply header img { width: 28px; height: 28px; border-radius: 8px; }
time, small { color: #8d9794; font-size: 12px; }
time { margin-left: auto; }
.author-level { flex: none; margin-left: -6px; }
.author-badge { display: inline-flex; align-items: center; flex: none; margin-left: -6px; padding: 0 5px; border-radius: 4px; background: linear-gradient(135deg, #f7c069, #ea8a2c); color: #fff; font-family: inherit; font-size: 9px; font-weight: 600; line-height: 14px; transform: translateY(0.5px); user-select: none; }
.pin-badge { flex: none; padding: 1px 6px; border: 1px solid #ff8a4c; border-radius: 3px; color: #ff8a4c; font-size: 12px; line-height: 16px; }
.note-mark { display: block; width: 48px; height: 22px; margin: 10px 0 -4px; object-fit: contain; }
.reply-form { margin: 14px 0 4px; }
.reply .reply-form { margin: 12px 0 0; }
.rich-content { margin: 12px 0; color: #3e4c45; font-size: 13px; }
footer { gap: 6px; }
footer .el-button { height: 34px; margin: 0; padding: 0 10px; border-radius: 8px; font-size: 12px; }
.comment-action-icon { display: inline-block; flex: none; width: 18px; height: 18px; margin-right: 6px; fill: none; stroke: currentColor; stroke-width: 1.7; stroke-linecap: round; stroke-linejoin: round; }
.comment-action-icon.is-dislike { transform: rotate(180deg); }
.login-mask { position: absolute; left: 0; right: 0; bottom: 0; display: flex; align-items: flex-end; justify-content: center; height: 150px; padding-bottom: 18px; background: linear-gradient(180deg, rgba(255, 255, 255, 0), #fff 78%); pointer-events: none; }
.login-mask-tip { pointer-events: auto; padding: 12px 20px; border: 0; border-radius: 6px; background-color: #e3f5f0; color: #0fa68e; font-family: inherit; font-size: 13.5px; cursor: pointer; transition: 0.2s; }
.login-mask-tip:hover { background-color: #d0efe8; }
.comment-animation { animation: video-comment-highlight 6s ease forwards; }
@keyframes video-comment-highlight {
  0% { background-color: #e3f5f0; }
  100% { background-color: transparent; }
}
.load-more { display: flex; min-height: 38px; margin: 20px auto 0; border-radius: 10px; }
.comment-load-error { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; padding: 16px 0; color: var(--ink-2); font-size: 13px; }
@media (max-width: 640px) {
  .discussion { padding: 16px; border-radius: 16px; }
  .heading { gap: 10px; padding-bottom: 14px; }
  .reply { margin-left: 12px; padding-left: 12px; }
  header { gap: 8px; }
  time { margin-left: 42px; }
  .share-tool > span { max-width: 8em; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
}
</style>
