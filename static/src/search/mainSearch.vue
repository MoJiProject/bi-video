<template>
  <div class="search-page">
    <div id="fixedBox" class="hiddenBox" :class="{ showHiddenBox: isVisible }">
      <a class="sticky-search-home" href="/" aria-label="返回青芒视频首页">青芒</a>
      <Searcha
        ref="searcha"
        class="sticky-search-field"
        :mainSearchFlag="true"
        :handleUrlChange="handleUrlChange"
      />
    </div>
    <div>
      <div
        class="head"
        :class="{
          headLoginIndex: store.loginDialogVisible || store.loginLoadFlag,
        }"
      >
        <head1
          :head2Flag="true"
          :searchFlag="true"
          :loginDialogVisibleFlag="loginDialogVisibleFlag"
        />
      </div>
      <!-- 搜索页顶部的大搜索框。
           原来 .searchBox2 没有任何样式，定位全靠 Searcha 上的 inline
           left:50% + translate(-50%,28px)：这个 50% 是相对 Searcha 自身父节点
           算的，父节点宽度又是内容宽度，所以「居中」每次渲染结果都不一样。
           改成容器自己居中，Searcha 内部按 100% 铺满。 -->
      <div class="searchShell">
        <div class="search-intro">
          <div class="searchBox2">
            <Searcha ref="searcha2" class="searchBox2-inner" :handleUrlChange="handleUrlChange" />
          </div>
        </div>
        <div class="middle">
          <div class="sort" ref="sortBarEl">
    <!-- 原来这里是 14 个 span：每个 tab 按选中与否各写一份 .aw / .aww，
         靠 17px / 26px / 8px / 14px 这些 translate 微调对齐；
         下面再挂 7 个写死 translate(63px, 137px) 的下划线。
         现在只留一份 tab，选中态走 is-on，下划线按 DOM 实测位置。 -->
    <span
      v-for="tab in resultTabs"
      :key="tab.flag"
      :ref="el => setTabEl(el, tab.flag)"
      class="aw"
      :class="{ 'is-on': clickFlag === tab.flag }"
      @click="ClickFlag(tab.flag)"
      >{{ tab.label }}
      <span class="ac" v-if="tab.total !== null">{{ tab.total > 99 ? '99+' : tab.total }}</span>
    </span>
    <span
      class="sort-underline"
      :style="{ transform: 'translateX('+sortLine.x+'px)', width: sortLine.w+'px' }"
      v-show="sortLine.visible"
    ></span>
          </div>
        </div>
      </div>
      <div class="content">
        <!-- 筛选栏：四行维度 + 展开更多。
             坐标、间距、选中态统一由 CSS 的 .filter-row / .chip 负责，
             数据来自脚本里的 SORT / DATE / TIME / CLASSIFY 四张表。 -->
        <div v-show="clickFlag1 || clickFlag2" class="videoSort">
          <section class="search-filters" aria-label="视频筛选">
          <div class="filter-section category-section">
            <span class="filter-label">内容分区</span>
            <div class="filter-row filter-row-wrap">
              <button
                v-for="(opt, i) in CLASSIFY"
                :key="opt.value"
                type="button"
                class="chip"
                :class="{ 'is-on': classifyIndexRef === i }"
                :aria-pressed="classifyIndexRef === i"
                @click="applyFilter('classify', i)"
              >{{ opt.label }}</button>
            </div>
          </div>
          <div class="filter-bar">
            <span class="filter-label">排序方式</span>
            <div class="filter-row sort-options">
              <button
                v-for="(opt, i) in SORT"
                :key="'sort' + i"
                type="button"
                class="chip"
                :class="{ 'is-on': sortIndex === i }"
                @click="applyFilter('sort', i)"
              >{{ opt.label }}</button>
            </div>

            <div class="filter-more">
              <button type="button" class="filter-toggle" :aria-expanded="expanded" @click="expanded = !expanded">
                <span>{{ expanded ? '收起筛选' : '更多筛选' }}</span>
                <img src="/img/更多.png" :class="{ 'is-open': expanded }" alt="" />
              </button>
            </div>
          </div>

          <div v-show="expanded" class="filter-panel" :class="{ expanded }">
            <div class="filter-section">
              <span class="filter-label">发布时间</span>
              <div class="filter-row">
              <button
                v-for="(opt, i) in DATE"
                :key="'date' + i"
                type="button"
                class="chip"
                :class="{ 'is-on': dateIndex === i }"
                @click="applyFilter('date', i)"
              >{{ opt.label }}</button>
              <el-date-picker
                v-model="datea"
                class="filter-date"
                popper-class="search-date-picker"
                type="daterange"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :default-value="[new Date(), new Date()]"
              />
              </div>
            </div>

            <div class="filter-section">
              <span class="filter-label">视频时长</span>
              <div class="filter-row">
              <button
                v-for="(opt, i) in TIME"
                :key="'time' + i"
                type="button"
                class="chip"
                :class="{ 'is-on': timeIndex === i }"
                @click="applyFilter('time', i)"
              >{{ opt.label }}</button>
              </div>
            </div>
          </div>
          </section>

          <AppEmptyState v-if="Videos.length === 0" kind="search" title="没有找到相关作品"
            description="试试其他关键词，或切换一个感兴趣的分区"/>

          <div class="bottomVideo">
            <!-- 与首页视频列表同款卡片组件：封面 16:9、时长角标、
                 悬停预览、稍后观看、UP 主头像行、播放/弹幕数；
                 highlightText 传入后标题命中关键词会高亮 -->
            <homeVideoCard
              v-for="video in Videos"
              :key="video.id"
              :video="video"
              :highlight-text="highlightText"
            />
            <div class="pager-row">
              <div v-show="acceptSearchData.videoTotal" class="page-container">
                <el-pagination
                  :current-page="videoPageNum"
                  :page-size="20"
                  layout="prev, pager, next"
                  :total="acceptSearchData.videoTotal"
                  :background="true"
                  @current-change="handleCurrentChangeVideo"
                />
                <span
                  >共 {{ Math.ceil(acceptSearchData.videoTotal / 20) }} 页 /
                  {{ acceptSearchData.videoTotal }} 个，跳至<input
                    type="number"
                    @keydown.enter="handleCurrentChangeVideo2"
                  />页</span
                >
              </div>
            </div>
          </div>
        </div>

        <AppEmptyState v-if="clickFlag3 || clickFlag4 || clickFlag5 || clickFlag6" kind="search"
          title="暂无相关内容" description="切换搜索类型，继续发现感兴趣的内容"/>

        <div v-if="clickFlag7" class="videoSort">
          <div class="filter-row">
            <button
              v-for="(opt, i) in USER_SORT"
              :key="'usort' + i"
              type="button"
              class="chip"
              :class="{ 'is-on': userSortIndex === i }"
              @click="applyFilter('userSort', i)"
            >{{ opt.label }}</button>
          </div>
          <div class="searchUsers">
              <AppEmptyState v-if="!searchUserList.length" kind="search" title="没有找到相关用户" description="试试其他昵称或关键词"/>
              <div
                class="usersContent"
                v-for="(user, index) in searchUserList"
                :key="index"
              >
                <img
                  :src="user.avatarAddress"
                  @click="openHome(user.userId)"
                />

                <el-tooltip
                  popper-class="custom-tooltip1"
                  class="box-item"
                  effect="light"
                  :content="user.userName"
                  placement="right"
                  :show-arrow="false"
                  :offset="20"
                >
                  <span
                    class="user-Name"
                    @click="openHome(user.userId)"
                  >{{ user.userName }}
                    <LevelBadge
                      v-if="user.grade > 0 && user.grade <= 10"
                      :level="user.grade"
                      class="user-grade"
                      size="xs"
                    />
                  </span>
                </el-tooltip>
                <el-tooltip
                  popper-class="custom-tooltip1"
                  class="box-item"
                  effect="light"
                  :content="
                    userContent(
                      user.fansNumber,
                      user.videoNumber,
                      user.introduce
                    )
                  "
                  placement="right"
                  :offset="-350"
                  :show-arrow="false"
                >
                  <div class="userInfo-Content">
                    <span>{{ user.fansNumber }}粉丝 </span>
                    <span style="margin-left: 5.5px; margin-right: 5.5px"
                      >·</span
                    >
                    <span>{{ user.videoNumber }}个视频 </span>
                    <span class="introduce">{{ user.introduce }}</span>
                  </div>
                </el-tooltip>
                <div
                  v-if="!user.followed"
                  class="follow"
                  v-debounce
                  @click="addFollowAxios(user.userId)"
                >
                  + 关注
                </div>
                <div
                  v-else
                  class="deleteFollow"
                  v-debounce
                  @click="deleteFollowAxios(user.userId)"
                >
                  已关注
                </div>
              </div>
            <div class="pager-row">
                <div v-show="acceptSearchData.userTotal" class="page-container">
                  <el-pagination
                    :current-page="userPageNum"
                    :page-size="20"
                    layout="prev, pager, next"
                    :total="acceptSearchData.userTotal"
                    :background="true"
                    @current-change="handleCurrentChangeUser"
                  />
                  <span
                    >共 {{ Math.ceil(acceptSearchData.userTotal / 20) }} 页 /
                    {{ acceptSearchData.userTotal }} 个，跳至<input
                      type="number"
                      @keydown.enter="handleCurrentChangeUser2"
                    />页</span
                  >
                </div>
              </div>
          </div>
        </div>
      </div>
    </div>
    <el-backtop :right="16" :bottom="24"/>
  </div>
</template>

<script>
import head1 from "../components/mainHead.vue";
import LevelBadge from "../components/LevelBadge.vue";
import Searcha from "../components/search.vue";
import homeVideoCard from "../components/homeVideoCard.vue";
import { computed, reactive, nextTick, onMounted, ref, watch, onUnmounted } from "vue";
import { searchApi, socialApi, videoApi } from "../api/product";
import { ElMessage } from "element-plus";
import { useGlobalStore } from "../store/store";
import { formatContentCategories } from "../constants/contentCategories";

/* 筛选选项表。
   原来 42 个选项散在模板里，每个写两份 span（未选中/选中）并各带一个
   translate(x, y)，未选中与选中态的坐标还差一个固定值（如 86/64、196/174），
   靠这个差值让文字在切换时不跳。分类行 22 个选项分两行，x 是等差数列。
   收成数据后，坐标交给 flex，选中态走 class。 */
const SORT = [
  { label: "综合排序" },
  { label: "最多播放" },
  { label: "最新发布" },
  { label: "最多弹幕" },
  { label: "最多收藏" },
];

const USER_SORT = [
  { label: "默认排序" },
  { label: "粉丝数由高到低" },
  { label: "粉丝数由低到高" },
  { label: "Lv等级由高到低" },
  { label: "Lv等级由低到高" },
];

const DATE = [
  { label: "全部日期" },
  { label: "最近一天" },
  { label: "最近一周" },
  { label: "最近半年" },
];

const TIME = [
  { label: "全部时长" },
  { label: "10分钟以下" },
  { label: "10-30分钟" },
  { label: "30-60分钟" },
  { label: "60分钟以上" },
];

export default {
  name: "MainSearch",
  components: {
    head1,
    LevelBadge,
    Searcha,
    homeVideoCard,
  },

  setup() {
    const loginDialogVisibleFlag = ref(0);
    const store = useGlobalStore();
    const onloadPage = ref(false);
    const videoPageNum = ref(1);
    const userPageNum = ref(1);
    const acceptSearchData = reactive({
      userId: 0,
      keyWord: "",
      sort: 0,
      date: 0,
      time: 0,
      classify: "全部",
      startTIme: null,
      startTime: "",
      endTime: "",
      userSort: 0,
      videoPageNum: 1,
      userPageNum: 1,
      classifyIndex: "",
      videoTotal: 0,
      userTotal: 0,
    });
    const searchUserList = reactive([]);
    const expanded = ref(false);
    const windowWidth = ref(0);
    const Videos = reactive([]);
    const availableCategories = ref(null);
    const CLASSIFY = computed(() => [
      { label: "全部分类", value: "全部" },
      ...formatContentCategories(availableCategories.value || []),
    ]);
    const searcha = ref(null);
    const searcha2 = ref(null);
    function userContent(fansNumber, videoNumber, introduce) {
      if (introduce !== null)
        return `${fansNumber}粉丝 ·  ${videoNumber}个视频 · ${introduce}`;
      else return `${fansNumber}粉丝 ·  ${videoNumber}个视频`;
    }
    const isVisible = ref(false); // 用于控制盒子的可见性
    const handleScroll = () => {
      const scrollPosition = window.scrollY; // 当前滚动距离
      isVisible.value = scrollPosition > 155; // 当滚动超过155px时显示盒子
    };
    const datea = ref("");
const clickFlag = ref(1);
const clickFlag1 = computed(() => clickFlag.value === 1);
const clickFlag2 = computed(() => clickFlag.value === 2);
const clickFlag3 = computed(() => clickFlag.value === 3);
const clickFlag4 = computed(() => clickFlag.value === 4);
const clickFlag5 = computed(() => clickFlag.value === 5);
const clickFlag6 = computed(() => clickFlag.value === 6);
const clickFlag7 = computed(() => clickFlag.value === 7);

// 结果分类页签。原来是 7 个 ref + 7 个各自把另外 6 个置 false 的函数，
// 写错一个就会让两个 tab 同时高亮。改成单一 clickFlag，页签表由它派生。
const resultTabs = computed(() => [
  { flag: 1, label: "综合", total: null },
  { flag: 2, label: "视频", total: acceptSearchData.videoTotal },
  { flag: 7, label: "用户", total: acceptSearchData.userTotal },
]);

const tabEls = new Map();
const sortLine = reactive({ visible: false, x: 0, w: 0 });

function setTabEl(el, flag) {
  if (el) tabEls.set(flag, el);
  else tabEls.delete(flag);
}

function measureSortLine() {
  const el = tabEls.get(clickFlag.value);
  if (!el) {
    sortLine.visible = false;
    return;
  }
  sortLine.x = el.offsetLeft;
  sortLine.w = el.offsetWidth;
  sortLine.visible = true;
}

watch(
  () => [clickFlag.value, acceptSearchData.videoTotal, acceptSearchData.userTotal],
  () => {
    nextTick(measureSortLine);
  }
);

    onMounted(() => {
      nextTick(measureSortLine);
    });
    // 四个筛选维度的当前选中项下标。原先是 42 个布尔 flag，
    // 每点一次要把同维度的其余 flag 全部置 false。
    const sortIndex = ref(0);
    const userSortIndex = ref(0);
    const dateIndex = ref(0);
    const timeIndex = ref(0);
    const classifyIndexRef = ref(0);
    watch(datea, (newValue) => {
      if (newValue !== null) {
        if (newValue.length !== 0) {
          const date1 = new Date(datea.value[0]);
          const date2 = new Date(datea.value[1]);
          if (!isNaN(date1.getTime())) {
            acceptSearchData.startTime = date1.toISOString();
          }
          if (!isNaN(date2.getTime())) {
            acceptSearchData.endTime = date2.toISOString();
          }
          videoPageNum.value = 1;
          searchByKeyWordVideo();
        }
        if (newValue.length === 0) {
          acceptSearchData.startTIme = "";
          acceptSearchData.endTime = "";
        }

        /* 选了自定义日期区间就把「最近一天/一周/半年」都取消，
           回到「全部日期」。原判断是三个 flag 同时为真 —— 但单选结构下
           三个 flag 不可能同时为真，这个分支实际永远进不去，
           改成判断「当前没停在任何预设日期上」。 */
        if (newValue.length !== 0) {
          dateIndex.value = 4;
        } else if (dateIndex.value >= 1 && dateIndex.value <= 3) {
          dateIndex.value = 0;
        }
      }
    });

   function handleUrlChange(keyword){
      searcha.value.Content = keyword || '';
      searcha2.value.Content = keyword || '';
      if (clickFlag1.value || clickFlag2.value) {
        videoPageNum.value = 1;
        searchByKeyWordVideo();
      }else if(clickFlag7.value){
        userPageNum.value = 1;
        selectUsersAxios();
      }
   }

    function ClickFlag(n) {
      clickFlag.value = n;
      nextTick(measureSortLine);
      if (n === 7) {
        selectUsersAxios();
      }
    }
    /* ---------- 筛选 ----------
       原来这里是 42 个 Click*FlagN 函数：每个函数把自己的 flag 置 true、
       同一维度的其余 4~21 个 flag 逐一置 false，分类组每个函数 22 行赋值，
       整块 770 行。五组筛选其实是同一个「单选」结构，收成一个 applyFilter。 */
    function applyFilter(dim, index) {
      if (dim === "classify") {
        acceptSearchData.classify = CLASSIFY.value[index].value || CLASSIFY.value[index].label;
        const params = new URLSearchParams(window.location.search);
        params.delete("categoryId");
        params.delete("classifyIndex");
        if (index === 0) params.delete("categoryName");
        else params.set("categoryName", acceptSearchData.classify);
        window.history.replaceState({}, document.title, `${window.location.pathname}?${params.toString()}`);
      } else {
        acceptSearchData[dim] = index;
      }
      if (dim === "userSort") {
        userPageNum.value = 1;
        selectUsersAxios();
        return;
      }
      videoPageNum.value = 1;
      searchByKeyWordVideo();
    }
    // 保留原函数名，模板里的 @click 不必逐个改
    const ClickSortFlag1 = () => applyFilter("sort", 0);
    const ClickSortFlag2 = () => applyFilter("sort", 1);
    const ClickSortFlag3 = () => applyFilter("sort", 2);
    const ClickSortFlag4 = () => applyFilter("sort", 3);
    const ClickSortFlag5 = () => applyFilter("sort", 4);
    const ClickUserFlag1 = () => applyFilter("userSort", 0);
    const ClickUserFlag2 = () => applyFilter("userSort", 1);
    const ClickUserFlag3 = () => applyFilter("userSort", 2);
    const ClickUserFlag4 = () => applyFilter("userSort", 3);
    const ClickUserFlag5 = () => applyFilter("userSort", 4);
    const ClickDateFlag1 = () => applyFilter("date", 0);
    const ClickDateFlag2 = () => applyFilter("date", 1);
    const ClickDateFlag3 = () => applyFilter("date", 2);
    const ClickDateFlag4 = () => applyFilter("date", 3);
    const ClickDateFlag5 = () => applyFilter("date", 4);
    const ClickTimeFlag1 = () => applyFilter("time", 0);
    const ClickTimeFlag2 = () => applyFilter("time", 1);
    const ClickTimeFlag3 = () => applyFilter("time", 2);
    const ClickTimeFlag4 = () => applyFilter("time", 3);
    const ClickTimeFlag5 = () => applyFilter("time", 4);
    const ClickClassifyFlag1 = () => applyFilter("classify", 0);
    const ClickClassifyFlag2 = () => applyFilter("classify", 1);
    const ClickClassifyFlag3 = () => applyFilter("classify", 2);
    const ClickClassifyFlag4 = () => applyFilter("classify", 3);
    const ClickClassifyFlag5 = () => applyFilter("classify", 4);
    const ClickClassifyFlag6 = () => applyFilter("classify", 5);
    const ClickClassifyFlag7 = () => applyFilter("classify", 6);
    const ClickClassifyFlag8 = () => applyFilter("classify", 7);
    const ClickClassifyFlag9 = () => applyFilter("classify", 8);
    const ClickClassifyFlag10 = () => applyFilter("classify", 9);
    const ClickClassifyFlag11 = () => applyFilter("classify", 10);
    const ClickClassifyFlag12 = () => applyFilter("classify", 11);
    const ClickClassifyFlag13 = () => applyFilter("classify", 12);
    const ClickClassifyFlag14 = () => applyFilter("classify", 13);
    const ClickClassifyFlag15 = () => applyFilter("classify", 14);
    const ClickClassifyFlag16 = () => applyFilter("classify", 15);
    const ClickClassifyFlag17 = () => applyFilter("classify", 16);
    const ClickClassifyFlag18 = () => applyFilter("classify", 17);
    const ClickClassifyFlag19 = () => applyFilter("classify", 18);
    const ClickClassifyFlag20 = () => applyFilter("classify", 19);
    const ClickClassifyFlag21 = () => applyFilter("classify", 20);
    const ClickClassifyFlag22 = () => applyFilter("classify", 21);
    function highlightText(title) {
      /* 关键词先转义成正则安全形式，命中处包 <span class="highlight">，
         交给 homeVideoCard 的 vcard-title 深插行上色 */
      const word = (acceptSearchData.keyWord || "").trim();
      if (!word || !title) return title;
      const escaped = word.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
      return title.replace(
        new RegExp(`(${escaped})`, "g"),
        '<span class="highlight">$1</span>'
      );
    }

    //获取用户ip和token
    // token 由 /auth/login 返回后存在本地，这里不再向后端要 IP + token。
    async function getUserIp(){
      store.setUserIp("0.0.0.0");
    }
    onMounted(async () => {
      window.scrollTo({ top: 0, behavior: "smooth" });
      windowWidth.value = window.screen.width;
      onloadPage.value = true;
      await getUserIp();
      try {
        availableCategories.value = await videoApi.categories();
      } catch (error) {
        ElMessage.error(error.message || "搜索分区加载失败");
      }
      await searchByKeyWordVideoOnce();
      selectUsersAxios();
      const classifyTitle =
        CLASSIFY.value.find((c) => (c.value || c.label) === acceptSearchData.classify)?.label ||
        acceptSearchData.classify;
      document.title = acceptSearchData.keyWord || classifyTitle + "-青芒视频";
      window.addEventListener("scroll", handleScroll); // 监听滚动事件
    });
    onUnmounted(() => {
      document.removeEventListener("scroll", handleScroll); // 移除监听滚动事件
    });

    /* 新接口的记录直接就是 homeVideoCard 消费的形状
       （id/title/coverUrl/playUrl/durationSeconds/ownerId/ownerNickname/
       ownerAvatar/playCount/danmakuCount/watchLater），
       与首页视频列表共用同一组件，字段无需再转换。 */
    function toVideoCard(r) {
      return { ...r };
    }

    /* 新接口的 total 在「还有下一页」时返回 -1（不查总数），
       此时用已取到的条数兜底，否则页码条会算出负数。 */
    function normalizeTotal(total, shown) {
      return total >= 0 ? total : shown;
    }

    function currentKeyword() {
      return new URLSearchParams(window.location.search).get("keyword") || "";
    }

    async function searchByKeyWordVideoOnce() {
      try {
        const keyWord = currentKeyword();
        const params = new URLSearchParams(window.location.search);
        const classifyIndex = params.get("categoryName") || params.get("classifyIndex");
        acceptSearchData.keyWord = keyWord;
        if (classifyIndex) {
          acceptSearchData.classify = classifyIndex;
        } else if (params.get("categoryId")) {
          if (!availableCategories.value) availableCategories.value = await videoApi.categories();
          const category = availableCategories.value.find(item => String(item.id) === params.get("categoryId"));
          if (!category) throw new Error("该分区不存在，请重新选择");
          acceptSearchData.classify = category.name;
        }
        acceptSearchData.classifyIndex = classifyIndex;
        acceptSearchData.videoPageNum = 1;
        videoPageNum.value = 1;
        await fetchVideos();
      } catch (error) {
        ElMessage({
          message: error.message || "搜索失败，请稍后重试",
          type: "error",
          duration: 1700,
        });
      }
    }

    //根据关键字搜索视频
    async function searchByKeyWordVideo() {
      try {
        const keyWord = currentKeyword();
        acceptSearchData.videoPageNum = videoPageNum.value;
        acceptSearchData.keyWord = keyWord;
        userPageNum.value = 1;
        await fetchVideos();
      } catch (error) {
        ElMessage({
          message: error.message || "搜索失败，请稍后重试",
          type: "error",
          duration: 1700,
        });
      }
    }

    /* api/product 的 get 已经把 {code,msg,data} 的 data 拆出来了，
       这里拿到的是 PageResult 本体，不要再取 response.data.data。 */
    async function fetchVideos() {
      let categoryId;
      if (acceptSearchData.classify !== "全部") {
        if (!availableCategories.value) availableCategories.value = await videoApi.categories();
        const category = availableCategories.value.find(item => item.name === acceptSearchData.classify);
        if (!category) throw new Error("该分区暂不可用，请选择其他分区");
        categoryId = category.id;
      }
      const params = {
        keyword: acceptSearchData.keyWord,
        categoryId,
        sort: sortIndex.value === 2 ? "newest" : "hot",
        pageNum: videoPageNum.value,
        pageSize: 20,
      };
      const data = acceptSearchData.keyWord.trim()
        ? await searchApi.videos(params)
        : await videoApi.list({ ...params, sort: sortIndex.value === 2 ? "latest" : "hot" });
      if (!data) return;
      const records = (data.records || []).map(toVideoCard);
      Videos.length = 0;
      Object.assign(Videos, records);
      acceptSearchData.videoTotal = normalizeTotal(data.total, records.length);
    }

    //根据关键词搜索用户
    async function selectUsersAxios() {
      try {
        const keyWord = currentKeyword();
        if (!keyWord) {
          return;
        }
        acceptSearchData.keyWord = keyWord;
        acceptSearchData.userPageNum = userPageNum.value;
        videoPageNum.value = 1;
        const data = await searchApi.users({
          keyword: keyWord,
          pageNum: userPageNum.value,
          pageSize: 20,
        });
        if (!data) return;
        const records = data.records || [];
        searchUserList.length = 0;
        Object.assign(searchUserList, records);
        acceptSearchData.userTotal = normalizeTotal(data.total, records.length);
      } catch (error) {
        ElMessage({
          message: "未知错误",
          type: "error",
          duration: 1700,
        });
      }
    }


    // 更改视频当前页
    function handleCurrentChangeVideo(val) {
      videoPageNum.value = val;
    }

    // 更改视频当前页
    function handleCurrentChangeVideo2(event) {
      if (
        event.target.value !== "" &&
        event.target.value <= Math.ceil(acceptSearchData.videoTotal / 20) &&
        event.target.value >= 1
      )
        videoPageNum.value = parseInt(event.target.value);
    }

    // 更改用户当前页
    function handleCurrentChangeUser(val) {
      userPageNum.value = val;
    }

    // 更改用户当前页
    function handleCurrentChangeUser2(event) {
      if (
        event.target.value !== "" &&
        event.target.value <= Math.ceil(acceptSearchData.userTotal / 20) &&
        event.target.value >= 1
      )
        userPageNum.value = parseInt(event.target.value);
    }

    //监视视频页数变化
    watch(videoPageNum, () => {
      searchByKeyWordVideo();
    });

    //监视用户页数变化
    watch(userPageNum, () => {
      selectUsersAxios();
    });

    //关注
    //关注
    /* 新后端的 /user/{userId}/follow 是「切换」语义（返回切换后的 following），
       没有单独的取关端点。两个按钮共用它，按钮的乐观更新交给随后的重新拉取。 */
    async function toggleFollowAxios(userId) {
      if (store.userId === null) {
        loginDialogVisibleFlag.value =
          loginDialogVisibleFlag.value === 0 ? 1 : 0;
        return;
      }
      if (String(store.userId) === String(userId)) {
        ElMessage({
          message: "不能关注自己哦",
          type: "warning",
          duration: 1700,
        });
        return;
      }
      try {
        await socialApi.follow(userId);
        await selectUsersAxios();
      } catch (error) {
        ElMessage({
          message: "操作失败",
          type: "error",
          duration: 1700,
        });
      }
    }

    const addFollowAxios = toggleFollowAxios;
    const deleteFollowAxios = toggleFollowAxios;


    /* 稍后观看 / 悬停预览 / 跳转详情等交互已收进 homeVideoCard 组件：
       waitWatch、locationHerfVideo、videoMouseover、videoMouseleave
       以及 videoAutoPlayTIme 定时器表一并移除，卡片行为与首页完全一致。 */

      /* 分类选中态跟着 acceptSearchData.classify 走。
         原来这里是 21 个 else if 把分类名映射到对应 flag，且只置 true
         不清其余 —— 从 URL 带分类进来时会和默认的「全部分类」同时高亮。
         改成按名称查表取下标，表里没有就回到 0。 */
      watch(
        [() => acceptSearchData.classify, CLASSIFY],
        ([name]) => {
          const i = CLASSIFY.value.findIndex((c) => (c.value || c.label) === name || c.label === name);
          classifyIndexRef.value = i < 0 ? 0 : i;
        },
        { immediate: true }
      );

    function openHome(userId) {
      window.open(`./home?userId=${userId}&homeMenu=1`, "_blank");
    }

    return {
      onloadPage,
      isVisible,
      clickFlag,
      clickFlag1,
      clickFlag2,
      clickFlag3,
      clickFlag4,
      clickFlag5,
      clickFlag6,
      clickFlag7,
      ClickFlag,
      resultTabs,
      sortLine,
      setTabEl,
      // 筛选：模板改用数据表 + 下标，Click*FlagN 保留是为了不改模板里的 @click
      SORT,
      USER_SORT,
      DATE,
      TIME,
      CLASSIFY,
      sortIndex,
      userSortIndex,
      dateIndex,
      timeIndex,
      classifyIndexRef,
      applyFilter,
      ClickSortFlag1,
      ClickSortFlag2,
      ClickSortFlag3,
      ClickSortFlag4,
      ClickSortFlag5,
      ClickUserFlag1,
      ClickUserFlag2,
      ClickUserFlag3,
      ClickUserFlag4,
      ClickUserFlag5,
      ClickDateFlag1,
      ClickDateFlag2,
      ClickDateFlag3,
      ClickDateFlag4,
      ClickDateFlag5,
      datea,
      ClickTimeFlag1,
      ClickTimeFlag2,
      ClickTimeFlag3,
      ClickTimeFlag4,
      ClickTimeFlag5,
      ClickClassifyFlag1,
      ClickClassifyFlag2,
      ClickClassifyFlag3,
      ClickClassifyFlag4,
      ClickClassifyFlag5,
      ClickClassifyFlag6,
      ClickClassifyFlag7,
      ClickClassifyFlag8,
      ClickClassifyFlag9,
      ClickClassifyFlag10,
      ClickClassifyFlag11,
      ClickClassifyFlag12,
      ClickClassifyFlag13,
      ClickClassifyFlag14,
      ClickClassifyFlag15,
      ClickClassifyFlag16,
      ClickClassifyFlag17,
      ClickClassifyFlag18,
      ClickClassifyFlag19,
      ClickClassifyFlag20,
      ClickClassifyFlag21,
      ClickClassifyFlag22,
      expanded,
      Videos,
      acceptSearchData,
      highlightText,
      selectUsersAxios,
      searchUserList,
      userContent,
      addFollowAxios,
      deleteFollowAxios,
      store,
      loginDialogVisibleFlag,
      openHome,
      handleCurrentChangeUser,
      handleCurrentChangeUser2,
      handleCurrentChangeVideo,
      handleCurrentChangeVideo2,
      windowWidth,
      videoPageNum,
      userPageNum,
      handleUrlChange,
      searcha,
      searcha2
    };
  },
};
</script>

<style lang="scss">
* {
  padding: 0; /* 移除内边距 */
  margin: 0; /* 移除外边距 */
  box-sizing: border-box; /* 包括内边距和边框在元素的总宽度和高度中 */
}

/* 原来写死 width:1425px 靠 left:50% + translateX(-50%) 居中，
   视口窄于 1425px 就必然出现横向滚动条。改成 max-width 由页面容器控宽。 */
.SearchBox {
  user-select: none;
  height: auto;
  position: relative;
  width: 100%;
  max-width: var(--page-max);
  margin: 0 auto;
}

/* 搜索页顶部：大搜索框 + 结果页签，共用一个容器。
   原来这两块是两个各自独立的 absolute/fixed 盒子，
   搜索框靠 left:50% + translate(-50%) 居中、页签靠 height:160px 定位，
   两者的间距是隐式产生的 —— 页头改高或搜索框改宽就会错位。 */
.searchShell {
  position: relative;
  z-index: var(--z-base);
  background: #fff;
  border-bottom: 1px solid var(--line);
}

/* 搜索页顶部的大搜索框：容器自己居中并限宽，
   内部 Searcha 铺满容器宽度。 */
.searchBox2 {
  position: static;
  display: flex;
  justify-content: center;
  box-sizing: border-box;
  width: 100%;
  padding: var(--gap-4) var(--page-pad) 0;
}

.searchBox2-inner {
  width: 100%;
  max-width: 560px;
}


.hiddenBox {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  box-sizing: border-box;
  width: 100%;
  height: var(--head-h);
  padding: 0 var(--page-pad);
  border-bottom: 1px solid var(--line);
  box-shadow: 0 4px 18px rgba(38, 66, 37, .06);
  position: fixed;
  top: 0;
  left: 0;
  transform: translateY(-100%);
  visibility: hidden;
  opacity: 0;
  pointer-events: none;
  background-color: white;
  z-index: -1000;
}

.showHiddenBox {
  opacity: 1;
  transition: all 0.3s ease;
  visibility: visible;
  pointer-events: auto;
  transform: translateY(0px);
  z-index: var(--z-head);
}

.hiddenBox .sticky-search-field {
  flex: 0 1 560px;
  min-width: 0;
  width: 100%;
}

.sticky-search-home {
  flex: none;
  color: #37684a;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 2px;
  text-decoration: none;
}

/* 页头外壳：sticky 高度取全局 --head-h，不再自己写 65px。
   原先这里 height:65px、外层页头 64px，两套值差 1px，阴影会露出细缝。 */
.head {
  position: relative;
  z-index: var(--z-head);
  box-sizing: border-box;
  width: 100%;
  height: var(--head-h);
  background-color: #fff;
  box-shadow: var(--shadow-2);
}

.headLoginIndex {
  z-index: var(--z-sticky);
}

/* 页签行 */
.middle {
  position: static;
  width: 100%;
}

/* 结果分类页签。
   原来 .sort 是 width:50% + translate(15px,122.5px)，下划线另有 7 个写死坐标；
   现在页签按内容宽度排布，选中态走 .is-on，下划线用 .sort-underline 由脚本定位。 */
.sort {
  position: relative;
  display: flex;
  align-items: center;
  gap: 34px;
  width: 100%;
  box-sizing: border-box;
  padding: 0 var(--page-pad) 10px;
  font-size: 15.5px;
}

/* 结果区。左侧留白来自这里 —— 原来结果网格靠
   translate(64px, -507px) 把每张卡片往右推 64px，
   页头/页签/筛选栏则各自用不同的 --page-pad，三者对不齐。 */
.content {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  margin: 0 auto;
  padding: var(--gap-4) var(--page-pad) 0;
}

.sort .ac {
  display: inline-block;
  min-width: 20px;
  height: 16px;
  margin-left: 5px;
  padding: 0 5px;
  font-size: 12px;
  line-height: 16px;
  text-align: center;
  background-color: #eaeaea;
  border-radius: 5px;
  color: #919090;
  vertical-align: middle;
}
.sort .aw {
  position: relative;
  cursor: pointer;
  color: var(--ink-2);
  white-space: nowrap;
  transition: color .2s ease;
}
.sort .aw.is-on {
  color: var(--brand);
  font-weight: 600;
}
.sort .aw:hover {
  color: var(--brand);
}

.sort-underline {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  border-radius: 3px;
  background-color: var(--brand);
  transition: transform .25s ease, width .25s ease;
}

/* ---------- 筛选栏 ----------
   原来每个选项两个 span 各自 position:absolute + translate(x,y)，
   未选中态与选中态的坐标还差一个固定值（如 86/64、196/174），
   靠这个差值让文字在切换时不动。现在一项一个 button，
   未选中 / 选中只差一个 .is-on 类，位置交给 flex。 */
.videoSort {
  position: static;
  width: 100%;
}

.filter-bar {
  display: flex;
  align-items: flex-start;
  gap: var(--gap-3);
}

.filter-row {
  display: flex;
  flex: 1 1 auto;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--gap-2);
  min-width: 0;
  padding: var(--gap-2) 0;
}

.chip {
  height: 32px;
  padding: 0 14px;
  border: 1px solid transparent;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--ink-2);
  font-size: 14px;
  line-height: 30px;
  white-space: nowrap;
  cursor: pointer;
  transform: none;
  transition: background-color .2s, color .2s;
}

.chip:hover {
  background: var(--fill);
  color: var(--brand);
}

.chip.is-on {
  background: var(--brand-soft);
  color: var(--brand);
  font-weight: 600;
}

.chip-has-sub {
  position: relative;
  padding-right: 20px;
}

.chip-has-sub::after {
  content: "";
  position: absolute;
  top: 13px;
  right: 8px;
  width: 5px;
  height: 5px;
  border-right: 1px solid currentColor;
  border-bottom: 1px solid currentColor;
  transform: rotate(45deg);
  opacity: .5;
}

/* 更多筛选：靠 max-height 过渡，不用一个 height:170px 的空 div 撑高度 */
.filter-toggle {
  display: flex;
  flex: none;
  align-items: center;
  gap: 4px;
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: #fff;
  color: var(--ink-2);
  font-size: 14px;
  white-space: nowrap;
  cursor: pointer;
  transform: none;
  transition: background-color .2s, color .2s;
}

.filter-toggle:hover {
  border-color: var(--brand-light-5);
  color: var(--brand);
}

.filter-toggle img {
  width: 11px;
  margin: 0;
  transition: transform .2s;
}

.filter-toggle img.is-open {
  transform: rotate(0deg);
}

.filter-panel {
  overflow: hidden;
  max-height: 0;
  opacity: 0;
  transition: max-height .28s ease, opacity .2s ease;
}

.filter-panel.expanded {
  max-height: 320px;
  opacity: 1;
}

.filter-date {
  width: 310px;
  max-width: 100%;
  height: 32px;
  border-radius: var(--radius-md);
}

/* 子分类气泡 */
.sub-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--gap-2);
  max-width: 420px;
  cursor: pointer;
}

.sub-tag {
  padding: 3px 10px;
  border-radius: var(--radius-sm);
  color: var(--ink-2);
  font-size: 13px;
  white-space: nowrap;
  transition: background-color .2s, color .2s;
}

.sub-tag:hover {
  background: var(--brand-soft);
  color: var(--brand);
}


/* 结果网格：卡片复用 homeVideoCard 组件，间距 / 悬停效果与首页列表一致；
   列数固定 5 列封顶（宽屏不再随 auto-fill 挤出第 6 列），
   窄屏由 mainSearchLayout.scss 的 1100 / 768 / 520 断点降为 3 / 2 / 1 列。 */
.bottomVideo {
  position: static;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 20px 16px;
  width: 100%;
  margin-top: var(--gap-4);
}

.videoBottomInfo {
  position: static;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  transform: none;
  cursor: pointer;
}

.upInfo {
  width: auto;
  color: var(--ink-3);
  font-size: 12.5px;
  transform: none;
  position: static;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color .2s ease;
}

.videoBottomInfo:hover .upInfo {
  color: var(--brand);
}

.title:hover {
  color: #0FA68E;
}
.highlight {
  color: #E06B33 !important;
}
/* 标题高亮留给 homeVideoCard 的深插行 —— vcard-title 的关键词命中色 */
:deep(.vcard-title .highlight) {
  color: #E06B33;
}

/* 搜索结果的用户卡片：原 .searchUsers 宽 91.15% + absolute + translate(-10px,-10px)，
   每个 .usersContent 再 flex:0 0 calc(40% - 20px) + width:200px + margin:10px
   + translate(64.5px,104.5px)，三套宽度互相对不上。 */
.searchUsers {
  position: static;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--gap-5);
  width: 100%;
  margin-top: var(--gap-4);
  transform: none;
}

.usersContent {
  position: relative;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--gap-2);
  width: auto;
  margin: 0;
  transform: none;
}

.usersContent img {
  width: 85px;
  height: 85px;
  border-radius: 50%;
  cursor: pointer;
  object-fit: cover;
}

.user-Name {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  max-width: 100%;
  color: var(--ink);
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  transform: none;
}

.user-Name:hover {
  color: var(--brand);
}

.user-grade {
  flex: none;
  margin: 0 0 0 4px;
}

.userInfo-Content {
  max-width: 100%;
  color: var(--ink-3);
  font-size: 12.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transform: none;
  position: static;
}

/* 关注 / 已关注：跟着卡片流式排布，原为 absolute + translate(100px,-55px) */
.follow,
.deleteFollow {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 32px;
  border-radius: var(--radius-md);
  font-size: 13.5px;
  transform: none;
  position: static;
  cursor: pointer;
  transition: opacity .2s;
}

.follow {
  background-color: var(--brand);
  color: #fff;
}

.deleteFollow {
  background-color: var(--fill);
  color: var(--ink-2);
}

.follow:hover,
.deleteFollow:hover {
  opacity: .8;
}

/* 分页条是 .bottomVideo 这个 grid 的一个 item，要占满整行，
   否则会被当成第 9 张卡片塞进第二行的第一列。 */
.bottomVideo .pager-row {
  grid-column: 1 / -1;
}

.pager-row {
  display: flex;
  justify-content: center;
  width: 100%;
  margin: var(--gap-4) 0 0;
  transform: none;
  position: static;
}

.page-container {
  position: static;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 0;
}

.custom-tooltip1 {
  display: flex;
  align-items: center;
  padding: 5px;
  border-radius: var(--radius-sm) !important;
}

.userInfo-Content {
  display: flex;
  align-items: center;
  max-width: 100%;
  color: var(--ink-3);
  font-size: 12.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transform: none;
  position: static;
}

.introduce {
  margin-left: 8px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.page-container span {
  color: var(--ink-2);
  font-size: 13px;
  margin-left: 16px;
  white-space: nowrap;
}

.page-container input {
  width: 50px;
  height: 32px;
  margin: 0 4px;
  padding: 0 8px;
  background-color: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  color: var(--ink);
  font-size: 13px;
  text-align: center;
  outline: none;
  transition: border-color .2s;
}

.page-container input:hover,
.page-container input:focus {
  border-color: var(--brand);
}

.page-container input::-webkit-inner-spin-button,
.page-container input::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* 断点原为 .bottomVideo 写死 width 1050/787.5/525/263px 并按 25%/33%/50%/100%
   分配列数：容器宽度定死 + flex-basis 百分比同时生效，两者一旦不同步
   就会出现横向溢出或右侧大片空白（实测 1280 视口下文档宽 1644）。
   现在 .bottomVideo 是 auto-fill grid，列数由可用宽度和 minmax(228px,1fr)
   自行决定，不再需要按视口枚举列宽。 */
</style>
<style lang="scss" scoped src="./mainSearchLayout.scss"></style>
