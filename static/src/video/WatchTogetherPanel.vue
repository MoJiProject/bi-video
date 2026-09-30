<template>
  <div class="watch-together">
    <button class="watch-trigger" type="button" title="一起看" @click="togglePanel">
      <span class="watch-trigger-icon">▶</span>
      <span>一起看</span>
      <span v-if="room" class="watch-count">{{ onlineCount }}</span>
    </button>

    <section v-if="panelOpen" class="watch-panel">
      <header class="watch-header">
        <div>
          <strong>{{ room ? `房间 ${room.roomId}` : '多人一起看' }}</strong>
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
        <button class="secondary-button" type="button" :disabled="busy" @click="loadHistory">
          我的房间
        </button>
      </div>

      <template v-else>
        <div class="watch-actions">
          <button class="primary-button" type="button" @click="copyInvite">复制邀请链接</button>
          <button v-if="canManage" class="secondary-button" type="button" @click="loadHistory">
            历史房间
          </button>
          <button class="leave-button" type="button" @click="leaveRoom">退出</button>
        </div>

        <div class="member-heading">
          <span>成员</span>
          <span>{{ onlineCount }} 人在线</span>
        </div>
        <ul class="member-list">
          <li v-for="member in sortedParticipants" :key="member.userId">
            <div class="member-avatar-wrap">
              <img
                class="member-avatar"
                :src="member.avatarAddress || '/默认头像.gif'"
                :alt="member.userName"
              />
              <span class="online-dot" :class="{ offline: !member.online }"></span>
            </div>
            <div class="member-info">
              <span class="member-name">
                {{ member.userName }}
                <small v-if="member.userId === currentUserId">我</small>
              </span>
              <span v-if="member.owner" class="role-label owner">房主</span>
              <span v-else-if="member.admin" class="role-label">管理员</span>
              <span v-else class="member-status">{{ member.online ? '正在一起看' : '已离线' }}</span>
            </div>
            <div v-if="member.userId !== currentUserId" class="member-actions">
              <button
                v-if="isOwner"
                class="icon-button"
                type="button"
                :title="member.admin ? '收回管理权限' : '授予管理权限'"
                @click="changeAdmin(member, !member.admin)"
              >
                {{ member.admin ? '−' : '+' }}
              </button>
              <button
                v-if="canKick(member)"
                class="icon-button danger"
                type="button"
                title="移出房间"
                @click="kick(member)"
              >
                ×
              </button>
            </div>
          </li>
        </ul>
      </template>

      <div v-if="historyOpen" class="history-section">
        <div class="member-heading">
          <span>历史房间</span>
          <button class="icon-button" type="button" title="关闭历史" @click="historyOpen = false">×</button>
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
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import apiClient from '../services/apiClient';
import { createWatchTogetherClient } from '../services/watchTogetherClient';
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
});

const store = useGlobalStore();
const initialRoomId = new URLSearchParams(window.location.search).get('room');
const panelOpen = ref(Boolean(initialRoomId));
const historyOpen = ref(false);
const historyRooms = ref([]);
const room = ref(null);
const connected = ref(false);
const busy = ref(false);
const roomId = ref(initialRoomId);
let client = null;
let clientToken = null;
let attachedVideo = null;
let applyingRemoteAction = false;
let releaseRemoteTimer = null;
let remoteSeekTarget = null;

const currentUserId = computed(() => store.userId);
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

function togglePanel() {
  panelOpen.value = !panelOpen.value;
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
        ElMessage.warning('登录已失效，请重新登录后加入房间');
      }
    },
  });
  client.connect();
}

async function createRoom() {
  if (!store.token || !store.userId) {
    ElMessage.info('请先登录');
    return;
  }
  if (!props.videoId || !props.videoElement) return;

  busy.value = true;
  try {
    const response = await apiClient.post('/watch-together/rooms', {
      videoId: props.videoId,
      currentTime: props.videoElement.currentTime || 0,
      paused: props.videoElement.paused,
      playbackRate: props.videoElement.playbackRate || 1,
    });
    room.value = response.data.data;
    roomId.value = room.value.roomId;
    updateRoomQuery(roomId.value);
    ensureClient();
    if (connected.value) joinRoom();
  } catch {
    ElMessage.error('房间创建失败，请稍后重试');
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

function leaveRoom() {
  if (!room.value) return;
  client?.send({ type: 'leave', roomId: room.value.roomId });
  runRemoteAction(() => props.videoElement?.pause());
  resetRoom();
  ElMessage.success('已退出一起看房间');
}

function sendPlayback(type) {
  if (!room.value || applyingRemoteAction || !props.videoElement) return;
  client?.send({
    type,
    roomId: room.value.roomId,
    videoId: props.videoId,
    currentTime: props.videoElement.currentTime || 0,
    playbackRate: props.videoElement.playbackRate || 1,
    paused: props.videoElement.paused,
  });
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
    ElMessage.info(`${message.userName} 已离开，一起看已暂停`);
    return;
  }
  if (message.type === 'kicked') {
    props.videoElement?.pause();
    client?.close();
    client = null;
    clientToken = null;
    connected.value = false;
    resetRoom();
    ElMessage.warning('你已被移出一起看房间');
    return;
  }
  if (message.type === 'error') {
    ElMessage.error(message.message || '一起看操作失败');
  }
}

function applyRoomState(nextRoom) {
  if (!props.videoElement || !nextRoom) return;
  runRemoteAction(() => {
    if (Math.abs(props.videoElement.currentTime - nextRoom.currentTime) > 0.75) {
      remoteSeekTarget = nextRoom.currentTime;
      props.videoElement.currentTime = nextRoom.currentTime;
    }
    props.videoElement.playbackRate = nextRoom.playbackRate || 1;
    if (nextRoom.paused) {
      props.videoElement.pause();
    } else {
      props.videoElement.play().catch(() => ElMessage.info('点击视频开始同步播放'));
    }
  });
}

function applyPlaybackMessage(message) {
  if (!props.videoElement) return;
  runRemoteAction(() => {
    if (Math.abs(props.videoElement.currentTime - message.currentTime) > 0.5) {
      remoteSeekTarget = message.currentTime;
      props.videoElement.currentTime = message.currentTime;
    }
    props.videoElement.playbackRate = message.playbackRate || 1;
    if (message.type === 'pause') props.videoElement.pause();
    if (message.type === 'play') {
      props.videoElement.play().catch(() => ElMessage.info('点击视频开始同步播放'));
    }
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
  const inviteUrl = new URL(window.location.href);
  inviteUrl.searchParams.set('room', room.value.roomId);
  try {
    await navigator.clipboard.writeText(inviteUrl.toString());
    ElMessage.success('邀请链接已复制');
  } catch {
    window.prompt('复制邀请链接', inviteUrl.toString());
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
  if (!canManage.value || member.owner) return false;
  return isOwner.value || !member.admin;
}

async function loadHistory() {
  if (!store.token) {
    ElMessage.info('请先登录');
    return;
  }
  try {
    const response = await apiClient.get('/watch-together/history');
    historyRooms.value = response.data.data || [];
    historyOpen.value = true;
  } catch {
    ElMessage.error('历史房间加载失败');
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
  historyOpen.value = false;
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
  remoteSeekTarget = null;
}

function onPlay() {
  sendPlayback('play');
}

function onPause() {
  sendPlayback('pause');
}

function onSeeked() {
  if (remoteSeekTarget !== null) {
    const isRemoteSeek = Math.abs(attachedVideo.currentTime - remoteSeekTarget) <= 0.5;
    remoteSeekTarget = null;
    if (isRemoteSeek) return;
  }
  sendPlayback('seek');
}

function onRateChange() {
  sendPlayback('rate');
}

watch(() => props.videoElement, attachVideo, { immediate: true });
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

onBeforeUnmount(() => {
  detachVideo();
  window.clearTimeout(releaseRemoteTimer);
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
  z-index: 3000;
  color: #20242b;
  font-family: "Microsoft YaHei", sans-serif;
}

.watch-trigger,
.primary-button,
.secondary-button,
.leave-button,
.icon-button,
.history-item {
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
}

.watch-trigger-icon {
  font-size: 11px;
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
  width: min(340px, calc(100vw - 32px));
  max-height: calc(100vh - 170px);
  margin-top: 8px;
  overflow: auto;
  background: #fff;
  border: 1px solid #e3e7eb;
  border-radius: 8px;
  box-shadow: 0 16px 42px rgba(24, 33, 43, 0.18);
}

.watch-header,
.member-heading,
.member-list li,
.watch-actions {
  display: flex;
  align-items: center;
}

.watch-header {
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #edf0f2;
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

.watch-empty,
.watch-actions {
  gap: 8px;
  padding: 14px 16px;
}

.primary-button,
.secondary-button,
.leave-button {
  min-height: 34px;
  padding: 0 12px;
  border-radius: 5px;
}

.primary-button {
  color: #fff;
  background: #00aeec;
}

.secondary-button {
  color: #3a424a;
  background: #edf1f4;
}

.leave-button {
  margin-left: auto;
  color: #d24c4c;
  background: #fff0f0;
}

.primary-button:disabled,
.secondary-button:disabled {
  opacity: 0.55;
  cursor: default;
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

.member-avatar {
  width: 38px;
  height: 38px;
  display: block;
  border: 1px solid #e4e9ed;
  border-radius: 50%;
  object-fit: cover;
}

.member-name {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #252b31;
  font-size: 13px;
  font-weight: 600;
}

.member-name small {
  margin-left: 4px;
  color: #98a1aa;
  font-size: 10px;
  font-weight: 400;
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

.icon-button.danger {
  color: #d84b4b;
}

.history-section {
  border-top: 1px solid #edf0f2;
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
