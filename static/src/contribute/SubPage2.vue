<template>
  <div class="creator-manager">
    <div class="manager-heading">
      <h1>稿件管理</h1>
      <router-link to="/contribute">＋ 视频投稿</router-link>
    </div>
    <div class="manuscript-statuses" aria-label="稿件状态">
      <button type="button" @click="allUpload" :class="{ active: click4 === 1 }" :aria-pressed="click4 === 1">
        <span>全部稿件</span><strong>{{ UserVideoVo.videoAllNumber }}</strong>
      </button>
      <button type="button" @click="waitUpload" :class="{ active: click1 === 1 }" :aria-pressed="click1 === 1">
        <span>进行中</span><strong>{{ UserVideoVo.videoWaitNumber }}</strong>
      </button>
      <button type="button" @click="successUpload" :class="{ active: click2 === 1 }" :aria-pressed="click2 === 1">
        <span>已通过</span><strong>{{ UserVideoVo.videoSuccessNumber }}</strong>
      </button>
      <button type="button" @click="errorUpload" :class="{ active: click3 === 1 }" :aria-pressed="click3 === 1">
        <span>未通过</span><strong>{{ UserVideoVo.videoErrorNumber }}</strong>
      </button>
    </div>
    <div class="manuscript-panel">
      <div class="manuscript-filters">
        <el-input @keypress.enter="searchVideo" v-model="videoTitle" maxlength="30" placeholder="搜索稿件" aria-label="搜索稿件" class="manuscript-search">
          <template #append><el-button @click="searchVideo" aria-label="搜索">搜索</el-button></template>
        </el-input>
      <div class="manuscript-filter-row">
      <el-select v-model="subZoneKey" aria-label="筛选分类">
        <el-option
          @click="selectsubZoneKeyVideo"
          v-for="item in classify"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-select v-model="sortWay" aria-label="稿件排序">
        <el-option
          @click="sortVideo"
          v-for="item in sort"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      </div>
      </div>
      <div class="manuscript-list">
      <article v-for="(video, index) in videoList" :key="index" class="manuscript-card content-action-surface">
        <div class="manuscript-cover">
          <img
            @click="openVideo(video)"
            :src="video.coverAddress"
            :alt="video.title"
            tabindex="0"
            role="button"
            @keydown.enter="openVideo(video)"
            @keydown.space.prevent="openVideo(video)"
          />
          <span class="videoTime">{{ video.videoTime }}</span>
        </div>
            <button
              type="button"
              class="videoTitle"
              @click="openVideo(video)"
              >{{ video.title }}</button>
          <div class="manuscript-meta">
            <span class="manuscript-state" :class="`state-${video.status}`">{{ video.status === 0 ? '进行中' : video.status === 2 ? '未通过' : '已通过' }}</span>
            <time>{{ video.createTime }}</time>
          </div>
          <div class="manuscript-actions content-card-actions">
          <ContentMoreMenu :label="`${video.title}的稿件操作`">
            <el-dropdown-item @click="$router.push({ path: '/contribute/edit', query: { videoId: video.id } })">编辑</el-dropdown-item>
            <el-dropdown-item v-if="video.status !== 0 || video.status !== 2" @click="viewVideoData">数据</el-dropdown-item>
            <el-dropdown-item class="is-danger" @click="tempDelete(video)">删除</el-dropdown-item>
          </ContentMoreMenu>
          <div
            v-if="video.status === 2"
            class="manuscript-rejection"
          >
            退回原因：{{ video.examineFiledMessage }}
          </div>
          </div>
          <div
            v-if="video.status !== 0 || video.status !== 2"
            class="manuscript-metrics"
          >
            <el-tooltip
              content="播放"
              placement="bottom"
              effect="light"
              :hide-after="0"
              :show-arrow="false"
            >
              <span class="ai">
                <img src="/img/真播放量.png" /><span>{{
                  video.playNumber
                }}</span></span
              >
            </el-tooltip>
            <el-tooltip
              content="点赞"
              placement="bottom"
              effect="light"
              :hide-after="0"
              :show-arrow="false"
            >
              <span class="ai">
                <img src="/img/点赞.png" /><span>{{ video.likeNumber }}</span>
              </span>
            </el-tooltip>
            <el-tooltip
              content="弹幕"
              placement="bottom"
              effect="light"
              :hide-after="0"
              :show-arrow="false"
            >
              <span class="ai">
                <img src="/img/弹幕.png" /><span>{{
                  video.scrollingNumber
                }}</span>
              </span>
            </el-tooltip>
            <el-tooltip
              content="评论"
              placement="bottom"
              effect="light"
              :hide-after="0"
              :show-arrow="false"
            >
              <span class="ai">
                <img src="/img/评论.png" /><span>{{
                  video.commentNumber
                }}</span>
              </span>
            </el-tooltip>
            <el-tooltip
              content="收藏"
              placement="bottom"
              effect="light"
              :hide-after="0"
              :show-arrow="false"
            >
              <span class="ai">
                <img src="/img/真收藏.png" /><span
                  >{{ video.collectNumber }}
                </span></span
              >
            </el-tooltip>
            <el-tooltip
              content="分享"
              placement="bottom"
              effect="light"
              :hide-after="0"
              :show-arrow="false"
            >
              <span class="ai">
                <img src="/img/分享.png" /><span>{{
                  video.shareNumber
                }}</span>
              </span>
            </el-tooltip>
          </div>
      </article>
      </div>
      <div v-if="!videoList.length">
        <AppEmptyState title="没有符合条件的稿件"/>
      </div>

      <div
        style="
          display: flex;
          justify-content: right;
          width: 100%;
          margin-top: 30px;
        "
      >
        <el-pagination
          :current-page="pageNum"
          :page-size="5"
          :total="total"
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
          :hide-on-single-page="true"
        />
      </div>
    </div>
    <el-dialog
      v-model="dialogVisible"
      class="creator-dialog"
      append-to-body
      width="min(392px, calc(100vw - 32px))"
      title="删除稿件"
      center
      :modal="false"
    >
      <span>
        确定要删除该稿件吗？
      </span>
      <template #footer>
        <div class="dialog-footer">
          <el-button v-debounce class="yesDelete" @click="deleteVideo">确定</el-button>
          <el-button v-debounce
            @click="dialogVisible = false"
            >取消</el-button
          >
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, watch } from "vue";
import apiClient from "../services/apiClient";
import { fetchPublishingCategories } from "../api/product";
import { ElMessage } from "element-plus";
import ContentMoreMenu from "../components/ContentMoreMenu.vue";
import {useGlobalStore} from "../store/store";
export default {
  name: "SubPage2",
  components: { ContentMoreMenu },
  setup() {
    const store = useGlobalStore();
    const dialogVisible = ref(false);
    const videoStatus = ref(-1);
    const pageNum = ref(1);
    const total = ref(0);
    const videoList=reactive([]);

    const handleCurrentChange = (page) => {
      pageNum.value = page;
      selectAllUploadVideo();
      window.scrollTo({top: 0, behavior: "smooth"});
    };
    const videoTitle = ref("");
    const subZoneKey = ref("全部分类");
    const sortWay = ref("发布时间排序");
    const click1 = ref(0);
    const click2 = ref(0);
    const click3 = ref(0);
    const click4 = ref(1);
    function waitUpload() {
      pageNum.value = 1;
      click1.value = 1;
      click2.value = 0;
      click3.value = 0;
      click4.value = 0;
      videoStatus.value = 0;
      selectAllUploadVideo();
    }
    function successUpload() {
      pageNum.value = 1;
      click1.value = 0;
      click2.value = 1;
      click3.value = 0;
      click4.value = 0;
      videoStatus.value = 1;
      selectAllUploadVideo();
    }
    function errorUpload() {
      pageNum.value = 1;
      click1.value = 0;
      click2.value = 0;
      click3.value = 1;
      click4.value = 0;
      videoStatus.value = 2;
      selectAllUploadVideo();
    }
    function allUpload() {
      pageNum.value = 1;
      click1.value = 0;
      click2.value = 0;
      click3.value = 0;
      click4.value = 1;
      videoStatus.value = -1;
      selectAllUploadVideo();
    }

    const classify = ref([
      { label: "全部分类", value: "全部分类" },
    ]);

    const sort = [
      { label: "发布时间排序", value: "发布时间排序" },
      { label: "播放量排序", value: "播放量排序" },
      { label: "收藏量排序", value: "收藏量排序" },
      { label: "弹幕量排序", value: "弹幕量排序" },
      { label: "评论数排序", value: "评论数排序" },
    ];

    const UserVideoVo = reactive({
      videos: [],
      users: {
        avatarAddress: "",
        collectNumber: 0,
        createTime: "",
        dynamicNumber: 0,
        grade: "",
        id: 0,
        messageNumber: 0,
        gender: "",
        userName: "", // 示例用户名
        users: "",
        videoNumber: 0,
        token: "",
        exp: 0,
        followNumber: 0,
        ownDynamicNumber: 0,
        fansNumber: 0,
        introduce: "",
      },
      videoSuccessNumber: 0,
      videoErrorNumber: 0,
      videoWaitNumber: 0,
      videoAllNumber: 0,
      total: 0,
      setVideos(newVideos) {
        this.videos = newVideos; // 更新视频数组
      },
    });

    async function selectAllUploadVideo() {
      try {
        const response = await apiClient.get("/video/userUploadVideo", {
          params: {
            userId: store.userId,
            videoTitle: videoTitle.value, // 确保使用.value获取值
            subZoneKey: subZoneKey.value, // 确保使用.value获取值
            sortWay: sortWay.value, // 确保使用.value获取值
            videoStatus: videoStatus.value, // 确保使用.value获取值
            pageNum: pageNum.value, // 确保使用.value获取值
          },
            headers: {
            "Content-Type": "application/json",
            "Authorization": store.token,
          },
        });
        if (response.data.code === 1) {
          videoList.length = 0;
          Object.assign(UserVideoVo, response.data.data);
          Object.assign(videoList, response.data.data.videos);
          total.value = response.data.data.total;
        }
      } catch (error) {
        ElMessage({
          message: error.message || "加载投稿列表失败",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
    }

    onMounted(() => {
      window.scrollTo({top: 0, behavior: "smooth"});
      selectAllUploadVideo();
      fetchPublishingCategories().then(rows => { classify.value.push(...rows); })
        .catch(error => ElMessage.error(error.message || "加载投稿分区失败"));
    });

    function searchVideo() {
      pageNum.value = 1;
      selectAllUploadVideo();
    }

    function selectsubZoneKeyVideo() {
      pageNum.value = 1;
      selectAllUploadVideo();
    }
    function sortVideo() {
      pageNum.value = 1;
      selectAllUploadVideo();
    }

    async function deleteVideo() {
      try {
        const response = await apiClient.delete("/video/deleteVideo", {
          params: {
            videoId: videoTemp.id,
            userId: UserVideoVo.users.id,
            videoStatus: videoTemp.status,
          },
            headers: {
            "Content-Type": "application/json",
            "Authorization": store.token,
          },
        });
        if (response.data.code === 1) {
          ElMessage({
            message: response.data.data,
            type: "info",
            plain: true,
            duration: 1700,
          });
          selectAllUploadVideo();
          dialogVisible.value = false;
        } else if (response.data.code === 0) {
          ElMessage({
            message: response.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
          });
        }
      } catch (error) {
        ElMessage({
          message: "未知错误",
          type: "info",
          plain: true,
          duration: 1700,
        });
      }
    }

    const videoTemp = reactive({});
    function tempDelete(video) 
    {
      Object.assign(videoTemp, video);
      dialogVisible.value = true;
    }

    function viewVideoData() {
      ElMessage({
        message: "该功能尚未开发(累)",
        type: "info",
        plain: true,
        duration: 1700,
      });
    }

    //打开视频页
    function openVideo(video) {
       
       if(video.status!==1)
       return;
       else{
         window.open("../video?videoId=BV"+video.id,"videoWindow");
       }
     }


     watch(videoTitle,()=>{
        if(videoTitle.value.length===0)
          selectAllUploadVideo();
     })


    return {
      videoTitle,
      UserVideoVo,
      subZoneKey,
      classify,
      sort,
      sortWay,
      click1,
      click2,
      click3,
      click4,
      waitUpload,
      successUpload,
      errorUpload,
      allUpload,
      total,
      videoList,
      pageNum,
      handleCurrentChange,
      searchVideo,
      selectsubZoneKeyVideo,
      sortVideo,
      deleteVideo,
      dialogVisible,
      tempDelete,
      viewVideoData,
      openVideo,
    };
  },
};
</script>
<style scoped>
.main-content {
  background-color: #f8f8f8;
  border-radius: 5px;
  width: 100%;
  min-height: 1500px;
  height: auto !important;
  padding: 20px;
  box-sizing: border-box;
}
.white {
  width: 100%;
  max-width: var(--page-max);
  overflow: visible;
  z-index: 10;
  min-height: 1500px;
  height: auto !important;
  margin: 0 auto;
  background-color: white;
  padding: 20px;
  box-sizing: border-box;
}

.empty-manuscripts {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  width: 100%;
  padding: 68px 16px;
  font-size: 14px;
  text-align: center;
}

.videoTime {
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 12px;
  background-color: rgba(0, 0, 0, 0.65);
  border-radius: 5px;
  width: 38.08px;
  height: 18px;
  color: white;
  position: absolute;
  transform: translate(166px, 7.5px);
}

.ai img {
  width: 13px;
  height: 13px;
  transform: translate(0px, 1px);
}
.ai {
  font-size: 11px;
  color: #aaaaaa;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.yesDelete {
  background-color: #0E9C85;
  color: white;
  border: none;
  width: 90px;
  height: 28px;
}

.yesDelete:hover {
  color: white;
  background-color: #5DB0AC;
}

.videoTitle{
  color: black;
  transition: color 0.3s ease;
}

.videoTitle:hover{
  color: #0FA68E;
}

@media (max-width: 1100px) {
  .main-content {
    padding: 12px;
  }
  .white {
    padding: 12px;
  }
}
</style>
<style scoped lang="scss">
.creator-manager { min-width: 0; color: var(--ink); font-family: var(--el-font-family); }
.manager-heading { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; margin-bottom: 24px; }
.manager-heading h1 { margin: 0; font-size: 24px; font-weight: 600; }
.manager-heading a { display: inline-flex; align-items: center; min-height: 38px; padding: 0 16px; border-radius: 10px; background: var(--brand); color: #fff; text-decoration: none; font-size: 13px; }
.manuscript-statuses { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 18px; }
.manuscript-statuses button { box-sizing: border-box; display: flex; align-items: center; justify-content: space-between; gap: 16px; min-width: 112px; max-width: 100%; min-height: 40px; padding: 8px 12px; border: 1px solid var(--line); border-radius: 8px; background: #fff; color: var(--ink-2); font: inherit; font-size: 13px; text-align: left; cursor: pointer; }
.manuscript-statuses button:hover { border-color: var(--brand-light-7); background: var(--fill-soft); }
.manuscript-statuses button.active { border-color: var(--brand-light-7); background: var(--brand-light-9); color: var(--brand-hover); }
.manuscript-statuses strong { font-size: 18px; font-weight: 500; line-height: 1.25; font-variant-numeric: tabular-nums; overflow-wrap: anywhere; min-width: 0; }
.manuscript-panel { padding: 24px; border: 1px solid var(--line); border-radius: 18px; background: #fff; }
.manuscript-filters { display: flex; flex-direction: column; align-items: flex-start; gap: 12px; padding-bottom: 20px; border-bottom: 1px solid var(--line); }
.manuscript-filters .manuscript-search { flex: none; width: 340px; max-width: 100%; min-width: 0; }
.manuscript-filter-row { display: flex; align-items: center; flex-wrap: wrap; gap: 12px; width: 100%; }
.manuscript-filters .el-select { flex: 0 1 132px; width: 132px; max-width: 100%; min-width: 0; }
.manuscript-filters :deep(.el-input__wrapper), .manuscript-filters :deep(.el-select__wrapper) { min-height: 40px; }
.manuscript-list { display: flex; flex-direction: column; gap: 16px; padding-top: 20px; }
.manuscript-card { display: grid; grid-template-columns: 176px minmax(0, 1fr); gap: 10px 20px; min-width: 0; padding: 18px; border: 1px solid var(--line); border-radius: 14px; }
.manuscript-cover { position: relative; grid-column: 1; grid-row: 1 / 5; align-self: start; min-width: 0; border-radius: 10px; overflow: hidden; }
.manuscript-cover > img { display: block; width: 100%; aspect-ratio: 16 / 9; object-fit: cover; cursor: pointer; }
.manuscript-card .videoTime { position: absolute; right: 6px; bottom: 6px; width: auto; height: auto; padding: 2px 5px; transform: none; font-size: 10px; }
.manuscript-card .videoTitle { grid-column: 2; grid-row: 1; min-width: 0; padding: 0; border: 0; background: none; font: inherit; font-size: 15px; line-height: 1.6; text-align: left; color: var(--ink); overflow-wrap: anywhere; cursor: pointer; }
.manuscript-card .videoTitle:hover { color: var(--brand); }
.manuscript-meta { grid-column: 2; grid-row: 2; display: flex; align-items: center; flex-wrap: wrap; gap: 8px 12px; color: var(--ink-3); font-size: 12px; }
.manuscript-state { padding: 3px 8px; border-radius: 6px; background: var(--brand-light-9); color: var(--brand-hover); font-size: 11px; }
.manuscript-state.state-0 { background: #fff7e7; color: #966400; }
.manuscript-state.state-2 { background: #fff0f0; color: #b44242; }
.manuscript-actions { grid-column: 2; grid-row: 4; display: flex; align-items: center; justify-content: flex-end; flex-wrap: wrap; gap: 10px; padding-top: 2px; }
.manuscript-rejection { order: 1; flex-basis: 100%; padding-top: 4px; color: #b44242; font-size: 12px; line-height: 1.6; overflow-wrap: anywhere; }
.manuscript-metrics { grid-column: 2; grid-row: 3; display: flex; flex-wrap: wrap; gap: 8px 18px; }
.manuscript-metrics .ai { gap: 5px; color: var(--ink-3); font-size: 11px; }
.manuscript-metrics .ai img { width: 13px; height: 13px; object-fit: contain; transform: none; }
.creator-manager button:focus-visible, .creator-manager a:focus-visible { outline: 2px solid var(--brand); outline-offset: 3px; }
.yesDelete { background: var(--brand); }
.yesDelete:hover { background: var(--brand-hover); }
@media (max-width: 900px) {
  .manuscript-card { grid-template-columns: 144px minmax(0, 1fr); gap: 10px 16px; }
}
@media (max-width: 640px) {
  .manager-heading { margin-bottom: 18px; }
  .manager-heading h1 { font-size: 21px; }
  .manuscript-statuses { margin-bottom: 16px; }
  .manuscript-statuses button { min-width: 104px; padding: 8px 10px; }
  .manuscript-panel { padding: 14px; border-radius: 14px; }
  .manuscript-filters { gap: 10px; }
  .manuscript-filter-row { gap: 10px; }
  .manuscript-card { grid-template-columns: 104px minmax(0, 1fr); gap: 10px 12px; padding: 12px; }
  .manuscript-cover { grid-row: 1 / 3; }
  .manuscript-card .videoTitle { font-size: 13px; }
  .manuscript-meta { gap: 6px; font-size: 11px; }
  .manuscript-metrics { grid-column: 1 / -1; gap: 10px 14px; }
  .manuscript-actions { grid-column: 1 / -1; }
}
@media (max-width: 360px) {
  .manuscript-card { grid-template-columns: minmax(0, 1fr); }
  .manuscript-cover, .manuscript-card .videoTitle, .manuscript-meta, .manuscript-metrics, .manuscript-actions { grid-column: 1; grid-row: auto; }
}
</style>
