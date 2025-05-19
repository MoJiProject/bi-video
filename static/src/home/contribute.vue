<template>
  <div class="contribute">
    <div class="aside-container">
      <div
        :class="{ activeMenu: contributeMenu !== 1 }"
        @click="contributeMenu = 1"
      >
        <span>视频</span>
        <span>{{ contributeTotal }}</span>
      </div>
      <div
        :class="{ activeMenu: contributeMenu !== 2 }"
        @click="contributeMenu = 2"
      >
        <span>图文</span>
        <span>{{ imgDynamicTotal }}</span>
      </div>
    </div>
    <div class="content">
      <div v-show="contributeMenu === 1" class="contribute-menu">
        <div class="title">
          {{
            store.userId !== null && store.userId == userId
              ? "我的视频"
              : "TA的视频"
          }}
        </div>
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
        <div v-show="display == 1 && !loadMore" class="video-list-container">
          <videoList3 :data="contributeVideos" />
        </div>
        <div v-show="display == 2 && !loadMore" class="video-list-container">
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
              :key="dynamic.id"
              :dynamic="dynamic"
            />
          </div>
          <div v-if="imgDynamicTotal === 0" class="no-data">
            <img src="../img/home_nodata.svg" />
            <div v-if="store.userId !== null && store.userId == userId">
              你还没有发布图文内容
            </div>
            <div v-else>空间主人还没投过图文内容，这里什么也没有...</div>
          </div>
        </div>
        <div v-show="!loadMore && imgDynamicTotal > 0" class="page-container">
          <el-pagination
            :current-page="pageNum"
            :page-size="20"
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
      <div v-show="loadMore" class="class-loading">
        <img src="../img/loading-blue.gif" />
        <span>拼命加载中...</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useGlobalStore } from "@/store/store";
import { onMounted, onUnmounted, reactive, ref, watch } from "vue";
import videoList3 from "@/utils/videoList3";
import videoList4 from "@/utils/videoList4";
import imgDynamic from "@/utils/imgDynamic.vue";
import { homeContributeVideos, dynamicImg } from "@/api/home/index";

const store = useGlobalStore();
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || null;
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

onMounted(async () => {
  if (store.homeUserInformation.id) {
    homeContributeVideosF();
    await dynamicImgF();
    store.setHomeLoad(true, "homeBody");
  }
});

onUnmounted(() => {
  store.setHomeLoad(false, "homeBody");
});

// 获取用户主页投稿的视频
async function homeContributeVideosF() {
  loadMore.value = true;
  let date = new Date();
  homeContributeVideos(
    store.homeUserInformation.id,
    store.userId,
    sort.value,
    pageNum.value,
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

// 获取动态图文
async function dynamicImgF() {
  let date = new Date();
  loadMore.value = true;
  dynamicImg(store.homeUserInformation.id, imgSource.value, pageNum.value).then(
    (res) => {
      if (res.data.code === 1) {
        imgDynamicList.length = 0;
        Object.assign(imgDynamicList, res.data.data.records);
        imgDynamicTotal.value = res.data.data.total;
        let date2 = new Date();
        setTimeout(
          () => {
            loadMore.value = false;
          },
          date2 - date < 300 ? 300 : date2 - date,
        );
      }
    },
  );
}

// 监听sort变化
watch(sort, () => {
  if (store.homeUserInformation.id) homeContributeVideosF();
});

// 监听store变化
let onceFlag = true;
watch(
  () => store.homeUserInformation.id,
  async () => {
    if (onceFlag && store.homeUserInformation.id) {
      onceFlag = false;
      homeContributeVideosF();
      await dynamicImgF();
      store.setHomeLoad(true, "homeBody");
    }
  },
);

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
  if (store.homeUserInformation.id && contributeMenu.value === 1)
    homeContributeVideosF();
  else if (store.homeUserInformation.id && contributeMenu.value === 2)
    dynamicImgF();
});

//重置页数
watch(contributeMenu, () => {
  pageNum.value = 1;
  if (store.homeUserInformation.id && contributeMenu.value === 1)
    homeContributeVideosF();
  else if (store.homeUserInformation.id && contributeMenu.value === 2)
    dynamicImgF();
});

//监视图片源变化
watch(imgSource, () => {
  if (store.homeUserInformation.id && contributeMenu.value === 2) dynamicImgF();
});
</script>

<style lang="scss" scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.contribute {
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
  .content {
    position: relative;
    width: 1139px;
    padding-bottom: 60px;

    .contribute-menu {
      .title {
        font-size: 24px;
        font-weight: 600;
        color: #18191c;
        line-height: 34px;
      }
      .title2 {
        display: flex;
        align-items: center;
        span {
          cursor: pointer;
          display: flex;
          align-items: center;
          color: #9499a0;
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
          background-color: #e3e5e7;
        }
        .activeImgSource {
          color: #18191c !important;
        }
        span:hover {
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
          column-count: 5; /* 让一行有 5 列 */
          column-gap: 35px; /* 设置列间距 */
        }
        .no-data {
          user-select: none;
          width: 1139px;
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
            color: #9499a0;
          }
        }
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
  }
}
</style>
