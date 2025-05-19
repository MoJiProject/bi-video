<template>
  <div class="like-dynamic-container-list">
      <div class="item" @click="openHome(1,user.id)" v-for="user in userList" :key="user.id">
          <div class="avatar">
              <img :src="user.avatarAddress">
              <userInfo2 @click.stop class="up-user-info" :user-info="user"/>
          </div>
          <div class="user-name">
              <div>{{ user.userName }} <div>赞了</div></div> 
          </div>
          <button v-show="!user.isFollowFlag" class="follow-btn" @click.stop="addFollowF(user)">关注</button>
          <button v-show="user.isFollowFlag" class="unfollow-btn" @click.stop>
            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 16 16" width="16" height="16" style="width: 16px; height: 16px;"><path d="M2 8C2 7.723866666666667 2.2238599999999997 7.5 2.5 7.5L13.5 7.5C13.776133333333334 7.5 14 7.723866666666667 14 8C14 8.276133333333332 13.776133333333334 8.5 13.5 8.5L2.5 8.5C2.2238599999999997 8.5 2 8.276133333333332 2 8z" fill="currentColor"></path><path d="M2 4C2 3.72386 2.2238599999999997 3.5 2.5 3.5L13.5 3.5C13.776133333333334 3.5 14 3.72386 14 4C14 4.27614 13.776133333333334 4.5 13.5 4.5L2.5 4.5C2.2238599999999997 4.5 2 4.27614 2 4z" fill="currentColor"></path><path d="M2 12C2 11.723866666666666 2.2238599999999997 11.5 2.5 11.5L13.5 11.5C13.776133333333334 11.5 14 11.723866666666666 14 12C14 12.276133333333334 13.776133333333334 12.5 13.5 12.5L2.5 12.5C2.2238599999999997 12.5 2 12.276133333333334 2 12z" fill="currentColor"></path></svg>
            已关注
            <div @click.stop="deleteFollowF(user)">取消关注</div> 
          </button>
      </div>
      <div v-show="fotterFlag" class="no-more">没有更多了</div>
  </div>
</template>

<script setup>
import { useGlobalStore } from "../store/store";
import { ElMessage } from "element-plus";
import { onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { addFollow,deleteFollow } from '@/api/user/index';
import { likeDynamicUser } from '@/api/dynamic/index';
import userInfo2 from "./userInfo2.vue";

const props=defineProps({
content3Menu:{
    type: Number,
    required:true
}
});

const pageNum=ref(1);
const store = useGlobalStore();
const userList=reactive([]);
const dynamicId=new URLSearchParams(window.location.search).get("dynamicId");
const fotterFlag=ref(false);

onMounted(()=>{
window.addEventListener("scroll",handleScroll);
})

onUnmounted(()=>{
window.removeEventListener("scroll",handleScroll);   
})


//关注
function addFollowF(user){

if(store.userId===null){
    store.loginDialogVisibleFlag=store.loginDialogVisibleFlag===0?1:0;
    return;   
} 

if(store.userId===user.id){
    ElMessage({
    message: "不能关注自己",
    type: "info",
    plain: true,
    duration: 1700,
    });
    return;
}

addFollow(store.token,store.userId,user.id).then(res=>{
    if(res.data.code===1){
        user.isFollowFlag=1;
        ElMessage({
        message: "关注成功",
        type: "info",
        plain: true,
        duration: 1700,
        });
    }
});       
}

//取消关注
function deleteFollowF(user){

if(store.userId===null){
store.loginDialogVisibleFlag=store.loginDialogVisibleFlag===0?1:0;
return;   
}     

deleteFollow(store.token,store.userId,user.id).then(res=>{
    if(res.data.code===1){
        user.isFollowFlag=0;
        ElMessage({
        message: `取消了对 ${user.userName} 的关注`,
        type: "info",
        plain: true,
        duration: 1700,
        });
    }
});       
}

//打开我的主页
function openHome(menu,id){
  window.open(
  `./home?homeMenu=${menu}&userId=${id}`,
  "_blank",
);
}

//查询喜欢的动态用户列表
let selectFlag=true;
function likeDynamicUserF(){

    if(selectFlag){
        selectFlag=false;
    }
    else{
        return;
    }
    likeDynamicUser(store.userId,dynamicId,pageNum.value++).then(res=>{
        if(res.data.code===1){
            pushData(res.data.data);
            if(!res.data.data||res.data.data.length===0){
                fotterFlag.value=true;
            }
            selectFlag=true;
        }
    })
}

//去除重复数据
function pushData(newData) {
if(newData===null)
    return;
    const idSet = new Set(userList.map(item => item.id)); // 获取已有数据的 ID
    const uniqueData = newData.filter(item => !idSet.has(item.id)); // 过滤掉重复的
    userList.push(...uniqueData); // Vue 响应式更新
}

//滚动增加数据
function handleScroll(){
if(fotterFlag.value||props.content3Menu===1)
    return;
if(window.innerHeight+document.documentElement.scrollTop+200>=document.body.scrollHeight){
    likeDynamicUserF();
}
}

//监视菜单
watch(()=>props.content3Menu,()=>{
    if(props.content3Menu===1){
        pageNum.value=1;
        userList.length=0;
        fotterFlag.value=false;
    }
    else{
        likeDynamicUserF();
    }
})

</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.like-dynamic-container-list{
padding: 0px 20px 0;
min-height: 50vh;
    .item{
    display: flex;
    align-items: center;
    padding: 16px 0;
    cursor: pointer;
    box-shadow: 0 -.5px 0 0 #E3E5E7 inset;  

        .avatar{
        width: 40px;
        height: 40px;
        flex-shrink: 0;
        border-radius: 50%;
        overflow: hidden;
        margin-right: 16px;
        cursor: pointer;

            img{
            width: 100%;
            height: 100%;
            object-fit: cover;   
            }
            .up-user-info{
             opacity: 0;
             transition: all .3s ease;
             visibility: hidden;   
            }
        }
        .avatar:hover{
            .up-user-info{
            transition-delay: 0.3s;
            opacity: 1;
            visibility: visible;
            }
        }
        .user-name{
         flex: 1;
         overflow: hidden;
         
            div{
            font-size: 15px;
            font-style: normal;
            font-weight: 500;
            line-height: 21px;
            color: #18191C;

                div{
                display: inline-block;
                font-weight: 400;
                margin-left: 6px;
                }
            }
        }
        .follow-btn{
        border-radius: 6px;
        height: 30px;
        min-width: 70px;
        font-size: 14px;
        border: 1px solid #00AEEC;
        background-color: white;
        color: #00AEEC;
        cursor: pointer;
        display: inline-block;
        line-height: 1;
        text-align: center;
        user-select: none;
        }
        .unfollow-btn{
        background-color: #F1F2F3;
        border: 1px solid #F1F2F3;
        color: #9499A0;
        border-radius: 6px;
        height: 30px;
        min-width: 70px;
        cursor: pointer;
        display: inline-block;
        line-height: 1;
        position: relative;
        text-align: center;
        user-select: none;

            svg{
            width: 16px;
            height: 16px;
            margin-right: 2px;
            vertical-align: sub;    
            }
            div{
            background-color: #FFFFFF;
            border: 1px solid #dcdfe6;
            border-radius: 4px;
            box-shadow: 0 0 30px rgba(0, 0, 0, .1);
            padding: 6px 0;
            position: absolute;
            text-align: center;
            top: 35px;
            width: 122px;
            z-index: 11;
            color: #18191C;
            cursor: pointer;
            font-size: 14px;
            line-height: 40px;
            list-style: none;
            transition: all .3s ease;
            opacity: 0;
            visibility: hidden;
            }
            div:hover{
            color: #00AEEC;
            transition-delay: 0s !important;
            background-color: #f4f5f5;
            }

        }
        .unfollow-btn:hover{
            div{
            opacity: 1;
            transition-delay: 0.3s;
            visibility: visible;
            }
        }
    }
    .no-more{
    width: 100%;
    margin-top: 20px;
    padding-top: 50px;
    font-size: 14px;
    color: #9499A0;
    text-align: center;
    user-select: none;
    padding-bottom: 120px;   
    }
}

</style>