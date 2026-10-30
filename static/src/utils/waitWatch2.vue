<template>
    <button
      v-show="store.userId !== null"
      type="button"
      class="wait-watch"
      :class="{ 'is-done': video.waitWatch === 1 }"
      :title="video.waitWatch === 1 ? '已加入稍后观看' : '加入稍后观看'"
      :aria-label="video.waitWatch === 1 ? '移出稍后观看' : '加入稍后观看'"
      :aria-pressed="video.waitWatch === 1"
      @click.stop="waitWatch(video)"
    >
      <svg viewBox="0 0 24 24" aria-hidden="true">
        <path v-if="video.waitWatch === 1" d="m6 12 4 4 8-8" />
        <template v-else>
          <circle cx="12" cy="12" r="8" />
          <path d="M12 7v5l3 2" />
        </template>
      </svg>
    </button>
</template>

<script setup>
import {useGlobalStore} from "@/store/store";
import { updateWaitWatch } from "@/api/video/index";
import { ElMessage } from "element-plus";

defineProps({
  video: {
    type: Object,
    required: true,
  },
});

const store = useGlobalStore();

async function waitWatch(video) {
  
  let dynamicDto={
      videoId: video.videoId,
      userId: store.userId,
  }
  updateWaitWatch(store.token,dynamicDto).then(res => {
    if (res.data.code === 1) {
        video.waitWatch= video.waitWatch === 0 ? 1 : 0;
        ElMessage({
            message: res.data.data === "1" ? "已加入稍后观看" : "已从稍后观看中移除",
            type: "success",
            duration: 1700,
        });
    }
    else{
      ElMessage({
        message: res.data.msg,
        type: "warning",
        duration: 1700,
      });
    }
  });
}

</script>

<style scoped lang="scss">
.wait-watch {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 32px;
    height: 32px;
    padding: 0;
    border: 1px solid rgba(255, 255, 255, .4);
    border-radius: 50%;
    background: rgba(16, 30, 27, .68);
    color: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, .2);
    backdrop-filter: blur(8px);
    user-select: none;
    transition: opacity .2s ease, background-color .2s ease, transform .2s ease;
    cursor: pointer;

    svg {
        width: 18px;
        height: 18px;
        fill: none;
        stroke: currentColor;
        stroke-width: 2;
        stroke-linecap: round;
        stroke-linejoin: round;
    }
}

.wait-watch:hover {
    background: rgba(16, 30, 27, .9);
    transform: translateY(-1px);
}

.wait-watch.is-done {
    border-color: rgba(255, 255, 255, .4);
    background: rgba(240, 252, 248, .4);
    color: var(--brand-hover);
}

.wait-watch.is-done:hover {
    background: rgba(240, 252, 248, .9);
}

.wait-watch:focus-visible {
    outline: 2px solid #fff;
    outline-offset: 3px;
}

</style>