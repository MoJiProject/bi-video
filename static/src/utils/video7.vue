<template>
    <div class="video" :style="{ 'width': size.width+'px', 'height': size.height+'px' }">
      <a class="video-link" :href="'./video?videoId=BV'+video.videoId" target="videoWindow" @mouseover="videoMouseover(video.videoId)" @mouseleave="videoMouseleave(video.videoId)">
       <img class="cover-img" :src="video.coverAddress">
       <video class="video-content" :src="video.videoAddress"
         :id="'contribute2'+video.videoId"
         preload="none"
         disablePictureInPicture
         muted
         loop
       ></video>
       <waitWatch2 @click.prevent class="wait-watch-c" :video="video"/>
       <div class="video-overlay"></div>
       <div v-if="video.createTime.includes('分钟前')||video.createTime.includes('小时前')" class="new-video">最新</div>
       <div class="video-time">
       {{ video.hour?video.hour:''}}<span class="split">{{ video.hour?':':''}}</span>{{ video.minutes }}<span class="split">:</span>{{ video.second }}
       </div>
       <div class="video-progress" :style="{ '--progress-width': video.watchCurrentTime/(video.hour?parseInt(video.hour)*60*60:0+parseInt(video.minutes)*60+parseInt(video.second))*100+'%' }"></div>
      </a>
      <div class="video-info">
           <div class="video-link2">
               <a :href="'./video?videoId=BV'+video.videoId" target="videoWindow">
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
                       <div v-if="store.keyword" class="video-title" v-html="highlightText(video.videoTitle)"></div>
                       <div v-else class="video-title">{{ video.videoTitle }}</div>
                   </el-tooltip>
               </a>
               <div class="introduce-container">
                   <div v-if="store.keyword" class="introduce" v-html="highlightText(video.content)"></div>
                   <div v-else class="introduce" v-html="video.content"></div>
               </div>
           </div>
            <div class="info">
                <div><img src="../img/播放量灰.png"><span>{{ video.videoPlayNumber }}</span></div>
                <div><img src="../img/弹幕灰.png"><span>{{ video.videoScrollingNumber }}</span></div>
                <div>{{ video.createTime }}</div>
            </div>
      </div>
   </div> 
 
</template>

<script setup>
import waitWatch2 from '@/utils/waitWatch2.vue';
import { useGlobalStore } from "@/store/store";

const store = useGlobalStore();
defineProps({
 video: {
   type: Object,
   required: true,
 },
 size:{
   type: Object,
   required: true,
 }
});

let videoAutoPlayTIme = null;
function videoMouseover(id) {
// 清除之前的定时器，防止重复触发
if (videoAutoPlayTIme) {
   clearTimeout(videoAutoPlayTIme);
}

// 延迟播放视频
videoAutoPlayTIme= setTimeout(() => {
   const video = document.getElementById('contribute2'+id);
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

   const video = document.getElementById('contribute2'+id);
   if(video)
   if (!video.paused) {  // 只有在视频播放时才暂停
       video.pause();
   }
}

function highlightText(title) {
const regex = new RegExp(`(${store.keyword})`, "g");
return title.replace(regex, '<span class="highlight2">$1</span>');
}
</script>

<style lang="scss" scoped>

*{
   margin: 0;
   padding: 0;
   box-sizing: border-box;
}

.video{
   position: relative;
   display: flex;
   top: 0px;
   
   .video-link{
       position: relative;
       display: block;
       flex-shrink: 0;
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
       background-color: black;
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
       .video-overlay{
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
       }
       .video-time{
           position: absolute;
           bottom: 2px;
           right: -3px;
           margin-right: 12px;
           color: white;
           font-size: 11.5px;
           height: 18px;
           z-index: 20;
           .split{
               position: relative;
               top: -1.5px;
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
   .video-info{
       width: 863px;
       margin-left: 20px;
       flex: 1;
       display: flex;
       justify-content: space-between;
       flex-direction: column;

       .video-link2{
           display: block;
           
           a{
               .video-title{
               width: 863px;    
               cursor: pointer;
               display: block;
               color: #18191C;
               line-height: 22px;
               font-size: 14.5px;
               display: -webkit-box;
               overflow: hidden;
               word-break: break-all;
               text-overflow: ellipsis;
               -webkit-line-clamp: 1;
               -webkit-box-orient: vertical;
               transition: color .3s ease;
           }
           .video-title:hover{
           color: #00AEEC;
           }
           }
           .introduce-container{
           .introduce{
           width: 863px;
           margin-top: 8px;
           font-size: 14px;
           line-height: 22px;
           color: #61666D;
           display: -webkit-box;
           -webkit-box-orient: vertical;
           overflow: hidden;
           -webkit-line-clamp: 2;
           text-overflow: ellipsis;
       }
           }
       }
       
       .info{
       display: flex;

       div{
           margin-right: 20px;
           display: flex;
           align-items: center;
           font-size: 13px;
           color: #9499A0;

           img{
               width: 15px;
               height: 12.5px;
               margin-right: 4px;
           }
       }
       }

   }
}
  

.video-link:hover{
   .cover-img{
       transition-delay: 0.3s;
       opacity: 0;
   }
   .video-overlay{
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
   .video-time{
       transition-delay: 0.3s;
       opacity: 0;
   }
}

</style>