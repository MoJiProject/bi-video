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
          <img v-show="display === 2" src="../img/grid1.png" />
          <img v-show="display === 2" src="../img/grid1蓝.png" />
          <img v-show="display === 1" src="../img/grid5.png" />
          <img v-show="display === 1" src="../img/grid5蓝.png" />
        </div>
        <div v-show="display == 1 && !loadMore && contributeVideos.length>0" class="video-list-container">
          <videoList6 :data="contributeVideos" />
        </div>
        <div v-show="display == 2 && !loadMore && contributeVideos.length>0" class="video-list-container">
          <videoList4 :data="contributeVideos" />
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
            <div v-show="!loadMore&&dynamicList.length>0" class="dynamic-item" v-for="dynamic in dynamicList" :key="dynamic.dynamic.id">
                <dynamic :dynamic="dynamic"/>
            </div>
            <div v-if="fotterFlag&&dynamicList.length>0&&!loadMore2&&!loadMore" class="no-more">你已经到达世界的尽头~</div>
        </div>
      </div>
      <div v-show="loadMore" class="class-loading">
        <img src="../img/loading-blue.gif" />
        <span>拼命加载中...</span>
      </div>
      <div v-show="!loadMore&&(searchMenu===1?contributeVideos.length===0:dynamicList.length===0)" class="no-data">
            <img src="../img/home_nodata.svg">
            <div>{{'没有找到任何关于"'+keyword+(searchMenu===1?'"的视频':'"的动态')}}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { useGlobalStore } from "../store/store";
import dynamic from "@/utils/dynamic.vue";
import { homeDynamic,homeContributeVideos } from "@/api/home/index";
import { getEitList } from "@/api/user/index";
import videoList4 from "@/utils/videoList4.vue";
import videoList6 from "@/utils/videoList6.vue";

const store = useGlobalStore();
const searchMenu = ref(1);
const loadMore = ref(true);
const loadMore2 = ref(false);
const display = ref(1);
const sort = ref(1);
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || null;
const pageNum = ref(1);
const pageNum2 = ref(1);
const dynamicList = reactive([]);
const fotterFlag = ref(false);
const contributeVideos = reactive([]);
const contributeTotal = ref(0);
const dynamicTotal = ref(0);
const keyword = ref(new URL(window.location).searchParams.get("keyword") || "");

onMounted(async () => {
   
  store.setKeyword(keyword.value);
  if (store.homeUserInformation.id) {
    homeContributeVideosF();
    await homeDynamicF();
    store.setHomeLoad(true, "homeBody");
  }

  if (store.userId) {
    getEitListF();
  }

  window.addEventListener("scroll", handleScroll);
});

onUnmounted(() => {
  store.setHomeLoad(false, "homeBody");
});

// 监听store变化
let onceFlag = true;
watch(
  () => store.homeUserInformation.id,
  async () => {
    if (onceFlag && store.homeUserInformation.id) {
      onceFlag = null;
      homeContributeVideosF();
      await homeDynamicF();
      store.setHomeLoad(true, "homeBody");
    }
  },
);

let onceFlag2 = true;
watch(
  () => store.userId,
  async () => {
    if (onceFlag2 && store.userId) {
      onceFlag2 = null;
      await getEitListF();
    }
  },
);

watch(searchMenu, () => {
  pageNum.value = 1;
  fotterFlag.value = false;
  store.commentOpenFlag = false;
  dynamicList.length = 0;
  if (store.homeUserInformation.id) homeDynamicF();
});

//获取动态列表
async function homeDynamicF() {
  if (dynamicList.length === 0) {
    loadMore.value = true;
  } else {
    loadMore2.value = true;
  }
  let date = new Date();
  homeDynamic(
    store.homeUserInformation.id,
    store.userId,
    pageNum2.value++,
    1,
    keyword.value,
  ).then((res) => {
    if (res.data.code === 1) {
      pushData(res.data.data.records);
      dynamicTotal.value = res.data.data.total;
      if (!res.data.data.records || res.data.data.records.length === 0) {
        fotterFlag.value = true;
      }
      const date2 = new Date();
      setTimeout(
        () => {
          loadMore.value = false;
          loadMore2.value = false;
        },
        date2 - date < 300 ? 300 : date2 - date,
      );
    }
  });
}

// 获取用户主页投稿的视频
async function homeContributeVideosF() {
  loadMore.value = true;
  let date = new Date();
  homeContributeVideos(
    store.homeUserInformation.id,
    store.userId,
    sort.value,
    pageNum.value,
    keyword.value,
  ).then((res) => {
    if (res.data.code === 1) {
      contributeVideos.length = 0;
      Object.assign(contributeVideos, res.data.data.records);
      contributeTotal.value = res.data.data.total;
      let date2 = new Date();
      setTimeout(
        () => {
          loadMore.value = false;
        },
        date2 - date < 300 ? 300 : date2 - date,
      );
    }
  });
}

//监视置顶动态变化
watch(
  () => store.upFlag,
  () => {
    if (store.upFlag) {
      const index = dynamicList.findIndex(
        (item) => item.dynamic.id === store.upFlag,
      );
      let temp = dynamicList[index];
      dynamicList.splice(index, 1);
      dynamicList.unshift(temp);
      dynamicList.forEach((item) => {
        if (item.dynamic.id !== store.upFlag) item.dynamic.upFlag = 0;
      });
      store.upFlag = null;
    }
  },
);

//监视删除动态变化
watch(
  () => store.deleteFlag,
  () => {
    if (store.deleteFlag) {
      const index = dynamicList.findIndex(
        (item) => item.dynamic.id === store.deleteFlag,
      );
      dynamicList.splice(index, 1);
      store.deleteFlag = null;
    }
  },
);

// 监听sort变化
watch(sort, () => {
  if (store.homeUserInformation.id) homeContributeVideosF();
});

//滚动加载
function handleScroll() {
  if (!loadMore2.value && !loadMore.value && !fotterFlag.value && searchMenu.value === 2) {
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
  const idSet = new Set(dynamicList.map((item) => item.dynamic.id)); // 获取已有数据的 ID
  const uniqueData = newData.filter((item) => !idSet.has(item.dynamic.id)); // 过滤掉重复的
  dynamicList.push(...uniqueData); // Vue 响应式更新
}

//获取eit列表
async function getEitListF() {
  getEitList(store).then((res) => {
    if (res.data.code === 1) {
      store.setEitList(res.data.data);
    }
  });
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

//监视页数变化
watch(pageNum, () => {
  if (store.homeUserInformation.id && searchMenu.value === 1)
    homeContributeVideosF();
});

//重置页数
watch(searchMenu, () => {
  pageNum.value = 1;
  pageNum2.value = 1;
  if (store.homeUserInformation.id && searchMenu.value === 1)
    homeContributeVideosF();
  else if (store.homeUserInformation.id && searchMenu.value === 2)
    homeDynamicF();
});

watch(()=>store.keyword,()=>{
    pageNum.value = 1;
    pageNum2.value = 1;
    keyword.value=store.keyword;
    if (store.homeUserInformation.id && searchMenu.value === 1)
        homeContributeVideosF();
    else if (store.homeUserInformation.id && searchMenu.value === 2)
        homeDynamicF();
})
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
      background-color: #00aeec;
      margin-bottom: 8px;
    }
    .activeMenu {
      background-color: white;
      color: #18191c;
      transition: background-color 0.3s ease;
    }
    .activeMenu:hover {
      background-color: #f1f2f3;
    }
  }
  .content-container {
    position: relative;
    width: 1139px;
    padding-bottom: 60px;

    .search-menu {
      .title {
        font-size: 24px;
        font-weight: 600;
        color: #18191c;
        line-height: 34px;
      }
      .find-info {
        color: #18191c;
        margin-top: 16px;
        font-size: 14px;
        font-weight: 400;
        line-height: 20px;

        span {
          color: #00aeec;
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
          color: #61666d;
          cursor: pointer;
          transition: all 0.3s;
          border-radius: 6px;
          background-color: #f6f7f8;
        }
        span:hover {
          color: #00aeec;
        }
        .sortClass {
          color: white !important;
          background-color: #00aeec !important;
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
          color: #18191c;
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
            border: 1px solid #e3e5e7;
            font-size: 14px;
            border-radius: 6px;
            transition: all 0.3s ease;
            padding: 0 10px;
          }
          input:hover {
            border-color: #00aeec;
          }
          input:focus {
            border-color: #00aeec;
          }
          input::-webkit-inner-spin-button,
          input::-webkit-outer-spin-button {
            -webkit-appearance: none;
          }
        }
      }
      .dynamic-item{
        width: 1000px;
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
        color: #9499A0;
        user-select: none;
      }
    }

    .class-loading {
      position: relative;
      margin-top: 60px;
      display: flex;
      justify-content: center;

      img {
        width: 30px;
        height: 30px;
      }
      span {
        position: relative;
        top: 5px;
        left: 5px;
        font-size: 14px;
        color: #61666d;
      }
    }

    .no-data{
        user-select: none;
        width: 101.3%;
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
                color: #9499A0;
                
            }
      }
  }
}
</style>
