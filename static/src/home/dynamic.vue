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
            <div><img src="../img/uid.png"> {{ store.homeUserInformation.id }}</div>
            <div><img src="../img/birthday.png"> {{ store.homeUserInformation?.birthday?.split(' ')[0] }}</div>
        </div> 
        <div class="dynamic-list">
            <div v-show="loadMore" class="class-loading">
               <img src="../img/loading-blue.gif">
               <span>拼命加载中...</span>
            </div>
            <div v-show="!loadMore&&dynamicList.length>0" class="dynamic-item" v-for="dynamic in dynamicList" :key="dynamic.dynamic.id">
                <dynamic :dynamic="dynamic"/>
            </div>
            <div v-if="dynamicList.length===0&&!loadMore" class="no-data">
                <img src="../img/home_nodata.svg">
                <div>好像没有东西诶</div>
            </div>
            <div v-show="loadMore2" class="class-loading2">
               <img src="../img/loading-blue.gif">
               <span>拼命加载中...</span>
            </div>
            <div v-if="fotterFlag&&dynamicList.length>0&&!loadMore2&&!loadMore" class="no-more">你已经到达世界的尽头</div>
        </div>
    </div>
</template>
  
<script setup>
import { onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { useGlobalStore } from '../store/store';
import dynamic from "@/utils/dynamic.vue";
import {homeDynamic} from "@/api/home/index";
import {getEitList} from "@/api/user/index";

const store = useGlobalStore();
const dynamicMenu=ref(1);
const loadMore=ref(true);
const loadMore2=ref(false);
const userId = parseInt(new URL(window.location).searchParams.get("userId"))|| null;
const pageNum=ref(1);
const dynamicList=reactive([]);
const fotterFlag=ref(false);

onMounted(async()=>{

if(store.homeUserInformation.id){
    await homeDynamicF();
    store.setHomeLoad(true,"homeBody");
}

if(store.userId){
    getEitListF();
}

window.addEventListener("scroll",handleScroll);

})

onUnmounted(()=>{


store.setHomeLoad(false,"homeBody");

})
  
// 监听store变化
let onceFlag=true;
watch(()=>store.homeUserInformation.id,async()=>{

    if(onceFlag&&store.homeUserInformation.id){
        onceFlag=null;
        await homeDynamicF();
        store.setHomeLoad(true,"homeBody");
    } 
})

let onceFlag2=true;
watch(()=>store.userId,async()=>{

    if(onceFlag2&&store.userId){
        onceFlag2=null;
        await getEitListF();
    } 
})

watch(dynamicMenu,()=>{
    pageNum.value=1;
    fotterFlag.value=false;
    store.commentOpenFlag=false;
    dynamicList.length=0;
    if(store.homeUserInformation.id)
        homeDynamicF();
})

//获取动态列表
async function homeDynamicF(){
    if(dynamicList.length===0)
    {
        loadMore.value=true;
    }
    else{
        loadMore2.value=true;
    }
    let date=new Date();
    homeDynamic(store.homeUserInformation.id,store.userId,pageNum.value++,dynamicMenu.value).then(res=>{

        if(res.data.code===1){
            pushData(res.data.data.records);
            if(!res.data.data.records||res.data.data.records.length===0)
            {
                fotterFlag.value=true;
            }
                const date2=new Date();
                setTimeout(() => {
                loadMore.value=false;
                loadMore2.value=false;
            }, date2 - date<300?300:date2 - date);
        }
    })

}

//监视置顶动态变化
watch(()=>store.upFlag,()=>{
    if(store.upFlag){
        const index=dynamicList.findIndex(item=>item.dynamic.id===store.upFlag);
        let temp=dynamicList[index];
        dynamicList.splice(index,1);
        dynamicList.unshift(temp);
        dynamicList.forEach(item=>{
            if(item.dynamic.id!==store.upFlag)
                item.dynamic.upFlag=0;
        })
        store.upFlag=null;
    }
})

//监视删除动态变化
watch(()=>store.deleteFlag,()=>{
    if(store.deleteFlag){
        const index=dynamicList.findIndex(item=>item.dynamic.id===store.deleteFlag);
        dynamicList.splice(index,1);
        store.deleteFlag=null;
    }
})

//滚动加载
function handleScroll(){
    if(!loadMore2.value&&!loadMore.value&&!fotterFlag.value){
        if((window.innerHeight+document.documentElement.scrollTop)>=document.documentElement.offsetHeight-100){
            homeDynamicF();
        }
    }
}

//去除重复数据
function pushData(newData) {
if(newData===null)
    return;
    const idSet = new Set(dynamicList.map(item => item.dynamic.id)); // 获取已有数据的 ID
    const uniqueData = newData.filter(item => !idSet.has(item.dynamic.id)); // 过滤掉重复的
    dynamicList.push(...uniqueData); // Vue 响应式更新
}

//获取eit列表
async function getEitListF(){
    getEitList(store).then(res=>{
        if(res.data.code===1){
            store.setEitList(res.data.data);
        }
    })
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
    display: flex;
    padding-left: 60px;
    padding-top: 30px;

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
        margin-right: 16px;

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
        background-color: #00AEEC;
        margin-bottom: 8px;
        }
        .activeMenu{
            background-color: white;
            color: #18191C;
            transition: background-color 0.3s ease;
        }
        .activeMenu:hover{
            background-color: #F1F2F3;
        }
    }
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
    .dynamic-list{
        width: 883px;
        display: flex;
        flex-direction: column;

            .class-loading{
                position: relative;
                margin-top: 60px;
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
            .dynamic-item{
                margin-bottom: 12px;
            }
            .class-loading2{
                position: relative;
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
                    color: #9499A0;
                    
                }
            }
            .no-more{
                padding-top: 12px;
                width: 100%;
                display: flex;
                align-items: end;
                justify-content: center;
                font-size: 14px;
                color: #9499A0;
                user-select: none;
                margin-bottom: 10px;
            }
    }
           

}


</style>