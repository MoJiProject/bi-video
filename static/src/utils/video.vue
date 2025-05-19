<template>
   <div class="video">
       <a class="video-link" :href="'./video?videoId=BV'+video.videoId" target="videoWindow" @mouseover="videoMouseover(video.videoId)" @mouseleave="videoMouseleave(video.videoId)">
        <img class="cover-img" :src="video.coverAddress">
        <video class="video-content" :src="video.videoAddress"
          :id="'contribute'+video.videoId"
          preload="none"
          disablePictureInPicture
          muted
          loop
        ></video>
        <waitWatch @click.prevent class="wait-watch-c" :video="video"/>
        <div v-if="video.createTime.includes('分钟前')||video.createTime.includes('小时前')" class="new-video">最新</div>
        <div class="video-info">
            <span class="video-info-item">
                <img src="../img/播放量白.png">
                <span>{{ video.videoPlayNumber }}</span>
            </span>
            <span style="left: -22px;" class="video-info-item">
                <img src="../img/弹幕白.png">
                <span>{{ video.videoScrollingNumber }}</span>
            </span>
            <span style="left: 12px;" class="video-info-item">
                {{ video.hour?video.hour:'' }} 
                <span class="split">{{ video.hour?':':'' }}</span> {{ video.minutes }} 
                <span class="split">:</span> {{ video.second }}
            </span>
        </div>
        <div class="video-progress" :style="{ '--progress-width': video.watchCurrentTime/(video.hour?parseInt(video.hour)*60*60:0+parseInt(video.minutes)*60+parseInt(video.second))*100+'%' }"></div>
       </a>
       <a class="video-link2" :href="'./video?videoId=BV'+video.videoId" target="videoWindow">
        <el-tooltip
          popper-class="custom-tooltip"
          class="box-item"
          effect="light"
          :show-after="300"
          :offset="-15"
          :content="video.videoTitle"
          placement="bottom"
          :show-arrow="false"
        >
            <div class="video-title">{{ video.videoTitle }}</div>
        </el-tooltip>
        <div class="video-date">{{ video.createTime }}</div>
       </a>
   </div> 
</template>

<script setup>
import waitWatch from '@/utils/waitWatch.vue';

defineProps({
  video: {
    type: Object,
    required: true,
  },
});

let videoAutoPlayTIme = null;
function videoMouseover(id) {
// 清除之前的定时器，防止重复触发
if (videoAutoPlayTIme) {
    clearTimeout(videoAutoPlayTIme);
}

// 延迟播放视频
videoAutoPlayTIme= setTimeout(() => {
    const video = document.getElementById('contribute'+id);
    if(video)
    if (video.paused) {  // 只有在视频处于暂停状态时才播放
        video.play().catch(function(error) {
        });
    }
}, 700);  // 1秒后播放视频
}

function videoMouseleave(id) {
    // 清除之前的视频播放定时器
    clearTimeout(videoAutoPlayTIme);

    const video = document.getElementById('contribute'+id);
    if(video)
    if (!video.paused) {  // 只有在视频播放时才暂停
        video.pause();
    }
}


</script>

<style lang="scss" scoped>
*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

video{
    object-fit: cover;
    background-color: black;
}

.video{
    position: relative;
    top: 0px;
    width: 193px;
    height: 110px;
    cursor: pointer;
    
    .video-link{
        position: relative;
        display: block;
        width: 100%;
        height: 100%;
        border-radius: 6px;
        overflow: hidden;
        .cover-img{
        position: relative;
        width: 100%;
        height: 100%;
        z-index: 10;
        object-fit: cover;
        background-color: black;
        transition: opacity 0.3s ease;
        }
        .video-content{
        position: absolute;
        width: 100%;
        height: 100%;
        left: 0px;
        object-fit: cover;
        }
        .wait-watch-c{ 
            display: flex;
            justify-content: left;
            align-items: center;
            position: absolute;
            top: 8px;
            right: 8px;
            border-radius: 5px;
            width: 28px;
            height: 28px;
            opacity: 0;
            visibility: hidden;
            background-color: rgba(33, 33, 33, .8);
            transition: all 0.3s ease; /* 添加宽度变化的动画效果 */
            cursor: pointer;
            z-index: 10;
            overflow: hidden;
        }
        .wait-watch-c:hover {
            transition-delay: 0.3s;
            width: 120px; /* 扩大宽度 */
            visibility: visible; /* 悬停时可见 */
        }
        .new-video{
        position: absolute;
        top: 8px;
        right: 8px;   
        font-size: 12.5px;
        line-height: 18px;
        user-select: none;
        color: white;
        border-radius: 4px;
        padding: 1px 4px;
        transition: all 0.3s ease;
        z-index: 10;
        background-color: #FF6699;
        }
        .video-info{
        position: absolute;
        bottom: 0px;
        left: 0;
        z-index: 10;
        box-sizing: border-box;
        padding: 14px 8px 6px;
        width: 100%;
        border-bottom-right-radius: 6px;
        border-bottom-left-radius: 6px;
        background-image: linear-gradient(180deg, #0000, #000c);
        color: #fff;
        opacity: 1;
        display: flex;
        align-items: center;
        justify-content: space-between;
        pointer-events: none;
        transition: all .2s linear .2s;
            .video-info-item{
            position: relative;
            display: flex;
            top: 3.5px;
            left: 1px;
            align-items: center;
            justify-content: center;
            margin-right: 12px;
            font-size: 12px;
            height: 18px;
            flex-shrink: 0;
            img{
                width: 13px;
                height: 12px;
                margin-right: 4px;
            }
            .split{
                margin-top: -2px;
                margin-left: 1px;
                margin-right: 1px;
            }
            }
        }
        .video-progress{
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        z-index: 50;
        height: 3px;
        pointer-events: none;
        background-color: rgba(0, 0, 0, .5);
        transition: opacity .2s linear .2s;
        }
        .video-progress::after{
        content: "";
        position: absolute;
        width: var(--progress-width, 0%);
        top: 0;
        left: 0;
        height: 100%;
        background-color: #FF6699;
        }
    }
    .video-link2{
      position: relative;  
      display: block;  
        .video-title{
            position: relative;
            top: 6px;
            padding-right: 16px;
            color: #18191C;
            font-size: 14.5px;
            line-height: 22px;
            height: 44px;
            display: -webkit-box;
            overflow: hidden;
            -webkit-box-orient: vertical;
            text-overflow: ellipsis;
            word-break: break-all;
            -webkit-line-clamp: 2;
            transition: color 0.3s ease;
        }
        .video-title:hover{
        color: #00AEEC;
        }
        .video-date{
            margin-top: 6px;
            display: flex;
            align-items: center;
            color: #9499A0;
            font-size: 11.5px;
            line-height: 18px;
            height: 18px;
        }
    }
}

.video-link:hover{
    .cover-img{
        transition-delay: 0.3s;
        opacity: 0;
        object-fit: cover;
    }
    .video-info{
        transition-delay: 0.3s;
        opacity: 0;
    }
    .wait-watch-c{
        opacity: 1;
        visibility: visible;
    }
    .new-video{
        opacity: 0;
        visibility: hidden;
    }
}

</style>