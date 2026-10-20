<template>
  <div class="dynamic-container">
    <div class="aside-container">
      <div :class="{ activeMenu: searchMenu !== 1 }" @click="searchMenu = 1">
        <span>视频</span><span>{{ contributeTotal }}</span>
      </div>
      <div :class="{ activeMenu: searchMenu !== 2 }" @click="searchMenu = 2">
        <span>动态</span><span>{{ dynamicTotal }}</span>
      </div>
    </div>
    <div class="content-container">
      <div v-show="searchMenu === 1" class="search-menu">
        <div class="title">
          {{
            store.userId !== null && store.userId == userId
              ? "我的视频"
              : "TA的视频"
          }}
        </div>
        <div v-show="contributeVideos.length>0" class="find-info">共找到关于"{{ keyword }}"的 <span>{{ contributeTotal }}</span> 个视频</div>
        <div v-show="contributeVideos.length>0" class="video-sort">
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
        <div v-show="contributeVideos.length>0" class="video-grid"
          @click="display === 1 ? (display = 2) : (display = 1)"
        >
          <img v-show="display === 2" src="/img/grid1.png" />
          <img v-show="display === 2" src="/img/grid1蓝.png" />
          <img v-show="display === 1" src="/img/grid5.png" />
          <img v-show="display === 1" src="/img/grid5蓝.png" />
        </div>
        <div v-show="display == 1 && !loadMore && contributeVideos.length>0" class="video-list-container">
          <videoList6 :data="contributeVideos" />
        </div>
        <div v-show="display == 2 && !loadMore && contributeVideos.length>0" class="video-list-container">
          <videoList4 :data="contributeVideos" layout="list"/>
        </div>
        <div v-show="!loadMore && contributeTotal > 0" class="page-container">
          <el-pagination
            :current-page="pageNum"
            :page-size="20"
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
      <div v-show="searchMenu === 2" class="search-menu">
        <div class="title">
          {{
            store.userId !== null && store.userId == userId
              ? "我的动态"
              : "TA的动态"
          }}
        </div>
        <div v-show="dynamicList.length>0" class="find-info">共找到关于"{{ keyword }}"的 <span>{{ dynamicTotal }}</span> 个动态</div>
        <div>
            <div v-show="!loadMore&&dynamicList.length>0" class="dynamic-item" v-for="dynamic in dynamicList" :key="dynamic.id">
                <dynamicC :dynamic="dynamic"/>
            </div>
            <div v-if="fotterFlag&&dynamicList.length>0&&!loadMore2&&!loadMore" class="no-more">你已经到达世界的尽头~</div>
        </div>
      </div>
      <loadingIndicator v-show="loadMore" min-height="160px"/>
      <div v-show="!loadMore&&(searchMenu===1?contributeVideos.length===0:dynamicList.length===0)" class="no-data">
            <img src="/img/home_nodata.svg">
            <div>{{'没有找到任何关于"'+keyword+(searchMenu===1?'"的视频':'"的动态')}}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { useGlobalStore } from "../store/store";
import dynamicC from "./homePost.vue";
import { homeDynamic,homeContributeVideos } from "@/api/home/index";
import { ElMessage } from "element-plus";
import videoList4 from "./homeVideos.vue";
import videoList6 from "./homeVideos.vue";
import loadingIndicator from "@/components/loadingIndicator.vue";

const store = useGlobalStore();
const searchMenu = ref(1);
const loadMore = ref(true);
const loadMore2 = ref(false);
const display = ref(1);
const sort = ref(1);
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || store.userId;
const pageNum = ref(1);
const pageNum2 = ref(1);
const dynamicList = reactive([]);
const fotterFlag = ref(false);
const contributeVideos = reactive([]);
const contributeTotal = ref(0);
const dynamicTotal = ref(0);
const keyword = ref(new URL(window.location).searchParams.get("keyword") || "");

onMounted(() => {
  window.addEventListener("scroll", handleScroll);
});
onUnmounted(() => window.removeEventListener("scroll", handleScroll));
store.setKeyword(keyword.value);
let requestId = 0;

//获取动态列表
async function homeDynamicF() {
  const current = requestId;
  if (dynamicList.length === 0) {
    loadMore.value = true;
  } else {
    loadMore2.value = true;
  }
  if(pageNum2.value === 1) 
  {
    dynamicList.length = 0;
  }
  try {
    const res = await homeDynamic(store.homeUserInformation.id, store.userId, pageNum2.value, 1, keyword.value);
    if (current !== requestId) return;
      pushData(res.data.data.records);
      dynamicTotal.value = res.data.data.total;
      fotterFlag.value = dynamicList.length >= dynamicTotal.value;
      pageNum2.value++;
  } catch (error) {
    if (current === requestId) {
      ElMessage.error(error.message);
      fotterFlag.value = true;
    }
  } finally {
    if (current === requestId) {
      loadMore.value = false;
      loadMore2.value = false;
      store.setHomeLoad(true, "homeBody");
      store.setHomeLoadMenuList(10);
    }
  }
}

// 获取用户主页投稿的视频
async function homeContributeVideosF() {
  const current = requestId;
  loadMore.value = true;
  try {
    const res = await homeContributeVideos(store.homeUserInformation.id, store.userId, sort.value, pageNum.value, keyword.value);
    if (current !== requestId) return;
      contributeVideos.length = 0;
      Object.assign(contributeVideos, res.data.data.records);
      contributeTotal.value = res.data.data.total;
  } catch (error) {
    if (current === requestId) ElMessage.error(error.message);
  } finally {
    if (current === requestId) {
      loadMore.value = false;
      store.setHomeLoad(true, "homeBody");
      store.setHomeLoadMenuList(10);
    }
  }
}

//监视删除动态变化
watch(
  () => store.deleteFlag,
  () => {
    if (store.deleteFlag) {
      const index = dynamicList.findIndex(
        (item) => item.id === store.deleteFlag,
      );
      if (index !== -1) {
        dynamicList.splice(index, 1);
        dynamicTotal.value--;
      }
    }
  },
);

// 监听sort变化
watch([sort, searchMenu, () => store.keyword], () => {
  pageNum.value = 1;
  pageNum2.value = 1;
  fotterFlag.value = false;
  dynamicList.length = 0;
  keyword.value = store.keyword;
}, { flush: 'sync' });

watch([() => store.homeUserInformation.id, searchMenu, sort, pageNum, keyword], ([id]) => {
  if (!id) return;
  requestId++;
  if (searchMenu.value === 1) homeContributeVideosF();
  else homeDynamicF();
}, { immediate: true });

//滚动加载
function handleScroll() {
  if (store.homeMenu === 10 && !loadMore2.value && !loadMore.value && !fotterFlag.value && searchMenu.value === 2) {
    if (
      window.innerHeight + document.documentElement.scrollTop >=
      document.documentElement.offsetHeight - 100
    ) {
      homeDynamicF();
    }
  }
}

//去除重复数据
function pushData(newData) {
  if (newData === null) return;
  const idSet = new Set(dynamicList.map((item) => item.id));
  const uniqueData = newData.filter((item) => !idSet.has(item.id));
  dynamicList.push(...uniqueData); // Vue 响应式更新
}

// 更改当前页
function handleCurrentChange(val) {
  pageNum.value = val;
}

// 更改当前页
function handleCurrentChange2(event) {
  if (
    event.target.value !== "" &&
    event.target.value <= Math.ceil(contributeTotal.value / 20) &&
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

.dynamic-container {
  display: flex;
  padding-left: 60px;
  padding-top: 30px;

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

    div {
      width: 150px;
      height: 52px;
      font-size: 14px;
      cursor: pointer;
      border-radius: 6px;
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
  .content-container {
    position: relative;
    width: 100%;
    padding-bottom: 60px;

    .search-menu {
      .title {
        font-size: 24px;
        font-weight: 600;
        color: #1C2321;
        line-height: 34px;
      }
      .find-info {
        color: #1C2321;
        margin-top: 16px;
        font-size: 14px;
        font-weight: 400;
        line-height: 20px;

        span {
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

        img {
          position: absolute;
          top: 3px;
          width: 21px;
          height: 21px;
          transition: opacity 0.3s ease;
        }
        img:nth-child(2n) {
          opacity: 0;
        }
      }
      .video-grid:hover {
        img:nth-child(n + 1) {
          opacity: 0;
        }
        img:nth-child(2n) {
          opacity: 1;
        }
      }
      .video-list-container{
        position: relative;
        margin-top: 60px;
        padding-bottom: 60px;
      }
      .page-container {
        position: relative;
        display: flex;
        justify-content: center;
        align-items: center;

        span {
          color: #1C2321;
          font-size: 13px;
          margin-left: 44px;
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
      .dynamic-item{
        width: 100%;
        margin-top: 30px;
      }
      .no-more{
        position: relative;
        padding-top: 30px;
        left: -8px;
        width: 100%;
        display: flex;
        align-items: end;
        justify-content: center;
        font-size: 14px;
        color: #8D9794;
        user-select: none;
      }
    }

    .no-data{
        user-select: none;
        width: 100%;
        height: 58vh;
        display: flex;
        justify-content: center;
        align-items: center;
        flex-direction: column;  
            img{
                width: 140px;
                height: 140px;
            }
            div{
                margin-top: 6px;
                font-size: 14px;
                line-height: 20px;
                font-weight: 400;
                color: #8D9794;
                
            }
      }
  }
}
</style>
