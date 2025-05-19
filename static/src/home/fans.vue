<template>
    <div class="fans-container">
        <div class="aside">
            <span v-if="store.userId!==null&&store.userId===userId" class="aside-head">我的关注</span>
            <span v-else class="aside-head">TA的关注</span>
            <div :class="{activeMenu: true}" @click="changeHomeMenu(8)">
                <span class="aside-content">
                    <svg data-v-72214e68="" class="vui_icon fans-sidebar-item__icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24" xmlns:xlink="http://www.w3.org/1999/xlink"><path d="M17.2036 6.10902C17.3632 5.72677 17.8024 5.54623 18.1846 5.70577C20.35055 6.60978 21.87505 8.74844 21.87505 11.24495C21.87505 13.51545 20.6136 15.49025 18.75685 16.50865C18.3937 16.70785 17.9378 16.57495 17.7386 16.2118C17.5394 15.8486 17.6723 15.3927 18.0355 15.1935C19.4314 14.4278 20.37505 12.94605 20.37505 11.24495C20.37505 9.37462 19.23385 7.76911 17.6069 7.09004C17.2246 6.93049 17.0441 6.49127 17.2036 6.10902z" fill="currentColor"></path><path d="M17.7553 15.4977C17.9578 15.1364 18.4149 15.00765 18.7762 15.21015C20.86785 16.3824 21.5243 18.1724 21.6619 18.62735C21.78185 19.0238 21.5577 19.44245 21.1612 19.5624C20.76475 19.68235 20.3461 19.4582 20.2262 19.06175C20.14655 18.7986 19.662 17.42605 18.04285 16.51865C17.68155 16.31615 17.5528 15.85905 17.7553 15.4977z" fill="currentColor"></path><path d="M9.125 4C6.0184 4 3.5 6.5184 3.5 9.625C3.5 11.7516 4.67988 13.6038 6.42431 14.5606C6.78748 14.7598 6.9204 15.2157 6.7212 15.5789C6.522 15.94205 6.06611 16.075 5.70294 15.8758C3.49771 14.6662 2 12.321 2 9.625C2 5.68997 5.18997 2.5 9.125 2.5C13.06005 2.5 16.25 5.68997 16.25 9.625C16.25 12.321 14.7523 14.6662 12.54705 15.8758C12.1839 16.075 11.728 15.94205 11.5288 15.57885C11.3296 15.2157 11.4625 14.7598 11.8257 14.5606C13.5701 13.6038 14.75 11.7516 14.75 9.625C14.75 6.5184 12.2316 4 9.125 4z" fill="currentColor"></path><path d="M6.70177 14.868C6.90428 15.2293 6.77552 15.6864 6.41418 15.8889C4.39752 17.0191 3.79067 18.7305 3.68779 19.07055C3.56784 19.467 3.14921 19.6912 2.75274 19.5712C2.35628 19.4513 2.13211 19.03265 2.25206 18.6362C2.41297 18.10435 3.19161 15.97545 5.68084 14.58035C6.04218 14.37785 6.49926 14.50665 6.70177 14.868zM11.5484 14.868C11.75095 14.50665 12.208 14.37785 12.56935 14.58035C15.0586 15.97545 15.8372 18.10435 15.9981 18.6362C16.1181 19.03265 15.89395 19.4513 15.4975 19.5712C15.10095 19.6912 14.6824 19.467 14.5624 19.07055C14.4595 18.7305 13.8527 17.0191 11.836 15.8889C11.47465 15.6864 11.3459 15.2293 11.5484 14.868z" fill="currentColor"></path></svg>
                    <span>全部关注</span>
                </span>
                <span>{{ followTotal }}</span>  
            </div>
            <span v-if="store.userId!==null&&store.userId===userId" class="aside-head">我的粉丝</span>
            <span v-else class="aside-head">TA的粉丝</span>
            <div>
                <span class="aside-content">
                    <img src="../img/粉丝蓝.png">
                    <span>{{ store.userId!==null&&store.userId===userId? '我的粉丝' : 'TA的粉丝' }}</span>
                </span>
                <span>{{ fansTotal }}</span>
            </div>
        </div>
        <div v-show="store.userId&&loadMore" class="class-loading">
               <img src="../img/loading-blue.gif">
               <span>拼命加载中...</span>
        </div>
        <div v-if="store.userId&&(fansTotal>0||searchWord.length>0)&&!loadMore" class="content">
            <div v-if="store.userId!==null&&store.userId!==userId" class="title">TA的粉丝</div>
            <div v-else class="title">我的粉丝</div>
            <div class="search-container">
                <div class="input-container">
                    <div>
                        <input type="text" placeholder="输入关键词" maxlength="20" v-model="searchWord" @keydown.enter="fansOrFollowF2">
                    </div>
                    <img @click="fansOrFollowF2" src="../img/搜索.png">
                    <svg v-show="searchWord.length>0" @click="searchWord=''" class="vui_icon vui_input-clear-base__icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" width="16" height="16" xmlns:xlink="http://www.w3.org/1999/xlink"><path d="M8 1.3333333333333333C4.318099999999999 1.3333333333333333 1.3333333333333333 4.318099999999999 1.3333333333333333 8C1.3333333333333333 11.681899999999999 4.318099999999999 14.666666666666666 8 14.666666666666666C11.681899999999999 14.666666666666666 14.666666666666666 11.681899999999999 14.666666666666666 8C14.666666666666666 4.318099999999999 11.681899999999999 1.3333333333333333 8 1.3333333333333333zM5.64258 6.3496C5.4473199999999995 6.1543399999999995 5.4473199999999995 5.837753333333333 5.64258 5.6424933333333325C5.837846666666666 5.447233333333333 6.154426666666667 5.447233333333333 6.349693333333333 5.6424933333333325L8 7.2928L9.650333333333332 5.6424933333333325C9.845566666666667 5.447233333333333 10.162166666666666 5.447233333333333 10.357433333333333 5.6424933333333325C10.552666666666667 5.837753333333333 10.552666666666667 6.1543399999999995 10.357433333333333 6.3496L8.7071 7.9999L10.357433333333333 9.650233333333333C10.552666666666667 9.845466666666667 10.552666666666667 10.162066666666666 10.357433333333333 10.357333333333333C10.162166666666666 10.5526 9.845566666666667 10.5526 9.650333333333332 10.357333333333333L8 8.706999999999999L6.349693333333333 10.357333333333333C6.154426666666667 10.5526 5.837846666666666 10.5526 5.64258 10.357333333333333C5.4473199999999995 10.162066666666666 5.4473199999999995 9.845466666666667 5.64258 9.650233333333333L7.2928999999999995 7.9999L5.64258 6.3496z" fill="currentColor"></path></svg>
                </div>
            </div>
            <div class="fans-list-container">
                <div :class="{activeFansItem: fans.newFans===fans.id&&store.userId===userId}" class="fans-item" v-for="fans in fansList" :key="fans.id">
                    <a class="fans-item-left" :href="'./home?homeMenu=1&userId='+fans.id" target="_blank">
                        <img :src="fans.avatarAddress">
                        <userInfo2 @click.prevent class="user" :userInfo="fans"/>
                    </a>
                    <div class="fans-item-right">
                        <a class="user-name" :href="'./home?homeMenu=1&userId='+fans.id" target="_blank"><div>{{ fans.userName }}</div></a>
                        <div class="introduce"><div>{{ fans.introduce }}</div></div>
                        <div class="controls">
                            <div v-show="fans.isFollowFlag===0" class="fans-btn" @click="addFollowF(fans)">
                                <svg data-v-2864ceb2="" class="vui_icon fans-btn__trigger-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" width="16" height="16" xmlns:xlink="http://www.w3.org/1999/xlink"><path d="M7.998750000000001 2.083333333333333C8.412883333333333 2.083333333333333 8.748750000000001 2.4191233333333333 8.748750000000001 2.833333333333333L8.748750000000001 7.249866666666667L13.166666666666666 7.249866666666667C13.5809 7.249866666666667 13.916666666666666 7.585649999999999 13.916666666666666 7.999866666666667C13.916666666666666 8.4141 13.5809 8.749866666666666 13.166666666666666 8.749866666666666L8.748750000000001 8.749866666666666L8.748750000000001 13.166666666666666C8.748750000000001 13.5809 8.412883333333333 13.916666666666666 7.998750000000001 13.916666666666666C7.584533333333333 13.916666666666666 7.248750000000001 13.5809 7.248750000000001 13.166666666666666L7.248750000000001 8.749866666666666L2.833333333333333 8.749866666666666C2.4191233333333333 8.749866666666666 2.083333333333333 8.4141 2.083333333333333 7.999866666666667C2.083333333333333 7.585649999999999 2.4191233333333333 7.249866666666667 2.833333333333333 7.249866666666667L7.248750000000001 7.249866666666667L7.248750000000001 2.833333333333333C7.248750000000001 2.4191233333333333 7.584533333333333 2.083333333333333 7.998750000000001 2.083333333333333z" fill="currentColor"></path></svg>
                                {{ store.userId===store.homeUserInformation.id?"回关":"关注" }}</div>
                            <div v-show="fans.isFollowFlag===1&&fans.isFansFlag===1" class="other-fans-btn" @click="deleteFollowF(fans)">
                                <svg data-v-2864ceb2="" class="vui_icon fans-btn__trigger-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" width="16" height="16" xmlns:xlink="http://www.w3.org/1999/xlink"><path d="M1.75 8C1.75 7.585766666666665 2.08579 7.25 2.5 7.25L13.5 7.25C13.914233333333332 7.25 14.25 7.585766666666665 14.25 8C14.25 8.414233333333334 13.914233333333332 8.75 13.5 8.75L2.5 8.75C2.08579 8.75 1.75 8.414233333333334 1.75 8z" fill="currentColor"></path><path d="M1.75 4C1.75 3.5857900000000003 2.08579 3.25 2.5 3.25L13.5 3.25C13.914233333333332 3.25 14.25 3.5857900000000003 14.25 4C14.25 4.414209999999999 13.914233333333332 4.75 13.5 4.75L2.5 4.75C2.08579 4.75 1.75 4.414209999999999 1.75 4z" fill="currentColor"></path><path d="M1.75 12C1.75 11.585766666666666 2.08579 11.25 2.5 11.25L13.5 11.25C13.914233333333332 11.25 14.25 11.585766666666666 14.25 12C14.25 12.414233333333332 13.914233333333332 12.75 13.5 12.75L2.5 12.75C2.08579 12.75 1.75 12.414233333333332 1.75 12z" fill="currentColor"></path></svg>
                                已互粉</div>
                            <div class="send-msg-btn"><svg  xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M176 416a112 112 0 1 1 0 224 112 112 0 0 1 0-224m336 0a112 112 0 1 1 0 224 112 112 0 0 1 0-224m336 0a112 112 0 1 1 0 224 112 112 0 0 1 0-224"></path></svg>
                                <div class="send-msg-btn-text" @click="addDialogueF(fans.id)">
                                        <div>发私信</div>
                                </div>
                            </div>    
                        </div>
                    </div>
                </div>
            </div>
            <div v-show="fansTotal>0&&!loadMore" class="page-container">
                <el-pagination
                :current-page="pageNum"
                :page-size="20"
                layout="prev, pager, next"
                :total="fansTotal"
                :background="true"
                @current-change="handleCurrentChange"
                />
                <span>共 {{ Math.ceil(fansTotal/20) }} 页 / {{ fansTotal }} 个，跳至<input type="number" @keydown.enter="handleCurrentChange2">页</span>
            </div>
        </div>
        <div v-else-if="store.userId&&fansTotal===0&&!loadMore" class="not-data-container">
                    <img src="../img/home_nodata.svg">
                    <div v-if="store.userId!==null&&store.userId===userId">你还没关注任何人</div>
                    <div v-else>TA还没关注任何人</div>
        </div>
        <div v-if="!store.userId" class="not-login-container">
            <img src="../img/home_nodata.svg">
            <div>请先登录</div>
        </div>
    </div>
</template>

<script setup>
import { useGlobalStore } from "@/store/store";
import { onMounted, onUnmounted, reactive, ref, watch } from "vue";
import {fansOrFollow,putNewFans} from "@/api/user";
import {addFollow,deleteFollow} from "@/api/user";
import { ElMessage } from "element-plus";
import userInfo2 from "@/utils/userInfo2";
import {addDialogue} from '../api/privateMessage/index';

const store = useGlobalStore();
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || null;
const pageNum=ref(1);
const followTotal=ref(0);
const fansTotal=ref(0);
const searchWord=ref("");
const loadMore=ref(false);
let searchFlag=false;
const fansList=reactive([]);

onMounted(async()=>{
    if(store.homeUserInformation.id&&store.userId){
        fansOrFollowF();
        await fansOrFollowF2();
        putNewFans(store.token,store.userId);
        store.setHomeLoad(true,'homeBody');
    } 

})

onUnmounted(()=>{
    store.setHomeLoad(false,"homeBody");
})

// 监听搜索词
watch(searchWord,()=>{
    
    if(searchWord.value.length===0&&searchFlag)
      {
        fansOrFollowF2();
      }
})

//监视页数
watch(pageNum,()=>{
    fansOrFollowF();
})

// 更改当前页
function handleCurrentChange(val){
    pageNum.value=val;
}

// 更改当前页
function handleCurrentChange2(event){

    if(event.target.value!==''&&event.target.value<=Math.ceil(fansTotal.value/20)&&event.target.value>=1)
        {
            pageNum.value=parseInt(event.target.value);
        }
}

//跳转到指定页
function changeHomeMenu(homeMenu){
    if(store.homeMenu!==homeMenu){
        if(homeMenu===8&&(store.homeUserInformation.publicFollowList||userId===store.userId)){
            store.setHomeMenu(homeMenu,userId,true);
        }else{
            ElMessage({
            message: "由于该用户隐私设置，关注列表不可见",
            type: "info",
            plain: true,
            duration: 1700,
            });
            return;
        }
    }
}

//查询粉丝或关注列表
function fansOrFollowF(){

fansOrFollow(userId,store.userId,8,pageNum.value,searchWord.value,store.token).then(res=>{
    followTotal.value=res.data.data.total;

})

}

//查询粉丝或关注列表
async function fansOrFollowF2(){
    loadMore.value=true;
    let date=new Date();
    if(searchWord.value.length===0)
      searchFlag=false;
    else
      searchFlag=true;
fansOrFollow(userId,store.userId,9,pageNum.value,searchWord.value,store.token).then(res=>{
    fansTotal.value=res.data.data.total;
    fansList.length=0;
    Object.assign(fansList,res.data.data.records);
    let date2=new Date();
    setTimeout(() => {
            loadMore.value=false;
    }, date2 - date<300?300:date2 - date);
})

}

//监视store
let onceFlag=true;
watch([()=>store.homeUserInformation.id,()=>store.userId],async()=>{
    if(onceFlag&&store.homeUserInformation.id&&store.userId){
        onceFlag=false;
        fansOrFollowF();
        await fansOrFollowF2();
        putNewFans(store.token,store.userId);
        store.setHomeLoad(true,'homeBody');
    }
})

//关注用户
function addFollowF(fans){

if(!store.userId)
return;

if(fans.id===store.userId)
{
    ElMessage({
        message: "不能关注自己(ง •̀_•́)ง",
        type: "info",
        plain: true,
        duration: 1700,
        });
    return;
}

addFollow(store.token,store.userId,fans.id).then(res=>{
      if(res.data.code===1){
         fans.isFollowFlag=1;
         ElMessage({
        message: "关注成功(ง •̀_•́)ง",
        type: "info",
        plain: true,
        duration: 1700,
        });
       }
})    
}

//取消关注用户
function deleteFollowF(fans){

if(!store.userId)
return;

deleteFollow(store.token,store.userId,fans.id).then(res=>{
      if(res.data.code===1){
         fans.isFollowFlag=0;
         ElMessage({
        message: "取消关注成功(ง •̀_•́)ง",
        type: "info",
        plain: true,
        duration: 1700,
        });
       }
})
}

//添加私信对话并跳转
async function addDialogueF(dialogueId) {

if (!store.userId)
return;

let dialogue = {
userId: store.userId,
dialogueId,
}
addDialogue(store.token,dialogue).then(res=>{

if(res.data.code===1)
{
window.open(
`./message?dialogueId=${dialogueId}`,
"_blank",
);
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

.fans-container{
    position: absolute;
    display: flex;
    top: 0px;
    padding: 30px 60px;
    line-height: 1;

    .aside{
        position: sticky;
        user-select: none;
        top: 80px;
        width: 190px;
        height: 130px;
        flex-shrink: 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 16px;

        .aside-head{
        display: flex;
        width: 190px;
        font-weight: 500;
        font-size: 15.5px;
        justify-content: space-between;
        cursor: pointer;
        margin-bottom: 10px;
        }

        div{
        width: 190px;
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
        margin-bottom: 20px;
        .aside-content{
            display: flex;
            align-items: center;
            justify-content: center;
            svg{
                margin-right: 10px;
            }
            img{
                width: 18px;
                height: 18px;
                margin-right: 10px;
            }
            span{
                font-weight: 500;
            }
        }

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
    .content{
        position: relative;
        width: 1099px;
        padding-left: 20px;
        padding-bottom: 70px;
        
        .title{
        font-size: 24px;
        font-weight: 600;
        color: #18191C;
        line-height: 34px;
        }
        .search-container{
            position: relative;
            width: 100%;
            margin-top: 32px;
            height: 34px;

            .input-container{
            position: absolute;
            right: 0px;
            width: 235px;
            height: 34px;

            div{
                width: 100%;
                height: 100%;
                overflow: hidden;
                display: inline-flex;
                -webkit-box-flex: 1;
                -ms-flex-positive: 1;
                flex-grow: 1;
                position: relative;
                background-color: white;
                border: 1px solid #E3E5E7;
                font-size: 14px;
                border-radius: 6px;
                padding: 0 10px;

                input{
                    padding: 0 45px 0 0;
                    display: inline-flex;
                    outline: 0;
                    width: 100%;
                    border: none;
                    outline: none;
                    text-align: inherit;
                    font-family: inherit;
                    box-sizing: border-box;
                    scrollbar-width: none;
                    background-color: transparent;
                    color: #18191C;
                }
            }
            img{
                position: absolute;
                right: 17px;
                top: 50%;
                transform: translateY(-50%);
                width: 16px;
                height: 16px;
                cursor: pointer;
            }
            svg{
                position: absolute;
                right: 45px;
                top: 50%;
                transform: translateY(-50%);
                color: #E3E5E7;
                transition: color .3s ease;
                cursor: pointer;
            }
            svg:hover{
                color: #C9CCD0;
            }
            }
        }
        .fans-list-container{
        margin-top: 30px;   
        column-gap: 38px;
        row-gap: 20px;
        display: grid;
        grid-template-columns: repeat(3, 1fr);

            .fans-item{
            display: flex;
            height: 118px;
            flex: none;
            align-items: center;
            justify-content: space-between;
            padding: 16px 0 18px;

            .fans-item-left{
            position: relative;
            cursor: pointer;
            display: block;
            width: 80px;
            height: 80px;
            border-radius: 50%;

              img{
                width: 80px;
                height: 80px;
                border-radius: 50%;
              }
              .user{
                opacity: 0;
                visibility: hidden;
                z-index: 100;
                transition: all .3s ease;
                top: 50%;
                left: 100%;
              }
            }
            .fans-item-left:hover{
                .user{
                    transition-delay: 0.3s;
                    visibility: visible;
                    opacity: 1;
                }
            }
            .fans-item-right{
            margin-left: 18px;
            display: flex;
            flex-direction: column;
            flex: 1;

                .user-name{
                font-size: 16px;
                font-weight: 500;
                color: #18191C;
                    div{
                    display: -webkit-box;
                    -webkit-box-orient: vertical;
                    -webkit-line-clamp: 1;
                    transition: color .3s ease;
                    }
                    div:hover{
                        color: #00AEEC;
                    }
                }
                .introduce{
                margin-top: 6px;
                margin-bottom: 8px;
                font-size: 12px;
                color: #61666D;
                    div{
                        display: -webkit-box;
                        -webkit-box-orient: vertical;
                        -webkit-line-clamp: 1;
                        overflow: hidden; 
                    }
                }
                .controls{
                display: flex;
                align-items: center;
                user-select: none;

                    .fans-btn{
                    cursor: pointer;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    width: 96px;
                    height: 35px;
                    border-radius: 6px;
                    font-size: 14px;
                    font-weight: 700;
                    color: #00AEEC;
                    border: 1px solid #00AEEC;
                    background-color: transparent;
                    transition: background-color .3s;

                    svg{
                        margin-right: 4px;
                        width: 16px;
                        height: 16px;
                    }
                    }
                    .fans-btn:hover{
                        background-color: #DFF6FD;
                    }
                    .other-fans-btn{
                    cursor: pointer;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    width: 96px;
                    height: 35px;
                    border-radius: 6px;
                    font-size: 14px;
                    font-weight: 700;
                    color: #9499A0;
                    background-color: #F6F7F8;
                    transition: background-color .3s;

                    svg{
                        margin-right: 4px;
                        width: 16px;
                        height: 16px;
                    }
                    }
                }
                .send-msg-btn{
                position: relative;    
                width: 24px;
                height: 24.5px;  
                color: #9499a0;
                display: flex;
                justify-content: center;
                align-items: center;
                opacity: 0;
                margin-left: 12px;
                cursor: pointer;
                border-radius: 4px;
                background-color: white;
                transition: background-color .3s ease,opacity .3s ease;

                    svg{
                        width: 19px;
                        height: 20px;
                        color: #9499a0;
                        transform: rotate(90deg);
                    }
                    .send-msg-btn-text{
                    width: 123px;
                    height: 74px;
                    z-index: 10000;
                    position: absolute;
                    bottom: -84px;
                    opacity: 0;
                    transition: opacity .3s ease,visibility .3s ease;
                    visibility: hidden;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    background-color: white;
                    border: 1px solid #E3E5E7;
                    box-shadow: 0 8px 40px rgba(0, 0, 0, .1);
                    padding: 4px;
                    border-radius: 12px;

                        div{
                        width: 96%;
                        text-align: center;    
                        cursor: pointer;
                        font-size: 14px;
                        color: #61666D;
                        transition: background-color .3s ease;
                        border-radius: 8px;
                        height: 40px;
                        line-height: 40px;
                        position: relative;
                        background-color: white;
                        }
                        div:hover{
                            background-color: #F1F2F3;
                        }
                    }
                }
                .send-msg-btn:hover{
                    background-color: #F1F2F3;

                    .send-msg-btn-text{
                        transition-delay: 0.3s;
                        opacity: 1;
                        visibility: visible;
                    }
                }
            }
            }
            .activeFansItem{
               animation: newFans 5s ease forwards;
            }
        }
        .fans-list-container:hover{
            .fans-item{
                .fans-item-right{
                    .send-msg-btn{
                    opacity: 1;
                    }
                }
            }   
        }
        .page-container{
            margin-top: 60px;
            position: relative;
            display: flex;
            justify-content: center;
            align-items: center;

            span{
             color: #18191C;
             font-size: 13px;
             margin-left: 44px; 
                input{
                width: 50px;
                height: 34px;
                overflow: hidden;
                display: inline-flex;
                flex-grow: 1;
                outline: none;
                position: relative;
                padding: 0 12px;
                background-color: white;
                border: 1px solid #E3E5E7;
                font-size: 14px;
                border-radius: 6px;
                transition: all .3s ease;
                padding: 0 10px;
                }
                input:hover{
                    border-color: #00AEEC;
                }
                input:focus{
                    border-color: #00AEEC;
                }
                input::-webkit-inner-spin-button,
                input::-webkit-outer-spin-button {
                -webkit-appearance: none;
                }
            }
        }
    }
    .class-loading{
    position: relative;
    width: 1099px;
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
    .not-data-container{
    user-select: none;
    width: 1139px;
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
    .not-login-container{
    user-select: none;
    width: 1139px;
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

}

@keyframes newFans{
    0%{
       background-color: rgba(199, 199, 199, 0.3);
    }100%{
       background-color: white;
    }
}

</style>