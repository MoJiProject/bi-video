<template>
  <article class="vcard" @mouseenter="onEnter" @mouseleave="onLeave">
    <div class="vcard-cover" @click="open">
      <img
        class="vcard-cover-img"
        :src="coverSrc"
        :alt="video.videoTitle"
        loading="lazy"
        @error="onCoverError"
      />
      <video
        v-show="previewing"
        class="vcard-cover-img vcard-preview"
        :id="'pv' + video.videoId"
        :src="video.videoAddress"
        preload="none"
        muted
        loop
        playsinline
        disablePictureInPicture
      ></video>

      <span class="vcard-duration" v-show="durationText">{{ durationText }}</span>

      <button
        class="vcard-mark"
        :class="{ 'is-done': video.waitWatch === 1 }"
        :title="markTitle"
        @click.stop="toggleMark"
      >
        <img class="icon icon-sm" :src="markIcon" alt="" />
      </button>
    </div>

    <div class="vcard-body">
      <h3 class="vcard-title" :title="video.videoTitle">{{ video.videoTitle }}</h3>

      <div class="vcard-meta">
        <span class="vcard-author" @click.stop="openUser">
          <img class="vcard-avatar icon" :src="avatarSrc" alt="" @error="onAvatarError" />
          <span class="vcard-author-name">{{ video.userName }}</span>
        </span>
        <span class="vcard-date">{{ video.createTime }}</span>
      </div>

      <div class="vcard-stats">
        <span class="vcard-stat">
          <img class="icon icon-sm" src="/img/播放量灰.png" alt="" />{{ video.videoPlayNumber }}
        </span>
        <span class="vcard-stat">
          <img class="icon icon-sm" src="/img/弹幕灰.png" alt="" />{{ video.videoScrollingNumber }}
        </span>
        <span class="vcard-stat" v-show="video.videoLikeNumber">
          <img class="icon icon-sm" src="/img/评论点赞灰.png" alt="" />{{ video.videoLikeNumber }}
        </span>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { useGlobalStore } from "../store/store";
import { updateWaitWatch } from "../api/video";

const props = defineProps({
  video: { type: Object, required: true },
});

const store = useGlobalStore();
const previewing = ref(false);
const coverBroken = ref(false);
const avatarBroken = ref(false);
let timer = null;

const FALLBACK_COVER = "/img/cover-fallback.png";
const DEFAULT_AVATAR = "/img/avatar-default.png";

const coverSrc = computed(() => (coverBroken.value ? FALLBACK_COVER : props.video.coverAddress));
const avatarSrc = computed(() => (avatarBroken.value ? DEFAULT_AVATAR : DEFAULT_AVATAR));

const durationText = computed(() => {
  const { hour, minutes, second } = props.video;
  const pad = (n) => String(n ?? 0).padStart(2, "0");
  if (hour !== null && hour !== undefined && hour !== "") {
    return `${hour}:${pad(minutes)}:${pad(second)}`;
  }
  return `${minutes ?? "00"}:${pad(second)}`;
});

const markIcon = computed(() => (props.video.waitWatch === 1 ? "/img/添加成功.png" : "/img/待看清单.png"));
const markTitle = computed(() =>
  props.video.waitWatch === 1 ? "已加入待看清单" : "加入待看清单"
);

watch(
  () => props.video.videoId,
  () => {
    coverBroken.value = false;
    avatarBroken.value = false;
  }
);

function onCoverError() {
  coverBroken.value = true;
}

function onAvatarError() {
  avatarBroken.value = true;
}

function open() {
  window.open("/video?vId=" + props.video.videoId, "_blank");
}

function openUser() {
  window.open("/home?userId=" + props.video.userId, "_blank");
}

function onEnter() {
  clearTimeout(timer);
  timer = setTimeout(async () => {
    if (!props.video.videoAddress) return;
    previewing.value = true;
    await new Promise((r) => setTimeout(r, 30));
    const el = document.getElementById("pv" + props.video.videoId);
    if (el) {
      el.currentTime = 0;
      el.play().catch(() => {});
    }
  }, 420);
}

function onLeave() {
  clearTimeout(timer);
  const el = document.getElementById("pv" + props.video.videoId);
  if (el) {
    el.pause();
    el.removeAttribute("src");
    el.load();
  }
  previewing.value = false;
}

async function toggleMark() {
  if (store.userId === null) {
    store.loginDialogVisible = true;
    return;
  }
  const next = props.video.waitWatch === 1 ? 0 : 1;
  const res = await updateWaitWatch(store.token, { videoId: props.video.videoId, userId: store.userId });
  if (res && res.data && res.data.code === 1) {
    props.video.waitWatch = next;
  }
}
</script>

<style scoped>
.vcard {
  display: flex;
  flex-direction: column;
  gap: var(--gap-2);
  cursor: pointer;
}

.vcard-cover {
  position: relative;
  aspect-ratio: 16 / 9;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--fill);
}

.vcard-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: opacity .25s ease;
}

.vcard-preview {
  position: absolute;
  inset: 0;
}

.vcard-duration {
  position: absolute;
  right: 8px;
  bottom: 8px;
  padding: 1px 6px;
  border-radius: 4px;
  background: rgba(0, 0, 0, .62);
  color: #fff;
  font-size: 12px;
  line-height: 18px;
  letter-spacing: .3px;
}

.vcard-mark {
  position: absolute;
  left: 8px;
  bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  padding: 0;
  border: 0;
  border-radius: 8px;
  background: rgba(0, 0, 0, .45);
  opacity: 0;
  cursor: pointer;
  transition: opacity .2s ease, background .2s ease;
}

.vcard:hover .vcard-mark,
.vcard-mark.is-done {
  opacity: 1;
}

.vcard-mark:hover {
  background: var(--brand);
}

.vcard-mark.is-done {
  background: var(--brand);
}

.vcard-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.vcard-title {
  margin: 0;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.4;
  color: var(--ink);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word !important;
  transition: color .2s ease;
}

.vcard:hover .vcard-title {
  color: var(--brand);
}

.vcard-meta {
  display: flex;
  align-items: center;
  gap: var(--gap-2);
  font-size: 12.5px;
  color: var(--ink-3);
  min-width: 0;
}

.vcard-author {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  cursor: pointer;
}

.vcard-avatar {
  border-radius: 50%;
  background: var(--fill);
}

.vcard-author-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color .2s ease;
}

.vcard-author:hover .vcard-author-name {
  color: var(--brand);
}

.vcard-date {
  flex: none;
}

.vcard-stats {
  display: flex;
  align-items: center;
  gap: var(--gap-4);
  font-size: 12.5px;
  color: var(--ink-3);
}

.vcard-stat {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}
</style>