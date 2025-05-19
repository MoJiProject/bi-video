<template>
  <div class="video-share-info" :style="{height: heightFlag?'270px':'250px'}">
      <span class="title">分享给</span>
      <div v-if="shareUserList.length===0" class="not-share-container">
         您还没有朋友哦，快去寻找吧！
      </div>
      <div v-else-if="shareUserList.length>0" class="share-container"  ref="scrollContainer"  @wheel.prevent="handleWheel">
          <div class="share-item" v-for="user in shareUserList" :key="user.id" @click="clickUser(user)">
              <img class="avatar" :src="user.avatarAddress">
              <img class="check" v-show="user?.checkFlag" src="../img/选中.png">
              <span style="color: #18191C;">{{ user.userName }}</span>
          </div>
      </div>
      <div class="line"></div>
      <textarea
        class="share-textarea"
        placeholder="请输入分享内容"
        maxlength="500"
        v-model="videoShareContent"
        @focus="store.setShareVideoFocusFlag(true)"
        @blur="store.setShareVideoFocusFlag(checkSelectUser())"
      >
      </textarea>
      <el-tooltip
          popper-class="custom-tooltip"
          class="dynamicContent-item-tooltip"
          effect="light"
          :show-after="300"
          :content="store.selectUpVideo.upVideo?.title"
          placement="bottom"
          :offset="14"
          :show-arrow="false"
          :hide-after="0"
        >
        <img class="cover" :src="store.selectUpVideo.upVideo?.coverAddress">
      </el-tooltip>
      <button v-if="shareUserList.length>0&&checkSelectUser()" class="share-btn" @click="shareVideoF">发送</button>
      <button v-else-if="shareUserList.length===0||!checkSelectUser()" class="not-share-btn">发送</button>
  </div>
</template>

<script>
import { reactive, ref, watch } from "vue";
import { useGlobalStore } from "../store/store";
import {selectFollowAndFans} from '../api/user/index';
import {shareVideoAxios} from '../api/privateMessage/index';
import { ElMessage } from "element-plus";

 
export default {
  name: "videoShare",
  props:{
    shareHover: {
      type:Boolean,
      required: false,
    },
    heightFlag: {
      type: Boolean,
      default: false,
    },
    dynamic:{
      type: Object,
      required: false,
    },
    openFlag:{
      type: Boolean,
      required: false,
    },
  },

  setup(props) {
const scrollContainer=ref(null);
const videoShareContent=ref('');
const store = useGlobalStore();
const shareUserList=reactive([]);
const pageNum=ref(1);
const heightFlag=ref(props.heightFlag);
let scrolledFlag=true;

//横向滚动
let targetScrollLeft = 0;
let isScrolling = false;

function handleWheel(e){
  if(!e.target) return;

  e.preventDefault();
  const speedFactor = e.deltaMode === 0 ? 1 : 15;
  const delta = Math.abs(e.deltaX) > 10 ? e.deltaX : e.deltaY;

  targetScrollLeft = scrollContainer.value.scrollLeft + delta * speedFactor * 0.5;
  targetScrollLeft = Math.max(0, Math.min(
    targetScrollLeft,
    scrollContainer.value.scrollWidth - scrollContainer.value.clientWidth
  ));

  if (!isScrolling) {
    isScrolling = true;
    smoothScroll();
  }

  if (scrolledFlag && e.target.scrollWidth - e.target.scrollLeft <= e.target.clientWidth + 1)
    selectFollowAndFansF();
}

const smoothScroll = () => {
  if (!scrollContainer.value) return;

  const current = scrollContainer.value.scrollLeft;
  const diff = targetScrollLeft - current;

  if (Math.abs(diff) < 0.5) {
    scrollContainer.value.scrollLeft = targetScrollLeft;
    isScrolling = false;
    return;
  }

  const newPosition = current + diff * 0.15;
  scrollContainer.value.scrollLeft = newPosition;
  requestAnimationFrame(smoothScroll);
}


// 获取分享列表
function selectFollowAndFansF(){
  scrolledFlag=true;
  selectFollowAndFans(store,pageNum.value++).then(res => {
      if(res.data.code===1){
          shareUserList.push(...res.data.data);
        if(res.data.data===null||res.data.data.length===0)
          scrolledFlag=false;
        else{
          scrolledFlag=true;
        }
      }
  })
}

//监听props变化
let onceFlag=true;
watch(()=>props.shareHover,()=>{
  if(onceFlag){
    onceFlag=false;
    selectFollowAndFansF();
  }
});

//监听props变化
watch(()=>props.openFlag,()=>{
  if(onceFlag){
    onceFlag=false;
    selectFollowAndFansF();
  }
});

//检查是否有选择的用户
function checkSelectUser(){
  return shareUserList.some(item => item.checkFlag);
}

//判断选中用户数量不能超过10个
function clickUser(user){
   
  if(shareUserList.filter(item => item.checkFlag).length>=10&&!user.checkFlag)
    {
      ElMessage({
            message: "最多只能选择10个用户",
            type: "info",
            plain: true,
            duration: 1700,
          });
      return;
    }  
  user.checkFlag=!user.checkFlag;
  store.setShareVideoFocusFlag(user.checkFlag);
}

//发送消息
function shareVideoF(){

  const shareUserListArr=shareUserList.filter(item => item.checkFlag);
  let userIds=[];
  shareUserListArr.forEach(item => {
    userIds.push(item.id);
  });

  let shareVideo={
    userId: store.userId,
    videoId: store.selectUpVideo.upVideo.id||props.dynamic.dynamic.videoId,
    content: videoShareContent.value,
    userIdList: userIds,
  };

  shareVideoAxios(store.token,shareVideo).then(res => {
     if(res.data.code===1)
       ElMessage({
            message: "分享成功",
            type: "info",
            plain: true,
            duration: 1700,
       });
      videoShareContent.value='';
      props.dynamic.video.videoShareNumber++;
      store.setShareHover(false);
      shareUserList.forEach(item => {
        item.checkFlag=false;
      });
  });

}



return {
  scrollContainer,
  videoShareContent,
  handleWheel,
  store,
  shareUserList,
  checkSelectUser,
  heightFlag,
  clickUser,
  shareVideoF,
}



  }
}

</script>

<style lang="scss" scoped>

.video-share-info{
  width: 500px;
  height: 250px;
  background-color: #fff;
  padding: 10px;
  border-radius: 5px;
  z-index: 1000;
  box-shadow: 0 2px 4px 0 rgba(121, 146, 185, 0.54);

  .title{
    font-size: 16px;
    user-select: none;
    color: #18191C;
  }

  .delete-share-info{
    position: absolute;
    width: 16px;
    height: 16px;
    top: 10px;
    right: 10px;
    cursor: pointer;
  }

  .delete-share-info:hover{
    color: #00AEEC;
  }

  .not-share-container{
    height: 90px;
    text-align: center;
    line-height: 100px;
    color: #999;
    user-select: none;
  }

  .share-container{
     position: relative;
     top: 15px;
     height: 90px;
     display: flex;
     flex-direction: row;
     overflow: hidden;

    .share-item{
      user-select: none;
      position: relative;
      display: inline-flex;
      justify-content: center;
      flex-direction: column;
      margin-right: 15px;
      cursor: pointer;
      .avatar{
        position: relative;
        top: -15px;
        border-radius: 50%;
        width: 48px;
        height: 48px;
      }
      .check{
        position: absolute;
        right: 0; /* 替代 transform */
        bottom: 37px; /* 替代 transform */
        width: 16px;
        background-color: white;
        height: 16px;
        border-radius: 50%;
      }
      span{
        position: absolute;
        top: 60px;
        font-size: 12px;
        width: 50px;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        text-align: center;
        overflow: hidden;
        text-overflow: ellipsis;
        -webkit-line-clamp: 2;
      }
    }
  }

  .line{
    position: relative;
    top: 25px;
    width: 100%;
    height: 1px;
    background-color: #e3e5e7;
  }

  .share-textarea{
    position: relative;
    top: 30px;
    border: none;
    resize: none;
    outline: none;
    padding-top: 5px;
    font-size: 14px;
    width: 80%;
    height: 70px;
  }

  .cover{
    position: relative;
    top: 27px;
    left: 10px;
    width: 87px;
    height: 70px;
    border-radius: 4px;
    object-fit: cover;
  }

  .share-textarea::-webkit-scrollbar {
  width: 5px; /* 滚动条的宽度 */
  border-radius: 30px; /* 滚动条滑块的圆角 */
  }

  .share-textarea::-webkit-scrollbar-thumb {
  background: #bcbcbc; /* 滚动条的滑块 */
  height: 10px;
  border-radius: 10px;
  }
  
  .share-btn{
    position: relative;
    top: 30px;
    width: 100%;
    height: 30px;
    background-color: #00AEEC;
    transition: background-color 0.3s ease;
    color: #fff;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    font-size: 14px;
  }

  .share-btn:hover{
    background-color: #00b8f6;
  }

  .not-share-btn{
    position: relative;
    top: 30px;
    width: 100%;
    height: 30px;
    background-color: #E3E5E7;
    transition: background-color 0.3s ease;
    color: #fff;
    border: none;
    border-radius: 5px;
    cursor: no-drop;
    font-size: 14px;
  }

}


</style>