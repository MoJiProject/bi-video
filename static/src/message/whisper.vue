<template>
  <div class="whisper-container" :class="{ 'conversation-open': mobileConversationOpen && !!currentDialogue }" @keydown.esc="messageDialogFlag=false, emojiFlag=false, handleContextMenuFlag=false" v-loading.fullscreen="loading" element-loading-background="rgba(122, 122, 122, 0)">
    <div class="head2">我的消息</div>
    <div class="body">
        <div class="body-left">
        <div class="recent-message">近期消息</div>
        <div class="message-user-list" @scroll="dialogueListScroll">
            <div v-if="dialogueState==='loading'" class="conversation-loading" role="status">正在加载会话…</div>
            <AppEmptyState v-if="dialogueState==='error'" kind="unavailable" title="加载会话失败" action-text="重新加载" compact @action="selectDialogueF"/>
            <div class="message-user-item" role="button" tabindex="0" :aria-label="'与' + sDialogue.userInfo.userName + '的会话'" :aria-pressed="sDialogue===currentDialogue" :style="{marginBottom: index===dialogueList.length-1&&dialogueLoadingFlag? '30px' : '0px'}" @click="currentDialogue = sDialogue, mobileConversationOpen = true" @keydown.enter.self="currentDialogue = sDialogue, mobileConversationOpen = true" @keydown.space.self.prevent="currentDialogue = sDialogue, mobileConversationOpen = true" :class="{'message-user-itemBackground': sDialogue===currentDialogue}" v-for="(sDialogue,index) in dialogueList" :key="sDialogue.dialogue.id">
                <button type="button" class="delete-message" @click.stop="deleteDialogueF(sDialogue)" aria-label="删除会话"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024"><path fill="currentColor" d="M764.288 214.592 512 466.88 259.712 214.592a31.936 31.936 0 0 0-45.12 45.12L466.752 512 214.528 764.224a31.936 31.936 0 1 0 45.12 45.184L512 557.184l252.288 252.288a31.936 31.936 0 0 0 45.12-45.12L557.12 512.064l252.288-252.352a31.936 31.936 0 1 0-45.12-45.184z"></path></svg></button>
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
        <AppEmptyState v-if="dialogueList.length===0&&dialogueState==='ready'" title="暂无会话" compact/>
        </div>
        <div v-show="currentDialogue" class="body-right">
         <div class="message-username-title">
            <button type="button" class="conversation-back" @click="mobileConversationOpen=false, messageDialogFlag=false, emojiFlag=false">‹ 会话列表</button>
            <span class="dialogue-title">{{ dialogueUserInfo?.userName }}</span>
            <button type="button" class="message-setting-dialog-img-container" aria-label="会话设置" :aria-expanded="messageDialogFlag">
                <svg class="message-setting-dialog-img" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><circle cx="12" cy="5" r="2"></circle><circle cx="12" cy="12" r="2"></circle><circle cx="12" cy="19" r="2"></circle></svg>
            </button>
            <div v-show="messageDialogFlag" class="message-setting-dialog" :class="{
                'message-setting-dialog-class': messageDialogFlag,
                'message-setting-dialog-class2': !messageDialogFlag,
            }">
              <button type="button" class="message-setting-action" v-show="currentDialogue?.dialogue.upDateTime===null" @click="changeUpStatusF(currentDialogue)">置顶聊天</button>
              <button type="button" class="message-setting-action" v-show="currentDialogue?.dialogue.upDateTime!==null" @click="changeUpStatusF(currentDialogue)">取消置顶</button>
              <button type="button" class="message-setting-action" v-show="currentDialogue?.dialogue.dnd===0" @click="changeDndStatusF(currentDialogue)">开启免打扰</button>
              <button type="button" class="message-setting-action" v-show="currentDialogue?.dialogue.dnd===1" @click="changeDndStatusF(currentDialogue)">关闭免打扰</button>
              <button type="button" disabled>加入黑名单</button>
              <button type="button" disabled>举报该用户</button>
              <button type="button" disabled>不接收推送</button>
            </div>
         </div>
         <div ref="messageContent" class="message-content-container" @scroll="messageListScroll">
            <div v-show="!messageLoading&&!messageLoadingFlag" class="no-message">没有更多消息了</div>
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
                            <img src="/img/author-badge-default.png">
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
             <div v-show="handleContextMenuFlag" class="context-menu" role="group" aria-label="消息操作"
             @contextmenu="handleContextMenu2" 
             :style="{
                top: handleContextMenuPosition.y+'px',
                left: handleContextMenuPosition.x+'px',
             }"
             >
                 <button type="button" v-if="!messageIsImgFlag&&messageObj?.messageType===1" @click="handleCopyMessage">复制</button>
                 <button type="button" v-if="messageObj?.senderId===store.userId" @click="revocationMessageF(messageObj)">撤回</button>
                 <button type="button" @click="deleteMessageDialogFlag=true">删除</button>
                 <button type="button" v-if="messageIsImgFlag" @click="handleSaveImg(messageObj)">保存图片</button>
             </div>
         </div>

         <button type="button" v-show="showScrollToBottom" class="scroll-to-bottom-btn" @click="scrollToBottomF" title="回到底部">
           <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg>
         </button>

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
                <button type="button" class="message-feature-button" aria-label="发送图片" @click="addSendImg">
                  <svg class="message-feature-img" viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="3" width="18" height="18" rx="4"/><circle cx="8" cy="8" r="1.5"/><path d="m3 17 5-5 4 4 4-6 5 7"/></svg>
                </button>
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
                <button type="button" class="message-feature-emoji message-feature-button" aria-label="表情" :aria-expanded="emojiFlag">
                  <svg class="message-feature-emoji" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9"/><path d="M8 14a4 4 0 0 0 8 0M8 9h.01M16 9h.01"/></svg>
                </button>
                </el-tooltip>
                <el-tooltip
                  popper-class="custom-tooltip"
                  effect="light"
                  :show-after="300"
                  content="@"
                  placement="right"
                  :offset="12"
                  :show-arrow="false"
                  :hide-after="0"
                >
                <button type="button" class="message-feature-at message-feature-button" aria-label="提醒某人（@）" @click="addAt">
                  <svg class="message-feature-at" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="4"/><path d="M16 8v5a3 3 0 0 0 6 0v-1a10 10 0 1 0-3.92 7.94"/></svg>
                </button>
                </el-tooltip>
                <eit
                    v-show="eitFlag"
                    :eitList="store.eitList"
                    :eitKeyWord="eitKeyWord"
                ></eit>
                <div v-show="emojiFlag" class="message-emoji-list">
                   <div class="emoji-title">表情</div>
                   <div class="emoji-grid">
                        <button
                            type="button"
                            class="comment-emoji-content"
                            :title="`表情 ${index}`"
                            :aria-label="`表情 ${index}`"
                            @click="addEmoji(index)"
                            v-for="index in 176"
                            :key="index"
                            >
                            <img class="comment-emoji-img" :src="'/img/emoji/' + index + '.png'" alt="">
                        </button>
                    </div>
                </div>
                <input type="file" class="message-file-input" style="display: none;" accept="image/*" @change="handleFileChange">
            </div>
            <div class="message-input-focus" @click="messageInput.focus()">
                <div class="message-input-container" ref="messageInput" @blur="getAfterBlurIndex" @input="onInputChange" @keydown="handleInputKeydown" contenteditable="true" role="textbox" aria-label="输入消息" aria-multiline="true" data-placeholder="输入消息"></div>
            </div>
            <div class="message-footer-container">
                <span>{{messageNumber}}/500</span>
                <button v-debounce :class="{sendMessageBtnClass: messageNumber===0}"  @click="sendMessageF">发送</button>
            </div>
         </div>
        </div>
        <div v-show="!currentDialogue" class="no-message-container">
            <AppEmptyState title="选择一个会话"/>
        </div>
    </div>
    <el-dialog
      v-model="deleteMessageDialogFlag"
      append-to-body
      width="min(464px, calc(100vw - 32px))"
      class="message-confirm-dialog"
      :lock-scroll="false"
      align-center
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
import eit from "../components/eit";
import { fetchMentionUsers } from "../api/product";
import { ElMessage } from "element-plus";
import {
  selectDialogue,
  selectNotReadNumber,
  changeUpStatus,
  changeDndStatus,
  deleteDialogue,
  sendMessage,
  revocationMessage,
  changeMessageStatus,
  deletePrivateMessage,
  selectPrivateMessage
} from "../api/privateMessage/index";
import { authApi } from "../api/product/index";
import { subscribeChat } from "../services/chatLive";
import { post } from "../services/http";
import { sanitizeRichText } from "../utils/richText";

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
const messageDialogFlag = ref(false);
const emojiFlag = ref(false);
const dialogueList = reactive([]);
const dialoguePageNum = ref(1);
const messagePageNum = ref(1);
const currentDialogue = ref(null);
const mobileConversationOpen = ref(false);
const dialogueState = ref("loading");
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
let initialDialogueLoad = Promise.resolve();
let dialogueFetching = false;
const eitFlag = ref(false);
const eitKeyWord = ref("");
let eitListLoaded = false;

// 懒加载 @ 用户列表（videoPage 也会拉，这里消息页独立拉一次）
async function loadEitList() {
  if (eitListLoaded || !store.userId) return;
  eitListLoaded = true;
  try {
    store.setEitList(await fetchMentionUsers());
  } catch (error) {
    eitListLoaded = false; // 失败允许重试
  }
}

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
  return sanitizeRichText(String(html)
    .split(/(<a\b[^>]*>[\s\S]*?<\/a>)/gi)
    .map(chunk => (chunk.toLowerCase().startsWith("<a") ? chunk : linkifyText(chunk)))
    .join(""));
}

onMounted(() => {
  window.addEventListener("click", outSideClick);
  initialDialogueLoad = selectDialogueF();
  refreshUnreadNumber();
  loadEitList();
});

onUnmounted(() => {
  chatClosed = true;
  window.removeEventListener("click", outSideClick);
  if (socket) {
    socket.close();
  }
});

function outSideClick(e) {
  handleContextMenuFlag.value = false;
  // 会话设置按钮内是 svg 图标，className 不是字符串，统一用 closest 命中判断
  const settingBtn = e.target instanceof Element && e.target.closest(".message-setting-dialog-img-container");
  if (!messageDialogFlag.value) {
    if (settingBtn) messageDialogFlag.value = true;
  } else {
    messageDialogFlag.value = false;
  }
  // @ 用户选择列表开合（eit 内部是普通元素，closest 可靠命中）
  const eitPanel = e.target instanceof Element && e.target.closest(".eit");
  const atBtn = e.target instanceof Element && e.target.closest(".message-feature-at");
  if (eitFlag.value) {
    if (!eitPanel && !atBtn) eitFlag.value = false; // 点列表外（除按钮）收起
  }
  const classText = e.target instanceof Element ? (e.target.getAttribute("class") || "") : "";
  if (!emojiFlag.value) {
    if (classText.indexOf("message-feature-emoji") !== -1) emojiFlag.value = true;
  } else {
    if (classText.indexOf("message-emoji-list") === -1 && classText.indexOf("emoji-title") === -1 && classText.indexOf("comment-emoji-content") === -1 && classText.indexOf("comment-emoji-img") === -1) {
      emojiFlag.value = false;
    }
  }
  // 点击 @ 按钮时收起表情面板，避免两个浮层叠在一起
  if (emojiFlag.value && classText.indexOf("message-feature-at") !== -1) {
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
  const receiverId = currentDialogue.value?.dialogue.dialogueId;
  if (!file) return;
  if (!receiverId) { showChatError(new Error("请先选择会话")); event.target.value=""; return; }
  if (!file.type.startsWith("image/")) {
    ElMessage({ message: "只能上传图片", type: "warning", duration: 1700 });
    event.target.value = "";
    return;
  } else if (file.size >= 10 * 1024 * 1024) {
    ElMessage({ message: "图片大小不能超过10M", type: "warning", duration: 1700 });
    event.target.value = "";
    return;
  }
  loading.value = true;
  try {
    const form = new FormData();
    form.append("file", file);
    const uploaded = await post("/media/image", form);
    if (uploaded.url) {
      let message = {
        senderId: store.userId,
        receiverId,
        content: uploaded.url,
        messageType: 2
      };
      sendMessage(store.token, message).then(res => {
        if (res.data.code === 1) {
          applyChatMessage(res.data.data);
          syncChatDialogues().catch(showChatError);
        } else {
          ElMessage({ message: res.data.msg, type: "warning", duration: 1700 });
        }
        loading.value = false;
      }).catch(error => {
        loading.value = false;
        showChatError(error);
      });
    }
  } catch (err) {
    loading.value = false;
    showChatError(err);
  }
  event.target.value = "";
}

async function selectDialogueF() {
  if (!store.token || !store.userId || chatClosed) return;
  dialogueState.value = "loading";
  try {
    const res = await selectDialogue(store.token, store.userId, 1);
    if (chatClosed) return;
    dialogueList.splice(0, dialogueList.length, ...res.data.data);
    dialoguePageNum.value = 2;
    dialogueLoadingFlag.value = res.data.data.length === 20;
    const peer = Number(new URLSearchParams(window.location.search).get("dialogueId"));
    if (peer > 0 && !dialogueList.some(item => item.dialogue.dialogueId === peer)) {
      const requested = await selectDialogue(store.token, store.userId, 1, peer);
      if (chatClosed) return;
      dialogueList.push(...requested.data.data);
    }
    currentDialogue.value = dialogueList.find(item => item.dialogue.dialogueId === peer) || dialogueList[0] || null;
    dialogueState.value = "ready";
  } catch (error) {
    dialogueState.value = "error";
    showChatError(error);
  }
}

async function selectDialogueF2() {
  if (!store.token || !store.userId || chatClosed || dialogueFetching) return;
  dialogueFetching = true;
  try {
    const res = await selectDialogue(store.token, store.userId, dialoguePageNum.value);
    if (chatClosed) return;
    const ids = new Set(dialogueList.map(item => item.dialogue.id));
    dialogueList.push(...res.data.data.filter(item => !ids.has(item.dialogue.id)));
    dialogueLoadingFlag.value = res.data.data.length === 20;
    dialoguePageNum.value++;
  } catch (error) {
    showChatError(error);
  } finally {
    dialogueFetching = false;
    dialogueLoading.value = false;
  }
}

function changeUpStatusF(sDialogue) {
  if (sDialogue.dialogue.id === null) return;
  changeUpStatus(store, sDialogue.dialogue.id).then(() => queueChatSync(false)).catch(showChatError);
}

function changeDndStatusF(sDialogue) {
  if (sDialogue.dialogue.id === null) return;
  changeDndStatus(store, sDialogue.dialogue.id).then(() => queueChatSync(false)).catch(showChatError);
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
      queueChatSync(false);
    }
  }).catch(showChatError);
}

function handleContextMenu(e, flag) {
  messageIsImgFlag.value = flag;
  e.preventDefault();
  const bounds = e.currentTarget.getBoundingClientRect();
  handleContextMenuPosition.value = {
    x: Math.max(8, Math.min(e.clientX || bounds.left, window.innerWidth - 168)),
    y: Math.max(8, Math.min(e.clientY || bounds.bottom, window.innerHeight - 168)),
  };
  handleContextMenuFlag.value = true;
  copyMessage.value = e.currentTarget.closest(".message-content-container-item")?.querySelector(".message-content")?.innerText || e.target.innerText;
}

function handleContextMenu2(e) {
  e.preventDefault();
}

async function selectPrivateMessageF(sDialogue, a) {
  if (!sDialogue?.dialogue?.id) return;
  messageLoadingFlag.value = false;
  const thisPage = Math.max(1, messagePageNum.value);
  messagePageNum.value = Math.max(0, messagePageNum.value - 1);
  return selectPrivateMessage(store, sDialogue.dialogue.dialogueId, thisPage).then(res => {
    if (sDialogue !== currentDialogue.value || chatClosed) return;
    if (res.data.code === 1) {
      const incoming = res.data.data.privateMessage || [];
      if (a === 1) {
        if (incoming.length) {
          const ids = new Set(privateMessageList.map(message => message.id));
          privateMessageList.push(...incoming.filter(message => !ids.has(message.id)));
        }
      } else {
        if (incoming.length) {
          const ids = new Set(privateMessageList.map(message => message.id));
          privateMessageList.unshift(...incoming.filter(message => !ids.has(message.id)));
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
  }).catch(error => {
    messagePageNum.value = thisPage;
    messageLoading.value = false;
    showChatError(error);
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
  return selectPrivateMessage(store, sDialogue.dialogue.dialogueId, thisPage).then(res => {
    if (sDialogue !== currentDialogue.value || chatClosed) return;
    if (res.data.code === 1) {
      const incoming = res.data.data.privateMessage || [];
      if (incoming.length) {
        const ids = new Set(privateMessageList.map(message => message.id));
        privateMessageList.unshift(...incoming.filter(message => !ids.has(message.id)));
      }
      messageLoadingFlag.value = messagePageNum.value > 0;
      messageLoading.value = false;
      dealyFlag = true;
    }
  }).catch((error) => {
    messagePageNum.value = thisPage;
    messageLoading.value = false;
    dealyFlag = true;
    showChatError(error);
  });
};

watch(currentDialogue, async (newVal) => {
  if (newVal === null) mobileConversationOpen.value = false;
  if (newVal !== null) {
    mobileConversationOpen.value = true;
    messageDialogFlag.value = false;
    handleContextMenuFlag.value = false;
    emojiFlag.value = false;
    userInfo.value = null;
    messageNumber.value = 0;
    dialogueUserInfo.value = null;
    privateMessageList.length = 0;
    messageLoadingFlag.value = true;
    messagePageNum.value = Math.max(1, Math.ceil(newVal?.allMessageNumber / 20));
    await selectPrivateMessageF(newVal, 1);
    if (newVal !== currentDialogue.value) return;
    if (messagePageNum.value > 0 && privateMessageList.length < 20) await selectPrivateMessageF(newVal, 2);
    dealyFlag = true;
    tryChangeMessageStatus(newVal);
  }
  if (messageInput?.value) messageInput.value.innerHTML = "";
});
  
//打开会话即已读：本地未读数立即扣减，不依赖聊天 socket 是否连上
function tryChangeMessageStatus(newVal) {
  if (!newVal?.dialogue?.id) return;
  changeMessageStatusF(newVal);
}

function revocationMessageF(privateMessage) {
  if (privateMessage.id === null) return;
  const startTime = new Date(privateMessage?.sendTime);
  const endTime = new Date();
  const seconds = Math.abs(startTime.getTime() - endTime.getTime()) / 1000;
  if (seconds > 180) {
    ElMessage({ message: "消息已超过3分钟，无法撤回", type: "warning", duration: 1700 });
    return;
  }
  revocationMessage(store, privateMessage.id).then(res => {
    if (res.data.code === 1) {
      const index = privateMessageList.findIndex(item => item.id === privateMessage.id);
      if (index !== -1) privateMessageList[index].status = 2;
      ElMessage({ message: "撤回成功", type: "success", duration: 1700 });
      queueChatSync(false);
    } else {
      ElMessage({ message: res.data.msg, type: "warning", duration: 1700 });
    }
  }).catch(showChatError);
}

function deletePrivateMessageF(privateMessage) {
  if (privateMessage.id === null) return;
  deletePrivateMessage(store, privateMessage.id).then(res => {
    if (res.data.code === 1) {
      const index = privateMessageList.findIndex(item => item.id === privateMessage.id);
      if (index !== -1) privateMessageList.splice(index, 1);
      //同步左侧会话预览，跟后端保持一致：取当前可见的最后一条
      refreshDialoguePreview();
      queueChatSync(false);
      ElMessage({ message: "删除成功", type: "success", duration: 1700 });
      deleteMessageDialogFlag.value = false;
    } else {
      ElMessage({ message: res.data.msg, type: "warning", duration: 1700 });
    }
  }).catch(showChatError);
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
  if (!sDialogue?.dialogue.dialogueId) return;
  return changeMessageStatus(store, sDialogue.dialogue.dialogueId).then(res => {
    if (res.data.code === 1) {
      const index = dialogueList.findIndex(item => item.dialogue.id === sDialogue.dialogue.id);
      if (index !== -1) dialogueList[index].notReadNumber = 0;
      //顶部/侧边的未读数会被头部组件的登录校验整体覆盖，这里用服务端最新值校正一次
      return syncUserMessageNumber();
    }
  }).catch(showChatError);
}

//同步服务端最新的未读私信数
function syncUserMessageNumber() {
  if (!store.userId) return;
  return authApi.me().then(meData => {
    if (!meData || !meData.userId) return;
    const userInformation = { ...store.userInformation };
    userInformation.messageNumber = Math.max(0, meData.messageNumber || 0);
    userInformation.allMessageNumber = userInformation.messageNumber
      + (userInformation.replyCommentNumber || 0)
      + (userInformation.atNumber || 0)
      + (userInformation.likeAllNumber || 0);
    store.setUserInformation(userInformation);
  });
}

function sendMessageF() {
  if (!currentDialogue.value) { showChatError(new Error("请先选择会话")); return; }
  if (messageNumber.value === 0) return;
  if (messageNumber.value > 500) {
    ElMessage({ message: "消息字数不能超过500", type: "warning", duration: 1700 });
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
      applyChatMessage(res.data.data);
      queueChatSync(false);
    } else {
      ElMessage({ message: res.data.msg, type: "warning", duration: 1700 });
    }
  }).catch(showChatError);
}

function getAfterBlurIndex() {
  const selection = window.getSelection();
  if (selection.rangeCount > 0) {
    const currentRange = selection.getRangeAt(0);
    // 只有光标确实在输入框内才记录，避免点击 @ 列表等浮层时把 range 带走
    if (messageInput.value?.contains(currentRange.commonAncestorContainer)) {
      range = currentRange;
    }
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

// 在光标处插入 @ 并弹出用户选择列表（光标始终锚定在文本节点内，便于后续定位 @ 关键字）
function addAt() {
  const selection = window.getSelection();
  const inputDom = messageInput.value;
  if (!inputDom) return;
  let anchorNode = null;
  let anchorOffset = 0;
  if (!range || !inputDom.contains(range.startContainer)) {
    // 光标不在输入框内时，追加到末尾（与末尾文本节点合并）
    const last = inputDom.lastChild;
    if (last && last.nodeType === Node.TEXT_NODE) {
      last.appendData("@");
      anchorNode = last;
      anchorOffset = last.length;
    } else {
      const atSymbol = document.createTextNode("@");
      inputDom.appendChild(atSymbol);
      anchorNode = atSymbol;
      anchorOffset = 1;
    }
  } else if (range.startContainer.nodeType === Node.TEXT_NODE) {
    range.startContainer.insertData(range.startOffset, "@");
    anchorNode = range.startContainer;
    anchorOffset = range.startOffset + 1;
  } else {
    // 光标锚在元素上：优先并入前一个文本节点，否则插入独立文本节点
    const parent = range.startContainer;
    const ref = parent.childNodes[range.startOffset] || null;
    const prev = ref ? ref.previousSibling : parent.lastChild;
    if (prev && prev.nodeType === Node.TEXT_NODE) {
      prev.appendData("@");
      anchorNode = prev;
      anchorOffset = prev.length;
    } else {
      const atSymbol = document.createTextNode("@");
      parent.insertBefore(atSymbol, ref);
      anchorNode = atSymbol;
      anchorOffset = 1;
    }
  }
  const r = document.createRange();
  r.setStart(anchorNode, anchorOffset);
  r.collapse(true);
  selection.removeAllRanges();
  selection.addRange(r);
  range = r;
  inputDom.focus();
  updateFontNumber();
  eitKeyWord.value = "";
  eitFlag.value = true;
}

// 获取光标之前的文本（与 comment.vue 同款遍历）
function getTextBeforeCursor(range) {
  let textBeforeCursor = "";
  let currentNode = range.startContainer;
  while (currentNode) {
    if (currentNode.nodeType === Node.TEXT_NODE) {
      textBeforeCursor = currentNode.textContent.slice(0, range.startOffset) + textBeforeCursor;
      break;
    }
    currentNode = currentNode.previousSibling || currentNode.parentNode;
  }
  return textBeforeCursor;
}

// 输入时检测光标前的 @ 关键字，实时过滤列表；离开 @ 词段则收起
function handleEitInput() {
  if (!eitFlag.value) return;
  const inputDom = messageInput.value;
  if (!inputDom) return;
  const selection = window.getSelection();
  if (!selection.rangeCount) return;
  const currentRange = selection.getRangeAt(0);
  if (!inputDom.contains(currentRange.commonAncestorContainer)) {
    eitFlag.value = false;
    return;
  }
  const textBeforeCursor = getTextBeforeCursor(currentRange);
  const atIndex = textBeforeCursor.lastIndexOf("@");
  if (atIndex === -1) {
    eitFlag.value = false;
    return;
  }
  const text = textBeforeCursor.substring(atIndex);
  if (text.length > 20 || text.includes(" ")) {
    eitFlag.value = false;
    return;
  }
  eitKeyWord.value = text.substring(1).trim();
}

// 输入事件：刷新字数 + @ 关键字检测
function onInputChange() {
  updateFontNumber();
  handleEitInput();
}

// Backspace 删除 @ 锚点（contenteditable=false 元素浏览器默认删不掉，需手动处理）
function handleInputKeydown(event) {
  if (event.key !== "Backspace" && event.key !== "Delete") return;
  const selection = window.getSelection();
  if (!selection.rangeCount) return;
  const currentRange = selection.getRangeAt(0);
  if (!currentRange.collapsed) return; // 有选区时交给浏览器默认删除
  const inputDom = messageInput.value;
  if (!inputDom) return;

  if (event.key === "Backspace") {
    const start = currentRange.startContainer;
    // 光标在文本节点内且前面有文字：交给浏览器默认删除
    if (start.nodeType === Node.TEXT_NODE && currentRange.startOffset > 0) return;
    // 找光标前第一个节点（文本节点看 previousSibling，元素上看 offset-1 处子节点）
    let before = start.nodeType === Node.TEXT_NODE
      ? start.previousSibling
      : start.childNodes[currentRange.startOffset - 1] || null;
    // 跳过空文本节点（顺手收集待清理），找到真正的前节点
    const emptyNodes = [];
    if (start.nodeType === Node.TEXT_NODE && start.textContent === "") emptyNodes.push(start);
    while (before && before.nodeType === Node.TEXT_NODE && before.textContent === "") {
      emptyNodes.push(before);
      before = before.previousSibling;
    }
    if (!before || before.nodeType !== Node.ELEMENT_NODE || !before.classList.contains("at-msg")) {
      return; // 前面不是锚点，交给浏览器
    }
    event.preventDefault();
    // 锚点前面若还有空文本节点也一并清理，并找到可承载光标的文本节点
    let prevText = before.previousSibling;
    while (prevText && prevText.nodeType === Node.TEXT_NODE && prevText.textContent === "") {
      emptyNodes.push(prevText);
      prevText = prevText.previousSibling;
    }
    emptyNodes.forEach(n => n.remove());
    before.remove();
    // 光标宿主优先级：当前仍连接的非空文本节点 > 锚点前文本节点末尾 > 元素上锚点原位置之后
    if (start.isConnected && start.nodeType === Node.TEXT_NODE && start.textContent !== "") {
      normalizeCursorToNodeStart(selection, start, 0);
    } else if (prevText && prevText.nodeType === Node.TEXT_NODE) {
      normalizeCursorToNodeStart(selection, prevText, prevText.length);
    } else {
      const idx = prevText ? Array.prototype.indexOf.call(inputDom.childNodes, prevText) + 1 : 0;
      normalizeCursorToNodeStart(selection, inputDom, Math.max(0, idx));
    }
    updateFontNumber();
    return;
  }

  // Delete 键：删光标后的锚点
  const start = currentRange.startContainer;
  let after;
  if (start.nodeType === Node.TEXT_NODE) {
    if (currentRange.startOffset < start.textContent.length) return; // 后面还有文字
    after = start.nextSibling;
  } else {
    after = start.childNodes[currentRange.startOffset] || null;
  }
  const emptyNodes = [];
  while (after && after.nodeType === Node.TEXT_NODE && after.textContent === "") {
    emptyNodes.push(after);
    after = after.nextSibling;
  }
  if (!after || after.nodeType !== Node.ELEMENT_NODE || !after.classList.contains("at-msg")) {
    return; // 后面不是锚点，交给浏览器
  }
  event.preventDefault();
  emptyNodes.forEach(n => n.remove());
  after.remove();
  updateFontNumber();
}

// 删除节点后把光标放回指定文本节点开头，避免光标丢失
function normalizeCursorToNodeStart(selection, textNode, offset) {
  const r = document.createRange();
  r.setStart(textNode, offset);
  r.collapse(true);
  selection.removeAllRanges();
  selection.addRange(r);
  range = r;
}

// 选中用户后回填：删掉 @关键字 换成可点击的 @昵称
watch(() => store.eitUserName, newValue => {
  if (!newValue || !newValue.trim()) return;
  insertEitUser();
});

function insertEitUser() {
  if (!eitFlag.value && !store.eitUserName) return;
  const selection = window.getSelection();
  const currentRange = selection?.rangeCount ? selection.getRangeAt(0) : null;
  if (currentRange && messageInput.value?.contains(currentRange.startContainer)) {
    range = currentRange.cloneRange();
  } else if (!range || !messageInput.value?.contains(range.startContainer)) {
    return;
  }

  // 找到最近的 @ 并删除 @+关键字（光标锚在文本节点上时才可定位）
  if (range.startContainer.nodeType === Node.TEXT_NODE) {
    const containerText = range.startContainer.textContent;
    const startOffset = range.startOffset;
    const atIndex = containerText.lastIndexOf("@", startOffset - 1);
    if (atIndex !== -1) {
      const beforeAt = containerText.slice(0, atIndex);
      const afterAt = containerText.slice(startOffset);
      range.startContainer.textContent = beforeAt + afterAt;
      range.setStart(range.startContainer, atIndex);
      range.setEnd(range.startContainer, atIndex);
    }
  }

  const buttonHtml = `<a href="./home?homeMenu=1&userId=${store.eitUserId}" target="_blank" data-eitt-userId="${store.eitUserId}" contentEditable="false" class="at-msg" style="color:#0A8271;margin-right:3px;">@${store.eitUserName}</a>`;
  const fragment = range.createContextualFragment(buttonHtml);
  const lastNode = fragment.lastChild;
  range.insertNode(fragment);
  // 光标放进锚点后的空文本节点，保证后续输入仍锚定在文本节点内
  const emptyNode = document.createTextNode("");
  if (lastNode && lastNode.parentNode) {
    lastNode.parentNode.insertBefore(emptyNode, lastNode.nextSibling);
  } else {
    messageInput.value.appendChild(emptyNode);
  }
  range.setStart(emptyNode, 0);
  range.setEnd(emptyNode, 0);
  selection.removeAllRanges();
  selection.addRange(range);

  eitKeyWord.value = "";
  eitFlag.value = false;
  store.setEitUserName("");
  store.setEitUserId(null);
  messageInput.value?.focus();
  messageNumber.value = messageInput.value.innerText.trim().length + (messageInput.value.getElementsByTagName("img")?.length || 0) * 5;
}

let chatSync = Promise.resolve();
let chatClosed = false;
const socket = subscribeChat(event => {
  if (event.kind === "deleted") {
    const index = privateMessageList.findIndex(item => item.id === event.messageId);
    if (index !== -1) privateMessageList.splice(index, 1);
  } else if (event.kind === "read" && event.actorId !== store.userId
      && event.conversationId === currentDialogue.value?.dialogue.id) {
    privateMessageList.forEach(message => {
      if (message.senderId === store.userId && message.status === 0) message.status = 1;
    });
  } else if (event.message) {
    applyChatMessage(event.message);
  }
  queueChatSync(false);
}, reconnect => { if (reconnect) queueChatSync(true); }, showChatError);

function showChatError(error) {
  ElMessage.error(error.message || "私信同步失败");
}

function queueChatSync(reconnect) {
  chatSync = chatSync.then(async () => {
    if (chatClosed) return;
    await initialDialogueLoad;
    await syncChatDialogues();
    if (reconnect && currentDialogue.value) {
      const selected = currentDialogue.value;
      const page = Math.max(1, Math.ceil(selected.allMessageNumber / 20));
      const res = await selectPrivateMessage(store, selected.dialogue.dialogueId, page);
      if (chatClosed || selected !== currentDialogue.value) return;
      privateMessageList.splice(0, privateMessageList.length, ...res.data.data.privateMessage);
      userInfo.value = res.data.data.userInfo;
      dialogueUserInfo.value = res.data.data.dialogueUserInfo;
      messagePageNum.value = page - 1;
      messageLoadingFlag.value = page > 1;
      tryChangeMessageStatus(selected);
    }
  }).catch(showChatError);
}

async function syncChatDialogues() {
  const count = Math.max(1, dialoguePageNum.value - 1);
  const pages = await Promise.all(Array.from({ length: count }, (_, index) =>
    selectDialogue(store.token, store.userId, index + 1)));
  if (chatClosed) return;
  const rows = pages.flatMap(res => res.data.data);
  const selected = currentDialogue.value;
  if (selected && !rows.some(row => row.dialogue.id === selected.dialogue.id)) {
    const requested = await selectDialogue(store.token, store.userId, 1, selected.dialogue.dialogueId);
    if (chatClosed) return;
    rows.push(...requested.data.data);
  }
  const items = rows.map(row => {
    const existing = dialogueList.find(item => item.dialogue.id === row.dialogue.id);
    if (existing) { Object.assign(existing, row); return existing; }
    return row;
  });
  dialogueList.splice(0, dialogueList.length, ...items);
  if (selected && !items.some(item => item.dialogue.id === selected.dialogue.id)) currentDialogue.value = null;
  dialogueLoadingFlag.value = pages[pages.length - 1].data.data.length === 20;
  await syncUserMessageNumber();
}

function applyChatMessage(message) {
  if (!message || String(message.selectSign) !== String(currentDialogue.value?.dialogue.sign)) return;
  if (!userInfo.value || !dialogueUserInfo.value) { queueChatSync(true); return; }
  const index = privateMessageList.findIndex(item => item.id === message.id);
  if (index === -1) privateMessageList.push(message);
  else privateMessageList.splice(index, 1, message);
  privateMessageList.sort((a, b) => a.id - b.id);
  if (message.receiverId === store.userId && message.status === 0) changeMessageStatusF(currentDialogue.value);
  nextTick(() => {
    if (messageContent.value && (message.senderId === store.userId || !showScrollToBottom.value))
      messageContent.value.scrollTop = messageContent.value.scrollHeight;
  });
}

function updateFontNumber() {
  const inputDom = messageInput?.value;
  // 只剩 <br> 或空文本节点（删锚点后可能残留）时视为空，清掉残留使 :empty 占位符恢复
  if (inputDom && inputDom.innerText.trim() === "" && !inputDom.querySelector("img")) {
    inputDom.innerHTML = "";
    const selection = window.getSelection();
    const r = document.createRange();
    r.selectNodeContents(inputDom);
    r.collapse(false);
    selection.removeAllRanges();
    selection.addRange(r);
  }
  messageNumber.value = (inputDom?.innerText?.trim()?.length || 0) + ((inputDom?.getElementsByTagName("img")?.length) || 0) * 5;
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
    ElMessage({ message: "复制成功", type: "success", duration: 1700 });
    navigator.clipboard.writeText(copyMessage.value);
    copyMessage.value = null;
  } else {
    ElMessage({ message: "不能复制表情", type: "warning", duration: 1700 });
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
  userInformation.allMessageNumber = userInformation.messageNumber
    + (userInformation.replyCommentNumber || 0)
    + (userInformation.atNumber || 0)
    + (userInformation.likeAllNumber || 0);
  store.setUserInformation(userInformation);
}

function setMessageNumber(messageNumber) {
  const userInformation = { ...(store.userInformation || {}) };
  userInformation.messageNumber = Math.max(0, messageNumber || 0);
  userInformation.allMessageNumber = userInformation.messageNumber
    + (userInformation.replyCommentNumber || 0)
    + (userInformation.atNumber || 0)
    + (userInformation.likeAllNumber || 0);
  store.setUserInformation(userInformation);
}

async function refreshUnreadNumber() {
  if (store.token === null || store.userId === null) return;
  try {
    const res = await selectNotReadNumber(store.token, store.userId);
    if (res.data.code !== 1) return;
    setMessageNumber(res.data.data);
  } catch (error) {
    showChatError(error);
  }
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
            background-color: #F0A03C;
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
          color: #F0A03C;
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
        display: flex;
        align-items: center;
        justify-content: center;
        color: #6b757b;

        &:hover,
        &[aria-expanded="true"]{
            color: #2BB2A0;
        }

        svg{
            user-select: none;
            width: 16px;
            height: 16px;
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
                    color: #F0A03C;
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
                    background-color: #F4F7F6;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    cursor: pointer;
                    user-select: none;

                    .delete-info{
                    align-items: center;
                    color: #8D9794;
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
                    background: #79C7B6;
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
                    background-color: #F4F7F6;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    cursor: pointer;
                    user-select: none;

                    .delete-info{
                    align-items: center;
                    color: #8D9794;
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

            .message-feature-button svg{
                display: block;
                width: 16px;
                height: 16px;
                margin: 0;
                fill: none;
                stroke: currentColor;
                stroke-width: 1.7;
                stroke-linecap: round;
                stroke-linejoin: round;
                pointer-events: none;
            }

            .message-emoji-list{
                position: relative;
                display: flex;
                flex-direction: column;
                width: 320px;
                left: -50px;
                top: -162px;
                border: 1px solid #E0E5E3;
                border-radius: 8px;
                padding: 12px;
                background-color: #fff;
                z-index: 20;

                .emoji-title{
                    flex-shrink: 0;
                    padding: 0 2px 10px;
                    border-bottom: 1px solid #EFF3F2;
                    font-size: 13px;
                    font-weight: 600;
                    color: #1C2321;
                }

                .emoji-grid{
                    display: grid;
                    grid-template-columns: repeat(8, minmax(0, 1fr));
                    gap: 3px;
                    max-height: 224px;
                    margin-top: 8px;
                    overflow-y: auto;
                    overscroll-behavior: contain;
                    scrollbar-width: thin;
                }

                .comment-emoji-content {
                    display: grid;
                    place-items: center;
                    min-width: 0;
                    height: 32px;
                    padding: 3px;
                    border: 0;
                    border-radius: 6px;
                    background: transparent;
                    cursor: pointer;
                    img {
                        width: 24px;
                        height: 24px;
                    }
                }

                .comment-emoji-content:hover {
                    background-color: #E7F6F3;
                }
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
                background-color: #0F7E77;
                border: 1px solid #0F7E77;
                color: #fff;
                cursor: pointer;
                transition: 200ms;
                border-radius: 4px;
            }

            button:hover{
                background-color: #2BB2A0;
                border: 1px solid #2BB2A0;
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
        background-color: #0FA68E;
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
        color: #2BB2A0;
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
      .body-right .message-send-container .message-feature-container .eit{
        left: 12px;
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
    border: 1px solid #20AF99;
    color: #20AF99;
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
    background-color: #20AF99;
    color: white;
}

.delete-dialog-confirm-btn{
    background-color: #20AF99;
    border: 1px solid #20AF99;
    color: #fff;
    border-radius: 4px;
    cursor: pointer;
    min-width: 104px;
    height: 32px;
    font-size: 14px;
    margin: 0 10px;
}

.delete-dialog-confirm-btn:hover{
    background-color: #2FB4A1;
}

@media (min-width: 901px){
  .whisper-container{
    --message-shell-left: var(--page-pad);
    --message-aside-width: 180px;
    position: fixed;
    top: 76px;
    left: calc(var(--message-shell-left) + var(--message-aside-width) + 16px);
    right: var(--page-pad);
    width: auto;
    max-width: none;
    min-width: 0;
    z-index: 40;
  }
}

@media (max-width: 900px){
  .whisper-container{
    position: fixed;
    left: 12px;
    right: 12px;
    top: 136px;
    width: auto;
    min-width: 0;
    max-width: none;
    z-index: 40;

    .body{
      height: calc(100vh - 198px);
      min-height: 420px;
      border-radius: 16px;
    }

    .head2{
      border-radius: 16px;
    }
  }
}

@media (max-width: 640px){
  .whisper-container{
    .body{
      flex-direction: column;
      height: calc(100vh - 188px);

      .body-left{
        flex: 0 0 158px;
        min-width: 0;
        border-right: 0;
        border-bottom: 1px solid #e9eaec;
      }
    }
  }
}

</style>