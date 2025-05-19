<template>
    <div class="home-aside">
        <div class="home-aside-logo">
           <div class="left">
               <div class="left-part1" >
                <span @click="changeHomeMenu(1)" style="margin-left: 0px;">
                    <img src="../img/主页.png">
                    <span :style="{fontWeight: store.homeMenu===1? 'bold' : 'normal', color: store.homeMenu===1? '#00AEEC' : '#18191C'}">主页</span>
                </span>
                <span @click="changeHomeMenu(2)">
                    <img src="../img/主页动态.png">
                    <span :style="{fontWeight: store.homeMenu===2? 'bold' : 'normal', color: store.homeMenu===2? '#00AEEC' : '#18191C'}">动态</span>
                </span>
                <span @click="changeHomeMenu(3)">
                    <img src="../img/主页投稿.png">
                    <span :style="{fontWeight: store.homeMenu===3? 'bold' : 'normal', color: store.homeMenu===3? '#00AEEC' : '#18191C'}">投稿</span>
                    <span class="num" :style="{color: store.homeMenu===3? '#00AEEC':'#18191C' }">{{ contributeTotal }}</span>
                </span>
                <span @click="changeHomeMenu(4)">
                    <img src="../img/主页合集.png">
                    <span :style="{fontWeight: store.homeMenu===4? 'bold' : 'normal', color: store.homeMenu===4? '#00AEEC' : '#18191C'}">合集</span>
                </span>
                <span v-if="(store.userId!==null&&store.userId===userId)||store.homeUserInformation.publicCollect===1" @click="changeHomeMenu(5)">
                    <img src="../img/主页收藏.png">
                    <span :style="{fontWeight: store.homeMenu===5? 'bold' : 'normal', color: store.homeMenu===5? '#00AEEC' : '#18191C'}">收藏</span>
                    <span class="num" :style="{color: store.homeMenu===5? '#00AEEC':'#18191C' }">{{ store.collectClassifyNumber }}</span>
                </span>
                <span v-if="(store.userId!==null&&store.userId===userId)||store.homeUserInformation.publicAnime===1" @click="changeHomeMenu(6)">
                    <img src="../img/主页追番追剧.png">
                    <span :style="{fontWeight: store.homeMenu===6? 'bold' : 'normal', color: store.homeMenu===6? '#00AEEC' : '#18191C'}">追番追剧</span>
                </span>
                <span v-if="store.userId!==null&&store.userId===userId" @click="changeHomeMenu(7)">
                    <img src="../img/主页设置.png">
                    <span :style="{fontWeight: store.homeMenu===7? 'bold' : 'normal', color: store.homeMenu==7? '#00AEEC' : '#18191C'}">设置</span>
                </span>
               </div>
               <div class="line" :style="{transform: 'translateX('+menuLinePositionX[store.homeMenu-1]+'px)', width: menuLineWidth[store.homeMenu-1]+'px'}"></div>
               <div class="search-box">
                    <input class="left-part2" type="text" ref="searchInput" placeholder="搜索视频、动态" maxlength="50" v-model="searchContent" @keydown.enter="searchJumpPage" @blur="searchInputFoucsFlag=false">
                    <img
                    v-show="searchContent.length>0"
                    @mouseover="deleteAllSearchFlag = false"
                    @mouseleave="deleteAllSearchFlag = true"
                    @click="searchContent = '',searchInputFoucsFlag=true"
                    :src="deleteAllSearchFlag ? '../img/删除搜索记录.png' : '../img/删除全部搜索hover.png'"
                    class="deleteAllSearchImg"/>
                    <img class="searchImg" :class="{searchImgHoverFlag: !searchImgHoverFlag}"  src="../img/主页搜索.png">         
                    <img class="searchImg2" @click="searchJumpPage" :class="{searchImgHoverFlag: searchImgHoverFlag}" @mouseover="searchImgHoverFlag=true" @mouseleave="searchImgHoverFlag=false" src="../img/主页搜索蓝.png">         
               </div>
           </div>
           <div class="right">
              <div class="user-middle-info">
                  <span @click="changeHomeMenu(8)">
                    <span :style="{color: store.homeMenu===8? '#00AEEC' : '#18191C'}">关注数</span>
                    <span :style="{color: store.homeMenu===8? '#00AEEC' : '#18191C'}">{{ store?.homeUserInformation?.followNumber }}</span>
                  </span>
                  <span @click="changeHomeMenu(9)">
                    <span :style="{color: store.homeMenu===9? '#00AEEC' : '#18191C'}">粉丝数</span>
                    <span :style="{color: store.homeMenu===9? '#00AEEC' : '#18191C'}">{{ store?.homeUserInformation?.fansNumber }}</span>
                  </span>
                  <div>
                    <span>获赞数</span>
                    <span>{{ store?.homeUserInformation?.likeNumber }}</span>
                  </div>
                  <div>
                    <span>播放数</span>
                    <span>{{ store?.homeUserInformation?.playNumber }}</span>
                  </div>
              </div>
           </div>
        </div>
    </div>
</template>

<script setup>
import { nextTick, onMounted, ref, watch } from 'vue';
import {useGlobalStore} from "../store/store";
import {homeContributeVideos} from '@/api/home/index';
import { ElMessage } from "element-plus";

const store = useGlobalStore();
const userId = parseInt(new URL(window.location).searchParams.get("userId"))|| null;
const searchContent = ref('');
const deleteAllSearchFlag = ref(true);
const searchInput = ref(null);
const searchInputFoucsFlag = ref(false);
const searchImgHoverFlag = ref(false);
const contributeTotal = ref(0);
const menuLinePositionX = ref([
    0,86,172,269,355,452,569
]);
const menuLineWidth = ref([
    56,56,66,56,66,87,56
]);

watch(searchInputFoucsFlag,()=>{
    if(searchInputFoucsFlag.value)
    {
        nextTick(() => {
            searchInput.value.focus();
        });
    }else{
        searchInput.value.blur();
    }
})

//检查链接是否是当前页面
function changeHomeMenu(homeMenu){
    
    if(store.userId!==store.homeUserInformation.id)
    {
        if(homeMenu===8&&!store.homeUserInformation.publicFollowList)
        {
            ElMessage({
            message: "由于该用户隐私设置，关注列表不可见",
            type: "info",
            plain: true,
            duration: 1700,
            });
            return;
        }
        else if(homeMenu===9&&!store.homeUserInformation.publicFansList)
        {
            ElMessage({
            message: "由于该用户隐私设置，粉丝列表不可见",
            type: "info",
            plain: true,
            duration: 1700,
            });
            return;
        }
    }
    if(store.homeMenu!==homeMenu)
      store.setHomeMenu(homeMenu,userId,true);
}

onMounted(async()=>{
   await homeContributeVideosF();
   store.setHomeLoad(true,'homeAside');

})

// 获取用户主页投稿的视频
async function homeContributeVideosF(){

homeContributeVideos(userId,store.userId,1,1).then(res=>{
        if(res.data.code===1){
            contributeTotal.value=res.data.data.total;
        }
});
}

function searchJumpPage(){
    
    const searchUrl = new URL(window.location);
    const keyword = searchUrl.searchParams.get("keyword", searchContent.value);
    if((store.keyword.length===0&&keyword)||(keyword!==store.keyword&&keyword))
        store.setKeyword(keyword);
    if(searchContent.value.length>0&&keyword!==searchContent.value)
    {    
        store.setKeyword(searchContent.value);
        store.setHomeMenu(10,userId,true);
    }
     
}

watch(searchContent,()=>{
    if(searchContent.value.length===0&&store.homeMenu===10)
    {
        window.history.back();
    }
})

watch(()=>store.homeMenu,()=>{
    if(store.homeMenu!==10)
    {
        searchContent.value='';
        store.setKeyword('');
        searchInputFoucsFlag.value=false;
    }
    else{
        searchContent.value=store.keyword;
        searchInputFoucsFlag.value=true;
    }
})



</script>

<style lang="scss" scoped>
*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.home-aside{
    position: sticky;
    top: 0;
    width: 100%;
    height: 64px;
    z-index: 5;
    background-color: white;
    box-shadow: 0 0 0 1px #F1F2F3;

    .home-aside-logo{
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 0 auto;
    padding: 0 60px;

    .left{
    flex-shrink: 0;
    display: flex;
    align-items: center;
    margin-right: 15px;

    .left-part1{
    position: relative;
    left: 1px;
    display: flex;
    align-items: center;

        span{
        user-select: none;    
        display: flex;
        align-items: center;
        justify-content: center;
        height: 64px;
        margin-left: 33px;
        cursor: pointer;

        img{
            width: 18px;
            height: 17px;
        }
        span{
            color: #18191C;
            font-size: 15px;
            margin-left: 5px;
            transition: color 0.3s ease;
        }
        .num{
          color: #61666D;
          font-size: 12px;
        }
        }
        span:hover{
            span{
                color: #00AEEC !important;
            }
        }
        .tab-line{
        position: absolute;
        width: 56px;
        height: 3px;
        left: -1px;
        bottom: 0px;
        height: 3px;
        border-radius: 3px;
        background-color: #00AEEC;
        transition: all .3s;
        }
    }
    .line{
    position: absolute;
    transition: transform .3s ease;    
    height: 3px;
    bottom: 0px;
    border-radius: 3px;
    background-color: #00AEEC;    
    }
    .search-box{
    position: relative;
    left: 30px;
    width: 150px;
    height: 30px;
    user-select: none;
    .left-part2{
    width: 150px;
    height: 30px;
    padding: 0 45px 0 10px;
    background-color: transparent;
    line-height: 16px;
    font-size: 11.5px;
    color: #18191C;
    border-radius: 6px;
    border: 1px solid #E3E5E7;
    transition: border-color .3s;
    outline: none;
    }
    .left-part2:focus{
    border-color: #00AEEC;
    }

    .deleteAllSearchImg{
    position: absolute;
    width: 13.5px;
    top: 50%;
    transform: translateY(-50%);
    right: 30px;
    cursor: pointer;
    }

    .searchImg{
    width: 13px;
    height: 13.5px;
    position: absolute;
    cursor: pointer;
    top: 50%;
    transform: translateY(-50%);
    right: 10px;
    transition: opacity 0.3s ease;
    }

    .searchImg2{
    width: 13px;
    height: 13.5px;
    position: absolute;
    cursor: pointer;
    top: 50%;
    transform: translateY(-50%);
    opacity: 0;
    right: 10px;
    transition: opacity 0.3s ease;
    }

    .searchImgHoverFlag{
    opacity: 1;
    }
    }
    }
    .right{
        width: 256px;
        flex-shrink: 0;
        margin-right: 24px;
        .user-middle-info{
            display: flex;
            justify-content: space-between;
            align-items: center;

            span{
                display: flex;
                flex-direction: column;
                align-items: center;
                min-width: 52px;
                
                span:nth-child(1){
                    color: #61666D;
                    font-size: 12.5px;
                    line-height: 18px;
                    cursor: pointer;
                }
                span:nth-child(2){
                    font-size: 14px;
                    line-height: 20px;
                    margin-top: 2px;
                    font-weight: 500;
                    color: #18191C;
                    cursor: pointer;
                }
                span{
                    transition: color 0.3s ease;
                }
            }
            span:hover{
                span{
                    color: #00AEEC !important;
                }
            }
            div{
                display: flex;
                flex-direction: column;
                align-items: center;
                min-width: 52px;
                margin-left: 0px;

                span:nth-child(1){
                    color: #61666D;
                    font-size: 12.5px;
                    line-height: 18px;
                }
                span:nth-child(2){
                    font-size: 14px;
                    line-height: 20px;
                    margin-top: 2px;
                    font-weight: 500;
                    color: #18191C;
                }
            }
        }
    }
    }
}

</style>