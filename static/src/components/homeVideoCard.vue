<template>
  <article class="vcard content-action-surface" @mouseenter="onEnter" @mouseleave="onLeave">
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
        :disabled="marking"
        :title="inWatchLater ? '已加入稍后观看' : '加入稍后观看'"
        :aria-label="inWatchLater ? '移出稍后观看' : '加入稍后观看'"
        :aria-pressed="inWatchLater"
        @click.stop="toggleMark"
      >
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path v-if="inWatchLater" d="m6 12 4 4 8-8" />
          <template v-else>
            <circle cx="12" cy="12" r="8" />
            <path d="M12 7v5l3 2" />
          </template>
        </svg>
      </button>
    </div>

    <div class="vcard-body">
      <div class="vcard-heading content-card-actions">
        <h3 class="vcard-title" :title="video.title">{{ video.title }}</h3>
        <div v-if="$slots.actions" class="vcard-actions" @click.stop><slot name="actions"/></div>
      </div>

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
        <span class="vcard-stat" title="播放量">
          <svg viewBox="0 0 16 16" aria-hidden="true">
            <path d="M4 3v10l10-5Z" />
          </svg>
          {{ formatCount(video.playCount) }}
        </span>
        <span class="vcard-stat" title="弹幕数">
          <svg viewBox="0 0 16 16" aria-hidden="true">
            <path fill-rule="evenodd" d="M4 2h8a3 3 0 0 1 3 3v6a3 3 0 0 1-3 3H4a3 3 0 0 1-3-3V5a3 3 0 0 1 3-3Zm0 3v2h8V5H4Zm0 4v2h5V9H4Z" />
          </svg>
          {{ formatCount(video.danmakuCount) }}
        </span>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { useGlobalStore } from "../store/store";
import { videoApi } from "../api/product";
import { ElMessage } from "element-plus";

const props = defineProps({
  video: { type: Object, required: true },
});

const store = useGlobalStore();
const previewing = ref(false);
const previewEl = ref(null);
const coverSrc = ref(props.video.coverUrl || "/img/cover-fallback.png");
/** 待看清单状态本地维护：props 是只读的，操作成功后由自己更新 */
const inWatchLater = ref(!!props.video.watchLater);
const marking = ref(false);
let timer = null;

const DEFAULT_AVATAR = "/img/avatar-default.png";
const FALLBACK_COVER = "/img/cover-fallback.png";

watch(
  () => [props.video.id, props.video.watchLater],
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
  window.open("/video?videoId=BV" + props.video.id, "_blank");
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
  if (marking.value) return;
  if (store.userId === null) {
    store.loginDialogVisible = true;
    return;
  }
  const next = !inWatchLater.value;
  inWatchLater.value = next;
  marking.value = true;
  try {
    const state = await videoApi.watchLater(props.video.id);
    inWatchLater.value = state.watchLater;
  } catch (e) {
    inWatchLater.value = !next;
    ElMessage.error(e.message);
  } finally {
    marking.value = false;
  }
}
</script>

<style scoped>
.vcard {
  display: flex;
  flex-direction: column;
  gap: var(--gap-2);
  min-width: 0;
  padding: 8px;
  border: 1px solid transparent;
  border-radius: 18px;
  background: transparent;
  cursor: pointer;
  transition: transform .2s ease, background .2s ease, border-color .2s ease, box-shadow .2s ease;
}

.vcard:hover {
  transform: translateY(-4px);
  border-color: rgba(224, 229, 227, .9);
  background: #fff;
  box-shadow: 0 16px 36px rgba(28, 35, 33, .08);
}

.vcard-cover {
  position: relative;
  aspect-ratio: 16 / 9;
  border-radius: 14px;
  overflow: hidden;
  background: var(--fill);
}

.vcard-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform .28s ease;
}

.vcard:hover .vcard-cover-img {
  transform: scale(1.035);
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
  width: 32px;
  height: 32px;
  padding: 0;
  border: 1px solid rgba(255, 255, 255, .4);
  border-radius: 50%;
  background: rgba(16, 30, 27, .68);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .2);
  backdrop-filter: blur(8px);
  opacity: 0;
  pointer-events: none;
  cursor: pointer;
  transition: opacity .2s ease, background .2s ease, transform .2s ease;
}

.vcard-mark svg {
  width: 20px;
  height: 20px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.vcard-cover:hover .vcard-mark,
.vcard-mark:focus-visible {
  opacity: 1;
  pointer-events: auto;
}

@media (hover: none) {
  .vcard-mark {
    min-width: 36px;
    min-height: 36px;
  }
}

.vcard-mark:hover {
  background: rgba(16, 30, 27, .9);
  transform: translateY(-1px);
}

.vcard-mark.is-done {
  border-color: rgba(255, 255, 255, .4);
  background: rgba(240, 252, 248, .4);
  color: var(--brand-hover);
}

.vcard-mark.is-done:hover { background: rgba(240, 252, 248, .9); }
.vcard-mark:focus-visible { outline: 2px solid #fff; outline-offset: 3px; }
.vcard-mark:disabled { cursor: wait; }

@media (prefers-reduced-motion: reduce) {
  .vcard-mark { transition: none; }
  .vcard-mark:hover { transform: none; }
}

.vcard-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
  padding: 2px 2px 4px;
}

.vcard-heading { display: flex; align-items: flex-start; gap: 6px; min-width: 0; }
.vcard-actions { display: flex; flex: none; margin-top: -2px; }

.vcard-title {
  flex: 1;
  min-width: 0;
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
  color: var(--ink-2);
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
  color: var(--ink-2);
  font-weight: 400;
  font-variant-numeric: tabular-nums;
}

.vcard-stat {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  line-height: 20px;
}

.vcard-stat svg {
  flex: none;
  width: 16px;
  height: 16px;
  fill: currentColor;
  opacity: .8;
}
</style>