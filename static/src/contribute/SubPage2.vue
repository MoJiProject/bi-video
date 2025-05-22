<template>
  <div class="main-content">
    <div class="white">
      <div
        style="
          width: 1200px;
          height: 3px;
          background-color: #f8f8f8;
          transform: translate(0px, 60px);
        "
      ></div>
      <div
        style="
          color: #00a1d6;
          font-weight: 800;
          transform: translate(50px, 22px);
          cursor: pointer;
        "
      >
        视频管理
      </div>
      <div
        style="
          width: 65px;
          height: 3px;
          background-color: #00a1d6;
          transform: translate(50px, 35px);
        "
      ></div>
      <div
        style="
          position: absolute;
          z-index: 10;
          background-color: #f8f8f8;
          transform: translate(1060px, -50px);
          width: 200px;
          height: 1800px;
        "
      ></div>
      <el-input
        @keypress.enter="searchVideo"
        v-model="videoTitle"
        maxlength="30"
        style="
          width: 230px;
          height: 32px;
          font-size: 12px;
          transform: translate(790px, -7px);
        "
        placeholder="搜索稿件"
      />
      <img
        @click="searchVideo"
        src="../img/搜索稿件.png"
        style="transform: translate(768px, -3px); width: 14px;cursor: pointer;"
      />
      <div
        @click="allUpload"
        v-if="click4 === 0"
        style="
          font-size: 14px;
          transform: translate(50px, 20px);
          cursor: pointer;
        "
      >
        全部稿件&nbsp;&nbsp;{{ UserVideoVo.videoAllNumber }}
      </div>
      <div
        @click="allUpload"
        v-else
        style="
          font-size: 14px;
          color: #00a1d6;
          transform: translate(50px, 20px);
          cursor: pointer;
        "
      >
        全部稿件&nbsp;&nbsp;{{ UserVideoVo.videoAllNumber }}
      </div>
      <div
        style="
          display: flex;
          justify-content: space-around;
          width: 256px;
          font-size: 14px;
          color: #8c9195;
          transform: translate(42px, 28px);
        "
      >
        <span v-if="click1 === 0" @click="waitUpload" style="cursor: pointer"
          >进行中&nbsp;{{ UserVideoVo.videoWaitNumber }}</span
        >
        <span v-else @click="waitUpload" style="cursor: pointer; color: #00a1d6"
          >进行中&nbsp;{{ UserVideoVo.videoWaitNumber }}</span
        >
        <span style="color: #e1e1e1">|</span>
        <span v-if="click2 === 0" @click="successUpload" style="cursor: pointer"
          >已通过&nbsp;{{ UserVideoVo.videoSuccessNumber }}</span
        >
        <span
          v-else
          @click="successUpload"
          style="cursor: pointer; color: #00a1d6"
          >已通过&nbsp;{{ UserVideoVo.videoSuccessNumber }}</span
        >
        <span style="color: #e1e1e1">|</span>
        <span v-if="click3 === 0" @click="errorUpload" style="cursor: pointer"
          >未通过&nbsp;{{ UserVideoVo.videoErrorNumber }}</span
        >
        <span
          v-else
          @click="errorUpload"
          style="cursor: pointer; color: #00a1d6"
          >未通过&nbsp;{{ UserVideoVo.videoErrorNumber }}</span
        >
      </div>

      <el-select
        v-model="subZoneKey"
        style="width: 110px; height: 30px; transform: translate(740px, -25px)"
      >
        <el-option
          @click="selectsubZoneKeyVideo"
          v-for="item in classify"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>

      <el-select
        v-model="sortWay"
        style="width: 162px; height: 30px; transform: translate(750px, -25px)"
      >
        <el-option
          @click="sortVideo"
          v-for="item in sort"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <div v-for="(video, index) in videoList" :key="index">
        <div style="margin-bottom: -20px">
          <img
            @click="openVideo(video)"
            :src="video.coverAddress"
            style="
              width: 154px;
              height: 87px;
              object-fit: cover;
              border-radius: 5px;
              cursor: pointer;
              transform: translate(50px, 30px);
            "
          />
          <span class="videoTime">{{ video.videoTime }}</span>
            <span
              class="videoTitle"
              @click="openVideo(video)"
              style="
                cursor: pointer;
                font-size: 16px;
                width: 780px;
                position: absolute;
                transform: translate(75px, 25px);
              "
              >{{ video.title }}</span>
          <span
            style="
              color: #50505b;
              font-size: 13px;
              position: absolute;
              transform: translate(75px, 67px);
            "
            >{{ video.createTime }}</span
          >
          <router-link
            :to="{ path: '/contribute/edit', query: { videoId: video.id } }"
            style="text-decoration: none;"
          >
            <div
              class="edit"
              @mouseenter="video.hover1 = true"
              @mouseleave="video.hover1 = false"
            >
              <img
                :src="video.hover1 ? editsBlue : edits"
                style="
                  transform: translate(1px, 0px);
                  width: 14px;
                  height: 14px;
                "
              />编辑
            </div>
          </router-link>
          <div
            v-if="video.status !== 0 || video.status !== 2"
            @click="viewVideoData"
            class="datas"
            @mouseenter="video.hover2 = true"
            @mouseleave="video.hover2 = false"
          >
            <img
              :src="video.hover2 ? datassBlue : datass"
              style="transform: translate(1px, 0px); width: 14px; height: 14px"
            />数据
          </div>
          <div
            v-if="video.status === 2"
            style="
              color: red;
              transform: translate(500px, -54px);
              position: absolute;
              font-size: 12px;
            "
          >
            退回原因：{{ video.examineFiledMessage }}
          </div>
          <div
            class="more"
            @mouseenter="video.hover3 = true"
            @mouseleave="video.hover3 = false"
          >
            <img
              :src="video.hover3 ? moreBlue : more"
              style="
                width: 3px;
                height: 12px;
                transform: translate(4px);
                position: absolute;
              "
            />
            <div class="deleteUpload" @click="tempDelete(video)">删除</div>
          </div>
          <div
            v-if="video.status !== 0 || video.status !== 2"
            style="
              display: flex;
              justify-content: space-around;
              width: 380px;
              transform: translate(225px, -45px);
            "
          >
            <el-tooltip
              content="播放"
              placement="bottom"
              effect="light"
              :hide-after="0"
              :show-arrow="false"
            >
              <span class="ai">
                <img src="../img/真播放量.png" /><span>{{
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
                <img src="../img/点赞.png" /><span>{{ video.likeNumber }}</span>
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
                <img src="../img/弹幕.png" /><span>{{
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
                <img src="../img/评论.png" /><span>{{
                  video.commentNumber
                }}</span>
              </span>
            </el-tooltip>
            <el-tooltip
              content="硬币"
              placement="bottom"
              effect="light"
              :hide-after="0"
              :show-arrow="false"
            >
              <span class="ai">
                <img src="../img/投币.png" /><span>{{
                  video.coinThrowNumber
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
                <img src="../img/真收藏.png" /><span
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
                <img src="../img/分享.png" /><span>{{
                  video.shareNumber
                }}</span>
              </span>
            </el-tooltip>
          </div>
          <hr
            style="
              width: 970px;
              transform: translate(50px, -30px);
              position: absolute;
              border-top: 1px solid rgb(222, 222, 222);
            "
          />
        </div>
      </div>
      <div
        v-show="
         ( (click1 === 1 && UserVideoVo.videoWaitNumber === 0) ||
          (click2 === 1 && UserVideoVo.videoSuccessNumber === 0) ||
          (click3 === 1 && UserVideoVo.videoErrorNumber === 0) ||
          (click4 === 1 && UserVideoVo.videoAllNumber === 0))&&!videoList.length
        "
        style="transform: translate(440px, 68px)"
      >
        <img src="../img/没有稿件.png" style="width: 234px; height: 177px" />
        <span
          style="
            transform: translate(-243px, 226px);
            font-size: 14px;
            position: absolute;
            height: 20px;
          "
          >一个稿件都没有，请换个筛选条件</span
        >
      </div>

      <div
        style="
          display: flex;
          justify-content: right;
          width: 1024px;
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
      append-to-body
      width="392"
      center
      :modal="false"
      style="transform: translate(0px, 100px); height: 167px"
    >
      <span
        style="
          transform: translate(80px, 0px);
          position: absolute;
          font-size: 18px;
          font-weight: 800;
          color: #8c9195;
        "
      >
        确定要删除该稿件吗？
      </span>
      <template #footer>
        <div class="dialog-footer" style="transform: translate(-10px, 50px)">
          <el-button class="yesDelete" @click="deleteVideo">确定</el-button>
          <el-button
            type="primary"
            @click="dialogVisible = false"
            style="
              background-color: white;
              color: black;
              border: 1px solid #b3b3b3;
              width: 90px;
              height: 28px;
            "
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
import { ElMessage } from "element-plus";
import editsBlue from "../img/编辑蓝.png";
import edits from "../img/编辑.png";
import datassBlue from "../img/数据蓝.png";
import datass from "../img/数据.png";
import more from "../img/删除.png";
import moreBlue from "../img/删除蓝.png";
import {useGlobalStore} from "../store/store";
export default {
  name: "SubPage2",
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
    const subZoneKey = ref("全部分区");
    const sortWay = ref("投稿时间排序");
    const click1 = ref(0);
    const click2 = ref(0);
    const click3 = ref(0);
    const click4 = ref(1);
    function waitUpload() {
      click1.value = 1;
      click2.value = 0;
      click3.value = 0;
      click4.value = 0;
      videoStatus.value = 0;
      selectAllUploadVideo();
    }
    function successUpload() {
      click1.value = 0;
      click2.value = 1;
      click3.value = 0;
      click4.value = 0;
      videoStatus.value = 1;
      selectAllUploadVideo();
    }
    function errorUpload() {
      click1.value = 0;
      click2.value = 0;
      click3.value = 1;
      click4.value = 0;
      videoStatus.value = 2;
      selectAllUploadVideo();
    }
    function allUpload() {
      click1.value = 0;
      click2.value = 0;
      click3.value = 0;
      click4.value = 1;
      videoStatus.value = -1;
      selectAllUploadVideo();
    }

    const classify = [
      { label: "全部分区", value: "全部分区" },
      { label: "生活", value: "生活" },
      { label: "游戏", value: "游戏" },
      { label: "娱乐", value: "娱乐" },
      { label: "知识", value: "知识" },
      { label: "影视", value: "影视" },
      { label: "音乐", value: "音乐" },
      { label: "动画", value: "动画" },
      { label: "时尚", value: "时尚" },
      { label: "美食", value: "美食" },
      { label: "汽车", value: "汽车" },
      { label: "运动", value: "运动" },
      { label: "科技", value: "科技" },
      { label: "动物圈", value: "动物圈" },
      { label: "舞蹈", value: "舞蹈" },
      { label: "国创", value: "国创" },
      { label: "鬼畜", value: "鬼畜" },
      { label: "纪录片", value: "纪录片" },
      { label: "番剧", value: "番剧" },
      { label: "电视剧", value: "电视剧" },
      { label: "电影", value: "电影" },
    ];

    const sort = [
      { label: "投稿时间排序", value: "投稿时间排序" },
      { label: "播放数排序", value: "播放数排序" },
      { label: "收藏数排序", value: "收藏数排序" },
      { label: "弹幕数排序", value: "弹幕数排序" },
      { label: "评论数排序", value: "评论数排序" },
    ];

    const UserVideoVo = reactive({
      videos: [],
      users: {
        avatarAddress: "",
        coinNumber: 0,
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
           console.log(response.data)
        if (response.data.code === 1) {
          videoList.length = 0;
          Object.assign(UserVideoVo, response.data.data);
          Object.assign(videoList, response.data.data.videos);
          total.value = response.data.data.total;
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

    onMounted(() => {
      window.scrollTo({top: 0, behavior: "smooth"});
      selectAllUploadVideo();
    });

    function searchVideo() {
      selectAllUploadVideo();
    }

    function selectsubZoneKeyVideo() {
      selectAllUploadVideo();
    }
    function sortVideo() {
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
    };

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
      editsBlue,
      edits,
      datass,
      datassBlue,
      more,
      moreBlue,
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
<style>
.main-content {
  background-color: #f8f8f8;
  border-radius: 5px;
  transform: translate(-100px, -20px);
  width: 1500px;
  height: 1500px !important;
}
.white {
  transform: translate(120px, 20px);
  width: 1200px;
  overflow-y: hidden;
  overflow-x: hidden;
  z-index: 10;
  height: 1500px;
  background-color: white;
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

.edit {
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 3px;
  border: 1px solid #dadada;
  color: #6c6c6c;
  width: 82px;
  height: 28px;
  font-size: 14px;
  transform: translate(800px, -33px);
  cursor: pointer;
  position: absolute;
}

.edit:hover {
  border: 1px solid #00a1d6;
  color: #00a1d6;
}

.datas {
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 3px;
  border: 1px solid #dadada;
  color: #6c6c6c;
  width: 82px;
  height: 28px;
  font-size: 14px;
  transform: translate(895px, -33px);
  cursor: pointer;
}

.datas:hover {
  border: 1px solid #00a1d6;
  color: #00a1d6;
}
.more {
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 3px;
  border: 1px solid #dadada;
  width: 28px;
  height: 28px;
  transform: translate(990px, -63px);
}

.more:hover {
  border: 1px solid #00a1d6;
}

.ai img {
  width: 13px;
  height: 13px;
  transform: translate(0px, 1px);
}
.ai {
  font-size: 11px;
  color: #aaaaaa;
}
.ai span {
  transform: translate(-2px);
}

.deleteUpload {
  position: absolute;
  opacity: 0;
  visibility: hidden;
  width: 80px;
  height: 30px;
  font-size: 14px;
  color: #6c6c6c;
  border: 1px solid #dadada;
  transform: translate(-10px, 40px);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.3s ease, visibility 0.3s ease;
  cursor: pointer;
}

.deleteUpload:hover {
  border: 1px solid #00a1d6;
  color: #00a1d6;
}

.more:hover .deleteUpload {
  opacity: 1;
  visibility: visible;
}

.yesDelete {
  background-color: #00a1d6;
  color: white;
  border: none;
  width: 90px;
  height: 28px;
}

.yesDelete:hover {
  color: white;
  background-color: #64aad2;
}

.videoTitle{
  color: black;
  transition: color 0.3s ease;
}

.videoTitle:hover{
  color: #00AEEC;
}
</style>
