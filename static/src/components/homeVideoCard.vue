<template>
  <article class="vcard" @mouseenter="onEnter" @mouseleave="onLeave">
    <div class="vcard-cover" @click="open">
      <img
        v-show="!previewing"
        class="vcard-cover-img"
        :src="coverSrc"
        :alt="video.title"
        loading="lazy"
        @error="coverSrc = FALLBACK_COVER"
      />
      <video
        v-if="previewing && video.playUrl"
        class="vcard-cover-img"
        :src="video.playUrl"
        preload="none"
        muted
        loop
        playsinline
        disablePictureInPicture
        ref="previewEl"
      ></video>

      <span class="vcard-duration">{{ durationText }}</span>

      <button
        class="vcard-mark"
        :class="{ 'is-done': inWatchLater }"
        :title="inWatchLater ? '已加入待看清单' : '加入待看清单'"
        @click.stop="toggleMark"
      >
        <img class="icon icon-sm" :src="inWatchLater ? '/img/添加成功.png' : '/img/待看清单.png'" alt="" />
      </button>
    </div>

    <div class="vcard-body">
      <h3 class="vcard-title" :title="video.title">{{ video.title }}</h3>

      <div class="vcard-meta">
        <span class="vcard-author" @click.stop="openUser">
          <img
            class="vcard-avatar"
            :src="video.ownerAvatar || DEFAULT_AVATAR"
            alt=""
            @error="(e) => (e.target.src = DEFAULT_AVATAR)"
          />
          <span class="vcard-author-name">{{ video.ownerNickname }}</span>
        </span>
      </div>

      <div class="vcard-stats">
        <span class="vcard-stat">
          <img class="icon icon-sm" src="/img/播放量灰.png" alt="" />{{ formatCount(video.playCount) }}
        </span>
        <span class="vcard-stat">
          <img class="icon icon-sm" src="/img/弹幕灰.png" alt="" />{{ formatCount(video.danmakuCount) }}
        </span>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { useGlobalStore } from "../store/store";
import { videoApi } from "../api/product";

const props = defineProps({
  video: { type: Object, required: true },
});

const store = useGlobalStore();
const previewing = ref(false);
const previewEl = ref(null);
const coverSrc = ref(props.video.coverUrl || "/img/cover-fallback.png");
/** 待看清单状态本地维护：props 是只读的，操作成功后由自己更新 */
const inWatchLater = ref(!!props.video.watchLater);
let timer = null;

const DEFAULT_AVATAR = "/img/avatar-default.png";
const FALLBACK_COVER = "/img/cover-fallback.png";

watch(
  () => props.video.id,
  () => {
    coverSrc.value = props.video.coverUrl || FALLBACK_COVER;
    inWatchLater.value = !!props.video.watchLater;
  }
);

const durationText = computed(() => {
  const total = props.video.durationSeconds || 0;
  const m = Math.floor(total / 60);
  const s = total % 60;
  const pad = (n) => String(n).padStart(2, "0");
  return `${pad(m)}:${pad(s)}`;
});

function formatCount(n) {
  const v = Number(n) || 0;
  if (v >= 100000000) return (v / 100000000).toFixed(1).replace(/\.0$/, "") + "亿";
  if (v >= 10000) return (v / 10000).toFixed(1).replace(/\.0$/, "") + "万";
  return String(v);
}

function open() {
  window.open("/video?vId=" + props.video.id, "_blank");
}

function openUser() {
  window.open("/home?userId=" + props.video.ownerId, "_blank");
}

function onEnter() {
  if (!props.video.playUrl) return;
  clearTimeout(timer);
  timer = setTimeout(() => {
    previewing.value = true;
  }, 400);
}

function onLeave() {
  clearTimeout(timer);
  const el = previewEl.value;
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
  const next = !inWatchLater.value;
  inWatchLater.value = next;
  try {
    await videoApi.watchLater(props.video.id);
  } catch (e) {
    inWatchLater.value = !next;
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

.vcard-mark:hover,
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
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
  background: var(--fill);
  flex: none;
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