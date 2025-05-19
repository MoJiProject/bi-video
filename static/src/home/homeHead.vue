<template>
   <div v-show="store.homeUserInformation.id" class="home-head" :style="{backgroundImage: store.homeUserInformation.backgroundAddress? `url(${store.homeUserInformation.backgroundAddress})`:'url(../img/主页背景图.webp)'}" @contextmenu="changeBackgroundMenu">
        <mainhead :head2-flag="false" :login-dialog-visible-flag="loginDialogVisibleFlag"/>
        <div class="overlay"></div>
        <div class="overlay2"></div>
        <a v-if="store.userId&&store.userId===store.homeUserInformation.id" href="./account?accountMenu=3" target="_blank" v-show="handleContextMenuVisible" class="menu" :style="{top:handleContextMenuPosition.y+'px',left:handleContextMenuPosition.x+'px'}">更换背景</a>
        <div class="user-info">
            <a v-if="store.userId&&store.userId===store.homeUserInformation.id" href="./account?accountMenu=2" target="_blank">
                <div class="user-avatar-link">
                    <img v-show="store.homeUserInformation?.avatarAddress" class="user-avatar" :src="store.homeUserInformation?.avatarAddress">
                    <div v-if="store.userId===store.homeUserInformation?.id">更换头像</div>
                </div>
            </a>
            <div v-else class="user-avatar-link">
                    <img v-show="store.homeUserInformation?.avatarAddress" class="user-avatar" :src="store.homeUserInformation?.avatarAddress">
                    <div v-if="store.userId===store.homeUserInformation?.id">更换头像</div>
            </div>
            <div class="user-info-top">
                <span class="user-name">{{ store.homeUserInformation?.userName }}</span>
                <img v-if="store.homeUserInformation.grade" class="level-icon" :src="'../img/'+store.homeUserInformation.grade+'级.png'">
                <img v-if="store.homeUserInformation?.gender===1" class="gender-icon" src="../img/man2.png">
                <img v-else-if="store.homeUserInformation?.gender===2" class="gender-icon" src="../img/woman2.png">
            </div>
            <div v-show="!introduceEditFlag&&store.userId!==store.homeUserInformation?.id" class="user-info-introduce">
                    <el-tooltip
                    popper-class="custom-tooltip"
                    class="dynamicContent-item-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="store.homeUserInformation.introduce"
                    placement="bottom"
                    :offset="14"
                    :show-arrow="false"
                    :hide-after="0"
                    >
                    {{ store.homeUserInformation.introduce }}
                    </el-tooltip>
            </div>   
            <div v-show="!introduceEditFlag&&store.userId===store.homeUserInformation?.id" class="user-info-introduce2" @click="introduceEditFlag=true" @contextmenu="introduceEditFlag=true">{{ store.homeUserInformation.introduce }}</div>
            <input v-if="store.userId===store.homeUserInformation?.id" ref="introduceInput" v-show="introduceEditFlag" v-model="introduce" @focus="introduce=store.homeUserInformation.introduce" @blur="introduceEditFlag=false,putUserInfoF()" @keydown.enter="introduceEditFlag=false,putUserInfoF()" type="text" placeholder="编辑个性签名" maxlength="250">
        </div> 
        <div v-if="(store.userId!==null&&store.userId!==userId)||store.userId===null" class="control-container">
            <div v-show="!store.homeUserInformation.isFollowFlag" class="follow" @click="followF"><img src="../img/加关注.png">关注</div>
            <div v-show="store.homeUserInformation.isFollowFlag===1" class="delete-follow" @click="unFollowF"><img src="../img/取消关注白.png">已关注</div>
            <div class="send-message" @click="addDialogueF">发消息</div>
        </div>
   </div>
</template>

<script setup>
import { nextTick, onMounted,onUnmounted,ref, watch } from 'vue';
import mainhead from '@/components/mainHead.vue';
import {useGlobalStore} from "@/store/store";
import { ElMessage } from "element-plus";
import {getCollectsClassify} from "@/api/home/index";
import {ChecklLogin,getUserIp,addFollow,deleteFollow,checkFollow,selectUserInfo,putUserInfo} from '../api/user/index';
import {addDialogue} from '../api/privateMessage/index';

const store = useGlobalStore();
const introduce = ref("");
const introduceInput = ref(null);
const introduceEditFlag = ref(false);
const homeMenu = parseInt(new URL(window.location).searchParams.get("homeMenu")) || 1;
const userId = parseInt(new URL(window.location).searchParams.get("userId")) || null;
const loginDialogVisibleFlag=ref(0);
const handleContextMenuPosition=ref({x:0,y:0});
const handleContextMenuVisible=ref(false);

onMounted(async() => {

    await getUserIpF();
    await ChecklLoginF();
    await selectUserInfoF();
    await checkFollowF();
    getCollectsClassifyF();
    store.setHomeLoad(true,"homeHead");
    window.addEventListener("click",()=>{
        handleContextMenuVisible.value=false;
    })
})

onUnmounted(()=>{
    store.setHomeLoad(false,"homeHead");
})

//获取用户ip和token
async function getUserIpF(){
      
   await getUserIp().then(response => {
      if(response.data.code === 1)
        store.setUserIp(response.data.data.userIp);
        store.setToken(response.data.data.token);
    });
}

//检查是否登录
async function ChecklLoginF(){

  await ChecklLogin(store.userIp).then(response=>{
    if (response.data.code === 1) {
        store.setUserId(response.data.data.id);
    } else {
        store.setUserId(null);
    }
    })
}

// 获取用户信息
async function selectUserInfoF(){
 
    if(userId===null||userId===undefined||userId.length===0)
    window.location.href="/";
    else
    selectUserInfo(userId).then(res => {
        if(res.data.code === 1){
            store.setHomeUserInformation(res.data.data);
            if(homeMenu === 1 || homeMenu === 5 || homeMenu === 6 || homeMenu === 7)
            document.title=res.data.data.userName+"的个人空间-"+res.data.data.userName+"个人主页-哔哩哔哩视频";
            else if(homeMenu === 2)
            document.title=res.data.data.userName+"的个人动态-"+res.data.data.userName+"动态记录-哔哩哔哩视频";
            else if(homeMenu === 3)
            document.title=res.data.data.userName+"的投稿视频-"+res.data.data.userName+"视频分享-哔哩哔哩视频";
            else if(homeMenu === 4)
            document.title=res.data.data.userName+"的视频专辑-"+res.data.data.userName+"视频合集-哔哩哔哩视频";
        }
        else{
            window.location.href="/";
        }
    })

}

//修改简介
async function putUserInfoF(){

    if(store.homeUserInformation.introduce===introduce.value)
    return;

    store.homeUserInformation.introduce=introduce.value;
    store.homeUserInformation.control=2;
    putUserInfo(store.token,store.homeUserInformation).then(res=>{
        if(res.data.code===1){
            ElMessage({
            message: "修改成功",
            type: "info",
            plain: true,
            duration: 1700,
          });
        }else{
            ElMessage({
            message: "修改失败",
            type: "info",
            plain: true,
            duration: 1700,
            });
        }
    })

    

}
    
// 监听introduceEditFlag变化
watch(introduceEditFlag,()=>{
    if(introduceEditFlag.value)
    {
        nextTick(()=>{
            introduceInput.value.focus();
        })
    }
})

// 关注
async function followF(){

    if(store.userId===null)
    {
        loginDialogVisibleFlag.value=loginDialogVisibleFlag.value===0?1:0;   
        return;
    }
   
    if(store.userId===store.homeUserInformation.id){
        ElMessage({
            message: "不能关注自己(ง •̀_•́)ง",
            type: "info",
            plain: true,
            duration: 1700,
        });
        return;
    }
    await addFollow(store.token,store.userId,store.homeUserInformation.id).then(res=>{
        if(res.data.code===1){
            store.homeUserInformation.isFollowFlag=1;
            ElMessage({
            message: "关注成功(ง •̀_•́)ง",
            type: "info",
            plain: true,
            duration: 1700,
          });
        }else{
            ElMessage({
            message: "关注失败(ง •̀_•́)ง",
            type: "info",
            plain: true,
            duration: 1700,
            });
        }
    })
}

// 取消关注
async function unFollowF(){
   
    if(store.userId===null)
    {
        loginDialogVisibleFlag.value=loginDialogVisibleFlag.value===0?1:0;   
        return;
    }

    await deleteFollow(store.token,store.userId,store.homeUserInformation.id).then(res=>{
        if(res.data.code===1){
            store.homeUserInformation.isFollowFlag=0;
            ElMessage({
            message: "取消关注成功(ง •̀_•́)ง",
            type: "info",
            plain: true,
            duration: 1700,
          });
        }else{
            ElMessage({
            message: "取消关注失败(ง •̀_•́)ง",
            type: "info",
            plain: true,
            duration: 1700,
            });
        }
    })
}

// 检查是否关注
async function checkFollowF(){
    await checkFollow(store.userId,userId,store.token).then(res=>{
        if(res.data.code===1)
          store.homeUserInformation.isFollowFlag=res.data.data;
    })
}

//添加私信对话并跳转
async function addDialogueF() {

    if (store.userId === null) {
        loginDialogVisibleFlag.value=loginDialogVisibleFlag.value===0?1:0;   
        return;
    }

    let dialogue = {
    userId: store.userId,
    dialogueId: userId,
    }
    addDialogue(store.token,dialogue).then(res=>{

    if(res.data.code===1)
    {
        window.open(
        `./message?dialogueId=${userId}`,
        "_blank",
        );
    }
    })
}

//弹出修改背景菜单
function changeBackgroundMenu(event){
    if(event.target&&(event.target.className==="overlay"||event.target.className==="overlay2"||event.target.className==="user-info"))
    {
        event.preventDefault();
        handleContextMenuPosition.value={x:event.clientX,y:event.clientY};
        handleContextMenuVisible.value=true;
    }
   
}

watch(()=>store.loginDialogVisibleFlag,()=>{
    if(store.loginDialogVisibleFlag)
    {
        loginDialogVisibleFlag.value=loginDialogVisibleFlag.value===1?0:1;
        store.setLoginDialogVisibleFlag(0);
    }
})

//获取所有收藏夹名称
function getCollectsClassifyF(){
    getCollectsClassify(store.homeUserInformation.id,store.userId,store.token).then(res=>{
        if(res.data.code === 1){
            store.setCollectClassifyNumber(res.data.data.length);
        }
    })
}

watch(()=>store.homeUserInformation.id,()=>{
    
     setTimeout(() => {
        if(store.homeUserInformation.id){
        if(window.location.href.indexOf("home")>0&&(store.userId===null||store.userId!==userId))
            if(window.location.href.indexOf("?homeMenu=7")>0)
              window.location.href = "./home?homeMenu=1&userId="+userId;
            else if(window.location.href.indexOf("?homeMenu=5")>0&&!store.homeUserInformation.publicCollect)
              window.location.href = "./home?homeMenu=1&userId="+userId;
            else if(window.location.href.indexOf("?homeMenu=8")>0&&!store.homeUserInformation.publicFollowList)
              window.location.href = "./home?homeMenu=1&userId="+userId;
            else if(window.location.href.indexOf("?homeMenu=9")>0&&!store.homeUserInformation.publicFansList)
              window.location.href = "./home?homeMenu=1&userId="+userId;
     }
     }, 100);
})

</script>

<style lang="scss" scoped>
*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.home-head {
top: 0px;
width: 101%;
height: 200px;
background-size: cover;
background-position: 50% 70%;
background-repeat: no-repeat;
left: -23px;
position: relative;
background-color: white;
box-shadow: 2px 0px 4px #d3d3d3;
z-index: 999;

.main-head {
    position: relative;
    z-index: 9999;
}

.overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100px;
    background: linear-gradient(to bottom, rgba(0,0,0,0.6), rgba(0,0,0,0));
    z-index: -20;
}
.overlay2 {
    position: absolute;
    top: 100px;
    left: 0;
    width: 100%;
    height: 100px;
    background: linear-gradient(to bottom, rgba(0,0,0,0), rgba(0,0,0,0.6));
    z-index: -20;
}
.menu{
    position: absolute;
    padding: 5px 10px;
    font-size: 12px;
    border-radius: 6px;
    background-color: white;
    color: #222;
    cursor: pointer;
    border: 1px solid #e0e0e0;
    transition: all .3s ease;
}
.menu:hover{
    background-color: #f5f5f5;
}
.user-info {
    position: absolute;
    width: 100%;
    height: 80px;
    bottom: 0px;
    z-index: -10;

    .user-avatar-link {
        position: absolute;
        top: 0;
        left: 85px;
        width: 64px;
        height: 64px;

        .user-avatar {
        width: 66px;
        height: 66px;
        border-radius: 50%;
        background-color: rgba(0, 0, 0, .5);
        display: flex;
        align-items: center;
        justify-content: center;
        border: 2px solid #d3d3d3;
        color: white;
        font-size: 14px;
        }
        div{
        position: absolute;
        top: 2px;
        left: 2px;
        text-align: center;
        width: 62px;
        height: 62px;
        cursor: pointer;
        background-color: rgba(0, 0, 0, .5);
        border-radius: 50%;
        font-size: 12.5px;
        color: white;
        line-height: 60px;
        opacity: 0;
        transition: opacity .3s;
        }

    }

    .user-avatar-link:hover div{
        opacity: 1;
    }

    .user-info-top{
        position: relative;
        width: auto;
        left: 174px;
        top: 2px;
        height: 32px;
        line-height: 32px;
        .user-name{
        position: relative;
        font-size: 23px;
        font-weight: 700;
        color: white;
        margin-right: 8px;
        text-shadow: 0px 1px 2px rgba(0, 0, 0, .4);
        }
        .level-icon{
            position: relative;
            top: -1px;
            width: 28px;
            height: 13px;
            margin-right: 8px;
        }
        .gender-icon{
            position: relative;
            width: 16px;
            height: 16px;
            margin-right: 8px;
        }
    }
    .user-info-introduce{
    position: relative;
    width: 892.5px;
    height: 17px;
    left: 177px;
    top: 9px;
    font-size: 12px;
    color: white;
    overflow: hidden;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    word-break: break-all;
    text-overflow: ellipsis;
    transition: background-color .3s, color .3s;
    -webkit-line-clamp: 1;
    }
    .user-info-introduce2{
    position: relative;
    width: 892.5px;
    height: 17px;
    left: 177px;
    top: 9px;
    font-size: 12px;
    color: white;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    overflow: hidden;
    word-break: break-all;
    text-overflow: ellipsis;
    transition: background-color .3s, color .3s;
    -webkit-line-clamp: 1;
    }

    .user-info-introduce2:hover{
    background-color: rgba(255, 255, 255, .2);
    box-shadow: 0 0 0 1px rgba(255, 255, 255, .5);
    }

    input{
    position: relative;
    width: 897.5px;
    left: 173px;
    top: 5px;
    padding: 2px 4px;
    font-size: 12px;
    outline: none;
    background-color: transparent;
    transition: background-color .3s, color .3s;
    color: #222;
    background-color: white;
    border: none;
    overflow: hidden;
    word-break: break-all;
    text-overflow: ellipsis;
    animation: opacity .3s forwards;
    -webkit-line-clamp: 1;
    line-clamp: 1;
    }

    
}
.control-container{
    position: absolute;
    display: flex;
    width: 324px;
    height: 34px;
    right: 135px;
    bottom: 22px;
    z-index: -10;
    user-select: none;
    .follow{  
    margin-right: 24px;
    cursor: pointer;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 150px;
    height: 34px;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 600;
    color: white;
    background-color: #00AEEC;
    transition: background-color .3s ease;

        img{
            position: relative;
            left: -6px;
            top: 1px;
            margin-left: 9px;
            width: 12px;
            height: 12px;
        }
        
    }
    .follow:hover{
        background-color: #40C5F1;
    }
    .delete-follow{
    cursor: pointer;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 150px;
    height: 34px;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 700;
    margin-right: 24px;
    color: white;
    border: 1px solid rgba(255, 255, 255, .2);
    background-color: rgba(255, 255, 255, .14);
    transition: all .3s ease;

    img{
            position: relative;
            left: -5px;
            top: 1px;
            margin-left: 9px;
            width: 12px;
            height: 9px;
        }
    }
    .delete-follow:hover{
        background-color: rgb(255 255 255 / 40%);
    }
    .send-message{
    cursor: pointer;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 150px;
    height: 34px;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 700;
    color: white;
    border: 1px solid rgba(255, 255, 255, .2);
    background-color: rgba(255, 255, 255, .14);
    transition: all .3s ease;
    }
    .send-message:hover{
        background-color: rgb(255 255 255 / 40%);
    }

}
}

@keyframes opacity {
    0% {
        opacity: 0;
    }
    100% {
        opacity: 1;
    }
}


</style>