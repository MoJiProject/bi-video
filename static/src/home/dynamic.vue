<template>
    <section class="space-activity" aria-label="空间动态">
        <aside class="aside-container" role="group" aria-label="动态类型">
            <button type="button" class="content-type-tab" :class="{ activeMenu: dynamicMenu!==1 }"
                :aria-pressed="dynamicMenu===1" @click="dynamicMenu=1">全部动态</button>
            <button type="button" class="content-type-tab" :class="{ activeMenu: dynamicMenu!==2 }"
                :aria-pressed="dynamicMenu===2" @click="dynamicMenu=2">视频动态</button>
        </aside>
        <div class="activity-content">
            <header class="activity-toolbar">
                <h2>动态</h2>
            </header>
            <div class="activity-layout" :class="{ 'has-profile': (store.userId!==null&&store.userId===userId)||store.homeUserInformation.publicBirthday===1 }">
                <SpaceProfileCard v-if="(store.userId!==null&&store.userId===userId)||store.homeUserInformation.publicBirthday===1" class="activity-profile"/>
                <div class="activity-stream">
                    <loadingIndicator v-show="loadMore"/>
                    <div v-show="!loadMore&&dynamicList.length" class="activity-item" v-for="dynamic in dynamicList" :key="dynamic.id">
                        <dynamicC :dynamic="dynamic" @changed="reloadPosts"/>
                    </div>
                    <AppEmptyState v-if="!dynamicList.length&&!loadMore" title="暂无动态"/>
                    <loadingIndicator v-show="loadMore2" inline min-height="0px"/>
                    <div v-if="fotterFlag&&dynamicList.length>0&&!loadMore2&&!loadMore" class="no-more">没有更多动态了</div>
                </div>
            </div>
        </div>
    </section>
</template>
  
<script setup>
import { onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { useGlobalStore } from '../store/store';
import dynamicC from "./homePost.vue";
import loadingIndicator from "@/components/loadingIndicator.vue";
import SpaceProfileCard from "./SpaceProfileCard.vue";
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
    reloadPosts();
}, { immediate: true })

function reloadPosts() {
    requestId++;
    pageNum.value=1;
    fotterFlag.value=false;
    loadMore2.value=false;
    store.commentOpenFlag=false;
    dynamicList.length=0;
    homeDynamicF();
}

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

</script>

<style scoped>
.space-activity { display: flex; gap: 20px; align-items: flex-start; min-width: 0; }
.space-activity *, .space-activity *::before { box-sizing: border-box; }
.space-activity > .aside-container { display: flex; flex-direction: column; flex-shrink: 0; }
.activity-content { flex: 1; width: 100%; min-width: 0; }
.activity-toolbar { margin-bottom: 18px; font-family: var(--el-font-family); }
.activity-toolbar h2 { margin: 0; color: var(--ink); font-size: 20px; font-weight: 600; }
.activity-layout { display: grid; grid-template-columns: minmax(0, 1fr); gap: 20px; align-items: start; min-width: 0; }
.activity-layout.has-profile { grid-template-columns: minmax(0, 1fr) 240px; }
.activity-profile { grid-column: 2; grid-row: 1; }
.activity-stream { grid-column: 1; grid-row: 1; display: flex; min-width: 0; flex-direction: column; gap: 18px; }
.activity-item { min-width: 0; }
.no-more { margin: 0; color: var(--ink-3); font-size: 12px; line-height: 1.8; text-align: center; }
@media (max-width: 960px) {
  .activity-layout.has-profile { grid-template-columns: minmax(0, 1fr); gap: 16px; }
  .activity-profile { grid-column: 1; grid-row: 1; }
  .has-profile .activity-stream { grid-row: 2; }
}
@media (max-width: 640px) {
  .activity-toolbar h2 { font-size: 18px; }
}
</style>