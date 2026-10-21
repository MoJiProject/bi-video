<template>
  <div class="home home-page">
    <!-- 滚动后出现的吸顶栏 -->
    <div class="home-sticky" :class="{ 'is-in': sticky }">
      <head1 class="header-stack-search" :head2Flag="true" />
    </div>

    <!-- 首屏 -->
    <div class="home-head" @mousemove="onMouseMove">
      <head1 class="header-stack-search" />
      <section class="hero">
        <HomeBannerCarousel v-if="banners.length" :banners="banners" />
        <div class="hero-inner page" :class="{ 'is-banner-placeholder': banners.length }"
          :inert="banners.length ? true : undefined" :aria-hidden="banners.length ? 'true' : undefined">
          <div class="hero-copy">
            <h1 class="hero-title">青芒视频</h1>
            <p class="hero-desc">看视频、发互动字幕、追创作者 —— 分享每一次观看</p>
            <div class="hero-actions">
              <button class="hero-btn is-primary" @click="scrollToFeed">开始浏览</button>
              <button class="hero-btn" @click="store.loginDialogVisible = true">登录 / 注册</button>
            </div>
          </div>
          <div class="hero-panel">
            <button class="hero-card" @click="goDiscover">
              <span class="hero-card-kicker">发现内容</span>
              <strong>按兴趣探索视频</strong>
              <em>分类、搜索、热度推荐</em>
            </button>
            <button class="hero-card" @click="goContribute">
              <span class="hero-card-kicker">创作发布</span>
              <strong>上传你的作品</strong>
              <em>封面、分区、标签管理</em>
            </button>
            <button class="hero-card" @click="goWatchLater">
              <span class="hero-card-kicker">稍后观看</span>
              <strong>收藏待看的灵感</strong>
              <em>登录后多端同步</em>
            </button>
          </div>
        </div>
      </section>
    </div>

    <!-- 分类导航：一行可横滑的胶囊，不再是原来那块 110px 的绝对定位色块 -->
    <nav class="rail-wrap">
      <div class="rail page">
        <button
          v-for="c in categories"
          :key="c.value"
          class="rail-chip"
          :class="{ 'is-on': c.value === activeCategory }"
          :aria-pressed="c.value === activeCategory"
          @click="goCategory(c)"
        >
          {{ c.label }}
        </button>
      </div>
    </nav>

    <!-- 视频流 -->
    <main class="page feed" ref="feedRef">
      <div class="section-head">
        <h2>{{ activeCategory === '推荐' ? '推荐视频' : activeCategoryLabel }}</h2>
        <button class="text-btn" :class="{ 'is-busy': refreshing }" @click="refresh">
          <img class="icon icon-sm" src="/img/换一换.png" alt="" />
          换一换
        </button>
      </div>

      <div class="grid" v-show="videos.length">
        <homeVideoCard v-for="v in videos" :key="v.id" :video="v" />
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
import { computed, onMounted, onUnmounted, ref } from "vue";
import head1 from "./mainHead.vue";
import homeVideoCard from "./homeVideoCard.vue";
import HomeBannerCarousel from "./HomeBannerCarousel.vue";
import { getHomeBanners } from "../api/banner";
import { ElMessage } from "element-plus";
import { videoApi } from "../api/product";
import { useGlobalStore } from "../store/store";
import { HOME_CATEGORIES } from "../constants/contentCategories";

const store = useGlobalStore();

const categories = HOME_CATEGORIES;
const activeCategory = ref("推荐");
const activeCategoryLabel = computed(() => {
  return categories.find((c) => c.value === activeCategory.value)?.label || activeCategory.value;
});

const videos = ref([]);
const cursorId = ref(null);
const loading = ref(false);
const hasMore = ref(true);
const refreshing = ref(false);
const sticky = ref(false);
const feedRef = ref(null);
const banners = ref([]);

async function loadBanners() {
  try {
    banners.value = (await getHomeBanners()).slice(0, 6);
  } catch (error) {
    ElMessage.error(error.message || "Banner加载失败");
  }
}

function onMouseMove(e) {
  store.setMouseX(e.clientX);
}

function scrollToFeed() {
  feedRef.value?.scrollIntoView({ behavior: "smooth", block: "start" });
}

function requireLoginThen(url) {
  if (!store.userId) {
    store.loginDialogVisible = true;
    return;
  }
  window.open(url, "_blank");
}

function goDiscover() {
  window.open("/search?keyword=", "_blank");
}

function goContribute() {
  requireLoginThen("/contribute");
}

function goWatchLater() {
  requireLoginThen("/waitWatch");
}

function goCategory(category) {
  const name = category.value;
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
    cursorId.value = data?.cursorId ?? (list.length ? list[list.length - 1]?.id : null);
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
  await Promise.all([load(true), loadBanners()]);
});

onUnmounted(() => {
  window.removeEventListener("scroll", onScroll);
});
</script>

<style scoped>
:global(body:has(.home-page)) {
  min-width: 0;
}

.home {
  width: 100%;
  min-width: 0;
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
  transform: translateY(-100%);
  visibility: hidden;
  pointer-events: none;
  transition: transform .28s cubic-bezier(.4, 0, .2, 1);
}

.home-sticky.is-in {
  transform: translateY(0);
  visibility: visible;
  pointer-events: auto;
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
  flex: 1 1 auto;
  min-width: 0;
  color: #fff;
}

.hero-inner.is-banner-placeholder { visibility: hidden; pointer-events: none; }

.hero-title {
  margin: 0 0 var(--gap-2);
  font-size: clamp(28px, 2.5vw, 44px);
  font-weight: 700;
  letter-spacing: 4px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, .18);
}

.hero-desc {
  margin: 0 0 var(--gap-5);
  font-size: 15px;
  letter-spacing: 1px;
  color: rgba(255, 255, 255, .9);
  line-height: 1.8;
  overflow-wrap: anywhere;
}

.hero-actions {
  display: flex;
  gap: var(--gap-3);
  flex-wrap: wrap;
}

.hero-panel {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--gap-3);
  width: min(620px, 52vw);
  flex: none;
  min-width: 0;
}

.hero-card {
  min-width: 0;
  min-height: 118px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, .34);
  border-radius: 18px;
  background: rgba(255, 255, 255, .16);
  color: #fff;
  text-align: left;
  cursor: pointer;
  backdrop-filter: blur(12px);
  box-shadow: 0 14px 34px rgba(0, 0, 0, .12);
  transition: transform .2s ease, background .2s ease, border-color .2s ease;
}

.hero-card:hover {
  transform: translateY(-3px);
  background: rgba(255, 255, 255, .24);
  border-color: rgba(255, 255, 255, .7);
}

.hero-card-kicker,
.hero-card em {
  display: block;
  color: rgba(255, 255, 255, .78);
  font-size: 12px;
  font-style: normal;
}

.hero-card strong {
  display: block;
  margin: 10px 0 8px;
  font-size: 16px;
  line-height: 1.35;
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

/* ---------- 分类导航 ---------- */
.rail-wrap {
  position: sticky;
  top: var(--head-h);
  z-index: 800;
  background: #fff;
  border-bottom: 1px solid var(--line);
}

.rail {
  display: flex;
  gap: var(--gap-2);
  overflow-x: auto;
  min-width: 0;
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
  scroll-margin-top: calc(var(--head-h) + 70px);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--gap-5) var(--gap-4);
}

@media (max-width: 1120px) {
  .hero-inner { flex-direction: column; align-items: stretch; gap: 24px; }
  .hero-panel { width: 100%; }
  .hero-card { min-height: 104px; }
}

@media (max-width: 820px) {
  .grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .hero-inner { padding-top: 28px; padding-bottom: 28px; }
  .hero-title { font-size: 32px; }
  .hero-desc { font-size: 14px; }
  .hero-card { padding: 14px; }
  .hero-card strong { font-size: 14px; }
  .hero-card em { font-size: 11px; }
}

@media (max-width: 640px) {
  .home { padding-bottom: 40px; }
  .hero-inner { gap: 22px; padding-top: 24px; padding-bottom: 24px; }
  .hero-title { font-size: 30px; letter-spacing: 2px; }
  .hero-desc { margin-bottom: 18px; font-size: 13px; letter-spacing: 0; }
  .hero-panel { gap: 8px; }
  .hero-card { min-height: 88px; padding: 12px 10px; border-radius: 12px; }
  .hero-card-kicker { font-size: 10px; }
  .hero-card strong { margin: 7px 0 0; font-size: 13px; line-height: 1.5; }
  .hero-card em { display: none; }
  .hero-btn { height: 36px; padding: 0 18px; font-size: 13px; }
  .rail { gap: 6px; padding-top: 10px; padding-bottom: 10px; }
  .rail-chip { height: 32px; padding: 0 12px; font-size: 12px; }
  .grid { gap: 20px 12px; }
  .section-head { gap: 12px; margin-top: 24px; }
  .section-head h2 { font-size: 18px; }
  .state { padding: 48px 0; }
}

@media (max-width: 480px) {
  .grid { grid-template-columns: minmax(0, 1fr); }
  .hero-title { font-size: 28px; }
  .hero-card { padding: 10px 8px; }
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
  width: min(180px, 45vw);
  height: auto;
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