<template>
   <div v-if="collect.deleteFlag===0" class="video">
       <div class="video-link" @click="openVideo(video)" @mouseover="videoMouseover(video.videoId)" @mouseleave="videoMouseleave(video.videoId)">
        <img class="cover-img" :src="video.coverAddress">
        <div v-show="batchControls" class="check-out-box" :class="{'active-check-out': checkExistVideo(collect.id)}">
            <div class="border">
                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path fill-rule="evenodd" clip-rule="evenodd" d="M14.3378 3.00678C14.0492 2.74694 13.6046 2.77024 13.3447 3.05882L5.46602 11.809L2.66657 8.69993C2.40673 8.41135 1.96215 8.38805 1.67357 8.64789C1.38498 8.90773 1.36168 9.35232 1.62153 9.6409L4.93385 13.3196C5.11955 13.5259 5.39962 13.5966 5.64967 13.5253C5.78192 13.4929 5.90618 13.4218 6.00416 13.313L14.3898 3.99979C14.6496 3.7112 14.6263 3.26662 14.3378 3.00678Z" fill="white"></path>
                <path d="M13.3447 3.05882L13.9021 3.56067L13.9021 3.56067L13.3447 3.05882ZM14.3378 3.00678L13.8359 3.56414L14.3378 3.00678ZM5.46602 11.809L4.90866 12.3109L5.46602 12.9299L6.02338 12.3109L5.46602 11.809ZM2.66657 8.69993L3.22393 8.19809L3.22393 8.19809L2.66657 8.69993ZM1.67357 8.64789L1.17172 8.09053L1.17172 8.09053L1.67357 8.64789ZM1.62153 9.6409L2.17888 9.13905L2.17888 9.13905L1.62153 9.6409ZM4.93385 13.3196L5.4912 12.8178L5.4912 12.8178L4.93385 13.3196ZM5.64967 13.5253L5.47111 12.7969L5.45752 12.8002L5.44406 12.8041L5.64967 13.5253ZM6.00416 13.313L6.56152 13.8148L6.56152 13.8148L6.00416 13.313ZM14.3898 3.99979L14.9472 4.50163L14.9472 4.50163L14.3898 3.99979ZM13.9021 3.56067C13.8848 3.57991 13.8551 3.58146 13.8359 3.56414L14.8396 2.44942C14.2432 1.91242 13.3244 1.96057 12.7874 2.55697L13.9021 3.56067ZM6.02338 12.3109L13.9021 3.56067L12.7874 2.55697L4.90866 11.3072L6.02338 12.3109ZM6.02338 11.3072L3.22393 8.19809L2.10921 9.20178L4.90866 12.3109L6.02338 11.3072ZM3.22393 8.19809C2.68693 7.60168 1.76812 7.55353 1.17172 8.09053L2.17541 9.20525C2.15618 9.22257 2.12654 9.22102 2.10921 9.20178L3.22393 8.19809ZM1.17172 8.09053C0.575316 8.62754 0.527163 9.54634 1.06417 10.1427L2.17888 9.13905C2.19621 9.15829 2.19465 9.18793 2.17541 9.20525L1.17172 8.09053ZM1.06417 10.1427L4.37649 13.8215L5.4912 12.8178L2.17888 9.13905L1.06417 10.1427ZM4.37649 13.8215C4.76102 14.2485 5.34044 14.3933 5.85527 14.2466L5.44406 12.8041C5.45258 12.8016 5.46061 12.8017 5.46824 12.8039C5.47717 12.8063 5.48577 12.8117 5.4912 12.8178L4.37649 13.8215ZM5.4468 12.8111C5.44921 12.8085 5.45292 12.8052 5.45767 12.8024C5.46225 12.7997 5.46684 12.7979 5.47111 12.7969L5.82823 14.2538C6.10199 14.1866 6.35971 14.039 6.56152 13.8148L5.4468 12.8111ZM13.8324 3.49794L5.4468 12.8111L6.56152 13.8148L14.9472 4.50163L13.8324 3.49794ZM13.8359 3.56414C13.8167 3.54681 13.8151 3.51718 13.8324 3.49794L14.9472 4.50163C15.4842 3.90523 15.436 2.98642 14.8396 2.44942L13.8359 3.56414Z" fill="white"></path>
                </svg>
            </div>
        </div>
        <video class="video-content" :src="video.videoAddress"
          :id="'contribute'+video.videoId"
          preload="none"
          disablePictureInPicture
          muted
          loop
        ></video>
        <waitWatch2 v-show="!batchControls" @click.stop class="wait-watch-c" :video="video"/>
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
       </div>
       <div class="video-link2" @click="openVideo(video)">
        <el-tooltip
          :disabled="batchControls"
          popper-class="custom-tooltip"
          class="box-item"
          effect="light"
          :show-after="300"
          :offset="-15"
          :content="video.videoTitle"
          placement="bottom"
          :show-arrow="false"
        >
            <div v-if="searchContent" class="video-title" v-html="highlightText(video.videoTitle)"></div>
            <div v-else class="video-title">{{ video.videoTitle }}</div>
        </el-tooltip>
        <div class="collect-controls-info">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M176 416a112 112 0 1 1 0 224 112 112 0 0 1 0-224m336 0a112 112 0 1 1 0 224 112 112 0 0 1 0-224m336 0a112 112 0 1 1 0 224 112 112 0 0 1 0-224"></path></svg>
            <div v-show="!batchControls" class="collect-controls-info-content" @click.stop>
                <span @click="cancelCollect(collect.id)">取消收藏</span>
                <span @click="copyOrMove(collect.id,1)">复制至</span>
                <span @click="copyOrMove(collect.id,2)">移动至</span>
            </div>
        </div>
       </div>
       <div class="up-user-name-link" @click="openHome(1,video)">
            <div class="up-user-name">
                <img src="../img/up蓝.png">
                <img src="../img/up.png">
                <el-tooltip
                popper-class="custom-tooltip"
                :disabled="batchControls"
                class="box-item"
                effect="light"
                :show-after="300"
                :content="video.userName+'&nbsp; ·&nbsp; 收藏于'+video.createTime"
                placement="bottom"
                :show-arrow="false"
                >
                <span>{{ video.userName }}&nbsp; ·&nbsp; 收藏于{{ video.createTime }}</span>
                </el-tooltip>
            </div>
       </div>
   </div>
   <div v-else class="video">
        <div class="cover-img2"></div>
        <div class="video-link2">
        <el-tooltip
          :disabled="batchControls"
          popper-class="custom-tooltip"
          class="box-item"
          effect="light"
          :show-after="300"
          :offset="-15"
          content="视频已经失效"
          placement="bottom"
          :show-arrow="false"
        >
            <div v-if="searchContent" class="video-title" v-html="highlightText(video.videoTitle)"></div>
            <div v-else class="video-title">视频已经失效</div>
        </el-tooltip>
        <div class="collect-controls-info">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M176 416a112 112 0 1 1 0 224 112 112 0 0 1 0-224m336 0a112 112 0 1 1 0 224 112 112 0 0 1 0-224m336 0a112 112 0 1 1 0 224 112 112 0 0 1 0-224"></path></svg>
            <div v-show="!batchControls" class="collect-controls-info-content" @click.stop>
                <span @click="cancelCollect(collect.id)">取消收藏</span>
                <span @click="copyOrMove(collect.id,1)">复制至</span>
                <span @click="copyOrMove(collect.id,2)">移动至</span>
            </div>
        </div>
       </div>
       <div class="up-user-name-link">
            <div class="up-user-name">
                <img src="../img/up蓝.png">
                <img src="../img/up.png">
                <el-tooltip
                popper-class="custom-tooltip"
                :disabled="batchControls"
                class="box-item"
                effect="light"
                :show-after="300"
                :content="'未知'+'&nbsp; ·&nbsp; 收藏于'+collect.collectTime.split(' ')[0]"
                placement="bottom"
                :show-arrow="false"
                >
                <span>未知&nbsp; ·&nbsp; 收藏于{{ collect.collectTime.split(' ')[0] }}</span>
                </el-tooltip>
            </div>
       </div>
   </div> 
</template>

<script setup>
import waitWatch2 from '@/utils/waitWatch2.vue';
import { useGlobalStore } from "@/store/store";

const props=defineProps({
  video: {
    type: Object,
    required: true,
  },
  collect:{
    type: Object,
    required: true,
  },
  batchControls:{
    type: Boolean,
    required: true,
   },
   searchContent:{
       type: String,
       required: true,
   },
});

const store = useGlobalStore();
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

function highlightText(title) {
const regex = new RegExp(`(${props.searchContent})`, "g");
return title.replace(regex, '<span class="highlight2">$1</span>');
}

//打开我的主页
function openHome(menu,video){

if(props.batchControls){

    if(store.autoVideoList.length>0&&store.autoVideoList.some(item=> item === props.collect.id)){
        store.autoVideoList.splice(store.autoVideoList.findIndex(item=> item === props.collect.id),1);
    }
    else{
        store.autoVideoList.push(props.collect.id);
    }

return;
}

window.open(
`./home?homeMenu=${menu}&userId=${video.userId}`,
"_blank",
);
}

//打开视频页
function openVideo(video) {

if(props.batchControls){

    if(store.autoVideoList.length>0&&store.autoVideoList.some(item=> item === props.collect.id)){
        store.autoVideoList.splice(store.autoVideoList.findIndex(item=> item === props.collect.id),1);
    }
    else{
        store.autoVideoList.push(props.collect.id);
    }

return;
}

if(props.collect.deleteFlag)
return;
    
window.open("../video?videoId=BV"+video.videoId,"videoWindow");
}

//检查是否选中该视频
function checkExistVideo(id){

return store.autoVideoList.some(item=> item === id);
}

//取消收藏
function cancelCollect(id){
    store.autoVideoList.push(id);
}

//复制或移动视频
function copyOrMove(id,control){

    store.collectIds.push(id);
    store.setCollectControlsDialogFlag(true);
    store.setCopyOrMove(control);

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
    width: 206px;
    height: 115.88px;
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
        .check-out-box{
        position: absolute;
        top: 8px;
        left: 8px;
        z-index: 200;
        border: 1.5px solid white;
        border-radius: 3px;
        background-color: rgba(0, 0, 0, .2);
        color: white;
        transition: background-color 0.3s ease;
        width: 24px;
        height: 24px;

            .border{
            width: 100%;
            height: 100%;
            display: flex;
            border-radius: 3px;
            opacity: 0;
            visibility: hidden;
            transition: all 0.3s ease;
            justify-content: center;
            align-items: center;
            pointer-events: none;
            }
        }
        .active-check-out{
         background-color: #00AEEC;
         
            .border{
            opacity: 1;
            visibility: visible;
            }
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
            top: 7px;
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
        .collect-controls-info{
        position: absolute;
        display: flex;
        justify-content: center;
        align-items: center;
        width: 18px;
        height: 18.5px;
        right: 0px;
        top: 8px;
        transition: background-color 0.3s ease;
        border-radius: 6px;
            
            svg{
                width: 15px;
                height: 13px;
                transform: rotate(-90deg);
                color: #61666D;
            }
            .collect-controls-info-content{
                position: absolute;
                left: -127px;
                top: -145px;
                display: flex;
                flex-direction: column;
                background-color: white;
                box-shadow: 0 0 30px rgba(0,0,0,.1);
                border-radius: 8px;
                border: 1px solid #E3E5E7;
                color: #18191C;
                padding: 12px 0;
                opacity: 0;
                visibility: hidden;
                transition: all 0.3s ease;
                width: 145px;
                z-index: 100;

                    span{
                    padding: 0 24px;
                    height: 40px;
                    line-height: 40px;
                    text-align: left;
                    font-size: 14px;
                    color: #61666D;
                    transition: background-color 0.3s ease;
                    cursor: pointer
                    }
                    span:hover{
                        background-color: #F6F7F8;
                    }
            }
        }
        .collect-controls-info:hover{
            background-color: #ebeced;

            .collect-controls-info-content{
                transition-delay: 0.3s;
                opacity: 1;
                visibility: visible;
            }
        }
    }
    .up-user-name-link{
        position: relative;
        top: 7px;
        left: 1.5px;
        height: 18px;
        .up-user-name{
            overflow: hidden;
            img{
                position: absolute;
                width: 13px;
                height: 11px;
                top: 1.5px;
                transition: opacity 0.3s ease;
            }
            span{
                position: relative;
                width: 89%;
                left: 18px;
                top: -1.5px;
                display: -webkit-box;
                overflow: hidden;
                -webkit-box-orient: vertical;
                text-overflow: ellipsis;
                word-break: break-all;
                -webkit-line-clamp: 1;
                font-size: 11.5px;
                color: #9499A0;
                transition: color 0.3s ease;
            }
         }
    }
    .up-user-name-link:hover{
    .up-user-name{
        img:nth-child(2){
            transition: opacity 0.3s ease;
            opacity: 0;
        }
        img:nth-child(1){
            opacity: 1;
            transition: opacity 0.3s ease;
        }
        span{
            color: #00AEEC;
        }
    }
    }
    .cover-img2{
    position: relative;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    cursor: pointer;
    background-color: #E3E5E7;
    border-radius: 6px;
    z-index: 10;
    }
    
}

.video-link:hover{
    .cover-img{
        transition-delay: 0.3s;
        opacity: 0;
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