<template>
    <div class="dynamic-container">
        <div class="aside-container">
            <div :class="{activeMenu:dynamicMenu!==1}" @click="dynamicMenu=1">
                <span>全部</span>
            </div>
            <div :class="{activeMenu:dynamicMenu!==2}" @click="dynamicMenu=2">
                <span>视频</span>
            </div>
        </div>
        <div v-if="(store.userId!==null&&store.userId===userId)||store.homeUserInformation.publicBirthday===1" class="home-user-info">
            <div>个人资料 <span v-if="store.homeUserInformation.id===store.userId" @click="openAccount">编辑 <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M340.864 149.312a30.592 30.592 0 0 0 0 42.752L652.736 512 340.864 831.872a30.592 30.592 0 0 0 0 42.752 29.12 29.12 0 0 0 41.728 0L714.24 534.336a32 32 0 0 0 0-44.672L382.592 149.376a29.12 29.12 0 0 0-41.728 0z"></path></svg></span></div>
            <div><img src="/img/uid.png"> {{ store.homeUserInformation.id }}</div>
            <div v-if="store.homeUserInformation.birthday"><img src="/img/birthday.png"> {{ store.homeUserInformation?.birthday?.split(' ')[0] }}</div>
        </div> 
        <div class="dynamic-list">
            <loadingIndicator v-show="loadMore"/>
            <div v-show="!loadMore&&dynamicList.length" class="dynamic-item" v-for="dynamic in dynamicList" :key="dynamic.id">
                <dynamicC :dynamic="dynamic"/>
            </div>
            <div v-if="!dynamicList.length&&!loadMore" class="no-data">
                <img src="/img/home_nodata.svg">
                <div>好像没有东西诶</div>
            </div>
            <loadingIndicator v-show="loadMore2" inline min-height="0px"/>
            <div v-if="fotterFlag&&dynamicList.length>0&&!loadMore2&&!loadMore" class="no-more">你已经到达世界的尽头</div>
        </div>
    </div>
</template>
  
<script setup>
import { onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { useGlobalStore } from '../store/store';
import dynamicC from "./homePost.vue";
import loadingIndicator from "@/components/loadingIndicator.vue";
import {homeDynamic} from "@/api/home/index";
import { ElMessage } from "element-plus";

const store = useGlobalStore();
const dynamicMenu=ref(1);
const loadMore=ref(true);
const loadMore2=ref(false);
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || store.userId;
const pageNum=ref(1);
const dynamicList=reactive([]);
const fotterFlag=ref(false);
const touchStartY = ref(0)
const touchMoveY = ref(0)
const touchEndY = ref(0)

onMounted(()=>{
    window.addEventListener("scroll",handleScroll);
    document.addEventListener("touchstart", touchStartF)
    document.addEventListener("touchmove", touchMoveF)
    document.addEventListener("touchend", touchEndF)
})

onUnmounted(()=>{
    window.removeEventListener("scroll",handleScroll);
    document.removeEventListener("touchstart", touchStartF)
    document.removeEventListener("touchmove", touchMoveF)
    document.removeEventListener("touchend", touchEndF)
})

  
// 监听store变化
let requestId = 0;
watch([() => store.homeUserInformation.id, dynamicMenu], ([id])=>{
    if (!id) return;
    requestId++;
    pageNum.value=1;
    fotterFlag.value=false;
    store.commentOpenFlag=false;
    dynamicList.length=0;
    homeDynamicF();
}, { immediate: true })

function touchStartF(e){
    touchStartY.value = e.touches[0].clientY
}

function touchMoveF(e){
    touchMoveY.value = e.touches[0].clientY
}

function touchEndF(e){
    touchEndY.value = e.changedTouches[0].clientY
    const delta = touchStartY.value - touchEndY.value

    if(delta > 50 && store.homeMenu === 2){
        if(!loadMore2.value && !loadMore.value && !fotterFlag.value){
            if((window.innerHeight + document.documentElement.scrollTop) >= document.documentElement.offsetHeight - 50){
                homeDynamicF()
            }
        }
    }
}

//获取动态列表
async function homeDynamicF(){
    const current = requestId;
    if(dynamicList.length===0)
    {
        loadMore.value=true;
    }
    else{
        loadMore2.value=true;
    }
    try {
        const res = await homeDynamic(store.homeUserInformation.id, store.userId, pageNum.value, dynamicMenu.value);
        if (current !== requestId) return;
        pushData(res.data.data.records);
        fotterFlag.value = dynamicList.length >= res.data.data.total;
        pageNum.value++;
    } catch (error) {
        if (current === requestId) {
            ElMessage.error(error.message);
            fotterFlag.value = true;
        }
    } finally {
        if (current === requestId) {
            loadMore.value = false;
            loadMore2.value = false;
            store.setHomeLoad(true, "homeBody");
            store.setHomeLoadMenuList(2);
        }
    }

}

//监视删除动态变化
watch(()=>store.deleteFlag,()=>{
    if(store.deleteFlag){
        const index=dynamicList.findIndex(item=>item.id===store.deleteFlag);
        if (index !== -1) dynamicList.splice(index,1);
    }
})

//滚动加载
function handleScroll(){
    if(store.homeMenu === 2 && !loadMore2.value&&!loadMore.value&&!fotterFlag.value){
        if((window.innerHeight+document.documentElement.scrollTop)>=document.documentElement.offsetHeight-100){
            homeDynamicF();
        }
    }
}

//去除重复数据
function pushData(newData) {
if(newData===null)
    return;
    const idSet = new Set(dynamicList.map(item => item.id));
    const uniqueData = newData.filter(item => !idSet.has(item.id));
    dynamicList.push(...uniqueData); // Vue 响应式更新
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

.dynamic-container{
    display: grid;
    grid-template-columns: 150px minmax(0, 1fr) 240px;
    align-items: start;
    gap: 0 var(--gap-5);
    width: 100%;
    max-width: var(--page-max);
    margin: 0 auto;
    padding: var(--gap-5) var(--page-pad);

    .aside-container{
        position: sticky;
        user-select: none;
        top: 80px;
        width: 150px;
        height: 130px;
        flex-shrink: 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 0;

        div{
        width: 150px;
        height: 52px;
        font-size: 14px;
        cursor: pointer;
        border-radius: 6px;
        padding: 14px 16px;
        color: white;
        display: flex;
        align-items: center;
        justify-content: space-between;
        background-color: #0FA68E;
        margin-bottom: 8px;
        }
        .activeMenu{
            background-color: white;
            color: #1C2321;
            transition: background-color 0.3s ease;
        }
        .activeMenu:hover{
            background-color: #EFF3F2;
        }
    }
    .home-user-info{
    grid-column: 3;
    grid-row: 1;
    position: sticky;
    top: 80px;
    width: 240px;
    height: 125px;
    padding: 20px 16px 24px;
    background-color: #F4F7F6;
    border-radius: 6px;

    div:nth-child(1){
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    color: #1C2321;
    font-size: 16px;
    font-weight: 500;
        span{
        color: #5C6664;
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
            color: #0FA68E;
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
    .dynamic-list{
        grid-column: 2;
        grid-row: 1;
        width: 100%;
        min-width: 0;
        display: flex;
        flex-direction: column;

            .dynamic-item{
                margin-bottom: 12px;
            }
            .no-data{
            user-select: none;
            width: 100%;
            height: 50vh;
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
                    color: #8D9794;
                    
                }
            }
            .no-more{
                padding-top: 12px;
                width: 100%;
                display: flex;
                align-items: end;
                justify-content: center;
                font-size: 14px;
                color: #8D9794;
                user-select: none;
                margin-bottom: 10px;
            }
    }
}

/* 资料卡在中等屏先让位给列表，窄屏整体单列 */
@media (max-width: 1200px) {
    .dynamic-container{
        grid-template-columns: 150px minmax(0, 1fr);

        .home-user-info{
            grid-column: 2;
            grid-row: 1;
            position: static;
            width: 100%;
            height: auto;
            margin-bottom: var(--gap-4);
        }
        .dynamic-list { grid-row: 2; }
    }
}

@media (max-width: 860px) {
    .dynamic-container{
        grid-template-columns: minmax(0, 1fr);

        .aside-container{
            position: static;
            flex-direction: row;
            width: 100%;
            height: auto;

            div{
                flex: 1;
            }
            .home-user-info, .dynamic-list { grid-column: 1; grid-row: auto; }
        }
    }
}

</style>