<template>
  <div class="contribute">
    <div class="aside-container" role="group" aria-label="投稿类型">
      <button type="button" class="content-type-tab" :aria-pressed="contributeMenu === 1"
        :class="{ activeMenu: contributeMenu !== 1 }"
        @click="contributeMenu = 1"
      >
        <span>视频</span>
        <span>{{ contributeTotal }}</span>
      </button>
      <button type="button" class="content-type-tab" :aria-pressed="contributeMenu === 2"
        :class="{ activeMenu: contributeMenu !== 2 }"
        @click="contributeMenu = 2"
      >
        <span>图文</span>
        <span>{{ imgDynamicTotal }}</span>
      </button>
    </div>
    <div class="content" :class="{ 'has-profile': showProfile }">
      <SpaceProfileCard v-if="showProfile" class="contribute-profile"/>
      <div class="contribute-stream">
      <div v-show="contributeMenu === 1" class="contribute-menu">
        <div class="title">
          {{
            store.userId !== null && store.userId == userId
              ? "我的视频"
              : "TA的视频"
          }}
        </div>
        <div class="video-filter space-search">
          <el-input v-model="keyword" placeholder="搜索投稿视频" clearable maxlength="50"
            @keyup.enter="searchVideos" @clear="searchVideos"/>
          <el-button type="primary" @click="searchVideos">搜索</el-button>
        </div>
        <div class="video-toolbar">
        <div class="video-sort">
          <span :class="{ sortClass: sort === 1 }" @click="sort = 1"
            >最新发布</span
          >
          <span :class="{ sortClass: sort === 2 }" @click="sort = 2"
            >最多播放</span
          >
          <span :class="{ sortClass: sort === 3 }" @click="sort = 3"
            >最多收藏</span
          >
        </div>
        <button v-show="contributeVideos.length>0" class="video-grid" type="button"
          :aria-label="display === 1 ? '切换为列表视图' : '切换为网格视图'"
          :title="display === 1 ? '切换为列表视图' : '切换为网格视图'"
          @click="display === 1 ? (display = 2) : (display = 1)"
        >
          <svg v-if="display === 1" viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="4" width="5" height="6" rx="1"/><rect x="3" y="14" width="5" height="6" rx="1"/><path d="M12 5h9M12 9h7M12 15h9M12 19h7"/></svg>
          <svg v-else viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/></svg>
          <span>{{ display === 1 ? '列表' : '网格' }}</span>
        </button>
        </div>
        <div v-show="display == 1 && !loadMore && contributeVideos.length" class="video-list-container">
          <videoList3 :data="contributeVideos" />
        </div>
        <div v-show="display == 2 && !loadMore && contributeVideos.length" class="video-list-container">
          <videoList4 :data="contributeVideos" layout="list"/>
        </div>
        <AppEmptyState v-if="!loadMore && !contributeVideos.length" :kind="appliedKeyword ? 'search' : 'empty'"
          :title="appliedKeyword ? '没有找到匹配的视频' : '还没有投稿视频'"
          :description="appliedKeyword ? '试试其他关键词，发现更多作品' : '精彩作品值得期待，发布后会在这里展示'"/>
        <div v-show="!loadMore && contributeTotal > 0" class="page-container">
          <el-pagination
            :current-page="pageNum"
            :page-size="20"
            :pager-count="5"
            layout="prev, pager, next"
            :total="contributeTotal"
            :background="true"
            @current-change="handleCurrentChange"
          />
          <span
            >共 {{ Math.ceil(contributeTotal / 20) }} 页 /
            {{ contributeTotal }} 个，跳至<input
              type="number"
              @keydown.enter="handleCurrentChange2"
            />页</span
          >
        </div>
      </div>
      <div v-show="contributeMenu === 2" class="contribute-menu">
        <div class="title2">
          <span
            :class="{ activeImgSource: imgSource === 1 }"
            @click="imgSource = 1"
            >全部图文</span
          >
          <span
            :class="{ activeImgSource: imgSource === 2 }"
            @click="imgSource = 2"
            >评论
          </span>
          <span
            :class="{ activeImgSource: imgSource === 3 }"
            @click="imgSource = 3"
            >动态</span
          >
        </div>
        <div v-show="!loadMore" class="img-list-container">
          <div class="img-list">
            <imgDynamic
              v-for="dynamic in imgDynamicList"
              :key="dynamic.sourceType + ':' + dynamic.id"
              :dynamic="dynamic"
              @changed="refreshImages"
            />
          </div>
          <AppEmptyState v-if="imgDynamicTotal === 0" title="还没有图文内容" description="发布的图文与生活分享会在这里展示"/>
        </div>
        <div v-show="!loadMore && imgDynamicTotal > 0" class="page-container">
          <el-pagination
            :current-page="pageNum"
            :page-size="20"
            :pager-count="5"
            layout="prev, pager, next"
            :total="imgDynamicTotal"
            :background="true"
            @current-change="handleCurrentChange"
          />
          <span
            >共 {{ Math.ceil(imgDynamicTotal / 20) }} 页 /
            {{ imgDynamicTotal }} 个，跳至<input
              type="number"
              @keydown.enter="handleCurrentChange2"
            />页</span
          >
        </div>
      </div>
      <loadingIndicator v-show="loadMore" min-height="160px"/>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useGlobalStore } from "@/store/store";
import { computed, reactive, ref, watch } from "vue";
import videoList3 from "./homeVideos.vue";
import videoList4 from "./homeVideos.vue";
import loadingIndicator from "@/components/loadingIndicator.vue";
import imgDynamic from "./homePost.vue";
import SpaceProfileCard from "./SpaceProfileCard.vue";
import { homeContributeVideos, dynamicImg } from "@/api/home/index";
import { ElMessage } from "element-plus";

const store = useGlobalStore();
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || store.userId;
const showProfile = computed(() => (store.userId !== null && store.userId === userId) || store.homeUserInformation.publicBirthday === 1);
const contributeMenu = ref(1);
const contributeVideos = reactive([]);
const imgDynamicList = reactive([]);
const contributeTotal = ref(0);
const imgDynamicTotal = ref(0);
const sort = ref(1);
const display = ref(1);
const imgSource = ref(1);
const loadMore = ref(true);
const pageNum = ref(1);
const keyword = ref('');
const appliedKeyword = ref('');
const refreshVersion = ref(0);
function refreshImages() { refreshVersion.value++; }
function searchVideos() {
  pageNum.value = 1;
  appliedKeyword.value = keyword.value.trim();
}

let requestId = 0;
watch([() => store.homeUserInformation.id, contributeMenu, pageNum, sort, imgSource, appliedKeyword, refreshVersion], async ([id]) => {
  if (!id) return;
  const current = ++requestId;
  loadMore.value = true;
  try {
    const videoRequest = homeContributeVideos(id, store.userId, sort.value, contributeMenu.value === 1 ? pageNum.value : 1, appliedKeyword.value);
    const imageRequest = dynamicImg(id, imgSource.value, contributeMenu.value === 2 ? pageNum.value : 1);
    const [videos, images] = await Promise.all([videoRequest, imageRequest]);
    if (current !== requestId) return;
    contributeVideos.splice(0, contributeVideos.length, ...videos.data.data.records);
    contributeTotal.value = videos.data.data.total;
    imgDynamicList.splice(0, imgDynamicList.length, ...images.data.data.records);
    imgDynamicTotal.value = images.data.data.total;
    if (contributeMenu.value === 2 && pageNum.value > Math.max(1, Math.ceil(imgDynamicTotal.value / 20))) {
      pageNum.value = Math.max(1, Math.ceil(imgDynamicTotal.value / 20));
    }
  } catch (error) {
    if (current === requestId) ElMessage.error(error.message);
  } finally {
    if (current === requestId) {
      loadMore.value = false;
      store.setHomeLoad(true, "homeBody");
      store.setHomeLoadMenuList(3);
    }
  }
}, { immediate: true });

watch([contributeMenu, sort, imgSource], () => { pageNum.value = 1; }, { flush: 'sync' });

watch(() => store.deleteFlag, (id) => {
  const index = imgDynamicList.findIndex(post => post.sourceType !== 2 && post.id === id);
  if (index !== -1) {
    imgDynamicList.splice(index, 1);
    imgDynamicTotal.value--;
  }
});

// 更改当前页
function handleCurrentChange(val) {
  pageNum.value = val;
}

// 更改当前页
function handleCurrentChange2(event) {
  if (
    event.target.value !== "" &&
    event.target.value <= Math.ceil((contributeMenu.value === 1 ? contributeTotal.value : imgDynamicTotal.value) / 20) &&
    event.target.value >= 1
  )
    pageNum.value = parseInt(event.target.value);
}

</script>

<style lang="scss" scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.contribute {
  display: flex;
  padding: var(--gap-5) var(--page-pad);

  .aside-container {
    position: sticky;
    user-select: none;
    top: 80px;
    width: 150px;
    height: 130px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-right: 16px;

    .content-type-tab {
      width: 150px;
      height: 52px;
      font-size: 14px;
      cursor: pointer;
      border-radius: 6px;
      border: 0;
      font-family: inherit;
      padding: 14px 16px;
      color: white;
      display: flex;
      align-items: center;
      justify-content: space-between;
      background-color: #0FA68E;
      margin-bottom: 8px;
    }
    .activeMenu {
      background-color: white;
      color: #1C2321;
      transition: background-color 0.3s ease;
    }
    .activeMenu:hover {
      background-color: #EFF3F2;
    }
  }
  .content {
    position: relative;
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    align-items: start;
    gap: 20px;
    min-width: 0;
    width: 100%;
    padding-bottom: 60px;
    &.has-profile { grid-template-columns: minmax(0, 1fr) 240px; }
    .contribute-profile { grid-column: 2; grid-row: 1; }
    .contribute-stream { grid-column: 1; grid-row: 1; min-width: 0; }
    .video-filter { display: flex; gap: 12px; margin: 16px 0; max-width: 360px; }

    .contribute-menu {
      .title {
        font-size: 24px;
        font-weight: 600;
        color: #1C2321;
        line-height: 34px;
      }
      .title2 {
        display: flex;
        align-items: center;
        span {
          cursor: pointer;
          display: flex;
          align-items: center;
          color: #8D9794;
          font-weight: 600;
          font-size: 24px;
          transition: color 0.3s ease;
        }
        span:nth-child(1)::after,
        span:nth-child(2)::after {
          content: "";
          margin: 0 16px;
          width: 2px;
          height: 20px;
          border-radius: 2px;
          background-color: #E0E5E3;
        }
        .activeImgSource {
          color: #1C2321 !important;
        }
        span:hover {
          color: #0FA68E;
        }
      }
      .video-sort {
        position: relative;
        top: 30px;
        display: flex;
        font-size: 14px;
        column-gap: 12px;
        row-gap: 12px;
        flex-wrap: wrap;
        user-select: none;
        span {
          display: flex;
          align-items: center;
          height: 34px;
          padding: 0 15px;
          flex-shrink: 0;
          color: #5C6664;
          cursor: pointer;
          transition: all 0.3s;
          border-radius: 6px;
          background-color: #F4F7F6;
        }
        span:hover {
          color: #0FA68E;
        }
        .sortClass {
          color: white !important;
          background-color: #0FA68E !important;
        }
      }
      .video-grid {
        position: absolute;
        right: 3.5px;
        width: 21px;
        height: 22px;
        cursor: pointer;
        user-select: none;

      }
      .video-list-container {
        position: relative;
        margin-top: 60px;
        padding-bottom: 60px;
      }
      .img-list-container {
        position: relative;
        margin-top: 32px;
        padding-bottom: 60px;

        .img-list {
          display: grid;
          grid-template-columns: minmax(0, 1fr);
          gap: 16px;
        }
        .no-data {
          user-select: none;
          width: 100%;
          height: 50vh;
          display: flex;
          justify-content: center;
          align-items: center;
          flex-direction: column;
          img {
            width: 140px;
            height: 140px;
          }
          div {
            margin-top: 6px;
            font-size: 14px;
            line-height: 20px;
            font-weight: 400;
            color: #8D9794;
          }
        }
      }
      .page-container {
        position: relative;
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
        justify-content: center;
        align-items: center;

        span {
          color: #1C2321;
          font-size: 13px;
          margin-left: 0;
          input {
            width: 50px;
            height: 34px;
            overflow: hidden;
            display: inline-flex;
            flex-grow: 1;
            outline: none;
            position: relative;
            padding: 0 12px;
            background-color: white;
            border: 1px solid #E0E5E3;
            font-size: 14px;
            border-radius: 6px;
            transition: all 0.3s ease;
            padding: 0 10px;
          }
          input:hover {
            border-color: #0FA68E;
          }
          input:focus {
            border-color: #0FA68E;
          }
          input::-webkit-inner-spin-button,
          input::-webkit-outer-spin-button {
            -webkit-appearance: none;
          }
        }
      }
    }
  }
}

@media (max-width: 960px) {
  .contribute .content.has-profile { grid-template-columns: minmax(0, 1fr); gap: 16px; }
  .contribute .content .contribute-profile { grid-column: 1; grid-row: 1; }
  .contribute .content.has-profile .contribute-stream { grid-row: 2; }
}

@media (max-width: 720px) {
  .contribute { flex-direction: column; padding: var(--gap-4) var(--page-pad); }
  .contribute .aside-container { position: static; flex-direction: row; gap: 8px; width: 100%; height: auto; margin-right: 0; }
  .contribute .aside-container > .content-type-tab { flex: 0 0 auto; min-width: 0; width: auto; }
  .contribute .content .contribute-menu .video-sort { gap: 8px; padding-right: 32px; }
  .contribute .content .contribute-menu .video-sort span { padding: 0 10px; font-size: 12px; }
}
</style>
