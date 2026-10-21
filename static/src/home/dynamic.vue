<template>
    <section class="space-activity" :class="{ 'has-profile': (store.userId!==null&&store.userId===userId)||store.homeUserInformation.publicBirthday===1 }" aria-label="空间动态">
        <header class="activity-toolbar">
            <h2>动态</h2>
            <div class="activity-tabs" role="group" aria-label="动态类型">
                <button type="button" :class="{ 'is-active': dynamicMenu===1 }" :aria-pressed="dynamicMenu===1" @click="dynamicMenu=1">全部动态</button>
                <button type="button" :class="{ 'is-active': dynamicMenu===2 }" :aria-pressed="dynamicMenu===2" @click="dynamicMenu=2">视频动态</button>
            </div>
        </header>
        <aside v-if="(store.userId!==null&&store.userId===userId)||store.homeUserInformation.publicBirthday===1" class="activity-profile">
            <div class="activity-profile-heading">
                <h3>个人资料</h3>
                <button v-if="store.homeUserInformation.id===store.userId" type="button" @click="openAccount">编辑</button>
            </div>
            <dl>
                <div><dt>UID</dt><dd>{{ store.homeUserInformation.id }}</dd></div>
                <div v-if="store.homeUserInformation.birthday"><dt>生日</dt><dd>{{ store.homeUserInformation?.birthday?.split(' ')[0] }}</dd></div>
            </dl>
        </aside>
        <div class="activity-stream">
            <loadingIndicator v-show="loadMore"/>
            <div v-show="!loadMore&&dynamicList.length" class="activity-item" v-for="dynamic in dynamicList" :key="dynamic.id">
                <dynamicC :dynamic="dynamic" @changed="reloadPosts"/>
            </div>
            <AppEmptyState v-if="!dynamicList.length&&!loadMore" title="暂无动态"/>
            <loadingIndicator v-show="loadMore2" inline min-height="0px"/>
            <div v-if="fotterFlag&&dynamicList.length>0&&!loadMore2&&!loadMore" class="no-more">没有更多动态了</div>
        </div>
    </section>
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

//打开个人中心
function openAccount(){
   
   window.open('./account','_blank');

}

</script>

<style scoped>
.space-activity { display: grid; grid-template-columns: minmax(0, 1fr); gap: 20px; align-items: start; min-width: 0; }
.space-activity.has-profile { grid-template-columns: 240px minmax(0, 1fr); }
.space-activity *, .space-activity *::before { box-sizing: border-box; }
.activity-toolbar { grid-column: 1 / -1; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; padding: 16px 20px; border: 1px solid var(--line); border-radius: 16px; background: #fff; }
.activity-toolbar h2 { margin: 0; color: var(--ink); font-size: 20px; font-weight: 600; }
.activity-tabs { display: flex; flex-wrap: wrap; gap: 4px; padding: 4px; border-radius: 10px; background: var(--fill-soft); }
.activity-tabs button { min-height: 36px; padding: 8px 16px; border: 0; border-radius: 8px; background: transparent; color: var(--ink-2); font: inherit; font-size: 13px; cursor: pointer; }
.activity-tabs button.is-active { background: var(--brand-light-9); color: var(--brand-hover); font-weight: 600; }
.activity-tabs button:focus-visible, .activity-profile button:focus-visible { outline: 2px solid var(--brand); outline-offset: 2px; }
.activity-profile { position: sticky; top: 20px; min-width: 0; padding: 20px; border: 1px solid var(--line); border-radius: 16px; background: #fff; }
.activity-profile-heading { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding-bottom: 14px; border-bottom: 1px solid var(--line); }
.activity-profile h3 { margin: 0; color: var(--ink); font-size: 15px; font-weight: 600; }
.activity-profile button { padding: 4px 8px; border: 0; border-radius: 6px; background: var(--brand-light-9); color: var(--brand-hover); font: inherit; font-size: 12px; cursor: pointer; }
.activity-profile dl { display: grid; gap: 14px; margin: 18px 0 0; font-size: 13px; }
.activity-profile dl > div { display: grid; grid-template-columns: 40px minmax(0, 1fr); gap: 8px; }
.activity-profile dt { color: var(--ink-3); }
.activity-profile dd { min-width: 0; margin: 0; color: var(--ink-2); overflow-wrap: anywhere; }
.activity-stream { display: flex; min-width: 0; flex-direction: column; gap: 18px; }
.activity-item { position: relative; min-width: 0; padding-left: 24px; }
.activity-item::before { content: ""; position: absolute; top: 28px; left: 0; width: 9px; height: 9px; border: 2px solid var(--brand); border-radius: 50%; background: var(--fill-soft); }
.activity-item::after { content: ""; position: absolute; top: 42px; bottom: -34px; left: 4px; width: 1px; background: var(--line); }
.activity-item:not(:has(~ .activity-item))::after { bottom: 0; }
.no-more { margin: 0; color: var(--ink-3); font-size: 12px; line-height: 1.8; text-align: center; }
@media (max-width: 960px) {
  .space-activity.has-profile { grid-template-columns: minmax(0, 1fr); gap: 16px; }
  .activity-profile { position: static; padding: 16px 20px; }
  .activity-profile dl { display: flex; flex-wrap: wrap; gap: 12px 28px; margin-top: 12px; }
}
@media (max-width: 640px) {
  .activity-toolbar { padding: 14px 16px; }
  .activity-toolbar h2 { font-size: 18px; }
  .activity-tabs button { padding: 8px 12px; font-size: 12px; }
  .activity-profile { padding: 16px; }
  .activity-profile dl { display: grid; }
  .activity-item { padding-left: 14px; }
  .activity-item::before { left: 0; width: 7px; height: 7px; }
  .activity-item::after { left: 3px; }
}
</style>