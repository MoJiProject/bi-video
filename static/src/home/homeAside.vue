<template>
    <nav class="home-aside" aria-label="个人空间导航">
        <div class="home-aside-logo">
           <div class="left">
               <div class="left-part1" >
                <span :ref="el => tabEls[0] = el" :class="{ 'is-on': store.homeMenu === 1 }" role="button" tabindex="0" :aria-pressed="store.homeMenu === 1" @keydown.enter.prevent="changeHomeMenu(1)" @keydown.space.prevent="changeHomeMenu(1)" @click="changeHomeMenu(1)">
                    <img src="/img/主页.png">
                    <span>主页</span>
                </span>
                <span :ref="el => tabEls[1] = el" :class="{ 'is-on': store.homeMenu === 2 }" role="button" tabindex="0" :aria-pressed="store.homeMenu === 2" @keydown.enter.prevent="changeHomeMenu(2)" @keydown.space.prevent="changeHomeMenu(2)" @click="changeHomeMenu(2)">
                    <img src="/img/主页动态.png">
                    <span>动态</span>
                </span>
                <span :ref="el => tabEls[2] = el" :class="{ 'is-on': store.homeMenu === 3 }" role="button" tabindex="0" :aria-pressed="store.homeMenu === 3" @keydown.enter.prevent="changeHomeMenu(3)" @keydown.space.prevent="changeHomeMenu(3)" @click="changeHomeMenu(3)">
                    <img src="/img/主页投稿.png">
                    <span>投稿</span>
                    <span class="num" v-if="contributeTotal">{{ contributeTotal }}</span>
                </span>
                <span v-if="(store.userId!==null&&store.userId===userId)||store.homeUserInformation.publicCollect===1" :ref="el => tabEls[4] = el" :class="{ 'is-on': store.homeMenu === 5 }" role="button" tabindex="0" :aria-pressed="store.homeMenu === 5" @keydown.enter.prevent="changeHomeMenu(5)" @keydown.space.prevent="changeHomeMenu(5)" @click="changeHomeMenu(5)">
                    <img src="/img/主页收藏.png">
                    <span>收藏</span>
                    <span class="num" v-if="store.collectClassifyNumber">{{ store.collectClassifyNumber }}</span>
                </span>
                <span v-if="store.homeUserInformation.publicAnime===1" :ref="el => tabEls[5] = el" :class="{ 'is-on': store.homeMenu === 6 }" role="button" tabindex="0" :aria-pressed="store.homeMenu === 6" @keydown.enter.prevent="changeHomeMenu(6)" @keydown.space.prevent="changeHomeMenu(6)" @click="changeHomeMenu(6)">
                    <img src="/img/主页追剧.png">
                    <span>追剧</span>
                </span>
                <span v-if="store.userId!==null&&store.userId===userId" :ref="el => tabEls[6] = el" :class="{ 'is-on': store.homeMenu === 7 }" role="button" tabindex="0" :aria-pressed="store.homeMenu === 7" @keydown.enter.prevent="changeHomeMenu(7)" @keydown.space.prevent="changeHomeMenu(7)" @click="changeHomeMenu(7)">
                    <img src="/img/主页设置.png">
                    <span>设置</span>
                </span>
                <div
                  v-show="tabLine.visible"
                  class="line"
                  :style="{ transform: 'translateX('+tabLine.x+'px)', width: tabLine.w+'px' }"
                ></div>
               </div>
               <div class="search-box">
                    <input class="left-part2" type="text" ref="searchInput" placeholder="搜索视频、动态" aria-label="搜索空间内的视频和动态" maxlength="50" v-model="searchContent" @keydown.enter="searchJumpPage" @blur="searchInputFoucsFlag=false">
                    <button v-show="searchContent.length > 0" type="button" class="space-search-action search-clear"
                      aria-label="清空搜索内容" title="清空搜索内容" @mousedown.prevent
                      @click="searchContent = '',searchInputFoucsFlag=true">
                      <svg viewBox="0 0 24 24" aria-hidden="true"><path d="m7 7 10 10M17 7 7 17"/></svg>
                    </button>
                    <button type="button" class="space-search-action search-submit" aria-label="搜索" title="搜索"
                      @mousedown.prevent @click="searchJumpPage">
                      <svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="10.5" cy="10.5" r="6.5"/><path d="m15.5 15.5 4.5 4.5"/></svg>
                    </button>
               </div>
           </div>
           <div class="right">
              <div class="user-middle-info">
                  <span role="button" tabindex="0" :aria-pressed="store.homeMenu===8" @keydown.enter.prevent="changeHomeMenu(8)" @keydown.space.prevent="changeHomeMenu(8)" @click="changeHomeMenu(8)">
                    <span :style="{color: store.homeMenu===8? 'var(--brand)' : 'var(--ink-2)'}">关注数</span>
                    <span :style="{color: store.homeMenu===8? 'var(--brand)' : 'var(--ink)'}">{{ store?.homeUserInformation?.followNumber }}</span>
                  </span>
                  <span role="button" tabindex="0" :aria-pressed="store.homeMenu===9" @keydown.enter.prevent="changeHomeMenu(9)" @keydown.space.prevent="changeHomeMenu(9)" @click="changeHomeMenu(9)">
                    <span :style="{color: store.homeMenu===9? 'var(--brand)' : 'var(--ink-2)'}">粉丝数</span>
                    <span :style="{color: store.homeMenu===9? 'var(--brand)' : 'var(--ink)'}">{{ store?.homeUserInformation?.fansNumber }}</span>
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
    </nav>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue';
import {useGlobalStore} from "../store/store";
import {homeContributeVideos} from '@/api/home/index';
import { ElMessage } from "element-plus";

const store = useGlobalStore();
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || store.userId;
const searchContent = ref('');
const searchInput = ref(null);
const searchInputFoucsFlag = ref(false);
const contributeTotal = ref(0);

const tabEls = ref([]);
const tabLine = reactive({ visible: false, x: 0, w: 0 });

const TABS_WITHOUT_LINE = [8, 9, 10];

function measureTabLine() {
  const index = store.homeMenu - 1;
  if (TABS_WITHOUT_LINE.includes(store.homeMenu) || index < 0 || index > 6) {
    tabLine.visible = false;
    return;
  }
  const el = tabEls.value[index];
  if (!el) {
    tabLine.visible = false;
    return;
  }
  tabLine.x = el.offsetLeft;
  tabLine.w = el.offsetWidth;
  tabLine.visible = true;
}

watch(() => store.homeMenu, () => nextTick(measureTabLine));
watch(
  () => [store.collectClassifyNumber, store.homeUserInformation.publicCollect, store.homeUserInformation.publicAnime],
  () => nextTick(measureTabLine)
);

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
            type: "warning",
            duration: 1700,
            });
            return;
        }
        else if(homeMenu===9&&!store.homeUserInformation.publicFansList)
        {
            ElMessage({
            message: "由于该用户隐私设置，粉丝列表不可见",
            type: "warning",
            duration: 1700,
            });
            return;
        }
    }
    if(store.homeMenu!==homeMenu)
      store.setHomeMenu(homeMenu,userId,true);
}

onMounted(async()=>{
   window.addEventListener('resize', measureTabLine);
   try {
     await homeContributeVideosF();
   } catch (error) {
     ElMessage.error(error.message);
   } finally {
     store.setHomeLoad(true,'homeAside');
   }
   await nextTick();
   measureTabLine();
})

onUnmounted(() => window.removeEventListener('resize', measureTabLine));

// 获取用户主页投稿的视频
async function homeContributeVideosF(){

await homeContributeVideos(userId,store.userId,1,1).then(res=>{
        if(res.data.code===1){
            contributeTotal.value=res.data.data.total;
        }
});
}

function searchJumpPage(){
    const keyword = searchContent.value.trim();
    if(keyword.length > 50) {
        ElMessage.info('搜索关键词不能超过 50 个字符');
        return;
    }
    if(keyword.length > 0)
    {
        store.setKeyword(keyword);
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
    min-height: 64px;
    z-index: 5;
    background-color: white;
    box-shadow: 0 0 0 1px #EFF3F2;

    .home-aside-logo{
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 0 auto;
    padding: 0 var(--page-pad);

    .left{
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    margin-right: 15px;

    .left-part1{
    position: relative;
    min-width: 0;
    display: flex;
    align-items: center;
    overflow-x: auto;

        > span{
        flex: none;
        user-select: none;    
        display: flex;
        align-items: center;
        justify-content: center;
        height: 64px;
        margin-right: 24px;
        cursor: pointer;

        img{
            width: 18px;
            height: 17px;
        }
        span{
            color: #1C2321;
            font-size: 15px;
            margin-left: 5px;
            margin-right: 0;
            transition: color 0.3s ease;
        }
        .num{
          color: #5C6664;
          font-size: 12px;
        }
        }
        span:hover{
            span{
                color: #0FA68E;
            }
        }
        span.is-on{
            font-weight: bold;
            span{
                color: #0FA68E;
            }
            .num{
                color: #0FA68E;
            }
        }
    }
    .line{
    position: absolute;
    transition: transform .3s ease, width .3s ease;
    height: 3px;
    bottom: 0px;
    border-radius: 3px;
    background-color: #0FA68E;    
    }
    .search-box{
    position: relative;
    flex: none;
    margin-left: 16px;
    width: 150px;
    height: 30px;
    user-select: none;
    .left-part2{
    width: 150px;
    height: 30px;
    padding: 0 68px 0 10px;
    background-color: transparent;
    line-height: 16px;
    font-size: 11.5px;
    color: #1C2321;
    border-radius: 6px;
    border: 1px solid #E0E5E3;
    transition: border-color .3s;
    outline: none;
    }
    .left-part2:focus{
    border-color: #0FA68E;
    }

    .space-search-action {
      position: absolute;
      top: 50%;
      display: grid;
      place-items: center;
      width: 28px;
      height: 28px;
      margin: 0;
      padding: 0;
      border: 0;
      border-radius: 7px;
      background: transparent;
      color: var(--ink-2);
      transform: translateY(-50%);
      cursor: pointer;
      transition: background-color .18s, color .18s;

      svg { display: block; width: 16px; height: 16px; fill: none; stroke: currentColor; stroke-width: 1.8; stroke-linecap: round; stroke-linejoin: round; }
      &:hover { background: var(--brand-light-9); color: var(--brand-hover); }
      &:focus-visible { outline: 2px solid var(--brand); outline-offset: -2px; }
    }
    .search-clear { right: 34px; }
    .search-submit { right: 4px; }
    }
    }
    .right{
        width: 256px;
        flex-shrink: 0;
        margin-left: 16px;
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
                    color: #5C6664;
                    font-size: 12.5px;
                    line-height: 18px;
                    cursor: pointer;
                }
                span:nth-child(2){
                    font-size: 14px;
                    line-height: 20px;
                    margin-top: 2px;
                    font-weight: 500;
                    color: #1C2321;
                    cursor: pointer;
                }
                span{
                    transition: color 0.3s ease;
                }
            }
            span:hover{
                span{
                    color: #0FA68E !important;
                }
            }
            div{
                display: flex;
                flex-direction: column;
                align-items: center;
                min-width: 52px;
                margin-left: 0px;

                span:nth-child(1){
                    color: #5C6664;
                    font-size: 12.5px;
                    line-height: 18px;
                }
                span:nth-child(2){
                    font-size: 14px;
                    line-height: 20px;
                    margin-top: 2px;
                    font-weight: 500;
                    color: #1C2321;
                }
            }
        }
    }
    }
}

@media (max-width: 1200px) {
    .home-aside {
        position: relative;

        .home-aside-logo {
            flex-wrap: wrap;

            .left { flex-basis: 100%; margin-right: 0; }
            .right { width: 100%; margin: 0; padding: 10px 0; border-top: 1px solid #EFF3F2; }
            .right .user-middle-info { justify-content: flex-start; gap: 24px; }
        }
    }
}

@media (max-width: 640px) {
    .home-aside .home-aside-logo {
        .left { flex-wrap: wrap; }
        .left .left-part1 { width: 100%; }
        .left .left-part1 > span { height: 48px; margin-right: 20px; }
        .left .search-box { width: 100%; height: 34px; margin: 0 0 var(--gap-3); }
        .left .search-box .left-part2 { width: 100%; height: 34px; font-size: 13px; }
        .right .user-middle-info { justify-content: space-between; gap: 4px; }
    }
}

</style>