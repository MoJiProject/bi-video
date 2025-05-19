<template>
    <div class="history-body">
        <div class="head">
            <div class="head-left">
                <img src="../img/历史记录图标.png">
                历史记录
            </div>
            <div class="head-right">
                <el-switch class="custom-switch1" v-model="historyFlag"/>
                记录浏览历史
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
                    <input type="text" v-model="keyWord" placeholder="搜索标题/up主昵称" maxlength="30" @keydown.enter="searchHistoryList">
                    <img
                        v-show="keyWord.length>0"
                        @mouseover="deleteAllSearchFlag = false"
                        @mouseleave="deleteAllSearchFlag = true"
                        @click="keyWord = ''"
                        :src="deleteAllSearchFlag ? '../img/删除搜索记录.png' : '../img/删除全部搜索hover.png'"
                        class="deleteAllSearchImg"/>
                        <img class="searchImg" src="../img/搜索.png" @click="searchHistoryList">         
                    </div>
                    <div v-show="!batchManageFlag" class="clear-btn" @click="deleteAllHistoryDialogFlag=true">
                    <img src="../img/清空历史记录.png">
                    清空历史
                    </div>
                    <div v-show="batchManageFlag" class="check-all" @click="checkAllF">
                        <span class="check-all-icon">
                            <span class="check-input-box" :class="{activeCheck:checkAllFlag}"></span>
                        </span>
                        <span class="check-all-text">全选</span>
                    </div>
                    <div v-show="batchManageFlag" class="num">已经选择 {{ store.autoVideoList.length }} 条历史记录</div>
                    <div v-show="batchManageFlag" class="line"><div></div></div>
                    <div v-show="batchManageFlag" class="delete-btn" @click="deleteCheckHistoryDialogFlag=store.autoVideoList.length>0" :class="{activeDelete: store.autoVideoList.length}">
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
            <el-timeline v-show="historyList.length" style="max-width: 1200px;">
                <el-timeline-item
                v-show="groupedHistoryList[timestamp]?.length"
                v-for="timestamp in timestampList"
                :key="timestamp"
                style="--el-font-size-small: 16px;--el-text-color-secondary: #61666D;"               
                placement="top"
                size="large"
                :hollow="true"
                :timestamp="timestamp"
                >
                <div style="margin-left: 32.5px;" :style="{paddingBottom: !paddingBottomF(timestamp)||timestamp==='一年前'? '100px' : '0px'}">
                    <imageList :data="groupedHistoryList[timestamp]" :batch-controls="batchManageFlag" :search-content="keyWord" />
                </div>
                </el-timeline-item>
            </el-timeline>
            <div v-show="!historyList.length&&dataFlag" class="no-data">
                <img src="../img/home_nodata.svg">
                <div>还没有观看记录</div>
            </div>
            <div v-show="dataFlag&&historyList.length" class="fotter">已经到底啦~</div>
        </div>
        <!-- 清空历史对话框 -->
        <el-dialog
        v-model="deleteAllHistoryDialogFlag"
        append-to-body
        width="320"
        :lock-scroll="false"
        align-center
        style="height: 152px; border-radius: 12px"
        >
            <div class="delete-dialog-title">确认要清空所有历史记录？</div>
            <div class="delete-dialog-content">
                记录清空后将不可恢复
            </div>
            <div class="delete-dialog-btn-container">
                <button class="delete-dialog-cancel-btn" @click="deleteAllHistoryDialogFlag=false">
                取消
            </button>
            <button
                class="delete-dialog-confirm-btn" @click="deleteAllHistoryF"
            >
                确认
            </button>
            </div>
        </el-dialog>
        <!-- 删除选中的历史记录 -->
        <el-dialog
        v-model="deleteCheckHistoryDialogFlag"
        append-to-body
        width="320"
        :lock-scroll="false"
        align-center
        style="height: 152px; border-radius: 12px"
        >
            <div class="delete-dialog-title">确认要删除选中的历史记录？</div>
            <div class="delete-dialog-content">
                记录删除后将不可恢复
            </div>
            <div class="delete-dialog-btn-container">
                <button class="delete-dialog-cancel-btn" @click="deleteCheckHistoryDialogFlag=false">
                取消
            </button>
            <button
                class="delete-dialog-confirm-btn" @click="deleteCheckHistoryF"
            >
                确认
            </button>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import {useGlobalStore} from "@/store/store";
import { computed, onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { putUserInfo } from '../api/user/index';
import { deleteAllHistory,selectHistoryList,deleteHistory } from '../api/history/index';
import { ElMessage } from "element-plus";
import imageList from "@/utils/imageList.vue";

const store = useGlobalStore();
const historyFlag=ref(false);
const sort=ref(1);//全部时间 1 今天 2 昨天 3 近一周 4自定义
const sortFlag=ref(false);
const custorTime=ref('');
const startTime=ref('');
const endTime=ref('');
const historyList=reactive([]);
const keyWord=ref('');
const deleteAllSearchFlag=ref(false);
const batchManageFlag=ref(false);
const deleteAllHistoryDialogFlag=ref(false);
const deleteCheckHistoryDialogFlag=ref(false);
const checkAllFlag=ref(false);
const pageNum=ref(1);
const searchFlag=ref(false);
const timestampList=[
    "今天",
    "昨天",
    "近一周",
    "一周前",
    "一个月前",
    "一年前"
];
const today = new Date();
const baseToday = new Date(today.getFullYear(), today.getMonth(), today.getDate());

const yesterday = new Date(baseToday);
yesterday.setDate(baseToday.getDate() - 1);

const oneWeekAgo = new Date(baseToday);
oneWeekAgo.setDate(baseToday.getDate() - 7);

const oneWeekLater = new Date(baseToday);
oneWeekLater.setDate(baseToday.getDate() + 7);

const oneMonthLater = new Date(baseToday);
oneMonthLater.setMonth(baseToday.getMonth() + 1);

const oneYearLater = new Date(baseToday);
oneYearLater.setFullYear(baseToday.getFullYear() + 1);

const dataFlag=ref(false);
const boxShadowFlag=ref(false);

onMounted(()=>{
    document.title="历史记录";
    window.addEventListener('scroll', handleScroll);
})

onUnmounted(()=>{
    window.removeEventListener('scroll', handleScroll);
})

//修改简介
async function putUserInfoF(){
store.userInformation.control=0;
store.userInformation.historyFlag=historyFlag.value?1:0;
putUserInfo(store.token,store.userInformation);
}

//初始化
let onceFlag=true;
watch(()=>store.userInformation.historyFlag,()=>{
    if(onceFlag)
    {
        historyFlag.value=store.userInformation.historyFlag?true:false;
        onceFlag=null;
    }
})

//修改是否记录浏览历史记录
watch(()=>historyFlag.value,()=>{
   putUserInfoF();
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
                historyList.length=0;
                selectHistoryListF();
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
    historyList.length=0;
    selectHistoryListF();
  }
  
})

//清空历史记录
function deleteAllHistoryF(){
    deleteAllHistory(store.userId,store.token).then(res=>{
        if(res.data.code===1){
            historyList.length=0;
            ElMessage({
            message: res.data.data,
            type: "info",
            plain: true,
            duration: 1700,
            });
            deleteAllHistoryDialogFlag.value=false;
        }
        else{
            ElMessage({
            message: '清除失败',
            type: "info",
            plain: true,
            duration: 1700,
            });
        }
    })
}

//查询历史记录
let selectHistoryListFlag=true;
function selectHistoryListF(){
    
    if(!selectHistoryListFlag)
    return;
    selectHistoryListFlag=false;
    selectHistoryList(store.token,store.userId,pageNum.value++,sort.value,startTime.value,endTime.value,keyWord.value).then(res=>{
        if(res.data.code===1){
            pushData(res.data.data);
            selectHistoryListFlag=true;
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
    const idSet = new Set(historyList.map(item => item.history.id)); // 获取已有数据的 ID
    const uniqueData = newData.filter(item => !idSet.has(item.history.id)); // 过滤掉重复的
    historyList.push(...uniqueData); // Vue 响应式更新
}

//初始化查询
let onceFlag2=true;
watch(()=>store.userId,()=>{
    if(onceFlag2&&store.userId){
        onceFlag2=null;
        selectHistoryListF();
    }
})

//搜索历史记录
function searchHistoryList(){
    pageNum.value=1;
    historyList.length=0;
    searchFlag.value=true;
    dataFlag.value=false;
    selectHistoryListF();
}

//监视搜索关键字
watch(keyWord,()=>{
    if(keyWord.value.length===0&&searchFlag.value){
        pageNum.value=1;
        searchFlag.value=false;
        historyList.length=0;
        dataFlag.value=false;
        selectHistoryListF();
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
       let ids=historyList.map(item=>item.history.id);
       store.setAutoVideoList(ids);
    }
    else{
       store.autoVideoList.length=0; 
    }
}

//监视选中视频的数量
watch(()=>store.autoVideoList.length,async(newValue)=>{
    if(newValue===historyList.length){
        checkAllFlag.value=true;
    }
    else{
        checkAllFlag.value=false;
    }
    if(newValue===1&&!batchManageFlag.value)
    {
        deleteCheckHistoryF();
    }
})

//检查日期
function isSameDay(d1, d2) {
return d1.getFullYear() === d2.getFullYear() &&
        d1.getMonth() === d2.getMonth() &&
        d1.getDate() === d2.getDate()
}

//计算显示
const groupedHistoryList = computed(() => {
  const groups = {
    "今天": [],
    "昨天": [],
    "近一周": [],
    "一周前": [],
    "一个月前": [],
    "一年前": [],
  }

  const now = new Date()
  const baseToday = new Date(now.toDateString()) // 清除时间部分
  const yesterday = new Date(baseToday)
  yesterday.setDate(yesterday.getDate() - 1)

  const oneWeekAgo = new Date(baseToday)
  oneWeekAgo.setDate(oneWeekAgo.getDate() - 7)

  const oneMonthAgo = new Date(baseToday)
  oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1)

  const oneYearAgo = new Date(baseToday)
  oneYearAgo.setFullYear(oneYearAgo.getFullYear() - 1)

  historyList.forEach(item => {
    const date = new Date(new Date(item.history.watchVideoDate).toDateString())

    if (isSameDay(date, baseToday)) {
      groups["今天"].push(item)
    } else if (isSameDay(date, yesterday)) {
      groups["昨天"].push(item)
    } else if (date < baseToday && date >= oneWeekAgo) {
      groups["近一周"].push(item)
    } else if (date < oneWeekAgo && date >= oneMonthAgo) {
      groups["一周前"].push(item)
    } else if (date < oneMonthAgo && date >= oneYearAgo) {
      groups["一个月前"].push(item)
    } else if (date < oneYearAgo) {
      groups["一年前"].push(item)
    }
  })

  return groups
})


//计算paddingBottom
function paddingBottomF(timestamp){
    
    if(timestamp==="今天"&&(groupedHistoryList["昨天"]?.length||groupedHistoryList["近一周"]?.length||groupedHistoryList["一周前"]?.length||groupedHistoryList["一个月前"]?.length||groupedHistoryList["一年前"]?.length))
      return true;
    else if(timestamp==="昨天"&&(groupedHistoryList["近一周"]?.length||groupedHistoryList["一周前"]?.length||groupedHistoryList["一个月前"]?.length||groupedHistoryList["一年前"]?.length))
      return true;
    else if(timestamp==="近一周"&&(groupedHistoryList["一周前"]?.length||groupedHistoryList["一个月前"]?.length||groupedHistoryList["一年前"]?.length))
      return true;
    else if(timestamp==="一周前"&&(groupedHistoryList["一个月前"]?.length||groupedHistoryList["一年前"]?.length))
      return true;
    else if(timestamp==="一个月前"&&groupedHistoryList["一年前"]?.length)
      return true;
    else
      return false;
}

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
        selectHistoryListF();
    }
}

//删除选中的历史记录
function deleteCheckHistoryF(){
    if(store.autoVideoList.length===0){
        ElMessage({
            message: '请先选择要删除的记录',
            type: "info",
            plain: true,
            duration: 1700,
        });
        return;
    }

    deleteHistory(store.token,store.userId,store.autoVideoList).then(res=>{
        if(res.data.code===1){
            historyList.forEach((item,index)=>{
                if(store.autoVideoList.includes(item.history.id)){
                    historyList.splice(index,1);
                }
            })
            ElMessage({
                message: res.data.data,
                type: "info",
                plain: true,
                duration: 1700,
            });
            deleteCheckHistoryDialogFlag.value=false;
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

.history-body{
min-width: fit-content;
background-color: white;
padding-top: 30px;
min-height: calc(100vh - 64px);
max-width: 2560px;

    .head{
    width: calc(1152px);
    margin-left: auto;
    margin-right: auto;
    padding-left: 74px;
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

            img{
            width: 27px;
            height: 27px;
            transform: translateY(2px);
            margin-right: 18px;
            }
        }
        .head-right{
        display: flex;
        align-items: center;
        font-size: 14px;
        padding-right: 70px;
        color: #18191C;
        user-select: none;
            
            .custom-switch1{
            margin-right: 8px;
            }
        }
    }
    .control-container{
     position: sticky;
     padding-top: 20px;
     padding-bottom: 5px;
     top: 0;
     background-color: white;
     z-index: 900;
     width: 100%;
        .controls{
        width: calc(1152px);
        min-height: 46px;
        padding-left: 74px;
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
     margin-top: 8.5px;   
     padding-left: 148px;
     
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