<template>
  <div
    ref="panelRoot"
    class="watch-together"
    :class="{ 'in-fullscreen': fullscreen, 'align-right': panelAlignRight }"
    :style="panelStyle"
  >
    <button
      class="watch-trigger"
      type="button"
      title="一起看"
      @pointerdown="startDrag"
      @click="togglePanel"
    >
      <span class="watch-trigger-icon">▶</span>
      <span class="watch-trigger-text">一起看</span>
      <span v-if="room" class="watch-count">{{ onlineCount }}</span>
    </button>

    <section v-if="panelOpen" class="watch-panel">
      <header class="watch-header">
        <div>
          <strong>{{ room ? `${room.ownerName}的房间` : '多人一起看' }}</strong>
          <small class="connection-status">
            <span v-if="room" class="connection-dot" :class="{ reconnecting: !connected }"></span>
            {{ statusText }}
          </small>
        </div>
        <button class="icon-button" type="button" title="关闭" @click="panelOpen = false">×</button>
      </header>

      <div v-if="!room" class="watch-empty">
        <button class="primary-button" type="button" :disabled="busy" @click="createRoom">
          创建房间
        </button>
<button
          class="secondary-button"
          :class="{ 'secondary-button-active': historyOpen }"
          type="button"
          :disabled="busy"
          @click="toggleHistory"
        >
          我的房间
        </button>
      </div>

      <template v-else>
        <div class="watch-actions">
          <button
            class="secondary-button"
            :class="{ 'secondary-button-active': inviteOpen }"
            type="button"
            @click="toggleInvitePanel"
          >
            邀请好友
          </button>
          <button
            v-if="canManage"
            class="secondary-button"
            :class="{ 'secondary-button-active': historyOpen }"
            type="button"
            @click="toggleHistory"
          >
            历史房间
          </button>
          <button
            v-if="canManage"
            class="secondary-button"
            :class="{ 'secondary-button-active': switchOpen }"
            type="button"
            @click="toggleSwitchPanel"
          >
            切换视频
          </button>
          <button class="leave-button" type="button" @click="leaveRoom">退出</button>
        </div>

        <div class="member-heading">
          <span>成员</span>
          <span>{{ onlineCount }} 人在线</span>
        </div>
        <ul class="member-list">
          <li v-for="member in sortedParticipants" :key="member.userId">
            <div class="member-avatar-wrap clickable" @click="openHome(member)">
              <img
                class="member-avatar"
                :src="member.avatarAddress || '/img/默认头像.gif'"
                :alt="member.userName"
              />
              <span class="online-dot" :class="{ offline: !member.online }"></span>
            </div>
            <div class="member-info">
              <span class="member-name">
                <span class="member-name-text clickable" @click="openHome(member)">{{ member.userName }}</span>
                <img
                  class="member-level"
                  :src="`/img/${member.grade ?? 0}级.png`"
                  :alt="`${member.grade ?? 0}级`"
                />
                <img
                  v-if="member.gender === 1"
                  class="member-gender"
                  src="/img/man2.png"
                  alt="男"
                />
                <img
                  v-else-if="member.gender === 2"
                  class="member-gender"
                  src="/img/woman2.png"
                  alt="女"
                />
                <small v-if="member.userId === currentUserId">我</small>
              </span>
              <span v-if="member.blacklisted" class="role-label blocked">已拉黑</span>
              <span v-else-if="member.owner" class="role-label owner">房主</span>
              <span v-else-if="member.admin" class="role-label">管理员</span>
              <span v-else class="member-status">{{ member.online ? '正在一起看' : '已离线' }}</span>
            </div>
            <div v-if="member.userId !== currentUserId" class="member-actions">
              <button
                v-if="canTransferOwner(member)"
                class="icon-button transfer"
                type="button"
                title="转让房主"
                @click="transferOwner(member)"
              >
                ⇄
              </button>
              <button
                v-if="isOwner && !member.owner && !member.blacklisted"
                class="icon-button"
                type="button"
                :title="member.admin ? '收回管理权限' : '授予管理权限'"
                @click="changeAdmin(member, !member.admin)"
              >
                {{ member.admin ? '−' : '+' }}
              </button>
              <button
                v-if="canKick(member)"
                class="icon-button kick"
                type="button"
                title="移出房间"
                @click="kick(member)"
              >
                ×
              </button>
              <button
                v-if="canBlacklist(member)"
                class="icon-button block-action"
                :class="{ blocked: member.blacklisted }"
                type="button"
                :title="member.blacklisted ? '解除拉黑' : '拉黑'"
                @click="toggleBlacklist(member)"
              >
                {{ member.blacklisted ? '○' : '⊘' }}
              </button>
            </div>
          </li>
        </ul>
      </template>

      <div v-if="inviteOpen" class="invite-section">
        <div class="member-heading">
          <span>邀请好友</span>
          <button class="icon-button" type="button" title="关闭" @click="closeSections()">×</button>
        </div>
        <div v-if="!inviteFriends.length" class="empty-text">
          {{ inviteLoading ? '好友加载中…' : '还没有互相关注的好友' }}
        </div>
        <div v-else class="invite-list" @scroll="onInviteScroll">
          <button
            v-for="friend in inviteFriends"
            :key="friend.id"
            class="invite-item"
            :class="{ selected: inviteSelected.includes(friend.id) }"
            type="button"
            @click="toggleInviteSelect(friend)"
          >
            <img
              class="invite-avatar"
              :src="friend.avatarAddress || '/img/默认头像.gif'"
              :alt="friend.userName"
            />
            <span class="invite-name">{{ friend.userName }}</span>
            <img
              v-if="inviteSelected.includes(friend.id)"
              class="invite-check"
              src="/img/选中.png"
              alt="已选择"
            />
          </button>
          <p v-if="inviteLoading" class="invite-loading">加载中…</p>
        </div>
        <div class="invite-footer">
          <button class="invite-copy-btn" type="button" @click="copyInvite">复制邀请链接</button>
          <button
            class="invite-send-btn"
            type="button"
            :disabled="!inviteSelected.length || inviteSending"
            @click="sendInvites"
          >
            {{ inviteSending ? '发送中' : `发送(${inviteSelected.length})` }}
          </button>
        </div>
      </div>

      <div v-if="switchOpen" class="switch-section">
        <div class="member-heading">
          <span>切换视频</span>
          <button class="icon-button" type="button" title="关闭" @click="closeSections()">×</button>
        </div>
        <div class="switch-search">
          <input
            v-model="switchKeyword"
            class="switch-search-input"
            type="text"
            maxlength="50"
            placeholder="搜索视频标题、UP主或BV号"
            @keyup.enter="searchSwitchVideos"
          />
          <button class="switch-search-btn" type="button" :disabled="searching" @click="searchSwitchVideos">
            {{ searching ? '搜索中' : '搜索' }}
          </button>
        </div>
        <p class="switch-tip">切换后房间内所有人会跳转到新视频，当前进度会重置</p>
        <div v-if="switchVideos.length" class="switch-list">
          <button
            v-for="item in switchVideos"
            :key="item.videoId"
            class="switch-item"
            type="button"
            @click="confirmSwitchVideo(item)"
          >
            <span
              class="switch-cover"
              :style="item.coverAddress ? { backgroundImage: `url(${item.coverAddress})` } : null"
            ></span>
            <span class="switch-info">
              <span class="switch-title">{{ item.videoTitle }}</span>
              <small class="switch-meta">{{ item.userName }} · {{ item.videoTime }}</small>
            </span>
          </button>
        </div>
        <p v-else class="empty-text">{{ switchSearched ? '没有找到相关视频' : '暂无相关视频，换个关键词试试' }}</p>
      </div>

      <div v-if="historyOpen" class="history-section">
        <div class="member-heading">
          <span>历史房间</span>
          <button class="icon-button" type="button" title="关闭历史" @click="closeSections()">×</button>
        </div>
        <button
          v-for="item in historyRooms"
          :key="item.roomId"
          class="history-item"
          type="button"
          @click="openHistoryRoom(item)"
        >
          <span>BV{{ item.videoId }}</span>
          <small>{{ formatDate(item.updatedAt) }}</small>
        </button>
        <p v-if="historyRooms.length === 0" class="empty-text">暂无可管理的历史房间</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, shallowRef, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import apiClient from '../services/apiClient';
import { createWatchTogetherClient } from '../services/watchTogetherClient';
import {
  registerWatchTogetherBridge,
  unregisterWatchTogetherBridge,
} from '../services/watchTogetherBridge';
import { useGlobalStore } from '../store/store';

const props = defineProps({
  videoElement: {
    type: Object,
    default: null,
  },
  videoId: {
    type: Number,
    default: null,
  },
  fullscreen: {
    type: Boolean,
    default: false,
  },
});

const store = useGlobalStore();
const panelRoot = ref(null);
const videoElement = shallowRef(null);
const initialRoomId = new URLSearchParams(window.location.search).get('room');
const panelOpen = ref(Boolean(initialRoomId));
const historyOpen = ref(false);
const historyRooms = ref([]);
const room = ref(null);
const connected = ref(false);
const busy = ref(false);
const roomId = ref(initialRoomId);
const switchOpen = ref(false);
const switchKeyword = ref('');
const switchVideos = ref([]);
const switchSearched = ref(false);
const searching = ref(false);
const inviteOpen = ref(false);
const inviteFriends = ref([]);
const inviteSelected = ref([]);
const inviteTitle = ref('');
const invitePageNum = ref(1);
const inviteHasMore = ref(true);
const inviteLoading = ref(false);
const inviteSending = ref(false);
let client = null;
let clientToken = null;
let attachedVideo = null;
let applyingRemoteAction = false;
let releaseRemoteTimer = null;
let remoteSeekUntil = 0;
let lastSeekSentAt = 0;
let pendingSeekTimer = null;

let position = ref(loadStoredPosition());
let dragState = null;
let dragFrame = 0;
let pendingDrag = null;
let suppressToggleClick = false;

const currentUserId = computed(() => store.userId);
const PANEL_WIDTH = 340;

// 面板靠右时改为右对齐，避免超出屏幕右边缘。
const panelAlignRight = computed(() => {
  if (!position.value) return false;
  const width = Math.min(PANEL_WIDTH, window.innerWidth - 32);
  return position.value.left + width > window.innerWidth - EDGE_GAP;
});
const panelStyle = computed(() => {
  if (props.fullscreen) {
    if (!position.value) return { top: '4%', right: '2.5%' };
    return {
      top: `${position.value.top}px`,
      left: `${position.value.left}px`,
      right: 'auto',
      '--watch-panel-max-height': 'calc(100vh - 110px)',
    };
  }
  if (!position.value) return null;
  return {
    top: `${position.value.top}px`,
    left: `${position.value.left}px`,
    right: 'auto',
    '--watch-panel-max-height': `${Math.max(160, window.innerHeight - position.value.top - 46)}px`,
  };
});
const isOwner = computed(() => room.value?.ownerId === currentUserId.value);
const canManage = computed(() => isOwner.value || room.value?.adminIds?.includes(currentUserId.value));
const onlineCount = computed(() => room.value?.participants?.filter(member => member.online).length || 0);
const sortedParticipants = computed(() => [...(room.value?.participants || [])].sort((left, right) => {
  const rank = member => member.owner ? 0 : member.admin ? 1 : member.online ? 2 : 3;
  return rank(left) - rank(right);
}));
const statusText = computed(() => {
  if (!room.value) return '创建房间并邀请好友';
  return connected.value ? '同步中' : '正在重连';
});

// 相关推荐、自动连播等组件共用这条房间连接，暴露统一的切换入口，
// 由这里负责确认弹窗与 socket 发送，调用方只管跳转意图。
const watchTogetherBridge = {
  inRoom: () => Boolean(room.value),
  canSwitch: () => canManage.value,
  currentVideoId: () => room.value?.videoId ?? null,
  switchVideo: video => confirmSwitchVideo(video),
  notify: (type, message) => notify(type, message),
};
registerWatchTogetherBridge(watchTogetherBridge);

// 全屏时面板被传送到原生全屏节点内，弹窗必须挂到面板里才能显示在最上层。
function overlayTarget() {
  return panelRoot.value || 'body';
}

function notify(type, message) {
  return ElMessage({ type, message, appendTo: overlayTarget() });
}

function togglePanel() {
  if (suppressToggleClick) {
    suppressToggleClick = false;
    return;
  }
  panelOpen.value = !panelOpen.value;
}

const STORAGE_KEY = 'watchTogetherPosition';
const EDGE_GAP = 8;
const SEEK_THROTTLE = 120;
const INVITE_LIMIT = 10;

function loadStoredPosition() {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw);
    if (!Number.isFinite(parsed?.top) || !Number.isFinite(parsed?.left)) return null;
    return clampPosition(parsed);
  } catch {
    return null;
  }
}

function measureTrigger() {
  const trigger = document.querySelector('.watch-together .watch-trigger');
  return {
    width: trigger?.offsetWidth || 96,
    height: trigger?.offsetHeight || 38,
  };
}

function clampPosition(next, size = null) {
  const measured = size || measureTrigger();
  const maxLeft = Math.max(EDGE_GAP, window.innerWidth - measured.width - EDGE_GAP);
  const maxTop = Math.max(EDGE_GAP, window.innerHeight - measured.height - EDGE_GAP);
  return {
    left: Math.min(Math.max(next.left, EDGE_GAP), maxLeft),
    top: Math.min(Math.max(next.top, EDGE_GAP), maxTop),
  };
}

function startDrag(event) {
  if (event.button !== undefined && event.button !== 0) return;
  const trigger = event.currentTarget;
  const container = trigger.closest('.watch-together');
  if (!container) return;
  const rect = trigger.getBoundingClientRect();
  if (!position.value) {
    position.value = {
      left: rect.left,
      top: rect.top,
    };
  }
  dragState = {
    pointerId: event.pointerId,
    offsetX: event.clientX - rect.left,
    offsetY: event.clientY - rect.top,
    startX: event.clientX,
    startY: event.clientY,
    width: trigger.offsetWidth || 96,
    height: trigger.offsetHeight || 38,
    container,
    moved: false,
  };
  container.classList.add('dragging');
  window.addEventListener('pointermove', onDrag);
  window.addEventListener('pointerup', endDrag);
  window.addEventListener('pointercancel', endDrag);
}

function onDrag(event) {
  if (!dragState || event.pointerId !== dragState.pointerId) return;
  if (!dragState.moved && Math.hypot(event.clientX - dragState.startX, event.clientY - dragState.startY) < 4) {
    return;
  }
  dragState.moved = true;
  pendingDrag = {
    left: event.clientX - dragState.offsetX,
    top: event.clientY - dragState.offsetY,
  };
  if (dragFrame) return;
  dragFrame = window.requestAnimationFrame(applyDrag);
}

function applyDrag() {
  dragFrame = 0;
  if (!pendingDrag || !dragState) return;
  position.value = clampPosition(pendingDrag, dragState);
}

function endDrag(event) {
  if (!dragState || (event && event.pointerId !== dragState.pointerId)) return;
  suppressToggleClick = dragState.moved;
  if (dragFrame) {
    window.cancelAnimationFrame(dragFrame);
    dragFrame = 0;
  }
  if (pendingDrag) {
    position.value = clampPosition(pendingDrag, dragState);
    pendingDrag = null;
  }
  dragState.container?.classList.remove('dragging');
  dragState = null;
  window.removeEventListener('pointermove', onDrag);
  window.removeEventListener('pointerup', endDrag);
  window.removeEventListener('pointercancel', endDrag);
  try {
    if (position.value) window.localStorage.setItem(STORAGE_KEY, JSON.stringify(position.value));
  } catch {
    // 忽略存储失败，不影响拖动。
  }
}

function ensureClient() {
  if (!store.token || (client && clientToken === store.token)) return;
  client?.close();
  clientToken = store.token;
  client = createWatchTogetherClient({
    token: clientToken,
    onMessage: handleMessage,
    onStatus(status) {
      connected.value = status === 'open';
      if (status === 'open' && roomId.value && props.videoId) joinRoom();
      if (status === 'auth_failed') {
        resetRoom();
        notify('warning', '登录已失效，请重新登录后加入房间');
      }
    },
  });
  client.connect();
}

async function createRoom() {
  if (!store.token || !store.userId) {
    notify('info', '请先登录');
    return;
  }
  if (!props.videoId || !videoElement.value) return;

  busy.value = true;
  try {
    const response = await apiClient.post('/watch-together/rooms', {
      videoId: props.videoId,
      currentTime: videoElement.value.currentTime || 0,
      paused: videoElement.value.paused,
      playbackRate: videoElement.value.playbackRate || 1,
    });
    room.value = response.data.data;
    roomId.value = room.value.roomId;
    updateRoomQuery(roomId.value);
    ensureClient();
    if (connected.value) joinRoom();
  } catch {
    notify('error', '房间创建失败，请稍后重试');
  } finally {
    busy.value = false;
  }
}

function joinRoom() {
  if (!client || !roomId.value || !props.videoId) return;
  client.send({
    type: 'join',
    roomId: roomId.value,
    videoId: props.videoId,
  });
}

async function leaveRoom() {
  if (!room.value) return;
  try {
    await ElMessageBox.confirm('退出后将不再同步播放进度，确定退出房间吗？', '退出一起看', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning',
      appendTo: overlayTarget(),
    });
  } catch {
    // 用户取消时无需提示。
    return;
  }
  client?.send({ type: 'leave', roomId: room.value.roomId });
  runRemoteAction(() => videoElement.value?.pause());
  resetRoom();
  notify('success', '已退出一起看房间');
}

function sendPlayback(type) {
  if (!room.value || applyingRemoteAction || !videoElement.value) return;
  client?.send({
    type,
    roomId: room.value.roomId,
    videoId: props.videoId,
    currentTime: videoElement.value.currentTime || 0,
    playbackRate: videoElement.value.playbackRate || 1,
    paused: videoElement.value.paused,
  });
}

// 远程跳转后短时间内产生的 seeked 属于同步结果，不再回播，避免互相回声形成死循环。
function markRemoteSeek() {
  remoteSeekUntil = Date.now() + 600;
}

function sendSeek() {
  const now = Date.now();
  if (now - lastSeekSentAt >= SEEK_THROTTLE) {
    lastSeekSentAt = now;
    sendPlayback('seek');
    return;
  }
  if (pendingSeekTimer) return;
  pendingSeekTimer = window.setTimeout(() => {
    pendingSeekTimer = null;
    lastSeekSentAt = Date.now();
    sendPlayback('seek');
  }, SEEK_THROTTLE - (now - lastSeekSentAt));
}

function handleMessage(message) {
  if (message.type === 'state') {
    room.value = message.room;
    applyRoomState(message.room);
    return;
  }
  if (['play', 'pause', 'seek', 'rate'].includes(message.type)) {
    applyPlaybackMessage(message);
    return;
  }
  if (message.type === 'user_left') {
    room.value = message.room;
    applyRoomState(message.room);
    notify('info', `${message.userName} 已离开，一起看已暂停`);
    return;
  }
  if (message.type === 'kicked') {
    closeClient();
    resetRoom();
    notify('warning', '你已被移出一起看房间');
    return;
  }
  if (message.type === 'blacklisted') {
    closeClient();
    resetRoom();
    notify('warning', '你已被拉黑，无法继续一起看');
    return;
  }
  if (message.type === 'join_denied') {
    closeClient();
    resetRoom();
    notify('warning', message.message || '无法加入一起看房间');
    return;
  }
  if (message.type === 'owner_transferred') {
    notify('success', `房主已转让给 ${message.ownerName}`);
    return;
  }
  if (message.type === 'video_switched') {
    applyVideoSwitch(message.room);
    return;
  }
  if (message.type === 'error') {
    notify('error', message.message || '一起看操作失败');
  }
}

function closeClient() {
  videoElement.value?.pause();
  client?.close();
  client = null;
  clientToken = null;
  connected.value = false;
}

// 房间换了视频：所有端整页跳到新视频，进度由服务端 state 重新同步。
function applyVideoSwitch(nextRoom) {
  room.value = nextRoom;
  switchVideos.value = [];
  switchKeyword.value = '';
  switchSearched.value = false;
  const target = nextRoom?.videoId;
  if (!target || target === props.videoId) {
    if (nextRoom) applyRoomState(nextRoom);
    return;
  }
  const url = new URL(window.location.href);
  url.searchParams.set('videoId', `BV${target}`);
  url.searchParams.set('room', roomId.value || nextRoom.roomId);
  window.location.href = url.toString();
}

function applyRoomState(nextRoom) {
  if (!videoElement.value || !nextRoom) return;
  runRemoteAction(() => {
    if (Math.abs(videoElement.value.currentTime - nextRoom.currentTime) > 0.75) {
      markRemoteSeek();
      videoElement.value.currentTime = nextRoom.currentTime;
    }
    videoElement.value.playbackRate = nextRoom.playbackRate || 1;
    if (nextRoom.paused) {
      videoElement.value.pause();
    } else {
      playRemote();
    }
  });
}

function applyPlaybackMessage(message) {
  if (!videoElement.value) return;
  runRemoteAction(() => {
    if (Math.abs(videoElement.value.currentTime - message.currentTime) > 0.5) {
      markRemoteSeek();
      videoElement.value.currentTime = message.currentTime;
    }
    videoElement.value.playbackRate = message.playbackRate || 1;
    if (message.type === 'pause') videoElement.value.pause();
    if (message.type === 'play') playRemote();
  });
}

// 浏览器不允许自动播放时才提示，避免 play 被 pause 打断时反复弹提示。
function playRemote() {
  const pending = videoElement.value.play();
  if (!pending?.catch) return;
  pending.catch(error => {
    if (error?.name === 'NotAllowedError') notify('info', '点击视频开始同步播放');
  });
}

function runRemoteAction(action) {
  applyingRemoteAction = true;
  window.clearTimeout(releaseRemoteTimer);
  action();
  releaseRemoteTimer = window.setTimeout(() => {
    applyingRemoteAction = false;
  }, 150);
}

async function copyInvite() {
  try {
    await navigator.clipboard.writeText(inviteUrl());
    notify('success', '邀请链接已复制');
  } catch {
    window.prompt('复制邀请链接', inviteUrl());
  }
}

function inviteUrl() {
  const url = new URL(window.location.href);
  url.searchParams.set('room', room.value.roomId);
  return url.toString();
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, char => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  }[char]));
}

function toggleInvitePanel() {
  if (inviteOpen.value) {
    closeSections();
    return;
  }
  switchOpen.value = false;
  historyOpen.value = false;
  inviteOpen.value = true;
  if (!inviteFriends.value.length) loadInviteFriends();
  loadInviteTitle();
}

async function loadInviteTitle() {
  if (!props.videoId || inviteTitle.value) return;
  try {
    const response = await apiClient.get(`/video/getVideoTitle/${props.videoId}`);
    if (response.data.code === 1) inviteTitle.value = response.data.data;
  } catch {
    // 标题获取失败时使用默认文案。
  }
}

async function loadInviteFriends() {
  if (inviteLoading.value || !inviteHasMore.value || !store.userId) return;
  inviteLoading.value = true;
  try {
    const response = await apiClient.get('/user/selectFollowAndFans', {
      params: { userId: store.userId, pageNum: invitePageNum.value },
    });
    const list = Array.isArray(response.data.data) ? response.data.data : [];
    inviteFriends.value = [
      ...inviteFriends.value,
      ...list.filter(item => item?.id && item.id !== store.userId),
    ];
    inviteHasMore.value = list.length > 0;
    invitePageNum.value += 1;
  } catch {
    notify('error', '好友列表加载失败');
  } finally {
    inviteLoading.value = false;
  }
}

function onInviteScroll(event) {
  const target = event.target;
  if (!target) return;
  if (target.scrollHeight - target.scrollTop <= target.clientHeight + 8) loadInviteFriends();
}

function toggleInviteSelect(friend) {
  const selected = inviteSelected.value;
  if (selected.includes(friend.id)) {
    inviteSelected.value = selected.filter(id => id !== friend.id);
    return;
  }
  if (selected.length >= INVITE_LIMIT) {
    notify('info', `最多只能邀请${INVITE_LIMIT}位好友`);
    return;
  }
  inviteSelected.value = [...selected, friend.id];
}

async function sendInvites() {
  const targets = inviteSelected.value;
  if (!targets.length || !room.value) return;
  // 标题本身作为链接文案，私信按 HTML 渲染，点标题即可进房间
  const title = escapeHtml(inviteTitle.value || '这个视频');
  const content = `一起来一起看<a href="${escapeHtml(inviteUrl())}" target="_blank">《${title}》</a>`;
  inviteSending.value = true;
  try {
    const results = await Promise.allSettled(targets.map(receiverId => apiClient.post('/privateMessage/sendMessage', {
      senderId: store.userId,
      receiverId,
      content,
      messageType: 1,
    })));
    const sent = results.filter(item => item.status === 'fulfilled' && item.value?.data?.code === 1).length;
    if (sent > 0) {
      notify('success', `已向${sent}位好友发送邀请`);
      inviteSelected.value = [];
    } else {
      notify('error', '邀请发送失败，请稍后重试');
    }
  } catch {
    notify('error', '邀请发送失败，请稍后重试');
  } finally {
    inviteSending.value = false;
  }
}

function changeAdmin(member, grant) {
  client?.send({
    type: grant ? 'grant_admin' : 'revoke_admin',
    roomId: room.value.roomId,
    targetUserId: member.userId,
  });
}

async function kick(member) {
  try {
    await ElMessageBox.confirm(`确定将 ${member.userName} 移出房间吗？`, '移出成员', {
      confirmButtonText: '移出',
      cancelButtonText: '取消',
      type: 'warning',
      appendTo: overlayTarget(),
    });
    client?.send({
      type: 'kick',
      roomId: room.value.roomId,
      targetUserId: member.userId,
    });
  } catch {
    // 用户取消时无需提示。
  }
}

function canKick(member) {
  if (!canModerate(member) || member.blacklisted) return false;
  return isOwner.value || !member.admin;
}

function canBlacklist(member) {
  return canModerate(member);
}

function canTransferOwner(member) {
  return isOwner.value && !member.owner && !member.blacklisted;
}

function openHome(member) {
  if (!member?.userId) return;
  window.open(`./home?homeMenu=1&userId=${member.userId}`, '_blank');
}

async function transferOwner(member) {
  try {
    await ElMessageBox.confirm(
      `确定把房主转让给 ${member.userName} 吗？转让后你将不再是房主。`,
      '转让房主',
      {
        confirmButtonText: '转让',
        cancelButtonText: '取消',
        type: 'warning',
        appendTo: overlayTarget(),
      }
    );
    client?.send({
      type: 'transfer_owner',
      roomId: room.value.roomId,
      targetUserId: member.userId,
    });
  } catch {
    // 用户取消时无需提示。
  }
}

function canModerate(member) {
  if (!canManage.value || member.owner) return false;
  return isOwner.value || !member.admin;
}

async function toggleBlacklist(member) {
  const block = !member.blacklisted;
  try {
    await ElMessageBox.confirm(
      block
        ? `确定将 ${member.userName} 拉黑吗？拉黑后对方无法再加入本房间。`
        : `确定解除对 ${member.userName} 的拉黑吗？`,
      block ? '拉黑成员' : '解除拉黑',
      {
        confirmButtonText: block ? '拉黑' : '解除拉黑',
        cancelButtonText: '取消',
        type: 'warning',
        appendTo: overlayTarget(),
      }
    );
    client?.send({
      type: block ? 'blacklist' : 'unblacklist',
      roomId: room.value.roomId,
      targetUserId: member.userId,
    });
  } catch {
    // 用户取消时无需提示。
  }
}

function toggleSwitchPanel() {
  if (switchOpen.value) {
    closeSections();
    return;
  }
  inviteOpen.value = false;
  historyOpen.value = false;
  switchOpen.value = true;
  if (!switchVideos.value.length) loadRelatedVideos();
}

async function loadRelatedVideos() {
  if (!props.videoId) return;
  searching.value = true;
  try {
    const response = await apiClient.post('/video/getVideoPageByVideo', { id: props.videoId });
    switchVideos.value = pickSwitchVideos(response.data.data);
  } catch {
    notify('error', '相关视频加载失败');
  } finally {
    searching.value = false;
  }
}

async function searchSwitchVideos() {
  const keyWord = switchKeyword.value.trim();
  if (!keyWord) {
    notify('info', '请输入要搜索的视频');
    return;
  }
  const bvMatch = /^bv?(\d+)$/i.exec(keyWord);
  if (bvMatch) {
    confirmSwitchVideo({ videoId: Number(bvMatch[1]), videoTitle: keyWord.toUpperCase() });
    return;
  }
  searching.value = true;
  try {
    const response = await apiClient.post('/search/searchVideoByKeyWord', {
      userId: store.userId || 0,
      keyWord,
      classifyIndex: '',
      classify: '全部',
      date: 0,
      time: 0,
      sort: 0,
      videoPageNum: 1,
    });
    switchVideos.value = pickSwitchVideos(response.data.data?.selectVideoDtoList);
    switchSearched.value = true;
  } catch {
    notify('error', '搜索失败，请稍后重试');
  } finally {
    searching.value = false;
  }
}

function pickSwitchVideos(list) {
  return (Array.isArray(list) ? list : [])
    .filter(item => item?.videoId && item.videoId !== props.videoId)
    .slice(0, 20);
}

async function confirmSwitchVideo(item) {
  if (!item?.videoId || !room.value) return;
  try {
    await ElMessageBox.confirm(
      `确定把房间切换到《${item.videoTitle}》吗？切换后所有人会跳转到新视频，当前进度将重置。`,
      '切换视频',
      {
        confirmButtonText: '切换',
        cancelButtonText: '取消',
        type: 'warning',
        appendTo: overlayTarget(),
      }
    );
  } catch {
    // 用户取消时无需提示。
    return;
  }
  client?.send({
    type: 'switch_video',
    roomId: room.value.roomId,
    videoId: item.videoId,
  });
}

function toggleHistory() {
  if (historyOpen.value) {
    closeSections();
    return;
  }
  inviteOpen.value = false;
  switchOpen.value = false;
  historyOpen.value = true;
  loadHistory();
}

// 三个扩展区块互斥，关闭时统一走这里，避免状态分散跳动。
function closeSections() {
  inviteOpen.value = false;
  switchOpen.value = false;
  historyOpen.value = false;
}

async function loadHistory() {
  if (!store.token) {
    notify('info', '请先登录');
    return;
  }
  try {
    const response = await apiClient.get('/watch-together/history');
    historyRooms.value = response.data.data || [];
  } catch {
    notify('error', '历史房间加载失败');
  }
}

function openHistoryRoom(item) {
  const url = new URL(window.location.href);
  url.searchParams.set('videoId', `BV${item.videoId}`);
  url.searchParams.set('room', item.roomId);
  window.location.href = url.toString();
}

function formatDate(value) {
  return value ? new Date(value).toLocaleString() : '';
}

function updateRoomQuery(value) {
  const url = new URL(window.location.href);
  if (value) url.searchParams.set('room', value);
  else url.searchParams.delete('room');
  window.history.replaceState(null, '', url);
}

function resetRoom() {
  room.value = null;
  roomId.value = null;
  closeSections();
  inviteSelected.value = [];
  switchVideos.value = [];
  switchKeyword.value = '';
  switchSearched.value = false;
  updateRoomQuery(null);
}

function attachVideo(video) {
  if (attachedVideo === video) return;
  detachVideo();
  attachedVideo = video;
  if (!video) return;
  video.addEventListener('play', onPlay);
  video.addEventListener('pause', onPause);
  video.addEventListener('seeked', onSeeked);
  video.addEventListener('ratechange', onRateChange);
}

function detachVideo() {
  if (!attachedVideo) return;
  attachedVideo.removeEventListener('play', onPlay);
  attachedVideo.removeEventListener('pause', onPause);
  attachedVideo.removeEventListener('seeked', onSeeked);
  attachedVideo.removeEventListener('ratechange', onRateChange);
  attachedVideo = null;
  remoteSeekUntil = 0;
}

function onPlay() {
  sendPlayback('play');
}

function onPause() {
  // 拖动进度条时页面会先暂停再恢复，这里不当作用户暂停同步给房间。
  if (attachedVideo?.seeking) return;
  sendPlayback('pause');
}

function onSeeked() {
  // 远程跳转产生的时间窗内不再回播 seek，避免两个客户端互相回声。
  if (Date.now() < remoteSeekUntil) return;
  sendSeek();
}

function onRateChange() {
  sendPlayback('rate');
}

watch(() => props.videoElement, video => {
  videoElement.value = video;
  attachVideo(video);
}, { immediate: true });
window.addEventListener('resize', onWindowResize);
watch(
  () => [store.token, store.userId, props.videoId],
  ([token, userId, videoId]) => {
    if (!token || !userId) {
      if (client) {
        client.close();
        client = null;
        clientToken = null;
        connected.value = false;
        resetRoom();
      }
      return;
    }
    if (!videoId) return;
    ensureClient();
  },
  { immediate: true }
);

function onWindowResize() {
  if (!position.value) return;
  position.value = clampPosition(position.value);
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(position.value));
  } catch {
    // 忽略存储失败，不影响位置校正。
  }
}

onBeforeUnmount(() => {
  detachVideo();
  unregisterWatchTogetherBridge(watchTogetherBridge);
  window.clearTimeout(releaseRemoteTimer);
  window.clearTimeout(pendingSeekTimer);
  if (dragFrame) window.cancelAnimationFrame(dragFrame);
  window.removeEventListener('resize', onWindowResize);
  window.removeEventListener('pointermove', onDrag);
  window.removeEventListener('pointerup', endDrag);
  window.removeEventListener('pointercancel', endDrag);
  client?.close();
  client = null;
  clientToken = null;
});
</script>

<style scoped>
.watch-together {
  position: fixed;
  top: 108px;
  right: 24px;
  z-index: 100000;
  user-select: none;
  color: #20242b;
  font-family: "Microsoft YaHei", sans-serif;
  animation: watch-panel-appear 0.28s ease both;
}

/* 只做透明度过渡：避免 transform 让挂载在面板内的弹窗脱离视口定位 */
@keyframes watch-panel-appear {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.watch-together.dragging {
  cursor: grabbing;
  user-select: none;
}

.watch-together.in-fullscreen {
  position: absolute;
  z-index: 100000;
}

.watch-trigger,
.primary-button,
.secondary-button,
.leave-button,
.icon-button,
.history-item,
.invite-item,
.switch-item {
  border: 0;
  cursor: pointer;
}

.watch-trigger {
  height: 38px;
  padding: 0 13px;
  display: flex;
  align-items: center;
  gap: 7px;
  color: #fff;
  background: #00aeec;
  box-shadow: 0 5px 16px rgba(0, 174, 236, 0.24);
  border-radius: 6px;
  cursor: grab;
  touch-action: none;
}

.watch-together.dragging .watch-trigger {
  cursor: grabbing;
  will-change: transform;
}

.watch-header {
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #edf0f2;
}

.watch-trigger-icon {
  font-size: 11px;
}

.watch-trigger-text {
  white-space: nowrap;
}

.watch-count {
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #087ea4;
  background: #fff;
  border-radius: 9px;
  font-size: 11px;
}

.watch-panel {
  position: absolute;
  top: 46px;
  left: 0;
  width: min(340px, calc(100vw - 32px));
  max-height: var(--watch-panel-max-height, calc(100vh - 170px));
  overflow: auto;
  background: #fff;
  border: 1px solid #e3e7eb;
  border-radius: 8px;
  box-shadow: 0 16px 42px rgba(24, 33, 43, 0.18);
}

.watch-together.align-right .watch-panel {
  left: auto;
  right: 0;
}

.watch-header,
.member-heading,
.member-list li,
.watch-actions {
  display: flex;
  align-items: center;
}

.watch-header strong,
.watch-header small {
  display: block;
}

.watch-header strong {
  font-size: 14px;
}

.watch-header small {
  margin-top: 3px;
  color: #7a838c;
  font-size: 11px;
}

.watch-empty {
  display: flex;
}

.watch-empty,
.watch-actions {
  gap: 8px;
  padding: 14px 16px;
}

.watch-actions {
  gap: 6px;
  padding: 14px 12px;
  flex-wrap: nowrap;
}

.primary-button,
.secondary-button,
.leave-button {
  min-height: 32px;
  padding: 0 10px;
  border-radius: 5px;
  font-size: 13px;
  white-space: nowrap;
  transition: color 0.16s ease, background-color 0.16s ease;
}

.primary-button {
  flex: 0 1 auto;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #fff;
  background: #00aeec;
}

.primary-button:hover {
  background: #0a9fe0;
}

.secondary-button,
.leave-button {
  flex: 0 0 auto;
}

.secondary-button {
  color: #3a424a;
  background: #edf1f4;
}

/* 只给未激活的按钮加 hover，激活态保持蓝底，避免悬停时颜色来回跳 */
.secondary-button:not(.secondary-button-active):hover {
  background: #dde4ea;
}

.secondary-button-active {
  color: #fff;
  background: #00aeec;
}

.secondary-button-active:hover {
  background: #0a9fe0;
}

.leave-button {
  margin-left: auto;
  color: #d24c4c;
  background: #fff0f0;
}

.leave-button:hover {
  background: #ffe0e0;
}

.primary-button:disabled,
.secondary-button:disabled {
  opacity: 0.55;
  cursor: default;
}

.primary-button:disabled:hover,
.secondary-button:disabled:hover {
  background: #00aeec;
}

.secondary-button:disabled:not(.secondary-button-active):hover {
  background: #edf1f4;
}

.member-heading {
  justify-content: space-between;
  padding: 8px 16px;
  color: #6c7680;
  background: #f7f9fa;
  font-size: 12px;
}

.member-list {
  max-height: 290px;
  margin: 0;
  padding: 6px 0;
  overflow-y: auto;
  list-style: none;
}

.connection-status {
  display: flex !important;
  align-items: center;
  gap: 5px;
}

.connection-dot {
  width: 6px;
  height: 6px;
  background: #20b26b;
  border-radius: 50%;
}

.connection-dot.reconnecting {
  background: #e39a2d;
}

.member-list li {
  min-height: 58px;
  padding: 6px 12px 6px 16px;
  transition: background-color 0.16s ease;
}

.member-list li:hover {
  background: #f7fafb;
}

.member-avatar-wrap {
  position: relative;
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
}

.member-avatar-wrap:hover .member-avatar {
  border-color: #00aeec;
  box-shadow: 0 0 0 3px rgba(0, 174, 236, 0.16);
  transform: scale(1.06);
}

.member-avatar {
  width: 38px;
  height: 38px;
  display: block;
  border: 1px solid #e4e9ed;
  border-radius: 50%;
  object-fit: cover;
  transition: border-color 0.16s ease, box-shadow 0.16s ease, transform 0.16s ease;
}

.member-name {
  max-width: 158px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #252b31;
  font-size: 13px;
  font-weight: 600;
}

.member-name-text {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-name small {
  color: #98a1aa;
  font-size: 10px;
  font-weight: 400;
  white-space: nowrap;
}

.member-gender {
  width: 12px;
  height: 12px;
  flex: 0 0 12px;
}

.member-level {
  width: 18px;
  height: 9px;
  flex: 0 0 18px;
}

.clickable {
  cursor: pointer;
}

.member-name-text.clickable:hover {
  color: #00aeec;
}

.member-info {
  min-width: 0;
  margin-left: 10px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.online-dot {
  position: absolute;
  right: -1px;
  bottom: 0;
  width: 9px;
  height: 9px;
  background: #20b26b;
  border: 2px solid #fff;
  border-radius: 50%;
  transition: transform 0.16s ease, background-color 0.16s ease;
}

.member-avatar-wrap:hover .online-dot {
  transform: scale(1.2);
}

.online-dot.offline {
  background: #b5bdc5;
}

.role-label {
  padding: 1px 6px;
  color: #087ea4;
  background: #e7f7fc;
  border-radius: 3px;
  font-size: 11px;
}

.role-label.owner {
  color: #b85d15;
  background: #fff1e5;
}

.role-label.blocked {
  color: #8a3b3b;
  background: #fdecec;
}

.member-status {
  color: #98a1aa;
  font-size: 11px;
}

.member-actions {
  display: flex;
  gap: 4px;
  margin-left: auto;
}

.icon-button {
  width: 28px;
  height: 28px;
  color: #66717c;
  background: transparent;
  border-radius: 4px;
  font-size: 18px;
}

.icon-button:hover {
  background: #eef2f5;
}

.icon-button.kick {
  color: #5b7c99;
}

.icon-button.kick:hover {
  background: #eaf1f6;
}

.icon-button.block-action {
  color: #d84b4b;
}

.icon-button.block-action:hover {
  background: #fdecec;
}

.icon-button.blocked {
  color: #b85d15;
  background: #fff1e5;
}

.icon-button.transfer {
  color: #7a5cc4;
  font-size: 16px;
}

.icon-button.transfer:hover {
  background: #f1ecfd;
}

.history-section {
  border-top: 1px solid #edf0f2;
}

.invite-section {
  border-top: 1px solid #edf0f2;
}

.invite-list {
  max-height: 208px;
  overflow-y: auto;
  padding: 4px 0;
}

.invite-item {
  width: 100%;
  padding: 6px 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 0;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: background-color 0.16s ease;
}

.invite-item:hover {
  background: #f7fafb;
}

.invite-item.selected {
  background: #eaf7fd;
}

.invite-avatar {
  width: 30px;
  height: 30px;
  flex: 0 0 30px;
  border: 1px solid #e4e9ed;
  border-radius: 50%;
  object-fit: cover;
}

.invite-item.selected .invite-avatar {
  border-color: #00aeec;
}

.invite-name {
  min-width: 0;
  flex: 1 1 auto;
  color: #252b31;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.invite-check {
  width: 16px;
  height: 16px;
  flex: 0 0 16px;
}

.invite-loading {
  margin: 0;
  padding: 6px 12px;
  color: #a9b2ba;
  font-size: 11px;
  text-align: center;
}

.invite-footer {
  display: flex;
  gap: 8px;
  padding: 10px 12px 12px;
}

.invite-copy-btn,
.invite-send-btn {
  flex: 1 1 0;
  min-width: 0;
  height: 32px;
  border: 0;
  border-radius: 5px;
  font-size: 13px;
  cursor: pointer;
  transition: opacity 0.16s ease, background-color 0.16s ease;
}

.invite-copy-btn {
  color: #3a424a;
  background: #edf1f4;
}

.invite-copy-btn:hover {
  background: #e3e9ee;
}

.invite-send-btn {
  color: #fff;
  background: #00aeec;
}

.invite-send-btn:hover {
  background: #0a9fe0;
}

.invite-send-btn:disabled {
  color: #fff;
  background: #cfd6dc;
  cursor: default;
}

.switch-section {
  border-top: 1px solid #edf0f2;
}

.switch-search {
  display: flex;
  gap: 6px;
  padding: 10px 12px 8px;
}

.switch-search-input {
  flex: 1 1 auto;
  min-width: 0;
  height: 30px;
  padding: 0 12px;
  color: #252b31;
  background: #f4f6f8;
  border: 1px solid #e3e7eb;
  border-radius: 15px;
  font-size: 12px;
  outline: none;
  user-select: text;
  transition: background-color 0.16s ease, border-color 0.16s ease;
}

.switch-search-input::placeholder {
  color: #a9b2ba;
}

.switch-search-input:focus {
  background: #fff;
  border-color: #00aeec;
}

.switch-search-btn {
  flex: 0 0 auto;
  height: 30px;
  padding: 0 14px;
  color: #fff;
  background: #00aeec;
  border: 0;
  border-radius: 15px;
  font-size: 12px;
  cursor: pointer;
  transition: opacity 0.16s ease;
}

.switch-search-btn:hover {
  opacity: 0.88;
}

.switch-search-btn:disabled {
  opacity: 0.55;
  cursor: default;
}

.switch-tip {
  margin: 0;
  padding: 0 12px 8px;
  color: #98a1aa;
  font-size: 11px;
  line-height: 1.5;
}

.switch-list {
  max-height: 236px;
  overflow-y: auto;
}

.switch-item {
  width: 100%;
  padding: 8px 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 0;
  background: #fff;
  text-align: left;
  transition: background-color 0.16s ease;
}

.switch-item:hover {
  background: #f7fafb;
}

.switch-cover {
  width: 78px;
  height: 46px;
  flex: 0 0 78px;
  background-color: #eef2f5;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  border-radius: 4px;
}

.switch-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.switch-title {
  color: #252b31;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.switch-item:hover .switch-title {
  color: #00aeec;
}

.switch-meta {
  color: #98a1aa;
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-item {
  width: 100%;
  padding: 10px 16px;
  display: flex;
  justify-content: space-between;
  color: #2e363d;
  background: #fff;
  text-align: left;
}

.history-item:hover {
  background: #f5f8fa;
}

.history-item small,
.empty-text {
  color: #87919a;
  font-size: 11px;
}

.empty-text {
  margin: 0;
  padding: 16px;
  text-align: center;
}

@media (max-width: 700px) {
  .watch-together {
    top: 72px;
    right: 12px;
  }
}
</style>
