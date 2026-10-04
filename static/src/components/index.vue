<template>
  <div class="home">
    <!-- 滚动后出现的吸顶栏 -->
    <div class="home-sticky" :class="{ 'is-in': sticky }">
      <head1 :head2Flag="true" />
    </div>

    <!-- 首屏 -->
    <div class="home-head" @mousemove="onMouseMove">
      <head1 />
      <section class="hero">
        <div class="hero-inner page">
          <div class="hero-copy">
            <h1 class="hero-title">青芒视频</h1>
            <p class="hero-desc">看视频、发弹幕、追创作者 —— 分享每一次观看</p>
            <div class="hero-actions">
              <button class="hero-btn is-primary" @click="scrollToFeed">开始浏览</button>
              <button class="hero-btn" @click="store.loginDialogVisible = true">登录 / 注册</button>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- 分类导航：一行可横滑的胶囊，不再是原来那块 110px 的绝对定位色块 -->
    <nav class="rail-wrap">
      <div class="rail page">
        <button
          v-for="c in categories"
          :key="c"
          class="rail-chip"
          :class="{ 'is-on': c === activeCategory }"
          @click="goCategory(c)"
        >
          {{ c }}
        </button>
      </div>
    </nav>

    <!-- 视频流 -->
    <main class="page feed" ref="feedRef">
      <div class="section-head">
        <h2>{{ activeCategory === '推荐' ? '推荐视频' : activeCategory }}</h2>
        <button class="text-btn" :class="{ 'is-busy': refreshing }" @click="refresh">
          <img class="icon icon-sm" src="/img/换一换.png" alt="" />
          换一换
        </button>
      </div>

      <div class="grid" v-show="videos.length">
        <homeVideoCard v-for="v in videos" :key="v.videoId" :video="v" />
      </div>

      <div class="state" v-if="loading && !videos.length">
        <div class="spinner"></div>
        <p>正在加载…</p>
      </div>

      <div class="state" v-else-if="!videos.length">
        <img class="state-img" src="/img/home_nodata.svg" alt="" />
        <p>这里还没有视频</p>
      </div>

      <p class="feed-end" v-show="videos.length && !hasMore">— 已经到底了 —</p>
    </main>

    <div class="backtop">
      <el-backtop :right="24" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import head1 from "./mainHead.vue";
import homeVideoCard from "./homeVideoCard.vue";
import { videoApi } from "../api/product";
import { useGlobalStore } from "../store/store";

const store = useGlobalStore();

const categories = [
  "推荐", "动画", "番剧", "国创", "综艺", "影视", "电影", "电视剧",
  "纪录片", "音乐", "舞蹈", "游戏", "知识", "生活", "时尚", "美食", "科技", "资讯",
];
const activeCategory = ref("推荐");

const videos = ref([]);
const cursorId = ref(null);
const loading = ref(false);
const hasMore = ref(true);
const refreshing = ref(false);
const sticky = ref(false);
const feedRef = ref(null);

function onMouseMove(e) {
  store.setMouseX(e.clientX);
}

function scrollToFeed() {
  feedRef.value?.scrollIntoView({ behavior: "smooth", block: "start" });
}

function goCategory(name) {
  if (name === "推荐") {
    scrollToFeed();
    refresh();
    return;
  }
  // 分类名对应后端的 categoryId，先查一次再带 id 跳搜索页
  searchByCategory(name);
}

async function searchByCategory(name) {
  try {
    const list = await videoApi.categories();
    const hit = list.find((c) => c.name === name);
    window.location.href = "/search?keyword=&categoryId=" + (hit ? hit.id : "");
  } catch (e) {
    window.location.href = "/search?keyword=" + encodeURIComponent(name);
  }
}

async function load(reset) {
  if (loading.value) return;
  if (!reset && !hasMore.value) return;
  loading.value = true;
  try {
    const sort = activeCategory.value === "推荐" ? "hot" : "latest";
    const data = await videoApi.list({
      sort,
      cursorId: reset ? undefined : cursorId.value || undefined,
      pageSize: 20,
    });
    const list = data?.records || [];
    videos.value = reset ? list : videos.value.concat(list);
    cursorId.value = data?.cursorId ?? list.length ? list[list.length - 1]?.id : null;
    hasMore.value = list.length > 0 && (data?.hasMore ?? false);
    if (!list.length) hasMore.value = false;
  } catch (e) {
    if (reset) videos.value = [];
    hasMore.value = false;
  } finally {
    loading.value = false;
  }
}

async function refresh() {
  refreshing.value = true;
  cursorId.value = null;
  hasMore.value = true;
  await load(true);
  refreshing.value = false;
}

function onScroll() {
  sticky.value = window.scrollY >= 120;
  if (hasMore.value && !loading.value) {
    if (window.innerHeight + window.scrollY >= document.body.scrollHeight - 600) load(false);
  }
}

onMounted(async () => {
  window.scrollTo(0, 0);
  window.addEventListener("scroll", onScroll, { passive: true });
  await load(true);
});

onUnmounted(() => {
  window.removeEventListener("scroll", onScroll);
});
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: var(--fill-soft);
  padding-bottom: 80px;
}

.home-sticky {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 900;
  background: #fff;
  border-bottom: 1px solid var(--line);
  transform: translateY(-100%);
  transition: transform .28s cubic-bezier(.4, 0, .2, 1);
}

.home-sticky.is-in {
  transform: translateY(0);
}

/* ---------- 首屏 ---------- */
.home-head {
  background: #fff;
}

.hero {
  position: relative;
  background-image: url("/img/hero-bg.png");
  background-size: cover;
  background-position: center;
  min-height: 260px;
  display: flex;
  align-items: center;
}

.hero::after {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(6, 60, 54, .35) 0%, rgba(6, 60, 54, 0) 55%);
}

.hero-inner {
  position: relative;
  z-index: 1;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--gap-6);
  padding-top: var(--gap-6);
  padding-bottom: var(--gap-6);
}

.hero-copy {
  color: #fff;
}

.hero-title {
  margin: 0 0 var(--gap-2);
  font-size: 40px;
  font-weight: 700;
  letter-spacing: 4px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, .18);
}

.hero-desc {
  margin: 0 0 var(--gap-5);
  font-size: 15px;
  letter-spacing: 1px;
  color: rgba(255, 255, 255, .9);
}

.hero-actions {
  display: flex;
  gap: var(--gap-3);
}

.hero-btn {
  height: 38px;
  padding: 0 22px;
  border: 1px solid rgba(255, 255, 255, .6);
  border-radius: 999px;
  background: rgba(255, 255, 255, .12);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  backdrop-filter: blur(4px);
  transition: background .2s, border-color .2s, transform .2s;
}

.hero-btn:hover {
  background: rgba(255, 255, 255, .26);
  border-color: #fff;
}

.hero-btn:active {
  transform: scale(.97);
}

.hero-btn.is-primary {
  background: #fff;
  border-color: #fff;
  color: var(--brand-hover);
  font-weight: 600;
}

.hero-btn.is-primary:hover {
  background: var(--brand-soft);
  border-color: var(--brand-soft);
}

.hero-stat {
  display: flex;
  gap: var(--gap-6);
  color: #fff;
}

.hero-stat-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  text-align: right;
}

.hero-stat-item b {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 1px;
}

.hero-stat-item span {
  font-size: 13px;
  color: rgba(255, 255, 255, .82);
}

/* ---------- 分类导航 ---------- */
.rail-wrap {
  position: sticky;
  top: 0;
  z-index: 800;
  background: #fff;
  border-bottom: 1px solid var(--line);
}

.rail {
  display: flex;
  gap: var(--gap-2);
  overflow-x: auto;
  padding: 10px var(--page-pad);
  scrollbar-width: none;
}

.rail::-webkit-scrollbar {
  display: none;
}

.rail-chip {
  flex: none;
  height: 30px;
  padding: 0 14px;
  border: 1px solid transparent;
  border-radius: 999px;
  background: var(--fill);
  color: var(--ink-2);
  font-size: 13px;
  cursor: pointer;
  transition: background .18s, color .18s;
}

.rail-chip:hover {
  background: var(--brand-soft);
  color: var(--brand-hover);
}

.rail-chip.is-on {
  background: var(--brand);
  color: #fff;
}

/* ---------- 视频流 ---------- */
.feed {
  background: transparent;
}

.grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: var(--gap-5) var(--gap-4);
}

@media (max-width: 1400px) {
  .grid { grid-template-columns: repeat(4, minmax(0, 1fr)); }
}

@media (max-width: 1120px) {
  .grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .hero-stat { display: none; }
}

@media (max-width: 820px) {
  .grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .hero-title { font-size: 30px; }
}

.state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--gap-3);
  padding: 80px 0;
  color: var(--ink-3);
  font-size: 14px;
}

.state-img {
  width: 180px;
  object-fit: contain;
}

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid var(--brand-light-7);
  border-top-color: var(--brand);
  border-radius: 50%;
  animation: spin .8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.feed-end {
  margin: var(--gap-6) 0 0;
  text-align: center;
  color: var(--ink-3);
  font-size: 13px;
  letter-spacing: 1px;
}

.backtop {
  position: fixed;
  right: 0;
  bottom: 120px;
  z-index: 700;
}
</style>

<!--
  页头组件（mainHead.vue）原本是按「压在一张动漫大图上」设计的：
  文字写死白色、宽度写死 1450px 再 translate(28px)、右侧六个功能区各写一套
  nth-child 微调，图标尺寸 14/15/16/17/18/20px 混用。
  换成纯色首屏后白字看不清、宽度也把页面撑出横向滚动条。
  这里用一段非 scoped 样式统一收口，只挂在 .home-head 下面，不影响其它页面。
-->
<style>
:is(.home-head, .home-sticky) .header {
  width: 100% !important;
  max-width: 1384px;
  height: 64px;
  margin: 0 auto;
  transform: none !important;
}

:is(.home-head, .home-sticky) .v-header-ul {
  left: 50%;
  right: auto;
  transform: translateX(-50%);
  width: 100%;
  max-width: 1384px;
  margin: 0;
  padding: 0;
}

:is(.home-head, .home-sticky) .v-header-ul li {
  flex: none;
  white-space: nowrap;
}

:is(.home-head, .home-sticky) .v-header-ul li a {
  color: var(--ink) !important;
  font-size: 13.5px;
  line-height: 1;
  margin-top: 0;
  white-space: nowrap;
  gap: 6px;
}

/* 右侧功能区统一成「图标在上、文字在下」，尺寸走同一档 */
:is(.home-head, .home-sticky) .v-header-ul li:nth-child(n + 12):nth-child(-n + 17) {
  transform: none !important;
}

:is(.home-head, .home-sticky) .v-header-ul li:nth-child(n + 12):nth-child(-n + 17) > a {
  flex-direction: column;
  gap: 3px;
  font-size: 12px !important;
  color: var(--ink-2) !important;
  line-height: 1.1;
}

:is(.home-head, .home-sticky) .v-header-ul li:nth-child(n + 12):nth-child(-n + 17) > a:hover {
  color: var(--brand) !important;
}

/* 图标统一：除文字标外一律 18px 正方形 + contain，任何比例都不被拉变形 */
:is(.home-head, .home-sticky) .v-header-ul li a img:not(.h-logo) {
  width: 18px !important;
  height: 18px !important;
  object-fit: contain;
  margin: 0 6px 0 0 !important;
  flex: none;
}

:is(.home-head, .home-sticky) .v-header-ul li:nth-child(n + 12):nth-child(-n + 17) > a img {
  margin: 0 !important;
}

/* 搜索框在白底页头上必须自己有底，否则白底白框看不见 */
:is(.home-head, .home-sticky) .search {
  background: var(--fill);
  border: 1px solid var(--line);
}

:is(.home-head, .home-sticky) .search:hover,
:is(.home-head, .home-sticky) .search:focus-within {
  background: #fff;
  border-color: var(--brand-light-5);
}

:is(.home-head, .home-sticky) .search-box:focus {
  background: #fff;
}

:is(.home-head, .home-sticky) .head-search {
  width: 300px;
  margin-right: 6px;
}
</style>