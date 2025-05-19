<template>
  <div class="videoBox" >
    <div class="video6Box" v-if="Videos.length >= 6">
      <div
        class="video6"
        v-for="(video, index) in Videos.slice(0, 6)"
        :key="index"
      >
       <div
        class="videoBox1"
        style="width: 92.6%;" 
        @mouseover.stop="videoMouseover(Videos[index].videoId)" 
        @mouseleave="videoMouseleave(Videos[index].videoId)">
        <img class="coverAddress" @click="locationHerfVideo(Videos[index].videoId)" :src="Videos[index].coverAddress"/>
        <video
          :id="Videos[index].videoId"
          preload="none"
          disablePictureInPicture
          muted
          loop
          @click="locationHerfVideo(Videos[index].videoId)"
          :src="Videos[index].videoAddress"
        ></video>
        <div
          v-show="store.userId !== null"
          class="waitWatch"
          @click="waitWatch(Videos[index].videoId)"
          @mouseover="waitFont = 1"
          @mouseleave="waitFont = 0"
        >
          <img
            v-show="Videos[index].waitWatch === 0"
            src="../img/稍后再看.png"
            style="
              width: 21px;
              height: 18px;
              opacity: 1 !important;
              margin-left: 4px;
            "
          />
          <img
            v-show="Videos[index].waitWatch === 1"
            src="../img/添加成功.png"
            style="
              width: 18px;
              height: 15px;
              opacity: 1 !important;
              margin-left: 5px;
            "
          />
          <span
            v-show="waitFont === 1 && Videos[index].waitWatch === 0"
            style="
              width: 110px;
              color: white;
              z-index: 10;
              font-size: 12px;
              transform: translate(28.5px, -0.5px);
              position: absolute;
            "
            >添加至稍后观看</span
          >
          <span
            v-show="waitFont === 1 && Videos[index].waitWatch === 1"
            style="
              width: 110px;
              color: white;
              z-index: 10;
              font-size: 12px;
              transform: translate(28.5px, -0.5px);
              position: absolute;
            "
            >已添加稍后观看</span
          >
        </div>
        <div class="videoContent1">
          <img
            src="../img/播放量白.png"
            style="
              width: 15px;
              height: 12px;
              transform: translate(9.5px, 6px);
              border-radius: 1px;
            "
          />
          <span
            style="
              font-size: 12.6px;
              transform: translate(13px, 8.5px);
              position: absolute;
            "
            >{{ Videos[index].videoPlayNumber }}</span
          >
          <img
            src="../img/弹幕白.png"
            style="
              width: 15px;
              height: 12px;
              transform: translate(57.5px, 6px);
              border-radius: 1px;
            "
          />
          <span
            style="
              font-size: 12.6px;
              transform: translate(61px, 8.5px);
              position: absolute;
            "
            >{{ Videos[index].videoScrollingNumber }}</span
          >
          <span
            v-if="Videos[index].hour !== null"
            style="
              font-size: 12.6px;
              transform: translate(158px, 8.5px);
              position: absolute;
            "
          >
            <span>{{ Videos[index].hour }}<span class="colon">:</span></span
            >{{ Videos[index].minutes }}<span class="colon">:</span
            >{{ Videos[index].second }}</span
          >

          <span
            v-if="Videos[index].hour === null"
            style="
              font-size: 12.6px;
              transform: translate(174px, 8.5px);
              position: absolute;
            "
          >
            {{ Videos[index].minutes }}<span class="colon">:</span
            >{{ Videos[index].second }}</span
          >
        </div>
        <div class="videoContent"></div>
       </div>
        <div
          style="
            width: 245px;
            height: 15px;
            transform: translate(0px, -35px);
            position: absolute;
            opacity: 0;
          "
        ></div>
        <el-tooltip
          popper-class="custom-tooltip"
          class="box-item"
          :show-after="300"
          effect="light"
          :content="Videos[index].videoTitle"
          placement="left"
          :show-arrow="false"
        >
          <span
            class="title"
            @click="locationHerfVideo(Videos[index].videoId)"
            >{{ Videos[index].videoTitle }}</span
          >
        </el-tooltip>
        <div
          style="
            height: 34px;
            position: absolute;
            width: 245px;
            transform: translate(0px, -8px);
            opacity: 0;
            z-index: -10;
          "
        ></div>
        <el-tooltip
          popper-class="custom-tooltip"
          class="box-item"
          effect="light"
          :show-after="300"
          :content="Videos[index].userName"
          placement="bottom-end"
          :show-arrow="false"
        >
          <a :href="'./home?homeMenu=1&userId='+Videos[index].userId" target="_blank">
            <span
            class="videoBottomInfo"
            @mouseover="upImgFlag = index - 200000"
            @mouseleave="upImgFlag = -index - 3000000"
            ><img
              :src="upImgFlag === index - 200000 ? upBlue : up"
              style="
                width: 14px;
                height: 12px;
                position: absolute;
                transform: translate(1.2px, 26.2px);
                border-radius: 0px;
              "
            />
            <span class="upInfo"
              >{{ Videos[index].userName }} &nbsp;·&nbsp;&nbsp;{{
                Videos[index].createTime
              }}</span
            >
          </span>
          </a>
        </el-tooltip>
      </div>
    </div>
    <div class="changer" @click="changeVideo">
      <img src="../img/换一换.png" /><span>换 一 换</span>
    </div>
    <div class="bottomVideo">
      <div
        class="video-video"
        v-for="(video, index) in Videos.slice(6,Videos.length)"
        :key="video.id"
      >
        <div class="videoBox1" style="width: 92.6%;" @mousemove="videoMouseover(video.videoId)" @mouseleave="videoMouseleave(video.videoId)">
          <img class="coverAddress"  @click="locationHerfVideo(video.videoId)" :src="video.coverAddress"/>
          <video
          :id="video.videoId"
          preload="none"
          disablePictureInPicture
          muted
          loop
          @click="locationHerfVideo(video.videoId)"
          :src="video.videoAddress"
        ></video>
        <div
          v-show="store.userId !== null"
          class="waitWatch"
          @click="waitWatch(video.videoId)"
          @mouseover="waitFont = 1"
          @mouseleave="waitFont = 0"
        >
          <img
            v-show="video.waitWatch === 0"
            src="../img/稍后再看.png"
            style="
              width: 21px;
              height: 18px;
              opacity: 1 !important;
              margin-left: 4px;
            "
          />
          <img
            v-show="video.waitWatch === 1"
            src="../img/添加成功.png"
            style="
              width: 18px;
              height: 15px;
              opacity: 1 !important;
              margin-left: 5px;
            "
          />
          <span
            v-show="waitFont === 1 && video.waitWatch === 0"
            style="
              width: 110px;
              color: white;
              z-index: 10;
              font-size: 12px;
              transform: translate(28.5px, -0.5px);
              position: absolute;
            "
            >添加至稍后观看</span
          >
          <span
            v-show="waitFont === 1 && video.waitWatch === 1"
            style="
              width: 110px;
              color: white;
              z-index: 10;
              font-size: 12px;
              transform: translate(28.5px, -0.5px);
              position: absolute;
            "
            >已添加稍后观看</span
          >
        </div>
        <div class="videoContent1">
          <img
            src="../img/播放量白.png"
            style="
              width: 15px;
              height: 12px;
              transform: translate(9.5px, 6px);
              border-radius: 1px;
            "
          />
          <span
            style="
              font-size: 12.6px;
              transform: translate(13px, 8.5px);
              position: absolute;
            "
            >{{ video.videoPlayNumber }}</span
          >
          <img
            src="../img/弹幕白.png"
            style="
              width: 15px;
              height: 12px;
              transform: translate(57.5px, 6px);
              border-radius: 1px;
            "
          />
          <span
            style="
              font-size: 12.6px;
              transform: translate(61px, 8.5px);
              position: absolute;
            "
            >{{ video.videoScrollingNumber }}</span
          >
          <span
            v-if="video.hour !== null"
            style="
              font-size: 12.6px;
              transform: translate(158px, 8.5px);
              position: absolute;
            "
          >
            <span>{{ video.hour }}<span class="colon">:</span></span
            >{{ video.minutes }}<span class="colon">:</span
            >{{ video.second }}</span
          >

          <span
            v-if="video.hour === null"
            style="
              font-size: 12.6px;
              transform: translate(174px, 8.5px);
              position: absolute;
            "
          >
            {{ video.minutes }}<span class="colon">:</span
            >{{ video.second }}</span
          >
        </div>
        <div
          style="
            width: 245px;
            height: 15px;
            transform: translate(0px, -35px);
            position: absolute;
            opacity: 0;
          "
        ></div>
        <div class="videoContent"></div>
        </div>
        <el-tooltip
          popper-class="custom-tooltip"
          class="box-item"
          effect="light"
          :show-after="300"
          :content="video.videoTitle"
          placement="left"
          :show-arrow="false"
        >
          <span class="title" @click="locationHerfVideo(video.videoId)">{{
            video.videoTitle
          }}</span>
        </el-tooltip>
        <div
          style="
            height: 32px;
            position: absolute;
            width: 245px;
            transform: translate(0px, -8px);
            opacity: 0;
            z-index: -10;
          "
        ></div>
        <el-tooltip
          popper-class="custom-tooltip"
          class="box-item"
          effect="light"
          :show-after="300"
          :content="video.userName"
          placement="bottom-end"
          :show-arrow="false"
        >
          <a :href="'./home?homeMenu=1&userId='+video.userId" target="_blank">
            <span
            class="videoBottomInfo"
            @mouseover="upImgFlag = index - 10"
            @mouseleave="upImgFlag = -index - 10"
            ><img
              :src="upImgFlag === index - 10 ? upBlue : up"
              style="
                width: 14px;
                height: 12px;
                position: absolute;
                transform: translate(1px, 26px);
                border-radius: 0px;
              "
            />
            <span class="upInfo"
              >{{ video.userName }} &nbsp;·&nbsp;&nbsp;{{
                video.createTime
              }}</span
            >
          </span>
          </a>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted, watch } from "vue";
import apiClient from "../services/apiClient";
import up from "../img/up.png";
import upBlue from "../img/up蓝.png";
import {useGlobalStore} from "../store/store";
import { ElMessage } from "element-plus";
import { selectVideo } from '@/api/video/index';
export default {
  name: "videos",
  setup() {
    const store = useGlobalStore();
    const waitFont = ref(0);
    const upImgFlag = ref(false);
    const Videos = reactive([]);
    const imgTrasitionFlag=ref(false);
    const pageNum=ref(1);
    let sort=Math.floor(Math.random() * (6)) + 1;//1升序视频标题 2降序视频标题 3升序播放量 4降序播放量 5升序发布时间 6降序发布时间
    let videoAutoPlayTIme={};
    onMounted(async() => {
      await getUserIp();
      await ChecklLogin();
      selectVideos();
      window.addEventListener("scroll", handleScroll1);
    });

    onUnmounted(() => {
      window.removeEventListener("scroll", handleScroll1);
    });

    //获取用户ip和token
    async function getUserIp(){
      
      const response = await apiClient.get("/userIp/getUserIp");

      if(response.data.code === 1)
        store.setUserIp(response.data.data.userIp);
        store.setToken(response.data.data.token);

    }

    const handleScroll1 = () => {

      if(nodata)
        return;

      const scrollHeight = document.documentElement.scrollHeight;  // 文档总高度
      const scrollTop = window.scrollY || document.documentElement.scrollTop;  // 当前滚动高度
      const clientHeight = window.innerHeight || document.documentElement.clientHeight;  // 可视区域高度

      if (scrollTop + clientHeight >= scrollHeight) {
        selectVideos();
      }
    };
    
    async function ChecklLogin() {
      try {

        const response = await apiClient.get(`/user/checkLoginFlag/${store.userIp}`);
        if (response.data.code === 1) {
          store.setUserId(response.data.data.id);
        }
       

      } catch (error) {
      }
    }

    let selectFlag=true;
    let nodata=false;
    async function selectVideos() {
        if(!selectFlag){
          return;
        }
        else{
          selectFlag=false;
        }
        let userId=0; 
        if(store.userId === null)
        userId=0;
        else
        userId=store.userId;
        selectVideo(userId,pageNum.value++,sort).then(response=>{
            if (response.data.code === 1) {
            Videos.push(...response.data.data);
            if(!response.data.data||response.data.data.length===0){
              nodata=true;
            }
            selectFlag=true;
            } else {
            ElMessage({
              message: response.data.msg,
              type: "info",
              plain: true,
              duration: 1700,
            });
        }
        })
    }

    async function waitWatch(videoId) {
      try {

        let dynamicDto={
            videoId: videoId,
            userId: store.userId,
        }
        const response = await apiClient.put(
          "/dynamic/updateWaitWatch",dynamicDto,
            {
            headers: {
            "Content-Type": "application/json",
            "Authorization": store.token,
          },        
          }
        );
        if (response.data.code === 1) {
          const updatedVideos = Videos.map((item) => {
            if (item.videoId === videoId && item.waitWatch === 0) {
              return { ...item, waitWatch: 1 }; // 创建一个新的对象并修改 waitWatch
            } else if (item.videoId === videoId && item.waitWatch === 1) {
              return { ...item, waitWatch: 0 };
            }
            return item; // 保留原始对象
          });
          Videos.length = 0; // 清空原数组
          Object.assign(Videos, updatedVideos); // 重新赋值
        }
        else{
          ElMessage({
            message: response.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
          });
        }
      } catch (error) {

      }
    }

    function locationHerfVideo(videoId) {
      window.open(
        `./video?videoId=BV${videoId}`,
        "videoWindow",
      );
    }

    function videoMouseover(id) {
    // 清除之前的定时器，防止重复触发
    if (videoAutoPlayTIme[id]) {
        clearTimeout(videoAutoPlayTIme[id]);
    }

    // 延迟播放视频
    videoAutoPlayTIme[id] = setTimeout(() => {
        const video = document.getElementById(id);
        if(video)
        if (video.paused) {  // 只有在视频处于暂停状态时才播放
            video.play().catch(function(error) {
            });
        }
    }, 700);  // 1秒后播放视频
    }

    function videoMouseleave(id) {
        // 清除之前的视频播放定时器
        clearTimeout(videoAutoPlayTIme[id]);

        const video = document.getElementById(id);
        if(video)
        if (!video.paused) {  // 只有在视频播放时才暂停
            video.pause();
        }
    }

    let loaddimgFlag=true;
    watch([()=>store.loginLoadFlag,()=>store.userId],()=>{
      
        if(store.loginLoadFlag&&loaddimgFlag&&store.userId!==null)
        {
          loaddimgFlag=false;
          selectVideos();
        }
      

    })

    function changeVideo(){
      sort=Math.floor(Math.random() * (6)) + 1;
      pageNum.value=1;
      Videos.length=0;
      nodata=false;
      selectVideos();
    }

    return {
      waitFont,
      upImgFlag,
      up,
      upBlue,
      Videos,
      selectVideos,
      waitWatch,
      locationHerfVideo,
      videoMouseover,
      videoMouseleave,
      store,
      imgTrasitionFlag,
      changeVideo,
    };
  },
};
</script>

<style lang="scss">

video{
  cursor: pointer;
  visibility: hidden;
  width: 92.6%; /* 视频宽度为100% */
  height: 100%; /* 视频高度为100% */
  object-fit: cover; /* 填充并保持比例 */
  position: absolute; /* 绝对定位 */
  top: 0; /* 上部对齐 */
  left: 0; /* 左部对齐 */
  z-index: 2000;
  border-radius: 5px;
}


* {
  padding: 0; /* 移除内边距 */
  margin: 0; /* 移除外边距 */
  box-sizing: border-box; /* 包括内边距和边框在元素的总宽度和高度中 */
}

.videoBox{
  height: auto;
}

.video6 {
  flex: 0 0 30%; /* 每个盒子占据 30% 的宽度 */
  width: 245px;
  height: 138px;
  margin-top: 95px;
  transform: translate(0px, -95px);
}
.video6Box {
  width: 883px;
  display: flex;
  flex-wrap: wrap; /* 允许换行 */
}

.imgTrasitionFlag {
  opacity: 0;
}


.video6:hover .waitWatch {
  visibility: visible;
}
.video6 img {
  width: 245px;
  height: 138px;
  object-fit: cover;
  border-radius: 6px;
  transition: all 0.3s ease;
}
.bottomVideo {
  width: 1324px;
  height: auto;
  display: flex;
  flex-wrap: wrap; /* 允许换行 */
}
.video-video {
  flex: 0 0 20%; /* 每个盒子占据 30% 的宽度 */
  width: 245px;
  height: 138px;
  margin-top: 295px;
  margin-bottom: -160px; /* 设置每行之间的间距 */
  transform: translate(75px, -95px);
}

.video-video:hover .waitWatch {
  visibility: visible;
}
.video-video img {
  width: 245px;
  height: 138px;
  object-fit: cover;
  border-radius: 6px;
}

.video-video:hover .waitWatch {
  visibility: visible;
}

.videoContent {
  position: absolute; /* 确保位置是绝对的 */
  width: 245px;
  height: 30px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.9), rgba(0, 0, 0, 0.2));
  top: 78%; /* 改为100%以使其在图片下方显示 */
  border-radius: 0px 0px 6px 6px;
  opacity: 1;
  transition: all 0.3s ease;
  color: white;
  visibility: visible;
  z-index: 2990;

}
.videoContent1 {
  width: 245px;
  height: 30px;
  position: relative;
  transform: translate(0px, -35px);
  color: white;
  z-index: 3000;
}



.videoBox1:hover{

  video{
    opacity: 1;
    transition: all 0.3s ease;
    visibility: visible;
    transition-delay: 0.6s;
  }

  .videoContent1{
    opacity: 0;
    transition: all 0.3s ease;
    visibility: hidden;
  }
  .videoContent{
    opacity: 0;
    transition: all 0.3s ease;
    visibility: hidden;
  }

  .waitWatch{
    transition-delay: 0.3s;
    opacity: 1;
    transition: all 0.3s ease;
    visibility: visible;
  }
}

.video6Box {
  position: absolute;
  transform: translate(605px, -305px);
}
.colon {
  display: inline-block;
  position: relative;
  transform: translateY(-1px);
}

.waitWatch {
  display: flex;
  justify-content: left;
  align-items: center;
  border-radius: 5px;
  width: 28px;
  height: 28px;
  background-color: rgba(33, 33, 33, .8);
  top: 8px;
  right: 28px;
  opacity: 0;
  visibility: hidden;
  transition: transform 0.3s ease, width 0.3s ease; /* 添加宽度变化的动画效果 */
  cursor: pointer;
  position: absolute;
  z-index: 3000;
  overflow: hidden;
}

.waitWatch:hover {
  transition-delay: 0.3s;
  width: 120px; /* 扩大宽度 */
  visibility: visible; /* 悬停时可见 */
}

.upInfo {
  width: 224.5px;
  color: #95999f;
  font-size: 12.5px;
  transform: translate(19.5px, 23px);
  position: absolute;
  display: -webkit-box; /* 使用 flexbox 布局 */
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  -webkit-line-clamp: 1; /* 限制为 1行 */
  overflow: hidden; /* 隐藏超出部分 */
  transition: all 0.2s ease;
  text-overflow: ellipsis; /* 显示省略号 */
}

.videoBottomInfo:hover .upInfo {
  color: #00aeec;
}
.videoBottomInfo {
  cursor: pointer;
  position: absolute;
  transform: translate(0px, 0.5px);
}

.changer {
  width: 40px;
  height: 83.5px;
  border-radius: 9px;
  position: absolute;
  transform: translate(1390px, -306px);
  border: 1px solid #e3e5e7;
  cursor: pointer;
  transition: all 0.3s ease;
}
.changer img {
  width: 13px;
  height: 13px;
  transform: translate(12px, 10px);
  position: absolute;
}
.changer:hover {
  background-color: #e3e5e7;
}

.changer span {
  font-size: 11px;
  writing-mode: vertical-rl; /* 垂直从右到左 */
  /* 或者使用: writing-mode: vertical-lr; 从左到右 */
  transform: translate(12px, 32px);
}

.title {
  width: 245px; /* 设置宽度 */
  word-wrap: break-word; /* 超出部分换行 */
  display: -webkit-box; /* 使用 flexbox 布局 */
  -webkit-box-orient: vertical; /* 垂直方向排列 */
  -webkit-line-clamp: 2; /* 限制为 2 行 */
  overflow: hidden; /* 隐藏超出部分 */
  text-overflow: ellipsis; /* 显示省略号 */
  line-height: 1.5; /* 设置行高 */
  color: black;
  font-weight: 500;
  font-size: 15px;
  transform: translate(0px, -25px);
  position: absolute; /* 或者 absolute，视布局而定 */
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: normal; /* 正常 */
  padding-bottom: 3px;
}

.title:hover {
  color: #00aeec;
}

.coverAddress{
  cursor: pointer;
}


</style>
