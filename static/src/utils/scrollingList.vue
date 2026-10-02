<template>
    <div
        @click="$emit('videoClick')"
        class="scrolling-container"
        v-for="scrolling in visibleList"
        :key="scrolling.id"
    >
        <!-- 滚动弹幕 -->
        <div
            v-show="
                openFlag &&
                scrollingDisplayTime(scrolling.videoTime, allDisplay) &&
                scrolling.location === 1 &&
                !rollOpenFlag &&
                scrollingDisplayFunction(scrolling)
            "
            class="scrolling-boder"
            :style="{
                color: isColorful(scrolling) ? 'transparent' : `${scrolling.color}`,
                fontSize: scrollingFontSize(scrolling.size),
                top: `${scrollingTop(scrolling)}px`,
                transform: scrollingTranslateX(scrolling.videoTime, allDisplay),
                opacity: `${parseFloat(displayOpacityValue) / 100}`,
                padding: scrolling.userId === userId ? '1px' : '0px',
                border:
                    scrolling.userId === userId
                        ? `1px solid #a7dacc`
                        : 'none',
            }"
            :class="{
                ...(allDisplay
                    ? { scrollingLocationRoll1: scrolling.location === 1 }
                    : { scrollingLocationRoll: scrolling.location === 1 }),
                scrollingColorful: isColorful(scrolling),
            }"
        >
            <span
                class="scrolling-content-container"
                @click.stop="$emit('copyScrolling', scrolling.content)"
                >{{ scrolling.content }}
            </span>
        </div>
        <!-- 顶部弹幕 -->
        <div
            v-show="
                scrolling.videoTime <= scrollingCurrentTime() &&
                openFlag &&
                scrolling.videoTime + 5 >= scrollingCurrentTime() &&
                scrolling.location === 2 &&
                !filexdOpenFlag &&
                scrollingDisplayFunction(scrolling)
            "
            class="scrolling-boder"
            :style="{
                color: isColorful(scrolling) ? 'transparent' : `${scrolling.color}`,
                fontSize: scrollingFontSize(scrolling.size),
                top: `${scrollingTop(scrolling)}px`,
                opacity: `${parseFloat(displayOpacityValue) / 100}`,
            }"
            :class="{
                ...(allDisplay
                    ? { scrollingLocationTop1: scrolling.location === 2 }
                    : { scrollingLocationTop: scrolling.location === 2 }),
                scrollingColorful: isColorful(scrolling),
            }"
        >
            <span
                class="scrolling-content-container"
                @click.stop="$emit('copyScrolling', scrolling.content)"
                >{{ scrolling.content }}
            </span>
        </div>
        <!-- 底部弹幕，小屏压在下半 6 行(row 6~11)，全屏压在第 13~25 行 -->
        <div
            v-show="
                scrolling.videoTime <= scrollingCurrentTime() &&
                openFlag &&
                scrolling.videoTime + 5 >= scrollingCurrentTime() &&
                scrolling.location === 3 &&
                !filexdOpenFlag &&
                scrollingDisplayFunction(scrolling)
            "
            class="scrolling-boder"
            :style="{
                color: isColorful(scrolling) ? 'transparent' : `${scrolling.color}`,
                fontSize: scrollingFontSize(scrolling.size),
                top: `${scrollingTop(scrolling)}px`,
                opacity: `${parseFloat(displayOpacityValue) / 100}`,
            }"
            :class="{
                ...(allDisplay
                    ? { scrollingLocationBottom1: scrolling.location === 3 }
                    : { scrollingLocationBottom: scrolling.location === 3 }),
                scrollingColorful: isColorful(scrolling),
            }"
        >
            <span
                class="scrolling-content-container"
                @click.stop="$emit('copyScrolling', scrolling.content)"
                >{{ scrolling.content }}
            </span>
        </div>
    </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';

const props = defineProps({
    list: { type: Array, default: () => [] },
    getCurrentTime: { type: Function, required: true },
    player: { type: Object, default: null },
    allDisplay: { type: Boolean, default: false },
    innerWidth: { type: Number, default: 0 },
    openFlag: { type: Boolean, default: true },
    rollOpenFlag: { type: Boolean, default: false },
    filexdOpenFlag: { type: Boolean, default: false },
    displayAreaValue: { type: [String, Number], default: "100" },
    displayFontSizeValue: { type: [String, Number], default: "50" },
    displayOpacityValue: { type: [String, Number], default: "100" },
    speedValue: { type: [String, Number], default: "50" },
    checkBoxOpenFlag: { type: Boolean, default: false },
    userId: { default: null },
});

defineEmits(["videoClick", "copyScrolling"]);

// 播放器容器尺寸，弹幕位置按容器宽高等比换算，兼容小屏/大屏/全屏
const trackWidth = ref(0);
const trackHeight = ref(0);
let resizeObserver = null;

// 测量基准取 video 元素本身而不是它的容器：小屏视频只占容器高度(94%)，
// 用容器高度会让末行落到视频画面之外；全屏时两者相等所以看不出差别
function trackHost() {
  return props.player || null;
}

function measureTrack() {
  const host = trackHost();
  if (!host) return;
  if (host.clientWidth > 0) trackWidth.value = host.clientWidth;
  if (host.clientHeight > 0) trackHeight.value = host.clientHeight;
}

function bindResizeObserver() {
  if (resizeObserver || typeof ResizeObserver === 'undefined') return;
  const host = trackHost();
  if (!host) return;
  resizeObserver = new ResizeObserver(measureTrack);
  resizeObserver.observe(host);
}

onMounted(() => {
    measureTrack();
    bindResizeObserver();
    window.addEventListener('resize', measureTrack);
});

// player 是父组件的模板 ref，首次渲染时还是 null，挂载后才会拿到真实元素。
// 不监听它的话测量会一直失败，trackHeight 保持 0，所有弹幕行会叠在顶部。
watch(
    () => props.player,
    () => {
        measureTrack();
        bindResizeObserver();
    },
    { immediate: true, flush: 'post' }
);

onBeforeUnmount(() => {
    window.removeEventListener('resize', measureTrack);
    if (resizeObserver) {
        resizeObserver.disconnect();
        resizeObserver = null;
    }
});

// 播放器当前时间统一走父组件的 rAF 时钟，避免依赖 player 元素导致顶/底弹幕不显示
function scrollingCurrentTime() {
  return props.getCurrentTime();
}

// 弹幕的纵向位置由后端下发的百分比决定，小屏和全屏各存一份：
//   top             小屏用的位置
//   all_display_top 全屏用的位置
// 同一个行号按各自轨道的行数换算，所以小屏占的行数少、比例却和大屏对齐：
//   全屏 26 行：顶部 0~12 行(0~50%)、底部 13~25 行(50~100%)、滚动 0~25 行
//   小屏 12 行：顶部 0~5  行(0~50%)、底部 6~11 行(50~100%)、滚动 0~11 行

// 是否彩色弹幕
function isColorful(scrolling) {
  return Number(scrolling.colorful) === 1;
}

// 当前模式用的纵向位置百分比，钳到 0~100
function scrollingPercent(scrolling) {
  const value = Number(props.allDisplay ? scrolling.allDisplayTop : scrolling.top);
  if (!Number.isFinite(value)) return 0;
  return Math.min(Math.max(value, 0), 100);
}

// 百分比乘播放器高度得到像素位置
function scrollingTop(scrolling) {
  if (trackHeight.value <= 0) return 0;
  return Math.round((scrollingPercent(scrolling) / 100) * trackHeight.value);
}

function scrollingSpeedRate() {
  if (props.checkBoxOpenFlag) {
    return 1;
  }

  return 1 + (parseInt(props.speedValue) - 50) / 100;
}

// 各分辨率下的基准宽高与行进距离，容器按实际尺寸等比缩放
const BASE_REFERENCE = {
  normal: { width: 700, height: 419, distance: 2350, duration: 13 },
  allDisplay: { width: 1920, height: 1080, distance: 3100, duration: 16 },
};

function scrollingReference(isAllDisplay) {
  return isAllDisplay ? BASE_REFERENCE.allDisplay : BASE_REFERENCE.normal;
}

function scrollingDuration(isAllDisplay) {
  const base = scrollingReference(isAllDisplay);
  const rate = Math.max(scrollingSpeedRate(), 0.1);
  // 未测量到容器时按基准宽度计算，避免用 window 宽度把弹幕带偏
  const width = trackWidth.value || base.width;
  // 距离随宽度等比放大，用同样的时长保持视觉速度一致
  return (base.duration * width) / base.width / rate;
}

function scrollingElapsed(videoTime) {
  return props.getCurrentTime() - Number(videoTime || 0);
}

function scrollingTranslateX(videoTime, isAllDisplay) {
  const duration = scrollingDuration(isAllDisplay);
  const elapsed = scrollingElapsed(videoTime);
  const progress = Math.min(Math.max(elapsed / duration, 0), 1);
  const base = scrollingReference(isAllDisplay);
  const width = trackWidth.value || base.width;
  // 起点由 CSS 的 left:100% 决定，这里只需走完容器宽度即可完全移出
  const distance = (base.distance * width) / base.width;

  return `translateX(${-distance * progress}px)`;
}

//滚动弹幕显示时间
function scrollingDisplayTime(videoTime, isAllDisplay) {
  const elapsed = scrollingElapsed(videoTime);
  return elapsed >= 0 && elapsed <= scrollingDuration(isAllDisplay);
}

function scrollingFontSize(size) {
  return `${size * (1 + (parseInt(props.displayFontSizeValue) - 50) / 250)}px`;
}

//显示弹幕区域。位置是百分比，显示区域也是轨道高度的百分比，直接比大小即可
function scrollingDisplayFunction(scrolling) {
    const area = parseInt(props.displayAreaValue);
    if (Number.isNaN(area)) return true;
    if (area >= 100) return true;
    if (area <= 0) return false;
    return scrollingPercent(scrolling) <= area;
}

//只渲染时间窗口内的弹幕，窗口外的弹幕原本就是隐藏的
const visibleList = computed(() => {
  const current = props.getCurrentTime();
  const duration =
    Math.max(scrollingDuration(false), scrollingDuration(true)) + 1;
  const result = [];
  for (let i = 0; i < props.list.length; i++) {
    const videoTime = Number(props.list[i].videoTime || 0);
    if (videoTime <= current + duration && videoTime >= current - duration)
      result.push(props.list[i]);
  }
  return result;
});
</script>
