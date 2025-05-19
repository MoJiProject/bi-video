<template>
  <div class="dynamic-detail" :style="{backgroundImage: 'url('+backgroundImgSrc[bIndex]+')'}">
     <div v-if="dynamic?.dynamic" class="dynamic-detail-container">
        <div class="dynamic-detail-content">
            <div class="content" v-if="dynamic.dynamic" ref="contentRef">
                <dynamic3 :dynamic="dynamic"/>
            </div>
            <div class="content2">
                <div class="menu" :style="{color: content3Menu===1?'#00AEEC':'#61666D'}" @click="content3Menu=1">评论 {{ dynamic.dynamic.videoId&&!dynamic.dynamic.commentId?dynamic.video.videoCommentNumber:dynamic.dynamic.commentNumber }}</div>
                <div class="menu" :style="{color: content3Menu===2?'#00AEEC':'#61666D'}" @click="content3Menu=2">赞与转发 {{ (dynamic.dynamic.videoId&&!dynamic.dynamic.commentId?dynamic.video.videoLikeNumber:dynamic.dynamic.likeNumber)+(dynamic.dynamic.videoId&&!dynamic.dynamic.commentId?dynamic.video.videoShareNumber:dynamic.dynamic.shareNumber) }}</div>
                <div :style="{ width: content3Menu===1?'51.88px':'98.44px',transform: content3Menu===1? 'translateX(53px)':'translateX(133px)' }" class="line"></div>
            </div>
            <div class="content3">
                <comment6 v-show="content3Menu===1" :dynamic="dynamic" :offSetHeight="contentHeight" :content3Menu="content3Menu"/>
                <likeDynamicList v-show="content3Menu===2" :content3Menu="content3Menu"/>
            </div>
        </div>
        <div class="dynamic-detail-controls" @mouseover="store.shareHover=true" @mouseleave="store.shareVideoFocusFlag?store.shareHover=true:(store.shareHover=false,shareOpenFlag=false)">
            <div class="content">
                <div class="controls" @click="likeDynamicF">
                  <svg v-show="!likeFlag" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M13.9252 3.04546C13.6432 3.01258 13.382 3.13521 13.2422 3.29868C12.9774 3.60848 12.8705 3.86276 12.7384 4.23799C12.7234 4.28037 12.7082 4.32422 12.6925 4.36961C12.5611 4.74857 12.3929 5.23391 12.044 5.85187C11.446 6.91117 10.8882 7.55173 10.2013 8.154C9.63865 8.6473 9.06639 8.98294 8.5 9.14275V19.7248C9.28689 19.7417 10.1287 19.7525 11 19.7525C13.2468 19.7525 15.1529 19.6812 16.4372 19.615C17.3477 19.5681 18.1379 19.1176 18.5497 18.3851C19.1274 17.3574 19.8396 15.8503 20.2712 14.0753C20.6734 12.4212 20.8569 11.0615 20.9392 10.1025C20.9774 9.65797 20.6256 9.25003 20.08 9.25003H15.0977C14.8476 9.25003 14.614 9.12542 14.4748 8.91775C14.3355 8.71015 14.3089 8.44684 14.4037 8.21557C14.4037 8.21555 14.4037 8.21553 14.4037 8.21551C14.4037 8.21547 14.4038 8.21543 14.4038 8.21539C14.4038 8.21537 14.4038 8.21536 14.4038 8.21534L14.4046 8.21341L14.4085 8.20377L14.4249 8.16256C14.4395 8.12573 14.4609 8.07081 14.4874 8.00089C14.5404 7.86084 14.6131 7.66185 14.6909 7.42856C14.8489 6.95439 15.0177 6.36917 15.0941 5.85681C15.2109 5.07451 15.1824 4.44592 14.8757 3.86439C14.5461 3.23954 14.1724 3.07429 13.9252 3.04546ZM16.164 7.75003H20.08C21.4037 7.75003 22.5555 8.81291 22.4337 10.2309C22.3455 11.2583 22.1508 12.6941 21.7288 14.4297C21.2555 16.3758 20.4798 18.0127 19.8573 19.1201C19.1594 20.3616 17.8654 21.0435 16.5144 21.113C15.21 21.1802 13.2777 21.2525 11 21.2525C8.7933 21.2525 6.77664 21.1846 5.34776 21.1195C3.73985 21.0461 2.39101 19.8517 2.21798 18.2152C2.1042 17.1391 2 15.7467 2 14.2525C2 12.8835 2.08746 11.6418 2.18985 10.6567C2.36874 8.93544 3.84615 7.75003 5.50754 7.75003H7.75C8.06896 7.75003 8.56382 7.59478 9.21241 7.02612C9.77509 6.53279 10.2246 6.0235 10.7378 5.11442C11.0177 4.61868 11.146 4.24973 11.2753 3.87788C11.2913 3.83198 11.3072 3.78604 11.3235 3.73981C11.4791 3.29802 11.6602 2.84083 12.1021 2.32398C12.5582 1.79047 13.3077 1.4633 14.0989 1.55555C14.9248 1.65186 15.6866 2.18668 16.2024 3.16456C16.7233 4.15199 16.7157 5.15349 16.5777 6.07814C16.4916 6.65522 16.3202 7.26593 16.164 7.75003ZM7 19.6835V9.25003H5.50754C4.54442 9.25003 3.77423 9.92257 3.68182 10.8118C3.58359 11.7569 3.5 12.9457 3.5 14.2525C3.5 15.6802 3.59975 17.0179 3.70967 18.0575C3.80099 18.9212 4.51252 19.5798 5.41611 19.621C5.88508 19.6424 6.41785 19.6641 7 19.6835Z" fill="currentColor"></path></svg>
                  <svg v-show="likeFlag" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M20.3186 7.74998H15.776C15.999 7.26616 16.2333 6.6567 16.3213 6.06668C16.3848 5.64061 16.3909 5.11874 16.3397 4.62199C16.2897 4.1373 16.1773 3.59536 15.9528 3.16982C15.4775 2.26864 14.8233 1.63578 14.0361 1.48224C13.1988 1.31893 12.4779 1.74495 12.0464 2.39778C11.6856 2.94375 11.5449 3.39925 11.4167 3.81444L11.4167 3.81444L11.4125 3.82786C11.2892 4.22696 11.1679 4.61901 10.8559 5.17154C10.3427 6.08056 9.92397 6.55203 9.36125 7.0454C9.071 7.29988 8.77253 7.47246 8.5 7.5831V21.225C9.28742 21.2418 10.1289 21.2524 11 21.2524C13.3441 21.2524 15.3224 21.1759 16.627 21.1071C17.9072 21.0396 19.1311 20.3943 19.7997 19.2217C20.4316 18.1135 21.2409 16.4358 21.7288 14.4296C22.1856 12.5508 22.3761 11.0232 22.4535 9.98474C22.5487 8.70656 21.5102 7.74998 20.3186 7.74998ZM7 21.1842V7.74998H5.30231C3.75929 7.74998 2.38771 8.85084 2.21213 10.4483C2.10046 11.4642 2 12.7852 2 14.2524C2 15.839 2.11749 17.3111 2.23926 18.4118C2.40726 19.9306 3.65916 21.0399 5.15251 21.1103C5.68498 21.1355 6.30853 21.1615 7 21.1842Z" fill="#00A1D6"></path></svg>
                  <span :style="{color: likeFlag?'#00AEEC':'#61666D'}">{{ dynamic.dynamic.videoId&&!dynamic.dynamic.commentId?dynamic.video.videoLikeNumber:dynamic.dynamic.likeNumber }}</span>    
                </div>
                <div class="controls" @click="collectDynamicF">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M10.1617 3.28288C10.9137 1.75922 13.0863 1.75922 13.8383 3.28288L15.5564 6.76404C15.6656 6.9854 15.8768 7.13882 16.1211 7.17432L19.9628 7.73255C21.6442 7.97688 22.3156 10.0432 21.0989 11.2292L18.319 13.9389C18.1423 14.1112 18.0616 14.3595 18.1033 14.6028L18.7596 18.4289C19.0468 20.1036 17.2891 21.3807 15.7851 20.59L12.349 18.7835C12.1305 18.6687 11.8695 18.6687 11.651 18.7835L8.21488 20.59C6.71094 21.3807 4.95321 20.1036 5.24043 18.4289L5.89667 14.6028C5.9384 14.3595 5.85774 14.1112 5.68097 13.9389L2.9011 11.2292C1.68439 10.0432 2.35579 7.97688 4.03724 7.73255L7.87893 7.17432C8.12321 7.13882 8.33439 6.9854 8.44363 6.76404L10.1617 3.28288ZM12.4932 3.94673C12.2915 3.53794 11.7085 3.53794 11.5068 3.94673L9.78874 7.42789C9.461 8.09196 8.82747 8.55224 8.09463 8.65873L4.25294 9.21696C3.80182 9.28251 3.62168 9.8369 3.94812 10.1551L6.72799 12.8648C7.25829 13.3817 7.50027 14.1265 7.37508 14.8563L6.71884 18.6825C6.64178 19.1318 7.11337 19.4744 7.51687 19.2623L10.953 17.4558C11.6085 17.1112 12.3915 17.1112 13.047 17.4558L16.4831 19.2623C16.8866 19.4744 17.3582 19.1318 17.2811 18.6825L16.6249 14.8563C16.4997 14.1265 16.7417 13.3817 17.272 12.8648L20.0519 10.1551C20.3783 9.8369 20.1982 9.28251 19.7471 9.21696L15.9054 8.65873C15.1725 8.55224 14.539 8.09196 14.2113 7.42789L12.4932 3.94673Z" fill="currentColor"></path></svg>
                  <span>0</span>
                </div>
                <div class="controls" @click="openShare">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M11.7148 3.3904L12.1883 2.80873V2.80873L11.7148 3.3904ZM10.0835 7.11906L10.1675 7.86434C10.5469 7.82159 10.8335 7.50077 10.8335 7.11906H10.0835ZM2.40872 16.249L1.66281 16.1708L2.40872 16.249ZM3.41451 16.5323L2.78976 16.1174H2.78976L3.41451 16.5323ZM10.0835 12.9746H10.8335C10.8335 12.5859 10.5366 12.2616 10.1494 12.2275L10.0835 12.9746ZM11.7148 16.7765L11.2414 16.1948L11.7148 16.7765ZM18.5084 11.2469L18.035 10.6652H18.035L18.5084 11.2469ZM18.5085 8.92019L18.9819 8.33852L18.5085 8.92019ZM10.8335 4.16596C10.8335 3.95534 11.078 3.83911 11.2414 3.97206L12.1883 2.80873C11.0449 1.87805 9.33354 2.69168 9.33354 4.16596H10.8335ZM10.8335 7.11906V4.16596H9.33354V7.11906H10.8335ZM3.15463 16.3272C3.51573 12.883 4.69455 10.8613 6.01872 9.66171C7.35327 8.45275 8.91679 8.00533 10.1675 7.86434L9.99953 6.37378C8.54838 6.53736 6.65086 7.06509 5.01166 8.55003C3.36208 10.0444 2.05444 12.4353 1.66281 16.1708L3.15463 16.3272ZM2.78976 16.1174C2.84989 16.0268 2.94169 16.0279 2.97464 16.0369C2.99716 16.043 3.04471 16.0623 3.0897 16.1195C3.14262 16.1869 3.16079 16.2684 3.15463 16.3272L1.66281 16.1708C1.58766 16.8875 2.09197 17.3518 2.58225 17.4846C3.05748 17.6134 3.68033 17.4877 4.03925 16.9473L2.78976 16.1174ZM10.1494 12.2275C8.06107 12.0433 5.11428 12.6176 2.78976 16.1174L4.03925 16.9473C6.01112 13.9785 8.38309 13.5775 10.0177 13.7217L10.1494 12.2275ZM10.8335 16.0009V12.9746H9.33354V16.0009H10.8335ZM11.2414 16.1948C11.078 16.3278 10.8335 16.2116 10.8335 16.0009H9.33354C9.33354 17.4752 11.0449 18.2888 12.1883 17.3582L11.2414 16.1948ZM18.035 10.6652L11.2414 16.1948L12.1883 17.3582L18.9819 11.8285L18.035 10.6652ZM18.035 9.50185C18.4037 9.80199 18.4037 10.3651 18.035 10.6652L18.9819 11.8285C20.0881 10.9282 20.0881 9.23891 18.9819 8.33852L18.035 9.50185ZM11.2414 3.97206L18.035 9.50185L18.9819 8.33852L12.1883 2.80873L11.2414 3.97206Z" fill="currentColor"></path> <path fill-rule="evenodd" clip-rule="evenodd" d="M12.0126 2.74638C10.9645 1.89326 9.39575 2.63908 9.39575 3.9905V6.52347C8.04285 6.7607 6.42622 7.33445 5.01037 8.63537C3.36503 10.1472 2.06603 12.5719 1.70486 16.3755C1.64281 17.029 2.10319 17.4525 2.5535 17.5729C2.99132 17.6899 3.56409 17.5696 3.88706 17.0656C4.90489 15.4772 6.02606 14.6008 7.07814 14.1323C7.88831 13.7715 8.68279 13.6415 9.39575 13.6345V16.176C9.39575 17.5275 10.9645 18.2733 12.0126 17.4202L19.0613 11.683C20.0753 10.8576 20.0753 9.30912 19.0613 8.48377L12.0126 2.74638ZM10.7707 3.9905C10.7707 3.79744 10.9949 3.6909 11.1446 3.81277L18.1933 9.55016C18.5313 9.82528 18.5313 10.3414 18.1933 10.6166L11.1446 16.3538C10.9949 16.4756 10.7707 16.3691 10.7707 16.176V12.9744C10.7707 12.6181 10.4986 12.3209 10.1436 12.2896C9.10884 12.1983 7.8347 12.2902 6.51878 12.8763C5.36413 13.3904 4.20995 14.2707 3.17068 15.6812C3.60099 12.6381 4.70283 10.7852 5.94069 9.64786C7.29583 8.40271 8.89001 7.94525 10.1603 7.80206C10.508 7.76287 10.7707 7.46879 10.7707 7.11889V3.9905Z" fill="currentColor"></path></svg>
                  <span>{{ dynamic.dynamic.videoId&&!dynamic.dynamic.commentId?dynamic.video.videoShareNumber:dynamic.dynamic.shareNumber }}</span>
                  <videoShare v-show="store.shareHover&&shareOpenFlag" class="video-share" @click.stop :height-flag="true" :open-flag="shareOpenFlag" :dynamic="dynamic"/>  
                </div>
                <div class="controls" @click="openComment">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" clip-rule="evenodd" d="M2 11C2 6.58172 5.58172 3 10 3H14C18.4183 3 22 6.58172 22 11C22 15.2656 18.6616 18.7514 14.4549 18.9873C13.4971 20.2148 11.9228 21.7529 9.71136 22.3555C9.2837 22.472 8.89202 22.2894 8.673 22.0075C8.46064 21.7342 8.38542 21.3424 8.5439 20.9775C8.87531 20.2145 8.94851 19.5143 8.90558 18.9257C5.00479 18.3918 2 15.0471 2 11ZM10 4.5C6.41015 4.5 3.5 7.41015 3.5 11C3.5 14.459 6.20234 17.2875 9.6105 17.4885C9.95008 17.5086 10.2337 17.7547 10.3013 18.088C10.4401 18.7716 10.4842 19.6217 10.2587 20.5643C11.6947 19.9026 12.7693 18.7583 13.4679 17.806C13.6076 17.6155 13.829 17.502 14.0652 17.4997C17.6249 17.4648 20.5 14.5681 20.5 11C20.5 7.41015 17.5899 4.5 14 4.5H10Z" fill="currentColor"></path></svg>
                  <span>{{ dynamic.dynamic.videoId&&!dynamic.dynamic.commentId?dynamic.video.videoCommentNumber:dynamic.dynamic.commentNumber }}</span>
                </div>
            </div>
        </div>
     </div>
  </div>
</template>

<script setup>
import { useGlobalStore } from "../store/store";
import { selectDynamicById,selectDynamicByIdLike,likeDynamic } from "@/api/dynamic/index";
import { getEitList } from "@/api/user/index";
import { onMounted, ref, watch } from "vue";
import dynamic3 from "@/utils/dynamic3";
import { ElMessage } from "element-plus";
import videoShare from '@/utils/videoShare.vue';
import comment6 from "@/comment/comment6.vue";
import likeDynamicList from "@/utils/likeDynamicList.vue";


const backgroundImgSrc=[
  '/background.jpg',
  '/background2.png',
  '/background3.jpg',
  '/background4.jpg',
  '/background5.jpg',
  '/background6.jpg',
  '/background7.png',
  '/background8.jpg',
];
let bIndex=Math.floor(Math.random()*backgroundImgSrc.length);
const store = useGlobalStore();
const dynamic=ref({});
const content3Menu=ref(1);//1评论 2点赞
const dynamicId=new URLSearchParams(window.location.search).get("dynamicId");
const likeFlag=ref(false);
const shareOpenFlag = ref(false);
const contentRef=ref(null);
const contentHeight=ref(100);
let resizeObserver = null;

onMounted(()=>{
selectDynamicByIdF();
})

//查询动态详情
function selectDynamicByIdF(){

  if(!dynamicId)
  {
      setTimeout(() => {
        window.location.href = "./dynamicNotFound";
      }, 200);
      return;
  }
  selectDynamicById(dynamicId).then(res=>{
      if(res.data.code===1){
          dynamic.value=res.data.data;
      }
      if(!res.data.data){
          setTimeout(() => {
            window.location.href = "./dynamicNotFound";
          }, 200);
      }
  })
}

//初始化查询
let onceFlag=true;
watch(()=>store.userId,()=>{

  if(store.userId&&onceFlag){
    onceFlag=null;
    selectDynamicByIdLike(store.token,dynamicId,store.userId).then(res=>{
        if(res.data.code===1){
          likeFlag.value=res.data.data;
        }
    })
    getEitList(store).then(res=>{
      if(res.data.code===1){
        Object.assign(store.eitList,res.data.data);
      }
    })
  }
})

//打开或关闭分享
function openShare(){
  if(!store.userId)
  {
    store.loginDialogVisibleFlag=store.loginDialogVisibleFlag===0?1:0;   
    return;
  }
  if(!dynamic.value.video)
  {
    ElMessage({
        message: "暂不支持分享动态",
        type: "info",
        plain: true,
        duration: 1700,
        });
    return;
  }
  shareOpenFlag.value=!shareOpenFlag.value;
  if(!shareOpenFlag.value)
    return;
}

//点赞或取消点赞动态
function likeDynamicF(){

  if(!store.userId)
  {
    store.loginDialogVisibleFlag=store.loginDialogVisibleFlag===0?1:0;   
    return;
  }

  likeDynamic(dynamic.value.dynamic.id,store.userId,store.token).then(res=>{
    if(res.data.code===1){
      likeFlag.value=!likeFlag.value;
      if(likeFlag.value){
        ElMessage({
        message: "点赞成功",
        type: "info",
        plain: true,
        duration: 1700,
        });
        if(dynamic.value.dynamic.videoId&&!dynamic.value.dynamic.commentId){
          dynamic.value.video.videoLikeNumber++;
        }else{
          dynamic.value.dynamic.likeNumber++;
        }
      }
      else{
        if(dynamic.value.dynamic.videoId&&!dynamic.value.dynamic.commentId){
          dynamic.value.video.videoLikeNumber--;
        }else{
          dynamic.value.dynamic.likeNumber--;
        }
      }
    }
  })

}

watch(contentRef,()=>{
  if(contentRef.value){
      resizeObserver = new ResizeObserver(entries => {
        for (const entry of entries) {
          const newHeight = entry.target.offsetHeight;
          contentHeight.value = 48 + newHeight;
        }
      });
      resizeObserver.observe(contentRef.value);
  }
})

//收藏动态
function collectDynamicF(){

  if(!store.userId)
  {
    store.loginDialogVisibleFlag=store.loginDialogVisibleFlag===0?1:0;   
    return;
  }

    ElMessage({
        message: "暂不支持收藏动态",
        type: "info",
        plain: true,
        duration: 1700,
        });
    return;

}

//打开评论
function openComment(){

  if(!store.userId)
  {
    store.loginDialogVisibleFlag=store.loginDialogVisibleFlag===0?1:0;   
    return;
  }
  content3Menu.value=1;
  window.scrollTo({top: contentHeight.value, behavior: 'smooth'});

}



</script>

<style lang="scss" scoped>

*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.dynamic-detail{
width: 100%;
background-size: cover;
background-repeat: no-repeat;
margin: 0 auto;
padding-top: 8px;
display: flex;
background-attachment: fixed;
min-height: 100vh;
min-width: 1044px;
max-width: 2560px;
justify-content: center;
position: relative;
top: 64px;
padding-bottom: 1px;

    .dynamic-detail-container{
        .dynamic-detail-content{
        margin: 0 auto;
        width: 708px;
        padding: 0px 36px;
        position: relative;
        border-radius: 6px; 
        background-color: white;
        z-index: 2;

          .content{
          font-weight: 400;
          padding: 20px;
          min-width: 556px;
          position: relative;
          }
          .content2{
          width: 708px;
          padding: 0px 36px;
          left: -36px;  
          margin-top: 10px;  
          align-items: center;
          display: flex;
          height: 48px;
          position: relative;
          background-color: white;
          border-bottom: 1px solid #E3E5E7;

            .menu:nth-child(1){
            cursor: pointer;
            font-size: 18px;
            height: 25px;
            line-height: 25px;
            margin-left: 18px;
            transition: color .3s ease;
            user-select: none;
            }
            .menu:nth-child(2){
            cursor: pointer;
            font-size: 18px;
            height: 25px;
            line-height: 25px;
            margin-left: 30px;
            transition: color .3s ease;
            user-select: none;
            }
            .menu:hover{
            color: #00AEEC !important;
            }
            .line{
            background-color: #00AEEC;
            bottom: 0;
            height: 2px;
            left: 0;
            list-style: none;
            position: absolute;
            transition: transform .3s ease;
            z-index: 1;
            }
          }
          .content3{
          padding-top: 15px;   
          }
        }
        .dynamic-detail-controls{
        position: fixed;
        margin-left: 720px;
        transition: all .5s ease-in-out;
        top: 180px;
        z-index: 10;

          .content{
          padding: 12px 6px;
          border-radius: 6px;
          background-color: white;

            .controls{
            position: relative;
            cursor: pointer;
            width: 46px;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: space-between;
            color: #9499A0;

              svg{
              width: 24px;
              height: 24px;
              color: #61666D;
              transition: color .3s ease;
              }
              span{
              user-select: none;
              margin-top: 4px;
              font-size: 12px;
              line-height: 14px;
              transition: color .3s ease;
              cursor: pointer;
              color: #9499A0;
              }
            }
            .controls:not(:first-of-type){
            margin-top: 16px;
            }
            .controls:hover{
              svg{
                color: #00AEEC;
              }
              span{
                color: #00AEEC;
              }
            }
            .video-share{
            position: absolute;
            top: 50px;
            }
          }
        }
    }
}

</style>