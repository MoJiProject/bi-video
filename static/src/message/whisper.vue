<template>
  <div class="whisper-container" v-loading.fullscreen="loading" element-loading-background="rgba(122, 122, 122, 0)">
    <div class="head2">我的消息</div>
    <div class="body">
        <div class="body-left">
        <div class="recent-message">近期消息</div>
        <div class="message-user-list" @scroll="dialogueListScroll">
            <div class="message-user-item" :style="{marginBottom: index===dialogueList.length-1&&dialogueLoadingFlag? '30px' : '0px'}" @click="currentDialogue = sDialogue" :class="{'message-user-itemBackground': sDialogue===currentDialogue}" v-for="(sDialogue,index) in dialogueList" :key="sDialogue.dialogue.id">
                <svg class="delete-message" @click="deleteDialogueF(sDialogue)" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M764.288 214.592 512 466.88 259.712 214.592a31.936 31.936 0 0 0-45.12 45.12L466.752 512 214.528 764.224a31.936 31.936 0 1 0 45.12 45.184L512 557.184l252.288 252.288a31.936 31.936 0 0 0 45.12-45.12L557.12 512.064l252.288-252.352a31.936 31.936 0 1 0-45.12-45.184z"></path></svg>
                <el-tooltip
                  popper-class="custom-tooltip"
                  effect="light"
                  :show-after="300"
                  :content="sDialogue.userInfo.userName"
                  placement="bottom-end"
                  :offset="12"
                  :show-arrow="false"
                  :hide-after="0"
                >
                <img class="message-user-avatar" :src="sDialogue.userInfo.avatarAddress">
                </el-tooltip>
                <div class="message-user-name">
                    <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    :content="sDialogue.userInfo.userName"
                    placement="bottom-end"
                    :offset="12"
                    :show-arrow="false"
                    :hide-after="0"
                    >
                    <span>{{ sDialogue.userInfo.userName }}</span>
                    </el-tooltip>
                </div>
                <img v-show="sDialogue.dialogue.dnd===1" class="message-dnd" src="/img/dnd.svg">
                <div v-show="sDialogue.dialogue.dnd===0&&sDialogue.notReadNumber>0" class="message-unread-number">
                      {{ sDialogue.notReadNumber<100 ? sDialogue.notReadNumber : '99+' }}
                </div>
                <div class="message-user-content">
                    <el-tooltip
                    popper-class="custom-tooltip"
                    effect="light"
                    :show-after="300"
                    placement="bottom-end"
                    :offset="0"
                    :show-arrow="false"
                    :hide-after="0"
                    >
<template #content>
                         <div v-html="linkifyHtml(sDialogue.dialogue.newContent)"></div>
                     </template>
                     <span v-html="linkifyHtml(sDialogue.dialogue.newContent)"></span>
                    </el-tooltip>
                </div>
            </div>
           <div v-show="dialogueLoading" class="loading-icon-container"> 
             <div class="loading-icon">
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
             </div>
           </div>
        </div> 
        </div>
        <div v-show="currentDialogue" class="body-right">
         <div class="message-username-title">{{ dialogueUserInfo?.userName }}
            <div class="message-setting-dialog-img-container" @mouseover="messageSettingHoverFlag = true" 
                @mouseleave="messageSettingHoverFlag = false">
                <img class="message-setting-dialog-img" 
                :src="messageSettingHoverFlag? '/img/弹幕列表蓝.png' : '/img/弹幕列表.png'">
            </div>
         </div>
         <div ref="messageContent" class="message-content-container" @scroll="messageListScroll">
            <div v-show="!messageDialogFlag&&!messageLoading&&!messageLoadingFlag" class="no-message">没有更多消息了～</div>
             <div v-show="messageLoading" class="loading-icon-container">
                <div class="loading-icon">
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
                <div></div>
             </div>
             </div>
             <div class="message-item" v-for="(message,index) in privateMessageList" :key="message.id">
             <div v-if="getDisplaySendTime(index)&&message.deleteSign!==store.userId" class="message-send-time">{{ getDisplySendTime2(message.sendTime) }}</div>
             <div v-if="message.status!==2&&message.deleteSign!==store.userId">
             <div class="message-content-container-item" :class="{'message-content-container-item2': message.senderId===store.userId}">
                <el-tooltip
                  popper-class="custom-tooltip"
                  effect="light"
                  :show-after="300"
                  :content="message.senderId===store.userId ? userInfo.userName : dialogueUserInfo.userName"
                  placement="bottom-end"
                  :offset="12"
                  :show-arrow="false"
                  :hide-after="0"
                >
                <img @click="openHome(1,message.senderId===store.userId ? userInfo.id : dialogueUserInfo.id)" class="message-user-avatar" :src="message.senderId===store.userId ? userInfo.avatarAddress : dialogueUserInfo.avatarAddress">
                </el-tooltip>
                <div class="message">
                    <div v-if="message.messageType===1" v-html="linkifyHtml(message.content)" @contextmenu="handleContextMenu($event,false),messageObj=message" class="message-content">
                    </div>
                    <el-tooltip
                  popper-class="custom-tooltip"
                  effect="light"
                  :show-after="300"
                  content="[图片]点击查看大图"
                  placement="bottom-end"
                  :offset="0"
                  :show-arrow="false"
                  :hide-after="0"
                >
                    <div>
                        <el-image
                        v-if="message.messageType===2" 
                        class="message-content-img"
                        :hide-on-click-modal="true"
                        :src="message.content"
                        @contextmenu="handleContextMenu($event,true),messageObj=message" 
                        :zoom-rate="1.2"
                        :max-scale="7"
                        :min-scale="0.2"
                        :preview-teleported="true"
                        :preview-src-list="[message.content]"
                        :initial-index="index"
                        fit="cover"
                        @close="restoreBodyOverflow"
                    />
                    </div>
                    </el-tooltip>
                    <div v-if="message.messageType===3&&message.videos" class="share-video-container" @click="jumpVideo(message.videos.id)" @contextmenu="handleContextMenu($event,false),messageObj=message">
                        <div class="share-video-top">
                            <img class="share-video-cover" :src="message.videos?.coverAddress">
                            <span class="share-video-time">{{ message.videos?.videoTime }}</span>
                            <span class="share-video-scrolling-number">{{ message.videos?.scrollingNumber }}条弹幕</span>
                            <img class="share-video-play-btn" src="/img/分享视频播放.svg">
                        </div>
                        <div class="share-video-title"><span>{{ message.videos?.title }}</span></div>
                        <div class="share-video-fotter">
                            <img src="/img/up.png">
                            <span class="share-video-user-name">{{ message.videos?.userName }}</span>
                            <span class="share-contribute-video">投稿视频</span>
                        </div>
                    </div>
                    <div v-if="message.messageType===3&&!message.videos" class="delete-video" @contextmenu="handleContextMenu($event,false),messageObj=message">
                        <div class="delete-info">
                            <svg class="dyn-none__icon" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 24 24" width="24" height="24"><path d="M9.83496 3.5C10.7972 1.83333 13.20285 1.83334 14.16505 3.5L21.7428 16.625C22.705 18.29165 21.5022 20.375 19.5777 20.375L4.42228 20.375C2.49778 20.375 1.29496 18.29165 2.25722 16.625L9.83496 3.5zM12 8.125C11.5858 8.125 11.25 8.46079 11.25 8.875L11.25 13.5C11.25 13.9142 11.5858 14.25 12 14.25C12.4142 14.25 12.75 13.9142 12.75 13.5L12.75 8.875C12.75 8.46079 12.4142 8.125 12 8.125zM12.75 16.125C12.75 15.7108 12.4142 15.375 12 15.375C11.5858 15.375 11.25 15.7108 11.25 16.125L11.25 16.5C11.25 16.9142 11.5858 17.25 12 17.25C12.4142 17.25 12.75 16.9142 12.75 16.5L12.75 16.125z" fill="currentColor"></path></svg>
                            <span>源视频已被作者删除</span>
                        </div>
                    </div> 
                </div>
             </div>
             <div v-if="message.senderId===store.userId&&index===privateMessageList.length-1&&message.status===0" class="read-message">未读</div>
             <div v-if="message.senderId===store.userId&&index===privateMessageList.length-1&&message.status===1" class="read-message2">已读</div>
             </div>
             <div v-if="message.status===2&&message.senderId===store.userId" class="notify-message">您撤回了一条消息</div>
             <div v-if="message.status===2&&message.senderId!==store.userId" class="notify-message">对方撤回了一条消息</div>
             </div>
             <div v-show="handleContextMenuFlag" class="context-menu"
             @contextmenu="handleContextMenu2" 
             :style="{
                top: handleContextMenuPosition.y+'px',
                left: handleContextMenuPosition.x+'px',
             }"
             >
                 <div v-if="!messageIsImgFlag&&messageObj?.messageType===1" @click="handleCopyMessage">复制</div>
                 <div v-if="messageObj?.senderId===store.userId" @click="revocationMessageF(messageObj)">撤回</div>
                 <div @click="deleteMessageDialogFlag=true">删除</div>
                 <div v-if="messageIsImgFlag" @click="handleSaveImg(messageObj)">保存图片</div>
             </div>
         </div>

         <div v-show="showScrollToBottom" class="scroll-to-bottom-btn" @click="scrollToBottomF" title="回到底部">
           <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg>
         </div>

         <div class="message-send-container">
            <div class="message-feature-container">
                <el-tooltip
                  popper-class="custom-tooltip"
                  effect="light"
                  :show-after="300"
                  content="图片"
                  placement="left"
                  :offset="12"
                  :show-arrow="false"
                  :hide-after="0"
                >
                <img @click="addSendImg" @mouseover="imgFeatureHoverFlag = true" @mouseleave="imgFeatureHoverFlag = false" class="message-feature-img" :src="imgFeatureHoverFlag ? '/img/媒体按钮蓝.png' : '/img/媒体按钮.png'">
                </el-tooltip>
                <el-tooltip
                  popper-class="custom-tooltip"
                  effect="light"
                  :show-after="300"
                  content="表情"
                  placement="right"
                  :offset="12"
                  :show-arrow="false"
                  :hide-after="0"
                >
                <img @mouseover="emojiHoverFlag = true" @mouseleave="emojiHoverFlag = false" class="message-feature-emoji" :src="emojiHoverFlag ? '/img/表情按钮蓝.png' : '/img/表情按钮.png'">
                </el-tooltip>
                <div v-show="emojiFlag" class="message-emoji-list">
                   <span class="emoji-title">表情</span>
                   <svg @click="emojiFlag=false" class="close-emoji" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M764.288 214.592 512 466.88 259.712 214.592a31.936 31.936 0 0 0-45.12 45.12L466.752 512 214.528 764.224a31.936 31.936 0 1 0 45.12 45.184L512 557.184l252.288 252.288a31.936 31.936 0 0 0 45.12-45.12L557.12 512.064l252.288-252.352a31.936 31.936 0 1 0-45.12-45.184z"></path></svg>
                   <span
                        class="comment-emoji-content"
                        @click="addEmoji(index)"
                        v-for="index in 176"
                        :key="index"
                        >
                        <img class="comment-emoji-img" :src="'/img/emoji/' + index + '.png'" />
                    </span>
                </div>
                <input type="file" class="message-file-input" style="display: none;" accept="image/*" @change="handleFileChange">
            </div>
            <div class="message-input-focus" @click="messageInput.focus()">
                <div class="message-input-container" ref="messageInput" @blur="getAfterBlurIndex" @input="updateFontNumber"  contenteditable="true"></div>
            </div>
            <div class="message-footer-container">
                <span>{{messageNumber}}/500</span>
                <button v-debounce :class="{sendMessageBtnClass: messageNumber===0}"  @click="sendMessageF">发送</button>
            </div>
         </div>
        </div>
        <div v-show="!currentDialogue" class="no-message-container">
            <img src="/img/no_message.png">
            <div>快找小伙伴聊天吧 ( ゜- ゜)つロ</div>
        </div>
    </div>
    <div v-show="messageDialogFlag" class="message-setting-dialog" :class="{
        'message-setting-dialog-class': messageDialogFlag,
        'message-setting-dialog-class2': !messageDialogFlag,
    }">
       <div v-show="currentDialogue?.dialogue.upDateTime===null" @click="changeUpStatusF(currentDialogue)">置顶聊天</div> 
       <div v-show="currentDialogue?.dialogue.upDateTime!==null" @click="changeUpStatusF(currentDialogue)">取消置顶</div> 
       <div v-show="currentDialogue?.dialogue.dnd===0" @click="changeDndStatusF(currentDialogue)">开启免打扰</div>
       <div v-show="currentDialogue?.dialogue.dnd===1" @click="changeDndStatusF(currentDialogue)">关闭免打扰</div>  
       <div>加入黑名单</div> 
       <div>举报该用户</div> 
       <div>不接收推送</div> 
    </div> 
    <el-dialog
      v-model="deleteMessageDialogFlag"
      append-to-body
      width="464"
      :lock-scroll="false"
      align-center
      style="height: 152.8px; border-radius: 12px"
    >
      <div class="delete-dialog-content">
        消息删除后不再显示在聊天记录里，确认删除吗？
      </div>
      <div class="delete-dialog-btn-container">
        <button v-debounce class="delete-dialog-confirm-btn" @click="deletePrivateMessageF(messageObj)">
        确定
      </button>
      <button v-debounce
        class="delete-dialog-cancel-btn"
        @click="deleteMessageDialogFlag=false"
      >
        取消
      </button>
      </div>
    </el-dialog>
  </div>

</template>

<script setup>
import { nextTick, onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { useGlobalStore } from "../store/store";
import { ElMessage } from "element-plus";
import {
  selectDialogue,
  changeUpStatus,
  changeDndStatus,
  deleteDialogue,
  sendMessage,
  revocationMessage,
  changeMessageStatus,
  deletePrivateMessage,
  selectPrivateMessage
} from "../api/privateMessage/index";
import { ChecklLogin } from "../api/user/index";

function throttle(fn, limit = 150) {
  let inThrottle = false;
  return function (...args) {
    if (!inThrottle) {
      fn.apply(this, args);
      inThrottle = true;
      setTimeout(() => (inThrottle = false), limit);
    }
  };
}


const store = useGlobalStore();
const messageSettingHoverFlag = ref(false);
const messageDialogFlag = ref(false);
const imgFeatureHoverFlag = ref(false);
const emojiHoverFlag = ref(false);
const emojiFlag = ref(false);
const dialogueList = reactive([]);
const dialoguePageNum = ref(1);
const messagePageNum = ref(1);
const currentDialogue = ref(null);
const handleContextMenuFlag = ref(false);
const handleContextMenuPosition = ref({ x: 0, y: 0 });
const privateMessageList = reactive([]);
const userInfo = ref(null);
const dialogueUserInfo = ref(null);
const messageInput = ref(null);
const messageContent = ref(null);
const showScrollToBottom = ref(false);
const messageNumber = ref(0);
const copyMessage = ref(null);
const messageIsImgFlag = ref(false);
const messageObj = ref(null);
const dialogueLoading = ref(false);
const dialogueLoadingFlag = ref(true);
const messageLoading = ref(false);
const messageLoadingFlag = ref(true);
const deleteMessageDialogFlag = ref(false);
const loading = ref(false);
let range = null;

// 把消息里的 http/https 网址识别成可点击链接（新标签页打开）
const URL_PATTERN = /https?:\/\/[^\s<>"'\u3000-\u303f\u4e00-\u9fa5\uff00-\uffef]+/gi;
const TRAILING_PATTERN = /[.,;:!?)\]}>，。；：！？、）》】」』"'’”]+$/;

function linkifyText(text) {
  return text.replace(URL_PATTERN, raw => {
    const trailing = (raw.match(TRAILING_PATTERN) || [""])[0];
    const url = trailing ? raw.slice(0, raw.length - trailing.length) : raw;
    if (!url) return raw;
    return `<a href="${url}" target="_blank" rel="noopener noreferrer">${url}</a>${trailing}`;
  });
}

function linkifyHtml(html) {
  if (!html) return html;
  return String(html)
    .split(/(<a\b[^>]*>[\s\S]*?<\/a>)/gi)
    .map(chunk => (chunk.toLowerCase().startsWith("<a") ? chunk : linkifyText(chunk)))
    .join("");
}

onMounted(() => {
  window.addEventListener("click", outSideClick);
  selectDialogueF();
});

onUnmounted(() => {
  window.removeEventListener("click", outSideClick);
  if (socket) {
    socket.onmessage = null;
    socket.close();
  }
});

function outSideClick(e) {
  handleContextMenuFlag.value = false;
  if (!e.target.className || typeof e.target.className.indexOf !== "function") return;
  if (!messageDialogFlag.value) {
    if (e.target.className.indexOf("message-setting-dialog-img-container") !== -1 || e.target.className.indexOf("message-setting-dialog-img") !== -1)
      messageDialogFlag.value = true;
  } else {
    messageDialogFlag.value = false;
  }
  if (!emojiFlag.value) {
    if (e.target.className.indexOf("message-feature-emoji") !== -1) emojiFlag.value = true;
  } else {
    if (e.target.className.indexOf("message-emoji-list") === -1 && e.target.className.indexOf("emoji-title") === -1 && e.target.className.indexOf("comment-emoji-content") === -1 && e.target.className.indexOf("comment-emoji-img") === -1)
      emojiFlag.value = false;
  }
  if (e.target.classList && e.target.classList.contains("message-emoji-img")) {
    const selection = window.getSelection();
    const r = document.createRange();
    const previousNode = e.target.previousSibling;
    if (previousNode) {
      r.setStartAfter(previousNode);
      r.setEndAfter(previousNode);
    } else {
      r.setStartAfter(e.target);
      r.setEndAfter(e.target);
    }
    selection.removeAllRanges();
    selection.addRange(r);
  }
}

function addSendImg() {
  const el = document.querySelector(".message-file-input");
  if (el) el.click();
}

async function handleFileChange(event) {
  let file = event.target.files[0];
  let base64 = null;
  if (!file) return;
  if (!file.type.startsWith("image/")) {
    ElMessage({ message: "只能上传图片", type: "info", plain: true, duration: 1700 });
    event.target.value = "";
    return;
  } else if (file.size >= 10 * 1024 * 1024) {
    ElMessage({ message: "图片大小不能超过10M", type: "info", plain: true, duration: 1700 });
    event.target.value = "";
    return;
  }
  loading.value = true;
  try {
    base64 = await readFileAsBase64(file);
    if (base64 !== null) {
      let message = {
        senderId: store.userId,
        receiverId: currentDialogue.value.dialogue.dialogueId,
        content: base64,
        messageType: 2
      };
      sendMessage(store.token, message).then(res => {
        if (res.data.code === 1) {
          const index = dialogueList.findIndex(iten => iten.dialogue.id === currentDialogue.value.dialogue.id);
          if (index !== -1) dialogueList[index].dialogue.newContent = "[图片]";
          socket.send("send:" + message.senderId + ":" + message.receiverId);
        } else {
          ElMessage({ message: res.data.msg, type: "info", plain: true, duration: 1700 });
        }
        loading.value = false;
      }).catch(() => {
        loading.value = false;
      });
    }
  } catch (err) {
    loading.value = false;
  }
  event.target.value = "";
}

function readFileAsBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = e => resolve(e.target.result);
    reader.onerror = error => reject(error);
    reader.readAsDataURL(file);
  });
}

function selectDialogueF() {
  if (store.token === null || store.userId === null) return;
  selectDialogue(store.token, store.userId, dialoguePageNum.value).then(res => {
    if (res.data.code === 1) {
      dialogueList.push(...res.data.data);
      dialoguePageNum.value++;
      const urlParams = new URLSearchParams(window.location.search);
      let dialogueId = Number(urlParams.get("dialogueId"));
      if (dialogueId !== null && dialogueId !== 0) {
        const index = dialogueList.findIndex(item => item.dialogue.dialogueId === dialogueId);
        if (index !== -1) {
          currentDialogue.value = dialogueList[index];
        }
      } else {
        if (dialogueList.length > 0) currentDialogue.value = dialogueList[0];
      }
    }
  });
}

function selectDialogueF2() {
  if (store.token === null || store.userId === null) return;
  setTimeout(() => {
    selectDialogue(store.token, store.userId, dialoguePageNum.value).then(res => {
      if (res.data.code === 1) {
        dialogueList.push(...res.data.data);
        if (res.data.data === null || res.data.data.length === 0) dialogueLoadingFlag.value = false;
        dialogueLoading.value = false;
        dialoguePageNum.value++;
        const urlParams = new URLSearchParams(window.location.search);
        let dialogueId = Number(urlParams.get("dialogueId"));
        if (dialogueId !== null && dialogueId !== 0) {
          const index = dialogueList.findIndex(item => item.dialogue.dialogueId === dialogueId);
          if (index !== -1) currentDialogue.value = dialogueList[index];
        } else {
          if (dialogueList.length > 0) currentDialogue.value = dialogueList[0];
        }
      }
    });
  }, 1000);
}

function changeUpStatusF(sDialogue) {
  if (sDialogue.dialogue.id === null) return;
  changeUpStatus(store, sDialogue.dialogue.id).then(res => {
    if (res.data.code === 1) {
      if (sDialogue.dialogue.upDateTime !== null) {
        const index = dialogueList.findIndex(item => item.dialogue.id === sDialogue.dialogue.id);
        if (index !== -1) {
          dialogueList.splice(index, 1);
          sDialogue.dialogue.upDateTime = null;
          const index2 = dialogueList.findIndex(item => item.dialogue.upDateTime === null);
          if (index2 !== -1) dialogueList.splice(index2, 0, sDialogue);
          else dialogueList.push(sDialogue);
        }
      } else {
        sDialogue.dialogue.upDateTime = Date.now();
        const index = dialogueList.findIndex(item => item.dialogue.id === sDialogue.dialogue.id);
        if (index !== -1) {
          dialogueList.splice(index, 1);
          dialogueList.unshift(sDialogue);
        }
      }
    }
  });
}

function changeDndStatusF(sDialogue) {
  if (sDialogue.dialogue.id === null) return;
  changeDndStatus(store, sDialogue.dialogue.id).then(res => {
    if (res.data.code === 1) {
      const index = dialogueList.findIndex(item => item.dialogue.id === sDialogue.dialogue.id);
      if (index !== -1) dialogueList[index].dialogue.dnd = dialogueList[index].dialogue.dnd === 0 ? 1 : 0;
    }
  });
}

function deleteDialogueF(sDialogue) {
  if (sDialogue.dialogue.id === null) return;
  deleteDialogue(store.token, sDialogue.dialogue.id).then(res => {
    if (res.data.code === 1) {
      const index = dialogueList.findIndex(item => item.dialogue.id === sDialogue.dialogue.id);
      //findIndex 未命中时返回 -1，直接 splice 会误删最后一条会话
      if (index === -1) return;
      dialogueList.splice(index, 1);
      //会话里的未读数要一并扣掉，否则顶部消息数会一直残留
      if (sDialogue.notReadNumber > 0) updateMessageNumber(-sDialogue.notReadNumber);
      if (currentDialogue.value?.dialogue.id === sDialogue.dialogue.id) {
        privateMessageList.length = 0;
        currentDialogue.value = null;
      }
    }
  });
}

function handleContextMenu(e, flag) {
  messageIsImgFlag.value = flag;
  e.preventDefault();
  handleContextMenuPosition.value = { x: e.clientX, y: e.clientY };
  handleContextMenuFlag.value = true;
  copyMessage.value = e.target.innerText;
}

function handleContextMenu2(e) {
  e.preventDefault();
}

async function selectPrivateMessageF(sDialogue, a) {
  if (!sDialogue?.dialogue?.id) return;
  messageLoadingFlag.value = false;
  const thisPage = messagePageNum.value;
  messagePageNum.value = Math.max(0, messagePageNum.value - 1);
  selectPrivateMessage(store, sDialogue.dialogue.dialogueId, thisPage).then(res => {
    if (res.data.code === 1) {
      const incoming = res.data.data.privateMessage || [];
      if (a === 1) {
        if (incoming.length) {
          privateMessageList.push(...incoming);
        }
      } else {
        if (privateMessageList.length < 20 && incoming.length === 20) {
          privateMessageList.splice(0, 0, ...incoming);
        }
      }
      userInfo.value = res.data.data.userInfo;
      dialogueUserInfo.value = res.data.data.dialogueUserInfo;
      nextTick(() => {
        requestAnimationFrame(() => {
          if (messageContent?.value) {
            messageContent.value.scrollTop = messageContent.value.scrollHeight;
            showScrollToBottom.value = false;
          }
          messageLoadingFlag.value = messagePageNum.value > 0;
          messageLoading.value = false;
        });
      });
    }
  });
}


const selectPrivateMessageF2 =async (sDialogue) => {
  if (!sDialogue || !sDialogue.dialogue?.id) {
    messageLoadingFlag.value = false;
    messageLoading.value = false;
    return;
  }
  if (messagePageNum.value <= 0) {
    messageLoadingFlag.value = false;
    messageLoading.value = false;
    return;
  }
  const thisPage = messagePageNum.value;
  messagePageNum.value = Math.max(0, messagePageNum.value - 1);
  selectPrivateMessage(store, sDialogue.dialogue.dialogueId, thisPage).then(res => {
    if (res.data.code === 1) {
      const incoming = res.data.data.privateMessage || [];
      if (incoming.length) {
        privateMessageList.unshift(...incoming);
      }
      messageLoadingFlag.value = messagePageNum.value > 0;
      messageLoading.value = false;
      dealyFlag = true;
    }
  }).catch((error) => {
    console.log(error);
  });
};

watch(currentDialogue, async (newVal) => {
  if (newVal !== null) {
    userInfo.value = null;
    messageNumber.value = 0;
    dialogueUserInfo.value = null;
    privateMessageList.length = 0;
    messageLoadingFlag.value = true;
    messagePageNum.value = Math.ceil(newVal?.allMessageNumber / 20);
    await selectPrivateMessageF(newVal, 1);
    await selectPrivateMessageF(newVal, 2);
    dealyFlag = true;
    tryChangeMessageStatus(newVal);
  }
  if (messageInput?.value) messageInput.value.innerHTML = "";
});
  
//打开会话即已读：本地未读数立即扣减，不依赖聊天 socket 是否连上
function tryChangeMessageStatus(newVal) {
  if (!newVal?.dialogue?.id) return;
  const unread = newVal.notReadNumber || 0;
  if (unread > 0) {
    updateMessageNumber(-unread);
    newVal.notReadNumber = 0;
    const index = dialogueList.findIndex(item => item.dialogue.id === newVal.dialogue.id);
    if (index !== -1) dialogueList[index].notReadNumber = 0;
  }
  changeMessageStatusF(newVal);
}

function revocationMessageF(privateMessage) {
  if (privateMessage.id === null) return;
  const startTime = new Date(privateMessage?.sendTime);
  const endTime = new Date();
  const seconds = Math.abs(startTime.getTime() - endTime.getTime()) / 1000;
  if (seconds > 180) {
    ElMessage({ message: "消息已超过3分钟，无法撤回", type: "info", plain: true, duration: 1700 });
    return;
  }
  revocationMessage(store, privateMessage.id).then(res => {
    if (res.data.code === 1) {
      const index = privateMessageList.findIndex(item => item.id === privateMessage.id);
      if (index !== -1) privateMessageList[index].status = 2;
      ElMessage({ message: "撤回成功", type: "info", plain: true, duration: 1700 });
      socket.send("put:" + privateMessage.id);
    } else {
      ElMessage({ message: res.data.msg, type: "info", plain: true, duration: 1700 });
    }
  });
}

function deletePrivateMessageF(privateMessage) {
  if (privateMessage.id === null) return;
  deletePrivateMessage(store, privateMessage.id).then(res => {
    if (res.data.code === 1) {
      const index = privateMessageList.findIndex(item => item.id === privateMessage.id);
      if (index !== -1) privateMessageList.splice(index, 1);
      //同步左侧会话预览，跟后端保持一致：取当前可见的最后一条
      refreshDialoguePreview();
      //通知对方同步删除状态
      if (socket && socket.ws.readyState === 1) socket.send("put:" + privateMessage.id);
      ElMessage({ message: "删除成功", type: "info", plain: true, duration: 1700 });
      deleteMessageDialogFlag.value = false;
    } else {
      ElMessage({ message: res.data.msg, type: "info", plain: true, duration: 1700 });
    }
  });
}

//按后端规则重算当前会话的预览文案与消息总数
function refreshDialoguePreview() {
  if (!currentDialogue.value) return;
  const index = dialogueList.findIndex(item => item.dialogue.id === currentDialogue.value.dialogue.id);
  if (index === -1) return;
  const visible = privateMessageList.filter(item => item.deleteSign !== store.userId);
  const last = visible[visible.length - 1];
  //allMessageNumber 由后端按可见消息统计，这里只用已加载的部分更新预览文案
  if (!last) {
    dialogueList[index].dialogue.newContent = null;
    dialogueList[index].notReadNumber = 0;
    return;
  }
  if (last.status === 2) {
    dialogueList[index].dialogue.newContent = last.senderId === store.userId ? "您撤回一条消息" : "对方撤回一条消息";
  } else if (last.messageType === 2) {
    dialogueList[index].dialogue.newContent = "[图片]";
  } else if (last.messageType === 3) {
    dialogueList[index].dialogue.newContent = "[视频]";
  } else {
    dialogueList[index].dialogue.newContent = last.content;
  }
}

function changeMessageStatusF(sDialogue) {
  if (sDialogue?.dialogue.dialogueId === null) return;
  changeMessageStatus(store, sDialogue.dialogue.dialogueId).then(res => {
    if (res.data.code === 1) {
      const index = dialogueList.findIndex(item => item.dialogue.id === sDialogue.dialogue.id);
      if (index !== -1) dialogueList[index].notReadNumber = 0;
      if (privateMessageList.length && privateMessageList[privateMessageList.length - 1].senderId !== store.userId)
        socket.send("put:" + privateMessageList[privateMessageList.length - 1].id);
      //顶部/侧边的未读数会被头部组件的登录校验整体覆盖，这里用服务端最新值校正一次
      syncUserMessageNumber();
    }
  });
}

//同步服务端最新的未读私信数
function syncUserMessageNumber() {
  if (!store.userIp) return;
  ChecklLogin(store.userIp).then(response => {
    if (response.data.code !== 1) return;
    const serverInfo = response.data.data;
    if (!serverInfo) return;
    const userInformation = { ...store.userInformation };
    userInformation.messageNumber = Math.max(0, serverInfo.messageNumber || 0);
    userInformation.allMessageNumber = userInformation.messageNumber
      + (userInformation.replyCommentNumber || 0)
      + (userInformation.atNumber || 0)
      + (userInformation.likeAllNumber || 0);
    store.setUserInformation(userInformation);
  }).catch(() => {});
}

function sendMessageF() {
  if (messageNumber.value === 0) return;
  if (messageNumber.value > 500) {
    ElMessage({ message: "消息字数不能超过500", type: "info", plain: true, duration: 1700 });
    return;
  }
  let message = {
    senderId: store.userId,
    receiverId: currentDialogue.value.dialogue.dialogueId,
    content: linkifyHtml(messageInput.value.innerHTML),
    messageType: 1
  };
  sendMessage(store.token, message).then(res => {
    if (res.data.code === 1) {
      messageInput.value.innerHTML = "";
      messageNumber.value = 0;
      const index = dialogueList.findIndex(iten => iten.dialogue.id === currentDialogue.value.dialogue.id);
      if (index !== -1) dialogueList[index].dialogue.newContent = message.content;
      socket.send("send:" + message.senderId + ":" + message.receiverId);
    } else {
      ElMessage({ message: res.data.msg, type: "info", plain: true, duration: 1700 });
    }
  });
}

function getAfterBlurIndex() {
  const selection = window.getSelection();
  if (selection.rangeCount > 0) {
    range = selection.getRangeAt(0);
  }
}

function addEmoji(index) {
  const button = document.createElement("img");
  button.src = `/img/emoji/${index}.png`;
  button.className = "message-emoji-img";
  button.style.width = "20px";
  button.style.height = "20px";
  button.style.marginRight = "2px";
  button.style.marginLeft = "2px";
  button.style.transform = "translateY(4px)";
  const selection = window.getSelection();
  if (!range) {
    if (messageInput?.value) {
      messageInput.value.appendChild(button);
      messageNumber.value = messageInput.value.innerText.trim().length + (messageInput.value.getElementsByTagName("img")?.length || 0) * 5;
    }
    return;
  }
  range.insertNode(button);
  range.setStartAfter(button);
  range.setEndAfter(button);
  selection.removeAllRanges();
  selection.addRange(range);
  messageNumber.value = messageInput.value.innerText.trim().length + (messageInput.value.getElementsByTagName("img")?.length || 0) * 5;
}

//连接websocket实时更新滚动弹幕（支持断线重连）
class ReconnectingWebSocket {
      constructor(url, protocols) {
        this.url = url;
        this.protocols = protocols;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.messageQueue = [];
        this.shouldReconnect = true;
        this.ws = null;
        this.onopen = null;
        this.onmessage = null;
        this.onclose = null;
        this.onerror = null;
        this.connect();
      }

      connect() {
        try {
          const ws = new WebSocket(this.url, this.protocols);
          this.ws = ws;

          ws.onopen = async (e) => {
            this.reconnectAttempts = 0;
            if (typeof this.onopen === 'function') this.onopen(e);
            while (this.messageQueue.length > 0 && ws.readyState === WebSocket.OPEN) {
              try {
                ws.send(this.messageQueue.shift());
              } catch (err) {
                break;
              }
            }
          };

          ws.onmessage = (e) => {
            if (typeof this.onmessage === 'function') this.onmessage(e);
          };

          ws.onclose = (e) => {
            if (typeof this.onclose === 'function') this.onclose(e);
            if (!this.shouldReconnect) return;
            this.reconnectAttempts++;
            if (this.maxReconnectAttempts && this.reconnectAttempts > this.maxReconnectAttempts) return;
            const delay = Math.min(30000, 1000 * Math.pow(2, this.reconnectAttempts - 1));
            setTimeout(() => this.connect(), delay);
          };

          ws.onerror = (e) => {
            if (typeof this.onerror === 'function') this.onerror(e);
          };
        } catch (err) {
          this.reconnectAttempts++;
          const delay = Math.min(30000, 1000 * Math.pow(2, this.reconnectAttempts - 1));
          setTimeout(() => this.connect(), delay);
        }
      }

      send(data) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
          this.ws.send(data);
        } else {
          this.messageQueue.push(data);
        }
      }

      close(code, reason) {
        this.shouldReconnect = false;
        if (this.ws) this.ws.close(code, reason);
      }

      addEventListener(...args) {
        return this.ws && this.ws.addEventListener(...args);
      }

      removeEventListener(...args) {
        return this.ws && this.ws.removeEventListener(...args);
      }
    }

const socket = new ReconnectingWebSocket(`${location.protocol === 'https:' ? 'wss:' : 'ws:'}//${location.host}/api/chat`);
socket.onopen = async () => {
  socket.send("open:" + store.userId);
};
socket.onmessage = (event) => {
  const data = JSON.parse(event.data);
  if (data.selectSign === currentDialogue.value?.dialogue.sign) {
    const index = privateMessageList.findIndex(item => item.id === data.id);
    if (index === -1) {
      privateMessageList.push(data);
      updateMessageNumber(-currentDialogue.value.notReadNumber);
      changeMessageStatusF(currentDialogue.value);
      const index1 = dialogueList.findIndex(item => item.dialogue.dialogueId === data.senderId && item.dialogue.userId === data.receiverId);
      if (index1 !== -1) {
        dialogueList[index1].notReadNumber++;
        if (data.messageType === 1) dialogueList[index1].dialogue.newContent = data.content;
        else dialogueList[index1].dialogue.newContent = "[图片]";
      }
      if (data.senderId === store.userId) {
        if (data.messageType === 1) {
          nextTick(() => requestAnimationFrame(() => { if (messageContent?.value) { messageContent.value.scrollTop = messageContent.value.scrollHeight; showScrollToBottom.value = false; } }));
        } else if (data.messageType === 2) {
          setTimeout(() => nextTick(() => requestAnimationFrame(() => { if (messageContent?.value) { messageContent.value.scrollTop = messageContent.value.scrollHeight; showScrollToBottom.value = false; } })), 700);
        }
      } else {
        if(currentDialogue.value.dialogue.dialogueId !== data.senderId){
          updateMessageNumber(1);
        }
        if (messageContent?.value && messageContent.value.scrollHeight - messageContent.value.scrollTop <= messageContent.value.clientHeight + 1) {
          if (data.messageType === 1) {
            nextTick(() => requestAnimationFrame(() => { if (messageContent?.value) { messageContent.value.scrollTop = messageContent.value.scrollHeight; showScrollToBottom.value = false; } }));
          } else if (data.messageType === 2) {
            setTimeout(() => nextTick(() => requestAnimationFrame(() => { if (messageContent?.value) { messageContent.value.scrollTop = messageContent.value.scrollHeight; showScrollToBottom.value = false; } })), 700);
          }
        }
      }
    } else {
      if (store.userId !== data.senderId) {
        const wasUnread = privateMessageList[index]?.status === 0;
        privateMessageList.splice(index, 1, data);
        //对方撤回未读消息时，本地未读数要一起扣掉
        if (data.status === 2 && wasUnread) {
          updateMessageNumber(-1);
          const unreadIndex = dialogueList.findIndex(item => item.dialogue.sign === data.selectSign);
          if (unreadIndex !== -1) {
            dialogueList[unreadIndex].notReadNumber = Math.max(0, dialogueList[unreadIndex].notReadNumber - 1);
          }
        }
        if (privateMessageList[privateMessageList.length - 1]?.id === data.id && data.content.length === 0) {
          const index1 = dialogueList.findIndex(item => item.dialogue.dialogueId === data.senderId && item.dialogue.userId === data.receiverId);
          if (index1 !== -1) dialogueList[index1].dialogue.newContent = "对方撤回一条消息";
        }
      } else {
        privateMessageList.splice(index, 1, data);
        if (privateMessageList[privateMessageList.length - 1]?.id === data.id && data.content.length === 0) {
          const index1 = dialogueList.findIndex(item => item.dialogue.dialogueId === data.receiverId && item.dialogue.userId === data.senderId);
          if (index1 !== -1) dialogueList[index1].dialogue.newContent = "您撤回一条消息";
        }
      }
    }
  } else {
    const index = dialogueList.findIndex(item => item.dialogue.sign === data.selectSign);
    if (index !== -1) {
      if (data.status !== 2) {
        if (data.messageType === 1) dialogueList[index].dialogue.newContent = data.content;
        else if (data.messageType === 2) dialogueList[index].dialogue.newContent = "[图片]";
        dialogueList[index].notReadNumber++;
        updateMessageNumber(1);
      } else if (data.status === 2) {
        if (store.userId !== data.senderId) {
          dialogueList[index].dialogue.newContent = "对方撤回一条消息";
          updateMessageNumber(-1);
        } else {
          dialogueList[index].dialogue.newContent = "您撤回一条消息";
        }
      }
    } else {
      dialoguePageNum.value = 1;
      dialogueList.length = 0;
      selectDialogueF();
    }
  }
};

function updateFontNumber() {
  messageNumber.value = (messageInput?.value?.innerText?.trim()?.length || 0) + ((messageInput?.value?.getElementsByTagName("img")?.length) || 0) * 5;
}

function getDisplaySendTime(index) {
  if (index === 0) return true;

  const currTime = new Date(privateMessageList[index]?.sendTime);

  for (let i = index - 1; i >= 0; i--) {
    const prevTime = new Date(privateMessageList[i]?.sendTime);
    const seconds = Math.abs(currTime - prevTime) / 1000;

    if (seconds > 300) {
      return true;
    } else {
      return false;
    }
  }

  return false;
}


function getDisplySendTime2(time) {
  const date = new Date(time);
  const today = new Date();
  const dateTime = time.split(" ");
  const dateTime2 = dateTime[1].split(":");
  const oneDay = 24 * 60 * 60 * 1000;
  const todayStart = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime();
  const dateStart = new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime();
  if (dateStart === todayStart) {
    return "今天 " + dateTime2[0] + ":" + dateTime2[1];
  } else if (dateStart === todayStart - oneDay) {
    return "昨天 " + dateTime2[0] + ":" + dateTime2[1];
  } else {
    return dateTime[0] + " " + dateTime2[0] + ":" + dateTime2[1];
  }
}

function handleCopyMessage() {
  if (copyMessage.value && copyMessage.value.length > 0) {
    ElMessage({ message: "复制成功", type: "info", plain: true, duration: 1700 });
    navigator.clipboard.writeText(copyMessage.value);
    copyMessage.value = null;
  } else {
    ElMessage({ message: "不能复制表情", type: "info", plain: true, duration: 1700 });
  }
}

const _dialogueListScrollHandler = throttle((e) => {
  if (!e.target) return;
  if (!dialogueLoadingFlag.value) return;
  dialogueLoading.value = e.target.scrollHeight - e.target.scrollTop <= e.target.clientHeight + 1;
  if (dialogueLoading.value) selectDialogueF2();
}, 150);

function dialogueListScroll(e) {
  _dialogueListScrollHandler(e);
}

let dealyFlag = true;
async function messageListScroll(e){
    if(!e.target) return;
    try{
      showScrollToBottom.value = e.target.scrollHeight - e.target.scrollTop > e.target.clientHeight + 50;
    }catch(err){
      showScrollToBottom.value = false;
    }
    if(!messageLoadingFlag.value) return;
    messageLoading.value = e.target.scrollTop === 0;
    if(messageLoading.value && dealyFlag){
        dealyFlag = false;
        const oldScrollHeight = e.target.clientHeight;
        await selectPrivateMessageF2(currentDialogue.value);
        nextTick(()=>{
          e.target.scrollTop = oldScrollHeight - 280;
        })
    }
}

function scrollToBottomF(){
  if(messageContent?.value){
    try{
      messageContent.value.scrollTo({ top: messageContent.value.scrollHeight, behavior: 'smooth' });
    }catch(e){
      messageContent.value.scrollTop = messageContent.value.scrollHeight;
    }
    showScrollToBottom.value = false;
  }
}

function handleSaveImg(msg) {
  const link = document.createElement("a");
  link.href = msg.content;
  link.download = msg.content.split('/').pop();
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

function updateMessageNumber(sum) {
  let userInformation = store.userInformation;
  userInformation.messageNumber = Math.max(0, (userInformation.messageNumber || 0) + sum);
  userInformation.allMessageNumber = userInformation.messageNumber + userInformation.replyCommentNumber + userInformation.atNumber + userInformation.likeAllNumber;
  store.setUserInformation(userInformation);
}

function jumpVideo(videoId) {
  if (videoId !== null) window.open(`./video?videoId=BV${videoId}`, 'videoWindow');
}

function openHome(menu, id) {
  window.open(`./home?homeMenu=${menu}&userId=${id}`, "_blank");
}

function restoreBodyOverflow() {
  document.body.style.overflowY = 'hidden';
}
</script>

<style lang="scss" scoped>
*{
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

.whisper-container{
  position: absolute;
  --whisper-left: clamp(20.7vw, 2.4vw + 15.2%, 44vw);
  left: var(--whisper-left);
  width: calc(100vw - var(--whisper-left) - 24px);
  max-width: 983px;
  min-width: 300px;
  top: 74px;
    
    .head2{
    height: 42px;
    padding: 0 16px;
    font-size: 15px;
    background-color: rgba(255, 255, 255, 1);
    color: #666;
    line-height: 42px;
    border-radius: 4px;
    box-shadow: 0 2px 4px 0 rgba(121, 146, 185, 0.54);
    user-select: none;
    }

    .body{
    position: relative;
    top: 10px;
    height: calc(100vh - 138px);
    font-size: 12px;
    color: #666;
    background-color: var(--background-color);
    box-shadow: 0 2px 4px 0 rgba(121, 146, 185, 0.54);
    border-radius: 4px;
    display: flex;
    overflow-y: hidden;

    .body-left{
    position: relative;
    flex: 0 0 clamp(190px, 24%, 240px);
    min-width: 180px;
    border-right: 1px solid #e9eaec;
    overflow: hidden;

    .recent-message{
    padding-left: 24px;
    line-height: 35px;
    height: 36px;
    background-color: var(--background-color);
    border-bottom: 1px solid #e9eaec;
    user-select: none;
    overflow: hidden;
    }

    .message-user-list{
      width: 100%;
      height: calc(100% - 36px);
        overflow-y: auto;
        overflow-x: hidden;


        .message-user-item{
        position: relative;
        width: 100%;
        height: 78px;
        flex-direction: row;
        padding: 19px clamp(14px, 2vw, 24px);
        background-color: var(--background-color);
        cursor: pointer;
        transition: background-color 0.3s ease;

        .delete-message{
            position: absolute;
            width: 20px;
            height: 20px;
            top: 50%;
            left: 0px;
            color: rgb(153, 153, 153);
            transform: translate(-50px,-50%);
        }

        .message-user-avatar{
            width: 40px;
            height: 40px;
            border-radius: 50%;
            user-select: none;
        }

        .message-user-name{
          position: relative;
          width: calc(100% - 58px);
          height: 20px;
          top: -43.5px;
          left: 48px;
          font-size: 14px;
          color: #333;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .message-dnd{
            position: absolute;
            width: 16px;
            height: 16px;
            right: 10px;
            top: 20px;
        }

        .message-unread-number{
            position: absolute;
            color: #fff;
            line-height: 16px;
            font-size: 12px;
            border-radius: 8px;
            height: 16px;
            min-width: 16px;
            top: 20px;
            right: 8px;
            padding: 0 4px;
            text-align: center;
            background-color: #fb7299;
        }

        .message-user-content{
          position: relative;
          width: calc(100% - 58px);
          height: 28px;
          top: -40px;
          left: 48px;
          font-size: 12px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;

          :deep(a){
          color: #fb7299;
          text-decoration: none;
          }
        }
        }
        
        .message-user-itemBackground{
            background-color: #e4e5e6;
        }
        
        .message-user-item:hover{
            background-color: #e4e5e6;
        .delete-message{
            transform-origin: 32px 32px;
            animation: deleteMsg 0.3s ease forwards;
        }
        }

        .loading-icon-container{
            position: absolute;
            display: flex;
            justify-content: center;
            align-items: center;
            left: 0px;
            bottom: 0px;
            width: 90%;
            height: 10px;
            
        }
    }

    .message-user-list::-webkit-scrollbar{
    width: 6px; /* 滚动条的宽度 */
    border-radius: 30px; /* 滚动条滑块的圆角 */
    }

    .message-user-list::-webkit-scrollbar-thumb {
    background: #aaa; /* 滚动条的滑块 */
    height: 10px;
    border-radius: 10px;
    }
    
    }

    .body-right{
    position: relative;
    flex: 1 1 auto;
    min-width: 0;
    display: flex;
    flex-direction: column;

    .loading-icon-container{
        position: relative;
        left: 0px;
        top: 0px;
        height: 40px;
        display: flex;
        justify-content: center;
    }
    .message-username-title{
    height: 36px;
    flex-shrink: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    border-bottom: 1px solid #e9eaec;
    background-color: var(--background-color);
    position: relative;
    font-size: 14px;
    text-align: center;
    color: #333;

    .message-setting-dialog-img-container{
        position: absolute;
        cursor: pointer;
        top: 5px;
        right: 8px;
        padding: 5px; 
        img{
        user-select: none;
        width: 3.5px;
        height: 14px;
        }
    }

    
    }

    .message-content-container{
        position: relative;
        width: 100%;
      flex: 1 1 auto;
      min-height: 0;
      height: auto;
        overflow-y: auto;
        overflow-x: hidden;

        .read-message{
            position: relative;
          display: block;
          left: auto;
            top: -12px;
          padding-right: 60px;
          text-align: right;
            font-size: 12px;
            color: #999;
        }

        .read-message2{
            position: relative;
          display: block;
          left: auto;
            top: -12px;
          padding-right: 60px;
          text-align: right;
            font-size: 12px;
            color: #424242;
        }
        
        .no-message{
            position: relative;
            text-align: center;
            margin-top: 15px;
            color: #999;
        }

        .message-send-time{
            position: relative;
            text-align: center;
            margin-top: 27px;
            color: #999;
            margin-bottom: 15px;
        }

        .notify-message{
            position: relative;
            display: inline-block;
            left: 50%;
            transform: translatex(-50%);
            padding: 2px 20px;
            background-color: #eaeaea;
            color: #b2b2b2;
            border-radius: 10px;
            line-height: 16px;
            height: 20px;
            margin-bottom: 10px;
        }

        .message-content-container-item{
            position: relative;
            min-height: 48px;
            padding: 0 16px 16px;
            overflow: hidden;

            .message-user-avatar{
                float: left;
                width: 30px;
                height: 30px;
                border-radius: 50%;
                cursor: pointer;
                user-select: none;
            }

            .message{
              max-width: min(480px, calc(100% - 70px));
                margin: 0 10px;
                position: relative;
                display: inline-block;
                overflow: hidden;
                color: #333;

                .message-content{
                    line-height: 1.5;
                    font-size: 14px;
                    min-height: 37px;
                    padding: 8px 16px;
                    word-wrap: break-all;
                    word-break: break-all;
                    border-radius: 0px 16px 16px 16px;
                    overflow: hidden;
                    background: #fff;

                    :deep(a){
                    color: #fb7299;
                    text-decoration: none;

                    &:hover{
                      text-decoration: underline;
                    }
                    }
                }

                .message-content-img{
                    max-width: 266px;
                    height: 100%;
                    border-radius: 0px 16px 16px 16px;
                    vertical-align: middle;
                }

                .share-video-container{
                  width: min(360px, 100%);
                    height: auto;
                    min-height: 273px;
                    max-height: 293px;
                    border-radius: 0px 16px 16px 16px;
                    cursor: pointer;
                    overflow: hidden;

                    .share-video-top{
                    position: relative;
                    .share-video-cover{
                        border-radius: 0px 0px 0px 0px;
                      width: 100%;
                        height: 200px;
                        object-fit: cover;
                    }
                    .share-video-time{
                        position: absolute;
                        bottom: 15px;
                        left: 5px;
                        font-size: 10px;
                        color: #fff;
                        background-color: rgba(0, 0, 0, 0.4);
                        border-radius: 2px;
                        height: 15px;
                        line-height: 15px;
                        padding: 0 4px;
                    }
                    .share-video-scrolling-number{
                        position: absolute;
                        bottom: 15px;
                        left: 48px;
                        font-size: 10px;
                        color: #fff;
                        height: 15px;
                        line-height: 15px;
                    }
                    .share-video-play-btn{
                      position: absolute;
                      bottom: 12px;
                      right: 10px;
                      width: 48px;
                      height: 48px;
                    }
                    }
                    
                    .share-video-title{
                        position: relative;
                        top: -3px;
                        color: #212121;
                        padding: 10px;
                        background-color: white;
                        span{
                        display: -webkit-box;
                        text-overflow: ellipsis;
                        word-break: break-word;
                        -webkit-line-clamp: 2;
                        -webkit-box-orient: vertical;
                        font-size: 14px;
                        line-height: 20px;
                        max-height: 2.857142857142857em;
                        overflow: hidden;
                        }
                    }
                    .share-video-fotter{
                        display: flex;
                        position: relative;
                        top: -3px;
                        padding: 8px 10px;
                        border-top: 1px solid #e5e9ef;
                        background-color: white;
                        color: #999;
                        font-size: 12px;
                        align-items: center;
                        img{
                            width: 14px;
                            height: 12px;
                        }
                        .share-video-user-name{
                            font-size: 12px;
                            color: #999;
                            line-height: 16px;
                            padding-left: 4px;
                            width: 200px;
                            display: -webkit-box;
                            -webkit-line-clamp: 1;
                            -webkit-box-orient: vertical;
                            text-overflow: ellipsis;
                            overflow: hidden;
                        }
                        .share-contribute-video{
                            position: absolute;
                            right: 10px;
                        }
                    }
                }

                .delete-video{
                  width: min(360px, 100%);
                    height: 40px;
                    border-radius: 6px;
                    font-size: 18px;
                    background-color: #F6F7F8;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    cursor: pointer;
                    user-select: none;

                    .delete-info{
                    align-items: center;
                    color: #9499A0;
                    display: flex;
                    font-size: 13px;
                    line-height: 18px;
                    text-indent: 6px;
                      
                      svg{
                      height: 16px;
                      vertical-align: -2px;
                      width: 16px;
                      }
                    }
                }
            }

        }

        .message-content-container-item2{
            position: relative;
            min-height: 48px;
            padding: 0 16px 16px;
            overflow: hidden;
            .message-user-avatar{
                float: right;
            }

            .message{
              max-width: min(480px, calc(100% - 70px));
                margin: 0 10px;
                position: relative;
                overflow: hidden;
                float: right;
                color: white;
                .message-content{
                    line-height: 1.5;
                    font-size: 14px;
                    min-height: 37px;
                    padding: 8px 16px;
                    word-wrap: break-all;
                    word-break: break-all;
                    background: #80b9f2;
                    border-radius: 16px 0 16px 16px;
                    overflow: hidden;
                }

                .message-content-img{
                    max-width: 266px;
                    height: 100%;
                    border-radius: 16px 0px 16px 16px;
                    vertical-align: middle;
                }

                .share-video-container{
                  width: min(360px, 100%);
                    height: auto;
                    min-height: 273px;
                    max-height: 293px;
                    border-radius: 0px 0px 16px 16px;
                    cursor: pointer;
                    overflow: hidden;

                    .share-video-top{
                        position: relative;
                        .share-video-cover{
                        border-radius: 16px 0px 0px 0px;
                        width: 100%;
                        height: 200px;
                        object-fit: cover;
                    }
                    .share-video-time{
                        position: absolute;
                        bottom: 15px;
                        left: 5px;
                        font-size: 10px;
                        color: #fff;
                        background-color: rgba(0, 0, 0, 0.4);
                        border-radius: 2px;
                        height: 15px;
                        line-height: 15px;
                        padding: 0 4px;
                    }
                    .share-video-scrolling-number{
                        position: absolute;
                        bottom: 15px;
                        left: 48px;
                        font-size: 10px;
                        color: #fff;
                        height: 15px;
                        line-height: 15px;
                    }
                    }
                    .share-video-play-btn{
                      position: absolute;
                      bottom: 12px;
                      right: 10px;
                      width: 48px;
                      height: 48px;
                    }
                    .share-video-title{
                        position: relative;
                        top: -3px;
                        color: #212121;
                        padding: 10px;
                        background-color: white;
                        span{
                        display: -webkit-box;
                        text-overflow: ellipsis;
                        word-break: break-word;
                        -webkit-line-clamp: 2;
                        -webkit-box-orient: vertical;
                        font-size: 14px;
                        line-height: 20px;
                        max-height: 2.857142857142857em;
                        overflow: hidden;
                        }
                    }
                    .share-video-fotter{
                        display: flex;
                        position: relative;
                        top: -3px;
                        padding: 8px 10px;
                        border-top: 1px solid #e5e9ef;
                        background-color: white;
                        color: #999;
                        font-size: 12px;
                        align-items: center;
                        img{
                            width: 14px;
                            height: 12px;
                        }
                        .share-video-user-name{
                            font-size: 12px;
                            color: #999;
                            line-height: 16px;
                            padding-left: 4px;
                            width: 200px;
                            display: -webkit-box;
                            -webkit-line-clamp: 1;
                            -webkit-box-orient: vertical;
                            text-overflow: ellipsis;
                            overflow: hidden;
                        }
                        .share-contribute-video{
                            position: absolute;
                            right: 10px;
                        }
                    }
                }

                .delete-video{
                  width: min(360px, 100%);
                    height: 40px;
                    border-radius: 6px;
                    font-size: 18px;
                    background-color: #F6F7F8;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    cursor: pointer;
                    user-select: none;

                    .delete-info{
                    align-items: center;
                    color: #9499A0;
                    display: flex;
                    font-size: 13px;
                    line-height: 18px;
                    text-indent: 6px;
                      
                      svg{
                      height: 16px;
                      vertical-align: -2px;
                      width: 16px;
                      }
                    }
                }
            }
        }

        .context-menu{
            position: fixed;
            z-index: 100;
            border-radius: 5px;
            box-shadow: 0 2px 3px 0 rgba(121, 146, 185, 0.54);
            transition: all 0.3s ease;

            div:nth-child(1){
              border-radius: 5px 5px 0 0;
            }
            div:last-child{
                border-radius: 0 0 5px 5px;
            }
            div{
                display: flex;
                justify-content: left;
                align-items: center;
                cursor: pointer;
                padding: 5px 50px 5px 10px;
                color: #6c6c6c;
                font-size: 13px;
                background-color: #eeeff1;
                border-top: 1px solid #cccccc;
                border-bottom: 1px solid #cccccc;
                transition: background-color 0.3s ease;
            }

            div:hover{
                background-color: #d4d4d4;
            }

        }


        
    }

    .message-content-container::-webkit-scrollbar{
    width: 6px; /* 滚动条的宽度 */
    border-radius: 30px; /* 滚动条滑块的圆角 */
    }

    .message-content-container::-webkit-scrollbar-thumb {
    background: #aaa; /* 滚动条的滑块 */
    height: 10px;
    border-radius: 10px;
    }

    .message-send-container{
        position: relative;
        width: 100%;
        height: 162px;
        border-top: 1px solid #d8d8d8;
        border-bottom-right-radius: 4px;
        background-color: rgba(255, 255, 255, 0.5);
        flex-shrink: 0;
        padding: 0 16px;
        z-index: 2;

        .message-feature-container{
            height: 48px;
            display: -webkit-box;
            display: flex;
            -webkit-box-orient: horizontal;
            flex-direction: row;
            align-items: center;
            img{
                margin-right: 16px;
                cursor: pointer;
                user-select: none;
            }

            .message-emoji-list{
                position: relative;
                width: 380px;
                height: 280px;
                left: -50px;
                top: -162px;
                border: 1px solid #e5e9ef;
                border-radius: 8px;
                padding: 11px 15px 30px 15px;
                background-color: #fff;
                box-shadow: 0 3px 5px 0 rgba(0, 0, 0, 0.17);
                overflow: hidden;
                overflow-y: scroll;
                z-index: 20;

                .emoji-title{
                    position: absolute;
                    font-size: 14px;
                    color: #333;
                }

                .close-emoji{
                    position: absolute;
                    width: 22px;
                    height: 22px;
                    top: 10px;
                    right: 11px;
                    color: #999;
                    cursor: pointer;
                }

                .close-emoji:hover{
                    color: #2faee3;
                }

                .comment-emoji-content {
                    position: relative;
                    top: 30px;
                    left: 0px;
                    width: 38px;
                    height: 38px;
                    padding: 4px 4px 4px 3px;
                    margin-right: 10.5px;
                    margin-bottom: 12px;
                    display: inline-flex;
                    align-items: center;
                    cursor: pointer;
                    background-color: white;
                    border-radius: 4px;
                    transition: background-color 0.1s ease;
                    img {
                        width: 32px;
                        height: 32px;
                    }
                }

                .comment-emoji-content:hover {
                    background-color: #e3e5e7;
                }
            }

            .message-emoji-list::-webkit-scrollbar{
            width: 6px; /* 滚动条的宽度 */
            border-radius: 30px; /* 滚动条滑块的圆角 */
            }

            .message-emoji-list::-webkit-scrollbar-thumb {
            background: #aaa; /* 滚动条的滑块 */
            height: 10px;
            border-radius: 10px;
            }
        }

        .message-input-focus{
            width: 100%;
            height: 50px;
            .message-input-container{
            position: relative;
            width: 100%;
            min-height: 22px;
            max-height: 60px;
            letter-spacing: 1px;
            box-sizing: border-box;
            white-space: pre-wrap;
            overflow-x: hidden;
            overflow-y: auto;
            color: #222;
            font-size: 14px;
            vertical-align: baseline;
            outline: none;
            line-height: normal;
            border-radius: 4px;
            word-break: break-all;
            word-wrap: break-all;
        }

        .message-input-container::-webkit-scrollbar{
        width: 6px; /* 滚动条的宽度 */
        border-radius: 30px; /* 滚动条滑块的圆角 */
        }

        .message-input-container::-webkit-scrollbar-thumb {
        background: #aaa; /* 滚动条的滑块 */
        height: 10px;
        border-radius: 10px;
        }

        }


        .message-footer-container{
            position: absolute;
            bottom: 15px;
            right: 16px;
            span{
                position: relative;
                right: 13px;
                top: -1px;
                display: inline-block;
                font-size: 12px;
                color: #c0c0c0;
            }

            button{
                width: 88px;
                height: 30px;
                background-color: #1389bf;
                border: 1px solid #1389bf;
                color: #fff;
                cursor: pointer;
                transition: 200ms;
                border-radius: 4px;
            }

            button:hover{
                background-color: #2faee3;
                border: 1px solid #2faee3;
            }

            .sendMessageBtnClass{
                background-color: white;
                color: black;
                border: 1px solid #e7e7e7;
                cursor: not-allowed;
            }
            .sendMessageBtnClass:hover{
                background-color: white;
                border: 1px solid #e7e7e7;
            }
        }
    }

      .scroll-to-bottom-btn{
        position: absolute;
        left: auto;
        right: 24px;
        top: auto;
        bottom: 184px;
        width: 36px;
        height: 36px;
        border-radius: 50%;
        background-color: #00AEEC;
        color: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 4px 8px rgba(0,0,0,0.15);
        cursor: pointer;
        z-index: 25;
        transition: transform 0.15s ease, opacity 0.15s ease;
      }

      .scroll-to-bottom-btn:hover{
        transform: translateY(-4px);
      }



    }

    .no-message-container{
       position: relative;
       flex: 1 1 auto;
       user-select: none;
       display: flex;
       flex-direction: column;
       justify-content: center;
       align-items: center;
       gap: 18px;
       img{
        position: static;
        max-width: min(260px, 70%);
       }
       div{
        position: static;
        font-size: 14px;
        color: #8896b8;
       }
    }

    }

    .message-setting-dialog{
        position: absolute;
        width: 120px;
        height: 210px;
        background-color: white;
        top: 90px;
        right: -51px;
        border: 1px solid #e9eaec;
        box-shadow: 0 6px 12px 0 rgba(106, 115, 133, 0.22);
        border-radius: 4px;
        padding: 4px 0;
        text-align: center;
        cursor: pointer;
        z-index: 2;

        div{
        display: block;
        height: 40px;
        line-height: 40px;
        font-size: 14px;
        color: #333;
        word-break: keep-all;
        transition: all 0.3s ease;
        }

        div:hover{
        color: #2faee3;
        background: #e5e9ef;
        }
    }

    .message-setting-dialog:before{
    position: absolute;
    top: -6px;
    left: calc(50% - 5px);
    z-index: -1;
    content: '';
    width: 10px;
    height: 10px;
    background-color: #fff;
    border: 1px solid #e9eaec;
    border-bottom-color: transparent;
    border-left-color: transparent;
    border-radius: 4px;
    transform: rotate(-45deg);
    }



}

@media (max-width: 900px){
  .whisper-container{
    --whisper-left: 132px;
    width: calc(100vw - var(--whisper-left) - 12px);
  }
}

@media (max-width: 720px){
  .whisper-container{
    --whisper-left: 120px;
    width: calc(100vw - var(--whisper-left) - 8px);

    .body{
      .body-left{
        flex-basis: 86px;
        min-width: 86px;

        .recent-message{
          padding-left: 0;
          text-align: center;
        }

        .message-user-list .message-user-item{
          padding: 19px 23px;

          .message-user-name,
          .message-user-content,
          .message-dnd{
            display: none;
          }

          .message-unread-number{
            top: 16px;
            right: 12px;
          }
        }
      }

      .body-right .message-send-container .message-feature-container .message-emoji-list{
        width: min(320px, calc(100vw - 150px));
        left: -38px;
      }
    }
  }
}


@keyframes message-setting-dialog-show{
    0%{
        opacity: 0;
        visibility: hidden;
        transform: scale(0.8);
    }
    100%{
        opacity: 1;
        visibility: visible;
        transform: scale(1);
    }
}

@keyframes message-setting-dialog-show2{
    0%{
        opacity: 1;
        visibility: visible;
        transform: scale(1);
    }
    100%{
        opacity: 0;
        visibility: hidden;
        transform: scale(0.8);
    }
}

.message-setting-dialog-class{
    animation: message-setting-dialog-show 0.3s ease forwards;
}

.message-setting-dialog-class2{
    animation: message-setting-dialog-show2 0.3s ease forwards;
}

@keyframes deleteMsg{
    0%{
        transform: translate(-50px,-50%);
    }
    100%{
        transform: translate(0px,-50%);
    }
}

@keyframes loading{
    0%{
        opacity: 1;
    }
    100%{
        opacity: 0;
    }
}

.loading-icon{
        display: inline-block;
        position: relative;
        width: 64px;
        height: 64px;
        transform: scale(0.4);

        div:after{
        content: ' ';
        display: block;
        position: absolute;
        top: 8px;
        left: 29px;
        width: 5px;
        height: 14px;
        border-radius: 20%;
        background: #333;
        }

        div{
            animation: loading 1.2s linear infinite;
        }

        div:nth-child(1){
        transform: rotate(0deg);
        animation-delay: -1.1s;
        }

        div:nth-child(2){
        transform: rotate(30deg);
        animation-delay: -1s;
        }

        div:nth-child(3){
        transform: rotate(60deg);
        animation-delay: -0.9s;
        }

        div:nth-child(4){
        transform: rotate(90deg);
        animation-delay: -0.8s;
        }

        div:nth-child(5){
        transform: rotate(120deg);
        animation-delay: -0.7s;
        }

        div:nth-child(6){
        transform: rotate(150deg);
        animation-delay: -0.6s;
        }

        div:nth-child(7){
        transform: rotate(180deg);
        animation-delay: -0.5s;
        }

        div:nth-child(8){
        transform: rotate(210deg);
        animation-delay: -0.4s;
        }

        div:nth-child(9){
        transform: rotate(240deg);
        animation-delay: -0.3s;
        }

        div:nth-child(10){
        transform: rotate(270deg);
        animation-delay: -0.2s;
        }

        div:nth-child(11){
        transform: rotate(300deg);
        animation-delay: -0.1s;
        }

        div:nth-child(12){
        transform: rotate(330deg);
        animation-delay: 0s;
        }
}

.delete-dialog-content{
    position: relative;
    left: 16px;
    top: 18.5px;
    color: #666;
    transition: all 0.2s ease;
}

.delete-dialog-btn-container{
    position: relative;
    left: 92px;
    top: 46.5px;
}

.delete-dialog-cancel-btn{
    border: 1px solid #23ade5;
    color: #23ade5;
    border-radius: 4px;
    cursor: pointer;
    min-width: 104px;
    height: 32px;
    font-size: 14px;
    background-color: white;
    margin: 0 10px;
    transition: all 0.2s ease;
}

.delete-dialog-cancel-btn:hover{
    background-color: #23ade5;
    color: white;
}

.delete-dialog-confirm-btn{
    background-color: #23ade5;
    border: 1px solid #23ade5;
    color: #fff;
    border-radius: 4px;
    cursor: pointer;
    min-width: 104px;
    height: 32px;
    font-size: 14px;
    margin: 0 10px;
}

.delete-dialog-confirm-btn:hover{
    background-color: #39b5e7;
}


</style>