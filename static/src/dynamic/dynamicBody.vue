<template>
  <div class="dynamic-body" v-loading.fullscreen="loading" element-loading-background="rgba(122, 122, 122, 0)" :style="{backgroundImage: 'url('+backgroundImgSrc[bIndex]+')'}">
      <div class="left">
          <div class="user-info">
              <div class="top">
                  <img class="avatar" @click="openHome(1,store.userId)" :src="store.userInformation.avatarAddress">
                  <div class="right">
                    <div class="user-name" @click="openHome(1,store.userId)">{{ store.userInformation.userName }}</div>
                    <div class="level-container"><img v-if="store.userInformation?.grade" class="level-icon" :src="'../img/'+store.userInformation.grade+'级.png'"></div>
                  </div>
              </div>
              <div class="bottom">
                 <a :href="'../home?homeMenu=8&userId='+store.userId" target="_blank">
                    <div>{{ store.userInformation.followNumber }}</div>
                    <div>关注</div>
                 </a>
                 <a :href="'../home?homeMenu=9&userId='+store.userId" target="_blank">
                    <div>{{ store.userInformation.fansNumber }}</div>
                    <div>粉丝</div>
                 </a>
                 <a :href="'../home?homeMenu=2&userId='+store.userId" target="_blank">
                    <div>{{ store.userInformation.ownDynamicNumber }}</div>
                    <div>动态</div>
                 </a>
              </div>
          </div>
      </div>
      <div class="center">
         <div class="top">
              <div class="publish-container">
                  <div class="title-input-container">
                     <input class="title-input" type="text" v-model="title" placeholder="好的标题更容易获得支持，选填20字" maxlength="20">
                     <div v-show="title.length" class="delete-icon" @click="title=''">
                        <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 8 8" width="8" height="8" style="width: 8px; height: 8px;"><path d="M1.28895 1.2898733333333334C1.5818566666666654 0.9969799999999991 2.0567133333333345 0.9969799999999991 2.349603333333332 1.2898733333333334L3.9994666666666667 2.939749999999999L5.6498333333333335 1.2893266666666667C5.942750000000002 0.9964333333333324 6.4176499999999965 0.9964333333333324 6.71048333333333 1.2893266666666667C7.00351666666667 1.5822166666666677 7.00351666666667 2.0570899999999996 6.71048333333333 2.3499799999999986L5.060116666666667 4.0004L6.710050000000003 5.650416666666669C7.003083333333336 5.943250000000003 7.003083333333336 6.418149999999997 6.710050000000003 6.7109833333333295C6.417216666666663 7.0039 5.942316666666663 7.0039 5.6494 6.7109833333333295L3.9994666666666667 5.06105L2.3500533333333333 6.710533333333332C2.057143333333334 7.0033666666666665 1.5822900000000004 7.0033666666666665 1.28938 6.710533333333332C0.996489999999999 6.4175 0.996489999999999 5.942716666666669 1.28938 5.649883333333336L2.938816666666666 4.0004L1.28895 2.3505266666666653C0.9960566666666673 2.0576366666666663 0.9960566666666673 1.5827633333333344 1.28895 1.2898733333333334z" fill="currentColor"></path></svg>                      
                     </div>
                     <span v-show="title.length" class="title-length">{{ title.length }}</span>
                  </div>
                  <div class="content-input-container">
                      <div class="content-input-container-inner">
                         <div class="content-input" ref="contentInput" :placeholder="placeholder" 
                         contenteditable="true" @click="handleContentInput" @input="handleContentInput"
                         @keyup.left="handleContentInput"
                         @keyup.right="handleContentInput"
                         @keyup.up="handleContentInput"
                         @keyup.down="handleContentInput"
                         @blur="getAfterBlurIndex"
                         ></div>
                         <div></div>
                      </div>
                  </div>
                  <div v-show="imgFlag" ref="imgUploadContainer" class="img-upload-container">
                     <div v-show="imgList2.length" class="img-upload-container-inner" v-for="(imgUrl,index) in imgList2" :key="imgUrl">
                        <img :src="imgUrl">
                        <div class="delete-icon" @click="deleteImg(index)"></div>
                     </div>
                     <div v-show="imgList2.length<9" @click="openFile2" class="img-upload-btn"></div>
                  </div>
                  <div class="control-container">
                     <div class="left">
                        <div @click="emojiFlag=!emojiFlag" class="emoji-container">
                            <svg style="width: 24px; height: 24px;" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 24 24" width="24" height="24"><path d="M9.2504 14.2737C9.65164 14.1708 10.06032 14.41265 10.16322 14.81385C10.27045 15.2321 10.52235 15.5175 10.76145 15.5313C10.96715 15.54315 11.18695 15.34575 11.282 15.03025C11.37745 14.71335 11.6693 14.4965 12.0002 14.49655C12.33115 14.49655 12.6229 14.7135 12.7183 15.0304C12.8132 15.3458 13.0329 15.54315 13.23855 15.5313C13.47775 15.5175 13.72975 15.23205 13.83705 14.81375C13.94005 14.41255 14.3488 14.17075 14.74995 14.2737C15.1512 14.3767 15.39295 14.7854 15.29005 15.1866C15.0153 16.25705 14.259 16.9749 13.32485 17.0288C12.82845 17.05745 12.3647 16.8875 12.00005 16.5679C11.63535 16.8875 11.17155 17.05745 10.67515 17.0288C9.74097 16.9749 8.98473 16.257 8.71022 15.18645C8.60732 14.78525 8.84917 14.3766 9.2504 14.2737z" fill="currentColor"></path><path d="M6.39641 9.19816C6.64297 8.86531 7.11266 8.79536 7.4455 9.04192L10.00893 10.9407C10.2108 11.09025 10.3244 11.33095 10.3115 11.5819C10.29865 11.83275 10.16095 12.06055 9.94487 12.18865L7.38143 13.7077C7.02509 13.91885 6.56503 13.8012 6.35386 13.4448C6.14269 13.0885 6.26038 12.62845 6.61673 12.41725L8.20864 11.4739L6.55266 10.24725C6.21982 10.0007 6.14986 9.53101 6.39641 9.19816z" fill="currentColor"></path><path d="M17.6036 9.19816C17.35705 8.86531 16.88735 8.79536 16.5545 9.04192L13.99105 10.9407C13.7892 11.09025 13.6756 11.33095 13.6885 11.5819C13.7014 11.83275 13.839 12.06055 14.05515 12.18865L16.61855 13.7077C16.9749 13.91885 17.43495 13.8012 17.64615 13.4448C17.8573 13.0885 17.7396 12.62845 17.38325 12.41725L15.79135 11.4739L17.44735 10.24725C17.7802 10.0007 17.85015 9.53101 17.6036 9.19816z" fill="currentColor"></path><path d="M12 3.49805C7.30558 3.49805 3.5 7.30363 3.5 11.998C3.5 16.69245 7.30558 20.498 12 20.498C16.6944 20.498 20.5 16.69245 20.5 11.998C20.5 7.30363 16.6944 3.49805 12 3.49805zM2 11.998C2 6.4752 6.47715 1.99805 12 1.99805C17.52285 1.99805 22 6.4752 22 11.998C22 17.5209 17.52285 21.998 12 21.998C6.47715 21.998 2 17.5209 2 11.998z" fill="currentColor"></path></svg>
                            <div v-show="emojiFlag" class="comment-emoji-container" @click.stop>
                              <span class="head-span">小黄脸</span>
                              <span
                                class="comment-emoji-content"
                                v-for="index in 76"
                                :key="index"
                                @click="addEmoji(index)"
                              >
                                <img :src="'../img/emoji/' + index + '.png'" />
                              </span>
                          </div> 
                        </div>
                        <div @click="openFile" class="img-container">
                            <svg style="width: 24px; height: 24px;" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 24 24" width="24" height="24"><path d="M8.5 8.47247C7.93252 8.47247 7.4725 8.93249 7.4725 9.49997C7.4725 10.06746 7.93252 10.52745 8.5 10.52745C9.06747 10.52745 9.5275 10.06746 9.5275 9.49997C9.5275 8.93249 9.06747 8.47247 8.5 8.47247zM6.0275 9.49997C6.0275 8.13444 7.13448 7.02746 8.5 7.02746C9.86552 7.02746 10.9725 8.13444 10.9725 9.49997C10.9725 10.86545 9.86552 11.97245 8.5 11.97245C7.13448 11.97245 6.0275 10.86545 6.0275 9.49997z" fill="currentColor"></path><path d="M12 4.99756C9.48178 4.99756 7.283 5.12566 5.73089 5.25153C4.65221 5.339 3.81611 6.16303 3.72 7.23205C3.60607 8.49931 3.5 10.1705 3.5 11.9976C3.5 13.82465 3.60607 15.4958 3.72 16.7631C3.81611 17.8321 4.65221 18.65615 5.73089 18.7436C7.283 18.8695 9.48178 18.9976 12 18.9976C14.5185 18.9976 16.7174 18.8694 18.2696 18.74355C19.3481 18.6561 20.184 17.8323 20.2801 16.76355C20.394 15.49685 20.5 13.82595 20.5 11.9976C20.5 10.16915 20.394 8.49828 20.2801 7.23156C20.184 6.16281 19.3481 5.33903 18.2696 5.25156C16.7174 5.12569 14.5185 4.99756 12 4.99756zM5.60965 3.75644C7.19232 3.6281 9.43258 3.49756 12 3.49756C14.5677 3.49756 16.8081 3.62812 18.3908 3.75647C20.1881 3.90224 21.6118 5.2923 21.7741 7.09724C21.8909 8.39641 22 10.11355 22 11.9976C22 13.88155 21.8909 15.5987 21.7741 16.8979C21.6118 18.7028 20.1881 20.0929 18.3908 20.23865C16.8081 20.367 14.5677 20.4976 12 20.4976C9.43258 20.4976 7.19232 20.367 5.60965 20.2387C3.81206 20.0929 2.38831 18.7025 2.22603 16.8974C2.10918 15.5977 2 13.8803 2 11.9976C2 10.1148 2.10918 8.39738 2.22603 7.09774C2.38831 5.29263 3.81206 3.90221 5.60965 3.75644z" fill="currentColor"></path><path d="M15.8624 13.5532C15.4524 13.1431 14.78755 13.1431 14.37745 13.55315L12.3451 15.58555C11.4912 16.43945 10.1065 16.43915 9.25299 15.5847C8.91511 15.2465 8.36696 15.24635 8.02887 15.5844L6.5303 17.08275C6.23739 17.3756 5.76251 17.3756 5.46964 17.0827C5.17676 16.7898 5.17679 16.3149 5.4697 16.022L6.96829 14.52365C7.89238 13.59965 9.39064 13.6001 10.3142 14.5246C10.58205 14.7927 11.0165 14.79285 11.28445 14.5249L13.31675 12.4925C14.31265 11.4966 15.92735 11.49665 16.9232 12.49265L18.5304 14.1001C18.82325 14.393 18.8232 14.86785 18.53025 15.16075C18.23735 15.4536 17.7625 15.4536 17.4696 15.16065L15.8624 13.5532z" fill="currentColor"></path></svg>
                            <input @click.stop type="file" ref="fileInput" accept="image/*" style="display: none;" multiple  @change="handleImg">
                        </div>
                        <div @click="addEit" class="at-container">
                            <svg style="width: 24px; height: 24px;" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 24 24" width="24" height="24"><path d="M11.357 8.46436C9.86897 8.46436 8.46413 9.92845 8.46413 12.0001C8.46413 14.0717 9.86897 15.5358 11.357 15.5358C12.845 15.5358 14.2498 14.0717 14.2498 12.0001C14.2498 9.92845 12.845 8.46436 11.357 8.46436zM6.96413 12.0001C6.96413 9.33784 8.82121 6.96436 11.357 6.96436C13.89275 6.96436 15.7498 9.33784 15.7498 12.0001C15.7498 14.6623 13.89275 17.0358 11.357 17.0358C8.82121 17.0358 6.96413 14.6623 6.96413 12.0001z" fill="currentColor"></path><path d="M15.2144 7.39282C15.62865 7.39282 15.9644 7.72861 15.9644 8.14282L15.9644 13.8378C15.9644 14.91395 16.8735 15.598 17.98735 15.4706C19.01245 15.3533 20.28405 14.4599 20.4329 12.05135C20.4585 11.6379 20.8143 11.32345 21.22775 11.34905C21.64115 11.3746 21.9556 11.73045 21.93005 12.1439C21.74465 15.1426 20.03275 16.74635 18.15785 16.96085C16.3718 17.1652 14.4644 16.01585 14.4644 13.8378L14.4644 8.14282C14.4644 7.72861 14.80025 7.39282 15.2144 7.39282z" fill="currentColor"></path><path d="M12 3.5625C7.3401 3.5625 3.5625 7.3401 3.5625 12C3.5625 16.6599 7.3401 20.4375 12 20.4375C13.6982 20.4375 15.2771 19.9366 16.5998 19.07465C16.94685 18.84855 17.4115 18.94655 17.63765 19.2936C17.86375 19.6406 17.76575 20.1053 17.4187 20.33145C15.8598 21.34725 13.9977 21.9375 12 21.9375C6.51167 21.9375 2.0625 17.48835 2.0625 12C2.0625 6.51167 6.51167 2.0625 12 2.0625C17.48835 2.0625 21.9375 6.51167 21.9375 12C21.9375 12.4142 21.6017 12.75 21.1875 12.75C20.7733 12.75 20.4375 12.4142 20.4375 12C20.4375 7.3401 16.6599 3.5625 12 3.5625z" fill="currentColor"></path></svg>
                            <eit
                              v-show="eitFlag"
                              :eitList="store.eitList"
                              :eitKeyWord="eitKeyWord"
                              @click.stop
                            />
                        </div>
                     </div>
                     <div class="right">
                        <div class="content-length">{{ contentLength }}</div>
                        <div class="publish-btn" :class="{ 'active-btn': contentLength||imgList2.length }" @click="addDynamicF">发布</div>
                     </div>
                  </div>
              </div>
         </div>
         <div class="center">
             <div class="user-list-container" ref="scrollContainer"  @wheel.prevent="handleWheel">
                 <div class="dynamic-item-container" @click="upChecked=0" :class="{ 'active-dynamic-item-container': upChecked===0 }">
                    <div class="img">
                    </div>
                    <div class="name">全部动态</div>
                 </div>
                 <div class="dynamic-item-container" @click="upChecked=up.id" v-for="up in upList" :key="up.id"
                  :class="{ 'active-dynamic-item-container': upChecked===up.id }">
                    <div class="img2" :style="`background-image:url(${up.avatarAddress})`">
                        <span v-show="up.newDynamicFlag" class="info"></span>
                    </div>
                    <div class="name">{{ up.userName }}</div>
                 </div>
             </div>
         </div>
         <div class="center2">
             <div class="title-list">
                 <div :class="{ 'active-title': menu===1 }" @click="menu=1">全部</div>
                 <div :class="{ 'active-title': menu===2 }" @click="menu=2">视频投稿</div>
                 <div :class="{ 'active-title': menu===3 }" @click="menu=3">图文</div>
             </div>
             <div class="line" :style="{ transform: `translateX(${titlePostionX[menu-1]}px)` }"></div>
         </div>
         <div v-show="newAllDynamicFlag&&menu===1" class="center3"><span @click="cleanNewDynamicMessageF">有新动态，点击查看</span></div>
         <div class="bottom">
            <div class="dynamic-container" v-for="dynamic in dynamicList" :key="dynamic.dynamic.id">
               <dynamic2 :dynamic="dynamic" :check-up-id="upChecked"/>
            </div>
            <div v-show="loadMore" class="class-loading2">
               <img src="../img/loading-blue.gif">
               <span>拼命加载中...</span>
            </div>
            <div v-if="noDataFlag&&!loadMore&&dynamicList.length===0" class="no-data">
                <img src="../img/home_nodata.svg">
                <div>好像没有东西诶</div>
            </div>
            <div v-show="fotterFlag&&!loadMore&&dynamicList.length>0" class="fotter">已经到底了</div>
         </div>
      </div>
      <div class="right"></div>
      <!-- 清除上传的图片对话框 -->
      <el-dialog
      v-model="deleteImgDialogFlag"
      append-to-body
      width="320"
      :lock-scroll="false"
      align-center
      style="height: 152px; border-radius: 12px"
      >
          <div class="delete-dialog-title">图片上传</div>
          <div class="delete-dialog-content">
            关闭将清除所有已添加图片，请确认
          </div>
          <div class="delete-dialog-btn-container">
              <button class="delete-dialog-cancel-btn" @click="deleteImgDialogFlag=false">
              点错了
          </button>
          <button
              class="delete-dialog-confirm-btn" @click="deleteImgList"
          >
              清除图片
          </button>
          </div>
      </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { useGlobalStore } from "../store/store";
import eit from "../components/eit";
import { ElMessage } from "element-plus";
import Sortable from "sortablejs";
import dynamic2 from "@/utils/dynamic2.vue";
import { getDynamic,usersDynamic,cleanDynamicMessage,addDynamic,allDynamicMessage } from "@/api/dynamic/index";

const store = useGlobalStore();
const title=ref("");
const contentInput=ref(null);
const contentLength=ref(0);
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
const content=ref("");
const placeholder=ref("有什么想和大家分享的？");
const emojiFlag=ref(false);
const eitFlag=ref(false);
const eitKeyWord=ref("");
const fileInput=ref(null);
const imgFlag=ref(false);
const imgList=reactive([]);
const imgList2=reactive([]);
const deleteImgDialogFlag=ref(false);
const imgUploadContainer=ref(null);
const menu=ref(1);//1全部 2视频投稿 3图文
const titlePostionX=ref([
  28,105,182
]); 
const upChecked=ref(0);
const upList=reactive([]);
const fotterFlag=ref(false);
const dynamicPageNum=ref(1);
const upPageNum=ref(1);
const dynamicList=reactive([]);
const loadMore=ref(false);
const noDataFlag=ref(false);
const newAllDynamicFlag=ref(false);
const loading=ref(false);

onMounted(()=>{

  window.addEventListener('click',handleClickOutside);
  window.addEventListener('scroll',handleScroll);
  nextTick(() => {
    initRange();
  });
  Sortable.create(imgUploadContainer.value, {
    animation: 150, // 拖动动画
    ghostClass: 'sortable-ghost',
    onEnd(event) {
      const movedItem = imgList2.splice(event.oldIndex, 1)[0];
      imgList2.splice(event.newIndex, 0, movedItem);
    }
  });

})

onUnmounted(()=>{
  window.removeEventListener('scroll',handleScroll);
  window.removeEventListener('click',handleClickOutside);
})

//初始化range
function initRange() {
  const selection = window.getSelection();
  if (contentInput.value && selection) {
    // 创建一个新的 Range
    range = document.createRange();

    // 让 Range 选中输入框的末尾
    range.selectNodeContents(contentInput.value);
    range.collapse(false); // collapse到末尾

    // 清空之前的选区
    selection.removeAllRanges();
    selection.addRange(range);
  } else {
    console.warn('contentInput 或 selection 不存在');
  }
}

//点击事件
function handleClickOutside(event){
  
  if(!event.target.closest('.emoji-container')&&!event.target.closest('.content-input-container')){
    emojiFlag.value=false;
  }

  //解决表情中的光标问题
  if (event.target.classList.contains("comment-emoji-img")) {
      const selection = window.getSelection();
      const range = document.createRange();

      // 找到图片的上一个兄弟节点
      const previousNode = event.target.previousSibling;

      // 如果上一个兄弟节点存在
      if (previousNode) {
        range.setStartAfter(previousNode); // 将光标设置到上一个节点之后
        range.setEndAfter(previousNode);
      } else {
        // 如果没有上一个节点，光标设置到图片前
        range.setStartAfter(event.target);
        range.setEndAfter(event.target);
      }
      selection.removeAllRanges();
      selection.addRange(range);
  }

}

//打开我的主页
function openHome(menu,userId){
    window.open(
    `./home?homeMenu=${menu}&userId=${userId}`,
    "_blank",
  );
}

//输入内容
function handleContentInput(){

  let inputDom=contentInput.value;
    if(!inputDom)
        return;

  if (inputDom.innerText.length > 2000)
      inputDom.innerText = inputDom.innerText.slice(0, 2000);
    if (
      inputDom.innerText.trim().length > 0 ||
      inputDom.innerHTML.includes('<img src="../img/emoji/')
    ) {
      content.value=inputDom.innerHTML;
      contentLength.value=inputDom.innerText.trim().length;
      placeholder.value='';
    } else {
      placeholder.value="有什么想和大家分享的？";
      content.value='';
      contentLength.value=0;
    }

    // 获取当前选区和光标位置
    const selection = window.getSelection();
    if (!selection.rangeCount) return; // 如果没有选区，直接返回

    const range = selection.getRangeAt(0);
    if (range.endContainer.parentNode.className === "at-msg") return;
    // 获取光标前的所有文本
    const textBeforeCursor = getTextBeforeCursor(range);

    // 检测是否包含 @ 并提取关键字
    const atIndex = textBeforeCursor.lastIndexOf("@");
    if (atIndex !== -1) {
      const text = textBeforeCursor.substring(atIndex);
      if (text.length > 20 || text.includes(" ")) {
        eitFlag.value = false;
        return;
      }
      eitFlag.value = true;
      eitKeyWord.value = text.substring(1).trim();
    } else {
      eitFlag.value = false;
    }
}

// 获取光标之前的文本，支持嵌套子节点
function getTextBeforeCursor(range) {
  let textBeforeCursor = "";
  let currentNode = range.startContainer;

  // 遍历节点，拼接光标前的文本
  while (currentNode) {
    if (currentNode.nodeType === Node.TEXT_NODE) {
      // 如果是文本节点，拼接到结果中
      textBeforeCursor =
        currentNode.textContent.slice(0, range.startOffset) +
        textBeforeCursor;
      break;
    }
    // 如果是元素节点，继续向上遍历
    currentNode = currentNode.previousSibling || currentNode.parentNode;
  }

  return textBeforeCursor;
}

let range = null;
// 聊天输入框失焦时获取失焦前的光标位置
function getAfterBlurIndex() {
  if (window.getSelection) {
    let sel = window.getSelection();
    if (sel.getRangeAt && sel.rangeCount) {
      range = sel.getRangeAt(0);
    }
  }
}

//添加表情
function addEmoji(index) {
  contentInput.value.focus();
  // 获取当前选区和光标位置
  const selection = window.getSelection();
  // 创建新的按钮元素
  const button = `<img src="../img/emoji/${index}.png" style="width:21px;height:21px;display:inline-block;vertical-align:middle;transform:translateY(-3px);" class="comment-emoji-img" />`;
  // 插入新按钮到光标位置
  const fragment = range.createContextualFragment(button); // 创建 DOM 片段
  const lastChild = fragment.lastChild;
  range.insertNode(lastChild); // 插入按钮
  // 设置光标位置到按钮后面
  const newRange = document.createRange();
  newRange.setStartAfter(lastChild);
  newRange.setEndAfter(lastChild);
  selection.removeAllRanges();
  selection.addRange(newRange);
  handleContentInput();
}

//添加eit
function addEit() {

// 创建一个文本节点
const atSymbol = document.createTextNode("@");
// 获取光标位置并插入 @
const selection = window.getSelection();
range.insertNode(atSymbol);
// 将光标移动到 @ 后面
range.setStartAfter(atSymbol);
range.setEndAfter(atSymbol);
selection.removeAllRanges();
selection.addRange(range);
eitKeyWord.value = "";
emojiFlag.value = false;
handleContentInput();
eitFlag.value = true;
handleContentInput();
}

//监视eitUserName变化
watch(()=>store.eitUserName,() => {
  if (store.eitUserName.trim() !== "") {
    insertEit();
  }
});        

//添加@用户
function insertEit() {
  if (eitKeyWord.value.trim() === "" && store.eitUserName === "") return;

  if(!eitFlag.value)
  return;
  // 找到最近的 `@` 并删除关键字
  const containerText = range.startContainer.textContent;
  const startOffset = range.startOffset;
  const atIndex = containerText.lastIndexOf("@", startOffset - 1);

  if (atIndex !== -1) {
    // 构建新内容，删除 @关键字
    const beforeAt = containerText.slice(0, atIndex); // `@` 之前的内容
    const afterAt = containerText.slice(startOffset); // 光标之后的内容
    range.startContainer.textContent = beforeAt + afterAt;

    // 更新 Range 位置
    const newOffset = atIndex; // 新光标位置在 `@` 之前
    range.setStart(range.startContainer, newOffset);
    range.setEnd(range.startContainer, newOffset);
  }

      // **创建新的按钮元素**
    const buttonHtml = `<a m1a,s,a2href="./home?homeMenu=1&userId=${store.eitUserId}" target="_blank" data-eitt-userId="${store.eitUserId}" 
                          contentEditable="false" class="at-msg" style="color:#008ac5;margin-right:3px;margin-top:2px;">
                          @${store.eitUserName}</a>`;

    // **确保页面中只有一个占位符**
    const oldPlaceholder = document.getElementById("cursor-placeholder");
    if (oldPlaceholder) oldPlaceholder.remove();

    // **创建占位符**（零宽字符，标记光标位置）
    const placeholder = document.createElement("span");
    placeholder.id = "cursor-placeholder";
    placeholder.textContent = "\u200B"; // 零宽字符，防止影响排版
    range.insertNode(placeholder);

    // **插入按钮**
    const fragment = range.createContextualFragment(buttonHtml);
    range.insertNode(fragment);

    // **保存 innerHTML**;
    content.value = contentInput.value.innerHTML;
    contentInput.value.innerHTML = content.value;

    // **恢复光标**
    setTimeout(() => {
      const newPlaceholder = document.getElementById("cursor-placeholder");
      if (newPlaceholder) {
        const newRange = document.createRange();
        const newSelection = window.getSelection();

        newRange.setStartAfter(newPlaceholder);
        newRange.setEndAfter(newPlaceholder);
        newSelection.removeAllRanges();
        newSelection.addRange(newRange);

        // **移除占位符**
        newPlaceholder.remove();
      }
    }, 0);

  // 清空输入框
  eitKeyWord.value = "";
  eitFlag.value = false;
  store.setEitUserName("");
  store.setEitUserId(null);
  handleContentInput();
}

watch(emojiFlag,()=>{
  if(emojiFlag.value){
    eitFlag.value = false;
  }
})

//打开文件选择器
function openFile(){

  if(imgList2.length>0&&imgFlag.value){
      deleteImgDialogFlag.value=true;
      return;
  }
  imgFlag.value=!imgFlag.value;
  if(imgFlag.value){
    fileInput.value.click();
  }
}

//打开文件选择器
function openFile2(){
  fileInput.value.click();
}

//上传图片触发
function handleImg(event) {
  let files = event.target.files;
  for (let i = 0; i < files.length; i++) {
    if (
      files[i].type.startsWith("image/") &&
      files[i].size <= 10 * 1024 * 1024&&!files[i].type.endsWith("gif")
    ) {
      imgList.push(files[i]);
    }
    if (!files[i].type.startsWith("image/")) continue;
    if (files[i].size > 10 * 1024 * 1024) {
      ElMessage({
        message: "图片不能超过10MB",
        type: "info",
        plain: true,
        duration: 1700,
      });
    }
  }

  if (imgList.length + imgList2.length > 9) {
    ElMessage({
      message: "上传图片不能超过9张",
      type: "info",
      plain: true,
      duration: 1700,
    });
  }

  imgList.forEach((file) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => {
      if (imgList2.length < 9) imgList2.push(reader.result);
    };
  });
  event.target.value = "";
  imgList.length = 0;
}

//删除图片
function deleteImg(index) {
  imgList2.splice(index, 1);
}

//关闭图片上传
function deleteImgList(){
  deleteImgDialogFlag.value=false;
  imgFlag.value=false;
  imgList2.length=0;
  imgList.length=0;
}

//获取动态列表
async function getDynamicF(){
    
    if(loadMore.value)
        return;
    loadMore.value=true;
    let date=new Date();
    getDynamic(store.token,store.userId,upChecked.value,dynamicPageNum.value++,menu.value).then(res=>{

        if(res.data.code===1){
            pushData(res.data.data);
            if(!res.data.data||res.data.data.length===0)
            {
                fotterFlag.value=true;
                if(dynamicList.length===0){
                  noDataFlag.value=true;
                }
            }
            const date2=new Date();
                setTimeout(() => {
                loadMore.value=false;
            }, date2 - date<300?300:date2 - date);
        }
    })
}

//监视菜单变化
watch(menu,()=>{
  dynamicPageNum.value=1;
  dynamicList.length=0;
  fotterFlag.value=false;
  getDynamicF();
})

//监视upChecked变化
watch(upChecked,()=>{
  dynamicPageNum.value=1;
  fotterFlag.value=false;
  dynamicList.length=0;

  const index=upList.findIndex(item=>item.id===upChecked.value);
  if(upChecked.value!==0&&upList[index].newDynamicFlag)
  {
    cleanDynamicMessage(store.token,store.userId,upChecked.value);
    upList[index].newDynamicFlag=0;
  }  
  getDynamicF();
})

//初始化查询
let onceFlag2=true;
watch(()=>store.userId,()=>{
    if(onceFlag2&&store.userId){
        onceFlag2=null;
        getDynamicF();
        usersDynamicF();
        setInterval(()=>{
          allDynamicMessage(store.token,store.userId).then(res=>{
              if(res.data.code===1){
                 newAllDynamicFlag.value=res.data.data;
              }
          })
        },60000)
    }
})

//去除重复数据
function pushData(newData) {
if(newData===null)
    return;
    const idSet = new Set(dynamicList.map(item => item.dynamic.id)); // 获取已有数据的 ID
    const uniqueData = newData.filter(item => !idSet.has(item.dynamic.id)); // 过滤掉重复的
    dynamicList.push(...uniqueData); // Vue 响应式更新
}

//滚动事件
function handleScroll() {
  if(!loadMore.value&&!fotterFlag.value){
        if((window.innerHeight+document.documentElement.scrollTop)>=document.documentElement.offsetHeight-100){
            getDynamicF();
        }
    }
}

//查询用户关注的up
function usersDynamicF(){
  if(!scrolledFlag)
    return;
  scrolledFlag=false;
  usersDynamic(store.token,store.userId,upPageNum.value++).then(res=>{
      if(res.data.code===1){
          upList.push(...res.data.data);
        if(res.data.data===null||res.data.data.length===0){
          scrolledFlag=false;
        }
        else{
          scrolledFlag=true;
        }
      }
  })

}

//横向滚动
const scrollContainer=ref(null);
let scrolledFlag=true;
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
  usersDynamicF();
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

//点击查看新动态
function cleanNewDynamicMessageF(){
   store.userInformation.dynamicNumber=0;
   dynamicPageNum.value=1;
   dynamicList.length=0;
   fotterFlag.value=false;
   newAllDynamicFlag.value=false;
   getDynamicF();
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

//新增动态
async function addDynamicF(){

  if(content.value.trim() === ""&&imgList2.length===0)
    return;
    
    if(imgList2.length)
      loading.value=true;

    // 网址超链接
    content.value = content.value.replace(
        /(https?:\/\/[^\s<>"]+?)(?=\s|&nbsp;|<div>|<\/div>|$)\b/g,
        '<a href="$1" target="_blank"><img style="width: 14px;height: 14px;padding-right: 2px;" src="../img/网页链接.png">$1<br></a>',
      );
    // @样式
    content.value = content.value.replace(
      /color:#008ac5;/g,
      "color:#008ac5;cursor:pointer;",
    );

    content.value = content.value.replace(/m1a,s,a2/g,"");
    content.value = content.value.replace(/data-eitt-userid/g,"data-eit-userid");

    // 替换本站链接格式
    const container = document.createElement("div");
    container.innerHTML = content.value; // 使用 DOM 操作管理 HTML
    
    let atUserList = [];

    const links = container.querySelectorAll("a");
    for (const link of links) {
      const match = link.href.match(/BV([0-9]+)/); // 检查链接是否匹配 BV 格式
      if (match) {
        try {
          const res = await apiClient.get(`/video/getVideoTitle/${match[1]}`);
          if (res.data.code === 1) {
            // 动态替换 HTML 中的链接
            link.innerHTML = `<img style="width: 14px;height: 14px;padding-right: 2px;transform: translateY(1px);" src="../img/视频标题.png">${res.data.data}<br>`;
            link.target = "_blank"; 
          }
        } catch (error) {}
      }
      const userId = link.getAttribute("data-eit-userid");
      const index=atUserList.findIndex((item) => item.atUserId === userId);
      if(userId&&index===-1&&userId!==store.userId)
      {
        let at={
          userId: store.userId,
          atUserId: userId,
        }
      atUserList.push(at);
      }
      
    }
    //去除多余的<br>
    content.value = container.innerHTML.replace(/(<div>\s*<br\s*\/?>\s*<\/div>\s*)+$/, '');

  let dynamic={
    followId: store.userId,
    imgAddress: imgList2.length?imgList2.join(",,"):null,
    content: content.value||"分享图片",
    title: title.value.length>0?title.value:null,
    upFlag: 0,
  };

  let addDynamicPo={
    dynamic,
    ats: atUserList,
  };

  addDynamic(store.token,addDynamicPo).then(res=>{
      if(res.data.code===1){
          content.value="";
          imgList2.length=0;
          title.value="";
          dynamicPageNum.value=1;
          dynamicList.length=0;
          fotterFlag.value=false;
          contentInput.value.innerHTML="";
          contentLength.value=0;
          placeholder.value="有什么想和大家分享的？";
          ElMessage({
            message: res.data.data,
            type: "info",
            duration: 1700,
          });
          getDynamicF();
      }
      else{
          ElMessage({
            message: res.data.msg,
            type: "info",
            duration: 1700,
          });
      }
      loading.value=false;
  })
}  

</script>

<style lang="scss" scoped>


*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.dynamic-body{
width: 100%;
background-size: cover;
background-repeat: no-repeat;
margin: 0 auto;
padding-top: 8px;
padding-bottom: 165px;
min-height: 100vh;
height: auto;
display: flex;
background-attachment: fixed;
min-width: 1044px;
max-width: 2560px;
justify-content: center;
position: relative;
top: 64px;

  .left{
  width: 264px;
  margin-right: 12px;

    .user-info{
    padding: 20px 16px;
    background: white;
    border-radius: 6px;

      .top{
      display: flex;
      align-items: center;
      margin-bottom: 17px;

        .avatar{
        width: 48px;
        height: 48px;
        object-fit: cover;
        border-radius: 50%;
        margin-right: 8px;
        cursor: pointer;
        }
        .right{
        flex: 1;
        min-width: 30px;
        height: 48px; 
          .user-name{
          cursor: pointer;
          text-overflow: ellipsis;
          overflow: hidden;
          white-space: nowrap;
          transition: color 0.3s ease;
          font-size: 15px;
          font-weight: 600;
          margin-top: 4px;
          color: #18191c;
          }
          .user-name:hover{
          color: #40c5f1;
          }
          .level-container{
          display: flex;
          align-items: center;
          height: 21px;

            .level-icon{
            width: 22px;
            height: 11px;
            }
          }
        }
      }
      .bottom{
      display: flex;
      justify-content: space-between;
      text-align: center;

        a{
        max-width: 40%;
        min-width: 40px;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        align-items: left;
        cursor: pointer;
          div:nth-child(1){
          overflow: hidden;
          transition: color 0.3s ease;
          text-overflow: ellipsis;
          white-space: nowrap;
          height: 20px;
          font-weight: 600;
          line-height: 20px;
          font-size: 15px;
          color: #18191c;
          }
          div:nth-child(2){
          height: 17px;
          line-height: 17px;
          transition: color 0.3s ease;
          margin-top: 2px;  
          font-size: 13px;
          color: #9499a0;
          }
        }
        a:hover{
          div{
            color: #40c5f1;
          }
        }
      }
    }
  }
  .center{
  width: 724px;
  margin-right: 12px;

    .top{
    width: 100%;
    margin-bottom: 8px;

      .publish-container{
      background-color: white;
      border-radius: 6px;
      box-sizing: border-box;
      padding: 44px 20px 12px 22px;
      position: relative;
      width: 100%;

        .title-input-container{
        height: 24px;
        margin-top: 6px;
        padding-right: 48px;
        position: relative;
        width: 100%;

          .title-input{
          background: white;
          border: none;
          font-size: 15px;
          font-weight: 500;
          height: 24px;
          line-height: 24px;
          outline: none;
          width: 100%;
          }
          .delete-icon{
          position: absolute;
          top: 4.5px;
          right: 28px;  
          align-items: center;
          background-color: #c9ccd0;
          border-radius: 50%;
          color: white;
          display: flex;
          height: 16px;
          cursor: pointer;
          justify-content: center;
          width: 16px;
          transition: background-color 0.3s ease;

            svg{
            width: 8px;
            height: 8px;
            }
          }
          .delete-icon:hover{
          background-color: #b3b6b8;
          }
          .title-length{
          position: absolute;
          top: 3px;
          right: -1px;
          font-size: 13px;
          color: #9499a0;
          }
        }
        .content-input-container{
        margin: 4px 14px 12px 0;
        min-height: 40px;
        padding-right: 4px;
        position: relative;

          .content-input-container-inner{
          max-height: 180px;
          cursor: text;
          height: 100%;
          overflow-y: auto;
          position: relative;
          width: 100%;

              .content-input{
              font-size: 15px;
              line-height: 24px;
              min-height: 24px;
              word-wrap: break-word;
              background-color: white;
              color: #18191c;
              letter-spacing: 1px;
              outline: none;
              padding-right: 5px;
              position: relative;
              vertical-align: baseline;
              width: 100%;
              word-break: break-all;
              }
              .content-input::before{
              color: #9499a0;
              content: attr(placeholder);
              font-size: inherit;
              letter-spacing: inherit;
              line-height: inherit;
              pointer-events: none;
              position: absolute;
              }
          }
        }
        .img-upload-container{
        width: 100%;
        display: flex;
        flex-wrap: wrap;

          .img-upload-container-inner{
          position: relative;  
          width: 92px;
          height: 92px;
          border-radius: 4px;
          margin-right: 7px;
          cursor: move;
          margin-bottom: 8px;
          background-color: #f1f2f3;  

            img{
            width: 92px;
            height: 92px;
            border-radius: 4px;
            object-fit: cover;
            z-index: 10;
            }
            .delete-icon{
            background: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAACXBIWXMAABYlAAAWJQFJUiTwAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAQvSURBVHgB7ZlNbBNXEIBn3qZpiZNDU8mOwVQiB9oqtUXoJeFKz4lUqZUaVYK2VOqhPVaq6BlUqccUCcQBgVBAgJDInVMQEBAJeHMJB+disLMSF4gDBPyGN5sYjPfvPXtxgvB38frtrHbm7byZefMAOnT4sEGIiWx2NGt9LAdB4tdENAiICTWc2LhdAaJlRHSqEpegCrZt37AhBloyIJfLJSzrkzGJYhzeKKsJOerl9ss1OLewMLsMTdKUAay4ED0/koBxiAEEutqsIcYGDA+PqBmHCTCe8SjIESSn5udvXzV5yjIR3rNn5JCadVa+G2IHE4RiJJ3KJMrl4pz2UzpCrstY2/4hxCy0A6ICVZ8ezufzlShRARqgte1o25R3X4iDPGE6opEuxG4DAkeh3SCmdNwp1IDc8Oh+9Y0OwmaB8GVqIFNZLhcXg0QCXSiX25dEkBOwyaCKeLwGg+4HGiC6aEI9nYTNx805QTd9DeDZJ6D9sEXghBn0FXwNcGffgD//+H3XwQM/ZXTlWdZEniGR8M36Xb7CJLPsfLrK/PrzgSG+3p5O9xz997/7YfKH//5r9w/ff7ebr/v6ej+a/P/4EmggBI2pnynPeOMAV5Umvl9vJivGCgbJ1ivPrDxZeQH6JLLfjHpykSeMprbv/FZNvnbSunsv/7i/vx+Ghr76jP/zL/+fuXb9EYQof+Hi5fuTx/RmvwZJWnLKDxZDDUinM+NqWo38k5UNM8JP+ShX8wVxbblUnKkf8q4BAUkgMKamUE3ReoVjUR7csnuwccxrAEHTsT/MCKYV5ddBTyj1C6Mt1fmsICvaON668i5aBrxX+BkQWYOH0bhga0SFWE08unkNIGraAL9oU+9OLRtB4Nkz+2Ri5NhsvJCjQmXjwm5mPRCC0zjmyQPJgc8/RaS9YECU8lF5QtsAiTNOuWiHGpDakelW5YF2JerWQr+s10J+yocZ8ez581XO5KAJCTznlIpvfQXPGrDvuB0z7XXQ15vojlK+RmOINevpkLOh21v4VqNS4rSq/gI3EfVMHjtRWFmprPH1qdNni1HybESpVFrtVZWojnwN7uIFjHvhzQN29ZyHLUR1jQ75de58Exn3YyTANGwRuPUY1HYMzMT4cpU3Dy0ltXggh/umQXcDDXC/gqDAB9sF90vDmr6hfSHn4YPFZDrTqxbKF7AJsBvfm791KUwmsjOn4u5cemAHbzNT0E5UfzQ/P3skSkyrGpXVp0dUHVKANkGENjd3dWSNckl278hvyuIxeIew29hzN0/qyhudD7A7pQZ2Ou7WDjHmAw7goHHGvjM7ZfJQk0dM+5Kgml8YU/eOZ53Dts55QCMtHvJtGLLeCDMtwSsScRpfVK40o3iN+I5ZuelUhawlcJckmVS9JY5ar49ZpdooWWgVQFQXqiQKfoVZhw4dzHkFUC3Woi8BiWsAAAAASUVORK5CYII=) 50% / cover no-repeat;
            cursor: pointer;
            height: 24px;
            position: absolute;
            right: 2px;
            top: 2px;
            width: 24px;
            z-index: 2;
            }
          }
          .img-upload-btn{
          border: 2px dashed #e3e5e7;
          border-radius: 4px;
          cursor: pointer;
          height: 92px;
          margin-bottom: 10px;
          position: relative;
          transition: border-color .3s ease;
          width: 92px;
          }
          .img-upload-btn:before{
          background-color: #e3e5e7;
          border-radius: 4px;
          content: "";
          height: 4px;
          left: 50%;
          position: absolute;
          top: 50%;
          transform: translate3d(-50%, -50%, 0);
          transition: background-color .3s ease;
          width: 28px;
          }
          .img-upload-btn::after{
          background-color: #e3e5e7;
          border-radius: 4px;
          content: "";
          height: 4px;
          left: 50%;
          position: absolute;
          top: 50%;
          transform: translate3d(-50%, -50%, 0) rotate(90deg);
          transition: background-color .3s ease;
          width: 28px;
          }
          .img-upload-btn:hover:before{
          background-color: #80daf6;
          }
          .img-upload-btn:hover:after{
          background-color: #80daf6;
          }
          .img-upload-btn:hover{
          border-color: #80daf6;
          }

        }
        .control-container{
        width: 100%;  
        height: 32px;
        align-items: center;
        display: flex;
        justify-content: space-between;

          .left{
          align-items: center;
          display: flex;
          position: relative;

            .emoji-container{
            position: relative;
            background-repeat: no-repeat;
            background-size: contain;
            color:#9499a0;
            cursor: pointer;
            height: 24px;
            user-select: none;
            width: 24px;
            transition: color 0.3s ease;

            .comment-emoji-container {
              position: absolute;
              width: 363px;
              height: 271px;
              left: -8px;
              top: 30px;
              overflow: hidden;
              background-color: white;
              border: 1px solid #e3e5e7;
              border-radius: 8px;
              padding-left: 11px;
              padding-bottom: 30px;
              z-index: 1000;
              overflow-y: auto;
              cursor: default;
              .head-span {
                position: absolute;
                left: 15px;
                top: 13px;
                color: #61666d;
                font-size: 12px;
              }

            .comment-emoji-content {
              position: relative;
              top: 37px;
              width: 32px;
              height: 32px;
              display: inline-flex;
              justify-content: center;
              align-items: center;
              cursor: pointer;
              background-color: white;
              border-radius: 4px;
              transition: background-color 0.1s ease;
              img {
                width: 24px;
                height: 24px;
              }
            }

            .comment-emoji-content:hover {
              background-color: #e3e5e7;
            }
            }
            .comment-emoji-container::-webkit-scrollbar {
              width: 5px; /* 滚动条的宽度 */
              border-radius: 30px; /* 滚动条滑块的圆角 */
            }
            .comment-emoji-container::-webkit-scrollbar-thumb {
              background: #e3e5e7; /* 滚动条的滑块 */
              border-radius: 10px;
            }
            }
            .emoji-container:hover{
            color: #00aeec;
            }
            .img-container{
            background-repeat: no-repeat;
            background-size: contain;
            color: #9499a0;
            cursor: pointer;
            height: 24px;
            margin-left: 16px;
            user-select: none;
            width: 24px;
            transition: color 0.3s ease;
            }
            .img-container:hover{
            color: #00aeec;
            }
            .at-container{
            background-repeat: no-repeat;
            background-size: contain;
            color: #9499a0;
            cursor: pointer;
            height: 24px;
            margin-left: 16px;
            user-select: none;
            width: 24px;
            transition: color 0.3s ease;
            }
            .at-container:hover{
              color: #00aeec;
            }
          }
          .right{
          display: flex;
          justify-content: right;

            .content-length{
            line-height: 32px;  
            color: #9499a0;
            font-size: 14px;
            margin-right: 25px;
            user-select: none;
            position: relative;
            }
            .publish-btn{
            cursor: not-allowed;
            min-width: 70px;
            opacity: .5; 
            align-items: center;
            background: #00aeec;
            border-radius: 6px;
            box-sizing: border-box;
            color: white;
            display: flex;
            font-size: 14px;
            height: 32px;
            justify-content: center;
            user-select: none;
            width: 70px;
            }
            .active-btn{
            cursor: pointer;
            transition: background-color 0.3s ease;
            opacity: 1;
            }
            .active-btn:hover{
            background-color: #40c5f1;
            opacity: 1;
            }
          }
        }
      }
    }
    .center{
    width: 100%;  
    align-items: center;
    background: white;
    border-radius: 6px;
    box-sizing: border-box;
    display: flex;
    position: relative;
    margin-bottom: 8px;
    user-select: none;

      .user-list-container{
      flex: 1;
      display: flex;
      overflow-x: hidden;
      overflow-y: hidden;
      padding: 9px 0 10px 8px;

        .dynamic-item-container{
        position: relative;  
        align-items: center;
        box-sizing: border-box;
        cursor: pointer;
        display: flex;
        flex-direction: column;
        flex-shrink: 0;
        height: 100px;
        margin-right: 6px;
        padding-top: 8px;
        width: 68px;

          .img{
          align-items: center;
          background-color: #b9e9f9;
          background-image: url(data:image/png;base64,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);
          background-position: 50%;
          transition: all .3s ease;
          background-repeat: no-repeat;
          background-size: cover;
          display: flex;
          justify-content: center;
          border: 1px solid white;
          border-radius: 50%;
          box-shadow: 0 0 0 1px white;
          flex-shrink: 0;
          height: 52px;
          margin: 1px;
          position: relative;
          width: 52px;

            .info{
            background-color: #00aeec;
            border: 2px solid white;
            border-radius: 50%;
            bottom: 0;
            height: 12px;
            position: absolute;
            right: 0;
            width: 12px;
            }
          }
          .img2{
          align-items: center;
          background-color: #b9e9f9;
          background-position: 50%;
          transition: all .3s ease;
          background-repeat: no-repeat;
          background-size: cover;
          display: flex;
          justify-content: center;
          border: 1px solid white;
          border-radius: 50%;
          box-shadow: 0 0 0 1px white;
          flex-shrink: 0;
          height: 50px;
          margin: 1px;
          position: relative;
          width: 50px;

            .info{
            background-color: #00aeec;
            border: 2px solid white;
            border-radius: 50%;
            bottom: 0;
            height: 12px;
            position: absolute;
            right: 0;
            width: 12px;
            }
          }
          .name{
          -webkit-line-clamp: 2;
          color: #6d757a;
          font-size: 13px;
          letter-spacing: 0;
          line-height: 16px;
          transition: all .3s ease;
          margin-top: 4px;
          text-align: center;
          user-select: none;
          }
        }
        .dynamic-item-container:hover{
            .img{
            box-shadow: 0 0 0 1px #00aeec;
            }
            .img2{
              box-shadow: 0 0 0 1px #00aeec;
            }
            .name{
            color: #00AEEC;  
            } 
        }
        .active-dynamic-item-container{
            .img{
              box-shadow: 0 0 0 1px #00aeec;
            }
            .img2{
              box-shadow: 0 0 0 1px #00aeec;
            }
            .name{
            color: #00AEEC;  
            } 
        }
      }
    }
    .center2{
    background-color: white;
    border-radius: 6px;
    height: 48px;
    position: relative;
    width: 100%;
    user-select: none;
    margin-bottom: 8px;

      .title-list{
      display: flex;
      height: 100%;
      padding-left: 20px;

        div{
        align-items: center;
        color: #61666d;
        cursor: pointer;
        display: flex;
        font-weight: 400;
        height: 100%;
        font-size: 15px;
        line-height: 25px;
        justify-content: center;
        margin-right: 32px;
        position: relative;
        }
        .active-title{
        color: #00aeec;
        font-weight: 600;
        }
      }
      .line{
      background-color: #00aeec;
      border-radius: 1px;
      bottom: 8px;
      height: 2px;
      left: 0;
      position: absolute;
      transition: transform .3s ease;
      width: 14px;
      }
    }
    .center3{
    background-color: white;
    border-radius: 6px;
    height: 48px;
    position: relative;
    width: 100%;
    margin-bottom: 8px;
    display: flex;
    justify-content: center;
    user-select: none;
    align-items: center;

      span{
      transition: color .3s ease;  
      cursor: pointer;
      font-size: 13px;
      color: #00aeec;
      }
      span:hover{
      color: #008ac5;
      }
    }
    .bottom{

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
      background-color: white;
      display: flex;
      justify-content: center;
      border-radius: 6px;
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
      .fotter{
      margin-top: 30px;
      font-size: 14px;
      color: #9499A0;
      user-select: none;
      text-align: center;
      }
    }
  }
  .right{
  width: 318px;
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

.sortable-ghost {
  opacity: 0;
}
</style>