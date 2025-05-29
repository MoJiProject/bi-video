<template>
    <div class="waitWatch-body">
        <div class="head">
            <div class="head-left">
                <svg data-v-69623c3f="" class="vui_icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" width="32" height="32" xmlns:xlink="http://www.w3.org/1999/xlink"><path d="M16 4.74968C9.78681 4.74968 4.750023333333333 9.786476666666665 4.750023333333333 15.999733333333333C4.750023333333333 22.212899999999998 9.78681 27.24973333333333 16 27.24973333333333C19.106216666666665 27.24973333333333 21.917133333333332 25.991816666666665 23.95375 23.955866666666665C24.24668333333333 23.663066666666666 24.721466666666664 23.663083333333333 25.0144 23.956016666666663C25.307199999999998 24.248966666666664 25.307066666666664 24.723866666666666 25.01423333333333 25.016683333333333C22.7078 27.322316666666666 19.519916666666667 28.74973333333333 16 28.74973333333333C8.958386666666666 28.74973333333333 3.2500233333333335 23.04135 3.2500233333333335 15.999733333333333C3.2500233333333335 8.958043333333332 8.958386666666666 3.24968 16 3.24968C23.04161666666667 3.24968 28.75 8.958043333333332 28.75 15.999733333333333C28.75 16.47798333333333 28.72368333333333 16.95038333333333 28.672283333333333 17.415416666666665C28.6267 17.827166666666663 28.256049999999995 18.124016666666666 27.84443333333333 18.078433333333333C27.4327 18.032966666666667 27.13583333333333 17.662316666666666 27.1813 17.25058333333333C27.226699999999997 16.84015 27.25 16.4228 27.25 15.999733333333333C27.25 9.786476666666665 22.213183333333333 4.74968 16 4.74968z" fill="currentColor"></path><path d="M24.803066666666666 14.802666666666667C25.095883333333333 14.509733333333333 25.57078333333333 14.509733333333333 25.863716666666665 14.802666666666667L28 16.93895L30.1364 14.802666666666667C30.429216666666665 14.509733333333333 30.904116666666667 14.509733333333333 31.197049999999997 14.802666666666667C31.489866666666664 15.095616666666666 31.489866666666664 15.570383333333332 31.197049999999997 15.863333333333333L28.766 18.294266666666665C28.34303333333333 18.717333333333332 27.657083333333333 18.717333333333332 27.233999999999998 18.294266666666665L24.803066666666666 15.863333333333333C24.510133333333332 15.570383333333332 24.510133333333332 15.095616666666666 24.803066666666666 14.802666666666667z" fill="currentColor"></path><path d="M19.83223333333333 14.89335C20.683899999999998 15.385 20.683899999999998 16.614166666666666 19.83223333333333 17.105816666666666L15.039966666666666 19.872716666666665C14.188416666666665 20.364366666666665 13.12385 19.74973333333333 13.12385 18.766416666666665L13.12385 13.232789999999998C13.12385 12.249416666666665 14.188416666666665 11.634813333333334 15.039966666666666 12.1265L19.83223333333333 14.89335z" fill="currentColor"></path></svg>
                稍后再看
                <span>·{{ store.collectNumber }}</span>
            </div>
            <div class="head-right">
                <button>
                    {{ sort2===1?"最近添加":"最早添加" }}
                    <svg data-v-69623c3f="" class="vui_icon option-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 12 12" width="12" height="12" xmlns:xlink="http://www.w3.org/1999/xlink"><path d="M5.91165 7.527550000000001C5.9604 7.478800000000001 6.0396 7.478800000000001 6.08835 7.527550000000001L2.405335 3.84452C2.1124300000000003 3.5516249999999996 1.6375599999999997 3.5516249999999996 1.344665 3.84452C1.051775 4.137410000000001 1.051775 4.612285 1.344665 4.905175000000001L5.0277 8.5882C5.564699999999999 9.125200000000003 6.435300000000001 9.125200000000003 6.9723 8.5882L10.655300000000002 4.905175000000001C10.9482 4.612285 10.9482 4.137410000000001 10.655300000000002 3.84452C10.362450000000003 3.5516249999999996 9.887549999999997 3.5516249999999996 9.594699999999998 3.84452L5.91165 7.527550000000001z" fill="currentColor"></path></svg>
                    <div class="dropdown-menu">
                         <div @click="sort2=1">最近添加</div>
                         <div @click="sort2=2">最早添加</div>
                    </div>
                </button>
            </div>
        </div>
        <div class="control-container" :style="{boxShadow: boxShadowFlag?'0 2px 4px rgba(0,0,0,.08)':'none'}">
            <div class="controls">
                <div class="control">
                    <div v-show="!batchManageFlag" class="sort-btn" @click="sortFlag=!sortFlag">
                        时间筛选 
                        <svg  xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="m488.832 344.32-339.84 356.672a32 32 0 0 0 0 44.16l.384.384a29.44 29.44 0 0 0 42.688 0l320-335.872 319.872 335.872a29.44 29.44 0 0 0 42.688 0l.384-.384a32 32 0 0 0 0-44.16L535.168 344.32a32 32 0 0 0-46.336 0"></path></svg>
                    </div>
                    <div v-show="!batchManageFlag" class="search-container">
                    <input type="text" v-model="keyWord" placeholder="搜索标题/up主昵称" maxlength="30" @keydown.enter="searchwaitWatchList">
                    <img
                        v-show="keyWord.length>0"
                        @mouseover="deleteAllSearchFlag = false"
                        @mouseleave="deleteAllSearchFlag = true"
                        @click="keyWord = ''"
                        :src="deleteAllSearchFlag ? '../img/删除搜索记录.png' : '../img/删除全部搜索hover.png'"
                        class="deleteAllSearchImg"/>
                        <img class="searchImg" src="../img/搜索.png" @click="searchwaitWatchList">         
                    </div>
                    <div v-show="!batchManageFlag" class="clear-btn" @click="deleteAllwaitWatchDialogFlag=true">
                    <img src="../img/清空历史记录.png">
                    清空稍后再看
                    </div>
                    <div v-show="batchManageFlag" class="check-all" @click="checkAllF">
                        <span class="check-all-icon">
                            <span class="check-input-box" :class="{activeCheck:checkAllFlag}"></span>
                        </span>
                        <span class="check-all-text">全选</span>
                    </div>
                    <div v-show="batchManageFlag" class="num">已经选择 {{ store.autoVideoList.length }} 条稍后再看</div>
                    <div v-show="batchManageFlag" class="line"><div></div></div>
                    <div v-show="batchManageFlag" class="delete-btn" @click="deleteCheckwaitWatchDialogFlag=store.autoVideoList.length>0" :class="{activeDelete: store.autoVideoList.length}">
                        <svg v-show="!store.autoVideoList.length"  xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M160 256H96a32 32 0 0 1 0-64h256V95.936a32 32 0 0 1 32-32h256a32 32 0 0 1 32 32V192h256a32 32 0 1 1 0 64h-64v672a32 32 0 0 1-32 32H192a32 32 0 0 1-32-32zm448-64v-64H416v64zM224 896h576V256H224zm192-128a32 32 0 0 1-32-32V416a32 32 0 0 1 64 0v320a32 32 0 0 1-32 32m192 0a32 32 0 0 1-32-32V416a32 32 0 0 1 64 0v320a32 32 0 0 1-32 32"></path></svg>
                        <svg v-show="store.autoVideoList.length"  xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M352 192V95.936a32 32 0 0 1 32-32h256a32 32 0 0 1 32 32V192h256a32 32 0 1 1 0 64H96a32 32 0 0 1 0-64zm64 0h192v-64H416zM192 960a32 32 0 0 1-32-32V256h704v672a32 32 0 0 1-32 32zm224-192a32 32 0 0 0 32-32V416a32 32 0 0 0-64 0v320a32 32 0 0 0 32 32m192 0a32 32 0 0 0 32-32V416a32 32 0 0 0-64 0v320a32 32 0 0 0 32 32"></path></svg>
                        删除
                    </div>
                    <div class="batch-manage-btn" @click="batchManageFlag=!batchManageFlag">
                    <img v-show="!batchManageFlag" src="../img/批量操作历史记录.png">
                    {{ batchManageFlag?"退出管理":"批量管理" }}
                    </div>
                </div>
                <div v-show="sortFlag" class="sort">
                    <div :class="{active: sort===1}" @click="sort=1">全部时间</div>
                    <div :class="{active: sort===2}" @click="sort=2">今天</div>
                    <div :class="{active: sort===3}" @click="sort=3">昨天</div>
                    <div :class="{active: sort===4}" @click="sort=4">近一周</div>
                    <el-date-picker
                    v-model="custorTime"
                    style="border-radius: 8px; width: 250px; height: 34px;margin-left: 12px;flex:0.4;"
                    type="daterange"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    :default-value="[new Date(), new Date()]"
                    >
                    </el-date-picker>
                </div>
            </div>
        </div>
        <div class="body">
            <div class="video-list">
                <videoList8 :batch-controls="batchManageFlag" :data="waitWatchList" :searchContent="keyWord"/>
            </div>
            <div v-show="!waitWatchList.length" class="no-data">
                <img src="../img/home_nodata.svg">
                <div>这里什么都没有哦~</div>
            </div>
            <div v-show="dataFlag&&waitWatchList.length" class="fotter">已经探索到底啦~</div>
        </div>
        <!-- 清空稍后再看对话框 -->
        <el-dialog
        v-model="deleteAllwaitWatchDialogFlag"
        append-to-body
        width="320"
        :lock-scroll="false"
        align-center
        style="height: 152px; border-radius: 12px"
        >
            <div class="delete-dialog-title">确认要清空所有稍后再看？</div>
            <div class="delete-dialog-content">
                记录清空后将不可恢复
            </div>
            <div class="delete-dialog-btn-container">
                <button class="delete-dialog-cancel-btn" @click="deleteAllwaitWatchDialogFlag=false">
                取消
            </button>
            <button
                class="delete-dialog-confirm-btn" @click="deleteAllwaitWatchF"
            >
                确认
            </button>
            </div>
        </el-dialog>
        <!-- 删除选中的稍后再看 -->
        <el-dialog
        v-model="deleteCheckwaitWatchDialogFlag"
        append-to-body
        width="320"
        :lock-scroll="false"
        align-center
        style="height: 152px; border-radius: 12px"
        >
            <div class="delete-dialog-title">确认要删除选中的稍后再看？</div>
            <div class="delete-dialog-content">
                记录删除后将不可恢复
            </div>
            <div class="delete-dialog-btn-container">
                <button class="delete-dialog-cancel-btn" @click="deleteCheckwaitWatchDialogFlag=false">
                取消
            </button>
            <button
                class="delete-dialog-confirm-btn" @click="deleteCheckwaitWatchF"
            >
                确认
            </button>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import {useGlobalStore} from "@/store/store";
import { onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { deleteCollect,selectWaitWatch,cleanAllWaitWatch } from '../api/collect/index';
import { ElMessage } from "element-plus";
import videoList8 from "@/utils/videoList8.vue";

const store = useGlobalStore();
const waitWatchFlag=ref(false);
const sort=ref(1);//全部时间 1 今天 2 昨天 3 近一周 4自定义
const sort2=ref(1);//1最近添加 2最早添加
const sortFlag=ref(false);
const custorTime=ref('');
const startTime=ref('');
const endTime=ref('');
const waitWatchList=reactive([]);
const keyWord=ref('');
const deleteAllSearchFlag=ref(false);
const batchManageFlag=ref(false);
const deleteAllwaitWatchDialogFlag=ref(false);
const deleteCheckwaitWatchDialogFlag=ref(false);
const checkAllFlag=ref(false);
const pageNum=ref(1);
const searchFlag=ref(false);
const dataFlag=ref(false);
const boxShadowFlag=ref(false);

onMounted(()=>{
    document.title="稍后再看";
    window.addEventListener('scroll', handleScroll);
})

onUnmounted(()=>{
    window.removeEventListener('scroll', handleScroll);
})


//初始化
let onceFlag=true;
watch(()=>store.userInformation.waitWatchFlag,()=>{
    if(onceFlag)
    {
        waitWatchFlag.value=store.userInformation.waitWatchFlag?true:false;
        onceFlag=null;
    }
})

//监视自定义时间
watch(custorTime, (newValue) => {
    if(newValue!== null){
        if (newValue.length !== 0) {
            const date1 = new Date(custorTime.value[0]);
            const date2 = new Date(custorTime.value[1]);
            if (!isNaN(date1.getTime())) {
                startTime.value = date1.toISOString();
            }
            if (!isNaN(date2.getTime())) {
                endTime.value = date2.toISOString();
            }
            if (startTime.value && endTime.value) {
                sort.value = 5;
                pageNum.value=1;
                dataFlag.value=false;
                waitWatchList.length=0;
                selectwaitWatchListF();
            }
        }
    }
    else{
        sort.value = 1;
    }
})

//重置值
watch(sort,()=>{
  if(sort.value!==5){
    custorTime.value='';
    pageNum.value=1;
    dataFlag.value=false;
    waitWatchList.length=0;
    selectwaitWatchListF();
  }
  
})

watch(sort2,()=>{
    pageNum.value=1;
    dataFlag.value=false;
    waitWatchList.length=0;
    selectwaitWatchListF();
})

//清空稍后再看
function deleteAllwaitWatchF(){
    cleanAllWaitWatch(store.token,store.userId).then(res=>{
        if(res.data.code===1){
            waitWatchList.length=0;
            ElMessage({
            message: res.data.data,
            type: "info",
            plain: true,
            duration: 1700,
            });
            deleteAllwaitWatchDialogFlag.value=false;
            waitWatchList.length=0;
        }
        else{
            ElMessage({
            message: res.data.msg,
            type: "info",
            plain: true,
            duration: 1700,
            });
        }
    })
}

//查询稍后再看
let selectwaitWatchListFlag=true;
function selectwaitWatchListF(){
    
    if(!selectwaitWatchListFlag)
    return;
    selectwaitWatchListFlag=false;
    selectWaitWatch(store.token,store.userId,pageNum.value++,sort.value,sort2.value,keyWord.value,startTime.value,endTime.value).then(res=>{
        if(res.data.code===1){
            pushData(res.data.data);
            selectwaitWatchListFlag=true;
            if(res.data.data.length===0){
                dataFlag.value=true;
            }
        }
    })
}

//去除重复数据
function pushData(newData) {
if(newData===null)
    return;
    const idSet = new Set(waitWatchList.map(item => item.collects.id)); // 获取已有数据的 ID
    const uniqueData = newData.filter(item => !idSet.has(item.collects.id)); // 过滤掉重复的
    waitWatchList.push(...uniqueData); // Vue 响应式更新
}

//初始化查询
let onceFlag2=true;
watch(()=>store.userId,()=>{
    if(onceFlag2&&store.userId){
        onceFlag2=null;
        selectwaitWatchListF();
    }
})

//搜索稍后再看
function searchwaitWatchList(){
    pageNum.value=1;
    waitWatchList.length=0;
    searchFlag.value=true;
    dataFlag.value=false;
    selectwaitWatchListF();
}

//监视搜索关键字
watch(keyWord,()=>{
    if(keyWord.value.length===0&&searchFlag.value){
        pageNum.value=1;
        searchFlag.value=false;
        waitWatchList.length=0;
        dataFlag.value=false;
        selectwaitWatchListF();
    }
})

//监视是否批量管理
watch(batchManageFlag,()=>{
    if(!batchManageFlag.value){
       store.autoVideoList.length=0;
       checkAllFlag.value=false;
    }
})    

//全部选中或全部取消选中
function checkAllF(){
    checkAllFlag.value=!checkAllFlag.value;
    if(checkAllFlag.value){
       let ids=waitWatchList.map(item=>item.collects.id);
       store.setAutoVideoList(ids);
    }
    else{
       store.autoVideoList.length=0; 
    }
}

//监视选中视频的数量
watch(()=>store.autoVideoList.length,async(newValue)=>{
    if(newValue===waitWatchList.length){
        checkAllFlag.value=true;
    }
    else{
        checkAllFlag.value=false;
    }
    if(newValue===1&&!batchManageFlag.value)
    {
        deleteCheckwaitWatchF();
    }
})

//滑动到底部加载更多
function handleScroll(){
     const scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
     if(scrollTop>124)
       boxShadowFlag.value=true;
     else
       boxShadowFlag.value=false;
     const scrollHeight = document.documentElement.scrollHeight || document.body.scrollHeight;
     const clientHeight = document.documentElement.clientHeight || document.body.clientHeight;
     const scrollBottom = scrollTop + clientHeight;
     if(scrollBottom >= scrollHeight - 300&&!dataFlag.value){
        selectwaitWatchListF();
    }
}

//删除选中的稍后再看
function deleteCheckwaitWatchF(){
    if(store.autoVideoList.length===0){
        ElMessage({
            message: '请先选择要删除的记录',
            type: "info",
            plain: true,
            duration: 1700,
        });
        return;
    }

    deleteCollect(store.token,store.userId,store.autoVideoList).then(res=>{
        if(res.data.code===1){
            waitWatchList.forEach((item,index)=>{
                if(store.autoVideoList.includes(item.collects.id)){
                    waitWatchList.splice(index,1);
                }
            })
            store.setCollectNumber(store.collectNumber-store.autoVideoList.length);
            ElMessage({
                message: res.data.data,
                type: "info",
                plain: true,
                duration: 1700,
            });
            deleteCheckwaitWatchDialogFlag.value=false;
            store.autoVideoList.length=0;
            batchManageFlag.value=false;
        }else{
            ElMessage({
                message: res.data.msg,
                type: "info",
                plain: true,
                duration: 1700,
            });
        }
    })
}

</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.waitWatch-body{
min-width: fit-content;
background-color: white;
padding-top: 30px;
min-height: calc(100vh - 64px);
max-width: 2560px;

    .head{
    width: calc(1152px);
    margin-left: auto;
    margin-right: auto;
    padding-left: 35px;
    display: flex;
    align-items: center;
    justify-content: space-between;

        .head-left{
        display: flex;
        align-items: center;
        color: #18191C;
        font-weight: 600;
        font-size: 28px;
        line-height: 40px;

            svg{
            width: 32px;
            height: 32px;
            transform: translateY(2px);
            margin-right: 8px;
            }
            span{
            margin-left: 4px;
            color: #61666D;
            font-size: 19px;   
            }
        }
        .head-right{
        display: flex;
        align-items: center;
        font-size: 14px;
        padding-right: 98px;
        color: #18191C;

            button{
            position: relative;    
            height: 34px;
            min-width: 100px;
            padding: 0 12px;
            border-radius: 8px;
            font-size: 14px;
            line-height: 1;
            user-select: none;
            cursor: pointer;
            border: none;
            background-color: white;
            transition: background-color .3s ease;

                svg{
                vertical-align: middle;
                }
                .dropdown-menu{
                z-index: 10000;
                position: absolute;
                left: 0px;
                bottom: -100px;
                background-color: white;
                border: 1px solid #E3E5E7;
                box-shadow: 0 8px 40px rgba(0, 0, 0, .1);
                padding: 4px;
                border-radius: 12px;
                opacity: 0;
                transition: all .3s ease;
                visibility: hidden;

                    div{
                    width: 90px;
                    cursor: pointer;
                    font-size: 14px;
                    color: #61666D;
                    height: 40px;
                    line-height: 40px;
                    position: relative;
                    border-radius: 8px;
                    transition: background-color .3s ease;
                    text-align: center;
                    }
                    div:hover{
                    background-color: #f4f4f5;    
                    }
                }
            }
            button:hover{
            background-color: #E3E5E7;

                .dropdown-menu{
                transition-delay: 0.3s;
                opacity: 1;
                visibility: visible;
                }
            }
        }
    }
    .control-container{
     position: sticky;
     padding-top: 28px;
     padding-bottom: 5px;
     top: 0;
     background-color: white;
     z-index: 900;
     width: 100%;
        .controls{
        width: calc(1152px);
        min-height: 46px;
        padding-left: 25px;
        margin-left: auto;
        margin-right: auto;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: left;
        user-select: none;

            .control{
            width: 100%;    
            display: flex;
            justify-content: left;
            padding-left: 12px;

                .sort-btn{
                display: flex;
                justify-content: center;
                align-items: center;
                height: 34px;
                min-width: 100px;
                padding: 0 12px;
                border-radius: 8px;
                font-size: 14px;
                line-height: 1;
                transition: all .3s ease;
                user-select: none;
                color: #18191C;
                background: white;
                border: 1px solid #E3E5E7;
                cursor: pointer;
                margin-right: 16px;
                    
                    svg{
                    width: 13px;
                    height: 13.5px;
                    margin-left: 4px;   
                    }
                }
                .sort-btn:hover{
                background: #E3E5E7;
                border: 1px solid #E3E5E7;
                }
                .search-container{
                position: relative;    
                display: flex;
                align-items: center;
                justify-content: flex-end;
                width: 200px;
                height: 34px;
                padding: 2px;
                padding-right: 0;
                border-radius: 8px;
                background-color: #F1F2F3;
                border: 1px solid #F1F2F3;
                margin-right: 12px;
                color: #18191C;
                transition: all .3s ease;

                    input{
                    position: relative;
                    left: -25px;    
                    width: 80%;
                    height: 100%;
                    border-radius: 8px;
                    padding: 0 30px 0 0px;
                    background-color: transparent;
                    font-size: 14px;
                    color: #18191C;
                    transition: background-color .3s;
                    outline: 0;
                    border: none;
                    }
                    .deleteAllSearchImg{
                    position: absolute;
                    width: 13.5px;
                    top: 50%;
                    transform: translateY(-50%);
                    right: 32px;
                    cursor: pointer;
                    }
                    .searchImg{
                    width: 16px;
                    height: 15px;
                    position: absolute;
                    cursor: pointer;
                    top: 50%;
                    transform: translateY(-50%);
                    right: 10px;
                    transition: opacity 0.3s ease;
                    }
                }
                .search-container:hover{
                border: 1px solid #00AEEC;
                }
                .search-container:focus-within{
                border: 1px solid #00AEEC;   
                }
                .clear-btn{
                display: flex;
                justify-content: center;
                align-items: center;
                height: 34px;
                min-width: 100px;
                padding: 0 12px;
                border-radius: 8px;
                font-size: 14px;
                line-height: 1;
                transition: all .3s ease;
                user-select: none;
                color: #18191C;
                background: white;
                border: 1px solid #E3E5E7;
                cursor: pointer;
                margin-right: 16px;

                    img{
                    width: 14px;
                    height: 14px;
                    margin-right: 3px;
                    }
                }
                .clear-btn:hover{
                background: #E3E5E7;
                border: 1px solid #E3E5E7;
                }
                .batch-manage-btn{
                display: flex;
                justify-content: center;
                align-items: center;
                height: 34px;
                min-width: 100px;
                padding: 0 12px;
                border-radius: 8px;
                font-size: 14px;
                line-height: 1;
                transition: all .3s ease;
                user-select: none;
                color: #18191C;
                background: white;
                border: 1px solid #E3E5E7;
                cursor: pointer;
                margin-right: 16px;

                    img{
                    width: 14px;
                    height: 13px;
                    margin-right: 3px;
                    }
                }
                .batch-manage-btn:hover{
                background: #E3E5E7;
                border: 1px solid #E3E5E7;
                }
                .check-all{
                position: relative;
                top: 0px;
                display: inline-flex;
                align-items: center;
                user-select: none;
                font-size: 14px;
                line-height: 1.5;
                color: #18191C;
                cursor: pointer;
                    .check-all-icon{
                    position: relative;
                    display: inline-block;
                    line-height: 1;

                        .check-input-box{
                        position: relative;
                        display: block;
                        width: 14px;
                        height: 14px;
                        -webkit-box-sizing: border-box;
                        box-sizing: border-box;
                        background-color: white;
                        border: 1px solid #E3E5E7;
                        border-radius: 4px;
                        -webkit-transition: all .3s ease-in-out;
                        transition: all .3s ease-in-out;
                        }
                        .check-input-box::after{
                        content: " ";
                        position: absolute;
                        top: 44%;
                        left: 21%;
                        display: table;
                        width: 4px;
                        height: 8px;
                        border: 2px solid white;
                        border-top: 0;
                        border-left: 0;
                        transform: rotate(45deg) scale(0.85) translate(-65%, -50%);                    
                        transition: all .3s ease-in-out;
                        }
                        .activeCheck{
                            border: 1px solid #00aeec;
                            background-color: #00aeec;
                        }
                    
                    }
                    .check-all-text{
                        padding: 0 8px;
                    }
                }
                .check-all:hover{
                    .check-all-icon{
                        .check-input-box{
                            border: 1px solid #00aeec;
                        }
                    }
                }
                .num{
                display: flex;
                align-items: center;    
                margin-left: 26px;
                min-width: 170px;
                font-size: 14px;
                color: #61666D;
                }
                .line{
                display: flex;
                align-items: center;
                
                    div{
                    margin: 0 36.5px;
                    width: 1px;
                    height: 18px;
                    background-color: #E3E5E7;
                    }
                }
                .delete-btn{
                display: flex;
                justify-content: center;
                align-items: center;
                height: 34px;
                min-width: 100px;
                padding: 0 12px;
                border-radius: 8px;
                font-size: 14px;
                line-height: 1;
                user-select: none;
                color: #8b8c8d;
                background: white;
                border: 1px solid #E3E5E7;
                cursor: not-allowed;
                margin-right: 16px;

                    svg{
                    width: 16px;
                    height: 14px;
                    margin-right: 2px;
                    color: #8b8c8d;
                    }
                }
                .activeDelete{
                color: #18191C;
                transition: all .3s ease;
                cursor: pointer;
                    
                    svg{
                        color: #18191C;
                    }
                }
                .activeDelete:hover{
                background-color: #E3E5E7;
                border: 1px solid #E3E5E7;
                }
            }
            .sort{
            width: 100%;;
            margin-top: 20px;
            margin-bottom: 10px;   
            display: flex;
            align-items: center;
            justify-content: left;

                div{
                display: flex;
                justify-content: center;
                align-items: center;
                min-width: 84px;
                height: 34px;
                flex-shrink: 0;
                color: #61666D;
                cursor: pointer;
                transition: all .3s;
                border-radius: 6px;
                margin-left: 12px; 
                }
                .active{
                color: #00AEEC;   
                background-color: #DFF6FD;
                }
            }
        }
    }
    .body{
     position: relative;
     display: flex;
     justify-content: center;
     margin: 8.5px auto;

        .video-list{
        margin-top: 3px;
        margin-bottom: 60px;   
        }
        .no-data{
                user-select: none;
                display: flex;
                height: 60vh;
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
    .fotter{
    display: flex;
    justify-content: center;
    text-align: center;
    padding-bottom: 15px;
    font-size: 14px;
    color: #9499A0;
    }
}

.delete-dialog-title{
    position: relative;
    text-align: center;
    font-size: 15.5px;
    font-weight: 550;
    color: #18191C;
}

.delete-dialog-content{
    position: relative;
    text-align: center;
    top: 10px;
    font-size: 13.5px;
    color: #666;
    transition: all 0.2s ease;
}

.delete-dialog-btn-container{
    position: relative;
    top: 35px;
    display: flex;
    justify-content: center;
}

.delete-dialog-confirm-btn{
    border: 1px solid #00AEEC;
    color: white;
    border-radius: 4px;
    min-width: 130px;
    height: 32px;
    cursor: pointer;
    font-size: 15.5px;
    margin: 0 6px;
    background-color: #00AEEC;
    transition: all 0.3s ease;
}

.delete-dialog-confirm-btn:hover{
    background-color: #40C5F1;
    border: 1px solid #40C5F1;
}

.delete-dialog-cancel-btn{
    border: 1px solid #E3E5E7;
    color: #18191C;
    border-radius: 4px;
    min-width: 130px;
    height: 32px;
    cursor: pointer;
    font-size: 15.5px;
    margin: 0 6px;
    background-color: white;
    transition: all 0.3s ease;
}

.delete-dialog-cancel-btn:hover{
    background-color: #E3E5E7;
}


</style>