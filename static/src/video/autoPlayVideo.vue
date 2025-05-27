<template>
   <div class="auto-play-video-container" :class="{ 'into-video-all-display-ing': propss.intoVideoAllDisplayIngFlag }">
       <div class="auto-play-video-header">
         <img @click="openHome(1,store.selectUpVideo.upUser?.id)" class="auto-play-video-avatar" :src="store.selectUpVideo.upUser?.avatarAddress">
         <span @click="openHome(1,store.selectUpVideo.upUser?.id)" class="auto-play-video-username">{{ store.selectUpVideo.upUser?.userName }}</span>
         <span class="auto-play-video-vip"> <img src="../img/充电.webp"> 充电</span>
         <span v-show="store.selectUpVideo?.isFansFlag===0" class="auto-play-video-follow" @click="store.setAddFollowFlag(1)"> <img src="../img/加关注.png"/> 关注 {{ store.selectUpVideo.upUser?.fansNumber }}</span>
         <span v-show="store.selectUpVideo?.isFansFlag===1" class="auto-play-video-delete-follow" @click="store.setAddFollowFlag(2)">已关注</span>
         <span class="auto-play-video-replay" @click="store.setReVideoPlayerFlag(true)">
            <img src="../img/重播灰.png">
            <span>重播</span>
         </span>
         <div class="auto-play-video-operation">
            <span class="auto-play-video-operation1" @click="waitLike">
                <img v-show="!propss.likeVideoClickFlag" src="../img/点赞灰.png">
                <img v-show="propss.likeVideoClickFlag" src="../img/点赞蓝小.png">
                <span :style="{color: propss.likeVideoClickFlag? '#0cbeff':'white'}">好评</span></span>
            <span class="auto-play-video-operation2" @click="store.setLTCAFlag(2)">
                <img v-show="!propss.videoThrowCoinClickFlag" src="../img/投币灰.png">
                <img v-show="propss.videoThrowCoinClickFlag" src="../img/视频投币蓝.png">
                <span style="height: 18.5px;" :style="{color: propss.videoThrowCoinClickFlag? '#0cbeff':'white'}">投币</span></span>
            <span class="auto-play-video-operation3" @click="store.setLTCAFlag(3)">
                <img v-show="!propss.videoCollectClickFlag" src="../img/收藏灰.png">
                <img v-show="propss.videoCollectClickFlag" src="../img/视频收藏蓝.png">
                <span style="margin-left: 1px;" :style="{color: propss.videoCollectClickFlag? '#0cbeff':'white'}">收藏</span></span>
            <span class="auto-play-video-operation4" @click="shareHoverF2()" @mouseleave="shareHoverF(false)">
                <img v-show="!propss.videoShareClickFlag" src="../img/分享灰.png">
                <img v-show="propss.videoShareClickFlag" src="../img/视频分享蓝.png">
                <span style="margin-left: 1px;" :style="{color: propss.videoShareClickFlag? '#0cbeff':'white'}">分享</span></span>
            </div>
       </div>
       <div class="auto-play-video-body">
        <div v-show="propss.intoVideoAllDisplayIngFlag" class="auto-play-video-body-video-recommend">相关推荐</div>
           <div v-show="!propss.intoVideoAllDisplayIngFlag" class="auto-play-video-item"  v-for="video in store.autoVideoList.slice(0,6)" :key="video.videoId" @click="intoVideo(video.videoId)">
               <span @mousemove="videoMouseover(video.videoId+'auto1')" @mouseleave="videoMouseleave(video.videoId+'auto1')">
                <div v-show="video.videoId===store.autoVideoList[0].videoId&&store.AutoPlayVideoList" class="cancel-auto-play-video" @click.stop="cancelAutoPlayVideo">
                   取消连播
                </div>
                <div v-show="video.videoId===store.autoVideoList[0].videoId&&store.AutoPlayVideoList" class="black-auto-play-video"></div>
                <div v-show="video.videoId===store.autoVideoList[0].videoId&&store.AutoPlayVideoList" class="loader">
                    <svg viewBox="0 0 100 100">
                        <circle cx="50" cy="50" r="45"></circle>
                    </svg>
                   <div class="loader-inner">
                    <img src="../img/播放白.png">
                   </div>
                   <div class="black-border"></div>
                </div>
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
                <img class="auto-play-video-item-cover" :src="video.coverAddress">
                <video :src="video.videoAddress"
                        class="auto-play-video-item-video"
                        :id="video.videoId+'auto1'"
                        preload="none"
                        disablePictureInPicture
                        muted
                        loop
                ></video>
                <span class="auto-play-video-item-title">{{ video.videoTitle }}</span>
               </span>
           </div>
           <div v-show="propss.intoVideoAllDisplayIngFlag" class="auto-play-video-item" v-for="video in store.autoVideoList.slice(0,8)" :key="video.videoId" @click="intoVideo(video.videoId)">
               <span @mousemove="videoMouseover(video.videoId+'auto2')" @mouseleave="videoMouseleave(video.videoId+'auto2')">
                <div v-show="video.videoId===store.autoVideoList[0].videoId&&store.AutoPlayVideoList" class="cancel-auto-play-video" @click.stop="cancelAutoPlayVideo">
                   取消连播
                </div>
                <div v-show="video.videoId===store.autoVideoList[0].videoId&&store.AutoPlayVideoList" class="black-auto-play-video"></div>
                <div v-show="video.videoId===store.autoVideoList[0].videoId&&store.AutoPlayVideoList" class="loader">
                    <svg viewBox="0 0 100 100">
                        <circle cx="50" cy="50" r="45"></circle>
                    </svg>
                   <div class="loader-inner">
                    <img src="../img/播放白.png">
                   </div>
                   <div class="black-border"></div>
                </div>
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
                <img class="auto-play-video-item-cover" :src="video.coverAddress">
                <video :src="video.videoAddress"
                        class="auto-play-video-item-video"
                        :id="video.videoId+'auto2'"
                        preload="none"
                        disablePictureInPicture
                        muted
                        loop
                ></video>
                <span class="auto-play-video-item-title">{{ video.videoTitle }}</span>
               </span>
           </div>
       </div>
       <div v-show="shareHover&&store.shareHover" :class="{'video-share-container2': propss.intoVideoAllDisplayIngFlag}" class="video-share-container" @mouseover="shareHoverF(true)" @mouseleave="shareHoverF(false)"><videoShareC :share-hover="shareHover" :height-flag="true"/></div> 
   </div>
</template>

<script>
import { reactive, onMounted, ref, watch } from "vue";
import apiClient from "../services/apiClient";
import {useGlobalStore} from "../store/store";
import videoShareC from '../utils/videoShare.vue';
export default {
    name: "autoPlayVideo",
    props:{
      
        intoVideoAllDisplayIngFlag:{
            type:Boolean,
        },
        likeVideoClickFlag:{
            type:Boolean,
        },
        videoThrowCoinClickFlag:{
            type:Boolean,
        },
        videoShareClickFlag:{
            type:Boolean,
        },
        videoCollectClickFlag:{
            type:Boolean,
        }
    },
    components:{
      videoShareC     
    },
    setup(props){

        const propss=reactive(props);
        const store=useGlobalStore();
        const videoList=reactive([]);
        const waitWatchInfoFlag=ref(false);
        let videoAutoPlayTIme={};
        const shareHover=ref(false);

       
        onMounted(async()=>{
            store.setAutoPlayVideoList(JSON.parse(localStorage.getItem("autoPlayVideoList")) || false);
            await getUserIp();
            await ChecklLogin();
            getVideoPageByVideo();
        })

        //获取用户ip和token
        async function getUserIp(){
            
            const response = await apiClient.get("/userIp/getUserIp");

            if(response.data.code === 1)
                store.setUserIp(response.data.data.userIp);
                store.setToken(response.data.data.token);

        }

        //检查是否登录
        async function ChecklLogin() {
        try {

            const response = await apiClient.get(`/user/checkLoginFlag/${store.userIp}`);
            if (response.data.code === 1) {
            store.setUserId(response.data.data.id);
            store.setUserInformation(response.data.data);
            } else 
            store.setUserId(null);

        } catch (error) {
            store.setUserId(null);
        }
        }

        //获取视频列表
        async function getVideoPageByVideo(){

            try {
                const urlParams = new URLSearchParams(window.location.search);
                let videoId = urlParams.get("videoId");
                videoId=videoId.replace("BV","");

                if(videoId===null||videoId.length===0)
                    return;

                let videos={
                    id: videoId,
                    userId: store.userId||0,
                }
                const response=await apiClient.post("/video/getVideoPageByVideo",videos);
                if(response.data.code===1)
               {
                videoList.length=0;
                 Object.assign(videoList,response.data.data);
                 store.setAutoVideoList(response.data.data);
               }                
            } catch (error) {
                
            }
        }

        //点赞
        let ctrlAwaitTimeFlag=true;
        function waitLike(){
            if(ctrlAwaitTimeFlag){
                ctrlAwaitTimeFlag=false;
                store.setLTCAFlag(1);
                setTimeout(() => {
                    ctrlAwaitTimeFlag=true;
                }, 1000);
            }
        }

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

        //进入视频播放页面
        function intoVideo(videoId){
            window.location.href = `./video?videoId=BV${videoId}`;
        }    

        //取消连播
        function cancelAutoPlayVideo(){
            localStorage.removeItem("autoPlayVideoList");
            store.setAutoPlayVideoList(false)
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

        let getVideoPageByVideoFlag=true;
        watch(()=>store.userId,async()=>{

            if(store.userId!==null&&getVideoPageByVideoFlag)
           {
            getVideoPageByVideoFlag=false;
            getVideoPageByVideo();
          }
        })

         //延时分享窗
        let delayShareTime = null;
        function shareHoverF(flag){
        if(flag)
        {
            clearTimeout(delayShareTime);
            delayShareTime=setTimeout(()=>{
            shareHover.value=true;
            store.setShareHover(true);   
            },300)
        }
            else{
            clearTimeout(delayShareTime);
            if(store.shareVideoFocusFlag)
            return;
            delayShareTime=setTimeout(() => {
            shareHover.value=false;
            store.setShareHover(false);   
            },300);
            }
        
        }

        function shareHoverF2(){
            if(store.shareHover)
                {
                    store.setShareHover(false);
                    shareHover.value=false;
                }
            else
                {
                    store.setShareHover(true);
                    shareHover.value=true;
                }
        }

        //打开我的主页
        function openHome(menu,id){
          window.open(
          `./home?homeMenu=${menu}&userId=${id}`,
          "_blank",
        );
        }

        return{
            videoList,
            waitWatchInfoFlag,
            store,
            ctrlAwaitTimeFlag,
            waitLike,
            waitWatch,
            propss,
            intoVideo,
            cancelAutoPlayVideo,
            videoMouseover,
            videoMouseleave,
            shareHover,
            shareHoverF,
            openHome,
            shareHoverF2,
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

.auto-play-video-container{

    position: relative;
    width: 595px;
    height: 328px;
    background-color: #000000;
    left: 50%;
    top: 34px;
    cursor: pointer;
    transform: translate(-50%);

    .auto-play-video-header{

        position: relative;
        top: 0;
        left: 0;
        z-index: 100;
        .auto-play-video-avatar{
            width: 48.78px;
            height: 48.78px;
            border-radius: 50%;
            cursor: pointer;
            border: 2px solid hsla(0, 0%, 100%, .6);

        }
        .auto-play-video-avatar:hover{
            border: 2px solid #00aeec;
        }

        .auto-play-video-username{
            position: absolute;
            top: -3px;
            color: white;
            font-size: 13px;
            left: 65px;
            cursor: pointer;
        }

        .auto-play-video-username:hover{
            text-shadow: 0 0 4px #fff;
        }

        .auto-play-video-vip{
            position: absolute;
            width: 62px;
            height: 25px;
            border: 1px solid #eb7a99;
            color: #eb7a99;
            font-size: 12px;
            cursor: pointer;
            display: flex;
            border-radius: 3px;
            line-height: 22px;
            top: 21.5px;
            left: 64px;
            transition: all 0.3s ease;
            img{
             width: 16px;
             height: 16px;
             margin-left: 9px;
             margin-right: 2px;
             transform: translateY(4px);
            }
        }

        .auto-play-video-vip:hover{
            background-color: rgba(251, 114, 153, .2);

        }

        .auto-play-video-follow{
            position: absolute;
            z-index: 1000;
            width: 114px;
            height: 25px;
            border: 1px solid #00aeec;
            background-color: #00aeec;
            color: white;
            font-size: 12px;
            cursor: pointer;
            display: flex;
            justify-content: center;
            border-radius: 3px;
            line-height: 24px;
            top: 21.5px;
            left: 136px;
            transition: all 0.3s ease;
            img{
             width: 11px;
             height: 11px;
             margin-left: 6px;
             margin-right: 5px;
             transform: translateY(6px);
            }
        }

        .auto-play-video-follow:hover{
            background-color: #0cbeff;
        }

        .auto-play-video-delete-follow{
            position: absolute;
            width: 74px;
            height: 25.5px;
            background: hsla(0, 0%, 100%, .2);
            color: white;
            font-size: 11.5px;
            cursor: pointer;
            display: flex;
            justify-content: center;
            align-items: center;
            border-radius: 3px;
            top: 21.5px;
            left: 136px;
            transition: all 0.3s ease;
        
        }
        .auto-play-video-delete-follow:hover{
            background: hsla(0, 0%, 100%, .3);

        }

        .auto-play-video-replay{
            position: absolute;
            left: 329px;
            top: 12px;
            width: 20px;
            height: 34px;
            cursor: pointer;
            img{
             width: 17px;
             height: 18px;
            }
            span{
              position: absolute;
              color: hsla(0, 0%, 100%, .9);              
              left: -1px;
              top: 24px;
              font-size: 10px;
            }
        }

        .auto-play-video-operation{
            position: absolute;
            left: 375px;
            top: 10px;
            border-left: 1px solid #121212;
            display: flex;
            justify-content: right;
            padding-left: 34px;
            font-size: 10px;
            z-index: 1000;
            line-height: 15px;
            color: hsla(0, 0%, 100%, .9);
            .auto-play-video-operation1{
                margin-left: 18px;
                cursor: pointer;

                img{
                    width: 20px;
                    height: 20px;
                }
            }
            .auto-play-video-operation2{
                margin-left: 25.5px;
                cursor: pointer;

                img{
                    width: 20px;
                    height: 20px;
                }
            }
            .auto-play-video-operation3{
                margin-left: 25px;
                cursor: pointer;

                img{
                    width: 20px;
                    height: 19px;
                    margin-left: 1px;
                    margin-bottom: 1px;
                }
            }
            .auto-play-video-operation4{
                margin-left: 25px;
                cursor: pointer;

                img{
                    width: 20px;
                    height: 18px;
                    margin-left: 1px;
                    margin-bottom: 3px;
                }
            }



            
        }


    }

    .auto-play-video-body {
    position: relative;
    width: 100%;
    top: -15px;
    left: 0px;
    display: flex;
    flex-wrap: wrap; /* 自动换行 */
    justify-content: space-between; /* 子元素均匀分布 */
    gap: 12px; /* 增加间距 */

}

.auto-play-video-item {
    position: relative;
    width: calc((100% - 24px) / 3); /* 每行 3 个，减去间距 */
    height: 119px;
    margin-bottom: -3px; /* 每行元素之间的间距 */
}

.auto-play-video-item span {
    cursor: pointer;
    color: white;
    font-size: 12px;
    .cancel-auto-play-video{
        position: absolute;
        background: hsla(0, 0%, 100%, .4);
        border-radius: 4px;
        color: #fff;
        cursor: pointer;
        font-size: 11px;
        height: 20px;
        line-height: 20px;
        left: 7px;
        top: 33px;
        text-align: center;
        width: 56px;
        z-index: 1000;
    }
    .black-auto-play-video{
        position: absolute;
        background-color: rgba(0, 0, 0, 0.4);
        width: 188px;
        height: 119px;
        top: 25px;
        z-index: 999;
    }
}   

.auto-play-video-item span .auto-play-video-item-cover {
    position: absolute;
    border-radius: 5px;
    width: 188px; /* 自适应宽度 */
    height: 119px; /* 高度根据图片比例自动调整 */
    transition: all 0.3s ease;
    object-fit: cover;
    border: 1px solid rgba(0, 0, 0, 0);
    z-index: 10;
}

.auto-play-video-item span .auto-play-video-item-video {
    position: relative;
    border-radius: 5px;
    width: 188px; /* 自适应宽度 */
    height: 119px; /* 高度根据图片比例自动调整 */
    border: 1px solid rgba(0, 0, 0, 0);
    background-color: #000000;
    visibility: hidden;
    z-index: 0;
}

.auto-play-video-item span .auto-play-video-item-title {
    transform: translate(10px, -24px);
    position: absolute;
    text-overflow: ellipsis;
    width: 170px; /* 自适应宽度 */
    font-size: 12px;
    height: 15px;
    text-shadow: 0 0 1px #000000;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    word-wrap: break-word;
    -webkit-line-clamp: 1; 
    overflow: hidden;
    transition: all 0.3s ease;
    z-index: 100;
}

.auto-play-video-item span:hover .auto-play-video-item-cover {
    transition-delay: 0.3s;
    opacity: 0;
}

.auto-play-video-item span:hover .auto-play-video-item-video {
    visibility: visible;
    border: 1px solid white;

}

.auto-play-video-item span:hover .auto-play-video-item-title {
    transform: translate(10px, -40px);
    height: 30px;
    -webkit-line-clamp: 2;
}

}

.auto-play-video-item-wait-watch{
    position: relative;
    left: 155px;
    top: 33px;
    width: 24px;
    height: 24px;
    border-radius: 6px;
    background-color: #000000;
    display: flex;
    z-index: 1000;
    justify-content: center;
    align-items: center;
    transition: all 0.3s ease;
    visibility: hidden;

    img{
        width: 24px;
        height: 24px;
        border-radius: 6px;
    }

}

.auto-play-video-item span:hover .auto-play-video-item-wait-watch{
    transition-delay: 0.3s;
    visibility: visible;
}

.auto-play-video-item-wait-watch-title{
 position: absolute;
 display: flex;
 justify-content: center;
 align-items: center;
 width: 50px;
 height: 27px;
 font-size: 11px;
 border-radius: 2px;
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
 width: 75px;
 height: 27px;
 font-size: 11px;
 border-radius: 2px;
 background-color: #000000;
 top: -33px;
}

.auto-play-video-item-wait-watch-info2{
position: absolute;
 display: flex;
 justify-content: center;
 align-items: center;
 width: 130px;
 height: 27px;
 font-size: 11px;
 border-radius: 2px;
 background-color: #000000;
 top: -33px;
}

.into-video-all-display-ing{
    position: relative;
    width: 1040px;
    height: 390px;
    transform: translate(-50%,40%);
    .auto-play-video-header{
        .auto-play-video-avatar{
            width: 59px;
            height: 59px;
        }
        .auto-play-video-username{
            left: 78px;
        }
        .auto-play-video-vip{
            left: 78px;
            width: 75px;
            height: 32px;
            display: flex;
            justify-content: center;
            align-items: center;
            font-size: 13px;
            img{
                width: 18px;
                height: 19px;
                transform: translate(-4px, 0px);
                margin-right: -3px;
                margin-left: 3px;
            }
        }
        .auto-play-video-follow{
            left: 164px;
            height: 32px;
            align-items: center;
            font-size: 13px;
            img{
                transform: translateY(0px);
                margin-left: 3px;
                width: 12px;
                height: 12px;
            }
        }
        .auto-play-video-delete-follow{
            width: 90px;
            left: 164px;
            height: 32px;
            align-items: center;
            font-size: 13px;
            img{
                transform: translateY(0px);
                margin-left: 3px;
                width: 12px;
                height: 12px;
            }
        }
        .auto-play-video-replay{
            left: 680px;
            transform: scale(1.15);
        }
        .auto-play-video-operation{
            left: 760px;
            transform: scale(1.15);
            span{
                margin-left: 0.5px;
            }
            img{
                margin-right: 7px;
            }
        }

    }

    .auto-play-video-body {
           
        transform: translateY(45px);
        width: 102%;

        .auto-play-video-body-video-recommend{
            position: absolute;
            color: white;
        }
        .auto-play-video-item {
            margin-top: 10px;
            width: calc((100% - 105px) / 4); /* 每行 3 个，减去间距 */
            height: 124px;
            margin-bottom: 28px;
            .auto-play-video-item-title{
            transform: translate(10px, -26px);
            width: 220px; /* 自适应宽度 */
            font-size: 13px;
           }
        
            span{
             .cancel-auto-play-video{
              transform: scale(1.2);
              left: 15px;
              top: 35px;
        
             }
             .black-auto-play-video{
                width: 239px;
                height: 145px;
                top: 25px;
             }
             .loader{
                transform: scale(1.2) rotate(-93deg);
                left: 90px;
                top: 75px;
             }

            }

        }

        .auto-play-video-item:hover{
            .auto-play-video-item-title{
                transform: translate(10px, -44px);
                height: 34px;
            } 
        }

        .auto-play-video-item span .auto-play-video-item-cover {
            width: 239px; /* 高度根据图片比例自动调整 */
            height: 145px;
            object-fit: cover;
          
        }

        .auto-play-video-item span .auto-play-video-item-video {
            width: 239px; /* 自适应宽度 */
            height: 145px; /* 高度根据图片比例自动调整 */
        }

        .auto-play-video-item-wait-watch{
            transform: scale(1.15);
            left: 195px;
            z-index: 1000;
        }

        .auto-play-video-item span:hover .auto-play-video-item-cover {
            transition-delay: 0.3s;
            opacity: 0;
        }

        .auto-play-video-item span:hover .auto-play-video-item-video{
            visibility: visible;
            border: 1px solid white;

        }
    
            

    }

}


.auto-play-video-item-play-button-container{
    position: absolute;
    width: 50px;
    height: 50px;
    left: 50px;
    top: 50px;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 50%;
    background-color: rgba(0, 0, 0, 0.7);
    div{
        width: 42px;
        height: 42px;
        border-radius: 50%;
        background-color: rgba(255, 255, 255, 0.4);
        display: flex;
        justify-content: center;
        align-items: center;
            img{
                width: 18px;
                height: 24px;
            }
    }
}


.loader{
    position: absolute;
    left: 69px;
    top: 61px;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    transform: rotate(-93deg);
    z-index: 1000;
    svg{
       circle{
           position: absolute;
           fill: none;
           stroke: white;
           stroke-width: 8px;
           animation: loader 5s linear forwards;
           z-index: 10;
       }
    }

    .loader-inner{
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 42px;
      height: 42px;
      border-radius: 50%;
      background-color: rgba(255, 255, 255, 0.4);
      display: flex;
      justify-content: center;
      align-items: center;
      img{
        position: absolute;
        width: 20px;
        height: 24px;
        animation: l3 6s infinite linear;
        transform: rotate(93deg) translateX(2px);
      }
  }

  .black-border{
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    width: 49px;
    height: 49px;
    border-radius: 50%;
    border: 4px solid rgba(0, 0, 0, 0.6);
    z-index: -10;
  }
}

@keyframes loader {
    0%{
       stroke-dasharray: 315;
       stroke-dashoffset: 315;
    }
    100%{
       stroke-dasharray: 315;
       stroke-dashoffset: 0;
    }
}

.video-share-container{
position: absolute;
left: 100px;
top: 55px;
z-index: 10000;
}

.video-share-container2{
    left: 500px;
    top: 90px;
    transform: scale(1.2);
}
</style>