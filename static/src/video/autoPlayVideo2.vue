<template>
  <div class="autoPlayVideo2-container">
       <div class="autoPlayVideo2-title">接下来播放</div>
       <div class="autoPlayVideo2-videoList"><span @click="setAutoPlayVideoList=!setAutoPlayVideoList">自动连播</span> <el-switch class="custom-switch1" v-model="setAutoPlayVideoList"/></div>
       <div class="autoPlayVideo2-item" v-for="video in store.autoVideoList.slice(0,10)" :key="video.videoId" :class="{lineFlag: video.videoId===store.autoVideoList[0].videoId}">
         <span class="autoPlayVideo2-item-left" @click="intoVideo(video.videoId)" @mousemove="videoMouseover(video.videoId)" @mouseleave="videoMouseleave(video.videoId)">
          <img class="autoPlayVideo2-cover" :src="video.coverAddress">
          <video :src="video.videoAddress"
          :id="video.videoId"
          preload="none"
          disablePictureInPicture
          muted
          loop
          ></video>
          <span class="autoPlayVideo2-videoTime">{{ video.videoTime }}</span>
          <span class="auto-play-video-item-wait-watch" @click.stop="waitWatch(video.videoId)">
            <div v-show="store.userId===null" class="auto-play-video-item-wait-watch-title">稍后再看</div>
            <div v-show="video.waitWatch===0" class="auto-play-video-item-wait-watch-title">稍后再看</div>
            <div v-show="video.waitWatch===1" class="auto-play-video-item-wait-watch-title">移除</div>
            <div v-show="waitWatchInfoFlag&&video.waitWatch===1"  class="auto-play-video-item-wait-watch-info1">已加稍后再看</div>  
            <div v-show="waitWatchInfoFlag&&video.waitWatch===0"  class="auto-play-video-item-wait-watch-info2">已从稍后再看列表中移除</div>
            <img v-show="store.userId===null" src="../img/waitWatch.png"/>
            <img v-show="video.waitWatch===0" src="../img/waitWatch.png"/>
            <img v-show="video.waitWatch===1" src="../img/addWaitWatch.png"/>
          </span>
         </span>
         <span class="autoPlayVideo2-item-right">
        <el-tooltip
          popper-class="custom-tooltip"
          class="dynamicContent-item-tooltip"
          effect="light"
          :show-after="300"
          :content="video.videoTitle"
          placement="left"
          :offset="24"
          :show-arrow="false"
          :hide-after="0"
        >
        <span @click="intoVideo(video.videoId)" class="autoPlayVideo2-item-title">{{ video.videoTitle }}</span>
        </el-tooltip>
        <span @click="openHome(1,video.userId)" class="autoPlayVideo2-item-right-username-container">
            <img class="img1" src="../img/up.png">
            <img class="img2" src="../img/up蓝.png">
            <span class="autoPlayVideo2-item-right-username">{{ video.userName }}</span> 
        </span>
        <span class="autoPlayVideo2-item-right-videoinfo-container">
            <img class="img1" src="../img/播放量灰.png">
            <span class="autoPlayVideo2-item-right-playNumber">{{ video.videoPlayNumber }}</span>
            <img class="img2" src="../img/弹幕灰.png">
            <span class="autoPlayVideo2-item-right-scrollNumber">{{ video.videoScrollingNumber }}</span>
        </span>
         </span>
         <span v-show="video.videoId===store.autoVideoList[0].videoId" class="line"></span>
       </div>
        <div v-if="store.autoVideoList.length>10" v-show="!showMoreVideoFlag" class="show-video-btn" @click="showMoreVideoFlag=true">展开</div>
        <div  v-show="showMoreVideoFlag" class="autoPlayVideo2-item" v-for="video in store.autoVideoList.slice(10,20)" :key="video.videoId" :class="{lineFlag: video.videoId===store.autoVideoList[0].videoId}">
         <span class="autoPlayVideo2-item-left" @click="intoVideo(video.videoId)" @mousemove="videoMouseover(video.videoId)" @mouseleave="videoMouseleave(video.videoId)">
          <img class="autoPlayVideo2-cover" :src="video.coverAddress">
          <video :src="video.videoAddress"
          :id="video.videoId"
          preload="none"
          disablePictureInPicture
          muted
          loop
          ></video>
          <span class="autoPlayVideo2-videoTime">{{ video.videoTime }}</span>
          <span class="auto-play-video-item-wait-watch" @click.stop="waitWatch(video.videoId)">
            <div v-show="store.userId===null" class="auto-play-video-item-wait-watch-title">稍后再看</div>
            <div v-show="video.waitWatch===0" class="auto-play-video-item-wait-watch-title">稍后再看</div>
            <div v-show="video.waitWatch===1" class="auto-play-video-item-wait-watch-title">移除</div>
            <div v-show="waitWatchInfoFlag&&video.waitWatch===1"  class="auto-play-video-item-wait-watch-info1">已加稍后再看</div>  
            <div v-show="waitWatchInfoFlag&&video.waitWatch===0"  class="auto-play-video-item-wait-watch-info2">已从稍后再看列表中移除</div>
            <img v-show="store.userId===null" src="../img/waitWatch.png"/>
            <img v-show="video.waitWatch===0" src="../img/waitWatch.png"/>
            <img v-show="video.waitWatch===1" src="../img/addWaitWatch.png"/>
          </span>
         </span>
         <span class="autoPlayVideo2-item-right">
        <el-tooltip
          popper-class="custom-tooltip"
          class="dynamicContent-item-tooltip"
          effect="light"
          :show-after="300"
          :content="video.videoTitle"
          placement="left"
          :offset="24"
          :show-arrow="false"
          :hide-after="0"
        >
        <span @click="intoVideo(video.videoId)" class="autoPlayVideo2-item-title">{{ video.videoTitle }}</span>
        </el-tooltip>
        <span @click="openHome(1,video.userId)" class="autoPlayVideo2-item-right-username-container">
            <img class="img1" src="../img/up.png">
            <img class="img2" src="../img/up蓝.png">
            <span class="autoPlayVideo2-item-right-username">{{ video.userName }}</span> 
        </span>
        <span class="autoPlayVideo2-item-right-videoinfo-container">
            <img class="img1" src="../img/播放量灰.png">
            <span class="autoPlayVideo2-item-right-playNumber">{{ video.videoPlayNumber }}</span>
            <img class="img2" src="../img/弹幕灰.png">
            <span class="autoPlayVideo2-item-right-scrollNumber">{{ video.videoScrollingNumber }}</span>
        </span>
         </span>
         <span v-show="video.videoId===store.autoVideoList[0].videoId" class="line"></span>
       </div>
       <div v-if="store.autoVideoList.length>10" v-show="showMoreVideoFlag" class="show-video-btn" @click="showMoreVideoFlag=false">收起</div>
  </div>
</template>

<script>
import { onMounted, ref, watch } from 'vue';
import apiClient from "../services/apiClient";
import {useGlobalStore} from "../store/store";
export default {
    name: "autoPlayVideo2",
    setup(){
        const store = useGlobalStore();
        const waitWatchInfoFlag=ref(false);
        const setAutoPlayVideoList=ref(false);
        const showMoreVideoFlag=ref(false);
        let videoAutoPlayTIme={};

        onMounted(() => {
            setAutoPlayVideoList.value=JSON.parse(localStorage.getItem("autoPlayVideoList")) || false;
        });

        // 监听localStorage中是否有自动播放列表
        watch(setAutoPlayVideoList,(newValue)=>{

            localStorage.setItem("autoPlayVideoList",newValue);
            store.setAutoPlayVideoList(newValue);
            
        })

        watch(()=>store.AutoPlayVideoList,()=>{
            setAutoPlayVideoList.value=store.AutoPlayVideoList;
        }, {deep: true})


         //添加到稍后再看
         async function waitWatch(videoId) {
            try {

                if(store.userId===null)
                {
                store.setLoginDialogVisibleFlag(true);    
                return;
                } 


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
                const updatedVideos = store.autoVideoList.map((item) => {
                    if (item.videoId === videoId && item.waitWatch === 0) {
                    return { ...item, waitWatch: 1 }; // 创建一个新的对象并修改 waitWatch
                    } else if (item.videoId === videoId && item.waitWatch === 1) {
                    return { ...item, waitWatch: 0 };
                    }
                    return item; // 保留原始对象
                });
                store.autoVideoList.length = 0; // 清空原数组
                store.setAutoVideoList(updatedVideos); // 重新赋值
                waitWatchInfoFlag.value=true;
                setTimeout(() => {
                waitWatchInfoFlag.value=false;
                }, 2000);
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

         //进入视频播放页面
         function intoVideo(videoId){
            window.location.href = `./video?videoId=BV${videoId}`;
        }    

        //打开我的主页
        function openHome(menu,id){
          window.open(
          `./home?homeMenu=${menu}&userId=${id}`,
          "_blank",
        );
        }


        return {
         store,
         waitWatchInfoFlag,
         setAutoPlayVideoList,
         waitWatch,
         videoMouseover,
         videoMouseleave,
         intoVideo,
         showMoreVideoFlag,
         openHome,
        }
    }

}
</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
 }

 .autoPlayVideo2-container{
    position: relative;
    top: -346px;
    left: 739.5px;
 }

 .autoPlayVideo2-title{
    position: absolute;
    top: -33px;
    font-size: 14.5px;
 }

 .autoPlayVideo2-item{
    position: relative;
    width: 350px;
    height: 82.5px;
    margin-bottom: 9px;
    .autoPlayVideo2-cover{
        width: 141px;
        height: 80px;
        border-radius: 6px;
        object-fit: cover;
        cursor: pointer;
        z-index: 10;
        transition: all 0.3s ease;
    }
    .autoPlayVideo2-videoTime{
        position: absolute;
        font-size: 13px;
        color: white;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 1px 3px;
        background-color: rgba(0,0,0,0.5);
        border-radius: 3px;
        left: 94.5px;
        top: 54px;
        transition: all 0.3s ease;
    }
    video{
        position: absolute;
        width: 141px;
        height: 80px;
        border-radius: 6px;
        left: 0;
        cursor: pointer; 
        z-index: -5;
        visibility: hidden;
    }
 }

 .autoPlayVideo2-item-left{
     position: absolute;
     width: 141px;
     height: 80px;
 }
 .autoPlayVideo2-item-left:hover{
        .autoPlayVideo2-videoTime{
            opacity: 0;
        }
        .auto-play-video-item-wait-watch{
        transition-delay: 0.3s;
        visibility: visible;
        opacity: 1;
       }

       video{
          visibility: visible;
          background-color: #000000;
       }

       .autoPlayVideo2-cover{
          transition-delay: 0.3s;
          visibility: hidden;
          
       }

}

.auto-play-video-item-wait-watch{
    position: relative;
    left: 107px;
    top: -76px;
    width: 26px;
    height: 26px;
    border-radius: 6px;
    background-color: #000000;
    display: flex;
    justify-content: center;
    align-items: center;
    transition: all 0.3s ease;
    visibility: hidden;
    opacity: 0;
    cursor: pointer;

    img{
        width: 24px;
        height: 24px;
        border-radius: 6px;
    }

}

.auto-play-video-item-wait-watch-title{
 position: absolute;
 display: flex;
 justify-content: center;
 align-items: center;
 color: white;
 width: 52px;
 height: 28px;
 font-size: 11px;
 border-radius: 4px;
 background-color: #000000;
 top: -15px;
 transition: all 0.3s ease;
 opacity: 0;
 visibility: hidden;
}

.auto-play-video-item-wait-watch:hover .auto-play-video-item-wait-watch-title{
    transition-delay: 0.3s;
    top: -33px;
    opacity: 1;
    visibility: visible;
}

.auto-play-video-item-wait-watch-info1{
position: absolute;
 display: flex;
 justify-content: center;
 align-items: center;
 color: white;
 width: 75px;
 height: 27px;
 font-size: 11px;
 border-radius: 4px;
 background-color: #000000;
 top: -33px;
}

.auto-play-video-item-wait-watch-info2{
position: absolute;
 display: flex;
 justify-content: center;
 align-items: center;
 color: white;
 width: 130px;
 height: 27px;
 font-size: 11px;
 border-radius: 4px;
 background-color: #000000;
 top: -33px;
}

.autoPlayVideo2-videoList{
    position: absolute;
    top: -37px;
    user-select: none;
    font-size: 13.5px;
    left: 260px;
    cursor: pointer;
    color: #9499a0;

    .custom-switch1 {
    width: 20px !important;
    --el-switch-height: 20px; /* 修改高度 */
    --el-switch-width: 30px !important; /* 修改宽度 */
    --el-switch-on-color: #00aeec !important;
    }
    
}

.autoPlayVideo2-item-right{
    position: relative;
    .autoPlayVideo2-item-title{
        cursor: pointer;
        position: absolute;
        color: black;
        font-size: 15px;
        width: 200px;
        left: 151px;
        line-height: 19px;
        transition: color 0.3s;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-overflow: ellipsis;
        word-break: break-all;
        line-break: anywhere;
    }
    .autoPlayVideo2-item-title:hover{
        color: #00aeec;
    }
}

.autoPlayVideo2-item-right-username-container{
   position: relative;
   cursor: pointer;
   img{
      width: 15px;
      height: 12.5px;
      position: absolute;
      left: 152.5px;
      top: 45px;
      transition: all 0.3s ease;
   }
   .img2{
    opacity: 0;
   }
   .autoPlayVideo2-item-right-username{
       position: absolute;
       color: #9499a0;
       display: -webkit-box;
       left: 171px;
       top: 43px;
        -webkit-line-clamp: 1;
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-overflow: ellipsis;
        transition: all 0.3s ease;
        word-break: break-all;
        line-break: anywhere;
        font-size: 12px;
        width: 170px;
   }
  
   
}

.autoPlayVideo2-item-right-username-container:hover{
    .img1{
       opacity: 0;
     }
     .img2{
        opacity: 1;
     }
     .autoPlayVideo2-item-right-username{
          color: #00aeec;
     }
}

.autoPlayVideo2-item-right-videoinfo-container{
    position: absolute;
    width: 200px;
    left: 152px;
    top: 61px;
    color: #9499a0;
    font-size: 13px;
    img{
        width: 15px;
        height: 12.5px;
    }
    .img1{
        transform: translate(0.5px, 3px);
    }
    .img2{
        transform: translate(20px, 3px);
    }
    .autoPlayVideo2-item-right-playNumber{
 
       display: inline-block;
       transform: translate(6px, 2px);
        
    }

    .autoPlayVideo2-item-right-scrollNumber{
        display: inline-block;
        left: 100px;
        transform: translate(25px, 2px);
    }
}

.line{
    display: inline-block;
    width: 100%;
    height: 1px;
    background-color: #e3e5e7;
    transform: translate(0px, 79px);
}

.lineFlag{
    margin-bottom: 32px;
}

.show-video-btn{
    position: relative;
    width: 350px;
    height: 42px;
    left: 0px;
    top: 3px;
    border-radius: 6px;
    cursor: pointer;
    background-color: #f1f2f3;
    color: #18191c;
    line-height: 42px;
    font-size: 11.5px;
    text-align: center;
    user-select: none;
    transition: all 0.3s ease;

}

.show-video-btn:hover{
    background-color: #e3e5e7;
}
</style>