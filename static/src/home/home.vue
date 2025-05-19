<template>
    <div class="home">
        <!-- 单独渲染个人资料部分 -->
        <div v-if="!loadMore && ((store.userId!== null && store.userId === userId) || store.homeUserInformation.publicBirthday === 1)" class="home-user-info">
            <div>个人资料
                <span v-if="store.homeUserInformation.id === store.userId" @click="openAccount">
                    编辑 
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024">
                        <path fill="currentColor" d="M340.864 149.312a30.592 30.592 0 0 0 0 42.752L652.736 512 340.864 831.872a30.592 30.592 0 0 0 0 42.752 29.12 29.12 0 0 0 41.728 0L714.24 534.336a32 32 0 0 0 0-44.672L382.592 149.376a29.12 29.12 0 0 0-41.728 0z"></path>
                    </svg>
                </span>
            </div>
            <div><img src="../img/uid.png"> {{ store.homeUserInformation.id }}</div>
            <div><img src="../img/birthday.png"> {{ store.homeUserInformation?.birthday?.split(' ')[0] }}</div>
        </div>
        <!-- 单独渲染加载中部分 -->
        <div v-show="loadMore" class="class-loading">
            <img src="../img/loading-blue.gif">
            <span>拼命加载中...</span>
        </div>
        <!-- 使用 v-for 遍历排序后的数组来渲染其他 div -->
        <div class="home-div" v-for="(item, index) in divs" :key="index">
            <!-- 代表作部分 -->
            <div :class="item.class" v-if="item.class === 'magnum-video'&&!loadMore">
                <div class="title"><span>代表作</span></div>
                <videoList5 :data="magnumVideos"/>
            </div>
            <!-- 视频部分 -->
            <div :class="item.class" v-if="item.class === 'tribute-video'&&!loadMore">
                <div class="title">
                    <div class="title-name" @click="store.setHomeMenu(3,userId,true)">视频</div> · &nbsp;
                    <span class="contribute-total">{{ contributeTotal }}</span>
                    <div class="video-sort">
                        <span :class="{sortClass: sort === 1}" @click="sort = 1">最新发布</span>
                        <span :class="{sortClass: sort === 2}" @click="sort = 2">最多播放</span>
                        <span :class="{sortClass: sort === 3}" @click="sort = 3">最多收藏</span>   
                    </div>
                    <div class="more" @click="store.setHomeMenu(3,userId,true)">
                        查看更多
                        <svg  xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024">
                            <path fill="currentColor" d="M340.864 149.312a30.592 30.592 0 0 0 0 42.752L652.736 512 340.864 831.872a30.592 30.592 0 0 0 0 42.752 29.12 29.12 0 0 0 41.728 0L714.24 534.336a32 32 0 0 0 0-44.672L382.592 149.376a29.12 29.12 0 0 0-41.728 0z"></path>
                        </svg>
                    </div>
                </div>
                <videoList :data="contributeVideos"/>
            </div>
            <!-- 最近投币的视频部分 -->
            <div :class="item.class" v-if="item.class === 'throw-coin-video'&&!loadMore && ((store.userId!== null && store.userId === userId) || store.homeUserInformation.publicCoin === 1)">
                <div class="title">
                    <span>最近投币的视频</span>
                    <span v-if="store.userId===store.homeUserInformation.id&&store.homeUserInformation.publicCoin===0" class="home-user-info-public"><img src="../img/隐藏.png">仅自己可见</span> 
                </div>
                <videoList2 :data="throwCoinVideos" :sign="'throwCoin'"/>
            </div>
            <!-- 收藏夹部分 -->
            <div :class="item.class" v-if="item.class === 'collect'&&!loadMore && ((store.userId!== null && store.userId === userId) || store.homeUserInformation.publicCollect === 1)">
                <div class="title">
                    <div class="title-name" @click="store.setHomeMenu(5,userId,true)">收藏夹</div> · &nbsp;
                    <span class="contribute-total">{{ collectTotal }}</span>
                    <span v-if="store.userId===store.homeUserInformation.id&&store.homeUserInformation.publicCollect===0" class="home-user-info-public"><img src="../img/隐藏.png">仅自己可见</span> 
                    <div class="more" @click="store.setHomeMenu(5,userId,true)">
                        查看更多
                        <svg  xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024">
                            <path fill="currentColor" d="M340.864 149.312a30.592 30.592 0 0 0 0 42.752L652.736 512 340.864 831.872a30.592 30.592 0 0 0 0 42.752 29.12 29.12 0 0 0 41.728 0L714.24 534.336a32 32 0 0 0 0-44.672L382.592 149.376a29.12 29.12 0 0 0-41.728 0z"></path>
                        </svg>
                    </div>
                </div>
                <div class="collect-list">
                    <div class="video-item" v-for="collect in collects" :key="collect.id">
                        <div class="collect-back"></div>
                        <div class="collect-back2"></div>
                        <div class="content" @click="store.setHomeMenu(5,userId,true,collect.id)">
                            <img :src="collect.coverAddress?collect.coverAddress:'../img/collect_cover.avif'">
                            <svg v-if="collect.status === 0" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024">
                                <path fill="currentColor" d="M224 448a32 32 0 0 0-32 32v384a32 32 0 0 0 32 32h576a32 32 0 0 0 32-32V480a32 32 0 0 0-32-32zm0-64h576a96 96 0 0 1 96 96v384a96 96 0 0 1-96 96H224a96 96 0 0 1-96-96V480a96 96 0 0 1 96-96"></path>
                                <path fill="currentColor" d="M512 544a32 32 0 0 1 32 32v192a32 32 0 1 1-64 0V576a32 32 0 0 1 32-32m192-160v-64a192 192 0 1 0-384 0v64zM512 64a256 256 0 0 1 256 256v128H256V320A256 256 0 0 1 512 64"></path>
                            </svg>
                            <div class="video-number">{{ collect.videoNumber }}个视频</div> 
                            <div class="collect-info">
                                <div class="name">{{ collect.collectName }}</div>
                                <div class="status">{{ collect.status === 1?'公开':'私密' }}</div>
                            </div>
                        </div>
                    </div>
                </div>        
            </div>
            <!-- 订阅追番部分 -->
            <div  :class="item.class" v-if="item.class === 'follow-anime'&&!loadMore && ((store.userId!== null && store.userId === userId) || store.homeUserInformation.publicAnime === 1)">
                <div class="title">
                    <div class="title-name" @click="store.setHomeMenu(6,userId,true)">订阅追番</div> · &nbsp;
                    <span class="contribute-total">12</span>
                    <span v-if="store.userId===store.homeUserInformation.id&&store.homeUserInformation.publicAnime===0" class="home-user-info-public"><img src="../img/隐藏.png">仅自己可见</span> 
                </div>
                <followAnimeList :data="followAnimeData.AnimeData.slice(0,6)"/>
            </div>
            <!-- 最近点赞的视频部分 -->
            <div :class="item.class" v-if="item.class === 'love-video'&&!loadMore && ((store.userId!== null && store.userId === userId) || store.homeUserInformation.publicLove === 1)">
                <div class="title">
                    <span>最近点赞的视频</span>
                    <span v-if="store.userId===store.homeUserInformation.id&&store.homeUserInformation.publicLove===0" class="home-user-info-public"><img src="../img/隐藏.png">仅自己可见</span> 
                </div>
                <videoList2 :data="loveVideos" :sign="'love'"/>
            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref, watch } from 'vue';
import {useGlobalStore} from "@/store/store";
import videoList from '@/utils/videoList';
import videoList2 from '@/utils/videoList2';
import videoList5 from '@/utils/videoList5';
import followAnimeData from '@/utils/followAnimeData';
import followAnimeList from '@/utils/followAnimeList';
import {homeContributeVideos,homeThrowCoinVideos,getCollectsClassify,homeLoveVideos,homeMagnumVideos} from '@/api/home/index';

const userId = parseInt(new URL(window.location).searchParams.get("userId"))|| null;
const store = useGlobalStore();
const contributeVideos=reactive([]);
const sort=ref(1);
const contributeTotal=ref(0);
const throwCoinVideos=reactive([]);
const collects=reactive([]);
const collectTotal=ref(0);
const loveVideos=reactive([]);
const loadMore=ref(true);
const magnumVideos=reactive([]);
let onceFlag=true;

const divs = reactive([
    { class: 'magnum-video'},
    { class: 'tribute-video'},
    { class: 'throw-coin-video'},
    { class: 'collect'},
    { class: 'follow-anime'},
    { class: 'love-video'}
]);

onMounted(async()=>{
    
    getSort();
    let date=new Date();
    if(store.homeUserInformation.id)
    {
        homeMagnumVideosF();
        homeContributeVideosF();
       if((store.userId!==null&&store.userId===userId)||store.homeUserInformation.publicCoin===1)
        homeThrowCoinVideosF();
       if((store.userId!==null&&store.userId===userId)||store.homeUserInformation.publicCollect===1)
        homeCollectF();
       if((store.userId!==null&&store.userId===userId)||store.homeUserInformation.publicLove===1)
        await homeLoveVideosF();
         store.setHomeLoad(true,'homeBody');
    }
    let date2=new Date();
    setTimeout(()=>{
        loadMore.value=false;
    },date2 - date<300?300:date2 - date) 
})

onUnmounted(()=>{
    store.setHomeLoad(false,'homeBody');
})

watch([()=>store.homeUserInformation.id,()=>store.updateVideoInfoFlag],async()=>{

    if(onceFlag&&store.homeUserInformation.id){
        onceFlag=false;
       let date=new Date();
        homeMagnumVideosF();
        homeThrowCoinVideosF();
        homeCollectF();
        homeContributeVideosF();
       await homeLoveVideosF();
       let date2=new Date();
       setTimeout(()=>{
        store.setHomeLoad(true,'homeBody');
           loadMore.value=false;
       },date2 - date<300?300:date2 - date)
    }
    if(store.updateVideoInfoFlag)
    {
        const index=contributeVideos.findIndex(item=>item.videoId===store.updateVideoInfoFlag);
        if(index!==-1)
        contributeVideos[index].waitWatch=contributeVideos[index].waitWatch===0?1:0;
        const index2=throwCoinVideos.findIndex(item=>item.videoId===store.updateVideoInfoFlag);
        if(index2!==-1)
        throwCoinVideos[index2].waitWatch=throwCoinVideos[index2].waitWatch===0?1:0;
        const index3=loveVideos.findIndex(item=>item.videoId===store.updateVideoInfoFlag);
        if(index3!==-1)
        loveVideos[index3].waitWatch=loveVideos[index3].waitWatch===0?1:0;
        const index4=magnumVideos.findIndex(item=>item.videoId===store.updateVideoInfoFlag);
        if(index4!==-1)
        magnumVideos[index4].waitWatch=magnumVideos[index4].waitWatch===0?1:0;
        store.setUpdateVideoInfoFlag(false);
    }

})

// 获取用户主页投稿的视频
async function homeContributeVideosF(){

    homeContributeVideos(store.homeUserInformation.id,store.userId,sort.value,1).then(res=>{
            if(res.data.code===1){
                contributeVideos.length=0;
                Object.assign(contributeVideos,res.data.data.records.slice(0,10));
                contributeTotal.value=res.data.data.total;
            }
    });
}

// 获取用户最近投币的视频
async function homeThrowCoinVideosF(){
    
    homeThrowCoinVideos(store.userId,store.homeUserInformation.id).then(res=>{
            if(res.data.code===1){
                throwCoinVideos.length=0;
                Object.assign(throwCoinVideos,res.data.data);
            }
    });
}

// 获取用户收藏夹的视频
async function homeCollectF(){
    
    getCollectsClassify(store.homeUserInformation.id,store.userId,store.token).then(res=>{
            if(res.data.code===1){
                collects.length=0;
                Object.assign(collects,res.data.data.slice(0,10));
                collectTotal.value=res.data.data.length;
            }
    });
}

// 获取用户最近喜欢的视频
async function homeLoveVideosF(){
    
    homeLoveVideos(store.userId,store.homeUserInformation.id).then(res=>{
            if(res.data.code===1){
                loveVideos.length=0;
                Object.assign(loveVideos,res.data.data);
            }
    });
}

watch(sort,()=>{
    homeContributeVideosF();
})

//查询代表作视频
function homeMagnumVideosF(){

    homeMagnumVideos(userId,store.userId).then(res=>{
        if(res.data.code===1){
            Object.assign(magnumVideos,res.data.data);
        }
    })

}

//获取排序
function getSort(){
    const homeSort=JSON.parse(localStorage.getItem('homeSort'));
    if(Array.isArray(homeSort)&&homeSort.length===6){
        divs.length=0;
        Object.assign(divs,homeSort);
    }
}

//打开个人中心
function openAccount(){
   
    window.open('./account','_blank');

}

</script>

<style lang="scss" scoped>
*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.home{
    width: 1090px;
    top: 24px;
    padding-left: 60px;
    padding-bottom: 25px;
    .home-user-info{
    position: absolute;
    width: 240px;
    height: 125px;
    right: 85px;
    top: 30px;
    padding: 20px 16px 24px;
    background-color: #F6F7F8;
    border-radius: 6px;

    div:nth-child(1){
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #18191C;
    font-size: 16px;
    font-weight: 500;
        span{
        color: #61666D;
        font-size: 12px;
        transition: color 0.3s ease;
        cursor: pointer;
        svg{
            position: relative;
            top: 2px;
            width: 12px;
            height: 12px;
            
        }
        }
        span:hover{
            color: #00AEEC;
        }
    }
    div:nth-child(2){
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        font-size: 13px;
    }
    div:nth-child(3){
        display: flex;
        align-items: center;
        font-size: 13px;
    }
    div{
        img{
            width: 19px;
            height: 16px;
            margin-right: 8px;
        }
    }
    }

    .class-loading{
    position: relative;
    margin-top: 90px;
    display: flex;
    justify-content: center;

    img{
        width: 30px;
        height: 30px;
    }
    span{
        position: relative;
        top: 5px;
        left: 5px;
        font-size: 14px;
        color: #61666D;
    }
    }

    .home-div{
        .magnum-video{
        position: relative;
        top: 35px;
        width: 100%;
        margin-bottom: 34.5px;
        padding-bottom: 24px;
        border-bottom: 1px solid #E3E5E7;
            .title{
            span{
                cursor: pointer;
                color: #18191C;
                font-size: 24px;
                font-weight: 600;
                transition: color 0.3s ease;
                margin-right: 5px;
            }
            span:hover{
                color: #00AEEC;
            }
            }
        }
        .tribute-video{
        position: relative;
        top: 24px;
        width: 100%;
        padding-bottom: 24px;
        margin-bottom: 24px;
        border-bottom: 1px solid #E3E5E7;
            .title{
            .title-name{
                cursor: pointer;
                color: #18191C;
                font-size: 24px;
                font-weight: 600;
                transition: color 0.3s ease;
                margin-right: 5px;
            }
            .title-name:hover{
                color: #00AEEC;
            }
            .contribute-total{
                margin-top: 2px;
            }
            }
            .video-sort{
            position: relative;
            left: 40px;
            top: 0px;   
            display: flex;
            font-size: 14px;
            column-gap: 12px;
            row-gap: 12px;
            flex-wrap: wrap;
            user-select: none;
                span{
                display: flex;
                align-items: center;
                height: 34px;
                padding: 0 15px;
                flex-shrink: 0;
                color: #61666D;
                cursor: pointer;
                transition: all .3s;
                border-radius: 6px;
                background-color: #F6F7F8;
                }
                span:hover{
                    color: #00AEEC;
                }
                .sortClass{
                    color: white !important;
                    background-color: #00AEEC !important;
                }
            }
            .more{
                    font-weight: normal;
                    position: absolute;
                    width: 100px;
                    height: 34px;
                    cursor: pointer;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    padding: 0 12px;
                    right: 0px;
                    top: 0;
                    font-size: 14px;
                    color: #18191C;
                    background: white;
                    border-radius: 8px;
                    transition: all .3s;
                    user-select: none;
                    border: 1px solid #E3E5E7;
                    svg{
                        width: 14px;
                        height: 14px;
                    }
            }
            .more:hover{
                background-color: #E3E5E7;
                border: #E3E5E7;
            }
        }

        .throw-coin-video{
        position: relative;
        top: 24px;
        width: 100%;
        margin-bottom: 24px;
        padding-bottom: 24px;
        border-bottom: 1px solid #E3E5E7;
            .title{
            span{
                cursor: pointer;
                color: #18191C;
                font-size: 24px;
                font-weight: 600;
                transition: color 0.3s ease;
                margin-right: 5px;
            }
            span:hover{
                color: #00AEEC;
            }
            }
            .video-sort{
            position: relative;
            left: 40px;
            top: 0px;   
            display: flex;
            font-size: 14px;
            column-gap: 12px;
            row-gap: 12px;
            flex-wrap: wrap;
            user-select: none;
                span{
                display: flex;
                align-items: center;
                height: 34px;
                padding: 0 15px;
                flex-shrink: 0;
                color: #61666D;
                cursor: pointer;
                transition: all .3s;
                border-radius: 6px;
                background-color: #F6F7F8;
                }
                span:hover{
                    color: #00AEEC;
                }
                .sortClass{
                    color: white !important;
                    background-color: #00AEEC !important;
                }
            }
            .more{
                    font-weight: normal;
                    position: absolute;
                    width: 100px;
                    height: 34px;
                    cursor: pointer;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    padding: 0 12px;
                    right: 0px;
                    top: 0;
                    font-size: 14px;
                    color: #18191C;
                    background: white;
                    border-radius: 8px;
                    transition: all .3s;
                    user-select: none;
                    border: 1px solid #E3E5E7;
                    svg{
                        width: 14px;
                        height: 14px;
                    }
            }
            .more:hover{
                background-color: #E3E5E7;
                border: #E3E5E7;
            }
        }

        .collect{
        position: relative;
        top: 24px;
        width: 100%;
        padding-bottom: 24px;
        margin-bottom: 24px;
        border-bottom: 1px solid #E3E5E7;
            .title{   
            .title-name{
                cursor: pointer;
                color: #18191C;
                font-size: 24px;
                font-weight: 600;
                transition: color 0.3s ease;
                margin-right: 5px;
            }
            .title-name:hover{
                color: #00AEEC;
            }
            .contribute-total{
                margin-top: 2px;
            }
            }
            .video-sort{
            position: relative;
            left: 40px;
            top: 0px;   
            display: flex;
            font-size: 14px;
            column-gap: 12px;
            row-gap: 12px;
            flex-wrap: wrap;
            user-select: none;
                span{
                display: flex;
                align-items: center;
                height: 34px;
                padding: 0 15px;
                flex-shrink: 0;
                color: #61666D;
                cursor: pointer;
                transition: all .3s;
                border-radius: 6px;
                background-color: #F6F7F8;
                }
                span:hover{
                    color: #00AEEC;
                }
                .sortClass{
                    color: white !important;
                    background-color: #00AEEC !important;
                }
            }
            .more{
                    font-weight: normal;
                    position: absolute;
                    width: 100px;
                    height: 34px;
                    cursor: pointer;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    padding: 0 12px;
                    right: 0px;
                    top: 0;
                    font-size: 14px;
                    color: #18191C;
                    background: white;
                    border-radius: 8px;
                    transition: all .3s;
                    user-select: none;
                    border: 1px solid #E3E5E7;
                    svg{
                        width: 14px;
                        height: 14px;
                    }
            }
            .more:hover{
                background-color: #E3E5E7;
                border: #E3E5E7;
            }
            .collect-list{
            column-gap: 16px;
            row-gap: 23px;
            display: grid;
            grid-template-columns: repeat(5,1fr);
                .video-item{
                    cursor: pointer;
                    position: relative;
                    top: 21px;
                    width: 193px;
                    height: 180px;

                    .collect-back{
                        position: absolute;
                        background: #F6F7F8;
                        width: 80%;
                        height: 16px;
                        border-radius: 6px;
                        top: -16px;
                        left: 0;
                        right: 0;
                        margin: auto;
                        z-index: -10;
                    }
                    .collect-back2{
                        position: absolute;
                        background: #F1F2F3;
                        width: 90%;
                        height: 16px;
                        border-radius: 6px;
                        top: -8px;
                        left: 0;
                        right: 0;
                        margin: auto;
                        z-index: -5;
                    }
                    .content{
                        img{
                        width: 100%;
                        height: 110px;
                        background-color: #E3E5E7;
                        border-radius: 6px;
                        object-fit: cover;
                        }
                        svg{
                            position: absolute;
                            width: 18px;
                            height: 16px;
                            bottom: 74px;
                            color: white;
                            z-index: 100;
                            left: 6px;
                        }
                        .video-number{
                            position: absolute;
                            bottom: 70px;
                            left: 0;
                            box-sizing: border-box;
                            padding: 14px 8px 6px;
                            width: 100%;
                            height: 34px;
                            z-index: 10;
                            border-bottom-right-radius: 6px;
                            border-bottom-left-radius: 6px;
                            background-image: linear-gradient(180deg, #0000, #000c);
                            color: #fff;
                            font-size: 11.5px;
                            opacity: 1;
                            display: -webkit-flex;
                            display: flex;
                            align-items: center;
                            justify-content: right;
                            pointer-events: none;
                        }
                        .collect-info{
                        cursor: pointer;
                        max-width: 100%;
                        margin-top: 10px;
                        font-weight: 500;
                        font-size: 16px;
                        line-height: 1.5;
                        color: #18191C;

                        .name{
                            font-size: 15.5px;
                            display: -webkit-box;
                            -webkit-line-clamp: 1;
                            -webkit-box-orient: vertical;
                            overflow: hidden;
                            transition: color 0.3s ease;
                        }
                        .name:hover{
                            color: #00AEEC;
                        }
                        .status{
                            font-size: 11.5px;
                            color: #9499A0;
                            margin-top: 4px;
                        }
                        }
                    }
                    
                }
            }
        }

        .follow-anime{
        position: relative;
        top: 24px;
        width: 100%;
        padding-bottom: 24px;
        margin-bottom: 24px;
        border-bottom: 1px solid #E3E5E7;
            .title{
            .title-name{
                cursor: pointer;
                color: #18191C;
                font-size: 24px;
                font-weight: 600;
                transition: color 0.3s ease;
                margin-right: 5px;
            }
            .title-name:hover{
                color: #00AEEC;
            }
            .contribute-total{
                margin-top: 2px;
            }
            }
            .video-sort{
            position: relative;
            left: 40px;
            top: 0px;   
            display: flex;
            font-size: 14px;
            column-gap: 12px;
            row-gap: 12px;
            flex-wrap: wrap;
            user-select: none;
                span{
                display: flex;
                align-items: center;
                height: 34px;
                padding: 0 15px;
                flex-shrink: 0;
                color: #61666D;
                cursor: pointer;
                transition: all .3s;
                border-radius: 6px;
                background-color: #F6F7F8;
                }
                span:hover{
                    color: #00AEEC;
                }
                .sortClass{
                    color: white !important;
                    background-color: #00AEEC !important;
                }
            }
            .more{
                    font-weight: normal;
                    position: absolute;
                    width: 100px;
                    height: 34px;
                    cursor: pointer;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    padding: 0 12px;
                    right: 0px;
                    top: 0;
                    font-size: 14px;
                    color: #18191C;
                    background: white;
                    border-radius: 8px;
                    transition: all .3s;
                    user-select: none;
                    border: 1px solid #E3E5E7;
                    svg{
                        width: 14px;
                        height: 14px;
                    }
            }
            .more:hover{
                background-color: #E3E5E7;
                border: #E3E5E7;
            }
            .video-list{
            column-gap: 16px;
            row-gap: 23px;
            display: grid;
            grid-template-columns: repeat(5,1fr);
                .video-item{
                    height: 173.5px;
                }
                .no-data{
                    user-select: none;
                    width: 1090px;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    flex-direction: column;  
                    img{
                        width: 140px;
                        height: 140px;
                    }
                    div{
                        margin-top: 6px;
                        font-size: 14px;
                        line-height: 20px;
                        font-weight: 400;
                        color: #9499A0;
                        
                    }
                }    
            }
        }

        .love-video{
        position: relative;
        top: 24px;
        width: 100%;
        margin-bottom: 24px;
        padding-bottom: 24px;
        border-bottom: 1px solid #E3E5E7;
            .title{
            span{
                cursor: pointer;
                color: #18191C;
                font-size: 24px;
                font-weight: 600;
                transition: color 0.3s ease;
                margin-right: 5px;
            }
            span:hover{
                color: #00AEEC;
            }
            }
            .video-sort{
            position: relative;
            left: 40px;
            top: 0px;   
            display: flex;
            font-size: 14px;
            column-gap: 12px;
            row-gap: 12px;
            flex-wrap: wrap;
            user-select: none;
                span{
                display: flex;
                align-items: center;
                height: 34px;
                padding: 0 15px;
                flex-shrink: 0;
                color: #61666D;
                cursor: pointer;
                transition: all .3s;
                border-radius: 6px;
                background-color: #F6F7F8;
                }
                span:hover{
                    color: #00AEEC;
                }
                .sortClass{
                    color: white !important;
                    background-color: #00AEEC !important;
                }
            }
            .more{
                    font-weight: normal;
                    position: absolute;
                    width: 100px;
                    height: 34px;
                    cursor: pointer;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    padding: 0 12px;
                    right: 0px;
                    top: 0;
                    font-size: 14px;
                    color: #18191C;
                    background: white;
                    border-radius: 8px;
                    transition: all .3s;
                    user-select: none;
                    border: 1px solid #E3E5E7;
                    svg{
                        width: 14px;
                        height: 14px;
                    }
            }
            .more:hover{
                background-color: #E3E5E7;
                border: #E3E5E7;
            }
                .video-list{
                column-gap: 16px;
                row-gap: 23px;
                display: grid;
                grid-template-columns: repeat(5,1fr);
                .video-item{
                    height: 173.5px;
                }
                .no-data{
                        user-select: none;
                        width: 1090px;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        flex-direction: column;  
                        img{
                            width: 140px;
                            height: 140px;
                        }
                        div{
                            margin-top: 6px;
                            font-size: 14px;
                            line-height: 20px;
                            font-weight: 400;
                            color: #9499A0;
                            
                        }
                }       
            }
        }
    }
    
}

.home-user-info-public{
    font-size: 12px !important;
    margin-left: 16px !important;
    color: #9499A0 !important;
    display: flex;
    font-weight: normal !important;
    align-items: center;

    img{
        margin-right: 4px;
        width: 16px;
        height: 13px;
    }
}
.title{
    user-select: none;
    display: flex;
    align-items: center;
    margin-bottom: 15px;
}

</style>