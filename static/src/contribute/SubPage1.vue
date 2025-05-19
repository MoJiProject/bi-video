<template>
  <div class="main-content2">
    <div class="white2">
      <span class="title"> 视频数据 </span>
      <div
        style="
          color: #8c9195;
          transform: translate(940px, 130px);
          font-size: 14px;
        "
      >
        实时更新
      </div>
      <div
        style="
          display: flex;
          flex-direction: column;
          transform: translate(35px, 150px);
        "
      >
        <div
          class="a"
          style="display: flex; justify-content: space-around; width: 1000px"
        >
          <span
            ><div>
              <img
                src="../img/新增粉丝.png"
                style="transform: translate(3px, 3px)"
              />净增粉丝
            </div>
            <div class="aa">{{ videoDto.newFansNumber }}</div>
          </span>
          <span
            ><div>
              <img
                src="../img/播放量.png"
                style="width: 14px; transform: translate(6px, 2px)"
              />
              播放量
            </div>
            <div class="aa">{{ videoDto.playNumber }}</div>
          </span>
          <span
            ><div>
              <img
                src="../img/评论.png"
                style="transform: translate(6px, 3px)"
              />
              评论
            </div>
            <div class="aa">{{ videoDto.commentNumber }}</div>
          </span>
          <span
            ><div>
              <img
                src="../img/弹幕.png"
                style="transform: translate(6px, 2px)"
              />
              弹幕
            </div>
            <div class="aa">{{ videoDto.scrollingNumber }}</div>
          </span>
        </div>

        <div
          class="a"
          style="
            display: flex;
            justify-content: space-around;
            width: 1000px;
            transform: translateY(20px);
          "
        >
          <span style="transform: translate(-4px)"
            ><div>
              <img
                src="../img/点赞.png"
                style="transform: translate(7px, 2px)"
              />
              点赞
            </div>
            <div class="aa">{{ videoDto.likeNumber }}</div>
          </span>
          <span style="transform: translate(-3px)"
            ><div>
              <img
                src="../img/分享.png"
                style="transform: translate(7px, 1px)"
              />
              分享
            </div>
            <div class="aa">{{ videoDto.shareNumber }}</div>
          </span>
          <span style="transform: translate(0px)"
            ><div>
              <img
                src="../img/真收藏.png"
                style="transform: translate(6px, 3px)"
              />
              收藏
            </div>
            <div class="aa">{{ videoDto.collectNumber }}</div>
          </span>
          <span style="transform: translate(-1px)"
            ><div>
              <img
                src="../img/投币.png"
                style="transform: translate(5px, 4px)"
              />
              投币
            </div>
            <div class="aa">{{ videoDto.coinThrowNumber }}</div>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, onMounted } from "vue";
import apiClient from "../services/apiClient";
import { ElMessage } from "element-plus";
import {useGlobalStore} from "../store/store";
export default {
  name: "SubPage1",

  setup() {
    const store = useGlobalStore();
    const videoDto = reactive({
      userId: 0,
      likeNumber: 0,
      commentNumber: 0,
      collectNumber: 0,
      shareNumber: 0,
      coinThrowNumber: 0,
      playNumber: 0,
      newFansNumber: 0,
      scrollingNumber: 0,
    });


    onMounted(() => {
     getVideoData();
    });

    async function getVideoData() {
      try {
        const response = await apiClient.post("/video/selectVideoData", {
          userId: store.userId, // 确保包装成对象
        },
        {
            headers: {
            "Content-Type": "application/json",
            "Authorization": store.token,
          },        
        }
        );

        if (response.data.code === 1) {
          Object.assign(videoDto, response.data.data);
        } else {
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

    return {
      videoDto,
    };
  },
};
</script>
<style>
.main-content2 {
  background-color: #f8f8f8;
  border-radius: 5px;
  transform: translate(-100px, -20px);
  width: 1500px;
}
.white2 {
  transform: translate(120px, 20px);
  width: 1200px;
  height: 1000px !important;
  z-index: 10;
  background-color: white;
}

.title {
  position: absolute;
  font-weight: 800;
  color: #00a1d6;
  margin-left: 50px;
  margin-top: 100px;
}

.a span {
  width: 221px;
  height: 88px;
  background-color: #f8fcfe;
  border-radius: 20px;
}

.a span div {
  color: #8c9195;
  transform: translate(16px, 14px);
  height: 20px;
}

.aa {
  color: #00a1d6 !important;
  font-size: 22px;
  font-weight: 800;
  transform: translate(20px, 22px) !important;
}

</style>
